/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * Abstract class to represent ASN.1 assignments.
 * 
 * @author Kristof Szabados
 */
public abstract class ASN1Assignment extends Assignment {
	private static final String NOTPARAMETERIZEDASSIGNMENT = "`{0}'' is not a parameterized assignment";
	public static final String UNREACHABLE = "The identifier `{0}'' is not reachable from TTCN-3";

	private static boolean markOccurrences;

	protected final Ass_pard ass_pard;

	static {
		final IPreferencesService prefService = Platform.getPreferencesService();
		markOccurrences = prefService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.MARK_OCCURRENCES_ASN1_ASSIGNMENTS,
				false, null);

		final Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					final String property = event.getProperty();
					if (PreferenceConstants.MARK_OCCURRENCES_ASN1_ASSIGNMENTS.equals(property)) {
						markOccurrences = prefService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.MARK_OCCURRENCES_ASN1_ASSIGNMENTS, false, null);
					}
				}
			});
		}
	}

	protected ASN1Assignment(final Identifier id, final Ass_pard ass_pard) {
		super(id);
		this.ass_pard = ass_pard;
	}

	/** @return the parameterizes assignment related to the assignment */
	public final Ass_pard getAssPard() {
		return ass_pard;
	}

	/**
	 * Internal new instance creating function, will only be called for
	 * parameterized assignments.
	 * 
	 * @param identifier
	 *                the name the new assignment instance shall have.
	 * @return a copy of the assignment.
	 * */
	protected abstract ASN1Assignment internalNewInstance(final Identifier identifier);

	/**
	 * Sets the scope of the right side of the assignment.
	 * 
	 * @param rightScope
	 *                the scope to be set for the right side.
	 * */
	public abstract void setRightScope(Scope rightScope);

	/**
	 * Creates a new instance of a parameterized assignment and returns i.
	 * In case of assignments which are not parameterized should return
	 * null.
	 * 
	 * @param module
	 *                the module in which the new assignment should be
	 *                created.
	 * 
	 * @return the assignment created.
	 * */
	public final ASN1Assignment newInstance(final Module module) {
		if (null == ass_pard) {
			if (null != location) {
				location.reportSemanticError(MessageFormat.format(NOTPARAMETERIZEDASSIGNMENT, getFullName()));
			}

			return null;
		}

		String newName = getIdentifier().getAsnName() + "." + module.getIdentifier().getAsnName() + ".inst";
		newName += ass_pard.newInstanceNumber(module);
		return internalNewInstance(new Identifier(Identifier_type.ID_ASN, newName));
	}

	/**
	 * Checks whether the assignment has a valid TTCN-3 identifier, i.e. is
	 * reachable from TTCN.
	 * */
	public final void checkTTCNIdentifier() {
		if (null == myScope || null == getIdentifier()) {
			return;
		}

		final Module myModule = myScope.getModuleScope();

		if (!getIdentifier().getHasValid(Identifier_type.ID_TTCN) && myScope.getParentScope().equals(myModule)
				&& null != myModule.getIdentifier() && '<' != myModule.getIdentifier().getDisplayName().charAt(0)) {
			location.reportSemanticWarning(MessageFormat.format(UNREACHABLE, getIdentifier().getDisplayName()));
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != ass_pard) {
			ass_pard.check(timestamp);
			lastTimeChecked = timestamp;
		}
	}

	/**
	 * Checks whether the actual assignment is of a specified type.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param assignment_type
	 *                the type to check against.
	 * @param referenceChain
	 *                the reference chain to detect circular references
	 * 
	 * @return true if the assignment is of the specified type, false
	 *         otherwise
	 * */
	public boolean isAssignmentType(final CompilationTimeStamp timestamp, final Assignment_type assignment_type,
			final IReferenceChain referenceChain) {
		return getAssignmentType().equals(assignment_type);
	}

	// TODO: remove when location is fixed
	public Location getLikelyLocation() {
		return getLocation();
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (ass_pard == null) {
			return;
		}

		ass_pard.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	public boolean shouldMarkOccurrences() {
		return markOccurrences;
	}
}
