/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.models.FilterPattern.Field;
import org.eclipse.titan.log.viewer.search.LogSearchQuery;
import org.eclipse.titan.log.viewer.search.SearchPattern;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.utils.SelectionUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

public class LogSearchPage extends DialogPage implements ISearchPage {

	private static final int ALL_NONE_BUTTONS = 2;
	private static final String WILDCARDS_TEXT = " * = any string, ? = any character, \\ = escape for literals: *?\\ ";
	private static final Image INFORMATION_ICON;
	private static final Image INVALID_DATA_ICON;
	
	private Combo searchString;
	private Button caseSensitive;
	private Button regularExpression;
	private CLabel wildcards;

	private Button[] searchFor;
	private Button[] limitTo;

	private IDialogSettings dialogSettings = null;
	private boolean firstTime = true;

	private static final int HISTORY_SIZE = 12;
	private final List<SearchPattern> previousSearchPatterns;

	private ISearchPageContainer container;

	
	static {
		INFORMATION_ICON = Activator.getDefault().getIcon(Constants.ICONS_INFORMATION); 
		INVALID_DATA_ICON = Activator.getDefault().getIcon(Constants.ICONS_INVALID_DATA); 
	}
	
	public LogSearchPage() {
		previousSearchPatterns = new ArrayList<SearchPattern>();
	}

	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		readConfiguration();

		Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 10;
		result.setLayout(layout);

		Control expression = createExpression(result);
		expression.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

		Control searchFor = createSearchFor(result);
		searchFor.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

