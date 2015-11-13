/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.testset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.executors.TreeLeaf;
import org.eclipse.titan.executor.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * */
public final class TestSetTab extends AbstractLaunchConfigurationTab {
	public static final String AVAILABLETESTCASES_LABEL = "availableTestcases";
	public static final String AVAILABLECONTROLPARTS_LABEL = "availableControlParts";
	public static final String TESTSETNAMES_LABEL = "testsetNames";
	public static final String TESTCASESOF_PREFIX = "testcasesOf";
	private static final String TESTCASELIST_CHANGED = "The list of available testcases seems to have changed, please visit the Testsets page";
	private static final String CONTROLPARTLIST_CHANGED = "The list of available control parts seems to have changed, please visit the Testsets page";

	private TableViewer knownTestcasesViewer;
	private TestcasesLabelProvider testcasesLabelProvider;
	private TestsetTreeElement tableRoot;
	private TreeViewer testsetViewer;
	private TestsetTreeElement treeRoot;
	private Composite mainComposite;

	private List<String> availableTestcases;
	private List<String> availableControlparts;
	private MenuManager manager;
	private Action newTestset;
	private Action remove;
	private Action rename;

	@Override
	public void createControl(final Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		mainComposite.setFont(parent.getFont());

		testcasesLabelProvider = new TestcasesLabelProvider();
		createKnownTestCasesViewer(mainComposite);
		createTestsetViewer(mainComposite);

		createActions();
		manager = new MenuManager("menuManager");
		createContextMenu();

		Dialog.applyDialogFont(mainComposite);
	}

	private void createKnownTestCasesViewer(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayout(layout);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());

		Label label = new Label(composite, SWT.NONE);
		label.setText("testcases:");

		knownTestcasesViewer = new TableViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;
		data.grabExcessVerticalSpace = true;
		data.grabExcessHorizontalSpace = true;
		knownTestcasesViewer.getControl().setLayoutData(data);
		knownTestcasesViewer.setContentProvider(new TestcasesContentProvider());
		knownTestcasesViewer.setLabelProvider(testcasesLabelProvider);

		int ops = DND.DROP_COPY;
		Transfer[] types = new Transfer[] {TestcaseTransfer.getInstance()};
		knownTestcasesViewer.addDragSupport(ops, types, new KnownTestcasesDragSourceListener(knownTestcasesViewer));

		tableRoot = new TestsetTreeElement("tableRoot");
		knownTestcasesViewer.setInput(tableRoot);

