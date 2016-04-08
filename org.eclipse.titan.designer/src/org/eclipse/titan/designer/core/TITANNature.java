/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.FileBuildPropertyData;
import org.eclipse.titan.designer.properties.data.FolderBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectRemoteBuildPropertyData;

/**
 * This class takes care of the TITAN Nature.
 * 
 * @author Kristof Szabados
 */
public final class TITANNature implements IProjectNature {
	public static final String NATURE_ID = ProductConstants.PRODUCT_ID_DESIGNER + ".core.TITANNature";
	public static final String LOG_NATURE_ID = "org.eclipse.titan.log.viewer.TitanLogProject";
	public static final String NO_TITAN_NATURE_FOUND = "The selected project does not have the TITAN nature among its natures";
	public static final String NO_TITAN_FILE_NATURE_FOUND = "The project containing the selected file does not have the TITAN nature among its natures";

	private IProject project;

	/**
	 * This visitor removes the attributes from every resource it visits.
	 */
	private static class NatureRemovingVisitor implements IResourceVisitor {
		@Override
		public boolean visit(final IResource resource) {
			switch (resource.getType()) {
			case IResource.PROJECT:
				ProjectBuildPropertyData.removeTITANAttributes((IProject) resource);
				ProjectRemoteBuildPropertyData.removeTITANAttributes((IProject) resource);
				break;
			case IResource.FOLDER:
				FolderBuildPropertyData.removeTITANAttributes((IFolder) resource);
				break;
			case IResource.FILE:
				FileBuildPropertyData.removeTITANAttributes((IFile) resource);
				break;
			default:
				break;
			}

			return true;
		}
	}

	@Override
	public void configure() throws CoreException {
		addTITANBuilderToProject(this.project);
	}

	@Override
	public void deconfigure() throws CoreException {
		removeTITANBuilderFromProject(this.project);
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(final IProject project) {
		this.project = project;
	}

	/**
	 * Adds the TITAN natures to the provided project.
	 * 
	 * @param description the project description to receive the natures.
	 * */
	public static void addTITANNatureToProject(final IProjectDescription description) {
		List<String> newIds = new ArrayList<String>();
		newIds.addAll(Arrays.asList(description.getNatureIds()));
		int index = newIds.indexOf(TITANNature.NATURE_ID);
		if (index == -1) {
			newIds.add(TITANNature.NATURE_ID);
		}
		index = newIds.indexOf(TITANNature.LOG_NATURE_ID);
		if (index == -1) {
			newIds.add(TITANNature.LOG_NATURE_ID);
		}
		
		description.setNatureIds(newIds.toArray(new String[newIds.size()]));
	}
	
	/**
	 * Remove the TITAN natures from the project.
	 * 
	 * @param description the project description to remove the natures from.
	 * */
	public static void removeTITANNature(final IProjectDescription description) {
		final List<String> natureIds = new ArrayList<String>();
		natureIds.addAll(Arrays.asList(description.getNatureIds()));
		
		int index = natureIds.indexOf(TITANNature.NATURE_ID);
		if (index != -1) {
			natureIds.remove(index);
		}
		
		index = natureIds.indexOf(TITANNature.LOG_NATURE_ID);
		if (index != -1) {
			natureIds.remove(index);
		}

		description.setNatureIds(natureIds.toArray(new String[natureIds.size()]));
	}

	/**
	 * Decides whether the project has the TITAN nature or not.
	 * 
	 * @param project
	 *                the project in question
	 * @return whether the project has the TITAN nature or not.
	 */
	public static boolean hasTITANNature(final IProject project) {
		try {
			return project != null && project.isAccessible() && project.hasNature(NATURE_ID);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		return false;
	}

	/**
	 * Returns the index of a command in the build specification.
	 * 
	 * @see #addTITANBuilderToProject(IProject)
	 * @see #removeTITANBuilderFromProject(IProject)
	 * 
	 * @param buildSpec
	 *                the build specification in question.
	 * @return the index of the command in the array.
	 */
	private static int getJavaCommandIndex(final ICommand[] buildSpec) {
		for (int i = 0; i < buildSpec.length; ++i) {
			if (TITANBuilder.BUILDER_ID.equals(buildSpec[i].getBuilderName())) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Adds the TITAN Builder to a project.
	 * 
	 * @param project
	 *                the project in question.
	 * @throws CoreException
	 *                 if something goes wrong.
	 */
	public static void addTITANBuilderToProject(final IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		int javaCommandIndex = getJavaCommandIndex(description.getBuildSpec());

		if (javaCommandIndex == -1) {
			ICommand newCommand = description.newCommand();
			newCommand.setBuilderName(TITANBuilder.BUILDER_ID);

			ICommand[] oldBuildSpec = description.getBuildSpec();
			ICommand[] newCommands = new ICommand[oldBuildSpec.length + 1];
			System.arraycopy(oldBuildSpec, 0, newCommands, 1, oldBuildSpec.length);
			newCommands[0] = newCommand;
			description.setBuildSpec(newCommands);
			project.setDescription(description, null);
		}
	}

	/**
	 * Removes the TITAN Builder from a project and also removes every TITAN
	 * related attribute from every resource in the project.
	 * 
	 * @param project
	 *                the project in question.
	 * @throws CoreException
	 *                 if something goes wrong
	 */
	public static void removeTITANBuilderFromProject(final IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		int javaCommandIndex = getJavaCommandIndex(description.getBuildSpec());
		if (javaCommandIndex == -1) {
			return;
		}

		ICommand[] oldBuildSpec = description.getBuildSpec();
		ICommand[] newCommands = new ICommand[oldBuildSpec.length - 1];
		System.arraycopy(oldBuildSpec, 0, newCommands, 0, javaCommandIndex);
		System.arraycopy(oldBuildSpec, javaCommandIndex + 1, newCommands, javaCommandIndex, oldBuildSpec.length - javaCommandIndex - 1);
		description.setBuildSpec(newCommands);
		project.setDescription(description, null);

		try {
			project.accept(new NatureRemovingVisitor());
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}
}
