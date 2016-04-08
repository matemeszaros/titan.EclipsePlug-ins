/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Class to represent a TTCN-3 ValueRange objects.
 * 
 * @author Kristof Szabados
 * */
public final class ValueRange extends ASTNode implements IIncrementallyUpdateable {
	private static final String FULLNAMEPART1 = ".<lower_boundary>";
	private static final String FULLNAMEPART2 = ".<lower_boundary>";

	private final Value min;
	private final Value max;

	public ValueRange(final Value min, final Value max) {
		super();
		this.min = min;
		this.max = max;

		if (min != null) {
			min.setFullNameParent(this);
		}
		if (max != null) {
			max.setFullNameParent(this);
		}
	}

	public Value getMin() {
		return min;
	}

	public Value getMax() {
		return max;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (min != null) {
			min.setMyScope(scope);
		}
		if (max != null) {
			max.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (min == child) {
			return builder.append(FULLNAMEPART1);
		} else if (max == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	/**
	 * Creates and returns a string representation if the range.
	 * 
	 * @return the string representation of the range.
	 * */
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		if (min == null) {
			builder.append("-infinity");
		} else {
			builder.append(min.createStringRepresentation());
		}
		builder.append(" .. ");
		if (max == null) {
			builder.append("infinity");
		} else {
			builder.append(max.createStringRepresentation());
		}
		builder.append(')');
		return builder.toString();
	}

	/**
	 * Calculates the governor of this value range.
	 * 
	 * @param timestamp
	 *                the actual semantic checking cycle
	 * @param expectedValue
	 *                the kind of the value to be expected.
	 * 
	 * @return the governor of the value range
	 * */
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (min != null) {
			IType type = min.getExpressionGovernor(timestamp, expectedValue);
			if (type != null) {
				return type;
			}
		}

		if (max != null) {
			IType type = max.getExpressionGovernor(timestamp, expectedValue);
			if (type != null) {
				return type;
			}
		}

		return null;
	}

	/**
	 * Calculates the returning type of this value range.
	 * 
	 * @param timestamp
	 *                the actual semantic checking cycle
	 * @param expectedValue
	 *                the kind of the value to be expected.
	 * 
	 * @return the returning type of the value range
	 * */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (min != null) {
			Type_type type = min.getExpressionReturntype(timestamp, expectedValue);
			if (!Type_type.TYPE_UNDEFINED.equals(type)) {
				return type;
			}
		}

		if (max != null) {
			Type_type type = max.getExpressionReturntype(timestamp, expectedValue);
			if (!Type_type.TYPE_UNDEFINED.equals(type)) {
				return type;
			}
		}

		return Type_type.TYPE_UNDEFINED;
	}

	/**
	 * Handles the incremental parsing of this value range.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (min != null) {
			min.updateSyntax(reparser, false);
			reparser.updateLocation(min.getLocation());
		}

		if (max != null) {
			max.updateSyntax(reparser, false);
			reparser.updateLocation(max.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (min != null) {
			min.findReferences(referenceFinder, foundIdentifiers);
		}
		if (max != null) {
			max.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (min != null && !min.accept(v)) {
			return false;
		}
		if (max != null && !max.accept(v)) {
			return false;
		}
		return true;
	}
}
