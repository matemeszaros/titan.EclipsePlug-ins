/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.compgroupmc;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.titan.common.parsers.CommonHiddenStreamToken;
import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.cfg.CfgLexer;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.ComponentSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ComponentSectionHandler.Component;
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
 * */
public final class ComponentsSubPage {

	private Label totalComponentsLabel;
	private Table componentsTable;
	private TableViewer componentsTableViewer;
	private Button add;
	private Button remove;

	private static final String[] COLUMN_NAMES = new String[] { "componentName", "hostName" };

	private ConfigEditor editor;
	private ComponentSectionHandler componentsSectionHandler;

	public ComponentsSubPage(final ConfigEditor editor) {
		this.editor = editor;
	}

	void createComponentsSection(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		client.setLayout(layout);
		toolkit.paintBordersFor(client);
		componentsTable = toolkit.createTable(client, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		componentsTable.setEnabled(componentsSectionHandler != null);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 100;
		gd.heightHint = 200;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		componentsTable.setLayoutData(gd);
		componentsTable.setLinesVisible(true);
		componentsTable.setHeaderVisible(true);

		TableColumn column = new TableColumn(componentsTable, SWT.LEFT, 0);
		column.setText("Component name");
		column.setWidth(130);
		column.setMoveable(false);

		column = new TableColumn(componentsTable, SWT.LEFT, 1);
		column.setText("Host name");
		column.setWidth(100);
		column.setMoveable(false);

		componentsTableViewer = new TableViewer(componentsTable);
		componentsTableViewer.setContentProvider(new ComponentsDataContentProvider());
		componentsTableViewer.setLabelProvider(new ComponentsDataLabelProvider());
		componentsTableViewer.setInput(componentsSectionHandler);
		componentsTableViewer.setColumnProperties(COLUMN_NAMES);
		final TextCellEditor[] cellEditors = new TextCellEditor[] { new TextCellEditor(componentsTable), new TextCellEditor(componentsTable) };
		componentsTableViewer.setCellEditors(cellEditors);
		componentsTableViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(final Object element, final String property) {
				return true;
			}

			@Override
			public String getValue(final Object element, final String property) {
				int columnIndex = Arrays.asList(COLUMN_NAMES).indexOf(property);
				ComponentsDataLabelProvider labelProvider = (ComponentsDataLabelProvider) componentsTableViewer.getLabelProvider();
				return labelProvider.getColumnText(element, columnIndex);
			}

			@Override
			public void modify(final Object element, final String property, final Object value) {
				int columnIndex = Arrays.asList(COLUMN_NAMES).indexOf(property);
				if (element != null && element instanceof TableItem && value instanceof String) {
					Component component = (Component) ((TableItem) element).getData();

					switch (columnIndex) {
					case 0:
						component.getComponentName().setText(((String) value).trim());
						break;
					case 1:
						component.getHostName().setText(((String) value).trim());
						break;
					default:
						break;
					}
					componentsTableViewer.refresh(component);
					editor.setDirty();
				}
			}
		});

