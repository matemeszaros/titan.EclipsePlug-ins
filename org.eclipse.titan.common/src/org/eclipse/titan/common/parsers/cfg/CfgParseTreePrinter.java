/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.eclipse.core.runtime.Path;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.AddedParseTree;
import org.eclipse.titan.common.path.PathConverter;

/**
 * Utility class to print a CFG parse tree with the following features:
 * <ul>
 *   <li> Print parse tree with or without resolving<br>
 *      Resolving means:
 *      <ul>
 *        <li> macros are changed to its actual values
 *        <li> include file name is changed to the content of the included config file (if it's not included yet)
 *      </ul>
 *   <li> Print parse tree with or without the hidden tokens before the parse tree.
 *        Hidden tokens inside the parse tree are always printed.
 * </ul>
 * @author Arpad Lovassy
 */
public class CfgParseTreePrinter {
	
	/** header for resolved included file contents in generated temp file */
	private static final String INCLUDED_BEGIN = "//This part was originally found in file: ";
	
	/** last line of resolved included file contents in generated temp file */
	private static final String INCLUDED_END = "//End of file: ";

	/** pattern for matching macro string, for example: \$a, \${a} */
	private static final Pattern PATTERN_MACRO = Pattern.compile("\\$\\s*\\{?\\s*([A-Za-z][A-Za-z0-9_]*)\\s*\\}?");

	/** pattern for matching typed macro string, for example: ${a, float} */
	private static final Pattern PATTERN_TYPED_MACRO = Pattern.compile("\\$\\s*\\{\\s*([A-Za-z][A-Za-z0-9_]*)\\s*,\\s*[A-Za-z][A-Za-z0-9_]*\\s*\\}");
	
	/**
	 * Mode of resolving <br>
	 * Resolving means:
	 * <ul>
	 *   <li> macros are changed to its actual values
	 *   <li> include file name is changed to the content of the included config file (if it's not included yet)
	 * </ul>
	 * @author Arpad Lovassy
	 */
	public enum ResolveMode {
		
		/** default */
		NO_RESOLVING,
		
		/** included file contents are copied after the main file after each other (used by [INCLUDE] section) */
		IN_ROW,
		
		/** included file content is copied in the place if the included file name (used by [ORDERED_INCLUDE] section) */
		NESTED
	}
	
	/**
	 * StringBuilder, where the text is written
	 */
	final StringBuilder mSb;

	/**
	 * token types, which are not printed (also their children).
	 * it can be null
	 */
	final List<Integer> mDisallowedNodes;

	/**
	 * original parse tree roots to resolve.
	 * it can be null, if no resolving is done
	 */
	final Map<Path, CfgParseResult> mCfgParseResults;

	/**
	 * macro definitions, which are collected during parsing from [DEFINE] sections.
	 * it can be null, if no resolving is done
	 */
	final Map<String, CfgDefinitionInformation> mDefinitions;
	
	/**
	 * environment variables.
	 * it can be null, if no resolving is done
	 */
	final Map<String, String> mEnvVariables;
	
	/**
	 * list of files to resolve, files, which are already resolved are removed from this list.
	 * it can be null, if no resolving is done
	 */
	final List<Path> mFilesToResolve;
	
	/**
	 * Constructor with all the functionalities
	 * @param aSb (in/out) StringBuilder, where the text is written
	 * @param aDisallowedNodes token types, which are not printed (also their children), it can be null
	 * @param aCfgParseResults original parse tree roots to resolve
	 *                        needed only if aResolveMode != NO_RESOLVING, otherwise it can be null
	 * @param aDefinitions macro definitions, which are collected during parsing from [DEFINE] sections
	 *                        needed only if aResolveMode != NO_RESOLVING, otherwise it can be null
	 * @param aEnvVariables environment variables
	 *                        needed only if aResolveMode != NO_RESOLVING, otherwise it can be null
	 * @param aFilesToResolve list of files to resolve, files, which are already resolved are removed from this list
	 *                        needed only if aResolveMode != NO_RESOLVING, otherwise it can be null
	 */
	public CfgParseTreePrinter( final StringBuilder aSb,
								final List<Integer> aDisallowedNodes,
								final Map<Path, CfgParseResult> aCfgParseResults,
								final Map<String, CfgDefinitionInformation> aDefinitions,
								final Map<String, String> aEnvVariables,
								final List<Path> aFilesToResolve ) {
		mSb = aSb;
		mDisallowedNodes = aDisallowedNodes;
		mCfgParseResults = aCfgParseResults;
		mDefinitions = aDefinitions;
		mEnvVariables = aEnvVariables;
		mFilesToResolve = aFilesToResolve;
	}
	
