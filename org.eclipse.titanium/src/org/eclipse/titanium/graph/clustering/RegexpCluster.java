/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.preferences.PreferenceConstants;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class partitions a module graph by matching module names to given
 * patterns.
 * 
 * @author Gobor Daniel
 */
public class RegexpCluster extends BaseCluster {

	private List<Matcher> matchers;
	private Map<String, Set<NodeDescriptor>> mapPatternCluster;
	private Map<NodeDescriptor, List<String>> mapNodeMatching;

	/**
	 * Initialize the variables for the clustering.
	 * 
	 * @param graph
	 *            The graph to be partitioned
	 */
	public RegexpCluster(final DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph) {
		moduleGraph = graph;
		successful = true;

		clusters = new HashSet<Set<NodeDescriptor>>();
		mapNameCluster = new HashMap<String, Set<NodeDescriptor>>();
	}

	/**
	 * Creates the matcher objects.
	 * 
	 * @return True if the regular expressions are correct.
	 */
	private boolean createMatchers() {
		matchers = new ArrayList<Matcher>();
		mapPatternCluster = new HashMap<String, Set<NodeDescriptor>>();

		final String stringList = Platform.getPreferencesService().getString(
				Activator.PLUGIN_ID, PreferenceConstants.CLUSTER_REGEXP, "",
				null);
		final List<String> splittedList = ResourceExclusionHelper.intelligentSplit(
				stringList, '#', '\\');

		if (splittedList.isEmpty()) {
			setErronous("No regular expressions were defined.\n"
					+ "Please visit the 'Clusters' Preference page to define them.");
			return false;
		}

		for (final String item : splittedList) {
			try {
				final Pattern pattern = Pattern.compile(item);
				final Set<NodeDescriptor> cluster = new HashSet<NodeDescriptor>();
				mapPatternCluster.put(pattern.toString(), cluster);
				final Matcher matcher = pattern.matcher("");
				matchers.add(matcher);
			} catch (PatternSyntaxException e) {
				final String errorString = "At least one of the regular expressions used is not correct.\n"
						+ "Please visit the 'Clusters' Preference page to correct it.\n"
						+ "Reason: "
						+ e.getLocalizedMessage()
						+ "\n"
						+ "Incorrect pattern: " + item;
				ErrorReporter.logExceptionStackTrace(errorString, e);
				setErronous(errorString);
				return false;
			}
		}

		return true;
	}

	/**
	 * Collects the nodes belonging to the matchers.
	 * 
	 * @param progress
	 *            A progress monitor
	 */
	private void collectMatchingNodes(final IProgressMonitor progress) {
		mapNodeMatching = new HashMap<NodeDescriptor, List<String>>();
		for (final NodeDescriptor v : moduleGraph.getVertices()) {
			final String name = v.getDisplayName();
			progress.subTask("Checking " + name);

			final List<String> matching = new LinkedList<String>();
			for (final Matcher matcher : matchers) {
				matcher.reset(name);
				if (matcher.matches()) {
					matching.add(matcher.pattern().toString());
				}
			}
			mapNodeMatching.put(v, matching);
		}
	}

	/**
	 * Clears the previous clustering to prevent interference.
	 */
	private void clearPrevClusters() {
		for (final NodeDescriptor v : moduleGraph.getVertices()) {
			v.setCluster(null);
		}
	}

	/**
	 * Creates the clusters.
	 * 
	 * @return True if a node has more than one matching pattern.
	 */
	private boolean fillClusters() {
		final Set<NodeDescriptor> missing = new HashSet<NodeDescriptor>();
		mapPatternCluster.put("missing", missing);

		boolean moreThanOneMatch = false;
		for (final NodeDescriptor v : moduleGraph.getVertices()) {
			final List<String> matching = mapNodeMatching.get(v);
			if (matching.isEmpty()) {
				missing.add(v);
				v.setCluster(missing);
			} else if (matching.size() == 1) {
				final Set<NodeDescriptor> cluster = mapPatternCluster.get(matching
						.get(0));
				cluster.add(v);
				v.setCluster(cluster);
			} else {
				moreThanOneMatch = true;
				final StringBuilder message = new StringBuilder(msg);
				message.append(v.getDisplayName()).append(":\t");
				for (final String pattern : matching) {
					message.append("   ").append(pattern);
				}
				message.append('\n');
				msg = message.toString();
			}
		}
		return moreThanOneMatch;
	}

	/**
	 * Adds the non empty clusters to the clustering.
	 */
	private void addClusters() {
		for (final Entry<String, Set<NodeDescriptor>> entry : mapPatternCluster
				.entrySet()) {
			final String name = entry.getKey();
			final Set<NodeDescriptor> cluster = entry.getValue();
			if (!cluster.isEmpty()) {
				mapNameCluster.put(name, cluster);
				clusters.add(cluster);
			}
		}
	}

	@Override
	public boolean createClusters(final IProgressMonitor monitor) {
		final IProgressMonitor progress = (monitor == null) ? new NullProgressMonitor()
				: monitor;
		progress.beginTask("Creating clusters",
				3 + moduleGraph.getVertexCount());

		progress.subTask("Cheking patterns");
		if (createMatchers()) {
			progress.worked(1);
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		} else {
			progress.done();
			return false;
		}

		clearPrevClusters();
		collectMatchingNodes(progress);
		progress.worked(1);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		progress.subTask("Creating clusters from gathered information");
		final boolean moreThanOneMatch = fillClusters();
		if (moreThanOneMatch) {
			msg = "At least one module name matches more than one regular expressions.\n"
					+ "Please visit the 'Clusters' Preference page to correct it.\n"
					+ "The following modules have too many matches:\n" + msg;
			setErronous(msg);
			return false;
		}

		addClusters();
		progress.worked(1);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}
		progress.done();

		if (clusters.size() < 2) {
			setErronous("Less than two clusters were found.\n"
					+ "The settings might not be correct or no matches were found.");
			return false;
		}

		return true;
	}

	@Override
	protected void reportError() {
		errorHandler.reportBadSetting(ERRORTITLE, msg, ERRORBUTTON, "org.eclipse.titanium.preferences.pages.GraphClusterRegexpPage");
	}

	@Override
	protected String getType() {
		return "Clustering using regural expressions";
	}

}
