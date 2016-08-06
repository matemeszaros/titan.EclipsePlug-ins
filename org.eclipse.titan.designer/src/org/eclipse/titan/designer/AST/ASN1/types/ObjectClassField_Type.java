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
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_Definition;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class ObjectClassField_Type extends ASN1Type implements IReferencingType {

	private final IASN1Type referred_type;
	private final ObjectClass_Definition objectClass;
	private final Identifier fieldName;

	public ObjectClassField_Type(final IASN1Type referredType, final ObjectClass_Definition objectClass, final Identifier identifier) {
		this.referred_type = referredType;
		this.objectClass = objectClass;
		fieldName = identifier;
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_OBJECTCLASSFIELDTYPE;
	}

	@Override
	public IASN1Type newInstance() {
		return new ObjectClassField_Type(referred_type, objectClass, fieldName);
	}

	@Override
	public String chainedDescription() {
		return "type ObjectClassFieldType: " + referred_type.getFullName();
	}

	public ObjectClass_Definition getMyObjectClass() {
		return objectClass;
	}

	public Identifier getObjectClassFieldName() {
		return fieldName;
	}

	@Override
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);

		if (null == referred_type) {
			return false;
		}

		final IType t1 = referred_type.getTypeRefdLast(timestamp);
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
		if (isErroneous || null == referred_type || this == referred_type) {
			return "Object class field type";
		}

		return referred_type.getTypename();
	}

	@Override
	public String getOutlineIcon() {
		return "titan.gif";
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		isErroneous = false;

		if (null != referred_type) {
			referred_type.check(timestamp);
		}

		if (null != constraints) {
			constraints.check(timestamp);
		}
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			final IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IType type = getTypeRefd(timestamp, tempReferenceChain);
			tempReferenceChain.release();

			if (null != type && !type.getIsErroneous(timestamp) && !this.equals(type)) {
				type.checkRecursions(timestamp, referenceChain);
			}
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
		final IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IType last = getTypeRefd(timestamp, tempReferenceChain);
		tempReferenceChain.release();

		if (null != last && last != this) {
			last.checkThisValue(timestamp, value, valueCheckingOptions);
		}

		value.setLastTimeChecked(timestamp);
	}

	@Override
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template, final boolean isModified,
			final boolean implicitOmit) {
		registerUsage(template);

		if (getIsErroneous(timestamp)) {
			return;
		}

		final IType tempType = getTypeRefdLast(timestamp);
		if (tempType != this) {
			tempType.checkThisTemplate(timestamp, template, isModified, implicitOmit);
		}
	}

	@Override
	public IType getTypeRefd(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		if (refChain.add(this) && !getIsErroneous(timestamp)) {
			return referred_type;
		}

		isErroneous = true;
		lastTimeChecked = timestamp;
		return this;
	}

	@Override
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return this;
		}

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDSUBREFERENCE, getTypename()));
			return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId()
							.getDisplayName(), getTypename()));
			return null;
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference)
							.getId().getDisplayName(), getTypename()));
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		if (null != referred_type) {
			return referred_type.getProposalDescription(builder);
		}

		return builder.append("unknown_referred_type");
	}

	/**
	 * Searches and adds a completion proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * The type referred last is identified, and the job of adding a
	 * proposal is delegated to it.
	 * 
	 * @param propCollector
	 *                the proposal collector to add the proposal to, and
	 *                used to get more information
	 * @param i
	 *                index, used to identify which element of the reference
	 *                (used by the proposal collector) should be checked for
	 *                completions.
	 * */
	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {

		if (null != referred_type && !this.equals(referred_type)) {
			referred_type.addProposal(propCollector, i);
		}
	}

	/**
	 * Searches and adds a declaration proposal to the provided collector if
	 * a valid one is found.
	 * <p>
	 * The type referred last is identified, and the job of adding a
	 * declaration is delegated to it.
	 * 
	 * @param declarationCollector
	 *                the declaration collector to add the declaration to,
	 *                and used to get more information.
	 * @param i
	 *                index, used to identify which element of the reference
	 *                (used by the declaration collector) should be checked.
	 * */
	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (null != referred_type && !this.equals(referred_type)) {
			referred_type.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		// TODO
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (referred_type != null && !referred_type.accept(v)) {
			return false;
		}
		if (objectClass != null && !objectClass.accept(v)) {
			return false;
		}
		if (fieldName != null && !fieldName.accept(v)) {
			return false;
		}
		return true;
	}
}
