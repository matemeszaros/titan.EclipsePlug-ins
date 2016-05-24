/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.logging;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
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
 * @author Adam Delic
 * @author Arpad Lovassy
 */
public final class LoggingTreeSubPage {

	private TreeViewer componentpluginViewer;
	private Tree componentpluginTree;
	private Button addComponent;
	private Button addPlugin;
	private Button removeSelected;

	private ConfigEditor editor;
	private LoggingPage loggingPage;
	private LoggingSectionHandler loggingSectionHandler;

	public LoggingTreeSubPage(final ConfigEditor editor, final LoggingPage loggingPage) {
		this.editor = editor;
		this.loggingPage = loggingPage;
	}

	void createSectionComponent(final FormToolkit toolkit, final Composite parent) {

		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		section.setText("Components and plugins");
		section.setDescription("In this section you can manage your components and plugins. For each component and plugin different"
				+ " log setting are available on the right section of the page. The settings of the default component/plugin are"
				+ " valid for all components/plugins unless it is overriden by component/plugin specific settings.");

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		client.setLayout(layout);
		section.setClient(client);

		toolkit.paintBordersFor(client);

		createMainPart(toolkit, client);
	}

	private void createMainPart(final FormToolkit toolkit, final Composite parent) {
		Composite components = toolkit.createComposite(parent, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		components.setLayout(layout);
		components.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		toolkit.paintBordersFor(components);

		componentpluginTree = toolkit.createTree(components, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
		componentpluginTree.setLayoutData(gd);
		componentpluginTree.setEnabled(loggingSectionHandler != null);
		componentpluginViewer = new TreeViewer(componentpluginTree);
		componentpluginViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				selectionRefresh();
			}
		});

		// componentpluginTree.setLinesVisible(true);
		// componentpluginTree.setHeaderVisible(true);

		componentpluginViewer.setContentProvider(new LoggerTreeContentProvider());
		componentpluginViewer.setLabelProvider(new LoggerTreeLabelProvider());
		componentpluginViewer.setInput(loggingSectionHandler);

