/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.modulepar;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.parsers.CommonHiddenStreamToken;
import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.cfg.CfgLexer;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.ModuleParameterSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ModuleParameterSectionHandler.ModuleParameter;
import org.eclipse.titan.designer.editors.configeditor.ConfigEditor;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kristof Szabados
 * */
public final class ModuleParameterSectionPage extends FormPage {
	private Label totalModuleParametersLabel;

	private Table moduleParametersTable;
	private TableViewer moduleParametersTableViewer;
	private Button add;
	private Button remove;

	private static final String[] COLUMN_NAMES = new String[] { "moduleName", "parameterName" };

	private ScrolledForm form;
	private ConfigEditor editor;
	private Text parameterValueText;
	private boolean valueChanged = false;

	private ModuleParameterSectionHandler moduleParametersHandler = null;

	public ModuleParameterSectionPage(final ConfigEditor editor) {
		super(editor, "Moduleparameters_section_page", "Module parameters");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText("Module parameters section");
		form.setBackgroundImage(ImageCache.getImage("form_banner.gif"));

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		form.getBody().setLayout(layout);

		createModuleParameterTable(form.getBody(), form, toolkit);
		createDetailsPart(form.getBody(), form, toolkit);

		internalRefresh();
		setErrorMessage();
	}

	private void createModuleParameterTable(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		client.setLayout(layout);

		toolkit.paintBordersFor(client);

		moduleParametersTable = toolkit.createTable(client, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		moduleParametersTable.setEnabled(moduleParametersHandler != null);

		TableColumn column = new TableColumn(moduleParametersTable, SWT.LEFT, 0);
		column.setText("Module name");
		column.setWidth(150);
		column.setMoveable(false);

		column = new TableColumn(moduleParametersTable, SWT.LEFT, 1);
		column.setText("Module parameter name");
		column.setMoveable(false);
		column.setWidth(200);

		moduleParametersTable.setHeaderVisible(true);
		moduleParametersTable.setLinesVisible(true);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 100;
		gd.heightHint = 200;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		moduleParametersTable.setLayoutData(gd);

		Composite buttons = toolkit.createComposite(client);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL));
		add = toolkit.createButton(buttons, "Add...", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		add.setLayoutData(gd);
		add.setEnabled(moduleParametersHandler != null);
		add.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (moduleParametersHandler == null) {
					return;
				}

				if (moduleParametersHandler.getLastSectionRoot() == null) {
					createNewModuleParameterSection();
				}

				ModuleParameter newModuleParameter = createNewParameter();
				if (newModuleParameter == null) {
					return;
				}

				if (moduleParametersHandler.getModuleParameters().isEmpty()) {
					moduleParametersHandler.getLastSectionRoot().setNextSibling(newModuleParameter.getRoot());
				} else {
					ModuleParameter moduleParameter = moduleParametersHandler.getModuleParameters().get(
							moduleParametersHandler.getModuleParameters().size() - 1);
					moduleParameter.getRoot().setNextSibling(newModuleParameter.getRoot());
				}

				moduleParametersHandler.getModuleParameters().add(newModuleParameter);

				internalRefresh();
				moduleParametersTableViewer.setSelection(new StructuredSelection(newModuleParameter));
				parameterValueText.setText(newModuleParameter.getValue().getText());

				editor.setDirty();
			}

		});

		remove = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		remove.setLayoutData(gd);
		remove.setEnabled(moduleParametersHandler != null);
		remove.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (moduleParametersTableViewer == null || moduleParametersHandler == null) {
					return;
				}

				removeSelectedParameters();

				if (moduleParametersHandler.getModuleParameters().isEmpty()) {
					removeModuleParameterSection();
				}

				/*
				 * ASTFrame frame = new
				 * ASTFrame("Tree structure"
				 * ,editor.parseTreeRoot);
				 * frame.setVisible(true);
				 */
				internalRefresh();
				editor.setDirty();
			}

		});

		totalModuleParametersLabel = toolkit.createLabel(buttons, "Total: 0");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		totalModuleParametersLabel.setLayoutData(gd);

		section.setText("Module parameters");
		section.setDescription("Specify the list of module parameters for this configuration.");
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

		moduleParametersTableViewer = new TableViewer(moduleParametersTable);
		moduleParametersTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection.size() != 1) {
					parameterValueText.setEnabled(false);
					return;
				}

				ModuleParameter moduleParameter = (ModuleParameter) selection.getFirstElement();
				if (moduleParameter != null) {
					if (moduleParameter.getValue() != null) {
						if (moduleParameter.getValue().getText().length() == 0) {
							String temp = ConfigTreeNodeUtilities.toString(moduleParameter.getValue());
							moduleParameter.getValue().removeChildren();
							moduleParameter.getValue().setText(temp);
						}

						parameterValueText.setText(moduleParameter.getValue().getText());
					}

					valueChanged = true;
				}
				parameterValueText.setEnabled(moduleParametersHandler != null && moduleParameter != null);
			}
		});

		moduleParametersTableViewer.setContentProvider(new ModuleParameterDataContentProvider());
		moduleParametersTableViewer.setLabelProvider(new ModuleParameterDataLabelProvider());
		moduleParametersTableViewer.setInput(moduleParametersHandler);
		moduleParametersTableViewer.setColumnProperties(COLUMN_NAMES);
		moduleParametersTableViewer.setCellEditors(new TextCellEditor[] { new TextCellEditor(moduleParametersTable),
				new TextCellEditor(moduleParametersTable) });
		moduleParametersTableViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(final Object element, final String property) {
				return true;
			}

			@Override
			public String getValue(final Object element, final String property) {
				int columnIndex = Arrays.asList(COLUMN_NAMES).indexOf(property);
				ModuleParameterDataLabelProvider labelProvider = (ModuleParameterDataLabelProvider) moduleParametersTableViewer
						.getLabelProvider();
				return labelProvider.getColumnText(element, columnIndex);
			}

			@Override
			public void modify(final Object element, final String property, final Object value) {
				int columnIndex = Arrays.asList(COLUMN_NAMES).indexOf(property);
				if (element != null && element instanceof TableItem && value instanceof String) {
					ModuleParameter moduleParameter = (ModuleParameter) ((TableItem) element).getData();

					switch (columnIndex) {
					case 0:
						// MODULE_NAME
						if (moduleParameter.getModuleName() != null) {
							String newValue = ((String) value).trim();
							moduleParameter.getModuleName().setText(((String) value).trim());
							if ("".equals(newValue)) {
								moduleParameter.getModuleName().getNextSibling().setText("");
							}
						} else {
							String newValue = ((String) value).trim();
							if (newValue != null && !"".equals(newValue)) {

								moduleParameter.setModuleName(new LocationAST(newValue));
								// moduleParameter.moduleName.setHiddenBefore(new
								// CommonHiddenStreamToken(cfgBaseLexerTokenTypes.WS,
								// "\n"));

								moduleParameter.getRoot().setFirstChild(moduleParameter.getModuleName());

								LocationAST node = new LocationAST(".");
								moduleParameter.getModuleName().setNextSibling(node);
								ConfigTreeNodeUtilities.moveHiddenBefore2HiddenBefore(
										moduleParameter.getParameterName(), moduleParameter.getModuleName());

								node.setNextSibling(moduleParameter.getParameterName());
							}
						}
						break;
					case 1:
						// PARAMETER_NAME
						moduleParameter.getParameterName().setText(((String) value).trim());
						break;
					default:
						break;
					}

					moduleParametersTableViewer.refresh(moduleParameter);
					editor.setDirty();
				}
			}
		});

		moduleParametersTableViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { ModuleParameterTransfer.getInstance() },
				new ModuleParameterSectionDragSourceListener(this, moduleParametersTableViewer));
		moduleParametersTableViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { ModuleParameterTransfer.getInstance() },
				new ModuleParameterSectionDropTargetListener(moduleParametersTableViewer, editor));
	}

	private void createDetailsPart(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		client.setLayout(layout);

		parameterValueText = toolkit.createText(client, "", SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		parameterValueText.setLayoutData(new GridData(GridData.FILL_BOTH));
		parameterValueText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				StructuredSelection selection = (StructuredSelection) moduleParametersTableViewer.getSelection();
				Iterator<?> iterator = selection.iterator();
				if (!iterator.hasNext()) {
					return;
				}

				ModuleParameter moduleParameter = (ModuleParameter) iterator.next();
				moduleParameter.getValue().setText(parameterValueText.getText());

				if (valueChanged) {
					valueChanged = false;
					return;
				}

				editor.setDirty();
			}
		});
		parameterValueText.setEnabled(moduleParametersHandler != null);

		section.setText("Module parameter value");
		section.setDescription("Specify the concrete value for the actually selected module parameter.");
		section.setClient(client);
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(false);
			}
		});
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessVerticalSpace = true;
		section.setLayoutData(gd);
	}

	private void internalRefresh() {
		StructuredSelection selection = (StructuredSelection) moduleParametersTableViewer.getSelection();
		Iterator<?> iterator = selection.iterator();
		if (iterator.hasNext()) {
			valueChanged = true;
		} else {
			parameterValueText.setText("");
		}

		add.setEnabled(moduleParametersHandler != null);
		remove.setEnabled(moduleParametersHandler != null);
		parameterValueText.setEnabled(moduleParametersHandler != null && selection.size() == 1);
		moduleParametersTableViewer.setInput(moduleParametersHandler);
		moduleParametersTable.setEnabled(moduleParametersHandler != null);
		if (moduleParametersHandler != null) {
			totalModuleParametersLabel.setText("Total: " + moduleParametersHandler.getModuleParameters().size());
		}
	}

	public void refreshData(final ModuleParameterSectionHandler moduleParametersHandler) {
		this.moduleParametersHandler = moduleParametersHandler;

		if (moduleParametersTableViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
				}
			});
		}
	}

	private void createNewModuleParameterSection() {
		if (moduleParametersHandler == null) {
			return;
		}

		moduleParametersHandler.setLastSectionRoot(new LocationAST("[MODULE_PARAMETERS]"));
		moduleParametersHandler.getLastSectionRoot().setHiddenBefore(new CommonHiddenStreamToken("\n"));
		LocationAST sectionRoot = new LocationAST("");
		sectionRoot.setFirstChild(moduleParametersHandler.getLastSectionRoot());

		LocationAST root = editor.getParseTreeRoot();
		if (root != null) {
			root.addChild(sectionRoot);
		}
	}

	private ModuleParameter createNewParameter() {
		if (moduleParametersHandler == null) {
			return null;
		}

		ModuleParameter newModuleParameter = new ModuleParameterSectionHandler.ModuleParameter();
		newModuleParameter.setRoot(new LocationAST(""));

		LocationAST node;
		newModuleParameter.setModuleName(new LocationAST("module_name"));
		newModuleParameter.getModuleName().setHiddenBefore(new CommonHiddenStreamToken(CfgLexer.WS, "\n"));
		newModuleParameter.getRoot().setFirstChild(newModuleParameter.getModuleName());
		node = new LocationAST(".");
		newModuleParameter.getModuleName().setNextSibling(node);
		newModuleParameter.setParameterName(new LocationAST("parameter_name"));
		node.setNextSibling(newModuleParameter.getParameterName());
		node = new LocationAST(" := ");
		newModuleParameter.getParameterName().setNextSibling(node);
		newModuleParameter.setValue(new LocationAST("value"));
		node.setNextSibling(newModuleParameter.getValue());

		return newModuleParameter;
	}

	private void removeModuleParameterSection() {
		if (moduleParametersHandler == null || moduleParametersHandler.getLastSectionRoot() == null) {
			return;
		}

		ConfigTreeNodeUtilities.removeFromChain(editor.getParseTreeRoot().getFirstChild(), moduleParametersHandler.getLastSectionRoot()
				.getParent());
		moduleParametersHandler.setLastSectionRoot(null);
	}

	public void removeSelectedParameters() {
		if (moduleParametersTableViewer == null || moduleParametersHandler == null) {
			return;
		}

		StructuredSelection selection = (StructuredSelection) moduleParametersTableViewer.getSelection();
		Iterator<?> iterator = selection.iterator();

		// remove the selected elements
		for (; iterator.hasNext();) {
			ModuleParameter moduleParameter = (ModuleParameter) iterator.next();
			if (moduleParameter != null) {
				ConfigTreeNodeUtilities.removeFromChain(moduleParametersHandler.getLastSectionRoot(), moduleParameter.getRoot());
				moduleParametersHandler.getModuleParameters().remove(moduleParameter);
			}
		}

		moduleParametersTableViewer.setSelection(null);
	}

	@Override
	public void setActive(final boolean active) {
		setErrorMessage();

		if (moduleParametersTableViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
				}
			});
		}

		super.setActive(active);
	}

	public void setErrorMessage() {
		if (form != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (editor.getErrorMessage() == null) {
						form.getForm().setMessage(null, IMessageProvider.NONE);
					} else {
						form.getForm().setMessage(editor.getErrorMessage(), IMessageProvider.ERROR);
					}
					form.getForm().getHead().layout();
					form.getForm().getHead().redraw();
				}
			});
		}
	}
}
