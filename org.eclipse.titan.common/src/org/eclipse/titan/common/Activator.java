/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.titan.common.product.ProductConstants;
import org.eclipse.titan.common.usagestats.InstalledProductInfoCollector;
import org.eclipse.titan.common.usagestats.UsageStatSender;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author Kristof Szabados
 */
public final class Activator extends AbstractUIPlugin {

	//** The plug-in ID */
	public static final String PLUGIN_ID = ProductConstants.PRODUCT_ID_COMMON;

	/** The shared instance */
	private static Activator plugin;
	private WorkspaceJob usageStatSenderJob;

	public Activator() {
		plugin = this;
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		if ( ProductConstants.USAGE_STAT_SENDING ) {
			usageStatSenderJob = new UsageStatSender(new InstalledProductInfoCollector()).sendAsync();
		}
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		if (usageStatSenderJob != null) {
			usageStatSenderJob.cancel();
		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
}
