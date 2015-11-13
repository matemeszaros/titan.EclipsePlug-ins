/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.regressiontests.library.MarkerHandlingLibrary;
import org.eclipse.titanium.regressiontests.library.ProjectHandlingLibrary;
import org.eclipse.titanium.regressiontests.library.WorkspaceHandlingLibrary;
import org.junit.Test;

public class GeneralTests {

	/**
	 * This general test checking the markers consistency between 2 code analization without any code change between them.
	 */
	@Test
	public void noChangeConsistency() throws Exception {
		IProject[] projects = WorkspaceHandlingLibrary.getProjectsInWorkspace();
		ProjectHandlingLibrary projectLibrary;
		for (IProject project : projects) {
			projectLibrary = new ProjectHandlingLibrary(project);
			projectLibrary.analyzeProject();

			HashMap<IResource, ArrayList<Map<?, ?>>> semanticMarkers1 = MarkerHandlingLibrary.transformMarkers(projectLibrary.getMarkers(CodeSmellType.MARKER_ID));

			projectLibrary.analyzeProject();

			HashMap<IResource, ArrayList<Map<?, ?>>> semanticMarkers2 = MarkerHandlingLibrary.transformMarkers(projectLibrary.getMarkers(CodeSmellType.MARKER_ID));

			if (semanticMarkers1.size() != semanticMarkers2.size()) {
				HashSet<IResource> all = new HashSet<IResource>();
				all.addAll(semanticMarkers1.keySet());
				all.addAll(semanticMarkers2.keySet());

				for (IResource resource : all) {
					if (!semanticMarkers1.containsKey(resource)) {
						System.err.println("The second check found semantic markers on the file '" + resource.getName() + "' but the first did not.");
					} else if (!semanticMarkers2.containsKey(resource)) {
						System.err.println("The first check semantic markers on the file '" + resource.getName() + "' but the second did not.");
					}
				}
			}

			assertEquals(semanticMarkers1.size(), semanticMarkers2.size());
			ArrayList<Map<?, ?>> markerlist1, markerlist2;
			for (IResource resource : semanticMarkers1.keySet()) {
				markerlist1 = semanticMarkers1.get(resource);
				if (!semanticMarkers2.containsKey(resource)) {
					assertTrue(false);
				}

				markerlist2 = semanticMarkers2.get(resource);

				assertEquals(markerlist1.size(), markerlist2.size());
				for (int i = 0; i < markerlist1.size(); i++) {
					MarkerHandlingLibrary.blindMarkerEquivencyCheck(markerlist1.get(i), markerlist2.get(i));
				}
			}
		}
	}

	/**
	 * This general test checking the markers consistency between 2 code analization with simulating code change between them.
	 * 
	 */
	@Test
	public void touchChangeConsistency() throws Exception {
		IProject[] projects = WorkspaceHandlingLibrary.getProjectsInWorkspace();
		ProjectHandlingLibrary projectLibrary;
		for (IProject project : projects) {

			projectLibrary = new ProjectHandlingLibrary(project);
			projectLibrary.analyzeProject();

			HashMap<IResource, ArrayList<Map<?, ?>>> semanticMarkers1 = MarkerHandlingLibrary.transformMarkers(projectLibrary.getMarkers(CodeSmellType.MARKER_ID));

			project.accept(new IResourceVisitor() {

				@Override
				public boolean visit(final IResource resource) throws CoreException {
					resource.touch(null);
					return true;
				}

			});

			projectLibrary.analyzeProject();

			HashMap<IResource, ArrayList<Map<?, ?>>> semanticMarkers2 = MarkerHandlingLibrary.transformMarkers(projectLibrary.getMarkers(CodeSmellType.MARKER_ID));

			assertEquals(semanticMarkers1.size(), semanticMarkers2.size());
			ArrayList<Map<?, ?>> markerlist1, markerlist2;
			for (IResource resource : semanticMarkers1.keySet()) {
				markerlist1 = semanticMarkers1.get(resource);
				if (!semanticMarkers2.containsKey(resource)) {
					assertTrue(false);
				}

				markerlist2 = semanticMarkers2.get(resource);

				if (markerlist1.size() != markerlist2.size()) {
					System.out.println(resource.getProjectRelativePath().toString());
					MarkerHandlingLibrary.printMarkerArray(resource.getProjectRelativePath().toString(), markerlist1);
				}
				assertEquals(markerlist1.size(), markerlist2.size());
				System.out.println(resource.getName());
				for (int i = 0; i < markerlist1.size(); i++) {
					MarkerHandlingLibrary.blindMarkerEquivencyCheck(markerlist1.get(i), markerlist2.get(i));
				}
			}
		}
	}
}
