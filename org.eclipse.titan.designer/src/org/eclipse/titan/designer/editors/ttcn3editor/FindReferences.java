/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titan.designer.editors.referenceSearch.ReferenceSearch;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/**
 * @author Adam Delic
 * */
public final class FindReferences extends AbstractHandler implements IEditorActionDelegate {
	private IEditorPart targetEditor = null;
	private ISelection selection = TextSelection.emptySelection();

	@Override
	public void run(final IAction action) {
		if (targetEditor == null || !(targetEditor instanceof TTCN3Editor)) {
			return;
		}
		ReferenceSearch.runAction(targetEditor, selection);
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		if (targetEditor == null || !(targetEditor instanceof TTCN3Editor)) {
			return null;
		}
		ReferenceSearch.runAction(targetEditor, selection);
		return null;
	}

}
