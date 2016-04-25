/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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

	public SonarDataExporter(final IProject project) {
		this.project = project;
	}

	public void exportDataForProject() throws IOException {
		final URI projectLocation = project.getLocationURI();
		if (projectLocation == null) {
			ErrorReporter.logError("Error while getting project uri");
			return;
		}

		final URI exportDir = URIUtil.append(projectLocation, ".sonar_titanium");
		System.out.println("Export dir is: " + exportDir.getPath());
		final File file = new File(exportDir);
		if (!file.exists() && !file.mkdirs()) {
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
		final URI srcDir = URIUtil.append(project.getLocationURI(), "src");
		final File srcDirFile = new File(srcDir);
		if (!srcDirFile.exists()) {
			srcDirFile.mkdirs();
		}
	}

	private void exportMetrics(final URI exportDir) throws IOException {
		final URI filePath = URIUtil.append(exportDir, "metrics.xml");
		final File file = new File(filePath);
		FileUtils.delete(file);
		final SonarMetricsExporter exporter = new SonarMetricsExporter();

		try {
			exporter.export(MetricData.measure(project), file);
		} catch (JAXBException e) {
			new ConsoleErrorHandler().reportException("Error while exporting the project metrics", e);
		}
	}

	private void exportProjectStructure(final URI exportDir) throws IOException {

		final URI filePath = URIUtil.append(exportDir, "project_structure.csv");
		final File file = new File(filePath);
		FileUtils.delete(file);

		final ProjectStructureExporter exporter = new ProjectStructureExporter(project);
		exporter.saveTo(filePath);
	}

	private void createSonarPropertiesFile() throws IOException {
		final URI projectLocation = project.getLocationURI();
		final URI propertiesFileLocaiton = URIUtil.append(projectLocation, SONAR_PROJECT_PROPERTIES_FILE);

		final File propertiesFile = new File(propertiesFileLocaiton);

		if (propertiesFile.exists()) {
			return;
		}

		final String propertyFileTemplate = "/resources/" + SONAR_PROJECT_PROPERTIES_FILE;
		final InputStream stream = SonarDataExporter.class.getResourceAsStream(propertyFileTemplate);
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

	private void exportCodeSmells(final URI exportDir) throws IOException {
		final URI filePath = URIUtil.append(exportDir, "code_smells.csv");
		final File file = new File(filePath);
		FileUtils.delete(file);
		final BaseProblemExporter exporter = new SingleCsvProblemExporter(project);
		exporter.exportMarkers(new NullProgressMonitor(), filePath.getPath(), Calendar.getInstance().getTime());
	}

	private void exportModuleDot(final URI exportDir) {
		final URI filePath = URIUtil.append(exportDir, "module_graph.dot");
		final ErrorHandler errorHandler = new ConsoleErrorHandler();
		final GraphGenerator generator = new ModuleGraphGenerator(project, errorHandler);
		try {
			generator.generateGraph();
			GraphHandler.saveGraphToDot(generator.getGraph(), filePath.getPath(),
					project.getName());
		} catch (Exception e) {
			errorHandler.reportException("Error while exporting the module graph", e);
		}
	}

	private void exportComponentDot(final URI exportDir) {
		final URI filePath = URIUtil.append(exportDir, "component_graph.dot");
		final ErrorHandler errorHandler = new ConsoleErrorHandler();
		final GraphGenerator generator = new ComponentGraphGenerator(project, errorHandler);
		try {
			generator.generateGraph();
			GraphHandler.saveGraphToDot(generator.getGraph(), filePath.getPath(),
					project.getName());
		} catch (Exception e) {
			errorHandler.reportException("Error while exporting the component graph", e);
		}

	}
}