		Composite buttons = toolkit.createComposite(client);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL));
		add = toolkit.createButton(buttons, "Add...", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		add.setLayoutData(gd);
		add.setEnabled(componentsSectionHandler != null);
		add.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (componentsSectionHandler == null) {
					return;
				}

				if (componentsSectionHandler.getLastSectionRoot() == null) {
					createNewComponentsSection();
				}

				Component newComponent = createNewComponent();
				if (newComponent == null) {
					return;
				}

				if (componentsSectionHandler.getComponents().isEmpty()) {
					componentsSectionHandler.getLastSectionRoot().setNextSibling(newComponent.getRoot());
				} else {
					final int size = componentsSectionHandler.getComponents().size();
					Component component = componentsSectionHandler.getComponents().get(size - 1);
					component.getRoot().setNextSibling(newComponent.getRoot());
				}

				componentsSectionHandler.getComponents().add(newComponent);

				internalRefresh();
				componentsTable.select(componentsSectionHandler.getComponents().size() - 1);
				componentsTable.showSelection();
				editor.setDirty();
			}

		});

		remove = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		remove.setLayoutData(gd);
		remove.setEnabled(componentsSectionHandler != null);
		remove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (componentsTableViewer == null || componentsSectionHandler == null) {
					return;
				}

				removeSelectedComponents();

				if (componentsSectionHandler.getComponents().isEmpty()) {
					removeComponentsSection();
				}

				internalRefresh();
				editor.setDirty();
			}

		});

		totalComponentsLabel = toolkit.createLabel(buttons, "Total: 0");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		totalComponentsLabel.setLayoutData(gd);

		section.setText("Components");
		section.setDescription("Specify the list of remote components for this configuration.");
		section.setClient(client);
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(false);
			}
		});

		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		section.setLayoutData(gd);

		final ComponentItemTransfer instance = ComponentItemTransfer.getInstance();
		componentsTableViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { instance },
				new ComponentSectionDragSourceListener(this, componentsTableViewer));
		componentsTableViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT, new Transfer[] { instance },
				new ComponentSectionDropTargetListener(componentsTableViewer, editor));

		internalRefresh();
	}

	private void internalRefresh() {
		add.setEnabled(componentsSectionHandler != null);
		remove.setEnabled(componentsSectionHandler != null);
		componentsTable.setEnabled(componentsSectionHandler != null);
		componentsTableViewer.setInput(componentsSectionHandler);
		if (componentsSectionHandler != null) {
			totalComponentsLabel.setText("Total: " + componentsSectionHandler.getComponents().size());
		}

	}

	public void refreshData(final ComponentSectionHandler componentsSectionHandler) {
		this.componentsSectionHandler = componentsSectionHandler;

		if (componentsTableViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
				}
			});
		}
	}

	private void createNewComponentsSection() {
		if (componentsSectionHandler == null) {
			return;
		}

		componentsSectionHandler.setLastSectionRoot(new LocationAST("[COMPONENTS]"));
		componentsSectionHandler.getLastSectionRoot().setHiddenBefore(new CommonHiddenStreamToken("\n"));
		LocationAST sectionRoot = new LocationAST("");
		sectionRoot.setFirstChild(componentsSectionHandler.getLastSectionRoot());

		LocationAST root = editor.getParseTreeRoot();
		if (root != null) {
			root.addChild(sectionRoot);
		}
	}

	private Component createNewComponent() {
		if (componentsSectionHandler == null) {
			return null;
		}

		Component newcomponent = new Component();
		newcomponent.setRoot(new LocationAST(""));

		LocationAST node;
		newcomponent.setComponentName(new LocationAST("component_name"));
		newcomponent.getComponentName().setHiddenBefore(new CommonHiddenStreamToken(CfgLexer.WS, "\n"));
		newcomponent.getRoot().setFirstChild(newcomponent.getComponentName());
		node = new LocationAST(" := ");
		newcomponent.getComponentName().setNextSibling(node);
		newcomponent.setHostName(new LocationAST("host_name"));
		newcomponent.getHostName().setHiddenAfter(new CommonHiddenStreamToken(CfgLexer.WS, "\n"));
		node.setNextSibling(newcomponent.getHostName());

		return newcomponent;
	}

	private void removeComponentsSection() {
		if (componentsSectionHandler == null || componentsSectionHandler.getLastSectionRoot() == null) {
			return;
		}

		final LocationAST parent = componentsSectionHandler.getLastSectionRoot().getParent();
		ConfigTreeNodeUtilities.removeFromChain(editor.getParseTreeRoot().getFirstChild(), parent);
		componentsSectionHandler.setLastSectionRoot(null);
	}

	public void removeSelectedComponents() {
		if (componentsTableViewer == null || componentsSectionHandler == null) {
			return;
		}

		StructuredSelection selection = (StructuredSelection) componentsTableViewer.getSelection();
		// remove the selected elements
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			Component component = (Component) iterator.next();
			if (component != null) {
				ConfigTreeNodeUtilities.removeFromChain(componentsSectionHandler.getLastSectionRoot(), component.getRoot());
				componentsSectionHandler.getComponents().remove(component);
			}
		}
	}
}
