/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

/**
 * This class can be used to acquire metric datas for a certain project.
 * It implements a strange singleton, as the objects are global and unique only for one project.
 * But more objects for several projects may exist (only one per project)
 * 
 * @author Gabor Jenei
 */
public final class WrapperStore {
	private static Map<IProject, ModuleMetricsWrapper> wrappers = new HashMap<IProject, ModuleMetricsWrapper>();

	private WrapperStore() {
		// Hide constructor
	}

	/**
	 * This method can be used to get the {@link ModuleMetricsWrapper} object for a certain project.
	 * 
	 * @param project : The project where we should calculate (or simply return) metrics values
	 * @return A new wrapper object if it doesn't exist for this project, the already constructed object otherwise
	 */
	public static ModuleMetricsWrapper getWrapper(final IProject project) {
		ModuleMetricsWrapper ret = wrappers.get(project);

		if (ret == null) {
			ret = new ModuleMetricsWrapper(project);
			wrappers.put(project, ret);
		}

		return ret;
	}
	
	/**
	 * This method clears the store, so all the metrics will be recalculated upon the first request
	 */
	public static void clearStore() {
		wrappers = new HashMap<IProject, ModuleMetricsWrapper>();
	}
	
	/**
	 * This method destroys the wrapper object for a given project
	 * @param project : The project to be deleted from the store
	 */
	public static void deleteWrapper(final IProject project) {
		wrappers.remove(project);
	}
}
