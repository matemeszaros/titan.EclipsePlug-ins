/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the base class of expressions.
 * 
 * @author Kristof Szabados
 * */
public abstract class Expression_Value extends Value {
	private static final String CONSTANTEXPECTED = "An evaluatable constant value was expected instead of operation `{0}''";
	private static final String STATICVALUEEXPECTED = "A static value was expected instead of operation `{0}''";
	private static final String NOTINCONTROLPART = "Operation `{0}'' is not allowed in the control part";
	private static final String NOTINRUNSON = "Operation `{0}'' cannot be used in a definition that has `runs on'' clause";
	private static final String ONLYINRUNSON = "Operation `{0}'' can be used only in a definition that has `runs on'' clause";
	private static final String ONLYINSTATEMENTS = "Operation `{0}'' is allowed only within statements";

	protected static final String OPERAND = ".<operand>";
	protected static final String OPERAND1 = ".<operand1>";
	protected static final String OPERAND2 = ".<operand2>";
	protected static final String OPERAND3 = ".<operand3>";
	protected static final String OPERAND4 = ".<operand4>";

	public enum Operation_type {
		/** unary plus (+). */						UNARYPLUS_OPERATION,
		/** unary minus (- val). */				UNARYMINUS_OPERATION,
		/** negation (not val). */				NOT_OPERATION,
		/** not4b (not4b val).*/					NOT4B_OPERATION,
		/** addition (val1+ val2).*/			ADD_OPERATION,
		/** subtract (val1 - val2).*/			SUBSTRACT_OPERATION,
		/** multiply (val1 * val2 ). */			MULTIPLY_OPERARTION,
		/** divide (val1 / val2). */				DIVIDE_OPERATION,
		/** modulo (val1 mod val2). */		MODULO_OPERATION,
		/** remainder (val1 rem val2). */	REMAINDER_OPERATION,
		/** and (val1 and val2). */				AND_OPERATION,
		/** or (val1 or val2). */					OR_OPERATION,
		/** xor (val1 xor val2). */				XOR_OPERATION,
		/** and4b (val1 and4b val2). */		AND4B_OPERATION,
		/** or4b (val1 or4b val2). */			OR4B_OPERATION,
		/** xor4b (val1 xor4b val2). */		XOR4B_OPERATION,
		/** shift left (val1 << val2). */		SHIFTLEFT_OPERATION,
		/** shift_right (val1 >> val2). */		SHIFTRIGHT_OPERATION,
		/** rotate left (val1 <@ val2).*/		ROTATELEFT_OPERATION,
		/** rotate right(val1 @>val2). */		ROTATERIGHT_OPERATION,
		/** concatenation (val1 & val2). */	CONCATENATION_OPERATION,
		/** equality (val1 == val2). */			EQUALS_OPERATION,
		/** not equals (val1 != val2). */			NOTEQUALS_OPERATION,
		/** less than (val1 < val2). */			LESSTHAN_OPERATION,
		/** greater than (val1 > val2). */					GREATERTHAN_OPERATION,
		/** greater than or equal (val1 >= val2). */	GREATERTHANOREQUAL_OPERATION,
		/** less than or equal (val1 <= val2). */		LESSTHANOREQUAL_OPERATION,
		/** bit2hex (bit2hex(value)). */						BIT2HEX_OPERATION,
		/** bit2int (bit2int(value)). */							BIT2INT_OPERATION,
		/** bit2oct (bit2oct(value)). */						BIT2OCT_OPERATION,
		/** bit2str (bit2str(value)). */							BIT2STR_OPERATION,
		/** char2str (char2str(value)). */					CHAR2STR_OPERATION,
		/** char2oct (char2oct(value)). */					CHAR2OCT_OPERATION,
		/** float2int (float2int(value)). */					FLOAT2INT_OPERATION,
		/** float2str (float2str(value)). */					FLOAT2STR_OPERATION,
		/** hex2bit (hex2str(value)). */						HEX2BIT_OPERATION,
		/** hex2Int (hex2int(value)). */						HEX2INT_OPERATION,
		/** hex2oct (hex2oct(value)). */					HEX2OCT_OPERATION,
		/** hex2str (hex2str(value)). */						HEX2STR_OPERATION,
		/** int2char (int2char(value)). */					INT2CHAR_OPERATION,
		/** int2float (int2float(value)). */					INT2FLOAT_OPERATION,
		/** int2str (int2str(value)). */							INT2STR_OPERATION,
		/** int2unichar (int2unichar(value)). */			INT2UNICHAR_OPERATION,
		/** oct2bit (oct2bit(value)). */						OCT2BIT_OPERATION,
		/** oct2char (oct2char(value)). */					OCT2CHAR_OPERATION,
		/** oct2hex (oct2hex(value)). */					OCT2HEX_OPERATION,
		/** oct2int (oct2int(value)). */						OCT2INT_OPERATION,
		/** oct2str (oct2str(value)). */						OCT2STR_OPERATION,
		/** str2bit (str2bit(value)). */							STR2BIT_OPERATION,
		/** str2float (str2float(value)). */					STR2FLOAT_OPERATION,
		/** str2hex (str2hex(value)). */						STR2HEX_OPERATION,
		/** str2int (str2int(value)). */							STR2INT_OPERATION,
		/** str2oct (str2oct(value)). */						STR2OCT_OPERATION,
		/** unichar2int (unichar2int(value)). */			UNICHAR2INT_OPERATION,
		/** unichar2char (unichar2char(value)). */	UNICHAR2CHAR_OPERATION,
		/** enum2int (enum2int(value)). */				ENUM2INT_OPERATION,
		/** encode (encode(value)). */								ENCODE_OPERATION,
		/** decode (decode(reference, reference)). */		DECODE_OPERATION,
		/** int2bit (int2bit(value1, value2)). */					INT2BIT_OPERATION,
		/** int2hex (int2hex(value1, value2)). */				INT2HEX_OPERATION,
		/** int2oct (int2oct(value1, value2)). */				INT2OCT_OPERATION,
		/** decomp (decomp(val1, val2, val3)). */				DECOMP_OPERATION,
		/** replace (replace(val1, val2, val3, val4)). */		REPLACE_OPERATION,
		/** substr (substr(val1, val2, val3)). */					SUBSTR_OPERATION,
		/** rnd. */																RND_OPERATION,
		/** rnd with value (rnd(value1)). */						RNDWITHVALUE_OPERATION,
		/** isbound (isbound(templateInstance)). */			ISBOUND_OPERATION,
		/** isvalue (isvalue(templateInstance)). */			ISVALUE_OPERATION,
		/** lengthof (lengthof(templateInstance)). */		LENGTHOF_OPERATION,
		/** ispresent (ispresent(reference)). */					ISPRESENT_OPERATION,
		/** ischoosen (ischoosen(reference)). */				ISCHOOSEN_OPERATION,
		/** sizeof (sizeof(reference)). */							SIZEOF_OPERATION,
		/** regexp (regexp(val1, val2, val3)). */				REGULAREXPRESSION_OPERATION,
		/** valueof (valueof(templateinstance)). */			VALUEOF_OPERATION,
		/** match (match(value, templateinstance)). */	MATCH_OPERATION,
		/** getverdict (getverdict()). */								GETVERDICT_OPERATION,
		/** null. */																COMPONENT_NULL_OPERATION,
		/** mtc. */																MTC_COMPONENT_OPERATION,
		/** system. */															SYSTEM_COMPONENT_OPERATION,
		/** self. */																SELF_COMPONENT_OPERATION,
		/** any component . running. */							ANY_COMPONENT_RUNNING_OPERATION,
		/** all component . running. */								ALL_COMPONENT_RUNNING_OPERATION,
		/** any component . alive. */									ANY_COMPONENT_ALIVE_OPERATION,
		/** all component . alive. */									ALL_COMPONENT_ALIVE_OPERATION,
		/** any timer . running. */										ANY_TIMER_RUNNING_OPERATION,
		/** component running (reference ().running). */	COMPONENT_RUNNING_OPERATION,
		/** component alive (reference.alive). */				COMPONENT_ALIVE_OPERATION,
		/** timer read (reference . read). */						TIMER_READ_OPERATION,
		/** undefined running (reference.running). */		UNDEFINED_RUNNING_OPERATION,
		/** timer running (reference.running). */						TIMER_RUNNING_OPERATION,
		/** create (reference.create(name, location) alive).*/	COMPONENT_CREATE_OPERATION,
		/** activate (activate(reference(pars))). */					ACTIVATE_OPERATION,
		/** activate (activate(derefers(value)(pars))).*/			ACTIVATE_REFERENCED_OPERATION,
		/** execute (execute(reference(pars), timer)). */				EXECUTE_OPERATION,
		/** execute (execute(derefers(value)(pars), timer)).*/		EXECUTE_REFERENCED_OPERATION,
		/** refers ( refers(reference)). */											REFERS_OPERATION,
		/** apply (value.apply(parameters)). */								APPLY_OPERATION,
		/** log2str(...). */									LOG2STR_OPERATION,
		/** testcasename. */									TESTCASENAME_OPERATION,
		/** ttcn2string */										TTCN2STRING_OPERATION,
		/** get_stringencoding (get_stringencoding(octetstring)) */ 			GET_STRINGENCODING_OPERATION,
		/** oct2unichar (oct2unichar(octetstring [,charstring])) */ 			OCT2UNICHAR_OPERATION,
		/** remove_bom (remove_bom(octetstring)) */ 							REMOVE_BOM_OPERATION,
		/** unichar2oct (unichar2oct(universal charstring [,charstring]))*/ 	UNICHAR2OCT_OPERATION,
		/** encode_base64 (encode_base64(octetstring [,boolean])) */ 			ENCODE_BASE64_OPERATION,
		/** decode_base64 (decode_base64(charstring)) */ 						DECODE_BASE64_OPERATION,
		/** @profiler . running */								PROFILER_RUNNING_OPERATION
	}

