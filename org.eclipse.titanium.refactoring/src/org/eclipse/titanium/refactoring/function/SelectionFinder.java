/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ControlPart;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Break_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Continue_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.DoWhile_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Goto_statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Interleave_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Label_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.While_Statement;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.refactoring.function.ReturnVisitor.ReturnCertainty;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

/**
 * This class is only instantiated by {@link ExtractToFunctionRefactoring} once per
 * each refactoring operation. By calling {@link #perform()}, a list of
 * statements ({@link StatementList} node) is extracted from the current
 * text selection.
 * <p>
 * Use {@link #performHeadless(IFile, ITextSelection)} when no GUI is available:
 * for testing, for example.
 * 
 * @author Viktor Varga
 */
class SelectionFinder {

	private static final String ERR_MSG_NO_SELECTION = "No statements to extract were found in the selection! ";
	private static final String WARNING_ERRONEOUS_GOTO = "A goto statement was found pointing out of the selection. "
			+ "The operation might produce an erroneous result. ";
	private static final String WARNING_ERRONEOUS_LABEL = "An external goto statement was found pointing into the selection. "
			+ "The operation might produce an erroneous result. ";
	private static final String WARNING_ERRONEOUS_BREAK = "A break statement was found to be referring to a "
			+ "loop/alt/interleave statement from outside the selection. "
			+ "The operation might produce an erroneous result. ";
	private static final String WARNING_ERRONEOUS_CONTINUE = "A continue statement was found to be referring to a "
			+ "loop statement from outside the selection. "
			+ "The operation might produce an erroneous result. ";
	private static final String WARNING_UNCERTAIN_RETURN = "The code to be extracted contains conditionally executed "
			+ "return statements. "
			+ "The operation might produce an erroneous result. ";

	private IStatusLineManager statusLineManager;
	private ITextSelection textSelection;
	private ProjectSourceParser sourceParser;
	private IProject project;

	// out
	private Module selectedModule;
	private IFile selectedFile;
	private StatementList selectedStatements;
	private final List<RefactoringStatusEntry> warnings;
	private Reference runsOnRef;
	private Type returnType;
	private IVisitableNode parentFunc;
	private int insertLoc = -1;
	private ReturnCertainty returnCertainty = ReturnCertainty.NO;

	SelectionFinder() {
		warnings = new ArrayList<RefactoringStatusEntry>();
	}

	List<RefactoringStatusEntry> getWarnings() {
		return warnings;
	}
	Module getModule() {
		return selectedModule;
	}
	IFile getSelectedFile() {
		return selectedFile;
	}
	StatementList getSelectedStatements() {
		return selectedStatements;
	}
	Reference getRunsOnRef() {
		return runsOnRef;
	}
	Type getReturnType() {
		return returnType;
	}
	IVisitableNode getParentFunc() {
		return parentFunc;
	}
	int getInsertLoc() {
		return insertLoc;
	}
	ReturnCertainty getReturnCertainty() {
		return returnCertainty;
	}
	ProjectSourceParser getProjectSourceParser() {
		return sourceParser;
	}
	IProject getProject() {
		return project;
	}
	boolean isSelectionValid() {
		return selectedStatements != null && !selectedStatements.isEmpty();
	}