	/**
	 * Constructor if no resolving is needed
	 * @param aSb (in/out) StringBuilder, where the text is written
	 * @param aDisallowedNodes token types, which are not printed (also their children), it can be null
	 */
	public CfgParseTreePrinter( final StringBuilder aSb,
								final List<Integer> aDisallowedNodes ) {
		this( aSb, aDisallowedNodes, null, null, null, null );
	}
	
	/**
	 * Constructor if no resolving is needed without disallowed nodes
	 * @param aSb (in/out) StringBuilder, where the text is written
	 * @param aDisallowedNodes token types, which are not printed (also their children), it can be null
	 */
	public CfgParseTreePrinter( final StringBuilder aSb ) {
		this( aSb, null );
	}
	
	/**
	 * Builds parse tree text including hidden tokens inside the parse tree
	 * @param aParseTreeRoot root of the parse tree to print
	 * @param aPrintHiddenBefore true to print hidden tokens before the parse tree
	 *                           (NOTE: hidden tokens in the parse tree will be printed)
	 * @param aTokens token list from the lexer (all, hidden and not hidden also)
	 * @return output parse tree text
	 */
	public static String toStringWithHidden( final ParseTree aParseTreeRoot,
											 final List<Token> aTokens,
											 final boolean aPrintHiddenBefore ) {
		final StringBuilder sb = new StringBuilder();
		CfgParseTreePrinter printer = new CfgParseTreePrinter( sb );
		printer.print( aParseTreeRoot, aTokens, aPrintHiddenBefore, ResolveMode.NO_RESOLVING, null );
		// there are no hidden tokens after the last token

		return sb.toString();
	}
	
	/**
	 * Builds resolved parse tree text including hidden tokens (also before the rule)<br>
	 * Resolving means:
	 * <ul>
	 *   <li> macros are changed to its actual values
	 *   <li> include file name is changed to the content of the included config file (if it's not included yet)
	 * </ul>
	 * @param aCfgParseResults original parse tree roots to resolve
	 * @param aSb (in/out) StringBuilder, where the text is written
	 * @param aDisallowedNodes token types, which are not printed (also their children), it can be null
	 * @param aResolveMode mode of resolving
	 * @param aDefinitions macro definitions, which are collected during parsing from [DEFINE] sections
	 *                        needed only if aResolveMode != NO_RESOLVING, otherwise it can be null
	 * @param aEnvVariables environment variables
	 *                        needed only if aResolveMode != NO_RESOLVING, otherwise it can be null
	 * @return output parse tree text
	 */
	public static void printResolved( final Map<Path, CfgParseResult> aCfgParseResults,
									  final StringBuilder aSb,
									  final List<Integer> aDisallowedNodes,
									  final ResolveMode aResolveMode,
									  final Map<String, CfgDefinitionInformation> aDefinitions,
									  final Map<String, String> aEnvVariables ) {
		switch ( aResolveMode ) {
			case IN_ROW:
			{
				// list of files to resolve, files, which are already resolved are removed from this list
				final List<Path> filesToResolve = new ArrayList<Path>( aCfgParseResults.keySet() );
				CfgParseTreePrinter printer = new CfgParseTreePrinter( aSb, aDisallowedNodes,
							aCfgParseResults, aDefinitions, aEnvVariables, filesToResolve );
				for ( Entry<Path, CfgParseResult> entry : aCfgParseResults.entrySet() ) {
					printer.printResolved( entry.getKey(), entry.getValue().getParseTreeRoot(),
							entry.getValue().getTokens(), aResolveMode );
				}
				break;
			}
			case NESTED:
			{
				// list of files to resolve, files, which are already resolved are removed from this list
				final List<Path> filesToResolve = new ArrayList<Path>( aCfgParseResults.keySet() );
				// run only the 1st, others will be nested in place
				Entry<Path, CfgParseResult> entry = aCfgParseResults.entrySet().iterator().next();
				CfgParseTreePrinter printer = new CfgParseTreePrinter( aSb, aDisallowedNodes,
						aCfgParseResults, aDefinitions, aEnvVariables, filesToResolve );
				printer.printResolved( entry.getKey(), entry.getValue().getParseTreeRoot(),
						entry.getValue().getTokens(), aResolveMode );
				break;
			}
			case NO_RESOLVING:
			default:
				// nothing to do
				break;
		}
	}
	
