/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.referenceSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

/**
 * @author Szabolcs Beres
 * */
public class ReferenceSearchContentProvider implements ITreeContentProvider {

	private class SearchResultListener implements ISearchResultListener {
		@Override
		public void searchResultChanged(final SearchResultEvent e) {

			if (e instanceof RemoveAllEvent) {
				tree.clear();
				return;
			}

			if (!(e instanceof MatchEvent)) {
				return;
			}

			MatchEvent event = (MatchEvent) e;
			switch (event.getKind()) {
			case MatchEvent.ADDED: {
				for (Match match : event.getMatches()) {
					Object child = match.getElement();
					for (Object parent = getParent(child); parent != null; child = parent, parent = getParent(parent)) {
						if (!addTreeElement((IResource) parent, (IResource) child)) {
							break;
						}
					}
				}
				break;
			}
			case MatchEvent.REMOVED: {
				for (Match match : event.getMatches()) {
					Object child = match.getElement();
					if (getChildren(child).length != 0) {
						return;
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
				break;
			}
			default:
				break;
			} // switch
		}
	} // SearchResultListener

	private HashMap<IResource, ArrayList<IResource>> tree;
	private SearchResultListener searchResultListener;
	private ReferenceSearchResult result;
	private ReferenceSearchResultView page;

	public ReferenceSearchContentProvider(final ReferenceSearchResultView page) {
		tree = new HashMap<IResource, ArrayList<IResource>>();
		searchResultListener = new SearchResultListener();
		if (page != null && page.getInput() != null) {
			this.page = page;
			setResult((ReferenceSearchResult) page.getInput());
		}
	}

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		tree = new HashMap<IResource, ArrayList<IResource>>();
		if (newInput instanceof ReferenceSearchResult) {
			setResult((ReferenceSearchResult) newInput);
		}

		if (newInput instanceof ReferenceSearchResultView) {
			page = ((ReferenceSearchResultView) newInput);
		}
	}

	protected void setResult(final ReferenceSearchResult result) {
		tree = new HashMap<IResource, ArrayList<IResource>>();
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
	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(final Object parentElement) {

		if (parentElement instanceof ReferenceSearchResult) {
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

	public void removeAll() {
		result.removeAll();
	}

	private boolean addTreeElement(final IResource parent, final IResource child) {
		ArrayList<IResource> children = tree.get(parent);
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

	protected ReferenceSearchResultView getPage() {
		return page;
	}
}
