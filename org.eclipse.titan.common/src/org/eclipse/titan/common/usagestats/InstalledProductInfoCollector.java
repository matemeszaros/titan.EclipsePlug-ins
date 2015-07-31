/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.usagestats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.common.product.ProductConstants;
import org.eclipse.titan.common.utils.Joiner;
import org.eclipse.titan.common.utils.StringUtils;

/**
 * This class collects the names of the installed Titan and Titanium plugins
 */
public class InstalledProductInfoCollector implements UsageStatInfoCollector {

	@Override
	public Map<String, String> collect() {
		String info = "plugin_start(" + new Joiner(",").join(getInstalledTitanPlugins()).toString() + ")";
		Map<String, String> dataToSend = new HashMap<String, String>();
		dataToSend.put("info", info);
		return dataToSend;
	}

	private static List<String> getInstalledTitanPlugins() {
		final List<String> result = new ArrayList<String>();

		String[] productIds = {
				ProductConstants.PRODUCT_ID_COMMON,
				ProductConstants.PRODUCT_ID_DESIGNER,
				ProductConstants.PRODUCT_ID_EXECUTOR,
				ProductConstants.PRODUCT_ID_LOGVIEWER,
				ProductConstants.PRODUCT_ID_TITANIUM };

		for (final String productId : productIds) {
			if (Platform.getBundle(productId) != null) {
				result.add(StringUtils.removePrefix(StringUtils.removePrefix(productId, "org.eclipse."), "titan."));
			}
		}

		return result;
	}
}
