/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.utils;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.markers.types.ProblemType;
import org.eclipse.titanium.markers.types.TaskType;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.StatColumn;
import org.eclipse.titanium.preferences.PreferenceConstants;

/**
 * @author poroszd
 */
public class RiskFactorCalculator {
	private static final ProblemType[] USED_MARKERS = {
		TaskType.FIXME,
		TaskType.TODO,
		CodeSmellType.CIRCULAR_IMPORTATION,
		CodeSmellType.TOO_MANY_STATEMENTS,
		CodeSmellType.TOO_MANY_PARAMETERS,
		// SemanticProblemType..name() // TODO: divergent naming convention
		CodeSmellType.UNCOMMENTED_FUNCTION,
		CodeSmellType.TYPENAME_IN_DEFINITION,
		CodeSmellType.MODULENAME_IN_DEFINITION,
		CodeSmellType.VISIBILITY_IN_DEFINITION,
		CodeSmellType.UNINITIALIZED_VARIABLE,
		CodeSmellType.GOTO,
		CodeSmellType.UNUSED_IMPORT,
		CodeSmellType.UNUSED_GLOBAL_DEFINITION,
		CodeSmellType.UNUSED_LOCAL_DEFINITION,
		CodeSmellType.UNUSED_FUNTION_RETURN_VALUES,
		CodeSmellType.UNUSED_STARTED_FUNCTION_RETURN_VALUES,
		CodeSmellType.INFINITE_LOOP,
		// SemanticProblemType..name(), // TODO: busy wait
		CodeSmellType.NONPRIVATE_PRIVATE,
		// SemanticProblemType.INCORRECT_SHIFT_ROTATE_SIZE.name(), TODO:
		// missing label in ExRotSize in the xls
		CodeSmellType.SIZECHECK_IN_LOOP,
		CodeSmellType.TOO_COMPLEX_EXPRESSIONS,
		CodeSmellType.READONLY_INOUT_PARAM,
		CodeSmellType.READONLY_OUT_PARAM,
		CodeSmellType.READONLY_LOC_VARIABLE,
		CodeSmellType.EMPTY_STATEMENT_BLOCK,
		CodeSmellType.SETVERDICT_WITHOUT_REASON,
		// SemanticProblemType., // TODO: cannot identify VariOutEn
		// smell
		CodeSmellType.STOP_IN_FUNCTION,
		CodeSmellType.UNNECESSARY_VALUEOF,
		CodeSmellType.MAGIC_NUMBERS,
		CodeSmellType.MAGIC_STRINGS,
		CodeSmellType.LOGIC_INVERSION,
		CodeSmellType.UNNECESSARY_CONTROLS };
	
	
	public RiskFactorCalculator() {
	}

	/**
	 * Calculate the quality of a project.
	 * <p>
	 * The code smells are not counted by this method, but rather supplied as
	 * parameter. The keys of the map are the enum names of markers (thus,
	 * {@link TaskType#getMarkerObject()} and {@link CodeSmellType#getMarkerObject()}). Exactly the
	 * code smells enumerated in {@link #USED_MARKERS} are taken in account:
	 * other keys are ignored; upon missing ones an exception is thrown.
	 * 
	 * @throws IllegalArgumentException
	 *             when a key is missing from <code>smellCount</code>
	 * 
	 * @param project
	 *            the project to measure
	 * @param smellCount
	 *            the number of occurrences for the code smells
	 * 
	 * @return the quality assignment of the project
	 */
	// TODO: it worth reconsidering to refactor, i.e. the smell counting as a
	// separate method of this class
	public int measure(final IProject project, final Map<String, Integer> smellCount) {
		final MetricData data = MetricData.measure(project);
		final Number n = data.getStatistics(ModuleMetric.LINES_OF_CODE).get(StatColumn.TOTAL);
		final int loc = n.intValue();

		int riskFactor = 0;
		int actualS, baseS, relativeOccurrene;
		for (final ProblemType  marker : USED_MARKERS) {
			final Integer count = smellCount.get(marker.toString());
			if (count == null) {
				throw new IllegalArgumentException("The supplied map has no entry for " + marker.toString()
						+ ". Collate the parameters and the 'usedMarkers' field.");
			}
			if (count.intValue() == 0) {
				relativeOccurrene = 0;
			} else {
				actualS = loc / count;
				baseS = marker.getBaseLine();
				if (actualS == 0) {
					relativeOccurrene = 0;
				} else if (actualS > baseS) {
					relativeOccurrene = 1;
				} else if (actualS > baseS / 2) {
					relativeOccurrene = 2;
				} else if (actualS > baseS / 8) {
					relativeOccurrene = 3;
				} else {
					relativeOccurrene = 4;
				}
			}
			riskFactor += relativeOccurrene * marker.getImpact();
		}

		return riskFactor;

	}

	/**
	 * Calculate the quality of a project.
	 * <p>
	 * The code smells are not counted by this method, but rather supplied as
	 * parameter. The keys of the map are the enum names of markers (thus,
	 * {@link TaskType#getMarkerObject()} and {@link CodeSmellType#getMarkerObject()}). Exactly the
	 * code smells enumerated in {@link #USED_MARKERS} are taken in account:
	 * other keys are ignored; upon missing ones an exception is thrown.
	 * 
	 * @throws IllegalArgumentException
	 *             when a key is missing from <code>smellCount</code>
	 * 
	 * @param riskFactor
	 *            the riskFactor measured
	 * 
	 * @return the quality assignment of the project
	 */
	public ProjectQualityLevel calculate(final int riskFactor) {
		ProjectQualityLevel quality;
		// denoted as T in the paper
		final int baseRiskFactor = Platform.getPreferencesService().getInt(Activator.PLUGIN_ID, PreferenceConstants.BASE_RISK_FACTOR, 43, null);
		if (riskFactor < baseRiskFactor) {
			quality = ProjectQualityLevel.VERY_HIGH;
		} else if (riskFactor < 2 * baseRiskFactor) {
			quality = ProjectQualityLevel.HIGH;
		} else if (riskFactor < 3 * baseRiskFactor) {
			quality = ProjectQualityLevel.MEDIUM;
		} else if (riskFactor < 4 * baseRiskFactor) {
			quality = ProjectQualityLevel.LOW;
		} else {
			quality = ProjectQualityLevel.VERY_LOW;
		}

		return quality;

	}

	public enum ProjectQualityLevel {
		VERY_HIGH("very high"),
		HIGH("high"),
		MEDIUM("medium"),
		LOW("low"),
		VERY_LOW("very low");

		private String name;

		ProjectQualityLevel(final String name) {
			this.name = name;
		}

		public String getHumanReadablename() {
			return name;
		}
	}
}