		Control limitTo = createLimitTo(result);
		limitTo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));

		setControl(result);
	}

	private Control createExpression(final Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		result.setLayout(layout);

		Label label = new Label(result, SWT.LEFT);
		label.setText("Search String");
		label.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 2, 1));

		searchString = new Combo(result, SWT.SINGLE | SWT.BORDER);
		searchString.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		searchString.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				getContainer().setPerformActionEnabled(isValidSearchPattern());
			}
		});
		searchString.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				handlePatternSelected();
				getContainer().setPerformActionEnabled(isValidSearchPattern());
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				handlePatternSelected();
				getContainer().setPerformActionEnabled(isValidSearchPattern());
			}
		});
		
		caseSensitive = new Button(result, SWT.CHECK);
		caseSensitive.setText("Case sensitive");
		
		wildcards = new CLabel(result, SWT.LEFT);
		wildcards.setText(WILDCARDS_TEXT);
		wildcards.setImage(INFORMATION_ICON);
		
		regularExpression = new Button(result, SWT.CHECK);
		regularExpression.setText("Regular expression");
		regularExpression.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				getContainer().setPerformActionEnabled(isValidSearchPattern());
			}
		});
		
		return result;
	}

	private Control createSearchFor(final Composite parent) {
		Group result = new Group(parent, SWT.NONE);
		result.setText("Search For");
		result.setLayout(new GridLayout(5, true));

		searchFor = new Button[Constants.EVENT_CATEGORIES.size() + 2];
		int i = 0;
		for (Iterator<String> it = Constants.EVENT_CATEGORIES.keySet().iterator(); it.hasNext(); ++i) {
			Button tmp = new Button(result, SWT.CHECK);
			String currentEvent = it.next();
			tmp.setText(currentEvent);
			tmp.setData(currentEvent);
			tmp.setSelection(true);
			tmp.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(final Event event) {
					getContainer().setPerformActionEnabled(isValidSearchPattern());
					searchFor[searchFor.length - 1].setSelection(false);
					searchFor[searchFor.length - 2].setSelection(false);
					
				}
			});
			searchFor[i] = tmp;
		}

		Button all = new Button(result, SWT.PUSH);
		all.setText("Select all");
		all.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				for (int i = 0; i < searchFor.length - ALL_NONE_BUTTONS; ++i) {
					searchFor[i].setSelection(true);
				}
				getContainer().setPerformActionEnabled(isValidSearchPattern());
			}
		});
		all.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		searchFor[i] = all;

		Button none = new Button(result, SWT.PUSH);
		none.setText("Deselect all");
		none.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				for (int i = 0; i < searchFor.length - ALL_NONE_BUTTONS; ++i) {
					searchFor[i].setSelection(false);
				}
				getContainer().setPerformActionEnabled(isValidSearchPattern());
			}
		});
		none.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		searchFor[i + 1] = none;
		return result;
	}

	private Control createLimitTo(final Composite parent) {
		Group result = new Group(parent, SWT.NONE);
		result.setText("Limit To");
		result.setLayout(new GridLayout(1, false));

		Button sourceInfo = new Button(result, SWT.CHECK);
		sourceInfo.setText("Source Info");
		sourceInfo.setData(Field.SOURCE_INFO);
		sourceInfo.setSelection(true);

		Button message = new Button(result, SWT.CHECK);
		message.setText("Message");
		message.setData(Field.MESSAGE);
		message.setSelection(true);

		limitTo = new Button[2];
		limitTo[0] = sourceInfo;
		limitTo[1] = message;

		return result;
	}

	private List<IFile> collectFilesForSearch() {
		List<IFile> files = new ArrayList<IFile>();

		switch (getContainer().getSelectedScope()) {
		case ISearchPageContainer.WORKSPACE_SCOPE:
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject project : projects) {
				getLogFilesFromResource(project, files);
			}
			break;
		case ISearchPageContainer.SELECTION_SCOPE:
			IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			ISelection selection = activePage.getSelection();
			if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				for (Iterator<?> it = structuredSelection.iterator(); it.hasNext();) {
					Object o = it.next();
					if (o instanceof IResource) {
						getLogFilesFromResource((IResource) o, files);
					}
				}
			}
			break;
		case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
			String[] projectNames = getContainer().getSelectedProjectNames();
			for (String name : projectNames) {
				IProject currentProject = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				getLogFilesFromResource(currentProject, files);
			}
			break;
		case ISearchPageContainer.WORKING_SET_SCOPE:
			IWorkingSet[] workingSets = getContainer().getSelectedWorkingSets();
			if (workingSets == null || workingSets.length < 1) {
				break;
			}
			for (IWorkingSet workingSet : workingSets) {
				for (IAdaptable element : workingSet.getElements()) {
					getLogFilesFromResource((IResource) element, files);
				}
			}
			break;
		default:
			break;
		}

		return files;
	}

	private void getLogFilesFromResource(final IResource resource, final List<IFile> files) {
		if (resource == null || files == null) {
			return;
		}

		if (resource instanceof IFile) {
			IFile myFile = (IFile) resource;
			if (isValidLogFile(myFile) && !files.contains(myFile)) {
				files.add((IFile) resource);
			}
		} else if (resource instanceof IContainer) {
			try {
				if (resource instanceof IProject) {
					IProject project = (IProject) resource;
					if (!project.isOpen() || project.getNature(Constants.NATURE_ID) == null) {
						return;
					}
				}
				for (IResource r : ((IContainer) resource).members()) {
					getLogFilesFromResource(r, files);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
				TitanLogExceptionHandler.handleException(
						new TechnicalException(Messages.getString("Could not collect the log files. Reason: ") + e.getMessage()));  //$NON-NLS-1$
			}
		}
	}

	@Override
	public boolean performAction() {
		List<IFile> files = collectFilesForSearch();
		LogSearchQuery query = new LogSearchQuery(files, getPattern());
		for (ISearchQuery runningQuery : NewSearchUI.getQueries()) {
			NewSearchUI.cancelQuery(runningQuery);
		}
		NewSearchUI.runQueryInBackground(query);
		return true;
	}

	@Override
	public void setContainer(final ISearchPageContainer container) {
		this.container = container;
	}

	private boolean isValidLogFile(final IFile file) {
		return SelectionUtils.hasLogFileExtension(file);
	}

	public ISearchPageContainer getContainer() {
		return container;
	}

	@Override
	public void dispose() {
		writeConfiguration();
		super.dispose();
	}

	private SearchPattern getPattern() {
		String string = this.searchString.getText();
		SearchPattern match = findInPrevious(string);
		if (match != null) {
			previousSearchPatterns.remove(match);
		}

		Map<String, Boolean> events = new TreeMap<String, Boolean>();
		for (int i = 0; i < searchFor.length - ALL_NONE_BUTTONS; ++i) {
			events.put(searchFor[i].getData().toString(), searchFor[i].getSelection());
		}

		Map<Field, Boolean> limitToMap = new HashMap<Field, Boolean>();
		for (Button aLimitTo : limitTo) {
			limitToMap.put(Field.valueOf(aLimitTo.getData().toString()), aLimitTo.getSelection());
		}

		match = new SearchPattern(string, caseSensitive.getSelection(), regularExpression.getSelection(), events, limitToMap, 
									getContainer().getSelectedScope(), getContainer().getSelectedWorkingSets());
		previousSearchPatterns.add(0, match);
		return match;
	}

	private SearchPattern findInPrevious(final String pattern) {
		for (SearchPattern element : previousSearchPatterns) {
			if (pattern.equals(element.getSearchString())) {
				return element;
			}
		}
		return null;
	}

	private void readConfiguration() {
		IDialogSettings settings = getDialogSettings();
		int historySize;
		try {
			historySize = settings.getInt("historySize");
		} catch (NumberFormatException e) {
			historySize = HISTORY_SIZE;
		}
		
		try {
			for (int i = 0; i < historySize; ++i) {
				IDialogSettings histSettings = settings.getSection("history" + i);
				if (histSettings != null) {
					SearchPattern data = SearchPattern.create(histSettings);
					if (data != null) {
						previousSearchPatterns.add(data);
					}
				}
			}
		} catch (NumberFormatException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(
					new TechnicalException(Messages.getString("Could not read the configuration of the page. Reason: ") + e.getMessage()));
		}
	}

	private void writeConfiguration() {
		IDialogSettings settings = getDialogSettings();

		int historySize = Math.min(previousSearchPatterns.size(), HISTORY_SIZE);
		settings.put("historySize", historySize);
		for (int i = 0; i < historySize; ++i) {
			IDialogSettings histSettings = settings.addNewSection("history" + i);
			SearchPattern pattern = previousSearchPatterns.get(i);
			pattern.store(histSettings);
		}
	}

	/**
	 * Returns the settings of the page
	 * 
	 * @return the page settings
	 */
	private IDialogSettings getDialogSettings() {
		if (dialogSettings == null) {
			dialogSettings = Activator.getDefault().getDialogSettingsSection("search");
		}
		return dialogSettings;
	}

	@Override
	public void setVisible(final boolean visible) {
		if (visible && searchString != null) {
			if (firstTime) {
				firstTime = false;
				String[] searchStrings = new String[previousSearchPatterns.size()];
				for (int i = 0; i < searchStrings.length; ++i) {
					searchStrings[i] = previousSearchPatterns.get(i).getSearchString();
				}
				searchString.setItems(searchStrings);
			}
			searchString.setFocus();
		}

		getContainer().setPerformActionEnabled(isValidSearchPattern());
		super.setVisible(visible);
	}

	private boolean isValidSearchPattern() {
		if (searchString.getText().length() == 0) {
			wildcards.setText(WILDCARDS_TEXT);
			wildcards.setForeground(null);
			wildcards.setImage(INFORMATION_ICON);
			return false;
		} 
		
		
		if (regularExpression.getSelection()) {
			try {
				Pattern.compile(searchString.getText());
			} catch (PatternSyntaxException e) {
				wildcards.setText("Invalid regular expression");
				wildcards.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
				wildcards.setImage(INVALID_DATA_ICON);
				return false;
			}
		} 
		
		wildcards.setText(WILDCARDS_TEXT);
		wildcards.setForeground(null);
		wildcards.setImage(INFORMATION_ICON);
		
		for (int i = 0; i < searchFor.length - ALL_NONE_BUTTONS; ++i) {
			if (searchFor[i].getSelection()) {
				return true;
			}
		}
		return false;
	}

	private void handlePatternSelected() {
		int selectionIndex = searchString.getSelectionIndex();
		if (selectionIndex < 0 || selectionIndex >= previousSearchPatterns.size()) {
			return;
		}
		SearchPattern pattern = previousSearchPatterns.get(selectionIndex);

		SortedMap<String, Boolean> events = pattern.getEvents();
		for (int i = 0; i < searchFor.length - ALL_NONE_BUTTONS; ++i) {
			searchFor[i].setSelection(events.get(searchFor[i].getData().toString()));
		}
		Map<Field, Boolean> limitToMap = pattern.getLimitTo();
		for (Button button : limitTo) {
			button.setSelection(limitToMap.get(button.getData()));
		}

		searchString.setText(pattern.getSearchString());
		caseSensitive.setSelection(pattern.isCaseSensitive());
		regularExpression.setSelection(pattern.isRegularExpression());

		if (pattern.getWorkingSets() != null) {
			getContainer().setSelectedWorkingSets(pattern.getWorkingSets());
		} else {
			getContainer().setSelectedScope(pattern.getScope());
		}

	}

}
