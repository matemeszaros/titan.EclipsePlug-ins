/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.logging;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.refactoring.logging.ContextLoggingRefactoring.Settings;

/**
 * @author Viktor Varga
 * */
public class ContextLoggingHeadless {

	private final IFile selectedFile;	//not null only if selection is of TextSelection
	private final ISelection selection;
	
	private final Settings settings;

	/** Use this constructor when the selection is a set of files, folders, or projects. */
	public ContextLoggingHeadless(final IStructuredSelection selection, final Settings settings) {
		this.selectedFile = null;
		this.selection = selection;
		this.settings = settings;
	}
	/** Use this constructor when the selection is a part of a single file. */
	public ContextLoggingHeadless(final IFile selectedFile, final ITextSelection selection, final Settings settings) {
		this.selectedFile = selectedFile;
		this.selection = selection;
		this.settings = settings;
	}
	
	public Settings getSettings() {
		return settings;
	}

	public void run() {
		final ContextLoggingRefactoring refactoring;
		if (selectedFile == null) {
			refactoring = new ContextLoggingRefactoring((IStructuredSelection)selection, settings);
		} else {
			refactoring = new ContextLoggingRefactoring(selectedFile, (ITextSelection)selection, settings);
		}
		try {
			Change change = refactoring.createChange(null);
			change.perform(new NullProgressMonitor());
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}
	
	
	
	
}
