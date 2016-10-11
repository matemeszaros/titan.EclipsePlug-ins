/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering;

import java.util.HashSet;
import java.util.Set;

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
public class FolderNameCluster extends PathCluster {

	public FolderNameCluster(final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph, final IProject project) {
		super(graph, project);
	}

	@Override
	protected void checkProject(final IProgressMonitor progress) throws CoreException {
		progress.subTask("Checking " + project.getName());
		final IResource[] contents = project.members();
		final Set<NodeDescriptor> cluster = new HashSet<NodeDescriptor>();
		for (final IResource content : contents) {
			check(content, cluster, progress);
		}
		if (!cluster.isEmpty()) {
			clusters.add(cluster);
			mapNameCluster.put("/", cluster);
		}
	}

	/**
	 * Checks the given file for containing modules.
	 * 
	 * @param file
	 *            The file to search
	 * @param cluster
	 *            The cluster to add the file to
	 */
	protected void checkFile(final IFile file, final Set<NodeDescriptor> cluster) {
		final String name = parser.containedModuleName(file);
		if (name == null) {
			return;
		}
		addNodeToCluster(name, cluster);
	}

	/**
	 * Checks the folder for modules.
	 * 
	 * @param folder
	 *            The folder to check
	 * @param monitor
	 *            A progress monitor
	 * @throws CoreException
	 */
	protected void checkFolder(final IFolder folder, final IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Checking " + folder.getName());
		final IResource[] contents = folder.members();
		final Set<NodeDescriptor> cluster = new HashSet<NodeDescriptor>();
		for (final IResource nextContent : contents) {
			check(nextContent, cluster, monitor);
		}
		addNewCluster(cluster, folder);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		monitor.worked(1);
	}

	/**
	 * Connects the cluster with the given folder.
	 * 
	 * @param cluster
	 *            The cluster
	 * @param folder
	 *            The folder
	 */
	protected void addNewCluster(final Set<NodeDescriptor> cluster, final IFolder folder) {
		if (!cluster.isEmpty()) {
			clusters.add(cluster);
			mapNameCluster.put(folder.getProjectRelativePath().toOSString(), cluster);
		}
	}

	/**
	 * Checks the given IResource. If it is a file, the node that represents its
	 * module is added to the given set. If it is a folder, checks the
	 * IResources in that folder.
	 * 
	 * @param content
	 *            The IResource to be checked
	 * @param cluster
	 *            The set to append the node if the represented module is in the
	 *            checked IResource
	 * @param monitor
	 *            The progress monitor
	 * @throws CoreException
	 */
	protected void check(final IResource content, final Set<NodeDescriptor> cluster, final IProgressMonitor monitor) throws CoreException {
		switch (content.getType()) {
		case IResource.FILE:
			checkFile((IFile) content, cluster);
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
		return "Clustering by folder name";
	}

}
