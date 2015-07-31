/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ProjectStructureDataCollector;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * This class represents general Modules, on which both the TTCN-3 and the ASN.1
 * modules are built.
 * 
 * @author Kristof Szabados
 * */
public abstract class Module extends Scope implements IOutlineElement, ILocateableNode, IReferencingElement {
	public enum module_type {
		/** ASN.1 module */
		ASN_MODULE,
		/** TTCN-3 module */
		TTCN3_MODULE
	}

	protected IProject project;
	protected Identifier identifier;
	protected String name;

	protected Location location;

	/**
	 * Tells whether the module can be skipped from semantic checking in
	 * this semantic check cycle or not.
	 */
	private boolean isSkippedFromSemanticChecking = false;

	protected CompilationTimeStamp lastCompilationTimeStamp;
	protected CompilationTimeStamp lastImportCheckTimeStamp;

	// The actual value of the severity level to report unused importations
	// on.
	private static String unusedModuleImportationSeverity;

	public static String getReportUnusedModuleImportationSeverity() {
		return unusedModuleImportationSeverity;
	}

	// true if unused importation related problems should not be reported.
	private static boolean silentMode = true;

	public static boolean getReportUnusedModuleImportationProblems() {
		return silentMode;
	}

	static {
		final IPreferencesService ps = Platform.getPreferencesService();
		if ( ps != null ) {
			unusedModuleImportationSeverity = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORTUNUSEDMODULEIMPORTATION, GeneralConstants.WARNING, null);
			silentMode = GeneralConstants.IGNORE.equals(unusedModuleImportationSeverity);

			final Activator activator = Activator.getDefault();
			if (activator != null) {
				activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent event) {
						final String property = event.getProperty();
						if (PreferenceConstants.REPORTUNUSEDMODULEIMPORTATION.equals(property)) {
							unusedModuleImportationSeverity = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.REPORTUNUSEDMODULEIMPORTATION, GeneralConstants.WARNING, null);
							silentMode = GeneralConstants.IGNORE.equals(unusedModuleImportationSeverity);
						}
					}
				});
			}
		}
	}

	public Module(final Identifier identifier, final IProject project) {
		this.project = project;
		this.identifier = identifier;
		name = (identifier == null) ? null : this.identifier.getName();

		scopeName = "module";
		setScopeMacroName((identifier == null) ? "unknown module" : identifier.getDisplayName());
	}

	public abstract module_type getModuletype();

	/**
	 * Returns the name of the module.
	 * 
	 * @return the name of the module
	 * */
	public String getName() {
		return name;
	}

	public final IProject getProject() {
		return project;
	}

	/**
	 * Returns the timestamp of the last importation check.
	 * <p>
	 * The import hierarchy check is a pre step of real check.
	 * 
	 * @return the timestamp of the last importation check.
	 * */
	public final CompilationTimeStamp getLastImportationCheckTimeStamp() {
		return lastImportCheckTimeStamp;
	}

	/**
	 * @return the timestamp of the last importation check.
	 * */
	public final CompilationTimeStamp getLastCompilationTimeStamp() {
		return lastCompilationTimeStamp;
	}

	/**
	 * Sets whether this module can be skipped from semantic checking.
	 * 
	 * @param state
	 *                the value telling if the module can be skipped from
	 *                semantic checking
	 * */
	public final void setSkippedFromSemanticChecking(final boolean state) {
		isSkippedFromSemanticChecking = state;
	}

	/**
	 * Checks weather this module can be skipped from semantic checking.
	 * 
	 * @return true if the semantic checking of the module can be skipped,
	 *         false otherwise.
	 * */
	public final boolean getSkippedFromSemanticChecking() {
		return isSkippedFromSemanticChecking;
	}

	@Override
	public Identifier getIdentifier() {
		return identifier;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Module getModuleScope() {
		return this;
	}

	/**
	 * @return the list of definitions declared in this module.
	 * */
	public abstract Assignments getAssignments();

	public abstract Def_Type getAnytype();

	/**
	 * Does the semantic checking of this module.
	 * <p>
	 * <ul>
	 * <li>the definitions of this module are checked one-by-one
	 * </ul>
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle
	 * */
	public abstract void check(final CompilationTimeStamp timestamp);

	/**
	 * Checks the import hierarchies of this module (and the ones imported
	 * here recursively).
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                a chain of references used to find circularly imported
	 *                modules.
	 * @param moduleStack
	 *                the stack of modules visited so far, from the starting
	 *                point.
	 * */
	public abstract void checkImports(final CompilationTimeStamp timestamp, final ModuleImportationChain referenceChain,
			final List<Module> moduleStack);

	/**
	 * Collects the imported modules into a list. The list shall always
	 * exist even if being empty, and shall have a null object when
	 * non-existing modules are imported.
	 * 
	 * @return the list of modules imported.
	 * */
	public abstract List<Module> getImportedModules();

	/**
	 * @return Whether any of the modules imported has changed to an other
	 *         one since the last importation check.
	 * */
	public abstract boolean hasUnhandledImportChanges();

	/**
	 * Checks if there is an assignment imported to the actual module with
	 * the provided identifier.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param identifier
	 *                the identifier to search for.
	 * 
	 * @return true if there is an assignment imported with the provided
	 *         name, false otherwise.
	 * */
	public abstract boolean hasImportedAssignmentWithID(final CompilationTimeStamp timestamp, final Identifier identifier);

	/**
	 * Checks whether an assignment exist in the actual module, whether it
	 * is visible or not. If it exists and is visible it is returned.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param moduleId
	 *                the identifier of the module, where the assignment
	 *                should be imported into.
	 * @param reference
	 *                the reference of the assignment to import.
	 * 
	 * @return the assignment, or null in case of errors.
	 * */
	public abstract Assignment importAssignment(final CompilationTimeStamp timestamp, final Identifier moduleId, final Reference reference);

	/**
	 * Checks whether the assignment in this module is visible in the
	 * provided module or not. Used by declaration, and completion proposal
	 * searching.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param moduleId
	 *                the identifier of the module, against which the
	 *                visibility of the assignment is checked.
	 * @param assignment
	 *                the assignment to check.
	 * 
	 * @return true if it is visible, false otherwise.
	 * */
	public abstract boolean isVisible(final CompilationTimeStamp timestamp, final Identifier moduleId, final Assignment assignment);

	/**
	 * Checks properties, that can only be checked after the semantic check
	 * was completely run.
	 */
	public abstract void postCheck();

	public String chainedDescription() {
		if (identifier != null) {
			return new StringBuilder("`").append(identifier.getDisplayName()).append('\'').toString();
		}

		return toString();
	}

	@Override
	public int category() {
		return 0;
	}

	@Override
	public String getOutlineText() {
		return "";
	}

	public abstract void extractStructuralInformation(ProjectStructureDataCollector collector);

	/**
	 * Called by accept(), objects have to call accept() of their members in
	 * this function
	 * 
	 * @param v
	 *                the visitor object
	 * @return false to abort, will be returned by accept()
	 */
	protected boolean memberAccept(ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (!memberAccept(v)) {
			return false;
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}

	@Override
	public Declaration getDeclaration() {
		return Declaration.createInstance(this);
	}
}