	void performHeadless(final IFile selFile, final ITextSelection textSel) {
		selectedFile = selFile;
		String fileContents = readFileContents(selFile);
		if (fileContents == null) {
			ErrorReporter.logError("ExtractToFunctionRefactoring.findSelection(): selFile does not exist at: "
					+ selFile.getFullPath());
			return;
		}
		IDocument doc = new Document(fileContents);
		textSelection = new TextSelection(doc, textSel.getOffset(), textSel.getLength());
		//
		project = selFile.getProject();
		sourceParser = GlobalParser.getProjectSourceParser(project);
		selectedModule = sourceParser.containedModule(selectedFile);
		if(selectedModule == null) {
			ErrorReporter.logError("ExtractToFunctionRefactoring.findSelection(): The module in the file "
					+ selectedFile.getName() + " has no name.");
			return;
		}
		// iterating through the module for the selected statements
		SelectionVisitor selectionVisitor = new SelectionVisitor(textSelection.getOffset(), textSelection.getLength());
		selectedModule.accept(selectionVisitor);
		selectedStatements = selectionVisitor.createStatementList(textSelection);
		if (selectedStatements.isEmpty()) {
			ErrorReporter.logError(ERR_MSG_NO_SELECTION);
			return;
		}
		if (ExtractToFunctionRefactoring.DEBUG_MESSAGES_ON) {
			ErrorReporter.logError(selectedStatements.createDebugInfo());
			ErrorReporter.logError(createDebugInfo());
		}
		// finding return type & runs on clause
		RunsOnClauseFinder runsonVisitor = new RunsOnClauseFinder(selectedStatements.getLocation());
		selectedModule.accept(runsonVisitor);
		runsOnRef = runsonVisitor.getRunsOnRef();
		parentFunc = runsonVisitor.getFuncDef();
		//finding insert location
		if (parentFunc instanceof Definition) {
			insertLoc = ((Definition)parentFunc).getLocation().getEndOffset();
		} else if (parentFunc instanceof ControlPart) {
			ControlPart cp = (ControlPart)parentFunc;
			Location commentLoc = cp.getCommentLocation();
			insertLoc = commentLoc == null ? cp.getLocation().getOffset() : commentLoc.getOffset();
		}
		//
		ReturnVisitor retVis = new ReturnVisitor();
		selectedStatements.accept(retVis);
		returnCertainty = retVis.getCertainty();
		if (retVis.getCertainty() != ReturnCertainty.NO) {
			returnType = runsonVisitor.getReturnType();
		}
		// checking erroneousness of selection
		checkErroneousGoto();
		if (containsBreakWithoutLoop()) {
			warnings.add(new RefactoringStatusEntry(RefactoringStatus.WARNING, WARNING_ERRONEOUS_BREAK));
		}
		if (containsContinueWithoutLoop()) {
			warnings.add(new RefactoringStatusEntry(RefactoringStatus.WARNING, WARNING_ERRONEOUS_CONTINUE));
		}
		if (retVis.getCertainty() == ReturnCertainty.MAYBE) {
			warnings.add(new RefactoringStatusEntry(RefactoringStatus.WARNING, WARNING_UNCERTAIN_RETURN));
		}
	}
	
