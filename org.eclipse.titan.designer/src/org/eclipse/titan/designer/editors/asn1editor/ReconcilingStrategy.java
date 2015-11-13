/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.GlobalIntervalHandler;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectConfigurationParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * @author Kristof Szabados
 * */
public final class ReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
	public static final String OUTLINEUPDATE = "Outline update";

	private ASN1Editor editor;
	private IDocument document;

	public ASN1Editor getEditor() {
		return editor;
	}

	public void setEditor(final ASN1Editor editor) {
		this.editor = editor;
	}

	@Override
	public void setDocument(final IDocument document) {
		this.document = document;
	}

	@Override
	public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
		fullReconciliation(false);
	}

	@Override
	public void reconcile(final IRegion partition) {
		fullReconciliation(false);
	}

	@Override
	public void initialReconcile() {
		fullReconciliation(true);
	}

	private void fullReconciliation(final boolean is_initial) {
		GlobalIntervalHandler.putInterval(document, null);
		IPreferencesService prefs = Platform.getPreferencesService();
		if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, true, null)) {
			analyze(is_initial);
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					List<Position> positions = (new ASN1FoldingSupport()).calculatePositions(document);
					editor.updateFoldingStructure(positions);
					final IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
					if (!MarkerHandler.hasMarker(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, file) ||
						!MarkerHandler.hasMarker(GeneralConstants.ONTHEFLY_MIXED_MARKER, file)) {
						getEditor().updateOutlinePage();
					}
				}
			});
		}
	}

	void analyze(final boolean is_initial) {
		final IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);

		if (file == null) {
			return;
		}

		IProject project = file.getProject();
		if (project == null) {
			return;
		}

		ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				List<Position> positions = (new ASN1FoldingSupport()).calculatePositions(document);
				getEditor().updateFoldingStructure(positions);
			}
		});

		projectSourceParser.reportOutdating(file);

		if (is_initial || !editor.isSemanticCheckingDelayed()) {
			projectSourceParser.setFullSemanticAnalysisNeeded();
			projectSourceParser.analyzeAll();
			ProjectConfigurationParser projectConfigurationParser = GlobalParser.getConfigSourceParser(project);
			projectConfigurationParser.analyzeAll();

			WorkspaceJob op = new WorkspaceJob(OUTLINEUPDATE) {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (!MarkerHandler.hasMarker(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, file) ||
								!MarkerHandler.hasMarker(GeneralConstants.ONTHEFLY_MIXED_MARKER, file)	) {
								getEditor().updateOutlinePage();
							}
						}
					});
					return Status.OK_STATUS;
				}
			};
			op.setPriority(Job.LONG);
			op.setUser(true);
			op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
			op.setRule(project);
			op.schedule();
		} else {
			projectSourceParser.analyzeAllOnlySyntactically();
		}
	}

	@Override
	public void setProgressMonitor(final IProgressMonitor monitor) {
	}

}
