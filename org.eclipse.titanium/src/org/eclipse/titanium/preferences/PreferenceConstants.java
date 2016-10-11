/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences;


public final class PreferenceConstants {
	// Import organization
	public static final String ORG_IMPORT_ADD = "ORGANIZE_IMPORT.ADD";
	public static final String ORG_IMPORT_REMOVE = "ORGANIZE_IMPORT.REMOVE";
	public static final String ORG_IMPORT_SORT = "ORGANIZE_IMPORT.SORT";
	public static final String ORG_IMPORT_METHOD = "ORGANIZE_IMPORT.METHOD";

	// Additional parameters for code smells
	private static final String SIZE_POSTFIX = "_SIZE";
	public static final String TOO_COMPLEX_EXPRESSIONS_SIZE = ProblemTypePreference.TOO_COMPLEX_EXPRESSIONS.getPreferenceName() + SIZE_POSTFIX;
	public static final String TOO_MANY_PARAMETERS_SIZE = ProblemTypePreference.TOO_MANY_PARAMETERS.getPreferenceName() + SIZE_POSTFIX;
	public static final String TOO_MANY_STATEMENTS_SIZE = ProblemTypePreference.TOO_MANY_STATEMENTS.getPreferenceName() + SIZE_POSTFIX;
	public static final String TOO_MANY_CONSECUTIVE_ASSIGNMENTS_SIZE = ProblemTypePreference.CONSECUTIVE_ASSIGNMENTS.getPreferenceName() + SIZE_POSTFIX;
	public static final String ON_THE_FLY_SMELLS = "CODE_SMELL.ON_THE_FLY_SMELLS";

	// Constants regarding metrics
	public static final String METRIC = "METRIC.";
	public static final String METRIC_ENABLED = METRIC + "ENABLED.";
	public static final String METRIC_RISK_LEVEL = METRIC + "RISK_LEVEL.";
	public static final String METRIC_LIMITS = METRIC + "LIMITS.";
	public static final String METRIC_GRAPH = METRIC + "GRAPH.";

	// graph constants
	public static final String NO_ITERATIONS = "Graph_Layout_Iterations";
	public static final String DAG_DISTANCE = "Graph_Layout_DAG_Distance";

	// Graph clustering constants
	// Folder cluster
	public static final String CLUSTER_TRUNCATE = "Graph_Folder_Truncate";
	// RegexpCluster
	public static final String CLUSTER_REGEXP = "Graph_Regexp_Clusters";
	// ModuleNameCluster
	public static final String CLUSTER_SPACE = "Check_Space_In_Name";
	public static final String CLUSTER_SMALL_LARGE = "Check_SmallLarga_Alt_In_Name";
	public static final String CLUSTER_DEPTH = "Cluster_Split_Depth";
	// AutomaticCluster
	public static final String CLUSTER_ITERATION = "Max_Auto_Iterations";
	public static final String CLUSTER_SIZE_LIMIT = "Max_Auto_Cluster_Number";
	public static final String CLUSTER_AUTO_FOLDER = "Auto_folder";
	public static final String CLUSTER_AUTO_REGEXP = "Auto_regexp";
	public static final String CLUSTER_AUTO_NAME = "Auto_name";

	public static final String BASE_RISK_FACTOR = "BASE_RISK_FACTOR";

	/** private constructor to disable instantiation */
	private PreferenceConstants() {
	}

	// Some aid method to make accessing metric data easier:
	public static String nameMetricEnabled(final String id) {
		return METRIC_ENABLED + id;
	}

	public static String nameMetricRisk(final String id) {
		return METRIC_RISK_LEVEL + id;
	}

	public static String nameMetricLimits(final String id) {
		return METRIC_LIMITS + id;
	}

	public static String nameMetricGraph(final String id) {
		return METRIC_GRAPH + id;
	}
}
