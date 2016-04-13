/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences.pages;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.markers.types.ProblemNameToPreferenceMapper;
import org.eclipse.titanium.markers.types.ProblemType;
import org.eclipse.titanium.markers.types.TaskType;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

class TimeDataEntry{
	public double minTime;
	public double avgTime;
	public double maxTime;
	
	public TimeDataEntry(final Double minTime, final Double avgTime, final Double maxTime){
		this.minTime = minTime;
		this.avgTime = avgTime;
		this.maxTime = maxTime;
	}
}

public class RepairTimePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Here we can set the repair times for code smells";
	private final IPreferenceStore prefStore;
	private Map<ProblemType, TimeDataEntry> storedValues;
	
	public RepairTimePage() {
		prefStore = Activator.getDefault().getPreferenceStore();
		storedValues = new HashMap<ProblemType, TimeDataEntry>();
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	protected Control createContents(final Composite parent) {
		Composite ret = new Composite(parent,0);
		ret.setLayout(new GridLayout(4, false));
		
		new Label(ret, SWT.NONE).setText("");
		new Label(ret, SWT.NONE).setText("Minimal");
		new Label(ret, SWT.NONE).setText("Average");
		new Label(ret, SWT.NONE).setText("Maximal");
		
		for (TaskType actSmell : TaskType.values()) {
			makeNewRow(ret, actSmell);
		}
		
		for (CodeSmellType actSmell : CodeSmellType.values()) {
			makeNewRow(ret, actSmell);
		}
		
		return ret;
	}
	
	@Override
	public boolean performOk() {
		
		for (TaskType actSmell : TaskType.values()) {
			TimeDataEntry value = storedValues.get(actSmell);
			if (value != null) {
				setAvgValue(actSmell, value.avgTime);
				setMinValue(actSmell, value.minTime);
				setMaxValue(actSmell, value.maxTime);
			}
		}
		
		for (CodeSmellType actSmell : CodeSmellType.values()) {
			TimeDataEntry value = storedValues.get(actSmell);
			if (value != null) {
				setAvgValue(actSmell, value.avgTime);
				setMinValue(actSmell, value.minTime);
				setMaxValue(actSmell, value.maxTime);
			}
		}
		
		return super.performOk();
	}
	
	@Override
	protected void performDefaults() {
		
		for (TaskType actSmell : TaskType.values()) {
			setAvgValue(actSmell, actSmell.getAvgDefaultTime());
			setMinValue(actSmell, actSmell.getMinDefaultTime());
			setMaxValue(actSmell, actSmell.getMaxDefaultTime());
		}
		
		for (CodeSmellType actSmell : CodeSmellType.values()) {
			setAvgValue(actSmell, actSmell.getAvgDefaultTime());
			setMinValue(actSmell, actSmell.getMinDefaultTime());
			setMaxValue(actSmell, actSmell.getMaxDefaultTime());
		}
		
		refresh();
		super.performDefaults();
	}
	
	private void refresh() {
		storedValues = new HashMap<ProblemType, TimeDataEntry>();
		createContents(getShell().getParent());
	}
	
	private void setAvgValue(final ProblemType smell, final Double value) {
		prefStore.setValue(ProblemNameToPreferenceMapper.nameSmellAvgTime(smell.toString()), value);
	}
	
	private void setMinValue(final ProblemType smell, final Double value) {
		prefStore.setValue(ProblemNameToPreferenceMapper.nameSmellMinTime(smell.toString()), value);
	}
	
	private void setMaxValue(final ProblemType smell, final Double value) {
		prefStore.setValue(ProblemNameToPreferenceMapper.nameSmellMaxTime(smell.toString()), value);
	}
	
	private void makeNewRow(final Composite parent,final ProblemType codeSmell) {
		Label lblValue = new Label(parent, SWT.NONE);
		lblValue.setText(codeSmell.getHumanReadableName()+": ");
		double minTime = codeSmell.getMinRepairTime();
		double avgTime = codeSmell.getAvgRepairTime();
		double maxTime = codeSmell.getMaxRepairTime();
		storedValues.put(codeSmell, new TimeDataEntry(minTime, avgTime, maxTime));
		
		final Text minText = new Text (parent, SWT.BORDER);
		try{
			minText.setText(String.valueOf(minTime));
			
		} catch(NumberFormatException e) {
			minText.setText("0.0");
		}
		minText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				try{
					TimeDataEntry times = storedValues.get(codeSmell);
					if (times != null) {
						times.minTime = Double.parseDouble(minText.getText());
					}
				} catch(NumberFormatException ex) {
					// Do nothing
				}
			}
		});
		
		final Text avgText = new Text (parent, SWT.BORDER);
		try{
			avgText.setText(String.valueOf(avgTime));
		} catch(NumberFormatException e) {
			avgText.setText("0.0");
		}
		avgText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				try{
					TimeDataEntry times = storedValues.get(codeSmell);
					if (times != null) {
						times.avgTime = Double.parseDouble(avgText.getText());
					}
				} catch(NumberFormatException ex) {
					// Do nothing
				}
			}
		});
		
		final Text maxText = new Text (parent, SWT.BORDER);
		try{
			maxText.setText(String.valueOf(maxTime));
		} catch(NumberFormatException e) {
			maxText.setText("0.0");
		}
		maxText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				try{
					TimeDataEntry times = storedValues.get(codeSmell);
					if (times != null) {
						times.maxTime = Double.parseDouble(maxText.getText());
					}
				} catch(NumberFormatException ex) {
					// Do nothing
				}
			}
		});
	}
}