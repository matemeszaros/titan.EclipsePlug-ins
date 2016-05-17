/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.view;

import java.io.File;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.StatColumn;
import org.eclipse.titanium.metrics.utils.RiskLevel;
import org.eclipse.titanium.utils.LocationHighlighter;
import org.eclipse.titanium.utils.ProjectAnalyzerJob;

/**
 * The metrics view viewpart.
 * 
 * This view displays metric information of a desired project. Users can set the
 * regarded preferences on preference page.
 * 
 * @author poroszd
 * 
 */
public class MetricsView extends ViewPart {
	private static final String COLUMN_HEADER_1 = "Metrics";
	private static final Map<StatColumn, Integer> ALIGNMENTS;
	static {
		ALIGNMENTS = new EnumMap<StatColumn, Integer>(StatColumn.class);
		ALIGNMENTS.put(StatColumn.TOTAL, SWT.CENTER);
		ALIGNMENTS.put(StatColumn.MAX, SWT.CENTER);
		ALIGNMENTS.put(StatColumn.MEAN, SWT.LEFT);
		ALIGNMENTS.put(StatColumn.DEV, SWT.LEFT);
	}
	private static final String REFRESH_TOOLTIP = "Start measuring";
	private static final String EXPORT_TOOLTIP = "Export to xls";

	private Composite parent;
	private TreeViewer browser;
	private Combo projectSelector;
	private Button refresh;
	private Button export;

	private MetricData data;

	public MetricsView() {
		super();
	}

	@Override
	public void createPartControl(final Composite parent) {
		this.parent = parent;
		parent.setLayout(new GridLayout());

		final Composite head = new Composite(parent, SWT.NONE);
		final Tree inner = new Tree(parent, SWT.SINGLE);

		browser = new TreeViewer(inner);
		browser.setContentProvider(new ContentProvider());
		browser.setInput(null);

		createHead(head);
		createTree(inner);
		addDoubleClickListener(browser);

	}

	@Override
	public void setFocus() {
		parent.setFocus();
	}
	
	private void refreshData() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if(browser == null) {
					return;
				}

				browser.setInput(data);
				browser.setContentProvider(new ContentProvider());
				browser.setLabelProvider(new LabelProvider(data));
				browser.setComparator(new Sorter(data));
			}
		});
	}

	private void createHead(final Composite head) {
		// upper composite, the head itself
		final GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		head.setLayout(layout);

		GridData g = new GridData(SWT.DEFAULT, 25);
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
					data = null;
					refreshData();

					return;
				}

				// Analyze project.
				// To avoid further analysis requests, disable the refresh
				// button.
				refresh.setEnabled(false);

				final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(selectedProjectName);
				if (project == null) {
					data = null;
					refreshData();

					return;
				}

				new ProjectAnalyzerJob("Metrics calculations") {
					@Override
					public IStatus doPostWork(final IProgressMonitor monitor) {
						try {
							data = MetricData.measure(project);

							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									refreshData();
									export.setEnabled(true);
								}
							});
							return Status.OK_STATUS;
						} finally {
							// The view should recover from any unexpected
							// error, so
							// the refresh button should not remain
							// disabled.
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									refresh.setEnabled(true);
								}
							});
						}
					}
				}.quickSchedule(project);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				//Do nothing
			}
		});

		createExportToXlsButton(head);
		
		final IResourceChangeListener PROJECT_CLOSE_LISTENER = new IResourceChangeListener() {
			@Override
			public void resourceChanged(final IResourceChangeEvent event) {
				switch (event.getType()) {
				case IResourceChangeEvent.PRE_CLOSE:
				case IResourceChangeEvent.PRE_DELETE:
					final IResource resource = event.getResource();
					//projectSelector.getText();
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							if(projectSelector == null || projectSelector.isDisposed()) {
								return;
							}

							projectSelector.deselectAll();
							projectSelector.removeAll();
							for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
								if (!project.equals(resource.getProject()) && TITANNature.hasTITANNature(project)) {
									projectSelector.add(project.getName());
								}
							}

							data = null;
							refreshData();
						}
					});
					break;
				default:
					break;
				}
			}
		};
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(PROJECT_CLOSE_LISTENER);
	}

	private void createExportToXlsButton(final Composite head) {
		
		export = new Button(head, SWT.NONE);
		export.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "resources/icons/metrics_export_csv.gif").createImage());
		export.setToolTipText(EXPORT_TOOLTIP);
		export.setEnabled(false);
		export.setLayoutData(new GridData(SWT.DEFAULT, 25));

		export.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				// choose the set of metrics to export
				final RiskLevel r = new ExportSetDialog(parent.getDisplay()).open();
				if (r == null) {
					return;
				}

				final FileDialog d = new FileDialog(parent.getShell(), SWT.SAVE);
				d.setText("Export metric results to xls");
				d.setFilterExtensions(new String[] { "*.xls" });
				final Calendar now = Calendar.getInstance();
				d.setFileName(projectSelector.getText() + "--" + now.get(Calendar.YEAR) + "-" + (1 + now.get(Calendar.MONTH)) + "-" + now.get(Calendar.DAY_OF_MONTH));
				final String fn = d.open();
				if (fn == null) {
					return;
				}

				final File file = new File(fn);
				final XLSExporter xlsWriter = new XLSExporter(data);
				xlsWriter.setFile(file);
				xlsWriter.write(r);
			}
		});
		
	}

	private void createTree(final Tree inner) {
		final GridData gd = new GridData();
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		inner.setLayoutData(gd);
		inner.setHeaderVisible(true);
		inner.setLinesVisible(true);

		final Map<StatColumn, TreeColumn> columns = new EnumMap<StatColumn, TreeColumn>(StatColumn.class);

		final TreeColumn names = new TreeColumn(inner, SWT.LEFT);
		names.setAlignment(SWT.LEFT);
		names.setText(COLUMN_HEADER_1);
		names.setWidth(250);

		for (final StatColumn col : StatColumn.values()) {
			final TreeColumn c = new TreeColumn(inner, SWT.RIGHT);
			c.setAlignment(ALIGNMENTS.get(col));
			c.setText(col.getName());
			c.setWidth(100);
			columns.put(col, c);
		}
	}

	private void addDoubleClickListener(final TreeViewer browser) {
		browser.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent event) {
				final ISelection selection = event.getSelection();
				if (!(selection instanceof IStructuredSelection)) {
					return;
				}

				final Object o = ((IStructuredSelection) selection).getFirstElement();
				if (!(o instanceof IOpenable)) {
					return;
				}

				final Location loc = ((IOpenable) o).getLocation();
				LocationHighlighter.jumpToLocation(loc);

			}
		});
	}

	public void setSelectedProject(final IProject project) {
		final String name = project.getName();
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
