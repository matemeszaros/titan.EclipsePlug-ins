/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.StringSetConstraint.ConstraintType;

/**
 * @author Adam Delic
 * */
public final class StringSetOperation extends StringSubtypeTreeElement {
	public enum OperationType {
		INTERSECTION, INHERIT, UNION, EXCEPT
	}

	private final OperationType operation_type;
	private StringSubtypeTreeElement a;
	private StringSubtypeTreeElement b;

	public StringSetOperation(final StringType string_type, final OperationType operation_type, final StringSubtypeTreeElement a,
			final StringSubtypeTreeElement b) {
		super(string_type);
		this.operation_type = operation_type;
		this.a = a;
		this.b = b;
	}

	@Override
	public ElementType getElementType() {
		return ElementType.OPERATION;
	}

	@Override
	public SubtypeConstraint complement() {
		StringSetOperation returnValue = new StringSetOperation(string_type, OperationType.EXCEPT, new FullStringSet(string_type), this);
		return returnValue.evaluate();
	}

	@Override
	public SubtypeConstraint intersection(final SubtypeConstraint other) {
		StringSetOperation returnValue = new StringSetOperation(string_type, OperationType.INTERSECTION, this,
				(StringSubtypeTreeElement) other);
		return returnValue.evaluate();
	}

	@Override
	public boolean isElement(final Object o) {
		switch (operation_type) {
		case INHERIT:
		case INTERSECTION:
			return a.isElement(o) && b.isElement(o);
		case UNION:
			return a.isElement(o) || b.isElement(o);
		case EXCEPT:
			return a.isElement(o) && !b.isElement(o);
		default:
			return false;
		}
	}

	@Override
	public TernaryBool isEmpty() {
		switch (operation_type) {
		case INHERIT:
		case INTERSECTION:
			return a.isEmpty().or(b.isEmpty());
		case UNION:
			return a.isEmpty().and(b.isEmpty());
		case EXCEPT: {
			TernaryBool aEmpty = a.isEmpty();
			return ((aEmpty != TernaryBool.TFALSE) ? aEmpty : ((b.isEmpty() == TernaryBool.TTRUE) ? TernaryBool.TFALSE
					: TernaryBool.TUNKNOWN));
		}
		default:
			return TernaryBool.TUNKNOWN;
		}
	}

	@Override
	public TernaryBool isEqual(final SubtypeConstraint other) {
		return TernaryBool.TUNKNOWN;
	}

	@Override
	public TernaryBool isFull() {
		switch (operation_type) {
		case INHERIT:
		case INTERSECTION:
			return a.isFull().and(b.isFull());
		case UNION:
			return a.isFull().or(b.isFull());
		case EXCEPT:
			return a.isFull().and(b.isEmpty());
		default:
			return TernaryBool.TUNKNOWN;
		}
	}

	@Override
	public void toString(final StringBuilder sb) {
		sb.append('(');
		a.toString(sb);
		switch (operation_type) {
		case INHERIT:
			sb.append(" intersection ");
			break;
		case INTERSECTION:
			sb.append(" intersection ");
			break;
		case UNION:
			sb.append(" union ");
			break;
		case EXCEPT:
			sb.append(" except ");
			break;
		default:
			sb.append(" <unknown operation> ");
		}
		b.toString(sb);
		sb.append(')');
	}

	@Override
	public SubtypeConstraint union(final SubtypeConstraint other) {
		StringSetOperation returnValue = new StringSetOperation(string_type, OperationType.UNION, this, (StringSubtypeTreeElement) other);
		return returnValue.evaluate();
	}

	@Override
	public SubtypeConstraint except(final SubtypeConstraint other) {
		StringSetOperation returnValue = new StringSetOperation(string_type, OperationType.EXCEPT, this, (StringSubtypeTreeElement) other);
		return returnValue.evaluate();
	}

	@Override
	public TernaryBool isSubset(final SubtypeConstraint other) {
		return TernaryBool.TUNKNOWN;
	}

