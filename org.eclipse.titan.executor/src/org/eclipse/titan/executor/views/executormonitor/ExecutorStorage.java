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
import org.eclipse.titan.executor.executors.BaseExecutor;

public class ExecutorStorage {
	private static final Map<ILaunch, BaseExecutor> EXECUTOR_MAP = new HashMap<ILaunch, BaseExecutor>();
	
	private ExecutorStorage() {
		//Do nothing
	}

	public static void clear() {
		EXECUTOR_MAP.clear();
	}

	/**
	 * Returns the plug-ins executor HashMap which contains all of the accessible executors. If needed than this map is also created here.
	 *
	 * @return the HashMap of executors.
	 * */
	public static Map<ILaunch, BaseExecutor> getExecutorMap() {
		return EXECUTOR_MAP;
	}
	
	/**
	 * Register the provided launch element.
	 * Only done if the launch is already registered.
	 * 
	 * @param element the element to be registered
	 * */
	public static void registerExecutorStorage(final LaunchElement element) {
		ILaunch launch = element.launch();
		if (ExecutorStorage.getExecutorMap().containsKey(launch)) {
			final BaseExecutor executor = ExecutorStorage.getExecutorMap().get(launch);
			if (null == executor.mainControllerRoot()) {
				executor.mainControllerRoot(new MainControllerElement(BaseExecutor.MAIN_CONTROLLER, executor));
			}
			element.addChildToEnd(executor.mainControllerRoot());
		}
	}
}
