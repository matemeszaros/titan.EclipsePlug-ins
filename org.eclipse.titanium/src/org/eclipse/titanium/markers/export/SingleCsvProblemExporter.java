/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.markers.handler.Marker;
import org.eclipse.titanium.markers.handler.MarkerHandler;
import org.eclipse.titanium.markers.utils.AnalyzerCache;

/**
 * This class implements export to a single CSV file
 * @author Gabor Jenei
 */
public class SingleCsvProblemExporter extends CsvProblemExporter {
	
	/**
	 * Constructor
	 * @param proj : The project to be analyzed
	 */
	public SingleCsvProblemExporter(IProject proj) {
		super(proj);
	}

	/**
	 * @param monitor : The monitor that we use for feedback
	 * @param path : The path of the file to be saved
	 * @param date : The time stamp (not used currently)
	 * 
	 * @see BaseProblemExporter#exportMarkers(IProgressMonitor, String, Date)
	 */
	@Override
	public void exportMarkers(IProgressMonitor monitor, String path, Date date) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		BufferedWriter summaryFile = null;
		try {
			summaryFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path)),"UTF-8"));

			MarkerHandler mh = AnalyzerCache.withAll().analyzeProject(progress.newChild(30), project);

			for (Map.Entry<IResource, List<Marker>> entry : mh.getMarkersByResource().entrySet()) {
				IResource resource = entry.getKey();

				for (Marker marker : entry.getValue()) {
					summaryFile.write(resource.getProjectRelativePath().toString());
					summaryFile.write(SEPARATOR);
					summaryFile.write(String .valueOf(marker.getLine()));
					summaryFile.write(SEPARATOR);
					summaryFile.write(marker.getProblemType().name());
					summaryFile.write(SEPARATOR);
					
					summaryFile.write(SEPARATOR);
					summaryFile.write(SEPARATOR);
					summaryFile.newLine();
				}
			}
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Error while exporting to CSV file: " + path, e);
		} finally {
			if (summaryFile != null) {
				try {
					summaryFile.close();
				} catch (IOException e) {
					ErrorReporter.logExceptionStackTrace("Error while closing the file: " + path, e);
				}
			}
		}
	}
}