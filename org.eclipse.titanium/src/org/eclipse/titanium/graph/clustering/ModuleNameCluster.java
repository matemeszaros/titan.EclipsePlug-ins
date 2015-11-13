/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.preferences.PreferenceConstants;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * The base class for the other module name clusters. If this one is used to create the cluster graph, the edges will represent the import hierarchy.
 * 
 * @author Gobor Daniel
 *
 */
public class ModuleNameCluster extends BaseCluster {

	protected static final String ALL = "/";

	protected Set<String> knownNames;

	private boolean checkUnderscore;
	private boolean checkAlternatingCase;
	private int depth;

	/**
	 * Initialize the variables for the clustering.
	 */
	public ModuleNameCluster(DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph) {
		moduleGraph = graph;
		successful = true;

		clusters = new HashSet<Set<NodeDescriptor>>();
		knownNames = new HashSet<String>();
		mapNameCluster = new HashMap<String, Set<NodeDescriptor>>();

		loadSettings();
	}

	/**
	 * Load clustering settings from preference store.
	 */
	private void loadSettings() {
		checkUnderscore = Platform.getPreferencesService().getBoolean(Activator.PLUGIN_ID, PreferenceConstants.CLUSTER_SPACE, true, null);
		checkAlternatingCase = Platform.getPreferencesService().getBoolean(Activator.PLUGIN_ID, PreferenceConstants.CLUSTER_SMALL_LARGE, false, null);
		depth = Platform.getPreferencesService().getInt(Activator.PLUGIN_ID, PreferenceConstants.CLUSTER_DEPTH, 3, null);
	}

	@Override
	public boolean createClusters(IProgressMonitor monitor) {
		IProgressMonitor progress = (monitor == null) ? new NullProgressMonitor() : monitor;
		progress.beginTask("Creating clusters", 1 + moduleGraph.getVertexCount());
		if ((!checkUnderscore && !checkAlternatingCase) || depth == 0) {
			setErronous("Module names will not be split with the current settings.\n"
					+ "Please visit the 'Clusters' Preference page to correct it.\n");
			return false;
		}
		createNames(progress);
		fillClusters();
		progress.done();
		if (clusters.size() > 1) {
			return true;
		} else {
			setErronous("Less than two clusters were found.\n" + "The settings might not be correct.");
			return false;
		}
	}

	/**
	 * Slice up the module names to guess the "package" name.
	 */
	private void createNames(IProgressMonitor monitor) {
		addCluster(ALL);
		for (NodeDescriptor v : moduleGraph.getVertices()) {
			final String name = v.getDisplayName();
			monitor.subTask("Checking " + name);
			boolean small = false;
			int i = 0;
			int splits = 0;
			final int length = name.length();
			char prev = '0';
			while (i < length && splits < depth) {
				char c = name.charAt(i);
				if (checkDash(prev, c)) {
					addSubName(name, i);
					++splits;
				}
				if (checkAlternatingCase) {
					if (small && Character.isUpperCase(c)) {
						addSubName(name, i);
						++splits;
					}
					small = Character.isLowerCase(c);
				}
				prev = c;
				++i;

			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
		}
	}

	private boolean checkDash(char prev, char cur) {
		return checkUnderscore && prev != '_' && (cur == '-' || cur == '_');
	}

	/**
	 * Create a cluster belonging to a slice.
	 * 
	 * @param name
	 *            The module name
	 * @param i
	 *            Position where the name is split
	 */
	private void addSubName(String name, int i) {
		final String word = name.substring(0, i);
		addCluster(word);
	}

	/**
	 * Create a cluster for the segment.
	 * 
	 * @param word
	 *            The segment the cluster belongs to
	 */
	private void addCluster(String word) {
		if (!knownNames.contains(word)) {
			knownNames.add(word);
			final Set<NodeDescriptor> cluster = new HashSet<NodeDescriptor>();
			mapNameCluster.put(word, cluster);
		}
	}

	/**
	 * Fill the clusters with the nodes.
	 */
	private void fillClusters() {
		for (NodeDescriptor v : moduleGraph.getVertices()) {
			final String name = v.getDisplayName();
			int length = 0;
			String match = null;
			for (String word : knownNames) {
				if (word.length() > length && name.startsWith(word)) {
					match = word;
					length = word.length();
				}
			}
			if (match == null) {
				final Set<NodeDescriptor> cluster = mapNameCluster.get(ALL);
				cluster.add(v);
				v.setCluster(cluster);
			} else {
				final Set<NodeDescriptor> cluster = mapNameCluster.get(match);
				cluster.add(v);
				v.setCluster(cluster);
			}
		}

		for (String word : knownNames) {
			final Set<NodeDescriptor> cluster = mapNameCluster.get(word);
			if (!cluster.isEmpty()) {
				clusters.add(cluster);
			}
		}
	}

	@Override
	protected void createGraph() {
		Set<String> empty = new HashSet<String>();
		for (String name : knownNames) {
			if (mapNameCluster.get(name).isEmpty()) {
				empty.add(name);
			}
		}
		for (String name : empty) {
			mapNameCluster.remove(name);
		}
		super.createGraph();
	}

	@Override
	protected void reportError() {
		errorHandler.reportBadSetting(ERRORTITLE, msg, ERRORBUTTON, "org.eclipse.titanium.preferences.pages.GraphClusterModuleNamePage");
	}

	@Override
	protected String getType() {
		return "Clustering by module names";
	}

}
