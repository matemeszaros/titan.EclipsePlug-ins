/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.text.table;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.actions.OpenSourceViewMenuAction;
import org.eclipse.titan.log.viewer.actions.OpenValueViewMenuAction;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.models.FilterPattern;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.preferences.PreferencesHandler;
import org.eclipse.titan.log.viewer.preferences.PreferencesHolder;
import org.eclipse.titan.log.viewer.readers.CachedLogReader;
import org.eclipse.titan.log.viewer.readers.FilteredLogReader;
import org.eclipse.titan.log.viewer.readers.LogFileReader;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.DetailsView;
import org.eclipse.titan.log.viewer.views.ILogViewerView;
import org.eclipse.titan.log.viewer.views.msc.model.EventObject;
import org.eclipse.titan.log.viewer.views.msc.model.EventSelection;
import org.eclipse.titan.log.viewer.views.msc.model.EventType;
import org.eclipse.titan.log.viewer.views.navigator.ProjectsViewerMenuListener;
import org.eclipse.titan.log.viewer.views.navigator.ProjectsViewerMenuManager;


/**
 * This class represents the Text Table View
 * 
 */
public class TextTableView extends ViewPart implements ISelectionProvider, ILogViewerView {

	private Table table;
	private LogFileMetaData logFileMetaData = null;
	private CachedLogReader cachedLogReader = null;

	private FilteredLogReader filteredLogReader = null;
	private int selectedRow;
	private IMemento memento = null;
	private int viewerWidth = 0;
	private boolean problemDuringRestore;
	private OpenValueViewMenuAction openValueViewMenuAction;
	private OpenValueViewMenuAction silentOpenValueViewMenuAction;
	private OpenSourceViewMenuAction openSourceViewMenuAction;
	private OpenSourceViewMenuAction silentOpenSourceViewMenuAction;
	private IAction filterAction;

	private ISelection eventSelection;
	private List<ISelectionChangedListener> registeredListeners;
	private EventObject selectedEventObject;
	private Listener tableSetDataListener = null;

	private FilterPattern filterPattern = null;

	private static final int DEFAULT_COLUMN_WIDTH = 80;
	private static final int CACHE_SIZE = 10;

	private enum Column {
		COL_TIMESTAMP {
			@Override
			public String getDisplayName() {
				return Messages.getString("TextTableModel.0");
			}

			@Override
			public String getData(LogRecord logRecord) {
				return logRecord.getTimestamp();
			}
		},
		COL_COMP_REF {
			@Override
			public String getDisplayName() {
				return Messages.getString("TextTableModel.1");
			}

			@Override
			public String getData(LogRecord logRecord) {
				return logRecord.getComponentReference();
			}
		},
		COL_EVENT_TYPE {
			@Override
			public String getDisplayName() {
				return Messages.getString("TextTableModel.3");
			}

			@Override
			public String getData(LogRecord logRecord) {
				return logRecord.getEventType();
			}
		},
		COL_SRC_INFO {
			@Override
			public String getDisplayName() {
				return Messages.getString("TextTableModel.4");
			}

			@Override
			public String getData(LogRecord logRecord) {
				return logRecord.getSourceInformation();
			}
		},
		COL_MESSAGE {
			IconHandler iconHandler = new IconHandler();

			@Override
			public String getDisplayName() {
				return Messages.getString("TextTableModel.5");
			}

			@Override
			public String getData(final LogRecord logRecord) {
				return logRecord.getMessage();
			}

			@Override
			public Image getIcon(final LogRecord logRecord, final SetVerdictFilter setVerdictFilter) {
				return iconHandler.getIcon(logRecord, setVerdictFilter);
			}

		};

		/**
		 * Returns the header of the column
		 */
		public abstract String getDisplayName();
		
		/**
		 * Returns the data to be displayed in this column.
		 */
		public abstract String getData(final LogRecord logRecord);

