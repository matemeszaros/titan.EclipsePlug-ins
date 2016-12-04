/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.scope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.For_Loop_Definitions;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.statements.Definition_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.refactoring.Utils;
import org.eclipse.titanium.refactoring.scope.MinimizeScopeRefactoring.Settings;
import org.eclipse.titanium.refactoring.scope.nodes.BlockNode;
import org.eclipse.titanium.refactoring.scope.nodes.Edit;
import org.eclipse.titanium.refactoring.scope.nodes.Environment;
import org.eclipse.titanium.refactoring.scope.nodes.MultiDeclaration;
import org.eclipse.titanium.refactoring.scope.nodes.Node;
import org.eclipse.titanium.refactoring.scope.nodes.StatementNode;
import org.eclipse.titanium.refactoring.scope.nodes.Variable;

/**
 * This class is only instantiated by the {@link MinimizeScopeRefactoring} once per each refactoring operation.
 * <p>
 * By passing the selection through the constructor and calling {@link ChangeCreator#perform()}, this class
 *  creates a {@link Change} object, which can be returned by the standard
 *  {@link Refactoring#createChange(IProgressMonitor)} method in the refactoring class.
 * 
 * @author Viktor Varga
 */
public class ChangeCreator {

	//in
	private final IFile fileSelection;
	private final Definition defSelection;	//null if only whole resources are selected
	private final Settings settings;
	//out
	private Change change;

	/** Use this constructor when the change should be created for the whole file. */
	ChangeCreator(final IFile file, final Settings settings) {
		this.fileSelection = file;
		this.defSelection = null;
		this.settings = settings;
	}
	/** Use this constructor when the change should only be created for a part of the file. */
	ChangeCreator(final IFile file, final Definition selectedDef, final Settings settings) {
		this.fileSelection = file;
		this.defSelection = selectedDef;
		this.settings = settings;
	}
	
	Change getChange() {
		return change;
	}
	
	/** 
	 * Creates the {@link #change} object, which contains all the inserted and edited visibility modifiers
	 * in the selected resources.
	 * */
	void perform() {
		if (fileSelection == null) {
			return;
		}
		change = createFileChange(fileSelection);
	}
	
