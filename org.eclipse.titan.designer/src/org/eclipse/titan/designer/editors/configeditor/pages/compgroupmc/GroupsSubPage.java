/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.compgroupmc;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
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
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.GroupSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.GroupSectionHandler.Group;
import org.eclipse.titan.common.parsers.cfg.indices.GroupSectionHandler.GroupItem;
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
public final class GroupsSubPage {

	private Label totalGroupsLabel;
	private Label totalGroupItemsLabel;
	private Table groupsTable;
	private Table itemsTable;

	private TableViewer groupsTableViewer;
	private TableViewer itemsTableViewer;
	private Button addGroup;
	private Button removeGroup;
	private Button addItem;
	private Button removeItem;

	private ConfigEditor editor;
	private GroupSectionHandler groupSectionHandler;
	private Group selectedGroup;

	public GroupsSubPage(final ConfigEditor editor) {
		this.editor = editor;
	}

	void createGroupsSection(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		client.setLayout(layout);
		client.setLayoutData(new GridData(GridData.FILL_BOTH));

		section.setText("Groups section");
		section.setDescription("Specify the contents of the groups section for this configuration.");
		section.setClient(client);
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(false);
			}
		});
		GridData gd = new GridData(GridData.FILL_BOTH);
		section.setLayoutData(gd);

		createMainGroupsSection(client, form, toolkit);
		createGroupItemsSection(client, form, toolkit);
	}

	void createMainGroupsSection(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		client.setLayout(layout);
		groupsTable = toolkit.createTable(client, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		groupsTable.setEnabled(groupSectionHandler != null);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		gd.widthHint = 100;
		groupsTable.setLayoutData(gd);
		toolkit.paintBordersFor(client);

		groupsTable.setLinesVisible(true);
		groupsTable.setHeaderVisible(true);

		Composite buttons = toolkit.createComposite(client);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		addGroup = toolkit.createButton(buttons, "Add group", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		addGroup.setLayoutData(gd);
		addGroup.setEnabled(groupSectionHandler != null);
		addGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (groupSectionHandler == null) {
					return;
				}

				if (groupSectionHandler.getLastSectionRoot() == null) {
					createNewGroupsSection();
				}

				Group newGroup = createNewGroup();
				if (newGroup == null) {
					return;
				}

				if (groupSectionHandler.getGroups().isEmpty()) {
					groupSectionHandler.getLastSectionRoot().setNextSibling(newGroup.getRoot());
				} else {
					Group group = groupSectionHandler.getGroups().get(groupSectionHandler.getGroups().size() - 1);
					group.getRoot().setNextSibling(newGroup.getRoot());
				}

				groupSectionHandler.getGroups().add(newGroup);

				internalRefresh();
				groupsTable.select(groupSectionHandler.getGroups().size() - 1);
				groupsTable.showSelection();
				selectedGroup = newGroup;
				itemsTableViewer.setInput(newGroup);
				refreshItemsbTableViewer();
				editor.setDirty();
			}
		});

		removeGroup = toolkit.createButton(buttons, "Remove group", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		removeGroup.setLayoutData(gd);
		removeGroup.setEnabled(groupSectionHandler != null);
		removeGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (groupsTableViewer == null || groupSectionHandler == null) {
					return;
				}

				removeSelectedGroups();

				if (groupSectionHandler.getGroups().isEmpty()) {
					removeGroupsSection();
				}

				internalRefresh();
				editor.setDirty();
			}
		});

		totalGroupsLabel = toolkit.createLabel(buttons, "Total: 0");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		totalGroupsLabel.setLayoutData(gd);

		section.setText("Groups");
		section.setDescription("Specify groups of components for this configuration.");
		section.setClient(client);
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(false);
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		section.setLayoutData(gd);

		TableColumn column = new TableColumn(groupsTable, SWT.LEFT, 0);
		column.setText("Group name");
		column.setWidth(150);
		column.setMoveable(false);

		groupsTableViewer = new TableViewer(groupsTable);
		groupsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				Group group = (Group) ((StructuredSelection) event.getSelection()).getFirstElement();
				selectedGroup = group;
				itemsTable.setEnabled(groupSectionHandler != null && selectedGroup != null);
				addItem.setEnabled(groupSectionHandler != null && selectedGroup != null);
				removeItem.setEnabled(groupSectionHandler != null && selectedGroup != null);
				itemsTableViewer.setInput(group);
				refreshItemsbTableViewer();
			}
		});

		groupsTableViewer.setContentProvider(new GroupDataContentProvider());
		groupsTableViewer.setLabelProvider(new GroupDataLabelProvider());
		groupsTableViewer.setInput(groupSectionHandler);
		groupsTableViewer.setColumnProperties(new String[] { "groupName" });
		groupsTableViewer.setCellEditors(new TextCellEditor[] { new TextCellEditor(groupsTable) });
		groupsTableViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(final Object element, final String property) {
				return true;
			}

			@Override
			public String getValue(final Object element, final String property) {
				GroupDataLabelProvider labelProvider = (GroupDataLabelProvider) groupsTableViewer.getLabelProvider();
				return labelProvider.getColumnText(element, 0);
			}

			@Override
			public void modify(final Object element, final String property, final Object value) {
				if (element != null && element instanceof TableItem && value instanceof String) {
					Group group = (Group) ((TableItem) element).getData();
					group.getGroupName().setText((String) value);
					groupsTableViewer.refresh(group);
					editor.setDirty();
				}
			}
		});
		refreshGroupsbTableViewer();
	}

	private void createGroupItemsSection(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		client.setLayout(layout);
		itemsTable = toolkit.createTable(client, SWT.NULL);
		itemsTable.setEnabled(groupSectionHandler != null && selectedGroup != null);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		gd.widthHint = 100;
		itemsTable.setLayoutData(gd);
		toolkit.paintBordersFor(client);

		itemsTable.setLinesVisible(true);
		itemsTable.setHeaderVisible(true);

		Composite buttons = toolkit.createComposite(client);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		addItem = toolkit.createButton(buttons, "Add item", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		addItem.setLayoutData(gd);
		addItem.setEnabled(groupSectionHandler != null && selectedGroup != null);
		addItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (groupSectionHandler == null) {
					return;
				}

				if (selectedGroup == null) {
					return;
				}
				GroupItem newItem;

				CommonHiddenStreamToken hidden = new CommonHiddenStreamToken(", ");

				LocationAST child = selectedGroup.getRoot().getFirstChild();
				while (child.getNextSibling() != null) {
					child = child.getNextSibling();
				}
				child.setHiddenAfter(hidden);

				LocationAST node = new LocationAST("item");
				node.setHiddenBefore(hidden);
				selectedGroup.getRoot().addChild(node);

				newItem = new GroupSectionHandler.GroupItem(node);
				selectedGroup.getGroupItems().add(newItem);

				internalRefresh();
				itemsTable.select(selectedGroup.getGroupItems().size() - 1);
				itemsTable.showSelection();
				editor.setDirty();
			}
		});

		removeItem = toolkit.createButton(buttons, "Remove item", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		removeItem.setLayoutData(gd);
		removeItem.setEnabled(groupSectionHandler != null && selectedGroup != null);
		removeItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (itemsTableViewer == null || groupSectionHandler == null) {
					return;
				}

				removeSelectedItems();

				internalRefresh();
				editor.setDirty();
			}
		});

		totalGroupItemsLabel = toolkit.createLabel(buttons, "");
		if (selectedGroup == null) {
			totalGroupItemsLabel.setText("Total: ");
		} else {
			totalGroupItemsLabel.setText("Total: " + selectedGroup.getGroupItems().size());
		}

		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		totalGroupItemsLabel.setLayoutData(gd);

		section.setText("Group items");
		section.setDescription("Specify items of the selected group.");
		section.setClient(client);
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(false);
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		section.setLayoutData(gd);

		TableColumn column = new TableColumn(itemsTable, SWT.LEFT, 0);
		column.setText("Group item");
		column.setWidth(150);
		column.setMoveable(false);

		itemsTableViewer = new TableViewer(itemsTable);
		itemsTableViewer.setContentProvider(new GroupItemDataContentProvider());
		itemsTableViewer.setLabelProvider(new GroupItemDataLabelProvider());
		itemsTableViewer.setInput(null);
		itemsTableViewer.setColumnProperties(new String[] { "groupItem" });
		itemsTableViewer.setCellEditors(new TextCellEditor[] { new TextCellEditor(itemsTable) });
		itemsTableViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(final Object element, final String property) {
				return true;
			}

			@Override
			public Object getValue(final Object element, final String property) {
				GroupItemDataLabelProvider labelProvider = (GroupItemDataLabelProvider) itemsTableViewer.getLabelProvider();
				return labelProvider.getColumnText(element, 0);
			}

			@Override
			public void modify(final Object element, final String property, final Object value) {
				if (element != null && element instanceof TableItem && value instanceof String) {
					GroupItem item = (GroupItem) ((TableItem) element).getData();
					item.getItem().setText((String) value);
					itemsTableViewer.refresh(item);
					editor.setDirty();
				}
			}
		});
	}

	public void refreshGroupsbTableViewer() {
		groupsTableViewer.refresh();
		if (groupSectionHandler != null) {
			totalGroupsLabel.setText("Total: " + groupSectionHandler.getGroups().size());
		}
	}

	public void refreshItemsbTableViewer() {
		itemsTableViewer.refresh();
		if (selectedGroup == null) {
			totalGroupItemsLabel.setText("Total: ");
		} else {
			totalGroupItemsLabel.setText("Total: " + selectedGroup.getGroupItems().size());
		}
	}

	private void internalRefresh() {
		/*
		 * StructuredSelection selection =
		 * (StructuredSelection)groupsTableViewer.getSelection();
		 * Iterator<?> iterator = selection.iterator();
		 * if(iterator.hasNext()){ valueChanged = true; }else{
		 * parameterValueText.setText(""); }
		 */

		addGroup.setEnabled(groupSectionHandler != null);
		removeGroup.setEnabled(groupSectionHandler != null);
		addItem.setEnabled(groupSectionHandler != null && selectedGroup != null);
		removeItem.setEnabled(groupSectionHandler != null && selectedGroup != null);
		groupsTable.setEnabled(groupSectionHandler != null);
		groupsTableViewer.setInput(groupSectionHandler);
		itemsTable.setEnabled(groupSectionHandler != null && selectedGroup != null);
		if (selectedGroup != null) {
			itemsTableViewer.setInput(selectedGroup);
		}
		if (groupSectionHandler == null) {
			totalGroupsLabel.setText("Total: 0");
		} else {
			totalGroupsLabel.setText("Total: " + groupSectionHandler.getGroups().size());
		}

	}

	public void refreshData(final GroupSectionHandler groupSectionHandler) {

		this.groupSectionHandler = groupSectionHandler;
		if (groupsTableViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
				}
			});
		}
	}

	private void createNewGroupsSection() {
		if (groupSectionHandler == null) {
			return;
		}

		groupSectionHandler.setLastSectionRoot(new LocationAST("[GROUPS]"));
		groupSectionHandler.getLastSectionRoot().setHiddenBefore(new CommonHiddenStreamToken("\n"));
		LocationAST sectionRoot = new LocationAST("");
		sectionRoot.setFirstChild(groupSectionHandler.getLastSectionRoot());

		LocationAST root = editor.getParseTreeRoot();
		if (root != null) {
			root.addChild(sectionRoot);
		}
	}

	private Group createNewGroup() {
		if (groupSectionHandler == null) {
			return null;
		}

		Group newGroup = new GroupSectionHandler.Group();
		newGroup.setRoot(new LocationAST(""));

		LocationAST node;
		newGroup.setGroupName(new LocationAST("group_name"));
		newGroup.getGroupName().setHiddenBefore(new CommonHiddenStreamToken("\n"));
		newGroup.getRoot().setFirstChild(newGroup.getGroupName());
		node = new LocationAST(" := ");
		newGroup.getGroupName().setNextSibling(node);
		newGroup.setGroupItems(new ArrayList<GroupItem>());
		LocationAST item = new LocationAST("item");
		node.setNextSibling(item);
		newGroup.getGroupItems().add(new GroupSectionHandler.GroupItem(item));

		return newGroup;
	}

	private void removeGroupsSection() {
		if (groupSectionHandler == null || groupSectionHandler.getLastSectionRoot() == null) {
			return;
		}

		ConfigTreeNodeUtilities.removeFromChain(editor.getParseTreeRoot().getFirstChild(), groupSectionHandler.getLastSectionRoot()
				.getParent());
		groupSectionHandler.setLastSectionRoot(null);
	}

	private void removeSelectedGroups() {
		if (groupsTableViewer == null || groupSectionHandler == null) {
			return;
		}

		StructuredSelection selection = (StructuredSelection) groupsTableViewer.getSelection();
		// remove the selected elements
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			Group group = (Group) iterator.next();
			if (group != null) {
				ConfigTreeNodeUtilities.removeFromChain(groupSectionHandler.getLastSectionRoot(), group.getRoot());
				groupSectionHandler.getGroups().remove(group);
			}
		}
	}

	private void removeSelectedItems() {
		if (itemsTableViewer == null || groupSectionHandler == null) {
			return;
		}

		StructuredSelection selection = (StructuredSelection) itemsTableViewer.getSelection();
		// remove the selected elements
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			GroupItem item = (GroupItem) iterator.next();
			if (item != null) {
				int index = selectedGroup.getGroupItems().indexOf(item);

				if (index == 0) {
					// if this is the first
					LocationAST next;
					if (selectedGroup.getGroupItems().size() == 1) {
						// if it is the only one
						// DO NOTHING
						// Each group must have at least one item.
						return;
					}

					next = item.getItem().getNextSibling().getNextSibling();
					if (next != null) {
						LocationAST assignment = selectedGroup.getRoot().getFirstChild().getNextSibling();
						assignment.setNextSibling(next);
						CommonHiddenStreamToken hidden = new CommonHiddenStreamToken(" ");
						assignment.setHiddenAfter(hidden);
					}
				} else {
					LocationAST previous = selectedGroup.getGroupItems().get(index - 1).getItem();
					if (index + 1 < selectedGroup.getGroupItems().size()) {
						// if this is not the last
						LocationAST next = selectedGroup.getGroupItems().get(index + 1).getItem();
						previous.setNextSibling(next);
						CommonHiddenStreamToken hidden = new CommonHiddenStreamToken(", ");
						previous.setHiddenAfter(hidden);
						next.setHiddenBefore(hidden);

					} else {
						previous.setNextSibling(null);
					}

				}

				selectedGroup.getGroupItems().remove(item);
			}
		}
	}
}
