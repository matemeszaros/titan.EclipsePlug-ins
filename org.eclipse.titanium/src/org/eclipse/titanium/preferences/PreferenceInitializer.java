/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.graph.gui.layouts.TitaniumDAGLayout;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.markers.types.ProblemNameToPreferenceMapper;
import org.eclipse.titanium.markers.types.TaskType;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.MetricGroup;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.ProjectMetric;
import org.eclipse.titanium.metrics.TestcaseMetric;
import org.eclipse.titanium.preferences.pages.OrganizeImportPreferencePage;

/**
 * This class is used for initializing the internal values to their default
 * state.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public final void initializeDefaultPreferences() {
		final IPreferenceStore preferenceStore = getPreference();

		// Import organization
		preferenceStore.setDefault(PreferenceConstants.ORG_IMPORT_ADD, true);
		preferenceStore.setDefault(PreferenceConstants.ORG_IMPORT_REMOVE, true);
		preferenceStore.setDefault(PreferenceConstants.ORG_IMPORT_SORT, true);
		preferenceStore.setDefault(PreferenceConstants.ORG_IMPORT_METHOD, OrganizeImportPreferencePage.JUST_CHANGE);

		// Code smells
		preferenceStore.setDefault(ProblemTypePreference.UNUSED_IMPORT.getPreferenceName(), GeneralConstants.WARNING);
		preferenceStore.setDefault(ProblemTypePreference.UNUSED_GLOBAL_DEFINITION.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.UNUSED_LOCAL_DEFINITION.getPreferenceName(), GeneralConstants.WARNING);
		preferenceStore.setDefault(ProblemTypePreference.UNUSED_FUNTION_RETURN_VALUES.getPreferenceName(), GeneralConstants.WARNING);
		preferenceStore.setDefault(ProblemTypePreference.MISSING_IMPORT.getPreferenceName(), GeneralConstants.ERROR);
		preferenceStore.setDefault(ProblemTypePreference.MISSING_FRIEND.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.INCORRECT_SHIFT_ROTATE_SIZE.getPreferenceName(), GeneralConstants.WARNING);
		preferenceStore.setDefault(ProblemTypePreference.UNNECESSARY_CONTROLS.getPreferenceName(), GeneralConstants.WARNING);
		preferenceStore.setDefault(ProblemTypePreference.UNNECESSARY_VALUEOF.getPreferenceName(), GeneralConstants.WARNING);
		preferenceStore.setDefault(ProblemTypePreference.INFINITE_LOOP.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.READONLY_VARIABLE.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.GOTO.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.CIRCULAR_IMPORTATION.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.NONPRIVATE_PRIVATE.getPreferenceName(), GeneralConstants.WARNING);
		preferenceStore.setDefault(ProblemTypePreference.VISIBILITY_IN_DEFINITION.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.MODULENAME_IN_DEFINITION.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.TYPENAME_IN_DEFINITION.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.MAGIC_CONSTANTS.getPreferenceName(), GeneralConstants.IGNORE);

		preferenceStore.setDefault(ProblemTypePreference.TOO_MANY_PARAMETERS.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.TOO_MANY_PARAMETERS_SIZE, 7);
		preferenceStore.setDefault(ProblemTypePreference.TOO_COMPLEX_EXPRESSIONS.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.TOO_COMPLEX_EXPRESSIONS_SIZE, 3);
		preferenceStore.setDefault(ProblemTypePreference.EMPTY_STATEMENT_BLOCK.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.TOO_MANY_STATEMENTS.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.TOO_MANY_STATEMENTS_SIZE, 150);
		preferenceStore.setDefault(ProblemTypePreference.IF_WITHOUT_ELSE.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.SWITCH_ON_BOOLEAN.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.SETVERDICT_WITHOUT_REASON.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.LAZY.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.PRIVATE_FIELD_VIA_PUBLIC.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.PRIVATE_VALUE_VIA_PUBLIC.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.LOGIC_INVERSION.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.UNCOMMENTED_FUNCTION.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.UNINITIALIZED_VARIABLE.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.SIZECHECK_IN_LOOP.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.STOP_IN_FUNCTION.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.RECEIVE_ANY_TEMPLATE.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.IF_INSTEAD_ALTGUARD.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.IF_INSTEAD_RECEIVE_TEMPLATE.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.ALTSTEP_COVERAGE.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.SHORTHAND.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.ISBOUND_WITHOUT_ELSE.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.ISVALUE_WITH_VALUE.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.ITERATE_ON_WRONG_ARRAY.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.CONSECUTIVE_ASSIGNMENTS.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.TOO_MANY_CONSECUTIVE_ASSIGNMENTS_SIZE, 4);
		preferenceStore.setDefault(ProblemTypePreference.CONVERT_TO_ENUM.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.SELECT_COVERAGE.getPreferenceName(), GeneralConstants.IGNORE);
		preferenceStore.setDefault(ProblemTypePreference.SELECT_WITH_NUMBERS_SORTED.getPreferenceName(), GeneralConstants.WARNING);
		preferenceStore.setDefault(PreferenceConstants.ON_THE_FLY_SMELLS, false);

		// Initialize default values of metrics
		for (final IMetricEnum metric : MetricGroup.knownMetrics()) {
			final String name = metric.id();
			preferenceStore.setDefault(PreferenceConstants.nameMetricEnabled(name), true);
			preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(name), 0);
			preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(name), "");
			if (!(metric instanceof ProjectMetric)) {
				preferenceStore.setDefault(PreferenceConstants.nameMetricGraph(name), true);
			}
		}
		// Set those exceptional:
		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(FunctionMetric.NUMBER_OF_PARAMETERS.id()), 3);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(FunctionMetric.NUMBER_OF_PARAMETERS.id()), "5;7");
		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(FunctionMetric.LINES_OF_CODE.id()), 3);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(FunctionMetric.LINES_OF_CODE.id()), "100;150");
		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(FunctionMetric.CYCLOMATIC_COMPLEXITY.id()), 3);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(FunctionMetric.CYCLOMATIC_COMPLEXITY.id()), "10;20");
		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(FunctionMetric.NESTING.id()), 3);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(FunctionMetric.NESTING.id()), "4;6");

		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(TestcaseMetric.LINES_OF_CODE.id()), 3);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(TestcaseMetric.LINES_OF_CODE.id()), "100;150");
		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(TestcaseMetric.CYCLOMATIC_COMPLEXITY.id()), 3);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(TestcaseMetric.CYCLOMATIC_COMPLEXITY.id()), "10;20");
		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(TestcaseMetric.NESTING.id()), 3);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(TestcaseMetric.NESTING.id()), "4;6");
		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(TestcaseMetric.NUMBER_OF_PARAMETERS.id()), 3);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(TestcaseMetric.NUMBER_OF_PARAMETERS.id()), "5;7");

		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(AltstepMetric.LINES_OF_CODE.id()), 3);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(AltstepMetric.LINES_OF_CODE.id()), "100;150");
		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(AltstepMetric.CYCLOMATIC_COMPLEXITY.id()), 3);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(AltstepMetric.CYCLOMATIC_COMPLEXITY.id()), "10;20");
		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(AltstepMetric.NESTING.id()), 3);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(AltstepMetric.NESTING.id()), "4;6");
		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(AltstepMetric.NUMBER_OF_PARAMETERS.id()), 3);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(AltstepMetric.NUMBER_OF_PARAMETERS.id()), "5;7");

		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(ModuleMetric.NOF_FIXME.id()), 1);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(ModuleMetric.NOF_FIXME.id()), "1");
		preferenceStore.setDefault(PreferenceConstants.nameMetricRisk(ModuleMetric.INSTABILITY.id()), 1);
		preferenceStore.setDefault(PreferenceConstants.nameMetricLimits(ModuleMetric.INSTABILITY.id()), "0.3");

		// Initialize default impact and baseline values for code smells:
		preferenceStore.setDefault(PreferenceConstants.BASE_RISK_FACTOR, 43);

		// care about task markers:
		setTaskImpactAndBaseLine(preferenceStore, TaskType.FIXME, 2, 28000);
		setTaskImpactAndBaseLine(preferenceStore, TaskType.TODO, 1, 7000);

		// populate baselines:
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.CIRCULAR_IMPORTATION, 1, 50000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.TOO_MANY_STATEMENTS, 2, 20000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.TOO_MANY_PARAMETERS, 2, 1500);
		// setSmellImpactAndBaseLine(preferenceStore, SemanticProblemType., 1,
		// 10); // TODO: divergent naming convention
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.UNCOMMENTED_FUNCTION, 2, 2000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.TYPENAME_IN_DEFINITION, 2, 200);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.MODULENAME_IN_DEFINITION, 2, 4000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.VISIBILITY_IN_DEFINITION, 2, 15000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.UNINITIALIZED_VARIABLE, 2, 50);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.GOTO, 3, 2000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.UNUSED_IMPORT, 1, 150);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.UNUSED_GLOBAL_DEFINITION, 1, 150);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.UNUSED_LOCAL_DEFINITION, 2, 300);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.UNUSED_FUNTION_RETURN_VALUES, 2, 2000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.UNUSED_STARTED_FUNCTION_RETURN_VALUES, 3, 5000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.INFINITE_LOOP, 3, 80000);
		// setSmellImpactAndBaseLine(preferenceStore, SemanticProblemType., 3,
		// 400000); // TODO: busy wait
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.NONPRIVATE_PRIVATE, 1, 100);
		// setSmellImpactAndBaseLine(preferenceStore,
		// SemanticProblemType.INCORRECT_SHIFT_ROTATE_SIZE, 2, ); // TODO:
		// missing label in ExRotSize in the xls
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.SIZECHECK_IN_LOOP, 2, 1000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.TOO_COMPLEX_EXPRESSIONS, 2, 1000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.READONLY_INOUT_PARAM, 2, 4000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.READONLY_OUT_PARAM, 2, 50000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.READONLY_LOC_VARIABLE, 2, 100);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.EMPTY_STATEMENT_BLOCK, 1, 1000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.SETVERDICT_WITHOUT_REASON, 2, 500);
		// setSmellImpactAndBaseLine(preferenceStore, SemanticProblemType., 1,
		// 8000); // TODO: cannot identify VariOutEn smell
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.STOP_IN_FUNCTION, 2, 30000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.UNNECESSARY_VALUEOF, 2, 80000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.MAGIC_NUMBERS, 2, 50);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.MAGIC_STRINGS, 2, 15);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.LOGIC_INVERSION, 1, 4000);
		setSmellImpactAndBaseLine(preferenceStore, CodeSmellType.UNNECESSARY_CONTROLS, 3, 50000);

		// graph constants
		preferenceStore.setDefault(PreferenceConstants.NO_ITERATIONS, 500);
		preferenceStore.setDefault(PreferenceConstants.DAG_DISTANCE, TitaniumDAGLayout.SUM_DISTANCE_ALGORITHM);

		// graph clustering defaults
		preferenceStore.setDefault(PreferenceConstants.CLUSTER_SPACE, true);
		preferenceStore.setDefault(PreferenceConstants.CLUSTER_SMALL_LARGE, false);
		preferenceStore.setDefault(PreferenceConstants.CLUSTER_DEPTH, 3);
		preferenceStore.setDefault(PreferenceConstants.CLUSTER_ITERATION, 20);
		preferenceStore.setDefault(PreferenceConstants.CLUSTER_SIZE_LIMIT, 7);
		preferenceStore.setDefault(PreferenceConstants.CLUSTER_AUTO_FOLDER, true);
		preferenceStore.setDefault(PreferenceConstants.CLUSTER_AUTO_REGEXP, true);
		preferenceStore.setDefault(PreferenceConstants.CLUSTER_AUTO_NAME, true);

	}

	private static void setSmellImpactAndBaseLine(final IPreferenceStore preferenceStore, final CodeSmellType t, final int imp, final int bl) {
		preferenceStore.setDefault(ProblemNameToPreferenceMapper.nameSmellImpact(t.name()), imp);
		preferenceStore.setDefault(ProblemNameToPreferenceMapper.nameSmellBaseLine(t.name()), bl);
	}

	private static void setTaskImpactAndBaseLine(final IPreferenceStore preferenceStore, final TaskType t, final int imp, final int bl) {
		preferenceStore.setDefault(ProblemNameToPreferenceMapper.nameSmellImpact(t.name()), imp);
		preferenceStore.setDefault(ProblemNameToPreferenceMapper.nameSmellBaseLine(t.name()), bl);
	}

	public IPreferenceStore getPreference() {
		return Activator.getDefault().getPreferenceStore();
	}
}
