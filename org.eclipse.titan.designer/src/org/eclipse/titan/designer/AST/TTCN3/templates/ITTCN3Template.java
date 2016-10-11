/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.Set;

import org.eclipse.titan.designer.AST.IGovernedSimple;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public interface ITTCN3Template extends IGovernedSimple {

	public enum Template_type {
		/** not used symbol (-). */
		TEMPLATE_NOTUSED,
		/** omit. */
		OMIT_VALUE,
		/** any value (?). */
		ANY_VALUE,
		/** any or omit (*). */
		ANY_OR_OMIT,
		/** specific value. */
		SPECIFIC_VALUE,
		/** reference to another template. */
		TEMPLATE_REFD,
		/** template returning invoke. */
		TEMPLATE_INVOKE,
		/** value list notation. */
		TEMPLATE_LIST,
		/** assignment notation. */
		NAMED_TEMPLATE_LIST,
		/** assignment notation with array indices. */
		INDEXED_TEMPLATE_LIST,
		/** value list match. */
		VALUE_LIST,
		/** complemented list match. */
		COMPLEMENTED_LIST,
		/** value range match. */
		VALUE_RANGE,
		/** superset match. */
		SUPERSET_MATCH,
		/** subset match. */
		SUBSET_MATCH,
		/** permutation match. */
		PERMUTATION_MATCH,
		/** bitstring pattern. */
		BSTR_PATTERN,
		/** hexstring pattern. */
		HSTR_PATTERN,
		/** octetstring pattern. */
		OSTR_PATTERN,
		/** character string pattern. */
		CSTR_PATTERN,
		/** universal charstring pattern. */
		USTR_PATTERN,
		/** all from template type, hides its real type*/
		ALLELEMENTSFROM,
		/** template body, hides its real type */
		TEMPLATEBODY
	}

	public enum Completeness_type {
		/** the body must be completely specified */
		MUST_COMPLETE,
		/** the body may be incompletely specified */
		MAY_INCOMPLETE,
		/**
		 * some part of the body may be incomplete, others must be
		 * complete
		 */
		PARTIAL
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
	void copyGeneralProperties(final ITTCN3Template original);

	/** @return the internal type of the template */
	Template_type getTemplatetype();

	/** @return the name of type of the template. */
	String getTemplateTypeName();

	@Override
	IType getMyGovernor();

	/**
	 * Sets the governor type.
	 * 
	 * @param governor
	 *                the type to be set.
	 * */
	void setMyGovernor(final IType governor);

	String chainedDescription();

	/**
	 * Creates and returns a string representation if the actual template.
	 * 
	 * @return the string representation of the template.
	 * */
	String createStringRepresentation();

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
	ITTCN3Template setLoweridToReference(final CompilationTimeStamp timestamp);

	/**
	 * Sets the length restriction of the template.
	 * 
	 * @param lengthRestriction
	 *                the length restriction to set
	 * */
	void setLengthRestriction(final LengthRestriction lengthRestriction);

	/** @return the length restriction of the template */
	LengthRestriction getLengthRestriction();

	void setIfpresent();

	/** @return the base template of the actual template */
	ITTCN3Template getBaseTemplate();

	/**
	 * Sets the base template.
	 * 
	 * @param baseTemplate
	 *                the template to set as the base template of this
	 *                template.
	 * */
	void setBaseTemplate(final ITTCN3Template baseTemplate);

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
	Completeness_type getCompletenessConditionSeof(final CompilationTimeStamp timestamp, final boolean incompleteAllowed);

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
	Completeness_type getCompletenessConditionChoice(final CompilationTimeStamp timestamp, final boolean incompleteAllowed,
			final Identifier fieldName);

	/**
	 * Returns the template referred last in case of a referred template, or
	 * itself in any other case.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * 
	 * @return the actual or the last referred template
	 * */
	TTCN3Template getTemplateReferencedLast(final CompilationTimeStamp timestamp);

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
	TTCN3Template getTemplateReferencedLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

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
	ITTCN3Template setTemplatetype(final CompilationTimeStamp timestamp, final Template_type newType);

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
	Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue);

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
	IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue);

	/**
	 * Checks for circular references within embedded templates.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references,
	 *                must not be null.
	 **/
	void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

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
	void checkSpecificValue(final CompilationTimeStamp timestamp, boolean allowOmit);

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
	ITTCN3Template getReferencedSubTemplate(final CompilationTimeStamp timestamp, final Reference reference, final IReferenceChain referenceChain);

	/**
	 * Checks if the template is actually a value.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * 
	 * @return true if the contents of the template can be handled as a
	 *         value.
	 * */
	boolean isValue(final CompilationTimeStamp timestamp);

	/**
	 * @return the value contained in this template if it can be handled as
	 *         a value, otherwise null
	 * */
	IValue getValue();

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
	void checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final IType type, final boolean isModified, final boolean allowOmit,
			final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit);

	/**
	 * Checks template restriction using common data members of this class,
	 * every check_xxx_restriction() function must call this function.
	 * 
	 * @param definitionName
	 *                name for the error/warning message
	 * @param templateRestriction
	 *                the template restriction to check
	 * @param usageLocation
	 *                the location to be used for reporting errors
	 * */
	void checkRestrictionCommon(final CompilationTimeStamp timestamp, final String definitionName, final TemplateRestriction.Restriction_type templateRestriction, final Location usageLocation);

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
	 * @param usageLocation
	 *                the location to be used for reporting errors
	 * @return false = always satisfies restriction -> no runtime check
	 *         needed or never satisfies restriction -> compiler error(s)
	 *         true = possibly violates restriction, cannot be determined at
	 *         compile time -> runtime check needed and compiler warning
	 *         given when inadequate restrictions are used, in other cases
	 *         there's no warning
	 */
	boolean checkValueomitRestriction(final CompilationTimeStamp timestamp, final String definitionName, final boolean omitAllowed, final Location usageLocation);

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
	 * @param usageLocation
	 *                the location to be used for reporting errors
	 * 
	 * @return true if a check at runtime is needed, false otherwise.
	 */
	boolean chkRestrictionNamedListBaseTemplate(final CompilationTimeStamp timestamp, final String definitionName,
			final Set<String> checkedNames, final int neededCheckedCnt, final Location usageLocation);

	/**
	 * Checks if this template conforms to the restriction TR_PRESENT. This
	 * is the default behavior, override for special cases.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param definitionName
	 *                name for the error/warning message
	 * @param usageLocation
	 *                the location to be used for reporting errors
	 * 
	 * @return true if the template conforms to the restriction TR_PRESENT.
	 */
	boolean checkPresentRestriction(final CompilationTimeStamp timestamp, final String definitionName, final Location usageLocation);

}
