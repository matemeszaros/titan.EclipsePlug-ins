/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.referenceSearch;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.ui.IEditorPart;

/**
 * @author Szabolcs Beres
 * */
public class ReferenceSearchResult extends AbstractTextSearchResult implements IFileMatchAdapter, IEditorMatchAdapter {

	private static final String ICON_SEARCH = "search.gif";

	private ReferenceSearchQuery query;

	public ReferenceSearchResult(final ReferenceSearchQuery query) {
		super();
		this.query = query;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchResult#getLabel()
	 */
	@Override
	public String getLabel() {
		if (query != null) {
			return query.getSearchPattern() + " - " + super.getMatchCount() + " Matches";
		}
		return "Search result";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchResult#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageCache.getImageDescriptor(ICON_SEARCH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.text.IEditorMatchAdapter
	 * #isShownInEditor(org.eclipse.search.ui.text.Match,
	 * org.eclipse.ui.IEditorPart)
	 */
	@Override
	public boolean isShownInEditor(final Match match, final IEditorPart editor) {
		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (match.getElement().equals(file)) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.text.IEditorMatchAdapter
	 * #computeContainedMatches
	 * (org.eclipse.search.ui.text.AbstractTextSearchResult,
	 * org.eclipse.ui.IEditorPart)
	 */
	@Override
	public Match[] computeContainedMatches(final AbstractTextSearchResult result, final IEditorPart editor) {
		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		return computeContainedMatches(result, file);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.search.ui.text.IFileMatchAdapter#computeContainedMatches
	 * (org.eclipse.search.ui.text.AbstractTextSearchResult,
	 * org.eclipse.core.resources.IFile)
	 */
	@Override
	public Match[] computeContainedMatches(final AbstractTextSearchResult result, final IFile file) {
		return result.getMatches(file);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchResult#getTooltip()
	 */
	@Override
	public String getTooltip() {
		return "Search result";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.search.ui.text.IFileMatchAdapter#getFile(java.lang.Object
	 * )
	 */
	@Override
	public IFile getFile(final Object element) {
		if (element instanceof Match) {
			return (IFile) ((Match) element).getElement();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchResult#getQuery()
	 */
	@Override
	public ISearchQuery getQuery() {
		return query;
	}

	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	@Override
	public Match[] getMatches(final Object element) {
		if (element instanceof Match) {
			Match[] result = { (Match) element };
			return result;
		}
		return super.getMatches(element);
	}

	@Override
	public int getMatchCount(final Object element) {
		if (element instanceof Match) {
			return 1;
		}
		return super.getMatchCount(element);
	}
}
