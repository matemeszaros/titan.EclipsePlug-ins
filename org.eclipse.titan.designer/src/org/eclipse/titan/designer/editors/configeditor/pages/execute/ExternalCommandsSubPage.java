/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.execute;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.parsers.CommonHiddenStreamToken;
import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.ExternalCommandSectionHandler;
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
public final class ExternalCommandsSubPage {

	private Text beginControlPartText;
	private Text beginTestCaseText;
	private Text endControlPartText;
	private Text endTestCaseText;

	private ConfigEditor editor;
	private ExternalCommandSectionHandler executeCommandSectionHandler;
	private boolean valueChanged = false;

	public ExternalCommandsSubPage(final ConfigEditor editor) {
		this.editor = editor;
	}

	void createExternalcommandsSection(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		section.setText("External commands");
		section.setDescription("Specify the external commands to be called at execution time for this configuration.");
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(false);
			}
		});
		GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
		section.setLayoutData(gd);

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		section.setClient(client);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		client.setLayout(layout);

		toolkit.paintBordersFor(client);

		valueChanged = true;

		toolkit.createLabel(client, "Begin control part:");
		beginControlPartText = toolkit.createText(client, "", SWT.SINGLE);
		beginControlPartText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		beginControlPartText.setEnabled(executeCommandSectionHandler != null);
		beginControlPartText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || executeCommandSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = beginControlPartText.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (executeCommandSectionHandler.getBeginControlPartRoot() != null) {
						ConfigTreeNodeUtilities.removeFromChain(executeCommandSectionHandler.getLastSectionRoot(),
								executeCommandSectionHandler.getBeginControlPartRoot());
					}
					executeCommandSectionHandler.setBeginControlPart(null);
					executeCommandSectionHandler.setBeginControlPartRoot(null);

					removeExternalCommandsSection();
				} else if (executeCommandSectionHandler.getBeginControlPart() == null) {
					// create the node
					createExternalCommandsSection();

					LocationAST oldsibling = executeCommandSectionHandler.getLastSectionRoot().getNextSibling();

					LocationAST node = new LocationAST("beginControlPart := ");
					node.setHiddenBefore(new CommonHiddenStreamToken("\n"));
					executeCommandSectionHandler.setBeginControlPart(new LocationAST(temp.trim()));
					node.setNextSibling(executeCommandSectionHandler.getBeginControlPart());
					executeCommandSectionHandler.setBeginControlPartRoot(new LocationAST(""));
					executeCommandSectionHandler.getBeginControlPartRoot().setFirstChild(node);
					executeCommandSectionHandler.getLastSectionRoot().setNextSibling(
							executeCommandSectionHandler.getBeginControlPartRoot());
					executeCommandSectionHandler.getBeginControlPartRoot().setNextSibling(oldsibling);
				} else {
					// simple modification
					executeCommandSectionHandler.getBeginControlPart().setText(temp.trim());
					executeCommandSectionHandler.getBeginControlPart().setFirstChild(null);
				}
			}
		});
		if (executeCommandSectionHandler != null && executeCommandSectionHandler.getBeginControlPart() != null) {
			beginControlPartText.setText(ConfigTreeNodeUtilities.toString(executeCommandSectionHandler.getBeginControlPart()));
		}
		Button browse = toolkit.createButton(client, "Browse...", SWT.PUSH);
		browse.addSelectionListener(new ExternalCommandBrowser(beginControlPartText, (IFile) editor.getEditorInput().getAdapter(IFile.class)));

		toolkit.createLabel(client, "Begin testcase:");
		beginTestCaseText = toolkit.createText(client, "", SWT.SINGLE);
		beginTestCaseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		beginTestCaseText.setEnabled(executeCommandSectionHandler != null);
		beginTestCaseText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || executeCommandSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = beginTestCaseText.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (executeCommandSectionHandler.getBeginTestcaseRoot() != null) {
						ConfigTreeNodeUtilities.removeFromChain(executeCommandSectionHandler.getLastSectionRoot(),
								executeCommandSectionHandler.getBeginTestcaseRoot());
					}
					executeCommandSectionHandler.setBeginTestcase(null);
					executeCommandSectionHandler.setBeginTestcaseRoot(null);

					removeExternalCommandsSection();
				} else if (executeCommandSectionHandler.getBeginTestcase() == null) {
					// create the node
					createExternalCommandsSection();

					LocationAST oldsibling = executeCommandSectionHandler.getLastSectionRoot().getNextSibling();

					LocationAST node = new LocationAST("beginTestcase := ");
					node.setHiddenBefore(new CommonHiddenStreamToken("\n"));
					executeCommandSectionHandler.setBeginTestcase(new LocationAST(temp.trim()));
					node.setNextSibling(executeCommandSectionHandler.getBeginTestcase());
					executeCommandSectionHandler.setBeginTestcaseRoot(new LocationAST(""));
					executeCommandSectionHandler.getBeginTestcaseRoot().setFirstChild(node);
					executeCommandSectionHandler.getLastSectionRoot().setNextSibling(
							executeCommandSectionHandler.getBeginTestcaseRoot());
					executeCommandSectionHandler.getBeginTestcaseRoot().setNextSibling(oldsibling);
				} else {
					// simple modification
					executeCommandSectionHandler.getBeginTestcase().setText(temp.trim());
					executeCommandSectionHandler.getBeginTestcase().setFirstChild(null);
				}
			}
		});
		if (executeCommandSectionHandler != null && executeCommandSectionHandler.getBeginTestcase() != null) {
			beginTestCaseText.setText(ConfigTreeNodeUtilities.toString(executeCommandSectionHandler.getBeginTestcase()));
		}
		browse = toolkit.createButton(client, "Browse...", SWT.PUSH);
		browse.addSelectionListener(new ExternalCommandBrowser(beginTestCaseText, (IFile) editor.getEditorInput().getAdapter(IFile.class)));

		toolkit.createLabel(client, "End control part:");
		endControlPartText = toolkit.createText(client, "", SWT.SINGLE);
		endControlPartText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		endControlPartText.setEnabled(executeCommandSectionHandler != null);
		endControlPartText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || executeCommandSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = endControlPartText.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (executeCommandSectionHandler.getEndControlPartRoot() != null) {
						ConfigTreeNodeUtilities.removeFromChain(executeCommandSectionHandler.getLastSectionRoot(),
								executeCommandSectionHandler.getEndControlPartRoot());
					}
					executeCommandSectionHandler.setEndControlPart(null);
					executeCommandSectionHandler.setEndControlPartRoot(null);

					removeExternalCommandsSection();
				} else if (executeCommandSectionHandler.getEndControlPart() == null) {
					// create the node
					createExternalCommandsSection();

					LocationAST oldsibling = executeCommandSectionHandler.getLastSectionRoot().getNextSibling();

					LocationAST node = new LocationAST("endControlpart := ");
					node.setHiddenBefore(new CommonHiddenStreamToken("\n"));
					executeCommandSectionHandler.setEndControlPart(new LocationAST(temp.trim()));
					node.setNextSibling(executeCommandSectionHandler.getEndControlPart());
					executeCommandSectionHandler.setEndControlPartRoot(new LocationAST(""));
					executeCommandSectionHandler.getEndControlPartRoot().setFirstChild(node);
					executeCommandSectionHandler.getLastSectionRoot().setNextSibling(
							executeCommandSectionHandler.getEndControlPartRoot());
					executeCommandSectionHandler.getEndControlPartRoot().setNextSibling(oldsibling);
				} else {
					// simple modification
					executeCommandSectionHandler.getEndControlPart().setText(temp.trim());
					executeCommandSectionHandler.getEndControlPart().setFirstChild(null);
				}
			}
		});
		if (executeCommandSectionHandler != null && executeCommandSectionHandler.getEndControlPart() != null) {
			endControlPartText.setText(ConfigTreeNodeUtilities.toString(executeCommandSectionHandler.getEndControlPart()));
		}
		browse = toolkit.createButton(client, "Browse...", SWT.PUSH);
		browse.addSelectionListener(new ExternalCommandBrowser(endControlPartText, (IFile) editor.getEditorInput().getAdapter(IFile.class)));

		toolkit.createLabel(client, "End testcase:");
		endTestCaseText = toolkit.createText(client, "", SWT.SINGLE);
		endTestCaseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		endTestCaseText.setEnabled(executeCommandSectionHandler != null);
		endTestCaseText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || executeCommandSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = endTestCaseText.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (executeCommandSectionHandler.getEndTestcaseRoot() != null) {
						ConfigTreeNodeUtilities.removeFromChain(executeCommandSectionHandler.getLastSectionRoot(),
								executeCommandSectionHandler.getEndTestcaseRoot());
					}
					executeCommandSectionHandler.setEndTestcase(null);
					executeCommandSectionHandler.setEndTestcaseRoot(null);

					removeExternalCommandsSection();
				} else if (executeCommandSectionHandler.getEndTestcase() == null) {
					// create the node
					createExternalCommandsSection();

					LocationAST oldsibling = executeCommandSectionHandler.getLastSectionRoot().getNextSibling();

					LocationAST node = new LocationAST("endTestcase := ");
					node.setHiddenBefore(new CommonHiddenStreamToken("\n"));
					executeCommandSectionHandler.setEndTestcase(new LocationAST(temp.trim()));
					node.setNextSibling(executeCommandSectionHandler.getEndTestcase());
					executeCommandSectionHandler.setEndTestcaseRoot(new LocationAST(""));
					executeCommandSectionHandler.getEndTestcaseRoot().setFirstChild(node);
					executeCommandSectionHandler.getLastSectionRoot().setNextSibling(
							executeCommandSectionHandler.getEndTestcaseRoot());
					executeCommandSectionHandler.getEndTestcaseRoot().setNextSibling(oldsibling);
				} else {
					// simple modification
					executeCommandSectionHandler.getEndTestcase().setText(temp.trim());
					executeCommandSectionHandler.getEndTestcase().setFirstChild(null);
				}
			}
		});
		if (executeCommandSectionHandler != null && executeCommandSectionHandler.getEndTestcase() != null) {
			endTestCaseText.setText(ConfigTreeNodeUtilities.toString(executeCommandSectionHandler.getEndTestcase()));
		}
		browse = toolkit.createButton(client, "Browse...", SWT.PUSH);
		browse.addSelectionListener(new ExternalCommandBrowser(endTestCaseText, (IFile) editor.getEditorInput().getAdapter(IFile.class)));

		valueChanged = false;
	}

	private void internalRefresh() {
		if (executeCommandSectionHandler == null) {
			beginControlPartText.setEnabled(false);
			endControlPartText.setEnabled(false);
			beginTestCaseText.setEnabled(false);
			endTestCaseText.setEnabled(false);
			return;
		}

		valueChanged = true;

		if (executeCommandSectionHandler.getBeginControlPart() != null && beginControlPartText != null) {
			beginControlPartText.setEnabled(true);
			beginControlPartText.setText(ConfigTreeNodeUtilities.toString(executeCommandSectionHandler.getBeginControlPart()).trim());
		}

		if (executeCommandSectionHandler.getEndControlPart() != null && endControlPartText != null) {
			endControlPartText.setEnabled(true);
			endControlPartText.setText(ConfigTreeNodeUtilities.toString(executeCommandSectionHandler.getEndControlPart()).trim());
		}

		if (executeCommandSectionHandler.getBeginTestcase() != null && beginTestCaseText != null) {
			beginTestCaseText.setEnabled(true);
			beginTestCaseText.setText(ConfigTreeNodeUtilities.toString(executeCommandSectionHandler.getBeginTestcase()).trim());
		}

		if (executeCommandSectionHandler.getEndTestcase() != null && endTestCaseText != null) {
			endTestCaseText.setEnabled(true);
			endTestCaseText.setText(ConfigTreeNodeUtilities.toString(executeCommandSectionHandler.getEndTestcase()).trim());
		}

		valueChanged = false;
	}

	public void refreshData(final ExternalCommandSectionHandler executeCommandSectionHandler) {
		this.executeCommandSectionHandler = executeCommandSectionHandler;

		if (beginControlPartText != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
				}
			});
		}
	}

	// creates the maincontroller section if needed
	private void createExternalCommandsSection() {
		if (executeCommandSectionHandler == null || executeCommandSectionHandler.getLastSectionRoot() != null) {
			return;
		}

		executeCommandSectionHandler.setLastSectionRoot(new LocationAST("[EXTERNAL_COMMANDS]"));
		executeCommandSectionHandler.getLastSectionRoot().setHiddenBefore(new CommonHiddenStreamToken("\n"));
		LocationAST sectionRoot = new LocationAST("");
		sectionRoot.setFirstChild(executeCommandSectionHandler.getLastSectionRoot());

		LocationAST root = editor.getParseTreeRoot();
		if (root != null) {
			root.addChild(sectionRoot);
		}
	}

	// removes the main controller section if needed
	private void removeExternalCommandsSection() {
		if (executeCommandSectionHandler == null || executeCommandSectionHandler.getLastSectionRoot() == null) {
			return;
		}

		if (executeCommandSectionHandler.getBeginControlPart() == null && executeCommandSectionHandler.getEndControlPart() == null
				&& executeCommandSectionHandler.getBeginTestcase() == null && executeCommandSectionHandler.getEndTestcase() == null) {
			ConfigTreeNodeUtilities.removeFromChain(editor.getParseTreeRoot().getFirstChild(), executeCommandSectionHandler
					.getLastSectionRoot().getParent());
			executeCommandSectionHandler.setLastSectionRoot(null);
		}
	}
}
