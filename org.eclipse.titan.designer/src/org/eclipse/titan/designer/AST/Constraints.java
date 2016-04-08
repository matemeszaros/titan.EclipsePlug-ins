/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASN1.TableConstraint;
import org.eclipse.titan.designer.AST.Constraint.Constraint_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents constraints.
 * 
 * @author Kristof Szabados
 * */
public final class Constraints extends ASTNode {
	private List<Constraint> constraints;

	/** @return a new instance of this constraint list */
	public Constraints newInstance() {
		final Constraints temp = new Constraints();

		if (null != constraints) {
			for (Constraint constraint : constraints) {
				temp.constraints.add(constraint.newInstance());
			}
		}

		return temp;
	}

	public void addConstraint(final Constraint constraint) {
		if (null == constraint) {
			return;
		}

		if (null == constraints) {
			constraints = new ArrayList<Constraint>(1);
		}

		constraints.add(constraint);
	}

	public int getNofConstraints() {
		if (null == constraints) {
			return 0;
		}

		return constraints.size();
	}

	public Constraint getConstraintByIndex(final int index) {
		if (null == constraints) {
			return null;
		}

		return constraints.get(index);
	}

	/**
	 * @return the first table constraint in the list, or null if none was found.
	 * */
	public TableConstraint getTableConstraint() {
		if (null == constraints) {
			return null;
		}

		Constraint constraint;
		for (int i = 0, size = constraints.size(); i < size; i++) {
			constraint = constraints.get(i);
			if (Constraint_type.CT_TABLE.equals(constraint.getConstraintType())) {
				return (TableConstraint) constraint;
			}
		}
		return null;
	}

	public void setMyType(final Type type) {
		if (null == constraints) {
			return;
		}

		for (int i = 0, size = constraints.size(); i < size; i++) {
			constraints.get(i).setMyType(type);
		}
	}

	/**
	 * Does the semantic checking of the constraint.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (null == constraints) {
			return;
		}

		for (int i = 0, size = constraints.size(); i < size; i++) {
			constraints.get(i).check(timestamp);
		}
	}
	
	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (constraints == null) {
			return;
		}

		for (Constraint c : constraints) {
			c.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (constraints != null) {
			for (Constraint c : constraints) {
				if (!c.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