	/**
	 * Builds resolved parse tree text including hidden tokens (but not before the rule) <br>
	 * Resolving means:
	 * <ul>
	 *   <li> macros are changed to its actual values
	 *   <li> include file name is changed to the content of the included config file (if it's not included yet)
	 * </ul>
	 * @param aFile the parse tree of this file to print
	 * @param aParseTreeRoot root of the parse tree to print
	 * @param aTokens token list from the lexer (all, hidden and not hidden also)
	 * @param aResolveMode mode of resolving
	 */
	private void printResolved( final Path aFile,
								final ParseTree aParseTreeRoot,
								final List<Token> aTokens,
								final ResolveMode aResolveMode ) {
		if ( mFilesToResolve.contains( aFile ) ) {
			mSb.append( INCLUDED_BEGIN ).append( aFile.toOSString()).append('\n');
			print( aParseTreeRoot, aTokens, true, aResolveMode, aFile );
			mSb.append( INCLUDED_END ).append( aFile.toOSString()).append('\n');
			mFilesToResolve.remove( aFile );
		}
	}
	
	/**
	 * RECURSIVE
	 * Builds parse tree text including hidden tokens
	 * @param aParseTree parse tree
	 * @param aTokens token list from the lexer (all, hidden and not hidden also)
	 * @param aPrintHiddenBefore true to print hidden tokens before the parse tree
	 *                           (NOTE: hidden tokens in the parse tree will be printed)
	 * @param aResolveMode mode of resolving
	 * @param aFile the parse tree of this file to print
	 *                        needed only if aResolveMode != NO_RESOLVING, in case of [ORDERED_INCLUDE]
	 */
	private void print( final ParseTree aParseTree,
						final List<Token> aTokens,
						final boolean aPrintHiddenBefore,
						final ResolveMode aResolveMode,
						final Path aFile ) {
		if ( aParseTree == null ) {
			ErrorReporter.logWarning("ConfigTreeNodeUtilities.print(): aParseTree == null");
			return;
		}

		if ( aParseTree instanceof ParserRuleContext ) {
			final ParserRuleContext rule = (ParserRuleContext)aParseTree;
			if ( mDisallowedNodes != null && mDisallowedNodes.contains( rule.start.getType() ) ) {
				return;
			}
			if ( aPrintHiddenBefore && rule.getChildCount() > 0 && rule.getChild( 0 ) instanceof AddedParseTree ) {
				// special case: if AddedParseTree is the 1st in the rule, it has no information
				// about the hidden tokens, as it has no position in the token list, but the rule may have
				printHiddenTokensBefore( rule, aTokens );
			}
		} else if ( aParseTree instanceof TerminalNodeImpl ) {
			final TerminalNodeImpl tn = (TerminalNodeImpl)aParseTree;
			final Token token = tn.getSymbol();
			if ( mDisallowedNodes == null || !mDisallowedNodes.contains( token.getType() ) ) {
				printToken( token, aTokens, aPrintHiddenBefore, aResolveMode, aFile );
			}
		} else if ( aParseTree instanceof AddedParseTree ) {
			final AddedParseTree t = (AddedParseTree)aParseTree;
			mSb.append( t.getText() );
		} else {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.print(): unexpected ParseTree type");
		}

		for ( int i = 0; i < aParseTree.getChildCount(); i++ ) {
			ParseTree child = aParseTree.getChild( i );
			if ( child == aParseTree ) {
				ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.print(): child == aParseTree");
			} else {
				print( child, aTokens, aPrintHiddenBefore || i > 0, aResolveMode, aFile );
			}
		}
	}
	
