/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.common.actions;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.titan.common.actions.FormatLog;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.WorkspaceHandlingLibrary;
import org.junit.Ignore;
import org.junit.Test;

public class FormatLogTest {

	@Ignore
	@Test
	public void testFormat() throws Exception {
		List<IFile> filesToFormat = new ArrayList<IFile>();
		filesToFormat.add(getFile("LogFile1.log"));
		FormatLog formatLog = new FormatLog();
		formatLog.formatFiles(filesToFormat);
		String formattedFileContent = IOUtils.inputStreamToString(new FileInputStream(new File(getFile("LogFile1_formatted.log").getLocationURI())));
		String expectedContent = IOUtils.inputStreamToString(new FileInputStream(new File(getFile("LogFile1_formatted_expected.log").getLocationURI())));

		assertEquals(expectedContent, formattedFileContent);
	}


	private IFile getFile(String fileName) {
		IProject project = WorkspaceHandlingLibrary.getWorkspace().getRoot().getProject(Designer_plugin_tests.PROJECT_NAME);
		return project.getFile("common/formatlog/" + fileName);
	}
}
