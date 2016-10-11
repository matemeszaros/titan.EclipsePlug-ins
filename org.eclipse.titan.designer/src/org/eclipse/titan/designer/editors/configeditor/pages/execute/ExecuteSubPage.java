/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.execute;

import java.util.Arrays;
import java.util.Iterator;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.titan.common.parsers.AddedParseTree;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.ExecuteSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ExecuteSectionHandler.ExecuteItem;
import org.eclipse.titan.designer.editors.configeditor.ConfigEditor;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ExecuteSubPage {

	private Label totalExecuteElementsLabel;

	private Table executeElementsTable;
	private TableViewer executeElementsTableViewer;
	private Button add;
	private Button remove;

	private static final String[] COLUMN_NAMES = new String[] { "moduleName", "testcaseName" };

	private ConfigEditor editor;
	private ExecuteSectionHandler executeSectionHandler;

	public ExecuteSubPage(final ConfigEditor editor) {
		this.editor = editor;
	}

	void createExecuteSection(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		section.setText("Elements to be executed");
		section.setDescription("Specify the list of testcases and control parts to be executed for this configuration.\n"
				+ "Use drag&drop to change the order of elements.");
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(false);
			}
		});

		GridData gd = new GridData(GridData.FILL_BOTH);
		section.setLayoutData(gd);

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		section.setClient(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		client.setLayout(layout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		client.setLayoutData(gd);

		toolkit.paintBordersFor(client);

		executeElementsTable = toolkit.createTable(client, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		executeElementsTable.setEnabled(executeSectionHandler != null);

		TableColumn column = new TableColumn(executeElementsTable, SWT.LEFT, 0);
		column.setText("Module name");
		column.setWidth(200);
		column.setMoveable(false);

		column = new TableColumn(executeElementsTable, SWT.LEFT, 1);
		column.setText("Testcase name");
		column.setMoveable(false);
		column.setWidth(200);

		executeElementsTable.setHeaderVisible(true);
		executeElementsTable.setLinesVisible(true);

		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 100;
		gd.heightHint = 200;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		executeElementsTable.setLayoutData(gd);

		Composite buttons = toolkit.createComposite(client);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL));
		add = toolkit.createButton(buttons, "Add...", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		add.setLayoutData(gd);
		add.setEnabled(executeSectionHandler != null);
		add.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				//Do nothing
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (executeSectionHandler == null) {
					return;
				}

				if (executeSectionHandler.getLastSectionRoot() == null) {
					createNewExecuteSection();
				}

				ExecuteItem newItem = createNewExecuteItem();
				if (newItem == null) {
					return;
				}

				ConfigTreeNodeUtilities.addChild(executeSectionHandler.getLastSectionRoot(), newItem.getRoot());

				executeSectionHandler.getExecuteitems().add(newItem);

				internalRefresh();
				executeElementsTable.select(executeSectionHandler.getExecuteitems().size() - 1);
				executeElementsTable.showSelection();
				editor.setDirty();
			}
		});

		remove = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		remove.setLayoutData(gd);
		remove.setEnabled(executeSectionHandler != null);
		remove.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				//Do nothing
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (executeElementsTableViewer == null || executeSectionHandler == null) {
					return;
				}

				removeSelectedExecuteItems();

				if (executeSectionHandler.getExecuteitems().isEmpty()) {
					removeExecuteSection();
				}

				internalRefresh();
				editor.setDirty();
			}

		});

		totalExecuteElementsLabel = toolkit.createLabel(buttons, "Total: 0");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		totalExecuteElementsLabel.setLayoutData(gd);

		executeElementsTableViewer = new TableViewer(executeElementsTable);
		executeElementsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				// not needed this time
			}
		});

		executeElementsTableViewer.setContentProvider(new ExecuteDataContentProvider());
		executeElementsTableViewer.setLabelProvider(new ExecuteDataLabelProvider());
		executeElementsTableViewer.setInput(executeSectionHandler);
		executeElementsTableViewer.setColumnProperties(COLUMN_NAMES);
		executeElementsTableViewer.setCellEditors(new TextCellEditor[] { new TextCellEditor(executeElementsTable),
				new TextCellEditor(executeElementsTable) });
		executeElementsTableViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(final Object element, final String property) {
				return true;
			}

			@Override
			public String getValue(final Object element, final String property) {
				int columnIndex = Arrays.asList(COLUMN_NAMES).indexOf(property);
				ExecuteDataLabelProvider labelProvider = (ExecuteDataLabelProvider) executeElementsTableViewer.getLabelProvider();
				return labelProvider.getColumnText(element, columnIndex);
			}

			@Override
			public void modify(final Object element, final String property, final Object value) {
				int columnIndex = Arrays.asList(COLUMN_NAMES).indexOf(property);
				if (element != null && element instanceof TableItem && value instanceof String) {
					ExecuteItem executeItem = (ExecuteItem) ((TableItem) element).getData();

					switch (columnIndex) {
					case 0:
						ConfigTreeNodeUtilities.setText( executeItem.getModuleName(), ((String) value).trim() );
						break;
					case 1:
						ConfigTreeNodeUtilities.setText( executeItem.getTestcaseName(), ((String) value).trim() );
						break;
					default:
						break;
					}

					executeElementsTableViewer.refresh(executeItem);
					editor.setDirty();
				}
			}
		});

		executeElementsTableViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { ExecuteItemTransfer.getInstance() },
				new ExecuteSectionDragSourceListener(this, executeElementsTableViewer));
		executeElementsTableViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT, new Transfer[] { ExecuteItemTransfer
				.getInstance() }, new ExecuteSectionDropTargetListener(executeElementsTableViewer, editor));
	}

	private void internalRefresh() {
		add.setEnabled(executeSectionHandler != null);
		remove.setEnabled(executeSectionHandler != null);
		executeElementsTable.setEnabled(executeSectionHandler != null);
		executeElementsTableViewer.setInput(executeSectionHandler);
		if (executeSectionHandler == null) {
			totalExecuteElementsLabel.setText("Total: 0");
		} else {
			totalExecuteElementsLabel.setText("Total: " + executeSectionHandler.getExecuteitems().size());
		}
	}

	public void refreshData(final ExecuteSectionHandler executeSectionHandler) {
		this.executeSectionHandler = executeSectionHandler;

		if (executeElementsTableViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
				}
			});
		}
	}

	private void createNewExecuteSection() {
		if (executeSectionHandler == null) {
			return;
		}

		ParserRuleContext sectionRoot = new ParserRuleContext();
		executeSectionHandler.setLastSectionRoot( sectionRoot );
		ParseTree header = new AddedParseTree("\n[EXECUTE]");
		ConfigTreeNodeUtilities.addChild(sectionRoot, header);

		ParserRuleContext root = editor.getParseTreeRoot();
		if (root != null) {
			root.addChild(sectionRoot);
		}
	}

	private ExecuteItem createNewExecuteItem() {
		if (executeSectionHandler == null) {
			return null;
		}

		final ExecuteItem item = new ExecuteSectionHandler.ExecuteItem();
		final ParseTree root = new ParserRuleContext();
		item.setRoot( root );

		final ParseTree moduleName = new AddedParseTree("module_name");
		final ParseTree testcaseName = new AddedParseTree("testcase_name");
		item.setModuleName( moduleName );
		item.setTestcaseName( testcaseName );
		
		ConfigTreeNodeUtilities.addChild( root, ConfigTreeNodeUtilities.createHiddenTokenNode( "\n" ) );
		ConfigTreeNodeUtilities.addChild( root, moduleName );
		ConfigTreeNodeUtilities.addChild( root, new AddedParseTree(".") );
		ConfigTreeNodeUtilities.addChild( root, testcaseName );
		
		return item;
	}

	private void removeExecuteSection() {
		if (executeSectionHandler == null || executeSectionHandler.getLastSectionRoot() == null) {
			return;
		}

		ConfigTreeNodeUtilities.removeChild(editor.getParseTreeRoot(), executeSectionHandler.getLastSectionRoot());
		executeSectionHandler.setLastSectionRoot(null);
	}

	public void removeSelectedExecuteItems() {
		if (executeSectionHandler == null || executeElementsTableViewer == null) {
			return;
		}

		StructuredSelection selection = (StructuredSelection) executeElementsTableViewer.getSelection();
		// remove the selected elements
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			ExecuteItem item = (ExecuteItem) iterator.next();
			if (item != null) {
				ConfigTreeNodeUtilities.removeChild(executeSectionHandler.getLastSectionRoot(), item.getRoot());
				executeSectionHandler.getExecuteitems().remove(item);
			}
		}
	}
}
