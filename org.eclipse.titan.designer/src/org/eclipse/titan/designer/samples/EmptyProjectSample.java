/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.samples;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

/**
 * @author Szabolcs Beres
 * */
final class EmptyProjectSample extends SampleProject {
	@Override
	public String getName() {
		return "Empty Project";
	}

	@Override
	public Map<String, String> getSourceFileContent() {
		return new HashMap<String, String>();
	}

	@Override
	public String getDescription() {
		return "Creates an empty project";
	}

	@Override
	public void setupProject(final IProject project, final IFolder sourceFolder) {
		// Do nothing
		return;
	}
}