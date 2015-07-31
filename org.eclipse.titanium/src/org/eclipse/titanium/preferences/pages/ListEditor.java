/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences.pages;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.designer.preferences.pages.ExcludeRegexpEditor;

public class ListEditor extends ExcludeRegexpEditor {

	public ListEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	protected String getNewInputObject() {
		Shell shell = Display.getCurrent().getActiveShell();
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setMessage("Please select the directory whose name will be ommitted from cluster names.");
		dialog.setText("Directory selection");

		final String directory = dialog.open();

		if (directory != null) {
			return directory;
		} else {
			return null;
		}
	}

	@Override
	protected String getEditInputObject(final String original) {
		Shell shell = Display.getCurrent().getActiveShell();
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setMessage("Please select the directory whose name will be ommitted from cluster names.");
		dialog.setText("Directory selection");
		dialog.setFilterPath(original);

		final String directory = dialog.open();

		if (directory != null) {
			return directory;
		} else {
			return original;
		}
	}
}
