/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.metrics.implementation.Metrics;
import org.eclipse.titanium.metrics.utils.RiskLevel;

/**
 * Immutable storage class for the result of the metrics.
 * <p>
 * Instances are obtained via the static factory method
 * {@link MetricData#measure(IProject)}, which executes all metrics on the given
 * project, gathers the results of the measurements, computes the necessary
 * statistics and risks. Once created, all these information can be queried via
 * this instance.
 * 
 * @author poroszd
 * 
 */
public class MetricData {
	final IProject project;
	final MutableMetricData data;

	private MetricData(final IProject project, final MutableMetricData data) {
		this.project = project;
		this.data = data;
	}

	public IProject getProject() {
		return project;
	}

	public List<Module> getModules() {
		return data.modules;
	}

	public Map<Module, List<Def_Function>> getFunctions() {
		return data.functions;
	}

	public Map<Module, List<Def_Testcase>> getTestcases() {
		return data.testcases;
	}

	public Map<Module, List<Def_Altstep>> getAltsteps() {
		return data.altsteps;
	}

	public Risks getRisks() {
		return data.risks;
	}

	public Number get(final AltstepMetric metric, final Def_Altstep altstep) {
		return data.altstepMetrics.get(metric).get(altstep);
	}

	public Number get(final FunctionMetric metric, final Def_Function function) {
		return data.functionMetrics.get(metric).get(function);
	}

	public Number get(final TestcaseMetric metric, final Def_Testcase testcase) {
		return data.testcaseMetrics.get(metric).get(testcase);
	}

	public double getRiskValue(final AltstepMetric metric, final Def_Altstep altstep) {
		return data.risks.getRisk(metric).getRiskValue(get(metric, altstep));
	}

	public double getRiskValue(final FunctionMetric metric, final Def_Function function) {
		return data.risks.getRisk(metric).getRiskValue(get(metric, function));
	}

	public double getRiskValue(final TestcaseMetric metric, final Def_Testcase testcase) {
		return data.risks.getRisk(metric).getRiskValue(get(metric, testcase));
	}

	public RiskLevel getRisk(final AltstepMetric metric, final Def_Altstep altstep) {
		return data.risks.getRisk(metric).getRiskLevel(get(metric, altstep));
	}

	public RiskLevel getRisk(final FunctionMetric metric, final Def_Function function) {
		return data.risks.getRisk(metric).getRiskLevel(get(metric, function));
	}

	public RiskLevel getRisk(final TestcaseMetric metric, final Def_Testcase testcase) {
		return data.risks.getRisk(metric).getRiskLevel(get(metric, testcase));
	}

	public Number get(final ModuleMetric metric, final Module module) {
		return data.moduleMetrics.get(metric).get(module);
	}

	public Number getHighestRiskCauser(final AltstepMetric metric, final Module module) {
		return data.altstepModuleStats.get(metric).get(module).getHighestRisk();
	}

	public Number getHighestRiskCauser(final FunctionMetric metric, final Module module) {
		return data.functionModuleStats.get(metric).get(module).getHighestRisk();
	}

	public Number getHighestRiskCauser(final TestcaseMetric metric, final Module module) {
		return data.testcaseModuleStats.get(metric).get(module).getHighestRisk();
	}

	public double getRiskValue(final ModuleMetric metric, final Module module) {
		return data.risks.getRisk(metric).getRiskValue(get(metric, module));
	}

	public double getRiskValue(final AltstepMetric metric, final Module module) {
		return data.risks.getRisk(metric).getRiskValue(getHighestRiskCauser(metric, module));
	}

	public double getRiskValue(final FunctionMetric metric, final Module module) {
		return data.risks.getRisk(metric).getRiskValue(getHighestRiskCauser(metric, module));
	}

	public double getRiskValue(final TestcaseMetric metric, final Module module) {
		return data.risks.getRisk(metric).getRiskValue(getHighestRiskCauser(metric, module));
	}

	public RiskLevel getRisk(final ModuleMetric metric, final Module module) {
		return data.risks.getRisk(metric).getRiskLevel(get(metric, module));
	}

	public RiskLevel getRisk(final AltstepMetric metric, final Module module) {
		return data.risks.getRisk(metric).getRiskLevel(getHighestRiskCauser(metric, module));
	}

