/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Helper class: helps in converting a location object into an open editor, where the location is highlighted. 
 **/
public final class LocationHighlighter {

	private LocationHighlighter() {
		//private constructor to disable instantiation
	}
	
	/***
	 * Jump to the provided location and highlight it.
	 */
	public static void jumpToLocation(final Location location) {
		final IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(location.getFile().getName());
		if (desc == null) {
			return;
		}
		try {
			final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			final IEditorPart editorPart = page.openEditor(new FileEditorInput((IFile) location.getFile()), desc.getId());
			if (editorPart != null && (editorPart instanceof AbstractTextEditor)) {
				((AbstractTextEditor) editorPart).setHighlightRange(location.getOffset(), 0, true);
			}

		} catch (final PartInitException e) {
			ErrorReporter.logExceptionStackTrace("Error while opening the editor", e);
		}
	}
}
