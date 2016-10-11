/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.search;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.core.resources.IFile;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.ui.IEditorPart;

public class LogSearchResult extends AbstractTextSearchResult 
	implements IFileMatchAdapter, IEditorMatchAdapter {
	
	private LogSearchQuery query;

	public LogSearchResult(final LogSearchQuery query) {
		super();
		this.query = query;
	}

	@Override
	public String getLabel() {
		if (query != null && query.getPattern() != null) {
			return query.getPattern().getSearchString() + " - " + super.getMatchCount() + " Matches";
		}
		return "Search result";
	}

	@Override
	public String getTooltip() {
		return "Search result";
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor(Constants.ICONS_SEARCH);
	}

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
	public Match[] computeContainedMatches(final AbstractTextSearchResult result, final IFile file) {
		return this.getMatches(file);
	}

	@Override
	public IFile getFile(final Object element) {
		if (element instanceof Match) {
			return (IFile) ((Match) element).getElement();
		}
		return null;
	}
	
	@Override
	public Match[] getMatches(final Object element) {
		if (element instanceof Match) {
			return new Match[]{(Match) element};
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

	@Override
	public boolean isShownInEditor(final Match match, final IEditorPart editor) {
		return false;
	}

	@Override
	public Match[] computeContainedMatches(final AbstractTextSearchResult result, final IEditorPart editor) {
		return new Match[0];
	}

}
