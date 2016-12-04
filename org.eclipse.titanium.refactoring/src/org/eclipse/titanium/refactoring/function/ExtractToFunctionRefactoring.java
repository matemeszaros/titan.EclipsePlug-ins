/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titanium.refactoring.function.ReturnVisitor.ReturnCertainty;

/**
 * This class represents the 'Extract to function' refactoring operation. The details of the operation in order:
 * <p>
 * <li>INIT (must be called first)
 *  <ul>
 *  <li>{@link #findSelection()} determines which statements are selected by the user;
 *  use {@link #findSelectionHeadless(IFile, ITextSelection)} in headless mode</li>
 *  <li>{@link #createFunction()} creates the function body and function call texts (made up of lists of mutable StringBuilders) </li>
 *  </ul>
 * </li>
 * <li>EDIT TEXT THROUGH WIZARD
 *  <ul>
 *  <li>the contents of the previously returned StringBuilders can be modified by the user through the wizards</li>
 *  </ul>
 * </li>
 * <li>FINISH
 *  <ul>
 *  <li>{@link #createChange()} is called by the wizard; this produces Strings from the StringBuilders and the TextChange is returned</li>
 *  </ul>
 * </li>
 *
 * @author Viktor Varga
 */
public class ExtractToFunctionRefactoring extends Refactoring {

	/*
	 * TODO errors:
	 *
	 * (not in selection: goto l1; in selection: goto l1; label l1;) error is not recognized
	 *
	 * */

	/*
	 * TODO dev:
	 *
	 * debug logging only when its necessary (and print a good log)
	 *
	 * wizard params page edit support: check variable names for validity and dissimilarity
	 *
	 * */

	public static final boolean DEBUG_MESSAGES_ON = false;

	private IProject project;

	private final StringBuilder newFuncName = new StringBuilder("newFunction");
	private IVisitableNode parentFunc;

	//StringBuilders are used as mutable Strings
	private List<StringBuilder> functionText;
	private List<StringBuilder> functionCallText;
	private String functionTextReady = "/* function body */";
	private String functionCallTextReady = "/* function call */";

	private SelectionFinder selectionFinder;
	private ParamCollector paramCollector;
	private FunctionCreator functionCreator;

//CALLED FROM WIZARD

	public StringBuilder getNewFunctionName() {
		return newFuncName;
	}

	public IModelProvider<ParamTableItem> getWizardModelProvider() {
		return functionCreator;
	}

//CALLED FROM WIZARD END

	public String getNewFunctionText() {
		return functionTextReady;
	}

//METHODS FROM REFACTORING

	@Override
	public String getName() {
		return "Extract to function";
	}

	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		final RefactoringStatus result = new RefactoringStatus();
		ListIterator<RefactoringStatusEntry> it = selectionFinder.getWarnings().listIterator();
		while (it.hasNext()) {
			result.addEntry(it.next());
		}
		it = paramCollector.getWarnings().listIterator();
		while (it.hasNext()) {
			result.addEntry(it.next());
		}
		it = functionCreator.getWarnings().listIterator();
		while (it.hasNext()) {
			result.addEntry(it.next());
		}
		return result;
	}

	@Override
	public Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
		createFunctionText();
		createFunctionCallText();
		//
		final TextFileChange tfc = new TextFileChange(selectionFinder.getSelectedFile().getName(), selectionFinder.getSelectedFile());
		final MultiTextEdit rootEdit = new MultiTextEdit();
		tfc.setEdit(rootEdit);
		//replace selection with function call & new declarations
		final int offset = selectionFinder.getSelectedStatements().getLocation().getOffset();
		final int len = selectionFinder.getSelectedStatements().getLocation().getEndOffset()-offset;
		rootEdit.addChild(new ReplaceEdit(offset, len, functionCallTextReady));
		//add new function after the one in which the selection is
		if (parentFunc != null && selectionFinder.getInsertLoc() >= 0) {
			rootEdit.addChild(new InsertEdit(selectionFinder.getInsertLoc(), functionTextReady));
		}

		return tfc;
	}