	/** 
	 * Creates the {@link #change} object, which contains all the inserted and edited visibility modifiers
	 * in the selected resources.
	 * */
	private Change createFileChange(final IFile toVisit) {
		if (toVisit == null) {
			return null;
		}
		ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(toVisit.getProject());
		Module module = sourceParser.containedModule(toVisit);
		if(module == null) {
			return null;
		}
		//
		//collect functions
		Set<Definition> funcs;
		FunctionCollector vis = new FunctionCollector();
		if (defSelection == null) {
			module.accept(vis);
			funcs = vis.getResult();
		} else {
			if (defSelection instanceof Def_Function ||
					defSelection instanceof Def_Testcase) {
				//TODO any other possibilities for the type of 'defSelection'?
				funcs = new HashSet<Definition>();
				funcs.add(defSelection);
			} else {
				ErrorReporter.logError("ChangeCreator.createFileChange(): " +
						"defSelection must be of type Def_Function or Def_Testcase. ");
				return null;
			}
		}
		//create edits
		List<Edit> allEdits = new ArrayList<Edit>();
		for (Definition def: funcs) {
			List<Edit> edits = analyzeFunction(def);
			if (edits == null) {
				continue;
			}
			allEdits.addAll(edits);
		}
		
		if(allEdits.isEmpty()) {
			return null;
		}
		
		String fileContents = loadFileContent(toVisit);
		//create text edits
		//
		TextFileChange tfc = new TextFileChange(toVisit.getName(), toVisit);
		MultiTextEdit rootEdit = new MultiTextEdit();
		tfc.setEdit(rootEdit);
		//TODO this is an O(n^2) algorithm
		//merge overlapping DeleteEdits
		//	used, when removing all parts of a multi-declaration statement:
		//	the DeleteEdit for removing the last part covers all the DeleteEdits for the other parts
		//WARNING merging edits might make debugging more difficult, since the overlapping edit errors are avoided
		List<TextEdit> allTes = new LinkedList<TextEdit>();
		//collect processed (insert) edits with their created insert edit
		Map<Edit, InsertEdit> editsDone = new HashMap<Edit, InsertEdit>();
		for (Edit e: allEdits) {
			TextEdit[] tes = createTextEdit(toVisit, fileContents, e, editsDone);
			for (TextEdit te: tes) {
				if (!(te instanceof DeleteEdit)) {
					allTes.add(te);
					//System.err.println("$ nonde added: " + te.getOffset() + "-" + te.getExclusiveEnd());
					continue;
				}
				DeleteEdit dte = (DeleteEdit)te;
				ListIterator<TextEdit> it = allTes.listIterator();
				while (it.hasNext()) {
					TextEdit currTe = it.next();
					if (!(currTe instanceof DeleteEdit)) {
						continue;
					}
					DeleteEdit currDte = (DeleteEdit)currTe;
					//dte: to be added, currDte: present in the list
					//if the new edit (dte) overlaps currDte, merge them
					if (doesDeleteEditsOverlap(dte, currDte)) {
						//System.err.println("$ de removed: " + currDte.getOffset() + "-" + currDte.getExclusiveEnd());
						it.remove();
						dte = mergeDeleteEdits(dte, currDte);
						//System.err.println("$ merged des: " + dte.getOffset() + "-" + dte.getExclusiveEnd());
					}
				}
				//System.err.println("$ de added: " + dte.getOffset() + "-" + dte.getExclusiveEnd());
				allTes.add(dte);
			}
		}
		Collections.reverse(allTes);
		for (TextEdit te: allTes) {
			rootEdit.addChild(te);
		}
		return tfc;
	}
	
	private TextEdit[] createTextEdit(final IFile toVisit, final String fileContent, final Edit e, final Map<Edit, InsertEdit> editsDone) {
		//check for multi-declaration statement
		if (e.declSt.isMultiDeclaration()) {
			Location cutLoc = calculateMultiDeclarationCutLoc(fileContent, e.declSt);
			String moveContent = calculateMultiDeclarationMoveContent(fileContent, e.declSt);
			//remove stmt from multi-declaration
			e.declSt.removeFromMultiDeclaration();
			//create remove edit
			int cutLen = cutLoc.getEndOffset() - cutLoc.getOffset();
			TextEdit cut = new DeleteEdit(cutLoc.getOffset(), cutLen);
			//create insert edit
			InsertEdit insert = null;
			if (!e.isRemoveEdit()) {
				//update insert location if the insertionPoint stmt was moved
				int insertOffset = ((ILocateableNode)e.insertionPoint.getAstNode()).getLocation().getOffset();
				for (Map.Entry<Edit, InsertEdit> ed: editsDone.entrySet()) {
					if (ed.getKey().declSt.equals(e.insertionPoint)) {
						insertOffset = ed.getValue().getOffset();
						break;
					}
				}
				//
				insertOffset = findLineBeginningOffset(fileContent, insertOffset);
				insert = new InsertEdit(insertOffset, moveContent);
				editsDone.put(e, insert);
			}
			if (insert != null) {
				return new TextEdit[]{insert, cut};
			}
			return new TextEdit[]{cut};
		} else {
			//
			Location cutLoc = findStatementLocation(fileContent, ((ILocateableNode)e.declSt.getAstNode()).getLocation(), true);
			InsertEdit insert = null;
			if (!e.isRemoveEdit()) {
				Location copyLoc = findStatementLocation(fileContent, ((ILocateableNode)e.declSt.getAstNode()).getLocation(), false);
				//update insert location if the insertionPoint stmt was moved
				Location insPLoc = ((ILocateableNode)e.insertionPoint.getAstNode()).getLocation();
				int insertOffset = insPLoc.getOffset();
				for (Map.Entry<Edit, InsertEdit> ed: editsDone.entrySet()) {
					if (ed.getKey().declSt.equals(e.insertionPoint)) {
						insertOffset = ed.getValue().getOffset();
						break;
					}
				}
				//
				int prefixStartOffset = findLineBeginningOffset(fileContent, insertOffset);
				String insertText = fileContent.substring(copyLoc.getOffset(), copyLoc.getEndOffset()) + "\n";
				String insertPrefix = fileContent.substring(prefixStartOffset, insertOffset);
				//if prefix is not whitespace only, do not use the prefix
				if (!insertPrefix.trim().equals("")) {
					insertPrefix = "";
				}
				insert = new InsertEdit(prefixStartOffset, insertPrefix + insertText);
				editsDone.put(e, insert);
			}
			int cutLen = cutLoc.getEndOffset() - cutLoc.getOffset();
			TextEdit cut = new DeleteEdit(cutLoc.getOffset(), cutLen);
			//System.err.println("DeleteEdit: " + fileContent.substring(cutLoc.getOffset(), cutLoc.getEndOffset()));
			if (insert != null) {
				return new TextEdit[]{insert, cut};
			}
			return new TextEdit[]{cut};
		}
	}

