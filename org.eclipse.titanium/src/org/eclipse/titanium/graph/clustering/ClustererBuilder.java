/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering;

import org.eclipse.core.resources.IProject;
import org.eclipse.titanium.error.ConsoleErrorHandler;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class implements a factory for clusterer algorithms
 * 
 * @author Gabor Jenei
 */
public class ClustererBuilder {
	private String clusterName="";
	private DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> originalGraph;
	private IProject project;

	public ClustererBuilder() {
		// Do nothing
	}
	
	/**
	 * Sets the clusterer algorithm's name
	 * @param algorithm : The algorithm name
	 * @return This object
	 */
	public ClustererBuilder setAlgorithm(final String algorithm){
		clusterName=algorithm;
		return this;
	}
	
	/**
	 * Sets the graph to be clustered
	 * @param graph : The graph to be clustered
	 * @return This object
	 */
	public ClustererBuilder setGraph(final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph){
		originalGraph=graph;
		return this;
	}
	
	/**
	 * Sets the project of the graph nodes
	 * @param project : The project to set
	 * @return This object
	 */
	public ClustererBuilder setProject(final IProject project){
		this.project=project;
		return this;
	}
	
	/**
	 * This method builds up the convenient clusterer object
	 * @return The built object
	 * @throws IllegalArgumentException If some needed parameters are not already set
	 */
	public BaseCluster build() throws IllegalArgumentException{
		if(originalGraph==null){
			throw new IllegalArgumentException("The graph parameter wasn't set for the builder");
		}
		
		final ConsoleErrorHandler errorHandler = new ConsoleErrorHandler();
		BaseCluster clusterer=null;
		
		if ("modulelocation".equalsIgnoreCase(clusterName)) {
			clusterer = new ModuleLocationCluster(originalGraph, project);
		} else if ("foldername".equalsIgnoreCase(clusterName)) {
			clusterer = new FolderNameCluster(originalGraph, project);
		} else if ("linkedlocation".equalsIgnoreCase(clusterName)) {
			clusterer = new LinkedFileCluster(originalGraph, project);
		} else if ("regularexpression".equalsIgnoreCase(clusterName)) {
			clusterer = new RegexpCluster(originalGraph);
		} else if ("modulename".equalsIgnoreCase(clusterName)) {
			clusterer = new ModuleNameCluster(originalGraph);
		} else if ("fullmodulenametree".equalsIgnoreCase(clusterName)) {
			clusterer = new FullModuleNameCluster(originalGraph);
		} else if ("sparsemodulenametree".equalsIgnoreCase(clusterName)) {
			clusterer = new SparseModuleNameCluster(originalGraph);
		} else if ("automatic".equalsIgnoreCase(clusterName)) {
			clusterer = new AutomaticCluster(originalGraph, project);
		} else {
			errorHandler.reportInformation("Usage: <output path> [-c<clustering algorithm name>]");
			errorHandler.reportInformation("The possible clustering algorithms are: ");
			errorHandler.reportInformation("\tModuleLocation\n\tFolderName\n\tLinkedLocation\n\tRegularExpression\n\tModuleName\n"
					+ "\tFullModuleNameTree\n\tSparseModuleNameTree");
			throw new IllegalArgumentException("The algorithm doesn't exist!");
		}
		
		return clusterer;
	}
	
	
}
