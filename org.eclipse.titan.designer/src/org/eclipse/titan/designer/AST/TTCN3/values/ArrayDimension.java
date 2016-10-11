/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an array dimension description.
 * 
 * @author Kristof Szabados
 * */
public abstract class ArrayDimension extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private static final String INTEGEREXPECTED = "Integer value expected as array index";
	private static final String INDEXUNDERFLOW = "Array index underflow: the index value must be at least {0} instead of {1}";
	private static final String INDEXOVERFLOW = "Array index oveflow: the index value must be at most {0} instead of {1}";

	protected CompilationTimeStamp lastTimeChecked;
	private boolean isErroneous = false;
	private Location location = NULL_Location.INSTANCE;

	@Override
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public final Location getLocation() {
		return location;
	}

	/**
	 * @return the size of the array dimension, how many elements it can hold.
	 * */
	public abstract long getSize();

	/**
	 * @return the starting index of the array dimension.
	 * */
	public abstract long getOffset();

	/**
	 * Creates and returns a string representation if the actual dimension.
	 *
	 * @return the string representation of the dimension.
	 * */
	public abstract String createStringRepresentation();

	/**
	 * Checks if the array dimension was already reported erroneous in the actual semantic check cycle.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 *
	 * @return true if this array dimension description was found erroneous in
	 *         the actual semantic check cycle, false otherwise.
	 * */
	public final boolean getIsErroneous(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return isErroneous;
		}

		return false;
	}

	/**
	 * Sets the erroneousness of the array dimension description to be erroneous
	 * in this semantic check cycle.
	 *
	 * @param isErroneous the value to be set for erroneousness.
	 * */
	public final void setIsErroneous(final boolean isErroneous) {
		this.isErroneous = isErroneous;
	}

	/**
	 * Checks the array dimension semantically.
	 *
	 * @param timestamp the timestamp of the actual build cycle.
	 * */
	public abstract void check(final CompilationTimeStamp timestamp);

	/**
	 * Check the array index against this dimension of the actual array type.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @param index the index of the array index sub reference.
	 * @param expectedValue the kind of value expected here
	 * */
	public final void checkIndex(final CompilationTimeStamp timestamp, final IValue index,
			final Expected_Value_type expectedValue) {
		check(timestamp);

		if (index == null) {
			return;
		}

		index.setLoweridToReference(timestamp);
		Type_type temporalType = index.getExpressionReturntype(timestamp, expectedValue);

		switch (temporalType) {
		case TYPE_INTEGER:
			break;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			index.getLocation().reportSemanticError(INTEGEREXPECTED);
			setIsErroneous(true);
			break;
		}

		if (getIsErroneous(timestamp) || index.isUnfoldable(timestamp)) {
			return;
		}

		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = index.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		long indexValue = ((Integer_Value) last).getValue();
		if (indexValue < getOffset()) {
			index.getLocation().reportSemanticError(MessageFormat.format(INDEXUNDERFLOW, getOffset(), indexValue));
			index.setIsErroneous(true);
		} else if (indexValue >= getOffset() + getSize()) {
			index.getLocation().reportSemanticError(MessageFormat.format(INDEXOVERFLOW, getOffset() + getSize() - 1, indexValue));
			index.setIsErroneous(true);
		}
	}

	/**
	 * Returns whether this dimension is identical to the one received as
	 * parameter.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param other the dimension to check against
	 * @return true if they are identical, false otherwise
	 * */
	public final boolean isIdentical(final CompilationTimeStamp timestamp, final ArrayDimension other) {
		if (other == null) {
			return true;
		}

		check(timestamp);
		other.check(timestamp);
		if (getIsErroneous(timestamp) || other.getIsErroneous(timestamp)) {
			return true;
		}

		return getSize() == other.getSize() && getOffset() == other.getOffset();
	}

	/**
	 * Handles the incremental parsing of this array dimension.
	 *
	 * @param reparser the parser doing the incremental parsing.
	 * @param isDamaged true if the location contains the damaged area, false if
	 *            only its' location needs to be updated.
	 * */
	@Override
	public abstract void updateSyntax(TTCN3ReparseUpdater reparser, boolean isDamaged) throws ReParseException;
}