//METHODS FROM REFACTORING END

	public void findSelection() {
		selectionFinder = new SelectionFinder();
		selectionFinder.perform();
		project = selectionFinder.getProject();
	}

	public void findSelectionHeadless(final IFile selFile, final ITextSelection textSel) {
		selectionFinder = new SelectionFinder();
		selectionFinder.performHeadless(selFile, textSel);
		project = selectionFinder.getProject();
	}

	public boolean isSelectionValid() {
		return selectionFinder != null && selectionFinder.isSelectionValid() && selectionFinder.getParentFunc() != null;
	}

	public IFile getSelectedFile() {
		return selectionFinder.getSelectedFile();
	}

	public Module getSelectedModule() {
		return selectionFinder.getModule();
	}

	public WorkspaceJob createFunction() {
		final WorkspaceJob job = new WorkspaceJob("ExtractToFunction: creating new function text") {

			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
				final StatementList selectedStatements = selectionFinder.getSelectedStatements();
				final Module selectedModule = getSelectedModule();
				final IFile selectedFile = getSelectedFile();

				final Reference runsOnRef = selectionFinder.getRunsOnRef();
				final Type returnType = selectionFinder.getReturnType();
				final ReturnCertainty retCertainty = selectionFinder.getReturnCertainty();
				parentFunc = selectionFinder.getParentFunc();

				if (parentFunc == null) {
					ErrorReporter.logError("ExtractToFunctionRefactoring.createFunction(): Could not find the enclosing function of the selection! ");
					return Status.CANCEL_STATUS;
				}
				if (selectionFinder.getInsertLoc() < 0) {
					ErrorReporter.logError("ExtractToFunctionRefactoring.createFunction(): Could not calculate the insert location! ");
					return Status.CANCEL_STATUS;
				}

				if (selectedStatements == null || selectedStatements.isEmpty() || selectedModule == null) {
					ErrorReporter.logError("ExtractToFunctionRefactoring.createFunction(): No or invalid selection! ");
					return Status.CANCEL_STATUS;
				}
				//collect params and find runs on clause
				paramCollector = new ParamCollector(project, selectedStatements, selectedModule);
				paramCollector.perform();
				List<Param> params = paramCollector.getParams();
				//
				if (params == null) {
					ErrorReporter.logError("ExtractToFunctionRefactoring.createFunction(): Unable to collect params! ");
					return Status.CANCEL_STATUS;
				}
				//create new function text
				functionCreator = new FunctionCreator(selectedStatements, selectedFile, newFuncName,
						params, runsOnRef, returnType, retCertainty);

				functionCreator.perform();
				functionText = functionCreator.getFunctionText();
				if (functionText == null) {
					ErrorReporter.logError("ExtractToFunctionRefactoring.createFunction(): Unable to create function text! ");
					return Status.CANCEL_STATUS;
				}

				functionCallText = functionCreator.getFunctionCallText();
				if (functionCallText == null) {
					ErrorReporter.logError("ExtractToFunctionRefactoring.createFunction(): Unable to create function call text! ");
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return job;
	}

	private void createFunctionText() {
		if (functionText == null) {
			functionTextReady = null;
			return;
		}

		final StringBuilder ret = new StringBuilder();
		for (StringBuilder sb: functionText) {
			ret.append(sb);
		}
		functionTextReady = ret.toString();
		if (DEBUG_MESSAGES_ON) {
			final StringBuilder sb = new StringBuilder();
			sb.append("ExtractToFunctionRefactoring->function body text debug info: >>>\n");
			sb.append(functionTextReady);
			sb.append("\n<<<");
			ErrorReporter.logError(sb.toString());
		}
	}
	private void createFunctionCallText() {
		if (functionCallText == null) {
			functionCallTextReady = null;
			return;
		}

		final StringBuilder ret = new StringBuilder();
		for (StringBuilder sb: functionCallText) {
			ret.append(sb);
		}
		functionCallTextReady = ret.toString();
		if (DEBUG_MESSAGES_ON) {
			final StringBuilder sb = new StringBuilder();
			sb.append("ExtractToFunctionRefactoring->function call text debug info: >>>\n");
			sb.append(functionCallTextReady);
			sb.append("\n<<<");
			ErrorReporter.logError(sb.toString());
		}
	}


	public static void setStatusLineMsg(final String msg, final IStatusLineManager toSet) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				toSet.setErrorMessage(msg);

			}
		});
	}


}
