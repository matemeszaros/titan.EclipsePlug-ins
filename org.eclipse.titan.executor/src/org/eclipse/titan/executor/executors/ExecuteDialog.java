/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.titan.executor.Activator;

/**
 * This class creates the dialog presenting the user with the selection of executable elements.
 * 
 * @author Kristof Szabados
 * */
public final class ExecuteDialog extends Dialog {
	/**
	 * A small class to store the type of the executable element.
	 * */
	private static final class InnerLeaf extends TreeLeaf {
		private ExecutableType type;

		public InnerLeaf(final String name, final ExecutableType type) {
			super(name);
			this.type = type;
		}
	}

	private TreeViewer viewer;
	private TreeBranch treeRoot;
	private List<String> testcases;
	private List<String> testsets;
	private List<String> controlparts;
	private String configurationFile;
	private String selectedElement;
	private Spinner timesSpinner;

	private Label timesPrefixLabel;
	private Label timesPostfixLabel;

	public enum ExecutableType {
		NONE(0), TESTCASE(1), TESTSET(2), CONTROLPART(3), CONFIGURATIONFILE(4);

		private int value;
		private ExecutableType(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static ExecutableType getExecutableType(final int value) {
			switch(value) {
			case 1 :
				return TESTCASE;
			case 2:
				return TESTSET;
			case 3:
				return CONTROLPART;
			case 4:
				return CONFIGURATIONFILE;
			default:
				return NONE;
			}
		}
	}

	private ExecutableType selectionType = ExecutableType.NONE;
	private InnerLeaf rawSelection;
	private int selectionTimes = 1;
	private boolean selectionTimesVisible = true;

	public ExecuteDialog(final Shell shell) {
		super(shell);
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Execute");
		newShell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(final DisposeEvent e) {
				if (null != treeRoot) {
					treeRoot.dispose();
					treeRoot = null;
				}
			}
		});
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		final GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;
		container.setLayoutData(data);

		viewer = new TreeViewer(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ExecuteDialogContentProvider());
		viewer.setLabelProvider(new ExecuteDialogLabelProvider());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				final Button button = getButton(IDialogConstants.OK_ID);
				if (null != button) {
					if (selection.isEmpty() || selection.getFirstElement() instanceof TreeBranch) {
						selectionType = ExecutableType.NONE;
						button.setEnabled(false);
						if (selectionTimesVisible) {
							timesPrefixLabel.setEnabled(false);
							timesSpinner.setEnabled(false);
							timesPostfixLabel.setEnabled(false);
						}
					} else {
						selectionType = ((InnerLeaf) selection.getFirstElement()).type;
						selectedElement = ((InnerLeaf) selection.getFirstElement()).name();
						button.setEnabled(true);
						if (selectionTimesVisible) {
							timesPrefixLabel.setEnabled(true);
							timesSpinner.setEnabled(true);
							timesPostfixLabel.setEnabled(true);
						}
					}
				}
			}
		});
		final ExecuteDialog thisDialog = this;
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.isEmpty() || selection.getFirstElement() instanceof TreeBranch) {
					return;
				}

				thisDialog.okPressed();
			}
		});
		if (selectionTimesVisible) {
			final Composite timesComp = new Composite(container, SWT.NONE);
			timesComp.setLayout(new GridLayout(3, false));

			timesPrefixLabel = new Label(timesComp, SWT.NONE);
			timesPrefixLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			timesPrefixLabel.setText("Run selected:");

			timesSpinner = new Spinner(timesComp, SWT.NONE);
			timesSpinner.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			timesSpinner.setMinimum(1);
			timesSpinner.setMaximum(100000);
			timesSpinner.setPageIncrement(100);
			timesSpinner.setSelection(selectionTimes);
			timesSpinner.setToolTipText("Controls how many times the selected item should be executed");
			timesSpinner.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					super.widgetSelected(e);
					selectionTimes = timesSpinner.getSelection();
				}

			});

			timesPostfixLabel = new Label(timesComp, SWT.NONE);
			timesPostfixLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			timesPostfixLabel.setText("times");
		}

		createInitialInput();
		viewer.setInput(treeRoot);
		if (null != rawSelection) {
			final StructuredSelection selection = new StructuredSelection(rawSelection);
			viewer.setSelection(selection, true);
		}

		Dialog.applyDialogFont(container);
		return container;
	}

	public void setTestcases(final List<String> testcases) {
		this.testcases = testcases;
	}

	public void setControlparts(final List<String> controlparts) {
		this.controlparts = controlparts;
	}

	public void setTestsets(final List<String> testsets) {
		this.testsets = testsets;
	}

	public void setConfigurationFile(final String configurationFile) {
		this.configurationFile = configurationFile;
	}

	public void setSelection(final String selectedElement, final int selectionTimes, final ExecutableType selectionType) {
		this.selectedElement = selectedElement;
		this.selectionTimes = selectionTimes;
		this.selectionType = selectionType;
	}

	/**
	 * Creates and fills the tree with initial values.
	 * <p>
	 * If no data is provided for a section, the whole section is left out
	 * */
	private void createInitialInput() {
		treeRoot = new TreeBranch("treeroot");
		TreeBranch helper;
		if (null != controlparts) {
			helper = new TreeBranch("control parts");
			treeRoot.addChildToEnd(helper);
			for (String name : controlparts) {
				helper.addChildToEnd(new InnerLeaf(name, ExecutableType.CONTROLPART));
			}
		}
		if (null != testsets) {
			helper = new TreeBranch("test sets");
			treeRoot.addChildToEnd(helper);
			for (String name : testsets) {
				helper.addChildToEnd(new InnerLeaf(name, ExecutableType.TESTSET));
			}
		}
		if (null != testcases) {
			helper = new TreeBranch("testcases");
			treeRoot.addChildToEnd(helper);
			for (String name : testcases) {
				helper.addChildToEnd(new InnerLeaf(name, ExecutableType.TESTCASE));
			}
		}
		if (null != configurationFile && configurationFile.length() > 0) {
			helper = new TreeBranch("configuration file");
			treeRoot.addChildToEnd(helper);
			helper.addChildToEnd(new InnerLeaf(configurationFile, ExecutableType.CONFIGURATIONFILE));
		}

		if (null != selectedElement) {
			for (ITreeLeaf branch : treeRoot.children()) {
				for (ITreeLeaf leaf : ((ITreeBranch) branch).children()) {
					if (selectedElement.equals(leaf.name())) {
						rawSelection = (InnerLeaf) leaf;
					}
				}
			}
		}
	}

	public String getSelectedElement() {
		return selectedElement;
	}

	public ExecutableType getSelectionType() {
		return selectionType;
	}

	public void disableSelectionTimes() {
		selectionTimesVisible = false;
	}

	public int getSelectionTimes() {
		return selectionTimes;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings();
	}
}
