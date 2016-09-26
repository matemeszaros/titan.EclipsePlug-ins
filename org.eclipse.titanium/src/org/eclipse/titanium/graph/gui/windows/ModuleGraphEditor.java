/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.windows;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titanium.graph.clustering.BaseCluster;
import org.eclipse.titanium.graph.clustering.ClustererBuilder;
import org.eclipse.titanium.graph.clustering.visualization.ClusterNode;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeColours;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.generators.ModuleGraphGenerator;
import org.eclipse.titanium.graph.gui.common.Layouts;
import org.eclipse.titanium.graph.gui.utils.LayoutEntry;
import org.eclipse.titanium.graph.gui.utils.MetricsEntry;
import org.eclipse.titanium.graph.gui.utils.MetricsLayoutEntry;
import org.eclipse.titanium.graph.visualization.MeasureableGraphHandler;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.TestcaseMetric;
import org.eclipse.titanium.metrics.preferences.PreferenceManager;
import org.eclipse.titanium.metrics.utils.WrapperStore;
import org.eclipse.ui.IFileEditorInput;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class implements the specialties of ModuleGraph editor window. It is a
 * child class of {@link GraphEditor}
 * 
 * @author Gabor Jenei
 * @see GraphEditor
 */
public class ModuleGraphEditor extends GraphEditor {
	protected MetricsEntry chosenMetric;
	protected DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> originalGraph;
	protected final ButtonGroup metricsGroup = new ButtonGroup();
	protected final JMenu metricsMenu = new JMenu("Metrics");
	protected final JMenu clusteringMenu = new JMenu("Clustering");
	protected final JMenu knotClusteringMenu = new JMenu("Groupping clusterer");
	protected final JMenu graphClusteringMenu = new JMenu("Graph generating clusterer");
	protected final JMenuItem nameKnotCluster = new JMenuItem("By module names");
	protected final JMenu nameClusteringMenu = new JMenu("By module names");
	protected final JMenuItem nameGraphCluster = new JMenuItem("Show edges representing import hierarchy");
	protected final JMenuItem fullNameGraphCluster = new JMenuItem("Show full tree module name hierarhy");
	protected final JMenuItem sparseNameGraphCluster = new JMenuItem("Show non-empty nodes only in the tree");
	protected final JMenuItem regKnotCluster = new JMenuItem("Using regular expressions");
	protected final JMenuItem regGraphCluster = new JMenuItem("Using regular expressions");
	protected final JMenuItem autoKnotCluster = new JMenuItem("Automatically");
	protected final JMenuItem folderKnotCluster = new JMenuItem("By folder name");
	protected final JMenuItem folderGraphCluster = new JMenuItem("By folder name");
	protected final JMenuItem linkedKnotCluster = new JMenuItem("By linked file location");
	protected final JMenuItem linkedGraphCluster = new JMenuItem("By linked file location");
	protected final JMenuItem locationKnotCluster = new JMenuItem("By module location");
	protected final JMenuItem locationGraphCluster = new JMenuItem("By module location");
	protected final JMenuItem closeGraphClusters = new JMenuItem("Leave cluster graph");
	protected final JRadioButtonMenuItem isom = Layouts.LAYOUT_ISOM.newInstance();
	protected final JRadioButtonMenuItem kk = Layouts.LAYOUT_KK.newInstance();
	protected final JRadioButtonMenuItem fr = Layouts.LAYOUT_FR.newInstance();
	protected final JRadioButtonMenuItem spring = Layouts.LAYOUT_SPRING.newInstance();
	protected final JRadioButtonMenuItem circle = Layouts.LAYOUT_CIRCLE.newInstance();
	protected final JRadioButtonMenuItem tdag = Layouts.LAYOUT_TDAG.newInstance();
	protected final JRadioButtonMenuItem rtdag = Layouts.LAYOUT_RTDAG.newInstance();
	protected final JMenu metricLayoutMenu = new JMenu("Metric layouts");
	public static final String ID = "org.eclipse.titanium.graph.editors.ModuleGraphEditor";