	public RiskLevel getRisk(final FunctionMetric metric, final Module module) {
		return data.risks.getRisk(metric).getRiskLevel(getHighestRiskCauser(metric, module));
	}

	public RiskLevel getRisk(final TestcaseMetric metric, final Module module) {
		return data.risks.getRisk(metric).getRiskLevel(getHighestRiskCauser(metric, module));
	}

	public Number get(final ProjectMetric metric) {
		return data.projectMetrics.get(metric);
	}

	public Number getHighestRiskCauser(final ModuleMetric metric) {
		return data.moduleProjectStats.get(metric).getHighestRisk();
	}

	public Number getHighestRiskCauser(final AltstepMetric metric) {
		return data.altstepProjectStats.get(metric).getHighestRisk();
	}

	public Number getHighestRiskCauser(final FunctionMetric metric) {
		return data.functionProjectStats.get(metric).getHighestRisk();
	}

	public Number getHighestRiskCauser(final TestcaseMetric metric) {
		return data.testcaseProjectStats.get(metric).getHighestRisk();
	}

	public double getRiskValue(final ProjectMetric metric) {
		return data.risks.getRisk(metric).getRiskValue(get(metric));
	}

	public double getRiskValue(final ModuleMetric metric) {
		return data.risks.getRisk(metric).getRiskValue(getHighestRiskCauser(metric));
	}

	public double getRiskValue(final AltstepMetric metric) {
		return data.risks.getRisk(metric).getRiskValue(getHighestRiskCauser(metric));
	}

	public double getRiskValue(final FunctionMetric metric) {
		return data.risks.getRisk(metric).getRiskValue(getHighestRiskCauser(metric));
	}

	public double getRiskValue(final TestcaseMetric metric) {
		return data.risks.getRisk(metric).getRiskValue(getHighestRiskCauser(metric));
	}

	public RiskLevel getRisk(final ProjectMetric metric) {
		return data.risks.getRisk(metric).getRiskLevel(get(metric));
	}

	public RiskLevel getRisk(final ModuleMetric metric) {
		return data.risks.getRisk(metric).getRiskLevel(getHighestRiskCauser(metric));
	}

	public RiskLevel getRisk(final AltstepMetric metric) {
		return data.risks.getRisk(metric).getRiskLevel(getHighestRiskCauser(metric));
	}

	public RiskLevel getRisk(final FunctionMetric metric) {
		return data.risks.getRisk(metric).getRiskLevel(getHighestRiskCauser(metric));
	}

	public RiskLevel getRisk(final TestcaseMetric metric) {
		return data.risks.getRisk(metric).getRiskLevel(getHighestRiskCauser(metric));
	}

	public Statistics getStatistics(final AltstepMetric metric, final Module module) {
		return data.altstepModuleStats.get(metric).get(module);
	}

	public Statistics getStatistics(final FunctionMetric metric, final Module module) {
		return data.functionModuleStats.get(metric).get(module);
	}

	public Statistics getStatistics(final TestcaseMetric metric, final Module module) {
		return data.testcaseModuleStats.get(metric).get(module);
	}

	public Statistics getStatistics(final AltstepMetric metric) {
		return data.altstepProjectStats.get(metric);
	}

	public Statistics getStatistics(final FunctionMetric metric) {
		return data.functionProjectStats.get(metric);
	}

	public Statistics getStatistics(final TestcaseMetric metric) {
		return data.testcaseProjectStats.get(metric);
	}

	public Statistics getStatistics(final ModuleMetric metric) {
		return data.moduleProjectStats.get(metric);
	}

