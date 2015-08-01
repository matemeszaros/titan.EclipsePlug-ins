/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a length restriction for a range.
 * 
 * @author Kristof Szabados
 * */
public final class RangeLenghtRestriction extends LengthRestriction {
	private static final String FULLNAMEPART1 = ".<lower>";
	private static final String FULLNAMEPART2 = ".<upper>";

	private final Value lower;
	private final Value upper;

	/** The time when this restriction was check the last time. */
	private CompilationTimeStamp lastTimeChecked;

	public RangeLenghtRestriction(final Value lower, final Value upper) {
		super();
		this.lower = lower;
		this.upper = upper;

		if (lower != null) {
			lower.setFullNameParent(this);
		}
		if (upper != null) {
			upper.setFullNameParent(this);
		}
	}

	@Override
	public String createStringRepresentation() {
		if (lower == null) {
			return "<erroneous length restriction>";
		}

		StringBuilder builder = new StringBuilder("length(");
		builder.append(lower.createStringRepresentation());
		builder.append(" .. ");
		if (upper != null) {
			builder.append(upper.createStringRepresentation());
		} else {
			builder.append("infinity");
		}
		builder.append(')');

		return builder.toString();
	}

	public IValue getLowerValue(final CompilationTimeStamp timestamp) {
		if (lower == null) {
			return null;
		}

		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = lower.getValueRefdLast(timestamp, chain);
		chain.release();

		return last;
	}

	public IValue getUpperValue(final CompilationTimeStamp timestamp) {
		if (upper == null) {
			return null;
		}

		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = upper.getValueRefdLast(timestamp, chain);
		chain.release();

		return last;
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
	public void check(final CompilationTimeStamp timestamp, final Expected_Value_type expected_value) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}
		lastTimeChecked = timestamp;

		Integer_Type integer = new Integer_Type();
		lower.setMyGovernor(integer);
		IValue last = integer.checkThisValueRef(timestamp, lower);
		integer.checkThisValueLimit(timestamp, last, expected_value, false, false, true, false);

		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue valueLower = last.getValueRefdLast(timestamp, chain);
		chain.release();

		if (last.getIsErroneous(timestamp)) {
			return;
		}

		BigInteger lowerInt;
		switch (valueLower.getValuetype()) {
		case INTEGER_VALUE: {
			lowerInt = ((Integer_Value) valueLower).getValueValue();
			if (lowerInt.compareTo(BigInteger.ZERO) == -1) {
				final String message = MessageFormat.format(
						"The lower boundary of the length restriction must be a non-negative integer value instead of {0}",
						lowerInt);
				valueLower.getLocation().reportSemanticError(message);
			}
			break;
		}
		default:
			lowerInt = BigInteger.ZERO;
			break;
		}

		if (upper == null) {
			return;
		}

		upper.setMyGovernor(integer);
		last = integer.checkThisValueRef(timestamp, upper);
		integer.checkThisValueLimit(timestamp, last, expected_value, false, false, true, false);

		chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue valueUpper = last.getValueRefdLast(timestamp, chain);
		chain.release();

		if (last.getIsErroneous(timestamp)) {
			return;
		}

