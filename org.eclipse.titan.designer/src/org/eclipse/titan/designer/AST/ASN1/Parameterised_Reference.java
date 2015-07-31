/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NameReStarter;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Parameterized reference. This is not the parameterised reference, that
 * represents a function call !
 * <p>
 * Should be implementing Defined Reference, but there seems to be no reason for
 * that.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class Parameterised_Reference extends Defined_Reference {
	protected static final String FULLNAMEPART1 = ".<block>";
	protected static final String FULLNAMEPART2 = "{}";

	private static final String ASSIGNMENTEXPECTED = "ASN.1 assignment expected";
	private static final String PARAMETERISEDASSIGNMENTEXPECTED = "Parameterized assignment expected";
	protected static final String DIFFERENTPARAMETERNUMBERS = "Too {0} parameters: `{1}'' was expected instead of `{2}''";

	protected final Defined_Reference assignmentReference;
	protected boolean isErroneous;

	protected ASN1Assignments assignments;
	private Defined_Reference finalReference;

	private CompilationTimeStamp lastChekTimeStamp;

	private NameReStarter newAssignmentNameStart;

	protected Location location;

	public Parameterised_Reference(final Defined_Reference reference) {
		super(null);
		assignmentReference = reference;
		isErroneous = false;

		assignmentReference.setFullNameParent(this);
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		assignmentReference.setMyScope(scope);
	}

	@Override
	public Identifier getId() {
		final Defined_Reference ref = getRefDefdSimple();
		if (null != ref) {
			return ref.getId();
		}
		return null;
	}

	@Override
	public Identifier getModuleIdentifier() {
		final Defined_Reference ref = getRefDefdSimple();
		if (null != ref) {
			return ref.getModuleIdentifier();
		}
		return null;
	}

	@Override
	public String getDisplayName() {
		getRefDefdSimple();
		if (null != finalReference) {
			return finalReference.getDisplayName();
		}

		return assignmentReference.getDisplayName() + "{}";
	}

	/**
	 * Resolve the formal parameters of the referenced assignment with the
	 * help of the actual parameters. Instantiate a new assignment from it
	 * and return a reference to this assignment.
	 *
	 * @return the reference to the newly instantiated assignment.
	 * */
	public Defined_Reference getRefDefdSimple() {
		final Module module = myScope.getModuleScope();

		// This is a little trick, but otherwise we would not have the
		// true compilation timestamp
		final CompilationTimeStamp compilationTimeStamp = module.getLastImportationCheckTimeStamp();
		if (null != lastChekTimeStamp && !lastChekTimeStamp.isLess(compilationTimeStamp)) {
			if (isErroneous) {
				return null;
			}

			return finalReference;
		}

		lastChekTimeStamp = compilationTimeStamp;

		final Assignment parass = assignmentReference.getRefdAssignment(compilationTimeStamp, true);
		if (null == parass) {
			isErroneous = true;
			return null;
		} else if (!(parass instanceof ASN1Assignment)) {
			assignmentReference.getLocation().reportSemanticError(ASSIGNMENTEXPECTED);
			isErroneous = true;
			return null;
		}

		final Ass_pard assPard = ((ASN1Assignment) parass).getAssPard();
		if (null == assPard) {
			assignmentReference.getLocation().reportSemanticError(PARAMETERISEDASSIGNMENTEXPECTED);
			isErroneous = true;
			return assignmentReference;
		}

		addAssignments( assPard, compilationTimeStamp );
		
		// Add the assignments made from the formal and actual
		// parameters to the actual module
		assignments.setRightScope(myScope);
		assignments.setParentScope(parass.getMyScope());
		assignments.setFullNameParent(this);
		assignments.check(compilationTimeStamp);

		// create a copy of the assignment and add it to the actual
		// module
		final ASN1Assignment newAssignment = ((ASN1Assignment) parass).newInstance(module);

		newAssignmentNameStart = new NameReStarter(new StringBuilder(module.getFullName()).append(INamedNode.DOT)
				.append(newAssignment.getIdentifier().getDisplayName()).toString());
		newAssignmentNameStart.setFullNameParent(parass);
		newAssignment.setFullNameParent(newAssignmentNameStart);
		newAssignment.setLocation(location);
		newAssignment.getIdentifier().setLocation(assignmentReference.getLocation());

		((ASN1Assignments) module.getAssignments()).addDynamicAssignment(compilationTimeStamp, newAssignment);
		newAssignment.setMyScope(assignments);
		newAssignment.check(compilationTimeStamp);

		final List<ISubReference> subreferences = new ArrayList<ISubReference>(1);
		subreferences.add(new FieldSubReference(newAssignment.getIdentifier()));

		finalReference = new Defined_Reference(module.getIdentifier(), subreferences);
		finalReference.setFullNameParent(this);
		finalReference.setMyScope(module);
		return finalReference;
	}
	
	/**
	 * Fill the assignments according to the formal parameters
	 * @param aAssPard (in) formal parameters for the conversion
	 * @param aCompilationTimeStamp compilation timestamp
	 */
	protected abstract void addAssignments( Ass_pard aAssPard, CompilationTimeStamp aCompilationTimeStamp );
}
