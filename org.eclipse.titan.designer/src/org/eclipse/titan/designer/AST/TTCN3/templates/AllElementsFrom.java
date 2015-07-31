/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.SequenceOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SetOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Sequence_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents the BNF element "AllElementsFrom" Ref: ttcn3 standard
 * "ETSI ES 201 873-1 V4.6.1 (2014-06)" A.1.6.1.3 Template definitions/127.
 * 
 * @author Jeno Balasko
 *
 */
public class AllElementsFrom extends TemplateBody {

	private static final String LISTEXPECTED = "After all from a variable or a template of list type is expected";
	private static final String TYPEMISMATCH = "Type mismatch error";
	private static final String REFERENCEEXPECTED = "Reference to a value was expected";
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
			template.getLocation().reportSemanticError(REFERENCEEXPECTED);
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
		IType assType = assignment.getType(timestamp);

		boolean compatibility_ok = false;

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
					//it = ((TTCN3_Sequence_Type) rt).getFieldType(timestamp, reference, actualSubReference, expected_index, interrupt_if_optional)
					break;
				default:
					//TODO: not handled yet, avoiding false negative error message
					//return;
					template.getLocation().reportSemanticError(LISTEXPECTED);
					template.setIsErroneous(true);
				}
			}

			if (it != null) {
				compatibility_ok = it.isCompatible(timestamp, type, null, null, null);
			}

		}

		if (!compatibility_ok) {
			template.getLocation().reportSemanticError(TYPEMISMATCH);
			template.setIsErroneous(true);
		}

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
}
