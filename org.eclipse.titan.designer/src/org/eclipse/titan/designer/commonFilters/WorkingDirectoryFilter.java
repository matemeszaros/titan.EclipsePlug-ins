/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.commonFilters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;

/**
 * @author Kristof Szabados
 * */
public final class WorkingDirectoryFilter extends org.eclipse.jface.viewers.ViewerFilter {
	private static final String FILTER_ID = "org.eclipse.titan.designer.commonFilters.WorkingDirectoryFilter";
	private static final String NAVIGATOR_QUALIFIER = "org.eclipse.ui.navigator";
	private static final String FILTER_ACTIVATION_KEY = "org.eclipse.ui.navigator.ProjectExplorer.filterActivation";

	private IContainer[] workingDirectories = null;

	private static boolean isActive = false;
	private static boolean isActiveSet = false;

	public WorkingDirectoryFilter() {
		// Do nothing
	}

	private static void checkIsActive() {
		IEclipsePreferences rootNode = (IEclipsePreferences) Platform.getPreferencesService().getRootNode().node(InstanceScope.SCOPE)
				.node(NAVIGATOR_QUALIFIER);
		rootNode.addPreferenceChangeListener(new IPreferenceChangeListener() {

			@Override
			public void preferenceChange(final PreferenceChangeEvent event) {
				if (FILTER_ACTIVATION_KEY.equals(event.getKey())) {
					String temp = Platform.getPreferencesService()
							.getString(NAVIGATOR_QUALIFIER, FILTER_ACTIVATION_KEY, "", null);
					isActive = temp.indexOf(FILTER_ID) != -1;
					isActiveSet = true;
				}
			}
		});

		String temp = Platform.getPreferencesService().getString(NAVIGATOR_QUALIFIER, FILTER_ACTIVATION_KEY, "", null);
		isActive = temp.indexOf(FILTER_ID) != -1;
		isActiveSet = true;
	}

	/**
	 * @return whether this filter is active at the time of calling or not.
	 * */
	public static boolean isActive() {
		if (!isActiveSet) {
			checkIsActive();
		}
		return isActive;
	}

	@Override
	public Object[] filter(final Viewer viewer, final Object parent, final Object[] elements) {
		workingDirectories = null;
		Object tempParent = parent;
		if (parent instanceof TreePath) {
			tempParent = ((TreePath) parent).getLastSegment();
		}

		if (!(tempParent instanceof IResource)) {
			return elements;
		}

		IResource parentResource = (IResource) tempParent;
		IProject project = parentResource.getProject();
		if (project == null) {
			return elements;
		}

		workingDirectories = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryResources(false);

		int size = elements.length;
		List<Object> out = new ArrayList<Object>(size);
		for (int i = 0; i < size; ++i) {
			Object element = elements[i];
			if (specialSelect(viewer, tempParent, element)) {
				out.add(element);
			}
		}
		return out.toArray();
	}

	@Override
	public Object[] filter(final Viewer viewer, final TreePath parentPath, final Object[] elements) {
		workingDirectories = null;

		return super.filter(viewer, parentPath, elements);
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (!(element instanceof IFolder)) {
			return true;
		}

		IFolder folder = (IFolder) element;
		IProject project = folder.getProject();
		IContainer[] workingDirectories = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryResources(false);
		for (IContainer workingDirectory : workingDirectories) {
			if (workingDirectory.equals(folder)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns whether the given element makes it through this filter.
	 * Special version assuming that the working directory of the project
	 * was already calculated.
	 * 
	 * @param viewer
	 *                the viewer
	 * @param parentElement
	 *                the parent element
	 * @param element
	 *                the element
	 * @return <code>true</code> if element is included in the filtered set,
	 *         and <code>false</code> if excluded
	 */
	private boolean specialSelect(final Viewer viewer, final Object parentElement, final Object element) {
		if (!(element instanceof IFolder) || workingDirectories == null || workingDirectories.length == 0) {
			return true;
		}

		IFolder folder = (IFolder) element;
		for (IContainer workingDirectory : workingDirectories) {
			if (workingDirectory.equals(folder)) {
				return false;
			}
		}

		return true;
	}
}
