/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.designerconnection;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This class stores constants and functions that make it possible for the executor plug-in the reach some settings from the Designer plug-in.
 * <p>
 * It is important to note that it is a valid case when the designer plug-in is not installed together with the executor plug-in.
 * 
 * @author Kristof Szabados
 * */
public final class DesignerHelper {
	public static final String DESIGNER_PLUGIN_ID = "org.eclipse.titan.designer";
	public static final String NATURE_ID = DESIGNER_PLUGIN_ID + ".core.TITANNature";
	public static final String TITAN_INSTALLATION_PATH = DESIGNER_PLUGIN_ID + ".TTCN3_INSTALL_DIR";
	public static final String LICENSE_FILE_PATH = DESIGNER_PLUGIN_ID + ".LICENSE_FILE";
	public static final String PROJECTPROPERTIESQUALIFIER = DESIGNER_PLUGIN_ID + ".Properties.Project";

	public static final String WORKINGDIR_PROPERTY = "workingDir";
	public static final String EXECUTABLE_PROPERTY = "targetExecutable";
	public static final String PROJECT_BUILD_PROPERTYPAGE_QUALIFIER = DesignerHelper.DESIGNER_PLUGIN_ID + ".Properties.Project";

	private DesignerHelper() {
	}

	/**
	 * If the designer plug-in is also installed this will try to gain the workspace level setting of the TITAN installation path.
	 *
	 * @return the installation path of TITAN (in native format), or empty string if not found.
	 * */
	public static String getTTCN3DIR() {
		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, DESIGNER_PLUGIN_ID);
		return preferenceStore.getString(TITAN_INSTALLATION_PATH);
	}

	/**
	 * If the designer plug-in is also installed this will try to gain the workspace level setting of the TTCN-3 license file.
	 *
	 * @return the location of the license file (in native format), or empty string if not found.
	 * */
	public static String getTTCN3LICENSEFILE() {
		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, DESIGNER_PLUGIN_ID);
		return preferenceStore.getString(LICENSE_FILE_PATH);
	}

}