	private DeleteEdit mergeDeleteEdits(final DeleteEdit de0, final DeleteEdit de1) {
		if (!doesDeleteEditsOverlap(de0, de1)) {
			ErrorReporter.logError("ChangeCreator.mergeDeleteEdits(): " +
					"DeleteEdits are not overlapping! ");
			return null;
		}
		int offset = Math.min(de0.getOffset(), de1.getOffset());
		int endOffset = Math.max(de0.getExclusiveEnd(), de1.getExclusiveEnd());
		return new DeleteEdit(offset, endOffset-offset);
	}
	private boolean doesDeleteEditsOverlap(final DeleteEdit de0, final DeleteEdit de1) {
		return (de0.getOffset() < de1.getExclusiveEnd() &&
				de0.getExclusiveEnd() > de1.getOffset());
	}
	
	/**
	 * Returns the exact location of a statement including the prefix whitespace and the suffix semicolon and whitespace (and comment)
	 * */
	private Location findStatementLocation(final String fileContent, final Location loc, final boolean includePrefix) {
		int offset = loc.getOffset();
		int endOffset = loc.getEndOffset();
		if (includePrefix) {
			filestart: {
				for (int i=offset-1;i>=0;i--) {
					switch (fileContent.charAt(i)) {
						case ' ':
						case '\t':
							break;
						default:
							offset = i+1;
							break filestart;
					}
				}
				//reached position #0
				offset = 0;
			}
		}
		boolean comment = false;
		fileend: {
			for (int i=endOffset;i<fileContent.length();i++) {
				switch (fileContent.charAt(i)) {
					case '\n':
					case '\r':
						endOffset = i;
						break fileend;
					case '/':
						if (fileContent.length() > i+1 && fileContent.charAt(i+1) == '/') {
							comment = true;
							i++;
						} else if (!comment) {
							endOffset = i;
							break fileend;
						}
						break;
					case ' ':
					case '\t':
					case ';':
						break;
					default:
						if (!comment) {
							endOffset = i;
							break fileend;
						}
				}
			}
			//reached eof
			endOffset = fileContent.length();
		}
		return new Location(loc.getFile(), loc.getLine(), offset, endOffset);
	}
	/**
	 * Returns the offset of the beginning of whitespace in front of a statement starting in the specified 'fromOffset'.
	 * */
	private int findLineBeginningOffset(final String fileContent, final int fromOffset) {
		for (int i=fromOffset-1;i>=0;i--) {
			switch (fileContent.charAt(i)) {
				case ' ':
				case '\t':
					break;
				default:
					return i+1;
			}
		}
		return 0;
	}
	
