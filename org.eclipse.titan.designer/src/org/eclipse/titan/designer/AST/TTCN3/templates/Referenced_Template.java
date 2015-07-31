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

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a referenced template.
 * 
 * @author Kristof Szabados
 * */
public final class Referenced_Template extends TTCN3Template {
	public static final String CIRCULARTEMPLATEREFERENCE = "circular template reference chain: `{0}''";
	private static final String TYPEMISSMATCH1 = "Type missmatch: a signature template of type `{0}'' was expected instead of `{1}''";
	private static final String TYPEMISSMATCH2 = "Type mismatch: a value or template of type `{0}'' was expected instead of `{1}''";
	private static final String INADEQUATETEMPLATERESTRICTION = "Inadequate restriction on the referenced {0} `{1}'',"
			+ " this may cause a dynamic test case error at runtime";

	private final Reference reference;

	// TODO could be optimized by using a reference to the last referred
	// template
	public Referenced_Template(final Reference reference) {
		this.reference = reference;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
	}

	public Referenced_Template(final CompilationTimeStamp timestamp, final SpecificValue_Template original) {
		copyGeneralProperties(original);
		IValue value = original.getSpecificValue();
		switch (value.getValuetype()) {
		case REFERENCED_VALUE:
			reference = ((Referenced_Value) value).getReference();
			break;
		case UNDEFINED_LOWERIDENTIFIER_VALUE:
			Identifier identifier = ((Undefined_LowerIdentifier_Value) value).getIdentifier();
			FieldSubReference subReference = new FieldSubReference(identifier);
			subReference.setLocation(value.getLocation());
			reference = new Reference(null);
			reference.addSubReference(subReference);
			reference.setLocation(value.getLocation());
			reference.setFullNameParent(this);
			reference.setMyScope(value.getMyScope());
			break;
		default:
			reference = null;
			break;
		}
	}

	@Override
	public Template_type getTemplatetype() {
		return Template_type.TEMPLATE_REFD;
	}

	@Override
	public String createStringRepresentation() {
		ITTCN3Template last = getTemplateReferencedLast(CompilationTimeStamp.getBaseTimestamp());
		if (Template_type.TEMPLATE_REFD.equals(last.getTemplatetype())) {
			return reference.getDisplayName();
		}

		StringBuilder builder = new StringBuilder();
		builder.append(last.createStringRepresentation());

		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}

