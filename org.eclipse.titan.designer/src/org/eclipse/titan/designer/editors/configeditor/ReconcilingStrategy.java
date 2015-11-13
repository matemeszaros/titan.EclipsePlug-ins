/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3FoldingSupport;
import org.eclipse.titan.designer.parsers.GlobalIntervalHandler;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectConfigurationParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * */
public final class ReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
	public static final String OUTLINEUPDATE = "Outline update";

	private ConfigTextEditor editor;
	private IDocument document;

	public ConfigTextEditor getEditor() {
		return editor;
	}

	public void setEditor(final ConfigTextEditor editor) {
		this.editor = editor;
	}

	@Override
	public void setDocument(final IDocument document) {
		this.document = document;
	}

	@Override
	public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
		initialReconcile();
	}

	@Override
	public void reconcile(final IRegion partition) {
		initialReconcile();
	}

	@Override
	public void initialReconcile() {
		GlobalIntervalHandler.putInterval(document, null);
		IPreferencesService prefs = Platform.getPreferencesService();
		if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, true, null)) {
			analyze();
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					List<Position> positions = (new TTCN3FoldingSupport()).calculatePositions(document);
					editor.updateFoldingStructure(positions);
				}
			});
		}
	}

	private void analyze() {
		final IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);

		if (file == null) {
			return;
		}

		IProject project = file.getProject();
		if (project == null) {
			return;
		}

		ProjectConfigurationParser projectConfigurationParser = GlobalParser.getConfigSourceParser(project);
		projectConfigurationParser.reportOutdating(file);
		projectConfigurationParser.analyzeAll();
	}

	@Override
	public void setProgressMonitor(final IProgressMonitor monitor) {
	}

}
