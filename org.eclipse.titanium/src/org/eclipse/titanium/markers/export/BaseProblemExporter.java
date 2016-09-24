/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.export;

import static org.eclipse.titan.designer.preferences.PreferenceConstants.DISPLAYDEBUGINFORMATION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titanium.markers.types.TaskType;

/**
 * This class provides a skeleton for code smell exporting.
 * It is independent from output file format.
 * 
 * @author Gabor Jenei
 */
public abstract class BaseProblemExporter {
	protected IProject project;
	protected boolean reportDebugInformation;
	
	/**
	 * Constructor
	 * @param proj : The project to export markers from
	 */
	public BaseProblemExporter(final IProject proj) {
		project = proj;
		reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				DISPLAYDEBUGINFORMATION, true, null);
	}
	
	/**
	 * Collect the markers from the selected project.
	 * 
	 * @return the list of markers collected.
	 * @throws CoreException
	 */
	protected Map<TaskType, List<IMarker>> collectMarkers() throws CoreException {
		final Map<TaskType, List<IMarker>> markers = new EnumMap<TaskType, List<IMarker>>(TaskType.class);
		for (final TaskType t : TaskType.values()) {
			markers.put(t, new ArrayList<IMarker>());
		}

		final List<IProject> projects = ProjectBasedBuilder.getProjectBasedBuilder(project).getAllReachableProjects();
		for(final IProject tempProject : projects) {
			final IMarker[] ms = tempProject.findMarkers(GeneralConstants.ONTHEFLY_TASK_MARKER, false, IResource.DEPTH_INFINITE);
			for (final IMarker m : ms) {
				for (final TaskType t : TaskType.values()) {
					if (t.equalType(m)) {
						markers.get(t).add(m);
					}
				}
			}
		}

		return markers;
	}
	
	/**
	 * This method should implement the export to the given file format
	 * 
	 * @param monitor : The monitor where we show the current state of the process
	 * @param path : The file's path where we export
	 * @param date : The time stamp to write on the summary page (null if we use present time)
	 * 
	 * @throws IOException On IO error
	 */
	public abstract void exportMarkers(IProgressMonitor monitor, String path, Date date) throws IOException;
}