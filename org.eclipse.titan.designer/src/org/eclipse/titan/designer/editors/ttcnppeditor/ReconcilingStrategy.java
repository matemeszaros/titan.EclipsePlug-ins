/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcnppeditor;

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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3FoldingSupport;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.GlobalIntervalHandler;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectConfigurationParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * @author Kristof Szabados
 * */
public final class ReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
	public static final String OUTLINEUPDATE = "Outline update";

	private TTCNPPEditor editor;
	private IDocument document;
	private StringBuilder actualCode;

	public TTCNPPEditor getEditor() {
		return editor;
	}

	public void setEditor(final TTCNPPEditor editor) {
		this.editor = editor;
	}

	@Override
	public void setDocument(final IDocument document) {
		this.document = document;
		actualCode = new StringBuilder(document.get());
	}

	@Override
	public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
		if (editor == null || document == null) {
			return;
		}

		int lineBreaks = 0;
		try {
			if (DirtyRegion.INSERT.equals(dirtyRegion.getType())) {
				actualCode.insert(dirtyRegion.getOffset(), dirtyRegion.getText());
				lineBreaks = org.eclipse.titan.designer.editors.ttcn3editor.ReconcilingStrategy.calculateLineBreaks(
						dirtyRegion.getText(), document.getLegalLineDelimiters());
			} else {
				lineBreaks = org.eclipse.titan.designer.editors.ttcn3editor.ReconcilingStrategy.calculateLineBreaks(
						actualCode.substring(dirtyRegion.getOffset(), dirtyRegion.getOffset() + dirtyRegion.getLength()),
						document.getLegalLineDelimiters());
				actualCode.delete(dirtyRegion.getOffset(), dirtyRegion.getOffset() + dirtyRegion.getLength());
			}
		} catch (StringIndexOutOfBoundsException e) {
			ErrorReporter.logExceptionStackTrace(e);
			ErrorReporter.logError("String length: " + actualCode.length() + " region type: " + dirtyRegion.getType()
					+ " region offset: " + dirtyRegion.getOffset() + " region length: " + dirtyRegion.getLength()
					+ " region text: '" + dirtyRegion.getText() + "'\n" + "Actual size of the document: "
					+ document.get().length());
			actualCode = new StringBuilder(document.get());
		}

		if (dirtyRegion.getOffset() == 0 && document.getLength() == dirtyRegion.getLength()) {
			// The editor window was closed, we don't have to do a
			// thing
			if (!editor.isDirty()) {
				return;
			}

			IPreferencesService prefs = Platform.getPreferencesService();
			if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, true, null)) {
				analyze(false);
			}
			return;
		}

		int firstLine;
		try {
			firstLine = document.getLineOfOffset(dirtyRegion.getOffset());
		} catch (BadLocationException e) {
			ErrorReporter.logWarningExceptionStackTrace(e);
			firstLine = 0;
		}

		final IFile editedFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (editedFile == null) {
			return;
		}

		final TTCN3ReparseUpdater reparser;
		int length = dirtyRegion.getLength();

		if (DirtyRegion.INSERT.equals(dirtyRegion.getType())) {
			reparser = new TTCN3ReparseUpdater(editedFile, actualCode.toString(), firstLine + 1, lineBreaks, dirtyRegion.getOffset(),
					dirtyRegion.getOffset(), length);
		} else {
			reparser = new TTCN3ReparseUpdater(editedFile, actualCode.toString(), firstLine + 1, -1 * lineBreaks,
					dirtyRegion.getOffset(), dirtyRegion.getOffset() + length, -1 * length);
		}

		final IProject project = editedFile.getProject();
		if (project == null) {
			return;
		}

		final ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(project);
		sourceParser.updateSyntax(editedFile, reparser);
		sourceParser.addModulesToBeSemanticallyAnalyzed(reparser.moduleToBeReanalysed);
		if (reparser.fullAnalysysNeeded) {
			sourceParser.setFullSemanticAnalysisNeeded();
		}
		if (!editor.isSemanticCheckingDelayed()) {
			sourceParser.analyzeAll();

			WorkspaceJob op = new WorkspaceJob(OUTLINEUPDATE) {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							List<Position> positions = (new TTCN3FoldingSupport()).calculatePositions(document);
							editor.updateFoldingStructure(positions);
							editor.refreshOutlinePage();
						}
					});
					return Status.OK_STATUS;
				}
			};
			op.setPriority(Job.LONG);
			op.setSystem(true);
			op.setUser(false);
			op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
			op.setRule(project);
			op.schedule();
		} else {
			sourceParser.reportSyntacticOutdatingOnly(editedFile);
			sourceParser.analyzeAllOnlySyntactically();
		}
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
		actualCode = new StringBuilder(document.get());
		
		GlobalIntervalHandler.putInterval(document, null);
		IPreferencesService prefs = Platform.getPreferencesService();
		if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, true, null)) {
			analyze(is_initial);
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					List<Position> positions = (new TTCN3FoldingSupport()).calculatePositions(document);
					editor.updateFoldingStructure(positions);
					editor.updateOutlinePage();
				}
			});
		}
	}

	public void analyze(final boolean is_initial) {
		final IFile editedFile = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (editedFile == null || ResourceExclusionHelper.isExcluded(editedFile)) {
			return;
		}

		IProject project = editedFile.getProject();
		if (project == null) {
			return;
		}

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				List<Position> positions = (new TTCN3FoldingSupport()).calculatePositions(document);
				getEditor().updateFoldingStructure(positions);
			}
		});

		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		projectSourceParser.setFullSemanticAnalysisNeeded();
		if (is_initial || !editor.isSemanticCheckingDelayed()) {
			projectSourceParser.reportOutdating(editedFile);
			projectSourceParser.analyzeAll();

			ProjectConfigurationParser projectConfigurationParser = GlobalParser.getConfigSourceParser(project);
			projectConfigurationParser.analyzeAll();

			WorkspaceJob op = new WorkspaceJob(OUTLINEUPDATE) {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (!MarkerHandler.hasMarker(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, editedFile)) {
								getEditor().updateOutlinePage();
							}
						}
					});
					return Status.OK_STATUS;
				}
			};
			op.setPriority(Job.LONG);
			op.setSystem(true);
			op.setUser(false);
			op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
			op.setRule(project);
			op.schedule();
		} else {
			projectSourceParser.reportSyntacticOutdatingOnly(editedFile);
			projectSourceParser.analyzeAllOnlySyntactically();
		}
	}

	@Override
	public void setProgressMonitor(final IProgressMonitor monitor) {
	}

}