	void perform() {
		// getting the active editor
		IEditorPart editor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		TTCN3Editor targetEditor;
		if (editor == null || !(editor instanceof TTCN3Editor)) {
			return;
		} else {
			targetEditor = (TTCN3Editor) editor;
		}
		statusLineManager = targetEditor.getEditorSite().getActionBars()
				.getStatusLineManager();
		// getting current selection
		ISelectionService selectionService = PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getSelectionService();
		textSelection = extractSelection(selectionService.getSelection());

		if (textSelection == null) {
			ErrorReporter.logError("No valid statements were found in the selection.");
			ExtractToFunctionRefactoring.setStatusLineMsg(ERR_MSG_NO_SELECTION, statusLineManager);
			return;
		}
		// getting selected module
		IResource selectedRes = extractResource(targetEditor);
		if (!(selectedRes instanceof IFile)) {
			ErrorReporter.logError("ExtractToFunctionRefactoring.findSelection(): Selected resource `"
							+ selectedRes.getName() + "' is not a file.");
			return;
		}
		selectedFile = (IFile)selectedRes;
		project = selectedFile.getProject();
		sourceParser = GlobalParser.getProjectSourceParser(project);
		selectedModule = sourceParser.containedModule(selectedFile);
		// iterating through the module for the selected statements
		SelectionVisitor selectionVisitor = new SelectionVisitor(textSelection.getOffset(), textSelection.getLength());
		selectedModule.accept(selectionVisitor);
		selectedStatements = selectionVisitor.createStatementList(textSelection);
		if (selectedStatements.isEmpty()) {
			ExtractToFunctionRefactoring.setStatusLineMsg(ERR_MSG_NO_SELECTION, statusLineManager);
			return;
		}
		if (ExtractToFunctionRefactoring.DEBUG_MESSAGES_ON) {
			ErrorReporter.logError(selectedStatements.createDebugInfo());
			ErrorReporter.logError(createDebugInfo());
		}
		// finding return type & runs on clause
		RunsOnClauseFinder runsonVisitor = new RunsOnClauseFinder(selectedStatements.getLocation());
		selectedModule.accept(runsonVisitor);
		runsOnRef = runsonVisitor.getRunsOnRef();
		parentFunc = runsonVisitor.getFuncDef();
		//finding insert location
		if (parentFunc instanceof Definition) {
			insertLoc = ((Definition)parentFunc).getLocation().getEndOffset();
		} else if (parentFunc instanceof ControlPart) {
			ControlPart cp = (ControlPart)parentFunc;
			Location commentLoc = cp.getCommentLocation();
			insertLoc = commentLoc == null ? cp.getLocation().getOffset() : commentLoc.getOffset();
		}
		//
		ReturnVisitor retVis = new ReturnVisitor();
		selectedStatements.accept(retVis);
		returnCertainty = retVis.getCertainty();
		if (retVis.getCertainty() != ReturnCertainty.NO) {
			returnType = runsonVisitor.getReturnType();
		}
		// checking erroneousness of selection
		checkErroneousGoto();
		if (containsBreakWithoutLoop()) {
			warnings.add(new RefactoringStatusEntry(RefactoringStatus.WARNING, WARNING_ERRONEOUS_BREAK));
		}
		if (containsContinueWithoutLoop()) {
			warnings.add(new RefactoringStatusEntry(RefactoringStatus.WARNING, WARNING_ERRONEOUS_CONTINUE));
		}
		if (retVis.getCertainty() == ReturnCertainty.MAYBE) {
			warnings.add(new RefactoringStatusEntry(RefactoringStatus.WARNING, WARNING_UNCERTAIN_RETURN));
		}
	}
	
	private IResource extractResource(final IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if (!(input instanceof IFileEditorInput)) {
			return null;
		}
		return ((IFileEditorInput) input).getFile();
	}

	private TextSelection extractSelection(final ISelection sel) {

		if (!(sel instanceof TextSelection)) {
			ErrorReporter
					.logError("ExtractToFunctionRefactoring.extractSelection(): selection is not a TextSelection");
			return null;
		}
		return (TextSelection) sel;
	}

	private String readFileContents(final IFile toRead) {
		StringBuilder sb = new StringBuilder();
		if (toRead == null || !toRead.exists()) {
			return null;
		}
		try {
			char[] buf = new char[1024];
			InputStream is = toRead.getContents();
			InputStreamReader isr = new InputStreamReader(is);
			while (isr.ready()) {
				isr.read(buf);
				sb.append(buf);
			}
			isr.close();
			is.close();
		} catch (CoreException ce) {
			ErrorReporter.logError("ExtractToFunctionRefactoring.readFileContents(): CoreException while reading file: "
					+ toRead.getFullPath());
			ErrorReporter.logExceptionStackTrace(ce);
		} catch (IOException ioe) {
			ErrorReporter.logError("ExtractToFunctionRefactoring.readFileContents(): IOException while reading file: "
					+ toRead.getFullPath());
			ErrorReporter.logExceptionStackTrace(ioe);
		}
		return sb.toString();
	}
	
	/**
	 * Collects Statements that are located inside the selection region (call
	 * for the enclosing Module)
	 * */
	private static class SelectionVisitor extends ASTVisitor {

		private final List<Statement> statements;
		private StatementBlock parentBlock;

		private final int offset;
		private final int endOffset;

		private SelectionVisitor(final int selOffset, final int selLen) {
			offset = selOffset;
			endOffset = offset + selLen;
			statements = new ArrayList<Statement>();
		}

