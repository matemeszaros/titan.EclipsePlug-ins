/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * @author Kristof Szabados
 * */
public final class ErrorsWarningsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Preferences of the on-the-fly analyzer";

	private static final String REPORTUNSUPPORTEDCONSTRUCTS = "Language constructs not supported yet:";
	private static final String REPORTUNSUPPORTEDCONSTRUCTS_TOOLTIP = "For example pattern subtyping in TTCN-3.";
	private static final String REPORTMISSINGIMPORTATION = "Missing imported module:";
	private static final String REPORTMISSINGIMPORTATION_TOOLTIP = "When a module refered to in an import statement could not be found.";
	private static final String REPORTUNUSEDMODULEIMPORTATION = "Report unused module importation:";
	private static final String REPORTUNUSEDMODULEIMPORTATION_TOOLTIP = "When nothing is used in the module from the imported module.";
	private static final String REPORTMISSINGFRIEND = "Report friend declarations with missing modules:";
	private static final String REPORTMISSINGFRIEND_TOOLTIP = "When the module refered to in a friend declaration doulc not be found..";
	private static final String REPORTUNUSEDGLOBALDEFINITION = "Report unused module level definition:";
	private static final String REPORTUNUSEDGLOBALDEFINITIONTOOLTIP = "When a module level definition is never read/written.\n"
			+ " Also when a type is not used to declare other definitions.";
	private static final String REPORTUNUSEDLOCALDEFINITION = "Report unused local definition:";
	private static final String REPORTUNUSEDLOCALDEFINITIONTOOLTIP = "When a local variable or formal parameter is never read/written.";
	private static final String REPORTUNUSEDFUNTIONRETURNVALUES = "Report unused function return values:";
	private static final String REPORTUNUSEDFUNCTIONRETURNVALUESTOOLTIP = "When a function returns a value or a template, but it is not used.";
	private static final String REPORT_INFINITE_LOOP = "Report infinite loops";
	private static final String REPORT_INFINITE_LOOP_TOOLTIP = "When there is no way to escape the loop";
	private static final String REPORT_READONLY = "Report read only variables";
	private static final String REPORT_READONLY_TOOLTIP = "When a definition was declared to be changeable, but is never modified";
	private static final String REPORT_NONPRIVATE_PRIVATE = "Report TTCN-3 definitions that could be private, but are not set so";
	private static final String REPORT_NONPRIVATE_PRIVATE_TOOLTIP = "By default all definitions are public,"
			+ " but by declaring some private one can make them invisible for importing modules.\n"
			+ "This might be usefull in case of internal functions, types, constants";
	private static final String REPORT_TOOMANY_PARAMETERS = "Report TTCN-3 definitions that have too many parameters";
	private static final String REPORT_TOOMANY_PARAMETERS_SIZE = "The size the amount of parameters should not exceed";
	private static final String REPORT_TOOMANY_PARAMETERS_TOOLTIP = "The more parameters some entity has,"
			+ " the harder it becomes to fill them out correctly without introducing faults";
	private static final String REPORT_EMPTY_STATEMENT_BLOCK = "Report empty statement blocks";
	private static final String REPORT_EMPTY_STATEMENT_BLOCK_TOOLTIP = "Empty statement blocks in the source code usually means,\n"
			+ "that the developer planned to write some code there to handle some use cases,\n"
			+ "but forgot to finish his work ... or is right now in the process of finishing it";
	private static final String REPORT_TOOMANY_STATEMENTS = "Report statement blocks that have too many statements";
	private static final String REPORT_TOOMANY_STATEMENTS_SIZE = "The size the amount of statements should not exceed";
	private static final String REPORT_TOOMANY_STATEMENTS_TOOLTIP = "Rationale:"
			+ " If a statement block becomes very long it is hard to understand."
			+ " Therefore long statemen tblocks should usually be refactored into several individual ones,"
			+ " or into individual functions that focus on a specific task. ";
	private static final String HANDLEDEFAULTASOPTIONAL = "DEFAULT elements of ASN.1 sequence and set types as OPTIONAL:";
	private static final String HANDLEDEFAULTASOPTIONALTOOTIP = "Handle the DEFAULT elements of set and sequence ASN.1 types as being optional.\n"
			+ "This is compatibility opition.";
	private static final String REPORTINCORRECTSHIFTROTATESIZE = "Report too big or too small shift and rotation sizes:";
	private static final String REPORTINCORRECTSHIFTROTATESIZE_TOOLTIP = "Report too big (bigger than the string itself) or negative"
			+ " shift and rotation sizes.";
	private static final String REPORT_IF_WITHOUT_ELSE = "Report conditional statements without else block";
	private static final String REPORT_IF_WITHOUT_ELSE_TOOLTIP = "All possible execution paths should be handled,"
			+ " atleast on the level of logging information in case of unexpected events";
	private static final String REPORT_SETVERDICT_WITHOUT_REASON = "Report setverdict without reason";
	private static final String REPORT_SETVERDICT_WITHOUT_REASON_TOOLTIP = "Setting any other verdict reason then pass,"
			+ " should be accompanied with a reason, with details for the verdict";
	private static final String REPORT_MODULENAME_IN_DEFINITION = "Report if the name of the module is mentioned in the name of the definition";
	private static final String REPORT_MODULENAME_IN_DEFINITION_TOOLTIP = "Definitions can be referenced in the modulename.identifier format,"
			+ " so it is of no value if the name of the module is duplicated in the name of the definition\n" + "But makes it longer";
	private static final String REPORT_VISIBILITY_IN_DEFINITION = "Report visibility settings mentioned in the name of definitions";
	private static final String REPORT_VISIBILITY_IN_DEFINITION_TOOLTIP = "Visibility attributes should not be mentioned"
			+ " in the names of the definitions\n" + "They should be explicitly set as visibility attributes of the definition";

	private static final String REPORTUNNECESSARYCONTROLS = "Report unnecessary controls:";
	private static final String REPORTUNNECESSARYCONTROLS_TOOLTIP = "Report controls that can be identified to be unnecessary"
			+ " in compilation time.\n"
			+ "For example when the conditional expression of an if statements evaluates to false in compilation time.";
	private static final String REPORT_IGNORED_PREPROCESSOR_DIRECTIVES = "Report ignored preprocessor directives:";
	private static final String REPORT_IGNORED_PREPROCESSOR_DIRECTIVES_TOOLTIP = "Some preprocessor directives (#line,#pragma,etc.) are ignored.\n"
			+ "These should either be removed or the file is an intermediate file (already preprocessed ttcnpp)"
			+ " that contains line markers.\n"
			+ "It is probably a bad idea to edit the intermediate file instead of the original ttcnpp file.";
	private static final String REPORTTYPECOMPATIBILITY = "Report uses of structured-type compatibility:";
	private static final String REPORTTYPECOMPATIBILITY_TOOLTIP = "When structured-type compatibility is used in the code.";
	private static final String REPORTERRORSINEXTENSIONSYNTAX = "Report incorrect syntax in extension attributes:";
	private static final String REPORTERRORSINEXTENSIONSYNTAX_TOOLTIP = "According to the standard"
			+ " syntax errors in the extension attribute should not be reported,"
			+ " but should be assumed as correct for some other tool .";
	private static final String REPORT_STRICT_CONSTANTS = "Use stricter checks for constants";
	private static final String REPORT_STRICT_CONSTANTS_TOOLTIP = "Although it is valid to leave fields of constants and literals unbound,"
			+ " in some cases this was not the intention.";
	private static final String REPORT_GOTO = "Report the usage of label and goto statements";
	private static final String REPORT_GOTO_TOOLTIP = "In almost all cases the usage of goto should be forbidden"
			+ " as it can very easily breaks the principles of structured/well designed source code.";

	private static final String[][] IGNORE_WARNING_ERROR = new String[][] { { "Ignore", GeneralConstants.IGNORE },
			{ "Warning", GeneralConstants.WARNING }, { "Error", GeneralConstants.ERROR } };

	private boolean changed = false;
	private Composite pagecomp;

	public ErrorsWarningsPreferencePage() {
		super(GRID);
	}

	/**
	 * Creates an expandable composite on the user interface.
	 * 
	 * @param parent
	 *                the parent composite where this one can be added to.
	 * @param title
	 *                the title of the new composite.
	 * 
	 * @return the created composite.
	 * */
	private ExpandableComposite createExtendableComposite(final Composite parent, final String title) {
		final ExpandableComposite ex = new ExpandableComposite(parent, SWT.NONE, ExpandableComposite.TWISTIE
				| ExpandableComposite.CLIENT_INDENT | ExpandableComposite.COMPACT);
		ex.setText(title);
		ex.setExpanded(false);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		ex.setLayoutData(data);
		ex.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				Composite temp = parent;
				while (temp != null && !(temp instanceof ScrolledComposite)) {
					temp = temp.getParent();
				}

				if (temp != null) {
					Point point = pagecomp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					((ScrolledComposite) temp).setMinSize(point);
					((ScrolledComposite) temp).layout(true, true);
				}
			}
		});

		return ex;
	}

	@Override
	protected Control createContents(final Composite parent) {
		pagecomp = new Composite(parent, SWT.NONE);
		pagecomp.setLayout(new GridLayout(1, false));
		pagecomp.setLayoutData(new GridData(GridData.FILL_BOTH));
		return super.createContents(pagecomp);
	}

	@Override
	protected void createFieldEditors() {
		Composite tempParent = getFieldEditorParent();

		createCodeStyleSection(tempParent);
		createUnnecessaryCodeSection(tempParent);
		createPotentialProgrammingProblemsSection(tempParent);
	}

	/**
	 * Creates the section of potential issues related to code style being
	 * used. These are not really problems, but rather self constraints the
	 * programmer can practice on himself to enforce the writing of better
	 * code.
	 * 
	 * @param parent
	 *                the parent composite to put the section under.
	 * */
	private void createCodeStyleSection(final Composite parent) {
		ExpandableComposite expandable = createExtendableComposite(parent, "Code style problems");
		Composite comp = new Composite(expandable, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expandable.setClient(comp);
		expandable.setExpanded(true);

		ComboFieldEditor comboedit = new ComboFieldEditor(PreferenceConstants.REPORTUNSUPPORTEDCONSTRUCTS, REPORTUNSUPPORTEDCONSTRUCTS,
				IGNORE_WARNING_ERROR, comp);
		Label text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTUNSUPPORTEDCONSTRUCTS_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		BooleanFieldEditor defaultAsOptional = new BooleanFieldEditor(PreferenceConstants.DEFAULTASOPTIONAL, HANDLEDEFAULTASOPTIONAL,
				BooleanFieldEditor.SEPARATE_LABEL, comp);
		defaultAsOptional.getLabelControl(comp).setToolTipText(HANDLEDEFAULTASOPTIONALTOOTIP);
		defaultAsOptional.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(defaultAsOptional);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORTTYPECOMPATIBILITY, REPORTTYPECOMPATIBILITY, IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTTYPECOMPATIBILITY_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		defaultAsOptional = new BooleanFieldEditor(PreferenceConstants.REPORT_STRICT_CONSTANTS, REPORT_STRICT_CONSTANTS,
				BooleanFieldEditor.SEPARATE_LABEL, comp);
		defaultAsOptional.getLabelControl(comp).setToolTipText(REPORT_STRICT_CONSTANTS_TOOLTIP);
		defaultAsOptional.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(defaultAsOptional);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORT_GOTO, REPORT_GOTO, IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_GOTO_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);
	}

	/**
	 * Creates the section of potential issues related to potential
	 * programming problems. All of these should be considered as normal
	 * errors, but on our current level we might not be able to detect them
	 * in all cases correctly.
	 * 
	 * @param parent
	 *                the parent composite to put the section under.
	 * */
	private void createPotentialProgrammingProblemsSection(final Composite parent) {
		ExpandableComposite expandable = createExtendableComposite(parent, "Potential programming problems");
		Composite comp = new Composite(expandable, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expandable.setClient(comp);
		expandable.setExpanded(false);

		ComboFieldEditor comboedit = new ComboFieldEditor(PreferenceConstants.REPORTMISSINGIMPORTEDMODULE, REPORTMISSINGIMPORTATION,
				IGNORE_WARNING_ERROR, comp);
		Label text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTMISSINGIMPORTATION_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORTMISSINGFRIENDMODULE, REPORTMISSINGFRIEND, IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTMISSINGFRIEND_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORTUNUSEDFUNCTIONRETURNVALUES, REPORTUNUSEDFUNTIONRETURNVALUES,
				IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTUNUSEDFUNCTIONRETURNVALUESTOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORTINFINITELOOPS, REPORT_INFINITE_LOOP, IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_INFINITE_LOOP_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORTREADONLY, REPORT_READONLY, IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_READONLY_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORT_NONPRIVATE_PRIVATE, REPORT_NONPRIVATE_PRIVATE, IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_NONPRIVATE_PRIVATE_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORT_TOOMANY_PARAMETERS, REPORT_TOOMANY_PARAMETERS, IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_TOOMANY_PARAMETERS_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);
		IntegerFieldEditor integeredit = new IntegerFieldEditor(PreferenceConstants.REPORT_TOOMANY_PARAMETERS_SIZE,
				REPORT_TOOMANY_PARAMETERS_SIZE, comp);
		text = integeredit.getLabelControl(comp);
		text.setToolTipText(REPORT_TOOMANY_PARAMETERS_TOOLTIP);
		integeredit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(integeredit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORT_EMPTY_STATEMENT_BLOCK, REPORT_EMPTY_STATEMENT_BLOCK,
				IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_EMPTY_STATEMENT_BLOCK_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});

		addField(comboedit);
		comboedit = new ComboFieldEditor(PreferenceConstants.REPORT_TOOMANY_STATEMENTS, REPORT_TOOMANY_STATEMENTS, IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_TOOMANY_STATEMENTS_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);
		integeredit = new IntegerFieldEditor(PreferenceConstants.REPORT_TOOMANY_STATEMENTS_SIZE, REPORT_TOOMANY_STATEMENTS_SIZE, comp);
		text = integeredit.getLabelControl(comp);
		text.setToolTipText(REPORT_TOOMANY_STATEMENTS_TOOLTIP);
		integeredit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(integeredit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORTINCORRECTSHIFTROTATESIZE, REPORTINCORRECTSHIFTROTATESIZE,
				IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTINCORRECTSHIFTROTATESIZE_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORT_IF_WITHOUT_ELSE, REPORT_IF_WITHOUT_ELSE, IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_IF_WITHOUT_ELSE_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORT_SETVERDICT_WITHOUT_REASON, REPORT_SETVERDICT_WITHOUT_REASON,
				IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_SETVERDICT_WITHOUT_REASON_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORT_MODULENAME_IN_DEFINITION, REPORT_MODULENAME_IN_DEFINITION,
				IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_MODULENAME_IN_DEFINITION_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORT_VISIBILITY_IN_DEFINITION, REPORT_VISIBILITY_IN_DEFINITION,
				IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_VISIBILITY_IN_DEFINITION_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORTERRORSINEXTENSIONSYNTAX, REPORTERRORSINEXTENSIONSYNTAX,
				IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTERRORSINEXTENSIONSYNTAX_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);
	}

	/**
	 * Creates the section of potential issues related to unnecessary code
	 * in the projects.
	 * 
	 * @param parent
	 *                the parent composite to put the section under.
	 * */
	private void createUnnecessaryCodeSection(final Composite parent) {
		ExpandableComposite expandable = createExtendableComposite(parent, "Unnecessary code");
		Composite comp = new Composite(expandable, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expandable.setClient(comp);
		expandable.setExpanded(false);

		ComboFieldEditor comboedit = new ComboFieldEditor(PreferenceConstants.REPORTUNUSEDMODULEIMPORTATION, REPORTUNUSEDMODULEIMPORTATION,
				IGNORE_WARNING_ERROR, comp);
		Label text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTUNUSEDMODULEIMPORTATION_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORTUNUSEDGLOBALDEFINITION, REPORTUNUSEDGLOBALDEFINITION,
				IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTUNUSEDGLOBALDEFINITIONTOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORTUNUSEDLOCALDEFINITION, REPORTUNUSEDLOCALDEFINITION, IGNORE_WARNING_ERROR,
				comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTUNUSEDLOCALDEFINITIONTOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORTUNNECESSARYCONTROLS, REPORTUNNECESSARYCONTROLS, IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTUNNECESSARYCONTROLS_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORT_IGNORED_PREPROCESSOR_DIRECTIVES, REPORT_IGNORED_PREPROCESSOR_DIRECTIVES,
				IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_IGNORED_PREPROCESSOR_DIRECTIVES_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);
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
	public boolean performOk() {
		boolean result = super.performOk();
		if (changed && getPreferenceStore().getBoolean(PreferenceConstants.USEONTHEFLYPARSING)) {
			changed = false;

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(new Shell(Display.getDefault()), "Error/Warning settings changed",
							"Error/Warning settings have changed, the known projects have to be re-analyzed completly.\nThis might take some time.");
				}
			});

			GlobalParser.clearSemanticInformation();
			GlobalParser.reAnalyzeSemantically();
		}

		return result;
	}
}