	/**
	 * Execute the metrics on the project and compose the results.
	 * <p>
	 * Note that internally the project is locked.
	 * 
	 * @param project
	 *            the project to analyze
	 * 
	 * @return the composed result of the measurements
	 */
	public static MetricData measure(final IProject project) {
		synchronized (project) {
			// reading the lists of altsteps, testcases, functions and modules
			// that are to be measured
			final ProjectSourceParser parser = GlobalParser.getProjectSourceParser(project);
			final Risks risks = new Risks();
			final MutableMetricData data = new MutableMetricData(risks);

			final List<Module> modules = new ArrayList<Module>();
			final Map<Module, List<Def_Function>> functions = new HashMap<Module, List<Def_Function>>();
			final Map<Module, List<Def_Testcase>> testcases = new HashMap<Module, List<Def_Testcase>>();
			final Map<Module, List<Def_Altstep>> altsteps = new HashMap<Module, List<Def_Altstep>>();
			for (final String modName : parser.getKnownModuleNames()) {
				final Module module = parser.getModuleByName(modName);
				modules.add(module);
			}
			for (final Module module : modules) {
				final List<Def_Function> funs = new ArrayList<Def_Function>();
				final List<Def_Testcase> tcs = new ArrayList<Def_Testcase>();
				final List<Def_Altstep> als = new ArrayList<Def_Altstep>();

				module.accept(new DefinitionCollector(funs, tcs, als));

				functions.put(module, funs);
				testcases.put(module, tcs);
				altsteps.put(module, als);
			}
			data.modules = Collections.unmodifiableList(modules);
			data.functions = Collections.unmodifiableMap(functions);
			data.testcases = Collections.unmodifiableMap(testcases);
			data.altsteps = Collections.unmodifiableMap(altsteps);

			MetricData immutableData = new MetricData(project, data);

			// initiate the metrics
			final Metrics metrics = new Metrics();
			for (final AltstepMetric am : AltstepMetric.values()) {
				metrics.get(am).init(immutableData);
			}
			for (final FunctionMetric fm : FunctionMetric.values()) {
				metrics.get(fm).init(immutableData);
			}
			for (final TestcaseMetric tm : TestcaseMetric.values()) {
				metrics.get(tm).init(immutableData);
			}
			for (final ModuleMetric mm : ModuleMetric.values()) {
				metrics.get(mm).init(immutableData);
			}
			for (final ProjectMetric pm : ProjectMetric.values()) {
				metrics.get(pm).init(immutableData);
			}

			// execute the metrics, one by one
			// altstep metrics and statistics
			for (final AltstepMetric am : AltstepMetric.values()) {
				final Statistics projectStats = measureEntities(data.altsteps, metrics.get(am), immutableData, data.altstepMetrics,
						data.altstepModuleStats);
				data.altstepProjectStats.put(am, projectStats);
				immutableData = new MetricData(project, data);
			}
			// function metrics and statistics
			for (final FunctionMetric fm : FunctionMetric.values()) {
				final Statistics projectStats = measureEntities(data.functions, metrics.get(fm), immutableData, data.functionMetrics,
						data.functionModuleStats);
				data.functionProjectStats.put(fm, projectStats);
				immutableData = new MetricData(project, data);
			}
			// testcase metrics and statistics
			for (final TestcaseMetric tm : TestcaseMetric.values()) {
				final Statistics projectStats = measureEntities(data.testcases, metrics.get(tm), immutableData, data.testcaseMetrics,
						data.testcaseModuleStats);
				data.testcaseProjectStats.put(tm, projectStats);
				immutableData = new MetricData(project, data);
			}

			// module metrics and statistics
			for (final ModuleMetric mm : ModuleMetric.values()) {
				final Map<Module, Number> metricResults = new HashMap<Module, Number>();
				final int numberOfModules = data.modules.size();

				double[] projectLevelResults = new double[numberOfModules];
				int projectLevelCounter = 0;
				for (final Module module : data.modules) {
					final Number result = metrics.get(mm).measure(immutableData, module);
					projectLevelResults[projectLevelCounter++] = result.doubleValue();
					metricResults.put(module, result);
				}
				data.moduleMetrics.put(mm, metricResults);
				data.moduleProjectStats.put(mm, new Statistics(projectLevelResults, mm, risks.getRisk(mm)));
			}

			// project metrics
			for (final ProjectMetric pm : ProjectMetric.values()) {
				final Number result = metrics.get(pm).measure(immutableData, project);
				data.projectMetrics.put(pm, result);
			}
			return immutableData;
		}
	}

