/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.export;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.markers.handler.Marker;
import org.eclipse.titanium.markers.handler.MarkerHandler;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.markers.types.TaskType;
import org.eclipse.titanium.markers.utils.AnalyzerCache;

/**
 * This class exports code smells to CSV files
 * @author Gabor Jenei
 */
public class CsvProblemExporter extends BaseProblemExporter {
	protected static final String SEPARATOR = ";";
	
	/**
	 * Constructor
	 * @param proj : The project to export markers from
	 */
	public CsvProblemExporter(IProject proj) {
		super(proj);
	}
	
	/**
	 * Export the code smells of a project to CSV files.
	 * <p>
	 * There is always a CSV with the name summary, which contains the names of
	 * all code smells, and their occurrence in the actual project. And for each
	 * code smell, where at least 1 occurrence was present a separate CSV is
	 * created too. In this CSV the reported error messages, the path of the
	 * resources and the line number in which the smell was found is listed.
	 * 
	 * Note: All code smell types are used in the analysis and are written in
	 * the output. Some code smells use external settings, which can be fine
	 * tuned on the preference page.
	 * 
	 * @param filenamePrefix
	 *            the filename prefix to be used in creating the CSV files.
	 * @param date
	 *            the time stamp to be used (currently unused)
	 * 
	 * @throws IOException
	 *             when writing the file fails
	 */
	@Override
	public void exportMarkers(IProgressMonitor monitor, String filenamePrefix, Date date) throws IOException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		PrintWriter summaryFile = new PrintWriter(new FileWriter(filenamePrefix + "_summary.csv"));
		PrintWriter timesFile = new PrintWriter(new FileWriter(filenamePrefix + "_times.csv"));

		try {
			summaryFile.println("Smell name" + SEPARATOR + "Amount");
			timesFile.println("Smell name" + SEPARATOR + "Minimal repair time"
					 + SEPARATOR + "Average repair time"  + SEPARATOR + "Maximal repair time");
			PrintWriter actualSmellFile = null;
			Map<TaskType, List<IMarker>> markers = collectMarkers();
			for (TaskType actSmell : TaskType.values()) {
				int row = 0;
				if (!markers.get(actSmell).isEmpty()) {
					actualSmellFile = new PrintWriter(new FileWriter(filenamePrefix + "_" + actSmell.getHumanReadableName() + ".csv"));
					actualSmellFile.println("Message" + SEPARATOR + "Smell name" + SEPARATOR + "Line number");
					for (IMarker m : markers.get(actSmell)) {
						actualSmellFile.println(
								m.getAttribute(IMarker.MESSAGE).toString()
								+ SEPARATOR
								+ m.getResource().getName()
								+ SEPARATOR
								+ m.getAttribute(IMarker.LINE_NUMBER));
						++row;
					}
					actualSmellFile.close();
					actualSmellFile = null;
				}
				summaryFile.println(actSmell.getHumanReadableName() + SEPARATOR + row);
				timesFile.println(actSmell.getHumanReadableName() + SEPARATOR + row * actSmell.getMinRepairTime()
						+ SEPARATOR + row * actSmell.getAvgRepairTime() + SEPARATOR + row * actSmell.getMaxRepairTime());
			}
			progress.worked(20);

			MarkerHandler mh = AnalyzerCache.withAll().analyzeProject(progress.newChild(30), project);
			progress.setWorkRemaining(CodeSmellType.values().length);
			for (CodeSmellType actSmell : CodeSmellType.values()) {
				int row = 0;
				if (!mh.get(actSmell).isEmpty()) {
					actualSmellFile = new PrintWriter(new FileWriter(filenamePrefix + "_" + actSmell.name() + ".csv"));
					actualSmellFile.println("Description; Resurce; Location");
					for (Marker m : mh.get(actSmell)) {
						if (m.getLine() == -1 || m.getResource() == null) {
							continue;
						}
						actualSmellFile.println(m.getMessage() + SEPARATOR + m.getResource().getName() + SEPARATOR
								+ m.getLine());
						++row;
					}
					actualSmellFile.close();
					actualSmellFile = null;
				}
				
				summaryFile.println(actSmell.getHumanReadableName() + SEPARATOR + row + SEPARATOR + row * actSmell.getMinRepairTime()
						+ SEPARATOR + row * actSmell.getAvgRepairTime() + SEPARATOR + row * actSmell.getMaxRepairTime());
				timesFile.println(actSmell.getHumanReadableName() + SEPARATOR + row * actSmell.getMinRepairTime()
						+ SEPARATOR + row * actSmell.getAvgRepairTime() + SEPARATOR + row * actSmell.getMaxRepairTime());
				progress.worked(1);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while exporting to csv", e);
		} finally {
			summaryFile.close();
			timesFile.close();
		}
	}
}