/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.Type.CompatibilityLevel;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public interface IType extends IGovernor, IIdentifierContainer, IVisitableNode, IReferenceChainElement {

	public enum Type_type {
		// special values never instantiated
		/** undefined. */
		TYPE_UNDEFINED,

		// common types (they reside among the TTCN-3 type package)
		/** boolean. */
		TYPE_BOOL,
		/** bitstring. */
		TYPE_BITSTRING,
		/** octetstring. */
		TYPE_OCTETSTRING,
		/** integer. */
		TYPE_INTEGER,
		/** real / float. */
		TYPE_REAL,
		/** object identifier. */
		TYPE_OBJECTID,
		/** referenced. */
		TYPE_REFERENCED,
		/** sequence of. */
		TYPE_SEQUENCE_OF,
		/** set of. */
		TYPE_SET_OF,

		// TTCN-3 types
		/** character string (TTCN-3). */
		TYPE_CHARSTRING,
		/** hexadecimal string (TTCN-3). */
		TYPE_HEXSTRING,
		/** unversal charstring (TTCN-3). */
		TYPE_UCHARSTRING,
		/** verdict type (TTCN-3). */
		TYPE_VERDICT,
		/** address (TTCN-3). */
		TYPE_ADDRESS,
		/** default (TTCN-3). */
		TYPE_DEFAULT,
		/** Sequence (TTCN-3). */
		TYPE_TTCN3_SEQUENCE,
		/** Set (TTCN-3). */
		TYPE_TTCN3_SET,
		/** Union (TTCN-3). */
		TYPE_TTCN3_CHOICE,
		/** enumeration (TTCN-3). */
		TYPE_TTCN3_ENUMERATED,
		/** function (TTCN-3). */
		TYPE_FUNCTION,
		/** altstep (TTCN-3). */
		TYPE_ALTSTEP,
		/** testcase (TTCN-3). */
		TYPE_TESTCASE,
		/** array (TTCN-3). */
		TYPE_ARRAY,
		/** signature (TTCN-3). */
		TYPE_SIGNATURE,
		/** component (TTCN-3). */
		TYPE_COMPONENT,
		/** port (TTCN-3). */
		TYPE_PORT,
		/** anytype (TTCN-3). */
		TYPE_ANYTYPE,

		// ASN.1 types
		/** null type (ASN.1). */
		TYPE_NULL,
		/** Integer type (ASN.1). */
		TYPE_INTEGER_A,
		/** enumeration (ASN.1). */
		TYPE_ASN1_ENUMERATED,
		/** bitstring (ASN.1). */
		TYPE_BITSTRING_A,
		/** UTF8String (ASN.1). */
		TYPE_UTF8STRING,
		/** numericString (ASN.1). */
		TYPE_NUMERICSTRING,
		/** printablestring (ASN.1). */
		TYPE_PRINTABLESTRING,
		/** teletexstring (ASN.1). */
		TYPE_TELETEXSTRING,
		/** videotexstring (ASN.1). */
		TYPE_VIDEOTEXSTRING,
		/** IA5String (ASN.1). */
		TYPE_IA5STRING,
		/** graphicstring (ASN.1). */
		TYPE_GRAPHICSTRING,
		/** visiblestring (ASN.1). */
		TYPE_VISIBLESTRING,
		/** generalString (ASN.1). */
		TYPE_GENERALSTRING,
		/** universalString (ASN.1). */
		TYPE_UNIVERSALSTRING,
		/** bmpString (ASN.1). */
		TYPE_BMPSTRING,
		/** unrestrictedString (ASN.1). */
		TYPE_UNRESTRICTEDSTRING,
		/** UTCtime (ASN.1). */
		TYPE_UTCTIME,
		/** generalised time (ASN.1). */
		TYPE_GENERALIZEDTIME,
		/** objectdescriptor. */
		TYPE_OBJECTDESCRIPTOR,
		/** relative object identifier (ASN.1). */
		TYPE_ROID,
		/** choice (ASN.1). */
		TYPE_ASN1_CHOICE,
		/** sequence (ASN.1). */
		TYPE_ASN1_SEQUENCE,
		/** set (ASN.1). */
		TYPE_ASN1_SET,
		/** ObjectClassField (ASN.1). */
		TYPE_OBJECTCLASSFIELDTYPE,
		/** opentype (ASN.1). */
		TYPE_OPENTYPE,
		/** ANY (ASN.1). */
		TYPE_ANY,
		/** external (ASN.1). */
		TYPE_EXTERNAL,
		/** embedded_pdv (ASN.1). */
		TYPE_EMBEDDED_PDV,
		/** selection (ASN.1). */
		TYPE_SELECTION
	}

	/** Encoding types. */
	public enum Encoding_type {
		/** not yet defined */
		UNDEFINED,
		/** ber encoding. */
		BER,
		/** per encoding. */
		PER,
		/** xer encoding. */
		XER,
		/** raw encoding. */
		RAW,
		/** text encoding. */
		TEXT,
		/** json encoding. */
		JSON
	}

	/**
	 * Represents the options that drive the value checking mechanisms. </p>
	 * All members have to be final, as it is not allowed to change the
	 * options during analysis. If this would be needed for a branch of the
	 * analysis, a copy should be made.
	 * */
	public static class ValueCheckingOptions {
		/** The kind of the value to be expected */
		public final Expected_Value_type expected_value;
		/**
		 * true if an incomplete value can be accepted at the given
		 * location, false otherwise
		 */
		public final boolean incomplete_allowed;
		/**
		 * true if the omit value can be accepted at the given location,
		 * false otherwise
		 */
		public final boolean omit_allowed;
		/** true if the subtypes should also be checked */
		public final boolean sub_check;
		/**
		 * true if the implicit omit optional attribute was set for the
		 * value, false otherwise
		 */
		public final boolean implicit_omit;
		/** true if the value to be checked is an element of a string */
		public final boolean str_elem;

		public ValueCheckingOptions(final Expected_Value_type expectedValue, final boolean incompleteAllowed, final boolean omitAllowed,
				final boolean subCheck, final boolean implicitOmit, final boolean strElem) {
			this.expected_value = expectedValue;
			this.incomplete_allowed = incompleteAllowed;
			this.omit_allowed = omitAllowed;
			this.sub_check = subCheck;
			this.implicit_omit = implicitOmit;
			this.str_elem = strElem;
		}
	}

	/** @return the internal type of the type */
	Type_type getTypetype();

	/** @return the parent type of the actual type */
	IType getParentType();

	/**
	 * Sets the parent type of the actual type.
	 * 
	 * @param type
	 *                the type to set.
	 * */
	void setParentType(final IType type);

	/**
	 * @return the with attribute path element of this type. If it did not
	 *         exist it will be created.
	 * */
	WithAttributesPath getAttributePath();

	/**
	 * Sets the parent path for the with attribute path element of this
	 * type. Also, creates the with attribute path node if it did not exist
	 * before.
	 * 
	 * @param parent
	 *                the parent to be set.
	 * */
	void setAttributeParentPath(final WithAttributesPath parent);

	/**
	 * Clears the with attributes assigned to this type.
	 * <p>
	 * Should only be used on component fields.
	 * */
	void clearWithAttributes();

	/**
	 * Sets the with attributes for this type.
	 * 
	 * @param attributes
	 *                the attributes to set.
	 * */
	void setWithAttributes(final MultipleWithAttributes attributes);

	/** @return true if the done extension was assigned to this type */
	boolean hasDoneAttribute();

	boolean isConstrained();

	void addConstraints(final Constraints constraints);

	Constraints getConstraints();

	/**
	 * @return the sub-type restriction of the actual type, or null if it
	 *         does not have one.
	 */
	SubType getSubtype();

	/**
	 * Sets the type restrictions as they were parsed.
	 * 
	 * @param parsedRestrictions
	 *                the restrictions to set on this type.
	 * */
	void setParsedRestrictions(final List<ParsedSubType> parsedRestrictions);

	/**
	 * Returns the type referred last in case of a referred type, or itself
	 * in any other case.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * 
	 * @return the actual or the last referred type
	 * */
	IType getTypeRefdLast(final CompilationTimeStamp timestamp);

	/**
	 * Returns the referenced field type for structured types, or itself in
	 * any other case.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference used to select the field.
	 * @param actualSubReference
	 *                the index used to tell, which element of the reference
	 *                to use as the field selector.
	 * @param expectedIndex
	 *                the expected kind of the index value
	 * @param interruptIfOptional
	 *                if true then returns null when reaching an optional
	 *                field
	 * @return the type of the field, or self. In case of error a null
	 *         reference is returned.
	 * */
	IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final boolean interruptIfOptional);

	/**
	 * Returns the referenced field type for structured types, or itself in
	 * any other case.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference used to select the field.
	 * @param actualSubReference
	 *                the index used to tell, which element of the reference
	 *                to use as the field selector.
	 * @param expectedIndex
	 *                the expected kind of the index value
	 * @param refChain
	 *                a chain of references used to detect circular
	 *                references.
	 * @param interruptIfOptional
	 *                if true then returns null when reaching an optional
	 *                field
	 * @return the type of the field, or self.
	 * */
	IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional);

	/**
	 * Calculates the list of field types traversed, in type_array and their
	 * local indices in subrefsArray parameters. Must be used only after
	 * getFieldType() was already successfully invoked. It can be used only
	 * when all array indexes are foldable, otherwise it returns false.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference used to select the field.
	 * @param actualSubReference
	 *                the index used to tell, which element of the reference
	 *                to use as the field selector.
	 * @param subrefsArray
	 *                the list of field indices the searched fields were
	 *                found at.
	 * @param typeArray
	 *                the list of types found while traversing the fields.
	 * @return true in case the type of the referenced field could be
	 *         evaluated, false otherwise.
	 */
	boolean getSubrefsAsArray(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			List<Integer> subrefsArray, List<IType> typeArray);

	/**
	 * Calculates the list of field types traversed. Does not check if the
	 * index values are valid. Does not give error messages, it just returns
	 * with false.
	 * 
	 * @param reference
	 *                the reference used to select the field.
	 * @param actualSubReference
	 *                the index used to tell, which element of the reference
	 *                to use as the field selector.
	 * @param typeArray
	 *                the list of types found while traversing the fields.
	 * @return true in case the type of the referenced field could be
	 *         evaluated, false otherwise.
	 */
	boolean getFieldTypesAsArray(final Reference reference, final int actualSubReference, List<IType> typeArray);

	/**
	 * Checks if there is a variant attribute among the ones reaching to
	 * this type.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * 
	 * @return true if there was a variant attribute found, false otherwise.
	 * */
	boolean hasVariantAttributes(final CompilationTimeStamp timestamp);

	/**
	 * Checks if a given type has the done extension attribute assigned to
	 * it, or not.
	 * */
	void checkDoneAttribute(final CompilationTimeStamp timestamp);

	/**
	 * Parses the attributes assigned to this type, and creates the needed
	 * semantic structures to store the gained data.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * */
	void parseAttributes(final CompilationTimeStamp timestamp);

	/**
	 * Checks if the complex type has a field whose name is exactly the same
	 * as the name of the definition defining the type.
	 * 
	 * @param definitionName
	 *                the name of the definition.
	 **/
	void checkConstructorName(final String definitionName);

	/**
	 * The type of sub-type that belongs to this type or ST_NONE if this
	 * type cannot have sub-type, every type that can have a sub-type must
	 * override this function
	 */
	SubType.SubType_type getSubtypeType();

	/**
	 * Checks for circular references within embedded types.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references,
	 *                must not be null.
	 **/
	void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	/**
	 * Checks if the values of this type can be used only in `self'. Some
	 * types (default, function reference with `runs on self') are invalid
	 * outside of the component they were created in, they should not be
	 * sent/received in ports or used in compref.start. All structured types
	 * that may contain such internal types are also internal.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @return true if component internal, false otherwise.
	 * */
	boolean isComponentInternal(final CompilationTimeStamp timestamp);

	/**
	 * Checks the types which should be component internal, if they have
	 * left the component. Should be called only if is_component_internal()
	 * returned true.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param typeSet
	 *                is used to escape infinite recursion, by maintaining
	 *                the set of types used to call this function.
	 * @param operation
	 *                the name of the operation to be included in the error
	 *                message.
	 * */
	void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation);

	/**
	 * Checks whether the type can be a component of another type definition
	 * (e.g. field of a structured type, parameter/return type/ exception of
	 * a signature). Ports and Signatures are not allowed, Default only
	 * within structured types.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param errorLocation
	 *                the location to report the errors to.
	 * @param defaultAllowed
	 *                whether default should be allowed or not.
	 * @param errorMessage
	 *                the part of the error message to be reported.
	 * */
	void checkEmbedded(final CompilationTimeStamp timestamp, final Location errorLocation, final boolean defaultAllowed, final String errorMessage);

	/**
	 * If the value is an undefined lowerid, then this member decides
	 * whether it is a reference or a lowerid value (e.g., enum, named
	 * number).
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param value
	 *                the value to be checked
	 * 
	 * @return the converted value so that it can replace the original, or
	 *         the original if no conversion was needed
	 * */
	IValue checkThisValueRef(final CompilationTimeStamp timestamp, final IValue value);

	/**
	 * Checks if a given value is valid according to this type.
	 * <p>
	 * The default / base implementation checks referenced, expression and
	 * macro values, as they must be unfolded (if possible) before gaining
	 * access to the real / final value and that values kind.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param value
	 *                the value to be checked
	 * @param valueCheckingOptions
	 *                the options according to which the given value should
	 *                be evaluated
	 * */
	void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions);

	/**
	 * Checks whether the provided template is a specific value and the
	 * embedded value is a referenced one.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param template
	 *                the template to check.
	 * 
	 * @return the checked template, might be different from the one passed
	 *         as parameter.
	 * */
	ITTCN3Template checkThisTemplateRef(final CompilationTimeStamp timestamp, final ITTCN3Template template);

	/**
	 * Does the semantic checking of the provided template according to the
	 * a specific type.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param template
	 *                the template to be checked by the type.
	 * @param isModified
	 *                true if the template is a modified template
	 * @param implicitOmit
	 *                true if the implicit omit optional attribute was set
	 *                for the template, false otherwise
	 * */
	void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit);

	/**
	 * Does the semantic checking of the provided template according to the
	 * sub-type of the actual type.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param template
	 *                the template to be checked by the sub-type.
	 * */
	void checkThisTemplateSubtype(final CompilationTimeStamp timestamp, final ITTCN3Template template);

	/**
	 * Returns whether this type is compatible with type.
	 * <p>
	 * Note: The compatibility relation is asymmetric. The function returns
	 * true if the set of possible values in type is a subset of possible
	 * values in this.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param otherType
	 *                the type to check against.
	 * @param info
	 *                the type compatibility information.
	 * @param leftChain
	 *                to detect type recursion on the left side.
	 * @param rightChain
	 *                to detect type recursion on the right side.
	 * @return true if they are compatible, false otherwise.
	 * */
	boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain);
	
	/**
	 * Returns whether this type is strongly compatible with type that is exactly has the same type and they are both base types
	 * <p>
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param otherType
	 *                the type to check against.
	 * @param info
	 *                the type compatibility information.
	 * @param leftChain
	 *                to detect type recursion on the left side.
	 * @param rightChain
	 *                to detect type recursion on the right side.
	 * @return true if they are compatible, false otherwise.
	 * */
	boolean isStronglyCompatible(final CompilationTimeStamp timestamp, final IType otherType, TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain);

	/**
	 * Returns whether this type and it's sub-type are compatible to the
	 * other type and it's sub-type.
	 */
	CompatibilityLevel getCompatibility(final CompilationTimeStamp timestamp, final IType type, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain);

	/**
	 * Returns whether this type is identical to the parameter type.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param type
	 *                the type to check against
	 * @return true if they are identical, false otherwise
	 * */
	boolean isIdentical(final CompilationTimeStamp timestamp, final IType type);

	String getTypename();

	/**
	 * @return the TTCN-3 equivalent of this type's type, or undefined if
	 *         none
	 */
	Type_type getTypetypeTtcn3();

	Assignment getDefiningAssignment();

	// TODO declaration and proposal collecting should not belong here
	/**
	 * Searches and adds a declaration proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * Simple types can not be used as declarations.
	 * 
	 * @param declarationCollector
	 *                the declaration collector to add the declaration to,
	 *                and used to get more information.
	 * @param i
	 *                index, used to identify which element of the reference
	 *                (used by the declaration collector) should be checked.
	 * */
	void addDeclaration(final DeclarationCollector declarationCollector, final int i);

	/**
	 * Searches and adds a completion proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * If this type is a simple type, it can never complete any proposals.
	 * 
	 * @param propCollector
	 *                the proposal collector to add the proposal to, and
	 *                used to get more information
	 * @param i
	 *                index, used to identify which element of the reference
	 *                (used by the proposal collector) should be checked for
	 *                completions.
	 * */
	void addProposal(final ProposalCollector propCollector, final int i);

	StringBuilder getProposalDescription(final StringBuilder builder);

	void getEnclosingField(final int offset, final ReferenceFinder rf);
}
