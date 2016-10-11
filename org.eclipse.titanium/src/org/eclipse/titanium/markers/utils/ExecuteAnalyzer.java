/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.designer.extensions.IProjectProcesser;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.handler.MarkerHandler;
import org.eclipse.titanium.preferences.PreferenceConstants;

/**
 * The extension class used by the titan designer.
 * <p>
 * Its purpose is to execute code smells (if the user asked so), and show up the
 * found code smell markers after the semantic analysis of the project done by
 * the titan.
 * 
 * @author poroszd
 * 
 */
public class ExecuteAnalyzer implements IProjectProcesser {
	private static boolean onTheFlyEnabled;
	static {
		final String titaniumId = Activator.PLUGIN_ID;
		final String onTheFlyName = PreferenceConstants.ON_THE_FLY_SMELLS;
		onTheFlyEnabled = Platform.getPreferencesService().getBoolean(titaniumId, onTheFlyName, false, null);

		if (Activator.getDefault() != null) {
			Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					final String property = event.getProperty();
					if (onTheFlyName.equals(property)) {
						onTheFlyEnabled = Platform.getPreferencesService().getBoolean(titaniumId, onTheFlyName, false, null);
					}
				}
			});
		}
	}

	@Override
	public void workOnProject(final IProgressMonitor monitor, final IProject project) {
		// If it is not enabled, do nothing
		if (!onTheFlyEnabled) {
			return;
		}
		// Otherwise analyze the project for code smells
		MarkerHandler mh;
		synchronized (project) {
			mh = AnalyzerCache.withPreference().analyzeProject(monitor, project);
		}
		// and show them
		mh.showAll();
	}
}
