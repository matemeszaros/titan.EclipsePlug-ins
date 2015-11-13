/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import org.eclipse.titan.designer.editors.Pair;
import org.eclipse.titan.designer.editors.ToggleComment;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextOperationAction;

/**
 * @author Kristof Szabados
 * */
public final class ConfigTextEditor extends AbstractDecoratedTextEditor implements ISemanticTITANEditor, IEditorWithCarretOffset {
	private static final String CONTENTASSISTPROPOSAL = "ContentAssistProposal.";
	private static final String CONFIG_EDITOR = ProductConstants.PRODUCT_ID_DESIGNER + ".editors.configeditor.ConfigEditor";
	private static final String EDITOR_CONTEXT = ProductConstants.PRODUCT_ID_DESIGNER + ".editors.configeditor.context";
	private static final String EDITOR_SCOPE = ProductConstants.PRODUCT_ID_DESIGNER + ".editors.ConfigEditorScope";
	private static final String TOGGLE_COMMENT_ACTION_ID = ProductConstants.PRODUCT_ID_DESIGNER + ".editors.configeditor.ToggleComment";

	private ProjectionSupport projectionSupport;
	private List<Annotation> oldAnnotations = new ArrayList<Annotation>();
	private ProjectionAnnotationModel annotationModel;
	private ColorManager colorManager;
	private Configuration configuration;
	private ProjectionViewer projectionViewer;

	// the multipage editor which this text editor is added to
	private ConfigEditor parentEditor;

	private IPropertyChangeListener foldingListener = new IPropertyChangeListener() {
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
						updateFoldingStructure((new ConfigFoldingSupport()).calculatePositions(getDocument()));
					}
				});
			}
		}
	};

	public ConfigTextEditor(final ConfigEditor parentEditor) {
		this.parentEditor = parentEditor;
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
		ForwardingDocumentProvider forwardingProvider = new ForwardingDocumentProvider(PartitionScanner.CONFIG_PARTITIONING, participant,
				new TextFileDocumentProvider());
		setDocumentProvider(forwardingProvider);
		setEditorContextMenuId(EDITOR_CONTEXT);
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
		Pair brackets = new Pair('{', '}');
		Pair parenthesis = new Pair('(', ')');
		Pair index = new Pair('[', ']');
		PairMatcher pairMatcher = new PairMatcher(new Pair[] { brackets, parenthesis, index });
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

		ToggleComment tcAction = new ToggleComment(Activator.getDefault().getResourceBundle(), "ToggleComment.", this);
		tcAction.setActionDefinitionId(TOGGLE_COMMENT_ACTION_ID);
		setAction(TOGGLE_COMMENT_ACTION_ID, tcAction);
		markAsStateDependentAction(TOGGLE_COMMENT_ACTION_ID, true);
		tcAction.configure(getSourceViewer(), getSourceViewerConfiguration());
		tcAction.setText("Toggle Comment");
		tcAction.setImageDescriptor(ImageCache.getImageDescriptor("titan.gif"));
	}

	@Override
	public void dispose() {
		oldAnnotations = null;
		projectionSupport.dispose();
		annotationModel = null;
		configuration = null;
		projectionViewer = null;
		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(foldingListener);

		IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
		EditorTracker.remove(file, this);

		super.dispose();
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
				ConfigTextEditor.this.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(null);
			}
		});

		IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
		EditorTracker.put(file, this);
	}

	@Override
	protected ISourceViewer createSourceViewer(final Composite parent, final IVerticalRuler ruler, final int styles) {
		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		getSourceViewerDecorationSupport(viewer);
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(foldingListener);

		// Context setting is placed here because getEditorSite() must
		// be called after the editor is
		// initialized.
		IContextService contextService = (IContextService) getEditorSite().getService(IContextService.class);
		// As the service is retrieved from the editor instance it will
		// be active only within the editor.
		contextService.activateContext(EDITOR_SCOPE);
		return viewer;
	}

	@Override
	protected void editorContextMenuAboutToShow(final IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, IWorkbenchActionConstants.MB_ADDITIONS, TOGGLE_COMMENT_ACTION_ID);
	}

	@Override
	public void updateOutlinePage() {
		// the configuration editor is not yet providing an outline page
	}

	public void refreshOutlinePage() {
		// the configuration editor is not yet providing an outline page
	}

	/**
	 * Updates the folding structure.
	 * <p>
	 * Works as a temporary call, that receives the positions of the new
	 * foldable positions, and adds the annotationmodel and old foldable
	 * positions of the actual editor
	 * 
	 * @param positions
	 *                The new folding regions
	 */
	@Override
	public void updateFoldingStructure(final List<Position> positions) {
		FoldingSupport.updateFoldingStructure(annotationModel, oldAnnotations, positions);
	}

	/**
	 * Invalidates the presentation of text inside this editor, forcing the
	 * editor to redraw itself.
	 * <p>
	 * This function practically enables the onthefly parser to redraw the
	 * texts, according to the information it has collected
	 * */
	@Override
	public void invalidateTextPresentation() {
		ISourceViewer viewer = getSourceViewer();
		if (viewer != null) {
			viewer.invalidateTextPresentation();
		}
	}

	/**
	 * Updates the source viewer's indent prefixes with the values provided
	 * by the source viewer configuration.
	 * <p>
	 * The reason for the strange name is, that Eclipse 3.3 will have a
	 * updateIndentPrefixes function.
	 * 
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

	public ConfigEditor getParentEditor() {
		return parentEditor;
	}

	// Stolen from TTCN3Editor.
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
	 * Finds a CFG editor in the provided workbench
	 * 
	 * @param workbench
	 *                the workbench to search for an open editor.
	 * @return the editor found, or null
	 * */
	public static IEditorDescriptor findCFGEditor(final IWorkbench workbench) {
		return workbench.getEditorRegistry().findEditor(CONFIG_EDITOR);
	}
}