		BigInteger upperInt;
		switch (valueUpper.getValuetype()) {
		case INTEGER_VALUE: {
			upperInt = ((Integer_Value) valueUpper).getValueValue();
			if (upperInt.compareTo(BigInteger.ZERO) == -1) {
				final String message = MessageFormat.format(
						"The upper boundary of the length restriction must be a non-negative integer value instead of {0}",
						upperInt);
				valueUpper.getLocation().reportSemanticError(message);
			} else if (upperInt.compareTo(lowerInt) == -1) {
				getLocation().reportSemanticError(
						MessageFormat.format(
								"The upper boundary of the length restriction ({0}) cannot be smaller than the lower boundary {1}",
								upperInt, lowerInt));
			}
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void checkArraySize(final CompilationTimeStamp timestamp, final ArrayDimension dimension) {
		if (lastTimeChecked == null || dimension.getIsErroneous(timestamp)) {
			return;
		}

		boolean errorFlag = false;
		long arraySize = dimension.getSize();

		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue lowerLast = lower.getValueRefdLast(timestamp, chain);
		chain.release();

		if (Value_type.INTEGER_VALUE.equals(lowerLast.getValuetype()) && !lowerLast.getIsErroneous(timestamp)) {
			BigInteger length = ((Integer_Value) lowerLast).getValueValue();
			if (length.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
				final String message = MessageFormat
						.format("An integer value less then `{0}'' was expected as the lower boundary of the length restriction instead of `{1}''",
								Integer.MAX_VALUE, length);
				lower.getLocation().reportSemanticError(message);
				errorFlag = true;
			} else if (length.compareTo(BigInteger.valueOf(arraySize)) == 1) {
				final String message = MessageFormat
						.format("There number of elements allowed by the length restriction (at least {0}) contradicts the array size ({1})",
								length, arraySize);
				lower.getLocation().reportSemanticError(message);
				errorFlag = true;
			}
		}

		if (upper != null) {
			chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IValue upperLast = upper.getValueRefdLast(timestamp, chain);
			chain.release();

			if (Value_type.INTEGER_VALUE.equals(upperLast.getValuetype()) && !upperLast.getIsErroneous(timestamp)) {
				BigInteger length = ((Integer_Value) upperLast).getValueValue();
				if (length.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
					final String message = MessageFormat.format(
							"An integer value less then `{0}'' was expected as the upper boundary of the length restriction instead of `{1}''",
							Integer.MAX_VALUE, length);
					upper.getLocation()
							.reportSemanticError(message
									);
					errorFlag = true;
				} else if (length.compareTo(BigInteger.valueOf(arraySize)) == 1) {
					final String message = MessageFormat
							.format("There number of elements allowed by the length restriction (at most {0}) contradicts the array size ({1})",
									length, arraySize);
					upper.getLocation().reportSemanticError(message);
					errorFlag = true;
				}
			}
		}

		if (!errorFlag) {
			getLocation().reportSemanticWarning("Length restriction is useless for an array template");
		}
	}

	@Override
	public void checkNofElements(final CompilationTimeStamp timestamp, final int nofElements, final boolean lessAllowed,
			final boolean more_allowed, final boolean has_anyornone, final ILocateableNode locatable) {
		if (lower == null) {
			return;
		}

		if (locatable instanceof CharString_Pattern_Template ||
			locatable instanceof UnivCharString_Pattern_Template) {
			return; // CharString Pattern Template will not be checked to No of elements 
		}

		if (!lessAllowed) {
			IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IValue last = lower.getValueRefdLast(timestamp, chain);
			chain.release();

			if (Value_type.INTEGER_VALUE.equals(last.getValuetype()) && !last.getIsErroneous(timestamp)) {
				BigInteger length = ((Integer_Value) last).getValueValue();
				if (length.compareTo(BigInteger.valueOf(nofElements)) == 1) {
					final String message = MessageFormat.format(
							"There are fewer ({0}) elements than it is allowed by the length restriction (at least {1})",
							nofElements, length);
					locatable.getLocation().reportSemanticError(message);
				}
			}
		}

		if (upper == null) {
			return;
		}

		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = upper.getValueRefdLast(timestamp, chain);
		chain.release();

		if (Value_type.INTEGER_VALUE.equals(last.getValuetype()) && !last.getIsErroneous(timestamp)) {
			BigInteger length = ((Integer_Value) last).getValueValue();
			if (length.compareTo(BigInteger.valueOf(nofElements)) == -1 && !more_allowed) {
				final String message = MessageFormat.format(
						"There are more ({0} {1}) elements than it is allowed by the length restriction ({2})",
						has_anyornone ? "at least" : "", nofElements, length);
				locatable.getLocation().reportSemanticError(message);
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
		if (lower != null && !lower.accept(v)) {
			return false;
		}
		if (upper != null && !upper.accept(v)) {
			return false;
		}
		return true;
	}
}