	@Override
	public StringSubtypeTreeElement evaluate() {
		// recursive evaluation
		a = a.evaluate();
		b = b.evaluate();

		// special simple cases when one side is ET_ALL or ET_NONE but
		// the other can be a tree
		if ((a instanceof EmptyStringSet) || (b instanceof EmptyStringSet)) {
			if ((operation_type == OperationType.INHERIT) || (operation_type == OperationType.INTERSECTION)) {
				return new EmptyStringSet(string_type);
			}
			if (operation_type == OperationType.UNION) {
				return (a instanceof EmptyStringSet) ? a : b;
			}
		}
		if ((b instanceof EmptyStringSet) && (operation_type == OperationType.EXCEPT)) {
			return a;
		}
		if ((a instanceof FullStringSet) || (b instanceof FullStringSet)) {
			if ((operation_type == OperationType.INHERIT) || (operation_type == OperationType.INTERSECTION)) {
				return (a instanceof FullStringSet) ? b : a;
			}
			if (operation_type == OperationType.UNION) {
				return (a instanceof FullStringSet) ? a : b;
			}
		}
		if ((b instanceof FullStringSet) && (operation_type == OperationType.EXCEPT)) {
			return new EmptyStringSet(string_type);
		}

		// both operands must be single constraints
		// (ALL,NONE,CONSTRAINT),
		// after this point trees will not be further simplified
		if ((a instanceof StringSetOperation) || (b instanceof StringSetOperation)) {
			return this;
		}

		// special case: ALL - some constraint type that can be
		// complemented
		if ((a instanceof FullStringSet) && (operation_type == OperationType.EXCEPT) && (b instanceof StringSetConstraint)) {
			switch (((StringSetConstraint) b).getType()) {
			case SIZE_CONSTRAINT:
			case ALPHABET_CONSTRAINT:
				return ((StringSetConstraint) b.complement()).evaluate();
			}
		}

		// special case: when one operand is VALUE_CONSTRAINT then
		// isElement() can be called for the values
		// and drop values or drop the other operand set or both
		// depending on the operation
		switch (operation_type) {
		case INHERIT:
		case INTERSECTION:
			if (a instanceof StringSetConstraint) {
				if (((StringSetConstraint) a).getType() == ConstraintType.VALUE_CONSTRAINT) {
					a = ((StringSetConstraint) a).remove(b, false);
					a.evaluate();
					return a;
				}
			}
			if (b instanceof StringSetConstraint) {
				if (((StringSetConstraint) b).getType() == ConstraintType.VALUE_CONSTRAINT) {
					b = ((StringSetConstraint) b).remove(a, false);
					b.evaluate();
					return b;
				}
			}
			break;
		case UNION:
			if (a instanceof StringSetConstraint) {
				if (((StringSetConstraint) a).getType() == ConstraintType.VALUE_CONSTRAINT) {
					a = ((StringSetConstraint) a).remove(b, true);
					a.evaluate();
					break;
				}
			}
			if (b instanceof StringSetConstraint) {
				if (((StringSetConstraint) b).getType() == ConstraintType.VALUE_CONSTRAINT) {
					b = ((StringSetConstraint) b).remove(a, true);
					b.evaluate();
					break;
				}
			}
			break;
		case EXCEPT:
			if (a instanceof StringSetConstraint) {
				if (((StringSetConstraint) a).getType() == ConstraintType.VALUE_CONSTRAINT) {
					a = ((StringSetConstraint) a).remove(b, true);
					a.evaluate();
					return a;
				}
			}
		}

		// operands of same types can be evaluated to one constraint
		// using their
		// set arithmetic member functions
		if (a.getElementType() == b.getElementType()) {
			switch (a.getElementType()) {
			case ALL:
				if (operation_type == OperationType.EXCEPT) {
					return new EmptyStringSet(string_type);
				}

				return a;
			case NONE:
				return a;
			case CONSTRAINT:
				if (((StringSetConstraint) a).getType() == ((StringSetConstraint) b).getType()) {
					if (((StringSetConstraint) a).getType() == ConstraintType.PATTERN_CONSTRAINT) {
						break;
					}
					switch (operation_type) {
					case INHERIT:
					case INTERSECTION:
						return (StringSubtypeTreeElement) a.intersection(b);
					case UNION:
						return (StringSubtypeTreeElement) a.union(b);
					case EXCEPT:
						return (StringSubtypeTreeElement) a.except(b);
					}
				}
			}
		}

		return this;
	}

}