		/**
		 * Returns an icon to be displayed in the cell.
		 * 
		 * @param logRecord The logrecord which contains the message.
		 * @param setVerdictFilter Filter for the verdicts
		 * @return The icon for the log record. Can be null if no icon should be displayed.
		 */
		public Image getIcon(final LogRecord logRecord, final SetVerdictFilter setVerdictFilter) {
			return null;
		}

	}

	private static PreferencesHolder preferences;

	private class TableSetDataListener implements Listener {
		@Override
		public void handleEvent(final Event event) {
			int topIndex = TextTableView.this.table.getTopIndex();
			int visibleItems = TextTableView.this.table.getClientArea().height / TextTableView.this.table.getItemHeight();
			int startPos = topIndex + visibleItems + TextTableView.CACHE_SIZE;
			if (startPos >= filteredLogReader.size()) {
				startPos = filteredLogReader.size() - 1;
			}

			// Get current table item, row number and corresponding log record
			final TableItem item = (TableItem) event.item;

			if (clearingThread != null) {
				clearingThread.stop();
			}

			clearingThread = new ClearingRunnable(topIndex, visibleItems);

			Display.getDefault().asyncExec(clearingThread);

			int rowIndex = event.index;

			try {
				cachedLogReader.cacheRecords(topIndex, startPos);
				LogRecord record = getLogRecordAtRow(rowIndex);
				TextTableView.this.setTableItem(item, record);
			} catch (IOException e) {
				ErrorReporter.logExceptionStackTrace(e);
				TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("TextTableModel.6") + e.getMessage()));  //$NON-NLS-1$
			} catch (ParseException e) {
				ErrorReporter.logExceptionStackTrace(e);
				TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("TextTableModel.7") + e.getMessage()));  //$NON-NLS-1$
			}
		}
	}
	

	private final List<Column> orderedColumns = Arrays.asList(
			Column.COL_TIMESTAMP,
			Column.COL_COMP_REF,
			Column.COL_EVENT_TYPE,
			Column.COL_SRC_INFO,
			Column.COL_MESSAGE);


	private ClearingRunnable clearingThread = null;
	/**
	 * Helper class used to clear out data of items not visible at a later time in a parallel thread.
	 * */
	private final class ClearingRunnable implements Runnable {
		private final int topIndex;
		private final int visibleItems;
		private volatile boolean stoped = false;

		private ClearingRunnable(final int topIndex, final int visibleItems) {
			this.topIndex = topIndex;
			this.visibleItems = visibleItems;
		}

		public void stop() {
			stoped = true;
		}

		@Override
		public void run() {
			if (stoped || TextTableView.this.table.isDisposed()) {
				return;
			}

			int endPos = topIndex - TextTableView.CACHE_SIZE;
			if (endPos > 0) {
				TextTableView.this.table.clear(0, endPos);
			}

			if (stoped || TextTableView.this.table.isDisposed()) {
				return;
			}
			int startPos = topIndex + visibleItems + TextTableView.CACHE_SIZE;
			endPos = TextTableView.this.table.getItemCount() - 1;
			if (startPos < endPos) {
				TextTableView.this.table.clear(startPos + 1, endPos);
			}

			clearingThread = null;
		}
	}

	public TextTableView() {
		this.registeredListeners = new ArrayList<ISelectionChangedListener>();
		filterAction = new FilterAction(this);
		filterAction.setEnabled(true);
		
		addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				if (!(event.getSelection() instanceof EventSelection)) {
					return;
				}
				final EventObject tmpEventObject = ((EventSelection) event.getSelection()).getEventObject();
				if (tmpEventObject == null) {
					return;
				}

				final int selectedRecord = tmpEventObject.getRecordNumber();
				TextTableViewHelper.updateSelectionInConnectedMscView(selectedRecord, TextTableView.this.getLogFileMetaData());
			}
		});
	}

	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		init(site);
		this.memento = memento;
	}

	@Override
	public void saveState(final IMemento memento) {
		// do not save empty views
		if (this.filteredLogReader == null) {
			return;
		}
		IMemento tempMemento = memento.createChild("selection"); //$NON-NLS-1$
		try {
			IMemento viewAttributes = tempMemento.createChild("viewAttributes"); //$NON-NLS-1$
			viewAttributes.putString("projectName", this.logFileMetaData.getProjectName()); //$NON-NLS-1$
			viewAttributes.putInteger("selectedRow", this.table.getSelectionIndex()); //$NON-NLS-1$
			viewAttributes.putInteger("viewWidth", this.table.getClientArea().width); //$NON-NLS-1$

			// save state about log file
			Path filePath = new Path(this.logFileMetaData.getProjectRelativePath());
			IFile logFile = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);

			if ((logFile != null) && logFile.exists()) {
				// add property file to the memento
				viewAttributes.putString("propertyFile", LogFileCacheHandler.getPropertyFileForLogFile(logFile).getAbsolutePath()); //$NON-NLS-1$
				File aLogFile = logFile.getLocation().toFile();
				viewAttributes.putString("fileSize", String.valueOf(aLogFile.length())); //$NON-NLS-1$
				viewAttributes.putString("fileModification", String.valueOf(aLogFile.lastModified())); //$NON-NLS-1$
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
	private void restoreState() {  
		if (this.memento == null) {
			return;
		}
//		this.problemDuringRestore = true; // TODO check the save and restore methods
		this.memento = this.memento.getChild("selection"); //$NON-NLS-1$
		if (this.memento == null) {
			return;
		}
		try {
			// get project
			IMemento viewAttributes = this.memento.getChild("viewAttributes"); //$NON-NLS-1$
			String projectName = viewAttributes.getString("projectName"); //$NON-NLS-1$
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if ((project == null) || !project.exists() || !project.isOpen()) {
				return;
			}

			// retrieve log file meta data
			String propertyFilePath = viewAttributes.getString("propertyFile"); //$NON-NLS-1$
			if (propertyFilePath == null) {
				return;
			}
			File propertyFile = new File(propertyFilePath);
			if (!propertyFile.exists()) {
				return;
			}
			this.logFileMetaData = LogFileCacheHandler.logFileMetaDataReader(propertyFile);

			// get log file
			Path path = new Path(this.logFileMetaData.getProjectRelativePath());
			IFile logFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			if ((logFile == null) || !logFile.exists() || !logFile.getProject().getName().equals(project.getName())) {
				return;
			}
			File file = logFile.getLocation().toFile();

			// get file attributes to see if file has changed
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
			if ((file.lastModified() != fileModification) || (file.length() != fileSize)) {
				return;
			}

			if (LogFileCacheHandler.hasLogFileChanged(logFile)) {
				return;
			}

			// create reader and set as input
			this.filteredLogReader = new FilteredLogReader(LogFileReader.getReaderForLogFile(logFile));
			this.cachedLogReader = new CachedLogReader(filteredLogReader);
			this.selectedRow = viewAttributes.getInteger("selectedRow"); //$NON-NLS-1$
			this.viewerWidth = viewAttributes.getInteger("viewWidth"); //$NON-NLS-1$
			this.problemDuringRestore = false;

		} catch (final IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
		} catch (final ClassNotFoundException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		this.memento = null;
	}

	@Override
	public void createPartControl(final Composite parent) {

		createToolbar();
		restoreState(); // restores any saved state
		if (this.problemDuringRestore) {
			Label text = new Label(parent, SWT.LEFT);
			text.setText(Messages.getString("TextTableModel.8")); //$NON-NLS-1$
			return;
		}
		Composite composite = new Composite(parent, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		composite.setLayout(new FillLayout());
		this.table = new Table(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);

		// Add columns
		for (Column col : orderedColumns) {
			TableColumn tableColumn = new TableColumn(this.table, SWT.BORDER);
			tableColumn.setText(col.getDisplayName());
			tableColumn.setResizable(true);
			tableColumn.setMoveable(true);
			tableColumn.setWidth(DEFAULT_COLUMN_WIDTH);
		}

		this.table.setHeaderVisible(true);
		this.table.setLinesVisible(true);
		this.table.setMenu(hookContextMenu(this.table));

		addListeners();

		createContextMenuActions();

		if (cachedLogReader != null && filteredLogReader != null) {
			setInput(logFileMetaData, cachedLogReader, filteredLogReader, selectedRow);
		}
	}

	private void addListeners() {
		this.table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (logFileMetaData == null) {
					return;
				}

				final IFile logFile = logFileMetaData.getLogfile();
				if (logFile == null) {
					return;
				}
				if (LogFileCacheHandler.hasLogFileChanged(logFile)) {
					LogFileCacheHandler.handleLogFileChange(logFile);
					return;
				}
				setContentDescription(
						TextTableView.this.logFileMetaData.getProjectRelativePath()
								+ " [" + (TextTableView.this.table.getSelectionIndex() + 1)
								+ "/" + TextTableView.this.filteredLogReader.size() + "]");
				if (e.getSource() instanceof Table) {
					updateSelection((Table) e.getSource());
				}

			}
		});

		this.table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				if (logFileMetaData == null) {
					return;
				}

				final IFile logFile = logFileMetaData.getLogfile();
				if (logFile == null) {
					return;
				}
				if (LogFileCacheHandler.hasLogFileChanged(logFile)) {
					LogFileCacheHandler.handleLogFileChange(logFile);
					return;
				}

				if (!(e.getSource() instanceof Table)) {
					return;
				}

				Table table = (Table) e.getSource();
				int index = table.getSelectionIndex();
				if (index < 0 || index >= table.getItemCount()) {
					return;
				}

				TableItem tableItem = table.getItem(index);
				Object data = tableItem.getData();
				if (data instanceof LogRecord) {
					LogRecord logrecord = (LogRecord) data;

					EventObject eventObject = createEventObject(logrecord);

					TextTableView.this.eventSelection = new EventSelection(eventObject, ""); //$NON-NLS-1$
					TextTableView.this.openValueViewMenuAction.run();
					silentOpenSourceViewMenuAction.selectionChanged(eventSelection);
					silentOpenSourceViewMenuAction.run();

				} else {
					TextTableView.this.eventSelection = new EventSelection(null, null);
				}
				fireSelectionChangeEvent();
			}
		});

		this.table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {

				int moves = e.keyCode;
				if (moves != SWT.CR) {
					return;
				}
				if (e.getSource() instanceof Table) {
					Table table = (Table) e.getSource();
					TableItem tableItem = table.getItem(table.getSelectionIndex());
					Object data = tableItem.getData();
					if (data instanceof LogRecord) {
						LogRecord logrecord = (LogRecord) data;

						EventObject eventObject = createEventObject(logrecord);

						TextTableView.this.eventSelection = new EventSelection(eventObject, "");  //$NON-NLS-1$
					} else {
						TextTableView.this.eventSelection = new EventSelection(null, null);
					}
				}
				fireSelectionChangeEvent();
				TextTableView.this.openValueViewMenuAction.run();
				silentOpenSourceViewMenuAction.selectionChanged(eventSelection);
				silentOpenSourceViewMenuAction.run();
			}
		});
	}

	private EventObject createEventObject(LogRecord logrecord) {
		EventObject newEventObject = new EventObject(EventType.UNKNOWN);
		newEventObject.setRecordLength(logrecord.getRecordLength());
		newEventObject.setRecordNumber(logrecord.getRecordNumber());
		newEventObject.setRecordOffset(logrecord.getRecordOffset());
		newEventObject.setReference(logrecord.getComponentReference());
		newEventObject.setEventType(logrecord.getEventType());

		return newEventObject;
	}

	@Override
	public void setFocus() {
		if ((this.table != null) && !this.table.isDisposed()) {
			this.table.forceFocus();
		}
	}

	/**
	 * Sets the given file as the input of the view. The content of the log file will be displayed.
	 * @param logFile The log file
	 * @throws IOException in case of file I/O errors
	 * @throws ClassNotFoundException if the log file meta data can not be found
	 */
	public void setInput(final IFile logFile, final int selection) throws IOException, ClassNotFoundException {
		LogFileMetaData newLogFileMetaData = LogFileCacheHandler.logFileMetaDataReader(LogFileCacheHandler.getPropertyFileForLogFile(logFile));
		final FilteredLogReader filteredReader = new FilteredLogReader(LogFileReader.getReaderForLogFile(logFile));
		final CachedLogReader cachedReader = new CachedLogReader(filteredReader);
		setInput(newLogFileMetaData, cachedReader, filteredReader, selection);
	}
	
	/**
	 * Sets the given file as the input of the view. The content of the log file will be displayed.
	 * @see TextTableView#setInput(IFile, int)
	 */
	public void setInput(final IFile logFile) throws IOException, ClassNotFoundException {
		setInput(logFile, 0);
	}

	/**
	 * Sets the model input
	 * 
	 * @param reader
	 *            the log file reader
	 */
	private void setInput(final LogFileMetaData logFileMetaData, final CachedLogReader cachedReader,
	                      final FilteredLogReader reader, final int selection) {
		setLogFileMetaData(logFileMetaData);

		if (tableSetDataListener != null) {
			table.removeListener(SWT.SetData, tableSetDataListener);
		}

		tableSetDataListener = new TableSetDataListener();
		table.addListener(SWT.SetData, tableSetDataListener);

		this.cachedLogReader = cachedReader;
		this.filteredLogReader = reader;
		this.table.setItemCount(reader.size());
		setPartName(new File(this.logFileMetaData.getFilePath()).getName());
		if (this.viewerWidth == 0) {
			this.viewerWidth = this.table.getClientArea().width;
		}
		TableColumn[] columns = this.table.getColumns();
		int colWidth = this.viewerWidth / columns.length;
		if (colWidth > 0) {
			this.table.setRedraw(false);
			for (TableColumn column : columns) {
				column.setWidth(colWidth);
			}
			this.table.setRedraw(true);
		}
		// Set top index/selection - *nix workaround
		this.table.setTopIndex(selection);
		this.table.setSelection(selection);

		this.table.setTopIndex(selection);
		this.table.setSelection(selection);
		setContentDescription(this.logFileMetaData.getProjectRelativePath() + " [" + (selection + 1) + "/" + reader.size() + "]");
	}

	/**
	 * Sets the current log file meta data
	 * 
	 * @param logFileMetaData the current log file meta data
	 */
	private void setLogFileMetaData(final LogFileMetaData logFileMetaData) {
		this.logFileMetaData = logFileMetaData;
		TextTableView.preferences = PreferencesHandler.getInstance().getPreferences(logFileMetaData.getProjectName());
	}


	@Override
	public void dispose() {
		if (this.cachedLogReader != null) {
			this.cachedLogReader.close();
		}
		
		if (this.filteredLogReader != null) {
			this.filteredLogReader.close();
		}

		// Clear Details View if needed
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (activePage != null) {
			DetailsView detailsView = (DetailsView) activePage.findView(Constants.DETAILS_VIEW_ID);
			if ((detailsView != null) && (this.selectedEventObject != null)) {
				URI dvFullPath = detailsView.getFullPath();
				URI ttFullPath = this.logFileMetaData.getFilePath();
				if ((dvFullPath != null) && (ttFullPath != null) && dvFullPath.equals(ttFullPath)) {
					detailsView.setData(null, false);
				}
			}
		}

		super.dispose();
	}

	private LogRecord getLogRecordAtRow(final int row) throws IOException, ParseException {
		return this.cachedLogReader.getRecord(row);
	}

	/**
	 * Create a close all action in the tool bar
	 */
	private void createToolbar() {

		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager mgr = actionBars.getToolBarManager();

		IAction closeAllAction = new CloseAllAction();
		closeAllAction.setEnabled(true);

		IAction switchToMSCAction = new SwitchToMscAction(this);
		switchToMSCAction.setEnabled(true);

		mgr.add(filterAction);
		mgr.add(closeAllAction);

		mgr.add(new Separator(Constants.ID_SWITCH_VIEW_GROUP));
		mgr.appendToGroup(Constants.ID_SWITCH_VIEW_GROUP, switchToMSCAction);

		actionBars.updateActionBars();
	}	
	
	/**
	 * Refreshes the visible part of the table
	 */
	void refreshTable() {
		
		table.setItemCount(filteredLogReader.size());
		table.setTopIndex(0);
		table.setSelection(0);

		int topIndex = 0;
		int visibleItems = TextTableView.this.table.getClientArea().height / TextTableView.this.table.getItemHeight();
		int startPos = topIndex + visibleItems + TextTableView.CACHE_SIZE;
		if (startPos >= filteredLogReader.size()) {
			startPos = filteredLogReader.size() - 1;
		}

		if (clearingThread != null) {
			clearingThread.stop();
		}

		clearingThread = new ClearingRunnable(topIndex, visibleItems);

		Display.getDefault().asyncExec(clearingThread);
		
		try {
			cachedLogReader.cacheRecords(topIndex, startPos);
			for (int rowIndex = topIndex; rowIndex <= startPos; ++rowIndex) {
				final TableItem item = table.getItem(rowIndex);
				LogRecord record = getLogRecordAtRow(rowIndex);
				setTableItem(item, record);
			}
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("TextTableModel.6") + e.getMessage()));
		} catch (ParseException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("TextTableModel.7") + e.getMessage()));
		}
	}
	
	/**
	 *  Loads the given record from the logfile to the table item.
	 * @param item The item to load in.
	 * @param record The record 
	 */
	private void setTableItem(final TableItem item, final LogRecord record) {
		final SetVerdictFilter setVerdictFilter = new SetVerdictFilter(preferences);
		// Set data
		for (int i = 0; i < TextTableView.this.orderedColumns.size(); i++) {
			Column column = orderedColumns.get(i);
			final String message = column.getData(record);
			item.setText(i, message);
			item.setData(record);

			//Check if setverdict message, and add icon
			final Image cellImage = column.getIcon(record, setVerdictFilter);
			if (cellImage != null) {
				item.setImage(i, cellImage);
			}
		}
		table.update();
		table.redraw();
	}

	private void createContextMenuActions() {
		this.openValueViewMenuAction = new OpenValueViewMenuAction(this, true);
		this.openValueViewMenuAction.setEnabled(true);
		this.openValueViewMenuAction.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_DETAILS_VIEW));
		this.silentOpenValueViewMenuAction = new OpenValueViewMenuAction(this, false);
		this.silentOpenValueViewMenuAction.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_DETAILS_VIEW));

		openSourceViewMenuAction = new OpenSourceViewMenuAction(this, false, true);
		this.openSourceViewMenuAction.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_DETAILS_VIEW));
		silentOpenSourceViewMenuAction = new OpenSourceViewMenuAction(this, true, false);
		this.silentOpenSourceViewMenuAction.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_DETAILS_VIEW));
	}

	/**
	 * Adds a menu to the selected row in the table
	 * @param control
	 * @return
	 */
	private Menu hookContextMenu(final Control control) {
		ProjectsViewerMenuManager menuMgr = new ProjectsViewerMenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new ProjectsViewerMenuListener() {
			@Override
			public void menuAboutToShow(final IMenuManager menuManager) {
				TextTableView.this.fillContextMenu(menuManager);
			}
		});
		return menuMgr.createContextMenu(control);
	}

	protected void fillContextMenu(final IMenuManager menuManager) {
		// MB_ADDITIONS must be added to the menuMgr or platform will
		// throw a part initialize exception, this will fix this	
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(this.openValueViewMenuAction);
		menuManager.add(openSourceViewMenuAction);
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(filterAction);
	}

	@Override
	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
		// Don't add listeners if it already exists
		if (!this.registeredListeners.contains(listener)) {
			this.registeredListeners.add(listener);
		}
	}

	@Override
	public ISelection getSelection() {
		return this.eventSelection;
	}

	@Override
	public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
		if (this.registeredListeners.contains(listener)) {
			this.registeredListeners.remove(listener);
		}
	}

	@Override
	public void setSelection(final ISelection selection) {
		this.eventSelection = selection;		
	}

	private void fireSelectionChangeEvent() {
		for (ISelectionChangedListener listener : this.registeredListeners) {
			listener.selectionChanged(new SelectionChangedEvent(this, this.eventSelection));
		}
	}
	
	/**
	 * Returns the currently selected record.
	 * @return The log record or null if no record is selected.
	 */
	public LogRecord getSelectedRecord() {
		int index = table.getSelectionIndex();
		if (index == -1) {
			return null;
		}
		
		TableItem selectedItem = table.getItem(index);
		Object data = selectedItem.getData();
		if (data instanceof LogRecord) {
			return (LogRecord) data;
		}
		return null;
	}

	public void setSelectedRecord(final int recordNumber) {
		if (filteredLogReader == null) {
			return;
		}

		int position = filteredLogReader.getPositionFromRecordNumber(recordNumber);
		if (position >= 0 && position < table.getItemCount()) {
			table.setSelection(position);
			updateSelection(table);
		}
	}

	/** 
	 * Update the selection in the provided table
	 * 
	 *  @param table to work on
	 **/
	private void updateSelection(Table table1) {
		TableItem tableItem = table1.getItem(table1.getSelectionIndex());
		Object data = tableItem.getData();
		if (data instanceof LogRecord) {
			LogRecord logrecord = (LogRecord) data;
			TextTableView.this.selectedEventObject = createEventObject(logrecord);
			TextTableView.this.eventSelection = new EventSelection(TextTableView.this.selectedEventObject, "");
		} else {
			TextTableView.this.eventSelection = new EventSelection(null, null);
		}
		fireSelectionChangeEvent();
		silentOpenSourceViewMenuAction.delayedRun(eventSelection);
		silentOpenValueViewMenuAction.delayedRun(eventSelection);
	}

	/**
	 * Returns true if the view is filtered.
	 * @return
	 */
	public boolean isFiltered() {
		return filteredLogReader.isFiltered();
	}
	
	/**
	 * Returns true if the TextTableView contains the given record and it is not filtered.
	 * @param recordNumber The record
	 * @return 
	 */
	public boolean contains(final int recordNumber) {
		return filteredLogReader.contains(recordNumber);
	}

	/**
	 * Returns the view id of this view. It can be used with {@link org.eclipse.ui.IWorkbenchPage#showView(String, String, int)},
	 * {@link org.eclipse.ui.IWorkbenchPage#findViewReference(String, String)} etc.
	 * 
	 * @return the view id
	 */
	public static String getViewId() {
		return Constants.TEXT_TABLE_VIEW_ID;
	}

	/**
	 * Returns the secondary id of the view. It can be used with {@link org.eclipse.ui.IWorkbenchPage#showView(String, String, int)},
	 * {@link org.eclipse.ui.IWorkbenchPage#findViewReference(String, String)} etc.<br/>
	 * The secondary id is created from the log file.
	 * 
	 * @param logFile The log file of the view
	 * @return
	 */
	public static String getSecondaryId(final IFile logFile) {
		return logFile.getFullPath().toOSString();
	}

	@Override
	public LogFileMetaData getLogFileMetaData() {
		return logFileMetaData;
	}

	@Override
	public String getName() {
		return "Text Table View";
	}

	Table getTable() {
		return table;
	}

	FilterPattern getFilterPattern() {
		return filterPattern;
	}

	void setFilterPattern(FilterPattern filterPattern) {
		this.filterPattern = filterPattern;
	}

	FilteredLogReader getFilteredLogReader() {
		return filteredLogReader;
	}

}

