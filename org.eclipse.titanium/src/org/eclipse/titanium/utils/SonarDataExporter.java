/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.FileUtils;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titanium.error.ConsoleErrorHandler;
import org.eclipse.titanium.error.ErrorHandler;
import org.eclipse.titanium.graph.generators.ComponentGraphGenerator;
import org.eclipse.titanium.graph.generators.GraphGenerator;
import org.eclipse.titanium.graph.generators.ModuleGraphGenerator;
import org.eclipse.titanium.graph.visualization.GraphHandler;
import org.eclipse.titanium.markers.export.BaseProblemExporter;
import org.eclipse.titanium.markers.export.SingleCsvProblemExporter;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.sonar.metrics.SonarMetricsExporter;

/**
 * A class providing data exporting features for sonar input
 *  
 * @author eszabre
 */
public class SonarDataExporter {

	private static final String SONAR_PROJECT_PROPERTIES_FILE = "sonar-project.properties";

	private IProject project;

	public SonarDataExporter(IProject project) {
		this.project = project;
	}

	public void exportDataForProject() throws IOException {
		URI projectLocation = project.getLocationURI();
		if (projectLocation == null) {
			ErrorReporter.logError("Error while getting project uri");
			return;
		}

		URI exportDir = URIUtil.append(projectLocation, ".sonar_titanium");
		System.out.println("Export dir is: " + exportDir.getPath());
		File f = new File(exportDir);
		if (!f.exists() && !f.mkdirs()) {
			throw new IOException("Cannot create output directory: " + exportDir);
		}
		try {
			createSonarPropertiesFile();
			createSrcDirIfMissing();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Error while creating Sonar properties file", e);
		}
		exportProjectStructure(exportDir);
		exportCodeSmells(exportDir);
		exportMetrics(exportDir);
		exportModuleDot(exportDir);
		exportComponentDot(exportDir);
		exportProjectStructure(exportDir);
	}

	private void createSrcDirIfMissing() {
		URI srcDir = URIUtil.append(project.getLocationURI(), "src");
		File srcDirFile = new File(srcDir);
		if (!srcDirFile.exists()) {
			srcDirFile.mkdirs();
		}
	}

	private void exportMetrics(URI exportDir) throws IOException {
		URI filePath = URIUtil.append(exportDir, "metrics.xml");
		File f = new File(filePath);
		FileUtils.delete(f);
		SonarMetricsExporter exporter = new SonarMetricsExporter();

		try {
			exporter.export(MetricData.measure(project), f);
		} catch (JAXBException e) {
			new ConsoleErrorHandler().reportException("Error while exporting the project metrics", e);
		}
	}

	private void exportProjectStructure(URI exportDir) throws IOException {

		URI filePath = URIUtil.append(exportDir, "project_structure.csv");
		File file = new File(filePath);
		FileUtils.delete(file);

		ProjectStructureExporter exporter = new ProjectStructureExporter(project);
		exporter.saveTo(filePath);
	}

	private void createSonarPropertiesFile() throws IOException {
		URI projectLocation = project.getLocationURI();
		URI propertiesFileLocaiton = URIUtil.append(projectLocation, SONAR_PROJECT_PROPERTIES_FILE);

		File propertiesFile = new File(propertiesFileLocaiton);

		if (propertiesFile.exists()) {
			return;
		}

		String propertyFileTemplate = "/resources/" + SONAR_PROJECT_PROPERTIES_FILE;
		InputStream stream = SonarDataExporter.class.getResourceAsStream(propertyFileTemplate);
		if (stream == null) {
			ErrorReporter.logError("Cannot find the resource " + propertyFileTemplate);
			return;
		}
		try {
			String content = IOUtils.inputStreamToString(stream);
			content = content.replaceAll(Pattern.quote("___PROJECT_KEY___"), project.getName());
			content = content.replaceAll(Pattern.quote("___PROJECT_NAME___"), project.getName());

			IOUtils.writeStringToFile(new File(propertiesFileLocaiton), content);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	private void exportCodeSmells(URI exportDir) throws IOException {
		URI filePath = URIUtil.append(exportDir, "code_smells.csv");
		File f = new File(filePath);
		FileUtils.delete(f);
		BaseProblemExporter exporter = new SingleCsvProblemExporter(project);
		exporter.exportMarkers(new NullProgressMonitor(), filePath.getPath(), Calendar.getInstance().getTime());
	}

	private void exportModuleDot(URI exportDir) {
		URI filePath = URIUtil.append(exportDir, "module_graph.dot");
		final ErrorHandler errorHandler = new ConsoleErrorHandler();
		GraphGenerator generator = new ModuleGraphGenerator(project, errorHandler);
		try {
			generator.generateGraph();
			GraphHandler.saveGraphToDot(generator.getGraph(), filePath.getPath(),
					project.getName());
		} catch (Exception e) {
			errorHandler.reportException("Error while exporting the module graph", e);
		}
	}

	private void exportComponentDot(URI exportDir) {
		URI filePath = URIUtil.append(exportDir, "component_graph.dot");
		final ErrorHandler errorHandler = new ConsoleErrorHandler();
		GraphGenerator generator = new ComponentGraphGenerator(project, errorHandler);
		try {
			generator.generateGraph();
			GraphHandler.saveGraphToDot(generator.getGraph(), filePath.getPath(),
					project.getName());
		} catch (Exception e) {
			errorHandler.reportException("Error while exporting the component graph", e);
		}

	}
}
