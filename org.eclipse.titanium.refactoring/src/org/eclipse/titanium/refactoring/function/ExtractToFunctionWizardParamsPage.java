/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Wizard page #2: edit parameter names.
 * 
 * @author Viktor Varga
 */
public class ExtractToFunctionWizardParamsPage extends UserInputWizardPage {

	private TableViewer tableViewer;
	private final IModelProvider<ParamTableItem> modelProvider;

	public ExtractToFunctionWizardParamsPage(final String name,
			final IModelProvider<ParamTableItem> modelProvider) {
		super(name);
		this.modelProvider = modelProvider;
	}

	@Override
	public void createControl(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		initializeDialogUnits(composite);
		setControl(composite);

		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		Label searchLabel = new Label(composite, SWT.NONE);
		searchLabel.setText("Specify new function parameter names: ");

		tableViewer = new TableViewer(composite, SWT.MULTI | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(composite);

		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(modelProvider.getItems());

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		tableViewer.getControl().setLayoutData(gridData);

	}

	private void createColumns(final Composite parent) {

		final String[] titles = { "Passing type", "Typename", "Name" };
		final int[] bounds = { 100, 200, 280 };

		// pass type
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				ParamTableItem p = (ParamTableItem) element;
				return p.getPassType();
			}
		});

		// type name
		col = createTableViewerColumn(titles[1], bounds[1]);
		col.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(final Object element) {
				ParamTableItem p = (ParamTableItem) element;
				return p.getType();
			}
		});

		// name
		col = createTableViewerColumn(titles[2], bounds[2]);
		col.setEditingSupport(new NameEditingSupport(col.getViewer()));
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				ParamTableItem p = (ParamTableItem) element;
				return p.getName();
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(final String title, final int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(
				tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(false);
		return viewerColumn;
	}

	/**
	 * Provides editing support for the parameters' table.
	 * */
	private class NameEditingSupport extends EditingSupport {

		private final TextCellEditor cellEditor;

		public NameEditingSupport(final ColumnViewer viewer) {
			super(viewer);
			this.cellEditor = new TextCellEditor(tableViewer.getTable());
			viewer.setCellEditors(new CellEditor[] { cellEditor });
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			ParamTableItem pti = (ParamTableItem) element;
			cellEditor.setValue(pti.getName());
			return cellEditor;
		}

		@Override
		protected boolean canEdit(final Object element) {
			return true;
		}

		@Override
		protected Object getValue(final Object element) {
			ParamTableItem pti = (ParamTableItem) element;
			return pti.getName();
		}

		@Override
		protected void setValue(final Object element, final Object value) {
			ParamTableItem pti = (ParamTableItem) element;
			if (!(value instanceof String)) {
				return;
			}
			pti.setName((String) value);
			getViewer().update(element, null);

		}

	}

}
