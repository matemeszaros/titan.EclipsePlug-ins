/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.GovernedSimple;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferenceChainElement;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.ASN1.types.Open_Type;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.types.Anytype_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Sequence_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TypeFactory;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the templates of the TTCN-3 language.
 * 
 * @author Kristof Szabados
 * */
public abstract class TTCN3Template extends GovernedSimple implements IReferenceChainElement, ITTCN3Template, IIncrementallyUpdateable {
	protected static final String RESTRICTIONERROR = "Restriction on {0} does not allow usage of `{1}''";
	private static final String LENGTHRESTRICTIONERROR = "Restriction on {0} does not allow usage of length restriction";

	/** The type of the template, which also happens to be its governor. */
	protected IType myGovernor;

	/** Length restriction. */
	protected LengthRestriction lengthRestriction;

	/** ifpresent flag. */
	protected boolean isIfpresent;

	/**
	 * Indicates whether it has been verified that the template is free of
	 * matching symbols.
	 */
	protected boolean specificValueChecked;

	/**
	 * Pointer to the base template (or template field) that this template
	 * is derived from. It is set in modified templates (including in-line
	 * modified templates) and only if the compiler is able to determine the
	 * base template. It is null otherwise.
	 */
	protected ITTCN3Template baseTemplate;

	@Override
	public Setting_type getSettingtype() {
		return Setting_type.S_TEMPLATE;
	}

	/**
	 * Copies the general template -ish properties of the template in
	 * parameter to the actual one.
	 * <p>
	 * This function is used to help writing conversion function without
	 * using a generic copy-constructor mechanism.
	 * 
	 * @param original
	 *                the original template, whose properties will be copied
	 * */
	@Override
	public final void copyGeneralProperties(final ITTCN3Template original) {
		location = original.getLocation();
		super.setFullNameParent(original.getNameParent());
		myGovernor = original.getMyGovernor();
		myScope = original.getMyScope();
	}

	/** @return the internal type of the template */
	@Override
	public abstract Template_type getTemplatetype();

	/** @return the name of type of the template. */
	@Override
	public abstract String getTemplateTypeName();

	/**
	 * Gets the governor type.
	 * 
	 * @return the governor type.
	 * */
	@Override
	public final IType getMyGovernor() {
		return myGovernor;
	}

	/**
	 * Sets the governor type.
	 * 
	 * @param governor
	 *                the type to be set.
	 * */
	@Override
	public final void setMyGovernor(final IType governor) {
		myGovernor = governor;
	}

	@Override
	public String chainedDescription() {
		return "template reference: " + getFullName();
	}

	@Override
	public final Location getChainLocation() {
		return getLocation();
	}

	/**
	 * Creates and returns a string representation if the actual template.
	 * 
	 * @return the string representation of the template.
	 * */
	@Override
	public abstract String createStringRepresentation();

	/**
	 * Creates template references from a template that is but a single
	 * word. This can happen if it was not possible to categorize it while
	 * parsing.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * 
	 * @return the reference that this lower identifier was converted to, or
	 *         this template.
	 * */
	@Override
	public ITTCN3Template setLoweridToReference(final CompilationTimeStamp timestamp) {
		return this;
	}

	/**
	 * Sets the length restriction of the template.
	 * 
	 * @param lengthRestriction
	 *                the length restriction to set
	 * */
	@Override
	public final void setLengthRestriction(final LengthRestriction lengthRestriction) {
		if (lengthRestriction != null) {
			this.lengthRestriction = lengthRestriction;
		}
	}

	/** @return the length restriction of the template */
	@Override
	public final LengthRestriction getLengthRestriction() {
		return lengthRestriction;
	}

	@Override
	public final void setIfpresent() {
		isIfpresent = true;
	}

	/** @return the base template of the actual template */
	@Override
	public final ITTCN3Template getBaseTemplate() {
		return baseTemplate;
	}

	/**
	 * Sets the base template.
	 * 
	 * @param baseTemplate
	 *                the template to set as the base template of this
	 *                template.
	 * */
	@Override
	public final void setBaseTemplate(final ITTCN3Template baseTemplate) {
		this.baseTemplate = baseTemplate;
	}

