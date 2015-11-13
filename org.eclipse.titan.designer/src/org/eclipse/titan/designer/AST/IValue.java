/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsValueExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public interface IValue extends IGovernedSimple, IIdentifierContainer, IVisitableNode {

	public enum Value_type {
		// common values (they reside among the TTCN-3 values package)
		/** NULL (ASN.1). */
		ASN1_NULL_VALUE,
		/** boolean. */
		BOOLEAN_VALUE,
		/** integer. */
		INTEGER_VALUE,
		/** real / float. */
		REAL_VALUE,
		/** charstring. */
		CHARSTRING_VALUE,
		/** universal charstrin. */
		UNIVERSALCHARSTRING_VALUE,
		/** omit value. */
		OMIT_VALUE,
		/** object identifier. */
		OBJECTID_VALUE,
		/** sequence of. */
		SEQUENCEOF_VALUE,
		/** set of. */
		SETOF_VALUE,
		/** sequence. */
		SEQUENCE_VALUE,
		/** set. */
		SET_VALUE,
		/** referenced. */
		REFERENCED_VALUE,
		/** enumerated. */
		ENUMERATED_VALUE,
		/** undefined loweridentifier. */
		UNDEFINED_LOWERIDENTIFIER_VALUE,
		/** choice. */
		CHOICE_VALUE,
		// choice

		// TTCN-3 values
		/** general NULL (TTCN-3). */
		TTCN3_NULL_VALUE,
		/** default null (TTCN-3). */
		DEFAULT_NULL_VALUE,
		/** function reference null (TTCN-3). */
		FAT_NULL_VALUE,
		/** bitstring (TTCN-3). */
		BITSTRING_VALUE,
		/** hexstring (TTCN-3). */
		HEXSTRING_VALUE,
		/** octetstring (TTCN-3). */
		OCTETSTRING_VALUE,
		/** expressions (TTCN-3). */
		EXPRESSION_VALUE,
		/** verdict. */
		VERDICT_VALUE,
		/** macro. */
		MACRO_VALUE,
		/** not used symbol ('-'). */
		NOTUSED_VALUE,
		/** array. */
		ARRAY_VALUE,
		/** function reference. */
		FUNCTION_REFERENCE_VALUE,
		/** altstep reference. */
		ALTSTEP_REFERENCE_VALUE,
		/** testcase reference. */
		TESTCASE_REFERENCE_VALUE,
		/** anytype. */
		ANYTYPE_VALUE,

		// ASN.1 values
		/** undefined block. */
		UNDEFINED_BLOCK,
		/** named integer. */
		NAMED_INTEGER_VALUE,
		/** named bits. */
		NAMED_BITS,
		/** parsed ASN.1 string notation. */
		CHARSYMBOLS_VALUE,
		/** ISO-2022 string. */
		ISO2022STRING_VALUE,
		/** relative object identifier */
		RELATIVEOBJECTIDENTIFIER_VALUE
		// iso2022str
		// opentype
	}

	/**
	 * Copies the general value -ish properties of the value in parameter to
	 * the actual one.
	 * <p>
	 * This function is used to help writing conversion function without
	 * using a generic copy-constructor mechanism.
	 *
	 * @param original
	 *                the original value, whose properties will be copied
	 * */
	void copyGeneralProperties(final IValue original);

	Value_type getValuetype();

	@Override
	IType getMyGovernor();

	/**
	 * Sets the governor type.
	 *
	 * @param governor
	 *                the governor to be set.
	 * */
	void setMyGovernor(final IType governor);

	/**
	 * Returns the compilation timestamp of the last time this value was
	 * checked.
	 * <p>
	 * In case of values their check is not self contained, but rather done
	 * by a type. As such the timestamp of checking must also be read and
	 * set externally.
	 *
	 * @return the timestamp of the last time this value was checked.
	 * */
	CompilationTimeStamp getLastTimeChecked();

	/**
	 * Sets the compilation timestamp of the last time this value was
	 * checked.
	 * <p>
	 * In case of values their check is not self contained, but rather done
	 * by a type. As such the timestamp of checking must also be read and
	 * set externally.
	 *
	 * @param lastTimeChecked
	 *                the timestamp when this value was last checked.
	 * */
	void setLastTimeChecked(final CompilationTimeStamp lastTimeChecked);

	/**
	 * Calculates the governor of the value when used in an expression.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected
	 *
	 * @return the governor of the value if it was used in an expression.
	 * */
	IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue);

	/**
	 * Returns true if the value is unknown at compile-time.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 *
	 * @return true if the value is unfoldable, false if it is foldable
	 * */
	boolean isUnfoldable(final CompilationTimeStamp timestamp);

	/**
	 * Returns true if the value is unknown at compile-time.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the reference chain to detect circular references.
	 *
	 * @return true if the value is unfoldable, false if it is foldable
	 * */
	boolean isUnfoldable(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	/**
	 * Returns true if the value is unknown at compile-time.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expected_value
	 *                the kind of the value to be expected.
	 * @param referenceChain
	 *                the reference chain to detect circular references.
	 *
	 * @return true if the value is unfoldable, false if it is foldable
	 * */
	boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expected_value, final IReferenceChain referenceChain);

	/**
	 * Returns the referenced field value for structured values, or itself
	 * in any other case.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference used to select the field.
	 * @param actualSubReference
	 *                the index used to tell, which element of the reference
	 *                to use as the field selector.
	 * @param refChain
	 *                a chain of references used to detect circular
	 *                references.
	 *
	 * @return the value of the field, self, or null.
	 * */
	IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference, int actualSubReference,
			final IReferenceChain refChain);

	/**
	 * Creates a value of the provided type from the actual value if that is
	 * possible.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param new_type
	 *                the new value_type the new value should belong to.
	 *
	 * @return the new value of the provided kind if the conversion is
	 *         possible, or this value otherwise.
	 * */
	IValue setValuetype(final CompilationTimeStamp timestamp, final Value_type new_type);

	/**
	 * Checks whether this value is defining itself in a recursive way. This
	 * can happen for example if a constant is using itself to determine its
	 * initial value.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references.
	 * */
	void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	/**
	 * Creates and returns a string representation if the actual value.
	 *
	 * @return the string representation of the value.
	 * */
	String createStringRepresentation();

	/**
	 * Returns the type of the value to be used in expression evaluation.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected
	 *
	 * @return the type of the value
	 * */
	IType.Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue);

	/**
	 * Returns the value referred last in case of a referred value, or
	 * itself in any other case.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references
	 *
	 * @return the actual or the last referred value
	 * */
	IValue getValueRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	/**
	 * Returns the value referred last in case of a referred value, or
	 * itself in any other case.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references
	 *
	 * @return the actual or the last referred value
	 * */
	IValue getValueRefdLast(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue, final IReferenceChain referenceChain);

	/**
	 * Creates value references from a value that is but a single word. This
	 * can happen if it was not possible to categorize it while parsing.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 *
	 * @return the reference that this lower identifier was converted to, or
	 *         this value.
	 * */
	IValue setLoweridToReference(final CompilationTimeStamp timestamp);

	/**
	 * Checks if the referenced value is equivalent with omit or not.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected
	 * */
	void checkExpressionOmitComparison(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue);

	/**
	 * Check whether the actual value equals the provided one.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param other
	 *                the value to check against.
	 *
	 * @return true if the two values equal, false otherwise.
	 * */
	boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other);

	/**
	 * Evaluates if a value is a valid value argument of the isvalue
	 * expression.
	 *
	 * @see IsValueExpression#evaluateValue(CompilationTimeStamp,
	 *      Expected_Value_type, ReferenceChain)
	 *
	 * @param fromSequence
	 *                true if called from a sequence.
	 *
	 * @return true if the value can be used within the isvalue expression
	 *         directly.
	 * */
	boolean evaluateIsvalue(final boolean fromSequence);

	/**
	 * Evaluates whether the value is bound or not.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference to resolve at this object.
	 * @param actualSubReference
	 *                the index of the sub reference we are resolving at
	 *                this time.
	 *
	 * @return true if the value is bound, false otherwise.
	 * */
	boolean evaluateIsbound(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference);

	/**
	 * Evaluates whether the value is present or not.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference to resolve at this object.
	 * @param actualSubReference
	 *                the index of the sub reference we are resolving at
	 *                this time.
	 *
	 * @return true if the value is present, false otherwise.
	 * */
	boolean evaluateIspresent(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference);

	Definition getDefiningAssignment();

}
