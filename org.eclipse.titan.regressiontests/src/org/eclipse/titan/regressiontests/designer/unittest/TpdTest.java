/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer.unittest;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.designer.wizards.projectFormat.TpdImporter;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.WorkspaceHandlingLibrary;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TpdTest {

	@Test
	public void testGetSchema() {
		try {
			TpdImporter.getTPDSchema();
		} catch (final IOException e) {
			fail("Cannot load the tpd file:" + e.toString());
		} catch (SAXException e) {
			fail("Cannot load the tpd file:" + e.toString());
		}
	}

	//@Ignore
	@Test
	public void validateAllKnownTpds() {
		for (IFile file : getAllTpdFiles()) {
			try {
				TpdImporter.validateTpd(new File(file.getLocationURI()));
			} catch (IOException e) {
				fail(e.getMessage());
			} catch (SAXException e) {
				fail(file.getName() + ": " + e.getMessage());
			}
		}
	}

	private List<IFile> getAllTpdFiles() {
		IProject project = WorkspaceHandlingLibrary.getWorkspace().getRoot().getProject(Designer_plugin_tests.PROJECT_NAME);
		IFolder folder = project.getFolder("tpdTest");
		final List<IFile> result = new ArrayList<IFile>();
		try {
			IResource[] children = folder.members();
			for (IResource child : children) {
				if (child instanceof IFile) {
					IFile file = (IFile) child;
					if ("tpd".equals(file.getFileExtension())) {
						result.add(file);
					}
				}
			}
		} catch (final CoreException e) {
			fail(e.toString());
		}
		return result;
	}

}