	/**
	 * Builds token text including hidden tokens before the token
	 * @param aToken token to print
	 * @param aTokens token list from the lexer (all, hidden and not hidden also)
	 * @param aPrintHiddenBefore true to print hidden tokens before the token
	 * @param aResolveMode mode of resolving
	 * @param aFile the parse tree of this file to print
	 *                        needed only if aResolveMode != NO_RESOLVING, in case of [ORDERED_INCLUDE]
	 */
	private void printToken( final Token aToken,
							 final List<Token> aTokens,
							 final boolean aPrintHiddenBefore,
							 final ResolveMode aResolveMode,
							 final Path aFile ) {
		final int tokenIndex = aToken.getTokenIndex();
		if ( tokenIndex > -1 && aPrintHiddenBefore ) {
			// Token has no index if tokenIndex == -1.
			// If a token is added to the parse tree after parse time, token start index in unknown (-1),
			// because token has no index in the token list.
			printHiddenTokensBefore( aToken, aTokens );
		}
		
		// the only non-hidden token
		if ( aResolveMode != ResolveMode.NO_RESOLVING ) {
			resolveToken( aToken, aResolveMode, aFile );
		} else {
			final String tokenText = aToken.getText();
			mSb.append( tokenText != null ? tokenText : "" );
		}
	}

	/**
	 * Builds hidden tokens before the token
	 * @param aToken the token, this will NOT be printed
	 * @param aTokens token list from the lexer (all, hidden and not hidden also)
	 */
	private void printHiddenTokensBefore( final Token aToken,
										  final List<Token> aTokens ) {
		final int tokenIndex = aToken.getTokenIndex();
		if ( tokenIndex == -1 ) {
			// Token has no index.
			// If a token is added to the parse tree after parse time, token start index in unknown (-1),
			// because token has no index in the token list.
			return;
		}
		int startHiddenIndex = tokenIndex;
		while ( isHiddenToken( startHiddenIndex - 1, aTokens ) ) {
			startHiddenIndex--;
		}
		for ( int i = startHiddenIndex; i < tokenIndex; i++ ) {
			final Token t = aTokens.get( i );
			final String tokenText = t.getText();
			mSb.append( tokenText != null ? tokenText : "" );
		}
	}

	/**
	 * Builds hidden tokens before the rule
	 * @param aRule the rule, this will NOT be printed
	 * @param aTokens token list from the lexer (all, hidden and not hidden also)
	 */
	private void printHiddenTokensBefore( final ParserRuleContext aRule, final List<Token> aTokens ) {
		Token startToken = aRule.start;
		if ( startToken == null ) {
			return;
		}
		printHiddenTokensBefore( startToken, aTokens );
	}

	/**
	 * @param aIndex token index to check
	 * @param aTokens token list from the lexer (all, hidden and not hidden also)
	 * @return true, iff token index is valid AND token is hidden
	 */
	public static boolean isHiddenToken( final int aIndex, final List<Token> aTokens ) {
		if ( aTokens == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.isHiddenToken(): aTokens == null");
			return false;
		}
		
		return aIndex >= 0 && aIndex < aTokens.size() && aTokens.get( aIndex ).getChannel() > 0;
	}
	
