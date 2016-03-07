/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.SequenceOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SetOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents the BNF element "AllElementsFrom" Ref: ttcn3 standard
 * "ETSI ES 201 873-1 V4.6.1 (2014-06)" A.1.6.1.3 Template definitions/127.
 * 
 * @author Jeno Balasko
 *
 */
public class AllElementsFrom extends TemplateBody {

	private static final String SPECIFICVALUEEXPECTED = "After all from a specific value is expected";
	private static final String LISTEXPECTED = "After all from a variable or a template of list type is expected";
	private static final String TYPEMISMATCH = "Type mismatch: `{0}'' was expected in the list";
	private static final String REFERENCEEXPECTED = "Reference to a value was expected";
	private static final String ANYOROMITANDPERMUTATIONPRHOHIBITED = "`all from' can not refer to a template containing permutation or AnyElementsOrNone";
	/**
	 * myGovernor is the governor of AllElementsFrom. It is the type/governor of
	 * the elements/items of its templates which shall be a sequence.
	 * 
	 */
	private IType myGovernor;

	public AllElementsFrom() {
		super();
	}

	public AllElementsFrom(TTCN3Template t) {
		template = t;
		// template shall be a specific value & a reference
		// element type => check function
		// These features are checked by checkThisTemplateGeneric()
	}

	@Override
	public IType getMyGovernor() {
		return myGovernor;
	}

	@Override
	public void setMyGovernor(IType governor) {
		myGovernor = governor;
	}

	@Override
	public void checkThisTemplateGeneric(CompilationTimeStamp timestamp, IType type, boolean isModified, boolean allowOmit,
			boolean allowAnyOrOmit, boolean subCheck, boolean implicitOmit) {

		if (template == null) {
			ErrorReporter.INTERNAL_ERROR();
			return;
		}

		if (!Template_type.SPECIFIC_VALUE.equals(template.getTemplatetype())) {
			template.getLocation().reportSemanticError(SPECIFICVALUEEXPECTED);
			template.setIsErroneous(true);
			return;
		}

		if (!((SpecificValue_Template) template).isReference()) {
			template.getLocation().reportSemanticError(REFERENCEEXPECTED);
			template.setIsErroneous(true);
			return;
		}

		// isReference branch:
		Reference reference = ((SpecificValue_Template) template).getReference();
		Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			template.getLocation().reportSemanticError("Assignment not found");
			template.setIsErroneous(true);
			return;
		}

		// ES 201 873-1 - V4.7.1 B.1.2.1.a:
		// The type of the template list and the member type of the template in
		// the all from clause shall be
		// compatible.
		IType assType = assignment.getType(timestamp);

		if (assType != null) {

			IType atype = assType.getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
			if (atype == null) {
				template.setIsErroneous(true);
				return;
			}

			IType referredType = atype.getTypeRefdLast(timestamp);
			IType it = null; // type of the fields of the sequence/set
			if (referredType != null) {
				switch (referredType.getTypetype()) {
				case TYPE_SEQUENCE_OF:
					it = ((SequenceOf_Type) referredType).getOfType();
					break;
				case TYPE_SET_OF:
					it = ((SetOf_Type) referredType).getOfType();
					break;
				case TYPE_TTCN3_SEQUENCE:
					// it = ((TTCN3_Sequence_Type) rt).getFieldType(timestamp,
					// reference, actualSubReference, expectedIndex,
					// interruptIfOptional)
					break;
				default:
					template.getLocation().reportSemanticError(LISTEXPECTED);
					template.setIsErroneous(true);
				}
			}

			if (it != null) {
				if (!it.isCompatible(timestamp, type, null, null, null)) {
					template.getLocation().reportSemanticError(MessageFormat.format(TYPEMISMATCH, type.getTypename()));
					template.setIsErroneous(true);
				}
			}

		}

		// ES 201 873-1 - V4.7.1 B.1.2.1.
		// b) The template in the all from clause as a whole shall not resolve
		// into a matching mechanism (i.e. its
		// elements may contain any of the matching mechanisms or matching
		// attributes with the exception of those
		// described in the following restriction).
		// c) Individual fields of the template in the all from clause shall not
		// resolve to any of the following matching
		// mechanisms: AnyElementsOrNone, permutation
		ITTCN3Template body = null;
		IValue value = null;
		switch (assignment.getAssignmentType()) {
		case A_TEMPLATE:
			body = ((Def_Template) assignment).getTemplate(timestamp);
			break;
		case A_VAR_TEMPLATE:
			body = ((Def_Var_Template) assignment).getInitialValue();
			break;
		case A_CONST:
			break;
		case A_MODULEPAR:
			value = ((Def_ModulePar) assignment).getDefaultValue();
			break;
		case A_MODULEPAR_TEMPLATE:
			body = ((Def_ModulePar_Template) assignment).getDefaultTemplate();
			break;
		case A_VAR:
			value = ((Def_Var) assignment).getInitialValue();
			break;
		default:
			return;
		}