	/**
	 * Returns the {@link Location} of the {@DeleteEdit} to remove a variable from a declaration list
	 * */
	private Location calculateMultiDeclarationCutLoc(final String fileContent, final StatementNode declStNode) {
		/*
		 * rules for removing multideclaration parts:
		 * 	if part is only one left: remove statement
		 * 	if part is first: remove trailing comma
		 * 	if part is last: remove leading comma
		 * 	if part is intermediate: remove trailing comma
		 * */
		MultiDeclaration md = declStNode.getMultiDeclaration();
		StatementNode firstDeclPart = md.getFirstStatement();
		Definition defVarToMove = (Definition)declStNode.getDeclaredVar().getAssmnt();
		Definition_Statement declStmt = (Definition_Statement)declStNode.getAstNode();
		boolean firstDefInMdMoved = firstDeclPart.isMoved();
		Location declStmtLoc = declStmt.getLocation();
		String stmtContent = fileContent.substring(declStmtLoc.getOffset(), declStmtLoc.getEndOffset());
		if (!stmtContent.contains(",")) {
			ErrorReporter.logError("ChangeCreator.calculateMultiDeclarationCutLoc(): Given statement" +
					" is not a multi-declaration statement; loc: " + declStmtLoc.getOffset() + "-" +
					declStmtLoc.getEndOffset() + " in file " + declStmtLoc.getFile());
			return null;
		}
		//
		if (md.getSize() <= 1) {
			Location cutLoc = findStatementLocation(fileContent, declStmt.getLocation(), true);
			//System.err.println("mdcutloc <= 1 -->>>" + fileContent.substring(cutLoc.getOffset(), cutLoc.getEndOffset()) + "<<<");
			return cutLoc;
		}
		//
		int cutOffset = defVarToMove.getLocation().getOffset();
		int cutEndOffset = defVarToMove.getLocation().getEndOffset();
		if (md.isFirstStatement(declStNode)) {
			//fist var
			if (!md.isAllStatementsMoved()) {
				cutOffset = defVarToMove.getIdentifier().getLocation().getOffset();
			}
			cutEndOffset = calculateEndOffsetIncludingTrailingComma(fileContent, cutEndOffset, declStmtLoc.getEndOffset());
		} else if (md.isLastStatement(declStNode)) {
			//last var
			cutOffset = calculateOffsetIncludingLeadingComma(fileContent, cutOffset, declStmtLoc.getOffset());
		} else {
			//intermediate var
			cutEndOffset = calculateEndOffsetIncludingTrailingComma(fileContent, cutEndOffset, declStmtLoc.getEndOffset());
		}
		//System.err.println("mdcutloc -->>>" + fileContent.substring(cutOffset, cutEndOffset) + "<<<");
		return new Location(declStmtLoc.getFile(), declStmtLoc.getLine(), cutOffset, cutEndOffset);
	}
	
