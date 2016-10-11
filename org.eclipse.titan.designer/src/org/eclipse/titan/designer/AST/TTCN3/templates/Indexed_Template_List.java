/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.IndexedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Values;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a list of templates given with indexed notation.
 * 
 * @author Kristof Szabados
 * */
public final class Indexed_Template_List extends TTCN3Template {

	private final IndexedTemplates indexedTemplates;

	private boolean hasPermutation = false;

	// cache storing the value form of this if already created, or null
	private SequenceOf_Value asValue = null;

	public Indexed_Template_List(final IndexedTemplates indexedTemplates) {
		this.indexedTemplates = indexedTemplates;

		indexedTemplates.setFullNameParent(this);
		for (int i = 0, size = indexedTemplates.getNofTemplates(); i < size; i++) {
			if (Template_type.PERMUTATION_MATCH.equals(indexedTemplates.getTemplateByIndex(i).getTemplate().getTemplatetype())) {
				hasPermutation = true; 
				break;
			}
		}
	}

	@Override
	public Template_type getTemplatetype() {
		return Template_type.INDEXED_TEMPLATE_LIST;
	}

	@Override
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous indexed assignment notation";
		}

		return "indexed assignment notation";
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append("{ ");
		for (int i = 0, size = indexedTemplates.getNofTemplates(); i < size; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			IndexedTemplate indexedTemplate = indexedTemplates.getTemplateByIndex(i);
			builder.append(" [").append(indexedTemplate.getIndex().getValue().createStringRepresentation());
			builder.append("] := ");
			builder.append(indexedTemplate.getTemplate().createStringRepresentation());
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
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = indexedTemplates.getNofTemplates(); i < size; i++) {
			IndexedTemplate template = indexedTemplates.getTemplateByIndex(i);
			if (template == child) {
				IValue index = template.getIndex().getValue();
				return builder.append(INamedNode.SQUAREOPEN).append(index.createStringRepresentation())
						.append(INamedNode.SQUARECLOSE);
			}
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (indexedTemplates != null) {
			indexedTemplates.setMyScope(scope);
		}
	}

	/** @return the number of templates in the list */
	public int getNofTemplates() {
		if (indexedTemplates == null) {
			return 0;
		}

		return indexedTemplates.getNofTemplates();
	}

	/**
	 * @param index
	 *                the index of the element to return.
	 *
	 * @return the template on the indexed position.
	 * */
	public IndexedTemplate getIndexedTemplateByIndex(final int index) {
		return indexedTemplates.getTemplateByIndex(index);
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
		default:
			arrayIndex.getLocation()
					.reportSemanticError(
							MessageFormat.format("Invalid array element reference: type `{0}'' cannot be indexed",
									tempType.getTypename()));
			return null;
		}

		for (int i = 0, size = indexedTemplates.getNofTemplates(); i < size; i++) {
			IndexedTemplate template = indexedTemplates.getTemplateByIndex(i);
			IValue lastValue = template.getIndex().getValue();

			IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			lastValue = lastValue.getValueRefdLast(timestamp, chain);
			chain.release();

			if (Value_type.INTEGER_VALUE.equals(lastValue.getValuetype())) {
				long tempIndex = ((Integer_Value) lastValue).getValue();
				if (index == tempIndex) {
					ITTCN3Template realTemplate = template.getTemplate();
					if (Template_type.TEMPLATE_NOTUSED.equals(realTemplate.getTemplatetype())) {
						if (baseTemplate != null) {
							return baseTemplate.getTemplateReferencedLast(timestamp, referenceChain)
									.getReferencedArrayTemplate(timestamp, indexValue, referenceChain);
						}

						return null;
					}

					return realTemplate;
				}
			}
		}

		switch (tempType.getTypetype()) {
		case TYPE_SEQUENCE_OF:
		case TYPE_SET_OF:
		case TYPE_ARRAY:
			arrayIndex.getLocation().reportSemanticError(
					MessageFormat.format("No elements were found with the index {0} in the referenced template", index));
			break;
		default:
			// the error was reported earlier
			break;
		}

		return null;
	}

	@Override
	public boolean isValue(final CompilationTimeStamp timestamp) {
		if (lengthRestriction != null || isIfpresent || getIsErroneous(timestamp)) {
			return false;
		}

		for (int i = 0, size = getNofTemplates(); i < size; i++) {
			if (!indexedTemplates.getTemplateByIndex(i).getTemplate().isValue(timestamp)) {
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

		Values values = new Values(true);
		for (int i = 0, size = getNofTemplates(); i < size; i++) {
			IndexedTemplate indexedTemplate = indexedTemplates.getTemplateByIndex(i);
			IndexedValue indexedValue = new IndexedValue(indexedTemplate.getIndex(), indexedTemplate.getTemplate().getValue());
			indexedValue.setLocation(indexedTemplate.getLocation());
			values.addIndexedValue(indexedValue);
		}
		asValue = new SequenceOf_Value(values);
		asValue.setLocation(getLocation());
		asValue.setMyScope(getMyScope());
		asValue.setFullNameParent(getNameParent());
		return asValue;
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		ITTCN3Template temp;
		for (int i = 0, size = getNofTemplates(); i < size; i++) {
			temp = indexedTemplates.getTemplateByIndex(i).getTemplate();
			if (temp != null) {
				temp.checkSpecificValue(timestamp, true);
			}
		}
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			for (int i = 0, size = indexedTemplates.getNofTemplates(); i < size; i++) {
				IndexedTemplate template = indexedTemplates.getTemplateByIndex(i);
				if (template != null) {
					referenceChain.markState();
					template.getTemplate().checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
		}
	}

	@Override
	public boolean checkValueomitRestriction(final CompilationTimeStamp timestamp, final String definitionName, final boolean omitAllowed, final Location usageLocation) {
		if (omitAllowed) {
			checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_OMIT, usageLocation);
		} else {
			checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_VALUE, usageLocation);
		}

		for (int i = 0, size = indexedTemplates.getNofTemplates(); i < size; i++) {
			indexedTemplates.getTemplateByIndex(i).getTemplate().checkValueomitRestriction(timestamp, definitionName, true, usageLocation);
		}

		// complete check was not done, always needs runtime check
		return true;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		indexedTemplates.updateSyntax(reparser, false);
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (asValue != null) {
			asValue.findReferences(referenceFinder, foundIdentifiers);
		} else if (indexedTemplates != null) {
			indexedTemplates.findReferences(referenceFinder, foundIdentifiers);
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
		} else if (indexedTemplates != null) {
			if (!indexedTemplates.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
