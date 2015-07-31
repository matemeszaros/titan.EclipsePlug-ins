/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.testexecution;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.executors.BaseExecutor;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.graphics.ImageCache;
import org.eclipse.titan.executor.views.executormonitor.LaunchElement;
import org.eclipse.titan.executor.views.executormonitor.MainControllerElement;
import org.eclipse.titan.executor.views.notification.NotificationView;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.titan.executor.GeneralConstants.ERROR;
import static org.eclipse.titan.executor.GeneralConstants.FAIL;
import static org.eclipse.titan.executor.GeneralConstants.INCONC;
import static org.eclipse.titan.executor.GeneralConstants.NONE;
import static org.eclipse.titan.executor.GeneralConstants.PASS;
import static org.eclipse.titan.executor.views.executormonitor.ExecutorMonitorView.EXECUTORMONITOR_VIEW_ID;

/**
 * @author Kristof Szabados
 * */
public final class TestExecutionView extends ViewPart implements ISelectionListener {
	public static final String TESTEXECUTIONVIEW = Activator.PLUGIN_ID + ".views.testExecution.TestExecutionView";

	private static TestExecutionView instance = null;

	private Table table;
	private TableViewer viewer;
	private TestExecutionContentProvider contentProvider;
	private TestExecutionLabelProvider labelProvider;

	private ITreeLeaf actualInput;
	private Action saveAsAction;
	private Action followLastRecord;
	private boolean isFollowing = true;

