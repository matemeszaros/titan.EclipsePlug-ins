/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.preferences.PreferenceConstants;
import org.eclipse.titanium.preferences.pages.GraphMetricsPage;
import org.eclipse.titanium.preferences.pages.MetricsLimitPreferencePage;
import org.eclipse.titanium.preferences.pages.MetricsViewPreferencePage;

/**
 * This is a convenience class to ease the access to values stored in the
 * preference stores.
 * 
 * @author poroszd
 * 
 */
public class PreferenceManager {
	private static IPreferenceStore store = null;
	
	private PreferenceManager() {
		// disabled constructor
	}

	public static IPreferenceStore getStore() {
		if (store == null) {
			store = Activator.getDefault().getPreferenceStore();
		}
		
		return store;
	}

	/**
	 * Query if a metric is checked on the {@link MetricsViewPreferencePage}.
	 * 
	 * @param metric
	 *            the metric in question
	 * @param def
	 *            if true, then the default value is returned
	 * @return is the metric enabled
	 */
	public static boolean getEnabled(final IMetricEnum metric, final boolean def) {
		final String name = PreferenceConstants.nameMetricEnabled(metric.id());
		if (def) {
			return getStore().getDefaultBoolean(name);
		} else {
			return getStore().getBoolean(name);
		}
	}

	/**
	 * Query the method used to set the risk limits of a metric on the
	 * {@link MetricsLimitPreferencePage}.
	 * <ul>
	 * <li>0 - {@link RiskMethod#NEVER}</li>
	 * <li>1 - {@link RiskMethod#NO_LOW}</li>
	 * <li>2 - {@link RiskMethod#NO_HIGH}</li>
	 * <li>3 - {@link RiskMethod#NO_LOW_HIGH}</li>
	 * </ul>
	 * 
	 * @param metric
	 *            the metric in question
	 * @param def
	 *            if true, then the default value is returned
	 * @return the method of warn
	 * @see RiskMethod
	 */
	public static RiskMethod getRiskMethod(final IMetricEnum metric, final boolean def) {
		final String name = PreferenceConstants.nameMetricRisk(metric.id());
		if (def) {
			return RiskMethod.myMethod(getStore().getDefaultInt(name));
		} else {
			return RiskMethod.myMethod(getStore().getInt(name));
		}
	}

	/**
	 * Query the risk limits associated to a {@link IValueMetric} metric. The
	 * returned array
	 * <ul>
	 * <li>is <code>null</code>, when no limits are set (the metric is
	 * configured with {@link RiskMethod#NEVER})</li>
	 * <li>contains one element for {@link RiskMethod#NO_LOW} and
	 * {@link RiskMethod#NO_HIGH}</li>
	 * <li>contains two elements for {@link RiskMethod#NO_LOW_HIGH}.</li>
	 * </ul>
	 * 
	 * @param metric
	 *            the metric in question
	 * @param def
	 *            if true, then the default value is returned
	 * @return an array containing the limits (or null).
	 */
	public static Number[] getLimits(final IMetricEnum metric, final boolean def) {
		double d;
		final String name = PreferenceConstants.nameMetricLimits(metric.id());
		switch (getRiskMethod(metric, def)) {
		case NEVER:
			return null;
		case NO_LOW: //$FALL-THROUGH$
		case NO_HIGH:
			d = Double.parseDouble(def ? getStore().getDefaultString(name) : getStore().getString(name));
			final Number n = metric.isInteger() ? Integer.valueOf((int) Math.round(d)) : new Double(d);
			return new Number[] { n };
		case NO_LOW_HIGH:
			final String[] parts = (def ? getStore().getDefaultString(name) : getStore().getString(name)).split(";");
			final Number[] ret = new Number[2];
			for (int i = 0; i < 2; ++i) {
				d = Double.parseDouble(parts[i]);
				ret[i] = metric.isInteger() ? Integer.valueOf((int) Math.round(d)) : new Double(d);
			}
			return ret;
		default:
			return null;
		}
	}

	/**
	 * Store the limits of a metric. Much like the dual of
	 * {@link #getLimits(MetricsEnum, boolean) getLimits}.
	 * 
	 * @param metric
	 *            the metric in question
	 * @param method
	 *            the method of warn
	 * @param limits
	 *            the value(s) of the limit(s).
	 */
	public static void storeRisk(final IMetricEnum metric, final RiskMethod method, final Number[] limits) {
		getStore().setValue(PreferenceConstants.nameMetricRisk(metric.id()), method.ordinal());
		final String name = PreferenceConstants.nameMetricLimits(metric.id());
		switch (method) {
		case NEVER:
			break;
		case NO_LOW: //$FALL-THROUGH$
		case NO_HIGH:
			getStore().setValue(name, limits[0].toString());
			break;
		case NO_LOW_HIGH:
			getStore().setValue(name, limits[0].toString() + ";" + limits[1].toString());
			break;
		}
	}

	/**
	 * Queries the metrics that are checked on the {@link GraphMetricsPage}.
	 * 
	 * @return a set of metrics that should be displayed in the module graph
	 *         view.
	 */
	public static boolean isEnabledOnModuleGraph(IMetricEnum metric) {
		return getStore().getBoolean(PreferenceConstants.nameMetricGraph(metric.id()));
	}

	/**
	 * Queries the metrics that are checked on the
	 * {@link MetricsViewPreferencePage}.
	 * 
	 * @return a set of metrics that should be displayed in the metrics view.
	 */
	public static boolean isEnabledOnView(IMetricEnum metric) {
		return getStore().getBoolean(PreferenceConstants.nameMetricEnabled(metric.id()));
	}
}