	private int calculateOffsetIncludingLeadingComma(final String fileContent, int offset, final int stopAtOffset) {
		boolean insideBlockComment = false;
		while (offset>stopAtOffset) {
			switch (fileContent.charAt(offset-1)) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
				case ',':
					offset--;
					continue;
				case '/':
					if (offset > 1 && fileContent.charAt(offset-2) == '*') {
						insideBlockComment = true;
						offset -= 2;
						continue;
					} else if (insideBlockComment) {
						offset--;
						continue;
					}
					break;
				case '*':
					if (insideBlockComment && offset > 1 && fileContent.charAt(offset-2) == '/') {
						insideBlockComment = false;
						offset -= 2;
						continue;
					} else if (insideBlockComment) {
						offset--;
						continue;
					}
					break;
				default:
					if (insideBlockComment) {
						offset--;
						continue;
					}
			}
			break;
		}
		return offset;
	}
	private int calculateEndOffsetIncludingTrailingComma(final String fileContent, int endOffset, final int stopAtEndOffset) {
		boolean insideBlockComment = false;
		while (endOffset<stopAtEndOffset) {
			switch (fileContent.charAt(endOffset)) {
			case ' ':
			case '\t':
			case '\n':
			case '\r':
			case ',':
				endOffset++;
				continue;
			case '/':
				if (endOffset < fileContent.length()-2 && fileContent.charAt(endOffset+1) == '*') {
					insideBlockComment = true;
					endOffset += 2;
					continue;
				} else if (insideBlockComment) {
					endOffset++;
					continue;
				}
				break;
			case '*':
				if (insideBlockComment && endOffset < fileContent.length()-2 && fileContent.charAt(endOffset+1) == '/') {
					insideBlockComment = false;
					endOffset += 2;
					continue;
				} else if (insideBlockComment) {
					endOffset++;
					continue;
				}
				break;
			default:
				if (insideBlockComment) {
					endOffset++;
					continue;
				}
			}
			break;
		}
		return endOffset;
	}
	
	/**
	 * Returns the content of an {@InsertEdit} to move a variable from a declaration list
	 * */
	private String calculateMultiDeclarationMoveContent(final String fileContent, final StatementNode declStNode) {
		MultiDeclaration md = declStNode.getMultiDeclaration();
		StatementNode firstDeclPart = md.getFirstStatement();
		Definition firstDefInStmt = (Definition)firstDeclPart.getDeclaredVar().getAssmnt();
		Definition defVarToMove = (Definition)declStNode.getDeclaredVar().getAssmnt();
		Definition_Statement declStmt = (Definition_Statement)declStNode.getAstNode();
		Location declStmtLoc = declStmt.getLocation();
		String stmtContent = fileContent.substring(declStmtLoc.getOffset(), declStmtLoc.getEndOffset());
		if (!stmtContent.contains(",")) {
			ErrorReporter.logError("ChangeCreator.calculateMultiDeclarationMoveContent(): Given statement" +
					" is not a multi-declaration statement; loc: " + declStmtLoc.getOffset() + "-" +
					declStmtLoc.getEndOffset() + " in file " + declStmtLoc.getFile());
			return null;
		}
		int prefixOffset;
		int prefixEndOffset;
		if (firstDefInStmt.equals(defVarToMove)) {
			//first var to move
			prefixOffset = findLineBeginningOffset(fileContent, declStmtLoc.getOffset());
			prefixEndOffset = declStmtLoc.getOffset();
		} else {
			//not first var to move
			prefixOffset = findLineBeginningOffset(fileContent, declStmtLoc.getOffset());
			prefixEndOffset = firstDefInStmt.getIdentifier().getLocation().getOffset();
		}
		String prefixContent = fileContent.substring(prefixOffset, prefixEndOffset);
		//
		int varOffset = defVarToMove.getLocation().getOffset();
		int varEndOffset = defVarToMove.getLocation().getEndOffset();
		String varContent = fileContent.substring(varOffset, varEndOffset);
		String suffixContent = "\n";
		if (varContent.charAt(varContent.length()-1) != ';') {
			suffixContent = ";" + suffixContent;
		}
		//remove newlines from varContent
		prefixContent = prefixContent.replaceAll("[\n\r]", " ");
		varContent = varContent.replaceAll("[\n\r]", " ");
		//System.err.println("mdcopyloc -->>>" + prefixContent + "<>" + varContent + "<>" + suffixContent + "<<<");
		return prefixContent + varContent + suffixContent;
	}
	
	/**
	 * Analyze a function or testcase
	 * */
	private List<Edit> analyzeFunction(final Definition def) {
		if (!(def instanceof Def_Function ||
				def instanceof Def_Testcase)) {
			ErrorReporter.logError("ChangeCreator.analyzeFunction(): def must be a Def_Function or a Def_Testcase! def type: " + def.getClass());
			return null;
		}
		FunctionAnalyzer vis = new FunctionAnalyzer();
		def.accept(vis);
		Environment env = vis.getResult();
		List<Edit> eds = env.refactor();
		return eds;
	}
	
	/**
	 * Analyzes a function or testcase
	 * */
	private class FunctionAnalyzer extends ASTVisitor {

		private Environment env = new Environment(settings);
		
		private LinkedList<Node> currStack = new LinkedList<Node>();
		
		private IVisitableNode suspendStackBuildingForNode;	// if not null: until this node is not left, the stack is not modified
		private IVisitableNode suspendDeclarationsForNode;	// if not null: until this node is not left, no declarations are recorded
		private IVisitableNode suspendReferencesForNode;	// if not null: until this node is not left, no references are recorded
		
		public Environment getResult() {
			return env;
		}
		
		private void setSuspendStackBuildingForNode(final IVisitableNode node) {
			if (suspendStackBuildingForNode == null) {
				suspendStackBuildingForNode = node;
			}
		}
		private void setSuspendDeclarationsForNode(final IVisitableNode node) {
			if (suspendDeclarationsForNode == null) {
				suspendDeclarationsForNode = node;
			}
		}
		private void setSuspendReferencesForNode(final IVisitableNode node) {
			if (suspendReferencesForNode == null) {
				suspendReferencesForNode = node;
			}
		}
		private void checkForUnsuspend(final IVisitableNode node) {
			if (node == suspendStackBuildingForNode) {
				suspendStackBuildingForNode = null;
			}
			if (node == suspendDeclarationsForNode) {
				suspendDeclarationsForNode = null;
			}
			if (node == suspendReferencesForNode) {
				suspendReferencesForNode = null;
			}
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof For_Loop_Definitions && suspendDeclarationsForNode == null) {
				setSuspendDeclarationsForNode(node);
				//System.err.println("*** suspended decl when entering: " + node);
				return V_CONTINUE;
			}
			if (node instanceof Statement && suspendStackBuildingForNode == null) {
				StatementNode sn = new StatementNode(node);
				if (currStack.peek() instanceof BlockNode) {
					BlockNode parent = (BlockNode)currStack.peek();
					sn.setParent(parent);
					parent.addStatement(sn);
					currStack.push(sn);
					//System.err.println("*** pushed sn: " + node);
				} else {
					setSuspendStackBuildingForNode(node);
					setSuspendDeclarationsForNode(node);
					//System.err.println("DEBUG > SN parent is not a BN! (1) st: " + sn + ",\n         stacktop: " + currStack.peek());
				}
			}
			if (node instanceof StatementBlock && suspendStackBuildingForNode == null) {
				BlockNode bn = new BlockNode(node);
				if (currStack.isEmpty()) {
					//root block
					env.setRootNode(bn);
					currStack.push(bn);
					//System.err.println("*** pushed bn as root node: " + node);
				} else {
					if (currStack.peek() instanceof StatementNode) {
						StatementNode parent = (StatementNode)currStack.peek();
						bn.setParent(parent);
						parent.addBlock(bn);
						currStack.push(bn);
						//System.err.println("*** pushed bn: " + node);
					} else {
						setSuspendStackBuildingForNode(node);
						setSuspendDeclarationsForNode(node);
						//System.err.println("DEBUG > BN parent is not a SN! (2) st: " + bn + ",\n         stacktop: " + currStack.peek());
					}
				}
			}
			if (node instanceof FormalParameter && suspendDeclarationsForNode == null) {
				Variable var = new Variable((FormalParameter)node, null, true);
				//System.err.println("*** var added as FormalParameter: " + var);
				env.addVariable(var);
				return V_SKIP;
			}
			if (node instanceof Def_Var && suspendDeclarationsForNode == null) {
				if (!(currStack.peek() instanceof StatementNode)) {
					//System.err.println("DEBUG > At a var def: stacktop is not a SN! (5) st: " + node + ",\n         stacktop: " + currStack.peek());
				}
				StatementNode declSt = (StatementNode)currStack.peek();
				Variable var = new Variable((Def_Var)node, declSt, false);
				if (declSt == null) {
					ErrorReporter.logError("ChangeCreator.FunctionAnalyzer: declSt is null; var: " + var + "; loc: " + Utils.createLocationString(node));
				}
				declSt.setDeclaredVar(var);
				env.addVariable(var);
				//System.err.println("*** var added, declSt is stacktop (SN) " + var);
				//test for multi-declaration
				StatementNode prevSt = declSt.getParent().getPreviousStatement(declSt);
				if (prevSt != null && prevSt.isDeclaration() && prevSt.isLocationEqualTo(declSt)) {
					prevSt.linkWithOtherAsMultiDeclaration(declSt);
				}
				return V_CONTINUE;
			}
			if (node instanceof Undefined_LowerIdentifier_Value) {
				((Undefined_LowerIdentifier_Value)node).getAsReference();
				return V_CONTINUE;
			}
			if (node instanceof Reference && suspendReferencesForNode == null) {
				Reference ref = (Reference)node;
				Assignment as = ref.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				StatementNode refSt = (StatementNode)currStack.peek();
				if (refSt == null) {
					//should be a return type or runs on reference (ignore it)
					return V_SKIP;
				}
				//does the statement contain function calls?
				if (as instanceof Def_Function || as instanceof Def_Extfunction) {
					refSt.setHasFunctionCall();
					return V_CONTINUE;
				}
				//
				Variable var = env.getVariable(as);
				if (var == null) {
					//System.err.println("DEBUG > Reference to undeclared variable! (3) id: " + as.getIdentifier());
					if (as instanceof Def_Var) {
						//TODO only this type can be unchecked? (consts are definitely not)
						refSt.setHasUncheckedRef();
					}
					return V_CONTINUE;
				}
				/*if (!(currStack.peek() instanceof StatementNode)) {
					System.err.println("DEBUG > At a reference: stacktop is not a SN! (4) id: " + as.getIdentifier() + "\n         stacktop: " + currStack.peek());
				}*/
				var.addReference(refSt, ref.getUsedOnLeftHandSide());
				refSt.addReferedVars(var);
				//System.err.println("*** ref added to stacktop st (SN) " + var);
				return V_CONTINUE;
			}
			return V_CONTINUE;
		}
		
		@Override
		public int leave(final IVisitableNode node) {
			if (node instanceof For_Loop_Definitions) {
				checkForUnsuspend(node);
				//System.err.println("*** unsuspended decl when leaving: " + node);
				return V_CONTINUE;
			}
			if (node instanceof Statement) {
				if (suspendStackBuildingForNode == null) {
					Node n = currStack.pop();
					//System.err.println("*** popped(" + n.getClass() + "): " + node);
				}
				checkForUnsuspend(node);
			}
			if (node instanceof StatementBlock) {
				if (suspendStackBuildingForNode == null) {
					Node n = currStack.pop();
					//System.err.println("*** popped(" + n.getClass() + "): " + node);
				}
				checkForUnsuspend(node);
			}
			return V_CONTINUE;
		}
		
	}
	
	
	/**
	 * Collects Def_Functions and Def_Testcases in a module.
	 * */
	private static class FunctionCollector extends ASTVisitor {
		
		Set<Definition> result = new HashSet<Definition>();
		
		public Set<Definition> getResult() {
			return result;
		}
		
		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Def_Function ||
					node instanceof Def_Testcase) {
				result.add((Definition)node);
				return V_SKIP;
			}
			return V_CONTINUE;
			
		}
		
		
	}
	
	
	private static String loadFileContent(final IFile toLoad) {
		StringBuilder fileContents;
		try {
			InputStream is = toLoad.getContents();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			fileContents = new StringBuilder();
			char[] buff = new char[1024];
			while (br.ready()) {
				int len = br.read(buff);
				fileContents.append(buff, 0, len);
			}
			br.close();
		} catch (IOException e) {
			ErrorReporter.logError("ChangeCreator.loadFileContent(): Unable to get file contents (IOException) for file: " + toLoad.getName());
			return null;
		} catch (CoreException ce) {
			ErrorReporter.logError("ChangeCreator.loadFileContent(): Unable to get file contents (CoreException) for file: " + toLoad.getName());
			return null;
		}
		return fileContents.toString();
	}
	
	
}
