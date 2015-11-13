/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.topview;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.metrics.IMetricEnum;
import org.eclipse.titanium.metrics.utils.ModuleMetricsWrapper;
import org.eclipse.titanium.metrics.utils.WrapperStore;
import org.eclipse.titanium.utils.ProjectAnalyzerJob;
import org.eclipse.ui.part.ViewPart;

/**
 * This view is to provide the user a general overview of the health of a TTCN3
 * project, particularly highlighting the "worst" modules.
 * <p>
 * In the view, the user can select a project to analyze, and choose some
 * metrics for the analysis. After running, the view will show each module of
 * the project, ordered by the cumulative risk of the selected metrics on these
 * modules.
 * 
 * @author poroszd
 * 
 */
public class TopView extends ViewPart {
	private static final String REFRESH_TOOLTIP = "Start measuring";

	private Composite parent;
	private Table moduleTable;
	private Combo projectSelector;
	private Button refresh;
	private ModuleMetricsWrapper mw;

	public TopView() {
		super();
	}

	@Override
	public void createPartControl(final Composite parent) {
		this.parent = parent;
		parent.setLayout(new GridLayout());

		final Composite head = new Composite(parent, SWT.NONE);
		createHead(head);
		createTable(Collections.<IMetricEnum> emptySet());
	}

	@Override
	public void setFocus() {
		parent.setFocus();
	}

	private void createHead(final Composite head) {
		// upper composite, the head itself
		final GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		head.setLayout(layout);

		GridData g = new GridData(SWT.DEFAULT, 35);
		g.grabExcessHorizontalSpace = true;
		g.horizontalAlignment = SWT.FILL;
		head.setLayoutData(g);

		// project selector in the head
		projectSelector = new Combo(head, SWT.READ_ONLY);
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (TITANNature.hasTITANNature(project)) {
				projectSelector.add(project.getName());
			}
		}
		projectSelector.select(0);
		g = new GridData();
		g.horizontalAlignment = SWT.FILL;
		g.grabExcessHorizontalSpace = true;
		projectSelector.setLayoutData(g);

		// refresh button in the head
		refresh = new Button(head, SWT.NONE);
		refresh.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "resources/icons/metrics_start_measure.gif").createImage());
		refresh.setToolTipText(REFRESH_TOOLTIP);
		refresh.setLayoutData(new GridData(SWT.DEFAULT, 25));

		refresh.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				final String selectedProjectName = projectSelector.getText();
				if ("".equals(selectedProjectName)) {
					// Nothing is selected (probably there are no projects in
					// the project browser);
					return;
				}

				// Analyze project.
				// To avoid further analysis requests, disable the refresh
				// button.
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(selectedProjectName);
				if (project != null) {
					refresh.setEnabled(false);

					new ProjectAnalyzerJob("Metrics calculations") {
						private Set<IMetricEnum> metrics;

						@Override
						public IStatus doPreWork(IProgressMonitor monitor) {
							// choose metrics
							final MetricSelectorDialog mst = new MetricSelectorDialog();
							Display.getDefault().syncExec(mst);
							metrics = mst.getUsed();
							return Status.OK_STATUS;
						}

						@Override
						public IStatus doPostWork(IProgressMonitor monitor) {
							try {
								mw = WrapperStore.getWrapper(getProject());
								if (!moduleTable.isDisposed()) {
									Display.getDefault().syncExec(new Runnable() {
										@Override
										public void run() {
											createTable(metrics);
										}
									});
								}

								return Status.OK_STATUS;
							} finally {
								// The view should recover from any unexpected
								// error, so
								// the refresh button should not remain
								// disabled.
								if (!refresh.isDisposed()) {
									Display.getDefault().syncExec(new Runnable() {
										@Override
										public void run() {
											refresh.setEnabled(true);
										}
									});
								}
							}
						}
					}.quickSchedule(project);
				}
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				//Do nothing
			}
		});
	}

	private void createTable(final Set<IMetricEnum> metrics) {
		if (moduleTable != null) {
			moduleTable.dispose();
		}
		moduleTable = new Table(parent, SWT.SINGLE);

		final GridData gd = new GridData();
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		moduleTable.setLayoutData(gd);
		if (!metrics.isEmpty()) {
			TableViewer viewer = new TableViewer(moduleTable);
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setInput(mw.getMetricData().getModules());
			moduleTable.setHeaderVisible(true);
			moduleTable.setLinesVisible(true);

			final TableViewerColumn names = new TableViewerColumn(viewer, SWT.LEFT);
			final TableColumn namesCol = names.getColumn();
			namesCol.setAlignment(SWT.LEFT);
			namesCol.setText("Modules");
			namesCol.setWidth(250);
			names.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					if (!(element instanceof Module)) {
						throw new AssertionError("Elements of the view should be modules");
					}
					Module module = (Module) element;

					return module.getName();
				}
			});

			for (final IMetricEnum m : metrics) {
				final TableViewerColumn cv = new TableViewerColumn(viewer, SWT.CENTER);
				final TableColumn c = cv.getColumn();
				c.setAlignment(SWT.CENTER);
				c.setText("(" + m.groupName() + " metrics)\n" + m.getName());
				c.setWidth(100);

				cv.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public Color getBackground(Object element) {
						if (!(element instanceof Module)) {
							throw new AssertionError("Elements of the view should be modules");
						}
						final Module module = (Module) element;

						double risk = mw.getRiskValue(m, module.getName());
						if (risk < 1) {
							return Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
						} else if (risk < 2) {
							return Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
						} else {
							return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
						}
					}

					@Override
					public String getText(Object element) {
						if (!(element instanceof Module)) {
							throw new AssertionError("Elements of the view should be  modules");
						}
						Module module = (Module) element;

						Number n = mw.getValue(m, module.getName());
						return n == null ? "" : n.toString();
					}
				});
			}

			viewer.addDoubleClickListener(new DCListener());
			viewer.setComparator(new Comparator(mw, metrics));
		}
		parent.layout();
	}

	public void setSelectedProject(final IProject project) {
		String name = project.getName();
		int found = -1;
		for (int index = 0; index < projectSelector.getItemCount(); ++index) {
			if (name.equals(projectSelector.getItem(index))) {
				found = index;
				break;
			}
		}
		if (found >= 0 ){
			projectSelector.select(found);
		}
	}

	public void startMeasuring() {
		refresh.notifyListeners(SWT.Selection, new Event());
	}
}