	@Override
	public void createPartControl(final Composite parent) {
		table = new Table(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
		table.setLayoutData(gridData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.LEFT, 0);
		column.setText("reason");
		column.setWidth(300);
		Image infoImage = ImageCache.getImage("information2.gif");
		column.setImage(infoImage);
		column.setMoveable(true);
		column.setAlignment(SWT.LEFT);

		column = new TableColumn(table, SWT.LEFT, 0);
		column.setText("verdict");
		column.setWidth(100);
		column.setImage(infoImage);
		column.setMoveable(true);
		column.setAlignment(SWT.LEFT);

		column = new TableColumn(table, SWT.LEFT, 0);
		column.setText("testcase");
		column.setWidth(300);
		column.setImage(infoImage);
		column.setMoveable(true);
		column.setAlignment(SWT.LEFT);

		column = new TableColumn(table, SWT.LEFT, 0);
		column.setText("timestamp");
		column.setWidth(164);
		column.setMoveable(true);
		column.setAlignment(SWT.LEFT);
		column.setImage(ImageCache.getImage("clock.gif"));

		viewer = new TableViewer(table);
		viewer.setColumnProperties(new String[] {"timestamp", "info", "verdict"});
		contentProvider = new TestExecutionContentProvider();
		viewer.setContentProvider(contentProvider);
		labelProvider = new TestExecutionLabelProvider();
		viewer.setLabelProvider(labelProvider);

		actualInput = getInitialInput();
		viewer.setInput(actualInput);
		createTooltip(actualInput);

		createActions();
		createToolBar();
		createMenu();

		getSite().getPage().addSelectionListener(EXECUTORMONITOR_VIEW_ID, this);
		setInstance(this);
	}

	@Override
	public void dispose() {
		setInstance(null);
		saveAsAction = null;
		followLastRecord = null;
		getSite().getPage().removeSelectionListener(this);
		super.dispose();
	}

	private ITreeLeaf getInitialInput() {
		final ISelection selection = getSite().getPage().getSelection(EXECUTORMONITOR_VIEW_ID);
		if (null == selection || selection.isEmpty()) {
			return null;
		}

		final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		ITreeLeaf element = (ITreeLeaf) structuredSelection.getFirstElement();
		if (null != element) {
			while (!(element instanceof LaunchElement)) {
				element = element.parent();
			}
		}
		return element;
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
		if (this.equals(part) || !(selection instanceof IStructuredSelection) || null == viewer.getContentProvider()) {
			return;
		}
		ITreeLeaf selectedElement;
		if (selection.isEmpty()) {
			selectedElement = null;
		} else {
			selectedElement = (ITreeLeaf) ((IStructuredSelection) selection).getFirstElement();
		}
		if (null != selectedElement) {
			while (!(selectedElement instanceof LaunchElement)) {
				selectedElement = selectedElement.parent();
			}
		}
		if (null != actualInput && actualInput.equals(selectedElement)) {
			viewer.refresh(false);
		} else {
			viewer.setInput(selectedElement);
			actualInput = selectedElement;
		}
		if (isFollowing && null != actualInput && actualInput instanceof LaunchElement) {
			final List<ITreeLeaf> children = ((LaunchElement) actualInput).children();
			for (ITreeLeaf aChildren : children) {
				final BaseExecutor executor = ((MainControllerElement) aChildren).executor();
				if (null != executor && !executor.executedTests().isEmpty()) {
					viewer.reveal(executor.executedTests().get(executor.executedTests().size() - 1));
				}
			}
		}
		createTooltip(selectedElement);
		if (null == selectedElement) {
			saveAsAction.setEnabled(false);
		} else {
			saveAsAction.setEnabled(true);
		}
	}

	/**
	 * Refreshes the input directly in case the Execution Control view is not yet opened.
	 *
	 * @param executor the executor to use.
	 * */
	public static void refreshInput(final BaseExecutor executor) {
		if (null == getInstance()) {
			return;
		}

		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				if (null != executor && !executor.executedTests().isEmpty()) {
					TableViewer tableViewer = getInstance().viewer;
					tableViewer.setInput(executor.executedTests());
					tableViewer.reveal(executor.executedTests().get(executor.executedTests().size() - 1));
				}
			}
		});
	}

	private void createTooltip(final ITreeLeaf element) {
		if (null == element) {
			setTitleToolTip(null);
			return;
		}

		final List<ExecutedTestcase> executedTestCases = new ArrayList<ExecutedTestcase>();
		final List<ITreeLeaf> children = ((LaunchElement) actualInput).children();
		ITreeLeaf executorElement;
		for (ITreeLeaf aChildren : children) {
			executorElement = aChildren;
			if (executorElement instanceof MainControllerElement) {
				BaseExecutor executor = ((MainControllerElement) executorElement).executor();
				if (null != executor) {
					executedTestCases.addAll(executor.executedTests());
				}
			}
		}
		if (executedTestCases.isEmpty()) {
			setTitleToolTip(element.name());
			return;
		}

		String statistics = createStatistics(element, executedTestCases);
		setTitleToolTip(statistics);
	}

	private String createStatistics(ITreeLeaf element, List<ExecutedTestcase> executedTestCases) {
		int inconc = 0;
		int fail = 0;
		int error = 0;
		int none = 0;
		int pass = 0;
		int sum = executedTestCases.size();
		for (ExecutedTestcase test : executedTestCases) {
			if (PASS.equals(test.getVerdict())) {
				pass++;
			} else if (INCONC.equals(test.getVerdict())) {
				inconc++;
			} else if (FAIL.equals(test.getVerdict())) {
				fail++;
			} else if (ERROR.equals(test.getVerdict())) {
				error++;
			} else if (NONE.equals(test.getVerdict())) {
				none++;
			}
		}
		final StringBuilder builder = new StringBuilder(element.name());
		builder.append("\nVerdict statistics: ")
				.append(none).append(" none (").append((100 * none) / sum).append("%), ")
				.append(pass).append(" pass (").append((100 * pass) / sum).append("%), ")
				.append(inconc).append(" inconc (").append((100 * inconc) / sum).append("%), ")
				.append(fail).append(" fail (").append((100 * fail) / sum).append("%), ")
				.append(error).append(" error(").append((100 * error) / sum).append("%)");
		return builder.toString();
	}

	private void createActions() {
		saveAsAction = new Action("Save As") {
			@Override
			public void run() {
				saveAs();
			}
		};
		saveAsAction.setToolTipText("Save As");
		saveAsAction.setImageDescriptor(ImageCache.getImageDescriptor("saveas_edit.gif"));
		saveAsAction.setEnabled(false);

		followLastRecord = new Action("Follow the last record") {
			@Override
			public void run() {
				isFollowing = followLastRecord.isChecked();
			}
		};
		followLastRecord.setToolTipText("Toggles the follow the last line behavior");
		followLastRecord.setEnabled(true);
		followLastRecord.setChecked(isFollowing);
	}

	private void createToolBar() {
		final IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		toolbarManager.add(saveAsAction);
	}

	private void createMenu() {
		final IMenuManager manager = getViewSite().getActionBars().getMenuManager();
		manager.add(followLastRecord);
	}

	@Override
	public void setFocus() {
		// Do nothing
	}

	private void saveAs() {
		NotificationView.saveLaunchElementAs(getSite().getShell(), actualInput);
	}
	
	private static synchronized TestExecutionView getInstance() {
		return instance;
	}

	private static synchronized void setInstance(TestExecutionView instance) {
		TestExecutionView.instance = instance;
	}
}
