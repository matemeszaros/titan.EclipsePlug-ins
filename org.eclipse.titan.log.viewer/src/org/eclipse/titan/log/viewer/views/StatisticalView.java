/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.actions.OpenMSCViewAction;
import org.eclipse.titan.log.viewer.actions.OpenTextTableStatisticalViewMenuAction;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.extractors.TestCaseExtractor;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.readers.CachedLogReader;
import org.eclipse.titan.log.viewer.readers.LogFileReader;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.details.StatisticalData;
import org.eclipse.titan.log.viewer.views.navigator.ProjectsViewerMenuListener;
import org.eclipse.titan.log.viewer.views.navigator.ProjectsViewerMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

/**
 * This class represents the statistical view
 */
public class StatisticalView extends ViewPart implements ISelectionProvider, ILogViewerView {

	private IMemento memento = null;
	private LogFileMetaData logFileMetaData;
	private Table amountTable = null;
	private Table errorTestCasesTable = null;
	private Table failTestCasesTable = null;
	private Table testCases = null;
	private CachedLogReader reader = null;
	private static final int DEFAULT_COLUMN_WIDTH = 55;
	private static final int DEFAULT_AMOUNT_COLUMN_WIDTH = 75;
	private OpenMSCViewAction openMSCViewAction;
	private OpenTextTableStatisticalViewMenuAction openTextTableStatisticalViewMenuAction;
	private List<ISelectionChangedListener> registeredListeners;
	private TestCase testcaseSelection = null;
	private ISelection eventSelection;
	
	private FormToolkit toolkit;
	private ScrolledForm form;
	private ExpandableComposite ecError;
	private ExpandableComposite ecFail;
	private ExpandableComposite ecTestCases;

	private Map<String, Section> cachedSections;
	private List<StatisticalData> statisticalDataVector;
	
