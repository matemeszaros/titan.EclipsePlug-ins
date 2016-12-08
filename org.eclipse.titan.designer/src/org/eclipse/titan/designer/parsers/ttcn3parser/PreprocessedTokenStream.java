/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.editors.EditorTracker;
import org.eclipse.titan.designer.editors.ISemanticTITANEditor;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.preprocess.PreprocessorDirective;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.common.parsers.TitanListener;
import org.eclipse.titan.common.product.ProductConstants;
/**
 * Directive types for state machine transitions
 * 
 */
enum ConditionalTransition {
	ELIF, ELSE, ENDIF;
}

/**
 * Conditionals state machine
 */
enum ConditionalState {
	BEGIN("#if") {
		@Override
		ConditionalState transition(ConditionalTransition transition) {
			switch (transition) {
			case ELIF:
				return ELIF;
			case ELSE:
				return ELSE;
			case ENDIF:
				return END;
			default:
				return null;
			}
		}
	},
	ELIF("#elif") {
		@Override
		ConditionalState transition(ConditionalTransition transition) {
			switch (transition) {
			case ELIF:
				return ELIF;
			case ELSE:
				return ELSE;
			case ENDIF:
				return END;
			default:
				return null;
			}
		}
	},
	ELSE("#else") {
		@Override
		ConditionalState transition(ConditionalTransition transition) {
			switch (transition) {
			case ENDIF:
				return END;
			default:
				return null;
			}
		}
	},
	END("#endif") {
		@Override
		ConditionalState transition(ConditionalTransition transition) {
			return null;
		}
	};
	String name;

	ConditionalState(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Given the actual state and the incoming directive return the
	 * resulting state
	 * 
	 * @param transition
	 *                the incoming directive
	 * @return resulting state or null if invalid state transition
	 */
	abstract ConditionalState transition(ConditionalTransition transition);
}

class ConditionalStateMachine {
	PreprocessorDirective beginDirective;
	ConditionalState state;
	// true if act_cond was ever true previously
	boolean prevCond;
	// true if actual condition is true
	boolean actCond;

	/**
	 * Creates a new state for for the #IF stuff, the evaluated conditional
	 * is true or false
	 * 
	 * @param if_condition
	 *                the evaluated condition of the initial #IF part of the
	 *                construct
	 */
	public ConditionalStateMachine(PreprocessorDirective beginDirective) {
		this.beginDirective = beginDirective;
		state = ConditionalState.BEGIN;
		prevCond = false;
		actCond = beginDirective.type == PreprocessorDirective.Directive_type.IFNDEF ? !beginDirective.condition : beginDirective.condition;
	}

	public void transition(PreprocessorDirective ppDirective, List<TITANMarker> errors) {
		ConditionalTransition transition;
		boolean newCond;
		switch (ppDirective.type) {
		case ELIF:
			transition = ConditionalTransition.ELIF;
			newCond = ppDirective.condition;
			break;
		case ELSE:
			transition = ConditionalTransition.ELSE;
			newCond = true;
			break;
		case ENDIF:
			transition = ConditionalTransition.ENDIF;
			newCond = true;
			break;
		default:
			ErrorReporter.INTERNAL_ERROR();
			return;
		}
		ConditionalState newState = state.transition(transition);
		if (newState == null) {
			// invalid transition was requested
			TITANMarker marker = new TITANMarker(MessageFormat.format("Directive {0} after {1} is not a valid preprocessor conditional",
					ppDirective.type.getName(), state.getName()), ppDirective.line, -1, -1, IMarker.SEVERITY_ERROR,
					IMarker.PRIORITY_NORMAL);
			errors.add(marker);
			return;
		}
		// execute transition
		state = newState;
		if (actCond) {
			prevCond = true;
		}
		actCond = newCond;
	}

	/**
	 * Returns true if this is not a filtering state, this is a passing
	 * state if no previous states were passing states.
	 * 
	 * @return true if tokens can be passed on
	 */
	public boolean isPassing() {
		return actCond && !prevCond;
	}

