/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.TypeFactory;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsValueExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The base of what a Value is.
 * 
 * @author Kristof Szabados
 * */
// TODO The ASN.1 values can not be incrementally updated.
public abstract class Value extends GovernedSimple implements IReferenceChainElement, IValue, IIncrementallyUpdateable {

	/** The type of the value, which also happens to be its governor. */
	protected IType myGovernor;

	@Override
	public Setting_type getSettingtype() {
		return Setting_type.S_V;
	}

	/**
	 * Copies the general value -ish properties of the value in parameter to the actual one.
	 * <p>
	 * This function is used to help writing conversion function without using a generic copy-constructor mechanism.
	 *
	 * @param original the original value, whose properties will be copied
	 * */
	@Override
	public final void copyGeneralProperties(final IValue original) {
		location = original.getLocation();
		super.setFullNameParent(original.getNameParent());
		myGovernor = original.getMyGovernor();
		setMyScope(original.getMyScope());
	}

	@Override
	public abstract Value_type getValuetype();

	/**
	 * Gets the governor type.
	 *
	 * @return the type governing this value.
	 * */
	@Override
	public final IType getMyGovernor() {
		return myGovernor;
	}

	/**
	 * Sets the governor type.
	 *
	 * @param governor the governor to be set.
	 * */
	@Override
	public final void setMyGovernor(final IType governor) {
		myGovernor = governor;
	}

	/**
	 * Returns the compilation timestamp of the last time this value was checked.
	 * <p>
	 * In case of values their check is not self contained, but rather done by a type.
	 * As such the timestamp of checking must also be read and set externally.
	 *
	 * @return the timestamp of the last time this value was checked.
	 * */
	@Override
	public CompilationTimeStamp getLastTimeChecked() {
		return lastTimeChecked;
	}

	/**
	 * Sets the compilation timestamp of the last time this value was checked.
	 * <p>
	 * In case of values their check is not self contained, but rather done by a type.
	 * As such the timestamp of checking must also be read and set externally.
	 *
	 * @param lastTimeChecked the timestamp when this value was last checked.
	 * */
	@Override
	public void setLastTimeChecked(final CompilationTimeStamp lastTimeChecked) {
		this.lastTimeChecked = lastTimeChecked;
	}

	@Override
	public String chainedDescription() {
		return getFullName();
	}

	@Override
	public Location getChainLocation() {
		return location;
	}

	/**
	 * Calculates the governor of the value when used in an expression.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param expectedValue the kind of the value to be expected
	 *
	 * @return the governor of the value if it was used in an expression.
	 * */
	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		IType type = getMyGovernor();

