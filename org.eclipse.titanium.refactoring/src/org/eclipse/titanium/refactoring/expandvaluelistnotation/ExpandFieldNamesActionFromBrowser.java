package org.eclipse.titanium.refactoring.expandvaluelistnotation;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.titanium.refactoring.Utils;

/**
 * This class handles the {@link MinimizeVisibilityRefactoring} class when the operation is
 * called from the package browser for a single or multiple project(s), folder(s) or file(s).
 * <p>
 * {@link #execute(ExecutionEvent)} is called by the UI (see plugin.xml).
 * 
 * @author Zsolt Tabi
 */
public class ExpandFieldNamesActionFromBrowser extends AbstractHandler implements IObjectActionDelegate {
	private ISelection selection;

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		performMinimizeVisibility();
		return null;
	}
	@Override
	public void run(final IAction action) {
		performMinimizeVisibility();
		
	}
	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}
	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
	}

	private void performMinimizeVisibility() {
		//find selection
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		
		final IStructuredSelection structSelection = (IStructuredSelection)selection;
		final Set<IProject> projsToUpdate = Utils.findAllProjectsInSelection(structSelection);
		
		//update AST before refactoring
		Utils.updateASTBeforeRefactoring(projsToUpdate, "ExpandFieldNames");
		Activator.getDefault().pauseHandlingResourceChanges();
		
		//create refactoring
		final ExpandFieldNamesRefactoring refactoring = new ExpandFieldNamesRefactoring(structSelection);
		//open wizard
		final ExpandFieldNamesWizard wiz = new ExpandFieldNamesWizard(refactoring);
		final RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(wiz);
		// getting the active editor
		final TTCN3Editor targetEditor = Utils.getActiveEditor();
		try {
			operation.run(targetEditor == null ? null : targetEditor.getEditorSite().getShell(), "");
		} catch (InterruptedException irex) {
			// operation was cancelled
		} catch (Exception e) {
			ErrorReporter.logError("ExpandFieldNamesActionFromBrowser: Error while performing refactoring change! ");
			ErrorReporter.logExceptionStackTrace(e);
		}
		Activator.getDefault().resumeHandlingResourceChanges();


		//update AST after refactoring
		Utils.updateASTAfterRefactoring(wiz, refactoring.getAffectedObjects(), refactoring.getName());
	}
	
	
}
