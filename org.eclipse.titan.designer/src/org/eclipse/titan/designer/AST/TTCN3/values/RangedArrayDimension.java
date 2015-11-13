/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class RangedArrayDimension extends ArrayDimension {
	private static final String FULLNAMEPART1 = ".<lower>";
	private static final String FULLNAMEPART2 = ".<upper>";

	private static final String OPERANDERROR1 = "An integer value was expected as lower boundary";
	private static final String OPERANDERROR2 = "An integer value was expected as upper boundary";
	private static final String OPERANDERROR3 = "The lower boundary is greater than the upper boundary";
	private static final String OPERANDERROR4 = "Using a large integer value ({0}) as the lower boundary of an array dimension is not supported";
	private static final String OPERANDERROR5 = "Using a large integer value ({0}) as the upper boundary of an array dimension is not supported";

	private Value lower;
	private Value upper;
	private long size;
	private long offset;

	public RangedArrayDimension(final Value lower, final Value upper) {
		super();
		this.lower = lower;
		this.upper = upper;
		size = 0;
		offset = 0;

		if (lower != null) {
			lower.setFullNameParent(this);
		}
		if (upper != null) {
			upper.setFullNameParent(this);
		}
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (lower != null) {
			lower.setMyScope(scope);
		}
		if (upper != null) {
			upper.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (lower == child) {
			return builder.append(FULLNAMEPART1);
		} else if (upper == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public long getOffset() {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return offset;
	}

	@Override
	public long getSize() {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return size;
	}

	@Override
	public String createStringRepresentation() {
		check(CompilationTimeStamp.getBaseTimestamp());

		StringBuilder builder = new StringBuilder();
		builder.append('[');
		if (lower == null) {
			builder.append("<erroneous>");
		} else {
			builder.append(lower.createStringRepresentation());
		}
		builder.append(" .. ");
		if (upper == null) {
			builder.append("<erroneous>");
		} else {
			builder.append(upper.createStringRepresentation());
		}
		builder.append(']');

		return builder.toString();
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		setIsErroneous(false);

		if (lower == null || upper == null) {
			return;
		}

		long lowerLimit = 0;
		long upperLimit = 0;

		IValue lowerLast = lower.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_CONSTANT, null);
		if (lowerLast == null || lowerLast.getIsErroneous(timestamp)) {
			return;
		}

		switch (lowerLast.getValuetype()) {
		case INTEGER_VALUE:
			if (lowerLast.isUnfoldable(timestamp)) {
				lower.getLocation().reportSemanticError(OPERANDERROR1);
				setIsErroneous(true);
			} else if (Value.Value_type.INTEGER_VALUE.equals(lowerLast.getValuetype())) {
				if (!((Integer_Value) lowerLast).isNative()) {
					lower.getLocation().reportSemanticError(MessageFormat.format(OPERANDERROR4, lowerLast));
					setIsErroneous(true);
				} else {
					lowerLimit = ((Integer_Value) lowerLast).getValue();
				}
			}
			break;
		default:
			lower.getLocation().reportSemanticError(OPERANDERROR1);
			lower.setIsErroneous(true);
		}

		IValue upperLast = upper.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_CONSTANT, null);
		if (upperLast == null || upperLast.getIsErroneous(timestamp)) {
			return;
		}

		switch (upperLast.getValuetype()) {
		case INTEGER_VALUE:
			if (upperLast.isUnfoldable(timestamp)) {
				upper.getLocation().reportSemanticError(OPERANDERROR2);
				setIsErroneous(true);
			} else if (Value.Value_type.INTEGER_VALUE.equals(upperLast.getValuetype())) {
				if (!((Integer_Value) upperLast).isNative()) {
					upper.getLocation().reportSemanticError(MessageFormat.format(OPERANDERROR5, upperLast));
					setIsErroneous(true);
				} else {
					upperLimit = ((Integer_Value) upperLast).getValue();
				}
			}
			break;
		default:
			upper.getLocation().reportSemanticError(OPERANDERROR2);
			upper.setIsErroneous(true);
		}

		if (!getIsErroneous(timestamp) && !lower.getIsErroneous(timestamp) && !upper.getIsErroneous(timestamp)) {
			if (upperLimit < lowerLimit) {
				getLocation().reportSemanticError(OPERANDERROR3);
				setIsErroneous(true);
			} else {
				size = upperLimit - lowerLimit + 1;
				offset = lowerLimit;
			}
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (lower != null) {
			lower.updateSyntax(reparser, false);
			reparser.updateLocation(lower.getLocation());
		}

		if (upper != null) {
			upper.updateSyntax(reparser, false);
			reparser.updateLocation(upper.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (lower != null) {
			lower.findReferences(referenceFinder, foundIdentifiers);
		}
		if (upper != null) {
			upper.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (lower != null) {
			if (!lower.accept(v)) {
				return false;
			}
		}
		if (upper != null) {
			if (!upper.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
