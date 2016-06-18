/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.editors.ColorManager;
import org.eclipse.titan.designer.editors.EditorTracker;
import org.eclipse.titan.designer.editors.FoldingSupport;
import org.eclipse.titan.designer.editors.IEditorWithCarretOffset;
import org.eclipse.titan.designer.editors.ISemanticTITANEditor;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.FileSaveTracker;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.GlobalProjectStructureTracker;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author Kristof Szabados
 * */
public final class ASN1Editor extends AbstractDecoratedTextEditor implements ISemanticTITANEditor, IEditorWithCarretOffset {
	private static final String CONTENTASSISTPROPOSAL = "ContentAssistProposal.";
	private static final String ASN1_EDITOR = ProductConstants.PRODUCT_ID_DESIGNER + ".editors.asn1editor.ASN1Editor";
	private static final String EDITOR_CONTEXT = ProductConstants.PRODUCT_ID_DESIGNER + ".editors.asn1editor.context";
	private static final String EDITOR_SCOPE = ProductConstants.PRODUCT_ID_DESIGNER + ".editors.ASN1EditorScope";

	private ProjectionSupport projectionSupport;
	private List<Annotation> oldAnnotations = new ArrayList<Annotation>();
	private ProjectionAnnotationModel annotationModel;
	private ColorManager colorManager;
	private Configuration configuration;
	private ProjectionViewer projectionViewer;
	private OutlinePage outlinePage;
	private ReconcilingStrategy strategy;

	/** It can be null if the feature is turned off. */
	private ASN1OccurrenceMarker occurrencesMarker;