	public boolean hasEnded() {
		return state == ConditionalState.END;
	}
}

class ConditionalStateStack {
	Stack<ConditionalStateMachine> stateStack = new Stack<ConditionalStateMachine>();
	List<TITANMarker> unsupportedConstructs;

	public ConditionalStateStack(List<TITANMarker> unsupportedConstructs) {
		this.unsupportedConstructs = unsupportedConstructs;
	}

	/**
	 * Changes the state of the stack according to the directive. Creates
	 * error/warning markers if the directive is invalid in this state.
	 * 
	 * @param ppDirective
	 *                the directive parsed
	 */
	public void processDirective(PreprocessorDirective ppDirective) {
		switch (ppDirective.type) {
		case IF:
		case IFDEF:
		case IFNDEF: {
			ConditionalStateMachine csm = new ConditionalStateMachine(ppDirective);
			stateStack.add(csm);
		}
			break;
		case ELIF:
		case ELSE:
		case ENDIF: {
			if (stateStack.isEmpty()) {
				TITANMarker marker = new TITANMarker(MessageFormat.format(
						"Directive {0} without corresponding #if/#ifdef/#ifndef directive", ppDirective.type.getName()),
						ppDirective.line, -1, -1, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL);
				unsupportedConstructs.add(marker);
			} else {
				ConditionalStateMachine topState = stateStack.peek();
				topState.transition(ppDirective, unsupportedConstructs);
				if (topState.hasEnded()) {
					stateStack.pop();
				}
			}
		}
			break;
		default:
			ErrorReporter.INTERNAL_ERROR();
		}
	}