		label = new Label(composite, SWT.NONE);
		label.setText("As extracted from the executable.\nRefreshed when the Executable on the MainController page changes.");
	}

	public void setAvailableTestcases(final String[] testcases) {
		tableRoot.dispose();
		tableRoot = new TestsetTreeElement("tableRoot");
		availableControlparts = new ArrayList<String>();
		availableTestcases = new ArrayList<String>();
		String testcaseName;
		for (String testcase : testcases) {
			testcaseName = testcase;
			if (testcaseName.endsWith(".control")) {
				availableControlparts.add(testcaseName);
				tableRoot.addChildToEnd(new TestCaseTreeElement(testcaseName));
			} else {
				availableTestcases.add(testcaseName);
				tableRoot.addChildToEnd(new TestCaseTreeElement(testcaseName));
			}
		}
		testcasesLabelProvider.addTestcases(availableTestcases);
		testcasesLabelProvider.addControlParts(availableControlparts);
		knownTestcasesViewer.setInput(tableRoot);
		mainComposite.layout(true);

		testsetViewer.refresh();
		updateLaunchConfigurationDialog();
	}

	private void createTestsetViewer(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayout(layout);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());

		Label label = new Label(composite, SWT.NONE);
		label.setText("test sets:");

		testsetViewer = new TreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		testsetViewer.getControl().setLayoutData(data);
		testsetViewer.setContentProvider(new TestcasesContentProvider());
		testsetViewer.setLabelProvider(testcasesLabelProvider);

		int dragOps = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] types = new Transfer[] {TestcaseTransfer.getInstance()};
		testsetViewer.addDragSupport(dragOps, types, new TestsetTreeDragSourceListener(testsetViewer));

		int dropOps = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT;
		testsetViewer.addDropSupport(dropOps, types, new TestsetTreeDropTargetListener(testsetViewer, this));

		testsetViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				remove.setEnabled(!selection.isEmpty());
				rename.setEnabled(!selection.isEmpty() && (selection.getFirstElement() instanceof TestsetTreeElement));
			}
		});

		treeRoot = new TestsetTreeElement("treeRoot");
		testsetViewer.setInput(treeRoot);

		label = new Label(composite, SWT.NONE);
		label.setText("Right click to add or remove testsets.\nUse drag&drop to add,remove or reorder testcases.");
	}

	public void update() {
		updateLaunchConfigurationDialog();
	}

	private void createActions() {
		newTestset = new Action("create new testset") {

			@Override
			public void run() {
				if (null == testsetViewer) {
					return;
				}
				TestsetDialog dialog = new TestsetDialog(getShell(), "Create new testset");

				if (dialog.open() != Window.OK) {
					return;
				}

				String testsetName = dialog.getTestsetName();
				List<ITreeLeaf> children = treeRoot.children();
				for (ITreeLeaf aChildren : children) {
					if (aChildren.name().equals(testsetName)) {
						return;
					}
				}
				treeRoot.addChildToEnd(new TestsetTreeElement(testsetName));

				testsetViewer.refresh();
				updateLaunchConfigurationDialog();
				super.run();
			}

		};
		newTestset.setToolTipText("creates a new testset");
		newTestset.setEnabled(true);

		rename = new Action("rename testset") {

			@Override
			public void run() {
				if (null == testsetViewer || testsetViewer.getSelection().isEmpty()) {
					return;
				}

				IStructuredSelection selection = (IStructuredSelection) testsetViewer.getSelection();
				TestsetDialog dialog = new TestsetDialog(getShell(), "Rename testset");
				dialog.setTestsetName(((TestsetTreeElement) selection.getFirstElement()).name());

				if (dialog.open() != Window.OK) {
					return;
				}

				String testsetName = dialog.getTestsetName();
				List<ITreeLeaf> children = treeRoot.children();
				for (ITreeLeaf aChildren : children) {
					if (aChildren.name().equals(testsetName)) {
						return;
					}
				}

				((TestsetTreeElement) selection.getFirstElement()).name(testsetName);

				testsetViewer.refresh();
				updateLaunchConfigurationDialog();
				super.run();
			}

		};
		rename.setToolTipText("rename selected testset");
		rename.setEnabled(false);

		remove = new Action("remove") {
			@Override
			public void run() {
				if (null == testsetViewer || testsetViewer.getSelection().isEmpty()) {
					return;
				}
				testsetViewer.getTree().setRedraw(false);
				IStructuredSelection selection = (IStructuredSelection) testsetViewer.getSelection();
				for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
					TreeLeaf element = (TreeLeaf) iterator.next();
					if (null != element.parent()) {
						((TestsetTreeElement) element.parent()).remove(element);
						element.dispose();
					}
				}
				testsetViewer.getTree().setRedraw(true);
				testsetViewer.refresh();
				updateLaunchConfigurationDialog();
				super.run();
			}
		};
		remove.setToolTipText("removes the selected elements");
		remove.setEnabled(false);
	}

	private void createContextMenu() {
		manager.removeAll();
		manager.add(newTestset);
		manager.add(rename);
		manager.add(remove);
		Menu menu = manager.createContextMenu(testsetViewer.getControl());
		testsetViewer.getControl().setMenu(menu);
	}

	@Override
	public String getName() {
		return "Testsets";
	}

	@Override
	public Image getImage() {
		return ImageCache.getImage("testset.gif");
	}

	@Override
	public void initializeFrom(final ILaunchConfiguration configuration) {
		treeRoot.dispose();
		treeRoot = new TestsetTreeElement("treeroot");

		TestsetTreeElement helperTestset;
		TestCaseTreeElement helperTestcase;

		try {
			List<String> testsetNames = configuration.getAttribute("testsetNames", (ArrayList<String>) null);
			if (null != testsetNames) {
				for (String testsetName1 : testsetNames) {
					String testsetName = testsetName1;
					helperTestset = new TestsetTreeElement(testsetName);
					treeRoot.addChildToEnd(helperTestset);
					List<String> testCases = configuration.getAttribute("testcasesOf" + testsetName, (ArrayList<String>) null);
					for (String testCase : testCases) {
						helperTestcase = new TestCaseTreeElement(testCase);
						helperTestset.addChildToEnd(helperTestcase);
					}
				}
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		testsetViewer.setInput(treeRoot);

		try {
			List<String> tempAvailableTestcases = configuration.getAttribute(AVAILABLETESTCASES_LABEL, (ArrayList<String>) null);
			List<String> tempAvailableControlParts = configuration.getAttribute(AVAILABLECONTROLPARTS_LABEL, (ArrayList<String>) null);
			if (tempAvailableTestcases != null && !tempAvailableTestcases.equals(availableTestcases)) {
				setErrorMessage(TESTCASELIST_CHANGED);
			}
			if (tempAvailableControlParts != null && !tempAvailableControlParts.equals(availableControlparts)) {
				setErrorMessage(CONTROLPARTLIST_CHANGED);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	@Override
	public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
		List<String> testsetNames = new ArrayList<String>();
		List<List<String>> testSets = new ArrayList<List<String>>();

		for (ITreeLeaf testSet : treeRoot.children()) {
			testsetNames.add(testSet.name());
			List<String> testcases = new ArrayList<String>();
			for (ITreeLeaf testcase : ((TestsetTreeElement) testSet).children()) {
				testcases.add(testcase.name());
			}
			testSets.add(testcases);
		}
		configuration.setAttribute(AVAILABLECONTROLPARTS_LABEL, availableControlparts);
		configuration.setAttribute(AVAILABLETESTCASES_LABEL, availableTestcases);
		configuration.setAttribute(TESTSETNAMES_LABEL, testsetNames);
		for (int i = 0, size = testSets.size(); i < size; i++) {
			configuration.setAttribute(TESTCASESOF_PREFIX + testsetNames.get(i), testSets.get(i));
		}
		setErrorMessage(null);
	}

	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		// Do nothing
	}

	@Override
	public void activated(final ILaunchConfigurationWorkingCopy workingCopy) {
		super.activated(workingCopy);
		updateLaunchConfigurationDialog();
	}

	/**
	 * Sets the testcases for the launch configuration in the proper format.
	 *
	 * @param workingCopy the launch configuration to set the data in.
	 * @param testcases the list of testcases to be added to the launch configuration.
	 * */
	public static void setTestcases(final ILaunchConfigurationWorkingCopy workingCopy, final String[] testcases) {
		List<String> availableControlparts = new ArrayList<String>();
		List<String> availableTestcases = new ArrayList<String>();
		String testcaseName;
		for (String testcase : testcases) {
			testcaseName = testcase;
			if (testcaseName.endsWith(".control")) {
				availableControlparts.add(testcaseName);
			} else {
				availableTestcases.add(testcaseName);
			}
		}

		workingCopy.setAttribute(AVAILABLECONTROLPARTS_LABEL, availableControlparts);
		workingCopy.setAttribute(AVAILABLETESTCASES_LABEL, availableTestcases);
	}
}
