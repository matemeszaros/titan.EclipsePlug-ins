/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.search.SearchLabelProvider;
import org.eclipse.titan.log.viewer.search.TreeContentProvider;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.text.table.TextTableView;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class LogSearchResultPage extends AbstractTextSearchViewPage {
	private TreeViewer treeViewer;
	private TreeContentProvider contentProvider;

	public LogSearchResultPage() {
		super(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
	}

	@Override
	protected void elementsChanged(final Object[] objects) {
		if (contentProvider != null) {
			this.treeViewer.refresh();
		}
	}

	@Override
	protected void clear() {
		treeViewer.refresh();
	}

	@Override
	protected void configureTreeViewer(final TreeViewer viewer) {
		treeViewer = viewer;	
		contentProvider = new TreeContentProvider(this);
		
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new SearchLabelProvider());
		viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				try {
					Object selectedElement = ((TreeSelection) event.getSelection()).getFirstElement();
					if (selectedElement instanceof Match) {
						showMatch((Match) selectedElement, 0, 0, true);
						
						return;
					}
				} catch (PartInitException e) {
					ErrorReporter.logExceptionStackTrace(e);
					TitanLogExceptionHandler.handleException(new TechnicalException("Could not show the selected match. Reason: " + e.getMessage()));  //$NON-NLS-1$
				}
			}
		});
	}

	@Override
	protected void configureTableViewer(final TableViewer viewer) {
	}

	@Override
	public StructuredViewer getViewer() {
		return treeViewer;
	}
	
	@Override
	protected void showMatch(final Match match, final int currentOffset, final int currentLength, final boolean activate) throws PartInitException {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		if (!selection.toList().contains(match)) {
			treeViewer.setSelection(new StructuredSelection(match));
		}
		
		IFile logFile = (IFile) match.getElement();
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		TextTableView openedView;
		IViewReference viewReference = activePage.findViewReference(Constants.TEXT_TABLE_VIEW_ID, logFile.getFullPath().toOSString());
		if (viewReference == null) {
			openTextTableView(logFile, match.getOffset());
			return;
		}
		
		TextTableView view = (TextTableView) viewReference.getView(false);
		if (view != null && view.isFiltered() && !view.contains(match.getOffset())) {
			MessageBox mb = new MessageBox(this.getSite().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			mb.setText("Filtered match");
			mb.setMessage("The selected record can not be displayed in the opened TextTableView." 
					+ " Would you like to open a new view? (The old one will be closed)");
			switch(mb.open()) {
			case SWT.YES:
				activePage.hideView(viewReference);
				openTextTableView(logFile, match.getOffset());
				break;
			case SWT.NO:
				break;
			default:
				return;
			}
			return;
		}

		openedView = (TextTableView) activePage.showView(Constants.TEXT_TABLE_VIEW_ID,
																		logFile.getFullPath().toOSString(),
																		org.eclipse.ui.IWorkbenchPage.VIEW_VISIBLE);
		openedView.setSelectedRecord(match.getOffset());
	}
	
	private TextTableView openTextTableView(final IFile logFile, final int recordToSelect) {
		try {
			TextTableView part = (TextTableView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(Constants.TEXT_TABLE_VIEW_ID, logFile.getFullPath().toOSString(), IWorkbenchPage.VIEW_VISIBLE);
			part.setInput(logFile, recordToSelect);
			return part;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("OpenTextTableProjectsViewMenuAction.5") + e.getMessage()));  //$NON-NLS-1$
		} catch (PartInitException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("OpenTextTableProjectsViewMenuAction.6") + e.getMessage()));  //$NON-NLS-1$
		} catch (ClassNotFoundException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("OpenTextTableProjectsViewMenuAction.7") + e.getMessage()));  //$NON-NLS-1$
		}
		return null;
	}

}
