/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.compgroupmc;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.parsers.AddedParseTree;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.MCSectionHandler;
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
public final class MCSubPage {

	private Text localAddressText;
	private Text tcpPortText;
	private Text killTimerText;
	private Text numHCsText;
	private CCombo unixDomainSocketText;

	private ConfigEditor editor;
	private MCSectionHandler mcSectionHandler;
	private boolean valueChanged = false;

	public MCSubPage(final ConfigEditor editor) {
		this.editor = editor;
	}

	void createMainControllerSection(final Composite parent, final ScrolledForm form, final FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));

		section.setText("Main Controller options");
		section.setDescription("Specify the Main Controller directing options for this configuration.");
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
		layout.numColumns = 2;
		client.setLayout(layout);

		toolkit.paintBordersFor(client);

		valueChanged = true;

		toolkit.createLabel(client, "Local Address:");
		localAddressText = toolkit.createText(client, "", SWT.SINGLE);
		localAddressText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		localAddressText.setEnabled(mcSectionHandler != null);
		localAddressText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || mcSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = localAddressText.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (mcSectionHandler.getLocalAddressRoot() != null) {
						ConfigTreeNodeUtilities.removeChild(mcSectionHandler.getLastSectionRoot(),
								mcSectionHandler.getLocalAddressRoot());
					}
					mcSectionHandler.setLocalAddress(null);
					mcSectionHandler.setLocalAddressRoot(null);

					removeMCSection();
				} else if (mcSectionHandler.getLocalAddress() == null) {
					// create the node
					createMCSection();

					ParseTree localAddressRoot = new ParserRuleContext();
					mcSectionHandler.setLocalAddressRoot( localAddressRoot );
					ConfigTreeNodeUtilities.addChild( mcSectionHandler.getLastSectionRoot(), localAddressRoot ); 
					ConfigTreeNodeUtilities.addChild( localAddressRoot, new AddedParseTree("\nlocalAddress := ") );
					ParseTree localAddress = new AddedParseTree( temp.trim() );
					mcSectionHandler.setLocalAddress( localAddress );
					ConfigTreeNodeUtilities.addChild( localAddressRoot, localAddress );
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( mcSectionHandler.getLocalAddress(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( mcSectionHandler.getLocalAddress() );
				}
			}
		});
		if (mcSectionHandler != null && mcSectionHandler.getLocalAddress() != null) {
			localAddressText.setText(ConfigTreeNodeUtilities.toString(mcSectionHandler.getLocalAddress()));
		}

		toolkit.createLabel(client, "TCP port:");
		tcpPortText = toolkit.createText(client, "", SWT.SINGLE);
		tcpPortText.setEnabled(mcSectionHandler != null);
		tcpPortText.setLayoutData(new GridData(75, SWT.DEFAULT));
		tcpPortText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || mcSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = tcpPortText.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (mcSectionHandler.getTcpPortRoot() != null) {
						ConfigTreeNodeUtilities.removeChild(mcSectionHandler.getLastSectionRoot(),
								mcSectionHandler.getTcpPortRoot());
					}
					mcSectionHandler.setTcpPort(null);
					mcSectionHandler.setTcpPortRoot(null);

					removeMCSection();
				} else if (mcSectionHandler.getTcpPort() == null) {
					// create the node
					createMCSection();

					ParseTree tcpPortRoot = new ParserRuleContext();
					mcSectionHandler.setTcpPortRoot( tcpPortRoot );
					ConfigTreeNodeUtilities.addChild( mcSectionHandler.getLastSectionRoot(), tcpPortRoot ); 
					ConfigTreeNodeUtilities.addChild( tcpPortRoot, new AddedParseTree("\nTCPPort := ") );
					ParseTree tcpPort = new AddedParseTree( temp.trim() );
					mcSectionHandler.setTcpPort( tcpPort );
					ConfigTreeNodeUtilities.addChild( tcpPortRoot, tcpPort );
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( mcSectionHandler.getTcpPort(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( mcSectionHandler.getTcpPort() );
				}
			}
		});
		if (mcSectionHandler != null && mcSectionHandler.getTcpPort() != null) {
			tcpPortText.setText(ConfigTreeNodeUtilities.toString(mcSectionHandler.getTcpPort()));
		}

		toolkit.createLabel(client, "Kill timer:");
		killTimerText = toolkit.createText(client, "", SWT.SINGLE);
		killTimerText.setEnabled(mcSectionHandler != null);
		killTimerText.setLayoutData(new GridData(75, SWT.DEFAULT));
		killTimerText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || mcSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = killTimerText.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (mcSectionHandler.getKillTimerRoot() != null) {
						ConfigTreeNodeUtilities.removeChild(mcSectionHandler.getLastSectionRoot(),
								mcSectionHandler.getKillTimerRoot());
					}
					mcSectionHandler.setKillTimer(null);
					mcSectionHandler.setKillTimerRoot(null);

					removeMCSection();
				} else if (mcSectionHandler.getKillTimer() == null) {
					// create the node
					createMCSection();

					ParseTree killTimerRoot = new ParserRuleContext();
					mcSectionHandler.setKillTimerRoot( killTimerRoot );
					ConfigTreeNodeUtilities.addChild( mcSectionHandler.getLastSectionRoot(), killTimerRoot ); 
					ConfigTreeNodeUtilities.addChild( killTimerRoot, new AddedParseTree("\nkillTimer := ") );
					ParseTree killTimer = new AddedParseTree( temp.trim() );
					mcSectionHandler.setTcpPort( killTimer );
					ConfigTreeNodeUtilities.addChild( killTimerRoot, killTimer );
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( mcSectionHandler.getKillTimer(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( mcSectionHandler.getKillTimer() );
				}
			}
		});
		if (mcSectionHandler != null && mcSectionHandler.getKillTimer() != null) {
			killTimerText.setText(ConfigTreeNodeUtilities.toString(mcSectionHandler.getKillTimer()));
		}

		toolkit.createLabel(client, "Number of Host Contollers:");
		numHCsText = toolkit.createText(client, "", SWT.SINGLE);
		numHCsText.setEnabled(mcSectionHandler != null);
		numHCsText.setLayoutData(new GridData(75, SWT.DEFAULT));
		numHCsText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || mcSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = numHCsText.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (mcSectionHandler.getNumHCsTextRoot() != null) {
						ConfigTreeNodeUtilities.removeChild(mcSectionHandler.getLastSectionRoot(),
								mcSectionHandler.getNumHCsTextRoot());
					}
					mcSectionHandler.setNumHCsText(null);
					mcSectionHandler.setNumHCsTextRoot(null);

					removeMCSection();
				} else if (mcSectionHandler.getNumHCsText() == null) {
					// create the node
					createMCSection();

					ParseTree numHCsTextRoot = new ParserRuleContext();
					mcSectionHandler.setKillTimerRoot( numHCsTextRoot );
					ConfigTreeNodeUtilities.addChild( mcSectionHandler.getLastSectionRoot(), numHCsTextRoot ); 
					ConfigTreeNodeUtilities.addChild( numHCsTextRoot, new AddedParseTree("\nnumHCs := ") );
					ParseTree numHCsText = new AddedParseTree( temp.trim() );
					mcSectionHandler.setNumHCsText( numHCsText );
					ConfigTreeNodeUtilities.addChild( numHCsTextRoot, numHCsText );
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( mcSectionHandler.getNumHCsText(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( mcSectionHandler.getNumHCsText() );
				}
			}
		});
		if (mcSectionHandler != null && mcSectionHandler.getNumHCsText() != null) {
			numHCsText.setText(ConfigTreeNodeUtilities.toString(mcSectionHandler.getNumHCsText()));
		}

		toolkit.createLabel(client, "Use of unix domain socket communication:");
		unixDomainSocketText = new CCombo(client, SWT.FLAT);
		unixDomainSocketText.setEnabled(mcSectionHandler != null);
		unixDomainSocketText.setLayoutData(new GridData(75, SWT.DEFAULT));
		unixDomainSocketText.add("Yes");
		unixDomainSocketText.add("No");
		unixDomainSocketText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (valueChanged || mcSectionHandler == null) {
					return;
				}

				editor.setDirty();

				String temp = unixDomainSocketText.getText();
				if (temp == null || temp.length() == 0) {
					// remove the node
					if (mcSectionHandler.getUnixDomainSocketRoot() != null) {
						ConfigTreeNodeUtilities.removeChild(mcSectionHandler.getLastSectionRoot(),
								mcSectionHandler.getUnixDomainSocketRoot());
					}
					mcSectionHandler.setUnixDomainSocket(null);
					mcSectionHandler.setUnixDomainSocketRoot(null);

					removeMCSection();
				} else if (mcSectionHandler.getUnixDomainSocket() == null) {
					// create the node
					createMCSection();

					ParseTree unixDomainSocketRoot = new ParserRuleContext();
					mcSectionHandler.setUnixDomainSocketRoot( unixDomainSocketRoot );
					ConfigTreeNodeUtilities.addChild( mcSectionHandler.getLastSectionRoot(), unixDomainSocketRoot ); 
					ConfigTreeNodeUtilities.addChild( unixDomainSocketRoot, new AddedParseTree("\nUnixSocketsEnabled := ") );
					ParseTree unixDomainSocket = new AddedParseTree( temp.trim() );
					mcSectionHandler.setUnixDomainSocket( unixDomainSocket );
					ConfigTreeNodeUtilities.addChild( unixDomainSocketRoot, unixDomainSocket );
				} else {
					// simple modification
					ConfigTreeNodeUtilities.setText( mcSectionHandler.getNumHCsText(), temp.trim() );
					ConfigTreeNodeUtilities.removeChildren( mcSectionHandler.getNumHCsText() );
				}
			}
		});
		if (mcSectionHandler != null && mcSectionHandler.getUnixDomainSocket() != null) {
			unixDomainSocketText.setText(ConfigTreeNodeUtilities.toString(mcSectionHandler.getUnixDomainSocket()));
		}

		valueChanged = false;
	}

	private void internalRefresh() {
		if (mcSectionHandler == null) {
			localAddressText.setEnabled(false);
			tcpPortText.setEnabled(false);
			killTimerText.setEnabled(false);
			numHCsText.setEnabled(false);
			unixDomainSocketText.setEnabled(false);
			return;
		}

		valueChanged = true;

		localAddressText.setEnabled(true);
		tcpPortText.setEnabled(true);
		killTimerText.setEnabled(true);
		numHCsText.setEnabled(true);
		unixDomainSocketText.setEnabled(true);

		if (mcSectionHandler.getLocalAddress() != null) {
			localAddressText.setText(ConfigTreeNodeUtilities.toString(mcSectionHandler.getLocalAddress()).trim());
		}

		if (mcSectionHandler.getTcpPort() != null) {
			tcpPortText.setText(ConfigTreeNodeUtilities.toString(mcSectionHandler.getTcpPort()).trim());
		}

		if (mcSectionHandler.getKillTimer() != null) {
			killTimerText.setText(ConfigTreeNodeUtilities.toString(mcSectionHandler.getKillTimer()).trim());
		}

		if (mcSectionHandler.getNumHCsText() != null) {
			numHCsText.setText(ConfigTreeNodeUtilities.toString(mcSectionHandler.getNumHCsText()).trim());
		}

		if (mcSectionHandler.getUnixDomainSocket() != null) {
			unixDomainSocketText.setText(ConfigTreeNodeUtilities.toString(mcSectionHandler.getUnixDomainSocket()).trim());
		}

		valueChanged = false;
	}

	public void refreshData(final MCSectionHandler mcSectionHandler) {
		this.mcSectionHandler = mcSectionHandler;

		if (localAddressText != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
				}
			});
		}
	}

	// creates the maincontroller section if needed
	private void createMCSection() {
		if (mcSectionHandler == null || mcSectionHandler.getLastSectionRoot() != null) {
			return;
		}

		ParserRuleContext sectionRoot = new ParserRuleContext();
		mcSectionHandler.setLastSectionRoot( sectionRoot );
		ParseTree header = new AddedParseTree("\n[MAIN_CONTROLLER]");
		ConfigTreeNodeUtilities.addChild(sectionRoot, header);

		ParserRuleContext root = editor.getParseTreeRoot();
		if (root != null) {
			root.addChild(sectionRoot);
		}
	}

	// removes the main controller section if needed
	private void removeMCSection() {
		if (mcSectionHandler == null || mcSectionHandler.getLastSectionRoot() == null) {
			return;
		}

		if (mcSectionHandler.getLocalAddress() == null && mcSectionHandler.getTcpPort() == null && mcSectionHandler.getKillTimer() == null
				&& mcSectionHandler.getNumHCsText() == null && mcSectionHandler.getUnixDomainSocket() == null) {
			ConfigTreeNodeUtilities.removeChild(editor.getParseTreeRoot(), mcSectionHandler.getLastSectionRoot());
			mcSectionHandler.setLastSectionRoot(null);
		}
	}
}
