/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class partitions a module graph according to the folders the represented
 * modules are located in.
 * 
 * @author Gobor Daniel
 */
public class LinkedFileCluster extends ModuleLocationCluster {

	public LinkedFileCluster(final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph, final IProject project) {
		super(graph, project);
	}

	@Override
	protected void checkProject(final IProgressMonitor progress) throws CoreException {
		progress.subTask("Cheking " + project.getName());
		final IResource[] contents = project.members();
		for (IResource content : contents) {
			check(content, progress);
		}
	}

	/**
	 * Checks the given file for containing modules.
	 * 
	 * @param file
	 *            The file to search
	 */
	protected void checkFile(final IFile file) {
		final String name = parser.containedModule(file);
		if (name == null) {
			return;
		}
		checkLinkedFile(name, ClusteringTools.truncate(file.getLocation().removeLastSegments(1).toOSString()));
	}

	@Override
	protected void checkFolder(final IFolder folder, final IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Checking " + folder.getName());
		final IResource[] contents = folder.members();
		for (IResource content : contents) {
			check(content, monitor);
		}
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		monitor.worked(1);
	}

	/**
	 * Checks the give IResource. If it is a file, the node that represents its
	 * module is added to the given set. If it is a folder, checks the
	 * IResources in that folder.
	 * 
	 * @param content
	 *            The IResource to be checked
	 * @param monitor
	 *            The progress monitor
	 * @throws CoreException
	 */
	protected void check(final IResource content, final IProgressMonitor monitor) throws CoreException {
		switch (content.getType()) {
		case IResource.FILE:
			checkFile((IFile) content);
			break;
		case IResource.FOLDER:
			checkFolder((IFolder) content, monitor);
			break;
		default:
			break;
		}
	}

	@Override
	protected String getType() {
		return "Clustering by absolute path";
	}

}