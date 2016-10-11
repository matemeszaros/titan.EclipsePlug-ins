/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.testportpar;

import java.util.Arrays;
import java.util.Iterator;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
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
import org.eclipse.titan.common.parsers.AddedParseTree;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.TestportParameterSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.TestportParameterSectionHandler.TestportParameter;
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
 * @author Arpad Lovassy
 */
public final class TestportParametersSectionPage extends FormPage {

	private Label totalTestportParametersLabel;
	private Table testportParametersTable;
	private TableViewer testportParametersTableViewer;
	private Button add;
	private Button remove;

	private String[] columnNames = new String[] { "componentName", "testportName", "parameterName" };

	private ScrolledForm form;
	private ConfigEditor editor;
	private Text parameterValueText;

	private boolean valueChanged = false;

	private TestportParameterSectionHandler testportParametersHandler = null;

	public TestportParametersSectionPage(final ConfigEditor editor) {
		super(editor, "TestportParameters_section_page", "Testport parameters");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText("Testport parameters section");
		form.setBackgroundImage(ImageCache.getImage("form_banner.gif"));

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		form.getBody().setLayout(layout);

		createTestportParameterTable(form.getBody(), form, toolkit);
		createDetailsPart(form.getBody(), form, toolkit);
		internalRefresh();

		setErrorMessage();
	}

