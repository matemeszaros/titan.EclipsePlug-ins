/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.logging;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.parsers.AddedParseTree;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler.LogParamEntry;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler.PluginSpecificParam;
import org.eclipse.titan.designer.editors.configeditor.ConfigEditor;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class GeneralOptionsSubPage {

	private Text logFileText;
	private CCombo timeStampFormat;
	private CCombo sourceInfoFormat;
	private CCombo appendFile;
	private CCombo logEventTypes;
	private CCombo logEntityName;
	private CCombo matchingHints;
	private Text actualLogFileSizeText;
	private Text actualLogFileNumberText;
	private CCombo actualDiskFullAction;
	private TableViewer paramViewer;
	private Table paramTable;
	private Button addPluginParameter;
	private Button removePluginParameter;
	private Button editPluginParameter;

	private ConfigEditor editor;
	private LoggingPage loggingPage;
	private LoggingSectionHandler loggingSectionHandler;
	private LogParamEntry selectedLogEntry;
	private boolean valueChanged = false;

	public GeneralOptionsSubPage(final ConfigEditor editor, final LoggingPage loggingPage) {
		this.editor = editor;
		this.loggingPage = loggingPage;
	}

	void createSectionGeneral(final FormToolkit toolkit, final Composite parent) {

		Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
		section.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		section.setText("Logging options for the selected component/plugin");
		// TODO the description should mention the selected
		// configuration.
		// section.setDescription("Specify the general logging options that are valid for all components.");
		section.marginWidth = 10;
		section.marginHeight = 0;

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);
		section.setClient(client);

		Composite generalOptions = toolkit.createComposite(client);

		generalOptions.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		GridLayout layoutGeneral = new GridLayout(2, false);
		generalOptions.setLayout(layoutGeneral);
		toolkit.paintBordersFor(generalOptions);

		valueChanged = true;

		toolkit.createLabel(generalOptions, "LogFile:");
		logFileText = toolkit.createText(generalOptions, "", SWT.SINGLE);
		logFileText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		logFileText.setEnabled(false);
		logFileText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || loggingSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = logFileText.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (selectedLogEntry.getLogFileRoot() != null) {
						ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry
								.getLogFileRoot());
					}
					selectedLogEntry.setLogFile(null);
					selectedLogEntry.setLogFileRoot(null);
				} else if (selectedLogEntry.getLogFile() == null) {
					// create the node
					createLogFileNode(loggingPage.getSelectedTreeElement(), selectedLogEntry, temp.trim());
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( selectedLogEntry.getLogFile(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( selectedLogEntry.getLogFile() );
				}
			}
		});
		if (selectedLogEntry != null && selectedLogEntry.getLogFile() != null) {
			if (selectedLogEntry.getLogFile().getText().length() == 0) {
				String temp = ConfigTreeNodeUtilities.toString(selectedLogEntry.getLogFile());
				ConfigTreeNodeUtilities.setText( selectedLogEntry.getLogFile(), temp );
				ConfigTreeNodeUtilities.removeChildren( selectedLogEntry.getLogFile() );
			}
			logFileText.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getLogFile()));
		}

		toolkit.createLabel(generalOptions, "TimeStampFormat:");
		timeStampFormat = new CCombo(generalOptions, SWT.FLAT);
		timeStampFormat.setEnabled(false);
		timeStampFormat.setLayoutData(new GridData(100, SWT.DEFAULT));
		timeStampFormat.add("Time");
		timeStampFormat.add("DateTime");
		timeStampFormat.add("Seconds");
		timeStampFormat.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || loggingSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = timeStampFormat.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (selectedLogEntry.getTimestampFormatRoot() != null) {
						ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry
								.getTimestampFormatRoot());
					}
					selectedLogEntry.setTimestampFormat(null);
					selectedLogEntry.setTimestampFormatRoot(null);

					// loggingPage.removeLoggingSection();
				} else if (selectedLogEntry.getTimestampFormat() == null) {
					// create the node
					createTimeStampFormatNode(loggingPage.getSelectedTreeElement(), selectedLogEntry, temp.trim());
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( selectedLogEntry.getTimestampFormat(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( selectedLogEntry.getTimestampFormat() );
				}
			}
		});
		if (selectedLogEntry != null && selectedLogEntry.getTimestampFormat() != null) {
			String temp = ConfigTreeNodeUtilities.toString(selectedLogEntry.getTimestampFormat()).trim();
			timeStampFormat.setText(temp);
		}

		toolkit.createLabel(generalOptions, "SourceInfoFormat:");
		sourceInfoFormat = new CCombo(generalOptions, SWT.FLAT);
		sourceInfoFormat.setEnabled(false);
		sourceInfoFormat.setLayoutData(new GridData(100, SWT.DEFAULT));
		sourceInfoFormat.add("None");
		sourceInfoFormat.add("Single");
		sourceInfoFormat.add("Stack");
		sourceInfoFormat.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || loggingSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = sourceInfoFormat.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (selectedLogEntry.getSourceInfoFormatRoot() != null) {
						ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry
								.getSourceInfoFormatRoot());
					}
					selectedLogEntry.setSourceInfoFormat(null);
					selectedLogEntry.setSourceInfoFormatRoot(null);

					// loggingPage.removeLoggingSection();
				} else if (selectedLogEntry.getSourceInfoFormat() == null) {
					// create the node
					createSourceInfoFormatNode(loggingPage.getSelectedTreeElement(), selectedLogEntry, temp.trim());
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( selectedLogEntry.getSourceInfoFormat(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( selectedLogEntry.getSourceInfoFormat() );
				}
			}
		});
		if (selectedLogEntry != null && selectedLogEntry.getSourceInfoFormat() != null) {
			sourceInfoFormat.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getSourceInfoFormat()).trim());
		}

		toolkit.createLabel(generalOptions, "AppendFile:");
		appendFile = new CCombo(generalOptions, SWT.FLAT);
		appendFile.setEnabled(false);
		appendFile.setLayoutData(new GridData(100, SWT.DEFAULT));
		appendFile.add("Yes");
		appendFile.add("No");
		appendFile.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || loggingSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = appendFile.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (selectedLogEntry.getAppendFileRoot() != null) {
						ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry
								.getAppendFileRoot());
					}
					selectedLogEntry.setAppendFile(null);
					selectedLogEntry.setAppendFileRoot(null);

					// loggingPage.removeLoggingSection();
				} else if (selectedLogEntry.getAppendFile() == null) {
					// create the node
					createAppendFileNode(loggingPage.getSelectedTreeElement(), selectedLogEntry, temp.trim());
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( selectedLogEntry.getAppendFile(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( selectedLogEntry.getAppendFile() );
				}
			}
		});
		if (selectedLogEntry != null && selectedLogEntry.getAppendFile() != null) {
			appendFile.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getAppendFile()).trim());
		}

		toolkit.createLabel(generalOptions, "LogEventTypes:");
		logEventTypes = new CCombo(generalOptions, SWT.FLAT);
		logEventTypes.setEnabled(false);
		logEventTypes.setLayoutData(new GridData(100, SWT.DEFAULT));
		logEventTypes.add("Yes");
		logEventTypes.add("No");
		logEventTypes.add("Detailed");
		logEventTypes.add("Subcategories");
		logEventTypes.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || loggingSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = logEventTypes.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (selectedLogEntry.getLogeventTypesRoot() != null) {
						ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry
								.getLogeventTypesRoot());
					}
					selectedLogEntry.setLogeventTypes(null);
					selectedLogEntry.setLogeventTypesRoot(null);

					// loggingPage.removeLoggingSection();
				} else if (selectedLogEntry.getLogeventTypes() == null) {
					// create the node
					createLogEventTypesNode(loggingPage.getSelectedTreeElement(), selectedLogEntry, temp.trim());
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( selectedLogEntry.getLogeventTypes(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( selectedLogEntry.getLogeventTypes() );
				}
			}
		});
		if (selectedLogEntry != null && selectedLogEntry.getLogeventTypes() != null) {
			logEventTypes.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getLogeventTypes()).trim());
		}

		toolkit.createLabel(generalOptions, "LogEntityName:");
		logEntityName = new CCombo(generalOptions, SWT.FLAT);
		logEntityName.setEnabled(false);
		logEntityName.setLayoutData(new GridData(100, SWT.DEFAULT));
		logEntityName.add("Yes");
		logEntityName.add("No");
		logEntityName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || loggingSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = logEntityName.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (selectedLogEntry.getLogEntityNameRoot() != null) {
						ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry
								.getLogEntityNameRoot());
					}
					selectedLogEntry.setLogEntityName(null);
					selectedLogEntry.setLogEntityNameRoot(null);

					// loggingPage.removeLoggingSection();
				} else if (selectedLogEntry.getLogEntityName() == null) {
					// create the node
					createLogEntityNameNode(loggingPage.getSelectedTreeElement(), selectedLogEntry, temp.trim());
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( selectedLogEntry.getLogEntityName(),temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( selectedLogEntry.getLogEntityName() );
				}
			}
		});
		if (selectedLogEntry != null && selectedLogEntry.getLogEntityName() != null) {
			logEntityName.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getLogEntityName()).trim());
		}

		toolkit.createLabel(generalOptions, "MatchingHints");
		matchingHints = new CCombo(generalOptions, SWT.FLAT);
		matchingHints.setEnabled(false);
		matchingHints.setLayoutData(new GridData(100, SWT.DEFAULT));
		matchingHints.add("Compact");
		matchingHints.add("Detailed");
		matchingHints.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || loggingSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = matchingHints.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (selectedLogEntry.getMatchingHintsRoot() != null) {
						ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry
								.getMatchingHintsRoot());
					}
					selectedLogEntry.setMatchingHints(null);
					selectedLogEntry.setMatchingHintsRoot(null);

					// loggingPage.removeLoggingSection();
				} else if (selectedLogEntry.getMatchingHints() == null) {
					// create the node
					createMatchingHintsNode(loggingPage.getSelectedTreeElement(), selectedLogEntry, temp.trim());
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( selectedLogEntry.getMatchingHints(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( selectedLogEntry.getMatchingHints() );
				}
			}
		});
		if (selectedLogEntry != null && selectedLogEntry.getMatchingHints() != null) {
			matchingHints.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getMatchingHints()).trim());
		}

		toolkit.createLabel(generalOptions, "Log file size:");
		actualLogFileSizeText = toolkit.createText(generalOptions, "", SWT.SINGLE);
		actualLogFileSizeText.setEnabled(false);
		actualLogFileSizeText.setLayoutData(new GridData(95, SWT.DEFAULT));
		actualLogFileSizeText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || selectedLogEntry == null) {
					return;
				}

				editor.setDirty();

				String temp = actualLogFileSizeText.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (selectedLogEntry.getLogfileSize() != null) {
						ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry
								.getLogfileSizeRoot());
					}
					selectedLogEntry.setLogfileSize(null);
					selectedLogEntry.setLogfileSizeRoot(null);

				} else if (selectedLogEntry.getLogfileSize() == null) {
					// create the node
					createLogFileSizeNode(loggingPage.getSelectedTreeElement(), selectedLogEntry, temp.trim());
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( selectedLogEntry.getLogfileSize(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( selectedLogEntry.getLogfileSize() );
				}
			}
		});
		if (selectedLogEntry != null && selectedLogEntry.getLogfileSize() != null) {
			actualLogFileSizeText.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getLogfileSize()));
		}

		toolkit.createLabel(generalOptions, "Log file number:");
		actualLogFileNumberText = toolkit.createText(generalOptions, "", SWT.SINGLE);
		actualLogFileNumberText.setEnabled(false);
		actualLogFileNumberText.setLayoutData(new GridData(95, SWT.DEFAULT));
		actualLogFileNumberText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || selectedLogEntry == null) {
					return;
				}

				editor.setDirty();

				String temp = actualLogFileNumberText.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (selectedLogEntry.getLogfileNumber() != null) {
						ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry
								.getLogfileNumberRoot());
					}
					selectedLogEntry.setLogfileNumber(null);
					selectedLogEntry.setLogfileNumberRoot(null);

				} else if (selectedLogEntry.getLogfileNumber() == null) {
					// create the node
					createLogFileNumberNode(loggingPage.getSelectedTreeElement(), selectedLogEntry, temp.trim());
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( selectedLogEntry.getLogfileNumber(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( selectedLogEntry.getLogfileNumber() );
				}
			}
		});
		if (selectedLogEntry != null && selectedLogEntry.getLogfileNumber() != null) {
			actualLogFileNumberText.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getLogfileNumber()));
		}

		toolkit.createLabel(generalOptions, "Disk full action:");
		actualDiskFullAction = new CCombo(generalOptions, SWT.FLAT);
		actualDiskFullAction.setEnabled(false);
		actualDiskFullAction.setLayoutData(new GridData(100, SWT.DEFAULT));
		actualDiskFullAction.add("Stop");
		actualDiskFullAction.add("Retry");
		actualDiskFullAction.add("Delete");
		actualDiskFullAction.add("Error");
		actualDiskFullAction.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || selectedLogEntry == null) {
					return;
				}

				editor.setDirty();

				String temp = actualDiskFullAction.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (selectedLogEntry.getDiskFullAction() != null) {
						ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry
								.getDiskFullActionRoot());
					}
					selectedLogEntry.setDiskFullAction(null);
					selectedLogEntry.setDiskFullActionRoot(null);

				} else if (selectedLogEntry.getDiskFullAction() == null) {
					// create the node
					createDiskFullActionNode(loggingPage.getSelectedTreeElement(), selectedLogEntry, temp.trim());
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( selectedLogEntry.getDiskFullAction(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( selectedLogEntry.getDiskFullAction() );
				}
			}
		});
		if (selectedLogEntry != null && selectedLogEntry.getDiskFullAction() != null) {
			actualDiskFullAction.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getDiskFullAction()));
		}

		// FIXME should not be here
		toolkit.createLabel(generalOptions, "Plugin specific:");
		Composite components = toolkit.createComposite(generalOptions, SWT.WRAP);
		GridLayout tablelayout = new GridLayout();
		tablelayout.numColumns = 2;
		components.setLayout(tablelayout);
		components.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		toolkit.paintBordersFor(components);

		paramTable = toolkit.createTable(components, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
		paramTable.setLayoutData(gd);
		paramTable.setEnabled(loggingSectionHandler != null);
		paramViewer = new TableViewer(paramTable);
		paramViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				internalRefresh();
			}
		});

		TableColumn column = new TableColumn(paramTable, SWT.LEFT, 0);
		column.setText("Parameter");
		column.setMoveable(false);
		column.setWidth(80);
		TableColumn column2 = new TableColumn(paramTable, SWT.LEFT, 0);
		column2.setText("Value");
		column2.setMoveable(false);
		column2.setWidth(90);

		paramTable.setLinesVisible(true);
		paramTable.setHeaderVisible(true);
		paramTable.setEnabled(false);

		paramViewer.setContentProvider(new ParamDataContentProvider());
		paramViewer.setLabelProvider(new ParamDataLabelProvider());

		Composite buttons = toolkit.createComposite(components);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL));
		addPluginParameter = toolkit.createButton(buttons, "Add...", SWT.PUSH);
		GridData gdButton = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		addPluginParameter.setLayoutData(gdButton);
		addPluginParameter.setEnabled(false);
		addPluginParameter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (loggingSectionHandler == null) {
					return;
				}

				if (loggingSectionHandler.getLastSectionRoot() == null) {
					loggingPage.createLoggingSection();
				}

				ParamDialog d = new ParamDialog(null, selectedLogEntry, null);
				if (d.open() == Window.OK) {
					PluginSpecificParam psp = createPluginSpecificParamNode(d.getName(), d.getValue());
					paramViewer.setInput(selectedLogEntry);
					TableItem[] items = paramTable.getItems();
					for (TableItem item : items) {
						if (item.getData().equals(psp)) {
							paramTable.setSelection(item);
							paramTable.showSelection();
							break;
						}
					}
					internalRefresh();
					editor.setDirty();
				}
			}
		});

		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL));
		editPluginParameter = toolkit.createButton(buttons, "Edit...", SWT.PUSH);
		gdButton = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		editPluginParameter.setLayoutData(gdButton);
		editPluginParameter.setEnabled(false);
		editPluginParameter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (loggingSectionHandler == null) {
					return;
				}

				if (loggingSectionHandler.getLastSectionRoot() == null) {
					loggingPage.createLoggingSection();
				}

				StructuredSelection selection = (StructuredSelection) paramViewer.getSelection();
				PluginSpecificParam psp = (PluginSpecificParam) selection.getFirstElement();

				ParamDialog d = new ParamDialog(null, selectedLogEntry, psp);
				if (d.open() == Window.OK) {
					if (psp.getParam() != null) {
						ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), psp.getParam());
					}
					selectedLogEntry.getPluginSpecificParam().remove(psp);
					psp = createPluginSpecificParamNode(d.getName(), d.getValue());

					TableItem[] items = paramTable.getItems();
					for (TableItem item : items) {
						if (item.getData().equals(psp)) {
							paramTable.setSelection(item);
							paramTable.showSelection();
							break;
						}
					}
					internalRefresh();
					editor.setDirty();
				}
			}
		});

		removePluginParameter = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		gdButton = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		removePluginParameter.setLayoutData(gdButton);
		removePluginParameter.setEnabled(false);
		removePluginParameter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (loggingSectionHandler == null) {
					return;
				}

				StructuredSelection selection = (StructuredSelection) paramViewer.getSelection();
				PluginSpecificParam psp = (PluginSpecificParam) selection.getFirstElement();

				if (psp.getParam() != null) {
					ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), psp.getParam());
				}

				selectedLogEntry.getPluginSpecificParam().remove(psp);
				internalRefresh();
				editor.setDirty();
			}
		});

		valueChanged = false;
	}
	
	private void createNode( final LoggingSectionHandler.LoggerTreeElement aLte,
							 final LogParamEntry aLogEntry,
							 final String aValue,
							 final String aNodeText,
							 final ParseTree aRoot,
							 final ParseTree aData ) {
		ConfigTreeNodeUtilities.addChild( loggingSectionHandler.getLastSectionRoot(), aRoot );

		final StringBuilder name = new StringBuilder();
		aLte.writeNamePrefix(name);
		name.append( aNodeText + " := ");
		
		ConfigTreeNodeUtilities.addChild( aRoot, new AddedParseTree( "\n" ) );
		ConfigTreeNodeUtilities.addChild( aRoot, new AddedParseTree( name.toString() ) );
		ConfigTreeNodeUtilities.addChild( aRoot, aData );
	}

	private void createLogFileNode(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry, final String value) {
		logentry.setLogFileRoot( new ParserRuleContext() );
		logentry.setLogFile(new AddedParseTree(value));
		createNode( lte, logentry, value, "LogFile", logentry.getLogFileRoot(), logentry.getLogFile() );
	}

	private void createTimeStampFormatNode(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry, final String value) {
		logentry.setLogFileRoot( new ParserRuleContext() );
		logentry.setLogFile(new AddedParseTree(value));
		createNode( lte, logentry, value, "TimeStampFormat", logentry.getTimestampFormatRoot(), logentry.getTimestampFormat() );
	}

	private void createSourceInfoFormatNode(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry, final String value) {
		logentry.setLogFileRoot( new ParserRuleContext() );
		logentry.setLogFile(new AddedParseTree(value));
		createNode( lte, logentry, value, "SourceInfoFormat", logentry.getSourceInfoFormatRoot(), logentry.getSourceInfoFormat() );
	}

	private void createAppendFileNode(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry, final String value) {
		logentry.setLogFileRoot( new ParserRuleContext() );
		logentry.setLogFile(new AddedParseTree(value));
		createNode( lte, logentry, value, "AppendFile", logentry.getAppendFileRoot(), logentry.getAppendFile() );
	}

	private void createLogEventTypesNode(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry, final String value) {
		logentry.setLogFileRoot( new ParserRuleContext() );
		logentry.setLogFile(new AddedParseTree(value));
		createNode( lte, logentry, value, "LogEventTypes", logentry.getLogeventTypesRoot(), logentry.getLogeventTypes() );
	}

	private void createLogEntityNameNode(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry, final String value) {
		logentry.setLogFileRoot( new ParserRuleContext() );
		logentry.setLogFile(new AddedParseTree(value));
		createNode( lte, logentry, value, "LogEntityName", logentry.getLogEntityNameRoot(), logentry.getLogEntityName() );
	}

	private void createMatchingHintsNode(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry, final String value) {
		logentry.setLogFileRoot( new ParserRuleContext() );
		logentry.setLogFile( new AddedParseTree( value ) );
		createNode( lte, logentry, value, "MatchingHints", logentry.getMatchingHintsRoot(), logentry.getMatchingHints() );
	}

	private void createLogFileSizeNode(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry, final String value) {
		logentry.setLogFileRoot( new ParserRuleContext() );
		logentry.setLogFile( new AddedParseTree( value ) );
		createNode( lte, logentry, value, "LogFileSize", logentry.getLogfileSizeRoot(), logentry.getLogfileSize() );
	}

	private void createLogFileNumberNode(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry, final String value) {
		logentry.setLogFileRoot( new ParserRuleContext() );
		logentry.setLogFile( new AddedParseTree( value ) );
		createNode( lte, logentry, value, "LogFileNumber", logentry.getLogfileNumberRoot(), logentry.getLogfileNumber() );
	}

	private void createDiskFullActionNode(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry, final String value) {
		logentry.setLogFileRoot( new ParserRuleContext() );
		logentry.setLogFile( new AddedParseTree( value ) );
		createNode( lte, logentry, value, "DiskFullAction", logentry.getDiskFullActionRoot(), logentry.getDiskFullAction() );
	}

	private PluginSpecificParam createPluginSpecificParamNode(final String param, final String value) {
		StringBuilder name = new StringBuilder();
		loggingPage.getSelectedTreeElement().writeNamePrefix(name);
		name.append(param);
		name.append(" := ");

		final ParseTree keyAST = new AddedParseTree(name.toString());
		final ParseTree valueAST = new AddedParseTree(value);

		final ParseTree root = new ParserRuleContext();
		ConfigTreeNodeUtilities.addChild( loggingSectionHandler.getLastSectionRoot(), root );
		ConfigTreeNodeUtilities.addChild( root, new AddedParseTree( "\n" ) );
		ConfigTreeNodeUtilities.addChild( root, keyAST );
		ConfigTreeNodeUtilities.addChild( root, valueAST );

		PluginSpecificParam psp = new PluginSpecificParam(root, keyAST, valueAST, param);
		selectedLogEntry.getPluginSpecificParam().add(psp);
		return psp;
	}

	private void internalRefresh() {
		if (selectedLogEntry == null) {
			logFileText.setEnabled(false);
			timeStampFormat.setEnabled(false);
			sourceInfoFormat.setEnabled(false);
			appendFile.setEnabled(false);
			logEventTypes.setEnabled(false);
			logEntityName.setEnabled(false);
			matchingHints.setEnabled(false);
			actualLogFileSizeText.setEnabled(false);
			actualLogFileNumberText.setEnabled(false);
			actualDiskFullAction.setEnabled(false);
			paramTable.setEnabled(false);
			addPluginParameter.setEnabled(false);
			editPluginParameter.setEnabled(false);
			removePluginParameter.setEnabled(false);
			return;
		}

		addPluginParameter.setEnabled(true);
		if (((StructuredSelection) paramViewer.getSelection()).getFirstElement() != null) {
			editPluginParameter.setEnabled(true);
			removePluginParameter.setEnabled(true);
		}

		valueChanged = true;

		if (logFileText != null) {
			if (selectedLogEntry.getLogFile() == null) {
				logFileText.setText("");
			} else {
				logFileText.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getLogFile()).trim());
			}
			logFileText.setEnabled(true);
		}

		if (timeStampFormat != null) {
			if (selectedLogEntry.getTimestampFormat() == null) {
				timeStampFormat.setText("");
			} else {
				timeStampFormat.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getTimestampFormat()).trim());
			}
			timeStampFormat.setEnabled(true);
		}

		if (sourceInfoFormat != null) {
			if (selectedLogEntry.getSourceInfoFormat() == null) {
				sourceInfoFormat.setText("");
			} else {
				sourceInfoFormat.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getSourceInfoFormat()).trim());
			}
			sourceInfoFormat.setEnabled(true);
		}

		if (appendFile != null) {
			if (selectedLogEntry.getAppendFile() == null) {
				appendFile.setText("");
			} else {
				appendFile.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getAppendFile()).trim());
			}
			appendFile.setEnabled(true);
		}

		if (logEventTypes != null) {
			if (selectedLogEntry.getLogeventTypes() == null) {
				logEventTypes.setText("");
			} else {
				logEventTypes.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getLogeventTypes()).trim());
			}
			logEventTypes.setEnabled(true);
		}

		if (logEntityName != null) {
			if (selectedLogEntry.getLogEntityName() == null) {
				logEntityName.setText("");
			} else {
				logEntityName.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getLogEntityName()).trim());
			}
			logEntityName.setEnabled(true);
		}

		if (matchingHints != null) {
			if (selectedLogEntry.getMatchingHints() == null) {
				matchingHints.setText("");
			} else {
				matchingHints.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getMatchingHints()).trim());
			}
			matchingHints.setEnabled(true);
		}

		if (actualLogFileSizeText != null) {
			if (selectedLogEntry.getLogfileSize() == null) {
				actualLogFileSizeText.setText("");
			} else {
				actualLogFileSizeText.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getLogfileSize()).trim());
			}
			actualLogFileSizeText.setEnabled(true);
		}

		if (actualLogFileNumberText != null) {
			if (selectedLogEntry.getLogfileNumber() == null) {
				actualLogFileNumberText.setText("");
			} else {
				actualLogFileNumberText.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getLogfileNumber()).trim());
			}
			actualLogFileNumberText.setEnabled(true);
		}

		if (actualDiskFullAction != null) {
			if (selectedLogEntry.getDiskFullAction() == null) {
				actualDiskFullAction.setText("");
			} else {
				actualDiskFullAction.setText(ConfigTreeNodeUtilities.toString(selectedLogEntry.getDiskFullAction()).trim());
			}
			actualDiskFullAction.setEnabled(true);
		}

		if (paramTable != null) {
			paramTable.setEnabled(true);
			paramViewer.setInput(selectedLogEntry);
		}

		valueChanged = false;
	}

	public void initializeEntry(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry) {
		if (logentry == null) {
			return;
		}

		createLogFileNode(lte, logentry, "\"%e.%h-%r-part%i.%s\"");
		createTimeStampFormatNode(lte, logentry, "Time");
		createSourceInfoFormatNode(lte, logentry, "None");
		createAppendFileNode(lte, logentry, "No");
		createLogEventTypesNode(lte, logentry, "No");
		createLogEntityNameNode(lte, logentry, "No");
		createLogFileSizeNode(lte, logentry, "0");
		createLogFileNumberNode(lte, logentry, "1");
		createDiskFullActionNode(lte, logentry, "Error");

		paramTable.setEnabled(true);
		paramViewer.setInput(logentry);
	}

	public void pluginRenamed() {
		LoggingSectionHandler.LoggerTreeElement lte = loggingPage.getSelectedTreeElement();
		if (selectedLogEntry.getLogFileRoot() != null) {
			ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry.getLogFileRoot());
			createLogFileNode(lte, selectedLogEntry, logFileText.getText().trim());
		}
		if (selectedLogEntry.getTimestampFormatRoot() != null) {
			ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry.getTimestampFormatRoot());
			createTimeStampFormatNode(lte, selectedLogEntry, timeStampFormat.getText().trim());
		}
		if (selectedLogEntry.getSourceInfoFormatRoot() != null) {
			ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry
					.getSourceInfoFormatRoot());
			createSourceInfoFormatNode(lte, selectedLogEntry, sourceInfoFormat.getText().trim());
		}
		if (selectedLogEntry.getAppendFileRoot() != null) {
			ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry.getAppendFileRoot());
			createAppendFileNode(lte, selectedLogEntry, appendFile.getText().trim());
		}
		if (selectedLogEntry.getLogeventTypesRoot() != null) {
			ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry.getLogeventTypesRoot());
			createLogEventTypesNode(lte, selectedLogEntry, logEventTypes.getText().trim());
		}
		if (selectedLogEntry.getLogEntityNameRoot() != null) {
			ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry.getLogEntityNameRoot());
			createLogEntityNameNode(lte, selectedLogEntry, logEntityName.getText().trim());
		}
		if (selectedLogEntry.getLogfileSizeRoot() != null) {
			ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry.getLogfileSizeRoot());
			createLogFileSizeNode(lte, selectedLogEntry, actualLogFileSizeText.getText().trim());
		}
		if (selectedLogEntry.getLogfileNumberRoot() != null) {
			ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry.getLogfileNumberRoot());
			createLogFileNumberNode(lte, selectedLogEntry, actualLogFileNumberText.getText().trim());
		}
		if (selectedLogEntry.getDiskFullActionRoot() != null) {
			ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), selectedLogEntry.getDiskFullActionRoot());
			createDiskFullActionNode(lte, selectedLogEntry, actualDiskFullAction.getText().trim());
		}
		List<PluginSpecificParam> list = new ArrayList<PluginSpecificParam>(selectedLogEntry.getPluginSpecificParam());
		for (PluginSpecificParam param : list) {
			ConfigTreeNodeUtilities.removeChild(loggingSectionHandler.getLastSectionRoot(), param.getParam());
		}
		selectedLogEntry.getPluginSpecificParam().clear();
		for (PluginSpecificParam param : list) {
			createPluginSpecificParamNode(param.getParamName(), param.getValue().getText());
		}
		paramViewer.setInput(selectedLogEntry);
	}

	public void refreshData(final LoggingSectionHandler loggingSectionHandler, final LogParamEntry logentry) {
		this.loggingSectionHandler = loggingSectionHandler;
		this.selectedLogEntry = logentry;

		if (logFileText != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
				}
			});
		}
	}
}
