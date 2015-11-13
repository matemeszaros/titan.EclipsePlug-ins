/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class Selection_Type extends ASN1Type implements IReferencingType {
	private static final String CHOICEFERENCEEXPECTED = "(Reference to) a CHOICE type was expected in selection type";
	private static final String MISSINGALTERNATIVE = "No alternative with name `{0}'' in the given type `{1}''";

	private final Identifier identifier;
	private final IASN1Type selectionType;
	private IType referencedLast;

	public Selection_Type(final Identifier identifier, final IASN1Type selectionType) {
		this.identifier = identifier;
		this.selectionType = selectionType;

		if (null != selectionType) {
			selectionType.setFullNameParent(this);
		}
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_SELECTION;
	}

	@Override
	public IASN1Type newInstance() {
		return new Selection_Type(identifier, selectionType.newInstance());
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != selectionType) {
			selectionType.setMyScope(scope);
		}
	}

	@Override
	public String chainedDescription() {
		return "selection type with name: " + identifier.getDisplayName();
	}

	@Override
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);

		if (null == selectionType) {
			return false;
		}

		final IType t1 = selectionType.getTypeRefdLast(timestamp);
		final IType t2 = otherType.getTypeRefdLast(timestamp);

		if (t1.getIsErroneous(timestamp) || t2.getIsErroneous(timestamp)) {
			return true;
		}

		return t1.isCompatible(timestamp, t2, null, null, null);
	}

	@Override
	public Type_type getTypetypeTtcn3() {
		return Type_type.TYPE_UNDEFINED;
	}

	@Override
	public String getTypename() {
		if (isErroneous || null == selectionType || this == selectionType) {
			return "Selection type";
		}

		return selectionType.getTypename();
	}

	@Override
	public String getOutlineIcon() {
		return "asn1_selection.gif";
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		builder.append("selection of ");
		if (null != selectionType) {
			selectionType.getProposalDescription(builder);
		}
		return builder;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		isErroneous = false;

		referencedLast = getTypeRefdLast(timestamp);

		if (!referencedLast.getIsErroneous(timestamp)) {
			referencedLast.check(timestamp);
		}

		if (null != selectionType) {
			selectionType.check(timestamp);
		}

		if (null != constraints) {
			constraints.check(timestamp);
		}
	}

	@Override
	public IValue checkThisValueRef(final CompilationTimeStamp timestamp, final IValue value) {
		if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(value.getValuetype())) {
			final IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IType refd = getTypeRefd(timestamp, tempReferenceChain);
			tempReferenceChain.release();

			if (null == refd) {
				return value;
			}

			return refd.checkThisValueRef(timestamp, value);
		}

		return value;
	}

	@Override
	public void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
		final IReferenceChain refChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IType last = getTypeRefd(timestamp, refChain);
		refChain.release();

		if (null != last && last != this) {
			last.checkThisValue(timestamp, value, valueCheckingOptions);
		}
	}

	@Override
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean is_modified,
			final boolean implicit_omit) {
		registerUsage(template);

		if (getIsErroneous(timestamp)) {
			return;
		}

		final IType tempType = getTypeRefdLast(timestamp);
		if (tempType != this) {
			tempType.checkThisTemplate(timestamp, template, is_modified, implicit_omit);
		}
	}

	@Override
	public Type getTypeRefd(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		if (refChain.add(this) && !getIsErroneous(timestamp)) {
			final IType type = selectionType.getTypeRefdLast(timestamp);
			if (type.getIsErroneous(timestamp)) {
				isErroneous = true;
				lastTimeChecked = timestamp;
				return this;
			}

			if (Type_type.TYPE_ASN1_CHOICE.equals(type.getTypetype())) {
				if (((ASN1_Choice_Type) type).hasComponentWithName(identifier)) {
					return ((ASN1_Choice_Type) type).getComponentByName(identifier).getType();
				}

				final String message = MessageFormat.format(MISSINGALTERNATIVE, identifier.getDisplayName(), type.getFullName());
				location.reportSemanticError(message);
			} else {
				selectionType.getLocation().reportSemanticError(CHOICEFERENCEEXPECTED);
			}
		}

		isErroneous = true;
		lastTimeChecked = timestamp;
		return this;
	}

	@Override
	public IType getTypeRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		final boolean newChain = null == referenceChain;
		IReferenceChain tempReferenceChain;
		if (newChain) {
			tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		} else {
			tempReferenceChain = referenceChain;
		}

		IType type = this;
		while (null != type && type instanceof IReferencingType && !type.getIsErroneous(timestamp)) {
			type = ((IReferencingType) type).getTypeRefd(timestamp, tempReferenceChain);
		}

		if (newChain) {
			tempReferenceChain.release();
		}
		return type;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			final IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final Type type = getTypeRefd(timestamp, tempReferenceChain);
			tempReferenceChain.release();

			if (null != type && !type.getIsErroneous(timestamp) && !this.equals(type)) {
				type.checkRecursions(timestamp, referenceChain);
			}
		}
	}

	@Override
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expected_index, final IReferenceChain refChain, final boolean interrupt_if_optional) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (null != referencedLast && this != referencedLast) {
			final Expected_Value_type internalExpectation =
					(expected_index == Expected_Value_type.EXPECTED_TEMPLATE) ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE
					: expected_index;

			return referencedLast.getFieldType(timestamp, reference, actualSubReference, internalExpectation, refChain,
					interrupt_if_optional);
		}

		return null;
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (selectionType != null) {
			selectionType.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		if (selectionType != null && !selectionType.accept(v)) {
			return false;
		}
		return true;
	}
}
