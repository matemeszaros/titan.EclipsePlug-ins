/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.include;

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
import org.eclipse.titan.common.parsers.AddedParseTree;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.IncludeSectionHandler;
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
public final class IncludeSubPage {

	private Label totalIncludeElementsLabel;
	private Table includeElementsTable;
	private TableViewer includeElementsTableViewer;

	private ConfigEditor editor;
	private IncludeSectionHandler includeSectionHandler;
	private Button add;
	private Button remove;

	public IncludeSubPage(final ConfigEditor editor) {
		this.editor = editor;
	}

	void createIncludeSection(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		client.setLayout(layout);

		toolkit.paintBordersFor(client);

		includeElementsTable = toolkit.createTable(client, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		includeElementsTable.setEnabled(includeSectionHandler != null);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 100;
		gd.heightHint = 200;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		includeElementsTable.setLayoutData(gd);

		TableColumn column = new TableColumn(includeElementsTable, SWT.LEFT, 0);
		column.setText("File name");
		column.setMoveable(false);
		column.setWidth(100);

		includeElementsTable.setLinesVisible(true);
		includeElementsTable.setHeaderVisible(true);

		Composite buttons = toolkit.createComposite(client);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL));
		add = toolkit.createButton(buttons, "Add...", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		add.setEnabled(includeSectionHandler != null);
		add.setLayoutData(gd);
		add.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (includeSectionHandler == null) {
					return;
				}

				if (includeSectionHandler.getLastSectionRoot() == null) {
					createNewIncludeSection();
				}

				ParseTree newItem = createNewIncludeItem();
				if (newItem == null) {
					return;
				}

				if (includeSectionHandler.getFiles().isEmpty()) {
					ParseTree root = includeSectionHandler.getLastSectionRoot();
					//TODO: remove
					//ConfigTreeNodeUtilities.addChild(root.getParent(), newItem);
					ConfigTreeNodeUtilities.addChild(root, newItem);
				} else {
					ParseTree item = includeSectionHandler.getFiles().get(includeSectionHandler.getFiles().size() - 1);
					//TODO: remove
					//ConfigTreeNodeUtilities.addChild(item.getParent(), newItem);
					ConfigTreeNodeUtilities.addChild(item, newItem);
				}

				includeSectionHandler.getFiles().add(newItem);

				internalRefresh();
				includeElementsTable.select(includeSectionHandler.getFiles().size() - 1);
				includeElementsTable.showSelection();
				editor.setDirty();
			}
		});

		remove = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		remove.setLayoutData(gd);
		remove.setEnabled(includeSectionHandler != null);
		remove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (includeElementsTableViewer == null || includeSectionHandler == null) {
					return;
				}

				removeSelectedIncludeItems();

				if (includeSectionHandler.getFiles().isEmpty()) {
					removeIncludeSection();
				}

				internalRefresh();
				editor.setDirty();
			}

		});

		totalIncludeElementsLabel = toolkit.createLabel(buttons, "Total: 0");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		totalIncludeElementsLabel.setLayoutData(gd);

		section.setText("Included configurations");
		section.setDescription("Specify the list of included configuration files for this configuration.");
		section.setClient(client);
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(false);
			}
		});

		gd = new GridData(GridData.FILL_HORIZONTAL);
		section.setLayoutData(gd);

		includeElementsTableViewer = new TableViewer(includeElementsTable);
		includeElementsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				// not needed this time
			}
		});

		includeElementsTableViewer.setContentProvider(new IncludeDataContentProvider());
		includeElementsTableViewer.setLabelProvider(new IncludeDataLabelProvider());
		includeElementsTableViewer.setInput(includeSectionHandler);
		includeElementsTableViewer.setColumnProperties(new String[] { "file_name" });
		includeElementsTableViewer.setCellEditors(new TextCellEditor[] { new TextCellEditor(includeElementsTable) });
		includeElementsTableViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(final Object element, final String property) {
				return true;
			}

			@Override
			public String getValue(final Object element, final String property) {
				IncludeDataLabelProvider labelProvider = (IncludeDataLabelProvider) includeElementsTableViewer.getLabelProvider();
				return labelProvider.getColumnText(element, 0);
			}

			@Override
			public void modify(final Object element, final String property, final Object value) {
				if (element != null && element instanceof TableItem && value instanceof String) {
					ParseTree item = (ParseTree) ((TableItem) element).getData();
					ConfigTreeNodeUtilities.setText(item, (String) value);
					includeElementsTableViewer.refresh(item);
					editor.setDirty();
				}
			}
		});

		includeElementsTableViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { IncludeItemTransfer.getInstance() },
				new IncludeSectionDragSourceListener(this, includeElementsTableViewer));
		includeElementsTableViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT, new Transfer[] { IncludeItemTransfer
				.getInstance() }, new IncludeSectionDropTargetListener(includeElementsTableViewer, editor));

		internalRefresh();
	}

	private void internalRefresh() {
		add.setEnabled(includeSectionHandler != null);
		remove.setEnabled(includeSectionHandler != null);
		includeElementsTable.setEnabled(includeSectionHandler != null);
		includeElementsTableViewer.setInput(includeSectionHandler);
		if (includeSectionHandler != null) {
			totalIncludeElementsLabel.setText("Total: " + includeSectionHandler.getFiles().size());
		}
	}

	public void refreshData(final IncludeSectionHandler includeSectionHandler) {
		this.includeSectionHandler = includeSectionHandler;

		if (includeElementsTableViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
				}
			});
		}
	}

	private void createNewIncludeSection() {
		if (includeSectionHandler == null) {
			return;
		}

		includeSectionHandler.setLastSectionRoot(new AddedParseTree("\n[INCLUDE]"));
		ParserRuleContext sectionRoot = new ParserRuleContext();
		ConfigTreeNodeUtilities.addChild(sectionRoot, includeSectionHandler.getLastSectionRoot());

		ParserRuleContext root = editor.getParseTreeRoot().getRule();
		if (root != null) {
			root.addChild(sectionRoot);
		}
	}

	private ParseTree createNewIncludeItem() {
		if (includeSectionHandler == null) {
			return null;
		}

		ParseTree item = new AddedParseTree("\n\"included_file\"");
		return item;
	}

	private void removeIncludeSection() {
		if (includeSectionHandler == null || includeSectionHandler.getLastSectionRoot() == null) {
			return;
		}

		ConfigTreeNodeUtilities.removeChild(editor.getParseTreeRoot().getFirstChild().getRule(), includeSectionHandler.getLastSectionRoot().getParent());
		includeSectionHandler.setLastSectionRoot((ParserRuleContext)null);
	}

	public void removeSelectedIncludeItems() {
		if (includeElementsTableViewer == null || includeSectionHandler == null) {
			return;
		}

		StructuredSelection selection = (StructuredSelection) includeElementsTableViewer.getSelection();
		// remove the selected elements
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			ParseTree item = (ParseTree) iterator.next();
			if (item != null) {
				ConfigTreeNodeUtilities.removeChild(includeSectionHandler.getLastSectionRoot(), item);
				includeSectionHandler.getFiles().remove(item);
			}
		}
	}
}
