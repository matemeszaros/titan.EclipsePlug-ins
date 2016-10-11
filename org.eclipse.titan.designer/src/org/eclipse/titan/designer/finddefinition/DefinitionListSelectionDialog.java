/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.finddefinition;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * @author Szabolcs Beres
 * */
public final class DefinitionListSelectionDialog extends ElementListSelectionDialog {

	private Label locationLabel;
	private final StoredDefinitionFilter filter;
	private final DefinitionFinder finder;

	public DefinitionListSelectionDialog(final Shell parent, final ILabelProvider renderer,
			final IProject currentProject) {
		super(parent, renderer);
		filter = StoredDefinitionFilter.getInstance(currentProject);
		finder = new DefinitionFinder(filter);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite control = (Composite) super.createDialogArea(parent);
		createLocationLabel(control);
		createFilterSection(control);
		return control;
	}

	private Control createLocationLabel(final Composite parent) {
		locationLabel = new Label(parent, SWT.BORDER);
		locationLabel.setFont(parent.getFont());

		GridData data = new GridData();
		data.grabExcessVerticalSpace = false;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		locationLabel.setLayoutData(data);
		return locationLabel;
	}

	private Control createFilterSection(final Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		Group scopeGroup = new Group(main, SWT.NONE);
		scopeGroup.setText("Scope");
		final Button projectScope = new Button(scopeGroup, SWT.RADIO);
		projectScope.setText("In the same project");
		final Button workspaceScope = new Button(scopeGroup, SWT.RADIO);
		workspaceScope.setSelection(filter.getWorkspaceScope());
		workspaceScope.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				filter.setWorkspaceScope(workspaceScope.getSelection());
				applyDefinitionFilter();
			}
		});
		workspaceScope.setText("In the workspace");
		scopeGroup.setLayout(new GridLayout());


		Group typeGroup = new Group(main, SWT.NONE);
		typeGroup.setText("Filter");
		final Button module = new Button(typeGroup, SWT.CHECK);
		module.setSelection(filter.getModules());
		module.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				filter.setModules(module.getSelection());
				applyDefinitionFilter();
			}
		});
		module.setText("modules");
		final Button function = new Button(typeGroup, SWT.CHECK);
		function.setSelection(filter.getFunctions());
		function.setText("functions, altsteps, testcases");
		function.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				filter.setFunctions(function.getSelection());
				applyDefinitionFilter();
			}
		});
		final Button type = new Button(typeGroup, SWT.CHECK);
		type.setSelection(filter.getTypes());
		type.setText("types");
		type.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				filter.setTypes(type.getSelection());
				applyDefinitionFilter();
			}
		});
		final Button globalVariables = new Button(typeGroup, SWT.CHECK);
		globalVariables.setSelection(filter.getGlobalVariables());
		globalVariables.setText("module parameters, global templates, global constants");
		globalVariables.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				filter.setGlobalVariables(globalVariables.getSelection());
				applyDefinitionFilter();
			}
		});
		typeGroup.setLayout(new GridLayout(2, false));

		main.setLayout(new GridLayout(2, false));

		return parent;
	}

	private void applyDefinitionFilter() {
		Object[] arr = finder.findDefinitions().toArray();
		setSelection(null);
		setListElements(arr);
	}

	public void init() {
		Object[] arr = finder.findDefinitions().toArray();
		setElements(arr);
	}

	@Override
	protected void handleSelectionChanged() {
		super.handleSelectionChanged();
		Object[] selectedElements = getSelectedElements();
		if (selectedElements.length > 0) {
			locationLabel.setText(((ILocateableNode) selectedElements[0]).getLocation().getFile().getFullPath().toOSString());
		}
	}

}
