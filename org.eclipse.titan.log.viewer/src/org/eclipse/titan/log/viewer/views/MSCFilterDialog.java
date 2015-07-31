/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.models.FilterPattern;
import org.eclipse.titan.log.viewer.models.TimeInterval;
import org.eclipse.titan.log.viewer.models.FilterPattern.Field;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileHandler;

public class MSCFilterDialog extends Dialog {
	private FilterPattern filterPattern;
	private Tree checkTree;
	private Button allButton;
	private Button noneButton;
	private Button inclusiveButton;
	private Button isCaseSensitiveButton;
	private Button isRegularExpressionButton;
	private Button sourceInfoButton;
	private Button messageButton;
	private Text filterStringBox;
	private Text startTimestamp;
	private Text endTimestamp;
	private boolean changed = false;
	private CLabel invalidStartTimestamp;
	private CLabel invalidEndTimestamp;
	private boolean isFilterStringValid = true;
	private boolean isStartTimeStampValid = true;
	private boolean isEndTimeStampValid = true;
	private Listener changeListener = new Listener() {
		@Override
		public void handleEvent(final Event event) {
			MSCFilterDialog.this.changed = true;
		}
	};
	
	private Listener filterStringChanged = new Listener() {
		@Override
		public void handleEvent(final Event event) {
			if (!isRegularExpressionButton.getSelection()) {
				filterStringBox.setBackground(null);
				filterStringBox.setToolTipText(null);
				isFilterStringValid = true;
				validate();
				return;
			}
			
			try {
				Pattern.compile(filterStringBox.getText());
				filterStringBox.setBackground(null);
				filterStringBox.setToolTipText(null);
				isFilterStringValid = true;
			} catch (PatternSyntaxException e) {
				filterStringBox.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
				filterStringBox.setToolTipText("Invalid regular expression!");
				isFilterStringValid = false;
			}
			validate();
		}
	};
	
	private Listener startTimeStampChanged = new Listener() {
		@Override
		public void handleEvent(final Event event) {
			if (startTimestamp.getText().length() > 0
					&& LogFileHandler.validateTimeStamp(startTimestamp.getText(), filterPattern.getTimeInterval().getTimeStampFormat()) == null) {
				startTimestamp.setToolTipText("Invalid timestamp");
				startTimestamp.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
				invalidStartTimestamp.setVisible(true);
				isStartTimeStampValid = false;
				validate();
				return;
			}
			
			startTimestamp.setToolTipText("");
			startTimestamp.setBackground(null);
			invalidStartTimestamp.setVisible(false);
			isStartTimeStampValid = true;
			validate();
		}
	};
	
	private Listener endTimeStampChanged = new Listener() {
		@Override
		public void handleEvent(final Event event) {
			if (endTimestamp.getText().length() > 0
					&& LogFileHandler.validateTimeStamp(endTimestamp.getText(), filterPattern.getTimeInterval().getTimeStampFormat()) == null) {
				endTimestamp.setToolTipText("Invalid timestamp");
				endTimestamp.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
				invalidEndTimestamp.setVisible(true);
				isEndTimeStampValid = false;
				validate();
				return;
			}
			
			endTimestamp.setToolTipText("");
			endTimestamp.setBackground(null);
			invalidEndTimestamp.setVisible(false);
			isEndTimeStampValid = true;
			validate();
		}
	};
	
	public MSCFilterDialog(final Shell parentShell, final FilterPattern filterPattern) {
		super(parentShell);

		this.filterPattern = new FilterPattern(filterPattern);
	
	}
	
	public MSCFilterDialog(final Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		parent.getShell().setText("Filter");
		parent.getShell().setImage(Activator.getDefault().getIcon(Constants.ICONS_FILTER));
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout mainLayout = new GridLayout(2, false);
		
		mainLayout.marginHeight = 20;
		mainLayout.marginWidth = 20;
		container.setLayout(mainLayout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		

		
		createEventTypesGroup(container);
		setTreeContent(filterPattern);
		
		Composite rightSide = new Composite(container, SWT.NONE);
		GridLayout rightLayout = new GridLayout(1, true);
		rightLayout.verticalSpacing = 20;
		rightSide.setLayout(rightLayout);
		rightSide.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, true));
		createSearchGroup(rightSide);

		createTimeGroup(rightSide);
		
