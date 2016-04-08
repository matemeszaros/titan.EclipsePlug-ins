/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.commonFilters;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Kristof Szabados
 * */
public final class ExcludedResourceFilter extends org.eclipse.jface.viewers.ViewerFilter {
	private static final String FILTER_ID = "org.eclipse.titan.designer.commonFilters.ExcludedResourceFilter";
	private static final String NAVIGATOR_QUALIFIER = "org.eclipse.ui.navigator";
	private static final String FILTER_ACTIVATION_KEY = "org.eclipse.ui.navigator.ProjectExplorer.filterActivation";

	private static boolean isActive = false;
	private static boolean isActiveSet = false;

	public ExcludedResourceFilter() {
		// Do nothing
	}

	private static void checkIsActive() {
		IEclipsePreferences rootNode = (IEclipsePreferences) Platform.getPreferencesService().getRootNode().node(InstanceScope.SCOPE).node(NAVIGATOR_QUALIFIER);
		rootNode.addPreferenceChangeListener(new IPreferenceChangeListener() {

			@Override
			public void preferenceChange(final PreferenceChangeEvent event) {
				if (FILTER_ACTIVATION_KEY.equals(event.getKey())) {
					String temp = Platform.getPreferencesService().getString(NAVIGATOR_QUALIFIER, FILTER_ACTIVATION_KEY, "", null);
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
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (element instanceof IFile) {
			IFile file = (IFile) element;
			ResourceExclusionHelper helper = new ResourceExclusionHelper();
			if (helper.isExcludedByRegexp(file.getName())) {
				return false;
			}
			if ('.' == file.getName().charAt(0)) {
				return false;
			}
			return !ResourceExclusionHelper.isDirectlyExcluded(file);
		} else if (element instanceof IFolder) {
			IFolder folder = (IFolder) element;
			ResourceExclusionHelper helper = new ResourceExclusionHelper();
			if (helper.isExcludedByRegexp(folder.getName())) {
				return false;
			}
			if ('.' == folder.getName().charAt(0)) {
				return false;
			}
			return !ResourceExclusionHelper.isDirectlyExcluded(folder);
		}

		return true;
	}

}
