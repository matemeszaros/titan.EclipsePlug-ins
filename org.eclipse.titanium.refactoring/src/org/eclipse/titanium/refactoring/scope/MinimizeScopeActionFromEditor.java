/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.scope;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.refactoring.Utils;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

/**
 * This class handles the {@link MinimizeScopeRefactoring} class when the operation is
 * called from the editor for a single function definition.
 * <p>
 * {@link #execute(ExecutionEvent)} is called by the UI (see plugin.xml).
 * 
 * @author Viktor Varga
 */
public class MinimizeScopeActionFromEditor extends AbstractHandler {
	
	private static final String ERR_MSG_NO_SELECTION = "Move the cursor into a function or testcase body! ";

	/** the definition which is being refactored */
	private Definition selection;
	
	public Definition getSelection() {
		return selection;
	}
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		//update AST
		Utils.updateASTForProjectActiveInEditor("MinimizeScope");
		Activator.getDefault().pauseHandlingResourceChanges();

		// getting the active editor
		final TTCN3Editor targetEditor = Utils.getActiveEditor();
		if (targetEditor == null) {
			return null;
		}
		
		//get selected definition
		selection = findSelection();
		if (selection == null) {
			return null;
		}
		
		final IResource selectedRes = selection.getLocation().getFile();
		if (!(selectedRes instanceof IFile)) {
			ErrorReporter.logError("MinimizeScopeActionFromEditor.execute(): Selected resource `"
							+ selectedRes.getName() + "' is not a file.");
			return null;
		}
		final IFile selectedFile = (IFile)selectedRes;

		//
		final MinimizeScopeRefactoring refactoring = new MinimizeScopeRefactoring(selection, null);

		//open wizard
		final MinimizeScopeWizard wiz = new MinimizeScopeWizard(refactoring);
		final RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(wiz);
		try {
			operation.run(targetEditor.getEditorSite().getShell(), "");
		} catch (InterruptedException irex) {
			// operation was cancelled
		} catch (Exception e) {
			ErrorReporter.logError("MinimizeScopeActionFromEditor: Error while performing refactoring change! ");
			ErrorReporter.logExceptionStackTrace(e);
		}
		
		//update AST again
		Activator.getDefault().resumeHandlingResourceChanges();

		final IProject project = selectedFile.getProject();
		GlobalParser.getProjectSourceParser(project).reportOutdating(selectedFile);
		GlobalParser.getProjectSourceParser(project).analyzeAll();
		
		return null;
	}
	

	private Definition findSelection() {
		//getting the active editor
		final IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		TTCN3Editor targetEditor;
		if (editor == null || !(editor instanceof TTCN3Editor)) {
			return null;
		} else {
			targetEditor = (TTCN3Editor) editor;
		}
		final IStatusLineManager statusLineManager = targetEditor.getEditorSite().getActionBars().getStatusLineManager();
		//getting current selection
		final ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		final TextSelection textSelection = extractSelection(selectionService.getSelection());
		
		//iterating through part of the module
		final IResource selectedRes = extractResource(targetEditor);
		if (!(selectedRes instanceof IFile)) {
			ErrorReporter.logError("SelectionFinder.findSelection(): Selected resource `" + selectedRes.getName() + "' is not a file.");
			return null;
		}
		
		final IFile selectedFile = (IFile)selectedRes;
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(selectedFile.getProject());
		final Module selectedModule = projectSourceParser.containedModule(selectedFile);

		//getting current selection nodes
		final int selectionOffset = textSelection.getOffset() + textSelection.getLength();
		final SelectionFinderVisitor selVisitor = new SelectionFinderVisitor(selectionOffset);
		selectedModule.accept(selVisitor);
		final Definition selectedDef = selVisitor.getSelection();
		if (selectedDef == null) {
			ErrorReporter.logWarning("SelectionFinder.findSelection(): Visitor did not find a definition in the selection.");
			statusLineManager.setErrorMessage(ERR_MSG_NO_SELECTION);
			return null;
		}
		return selectedDef;
	}

	private TextSelection extractSelection(final ISelection sel) {
		if (!(sel instanceof TextSelection)) {
			ErrorReporter.logError("ContextLoggingActionFromEditor.extractSelection():" +
					" selection is not a TextSelection");
			return null;
		}
		return (TextSelection)sel;
	}
	private IResource extractResource(final IEditorPart editor) {
		final IEditorInput input = editor.getEditorInput();
		if (!(input instanceof IFileEditorInput)) {
			return null;
		}
		return ((IFileEditorInput) input).getFile();
	}

	/**
	 * Searches for a {@link Def_Function} or {@link Def_Testcase} node that contains the current selection offset
	 * (selection is inside the node).
	 */
	private static class SelectionFinderVisitor extends ASTVisitor {
		
		private Definition def;
		private final int offset;
		
		SelectionFinderVisitor(final int selectionOffset) {
			offset = selectionOffset;
		}
		
		private Definition getSelection() {
			return def;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (!(node instanceof ILocateableNode)) {
				return V_CONTINUE;
			}
			final Location loc = ((ILocateableNode) node).getLocation();
			if (loc == null) {
				return V_ABORT;
			}
			if (!loc.containsOffset(offset)) {
				// skip the children, the offset is not inside this node
				return V_SKIP;
			}
			if (node instanceof Definition) {
				if (isGoodType(node)) {
					def = (Definition)node;
				} else {
					def = null;
				}
			}
			if (node instanceof Undefined_LowerIdentifier_Value) {
				((Undefined_LowerIdentifier_Value)node).getAsReference();
				return V_CONTINUE;
			}
			if (node instanceof Reference) {
				final Reference ref = (Reference)node;
				final Assignment as = ref.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				if (as instanceof Definition) {
					if (isGoodType(as)) {
						def = (Definition)as;
					} else {
						def = null;
					}
				}
			}
			return V_CONTINUE;
		}
		
		private static boolean isGoodType(final IVisitableNode node) {
			return (node instanceof Def_Function ||
					node instanceof Def_Testcase);
		}
		
	}
	
}
