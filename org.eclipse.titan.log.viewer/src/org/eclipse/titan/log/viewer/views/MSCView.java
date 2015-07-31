/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.extractors.TestCaseExtractor;
import org.eclipse.titan.log.viewer.models.FilterPattern;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.models.LogRecordIndex;
import org.eclipse.titan.log.viewer.models.TimeInterval;
import org.eclipse.titan.log.viewer.parsers.Parser;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.preferences.DecipheringPreferenceHandler;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.preferences.PreferencesHandler;
import org.eclipse.titan.log.viewer.preferences.PreferencesHolder;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.msc.model.ExecutionModel;
import org.eclipse.titan.log.viewer.views.msc.model.IEventObject;
import org.eclipse.titan.log.viewer.views.msc.model.MSCModel;
import org.eclipse.titan.log.viewer.views.msc.ui.actions.JumpToNextSetverdictAction;
import org.eclipse.titan.log.viewer.views.msc.ui.actions.JumpToPreviousSetverdictAction;
import org.eclipse.titan.log.viewer.views.msc.ui.actions.OpenSourceAction;
import org.eclipse.titan.log.viewer.views.msc.ui.actions.OpenTextTableAction;
import org.eclipse.titan.log.viewer.views.msc.ui.actions.OpenValueViewAction;
import org.eclipse.titan.log.viewer.views.msc.ui.actions.RefreshMSCViewAction;
import org.eclipse.titan.log.viewer.views.msc.ui.actions.ZoomAction;
import org.eclipse.titan.log.viewer.views.msc.ui.core.Frame;
import org.eclipse.titan.log.viewer.views.msc.ui.core.Lifeline;
import org.eclipse.titan.log.viewer.views.msc.ui.view.MSCWidget;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;
import org.eclipse.titan.log.viewer.views.text.table.TextTableView;
import org.eclipse.titan.log.viewer.views.text.table.TextTableViewHelper;

public class MSCView extends ViewPart implements ILogViewerView {
	
	private MSCWidget mscWidget = null;
	private MenuManager menuMgr = null;
	private LogFileMetaData logFileMetaData = null;
	private ExecutionModel model;
	private IAction refresh;
	private IAction jumpToNextSetverdict;
	private IAction jumpToPreviousSetverdict;
	private OpenValueViewAction openValueView;
	private OpenValueViewAction silentOpenValueView;
	private IAction openTextTable;
	private OpenSourceAction openSource;
	private OpenSourceAction silentOpenSource;
	private IMemento memento;
	private boolean problemDuringRestore = false;
	private Integer restoredSelection;
	private IAction filterAction;
	private FilterPattern filterPattern;
	
	public MSCView() {
		super();
		filterAction = new Action() {
			@Override
			public void run() {
				MSCFilterDialog dialog;
				if (model.getFilterPattern() != null) {	// the model is filtered
					dialog = new MSCFilterDialog(MSCView.this.getSite().getShell(), model.getFilterPattern());
					if (dialog.open() == 0 && dialog.getChanged()
							&& !model.getFilterPattern().equals(dialog.getFilterPattern())) {
						
						MSCView.this.filterPattern = dialog.getFilterPattern();
						MSCView.this.refresh.run();
					}
				} else {
					FilterPattern pattern = new FilterPattern(loadEventsFromPreferences(), true, true);
					pattern.setTimeInterval(new TimeInterval("", "", MSCView.this.getLogFileMetaData().getTimeStampFormat()));
					dialog = new MSCFilterDialog(MSCView.this.getSite().getShell(), pattern);
					if (dialog.open() == 0 && dialog.getChanged()) {
						MSCView.this.filterPattern = dialog.getFilterPattern();
						MSCView.this.refresh.run();
					}
				}
			}
		};
		
		filterAction.setImageDescriptor(Activator.getImageDescriptor(Constants.ICONS_FILTER));
		filterAction.setText("Filter");
	}
	