		return container;
	}
	
 	protected Group createEventTypesGroup(final Composite parent) {
		Group eventContainer = new Group(parent, SWT.NONE);
 		GridLayout layout = new GridLayout(2, true);
 		
		eventContainer.setText("Event types");
		eventContainer.setLayout(layout);
		
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.CENTER;
		eventContainer.setLayoutData(gd);

		this.checkTree = new Tree(eventContainer, SWT.CHECK | SWT.BORDER);
		this.checkTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.checkTree.addListener(SWT.Selection, MSCFilterDialog.this.changeListener);
		this.checkTree.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (e.detail == SWT.CHECK
						&& e.item instanceof TreeItem) {
					TreeItem item = (TreeItem) e.item;
					// Check if item has a parent
					TreeItem parent = item.getParentItem();

					// Item is a child
					if (parent != null) {
						TreeItem[] children = parent.getItems();
						int numberOfChildren = children.length;
						int checkedChildren = 0;
						for (TreeItem child : children) {
							if (child.getChecked()) {
								checkedChildren++;
							}
						}
						// Check if all children checked
						if (checkedChildren == numberOfChildren) {
							parent.setGrayed(false);
							parent.setChecked(true);
						} else if (checkedChildren == 0) {
							// Check if all children unchecked
							parent.setGrayed(false);
							parent.setChecked(false);
						} else {
							// Otherwise...
							parent.setChecked(true);
							parent.setGrayed(true);
						}

						// Item is a parent
					} else {
						item.setGrayed(false);
						boolean checked = item.getChecked();
						TreeItem[] children = item.getItems();
						for (TreeItem aChildren : children) {
							aChildren.setChecked(checked);
						}
					}
					changed = true;
				}
			}
			
		});

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		this.checkTree.setLayoutData(gridData);
		gridData.widthHint = 180;
		
		inclusiveButton = new Button(eventContainer, SWT.CHECK);
		inclusiveButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false, 2, 1));
		inclusiveButton.setText("Inclusive");
		inclusiveButton.setSelection(filterPattern.isInclusive());
		inclusiveButton.addListener(SWT.Selection, changeListener);
		
		allButton = new Button(eventContainer, SWT.PUSH);
		allButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		allButton.setText("Select all");
		allButton.addListener(SWT.Selection, changeListener);
		allButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				MSCFilterDialog.this.changed = true;
				MSCFilterDialog.this.selectAll();
			}
		});

		
		noneButton = new Button(eventContainer, SWT.PUSH);
		noneButton.setText("Deselect all");
		noneButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		noneButton.addListener(SWT.Selection, changeListener);
		noneButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				MSCFilterDialog.this.changed = true;
				MSCFilterDialog.this.deselectAll();
			}
		});
		
		return eventContainer;
	}
 
	protected Group createSearchGroup(final Composite parent) {
		Group searchGroup = new Group(parent, SWT.NONE);
		GridLayout searchLayout = new GridLayout(2, true);
		searchGroup.setLayout(searchLayout);
		searchGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		searchGroup.setText("Filter string");

		CLabel wildCardsLabel = new CLabel(searchGroup, SWT.NONE);
		wildCardsLabel.setText("* = any string, ? = any character, \\ = escape for literals: *?\\");
		wildCardsLabel.setImage(Activator.getDefault().getIcon(Constants.ICONS_TREE_TEXT_OBJ));
		wildCardsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		
		filterStringBox = new Text(searchGroup, SWT.BORDER);
		filterStringBox.setText(filterPattern.getFilterExpression());
		filterStringBox.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true , false, 2, 1));
		filterStringBox.addListener(SWT.Modify, filterStringChanged);
		filterStringBox.addListener(SWT.Modify, changeListener);
		
		isCaseSensitiveButton = new Button(searchGroup, SWT.CHECK);
		isCaseSensitiveButton.setText("Case sensitive");
		isCaseSensitiveButton.setSelection(filterPattern.isCaseSensitive());
		isCaseSensitiveButton.addListener(SWT.Selection, changeListener);
		
		isRegularExpressionButton = new Button(searchGroup, SWT.CHECK);
		isRegularExpressionButton.setText("Regular expression");
		isRegularExpressionButton.setSelection(filterPattern.isRegularExpression());
		isRegularExpressionButton.addListener(SWT.Selection, filterStringChanged);
		isRegularExpressionButton.addListener(SWT.Selection, changeListener);
		
		Label searchInLabel = new Label(searchGroup, SWT.NONE);
		searchInLabel.setText("Search in: ");
		searchInLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1));
		
		sourceInfoButton = new Button(searchGroup, SWT.CHECK);
		sourceInfoButton.setText("Source Info");
		if (filterPattern.getFieldsToFilter() != null) {
			sourceInfoButton.setSelection(filterPattern.getFieldsToFilter().get(Field.SOURCE_INFO));
		} else {
			sourceInfoButton.setSelection(true);
		}
		sourceInfoButton.addListener(SWT.Selection, changeListener);
		messageButton = new Button(searchGroup, SWT.CHECK);
		messageButton.setText("Message");
		if (filterPattern.getFieldsToFilter() != null) {
			messageButton.setSelection(filterPattern.getFieldsToFilter().get(Field.MESSAGE));
		} else {
			messageButton.setSelection(true);
		}
		messageButton.addListener(SWT.Selection, changeListener);
		
		return searchGroup;
	}
	
	protected Group createTimeGroup(final Composite parent) {
		Group timeGroup = new Group(parent, SWT.NONE);
		GridLayout timeLayout = new GridLayout(2, true);
		timeLayout.marginHeight = 20;
		timeGroup.setLayout(timeLayout);
		timeGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		timeGroup.setText("Filter by time interval");
		
		Composite start = new Composite(timeGroup, SWT.NONE);
		start.setLayout(new GridLayout(2, false));
		Label startLabel = new Label(start, SWT.NONE);
		startLabel.setText("Start: ");
		startTimestamp = new Text(start, SWT.BORDER);
		startTimestamp.setText(filterPattern.getTimeInterval() != null ? filterPattern.getTimeInterval().getStart() : "");
		startTimestamp.setLayoutData(new GridData(filterPattern.getTimeInterval().getTimeStampFormat().length() * 6, 20));
		startTimestamp.addListener(SWT.Modify, startTimeStampChanged);
		startTimestamp.addListener(SWT.Modify, changeListener);
		startTimestamp.setTextLimit(filterPattern.getTimeInterval().getTimeStampFormat().length());
		
		Composite end = new Composite(timeGroup, SWT.NONE);
		end.setLayout(new GridLayout(2, false));
		Label endLabel = new Label(end, SWT.NONE);
		endLabel.setText("End: ");
		
		endTimestamp = new Text(end, SWT.BORDER);
		endTimestamp.setText(filterPattern.getTimeInterval().getEnd() != null ? filterPattern.getTimeInterval().getEnd() : "");
		endTimestamp.setLayoutData(new GridData(filterPattern.getTimeInterval().getTimeStampFormat().length() * 6, 20));
		endTimestamp.addListener(SWT.Modify, endTimeStampChanged);
		endTimestamp.addListener(SWT.Modify, changeListener);
		endTimestamp.setTextLimit(filterPattern.getTimeInterval().getTimeStampFormat().length());
		
		invalidStartTimestamp = new CLabel(timeGroup, SWT.NONE);
		invalidStartTimestamp.setText("Invalid timestamp");
		invalidStartTimestamp.setImage(Activator.getDefault().getIcon(Constants.ICONS_ERROR));
		invalidStartTimestamp.setVisible(false);
		
		invalidEndTimestamp = new CLabel(timeGroup, SWT.NONE);
		invalidEndTimestamp.setText("Invalid timestamp");
		invalidEndTimestamp.setImage(Activator.getDefault().getIcon(Constants.ICONS_ERROR));
		invalidEndTimestamp.setVisible(false);
		
		CLabel formatLabel = new CLabel(timeGroup, SWT.NONE);
		formatLabel.setText("Format: " + filterPattern.getTimeInterval().getTimeStampFormat());
		formatLabel.setImage(Activator.getDefault().getIcon(Constants.ICONS_TREE_TEXT_OBJ));
		formatLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));
		
		return timeGroup;
	}
	
	
	protected void loadFromPreferenceStore() {
		// Get default values from preference store
		String prefDefValues = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.PREF_SILENT_EVENTS_CATEGORIES);
		if (prefDefValues.length() > 0) {
			updateCheckedState(prefDefValues);
			updateGrayedState();
		}
	}
	
	/**
	 * Updates the setChecked state of all tree items (parents and children)
	 * 
	 * @param prefValues the preference store value for which 
	 * tree items that should be checked
	 */
	private void updateCheckedState(final String prefValues) {
		String[] categories = prefValues.split(PreferenceConstants.PREFERENCE_DELIMITER);
		for (String category : categories) {
			String[] currCategory = category.split(PreferenceConstants.SILENT_EVENTS_KEY_VALUE_DELIM);

			if (currCategory.length > 1) {
				String currKey = currCategory[0];
				boolean currValue = Boolean.valueOf(currCategory[1]);

				if (currKey.contains(PreferenceConstants.SILENT_EVENTS_UNDERSCORE)) {
					// CAT + SUB CAT
					String cat = currKey.split(PreferenceConstants.SILENT_EVENTS_UNDERSCORE)[0];
					int parentIndex = (Integer) this.checkTree.getData(cat);
					int childIndex = (Integer) this.checkTree.getData(currKey);
					this.checkTree.getItem(parentIndex).getItem(childIndex).setChecked(currValue);
				} else {
					// CAT
					this.checkTree.getItem((Integer) this.checkTree.getData(currKey)).setChecked(currValue);
				}
			}
		}
	}
	
	/**
	 * Updates the setGrayed state of the parents
	 * A parent is grayed if one or more, but not all of the children is checked  
	 */
	private void updateGrayedState() {
		TreeItem[] parents = this.checkTree.getItems();
		int numOfCheckChildren;
		for (TreeItem parent : parents) {
			TreeItem[] children = parent.getItems();
			numOfCheckChildren = 0;
			for (TreeItem child : children) {
				if (child.getChecked()) {
					numOfCheckChildren++;
				}
			}
			if ((numOfCheckChildren > 0) && (numOfCheckChildren < children.length)) {
				parent.setGrayed(true);
			} else {
				parent.setGrayed(false);
			}
		}
	}
	
	public FilterPattern getFilterPattern() {
		return filterPattern;
	}
	
	public boolean getChanged() {
		return changed;
	}
	
	private boolean validate() {
		if (isFilterStringValid && isStartTimeStampValid && isEndTimeStampValid) {
			MSCFilterDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(true);
			return true;
		}
		MSCFilterDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(false);
		return false;
	}

	@Override
	protected void okPressed() {
		if (!changed) {
			setReturnCode(OK);
			close();
			return;
		}
		
		for (TreeItem item : checkTree.getItems()) {
			filterPattern.getEventsToFilter().put((String) item.getData(), item.getChecked());
		}
		filterPattern.setInclusive(inclusiveButton.getSelection());
		
		Map<Field, Boolean> fieldsToFilter = new HashMap<Field, Boolean>();
		
		fieldsToFilter.put(Field.SOURCE_INFO, sourceInfoButton.getSelection());
		fieldsToFilter.put(Field.MESSAGE, messageButton.getSelection());
		
		filterPattern.setFilterExpression(filterStringBox.getText(), fieldsToFilter, 
				isCaseSensitiveButton.getSelection(), isRegularExpressionButton.getSelection());
		
		filterPattern.setCaseSensitive(isCaseSensitiveButton.getSelection());
		filterPattern.setRegularExpression(isRegularExpressionButton.getSelection());
		
		filterPattern.setTimeInterval(
				new TimeInterval(startTimestamp.getText(), endTimestamp.getText(), filterPattern.getTimeInterval().getTimeStampFormat()));
		
		setReturnCode(OK);
		close();
	}
	
	private void setTreeContent(final FilterPattern pattern) {
		checkTree.setRedraw(false);
		if (pattern.containsSilentEvents()) {
			for (Map.Entry<String, String[]> entry : Constants.EVENT_CATEGORIES.entrySet()) {
				TreeItem parentItem = new TreeItem(checkTree, SWT.NONE);
				parentItem.setText(entry.getKey());
				parentItem.setData(entry.getKey());
				parentItem.setChecked(pattern.getEventsToFilter().get(entry.getKey()));
				
				for (String str : entry.getValue()) {
					String silentEvent = entry.getKey() + PreferenceConstants.SILENT_EVENTS_UNDERSCORE + str;
					TreeItem childItem = new TreeItem(parentItem, SWT.NONE);
					childItem.setText(str);
					childItem.setData(silentEvent);
					if (pattern.getEventsToFilter().get(silentEvent) != null) {
						childItem.setChecked(pattern.getEventsToFilter().get(silentEvent));
					} else {
						childItem.setChecked(false);
					}
				}
			}
			checkTree.setRedraw(true);
			return;
		}
		
		for (Map.Entry<String, String[]> entry : Constants.EVENT_CATEGORIES.entrySet()) {
			TreeItem parentItem = new TreeItem(checkTree, SWT.NONE);
			parentItem.setText(entry.getKey());
			parentItem.setData(entry.getKey());
			parentItem.setChecked(filterPattern.getEventsToFilter().get(entry.getKey()));
		}
		checkTree.setRedraw(true);
	}
	
	/**
	 * Deselects all tree items (parents and children)
	 */
	public void deselectAll() {
		TreeItem[] parents = this.checkTree.getItems();
		for (TreeItem parent : parents) {
			parent.setChecked(false);
			if (parent.getGrayed()) {
				parent.setGrayed(false);
			}
			TreeItem[] children = parent.getItems();
			for (TreeItem child : children) {
				child.setChecked(false);
			}
		}
	}
	
	/**
	 * Selects all tree items (parents and children)
	 */
	public void selectAll() {
		TreeItem[] parents = this.checkTree.getItems();
		for (TreeItem parent : parents) {
			parent.setChecked(true);
			if (parent.getGrayed()) {
				parent.setGrayed(false);
			}
			TreeItem[] children = parent.getItems();
			for (TreeItem child : children) {
				child.setChecked(true);
			}
		}
	}

}
