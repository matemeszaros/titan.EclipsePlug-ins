/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TemporalReference;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ExternalConst;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ValueList_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Sequence_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReparseUtilities;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Assignment_Statement extends Statement {
	private static final String FULLNAMEPART = ".assignment";
	private static final String TEMPLATEASSIGNMENTTOVALUE = "A template body with matching symbols cannot be assigned to a variable";
	private static final String VARIABLEREFERENCEEXPECTED = "Reference to a variable or template variable was expected instead of `{0}''";
	private static final String OMITTOMANDATORYASSIGNMENT1 = "Omit value can only be assigned to an optional field of a record or set value";
	private static final String OMITTOMANDATORYASSIGNMENT2 = "Assignment of `omit'' to mandatory field `{0}'' of type `{1}''";
	private static final String STATEMENT_NAME = "assignment";

	private final Reference reference;
	private final TTCN3Template template;

	public Assignment_Statement(final Reference reference, final TTCN3Template template) {
		this.reference = reference;
		this.template = template;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
		if (template != null) {
			template.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_ASSIGNMENT;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (reference == child) {
			return builder.append(FULLNAMEPART);
		} else if (template == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference != null) {
			reference.setMyScope(scope);
		}
		if (template != null) {
			template.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		isErroneous = false;

		if (reference == null) {
			return;
		}

		reference.setUsedOnLeftHandSide();
		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null || assignment.getIsErroneous()) {
			isErroneous = true;
			return;
		}

		if (template == null) {
			return;
		}

		switch (assignment.getAssignmentType()) {
		case A_PAR_VAL_IN:
			((FormalParameter) assignment).useAsLValue(reference);
			if (template.isValue(timestamp)) {
				final IValue temporalValue = template.getValue();
				checkVarAssignment(timestamp, assignment, temporalValue);
			} else {
				template.getLocation().reportSemanticError(TEMPLATEASSIGNMENTTOVALUE);
				template.setIsErroneous(true);
			}
			break;
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
		case A_PAR_VAL:
			((FormalParameter) assignment).setWritten();
			if (template.isValue(timestamp)) {
				final IValue temporalValue = template.getValue();
				checkVarAssignment(timestamp, assignment, temporalValue);
				break;
			} else {
				if( Template_type.VALUE_LIST.equals(template.getTemplatetype()) && ((ValueList_Template) template).getNofTemplates() == 1) {
					break;
				} else {
					template.getLocation().reportSemanticError(TEMPLATEASSIGNMENTTOVALUE);
					template.setIsErroneous(true);
					return;
				}
			}
		case A_VAR:
			((Def_Var) assignment).setWritten();
			if (template.isValue(timestamp)) {
				final 	IValue temporalValue = template.getValue();
				checkVarAssignment(timestamp, assignment, temporalValue);
				break;
			} else {
				if( Template_type.VALUE_LIST.equals(template.getTemplatetype()) && ((ValueList_Template) template).getNofTemplates() == 1) {
					break;
				} else {
					template.getLocation().reportSemanticError(TEMPLATEASSIGNMENTTOVALUE);
					template.setIsErroneous(true);
					return;
				}
			}
		case A_PAR_TEMP_IN:
			((FormalParameter) assignment).useAsLValue(reference);
			checkTemplateAssignment(timestamp, assignment);
			break;
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT:
			((FormalParameter) assignment).setWritten();
			checkTemplateAssignment(timestamp, assignment);
			break;
		case A_VAR_TEMPLATE:
			((Def_Var_Template) assignment).setWritten();
			checkTemplateAssignment(timestamp, assignment);
			break;
		default:
			reference.getLocation().reportSemanticError(MessageFormat.format(VARIABLEREFERENCEEXPECTED, assignment.getAssignmentName()));
			reference.setIsErroneous(true);
			isErroneous = true;
		}
	}

	private void checkVarAssignment(final CompilationTimeStamp timestamp, final Assignment assignment, final IValue value) {
		final IType varType = getType(timestamp, assignment);

		if (varType == null || value == null) {
			isErroneous = true;
			return;
		}

		final IType type = varType.getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		if (type == null) {
			isErroneous = true;
			return;
		}

		value.setMyGovernor(type);
		IValue lastValue = type.checkThisValueRef(timestamp, value);

		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		lastValue = lastValue.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (Value_type.OMIT_VALUE.equals(lastValue.getValuetype())) {
			final ISubReference lastReference = reference.removeLastSubReference();

			if (lastReference == null || lastReference.getId() == null) {
				value.getLocation().reportSemanticError(OMITTOMANDATORYASSIGNMENT1);
				isErroneous = true;
				reference.addSubReference(lastReference);
				return;
			}
			final Identifier lastField = lastReference.getId();

			final List<ISubReference> baseReference = reference.getSubreferences(0, reference.getSubreferences().size() - 1);
			reference.addSubReference(lastReference);

			final Reference newReference = new TemporalReference(null, baseReference);
			newReference.clearStringElementReferencing();
			IType baseType = varType.getFieldType(timestamp, newReference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
			if (baseType == null) {
				isErroneous = true;
				return;
			}

			baseType = baseType.getTypeRefdLast(timestamp);
			if (baseType.getIsErroneous(timestamp)) {
				isErroneous = true;
				return;
			}

			CompField componentField;
			switch (baseType.getTypetype()) {
			case TYPE_TTCN3_SEQUENCE:
				componentField = ((TTCN3_Sequence_Type) baseType).getComponentByName(lastField.getName());
				if (componentField != null && !componentField.isOptional()) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(OMITTOMANDATORYASSIGNMENT2, lastField.getDisplayName(),
									baseType.getTypename()));
					value.setIsErroneous(true);
				}
				break;
			case TYPE_ASN1_SEQUENCE:
				componentField = ((ASN1_Sequence_Type) baseType).getComponentByName(lastField);
				if (componentField != null && !componentField.isOptional()) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(OMITTOMANDATORYASSIGNMENT2, lastField.getDisplayName(),
									baseType.getTypename()));
					value.setIsErroneous(true);
				}
				break;
			case TYPE_TTCN3_SET:
				componentField = ((TTCN3_Set_Type) baseType).getComponentByName(lastField.getName());
				if (componentField != null && !componentField.isOptional()) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(OMITTOMANDATORYASSIGNMENT2, lastField.getDisplayName(),
									baseType.getTypename()));
					value.setIsErroneous(true);
				}
				break;
			case TYPE_ASN1_SET:
				componentField = ((ASN1_Set_Type) baseType).getComponentByName(lastField);
				if (componentField != null && !componentField.isOptional()) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(OMITTOMANDATORYASSIGNMENT2, lastField.getDisplayName(),
									baseType.getTypename()));
					value.setIsErroneous(true);
				}
				break;
			default:
				value.getLocation().reportSemanticError(OMITTOMANDATORYASSIGNMENT1);//TODO:check this!!!
				value.setIsErroneous(true);
				isErroneous = true;
				break;
			}
		} else {
			final boolean isStringElement = reference.refersToStringElement();
			type.checkThisValue(timestamp, value, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, true, false,
					!isStringElement, false, isStringElement));

			if (isStringElement) {
				// The length of the right hand side should be 1
				final IType lastType = type.getTypeRefdLast(timestamp);
				int stringLength = 1;
				switch (lastType.getTypetype()) {
				case TYPE_BITSTRING:
				case TYPE_BITSTRING_A:
					if (!Value_type.BITSTRING_VALUE.equals(lastValue.getValuetype())) {
						return;
					}

					stringLength = ((Bitstring_Value) lastValue).getValueLength();
					break;
				case TYPE_HEXSTRING:
					if (!Value_type.HEXSTRING_VALUE.equals(lastValue.getValuetype())) {
						lastValue = null;
					} else {
						stringLength = ((Hexstring_Value) lastValue).getValueLength();
					}
					break;
				case TYPE_OCTETSTRING:
					if (!Value_type.OCTETSTRING_VALUE.equals(lastValue.getValuetype())) {
						return;
					}

					stringLength = ((Octetstring_Value) lastValue).getValueLength();
					break;
				case TYPE_CHARSTRING:
				case TYPE_NUMERICSTRING:
				case TYPE_PRINTABLESTRING:
				case TYPE_IA5STRING:
				case TYPE_VISIBLESTRING:
				case TYPE_UTCTIME:
				case TYPE_GENERALIZEDTIME:
					if (!Value_type.CHARSTRING_VALUE.equals(lastValue.getValuetype())) {
						return;
					}

					stringLength = ((Charstring_Value) lastValue).getValueLength();
					break;
				case TYPE_UCHARSTRING:
				case TYPE_UTF8STRING:
				case TYPE_TELETEXSTRING:
				case TYPE_VIDEOTEXSTRING:
				case TYPE_GRAPHICSTRING:
				case TYPE_GENERALSTRING:
				case TYPE_UNIVERSALSTRING:
				case TYPE_BMPSTRING:
				case TYPE_OBJECTDESCRIPTOR:
					if (Value_type.UNIVERSALCHARSTRING_VALUE.equals(lastValue.getValuetype())) {
						stringLength = ((UniversalCharstring_Value) lastValue).getValueLength();
					} else if (Value_type.CHARSTRING_VALUE.equals(lastValue.getValuetype())) {
						stringLength = ((Charstring_Value) lastValue).getValueLength();
					} else {
						return;
					}
					break;
				default:
					lastValue = null;
					return;
				}

				if (stringLength != 1) {
					final String message = MessageFormat
							.format("The length of the string to be assigned to a string element of type `{0}'' should be 1 instead of {1}",
									type.getTypename(), stringLength);
					value.getLocation().reportSemanticError(message);
					value.setIsErroneous(true);
				}
			}
		}
	}

	private void checkTemplateAssignment(final CompilationTimeStamp timestamp, final Assignment assignment) {
		IType type = getType(timestamp, assignment);

		if (type == null) {
			isErroneous = true;
			return;
		}

		type = type.getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		if (type == null) {
			isErroneous = true;
			return;
		}

		template.setMyGovernor(type);
		final ITTCN3Template temporalTemplate = type.checkThisTemplateRef(timestamp, template);
		temporalTemplate.checkThisTemplateGeneric(timestamp, type, false, true, true, true, false);
		final Assignment ass = reference.getRefdAssignment(timestamp, true);
		if (ass != null && ass instanceof Definition) {
			TemplateRestriction.check(timestamp, (Definition) ass, template, reference);
		}

		if (reference.refersToStringElement()) {
			if (!template.isValue(timestamp)) {
				template.getLocation().reportSemanticError(
						TEMPLATEASSIGNMENTTOVALUE);
				template.setIsErroneous(true);
				//isErroneous = true; //????
				return;
			}
		}
	}

	/**
	 * Calculates the type of this assignment.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * 
	 * @return the type of the assignment if it has one, otherwise null
	 * */
	private IType getType(final CompilationTimeStamp timestamp, final Assignment assignment) {
		switch (assignment.getAssignmentType()) {
		case A_CONST:
			return ((Def_Const) assignment).getType(timestamp);
		case A_EXT_CONST:
			return ((Def_ExternalConst) assignment).getType(timestamp);
		case A_VAR:
			return ((Def_Var) assignment).getType(timestamp);
		case A_VAR_TEMPLATE:
			return ((Def_Var_Template) assignment).getType(timestamp);
		case A_TEMPLATE:
			return ((Def_Template) assignment).getType(timestamp);
		case A_MODULEPAR:
			return ((Def_ModulePar) assignment).getType(timestamp);
		case A_MODULEPAR_TEMPLATE:
			return ((Def_ModulePar_Template) assignment).getType(timestamp);
		case A_EXT_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RTEMP:
			return ((Def_ExternalConst) assignment).getType(timestamp);
		case A_FUNCTION_RVAL:
		case A_FUNCTION_RTEMP:
			return ((Def_Function) assignment).getType(timestamp);
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
		case A_PAR_TEMP_IN:
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT:
		case A_PAR_PORT:
		case A_PAR_TIMER:
			return ((FormalParameter) assignment).getType(timestamp);
		default:
			return null;
		}
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		return ReparseUtilities.getAllValidTokenTypes();
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (reference != null) {
			reference.updateSyntax(reparser, false);
			reparser.updateLocation(reference.getLocation());
		}

		if (template != null) {
			template.updateSyntax(reparser, false);
			reparser.updateLocation(template.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (reference != null) {
			reference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (template != null) {
			template.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		if (template != null && !template.accept(v)) {
			return false;
		}
		return true;
	}

	public Reference getReference() {
		return reference;
	}

	public TTCN3Template getTemplate() {
		return template;
	}
}