	@Override
	public void saveState(final IMemento memento) {
		// Do not save empty views
		if (this.model == null) {
			return;
		}

		IMemento tempMemento = memento.createChild("mscview"); //$NON-NLS-1$

		try {
			IMemento viewAttributes = tempMemento.createChild("attributes"); //$NON-NLS-1$
			// Save state to be able to restore logfilemetaData
			Path filePath = new Path(this.logFileMetaData.getProjectRelativePath());
			IFile logFile = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);

			// Store project name
			viewAttributes.putString("projectName", this.logFileMetaData.getProjectName()); //$NON-NLS-1$

			if ((logFile != null) && logFile.exists()) {
				// Store property file
				viewAttributes.putString("propertyFile", LogFileCacheHandler.getPropertyFileForLogFile(logFile).getAbsolutePath()); //$NON-NLS-1$
				File aLogFile = logFile.getLocation().toFile();
				viewAttributes.putString("fileSize", String.valueOf(aLogFile.length())); //$NON-NLS-1$
				viewAttributes.putString("fileModification", String.valueOf(aLogFile.lastModified())); //$NON-NLS-1$
			}

			// Store test case number
			TestCase testCase = this.model.getTestCase();
			viewAttributes.putInteger("testCaseNumber", testCase.getTestCaseNumber()); //$NON-NLS-1$
			
			// Store current selection
			if (this.mscWidget.getFrame() != null) {
				viewAttributes.putInteger("rowSelection", this.mscWidget.getFrame().getSelectedLine()); //$NON-NLS-1$
			}

		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * Called in the view life-cycle restore chain Reads back all view data if
	 * memento has been set
	 * 
	 * The restore is very restricted an checks that the
	 * <li> Project still exists and is open
	 * <li> The file is within the project
	 * <li> The file size and file date has not changed
	 */
	private WorkspaceJob restoreState() {
		if (this.memento == null) {
			return null;
		}

		WorkspaceJob job = null;
		this.problemDuringRestore = true;
		this.memento = this.memento.getChild("mscview"); //$NON-NLS-1$
		if (this.memento != null) {
			try {
				IMemento viewAttributes = this.memento.getChild("attributes"); //$NON-NLS-1$
				
				// Restore logfilemetaData
				String propertyFilePath = viewAttributes.getString("propertyFile"); //$NON-NLS-1$
				if (propertyFilePath != null) {
					File propertyFile = new File(propertyFilePath);
					if (propertyFile.exists()) {
						this.logFileMetaData = LogFileCacheHandler.logFileMetaDataReader(propertyFile);
					}
				}
				
				// Get project
				String projectName = viewAttributes.getString("projectName"); //$NON-NLS-1$
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

				if ((this.logFileMetaData != null) && (project != null) && project.exists() && project.isOpen()) {
					Path path = new Path(this.logFileMetaData.getProjectRelativePath());
					IFile logFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

					if ((logFile != null) && logFile.exists() && logFile.getProject().getName().equals(project.getName())) {
					
						String fileSizeString = viewAttributes.getString("fileSize"); //$NON-NLS-1$
						long fileSize = 0;
						if (fileSizeString != null) {
							fileSize = Long.parseLong(fileSizeString);
						}
						String fileModificationString = viewAttributes.getString("fileModification");  //$NON-NLS-1$
						long fileModification = 0;
						if (fileModificationString != null) {
							fileModification = Long.parseLong(fileModificationString);
						}
						File file = logFile.getLocation().toFile();
						
						if ((file.lastModified() == fileModification)	&& (file.length() == fileSize)) {
							
							// Load the Test case from index file
							Integer testCaseNumber = viewAttributes.getInteger("testCaseNumber"); //$NON-NLS-1$
							File indexFileForLogFile = LogFileCacheHandler.getIndexFileForLogFile(logFile);
							File logRecordIndexFile = LogFileCacheHandler.getLogRecordIndexFileForLogFile(logFile);
							
							if (!indexFileForLogFile.exists() || !logRecordIndexFile.exists()) {
								return null;
							}
							
							final Parser parser = new Parser(this.logFileMetaData);
							final TestCase testCase = TestCaseExtractor.getTestCaseFromIndexFile(indexFileForLogFile, testCaseNumber); 
							final LogRecordIndex[] logRecordIndexes = LogFileCacheHandler.readLogRecordIndexFile(
									logRecordIndexFile, testCase.getStartRecordNumber(), testCase.getNumberOfRecords());
							final PreferencesHolder preferences = PreferencesHandler.getInstance().getPreferences(projectName);

							// Restore model
							job = new WorkspaceJob("Loading log information") {

								@Override
								public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
									try {
										MSCView.this.model = parser.preParse(testCase, logRecordIndexes, preferences, null, monitor);
									} catch (Exception e) {
										ErrorReporter.logExceptionStackTrace(e);
									}
									return Status.OK_STATUS;
								}
							};
							job.schedule();


							// Restore selection
							this.restoredSelection = viewAttributes.getInteger("rowSelection"); //$NON-NLS-1$
							
							this.problemDuringRestore = false;
						}
					}
				}

			} catch (Exception e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		this.memento = null;

		return job;
	}
	
	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		init(site);
		this.memento = memento;
	}
	
	/**
	 * Gets the MSCWidget for this view
	 * @return the MSCWidget
	 */
	public MSCWidget getMSCWidget() {
		return this.mscWidget;
	}
	
	/**
	 * Sets the current log file meta data
	 * 
	 * @param logFileMetaData
	 *            the current log file meta data
	 */
	public void setLogFileMetaData(final LogFileMetaData logFileMetaData) {
		this.logFileMetaData = logFileMetaData;
	}

	/**
	 * Selects the given record on the MSC view.
	 * Does nothing if the given record does not exist in the model.
	 * @param recordNumber The record number of the record.
	 */
	public void setSelection(final int recordNumber) {
		int positionInEventsVector = model.getRecordsPosition(recordNumber);
		if (positionInEventsVector == -1) {
			return;
		}
		
		int lineToSelect = positionInEventsVector + 2;	// + 2 == difference between the log records position in the events vector
														//			and the position on the screen
		mscWidget.setSelection(new StructuredSelection(lineToSelect));
	}
	
	/**
	 * Returns the record number of the currently selected record.
	 * @return The record number
	 */
	public int getSelectedRecordNumber() {
		int selectedLine = (Integer) ((IStructuredSelection) mscWidget.getSelection()).getFirstElement();
		IEventObject event = model.getEvent(selectedLine - 2);
		return event.getRecordNumber();
	}
	
	@Override
	public void createPartControl(final Composite c) {
		
		final WorkspaceJob job = restoreState(); // restores any saved state
		if (this.problemDuringRestore) {
			Label text = new Label(c, SWT.LEFT);
			text.setText(Messages.getString("MSCView.0")); //$NON-NLS-1$
			return;
		}
		
		Composite parent = new Composite(c, SWT.NONE);
		GridLayout parentLayout = new GridLayout();
		parentLayout.numColumns = 1;
		parentLayout.marginWidth = 0;
		parentLayout.marginHeight = 0;
		parent.setLayout(parentLayout);
		GridData seqDiagLayoutData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
												 | GridData.GRAB_HORIZONTAL
												 | GridData.GRAB_VERTICAL
												 | GridData.VERTICAL_ALIGN_FILL);
		this.mscWidget = new MSCWidget(parent, SWT.NONE);
		this.mscWidget.setLayoutData(seqDiagLayoutData);
		this.mscWidget.setDragAutoScroll(false);
		this.mscWidget.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.keyCode != SWT.CR) {
					return;
				}

