/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.titan.designer.editors.ttcnppeditor.TTCNPPEditor;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titanium.organize.OrganizeImports;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/**
 * The organize imports action, which can be called while editing a ttcn3 file.
 * 
 * @author poroszd
 * 
 */
public final class OrganizeFromEditor extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		if (editor == null || !(editor instanceof TTCN3Editor || editor instanceof TTCNPPEditor)) {
			ErrorReporter.logError("The editor is not found or not a Titan TTCN-3 editor");
			return null;
		}

		final IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		TextFileChange change = null;
		try {
			change = OrganizeImports.organizeImportsChange(file);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while creating the needed import changes",e);
			return null;
		}

		try {
			change.perform(new NullProgressMonitor());
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while performing the needed import changes",e);
		}

		final IProject project = file.getProject();
		GlobalParser.getProjectSourceParser(project).reportOutdating(file);
		GlobalParser.getProjectSourceParser(project).analyzeAll();

		return null;
	}

}
