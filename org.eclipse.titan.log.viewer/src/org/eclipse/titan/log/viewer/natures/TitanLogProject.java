/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * Class that implements a TITAN project nature
 *
 */
//TODO: check if this is needed still
public class TitanLogProject implements IProjectNature {

	
	private IProject project;

	@Override
	public void configure() throws CoreException {
		// Do nothing
	}

	@Override
	public void deconfigure() throws CoreException {
		// Do nothing
	}

	@Override
	public IProject getProject() {
		return this.project;
	}

	@Override
	public void setProject(final IProject project) {
		this.project = project;
	}
}
