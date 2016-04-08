/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Values;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a list of templates.
 * 
 * @author Kristof Szabados
 * */
public final class Template_List extends CompositeTemplate {
	/** Indicates whether the embedded templates contain PERMUTATION_MATCH. */
	//private boolean hasPermutation = false;

	// cache storing the value form of this list of templates if already
	// created, or null
	private SequenceOf_Value asValue = null;

	public Template_List(final ListOfTemplates templates) {
		super(templates);

//		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
//			if (Template_type.PERMUTATION_MATCH.equals(templates.getTemplateByIndex(i).getTemplatetype())) {
//				hasPermutation = true;
//			}
//		}
	}

	@Override
	public Template_type getTemplatetype() {
		return Template_type.TEMPLATE_LIST;
	}

	@Override
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous value list notation";
		}

		return "value list notation";
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append("{ ");
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			ITTCN3Template template = templates.getTemplateByIndex(i);
			builder.append(template.createStringRepresentation());
		}
		builder.append(" }");

		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}

		return builder.toString();
	}

	@Override
	public TTCN3Template setTemplatetype(final CompilationTimeStamp timestamp, final Template_type newType) {
		switch (newType) {
		case NAMED_TEMPLATE_LIST:
			return Named_Template_List.convert(timestamp, this);
		default:
			return super.setTemplatetype(timestamp, newType);
		}
	}

	@Override
	protected ITTCN3Template getReferencedArrayTemplate(final CompilationTimeStamp timestamp, final IValue arrayIndex,
			final IReferenceChain referenceChain) {
		IValue indexValue = arrayIndex.setLoweridToReference(timestamp);
		indexValue = indexValue.getValueRefdLast(timestamp, referenceChain);
		if (indexValue.getIsErroneous(timestamp)) {
			return null;
		}

		long index = 0;
		if (!indexValue.isUnfoldable(timestamp)) {
			if (Value_type.INTEGER_VALUE.equals(indexValue.getValuetype())) {
				index = ((Integer_Value) indexValue).getValue();
			} else {
				arrayIndex.getLocation().reportSemanticError("An integer value was expected as index");
				return null;
			}
		} else {
			return null;
		}

		IType tempType = myGovernor.getTypeRefdLast(timestamp);
		if (tempType.getIsErroneous(timestamp)) {
			return null;
		}

		switch (tempType.getTypetype()) {
		case TYPE_SEQUENCE_OF: {
			if (index < 0) {
				final String message = MessageFormat
						.format("A non-negative integer value was expected instead of {0} for indexing a template of `sequence of'' type `{1}''",
								index, tempType.getTypename());
				arrayIndex.getLocation().reportSemanticError(message);
				return null;
			}

			int nofElements = getNofTemplates();
			if (index > nofElements) {
				final String message = MessageFormat
						.format("Index overflow in a template of `sequence of'' type `{0}'': the index is {1}, but the template has only {2} elements",
								tempType.getTypename(), index, nofElements);
				arrayIndex.getLocation().reportSemanticError(message		);
				return null;
			}
			break;
		}
		case TYPE_SET_OF: {
			if (index < 0) {
				final String message = MessageFormat
						.format("A non-negative integer value was expected instead of {0} for indexing a template of `set of'' type `{1}''",
								index, tempType.getTypename());
				arrayIndex.getLocation().reportSemanticError(message);
				return null;
			}

			int nofElements = getNofTemplates();
			if (index > nofElements) {
				final String message = MessageFormat
						.format("Index overflow in a template of `set of'' type `{0}'': the index is {1}, but the template has only {2} elements",
								tempType.getTypename(), index, nofElements);
				arrayIndex.getLocation().reportSemanticError(message);
				return null;
			}
			break;
		}
		case TYPE_ARRAY: {
			ArrayDimension dimension = ((Array_Type) tempType).getDimension();
			dimension.checkIndex(timestamp, indexValue, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (!dimension.getIsErroneous(timestamp)) {
				// re-base the index
				index -= dimension.getOffset();
				if (index < 0 || index > getNofTemplates()) {
					arrayIndex.getLocation().reportSemanticError(
							MessageFormat.format("The index value {0} is outside the array indexable range", index
									+ dimension.getOffset()));
					return null;
				}
			} else {
				return null;
			}
			break;
		}
		default:{
			final String message = MessageFormat.format("Invalid array element reference: type `{0}'' cannot be indexed",
					tempType.getTypename());
			arrayIndex.getLocation().reportSemanticError(message);
			return null;
		}
		}

		ITTCN3Template returnValue = getTemplateByIndex((int) index);
		if (Template_type.TEMPLATE_NOTUSED.equals(returnValue.getTemplatetype())) {
			if (baseTemplate != null) {
				return baseTemplate.getTemplateReferencedLast(timestamp, referenceChain).getReferencedArrayTemplate(timestamp,
						indexValue, referenceChain);
			}

			return null;
		}

		return returnValue;
	}

	@Override
	public boolean isValue(final CompilationTimeStamp timestamp) {
		if (lengthRestriction != null || isIfpresent || getIsErroneous(timestamp)) {
			return false;
		}

		for (int i = 0, size = getNofTemplates(); i < size; i++) {
			if (!templates.getTemplateByIndex(i).isValue(timestamp)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public IValue getValue() {
		if (asValue != null) {
			return asValue;
		}

		Values values = new Values(false);
		for (int i = 0, size = getNofTemplates(); i < size; i++) {
			values.addValue(templates.getTemplateByIndex(i).getValue());
		}

		asValue = new SequenceOf_Value(values);
		asValue.setLocation(getLocation());
		asValue.setMyScope(getMyScope());
		asValue.setFullNameParent(getNameParent());

		return asValue;
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			templates.getTemplateByIndex(i).checkSpecificValue(timestamp, true);
		}
	}

	@Override
	protected void checkTemplateSpecificLengthRestriction(final CompilationTimeStamp timestamp, final Type_type typeType) {
		if (Type_type.TYPE_SEQUENCE_OF.equals(typeType) || Type_type.TYPE_SET_OF.equals(typeType)) {
			int nofTemplatesGood = getNofTemplatesNotAnyornone(timestamp); //at least !
			
			boolean hasAnyOrNone = templateContainsAnyornone();

			lengthRestriction.checkNofElements(timestamp, nofTemplatesGood, hasAnyOrNone, false, hasAnyOrNone, this);
		}
	}

	@Override
	public boolean checkValueomitRestriction(final CompilationTimeStamp timestamp, final String definitionName, final boolean omitAllowed, final Location usageLocation) {
		if (omitAllowed) {
			checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_OMIT, usageLocation);
		} else {
			checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_VALUE, usageLocation);
		}

		boolean needsRuntimeCheck = false;
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			if (templates.getTemplateByIndex(i).checkValueomitRestriction(timestamp, definitionName, true, usageLocation)) {
				needsRuntimeCheck = true;
			}
		}
		return needsRuntimeCheck;
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (lengthRestriction != null) {
			lengthRestriction.findReferences(referenceFinder, foundIdentifiers);
		}

		if (asValue != null) {
			asValue.findReferences(referenceFinder, foundIdentifiers);
		} else if (templates != null) {
			templates.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (asValue != null) {
			if (!asValue.accept(v)) {
				return false;
			}
		} else if (templates != null) {
			if (!templates.accept(v)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected String getNameForStringRep() {
		return "";
	}
}