	/**
	 * Resolves token if needed<br>
	 * Resolving means:
	 * <ul>
	 *   <li> macros are changed to its actual values
	 *   <li> include file name is changed to the content of the included config file (if it's not included yet)
	 * </ul>
	 * @param aToken token to resolve or print
	 * @param aResolveMode mode of resolving
	 * @param aFile the parse tree of this file to print
	 *                        needed only if aResolveMode != NO_RESOLVING, in case of [ORDERED_INCLUDE]
	 */
	private void resolveToken( final Token aToken,
							   final ResolveMode aResolveMode,
							   final Path aFile ) {
		final int tokenType = aToken.getType();
		if ( isMacro( tokenType ) ) {
			final String macroValue = getMacroValue( aToken );
			mSb.append( macroValue );
		} else if ( isTypedMacro( tokenType ) ) {
			final String macroValue = getTypedMacroValue( aToken );
			mSb.append( macroValue );
		} else if ( tokenType == CfgLexer.STRING2 ) {
			// Quoted string in [INCLUDE] section
			// this is the only non-hidden token in [INCLUDE] section
			if ( aResolveMode == ResolveMode.NESTED ) {
				resolveTokenNestedInclude( aToken, aResolveMode, aFile );
			}
			// otherwise nothing to do, included files are already collected in mParseTreeRoots
		} else if ( tokenType == CfgLexer.STRING4 ) {
			// Quoted string in [ORDERED_INCLUDE] section
			// this is the only non-hidden token in [ORDERED_INCLUDE] section
			resolveTokenNestedInclude( aToken, aResolveMode, aFile );
		} else {
			final String tokenText = aToken.getText();
			mSb.append( tokenText != null ? tokenText : "" );
		}
	}
	
	/**
	 * Include file is changed to its content
	 * @param aToken token to resolve or print
	 * @param aResolveMode mode of resolving
	 * @param aFile the parse tree of this file to print
	 *                        needed only if aResolveMode != NO_RESOLVING, in case of [ORDERED_INCLUDE]
	 */
	private void resolveTokenNestedInclude( final Token aToken,
											final ResolveMode aResolveMode,
											final Path aFile ) {
		// token text is the file name with quotes, which will be included in place
		final String tokenText = aToken.getText();
		// remove quotes
		final String filename = tokenText.replaceAll("^\"|\"$", "");
		// filename is a relative file name to aFile, we need the absolute file name,
		// because absolute file names are used as keys in aParseTreeRoots
		final Path absolutePath = getAbsolutePath( aFile, filename );
		final CfgParseResult cfgParseResult = mCfgParseResults.get( absolutePath );
		if ( cfgParseResult != null ) {
			printResolved( absolutePath, cfgParseResult.getParseTreeRoot(), cfgParseResult.getTokens(), aResolveMode );
		} else {
			// include file is missing from mCfgParseResults, so the included cfg file is not parsed
			// in ConfigFileHandler.readFromFile()
			ErrorReporter.INTERNAL_ERROR("ParseTreePrinter.resolveTokenNestedInclude(): cfgParseResult == null");
		}
	}
	
	/**
	 * Converts relative filename to absolute path
	 * @param aBaseFile the file to be used as the base of the full path
	 * @param aFilename filename to convert
	 * @return the converted absolute path of the given relative filename
	 */
	private static Path getAbsolutePath( final Path aBaseFile, final String aFilename ) {
		final String absoluteFilename = PathConverter.getAbsolutePath(aBaseFile.toOSString(), aFilename);
		if( absoluteFilename == null ) {
			return null;
		}
		return new Path( absoluteFilename );
	}

	/**
	 * @param aToken token to check
	 * @return true, if and only if token type is a macro, for example: \$a, \${a}
	 */
	private static boolean isMacro( final int aTokenType ) {
		return
			    aTokenType == CfgLexer.MACRO1
			 || aTokenType == CfgLexer.MACRO5
			 || aTokenType == CfgLexer.MACRO6
			 || aTokenType == CfgLexer.MACRO7
			 || aTokenType == CfgLexer.MACRO9
			 || aTokenType == CfgLexer.MACRO10
			 || aTokenType == CfgLexer.MACRO11
			 || aTokenType == CfgLexer.MACRO12;
	}