	/**
	 * Check if the tokens can be passed on or must be filtered out from the
	 * stream
	 * 
	 * @return true to not filter
	 */
	public boolean isPassing() {
		for (ConditionalStateMachine csm : stateStack) {
			if (!csm.isPassing()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if at the EOF the state stack is empty, otherwise creates
	 * error marker(s).
	 */
	public void eofCheck() {
		for (ConditionalStateMachine csm : stateStack) {
			TITANMarker marker = new TITANMarker(MessageFormat.format("{0} directive was not terminated",
					csm.beginDirective.type.getName()), csm.beginDirective.line, -1, -1, IMarker.SEVERITY_ERROR,
					IMarker.PRIORITY_NORMAL);
			unsupportedConstructs.add(marker);
		}
	}
}

/**
 * Helper class to store data related to lexers in lexer stack
 */
class TokenStreamData extends CommonTokenStream {
	public IFile file;
	public Ttcn3Lexer lexer;
	public Reader reader;

	public TokenStreamData(Ttcn3Lexer source, IFile file, Reader reader) {
		super (source);
		this.file = file;
		this.lexer = source;
		this.reader = reader;
	}
}

public class PreprocessedTokenStream extends CommonTokenStream {
	private static final int RECURSION_LIMIT = 20;
	
	IFile actualFile;
	Ttcn3Lexer actualLexer;
	Ttcn3Parser parser;
	ConditionalStateStack condStateStack;
	// global, non-recursive macros (symbols)
	Map<String, String> macros = new Hashtable<String, String>();
	// #include files
	Stack<TokenStreamData> tokenStreamStack = new Stack<TokenStreamData>();

	Set<IFile> includedFiles = new HashSet<IFile>();
	List<Location> inactiveCodeLocations = new ArrayList<Location>();
	Location lastPPDirectiveLocation = null;
	private TitanListener lexerListener = null;
	private TitanListener parserListener = null;

	private List<SyntacticErrorStorage> errorsStored = new ArrayList<SyntacticErrorStorage>();
	private List<TITANMarker> warnings = new ArrayList<TITANMarker>();
	private List<TITANMarker> unsupportedConstructs = new ArrayList<TITANMarker>();

	public Set<IFile> getIncludedFiles() {
		return includedFiles;
	}

	public List<Location> getInactiveCodeLocations() {
		return inactiveCodeLocations;
	}

	public List<SyntacticErrorStorage> getErrorStorage() {
		return errorsStored;
	}

	public void reportWarning(TITANMarker marker) {
		warnings.add(marker);
	}

	public List<TITANMarker> getWarnings() {
		return warnings;
	}

	public void reportUnsupportedConstruct(TITANMarker marker) {
		unsupportedConstructs.add(marker);
	}

	public List<TITANMarker> getUnsupportedConstructs() {
		return unsupportedConstructs;
	}

	public PreprocessedTokenStream(TokenSource tokenSource) {
		super(tokenSource);
		condStateStack = new ConditionalStateStack(unsupportedConstructs);
	}

	public void setActualFile(IFile file) {
		actualFile = file;
	}

	public void setActualLexer(Ttcn3Lexer lexer) {
		actualLexer = lexer;
	}

	public void setParser(Ttcn3Parser parser) {
		this.parser = parser;
	}

	public void setMacros(String[] definedList) {
		for (String s : definedList) {
			macros.put(s, "");
		}
	}

	/**
	 * Adds a new lexer to the lexer stack to read tokens from the included
	 * file
	 * 
	 * @param fileName
	 *                the file name paramtere of the #include directive
	 */
	private void processIncludeDirective(PreprocessorDirective ppDirective) {
		if (ppDirective.str == null || "".equals(ppDirective.str)) {
			TITANMarker marker = new TITANMarker("File name was not provided", ppDirective.line, -1, -1, IMarker.SEVERITY_ERROR,
					IMarker.PRIORITY_NORMAL);
			unsupportedConstructs.add(marker);
			return;
		}
		IFile includedFile = GlobalParser.getProjectSourceParser(actualFile.getProject()).getTTCN3IncludeFileByName(ppDirective.str);
		if (includedFile == null) {
			TITANMarker marker = new TITANMarker(MessageFormat.format("Included file `{0}'' could not be found", ppDirective.str),
					ppDirective.line, -1, -1, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL);
			unsupportedConstructs.add(marker);
			return;
		}
		// check extension
		if (!GlobalParser.TTCNIN_EXTENSION.equals(includedFile.getFileExtension())) {
			TITANMarker marker = new TITANMarker(MessageFormat.format("File `{0}'' does not have the `{1}'' extension", ppDirective.str,
					GlobalParser.TTCNIN_EXTENSION), ppDirective.line, -1, -1, IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL);
			warnings.add(marker);
		}
		// check if the file is already loaded into an editor
		String code = null;
		if (EditorTracker.containsKey(includedFile)) {
			List<ISemanticTITANEditor> editors = EditorTracker.getEditor(includedFile);
			ISemanticTITANEditor editor = editors.get(0);
			IDocument document = editor.getDocument();
			code = document.get();
		}
		// create lexer and set it up
		Reader reader = null;
		CharStream charStream = null;
		Ttcn3Lexer lexer = null;
		int rootInt;
		if (code != null) {
			reader = new StringReader(code);
			charStream = new UnbufferedCharStream(reader);
			lexer = new Ttcn3Lexer(charStream);
			lexer.setTokenFactory( new CommonTokenFactory( true ) );
			rootInt = code.length();
		} else {
			try {
				InputStreamReader temp = new InputStreamReader(includedFile.getContents());
				if (!includedFile.getCharset().equals(temp.getEncoding())) {
					try {
						temp.close();
					} catch (IOException e) {
						ErrorReporter.logWarningExceptionStackTrace(e);
					}
					temp = new InputStreamReader(includedFile.getContents(), includedFile.getCharset());
				}

				reader = new BufferedReader(temp);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
				return;
			} catch (UnsupportedEncodingException e) {
				ErrorReporter.logExceptionStackTrace(e);
				return;
			}
			charStream = new UnbufferedCharStream(reader);
			lexer = new Ttcn3Lexer(charStream);
			lexer.setTokenFactory( new CommonTokenFactory( true ) );
			lexerListener = new TitanListener();
			lexer.removeErrorListeners(); // remove ConsoleErrorListener
			lexer.addErrorListener(lexerListener);

			IFileStore store;
			try {
				store = EFS.getStore(includedFile.getLocationURI());
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
				return;
			}
			IFileInfo fileInfo = store.fetchInfo();
			rootInt = (int) fileInfo.getLength();
		}
		lexer.setTokenFactory(new CommonTokenFactory(true));
		lexer.setTTCNPP();
		lexer.initRootInterval(rootInt);
		lexer.setActualFile(includedFile);
		// add the lexer to the stack of lexers
		tokenStreamStack.push(new TokenStreamData(lexer, includedFile, reader));
		if (parser != null) {
			parser.setActualFile(includedFile);
		}
		includedFiles.add(includedFile);
	}

	@Override
	public int fetch(int n){
		if (fetchedEOF) {
			return 0;
		}
		int i = 0;
		do {
			Token t;
			if (tokenStreamStack.isEmpty()) {
				t = getTokenSource().nextToken();
			} else {
				t = tokenStreamStack.peek().getTokenSource().nextToken();
			}
			if (t == null) {
				return 0;
			}
			int tokenType = t.getType();
			if (tokenType == Ttcn3Lexer.PREPROCESSOR_DIRECTIVE) {
				lastPPDirectiveLocation = new Location(actualFile, t.getLine(), t.getStartIndex(), t.getStopIndex() + 1);
				// 1. the first # shall be discarded
				// 2. "\\\n" strings are removed, so multiline tokens, which are split by backslash are extracted to one line
				final String text = t.getText().substring(1).replace("\\\n", "");
				Reader reader = new StringReader( text );
				CharStream charStream = new UnbufferedCharStream(reader);
				PreprocessorDirectiveLexer lexer = new PreprocessorDirectiveLexer(charStream);
				lexer.setTokenFactory(new PPDirectiveTokenFactory(true, t));
				lexerListener = new PPListener();
				lexer.removeErrorListeners();
				lexer.addErrorListener(lexerListener);
				lexer.setLine(t.getLine());
				lexer.setCharPositionInLine(t.getCharPositionInLine());

				// 1. Previously it was UnbufferedTokenStream(lexer), but it was changed to BufferedTokenStream, because UnbufferedTokenStream seems to be unusable. It is an ANTLR 4 bug.
				// Read this: https://groups.google.com/forum/#!topic/antlr-discussion/gsAu-6d3pKU
				// pr_PatternChunk[StringBuilder builder, boolean[] uni]:
				//   $builder.append($v.text); <-- exception is thrown here: java.lang.UnsupportedOperationException: interval 85..85 not in token buffer window: 86..341
				// 2. Changed from BufferedTokenStream to CommonTokenStream, otherwise tokens with "-> channel(HIDDEN)" are not filtered out in lexer.
				final CommonTokenStream tokenStream = new CommonTokenStream( lexer );

				PreprocessorDirectiveParser localParser = new PreprocessorDirectiveParser( tokenStream );
				localParser.setBuildParseTree(false);
				parserListener = new PPListener(localParser);
				localParser.removeErrorListeners();
				localParser.addErrorListener(parserListener);
				localParser.setIsActiveCode(condStateStack.isPassing());
				localParser.setMacros(macros);
				localParser.setLine(t.getLine());
				PreprocessorDirective ppDirective = null;
				ppDirective = localParser.pr_Directive().ppDirective;
				errorsStored.addAll(localParser.getErrorStorage());
				warnings.addAll(localParser.getWarnings());
				unsupportedConstructs.addAll(localParser.getUnsupportedConstructs());
				if (ppDirective != null) {
					ppDirective.line = t.getLine();
					if (ppDirective.isConditional()) {
						boolean preIsPassing = condStateStack.isPassing();
						condStateStack.processDirective(ppDirective);
						boolean postIsPassing = condStateStack.isPassing();
						if (preIsPassing != postIsPassing && tokenStreamStack.isEmpty() && getTokenSource() instanceof Ttcn3Lexer) { 
							// included files are ignored because of ambiguity
							Location ppLocation = lastPPDirectiveLocation;
							if (ppLocation != null) {
								if (preIsPassing) {
									// switched to inactive: begin a new inactive location
									Location loc = new Location(actualFile, ppLocation.getLine(),
											ppLocation.getEndOffset(), ppLocation.getEndOffset());
									inactiveCodeLocations.add(loc);
								} else {
									// switched to active: end the current inactive location
									int iclSize = inactiveCodeLocations.size();
									if (iclSize > 0) {
										Location lastLocation = inactiveCodeLocations.get(iclSize - 1);
										lastLocation.setEndOffset(ppLocation.getOffset());
									}
								}
							}
						}
					} else {
						// other directive types
						if (condStateStack.isPassing()) {
							// do something with the
							// directive
							switch (ppDirective.type) {
							case INCLUDE: {
								if (tokenStreamStack.size() > RECURSION_LIMIT) {
									// dumb but safe defense against infinite recursion, default value from gcc
									TITANMarker marker = new TITANMarker(
											"Maximum #include recursion depth reached", ppDirective.line,
											-1, -1, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL);
									unsupportedConstructs.add(marker);
								} else {
								//TODO: Makes the Eclipse slow down
									processIncludeDirective(ppDirective);
								}
							}
								break;
							case ERROR: {
								String errorMessage = ppDirective.str == null ? "" : ppDirective.str;
								TITANMarker marker = new TITANMarker(errorMessage, ppDirective.line, -1, -1,
										IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL);
								unsupportedConstructs.add(marker);
							}
								break;
							case WARNING: {
								String warningMessage = ppDirective.str == null ? "" : ppDirective.str;
								TITANMarker marker = new TITANMarker(warningMessage, ppDirective.line, -1, -1,
										IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL);
								warnings.add(marker);
							}
								break;
							case LINECONTROL:
							case LINEMARKER:
							case PRAGMA:
							case NULL: {
								String reportPreference = Platform.getPreferencesService().getString(
										ProductConstants.PRODUCT_ID_DESIGNER,
										PreferenceConstants.REPORT_IGNORED_PREPROCESSOR_DIRECTIVES,
										GeneralConstants.WARNING, null);
								if (!GeneralConstants.IGNORE.equals(reportPreference)) {
									boolean isError = GeneralConstants.ERROR.equals(reportPreference);
									TITANMarker marker = new TITANMarker(MessageFormat.format(
											"Preprocessor directive {0} is ignored",
											ppDirective.type.getName()), ppDirective.line, -1, -1,
											isError ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING,
											IMarker.PRIORITY_NORMAL);
									if (isError) {
										unsupportedConstructs.add(marker);
									} else {
										warnings.add(marker);
									}
								}
							}
								break;
							default:
								// ignore
							}
						}
					}
				}
			} else if (tokenType == Token.EOF) {
				if (!tokenStreamStack.isEmpty()) {
					// the included file ended, drop lexer
					// from the stack and ignore EOF token
					TokenStreamData tsd = tokenStreamStack.pop();
					if (parser != null) {
						if (tokenStreamStack.isEmpty()) {
							parser.setActualFile(actualFile);
						} else {
							parser.setActualFile(tokenStreamStack.peek().file);
						}
					}
					if (tsd.reader != null) {
						try {
							tsd.reader.close();
						} catch (IOException e) {
						}
					}
				} else {
					fetchedEOF = true;
					condStateStack.eofCheck();
					tokens.add(t);
					((CommonToken) t).setTokenIndex(tokens.size()-1);
					--n;
					++i;
					if (n == 0) {
						return i;
					}
				}
			} else {
				if (condStateStack.isPassing()) {
					tokens.add(t);
					((CommonToken) t).setTokenIndex(tokens.size()-1);
					--n;
					++i;
					if (n == 0) {
						return i;
					}
				}
			}
		} while (true);
	}
	
}