	/**
	 * Checks the condition for the completeness of template body which is a
	 * 'record of' or 'set of' template.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param incompleteAllowed
	 *                tells if incomplete list is allowed in the calling
	 *                context or not.
	 * 
	 * @return the type of completeness, that can be expected from this
	 *         template in the actual context
	 * */
	@Override
	public final Completeness_type getCompletenessConditionSeof(final CompilationTimeStamp timestamp, final boolean incompleteAllowed) {
		if (!incompleteAllowed) {
			return Completeness_type.MUST_COMPLETE;
		}

		if (baseTemplate == null) {
			return Completeness_type.MAY_INCOMPLETE;
		}

		ITTCN3Template temp = baseTemplate.getTemplateReferencedLast(timestamp);
		if (temp.getIsErroneous(timestamp)) {
			return Completeness_type.MAY_INCOMPLETE;
		}

		switch (temp.getTemplatetype()) {
		case TEMPLATE_NOTUSED:
		case ANY_VALUE:
		case ANY_OR_OMIT:
		case TEMPLATE_REFD:
		case TEMPLATE_INVOKE:
		case NAMED_TEMPLATE_LIST:
		case INDEXED_TEMPLATE_LIST:
			return Completeness_type.MAY_INCOMPLETE;
		case TEMPLATE_LIST:
			if (myGovernor == null) {
				return Completeness_type.MAY_INCOMPLETE;
			}

			IType type = myGovernor.getTypeRefdLast(timestamp);
			if (type == null) {
				return Completeness_type.MAY_INCOMPLETE;
			}

			switch (type.getTypetype()) {
			case TYPE_SEQUENCE_OF:
			case TYPE_SET_OF:
				return Completeness_type.PARTIAL;
			default:
				return Completeness_type.MAY_INCOMPLETE;
			}
		default:
			return Completeness_type.MUST_COMPLETE;
		}
	}

	/**
	 * Checks the condition for the completeness of template body which is a
	 * 'record of' or 'set of' template.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param incompleteAllowed
	 *                tells if incomplete list is allowed in the calling
	 *                context or not.
	 * @param fieldName
	 *                the name of the field to check for.
	 * 
	 * @return the type of completeness, that can be expected from this
	 *         template in the actual context
	 * */
	@Override
	public final Completeness_type getCompletenessConditionChoice(final CompilationTimeStamp timestamp, final boolean incompleteAllowed,
			final Identifier fieldName) {
		if (!incompleteAllowed) {
			return Completeness_type.MUST_COMPLETE;
		}

		if (baseTemplate == null) {
			return Completeness_type.MAY_INCOMPLETE;
		}

		ITTCN3Template temp = baseTemplate.getTemplateReferencedLast(timestamp);
		if (temp.getIsErroneous(timestamp)) {
			return Completeness_type.MAY_INCOMPLETE;
		}

		switch (temp.getTemplatetype()) {
		case TEMPLATE_NOTUSED:
		case ANY_VALUE:
		case ANY_OR_OMIT:
		case TEMPLATE_REFD:
		case TEMPLATE_INVOKE:
		case TEMPLATE_LIST:
			return Completeness_type.MAY_INCOMPLETE;
		case NAMED_TEMPLATE_LIST:
			if (((Named_Template_List) temp).hasNamedTemplate(fieldName)) {
				return Completeness_type.MAY_INCOMPLETE;
			}

			return Completeness_type.MUST_COMPLETE;
		default:
			return Completeness_type.MUST_COMPLETE;
		}
	}

	/**
	 * Returns the template referred last in case of a referred template, or
	 * itself in any other case.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * 
	 * @return the actual or the last referred template
	 * */
	@Override
	public final TTCN3Template getTemplateReferencedLast(final CompilationTimeStamp timestamp) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		TTCN3Template result = getTemplateReferencedLast(timestamp, referenceChain);
		referenceChain.release();

