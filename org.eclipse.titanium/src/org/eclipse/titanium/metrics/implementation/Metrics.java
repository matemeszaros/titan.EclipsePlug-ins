/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.IMetric;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.ProjectMetric;
import org.eclipse.titanium.metrics.TestcaseMetric;

/**
 * Encapsulates a mapping from metric enums concrete metric implementations.
 * 
 * @author poroszd
 */
public class Metrics {
	final Map<AltstepMetric, IMetric<Def_Altstep, AltstepMetric>> altstepMetrics;
	final Map<FunctionMetric, IMetric<Def_Function, FunctionMetric>> functionMetrics;
	final Map<TestcaseMetric, IMetric<Def_Testcase, TestcaseMetric>> testcaseMetrics;
	final Map<ModuleMetric, IMetric<Module, ModuleMetric>> moduleMetrics;
	final Map<ProjectMetric, IMetric<IProject, ProjectMetric>> projectMetrics;

	/**
	 * Create a new instance for all metrics.
	 */
	public Metrics() {
		altstepMetrics = new EnumMap<AltstepMetric, IMetric<Def_Altstep, AltstepMetric>>(AltstepMetric.class);
		altstepMetrics.put(AltstepMetric.BRANCHES, new AMBranches());
		altstepMetrics.put(AltstepMetric.CYCLOMATIC_COMPLEXITY, new AMCyclomaticComplexity());
		altstepMetrics.put(AltstepMetric.IN_ENVY, new AMInEnvy());
		altstepMetrics.put(AltstepMetric.LINES_OF_CODE, new AMLinesOfCode());
		altstepMetrics.put(AltstepMetric.NESTING, new AMNesting());
		altstepMetrics.put(AltstepMetric.NUMBER_OF_PARAMETERS, new AMNumberOfParams());
		altstepMetrics.put(AltstepMetric.OUT_ENVY, new AMOutEnvy());

		functionMetrics = new EnumMap<FunctionMetric, IMetric<Def_Function, FunctionMetric>>(FunctionMetric.class);
		functionMetrics.put(FunctionMetric.CYCLOMATIC_COMPLEXITY, new FMCyclomaticComplexity());
		functionMetrics.put(FunctionMetric.DEFAULT_ACTIVATIONS, new FMDefaultActivations());
		functionMetrics.put(FunctionMetric.IN_ENVY, new FMInEnvy());
		functionMetrics.put(FunctionMetric.LINES_OF_CODE, new FMLinesOfCode());
		functionMetrics.put(FunctionMetric.NESTING, new FMNesting());
		functionMetrics.put(FunctionMetric.NUMBER_OF_PARAMETERS, new FMNumberOfParams());
		functionMetrics.put(FunctionMetric.OUT_ENVY, new FMOutEnvy());
		functionMetrics.put(FunctionMetric.RETURN_POINTS, new FMReturnPoints());

		testcaseMetrics = new EnumMap<TestcaseMetric, IMetric<Def_Testcase, TestcaseMetric>>(TestcaseMetric.class);
		testcaseMetrics.put(TestcaseMetric.CYCLOMATIC_COMPLEXITY, new TMCyclomaticComplexity());
		testcaseMetrics.put(TestcaseMetric.IN_ENVY, new TMInEnvy());
		testcaseMetrics.put(TestcaseMetric.LINES_OF_CODE, new TMLinesOfCode());
		testcaseMetrics.put(TestcaseMetric.NESTING, new TMNesting());
		testcaseMetrics.put(TestcaseMetric.NUMBER_OF_PARAMETERS, new TMNumberOfParams());
		testcaseMetrics.put(TestcaseMetric.OUT_ENVY, new TMOutEnvy());

		moduleMetrics = new EnumMap<ModuleMetric, IMetric<Module, ModuleMetric>>(ModuleMetric.class);
		moduleMetrics.put(ModuleMetric.AFFERENT_COUPLING, new MMAfferentCoupling());
		moduleMetrics.put(ModuleMetric.EFFERENT_COUPLING, new MMEfferentCoupling());
		moduleMetrics.put(ModuleMetric.IN_ENVY, new MMInEnvy());
		moduleMetrics.put(ModuleMetric.INSTABILITY, new MMInstability());
		moduleMetrics.put(ModuleMetric.LINES_OF_CODE, new MMLinesOfCode());
		moduleMetrics.put(ModuleMetric.NOF_STATEMENTS, new MMNofStatements());
		moduleMetrics.put(ModuleMetric.NOF_ALTSTEPS, new MMNofAltsteps());
		moduleMetrics.put(ModuleMetric.NOF_FIXME, new MMNofFixme());
		moduleMetrics.put(ModuleMetric.NOF_FUNCTIONS, new MMNofFunctions());
		moduleMetrics.put(ModuleMetric.NOF_IMPORTS, new MMNofImports());
		moduleMetrics.put(ModuleMetric.NOF_TESTCASES, new MMNofTestcases());
		moduleMetrics.put(ModuleMetric.OUT_ENVY, new MMOutEnvy());
		moduleMetrics.put(ModuleMetric.TIMES_IMPORTED, new MMTimesImported());

		projectMetrics = new EnumMap<ProjectMetric, IMetric<IProject, ProjectMetric>>(ProjectMetric.class);
		projectMetrics.put(ProjectMetric.NOF_ASN1_MODULES, new PMNofASN1Modules());
		projectMetrics.put(ProjectMetric.NOF_TTCN3_MODULES, new PMNofTTCN3Modules());
	}

	public IMetric<Def_Altstep, AltstepMetric> get(AltstepMetric metric) {
		return altstepMetrics.get(metric);
	}

	public IMetric<Def_Function, FunctionMetric> get(FunctionMetric metric) {
		return functionMetrics.get(metric);
	}

	public IMetric<Def_Testcase, TestcaseMetric> get(TestcaseMetric metric) {
		return testcaseMetrics.get(metric);
	}

	public IMetric<Module, ModuleMetric> get(ModuleMetric metric) {
		return moduleMetrics.get(metric);
	}

	public IMetric<IProject, ProjectMetric> get(ProjectMetric metric) {
		return projectMetrics.get(metric);
	}
}