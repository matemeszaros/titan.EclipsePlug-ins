/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.preferences;

import org.eclipse.titan.common.Activator;

public final class PreferenceConstants {

	public static final String LOG_MERGE_OPTIONS = Activator.PLUGIN_ID + ".automaticMergeOptions";
	public static final String LOG_MERGE_OPTIONS_OVERWRITE = "overwrite";
	public static final String LOG_MERGE_OPTIONS_CREATE = "create";
	public static final String LOG_MERGE_OPTIONS_ASK = "ask";

	/** private constructor to disable instantiation */
	private PreferenceConstants() {
	}
}
