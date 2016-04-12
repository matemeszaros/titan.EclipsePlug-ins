/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.referenceSearch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Szabolcs Beres
 * */
public class ReferenceSearchResultView extends AbstractTextSearchViewPage {
	private static class ReferenceSearchLabelProvider extends LabelProvider {
		private static final String ICON_MATCH = "match.gif";
		private final Image matchIcon;

		public ReferenceSearchLabelProvider() {
			super();
			matchIcon = ImageCache.getImage(ICON_MATCH);
		}

		@Override
		public Image getImage(final Object element) {
			if (element instanceof Match) {
				return matchIcon;
			}

			return WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider().getImage(element);
		}

		@Override
		public String getText(final Object element) {
			if (element instanceof IResource) {
				return WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider().getText(element);
			}

			if (element instanceof ReferenceSearchMatch) {
				final ReferenceSearchMatch refSearchMatch = (ReferenceSearchMatch) element;
				return refSearchMatch.getId().getDisplayName() + " at line " + refSearchMatch.getId().getLocation().getLine() + ".";
			}

			return "Unexpected element";
		}
	}

	private ReferenceSearchContentProvider contentProvider;

	public ReferenceSearchResultView() {
		super(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
	}

	@Override
	protected void elementsChanged(final Object[] objects) {
		if (contentProvider != null) {
			getViewer().refresh();
		}
	}

	@Override
	protected void clear() {
		getViewer().refresh();
	}

	@Override
	protected void configureTreeViewer(final TreeViewer viewer) {
		contentProvider = new ReferenceSearchContentProvider(this);

		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new ReferenceSearchLabelProvider());
		viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());

		getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				try {
					Object selectedElement = ((TreeSelection) event.getSelection()).getFirstElement();
					if (selectedElement instanceof ReferenceSearchMatch) {
						ReferenceSearchMatch match = (ReferenceSearchMatch) selectedElement;
						showMatch(match, match.getOffset(), match.getLength(), false);
						return;
					}
				} catch (PartInitException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}
		});
	}

	@Override
	protected void configureTableViewer(final TableViewer viewer) {
		// Do nothing
		// Not supported
	}

	@Override
	protected void showMatch(final Match match, final int currentOffset, final int currentLength) throws PartInitException {
		if (!(match instanceof ReferenceSearchMatch)) {
			return;
		}

		IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		if (selection != null && !selection.toList().contains(match)) {
			getViewer().setSelection(new StructuredSelection(match));
		}

		IFile file = (IFile) match.getElement();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
		if (desc == null) {
			return;
		}

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart part = page.openEditor(new FileEditorInput(file), desc.getId(), false);
		if (part != null && part instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) part;
			textEditor.selectAndReveal(currentOffset, currentLength);
		}
	}

}
