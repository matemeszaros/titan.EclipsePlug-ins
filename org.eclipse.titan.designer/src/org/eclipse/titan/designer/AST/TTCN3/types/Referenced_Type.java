/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.Type_Assignment;
import org.eclipse.titan.designer.AST.ASN1.Undefined_Assignment;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Referenced_Type extends ASN1Type implements IReferencingType {

	private final Reference reference;
	private IType refd;
	private IType refdLast;

	private boolean componentInternal;

	public Referenced_Type(final Reference reference) {
		this.reference = reference;
		componentInternal = false;

		if (reference != null) {
			reference.setFullNameParent(this);
			setLocation(reference.getLocation());
			setMyScope(reference.getMyScope());
		}
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_REFERENCED;
	}

	public Reference getReference() {
		return reference;
	}

	@Override
	// Location is optimized not to store an object that it is not needed
	public Location getLocation() {
		if (reference != null && reference.getLocation() != null) {
			return new Location(reference.getLocation());
		}

		return NULL_Location.INSTANCE;
	}

	@Override
	public void setLocation(final Location location) {
	}

	@Override
	public IASN1Type newInstance() {
		return new Referenced_Type(reference);
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference != null) {
			reference.setMyScope(scope);
		}
	}

	@Override
	public String chainedDescription() {
		return "type reference: " + reference;
	}

	@Override
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		IType t1 = getTypeRefdLast(timestamp);
		IType t2 = otherType.getTypeRefdLast(timestamp);

		if (t1.getIsErroneous(timestamp) || t2.getIsErroneous(timestamp)) {
			return true;
		}

		return t1.isCompatible(timestamp, t2, info, null, null);
	}

	@Override
	public boolean isIdentical(final CompilationTimeStamp timestamp, final IType type) {
		check(timestamp);
		type.check(timestamp);
		IType t1 = getTypeRefdLast(timestamp);
		IType t2 = type.getTypeRefdLast(timestamp);

		if (t1.getIsErroneous(timestamp) || t2.getIsErroneous(timestamp)) {
			return true;
		}

		return t1.isIdentical(timestamp, t2);
	}

	@Override
	public Type_type getTypetypeTtcn3() {
		if (isErroneous) {
			return Type_type.TYPE_UNDEFINED;
		}

		return getTypetype();
	}

	@Override
	public String getTypename() {
		if (isErroneous || refdLast == null || refdLast == this) {
			return "Referenced type";
		}

		return refdLast.getTypename();
	}

	@Override
	public String getOutlineIcon() {
		return "referenced.gif";
	}

	@Override
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {
		if (lastTimeChecked == null) {
			check(timestamp);
		}

		if (reference.getSubreferences().size() == 1) {
			return this;
		}

		if (refdLast != null && this != refdLast && !getIsErroneous(timestamp)) {
			Expected_Value_type internalExpectation =
					expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE : expectedIndex;
			IType temp = refdLast.getFieldType(timestamp, reference, actualSubReference, internalExpectation, refChain, interruptIfOptional);
			if (reference.getIsErroneous(timestamp)) {
				setIsErroneous(true);
			}
			return temp;
		}

		return this;
	}

	@Override
	public boolean getSubrefsAsArray(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final List<Integer> subrefsArray, final List<IType> typeArray) {
		if (reference.getSubreferences().size() == 1) {
			return true;
		}

		if (this == refdLast) {
			return false;
		}

		return refdLast.getSubrefsAsArray(timestamp, reference, actualSubReference, subrefsArray, typeArray);
	}

	@Override
	public boolean getFieldTypesAsArray(final Reference reference, final int actualSubReference, final List<IType> typeArray) {
		if (reference.getSubreferences().size() == 1) {
			return true;
		}
		if (this == refdLast || refdLast == null) {
			return false;
		}
		return refdLast.getFieldTypesAsArray(reference, actualSubReference, typeArray);
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		Assignment ass;
		if (lastTimeChecked == null) {
			ass = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);
		} else {
			ass = reference.getRefdAssignment(lastTimeChecked, true);
		}

		if (ass != null && Assignment_type.A_TYPE.equals(ass.getAssignmentType())) {
			if (ass instanceof Def_Type) {
				Def_Type defType = (Def_Type) ass;
				return builder.append(defType.getIdentifier().getDisplayName());
			} else if (ass instanceof Type_Assignment) {
				return builder.append(((Type_Assignment) ass).getIdentifier().getDisplayName());
			}
		}

		return builder.append("unknown_referred_type");
	}

	@Override
	public boolean isComponentInternal(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return componentInternal;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		componentInternal = false;
		isErroneous = false;
		refd = null;

		parseAttributes(timestamp);

		refdLast = getTypeRefdLast(timestamp);

		if (refdLast != null && !refdLast.getIsErroneous(timestamp)) {
			refdLast.check(timestamp);
			componentInternal = refdLast.isComponentInternal(timestamp);
		}

		if (constraints != null) {
			constraints.check(timestamp);
		}
		IType typeLast = getTypeRefdLast(timestamp);
		IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IType typeParent = getTypeRefd(timestamp, tempReferenceChain);
		tempReferenceChain.release();
		if (!typeLast.getIsErroneous(timestamp) && !typeParent.getIsErroneous(timestamp)) {
			checkSubtypeRestrictions(timestamp, typeLast.getSubtypeType(), typeParent.getSubtype());
		}
	}

	@Override
	public void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		IType last = getTypeRefdLast(timestamp);

		if (last != null && !last.getIsErroneous(timestamp) && last != this) {
			last.checkComponentInternal(timestamp, typeSet, operation);
		}
	}

	@Override
	public void checkEmbedded(final CompilationTimeStamp timestamp, final Location errorLocation, final boolean defaultAllowed,
			final String errorMessage) {
		IType last = getTypeRefdLast(timestamp);

		if (last != null && !last.getIsErroneous(timestamp) && last != this) {
			last.checkEmbedded(timestamp, errorLocation, defaultAllowed, errorMessage);
		}
	}

	@Override
	public IValue checkThisValueRef(final CompilationTimeStamp timestamp, final IValue value) {
		if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(value.getValuetype())) {
			IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IType refd = getTypeRefd(timestamp, tempReferenceChain);
			tempReferenceChain.release();

			if (refd == null || this.equals(refd)) {
				return value;
			}

			return refd.checkThisValueRef(timestamp, value);
		}

		return value;
	}

	@Override
	public void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
		if (getIsErroneous(timestamp)) {
			return;
		}

		IType tempType = getTypeRefdLast(timestamp);
		if (tempType != this) {
			tempType.checkThisValue(timestamp, value, new ValueCheckingOptions(valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.omit_allowed, false, valueCheckingOptions.implicit_omit,
					valueCheckingOptions.str_elem));
			Definition def = value.getDefiningAssignment();
			if (def != null) {
					String referingModuleName = getMyScope().getModuleScope().getName();
					if (!def.referingHere.contains(referingModuleName)) {
						def.referingHere.add(referingModuleName);
					}
			}
		}

		if (valueCheckingOptions.sub_check && (subType != null)) {
			subType.checkThisValue(timestamp, value);
		}
	}

	@Override
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit) {
		if (getIsErroneous(timestamp)) {
			return;
		}

		registerUsage(template);

		IType tempType = getTypeRefdLast(timestamp);
		if (tempType != this) {
			tempType.checkThisTemplate(timestamp, template, isModified, implicitOmit);
		}
	}

	@Override
	public IType getTypeRefd(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		if (refChain.add(this) && reference != null && !getIsErroneous(timestamp)) {
			if (refd != null) {
				return refd;
			}

			Assignment ass = reference.getRefdAssignment(timestamp, true);

			if (ass != null && Assignment_type.A_UNDEF.equals(ass.getAssignmentType())) {
				ass = ((Undefined_Assignment) ass).getRealAssignment(timestamp);
			}

			if (ass == null || ass.getIsErroneous()) {
				// The referenced assignment was not found, or is erroneous
				isErroneous = true;
				lastTimeChecked = timestamp;
				return this;
			}

			switch (ass.getAssignmentType()) {
			case A_TYPE: {
				IType tempType = ass.getType(timestamp);
				if (tempType != null) {
					if (!tempType.getIsErroneous(timestamp)) {
						tempType.check(timestamp);
						tempType = tempType.getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, refChain, false);
						if (tempType == null) {
							setIsErroneous(true);
							return this;
						}

						refd = tempType;
						return refd;
					}
				}
				break;
			}
			case A_VS: {
				IType tempType = ass.getType(timestamp);
				if (tempType == null) {
					isErroneous = true;
					lastTimeChecked = timestamp;
					return this;
				}

				refd = tempType;
				return refd;
			}
			case A_OC:
			case A_OBJECT:
			case A_OS:
				ISetting setting = reference.getRefdSetting(timestamp);
				if (setting == null || setting.getIsErroneous(timestamp)) {
					isErroneous = true;
					lastTimeChecked = timestamp;
					return this;
				}

				if (!Setting_type.S_T.equals(setting.getSettingtype())) {
					reference.getLocation().reportSemanticError(MessageFormat.format(TYPEREFERENCEEXPECTED, reference.getDisplayName()));
					isErroneous = true;
					lastTimeChecked = timestamp;
					return this;
				}

				refd = (Type) setting;
				return refd;
			default:
				reference.getLocation().reportSemanticError(MessageFormat.format(TYPEREFERENCEEXPECTED, reference.getDisplayName()));
				break;
			}
		}

		isErroneous = true;
		lastTimeChecked = timestamp;
		return this;
	}

	@Override
	public IType getTypeRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		IReferenceChain tempReferenceChain =
				referenceChain != null ? referenceChain : ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);

		IType t = this;
		while (t != null && t instanceof IReferencingType && !t.getIsErroneous(timestamp)) {
			t = ((IReferencingType) t).getTypeRefd(timestamp, tempReferenceChain);
		}

		if (!tempReferenceChain.equals(referenceChain)) {
			tempReferenceChain.release();
		}

		if (t != null && t.getIsErroneous(timestamp)) {
			setIsErroneous(true);
		}

		return t;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IType t = getTypeRefd(timestamp, tempReferenceChain);
			tempReferenceChain.release();

			if (t != null && !t.getIsErroneous(timestamp) && !this.equals(t)) {
				t.checkRecursions(timestamp, referenceChain);
			}
		}
	}

	/**
	 * Searches and adds a completion proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * The type referred last is identified, and the job of adding a proposal is
	 * delegated to it.
	 *
	 * @param propCollector the proposal collector to add the proposal to, and
	 *            used to get more information
	 * @param i index, used to identify which element of the reference (used by
	 *            the proposal collector) should be checked for completions.
	 * */
	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (refdLast != null && !this.equals(refdLast)) {
			refdLast.addProposal(propCollector, i);
		}
	}

	/**
	 * Searches and adds a declaration proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * The type referred last is identified, and the job of adding a declaration
	 * is delegated to it.
	 *
	 * @param declarationCollector the declaration collector to add the
	 *            declaration to, and used to get more information.
	 * @param i index, used to identify which element of the reference (used by
	 *            the declaration collector) should be checked.
	 * */
	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (refdLast != null && !this.equals(refdLast)) {
			refdLast.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reference.updateSyntax(reparser, false);
		reparser.updateLocation(reference.getLocation());

		if (subType != null) {
			subType.updateSyntax(reparser, false);
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (reference != null) {
			reference.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (reference!=null && !reference.accept(v)) {
			return false;
		}
		return true;
	}
}
