/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

/**
 * interface for all limit classes used by RangeListConstraint
 * 
 * @author Adam Delic
 */
public abstract class LimitType implements Comparable<LimitType> {

	/** all possible kinds of limits, each one is implemented with a class */
	public enum Type {
		SIZE, INTEGER, CHAR, UCHAR, REAL
	}

	public abstract LimitType.Type getType();

	public abstract boolean isAdjacent(LimitType other);

	public abstract LimitType increment();

	public abstract LimitType decrement();

	public abstract void toString(StringBuilder sb);

	public static LimitType getMaximum(final Type type) {
		switch (type) {
		case SIZE:
			return SizeLimit.MAXIMUM;
		case INTEGER:
			return IntegerLimit.MAXIMUM;
		case CHAR:
			return CharLimit.MAXIMUM;
		case UCHAR:
			return UCharLimit.MAXIMUM;
		case REAL:
			return RealLimit.MAXIMUM;
		default:
			return null;
		}
	}

	public static LimitType getMinimum(final Type type) {
		switch (type) {
		case SIZE:
			return SizeLimit.MINIMUM;
		case INTEGER:
			return IntegerLimit.MINIMUM;
		case CHAR:
			return CharLimit.MINIMUM;
		case UCHAR:
			return UCharLimit.MINIMUM;
		case REAL:
			return RealLimit.MINIMUM;
		default:
			return null;
		}
	}
}
