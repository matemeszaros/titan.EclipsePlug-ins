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
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;

/**
 * @author Viktor Varga
 * */
public class ExtractToFunctionHeadless {

	private final IFile selectedFile;
	private final ITextSelection textSelection;

	private final String newFuncName;
	private final List<String> newParamNames;

	private RefactoringStatus refactoringStatus;

	private boolean selectionValid = true;

	public ExtractToFunctionHeadless(final IFile selFile, final ITextSelection textSel,
			final String newFuncName, final List<String> newParamNames) {
		this.selectedFile = selFile;
		this.textSelection = textSel;
		this.newFuncName = newFuncName;
		this.newParamNames = newParamNames;
	}

	public boolean isSelectionValid() {
		return selectionValid;
	}
	public RefactoringStatus getRefactoringStatus() {
		return refactoringStatus;
	}

	public void run() {

		final ExtractToFunctionRefactoring refactoring = new ExtractToFunctionRefactoring();
		try {
			refactoring.findSelectionHeadless(selectedFile, textSelection);
			if (!refactoring.isSelectionValid()) {
				//ErrorReporter.logError("ExtractToFunctionHeadless: Invalid selection! ");
				selectionValid = false;
				return;
			}
			Activator.getDefault().pauseHandlingResourceChanges();
			//
			try {
				final WorkspaceJob job1 = refactoring.createFunction();
				job1.join();
				if (!job1.getResult().isOK()) {
					ErrorReporter.logError("ExtractToFunctionHeadless: createFunction() job failed! ");
					return;
				}
			} catch (InterruptedException ie) {
				ErrorReporter.logExceptionStackTrace(ie);
			}
			refactoringStatus = refactoring.checkFinalConditions(new NullProgressMonitor());
			//setting new function name
			editFuncName(refactoring.getNewFunctionName());
			//setting new parameter names
			final IModelProvider<ParamTableItem> modelProvider = refactoring.getWizardModelProvider();
			editParamNames(modelProvider.getItems());
			//
			Activator.getDefault().resumeHandlingResourceChanges();

			final Change change = refactoring.createChange(null);
			change.perform(new NullProgressMonitor());

		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	private void editFuncName(final StringBuilder funcName) {
		if (newFuncName == null) {
			return;
		}
		funcName.setLength(0);
		funcName.append(newFuncName);
	}

	private void editParamNames(final List<ParamTableItem> params) {
		if (newParamNames == null || newParamNames.isEmpty()) {
			return;
		}

		final ListIterator<ParamTableItem> itPti = params.listIterator();
		final ListIterator<String> itNewNames = newParamNames.listIterator();
		while (itPti.hasNext() && itNewNames.hasNext()) {
			itPti.next().setName(itNewNames.next());
		}
	}

}
