/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.utils;

import static org.eclipse.titan.common.utils.SelectionUtils.filterSelection;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public final class SelectionUtils {

	private SelectionUtils() {
		// Hide constructor
	}

	/**
	 * Checks if the selected file has the right extension
	 * @param selection
	 * @return
	 */
	public static boolean isSelectionALogFile(final ISelection selection) {
		IFile logFile = selectionToIFile(selection);
		return logFile != null && hasLogFileExtension(logFile);
	}
	
	/**
	 * Returns an IFile from an selection
	 * @param selection the selection
	 * @return an IFile or null if the selection is not an IFile
	 */
	public static IFile selectionToIFile(final ISelection selection) {
		List<IFile> iFiles = filterSelection(selection, IFile.class);
		if (iFiles.size() != 1) {
			return null;
		}
		return iFiles.get(0);
	}

	/**
	 * @param selection the selection (REQUIRES that isSelectionOnlyProjects(selection) == true)
	 * @return true is only one project is selected and the project is a Log Viewer project, otherwise false
	 */
	public static boolean isSelectionALogViewerProject(final ISelection selection) {
		List<IProject> iProjects = filterSelection(selection, IProject.class);
		if (iProjects.size() != 1) {
			return false;
		}

		IProject project = iProjects.get(0);
		try {
			if (project.getNature(Constants.NATURE_ID) == null) {
				return false;
			}
		} catch (CoreException e) {
			// Could not get Nature ID -> return false
			return false;
		}
		return true;
	}

	/**
	 * @param selection the selection (REQUIRES that isSelectionOnlyProjects(selection) == true)
	 * @return true is only one project is selected and the project is a Log Viewer project, otherwise false
	 */
	public static boolean isSelectionAnUnlinkedFolder(final ISelection selection) {
		List<IFolder> iFolders = filterSelection(selection, IFolder.class);

		if (iFolders.size() != 1) {
			return false;
		}
		IFolder folder = iFolders.get(0);
		if (folder.isLinked()) {
			return false;
		}
		//loop through structure to see if the selected folder is inside a linked folder
		IContainer parentResource = folder.getParent();
		while (parentResource.getType() != IResource.PROJECT) {
			if (parentResource instanceof IFolder) {
				IFolder parentFolder = (IFolder) parentResource;
				if (parentFolder.isLinked()) {
					return false;
				}
			}
			//fetch parent
			parentResource = parentResource.getParent();
		}
		// return true if it is one selected folder and where the folder is not linked
		return true;
	}

	/**
	 * @param selection the selection (REQUIRES that isSelectionOnlyProjects(selection) == true)
	 * @return true is there are one or more projects that are closed in the selection, otherwise false
	 */
	public static boolean hasSelectionClosedProjects(final ISelection selection) {
		IStructuredSelection ss = (IStructuredSelection) selection;
		for (Iterator<?> iterator = ss.iterator(); iterator.hasNext();) {
			IProject project = (IProject) iterator.next();
			if (!project.isOpen()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param selection the selection (REQUIRES that isSelectionOnlyProjects(selection) == true)
	 * @return true is there are one or more projects that are opened in the selection, otherwise false
	 */
	public static boolean hasSelectionOpenedProjects(final ISelection selection) {
		IStructuredSelection ss = (IStructuredSelection) selection;
		for (Iterator<?> iterator = ss.iterator(); iterator.hasNext();) {
			IProject project = (IProject) iterator.next();
			if (project.isOpen()) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasLogFileExtension(IFile logFile) {
		String fileExtension = logFile.getFileExtension();
		return Constants.LOG_EXTENSION.equals(fileExtension);
	}
}