	/** The value of the expression if already evaluated, used to speed things up. */
	protected IValue lastValue;

	@Override
	public final Value_type getValuetype() {
		return Value_type.EXPRESSION_VALUE;
	}

	public abstract Operation_type getOperationType();

	@Override
	public final String chainedDescription() {
		return "expression: " + getFullName();
	}

	@Override
	public final IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference,
			final int actualSubReference, final IReferenceChain refChain) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return this;
		}

		IValue result = getValueRefdLast(timestamp, refChain);
		if (result != null && result != this) {
			result = result.getReferencedSubValue(timestamp, reference, actualSubReference, refChain);
			if (result != null && result.getIsErroneous(timestamp)) {
				setIsErroneous(true);
			}
		}

		return this;
	}

	/**
	 * Evaluates the expression.
	 * <p>
	 * Checks the operands if any.
	 * If the expression if foldable this function will calculate and return with the result otherwise returns with itself
	 * <p>
	 * Must only be called from the {@link #getValueRefdLast(CompilationTimeStamp, ReferenceChain)} function.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @param expectedValue the kind of the value to be expected
	 * @param referenceChain a initialized reference chain to help detecting circular references.
	 *
	 * @return the result of the evaluation
	 * */
	public abstract IValue evaluateValue(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain);

	/**
	 * This function is used to check the expression semantically.
	 * The function itself shell evaluate the internal semantic checking of the value, and return with the either the folded value or itself.
	 * This way when this function is called from a type, it is able to check the kind of the value against the type.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @param expectedValue the kind of the value to be expected
	 * @param referenceChain an initialized reference chain to help detecting circular references.
	 *
	 * @return the evaluated value of the expression or the expression itself if it can not be folded in compilation time.
	 * */
	@Override
	public final IValue getValueRefdLast(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		IReferenceChain tempReferenceChain =
				referenceChain != null ? referenceChain : ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);

		IValue last = this;

		tempReferenceChain.markState();

		if (tempReferenceChain.add(this)) {
			last = evaluateValue(timestamp, expectedValue, tempReferenceChain);
		} else {
			setIsErroneous(true);
		}

		tempReferenceChain.previousState();

		if (!tempReferenceChain.equals(referenceChain)) {
			tempReferenceChain.release();
		}

		// the last safety check just to be sure
		if (last == null) {
			return this;
		}

		return last;
	}

	@Override
	public final boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		return this == other;
	}

	/**
	 * Checks if the operation is used in the right dynamic context.
	 *
	 * @param expectedValue the value kind expected.
	 * @param operationName the name of the operation.
	 * @param allowControlpart true if the operation is allowed in a control part, otherwise false.
	 * @param allowRunsOn true if the operation is allowed in a definition with runs on clause, otherwise false.
	 * @param requireRunsOn true if the operation can only be used in a definition with runs on clause, otherwise false.
	 * */
	protected final void checkExpressionDynamicPart(final Expected_Value_type expectedValue, final String operationName,
			final boolean allowControlpart, final boolean allowRunsOn, final boolean requireRunsOn) {
		switch(expectedValue) {
		case EXPECTED_CONSTANT:
			location.reportSemanticError(MessageFormat.format(CONSTANTEXPECTED, operationName));
			setIsErroneous(true);
			return;
		case EXPECTED_STATIC_VALUE:
			location.reportSemanticError(MessageFormat.format(STATICVALUEEXPECTED, operationName));
			setIsErroneous(true);
			return;
		default:
			break;
		}

		if (myScope == null) {
			return;
		}

		if (!allowRunsOn && myScope.getScopeRunsOn() != null) {
			location.reportSemanticError(MessageFormat.format(NOTINRUNSON, operationName));
			setIsErroneous(true);
		} else if (requireRunsOn && myScope.getScopeRunsOn() == null) {
			location.reportSemanticError(MessageFormat.format(ONLYINRUNSON, operationName));
			setIsErroneous(true);
		} else {
			StatementBlock myStatementBlock = myScope.getStatementBlockScope();
			if (myStatementBlock == null) {
				location.reportSemanticError(MessageFormat.format(ONLYINSTATEMENTS, operationName));
				setIsErroneous(true);
			} else if (!allowControlpart && myStatementBlock.getMyDefinition() == null) {
				location.reportSemanticError(MessageFormat.format(NOTINCONTROLPART, operationName));
				setIsErroneous(true);
			}
		}
	}

	@Override
	public abstract void updateSyntax(TTCN3ReparseUpdater reparser, boolean isDamaged) throws ReParseException;
}