	private final IPropertyChangeListener foldingListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			final String property = event.getProperty();
			if (PreferenceConstants.FOLDING_ENABLED.equals(property) || PreferenceConstants.FOLD_COMMENTS.equals(property)
					|| PreferenceConstants.FOLD_STATEMENT_BLOCKS.equals(property)
					|| PreferenceConstants.FOLD_PARENTHESIS.equals(property)
					|| PreferenceConstants.FOLD_DISTANCE.equals(property)) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						updateFoldingStructure((new ASN1FoldingSupport()).calculatePositions(getDocument()));
					}
				});
			}
		}
	};

	public ASN1Editor() {
		super();

		occurrencesMarker = new ASN1OccurrenceMarker(this);
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		IPreferenceStore[] stores = { getPreferenceStore(), Activator.getDefault().getPreferenceStore() };
		setPreferenceStore(new ChainedPreferenceStore(stores));
		colorManager = new ColorManager();
		configuration = new Configuration(colorManager, this);
		setSourceViewerConfiguration(configuration);
		DocumentSetupParticipant participant = new DocumentSetupParticipant();
		ForwardingDocumentProvider forwardingProvider = new ForwardingDocumentProvider(PartitionScanner.ASN1_PARTITIONING, participant,
				new TextFileDocumentProvider());
		setDocumentProvider(forwardingProvider);
		setEditorContextMenuId(EDITOR_CONTEXT);
	}

	public boolean isSemanticCheckingDelayed() {
		IPreferencesService prefs = Platform.getPreferencesService();
		return prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DELAYSEMANTICCHECKINGTILLSAVE,
				false, null);
	}

	@Override
	public void doSave(final IProgressMonitor progressMonitor) {
		final IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
		if (file != null) {
			FileSaveTracker.fileBeingSaved(file);
		}

		super.doSave(progressMonitor);

		if (file != null && isSemanticCheckingDelayed()) {
			final WorkspaceJob op = new WorkspaceJob("Reconciliation on save") {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) {
					ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
					projectSourceParser.reportOutdating(file);

					strategy.analyze(true);

					return Status.OK_STATUS;
				}
			};
			op.setPriority(Job.LONG);
			op.setSystem(true);
			op.setUser(false);
			op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
			op.schedule();

		}
	}

	@Override
	protected boolean affectsTextPresentation(final PropertyChangeEvent event) {
		if (event.getProperty().startsWith(ProductConstants.PRODUCT_ID_DESIGNER)) {
			colorManager.update(event.getProperty());
			invalidateTextPresentation();
			updateTITANIndentPrefixes();
			return true;
		}
		return super.affectsTextPresentation(event);
	}

	@Override
	protected void configureSourceViewerDecorationSupport(final SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);
		PairMatcher pairMatcher = new PairMatcher();
		support.setCharacterPairMatcher(pairMatcher);
		support.setMatchingCharacterPainterPreferenceKeys(PreferenceConstants.MATCHING_BRACKET_ENABLED,
				PreferenceConstants.COLOR_MATCHING_BRACKET);
	}

	@Override
	protected void createActions() {
		super.createActions();

		Action caAction = new TextOperationAction(Activator.getDefault().getResourceBundle(), CONTENTASSISTPROPOSAL, this,
				ISourceViewer.CONTENTASSIST_PROPOSALS);
		String id = IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST;
		caAction.setActionDefinitionId(id);
		setAction(CONTENTASSISTPROPOSAL, caAction);
		markAsStateDependentAction(CONTENTASSISTPROPOSAL, true);
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
		if (IContentOutlinePage.class.equals(adapter)) {
			if (outlinePage == null) {
				outlinePage = new OutlinePage(this);
			}

			return outlinePage;
		}

		return super.getAdapter(adapter);
	}

	@Override
	public void dispose() {
		oldAnnotations = null;
		if (projectionSupport != null) {
			projectionSupport.dispose();
			projectionSupport = null;
		}
		if (outlinePage != null) {
			outlinePage.dispose();
			outlinePage = null;
		}
		annotationModel = null;
		configuration = null;
		projectionViewer = null;
		if(occurrencesMarker != null) {
			occurrencesMarker.dispose();
			occurrencesMarker = null;
		}
		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(foldingListener);

		IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
		if (file != null) {
			EditorTracker.remove(file, this);
		}

		super.dispose();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	protected void editorSaved() {
		super.editorSaved();
		IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
		if (file != null) {
			GlobalProjectStructureTracker.saveFile(file);
		}
	}

	@Override
	public IDocument getDocument() {
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer == null) {
			return null;
		}
		return sourceViewer.getDocument();
	}

	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);

		projectionViewer = (ProjectionViewer) getSourceViewer();

		projectionSupport = new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors());
		projectionSupport.install();

		projectionViewer.doOperation(ProjectionViewer.TOGGLE);

		annotationModel = projectionViewer.getProjectionAnnotationModel();

		getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				ASN1Editor.this.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(null);
			}
		});

		getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection.isEmpty() || !(selection instanceof TextSelection)
						|| "".equals(((TextSelection) selection).getText())) {
					return;
				}

				final TextSelection textSelection = (TextSelection) selection;
				final int offset = textSelection.getOffset() + textSelection.getLength();
				occurrencesMarker.markOccurences(getDocument(), offset);
			}
		});

		IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
		if (file != null) {
			EditorTracker.put(file, this);
		}
	}

	@Override
	protected void handleCursorPositionChanged() {
		super.handleCursorPositionChanged();

		occurrencesMarker.markOccurences(getDocument(), getCarretOffset());
	}

	@Override
	protected ISourceViewer createSourceViewer(final Composite parent, final IVerticalRuler ruler, final int styles) {
		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		getSourceViewerDecorationSupport(viewer);
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(foldingListener);

		// Context setting is placed here because getEditorSite() must
		// be called after the editor is initialized.
		IContextService contextService = (IContextService) getEditorSite().getService(IContextService.class);
		// As the service is retrieved from the editor instance it will
		// be active only within the editor.
		contextService.activateContext(EDITOR_SCOPE);

		return viewer;
	}

	@Override
	public void updateOutlinePage() {
		if (outlinePage != null) {
			outlinePage.update();
		}
	}

	public void refreshOutlinePage() {
		if (outlinePage != null) {
			outlinePage.refresh();
		}
	}

	@Override
	public void updateFoldingStructure(final List<Position> positions) {
		FoldingSupport.updateFoldingStructure(annotationModel, oldAnnotations, positions);
	}

	@Override
	public void invalidateTextPresentation() {
		ISourceViewer viewer = getSourceViewer();
		if (viewer != null) {
			viewer.invalidateTextPresentation();
		}
	}

	@Override
	public int getCarretOffset() {
		int widgetOffset = getSourceViewer().getTextWidget().getCaretOffset();
		return projectionViewer.widgetOffset2ModelOffset(widgetOffset);
	}

	public void setCarretOffset(final int i) {
		int temp = projectionViewer.modelOffset2WidgetOffset(i);
		getSourceViewer().getTextWidget().setCaretOffset(temp);
	}

	/**
	 * Updates the source viewer's indent prefixes with the values provided
	 * by the source viewer configuration.
	 * <p>
	 * The reason for the strange name is, that Eclipse 3.3 will have a
	 * updateIndentPrefixes function.
	 */
	protected void updateTITANIndentPrefixes() {
		SourceViewerConfiguration configuration = getSourceViewerConfiguration();
		ISourceViewer sourceViewer = getSourceViewer();
		String[] types = configuration.getConfiguredContentTypes(sourceViewer);
		for (int i = 0; i < types.length; i++) {
			String[] prefixes = configuration.getIndentPrefixes(sourceViewer, types[i]);
			if (prefixes != null && prefixes.length > 0) {
				sourceViewer.setIndentPrefixes(prefixes, types[i]);
			}
		}
	}

	/**
	 * Sets the reconciling strategy for this editor.
	 * 
	 * @param strategy
	 *                the strategy to be set.
	 * */
	public void setReconciler(final ReconcilingStrategy strategy) {
		this.strategy = strategy;
	}

	/**
	 * Finds a ASN.1 editor in the provided workbench
	 * 
	 * @param workbench
	 *                the workbench to search for an open editor.
	 * @return the editor found, or null
	 * */
	public static IEditorDescriptor findASN1Editor(final IWorkbench workbench) {
		return workbench.getEditorRegistry().findEditor(ASN1_EDITOR);
	}
}