		return result;
	}

	/**
	 * Returns the template referred last in case of a referred template, or
	 * itself in any other case.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references
	 * 
	 * @return the actual or the last referred template
	 * */
	@Override
	public TTCN3Template getTemplateReferencedLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		return this;
	}

	/**
	 * Creates a template of the provided type from the actual template if
	 * it is possible.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param newType
	 *                the new template_type the new template should belong
	 *                to.
	 * 
	 * @return the new template of the provided kind if the conversion is
	 *         possible, or this template otherwise.
	 * */
	@Override
	public TTCN3Template setTemplatetype(final CompilationTimeStamp timestamp, final Template_type newType) {
		setIsErroneous(true);
		return this;
	}

	/**
	 * Calculates the return type of the template when used in an
	 * expression.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected.
	 * 
	 * @return the Type_type of the template if it was used in an
	 *         expression.
	 * */
	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_UNDEFINED;
	}

	/**
	 * Calculates the governor of the template when used in an expression.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected.
	 * 
	 * @return the governor of the template if it was used in an expression.
	 * */
	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		return TypeFactory.createType(getExpressionReturntype(timestamp, expectedValue));
	}

	/**
	 * Checks for circular references within embedded templates.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references,
	 *                must not be null.
	 **/
	@Override
	public abstract void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	/**
	 * Checks whether the template (including embedded fields) contains no
	 * matching symbols. Allow_omit is used because omit is allowed in only
	 * in embedded fields.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param allowOmit
	 *                whether the omit value is allowed at this point or
	 *                not.
	 * */
	@Override
	public abstract void checkSpecificValue(final CompilationTimeStamp timestamp, boolean allowOmit);

	/**
	 * Helper function, for checking union typed templates whether field
	 * access is allowed for a given template sub reference or not.
	 * 
	 * @param fieldIdentifier
	 *                the identifier of the field to search for.
	 * @param reference
	 *                the reference to report the error to.
	 * @param type
	 *                the type of the template.
	 * */
	private ITTCN3Template getRefdUnionFieldTemplate(final Identifier fieldIdentifier, final Reference reference, final IType type) {
		if (!Template_type.NAMED_TEMPLATE_LIST.equals(getTemplatetype())) {
			return null;
		}

		Named_Template_List namedList = (Named_Template_List) this;
		if (namedList.getNofTemplates() != 1) {
			// invalid template, the error is already reported
			return null;
		}

		NamedTemplate namedTemplate = namedList.getTemplateByIndex(0);
		if (namedTemplate.getName().equals(fieldIdentifier)) {
			return namedTemplate.getTemplate();
		}

		if (!reference.getUsedInIsbound()) {
			final String message = MessageFormat.format(
					"Reference to inactive field `{0}'' in a template of union type `{1}''. The active field is `{2}''",
					fieldIdentifier.getDisplayName(), type.getTypename(), namedTemplate.getName().getDisplayName());
			reference.getLocation().reportSemanticError(message);
		}

		return null;
	}

	/**
	 * Helper function, for checking set and sequence typed templates
	 * whether field access is allowed for a given template sub reference or
	 * not.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param fieldIdentifier
	 *                the identifier of the field to search for.
	 * @param reference
	 *                the reference to report the error to.
	 * @param referenceChain
	 *                the reference chain use to detect circular references.
	 * */
	private ITTCN3Template getReferencedSetSequenceFieldTemplate(final CompilationTimeStamp timestamp, final Identifier fieldIdentifier,
			final Reference reference, final IReferenceChain referenceChain) {
		if (!Template_type.NAMED_TEMPLATE_LIST.equals(getTemplatetype())) {
			return null;
		}

		Named_Template_List namedList = (Named_Template_List) this;
		if (namedList.hasNamedTemplate(fieldIdentifier)) {
			return namedList.getNamedTemplate(fieldIdentifier).getTemplate();
		} else if (baseTemplate != null) {
			// take the field from the base template
			final TTCN3Template temp = baseTemplate.getTemplateReferencedLast(timestamp, referenceChain);
			if (temp == null) {
				return null;
			}

			return temp.getReferencedFieldTemplate(timestamp, fieldIdentifier, reference, referenceChain);
		} else {
			if (!reference.getUsedInIsbound()) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format("Reference to unbound field `{0}''.", fieldIdentifier.getDisplayName()));
			}

			return null;
		}
	}

	/**
	 * Helper function, checks whether field access is allowed for a given
	 * template sub reference or not.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param fieldIdentifier
	 *                the identifier of the field to search for.
	 * @param reference
	 *                the reference to report the error to.
	 * @param referenceChain
	 *                the reference chain use to detect circular references.
	 * */
	private ITTCN3Template getReferencedFieldTemplate(final CompilationTimeStamp timestamp, final Identifier fieldIdentifier,
			final Reference reference, final IReferenceChain referenceChain) {
		switch (getTemplatetype()) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
		case SUBSET_MATCH:
		case SUPERSET_MATCH:
		case PERMUTATION_MATCH:
		case BSTR_PATTERN:
		case HSTR_PATTERN:
		case OSTR_PATTERN:
		case CSTR_PATTERN:
		case USTR_PATTERN:
			// TODO compiler problem: check if the list is complete
			reference.getLocation().reportSemanticError(
					MessageFormat.format("Reference to field `{0}'' of {1} `{2}''", fieldIdentifier.getDisplayName(),
							getTemplateTypeName(), getFullName()));
			break;
		default:
			break;
		}

		IType tempType = myGovernor.getTypeRefdLast(timestamp);
		if (tempType.getIsErroneous(timestamp)) {
			return null;
		}

		switch (tempType.getTypetype()) {
		case TYPE_ASN1_CHOICE:
			if (!((ASN1_Choice_Type) tempType).hasComponentWithName(fieldIdentifier)) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format("Reference to non-existent union field `{0}'' in type `{1}''",
								fieldIdentifier.getDisplayName(), tempType.getTypename()));
				return null;
			}

			return getRefdUnionFieldTemplate(fieldIdentifier, reference, tempType);
		case TYPE_TTCN3_CHOICE:
			if (!((TTCN3_Choice_Type) tempType).hasComponentWithName(fieldIdentifier.getName())) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format("Reference to non-existent union field `{0}'' in type `{1}''",
								fieldIdentifier.getDisplayName(), tempType.getTypename()));
				return null;
			}

			return getRefdUnionFieldTemplate(fieldIdentifier, reference, tempType);
		case TYPE_OPENTYPE:
			if (!((Open_Type) tempType).hasComponentWithName(fieldIdentifier)) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format("Reference to non-existent union field `{0}'' in type `{1}''",
								fieldIdentifier.getDisplayName(), tempType.getTypename()));
				return null;
			}

			return getRefdUnionFieldTemplate(fieldIdentifier, reference, tempType);
		case TYPE_ANYTYPE:
			if (!((Anytype_Type) tempType).hasComponentWithName(fieldIdentifier.getName())) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format("Reference to non-existent union field `{0}'' in type `{1}''",
								fieldIdentifier.getDisplayName(), tempType.getTypename()));
				return null;
			}

			return getRefdUnionFieldTemplate(fieldIdentifier, reference, tempType);
		case TYPE_ASN1_SEQUENCE:
			if (!((ASN1_Sequence_Type) tempType).hasComponentWithName(fieldIdentifier)) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format("Reference to non-existent record field `{0}'' in type `{1}''",
								fieldIdentifier.getDisplayName(), tempType.getTypename()));
				return null;
			}

			return getReferencedSetSequenceFieldTemplate(timestamp, fieldIdentifier, reference, referenceChain);
		case TYPE_TTCN3_SEQUENCE:
			if (!((TTCN3_Sequence_Type) tempType).hasComponentWithName(fieldIdentifier.getName())) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format("Reference to non-existent record field `{0}'' in type `{1}''",
								fieldIdentifier.getDisplayName(), tempType.getTypename()));
				return null;
			}

			return getReferencedSetSequenceFieldTemplate(timestamp, fieldIdentifier, reference, referenceChain);
		case TYPE_ASN1_SET:
			if (!((ASN1_Set_Type) tempType).hasComponentWithName(fieldIdentifier)) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format("Reference to non-existent set field `{0}'' in type `{1}''",
								fieldIdentifier.getDisplayName(), tempType.getTypename()));
				return null;
			}

			return getReferencedSetSequenceFieldTemplate(timestamp, fieldIdentifier, reference, referenceChain);
		case TYPE_TTCN3_SET:
			if (!((TTCN3_Set_Type) tempType).hasComponentWithName(fieldIdentifier.getName())) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format("Reference to non-existent set field `{0}'' in type `{1}''",
								fieldIdentifier.getDisplayName(), tempType.getTypename()));
				return null;
			}

			return getReferencedSetSequenceFieldTemplate(timestamp, fieldIdentifier, reference, referenceChain);
		default:
			reference.getLocation().reportSemanticError(
					MessageFormat.format("Invalid field reference `{0}'': type `{1}'' does not have fields",
							fieldIdentifier.getDisplayName(), tempType.getTypename()));
			return null;
		}
	}

	/**
	 * Checks whether array indexing is allowed for a given template sub
	 * reference or not.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param arrayIndex
	 *                the index to check.
	 * @param referenceChain
	 *                the reference chain use to detect circular references.
	 * */
	protected ITTCN3Template getReferencedArrayTemplate(final CompilationTimeStamp timestamp, final IValue arrayIndex,
			final IReferenceChain referenceChain) {
		switch (getTemplatetype()) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
		case SUPERSET_MATCH:
		case SUBSET_MATCH:
			arrayIndex.getLocation()
					.reportSemanticError(
							MessageFormat.format("Reference with index to an element of {0} `{1}''",
									getTemplateTypeName(), getFullName()));
			break;
		default:
			break;
		}

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
		case TYPE_SEQUENCE_OF:
			if (index < 0) {
				final String message = MessageFormat
						.format("A non-negative integer value was expected instead of {0} for indexing a template of `sequence of'' type `{1}''",
								index, tempType.getTypename());
				arrayIndex.getLocation().reportSemanticError(message);
				return null;
			} else if (!Template_type.TEMPLATE_LIST.equals(getTemplatetype())) {
				return null;
			} else {
				int nofElements = ((Template_List) this).getNofTemplates();
				if (index > nofElements) {
					final String message = MessageFormat
							.format("Index overflow in a template of `sequence of'' type `{0}'': the index is {1}, but the template has only {2} elements",
									tempType.getTypename(), index, nofElements);
					arrayIndex.getLocation().reportSemanticError(message);
					return null;
				}
			}
			break;
		case TYPE_SET_OF:
			if (index < 0) {
				final String message = MessageFormat
						.format("A non-negative integer value was expected instead of {0} for indexing a template of `set of'' type `{1}''",
								index, tempType.getTypename());
				arrayIndex.getLocation().reportSemanticError(message);
				return null;
			} else if (!Template_type.TEMPLATE_LIST.equals(getTemplatetype())) {
				return null;
			} else {
				int nofElements = ((Template_List) this).getNofTemplates();
				if (index > nofElements) {
					final String message = MessageFormat
							.format("Index overflow in a template of `set of'' type `{0}'': the index is {1}, but the template has only {2} elements",
									tempType.getTypename(), index, nofElements);
					arrayIndex.getLocation().reportSemanticError(message);
					return null;
				}
			}
			break;
		case TYPE_ARRAY: {
			ArrayDimension dimension = ((Array_Type) tempType).getDimension();
			dimension.checkIndex(timestamp, indexValue, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (Template_type.TEMPLATE_LIST.equals(getTemplatetype()) && !dimension.getIsErroneous(timestamp)) {
				// re-base the index
				index -= dimension.getOffset();
				if (index < 0 || index > ((Template_List) this).getNofTemplates()) {
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
		default: {
			final String message = MessageFormat.format("Invalid array element reference: type `{0}'' cannot be indexed",
					tempType.getTypename());
			arrayIndex.getLocation().reportSemanticError(message);
			return null;
		}
		}
		if(this instanceof Template_List) {
		ITemplateListItem returnValue = ((Template_List) this).getTemplateByIndex((int) index);
		if (Template_type.TEMPLATE_NOTUSED.equals(returnValue.getTemplatetype())) {
			if (baseTemplate != null) {
				return baseTemplate.getTemplateReferencedLast(timestamp, referenceChain).getReferencedArrayTemplate(timestamp,
						indexValue, referenceChain);
			}

			return null;
		}

		return returnValue;
		} else {
			return null;
		}
	}

	/**
	 * Calculates the referenced sub template, and also checks the reference
	 * itself.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference used to find the sub template.
	 * @param referenceChain
	 *                the reference chain used to detect circular
	 *                references.
	 * */
	@Override
	public ITTCN3Template getReferencedSubTemplate(final CompilationTimeStamp timestamp, final Reference reference,
			final IReferenceChain referenceChain) {
		List<ISubReference> subreferences = reference.getSubreferences();
		ITTCN3Template template = this;
		for (int i = 1; i < subreferences.size(); i++) {
			if (template == null) {
				return template;
			}

			template = template.getTemplateReferencedLast(timestamp, referenceChain);
			if (template.getIsErroneous(timestamp)) {
				return template;
			}
			template = template.setLoweridToReference(timestamp);

			if (Template_type.TEMPLATE_REFD.equals(template.getTemplatetype())) {
				// unfoldable
				return null;
			} else if (Template_type.SPECIFIC_VALUE.equals(template.getTemplatetype())) {
				((SpecificValue_Template) template).getValue().getReferencedSubValue(timestamp, reference, i, referenceChain);
			}

			ISubReference ref = subreferences.get(i);
			if (Subreference_type.fieldSubReference.equals(ref.getReferenceType())) {
				template = ((TTCN3Template) template).getReferencedFieldTemplate(timestamp, ref.getId(), reference, referenceChain);
			} else if (Subreference_type.arraySubReference.equals(ref.getReferenceType())) {
				template = ((TTCN3Template) template).getReferencedArrayTemplate(timestamp, ((ArraySubReference) ref).getValue(),
						referenceChain);
			} else {
				// error found
				return this;
			}
		}

		return template;
	}

	/**
	 * Checks if the template is actually a value.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * 
	 * @return true if the contents of the template can be handled as a
	 *         value.
	 * */
	@Override
	public boolean isValue(final CompilationTimeStamp timestamp) {
		return false;
	}

	/**
	 * @return the value contained in this template if it can be handled as
	 *         a value, otherwise null
	 * */
	@Override
	public IValue getValue() {
		return null;
	}

	/**
	 * Checks that if there is a length restriction applied to this
	 * template, it is semantically correct.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param type
	 *                the type the template is being checked against.
	 * */
	protected void checkLengthRestriction(final CompilationTimeStamp timestamp, final IType type) {
		if (lengthRestriction == null) {
			return;
		}

		lengthRestriction.check(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		if (type instanceof IReferencingType) {
			IReferenceChain refChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IType last = ((IReferencingType) type).getTypeRefd(timestamp, refChain);
			refChain.release();
			if (!last.getIsErroneous(timestamp)) {
				checkLengthRestriction(timestamp, last);
			}
			return;
		}

		if (type.getIsErroneous(timestamp)) {
			return;
		}

		Type_type typeType = type.getTypetypeTtcn3();
		switch (typeType) {
		case TYPE_PORT:
			// the error was already reported.
			return;
		case TYPE_ARRAY:
			lengthRestriction.checkArraySize(timestamp, ((Array_Type) type).getDimension());
			break;
		case TYPE_BITSTRING:
		case TYPE_HEXSTRING:
		case TYPE_OCTETSTRING:
		case TYPE_CHARSTRING:
		case TYPE_UCHARSTRING:
		case TYPE_SEQUENCE_OF:
		case TYPE_SET_OF:
			break;
		default:
			lengthRestriction.getLocation().reportSemanticError(
					MessageFormat.format("Length restriction cannot be used in template of type `{0}''", type.getTypename()));
			return;
		}

		checkTemplateSpecificLengthRestriction(timestamp, typeType);
	}

	/**
	 * Does the template specific part of checking the length restriction.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param typeType
	 *                the TTCN-3 type-type to describe the type.
	 * */
	protected void checkTemplateSpecificLengthRestriction(final CompilationTimeStamp timestamp, final Type_type typeType) {
		// In the general implementation we cannot verify anything on
		// the matching mechanisms
		// they are either correct or not applicable to the type
	}

	/**
	 * Checks the generic properties of the template, and serves as starting
	 * point for the more specific checks.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param type
	 *                the type this template should be checked against
	 * @param isModified
	 *                should be true if this template is a modified
	 *                template.
	 * @param allowOmit
	 *                enables the acceptance of omit templates.
	 * @param allowAnyOrOmit
	 *                enables the acceptance of any or omit template.
	 * @param subCheck
	 *                enables the checking of sub types.
	 * @param implicitOmit
	 *                true if the implicit omit optional attribute was set
	 *                for the template, false otherwise
	 * */
	@Override
	public void checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final IType type, final boolean isModified,
			final boolean allowOmit, final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit) {
		
		if(type == null) { 
			return;
		}
		
		if (!getIsErroneous(timestamp)) {
			
			if( !(ITTCN3Template.Template_type.TEMPLATE_NOTUSED.equals(this.getTemplatetype())) ) {
				  type.checkThisTemplate(timestamp, this, isModified, implicitOmit);
			} 

			if (getLengthRestriction() != null) {
				checkLengthRestriction(timestamp, type);
			}
			if (!allowOmit && isIfpresent) {
				location.reportSemanticError("`ifpresent' is not allowed here");
			}
			if (subCheck) {
				type.checkThisTemplateSubtype(timestamp, this);
			}
		}
	}

	/**
	 * Checks template restriction using common data members of this class,
	 * every check_xxx_restriction() function must call this function.
	 * 
	 * @param definitionName
	 *                name for the error/warning message
	 * @param templateRestriction
	 *                the template restriction to check
	 * */
	@Override
	public final void checkRestrictionCommon(final String definitionName, final TemplateRestriction.Restriction_type templateRestriction) {
		switch (templateRestriction) {
		case TR_VALUE:
		case TR_OMIT:
			if (lengthRestriction != null) {
				location.reportSemanticError(MessageFormat.format(LENGTHRESTRICTIONERROR, definitionName));
			}
			if (isIfpresent) {
				location.reportSemanticError(MessageFormat.format(RESTRICTIONERROR, definitionName, "ifpresent"));
			}
			break;
		case TR_PRESENT:
			if (isIfpresent) {
				location.reportSemanticError(MessageFormat.format(RESTRICTIONERROR, definitionName, "ifpresent"));
			}
			break;
		default:
			return;
		}
	}

	/**
	 * Checks if this template conforms to the restriction TR_OMIT or
	 * TR_VALUE This is the default behavior, override for special cases.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param definitionName
	 *                name for the error/warning message
	 * @param omitAllowed
	 *                true in case of TR_OMIT, false in case of TR_VALUE
	 * @return false = always satisfies restriction -> no runtime check
	 *         needed or never satisfies restriction -> compiler error(s)
	 *         true = possibly violates restriction, cannot be determined at
	 *         compile time -> runtime check needed and compiler warning
	 *         given when inadequate restrictions are used, in other cases
	 *         there's no warning
	 */
	@Override
	public boolean checkValueomitRestriction(final CompilationTimeStamp timestamp, final String definitionName, final boolean omitAllowed) {
		if (omitAllowed) {
			checkRestrictionCommon(definitionName, TemplateRestriction.Restriction_type.TR_OMIT);
		} else {
			checkRestrictionCommon(definitionName, TemplateRestriction.Restriction_type.TR_VALUE);
		}

		location.reportSemanticError(MessageFormat.format(RESTRICTIONERROR, definitionName, getTemplateTypeName()));
		return false;
	}

	/**
	 * Helper function for check_valueomit_restriction called by
	 * Named_Template_List instances.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param definitionName
	 *                name for the error/warning message.
	 * @param checkedNames
	 *                the names of the named templates already checked.
	 * @param neededCheckedCnt
	 *                the number of elements left to be checked.
	 * 
	 * @return true if a check at runtime is needed, false otherwise.
	 */
	@Override
	public boolean chkRestrictionNamedListBaseTemplate(final CompilationTimeStamp timestamp, final String definitionName,
			final Set<String> checkedNames, final int neededCheckedCnt) {
		// override when needed
		return false;
	}

	/**
	 * Checks if this template conforms to the restriction TR_PRESENT. This
	 * is the default behavior, override for special cases.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param definitionName
	 *                name for the error/warning message
	 * 
	 * @return true if the template conforms to the restriction TR_PRESENT.
	 */
	@Override
	public boolean checkPresentRestriction(final CompilationTimeStamp timestamp, final String definitionName) {
		checkRestrictionCommon(definitionName, TemplateRestriction.Restriction_type.TR_PRESENT);
		return false;
	}

	/**
	 * Handles the incremental parsing of this template.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (lengthRestriction != null) {
			lengthRestriction.updateSyntax(reparser, false);
			reparser.updateLocation(lengthRestriction.getLocation());
		}

		if (baseTemplate instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) baseTemplate).updateSyntax(reparser, false);
			reparser.updateLocation(baseTemplate.getLocation());
		} else if (baseTemplate != null) {
			throw new ReParseException();
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (lengthRestriction == null) {
			return;
		}

		lengthRestriction.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (lengthRestriction != null && !lengthRestriction.accept(v)) {
			return false;
		}
		return true;
	}
}
