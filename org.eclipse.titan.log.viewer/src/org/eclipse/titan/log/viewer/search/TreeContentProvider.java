/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.search;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchEvent;
import org.eclipse.search.ui.text.RemoveAllEvent;
import org.eclipse.titan.log.viewer.views.LogSearchResultPage;

public class TreeContentProvider implements ITreeContentProvider {

	private class SearchResultListener implements ISearchResultListener {
		@Override
		public void searchResultChanged(final SearchResultEvent e) {
			
			if (e instanceof MatchEvent) {
				MatchEvent event = (MatchEvent) e;
				if (event.getKind() == MatchEvent.ADDED) {
					addMatch(event);
				}

				if (event.getKind() == MatchEvent.REMOVED) {
					removeMatch(event);
				}
				return;
			}
				
			if (e instanceof RemoveAllEvent) {
				tree.clear();
			}
		}

		private boolean removeMatch(MatchEvent event) {
			for (Match match : event.getMatches()) {
				Object child = match.getElement();
				if (getChildren(child).length != 0) {
					return true;
				}
				for (Object parent = getParent(child); parent != null; child = getParent(child), parent = getParent(parent)) {
					List<IResource> children = tree.get(parent);
					if (children != null) {
						children.remove(child);
						if (!children.isEmpty()) {
							break;
						}
					}
					tree.remove(parent);
				}
			}
			return false;
		}

		private void addMatch(MatchEvent event) {
			for (Match match : event.getMatches()) {
				Object child = match.getElement();
				for (Object parent = getParent(child); parent != null; child = parent, parent = getParent(parent)) {
					 if (!addTreeElement((IResource) parent, (IResource) child)) {
						 break;
					 }
				}
			}
		}
	}

	private LogSearchResult result;
	private LogSearchResultPage page;
	private SearchResultListener searchResultListener;
	
	private Map<IResource, List<IResource>> tree;
	
	
	public TreeContentProvider(final LogSearchResultPage page) {
		tree = new HashMap<IResource, List<IResource>>();
		searchResultListener = new SearchResultListener();
		if (page != null && page.getInput() != null) {
			this.page = page;
			setResult((LogSearchResult) page.getInput());
		}
	}
	
	@Override
	public void dispose() {
		// Do nothig
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		tree = new HashMap<IResource, List<IResource>>();
		if (newInput instanceof LogSearchResult) {
			setResult((LogSearchResult) newInput);
		}
		
		if (newInput instanceof LogSearchResultPage) {
			page = ((LogSearchResultPage) newInput);
		}
	}
	
	protected void setResult(final LogSearchResult result) {
		tree = new HashMap<IResource, List<IResource>>();
		this.result = result;
		for (Object child : result.getElements()) {
			for (Object parent = getParent(child); parent != null; child = parent, parent = getParent(parent)) {
				 if (!addTreeElement((IResource) parent, (IResource) child)) { 
					 break;
				 }
			}
		}
		result.addListener(searchResultListener);
	}
	
	protected LogSearchResultPage getPage() {
		return page;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}
	
	public List<IProject> getProjects() {
		List<IProject> result = new ArrayList<IProject>();
		for (IResource resource : tree.keySet()) {
			if (resource instanceof IProject) {
				result.add((IProject) resource);
			}
		}
		return result;
	}
	
	@Override
	public Object[] getChildren(final Object parentElement) {
		
		if (parentElement instanceof LogSearchResult) {
			return getProjects().toArray();
		}
		
		if (parentElement instanceof IFile) {
			return result.getMatches(parentElement);
		}
		
		if (parentElement instanceof IResource) {
			List<IResource> result = tree.get(parentElement);
			return result == null ? new Object[0] : result.toArray();  
		}
		
		return new Object[0];
	}
 
	@Override
	public Object getParent(final Object element) {
		if (element instanceof IResource) {
			return ((IResource) element).getParent();
		}
		
		if (element instanceof Match) {
			((Match) element).getElement();
		}	
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof IFile) {
			return result.getMatchCount(element) > 0;
		}
		
		return tree.get(element) != null;
	}


	public synchronized void elementsChanged(final Object[] updatedElements) {
		getPage().getViewer().refresh();
	}
	
	public void removeAll() {
		result.removeAll();
	}
	
	private boolean addTreeElement(final IResource parent, final IResource child) {
		List<IResource> children = tree.get(parent);
		if (children == null) {
			children = new ArrayList<IResource>();
			children.add(child);
			tree.put(parent, children);
			return true;
		}
		
		if (!children.contains(child)) {
			children.add(child);
			return true;
		}
		
		return false;
	}
}