		//it is too complex to analyse anyoromit. Perhaps it can be omit
		
		if (body != null) {
			
			switch (body.getTemplatetype()) {
			case TEMPLATE_LIST: 
				//TODO: if "all from" is in a permutation list it anyoromit and any is permitted
				if (!allowAnyOrOmit && ((Template_List) body).containsAnyornoneOrPermutation()) {
					template.getLocation().reportSemanticError(ANYOROMITANDPERMUTATIONPRHOHIBITED);
					template.setIsErroneous(true);
				}
				break;
			case NAMED_TEMPLATE_LIST:
				((Named_Template_List) body).checkSpecificValue(timestamp, true);
				break;
			case SPECIFIC_VALUE:
				break;
			default:				
				template.getLocation().reportSemanticError(LISTEXPECTED);
				template.setIsErroneous(true);
				return;
			}
			
		}

//		if (value != null) {
//			// TODO
//			return;
//		}

	}

	// @Override
	public Template_type getTemplatetype() {
		return Template_type.ALLELEMENTSFROM;
	}

	@Override
	public String getTemplateTypeName() {
		return "all from ".concat(template.getTemplateTypeName());
	}

	@Override
	public boolean isValue(CompilationTimeStamp timestamp) {
		return false;
	}

	/**
	 * Gets the number of values If the value is type of SEQUENCEOF_VALUE or
	 * type of SETOF_VALUE then returns their size otherwise returns 1
	 */
	private int getNofValues(final IValue value, final CompilationTimeStamp timestamp) {
		int result = 0;
		if (value == null) {
			return result;
		}
		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue lastValue = value.getValueRefdLast(timestamp, chain);
		chain.release();
		if (lastValue.getIsErroneous(timestamp)) {
			return result;
		}
		if (Value_type.SEQUENCEOF_VALUE.equals(lastValue.getValuetype())) {
			SequenceOf_Value lvalue = (SequenceOf_Value) lastValue;
			result = lvalue.getNofComponents();
			return result;
		} else if (Value_type.SETOF_VALUE.equals(lastValue.getValuetype())) {
			SetOf_Value svalue = (SetOf_Value) lastValue;
			result = svalue.getNofComponents();
			return result;
		} else {
			return 1; // this value is calculated as 1 in an all from
		}
	}

	/**
	 * Calculates the number of list members which are not the any or none
	 * symbol.
	 *
	 * @return the number calculated.
	 * */
	public int getNofTemplatesNotAnyornone(CompilationTimeStamp timestamp) {
		int result = 0;
		if (template == null) {
			ErrorReporter.INTERNAL_ERROR();
			return result;
		}

		if (!Template_type.SPECIFIC_VALUE.equals(template.getTemplatetype())) {
			template.getLocation().reportSemanticError(REFERENCEEXPECTED);
			template.setIsErroneous(true);
			return result;
		}

		if (!((SpecificValue_Template) template).isReference()) {
			template.getLocation().reportSemanticError(REFERENCEEXPECTED);
			template.setIsErroneous(true);
			return result;
		}

		// isReference branch:
		Reference reference = ((SpecificValue_Template) template).getReference();
		Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			template.getLocation().reportSemanticError("Assignment not found");
			template.setIsErroneous(true);
			return result;
		}

		ITTCN3Template body = null;

		switch (assignment.getAssignmentType()) {
		case A_TEMPLATE:
			body = ((Def_Template) assignment).getTemplate(timestamp);
			break;
		case A_VAR_TEMPLATE:
			body = ((Def_Var_Template) assignment).getInitialValue();
			break;
		case A_CONST:
			IValue value = ((Def_Const) assignment).getValue();
			return getNofValues(value, timestamp);
		case A_MODULEPAR:
			IValue mvalue = ((Def_ModulePar) assignment).getDefaultValue();
			return getNofValues(mvalue, timestamp);
		case A_MODULEPAR_TEMPLATE:
			body = ((Def_ModulePar_Template) assignment).getDefaultTemplate();
			break;
		default:
			return result;
		}
		if (body == null) {
			ErrorReporter.INTERNAL_ERROR();
			return result;
		}
		if (!Template_type.TEMPLATE_LIST.equals(body.getTemplatetype())) {
			template.getLocation().reportSemanticError("Template must be a record of or a set of values");
			template.setIsErroneous(true);
			return result;
		}
		result = ((Template_List) body).getNofTemplatesNotAnyornone(timestamp);
		return result;
	}

}
