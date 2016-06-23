/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class partitions a module graph according to the folders the represented
 * modules are located in.
 * 
 * @author Gobor Daniel
 */
public class ModuleLocationCluster extends FolderNameCluster {

	public ModuleLocationCluster(final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph, final IProject project) {
		super(graph, project);
	}

	@Override
	protected void checkFile(final IFile file, final Set<NodeDescriptor> cluster) {
		final String name = parser.containedModuleName(file);
		if (name == null) {
			return;
		}
		if (file.isLinked()) {
			checkLinkedFile(name, "Linked from " + ClusteringTools.truncate(file.getLocation().removeLastSegments(1).toOSString()));
		} else {
			addNodeToCluster(name, cluster);
		}
	}

	/**
	 * Checks a linked file.
	 * 
	 * @param filename
	 *            The name of the file
	 * @param clustername
	 *            The name of the cluster this file should belong to
	 */
	protected void checkLinkedFile(final String filename, final String clustername) {
		if (mapNameCluster.containsKey(clustername)) {
			final Set<NodeDescriptor> cluster = mapNameCluster.get(clustername);
			addNodeToCluster(filename, cluster);
		} else {
			final Set<NodeDescriptor> cluster = new HashSet<NodeDescriptor>();
			addNodeToCluster(filename, cluster);
			clusters.add(cluster);
			mapNameCluster.put(clustername, cluster);
		}
	}

	@Override
	protected void addNewCluster(final Set<NodeDescriptor> cluster, final IFolder folder) {
		if (!cluster.isEmpty()) {
			clusters.add(cluster);
			if (folder.isLinked()) {
				mapNameCluster.put("Linked from " + ClusteringTools.truncate(folder.getLocation().toOSString()), cluster);
			} else {
				mapNameCluster.put(folder.getProjectRelativePath().toOSString(), cluster);
			}
		}
	}

	@Override
	protected String getType() {
		return "Clustering by module location";
	}

}