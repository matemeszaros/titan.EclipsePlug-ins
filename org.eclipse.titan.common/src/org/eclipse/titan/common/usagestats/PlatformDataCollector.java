/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.usagestats;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.product.ProductConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * This class collects the platform related information.
 */
public class PlatformDataCollector implements UsageStatInfoCollector {
	@Override
	public Map<String, String> collect() {
		final Map<String, String> result = collectSystemData();
		result.put("plugin_id", ProductConstants.PRODUCT_ID_COMMON);
		addBundleVersion(result, "eclipse", "org.eclipse.platform");
		addBundleVersion(result, "plugin", ProductConstants.PRODUCT_ID_COMMON);
		try {
			result.put("hostname", InetAddress.getLocalHost().getCanonicalHostName());
		} catch (final Exception e) {
			ErrorReporter.logWarningExceptionStackTrace("While resolving the local host's address", e);
			result.put("hostname", "UNKNOWN");
		}
		return result;
	}

	private Map<String, String> collectSystemData() {
		final Map<String, String> result = new HashMap<String, String>();
		try {
			result.put("user_id", System.getProperty("user.name"));
			result.put("os_name", System.getProperty("os.name"));
			result.put("os_arch", System.getProperty("os.arch"));
			result.put("os_version", System.getProperty("os.version"));
			result.put("java_version", System.getProperty("java.version"));
			result.put("java_vendor", System.getProperty("java.vendor"));
		} catch (final SecurityException e) {
			ErrorReporter.logWarningExceptionStackTrace("Could not access a system property",e);
			return result;
		}
		return result;
	}

	private void addBundleVersion(final Map<String, String> data, final String key, final String pluginId) {
		final Bundle bundle = Platform.getBundle(pluginId);
		if (bundle == null) {
			return;
		}

		final Version version = bundle.getVersion();
		final String versionString = "" + version.getMajor() + "." + version.getMinor() + "." + version.getMicro();
		data.put(key + "_version", versionString);
		data.put(key + "_version_qualifier", version.getQualifier());
	}
}