		Composite buttons = toolkit.createComposite(components);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL));

		addComponent = toolkit.createButton(buttons, "Add component...", SWT.PUSH);
		GridData gdButton = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		addComponent.setLayoutData(gdButton);
		addComponent.setEnabled(loggingSectionHandler != null);
		addComponent.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (loggingSectionHandler == null) {
					return;
				}

				if (loggingSectionHandler.getLastSectionRoot() == null) {
					loggingPage.createLoggingSection();
				}

				NewComponentDialog dialog = new NewComponentDialog(addComponent.getShell(), loggingSectionHandler.getComponents());
				if (Window.OK == dialog.open()) {
					String name = dialog.getName();
					if (name != null) {
						LogParamEntry lpe = loggingSectionHandler.componentPlugin(name, null);
						internalRefresh();
						loggingPage.treeElementAdded(new LoggingSectionHandler.LoggerTreeElement(loggingSectionHandler, name,
								null), lpe);
						editor.setDirty();
					}
				}
			}
		});

		addPlugin = toolkit.createButton(buttons, "Add plugin...", SWT.PUSH);
		gdButton = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		addPlugin.setLayoutData(gdButton);
		addPlugin.setEnabled(loggingSectionHandler != null);
		addPlugin.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (loggingSectionHandler == null) {
					return;
				}

				if (loggingSectionHandler.getLastSectionRoot() == null) {
					loggingPage.createLoggingSection();
				}

				LoggingSectionHandler.LoggerTreeElement lte = getSelection();
				if (lte != null) {
					NewPluginDialog dialog = new NewPluginDialog(addPlugin.getShell(), loggingSectionHandler.getPlugins(lte
							.getComponentName()));
					if (Window.OK == dialog.open()) {
						String name = dialog.getName();
						String path = dialog.getPath();
						if (name != null) {
							LogParamEntry lpe = loggingSectionHandler.componentPlugin(lte.getComponentName(), name);
							lpe.setPluginPath(path);
							addPluginToList(lte.getComponentName(), name, path);
							internalRefresh();
							loggingPage.treeElementAdded(new LoggingSectionHandler.LoggerTreeElement(
									loggingSectionHandler, lte.getComponentName(), name), lpe);
							editor.setDirty();
						}
					}
				}
			}
		});

		removeSelected = toolkit.createButton(buttons, "Remove selected", SWT.PUSH);
		gdButton = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		removeSelected.setLayoutData(gdButton);
		removeSelected.setEnabled(loggingSectionHandler != null);
		removeSelected.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (componentpluginViewer == null || loggingSectionHandler == null) {
					return;
				}

				if (loggingSectionHandler.getComponents().isEmpty()) {
					loggingPage.removeLoggingSection();
				}
				LoggingSectionHandler.LoggerTreeElement lte = getSelection();
				if (lte == null || "*".equals(lte.getPluginName())) {
					return;
				}

				String tempComponentName = lte.getComponentName();
				if (tempComponentName == null) {
					tempComponentName = "*";
				}

				Set<String> plugins = loggingSectionHandler.getPlugins(tempComponentName);
				String tempPluginName = lte.getPluginName();
				if(plugins.contains(tempPluginName)) {
					LogParamEntry lpe = loggingSectionHandler.componentPlugin(tempComponentName, tempPluginName);
					removeLoggingComponents(lpe);
					removeFromPluginList(tempComponentName, tempPluginName);
				}

				loggingSectionHandler.removeTreeElement(lte);

				internalRefresh();
				loggingPage.refreshData(loggingSectionHandler, null);
				editor.setDirty();
			}

		});
	}

	public LoggingSectionHandler.LoggerTreeElement getSelection() {
		ITreeSelection selection = (ITreeSelection) componentpluginViewer.getSelection();
		Object o = selection.getFirstElement();
		return (LoggingSectionHandler.LoggerTreeElement) o;
	}

	private void internalRefresh() {
		addComponent.setEnabled(loggingSectionHandler != null);
		addPlugin.setEnabled(loggingSectionHandler != null);
		removeSelected.setEnabled(loggingSectionHandler != null);
		componentpluginTree.setEnabled(loggingSectionHandler != null);
		componentpluginViewer.setInput(loggingSectionHandler);
	}

	private void selectionRefresh() {
		LoggingSectionHandler.LoggerTreeElement lte = loggingPage.getSelectedTreeElement();
		if (lte != null) {
			loggingPage.treeElementSelected(lte);
		}
	}

	public void refreshData(final LoggingSectionHandler loggingSectionHandler) {
		this.loggingSectionHandler = loggingSectionHandler;

		if (componentpluginViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
					selectionRefresh();
				}
			});
		} else {
			loggingPage.refreshData(loggingSectionHandler, null);
		}
	}

	/**
	 * Adds a new logging plugin to the list of logging plugins available
	 * for a given component. If necessary also creates the list of logging
	 * plugins.
	 * 
	 * @param componentName
	 *                the name of the component to add the plugin to.
	 * @param pluginName
	 *                the name of the plugin to add
	 * @param path
	 *                the path of the plugin to add, or null if none.
	 * */
	private void addPluginToList(final String componentName, final String pluginName, final String path) {
		if (loggingSectionHandler == null) {
			return;
		}

		final StringBuilder pluginBuilder = new StringBuilder();
		pluginBuilder.append(pluginName);
		if (path != null && path.length() != 0) {
			pluginBuilder.append(" := \"").append(path).append("\"");
		}

		/*
		 *   loggingSectionHandler.getLastSectionRoot()
		 *     entry.getLoggerPluginsRoot()
		 *       entry.getLoggerPluginsListRoot()
		 *         {
		 *           pluginEntry.getLoggerPluginRoot()
		 *         }
		 */
		LoggingSectionHandler.LoggerPluginsEntry entry = loggingSectionHandler.getLoggerPluginsTree().get(componentName);
		if (entry == null) {
			entry = new LoggingSectionHandler.LoggerPluginsEntry();
			loggingSectionHandler.getLoggerPluginsTree().put(componentName, entry);
			final ParseTree loggerPluginsRoot = new ParserRuleContext();
			ConfigTreeNodeUtilities.addChild( loggingSectionHandler.getLastSectionRoot(), loggerPluginsRoot ); 
			entry.setLoggerPluginsRoot( loggerPluginsRoot );
			
			final StringBuilder builder = new StringBuilder();
			builder.append("\n").append(componentName).append(".LoggerPlugins := ");
			ConfigTreeNodeUtilities.addChild( loggerPluginsRoot, new AddedParseTree( builder.toString() ) ); 
			ConfigTreeNodeUtilities.addChild( loggerPluginsRoot, new AddedParseTree("{") );
			
			final ParseTree loggerPluginsListRoot = new ParserRuleContext();
			entry.setLoggerPluginsListRoot( loggerPluginsListRoot );

			final LoggingSectionHandler.LoggerPluginEntry pluginEntry = new LoggingSectionHandler.LoggerPluginEntry();
			final ParseTree pluginRoot = new ParserRuleContext();
			pluginEntry.setLoggerPluginRoot( pluginRoot );
			pluginEntry.setName(pluginName);
			pluginEntry.setPath(path);
			ConfigTreeNodeUtilities.addChild( pluginRoot, new AddedParseTree( pluginBuilder.toString() ) );
			entry.setPluginRoots(new HashMap<String, LoggingSectionHandler.LoggerPluginEntry>(1));
			entry.getPluginRoots().put(pluginName, pluginEntry);
			ConfigTreeNodeUtilities.addChild( loggerPluginsListRoot, pluginRoot );
			
			ConfigTreeNodeUtilities.addChild( loggerPluginsRoot, loggerPluginsListRoot );
			ConfigTreeNodeUtilities.addChild( loggerPluginsRoot, new AddedParseTree("}") );
			return;
		}

		final int childCount = entry.getLoggerPluginsListRoot().getChildCount();
		ConfigTreeNodeUtilities.addChild( entry.getLoggerPluginsListRoot(), new AddedParseTree( ( childCount > 0 ? ", " : "" ) + pluginBuilder.toString() ) );
	}

	/**
	 * Removes a plugin from the list of plugins active on a given
	 * component. If necessary removes the whole list too.
	 * 
	 * @param componentName
	 *                the name of the component where the plugin should be
	 *                deleted.
	 * @param pluginName
	 *                the name of the plugin to be deleted.
	 * */
	private void removeFromPluginList(final String componentName, final String pluginName) {
		if (loggingSectionHandler == null) {
			return;
		}

		final LoggingSectionHandler.LoggerPluginsEntry entry = loggingSectionHandler.getLoggerPluginsTree().get(componentName);
		if (entry == null) {
			return;
		}

		if (!entry.getPluginRoots().containsKey(pluginName)) {
			return;
		}

		LoggingSectionHandler.LoggerPluginEntry pluginEntry = entry.getPluginRoots().remove(pluginName);
		ConfigTreeNodeUtilities.removeChild( entry.getLoggerPluginsListRoot(), pluginEntry.getLoggerPluginRoot() );

		if (entry.getPluginRoots().size() == 0) {
			// if this was the last plugin entry, the whole entry has to be removed
			ConfigTreeNodeUtilities.removeChild( loggingSectionHandler.getLastSectionRoot(), entry.getLoggerPluginsRoot() );
		}
	}

	/**
	 * Remove the provided log entry (containing information on a logging
	 * component) from the tree built of the configuration file, so that
	 * once we write back the tree its nodes will be missing.
	 * 
	 * @param logentry
	 *                the entry to be removed.
	 * */
	private void removeLoggingComponents(final LogParamEntry logentry) {
		final ParseTree lastSectionRoot = loggingSectionHandler.getLastSectionRoot();
		if (logentry.getAppendFile() != null && logentry.getAppendFileRoot() != null) {
			logentry.setAppendFile(null);
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, logentry.getAppendFileRoot() );
		}
		if (logentry.getConsoleMask() != null && logentry.getConsoleMaskRoot() != null) {
			logentry.setConsoleMask(null);
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, logentry.getConsoleMaskRoot() );
		}
		if (logentry.getDiskFullAction() != null && logentry.getDiskFullActionRoot() != null) {
			logentry.setDiskFullAction(null);
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, logentry.getDiskFullActionRoot() );
		}
		if (logentry.getFileMask() != null && logentry.getFileMaskRoot() != null) {
			logentry.setFileMask(null);
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, logentry.getFileMaskRoot() );
		}
		if (logentry.getLogEntityName() != null && logentry.getLogEntityNameRoot() != null) {
			logentry.setLogEntityName(null);
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, logentry.getLogEntityNameRoot() );
		}
		if (logentry.getLogeventTypes() != null && logentry.getLogeventTypesRoot() != null) {
			logentry.setLogeventTypes(null);
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, logentry.getLogeventTypesRoot() );
		}
		if (logentry.getLogFile() != null && logentry.getLogFileRoot() != null) {
			logentry.setLogFile(null);
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, logentry.getLogFileRoot() );
		}
		if (logentry.getLogfileNumber() != null && logentry.getLogfileNumberRoot() != null) {
			logentry.setLogfileNumber(null);
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, logentry.getLogfileNumberRoot() );
		}
		if (logentry.getLogfileSize() != null && logentry.getLogfileSizeRoot() != null) {
			logentry.setLogfileSize(null);
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, logentry.getLogfileSizeRoot() );
		}
		if (logentry.getMatchingHints() != null && logentry.getMatchingHintsRoot() != null) {
			logentry.setMatchingHints(null);
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, logentry.getMatchingHintsRoot() );
		}
		if (logentry.getSourceInfoFormat() != null && logentry.getSourceInfoFormatRoot() != null) {
			logentry.setSourceInfoFormat(null);
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, logentry.getSourceInfoFormatRoot() );
		}
		if (logentry.getTimestampFormat() != null && logentry.getTimestampFormatRoot() != null) {
			logentry.setTimestampFormat(null);
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, logentry.getTimestampFormatRoot() );
		}
		Iterator<PluginSpecificParam> pspit = logentry.getPluginSpecificParam().iterator();
		while (pspit.hasNext()) {
			PluginSpecificParam psp = pspit.next();
			ConfigTreeNodeUtilities.removeChild( lastSectionRoot, psp.getRoot() );
			psp.setParamName(null);
		}

		loggingPage.removeLoggingSection();
		loggingPage.refreshData(loggingSectionHandler);
	}
}