	static <ENTITY, METRIC extends IMetricEnum> Statistics measureEntities(final Map<Module, List<ENTITY>> entities, final IMetric<ENTITY, METRIC> metric,
			final MetricData currentData, final Map<METRIC, Map<ENTITY, Number>> metricResults, final Map<METRIC, Map<Module, Statistics>> moduleStats) {
		int numberOfEntities = 0;
		
		for (final List<ENTITY> entitylist : entities.values()) {
			numberOfEntities += entitylist.size();
		}

		final Map<ENTITY, Number> myMetricResults = new HashMap<ENTITY, Number>();
		final Map<Module, Statistics> myModuleStats = new HashMap<Module, Statistics>();

		double[] projectLevelResults = new double[numberOfEntities];
		int projectLevelCounter = 0;
		for (final Entry<Module, List<ENTITY>> entry : entities.entrySet()) {
			final Module module = entry.getKey();
			final List<ENTITY> entitiesOfModule = entry.getValue();
			int moduleLevelCounter = 0;
			double[] moduleLevelResults = new double[entitiesOfModule.size()];
			for (final ENTITY entity : entitiesOfModule) {
				final Number result = metric.measure(currentData, entity);
				moduleLevelResults[moduleLevelCounter++] = result.doubleValue();
				projectLevelResults[projectLevelCounter++] = result.doubleValue();
				myMetricResults.put(entity, result);
			}
			myModuleStats.put(module,
					new Statistics(moduleLevelResults, metric.getMetric(), currentData.getRisks().getRisk(metric.getMetric())));
		}

		metricResults.put(metric.getMetric(), myMetricResults);
		moduleStats.put(metric.getMetric(), myModuleStats);

		return new Statistics(projectLevelResults, metric.getMetric(), currentData.getRisks().getRisk(metric.getMetric()));
	}

	private static class MutableMetricData {
		List<Module> modules = null;
		Map<Module, List<Def_Function>> functions = null;
		Map<Module, List<Def_Testcase>> testcases = null;
		Map<Module, List<Def_Altstep>> altsteps = null;
		Map<AltstepMetric, Map<Def_Altstep, Number>> altstepMetrics = new HashMap<AltstepMetric, Map<Def_Altstep, Number>>();
		Map<AltstepMetric, Map<Module, Statistics>> altstepModuleStats = new HashMap<AltstepMetric, Map<Module, Statistics>>();
		Map<AltstepMetric, Statistics> altstepProjectStats = new HashMap<AltstepMetric, Statistics>();
		Map<FunctionMetric, Map<Def_Function, Number>> functionMetrics = new HashMap<FunctionMetric, Map<Def_Function, Number>>();
		Map<FunctionMetric, Map<Module, Statistics>> functionModuleStats = new HashMap<FunctionMetric, Map<Module, Statistics>>();
		Map<FunctionMetric, Statistics> functionProjectStats = new HashMap<FunctionMetric, Statistics>();
		Map<TestcaseMetric, Map<Def_Testcase, Number>> testcaseMetrics = new HashMap<TestcaseMetric, Map<Def_Testcase, Number>>();
		Map<TestcaseMetric, Map<Module, Statistics>> testcaseModuleStats = new HashMap<TestcaseMetric, Map<Module, Statistics>>();
		Map<TestcaseMetric, Statistics> testcaseProjectStats = new HashMap<TestcaseMetric, Statistics>();
		Map<ModuleMetric, Map<Module, Number>> moduleMetrics = new HashMap<ModuleMetric, Map<Module, Number>>();
		Map<ModuleMetric, Statistics> moduleProjectStats = new HashMap<ModuleMetric, Statistics>();
		Map<ProjectMetric, Number> projectMetrics = new HashMap<ProjectMetric, Number>();

		Risks risks;

		public MutableMetricData(final Risks risks) {
			this.risks = risks;
		}
	}

	private static class DefinitionCollector extends ASTVisitor {
		private final List<Def_Function> funs;
		private final List<Def_Testcase> tcs;
		private final List<Def_Altstep> als;

		public DefinitionCollector(final List<Def_Function> functions, final List<Def_Testcase> testcases, final List<Def_Altstep> altsteps) {
			tcs = testcases;
			funs = functions;
			als = altsteps;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Def_Function) {
				funs.add((Def_Function) node);
				return V_SKIP;
			} else if (node instanceof Def_Testcase) {
				tcs.add((Def_Testcase) node);
				return V_SKIP;
			} else if (node instanceof Def_Altstep) {
				als.add((Def_Altstep) node);
				return V_SKIP;
			} else {
				return V_CONTINUE;
			}
		}
	}
}
