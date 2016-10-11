/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.StringSetOperation.OperationType;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring;

/**
 * @author Adam Delic
 * */
public final class StringSetConstraint extends StringSubtypeTreeElement {
	public enum ConstraintType {
		// RangeListConstraint ( using SizeLimit )
		SIZE_CONSTRAINT,
		// RangeListConstraint ( using CharLimit or UCharLimit ) 
		ALPHABET_CONSTRAINT,
		// StringValueConstraint or UStringValueConstraint
		VALUE_CONSTRAINT,
		// StringPatternConstraint
		PATTERN_CONSTRAINT
	}

	private final ConstraintType constraintType;
	private final SubtypeConstraint constraint;

	public StringSetConstraint(final StringType stringType, final ConstraintType constraintType, final SubtypeConstraint constraint) {
		super(stringType);
		this.constraintType = constraintType;
		this.constraint = constraint;
	}

	@Override
	public ElementType getElementType() {
		return ElementType.CONSTRAINT;
	}

	public ConstraintType getType() {
		return constraintType;
	}

	public SubtypeConstraint getConstraint() {
		return constraint;
	}

	@Override
	public StringSetConstraint complement() {
		return new StringSetConstraint(stringType, constraintType, constraint.complement());
	}

	@Override
	public SubtypeConstraint intersection(final SubtypeConstraint other) {
		StringSubtypeTreeElement o = (StringSubtypeTreeElement) other;
		if (o instanceof StringSetConstraint) {
			StringSetConstraint ssc = (StringSetConstraint) o;
			if (ssc.constraintType == constraintType) {
				return new StringSetConstraint(stringType, constraintType,
						constraint.intersection(((StringSetConstraint) o).constraint));
			}
		}
		StringSetOperation returnValue = new StringSetOperation(stringType, OperationType.INTERSECTION, this, o);
		return returnValue.evaluate();
	}

	@Override
	public SubtypeConstraint union(final SubtypeConstraint other) {
		StringSubtypeTreeElement o = (StringSubtypeTreeElement) other;
		if (o instanceof StringSetConstraint) {
			StringSetConstraint ssc = (StringSetConstraint) o;
			if (ssc.constraintType == constraintType) {
				return new StringSetConstraint(stringType, constraintType, constraint.union(((StringSetConstraint) o).constraint));
			}
		}
		StringSetOperation returnValue = new StringSetOperation(stringType, OperationType.UNION, this, o);
		return returnValue.evaluate();
	}

	@Override
	public SubtypeConstraint except(final SubtypeConstraint other) {
		StringSubtypeTreeElement o = (StringSubtypeTreeElement) other;
		if (o instanceof StringSetConstraint) {
			StringSetConstraint ssc = (StringSetConstraint) o;
			if (ssc.constraintType == constraintType) {
				return new StringSetConstraint(stringType, constraintType, constraint.except(((StringSetConstraint) o).constraint));
			}
		}
		StringSetOperation returnValue = new StringSetOperation(stringType, OperationType.EXCEPT, this, o);
		return returnValue.evaluate();
	}

	// if the constraints are orthogonal (e.g. size and alphabet) or just
	// different then return TUNKNOWN
	// in case of orthogonal constraints we should return TFALSE (if other
	// is not full set)
	// but it seems that the standard wants to ignore such trivial cases,
	// example:
	// length(1..4) is_subset ('a'..'z') shall not report an error
	@Override
	public TernaryBool isSubset(final SubtypeConstraint other) {
		StringSubtypeTreeElement o = (StringSubtypeTreeElement) other;
		if (o instanceof StringSetConstraint) {
			StringSetConstraint ssc = (StringSetConstraint) o;
			if (constraintType == ssc.constraintType) {
				return constraint.isSubset(ssc.constraint);
			}
		}
		return TernaryBool.TUNKNOWN;
	}

	@Override
	public boolean isElement(final Object o) {
		switch (constraintType) {
		case ALPHABET_CONSTRAINT: {
			if (stringType == StringType.CHARSTRING) {
				String str = (String) o;
				for (int i = 0; i < str.length(); i++) {
					if (!constraint.isElement(new CharLimit(str.charAt(i)))) {
						return false;
					}
				}
				return true;
			}

			UniversalCharstring str = (UniversalCharstring) o;
			for (int i = 0; i < str.length(); i++) {
				if (!constraint.isElement(new UCharLimit(str.get(i)))) {
					return false;
				}
			}
			return true;
		}
		case SIZE_CONSTRAINT: {
			if (stringType == StringType.CHARSTRING) {
				String str = (String) o;
				return constraint.isElement(new SizeLimit(str.length()));
			}

			UniversalCharstring str = (UniversalCharstring) o;
			return constraint.isElement(new SizeLimit(str.length()));
		}
		case PATTERN_CONSTRAINT:
		case VALUE_CONSTRAINT:
			return constraint.isElement(o);
		default:
			ErrorReporter.INTERNAL_ERROR();
		}
		return true;
	}

	@Override
	public TernaryBool isEmpty() {
		return constraint.isEmpty();
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		return constraint.isEqual(other);
	}

	@Override
	public TernaryBool isFull() {
		return constraint.isFull();
	}

	@Override
	public void toString(final StringBuilder sb) {
		if (constraintType == ConstraintType.SIZE_CONSTRAINT) {
			sb.append("length");
		}
		constraint.toString(sb);
	}

	@Override
	public StringSubtypeTreeElement evaluate() {
		if (constraint.isEmpty() == TernaryBool.TTRUE) {
			return new EmptyStringSet(stringType);
		}
		if (constraint.isFull() == TernaryBool.TTRUE) {
			return new FullStringSet(stringType);
		}
		return this;
	}

	/**
	 * if this is a value list and the other is a size/alphabet/pattern
	 * constraint then call the remove function of the value list
	 */
	public StringSetConstraint remove(final StringSubtypeTreeElement other, final boolean ifElement) {
		if (constraintType != ConstraintType.VALUE_CONSTRAINT) {
			return this;
		}

		if (!(other instanceof StringSetConstraint)) {
			return this;
		}

		StringSetConstraint o = (StringSetConstraint) other;
		switch (o.getType()) {
		case SIZE_CONSTRAINT:
		case ALPHABET_CONSTRAINT: {
			if (stringType == StringType.CHARSTRING) {
				StringValueConstraint svc = (StringValueConstraint) constraint;
				return new StringSetConstraint(stringType, constraintType, svc.remove((RangeListConstraint) o.getConstraint(),
						ifElement));
			}

			UStringValueConstraint usvc = (UStringValueConstraint) constraint;
			return new StringSetConstraint(stringType, constraintType, usvc.remove((RangeListConstraint) o.getConstraint(), ifElement));
		}
		case PATTERN_CONSTRAINT: {
			if (stringType == StringType.CHARSTRING) {
				StringValueConstraint svc = (StringValueConstraint) constraint;
				return new StringSetConstraint(stringType, constraintType, svc.remove((StringPatternConstraint) o.getConstraint(),
						ifElement));
			}

			UStringValueConstraint usvc = (UStringValueConstraint) constraint;
			return new StringSetConstraint(stringType, constraintType, usvc.remove((StringPatternConstraint) o.getConstraint(),
					ifElement));
		}
		default:
			return this;
		}
	}
}
