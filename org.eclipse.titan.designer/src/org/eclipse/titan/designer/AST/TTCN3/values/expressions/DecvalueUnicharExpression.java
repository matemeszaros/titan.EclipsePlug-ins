/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.BridgingNamedNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Referenced_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.values.CharstringExtractor;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Expression type for
 * decvalue_unichar(inout universal charstring encoded_value,
 *                  out any_type decoded_value,
 *                  in charstring string_serialization:= "UTF-8",
 *                  in universal charstring decoding_info := "")
 *           return integer
 * @author Arpad Lovassy
 */
public final class DecvalueUnicharExpression extends Expression_Value {
	private static final String OPERAND1_ERROR1 = "The 1st operand of the `decvalue_unichar' operation should be a universal charstring value";

	private static final String OPERAND2_ERROR1 = "The 2nd operand of the `decvalue_unichar' operation is unable to hold a decoded value";

	private static final String OPERAND3_ERROR1 = "The 3rd operand of the `encvalue_unichar' operation should be a charstring value";
	
	private static final String OPERAND4_ERROR1 = "The 4th operand of the `decvalue_unichar' operation should be a universal charstring value";
	
	private final Reference reference1;
	private final Reference reference2;
	private final Value value3;
	private final Value value4;

	public DecvalueUnicharExpression(final Reference reference1, final Reference reference2, final Value value3, final Value value4) {
		this.reference1 = reference1;
		this.reference2 = reference2;

		this.value3 = value3;
		this.value4 = value4;

		if (reference1 != null) {
			reference1.setFullNameParent(this);
		}
		if (reference2 != null) {
			reference2.setFullNameParent(this);
		}
		if (value3 != null) {
			value3.setFullNameParent(this);
		}
		if (value4 != null) {
			value4.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.DECVALUE_UNICHAR_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("decvalue_unichar(");
		builder.append(reference1.getDisplayName());
		builder.append(", ");
		builder.append(reference2.getDisplayName());
		builder.append(", ");
		builder.append(value3 == null ? "null" : value3.createStringRepresentation());
		builder.append(", ");
		builder.append(value4 == null ? "null" : value4.createStringRepresentation());
		builder.append(')');
		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference1 != null) {
			reference1.setMyScope(scope);
		}
		if (reference2 != null) {
			reference2.setMyScope(scope);
		}
		if (value3 != null) {
			value3.setMyScope(scope);
		}
		if (value4 != null) {
			value4.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (reference1 == child) {
			return builder.append(OPERAND1);
		} else if (reference2 == child) {
			return builder.append(OPERAND2);
		} else if (value3 == child) {
			return builder.append(OPERAND3);
		} else if (value4 == child) {
			return builder.append(OPERAND4);
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_INTEGER;
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		return true;
	}

	/**
	 * Checks the parameters of the expression and if they are valid in
	 * their position in the expression or not.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 */
	private void checkExpressionOperands( final CompilationTimeStamp timestamp,
										  final Expected_Value_type expectedValue,
										  final IReferenceChain referenceChain) {
		//check reference1
		checkExpressionOperand1(timestamp, expectedValue, referenceChain);
		//check reference2
		checkExpressionOperand2(timestamp, expectedValue, referenceChain);
		//check value3
		checkExpressionOperand3(timestamp, expectedValue, referenceChain);
		//check value4
		checkExpressionOperand4(timestamp, expectedValue, referenceChain);
	}

	/** 
	 * Checks the 1st operand
	 * inout universal charstring
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 */
	private void checkExpressionOperand1( final CompilationTimeStamp timestamp,
										  final Expected_Value_type expectedValue,
										  final IReferenceChain referenceChain ) {
		if (reference1 == null) {
			setIsErroneous(true);
			return;
		}

		final Assignment temporalAssignment = reference1.getRefdAssignment(timestamp, true);

		if (temporalAssignment == null) {
			setIsErroneous(true);
			return;
		}

		switch (temporalAssignment.getAssignmentType()) {
		case A_CONST:
		case A_EXT_CONST:
		case A_MODULEPAR:
		case A_TEMPLATE:
			reference1.getLocation().reportSemanticError(
					MessageFormat.format("Reference to `{0}'' cannot be used as the first operand of the `decvalue'' operation",
							temporalAssignment.getAssignmentName()));
			setIsErroneous(true);
			break;
		case A_VAR:
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
			break;
		case A_VAR_TEMPLATE:
		case A_PAR_TEMP_IN:
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT: {
			Referenced_Template template = new Referenced_Template(reference1);
			template.setMyScope(getMyScope());
			template.setFullNameParent(new BridgingNamedNode(this, ".<operand>"));
			ITTCN3Template last = template.getTemplateReferencedLast(timestamp);
			if (!Template_type.SPECIFIC_VALUE.equals(last.getTemplatetype()) && last != template) {
				reference1.getLocation().reportSemanticError(
						MessageFormat.format("Specific value template was expected instead of `{0}''",
								last.getTemplateTypeName()));
				setIsErroneous(true);
				return;
			}
			break;
		}
		default:
			reference1.getLocation().reportSemanticError(
					MessageFormat.format("Reference to `{0}'' cannot be used as the first operand of the `decvalue' operation",
							temporalAssignment.getAssignmentName()));
			setIsErroneous(true);
			return;
		}

		final IType temporalType = temporalAssignment.getType(timestamp).getFieldType(timestamp, reference1, 1,
				Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		if (temporalType == null) {
			setIsErroneous(true);
			return;
		}
		final Type_type type_type = temporalType.getTypeRefdLast(timestamp).getTypetype();  
		if ( type_type != Type_type.TYPE_UCHARSTRING ||
			 type_type  != Type_type.TYPE_UCHARSTRING) {
			if (!isErroneous) {
				location.reportSemanticError(OPERAND1_ERROR1);
				setIsErroneous(true);
			}
			return;
		}
	}
	
	/** 
	 * Checks the 2nd operand
	 * out any_type
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 */
	private void checkExpressionOperand2( final CompilationTimeStamp timestamp,
										  final Expected_Value_type expectedValue,
										  final IReferenceChain referenceChain ) {
		if (reference1 == null) {
			setIsErroneous(true);
			return;
		}

		final Assignment temporalAssignment = reference2.getRefdAssignment(timestamp, true);

		if (temporalAssignment == null) {
			setIsErroneous(true);
			return;
		}

		IType temporalType = temporalAssignment.getType(timestamp).getFieldType(timestamp, reference2, 1,
				Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		if (temporalType == null) {
			setIsErroneous(true);
			return;
		}
		temporalType = temporalType.getTypeRefdLast(timestamp);
		switch (temporalType.getTypetype()) {
		case TYPE_UNDEFINED:
		case TYPE_NULL:
		case TYPE_REFERENCED:
		case TYPE_VERDICT:
		case TYPE_PORT:
		case TYPE_COMPONENT:
		case TYPE_DEFAULT:
		case TYPE_SIGNATURE:
		case TYPE_FUNCTION:
		case TYPE_ALTSTEP:
		case TYPE_TESTCASE:
			reference2.getLocation().reportSemanticError(OPERAND2_ERROR1);
			setIsErroneous(true);
			break;
		default:
			break;
		}
	}
	
	/** 
	 * Checks the 3rd operand
	 * in charstring (optional)
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 */
	private void checkExpressionOperand3( final CompilationTimeStamp timestamp,
										  final Expected_Value_type expectedValue,
										  final IReferenceChain referenceChain ) {
		if (value3 == null) {
			return;
		}

		value3.setLoweridToReference(timestamp);
		final Type_type tempType = value3.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType) {
		case TYPE_CHARSTRING:
			IValue last = value3.getValueRefdLast(timestamp, expectedValue, referenceChain);
			if (!last.isUnfoldable(timestamp)) {
				final String originalString = ((Charstring_Value) last).getValue();
				CharstringExtractor cs = new CharstringExtractor( originalString );
				if ( cs.isErrorneous() ) {
					value3.getLocation().reportSemanticError( cs.getErrorMessage() );
					setIsErroneous(true);
				}
			}

			break;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			if (!isErroneous) {
				location.reportSemanticError(OPERAND3_ERROR1);
				setIsErroneous(true);
			}
			break;
		}
	}
	
	/** 
	 * Checks the 4th operand
	 * universal charstring (optional)
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 */
	private void checkExpressionOperand4( final CompilationTimeStamp timestamp,
										  final Expected_Value_type expectedValue,
										  final IReferenceChain referenceChain ) {
		if (value4 == null) {
			return;
		}

		value4.setLoweridToReference(timestamp);
		final Type_type tempType = value4.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType) {
		case TYPE_CHARSTRING:
		case TYPE_UCHARSTRING:
			break;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			break;
		default:
			if (!isErroneous) {
				location.reportSemanticError(OPERAND4_ERROR1);
				setIsErroneous(true);
			}
			break;
		}
	}
	
	@Override
	public IValue evaluateValue(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return lastValue;
		}

		isErroneous = false;
		lastTimeChecked = timestamp;
		lastValue = this;

		if (reference1 != null) {
			checkExpressionOperands(timestamp, expectedValue, referenceChain);
		}

		return lastValue;
	}

	/**
	 * Helper function checking if a provided reference is in a recursive
	 * reference chain or not.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference to check for recursion.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references,
	 *                must not be null.
	 * */
	private void checkRecursionHelper(final CompilationTimeStamp timestamp, final Reference reference, final IReferenceChain referenceChain) {
		Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			setIsErroneous(true);
			return;
		}

		switch (assignment.getAssignmentType()) {
		case A_CONST:
		case A_EXT_CONST:
		case A_MODULEPAR:
		case A_VAR:
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT: {
			Referenced_Value value = new Referenced_Value(reference);
			value.setMyScope(getMyScope());
			value.setFullNameParent(this);

			referenceChain.markState();
			value.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
			break;
		}
		case A_TEMPLATE:
		case A_VAR_TEMPLATE:
		case A_PAR_TEMP_IN:
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT: {
			Referenced_Template template = new Referenced_Template(reference1);
			template.setMyScope(getMyScope());
			template.setFullNameParent(this);

			referenceChain.markState();
			template.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
			break;
		}
		default:
			// remain silent, the error was already detected and
			// reported
			break;
		}
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			checkRecursionHelper(timestamp, reference1, referenceChain);
			checkRecursionHelper(timestamp, reference2, referenceChain);
			if (value3 != null) {
				referenceChain.markState();
				value3.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
			if (value4 != null) {
				referenceChain.markState();
				value4.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (reference1 != null) {
			reference1.updateSyntax(reparser, false);
			reparser.updateLocation(reference1.getLocation());
		}
		if (reference2 != null) {
			reference2.updateSyntax(reparser, false);
			reparser.updateLocation(reference2.getLocation());
		}

		if (value3 != null) {
			value3.updateSyntax(reparser, false);
			reparser.updateLocation(value3.getLocation());
		}
		
		if (value4 != null) {
			value4.updateSyntax(reparser, false);
			reparser.updateLocation(value4.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (reference1 != null) {
			reference1.findReferences(referenceFinder, foundIdentifiers);
		}
		if (reference2 != null) {
			reference2.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value3 != null) {
			value3.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value4 != null) {
			value4.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (reference1 != null && !reference1.accept(v)) {
			return false;
		}
		if (reference2 != null && !reference2.accept(v)) {
			return false;
		}
		if (value3 != null && !value3.accept(v)) {
			return false;
		}
		if (value4 != null && !value4.accept(v)) {
			return false;
		}
		return true;
	}
}
