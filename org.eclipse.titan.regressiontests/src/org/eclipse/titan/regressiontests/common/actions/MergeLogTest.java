/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.common.actions;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.titan.common.Activator;
import org.eclipse.titan.common.actions.MergeLog;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.WorkspaceHandlingLibrary;
import org.junit.Test;

public class MergeLogTest {

	@Test
	public final void test() throws Exception {
		Activator.getDefault().getPreferenceStore().setValue("org.eclipse.titan.common.automaticMergeOptions", "overwrite");

		MergeLog mergeLog = new MergeLog();
		mergeLog.setShowDialog(false);

		List<IFile> filesToMerge = new ArrayList<IFile>();
		filesToMerge.add(getFile("LogFile1.log"));
		filesToMerge.add(getFile("LogFile2.log"));
		mergeLog.run(filesToMerge, true);
		String mergedFileContent = IOUtils.inputStreamToString(new FileInputStream(new File(getFile("LogFile1_merged.log").getLocationURI())));
		String expectedContent = IOUtils.inputStreamToString(new FileInputStream(new File(getFile("LogFile1_merged_expected.log").getLocationURI())));

		assertEquals(expectedContent, mergedFileContent);
	}

	private IFile getFile(String fileName) {
		IProject project = WorkspaceHandlingLibrary.getWorkspace().getRoot().getProject(Designer_plugin_tests.PROJECT_NAME);
		return project.getFile("common/mergelog/" + fileName);
	}

}