	/**
	 * Constructor
	 */
	public ModuleGraphEditor() {
		super();
		originalGraph = null;
	}

	@Override
	protected void initWindow() {
		super.initWindow();

		isom.addActionListener(layoutListener);
		layoutGroup.add(isom);
		layoutMenu.add(isom);

		kk.addActionListener(layoutListener);
		layoutGroup.add(kk);
		layoutMenu.add(kk);

		fr.addActionListener(layoutListener);
		layoutGroup.add(fr);
		layoutMenu.add(fr);

		spring.addActionListener(layoutListener);
		layoutMenu.add(spring);
		layoutGroup.add(spring);

		circle.addActionListener(layoutListener);
		layoutMenu.add(circle);
		layoutGroup.add(circle);

		final JMenu dagMenu = new JMenu("Directed layouts");
		layoutMenu.add(dagMenu);

		tdag.setSelected(true);
		tdag.addActionListener(layoutListener);
		dagMenu.add(tdag);
		layoutGroup.add(tdag);

		rtdag.addActionListener(layoutListener);
		dagMenu.add(rtdag);
		layoutGroup.add(rtdag);
		
		layoutMenu.add(metricLayoutMenu);

		final ActionListener changeMetrics = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				MetricsEntry metric = (MetricsEntry) e.getSource();
				chosenMetric = metric;
				for (NodeDescriptor node : graph.getVertices()) {
					((MeasureableGraphHandler) handler).calculateColour(node, chosenMetric.getMetric());
				}
				drawArea.repaint();
				if (satView != null) {
					satView.repaint();
				}
			}
		};

		boolean isFirst = true;

		// Creating the metric-selection menu:

		JMenu actMenu = new JMenu(ModuleMetric.GROUP_NAME);
		JMenu actLayoutMenu = new JMenu(ModuleMetric.GROUP_NAME);
		metricsMenu.add(actMenu);
		metricLayoutMenu.add(actLayoutMenu);
		for (final ModuleMetric metric : ModuleMetric.values()) {
			if (!PreferenceManager.isEnabledOnModuleGraph(metric)) {
				continue;
			}
			final MetricsEntry item = new MetricsEntry(metric.getName(), metric);
			if (isFirst) {
				item.setSelected(true);
				chosenMetric = item;
				isFirst = false;
			}
			item.addActionListener(changeMetrics);
			metricsGroup.add(item);
			actMenu.add(item);
			final MetricsLayoutEntry layoutItem = new MetricsLayoutEntry(metric);
			layoutItem.addActionListener(layoutListener);
			layoutGroup.add(layoutItem);
			actLayoutMenu.add(layoutItem);
		}

		actMenu = new JMenu(AltstepMetric.GROUP_NAME);
		actLayoutMenu = new JMenu(AltstepMetric.GROUP_NAME);
		metricsMenu.add(actMenu);
		metricLayoutMenu.add(actLayoutMenu);
		for (final AltstepMetric metric : AltstepMetric.values()) {
			if (!PreferenceManager.isEnabledOnModuleGraph(metric)) {
				continue;
			}
			final MetricsEntry item = new MetricsEntry(metric.getName(), metric);
			item.addActionListener(changeMetrics);
			metricsGroup.add(item);
			actMenu.add(item);
			final MetricsLayoutEntry layoutItem = new MetricsLayoutEntry(metric);
			layoutItem.addActionListener(layoutListener);
			layoutGroup.add(layoutItem);
			actLayoutMenu.add(layoutItem);
		}

		actMenu = new JMenu(FunctionMetric.GROUP_NAME);
		actLayoutMenu = new JMenu(FunctionMetric.GROUP_NAME);
		metricLayoutMenu.add(actLayoutMenu);
		metricsMenu.add(actMenu);
		for (final FunctionMetric metric : FunctionMetric.values()) {
			if (!PreferenceManager.isEnabledOnModuleGraph(metric)) {
				continue;
			}
			final MetricsEntry item = new MetricsEntry(metric.getName(), metric);
			item.addActionListener(changeMetrics);
			metricsGroup.add(item);
			actMenu.add(item);
			final MetricsLayoutEntry layoutItem = new MetricsLayoutEntry(metric);
			layoutItem.addActionListener(layoutListener);
			layoutGroup.add(layoutItem);
			actLayoutMenu.add(layoutItem);
		}

		actMenu = new JMenu(TestcaseMetric.GROUP_NAME);
		actLayoutMenu = new JMenu(TestcaseMetric.GROUP_NAME);
		metricLayoutMenu.add(actLayoutMenu);
		metricsMenu.add(actMenu);
		for (final TestcaseMetric metric : TestcaseMetric.values()) {
			if (!PreferenceManager.isEnabledOnModuleGraph(metric)) {
				continue;
			}
			final MetricsEntry item = new MetricsEntry(metric.getName(), metric);
			item.addActionListener(changeMetrics);
			metricsGroup.add(item);
			actMenu.add(item);
			final MetricsLayoutEntry layoutItem = new MetricsLayoutEntry(metric);
			layoutItem.addActionListener(layoutListener);
			layoutGroup.add(layoutItem);
			actLayoutMenu.add(layoutItem);
		}
		
		final JMenuItem recalcItem = new JMenuItem("Recalculate metrics");
		recalcItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				WrapperStore.clearStore();
				GlobalParser.reAnalyzeSemantically();
				recolour(graph.getVertices());
				if (chosenLayout instanceof MetricsLayoutEntry) {
					chosenLayout.doClick();
				}
			}
		});
		metricsMenu.add(recalcItem);

		folderKnotCluster.addActionListener(new ClusteringAction("foldername", true));
		knotClusteringMenu.add(folderKnotCluster);

		folderGraphCluster.addActionListener(new ClusteringAction("foldername", false));
		graphClusteringMenu.add(folderGraphCluster);

		linkedKnotCluster.addActionListener(new ClusteringAction("linkedlocation", true));
		knotClusteringMenu.add(linkedKnotCluster);

		linkedGraphCluster.addActionListener(new ClusteringAction("linkedlocation", false));
		graphClusteringMenu.add(linkedGraphCluster);

		locationKnotCluster.addActionListener(new ClusteringAction("modulelocation", true));
		knotClusteringMenu.add(locationKnotCluster);

		locationGraphCluster.addActionListener(new ClusteringAction("modulelocation", false));
		graphClusteringMenu.add(locationGraphCluster);

		regKnotCluster.addActionListener(new ClusteringAction("regularexpression", true));
		knotClusteringMenu.add(regKnotCluster);

		regGraphCluster.addActionListener(new ClusteringAction("regularexpression", false));
		graphClusteringMenu.add(regGraphCluster);

		nameKnotCluster.addActionListener(new ClusteringAction("modulename", true));
		knotClusteringMenu.add(nameKnotCluster);

		nameGraphCluster.addActionListener(new ClusteringAction("modulename", false));
		nameClusteringMenu.add(nameGraphCluster);
		
		fullNameGraphCluster.addActionListener(new ClusteringAction("fullmodulenametree", false, (LayoutEntry) tdag));
		nameClusteringMenu.add(fullNameGraphCluster);
		
		sparseNameGraphCluster.addActionListener(new ClusteringAction("sparsemodulenametree", false, (LayoutEntry) tdag));
		nameClusteringMenu.add(sparseNameGraphCluster);

		autoKnotCluster.addActionListener(new ClusteringAction("automatic", true));
		knotClusteringMenu.add(autoKnotCluster);

		closeGraphClusters.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				chosenLayout.setSelected(false);
				isom.setSelected(true);
				chosenLayout = (LayoutEntry) isom;
				enableModuleMenus(true);
				setGraph(originalGraph);
			}
		});
		closeGraphClusters.setEnabled(false);

		menuBar.add(metricsMenu);
		clusteringMenu.add(knotClusteringMenu);
		graphClusteringMenu.add(nameClusteringMenu);
		clusteringMenu.add(graphClusteringMenu);
		clusteringMenu.add(closeGraphClusters);
		menuBar.add(clusteringMenu);

	}

	@Override
	public void recolour(final Collection<NodeDescriptor> nodeSet) {
		for (final NodeDescriptor v : nodeSet) {
			if (v instanceof ClusterNode) {
				v.setNodeColour(NodeColours.DARK_GREEN);
			} else {
				((MeasureableGraphHandler) handler).calculateColour(v, chosenMetric.getMetric());
			}
		}
		refresh();
	}

	protected void enableModuleMenus(final boolean value) {
		((MeasureableGraphHandler) handler).enableInfoWindow(value);
		metricsMenu.setEnabled(value);
		closeGraphClusters.setEnabled(!value);
		nameKnotCluster.setEnabled(value);
		autoKnotCluster.setEnabled(value);
		regKnotCluster.setEnabled(value);
		folderKnotCluster.setEnabled(value);
		nameClusteringMenu.setEnabled(value);
		regGraphCluster.setEnabled(value);
		folderGraphCluster.setEnabled(value);
		linkedKnotCluster.setEnabled(value);
		linkedGraphCluster.setEnabled(value);
		locationKnotCluster.setEnabled(value);
		locationGraphCluster.setEnabled(value);
		metricLayoutMenu.setEnabled(value);
		handler.setMenusEnabled(value);
	}

	@Override
	protected void initGeneratorAndHandler(final Composite parent) {
		handler = new MeasureableGraphHandler(parent.getShell());
		generator = new ModuleGraphGenerator(((IFileEditorInput) getEditorInput()).getFile().getProject(), errorHandler);
	}
	
	protected class ClusteringAction implements ActionListener {
		
		private String algorithm;
		private boolean grouping;
		private LayoutEntry layout;
		
		public ClusteringAction(final String algorithm, final boolean grouping) {
			this(algorithm, grouping, null);
		}
		
		public ClusteringAction(final String algorithm, final boolean grouping, final LayoutEntry layout) {
			this.algorithm = algorithm;
			this.grouping = grouping;
			this.layout = layout;
		}

		@Override
		public void actionPerformed(final ActionEvent event) {
			final BaseCluster clusterer = new ClustererBuilder().setAlgorithm(algorithm).setGraph(graph).setProject(project).build();
			final ClusteringJob job = new ClusteringJob("Clustering of the module graph", clusterer, grouping, event, layout);
			job.setSystem(false);
			job.setUser(true);
			job.schedule();
		}
	}
	
	protected class ClusteringJob extends Job {

		private final BaseCluster clusterer;
		private final boolean grouping;
		private final ActionEvent event;
		private final LayoutEntry layout;
		
		public ClusteringJob(final String name, final BaseCluster clusterer, final boolean grouping, final ActionEvent event, final LayoutEntry layout) {
			super(name);
			this.clusterer = clusterer;
			this.grouping = grouping;
			this.event = event;
			this.layout = layout;
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			clusterer.run(monitor, grouping);
			if (!clusterer.isOK()) {
				return Status.OK_STATUS;
			}
			if (grouping) {
				chosenLayout.setSelected(false);
				chosenLayout = new LayoutEntry(Layouts.S_LAYOUT_CLUSTER, "");
				handler.setClusters(clusterer.getClusters());
				event.setSource(chosenLayout);
				layoutListener.actionPerformed(event);
			} else {
				enableModuleMenus(false);
				if (layout != null) {
					chosenLayout.setSelected(false);
					chosenLayout = layout;
					chosenLayout.setSelected(true);
				}
				originalGraph = graph;
				setGraph(clusterer.getGraph());
			}
			return Status.OK_STATUS;
		}
		
	}
}
