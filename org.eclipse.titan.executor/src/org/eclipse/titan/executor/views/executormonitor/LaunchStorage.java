/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.executormonitor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.ILaunch;

public class LaunchStorage {
	private static final Map<ILaunch, LaunchElement> LAUNCHELEMENTS_MAP = new HashMap<ILaunch, LaunchElement>();
	
	private LaunchStorage() {
		//Do nothing
	}

	public static void clear() {
		LAUNCHELEMENTS_MAP.clear();
	}

	/**
	 * Returns the plug-ins LaunchElement HashMap which contains all of the accessible launchElements. If needed than this map is also created here.
	 *
	 * @return the HashMap of launchElements
	 * */
	public static Map<ILaunch, LaunchElement> getLaunchElementMap() {
		return LAUNCHELEMENTS_MAP;
	}
	
	/**
	 * Registers the provided launch element.
	 * If it is not yet registered.
	 * 
	 * @param element the launch element to be registered.
	 * */
	public static void registerLaunchElement(final LaunchElement element) {
		ILaunch launch = element.launch();
		if (!LAUNCHELEMENTS_MAP.containsKey(launch)) {
			LAUNCHELEMENTS_MAP.put(launch, element);
		}
	}
}