	/**
	 * The constructor.
	 */
	public StatisticalView() {
		registeredListeners = new ArrayList<ISelectionChangedListener>();
		cachedSections = new HashMap<String, Section>();
		statisticalDataVector = new Vector<StatisticalData>();
	}

	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		init(site);
		this.memento = memento;
	}

	@Override
	public void saveState(final IMemento memento) {
		// do not save empty views
		if (this.reader == null) {
			return;
		}
		IMemento tempMemento = memento.createChild("selection"); //$NON-NLS-1$
		try {
			IMemento[] viewAttributes = new IMemento[this.statisticalDataVector.size()];
			for (int i = 0; i < this.statisticalDataVector.size(); i++) {
				IMemento viewAttribute = tempMemento.createChild("viewAttributes"); //$NON-NLS-1$
				StatisticalData statisticData = this.statisticalDataVector.get(i);
				LogFileMetaData logFileMetaData =  statisticData.getLogFileMetaData();
				viewAttribute.putString("projectName", logFileMetaData.getProjectName()); //$NON-NLS-1$
	
				// save state about log file
				Path filePath = new Path(logFileMetaData.getProjectRelativePath());
				IFile logFile = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
				if ((logFile != null) && logFile.exists()) {
					// add property file to the memento
					viewAttribute.putString("propertyFile", LogFileCacheHandler.getPropertyFileForLogFile(logFile).getAbsolutePath()); //$NON-NLS-1$
					File aLogFile = logFile.getLocation().toFile();
					viewAttribute.putString("fileSize", String.valueOf(aLogFile.length())); //$NON-NLS-1$
					viewAttribute.putString("fileModification", String.valueOf(aLogFile.lastModified())); //$NON-NLS-1$
				}
				viewAttributes[i] = viewAttribute;
			}
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * Set data for Statistical View 
	 * @param statisticalDataVector the new data
	 */
	public void setData(final List<StatisticalData> statisticalDataVector) {
		this.statisticalDataVector = statisticalDataVector;
		if (this.statisticalDataVector.size() > 1) {
			Set<String> keys = cachedSections.keySet();
			for (String currentKey : keys) {
				Section tmpSection = cachedSections.get(currentKey);
				if (tmpSection != null && !tmpSection.isDisposed()) {
					tmpSection.dispose();
				}
			}
			cachedSections.clear();
		}

		for (StatisticalData statisticalData : this.statisticalDataVector) {
			this.logFileMetaData = statisticalData.getLogFileMetaData();
			List<TestCase> tmpTestCases = statisticalData.getTestCaseVector();
			this.reader = statisticalData.getCachedLogFileReader();


			String projectRelativePath = this.logFileMetaData.getProjectRelativePath();
			Section tmpSection = cachedSections.get(projectRelativePath);
			if (tmpSection == null) {
				createSection();
			}
			//Clear all tables before setting the data
			this.amountTable.removeAll();
			this.errorTestCasesTable.removeAll();
			this.failTestCasesTable.removeAll();
			this.testCases.removeAll();
			int noOfPass = 0;
			int noOfFail = 0;
			int noOfInconc = 0;
			int noOfNone = 0;
			int noOfError = 0;
			int noOfCrash = 0;

			// If input is null
			if (tmpTestCases == null) {
				continue;
			}

			int noTotal = tmpTestCases.size();

			for (TestCase tc : tmpTestCases) {
				TableItem tcItem = new TableItem(this.testCases, SWT.BORDER);

				LogRecord record = getLogRecordAtRow(tc.getStartRecordNumber());
				String start = record.getTimestamp();
				record = getLogRecordAtRow(tc.getEndRecordNumber());
				String stop = record.getTimestamp();
				Image image;
				switch (tc.getVerdict()) {
					case Constants.VERDICT_PASS:
						image = Activator.getDefault().getIcon(Constants.ICONS_PASS);
						noOfPass++;
						break;
					case Constants.VERDICT_ERROR:
						image = Activator.getDefault().getIcon(Constants.ICONS_ERROR);
						TableItem tcErrorItem = new TableItem(this.errorTestCasesTable, SWT.BORDER);
						tcErrorItem.setImage(1, image);
						tcErrorItem.setText(2, tc.getTestCaseName());
						tcErrorItem.setText(3, start);
						tcErrorItem.setText(4, stop);
						tcErrorItem.setData(tc);
						noOfError++;
						break;
					case Constants.VERDICT_FAIL:
						image = Activator.getDefault().getIcon(Constants.ICONS_FAIL);
						TableItem tcFailItem = new TableItem(this.failTestCasesTable, SWT.BORDER);
						tcFailItem.setImage(1, image);
						tcFailItem.setText(2, tc.getTestCaseName());
						tcFailItem.setText(3, start);
						tcFailItem.setText(4, stop);
						tcFailItem.setData(tc);

						noOfFail++;
						break;
					case Constants.VERDICT_INCONCLUSIVE:
						image = Activator.getDefault().getIcon(Constants.ICONS_INCONCLUSIVE);
						noOfInconc++;
						break;
					case Constants.VERDICT_NONE:
						image = Activator.getDefault().getIcon(Constants.ICONS_NONE);
						noOfNone++;
						break;
					case Constants.VERDICT_CRASHED:
						image = Activator.getDefault().getIcon(Constants.ICONS_CRASHED);
						noOfCrash++;
						break;

					default:
						// Could not find image return null
						image = null;
						break;
				}

				tcItem.setImage(1, image);
				tcItem.setText(2, tc.getTestCaseName());
				tcItem.setText(3, start);
				tcItem.setText(4, stop);
				tcItem.setData(tc);

			}

			if (this.errorTestCasesTable.getItems().length < 1) {
				this.errorTestCasesTable.setLinesVisible(false);

			} else {
				this.errorTestCasesTable.redraw();
				ecError.setExpanded(true);
			}

			if (this.failTestCasesTable.getItems().length < 1) {
				this.failTestCasesTable.setLinesVisible(false);

			} else {
				this.failTestCasesTable.redraw();
				ecFail.setExpanded(true);
			}


			// Create the statistical row
			TableItem item = new TableItem(this.amountTable, SWT.BORDER);
			item.setText(0, String.valueOf(noTotal));
			item.setText(1, String.valueOf(noOfPass + getPercent(noOfPass, noTotal)));
			item.setText(2, String.valueOf(noOfFail + getPercent(noOfFail, noTotal)));
			item.setText(3, String.valueOf(noOfInconc + getPercent(noOfInconc, noTotal)));
			item.setText(4, String.valueOf(noOfNone + getPercent(noOfNone, noTotal)));
			item.setText(5, String.valueOf(noOfError + getPercent(noOfError, noTotal)));
			item.setText(6, String.valueOf(noOfCrash + getPercent(noOfCrash, noTotal)));

		}
		
		if (statisticalDataVector.size() > 1) {
			setPartName("Statistics"); //$NON-NLS-1$
			setContentDescription(""); //$NON-NLS-1$
		} else if (this.logFileMetaData != null) {
			File file = new File(this.logFileMetaData.getFilePath());
			String fileName = file.getName();
			//Set the name of the part
			setPartName(fileName);
			setContentDescription(this.logFileMetaData.getProjectRelativePath());
		}
		
		// Finally redraw form
		form.reflow(true);
		form.setRedraw(true);
	}

	@Override
	public void dispose() {
		IOUtils.closeQuietly(reader);
		toolkit.dispose();
		super.dispose();
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
	private List<StatisticalData> restoreState() {
		if (this.memento == null) {
			return new ArrayList<StatisticalData>();
		}

		this.memento = this.memento.getChild("selection"); //$NON-NLS-1$
		if (this.memento == null) {
			return new ArrayList<StatisticalData>();
		}
		List<StatisticalData> tmpStatisticalDataVector = new ArrayList<StatisticalData>();
		try {
			// get project
			IMemento[] viewAttributes = this.memento.getChildren("viewAttributes"); //$NON-NLS-1$
			for (IMemento viewAttribute : viewAttributes) {
				String projectName = viewAttribute.getString("projectName"); //$NON-NLS-1$
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if ((project == null) || !project.exists() || !project.isOpen()) {
					return new ArrayList<StatisticalData>();
				}

				// retrieve log file meta data
				String propertyFilePath = viewAttribute.getString("propertyFile"); //$NON-NLS-1$
				if (propertyFilePath == null) {
					return new ArrayList<StatisticalData>();
				}
				File propertyFile = new File(propertyFilePath);
				if (!propertyFile.exists()) {
					return new ArrayList<StatisticalData>();
				}
				LogFileMetaData tmpLogFileMetaData = LogFileCacheHandler.logFileMetaDataReader(propertyFile);

				// get log file
				Path path = new Path(tmpLogFileMetaData.getProjectRelativePath());
				IFile logFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
				if ((logFile == null) || !logFile.exists() || !logFile.getProject().getName().equals(project.getName())) {
					return new ArrayList<StatisticalData>();
				}
				File file = logFile.getLocation().toFile();

				// get file attributes to see if file has changed
				String fileSizeString = viewAttribute.getString("fileSize"); //$NON-NLS-1$
				long fileSize = 0;
				if (fileSizeString != null) {
					fileSize = Long.parseLong(fileSizeString);
				}
				String fileModificationString = viewAttribute.getString("fileModification");  //$NON-NLS-1$
				long fileModification = 0;
				if (fileModificationString != null) {
					fileModification = Long.valueOf(fileModificationString);
				}
				if ((file.lastModified() != fileModification) || (file.length() != fileSize)
						|| LogFileCacheHandler.hasLogFileChanged(logFile)) {
					return new ArrayList<StatisticalData>();
				}

				// create reader and set as input
				this.reader = new CachedLogReader(LogFileReader.getReaderForLogFile(logFile));
				TestCaseExtractor extractor = new TestCaseExtractor();
				extractor.extractTestCasesFromIndexedLogFile(logFile);

				StatisticalData statisticalData = new StatisticalData(tmpLogFileMetaData, extractor.getTestCases(), reader);
				tmpStatisticalDataVector.add(statisticalData);

			}
			return tmpStatisticalDataVector;

		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
		} finally {
			this.memento = null;
		}
		return new ArrayList<StatisticalData>();
	}

	/**
	 * Set the log file meta data
	 */
	public void setLogFileMetaData(final LogFileMetaData logFileMetaData) {
		this.logFileMetaData = logFileMetaData;
	}

	private String getPercent(final int noOf, final int noTotal) {
		int result = 0;

		if (noTotal > 0) {
			float percent = ((float) noOf / (float) noTotal * 100);
			result = Math.round(percent);
		}
		if (result > 0) {
			return Messages.getString("StatisticalView.23")
					+ String.valueOf(result) + Messages.getString("StatisticalView.24");
		}
		return Messages.getString("StatisticalView.25");
	}

	private LogRecord getLogRecordAtRow(final int row) {
		LogRecord logRecord = null;
		try {
			logRecord = this.reader.getRecord(row);
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("TextTableModel.6") + e.getMessage()));  //$NON-NLS-1$
		} catch (ParseException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("TextTableModel.7") + e.getMessage()));  //$NON-NLS-1$
		}
		return logRecord;
	}
	
	private void createStatisticalViewContextMenuActions() {

		this.openMSCViewAction = new OpenMSCViewAction();
		this.openMSCViewAction.setEnabled(false);
		this.openMSCViewAction.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_MSC_VIEW));
		this.addSelectionChangedListener(openMSCViewAction);
		
		this.openTextTableStatisticalViewMenuAction = new OpenTextTableStatisticalViewMenuAction(this);
		this.openTextTableStatisticalViewMenuAction.setEnabled(false);
		this.openTextTableStatisticalViewMenuAction.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_TEXT_TABLE_VIEW));
	}

	/**
	 * Adds a menu to the selected row in the table
	 */
	private Menu hookStatisticalViewTableContextMenu(final Control control) {
		ProjectsViewerMenuManager menuMgr = new ProjectsViewerMenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new ProjectsViewerMenuListener() {
			@Override
			public void menuAboutToShow(final IMenuManager menuManager) {
					StatisticalView.this.fillStatisticalViewContextMenu(menuManager);
			}
		});
		return menuMgr.createContextMenu(control);
	}
	
	
	protected void fillStatisticalViewContextMenu(final IMenuManager menuManager) {
		// MB_ADDITIONS must be added to the menuMgr or platform will
		// throw a part initialize exception, this will fix this	
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(this.openMSCViewAction);
		menuManager.add(this.openTextTableStatisticalViewMenuAction);
	}

	@Override
	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
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
			listener.selectionChanged(new SelectionChangedEvent(this, new StructuredSelection(this.testcaseSelection)));
		}
	}

	/**
	 * Create a close all action in the tool bar
	 */
	private void createToolbar() {
		
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager mgr = actionBars.getToolBarManager();
		
		IAction closeAllAction = new Action() {
			@Override
			public void run() {

				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewReference[] viewReferences = activePage.getViewReferences();

				for (IViewReference reference : viewReferences) {
					IViewPart view = reference.getView(false);

					// memento restored views that never have had focus are
					// null!!!
					if (view == null) {
						activePage.hideView(reference);
					} else if (view instanceof StatisticalView) {
						activePage.hideView(reference);
					}
				}
			}
		};

		closeAllAction.setImageDescriptor(ImageDescriptor.createFromImage(Activator.getDefault().getIcon(
						Constants.ICONS_MSC_DELETE)));
		closeAllAction.setId(Messages.getString("StatisticalView.27")); //$NON-NLS-1$
		closeAllAction.setToolTipText(Messages.getString("StatisticalView.26")); //$NON-NLS-1$
		closeAllAction.setEnabled(true);

		mgr.add(closeAllAction);
		actionBars.updateActionBars();
	}

	/**
	 * This is a callback that will allow us to create the viewer and
	 * initialize it.
	 */
	@Override
	public void createPartControl(final Composite parent) {
		List<StatisticalData> statisticalData = restoreState();
		
		createToolbar();
		createStatisticalViewContextMenuActions();
		
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText("Statistics"); //$NON-NLS-1$
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		TableWrapData td = new TableWrapData();
		td.colspan = 2;
		
		layout.numColumns = 2;

		toolkit.paintBordersFor(form.getBody());

		if (statisticalData != null) {
			setData(statisticalData);
		}
	}
	
	/**
	 * Passing the focus request to the form.
	 */
	@Override
	public void setFocus() {
		form.setFocus();
	}
		
	private Section createSection() {
		Section section = toolkit.createSection(form.getBody(), Section.DESCRIPTION
				| ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
		TableWrapData td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		section.setLayoutData(td);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(true);
			}
		});
		
		File file = new File(this.logFileMetaData.getFilePath());
		Date date = new Date(file.lastModified());
		section.setText(file.getName());
		section.setData(this.logFileMetaData.getProjectRelativePath());
		section.setDescription(this.logFileMetaData.getProjectRelativePath() + " " + date.toString()); //$NON-NLS-1$
		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new GridLayout());
		createAmountTable(sectionClient);
		
		this.ecError = toolkit.createExpandableComposite(sectionClient, ExpandableComposite.TREE_NODE | ExpandableComposite.CLIENT_INDENT);
		ecError.setText("Error test cases"); //$NON-NLS-1$
		this.errorTestCasesTable = createTestCaseTable(ecError);
		ecError.setClient(this.errorTestCasesTable);
		ecError.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(true);
			}
		});
		
		this.ecFail = toolkit.createExpandableComposite(sectionClient, ExpandableComposite.TREE_NODE | ExpandableComposite.CLIENT_INDENT);
		ecFail.setText("Fail test cases"); //$NON-NLS-1$
		this.failTestCasesTable = createTestCaseTable(ecFail);
		ecFail.setClient(this.failTestCasesTable);
		
		ecFail.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(true);
			}
		});
		
		this.ecTestCases = toolkit.createExpandableComposite(sectionClient, ExpandableComposite.TREE_NODE | ExpandableComposite.CLIENT_INDENT);
		ecTestCases.setText("Test cases"); //$NON-NLS-1$
		this.testCases = createTestCaseTable(ecTestCases);
		ecTestCases.setClient(this.testCases);
		ecTestCases.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setClient(sectionClient);
		
		cachedSections.put(this.logFileMetaData.getProjectRelativePath(), section);
		
		return section;
		
	}

	private Table createAmountTable(final Composite composite) {
		this.amountTable = toolkit.createTable(composite, SWT.NONE);
		amountTable.setBackgroundMode(SWT.INHERIT_DEFAULT);

		this.amountTable.setHeaderVisible(true);
		
		this.amountTable.setLinesVisible(true);

		createAmountColumn(Messages.getString("StatisticalView.1"));
		createAmountColumn(Messages.getString("StatisticalView.2"));
		createAmountColumn(Messages.getString("StatisticalView.3"));
		createAmountColumn(Messages.getString("StatisticalView.4"));
		createAmountColumn(Messages.getString("StatisticalView.5"));
		createAmountColumn(Messages.getString("StatisticalView.6"));
		createAmountColumn(Messages.getString("StatisticalView.7"));
		return amountTable;
	}

	private void createAmountColumn(String title) {
		TableColumn column = new TableColumn(this.amountTable, SWT.BORDER);
		column.setText(title);
		column.setResizable(true);
		column.setMoveable(true);
		column.setWidth(DEFAULT_AMOUNT_COLUMN_WIDTH);
	}

	private Table createTestCaseTable(final Composite composite) {

		Table testCasesTable = toolkit.createTable(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.VIRTUAL);
		testCasesTable.setHeaderVisible(true);
		testCasesTable.setLinesVisible(true);

		testCasesTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
					if (e.getSource() instanceof Table) {
						Table table = (Table) e.getSource();
							
						TableItem tableItem = table.getItem(table.getSelectionIndex());
						Object data = tableItem.getData();
						if (data instanceof TestCase) {
							StatisticalView.this.testcaseSelection = (TestCase) data;
						} else {
							StatisticalView.this.testcaseSelection = null;
						}
						fireSelectionChangeEvent();
					}
				}
		});
		testCasesTable.setMenu(hookStatisticalViewTableContextMenu(testCasesTable));
		
		new TableColumn(testCasesTable, SWT.BORDER);
		createTestCasesColumn(testCasesTable, Messages.getString("StatisticalView.9"), DEFAULT_COLUMN_WIDTH);
		createTestCasesColumn(testCasesTable, Messages.getString("StatisticalView.10"), 5 * DEFAULT_COLUMN_WIDTH);
		createTestCasesColumn(testCasesTable, Messages.getString("StatisticalView.11"), 4* DEFAULT_COLUMN_WIDTH);
		createTestCasesColumn(testCasesTable, Messages.getString("StatisticalView.12"), 4* DEFAULT_COLUMN_WIDTH);
		testCasesTable.redraw();
		return testCasesTable;
	}

	private void createTestCasesColumn(Table testCasesTable, String title, int width) {
		TableColumn column = new TableColumn(testCasesTable, SWT.BORDER);
		column.setText(title);
		column.setResizable(true);
		column.setMoveable(true);
		column.setWidth(width);
	}

	@Override
	public LogFileMetaData getLogFileMetaData() {
		return logFileMetaData;
	}

	@Override
	public String getName() {
		return "Statistical View";
	}
	
}