		return builder.toString();
	}

	@Override
	// Location is optimized not to store an object at it is not needed
	public Location getLocation() {
		return new Location(reference.getLocation());
	}

	@Override
	public void setLocation(final Location location) {
		// Do nothing
	}

	@Override
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous referenced template";
		}

		return "reference template";
	}

	public Reference getReference() {
		return reference;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference != null) {
			reference.setMyScope(scope);
		}
	}

	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return null;
		}

		IType type = assignment.getType(timestamp).getFieldType(timestamp, reference, 1, expectedValue, false);
		if (type == null) {
			setIsErroneous(true);
		}

		return type;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		IType type = getExpressionGovernor(timestamp, expectedValue);
		if (type == null) {
			return Type_type.TYPE_UNDEFINED;
		}

		return type.getTypeRefdLast(timestamp).getTypetypeTtcn3();
	}

	/**
	 * Calculates the referenced template, and while doing so checks the
	 * reference too.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the reference chain used to detect cyclic references.
	 *
	 * @return the template referenced
	 * */
	private ITTCN3Template getTemplateReferenced(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (reference == null) {
			setIsErroneous(true);
			return null;
		}

		Assignment ass = reference.getRefdAssignment(timestamp, true);

		if (ass == null || !Assignment_type.A_TEMPLATE.equals(ass.getAssignmentType())) {
			// the error was already reported
			setIsErroneous(true);
			return this;
		}

		List<ISubReference> subreferences = reference.getSubreferences();

		ITTCN3Template template = ((Def_Template) ass).getTemplate(timestamp);
		template = template.getReferencedSubTemplate(timestamp, reference, referenceChain);

		if (template != null) {
			return template;
		} else if (subreferences != null && reference.hasUnfoldableIndexSubReference(timestamp)) {
			// some array indices could not be evaluated
		} else if (reference.getUsedInIsbound()) {
			return this;
		} else {
			setIsErroneous(true);
		}

		return this;
	}

	@Override
	public TTCN3Template getTemplateReferencedLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (reference == null) {
			setIsErroneous(true);
			return this;
		}

		IReferenceChain tempReferenceChain = (referenceChain != null) ? referenceChain : ReferenceChain.getInstance(
				CIRCULARTEMPLATEREFERENCE, true);

		TTCN3Template template = this;
		Assignment ass = reference.getRefdAssignment(timestamp, true);

		if (ass != null && ass.getAssignmentType() == Assignment_type.A_TEMPLATE) {
			tempReferenceChain.markState();

			if (tempReferenceChain.add(this)) {
				ITTCN3Template refd = getTemplateReferenced(timestamp, tempReferenceChain);
				if (refd != this) {
					template = refd.getTemplateReferencedLast(timestamp, referenceChain);
				}
			} else {
				setIsErroneous(true);
			}

			tempReferenceChain.previousState();
		} else {
			setIsErroneous(true);
		}

		if (!tempReferenceChain.equals(referenceChain)) {
			tempReferenceChain.release();
		}

		return template;
	}

	/**
	 * Returns whether in the chain of referenced templates there is one
	 * which was defined to have the implicit omit attribute set
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references
	 *
	 * @return true if it has, false otherwise.
	 * */
	private boolean hasTemplateImpliciteOmit(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		IReferenceChain tempReferenceChain = (referenceChain != null) ? referenceChain : ReferenceChain.getInstance(
				CIRCULARTEMPLATEREFERENCE, true);

		boolean result = false;
		if (reference != null) {
			Assignment ass = reference.getRefdAssignment(timestamp, true);

			if (ass != null && ass.getAssignmentType() == Assignment_type.A_TEMPLATE) {
				Def_Template templateDefinition = (Def_Template) ass;
				if (templateDefinition.hasImplicitOmitAttribute(timestamp)) {
					result = true;
				} else {
					tempReferenceChain.markState();

					if (tempReferenceChain.add(this)) {
						ITTCN3Template refd = getTemplateReferenced(timestamp, tempReferenceChain);
						if (refd != this && refd instanceof Referenced_Template) {
							result = ((Referenced_Template) refd).hasTemplateImpliciteOmit(timestamp, referenceChain);
						}
					} else {
						setIsErroneous(true);
					}

					tempReferenceChain.previousState();
				}
			}
		}

		if (!tempReferenceChain.equals(referenceChain)) {
			tempReferenceChain.release();
		}

		return result;
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		TTCN3Template temp = getTemplateReferencedLast(timestamp);
		if (!temp.getIsErroneous(timestamp)) {
			temp.checkSpecificValue(timestamp, allowOmit);
		}
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this) && reference != null) {
			ISubReference subReference = reference.getSubreferences().get(0);
			if (subReference instanceof ParameterisedSubReference) {
				ActualParameterList parameterList = ((ParameterisedSubReference) subReference).getActualParameters();
				if (parameterList != null) {
					parameterList.checkRecursions(timestamp, referenceChain);
				}
			}

			IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			ITTCN3Template template = getTemplateReferenced(timestamp, tempReferenceChain);
			tempReferenceChain.release();

			if (template != null && !template.getIsErroneous(timestamp) && !this.equals(template)) {
				template.checkRecursions(timestamp, referenceChain);
			}
		}
	}

	@Override
	public void checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final IType type, final boolean isModified,
			final boolean allowOmit, final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit) {
		if (getIsErroneous(timestamp) || reference == null) {
			return;
		}

		Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return;
		}

		assignment.check(timestamp);

		IType governor = assignment.getType(timestamp);
		if (governor != null) {
			governor = governor.getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		}
		if (governor == null) {
			setIsErroneous(true);
			return;
		}

		if (!type.isCompatible(timestamp, governor, null, null, null)) {
			IType last = type.getTypeRefdLast(timestamp);

			switch (last.getTypetype()) {
			case TYPE_PORT:
				// no such thing exists, remain silent
				break;
			case TYPE_SIGNATURE:
				getLocation().reportSemanticError(MessageFormat.format(TYPEMISSMATCH1, type.getTypename(), governor.getTypename()));
				break;
			default:
				getLocation().reportSemanticError(MessageFormat.format(TYPEMISSMATCH2, type.getTypename(), governor.getTypename()));
				break;
			}
			setIsErroneous(true);
			return;
		}

		// check for circular references
		ITTCN3Template temp = getTemplateReferencedLast(timestamp);
		if (temp != this) {
			IReferenceChain referenceChain = ReferenceChain.getInstance(CIRCULARTEMPLATEREFERENCE, true);
			boolean referencedHasImplicitOmit = hasTemplateImpliciteOmit(timestamp, referenceChain);
			referenceChain.release();

			temp.checkThisTemplateGeneric(timestamp, type, temp.getBaseTemplate() != null, allowOmit, allowAnyOrOmit, subCheck,
					implicitOmit || referencedHasImplicitOmit);
		}
	}

	@Override
	protected void checkTemplateSpecificLengthRestriction(final CompilationTimeStamp timestamp, final Type_type typeType) {
		TTCN3Template last = getTemplateReferencedLast(timestamp);
		last.checkTemplateSpecificLengthRestriction(timestamp, typeType);
	}

	@Override
	public boolean checkValueomitRestriction(final CompilationTimeStamp timestamp, final String definitionName, final boolean omitAllowed) {
		if (omitAllowed) {
			checkRestrictionCommon(definitionName, TemplateRestriction.Restriction_type.TR_OMIT);
		} else {
			checkRestrictionCommon(definitionName, TemplateRestriction.Restriction_type.TR_VALUE);
		}

		if (reference != null) {
			Assignment ass = reference.getRefdAssignment(timestamp, true);
			switch (ass.getAssignmentType()) {
			case A_TEMPLATE:
				ITTCN3Template templateLast = getTemplateReferencedLast(timestamp);
				return templateLast.checkValueomitRestriction(timestamp, definitionName, omitAllowed);
			case A_VAR_TEMPLATE:
			case A_EXT_FUNCTION_RTEMP:
			case A_FUNCTION_RTEMP:
			case A_PAR_TEMP_IN:
			case A_PAR_TEMP_OUT:
			case A_PAR_TEMP_INOUT:
				if (ass instanceof Definition) {
					TemplateRestriction.Restriction_type refdTemplateRestriction = ((Definition) ass).getTemplateRestriction();
					refdTemplateRestriction = TemplateRestriction
							.getSubRestriction(refdTemplateRestriction, timestamp, reference);
					// if restriction not satisfied issue
					// warning
					if (TemplateRestriction.isLessRestrictive(omitAllowed ? TemplateRestriction.Restriction_type.TR_OMIT
							: TemplateRestriction.Restriction_type.TR_VALUE, refdTemplateRestriction)) {
						getLocation().reportSemanticError(
								MessageFormat.format(INADEQUATETEMPLATERESTRICTION, ass.getAssignmentName(),
										reference.getDisplayName()));
						return true;
					}
				}
				return false;
			default:
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean checkPresentRestriction(final CompilationTimeStamp timestamp, final String definitionName) {
		checkRestrictionCommon(definitionName, TemplateRestriction.Restriction_type.TR_PRESENT);
		if (reference != null) {
			Assignment ass = reference.getRefdAssignment(timestamp, true);
			switch (ass.getAssignmentType()) {
			case A_TEMPLATE:
				ITTCN3Template templateLast = getTemplateReferencedLast(timestamp);
				return templateLast.checkPresentRestriction(timestamp, definitionName);
			case A_VAR_TEMPLATE:
			case A_EXT_FUNCTION_RTEMP:
			case A_FUNCTION_RTEMP:
			case A_PAR_TEMP_IN:
			case A_PAR_TEMP_OUT:
			case A_PAR_TEMP_INOUT:
				if (ass instanceof Definition) {
					TemplateRestriction.Restriction_type refdTemplateRestriction = ((Definition) ass).getTemplateRestriction();
					refdTemplateRestriction = TemplateRestriction
							.getSubRestriction(refdTemplateRestriction, timestamp, reference);
					// if restriction not satisfied issue
					// warning
					if (TemplateRestriction.isLessRestrictive(TemplateRestriction.Restriction_type.TR_PRESENT,
							refdTemplateRestriction)) {
						getLocation().reportSemanticError(
								MessageFormat.format(INADEQUATETEMPLATERESTRICTION, ass.getAssignmentName(),
										reference.getDisplayName()));
						return true;
					}
				}
				return false;
			default:
				return false;
			}
		}
		return false;
	}

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

		if (reference != null) {
			reference.updateSyntax(reparser, false);
			reparser.updateLocation(reference.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (reference == null) {
			return;
		}

		reference.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		return true;
	}
}
