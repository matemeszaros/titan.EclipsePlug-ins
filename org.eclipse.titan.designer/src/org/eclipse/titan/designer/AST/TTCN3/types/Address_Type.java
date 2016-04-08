/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * address type (TTCN-3).
 * 
 * @author Kristof Szabados
 * */
public final class Address_Type extends Type implements IReferencingType {

	/**
	 * Pointer to the real address type. Does not belong to this class.
	 * */
	private IType address;

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_ADDRESS;
	}

	@Override
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		return false;
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
		return "address";
	}

	@Override
	public String getOutlineIcon() {
		return "address.gif";
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		isErroneous = false;

		parseAttributes(timestamp);

		IType referencedLast = getTypeRefdLast(timestamp);

		if (referencedLast == null || referencedLast.getIsErroneous(timestamp)) {
			return;
		}

		referencedLast.check(timestamp);
	}

	public static void checkAddress(final CompilationTimeStamp timestamp, final Type type) {
		IType referencedLast = type.getTypeRefdLast(timestamp);

		if (referencedLast == null || referencedLast.getIsErroneous(timestamp)) {
			return;
		}

		referencedLast.check(timestamp);

		switch (referencedLast.getTypetype()) {
		case TYPE_PORT:
			type.getLocation().reportSemanticError(MessageFormat.format("Port type `{0}'' cannot be the address type", referencedLast.getTypename()));
			break;
		case TYPE_COMPONENT:
			type.getLocation().reportSemanticError(MessageFormat.format("Component type `{0}'' cannot be the address type", referencedLast.getTypename()));
			break;
		case TYPE_SIGNATURE:
			type.getLocation().reportSemanticError(MessageFormat.format("Signature type `{0}'' cannot be the address type", referencedLast.getTypename()));
			break;
		case TYPE_DEFAULT:
			type.getLocation().reportSemanticError("The default type cannot be the address type");
			break;
		case TYPE_ANY:
			type.getLocation().reportSemanticError("The any type cannot be the address type");
			break;
		default:
			break;
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

		IType tempType = getTypeRefd(timestamp, null);
		if (tempType != this) {
			tempType.checkThisValue(timestamp, value, valueCheckingOptions);
		}
	}

	@Override
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit) {
		registerUsage(template);
		template.setMyGovernor(this);

		IType tempType = getTypeRefd(timestamp, null);
		if (tempType != this) {
			tempType.checkThisTemplate(timestamp, template, isModified, implicitOmit);
		}
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

		IType lastType = this;
		while (lastType != null && lastType instanceof IReferencingType && !lastType.getIsErroneous(timestamp)) {
			lastType = ((IReferencingType) lastType).getTypeRefd(timestamp, tempReferenceChain);
		}

		if (newChain) {
			tempReferenceChain.release();
		}

		if (lastType != null && lastType.getIsErroneous(timestamp)) {
			setIsErroneous(true);
		}

		return lastType;
	}

	@Override
	public IType getTypeRefd(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		if (refChain != null && !refChain.add(this)) {
			setIsErroneous(true);
			return this;
		}

		if (myScope != null) {
			TTCN3Module module = (TTCN3Module) myScope.getModuleScope();
			address = module.getAddressType(timestamp);
			if (address != null) {
				return address;
			}

			location.reportSemanticError("Type `address' is not defined in this module");
		}

		setIsErroneous(true);
		return this;
	}

	@Override
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {
		if (lastTimeChecked == null) {
			check(timestamp);
		}

		if (address != null && this != address) {
			Expected_Value_type internalExpectation =
					(expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE) ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE : expectedIndex;
			IType temp = address.getFieldType(timestamp, reference, actualSubReference, internalExpectation, refChain, false);
			if (reference.getIsErroneous(timestamp)) {
				setIsErroneous(true);
			}
			return temp;
		}

		return this;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IType lastType = getTypeRefd(timestamp, tempReferenceChain);
			tempReferenceChain.release();

			if (lastType != null && !lastType.getIsErroneous(timestamp) && !this.equals(lastType)) {
				lastType.checkRecursions(timestamp, referenceChain);
			}
		}
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("address");
	}
	
	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (address != null) {
			address.findReferences(referenceFinder, foundIdentifiers);
		}
	}
	
	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (address!=null && !address.accept(v)) {
			return false;
		}
		return true;
	}
}