		/**
		 * Creates a new StatementList from the selection and returns it.
		 * <p>
		 * If necessary, the location interval of the StatementList is extended
		 * to include possible ending semicolons (location interval extension
		 * only if the semicolon itself is selected).
		 * */
		StatementList createStatementList(final ITextSelection textSel) {
			final char SEMICOLON = ';';
			StatementList sl = new StatementList(statements);
			if (textSel == null || sl.isEmpty()) {
				return sl;
			}
			String content = textSel.getText();
			int ind = sl.getLocation().getEndOffset() - textSel.getOffset();
			if (ind < 0 || ind >= textSel.getLength()) {
				return sl;
			}
			if (content.charAt(ind) != SEMICOLON) {
				return sl;
			}
			sl.increaseLocationEndOffset(1);
			return sl;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof ILocateableNode) {
				final Location loc = ((ILocateableNode) node).getLocation();
				if (loc == null) {
					return V_ABORT;
				}
				if (node instanceof Statement && loc.getOffset() >= offset
						&& loc.getEndOffset() <= endOffset) {
					Statement st = (Statement) node;
					if (parentBlock == null) {
						parentBlock = st.getMyStatementBlock();
						if (parentBlock == null) {
							// filters special statements, like: initial/step
							// statement of a for loop
							return V_SKIP;
						}
						statements.add(st);
					} else if (st.getMyStatementBlock() == parentBlock) {
						statements.add(st);
						return V_SKIP;
					}
				}
				// do not enter Alt_Statements (only extract them if they are
				// wholly included in the selection)
				if (node instanceof Alt_Statement) {
					return V_SKIP;
				}
			}
			return V_CONTINUE;
		}

	}

	/**
	 * Reports warnings if there are any unmatched goto/label statements in the selection.
	 * */
	private void checkErroneousGoto() {
		if (selectedStatements == null) {
			return;
		}
		GotoFinder vis = new GotoFinder();
		selectedStatements.accept(vis);
		List<Identifier> gotos = vis.getGotoStatements();
		List<Identifier> labels = vis.getLabelStatements();
		if (!labels.containsAll(gotos)) {
			warnings.add(new RefactoringStatusEntry(RefactoringStatus.WARNING, WARNING_ERRONEOUS_GOTO));
		}
		if (!gotos.containsAll(labels)) {
			warnings.add(new RefactoringStatusEntry(RefactoringStatus.WARNING, WARNING_ERRONEOUS_LABEL));
		}
	}

	/**
	 * Collects goto & label statements in the selection.
	 * <p>
	 * Call on StatementList.
	 * */
	private static class GotoFinder extends ASTVisitor {

		private final List<Identifier> gotoStatements;
		private final List<Identifier> labelStatements;

		private boolean insideGoto = false;
		private boolean insideLabel = false;

		GotoFinder() {
			gotoStatements = new ArrayList<Identifier>();
			labelStatements = new ArrayList<Identifier>();
		}

		List<Identifier> getGotoStatements() {
			return gotoStatements;
		}

		List<Identifier> getLabelStatements() {
			return labelStatements;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Goto_statement) {
				insideGoto = true;
				return V_CONTINUE;
			}
			if (node instanceof Label_Statement) {
				insideLabel = true;
				return V_CONTINUE;
			}
			if (node instanceof Identifier) {
				if (insideGoto) {
					gotoStatements.add((Identifier) node);
				} else if (insideLabel) {
					labelStatements.add((Identifier) node);
				}
			}
			insideGoto = false;
			insideLabel = false;
			return V_CONTINUE;
		}

	}

	/**
	 * @return whether the selection contains break statements that are not
	 *         enclosed in a loop/alt/interleave block.
	 * */
	private boolean containsBreakWithoutLoop() {
		if (selectedStatements == null) {
			return false;
		}
		BreakFinder vis = new BreakFinder();
		selectedStatements.accept(vis);
		return vis.isFound();
	}

	/**
	 * @return whether the selection contains continue statements that are not
	 *         enclosed in a loop block.
	 * */
	private boolean containsContinueWithoutLoop() {
		if (selectedStatements == null) {
			return false;
		}
		ContinueFinder vis = new ContinueFinder();
		selectedStatements.accept(vis);
		return vis.isFound();
	}

	/**
	 * Tests whether the selection contains any break statements without an
	 * enclosing loop/alt/interleave statement block.
	 * <p>
	 * Call on StatementList.
	 * */
	private static class BreakFinder extends ASTVisitor {

		private boolean found = false;

		boolean isFound() {
			return found;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof For_Statement
					|| node instanceof DoWhile_Statement
					|| node instanceof While_Statement
					|| node instanceof Alt_Statement
					|| node instanceof Interleave_Statement) {
				return V_SKIP;
			}
			if (node instanceof Break_Statement) {
				found = true;
				return V_ABORT;
			}
			return V_CONTINUE;
		}

	}

	/**
	 * Tests whether the selection contains any continue statements without an
	 * enclosing loop block.
	 * <p>
	 * Call on StatementList.
	 * */
	private static class ContinueFinder extends ASTVisitor {

		private boolean found = false;

		boolean isFound() {
			return found;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof For_Statement
					|| node instanceof DoWhile_Statement
					|| node instanceof While_Statement) {
				return V_SKIP;
			}
			if (node instanceof Continue_Statement) {
				found = true;
				return V_ABORT;
			}
			return V_CONTINUE;
		}

	}

	/**
	 * Finds the 'runs on ...' clause of the function inside which the
	 * <code>atLocation</code> is located. Call for a Module.
	 * */
	private static class RunsOnClauseFinder extends ASTVisitor {

		private final Location atLocation;
		private boolean insideFunc = false;
		private IVisitableNode funcDef;
		private Reference runsOnRef;
		private Type returnType;

		RunsOnClauseFinder(final Location atLocation) {
			this.atLocation = atLocation;
		}

		IVisitableNode getFuncDef() {
			return funcDef;
		}

		Reference getRunsOnRef() {
			return runsOnRef;
		}

		Type getReturnType() {
			return returnType;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (insideFunc) {
				if (node instanceof Reference) {
					runsOnRef = (Reference) node;
					return V_ABORT;
				} else {
					return V_SKIP;
				}
			}
			if (node instanceof Def_Function || node instanceof Def_Testcase
					|| node instanceof Def_Altstep) {
				if (((Definition) node).getLocation().containsOffset(atLocation.getOffset())) {
					if (node instanceof Def_Function) {
						returnType = ((Def_Function) node).getType(CompilationTimeStamp.getBaseTimestamp());
					}
					funcDef = (Definition)node;
					insideFunc = true;
					return V_CONTINUE;
				}
				return V_SKIP;
			}
			if (node instanceof ControlPart) {
				funcDef = (ControlPart)node;
				return V_ABORT;
			}
			return V_CONTINUE;
		}

	}

	private String createDebugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("ExtractToFunctionRefactoring->SelectionFinder debug info: \n");
		sb.append("  Runs on reference: ");
		sb.append(runsOnRef == null ? "null" : runsOnRef.getId());
		sb.append(", enclosing function: ");
		if (parentFunc == null) {
			sb.append("null");
		} else if (parentFunc instanceof Definition) {
			sb.append(((Definition)parentFunc).getIdentifier());
		} else if (parentFunc instanceof ControlPart) {
			sb.append("<controlpart>");
		} else {
			sb.append("<invalid: ").append(parentFunc).append(">");
		}
		sb.append("\n");
		sb.append("  Return clause: ");
		sb.append(returnType == null ? "null" : returnType.getIdentifier());
		sb.append("  Warnings: ");
		for (RefactoringStatusEntry rse : warnings) {
			sb.append("severity: " + rse.getSeverity() + "; msg: "
					+ rse.getMessage());
			sb.append("\n");
		}
		return sb.toString();
	}

}
