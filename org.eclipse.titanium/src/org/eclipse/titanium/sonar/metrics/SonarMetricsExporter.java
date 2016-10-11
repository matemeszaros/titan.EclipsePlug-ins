/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.sonar.metrics;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.TestcaseMetric;

import java.io.File;

public class SonarMetricsExporter {

	public void export(final MetricData metricData, final File file) throws JAXBException {

		final ProjectMetricsDto project = new ProjectMetricsDto(metricData.getProject().getName());

		for (final Module module : metricData.getModules()) {
			final ModuleMetricsDto metricModule = new ModuleMetricsDto(module.getLocation().getFile().getProjectRelativePath().toPortableString());
			metricModule.setLinesOfCode(metricData.get(ModuleMetric.LINES_OF_CODE, module).intValue());
			metricModule.setStatements(metricData.get(ModuleMetric.NOF_STATEMENTS, module).intValue());
			metricModule.setFunctions(metricData.get(ModuleMetric.NOF_FUNCTIONS, module).intValue());
			metricModule.setAltsteps(metricData.get(ModuleMetric.NOF_ALTSTEPS, module).intValue());
			metricModule.setTestCases(metricData.get(ModuleMetric.NOF_TESTCASES, module).intValue());
			metricModule.setComplexity(calculateComplexity(metricData, module));

			project.addModule(metricModule);
		}

		marshalToFile(project, file);
	}

	private int calculateComplexity(final MetricData metricData, final Module module) {
		int complexity = 0;
		for (final Def_Function function : metricData.getFunctions().get(module)) {
			complexity += metricData.get(FunctionMetric.CYCLOMATIC_COMPLEXITY, function).intValue();
		}

		for (final Def_Altstep altstep : metricData.getAltsteps().get(module)) {
			complexity += metricData.get(AltstepMetric.CYCLOMATIC_COMPLEXITY, altstep).intValue();
		}

		for (final Def_Testcase testcase : metricData.getTestcases().get(module)) {
			complexity += metricData.get(TestcaseMetric.CYCLOMATIC_COMPLEXITY, testcase).intValue();
		}
		return complexity;
	}



	public static void marshalToFile(final Object objectToMarshal, final File file) throws JAXBException {
		final JAXBContext jaxbContext = JAXBContext.newInstance(objectToMarshal.getClass());
		final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		jaxbMarshaller.marshal(objectToMarshal,file);
	}
}