		if (type == null) {
			type = TypeFactory.createType(getExpressionReturntype(timestamp, expectedValue));
		}
		return type;
	}

	 /**
	  * Returns true if the value is unknown at compile-time.
	  *
	  * @param timestamp the time stamp of the actual semantic check cycle.
	  *
	  * @return true if the value is unfoldable, false if it is foldable
	  * */
	@Override
	public final boolean isUnfoldable(final CompilationTimeStamp timestamp) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		boolean result = isUnfoldable(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, referenceChain);
		referenceChain.release();

		return result;
	}

	 /**
	  * Returns true if the value is unknown at compile-time.
	  *
	  * @param timestamp the time stamp of the actual semantic check cycle.
	  * @param referenceChain the reference chain to detect circular references.
	  *
	  * @return true if the value is unfoldable, false if it is foldable
	  * */
	@Override
	public final boolean isUnfoldable(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		return isUnfoldable(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, referenceChain);
	}

	 /**
	  * Returns true if the value is unknown at compile-time.
	  *
	  * @param timestamp the time stamp of the actual semantic check cycle.
	  * @param expectedValue the kind of the value to be expected.
	  * @param referenceChain the reference chain to detect circular references.
	  *
	  * @return true if the value is unfoldable, false if it is foldable
	  * */
	@Override
	public abstract boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain);

	/**
	 * Returns the referenced field value for structured values, or itself in any other case.
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param reference the reference used to select the field.
	 * @param actualSubReference the index used to tell, which element of the reference to use as the field selector.
	 * @param refChain a chain of references used to detect circular references.
	 *
	 * @return the value of the field, self, or null.
	 * */
	@Override
	public abstract IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference, int actualSubReference,
			final IReferenceChain refChain);

	/**
	 * Creates a value of the provided type from the actual value if that is possible.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param newType the new value_type the new value should belong to.
	 *
	 * @return the new value of the provided kind if the conversion is possible, or this value otherwise.
	 * */
	@Override
	public Value setValuetype(final CompilationTimeStamp timestamp, final Value_type newType) {
		setIsErroneous(true);
		return this;
	}

	/**
	 * Checks whether this value is defining itself in a recursive way.
	 * This can happen for example if a constant is using itself to determine its initial value.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param referenceChain the ReferenceChain used to detect circular references.
	 * */
	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		IReferenceChain tempReferencChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue temp = getValueRefdLast(timestamp, tempReferencChain);
		tempReferencChain.release();

		if (!temp.getIsErroneous(timestamp) && this != temp && referenceChain.add(this)) {
			temp.checkRecursions(timestamp, referenceChain);
		}
	}

	/**
	 * Creates and returns a string representation if the actual value.
	 *
	 * @return the string representation of the value.
	 * */
	@Override
	public abstract String createStringRepresentation();

	/**
	 * Returns the type of the value to be used in expression evaluation.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param expectedValue the kind of the value to be expected
	 *
	 *  @return the type of the value
	 * */
	@Override
	public abstract IType.Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue);

	/**
	 * Returns the value referred last in case of a referred value, or itself in any other case.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param referenceChain the ReferenceChain used to detect circular references
	 *
	 * @return the actual or the last referred value
	 * */
	@Override
	public final IValue getValueRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		return getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, referenceChain);
	}

	/**
	 * Returns the value referred last in case of a referred value, or itself in any other case.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param expectedValue the kind of the value to be expected
	 * @param referenceChain the ReferenceChain used to detect circular references
	 *
	 * @return the actual or the last referred value
	 * */
	@Override
	public IValue getValueRefdLast(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return this;
		}

		setIsErroneous(false);
		lastTimeChecked = timestamp;
		return this;
	}
	/**
	 * Creates value references from a value that is but a single word.
	 * This can happen if it was not possible to categorize it while parsing.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 *
	 * @return the reference that this lower identifier was converted to, or this value.
	 * */
	@Override
	public IValue setLoweridToReference(final CompilationTimeStamp timestamp) {
		return this;
	}

	/**
	 * Checks if the referenced value is equivalent with omit or not.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param expectedValue the kind of the value to be expected
	 * */
	@Override
	public void checkExpressionOmitComparison(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return;
		}

		getLocation().reportSemanticError("Only a referenced value can be compared with `omit'");
		setIsErroneous(true);
	}

	/**
	 * Check whether the actual value equals the provided one.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param other the value to check against.
	 *
	 * @return true if the two values equal, false otherwise.
	 * */
	@Override
	public abstract boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other);

	/**
	 * Evaluates if a value is a valid value argument of the isvalue expression.
	 *
	 * @see IsValueExpression#evaluateValue(CompilationTimeStamp, Expected_Value_type, ReferenceChain)
	 *
	 * @param fromSequence true if called from a sequence.
	 *
	 * @return true if the value can be used within the isvalue expression directly.
	 * */
	@Override
	public boolean evaluateIsvalue(final boolean fromSequence) {
		return true;
	}

	@Override
	public boolean evaluateIsbound(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference) {
		return true;
	}

	@Override
	public boolean evaluateIspresent(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference) {
		return true;
	}

	/**
	 *  Handles the incremental parsing of this value.
	 *
	 *  @param reparser the parser doing the incremental parsing.
	 *  @param isDamaged true if the location contains the damaged area,
	 *    false if only its' location needs to be updated.
	 * */
	@Override
	public abstract void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException;

	@Override
	public Definition getDefiningAssignment() {
		INamedNode parent = getNameParent();
		while (parent != null && !(parent instanceof Definition)) {
			parent = parent.getNameParent();
		}

		return (Definition) parent;

	}
}
