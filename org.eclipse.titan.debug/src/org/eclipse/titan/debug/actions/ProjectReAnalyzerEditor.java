/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.debug.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.titan.designer.editors.asn1editor.ASN1Editor;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionDelegate;

public class ProjectReAnalyzerEditor extends ActionDelegate implements IEditorActionDelegate {
	private IEditorPart targetEditor = null;

	@Override
	public void run(final IAction action) {
		if (targetEditor == null) return;
		final IFile file = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
		final IProject project = file.getProject();
		GlobalParser.clearAllInformation(project);

		GlobalParser.getProjectSourceParser(project).analyzeAll();
	}

	public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
		if (targetEditor instanceof TTCN3Editor || targetEditor instanceof ASN1Editor) {
			this.targetEditor = targetEditor;
		} else {
			this.targetEditor = null;
		}
	}
}
