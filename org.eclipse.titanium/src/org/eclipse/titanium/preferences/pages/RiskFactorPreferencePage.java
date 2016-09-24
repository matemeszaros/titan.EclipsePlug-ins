/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.markers.types.ProblemNameToPreferenceMapper;
import org.eclipse.titanium.markers.types.TaskType;
import org.eclipse.titanium.preferences.PreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class RiskFactorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Tuning parameters of project risk factor calculation.";

	private static final Map<String, String> USED_MARKERS;
	static {
		USED_MARKERS = new HashMap<String, String>();
		insertTask(TaskType.FIXME);
		insertTask(TaskType.TODO);
		insertSP(CodeSmellType.CIRCULAR_IMPORTATION);
		insertSP(CodeSmellType.TOO_MANY_STATEMENTS);
		insertSP(CodeSmellType.TOO_MANY_PARAMETERS);
		// insertSP(SemanticProblemType.); // TODO: divergent naming convention
		insertSP(CodeSmellType.UNCOMMENTED_FUNCTION);
		insertSP(CodeSmellType.TYPENAME_IN_DEFINITION);
		insertSP(CodeSmellType.MODULENAME_IN_DEFINITION);
		insertSP(CodeSmellType.VISIBILITY_IN_DEFINITION);
		insertSP(CodeSmellType.UNINITIALIZED_VARIABLE);
		insertSP(CodeSmellType.GOTO);
		insertSP(CodeSmellType.UNUSED_IMPORT);
		insertSP(CodeSmellType.UNUSED_GLOBAL_DEFINITION);
		insertSP(CodeSmellType.UNUSED_LOCAL_DEFINITION);
		insertSP(CodeSmellType.UNUSED_FUNTION_RETURN_VALUES);
		insertSP(CodeSmellType.UNUSED_STARTED_FUNCTION_RETURN_VALUES);
		insertSP(CodeSmellType.INFINITE_LOOP);
		// insertSP(SemanticProblemType.); // TODO: busy wait
		insertSP(CodeSmellType.NONPRIVATE_PRIVATE);
		// insertSP(SemanticProblemType.INCORRECT_SHIFT_ROTATE_SIZE); TODO:
		// missing label in ExRotSize in the xls
		insertSP(CodeSmellType.SIZECHECK_IN_LOOP);
		insertSP(CodeSmellType.TOO_COMPLEX_EXPRESSIONS);
		insertSP(CodeSmellType.READONLY_INOUT_PARAM);
		insertSP(CodeSmellType.READONLY_OUT_PARAM);
		insertSP(CodeSmellType.READONLY_LOC_VARIABLE);
		insertSP(CodeSmellType.EMPTY_STATEMENT_BLOCK);
		insertSP(CodeSmellType.SETVERDICT_WITHOUT_REASON);
		// insertSP(SemanticProblemType.); // TODO: cannot identify VariOutEn
		// smell
		insertSP(CodeSmellType.STOP_IN_FUNCTION);
		insertSP(CodeSmellType.UNNECESSARY_VALUEOF);
		insertSP(CodeSmellType.MAGIC_NUMBERS);
		insertSP(CodeSmellType.MAGIC_STRINGS);
		insertSP(CodeSmellType.LOGIC_INVERSION);
		insertSP(CodeSmellType.UNNECESSARY_CONTROLS);
	}

	private IPreferenceStore prefStore;
	private Text projectBase;
	private List<String> markers;
	private List<Combo> impacts;
	private List<Text> baselines;
	

	public RiskFactorPreferencePage() {
		super("Risk factor parameters");
		prefStore = Activator.getDefault().getPreferenceStore();
		markers = new ArrayList<String>();
		impacts = new ArrayList<Combo>();
		baselines = new ArrayList<Text>();
	}
	
	@Override
	public void init(final IWorkbench sworkbench) {
		setDescription(DESCRIPTION);
	}
	
	@Override
	protected Control createContents(final Composite parent) {
		final Composite tmpParent = new Composite(parent, 0);
		GridData gd;

		tmpParent.setLayout(new GridLayout(1, true));
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		tmpParent.setLayoutData(gd);

		final Composite head = new Composite(tmpParent, 0);
		head.setLayout(new GridLayout(2, false));
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		head.setLayoutData(gd);

		Label label = new Label(head, 0);
		label.setText("Base risk factor");
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		label.setLayoutData(gd);

		projectBase = new Text(head, SWT.SINGLE);
		projectBase.setTextLimit(7);
		gd = new GridData();
		gd.widthHint = 100;
		projectBase.setLayoutData(gd);

		final Group rest = new Group(tmpParent, 0);
		rest.setText("Parameters of the code smells");
		final GridLayout layout = new GridLayout(3, false);
		rest.setLayout(layout);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		rest.setLayoutData(gd);

		label = new Label(rest, 0);
		label.setText("Code smell");
		gd = new GridData();
		gd.widthHint = 250;
		gd.grabExcessHorizontalSpace = true;
		label.setLayoutData(gd);

		label = new Label(rest, 0);
		label.setText("Impact factor");
		gd = new GridData();
		gd.widthHint = 80;
		label.setLayoutData(gd);

		label = new Label(rest, 0);
		label.setText("LoC for one occurrence");
		gd = new GridData();
		gd.widthHint = 150;
		label.setLayoutData(gd);

		for (final String markerName : USED_MARKERS.keySet()) {
			createRow(markerName, rest);
		}
		load();
		return tmpParent;
	}

	private void createRow(final String markerName, final Composite comp) {
		GridData gd;
		final Label label = new Label(comp, 0);
		label.setText(USED_MARKERS.get(markerName));
		final Combo imp = new Combo(comp, SWT.DROP_DOWN);
		imp.setItems(new String[] { "low", "medium", "high" });
		imp.setTextLimit(6);
		gd = new GridData();
		gd.widthHint = 80;
		imp.setLayoutData(gd);
		final Text bl = new Text(comp, SWT.SINGLE);
		bl.setTextLimit(7);
		gd = new GridData();
		gd.widthHint = 60;
		bl.setLayoutData(gd);

		markers.add(markerName);
		impacts.add(imp);
		baselines.add(bl);
		// TODO: validators to check if the field contains sensible LoC info
		// TODO: tooltips
	}

	private void load() {
		projectBase.setText(prefStore.getString(PreferenceConstants.BASE_RISK_FACTOR));
		for (int i = 0; i < markers.size(); ++i) {
			impacts.get(i).select(prefStore.getInt(ProblemNameToPreferenceMapper.nameSmellImpact(markers.get(i))) - 1);
			baselines.get(i).setText(prefStore.getString(ProblemNameToPreferenceMapper.nameSmellBaseLine(markers.get(i))));
		}
		updateApplyButton();
	}

	@Override
	protected void performDefaults() {
		projectBase.setText(prefStore.getDefaultString(PreferenceConstants.BASE_RISK_FACTOR));
		for (int i = 0; i < USED_MARKERS.size(); ++i) {
			impacts.get(i).select(prefStore.getDefaultInt(ProblemNameToPreferenceMapper.nameSmellImpact(markers.get(i))) - 1);
			baselines.get(i).setText(prefStore.getDefaultString(ProblemNameToPreferenceMapper.nameSmellBaseLine(markers.get(i))));
		}
		updateApplyButton();
	}

	@Override
	public boolean performOk() {
		prefStore.setValue(PreferenceConstants.BASE_RISK_FACTOR, projectBase.getText());
		for (int i = 0; i < USED_MARKERS.size(); ++i) {
			prefStore.setValue(ProblemNameToPreferenceMapper.nameSmellImpact(markers.get(i)), impacts.get(i).getSelectionIndex() + 1);
			prefStore.setValue(ProblemNameToPreferenceMapper.nameSmellBaseLine(markers.get(i)), baselines.get(i).getText());
		}
		return true;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return prefStore;
	}

	private static void insertSP(final CodeSmellType p) {
		USED_MARKERS.put(p.name(), p.getHumanReadableName());
	}

	private static void insertTask(final TaskType p) {
		USED_MARKERS.put(p.name(), p.getHumanReadableName());
	}
}