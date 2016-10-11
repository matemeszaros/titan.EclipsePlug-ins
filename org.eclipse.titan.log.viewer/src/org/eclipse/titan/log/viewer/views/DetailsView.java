/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.console.TITANDebugConsole;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.preferences.PreferencesHandler;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.details.DetailData;
import org.eclipse.titan.log.viewer.views.details.TextViewComposite;
import org.eclipse.titan.log.viewer.views.details.TreeViewComposite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * This class shows data obtained from the
 * tree model. 
 */
public class DetailsView extends ViewPart implements ILogViewerView {
	private DetailData currentEventObject;
	private URI fullPath;
	private String projectName;
	private String testCaseName;
	private IMemento memento;

	private static ImageDescriptor imgTextView = Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_TEXT_VIEW);
	private static ImageDescriptor imgTreeView = Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_GRAPH_VIEW);

	private Composite stackComposite;
	private final FillLayout fillLayout = new FillLayout();
	private final StackLayout layout = new StackLayout();
	private TreeViewComposite treeView;
	private TextViewComposite textView;

	private boolean useFormatting;
	private boolean treeVisible;

	/** True if the view mode has been changed since the opening of this view. */
	private boolean viewModeChanged = false;
	private IAction setTreeModeAction;
	private IAction setTextModeAction;
	private LogFileMetaData logFileMetaData;

	/**
	 * The constructor.
	 */
	public DetailsView() {
		useFormatting = false;
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(final Composite parent) {
		// Set parent layout, one column
		parent.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		parent.setLayout(new GridLayout(1, false));
		
		// Create tool bar
		createToolbar();

		// Create stack composite 
		this.stackComposite = new Composite(parent, SWT.NULL);
		this.stackComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
		this.stackComposite.setLayout(layout);
		this.stackComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// Initialize top control
		layout.topControl = getComposite();
		restoreState();
	}

	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		init(site);
		this.memento = memento;
	}

	/**
	 * Saves the state of the View
	 * @param memento the callback memento object
	 */
	@Override
	public void saveState(final IMemento memento) {
		// do not save empty views
		if (currentEventObject == null) {
			return;
		}

		try {
			IMemento childMemento = memento.createChild("selection"); //$NON-NLS-1$
			IMemento mementoEventObject = childMemento.createChild("eventObject"); //$NON-NLS-1$
			mementoEventObject.putString("name", currentEventObject.getName()); //$NON-NLS-1$
			mementoEventObject.putString("port", currentEventObject.getPort()); //$NON-NLS-1$
			mementoEventObject.putString("line", currentEventObject.getLine()); //$NON-NLS-1$
			mementoEventObject.putString("projectName", logFileMetaData.getProjectName()); //$NON-NLS-1$
			mementoEventObject.putString("fullPath", logFileMetaData.getFilePath().toString()); //$NON-NLS-1$
			mementoEventObject.putString("testCaseName", currentEventObject.getTestCaseName()); //$NON-NLS-1$
			mementoEventObject.putString("eventType", currentEventObject.getEventType()); //$NON-NLS-1$
			mementoEventObject.putString("sourceInfo", currentEventObject.getSourceInfo()); //$NON-NLS-1$

			mementoEventObject.putInteger("isSilentEvent", useFormatting ? 1 : 0);
			
			// save state about log file
			Path filePath = new Path(logFileMetaData.getProjectRelativePath());
			IFile logFile = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
			if (logFile != null && logFile.exists()) {
				File aLogFile = logFile.getLocation().toFile();
				
				// add property file to the memento
				mementoEventObject.putString("propertyFile", LogFileCacheHandler.getPropertyFileForLogFile(logFile).getAbsolutePath()); //$NON-NLS-1$
				mementoEventObject.putString("fileSize", String.valueOf(aLogFile.length())); //$NON-NLS-1$
				mementoEventObject.putString("fileModification", String.valueOf(aLogFile.lastModified())); //$NON-NLS-1$
			}
			
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * Gets the original test case full path
	 * @return Full path to the file name that contains the message
	 */
	public URI getFullPath() {
		return fullPath;
	}

	/**
	 * Set data for tree model from MSCView
	 * @param newEventObject the new data
	 */
	public void setData(final DetailData newEventObject, final boolean useFormatting) {
		
		if (newEventObject != null) {
			this.currentEventObject = newEventObject;
			this.fullPath = logFileMetaData.getFilePath();
			this.projectName = logFileMetaData.getProjectName();
			this.testCaseName = newEventObject.getTestCaseName();
		} else {
			this.currentEventObject = null;
		}
		this.useFormatting = useFormatting;

		// Check if input data is silent mode or not 
		
		textView = getTextView();
		if (this.useFormatting) {
			textView.setUseFormatting(false);
			setTextModeAction.run();
			setTreeModeAction.setEnabled(false);
		} else {
			textView.setUseFormatting(true);
			// Update input for text and tree views (if created)
			if (isTreeVisibleNow()) { // tree mode
				setTreeModeAction.run();
			} else { // text mode
				setTextModeAction.run();
			}
		}
	}

	/**
	 * Returns the associated project name
	 * 
	 * @return the project name
	 */
	public String getProjectName() {
		return projectName;
	}
	
	/**
	 * Returns the associated test case name
	 * 
	 * @return the test case name
	 */
	public String getTestCaseName() {
		return testCaseName;
	}

	/**
	 * The dispose method for the view
	 * Clean up off all resources etc should be here
	 */
	@Override
	public void dispose() {
		if (Constants.DEBUG) {
			TITANDebugConsole.getConsole().newMessageStream().println("dispose Detail view " + getPartName()); //$NON-NLS-1$
		}
		if (treeView != null && !treeView.isDisposed()) {
			treeView.dispose();
		}
		if (textView != null && !textView.isDisposed()) {
			textView.dispose();
		}
		super.dispose();
	}
	
	@Override
	public void setFocus() {
		// Do nothing
	}
	
	/**
	 * Create a Tree mode and text mode action in the tool bar
	 */
	private void createToolbar() {
		// Action for setting tree mode
		setTreeModeAction = new Action() {
			@Override
			public void run() {
				treeView = getTreeView();
				layout.topControl = treeView;
				stackComposite.layout();
				viewModeChanged = true;
				treeVisible = true;
				setTreeModeAction.setEnabled(false);
				setTextModeAction.setEnabled(true);
				treeView.inputChanged(currentEventObject);
				updateHeader();
			}
		};
		setTreeModeAction.setToolTipText(Messages.getString("DetailsView.0")); //$NON-NLS-1$
		setTreeModeAction.setImageDescriptor(imgTreeView);
		setTreeModeAction.setEnabled(!treeVisible);

		// Action for setting text mode
		setTextModeAction = new Action() {
			@Override
			public void run() {
				textView = getTextView();
				layout.topControl = textView;
				stackComposite.layout();
				viewModeChanged = true;
				treeVisible = false;
				setTextModeAction.setEnabled(false);
				setTreeModeAction.setEnabled(true);
				textView.setLogFileMetaData(logFileMetaData);
				textView.inputChanged(currentEventObject);
				updateHeader();
			}
		};
		setTextModeAction.setToolTipText(Messages.getString("DetailsView.1")); //$NON-NLS-1$
		setTextModeAction.setImageDescriptor(imgTextView);
		setTextModeAction.setEnabled(treeVisible);

		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(setTreeModeAction);
		mgr.add(setTextModeAction);
	}

	/**
	 * Returns the composite to show (on top)
	 * @return the composite to show
	 */
	private Composite getComposite() {
		if (isTreeVisibleNow()) {
			return getTreeView();
		}
		return getTextView();
	}
	
	/**
	 * Returns the graph view composite
	 * @return the graph view composite
	 */
	private TreeViewComposite getTreeView() {
		// Lazy initialize
		if (treeView == null) {
			treeView = new TreeViewComposite(stackComposite, getViewSite());
			treeView.setLayout(fillLayout);
			treeView.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		}
		return treeView;
	}

	/**
	 * Returns the text view composite
	 * @return the text view composite
	 */
	private TextViewComposite getTextView() {
		// Lazy initialize
		if (textView == null) {
			textView = new TextViewComposite(stackComposite);
			textView.setLogFileMetaData(logFileMetaData);
			textView.setLayout(fillLayout);
			textView.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		}
		return textView;
	}
	
	/**
	 * Updates the header of the value view 
	 */
	private void updateHeader() {
		if (this.currentEventObject != null) { // tree 
			String header = this.logFileMetaData.getProjectRelativePath();
			String testCaseName = this.currentEventObject.getTestCaseName();
			if (testCaseName.length() > 0) {
				header = header + "  -  " + testCaseName; //$NON-NLS-1$
			}
			String name = this.currentEventObject.getName();
            if (name != null && name.trim().length() > 0) {
            	header = header + " - " + name; //$NON-NLS-1$
            }
			String eventType = this.currentEventObject.getEventType();
            if (eventType != null && eventType.trim().length() > 0) {
            	header = header + " - " + eventType; //$NON-NLS-1$
            }
            this.setContentDescription(header);
		} else {
			// Clear header
			this.setContentDescription(""); //$NON-NLS-1$
		}
	}
	
	/**
	 * Called in the view life-cycle restore chain
	 * Reads back all view data if memento has been set
	 * 
	 * The restore is very restricted an checks that the
	 * <li> Project still exists and is open
	 * <li> The file is within the project
	 * <li> The file size and file date has not changed
	 */
	private void restoreState() {
		if (memento == null) {
			return;
		}

		memento = memento.getChild("selection"); //$NON-NLS-1$
		if (memento != null) {
			DetailData detailObject = new DetailData();
			try {
				IMemento mementoEventObject = memento.getChild("eventObject"); //$NON-NLS-1$
				detailObject.setName(mementoEventObject.getString("name")); //$NON-NLS-1$
				detailObject.setPort(mementoEventObject.getString("port")); //$NON-NLS-1$
				detailObject.setLine(mementoEventObject.getString("line")); //$NON-NLS-1$
				detailObject.setTestCase(mementoEventObject.getString("testCaseName")); //$NON-NLS-1$
				detailObject.setEventType(mementoEventObject.getString("eventType")); //$NON-NLS-1$
				detailObject.setSourceInfo(mementoEventObject.getString("sourceInfo")); //$NON-NLS-1$
				
				long fileSize = Long.parseLong(mementoEventObject.getString("fileSize")); //$NON-NLS-1$
				long fileModification = Long.parseLong(mementoEventObject.getString("fileModification")); //$NON-NLS-1$
				
				//retrieve logfilemetaData
				String propertyFilePath = mementoEventObject.getString("propertyFile"); //$NON-NLS-1$
				if (propertyFilePath != null) {
					File propertyFile = new File(propertyFilePath);
					if (!propertyFile.exists()) {
						return;
					}
					logFileMetaData = LogFileCacheHandler.logFileMetaDataReader(propertyFile);
				}

				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(logFileMetaData.getProjectName());
				if (project != null
						&& project.exists() && project.isOpen()) {
					Path path = new Path(logFileMetaData.getProjectRelativePath());
					IFile logFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
					if (logFile != null && logFile.getProject().getName().equals(project.getName()) && logFile.exists()) {
						File file = logFile.getLocation().toFile();
						if (file.lastModified() == fileModification && file.length() == fileSize) {

							Integer silentEvent = mementoEventObject.getInteger("isSilentEvent"); //$NON-NLS-1$

							this.textView = getTextView();
							if (silentEvent == 1) {
								this.useFormatting = true;
								this.textView.setUseFormatting(false);
							} else {
								this.useFormatting = false;
								this.textView.setUseFormatting(true);
							}

							setData(detailObject, this.useFormatting);
						}
					}
				}
			} catch (final IOException e) {
				ErrorReporter.logExceptionStackTrace(e);
			} catch (final ClassNotFoundException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		memento = null;
	}

	private boolean isTreeVisibleNow() {
		if (viewModeChanged) {
			return treeVisible;
		}

		return isTreeViewPreferred();
	}

	/**
	 * Reads the preferred view from the preference store.
	 * 
	 * @return true if the Tree view is preferred
	 */
	private boolean isTreeViewPreferred() {
		String selectedView = null;
		if (this.logFileMetaData != null) {
			String currProjName = logFileMetaData.getProjectName();
			selectedView = PreferencesHandler.getInstance().getPreferences(currProjName).getSelectedValueContentType();
		}
		if (selectedView == null) {
			// problem while getting preferences -> return default (which is tree view)
			return true;
		}
		if (selectedView.contentEquals(PreferenceConstants.ASN1_TREEVIEW)) { // Tree view
			return true;
		}

		return false;	// Text view
	}

	public void setLogFileMetaData(final LogFileMetaData logFileMetaData) {
		this.logFileMetaData = logFileMetaData;
	}

	@Override
	public LogFileMetaData getLogFileMetaData() {
		return logFileMetaData;
	}

	@Override
	public String getName() {
		return "Details View";
	}
}
