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
import org.eclipse.titan.common.parsers.CommonHiddenStreamToken;
import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler.LogParamEntry;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler.PluginSpecificParam;
import org.eclipse.titan.designer.editors.configeditor.ConfigEditor;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Adam Delic
 * */
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
				for (String pluginName : plugins) {
					LogParamEntry lpe = loggingSectionHandler.componentPlugin(tempComponentName, pluginName);
					removeLoggingComponents(lpe);
					removeFromPluginList(tempComponentName, pluginName);
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

		StringBuilder pluginBuilder = new StringBuilder();
		pluginBuilder.append(pluginName);
		if (path != null && path.length() != 0) {
			pluginBuilder.append(" := \"").append(path).append("\"");
		}

		LocationAST nextSibling = loggingSectionHandler.getLastSectionRoot().getNextSibling();
		LoggingSectionHandler.LoggerPluginsEntry entry = loggingSectionHandler.getLoggerPluginsTree().get(componentName);
		if (entry == null) {
			entry = new LoggingSectionHandler.LoggerPluginsEntry();
			loggingSectionHandler.getLoggerPluginsTree().put(componentName, entry);
			StringBuilder builder = new StringBuilder();
			builder.append("\n").append(componentName).append(".LoggerPlugins := ");
			entry.setLoggerPluginsRoot(new LocationAST(builder.toString()));
			loggingSectionHandler.getLastSectionRoot().setNextSibling(entry.getLoggerPluginsRoot());
			entry.setLoggerPluginsListRoot(new LocationAST("{"));
			entry.getLoggerPluginsRoot().setNextSibling(entry.getLoggerPluginsListRoot());

			LoggingSectionHandler.LoggerPluginEntry pluginEntry = new LoggingSectionHandler.LoggerPluginEntry();
			pluginEntry.setLoggerPluginRoot(new LocationAST(pluginBuilder.toString()));
			pluginEntry.setName(pluginName);
			pluginEntry.setPath(path);
			entry.setPluginRoots(new HashMap<String, LoggingSectionHandler.LoggerPluginEntry>(1));
			entry.getPluginRoots().put(pluginName, pluginEntry);
			entry.getLoggerPluginsListRoot().setNextSibling(pluginEntry.getLoggerPluginRoot());
			LocationAST temp = new LocationAST("}");
			pluginEntry.getLoggerPluginRoot().setNextSibling(temp);

			temp.setNextSibling(nextSibling);

			return;
		}

		LocationAST firstPlugin = entry.getLoggerPluginsListRoot().getNextSibling();
		if (firstPlugin == null) {
			entry.getLoggerPluginsListRoot().setNextSibling(new LocationAST(pluginBuilder.toString()));
			return;
		}

		LocationAST newPlugin = new LocationAST(pluginBuilder.toString() + ", ");
		entry.getLoggerPluginsListRoot().setNextSibling(newPlugin);
		newPlugin.setNextSibling(firstPlugin);
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

		LoggingSectionHandler.LoggerPluginsEntry entry = loggingSectionHandler.getLoggerPluginsTree().get(componentName);
		if (entry == null) {
			return;
		}

		if (!entry.getPluginRoots().containsKey(pluginName)) {
			return;
		}

		if (entry.getPluginRoots().size() == 1) {
			// if this is the last plugin entry, the whole entry has
			// to be removed
			entry.getLoggerPluginsRoot().setNextSibling(null);
			entry.getLoggerPluginsRoot().setText("");
			CommonHiddenStreamToken temp = entry.getLoggerPluginsRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				entry.getLoggerPluginsRoot().setHiddenBefore(null);
			}
		}

		LoggingSectionHandler.LoggerPluginEntry pluginEntry = entry.getPluginRoots().remove(pluginName);
		pluginEntry.getLoggerPluginRoot().setText("");
		LocationAST nextSibling = pluginEntry.getLoggerPluginRoot().getNextSibling();
		// if the ',' is after the item remove it from there
		if (nextSibling != null && ",".equals(nextSibling.getText())) {
			nextSibling.setText("");
			return;
		}

		// if the item is the last one in the list we have to remove the
		// ',' from before it.
		LocationAST actual = entry.getLoggerPluginsRoot();
		while (actual != null && !actual.equals(pluginEntry.getLoggerPluginRoot())) {
			nextSibling = actual.getNextSibling();
			if (",".equals(actual.getText()) && pluginEntry.getLoggerPluginRoot().equals(nextSibling)) {
				actual.setText("");
				return;
			}

			actual = nextSibling;
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
		if (logentry.getAppendFile() != null && logentry.getAppendFileRoot() != null) {
			logentry.setAppendFile(null);
			logentry.getAppendFileRoot().setNextSibling(null);
			logentry.getAppendFileRoot().setText("");
			CommonHiddenStreamToken temp = logentry.getAppendFileRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				logentry.getAppendFileRoot().setHiddenBefore(null);
			}
		}
		if (logentry.getConsoleMask() != null && logentry.getConsoleMaskRoot() != null) {
			logentry.setConsoleMask(null);
			logentry.getConsoleMaskRoot().setNextSibling(null);
			logentry.getConsoleMaskRoot().setText("");
			logentry.getConsoleMaskRoot().removeChildren();
			CommonHiddenStreamToken temp = logentry.getConsoleMaskRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				logentry.getConsoleMaskRoot().setHiddenBefore(null);
			}
		}
		if (logentry.getDiskFullAction() != null && logentry.getDiskFullActionRoot() != null) {
			logentry.setDiskFullAction(null);
			logentry.getDiskFullActionRoot().setNextSibling(null);
			logentry.getDiskFullActionRoot().setText("");
			CommonHiddenStreamToken temp = logentry.getDiskFullActionRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				logentry.getDiskFullActionRoot().setHiddenBefore(null);
			}
		}
		if (logentry.getFileMask() != null && logentry.getFileMaskRoot() != null) {
			logentry.setFileMask(null);
			logentry.getFileMaskRoot().setNextSibling(null);
			logentry.getFileMaskRoot().setText("");
			logentry.getFileMaskRoot().removeChildren();
			CommonHiddenStreamToken temp = logentry.getFileMaskRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				logentry.getFileMaskRoot().setHiddenBefore(null);
			}
		}
		if (logentry.getLogEntityName() != null && logentry.getLogEntityNameRoot() != null) {
			logentry.setLogEntityName(null);
			logentry.getLogEntityNameRoot().setNextSibling(null);
			logentry.getLogEntityNameRoot().setText("");
			CommonHiddenStreamToken temp = logentry.getLogEntityNameRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				logentry.getLogEntityNameRoot().setHiddenBefore(null);
			}
		}
		if (logentry.getLogeventTypes() != null && logentry.getLogeventTypesRoot() != null) {
			logentry.setLogeventTypes(null);
			logentry.getLogeventTypesRoot().setNextSibling(null);
			logentry.getLogeventTypesRoot().setText("");
			CommonHiddenStreamToken temp = logentry.getLogeventTypesRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				logentry.getLogeventTypesRoot().setHiddenBefore(null);
			}
		}
		if (logentry.getLogFile() != null && logentry.getLogFileRoot() != null) {
			logentry.setLogFile(null);
			logentry.getLogFileRoot().setNextSibling(null);
			logentry.getLogFileRoot().setText("");
			CommonHiddenStreamToken temp = logentry.getLogFileRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				logentry.getLogFileRoot().setHiddenBefore(null);
			}
		}
		if (logentry.getLogfileNumber() != null && logentry.getLogfileNumberRoot() != null) {
			logentry.setLogfileNumber(null);
			logentry.getLogfileNumberRoot().setNextSibling(null);
			logentry.getLogfileNumberRoot().setText("");
			CommonHiddenStreamToken temp = logentry.getLogfileNumberRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				logentry.getLogfileNumberRoot().setHiddenBefore(null);
			}
		}
		if (logentry.getLogfileSize() != null && logentry.getLogfileSizeRoot() != null) {
			logentry.setLogfileSize(null);
			logentry.getLogfileSizeRoot().setNextSibling(null);
			logentry.getLogfileSizeRoot().setText("");
			CommonHiddenStreamToken temp = logentry.getLogfileSizeRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				logentry.getLogfileSizeRoot().setHiddenBefore(null);
			}
		}
		if (logentry.getMatchingHints() != null && logentry.getMatchingHintsRoot() != null) {
			logentry.setMatchingHints(null);
			logentry.getMatchingHintsRoot().setNextSibling(null);
			logentry.getMatchingHintsRoot().setText("");
			CommonHiddenStreamToken temp = logentry.getMatchingHintsRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				logentry.getMatchingHintsRoot().setHiddenBefore(null);
			}
		}
		if (logentry.getSourceInfoFormat() != null && logentry.getSourceInfoFormatRoot() != null) {
			logentry.setSourceInfoFormat(null);
			logentry.getSourceInfoFormatRoot().setNextSibling(null);
			logentry.getSourceInfoFormatRoot().setText("");
			CommonHiddenStreamToken temp = logentry.getSourceInfoFormatRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				logentry.getSourceInfoFormatRoot().setHiddenBefore(null);
			}
		}
		if (logentry.getTimestampFormat() != null && logentry.getTimestampFormatRoot() != null) {
			logentry.setTimestampFormat(null);
			logentry.getTimestampFormatRoot().setNextSibling(null);
			logentry.getTimestampFormatRoot().setText("");
			CommonHiddenStreamToken temp = logentry.getTimestampFormatRoot().getHiddenBefore();
			if (temp != null && "\n".equals(temp.getText())) {
				logentry.getTimestampFormatRoot().setHiddenBefore(null);
			}
		}
		Iterator<PluginSpecificParam> pspit = logentry.getPluginSpecificParam().iterator();
		while (pspit.hasNext()) {
			PluginSpecificParam psp = pspit.next();
			psp.getRoot().setNextSibling(null);
			psp.getRoot().setText("");
			psp.getRoot().removeChildren();
			psp.setParamName(null);
		}

		loggingPage.removeLoggingSection();
		loggingPage.refreshData(loggingSectionHandler);
	}
}