	/**
	 * @param aToken token to check
	 * @return true, if and only if token type is a typed macro, for example: ${a, float}
	 */
	private static boolean isTypedMacro( final int aTokenType ) {
		return
			    aTokenType == CfgLexer.MACRO_HOSTNAME1
			 || aTokenType == CfgLexer.MACRO_INT1
			 || aTokenType == CfgLexer.MACRO_EXP_CSTR1
			 || aTokenType == CfgLexer.MACRO_FLOAT1 
			 || aTokenType == CfgLexer.MACRO_ID5
			 || aTokenType == CfgLexer.MACRO_INT5
			 || aTokenType == CfgLexer.MACRO_BOOL5
			 || aTokenType == CfgLexer.MACRO_FLOAT5
			 || aTokenType == CfgLexer.MACRO_EXP_CSTR5 
			 || aTokenType == CfgLexer.MACRO_BSTR5
			 || aTokenType == CfgLexer.MACRO_HSTR5
			 || aTokenType == CfgLexer.MACRO_OSTR5
			 || aTokenType == CfgLexer.MACRO_BINARY5
			 || aTokenType == CfgLexer.MACRO_HOSTNAME5
			 || aTokenType == CfgLexer.MACRO_EXP_CSTR6
			 || aTokenType == CfgLexer.MACRO_INT7
			 || aTokenType == CfgLexer.MACRO_ID7
			 || aTokenType == CfgLexer.MACRO_EXP_CSTR7
			 || aTokenType == CfgLexer.MACRO_ID8
			 || aTokenType == CfgLexer.MACRO_ID9
			 || aTokenType == CfgLexer.MACRO_INT9
			 || aTokenType == CfgLexer.MACRO_BOOL9
			 || aTokenType == CfgLexer.MACRO_FLOAT9
			 || aTokenType == CfgLexer.MACRO_EXP_CSTR9
			 || aTokenType == CfgLexer.MACRO_BSTR9
			 || aTokenType == CfgLexer.MACRO_HSTR9
			 || aTokenType == CfgLexer.MACRO_OSTR9
			 || aTokenType == CfgLexer.MACRO_BINARY9
			 || aTokenType == CfgLexer.MACRO_ID10
			 || aTokenType == CfgLexer.MACRO_HOSTNAME10
			 || aTokenType == CfgLexer.MACRO_BOOL11
			 || aTokenType == CfgLexer.MACRO_ID11
			 || aTokenType == CfgLexer.MACRO_INT11
			 || aTokenType == CfgLexer.MACRO_EXP_CSTR11;
	}

	/**
	 * Gets the value of a macro or an environment variable
	 * @param aDefinition macro or environment variable
	 * @return macro or environment variable value, or null if there is no such definition
	 */
	private String getDefinitionValue( final String aDefinition ){
		if ( mDefinitions != null && mDefinitions.containsKey( aDefinition ) ) {
			return mDefinitions.get( aDefinition ).getValue();
		} else if ( mEnvVariables != null && mEnvVariables.containsKey( aDefinition ) ) {
			return mEnvVariables.get( aDefinition );
		} else {
			return null;
		}
	}
	
	/**
	 * Extracts macro name from macro string
	 * @param aMacroString macro string, for example: \$a, \${a}
	 * @return extracted macro name without extra characters, for example: a
	 */
	private static String getMacroName( final String aMacroString ) {
		final Matcher m = PATTERN_MACRO.matcher( aMacroString );
		if ( m.find() ) {
			return m.group(1);
		} else {
			return null;
		}
	}
	
	/**
	 * Extracts macro name from typed macro string
	 * @param aMacroString macro string, for example: \${a, float}
	 * @return extracted macro name without extra characters, for example: a
	 */
	private static String getTypedMacroName( final String aMacroString ) {
		final Matcher m = PATTERN_TYPED_MACRO.matcher( aMacroString );
		if ( m.find() ) {
			return m.group(1);
		} else {
			return null;
		}
	}
		
	/**
	 * Gets the macro value string of a macro (without type)
	 * @param aMacroToken the macro token
	 * @return the macro value string
	 *         or "" if macro is invalid. In this case an error marker is also created
	 */
	private String getMacroValue( final Token aMacroToken ) {
		final String definition = getMacroName( aMacroToken.getText() );
		final String value = getDefinitionValue( definition );
		if ( value == null ) {
			return "";
		}
		return value;
	}
	
	/**
	 * Gets the macro value string of a macro (with type)
	 * @param aMacroToken the macro token
	 * @return the macro value string
	 *         or "" if macro is invalid. In this case an error marker is also created
	 */
	private String getTypedMacroValue( Token aMacroToken ) {
		final String definition = getTypedMacroName( aMacroToken.getText() );
		final String value = getDefinitionValue( definition );
		if ( value == null ) {
			return "";
		}
		return value;
	}

}