				PreferencesHolder preferences = PreferencesHandler.getInstance().getPreferences(MSCView.this.logFileMetaData.getProjectName());
				int defaultBehaviour = preferences.getMscViewDefault();
				if (defaultBehaviour == PreferenceConstants.DEFAULT_TEXT) {
					MSCView.this.openTextTable.run();
				} else {
					MSCView.this.openValueView.run();
				}
			}
		});
		this.mscWidget.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				PreferencesHolder preferences = PreferencesHandler.getInstance().getPreferences(MSCView.this.logFileMetaData.getProjectName());
				int defaultBehaviour = preferences.getMscViewDefault();
				if (defaultBehaviour == PreferenceConstants.DEFAULT_TEXT) {
					MSCView.this.openTextTable.run();
				} else {
					MSCView.this.openValueView.run();
				}
			}
		});
		this.mscWidget.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				if (!(event.getSelection() instanceof IStructuredSelection)) {
					return;
				}

				if (MSCView.this.model == null) {
					return;
				}

				final IFile logFile = MSCView.this.model.getTestCase().getLogFile();
				if (LogFileCacheHandler.hasLogFileChanged(logFile)) {
					LogFileCacheHandler.handleLogFileChange(logFile);
					return;
				}

				final IStructuredSelection structuredSelection = (IStructuredSelection) event.getSelection();
				final IViewReference[] viewReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();

				if (MSCView.this.logFileMetaData == null) {
					return;
				}
				
				for (IViewReference viewReference : viewReferences) {
					final IViewPart viewPart = viewReference.getView(false);

					if (viewPart instanceof TextTableView
							&& ((TextTableView) viewPart).getLogFileMetaData() != null
							&& MSCView.this.logFileMetaData.getFilePath().equals(
									((TextTableView) viewPart).getLogFileMetaData().getFilePath())) {
						final Integer selectedLine = (Integer) structuredSelection.getFirstElement();
						final int recordNumber = MSCView.this.model.getEvent(selectedLine - 2).getRecordNumber();
						TextTableView textTableView = (TextTableView) viewPart;
						if (textTableView.getSelectedRecord() != null
								&& textTableView.getSelectedRecord().getRecordNumber() != recordNumber) {
							textTableView.setSelectedRecord(recordNumber);
						}

						break;
					}
				}
				silentOpenSource.delayedRun(event.getSelection());
				silentOpenValueView.delayedRun(event.getSelection());
			}
		});
		
		createCoolbarContent();
		hookContextMenu();
		parent.layout(true);

		WorkspaceJob job2 = new WorkspaceJob("Displaying loaded log information") {

			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
				if (job == null) {
					return Status.OK_STATUS;
				}

				try {
					job.join();
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}

				// Check if model was restored
				if ((MSCView.this.logFileMetaData != null) && (MSCView.this.model != null)) {
					// always open restore at top
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							setModel(MSCView.this.model, MSCView.this.restoredSelection);
						}
					});
				}
				return Status.OK_STATUS;
			}
		};
		job2.setSystem(true);
		job2.schedule();
	}
	
	@Override
	public void setFocus() {
		if (this.mscWidget != null && !this.mscWidget.isDisposed()) {
			this.mscWidget.setFocus();
		}
	}
	
	/**
	 * Creates context menu for the message sequence chart
	 *
	 */
	private void hookContextMenu() {
		this.menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		this.menuMgr.setRemoveAllWhenShown(true);
		this.menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(final IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = this.menuMgr.createContextMenu(this.mscWidget.getViewControl());
		this.mscWidget.getViewControl().setMenu(menu);
	}
	
	/**
	 * Fills the context menu with actions
	 * @param manager the menu manager
	 */
	private void fillContextMenu(final IMenuManager manager) {
		manager.add(this.openValueView);
		manager.add(this.openTextTable);
		manager.add(this.openSource);
		manager.add(new Separator());
		manager.add(this.jumpToPreviousSetverdict);
		manager.add(this.jumpToNextSetverdict);
		manager.add(new Separator());
		manager.add(filterAction);
		manager.add(new Separator());
		manager.add(this.refresh);
	}
	
	/**
	 * Creates the coolBar icon depending on the actions supported by the 
	 * Message Sequence Chart provider<br>
	 * - Navigation buttons are displayed if ISDPovider.HasPaging return true<br>
	 * - Navigation buttons are enabled depending on the value
	 * return by ISDPovider.HasNext and HasPrev<br>
	 */
	protected void createCoolbarContent() {
		IActionBars bar = getViewSite().getActionBars();
		
		bar.getToolBarManager().removeAll();

		createMenuGroups();
		
		this.openValueView = new OpenValueViewAction(this, true);
		this.openValueView.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_DETAILS_VIEW));
		this.silentOpenValueView = new OpenValueViewAction(this, false);
		this.silentOpenValueView.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_DETAILS_VIEW));
		
		this.openTextTable = new OpenTextTableAction(this);
		this.openTextTable.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_TEXT_TABLE_VIEW));

		this.openSource = new OpenSourceAction(this, false, true);
		this.openSource.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_TEXT_TABLE_VIEW));
		this.silentOpenSource = new OpenSourceAction(this, true, false);
		this.silentOpenSource.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_TEXT_TABLE_VIEW));
		
		IAction switchToTextTable = new Action() {
			@Override
			public void run() {
				TextTableViewHelper.open(logFileMetaData.getProjectName(), logFileMetaData.getProjectRelativePath(), getSelectedRecordNumber());
			}
		};
		switchToTextTable.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_TEXT_TABLE_VIEW));
		switchToTextTable.setText("Switch to Table view");
		switchToTextTable.setToolTipText(switchToTextTable.getText());
		bar.getToolBarManager().appendToGroup(Constants.ID_SWITCH_VIEW_GROUP, switchToTextTable);
		
		this.jumpToPreviousSetverdict = new JumpToPreviousSetverdictAction(this);
		this.jumpToPreviousSetverdict.setId("jumpToNextSetVerdict"); //$NON-NLS-1$
		this.jumpToPreviousSetverdict.setText(Messages.getString("MSCView.11")); //$NON-NLS-1$
		this.jumpToPreviousSetverdict.setToolTipText(Messages.getString("MSCView.12")); //$NON-NLS-1$
		this.jumpToPreviousSetverdict.setImageDescriptor(
				ImageDescriptor.createFromImage(Activator.getDefault().getIcon(Constants.ICONS_MSC_JUMP_TO_PREVIOUS)));
		this.jumpToPreviousSetverdict.setEnabled(false);
		bar.getToolBarManager().appendToGroup(MSCConstants.ID_ZOOM_GROUP, this.jumpToPreviousSetverdict);
		
		this.jumpToNextSetverdict = new JumpToNextSetverdictAction(this);
		this.jumpToNextSetverdict.setId("jumpToNextSetVerdict"); //$NON-NLS-1$
		this.jumpToNextSetverdict.setText(Messages.getString("MSCView.13")); //$NON-NLS-1$
		this.jumpToNextSetverdict.setToolTipText(Messages.getString("MSCView.14")); //$NON-NLS-1$
		this.jumpToNextSetverdict.setImageDescriptor(
				ImageDescriptor.createFromImage(Activator.getDefault().getIcon(Constants.ICONS_MSC_JUMP_TO_NEXT)));
		this.jumpToNextSetverdict.setEnabled(false);
		bar.getToolBarManager().appendToGroup(MSCConstants.ID_ZOOM_GROUP, this.jumpToNextSetverdict);

		this.refresh = new RefreshMSCViewAction(this);
		this.refresh.setId("refreshMSCView"); //$NON-NLS-1$
		this.refresh.setToolTipText(Messages.getString("MSCView.1")); //$NON-NLS-1$
		this.refresh.setImageDescriptor(ImageDescriptor.createFromImage(Activator.getDefault().getIcon(Constants.ICONS_REFRESH)));
		IActionBars actionBar = getViewSite().getActionBars();
		actionBar.setGlobalActionHandler(ActionFactory.REFRESH.getId(), this.refresh);
		bar.getToolBarManager().appendToGroup(MSCConstants.ID_ZOOM_GROUP, this.refresh);
		
		IAction closeAllAction = new Action() {
			@Override
			public void run() {
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewReference[] viewReferences = activePage.getViewReferences();

				for (IViewReference reference : viewReferences) {
					IViewPart view = reference.getView(false);
					// memento restored views that never have had focus are null!!!
					if (view == null) {
						activePage.hideView(reference);
					} else if (view instanceof MSCView) {
						activePage.hideView(reference);
					}
				}

				// Clear Details view if needed
				DetailsView detailsView = (DetailsView) activePage.findView(Constants.DETAILS_VIEW_ID);
				if (detailsView != null) {
					detailsView.setData(null, false);
				}
			}
		};

		closeAllAction.setImageDescriptor(ImageDescriptor.createFromImage(Activator.getDefault().getIcon(Constants.ICONS_MSC_DELETE)));
		closeAllAction.setId("closeMSC"); //$NON-NLS-1$
		closeAllAction.setToolTipText(Messages.getString("MSCView.2")); //$NON-NLS-1$
		closeAllAction.setEnabled(true);
		bar.getToolBarManager().appendToGroup(MSCConstants.ID_ZOOM_GROUP, closeAllAction);
		
		ZoomAction resetZoom = new ZoomAction(this);
		resetZoom.setId(MSCConstants.ID_RESET_ZOOM);
		resetZoom.setText(Messages.getString("MSCView.3")); //$NON-NLS-1$
		resetZoom.setToolTipText(Messages.getString("MSCView.4")); //$NON-NLS-1$
		resetZoom.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(MSCConstants.ICON_RESET_ZOOM));
		bar.getToolBarManager().appendToGroup(MSCConstants.ID_ZOOM_GROUP, resetZoom);
		
		ZoomAction noZoom = new ZoomAction(this);
		noZoom.setChecked(true);
		noZoom.setId(MSCConstants.ID_NO_ZOOM);
		noZoom.setText(Messages.getString("MSCView.5")); //$NON-NLS-1$
		noZoom.setToolTipText(Messages.getString("MSCView.6")); //$NON-NLS-1$
		noZoom.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(MSCConstants.ICON_MOVE));
		bar.getToolBarManager().appendToGroup(MSCConstants.ID_ZOOM_GROUP, noZoom);
		
		ZoomAction zoomIn = new ZoomAction(this);
		zoomIn.setId(MSCConstants.ID_ZOOM_IN);
		zoomIn.setText(Messages.getString("MSCView.7")); //$NON-NLS-1$
		zoomIn.setToolTipText(Messages.getString("MSCView.8")); //$NON-NLS-1$
		zoomIn.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(MSCConstants.ICON_ZOOM_IN));
		bar.getToolBarManager().appendToGroup(MSCConstants.ID_ZOOM_GROUP, zoomIn);
		
		ZoomAction zoomOut = new ZoomAction(this);
		zoomOut.setId(MSCConstants.ID_ZOOM_OUT);
		zoomOut.setText(Messages.getString("MSCView.9")); //$NON-NLS-1$
		zoomOut.setToolTipText(Messages.getString("MSCView.10")); //$NON-NLS-1$
		zoomOut.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(MSCConstants.ICON_ZOOM_OUT));
		bar.getToolBarManager().appendToGroup(MSCConstants.ID_ZOOM_GROUP, zoomOut);
		
		bar.getToolBarManager().appendToGroup(MSCConstants.ID_ZOOM_GROUP, filterAction);

		Action decipheringAction = new Action() {
			@Override
			public void run() {
				final List<String> rulesets = DecipheringPreferenceHandler.getAvailableRuleSets();

				ElementListSelectionDialog dialog = new ElementListSelectionDialog(MSCView.this.getSite().getShell(), new LabelProvider());
				dialog.setTitle("Message name deciphering");
				dialog.setMessage("Select a deciphering ruleset");
				dialog.setHelpAvailable(false);
				dialog.setElements(rulesets.toArray());

				if (dialog.open() == Window.CANCEL || dialog.getFirstResult() == null) {
					return;
				}

				MSCView.this.model.setDecipheringRuleset((String) dialog.getFirstResult());
				mscWidget.redraw();
			}

		};
		decipheringAction.setImageDescriptor(Activator.getImageDescriptor(Constants.ICONS_MSC_DECIPHERING));
		decipheringAction.setToolTipText("Select a message name deciphering ruleset");
		bar.getToolBarManager().appendToGroup(MSCConstants.ID_ZOOM_GROUP, decipheringAction);

		bar.updateActionBars();
	}
	
	public FilterPattern getFilterPattern() {
		return filterPattern;
	}
	
	private SortedMap<String, Boolean> loadEventsFromPreferences() {
		SortedMap<String, Boolean> result = new TreeMap<String, Boolean>();
		
		String prefValues = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.PREF_SILENT_EVENTS_CATEGORIES);
		String[] categories = prefValues.split(PreferenceConstants.PREFERENCE_DELIMITER);
		for (String category : categories) {
			String[] currCategory = category.split(PreferenceConstants.SILENT_EVENTS_KEY_VALUE_DELIM);

			if (currCategory.length > 1) {
				String currKey = currCategory[0];
				boolean currValue = !Boolean.valueOf(currCategory[1]);
				result.put(currKey, currValue);
			}
		}
		return result;
	}

	public void setModel(final ExecutionModel model, final int firstRow) {
		if ((this.logFileMetaData == null) || (model == null)) {
			return;
		}
		
		// Create MSCModel
		
		String sutName = PreferencesHandler.getInstance().getPreferences(this.logFileMetaData.getProjectName()).getSutName();
		if (sutName.length() == 0) {
			sutName = MSCConstants.SUT_NAME;
		}		
		this.model = model;
		MSCModel mscModel = new MSCModel(model, this.logFileMetaData, sutName);
		Frame frame = mscModel.getModelFrame();

		// Change order of components according to preferences
		List<String> visualOrderComponents = 
				PreferencesHandler.getInstance().getPreferences(this.logFileMetaData.getProjectName()).getVisualOrderComponents();
		for (int i = visualOrderComponents.size() - 1; i >= 0; i--) {
			String currentComp = visualOrderComponents.get(i);
			if (currentComp.contentEquals(Constants.SUT)) {
				currentComp = sutName;
			} else if (currentComp.contentEquals(Constants.MTC)) {
				currentComp = MSCConstants.MTC_NAME;
			}
			for (int j = 1; j < frame.lifeLinesCount(); j++) {
				Lifeline lifeLine = frame.getLifeline(j);
				if (lifeLine.getName().contentEquals(currentComp)) {
					// Move to first position
					frame.moveLifeLineToPosition(lifeLine, 1);
				}
			}
		}
		
		setPartName(frame.getName());
		setFrame(frame, true);
		setContentDescription(this.logFileMetaData.getProjectRelativePath());
		
		int verdict = model.getTestCase().getVerdict();
		switch (verdict) {
		case Constants.VERDICT_PASS:
			setTitleImage(Activator.getDefault().getIcon(Constants.ICONS_PASS));
			break;
		case Constants.VERDICT_FAIL:
			setTitleImage(Activator.getDefault().getIcon(Constants.ICONS_FAIL));
			break;
		case Constants.VERDICT_ERROR:
			setTitleImage(Activator.getDefault().getIcon(Constants.ICONS_ERROR));
			break;
		case Constants.VERDICT_INCONCLUSIVE:
			setTitleImage(Activator.getDefault().getIcon(Constants.ICONS_INCONCLUSIVE));
			break;
		// Verdict NONE is default
		default:
			setTitleImage(Activator.getDefault().getIcon(Constants.ICONS_NONE));
			break;
		}
		if (this.mscWidget != null) {
			this.mscWidget.setSelection(new StructuredSelection(firstRow));
		}
	}
	
	/**
	 * The frame to render (the sequence diagram)
	 * @param frame the frame to display
	 */
	private void setFrame(final Frame frame, final boolean resetPosition) {
		if ((this.mscWidget == null) || (frame == null)) {
			return;
		}
		if (getMSCWidget() != null) {
			getMSCWidget().setFrame(frame, resetPosition);
		}
	}
	
	/**
	 * Creates the menu group 
	 */
	protected void createMenuGroups() {
		IActionBars bar = getViewSite().getActionBars();
		if (bar == null) {
			return;
		}
		bar.getToolBarManager().add(new Separator(MSCConstants.ID_ZOOM_GROUP));
		bar.getToolBarManager().add(new Separator(Constants.ID_SWITCH_VIEW_GROUP));
	}
	
	/**
	 * Gets the current model for this view
	 * @return the model
	 */
	public ExecutionModel getModel() {
		return this.model;
	}
	
	@Override
	public void dispose() {
		if (silentOpenSource != null) {
			silentOpenSource.dispose();
			silentOpenSource = null;
		}
		if (openSource != null) {
			openSource.dispose();
			openSource = null;
		}
		if (openValueView != null) {
			openValueView.dispose();
			openValueView = null;
		}
		if (silentOpenValueView != null) {
			silentOpenValueView.dispose();
			silentOpenValueView = null;
		}

		// Clear Details View if needed
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (activePage != null) {
			DetailsView detailsView = (DetailsView) activePage.findView(Constants.DETAILS_VIEW_ID);
			if ((detailsView != null) && (this.model != null)) {
				String dvTestCaseName = detailsView.getTestCaseName();
				String mTestCaseName = model.getTestCase().getTestCaseName();
				URI dvFullPath = detailsView.getFullPath();
				URI mFullPath = this.logFileMetaData.getFilePath();
				if (dvTestCaseName != null && mTestCaseName != null	&& dvFullPath != null && mFullPath != null) {
					if (dvTestCaseName.equals(mTestCaseName) && dvFullPath.equals(mFullPath)) {
						detailsView.setData(null, false);
					}
				}
			}
		}
		super.dispose();
	}

	@Override
	public LogFileMetaData getLogFileMetaData() {
		return logFileMetaData;
	}

	@Override
	public String getName() {
		return "MSC View";
	}

}
