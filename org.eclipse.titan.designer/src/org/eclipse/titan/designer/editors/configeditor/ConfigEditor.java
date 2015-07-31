/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.parsers.cfg.CfgAnalyzer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class ConfigEditor extends FormEditor implements IResourceChangeListener {
	protected ConfigTextEditor editor;

	protected int editorPageIndex;
	protected boolean isDirty = false;
	protected String errorMessage;

	public ConfigEditor() {
		editor = new ConfigTextEditor(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	@Override
	protected FormToolkit createToolkit(final Display display) {
		return new FormToolkit(FormColorCache.getFormColors(display));
	}

	void createTextEditorPage() {
		try {
			editorPageIndex = addPage(editor, getEditorInput());
			setPageText(editorPageIndex, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	public void setDirty() {
		isDirty = true;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public boolean isDirty() {
		return isDirty || editor.isDirty();
	}

	public abstract void refresh(final CfgAnalyzer cfgAnalyzer);

	public abstract void setErrorMessage(final String errorMessage);
	
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	public void gotoMarker(final IMarker marker) {
		setActivePage(editorPageIndex);
		IDE.gotoMarker(getEditor(editorPageIndex), marker);
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			IEditorInput input = editor.getEditorInput();
			if (input instanceof FileEditorInput && ((FileEditorInput) input).getFile().getProject().equals(event.getResource())) {
				final IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < pages.length; i++) {
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				});
			}
		}
	}

	public ConfigTextEditor getEditor() {
		return editor;
	}
}