	private void createTestportParameterTable(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		client.setLayout(layout);

		toolkit.paintBordersFor(client);

		testportParametersTable = toolkit.createTable(client, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		testportParametersTable.setEnabled(testportParametersHandler != null);

		TableColumn column = new TableColumn(testportParametersTable, SWT.LEFT, 0);
		column.setText("Parameter name");
		column.setWidth(150);
		column.setMoveable(false);

		column = new TableColumn(testportParametersTable, SWT.LEFT, 0);
		column.setText("Testport name");
		column.setMoveable(false);
		column.setWidth(130);

		column = new TableColumn(testportParametersTable, SWT.LEFT, 0);
		column.setText("Component name");
		column.setMoveable(false);
		column.setWidth(130);

		testportParametersTable.setHeaderVisible(true);
		testportParametersTable.setLinesVisible(true);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 100;
		gd.heightHint = 200;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		testportParametersTable.setLayoutData(gd);

		Composite buttons = toolkit.createComposite(client);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL));
		add = toolkit.createButton(buttons, "Add...", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		add.setLayoutData(gd);
		add.setEnabled(testportParametersHandler != null);
		add.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				//Do nothing
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (testportParametersHandler == null) {
					return;
				}

				if (testportParametersHandler.getLastSectionRoot() == null) {
					createNewTestportParameterSection();
				}

				TestportParameter newTestportParameter = createNewParameter();
				if (newTestportParameter == null) {
					return;
				}

				ConfigTreeNodeUtilities.addChild(testportParametersHandler.getLastSectionRoot(), newTestportParameter.getRoot());

				testportParametersHandler.getTestportParameters().add(newTestportParameter);

				internalRefresh();
				testportParametersTableViewer.setSelection(new StructuredSelection(newTestportParameter));
				parameterValueText.setText(newTestportParameter.getValue().getText());
				editor.setDirty();
			}

		});

		remove = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		remove.setLayoutData(gd);
		remove.setEnabled(testportParametersHandler != null);
		remove.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				//Do nothing
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (testportParametersTableViewer == null || testportParametersHandler == null) {
					return;
				}

				removeSelectedParameters();

				if (testportParametersHandler.getTestportParameters().isEmpty()) {
					removeTestportParameterSection();
				}

				internalRefresh();
				editor.setDirty();
			}

		});

		totalTestportParametersLabel = toolkit.createLabel(buttons, "Total: 0");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		totalTestportParametersLabel.setLayoutData(gd);

		section.setText("Testport parameters");
		section.setDescription("Specify the list of testport parameters for this configuration.");
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

		testportParametersTableViewer = new TableViewer(testportParametersTable);
		testportParametersTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection.size() != 1) {
					parameterValueText.setEnabled(false);
					return;
				}

				TestportParameter testportParameter = (TestportParameter) selection.getFirstElement();
				if (testportParameter != null) {
					if (testportParameter.getValue() != null) {
						if (testportParameter.getValue().getText().length() == 0) {
							String temp = ConfigTreeNodeUtilities.toString(testportParameter.getValue());
							ConfigTreeNodeUtilities.setText( testportParameter.getValue(), temp );
						}

						parameterValueText.setText(testportParameter.getValue().getText());
					}

					valueChanged = true;
				}

				parameterValueText.setEnabled(testportParametersHandler != null && testportParameter != null);
			}
		});

		testportParametersTableViewer.setContentProvider(new TestportParameterDataContentProvider());
		testportParametersTableViewer.setLabelProvider(new TestportParameterDataLabelProvider());
		testportParametersTableViewer.setInput(testportParametersHandler);
		testportParametersTableViewer.setColumnProperties(columnNames);
		testportParametersTableViewer.setCellEditors(new TextCellEditor[] { new TextCellEditor(testportParametersTable),
				new TextCellEditor(testportParametersTable), new TextCellEditor(testportParametersTable) });
		testportParametersTableViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(final Object element, final String property) {
				return true;
			}

			@Override
			public String getValue(final Object element, final String property) {
				int columnIndex = Arrays.asList(columnNames).indexOf(property);
				TestportParameterDataLabelProvider labelProvider = (TestportParameterDataLabelProvider) testportParametersTableViewer
						.getLabelProvider();
				return labelProvider.getColumnText(element, columnIndex);
			}

			@Override
			public void modify(final Object element, final String property, final Object value) {
				int columnIndex = Arrays.asList(columnNames).indexOf(property);
				if (element != null && element instanceof TableItem && value instanceof String) {
					TestportParameter testportParameter = (TestportParameter) ((TableItem) element).getData();

					switch (columnIndex) {
					case 0:
						// COMPONENT_NAME
						ConfigTreeNodeUtilities.setText( testportParameter.getComponentName(), ((String) value).trim() );
						break;
					case 1:
						// TESTPORT_NAME
						ConfigTreeNodeUtilities.setText( testportParameter.getTestportName(), ((String) value).trim() );
						break;
					case 2:
						// PARAMETER_NAME
						ConfigTreeNodeUtilities.setText( testportParameter.getParameterName(), ((String) value).trim() );
						break;
					default:
						break;
					}
					testportParametersTableViewer.refresh(testportParameter);
					editor.setDirty();
				}
			}
		});

		testportParametersTableViewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE,
				new Transfer[] { TestportParameterTransfer.getInstance() }, new TestportParameterSectionDragSourceListener(this,
						testportParametersTableViewer));
		testportParametersTableViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE,
				new Transfer[] { TestportParameterTransfer.getInstance() }, new TestportParameterSectionDropTargetListener(
						testportParametersTableViewer, editor));
	}

	private void createDetailsPart(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		client.setLayout(layout);

		parameterValueText = toolkit.createText(client, "", SWT.MULTI | SWT.BORDER);
		parameterValueText.setLayoutData(new GridData(GridData.FILL_BOTH));
		parameterValueText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				StructuredSelection selection = (StructuredSelection) testportParametersTableViewer.getSelection();
				Iterator<?> iterator = selection.iterator();
				if (!iterator.hasNext()) {
					return;
				}

				TestportParameter testportParameter = (TestportParameter) iterator.next();
				ConfigTreeNodeUtilities.setText( testportParameter.getValue(), parameterValueText.getText() );

				if (valueChanged) {
					valueChanged = false;
					return;
				}

				editor.setDirty();
			}
		});
		parameterValueText.setEnabled(testportParametersHandler != null);

		section.setText("Testport parameter details");
		section.setDescription("Specify the concrete value for the actually selected testport parameter.");
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
		StructuredSelection selection = (StructuredSelection) testportParametersTableViewer.getSelection();
		Iterator<?> iterator = selection.iterator();
		if (iterator.hasNext()) {
			valueChanged = true;
		} else {
			parameterValueText.setText("");
		}

		add.setEnabled(testportParametersHandler != null);
		remove.setEnabled(testportParametersHandler != null);
		testportParametersTableViewer.setInput(testportParametersHandler);
		testportParametersTable.setEnabled(testportParametersHandler != null);
		parameterValueText.setEnabled(testportParametersHandler != null && selection.size() == 1);
		if (testportParametersHandler != null) {
			totalTestportParametersLabel.setText("Total: " + testportParametersHandler.getTestportParameters().size());
		}
	}

	public void refreshData(final TestportParameterSectionHandler testportParametersHandler) {
		this.testportParametersHandler = testportParametersHandler;

		if (testportParametersTableViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
				}
			});
		}
	}

	private void createNewTestportParameterSection() {
		if (testportParametersHandler == null) {
			return;
		}

		ParserRuleContext sectionRoot = new ParserRuleContext();
		testportParametersHandler.setLastSectionRoot( sectionRoot );
		ParseTree header = new AddedParseTree("\n[TESTPORT_PARAMETERS]");
		ConfigTreeNodeUtilities.addChild(sectionRoot, header);

		ParserRuleContext root = editor.getParseTreeRoot();
		if (root != null) {
			root.addChild(sectionRoot);
		}
	}

	private TestportParameter createNewParameter() {
		if (testportParametersHandler == null) {
			return null;
		}

		TestportParameter newTestportParameter = new TestportParameterSectionHandler.TestportParameter();
		final ParseTree root = new ParserRuleContext();
		newTestportParameter.setRoot( root );

		ConfigTreeNodeUtilities.addChild( root, new AddedParseTree("\n") );
		newTestportParameter.setComponentName(new AddedParseTree("component_name"));
		ConfigTreeNodeUtilities.addChild( root, newTestportParameter.getComponentName() );
		ConfigTreeNodeUtilities.addChild( root, new AddedParseTree(".") );

		newTestportParameter.setTestportName(new AddedParseTree("testport_name"));
		ConfigTreeNodeUtilities.addChild( root, newTestportParameter.getTestportName() );
		ConfigTreeNodeUtilities.addChild( root, new AddedParseTree(".") );

		newTestportParameter.setParameterName(new AddedParseTree("parameter_name"));
		ConfigTreeNodeUtilities.addChild( root, newTestportParameter.getParameterName() );
		ConfigTreeNodeUtilities.addChild( root, new AddedParseTree(" := ") );
		ConfigTreeNodeUtilities.addChild( root, new AddedParseTree("\"value\"") );

		return newTestportParameter;
	}

	private void removeTestportParameterSection() {
		if (testportParametersHandler == null || testportParametersHandler.getLastSectionRoot() == null) {
			return;
		}

		ConfigTreeNodeUtilities.removeChild(editor.getParseTreeRoot(), testportParametersHandler.getLastSectionRoot());
		testportParametersHandler.setLastSectionRoot(null);
	}

	public void removeSelectedParameters() {
		if (testportParametersTableViewer == null || testportParametersHandler == null) {
			return;
		}

		StructuredSelection selection = (StructuredSelection) testportParametersTableViewer.getSelection();
		Iterator<?> iterator = selection.iterator();
		// remove the selected elements
		for (; iterator.hasNext();) {
			TestportParameter testportParameter = (TestportParameter) iterator.next();
			if (testportParameter != null) {
				ConfigTreeNodeUtilities.removeChild(testportParametersHandler.getLastSectionRoot(), testportParameter.getRoot());
				testportParametersHandler.getTestportParameters().remove(testportParameter);
			}
		}

		testportParametersTableViewer.setSelection(null);
	}

	@Override
	public void setActive(final boolean active) {
		setErrorMessage();

		if (testportParametersTableViewer != null) {
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
