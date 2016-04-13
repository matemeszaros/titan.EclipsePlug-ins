/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.preferences.pages.ComboFieldEditor;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.handler.MarkerHandler;
import org.eclipse.titanium.markers.utils.Analyzer;
import org.eclipse.titanium.markers.utils.AnalyzerCache;
import org.eclipse.titanium.preferences.PreferenceConstants;
import org.eclipse.titanium.preferences.ProblemTypePreference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * Preference page of code smells.
 * 
 * @author poroszd
 * 
 */
public final class MarkersPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String REPORT_TOO_COMPLEX_EXPRESSIONS_SIZE = "The amount of operators that should not be exceed";
	private static final String REPORT_TOO_MANY_PARAMETERS_SIZE = "The size the amount of parameters should not exceed";
	private static final String REPORT_TOO_MANY_STATEMENTS_SIZE = "The size the amount of statements should not exceed";
	private static final String REPORT_TOO_MANY_CONSECUTIVE_ASSIGNMENTS_SIZE =
			"The number of consecutive assignment statements should not exceed";
	private static final String ON_THE_FLY = "Check code smells on-the-fly";
	private static final String ON_THE_FLY_TOOLTIP = "When checked, analysis for code smells will run each time after a sematic analysis.\n"
			+ "Note that enabling on-the-fly analysis on the TITAN preference page is required.";

	private static final String DESCRIPTION = "Preferences on reporting code smells";

	private static final String[][] IGNORE_WARNING_ERROR = new String[][] { { "Ignore", GeneralConstants.IGNORE },
			{ "Warning", GeneralConstants.WARNING }, { "Error", GeneralConstants.ERROR } };

	private static final Map<ProblemTypePreference, String> TOOLTIP_MAPPING;
	static {
		Map<ProblemTypePreference, String> m = new EnumMap<ProblemTypePreference, String>(ProblemTypePreference.class);

		m.put(ProblemTypePreference.ALTSTEP_COVERAGE, "Through it is not strictly necessary,"
				+ " it recommendable for an altstep to handle any possible incoming message types");
		m.put(ProblemTypePreference.CIRCULAR_IMPORTATION, "Circular importation is not prohibited in TTCN-3,"
				+ " but it is also not recommended.");
		m.put(ProblemTypePreference.CONSECUTIVE_ASSIGNMENTS, "Consecutive assignments should be merged due to performance issues");
		m.put(ProblemTypePreference.CONVERT_TO_ENUM, "Select statements should be used with enumerations. Branch coverage can not \n" +
				"be calculated on integers.");
		m.put(ProblemTypePreference.EMPTY_STATEMENT_BLOCK, "Empty statement blocks in the source code usually means,\n"
				+ "that the developer planned to write some code there to handle some use cases,\n"
				+ "but forgot to finish his work ... or is right now in the process of finishing it");
		m.put(ProblemTypePreference.GOTO,
				"In almost all cases the usage of goto should be forbidden as it can very easily breaks the principles of"
						+ " structured/well designed source code.");
		m.put(ProblemTypePreference.IF_INSTEAD_ALTGUARD, "When an alt branch begins with an if statement,"
				+ " it should be considered whether it might be changed to an alt guard");
		m.put(ProblemTypePreference.IF_INSTEAD_RECEIVE_TEMPLATE,
				"When an alt branch has an if statement, where the condition is a matching to the received value,"
						+ " it might be a better idea to split to multiple branch,"
						+ " each with different template matching in the receive statements.");
		m.put(ProblemTypePreference.IF_WITHOUT_ELSE, "All possible execution paths should be handled,"
				+ " atleast on the level of logging information in case of unexpected events");
		m.put(ProblemTypePreference.INCORRECT_SHIFT_ROTATE_SIZE,
				"Report too big (bigger than the string itself) or too small (negative) shift and rotation sizes.");
		m.put(ProblemTypePreference.INFINITE_LOOP, "When there is no way to escape the loop");
		m.put(ProblemTypePreference.ISBOUND_WITHOUT_ELSE, "In test systems it is valuable to log some information in the\n"
		+ " else branch on why it failed the test when using isbound/ispresent/ischosen");
		m.put(ProblemTypePreference.ISVALUE_WITH_VALUE, "Isvalue check on value always returns true. 'isbound' should be\n" +
				"used to check existence. ");
		m.put(ProblemTypePreference.ITERATE_ON_WRONG_ARRAY, "The loop parameter might be used to index the wrong list.");
		m.put(ProblemTypePreference.MAGIC_CONSTANTS,
				"It is always recommended to extract local literal values into constants and use the constants in the code.\n"
						+ "Since otherwise sooner or later it will be forgotten what that exact value was meaning");
		m.put(ProblemTypePreference.MISSING_FRIEND, "When the module referred to in a friend declaration could not be found..");
		m.put(ProblemTypePreference.MISSING_IMPORT, "When a module referred to in an import statement could not be found.");
		m.put(ProblemTypePreference.MODULENAME_IN_DEFINITION, "As definitions can be referenced in the modulename.identifier format,"
				+ " it is of no value if the name of the module is duplicated in the name of the definition\n"
				+ "But makes it longer");
		m.put(ProblemTypePreference.LAZY,
				"When an in parameter evaluation not sure under code execution it should be a @lazy parameter, "
				+ "otherwise not.");		
		m.put(ProblemTypePreference.LOGIC_INVERSION,
				"When an if statement has negated condition, and consists only an if- and an else branch,"
						+ " the negation can be left simply by swapping the two branches.");
		m.put(ProblemTypePreference.NONPRIVATE_PRIVATE, "By default all definitions are public,"
				+ " but by declaring some private one can make them invisible for importing modules.\n"
				+ "This might be usefull in case of internal functions, types, constants");
		m.put(ProblemTypePreference.PRIVATE_FIELD_VIA_PUBLIC, "It is able to find references, fields pointing to types, that otherwise should be invisible for the actual module.");
		m.put(ProblemTypePreference.PRIVATE_VALUE_VIA_PUBLIC, "It is able to find parametrized values, that pointing to types, that otherwise should be invisible for the actual module.");
		m.put(ProblemTypePreference.READING_OUT_PAR_BEFORE_WRITTEN, "An out parameter is read before a value is assigned to it.");
		m.put(ProblemTypePreference.READONLY_VARIABLE, "When a definition was declared to be changeable, but is never modified");
		m.put(ProblemTypePreference.SELECT_COVERAGE, "When using a select statement with enumeration type, all the possible \n" +
				"enumeration values should be handled in the select statement");
		m.put(ProblemTypePreference.SELECT_WITH_NUMBERS_SORTED, "When using a select statement with integer value type, the \n"
				+ "cases should be incremental ordered.");
		m.put(ProblemTypePreference.SETVERDICT_WITHOUT_REASON,
				"Setting any other verdict reason then pass, should be accompanied with a reason, with details for the verdict");
		m.put(ProblemTypePreference.SIZECHECK_IN_LOOP, "The loop condition is evaluated at the end of each loop\n"
				+ "It is therefore better performance wise to check it only once and save this value in a constant if possible");
		m.put(ProblemTypePreference.RECEIVE_ANY_TEMPLATE,
				"Report when a branch of an altstep uses receive action with a template matching to all possible values");
		m.put(ProblemTypePreference.SHORTHAND, "Some shorthand statements (timeout, receive, ...) should not be used in \n"
				+ "functions/altsteps/testcases with a 'runs on' clause; an activated default can change their behaviour");
		m.put(ProblemTypePreference.STOP_IN_FUNCTION, "The testcase stopping stop operation stops execution immediatelly\n"
				+ "Which can leave behind garbage in the test system, and the SUT in any state");
		m.put(ProblemTypePreference.SWITCH_ON_BOOLEAN, "In this case 'if' conditionals would be nmore effective");
		m.put(ProblemTypePreference.TOO_COMPLEX_EXPRESSIONS, "The more complex an expression is, the less likely it is to be correct");
		m.put(ProblemTypePreference.TOO_MANY_PARAMETERS,
				"The more parameters some entity has, the harder it becomes to fill them out correctly without introducing faults");
		m.put(ProblemTypePreference.TOO_MANY_STATEMENTS, "Rationale: If a statement block becomes very long it is hard to understand."
				+ " Therefore long statemen tblocks should usually be refactored into several individual ones,"
				+ " or into individual functions that focus on a specific task. ");
		m.put(ProblemTypePreference.TYPENAME_IN_DEFINITION, "As the comment and hover can display the type of the definition,"
				+ " it is of no value if it is duplicated in the name of the definition\n" + "But makes it longer");
		m.put(ProblemTypePreference.UNCOMMENTED_FUNCTION,
				"Functions, altsteps and testcases should have a header comment describing what and why they do");
		m.put(ProblemTypePreference.UNINITIALIZED_VARIABLE,
				"Initializing variables at declaration time is better performance wise, than assigning a value later");
		m.put(ProblemTypePreference.UNNECESSARY_CONTROLS, "Report controls that can be identified to be unnecessary in compilation time.\n"
				+ "For example when the conditional expression of an if statements evaluates to false in compilation time.");
		m.put(ProblemTypePreference.UNNECESSARY_VALUEOF, "Report when 'valueof' is applied to an actual value, thus having no effect");
		m.put(ProblemTypePreference.UNUSED_FUNTION_RETURN_VALUES, "When a function returns a value or a template, but it is not used.");
		m.put(ProblemTypePreference.UNUSED_GLOBAL_DEFINITION, "When a module level definition is never read/written.\n"
				+ " Also when a type is not used to declare other definitions.");
		m.put(ProblemTypePreference.UNUSED_IMPORT, "When nothing is used in the module from the imported module.");
		m.put(ProblemTypePreference.UNUSED_LOCAL_DEFINITION, "When a local variable or formal parameter is never read/written.");
		m.put(ProblemTypePreference.VISIBILITY_IN_DEFINITION,
				"Visibility attributes should not be mentioned in the names of the definitions\n"
						+ "They should be explicitly set as visibility attributes of the definition");

		TOOLTIP_MAPPING = Collections.unmodifiableMap(m);
	}

	private FontRegistry fonts;
	private List<ComboFieldEditor> editors;

	private boolean changed = false;

	public MarkersPreferencePage() {
		super(GRID);
		fonts = new FontRegistry();
		editors = new ArrayList<ComboFieldEditor>();
	}

	@Override
	protected Control createContents(final Composite parent) {
		Composite pagecomp = new Composite(parent, SWT.NONE);
		pagecomp.setLayout(new GridLayout(1, true));
		pagecomp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		return super.createContents(pagecomp);
	}

	@Override
	protected void createFieldEditors() {
		final Composite tempParent = new Composite(getFieldEditorParent(), 0);
		tempParent.setLayout(new GridLayout(1, true));

		Group top = new Group(tempParent, SWT.SHADOW_ETCHED_IN);
		top.setText("On-the-fly check");
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		top.setLayout(new FillLayout(SWT.VERTICAL));
		BooleanFieldEditor b1 = new BooleanFieldEditor(PreferenceConstants.ON_THE_FLY_SMELLS, ON_THE_FLY, BooleanFieldEditor.SEPARATE_LABEL,
				top);
		b1.getLabelControl(top).setToolTipText(ON_THE_FLY_TOOLTIP);
		addField(b1);
		{
			ExpandableComposite sec = new ExpandableComposite(tempParent,
					ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
			sec.setText("Potential Structural problems");
			sec.setFont(fonts.getBold(""));
			Composite comp = new Composite(sec, 0);
			comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			comp.setLayout(new FillLayout(SWT.VERTICAL));
			createField(comp, ProblemTypePreference.GOTO);
			createField(comp, ProblemTypePreference.CIRCULAR_IMPORTATION);
			createField(comp, ProblemTypePreference.MISSING_IMPORT);
			createField(comp, ProblemTypePreference.UNUSED_IMPORT);
			createField(comp, ProblemTypePreference.MISSING_FRIEND);
			createField(comp, ProblemTypePreference.NONPRIVATE_PRIVATE);
			createField(comp, ProblemTypePreference.PRIVATE_FIELD_VIA_PUBLIC);
			createField(comp, ProblemTypePreference.PRIVATE_VALUE_VIA_PUBLIC);
			createField(comp, ProblemTypePreference.VISIBILITY_IN_DEFINITION);
			sec.addExpansionListener(new CustomExpansionListener(comp));
			sec.setClient(comp);
			sec.setExpanded(true);
		}
		{
			ExpandableComposite sec = new ExpandableComposite(tempParent,
					ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
			sec.setText("Code style problems");
			sec.setFont(fonts.getBold(""));
			Composite comp = new Composite(sec, 0);
			comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			comp.setLayout(new FillLayout(SWT.VERTICAL));
			createField(comp, ProblemTypePreference.LOGIC_INVERSION);
			createField(comp, ProblemTypePreference.MODULENAME_IN_DEFINITION);
			createField(comp, ProblemTypePreference.TYPENAME_IN_DEFINITION);
			createField(comp, ProblemTypePreference.MAGIC_CONSTANTS);
			sec.addExpansionListener(new CustomExpansionListener(comp));
			sec.setClient(comp);
			sec.setExpanded(false);
		}
		{
			ExpandableComposite sec = new ExpandableComposite(tempParent,
					ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
			sec.setText("Potential performance problems");
			sec.setFont(fonts.getBold(""));
			Composite comp = new Composite(sec, 0);
			comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			comp.setLayout(new FillLayout(SWT.VERTICAL));
			createField(comp, ProblemTypePreference.INFINITE_LOOP);
			createField(comp, ProblemTypePreference.UNINITIALIZED_VARIABLE);
			createField(comp, ProblemTypePreference.SIZECHECK_IN_LOOP);
			createField(comp, ProblemTypePreference.CONSECUTIVE_ASSIGNMENTS);
			createIntegerEditor(comp, PreferenceConstants.TOO_MANY_CONSECUTIVE_ASSIGNMENTS_SIZE, REPORT_TOO_MANY_CONSECUTIVE_ASSIGNMENTS_SIZE);
			createField(comp, ProblemTypePreference.LAZY);	
			sec.addExpansionListener(new CustomExpansionListener(comp));
			sec.setClient(comp);
			sec.setExpanded(false);
		}
		{
			ExpandableComposite sec = new ExpandableComposite(tempParent,
					ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
			sec.setText("Potential programming problems");
			sec.setFont(fonts.getBold(""));
			Composite comp = new Composite(sec, 0);
			comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			comp.setLayout(new FillLayout(SWT.VERTICAL));
			createField(comp, ProblemTypePreference.READONLY_VARIABLE);
			createField(comp, ProblemTypePreference.TOO_MANY_PARAMETERS);
			createIntegerEditor(comp, PreferenceConstants.TOO_MANY_PARAMETERS_SIZE, REPORT_TOO_MANY_PARAMETERS_SIZE);
			createField(comp, ProblemTypePreference.TOO_COMPLEX_EXPRESSIONS);
			createIntegerEditor(comp, PreferenceConstants.TOO_COMPLEX_EXPRESSIONS_SIZE, REPORT_TOO_COMPLEX_EXPRESSIONS_SIZE);
			createField(comp, ProblemTypePreference.EMPTY_STATEMENT_BLOCK);
			createField(comp, ProblemTypePreference.TOO_MANY_STATEMENTS);
			createIntegerEditor(comp, PreferenceConstants.TOO_MANY_STATEMENTS_SIZE, REPORT_TOO_MANY_STATEMENTS_SIZE);
			createField(comp, ProblemTypePreference.INCORRECT_SHIFT_ROTATE_SIZE);
			createField(comp, ProblemTypePreference.IF_WITHOUT_ELSE);
			createField(comp, ProblemTypePreference.SWITCH_ON_BOOLEAN);
			createField(comp, ProblemTypePreference.SETVERDICT_WITHOUT_REASON);
			createField(comp, ProblemTypePreference.UNCOMMENTED_FUNCTION);
			createField(comp, ProblemTypePreference.STOP_IN_FUNCTION);
			createField(comp, ProblemTypePreference.UNUSED_FUNTION_RETURN_VALUES);
			createField(comp, ProblemTypePreference.RECEIVE_ANY_TEMPLATE);
			createField(comp, ProblemTypePreference.ALTSTEP_COVERAGE);
			createField(comp, ProblemTypePreference.IF_INSTEAD_ALTGUARD);
			createField(comp, ProblemTypePreference.IF_INSTEAD_RECEIVE_TEMPLATE);
			createField(comp, ProblemTypePreference.SHORTHAND);
			createField(comp, ProblemTypePreference.ISBOUND_WITHOUT_ELSE);
			createField(comp, ProblemTypePreference.CONVERT_TO_ENUM);
			createField(comp, ProblemTypePreference.SELECT_COVERAGE);
			createField(comp, ProblemTypePreference.SELECT_WITH_NUMBERS_SORTED);
			createField(comp, ProblemTypePreference.ISVALUE_WITH_VALUE);
			createField(comp, ProblemTypePreference.ITERATE_ON_WRONG_ARRAY);
			createField(comp, ProblemTypePreference.READING_OUT_PAR_BEFORE_WRITTEN);
			sec.addExpansionListener(new CustomExpansionListener(comp));
			sec.setClient(comp);
			sec.setExpanded(false);
		}
		{
			ExpandableComposite sec = new ExpandableComposite(tempParent,
					ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
			sec.setText("Unnecessary code");
			sec.setFont(fonts.getBold(""));
			Composite comp = new Composite(sec, 0);
			comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			comp.setLayout(new FillLayout(SWT.VERTICAL));
			createField(comp, ProblemTypePreference.UNUSED_GLOBAL_DEFINITION);
			createField(comp, ProblemTypePreference.UNUSED_LOCAL_DEFINITION);
			createField(comp, ProblemTypePreference.UNNECESSARY_CONTROLS);
			createField(comp, ProblemTypePreference.UNNECESSARY_VALUEOF);
			sec.addExpansionListener(new CustomExpansionListener(comp));
			sec.setClient(comp);
			sec.setExpanded(false);
		}
	}

	private void createIntegerEditor(final Composite comp, final String name, final String labelText) {
		IntegerFieldEditor temp = new IntegerFieldEditor(name, labelText, comp);
		temp.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(temp);
	}

	private void createField(final Composite comp, final ProblemTypePreference type) {
		ComboFieldEditor comboedit = new ComboFieldEditor(type.getPreferenceName(), type.getDescription(), IGNORE_WARNING_ERROR, comp);
		comboedit.getLabelControl(comp).setToolTipText(TOOLTIP_MAPPING.get(type));
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);
		editors.add(comboedit);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		changed = true;
		super.propertyChange(event);
	}

	@Override
	public void performApply() {
		super.performOk();
	}

	@Override
	public boolean performOk() {
		boolean result = super.performOk();

		// it would be better to use IPreferenceStore.needsSaving(), but
		// regardless the user
		// needsSaving will set true, because storing a string value in
		// a
		// ScopedPreferenceStore
		// does not check whether the value has changed, and always sets
		// dirty
		// true.
		// TODO: currently only the combo field editors are checked for
		// change,
		// the others should be checked too.
		if (changed) {
			
			ErrorReporter.parallelWarningDisplayInMessageDialog(
				"Code smell markers",
				"Settings of the code smell analyzer have changed," +
				   " the known projects have to be re-analyzed completly.\nThis might take some time.");

			IProject[] projs = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			final Analyzer analyzer = AnalyzerCache.withPreference();
			for (final IProject project : projs) {
				if (TITANNature.hasTITANNature(project)) {
					WorkspaceJob op = new WorkspaceJob("Code smells") {
						@Override
						public IStatus runInWorkspace(final IProgressMonitor monitor) {
							MarkerHandler mh;
							synchronized (project) {
								mh = analyzer.analyzeProject(monitor, project);
							}
							mh.showAll();
							return Status.OK_STATUS;
						}
					};
					op.setPriority(Job.SHORT);
					op.setSystem(false);
					op.setUser(true);
					op.schedule();
				}
			}
		}
		return result;
	}
}

class CustomExpansionListener extends ExpansionAdapter {
	// The scrolled composite that is the (indirect) parent of our inner
	// composite
	private ScrolledComposite sc;
	// Out inner composite, that is expanded/closed by the expandable
	// composite
	private Composite inner;
	// (current) height of the inner composite
	private int height;

	public CustomExpansionListener(final Composite inner) {
		super();
		this.inner = inner;
		Composite parent = inner;
		while (parent != null && !(parent instanceof ScrolledComposite)) {
			parent = parent.getParent();
		}
		if (parent == null) {
			throw new AssertionError("This ExpandableComposite should reside in a ScrolledComposite");
		}

		sc = (ScrolledComposite) parent;
	}

	@Override
	public void expansionStateChanging(final ExpansionEvent e) {
		// when closing, store the height BEFORE the collapsion happens
		if (!e.getState()) {
			height = inner.getSize().y;
		}
	}

	@Override
	public void expansionStateChanged(final ExpansionEvent e) {
		// lay out every composite above 'inner'
		Composite tmp = inner;
		while (tmp.getParent() != sc) {
			tmp.layout();
			tmp = tmp.getParent();
		}
		// when expanding, store the height AFTER the composite opens
		if (e.getState()) {
			height = inner.getSize().y;
		}
		// Depending on expansion/collapsion, we shall add/subtract the
		// height
		int dir = e.getState() ? 1 : -1;
		sc.setMinHeight(sc.getMinHeight() + dir * height);
	}
}
