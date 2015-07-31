/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent a FixedTypeValueFieldSpec.
 * 
 * @author Kristof Szabados
 */
public final class FixedTypeValue_FieldSpecification extends FieldSpecification {

	/** Fixed type. */
	private final ASN1Type fixedType;
	private final boolean isUnique;
	// FIXME only temporal solution, should be corrected when values become
	// fully supported for this usage
	private final boolean hasDefault;
	private final IValue defaultValue;

	public FixedTypeValue_FieldSpecification(final Identifier identifier, final ASN1Type fixedType, final boolean isUnique,
			final boolean isOptional, final boolean hasDefault, final IValue defaultValue) {
		super(identifier, isOptional);
		this.fixedType = fixedType;
		this.isUnique = isUnique;
		this.hasDefault = hasDefault;
		this.defaultValue = defaultValue;

		if (null != fixedType) {
			fixedType.setFullNameParent(this);
		}
		if (null != defaultValue) {
			defaultValue.setFullNameParent(this);
		}
	}

	@Override
	public Fieldspecification_types getFieldSpecificationType() {
		return Fieldspecification_types.FS_V_FT;
	}

	@Override
	public void setMyObjectClass(final ObjectClass_Definition objectClass) {
		super.setMyObjectClass(objectClass);
		if (null != fixedType) {
			fixedType.setMyScope(myObjectClass.getMyScope());
		}
		if (null != defaultValue) {
			defaultValue.setMyScope(myObjectClass.getMyScope());
		}
	}

	@Override
	public boolean hasDefault() {
		return hasDefault;
	}

	@Override
	public ISetting getDefault() {
		return defaultValue;
	}

	public IASN1Type getType() {
		return fixedType;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (null != fixedType) {
			fixedType.check(timestamp);

			if (null != defaultValue) {
				defaultValue.setMyGovernor(fixedType);
				final IValue tempValue = fixedType.checkThisValueRef(timestamp, defaultValue);
				fixedType.checkThisValue(timestamp, tempValue, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT,
						false, false, true, false, false));
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (null != fixedType) {
			fixedType.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (null != fixedType) {
			fixedType.addProposal(propCollector, i);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		if (fixedType != null && !fixedType.accept(v)) {
			return false;
		}
		if (defaultValue != null && !defaultValue.accept(v)) {
			return false;
		}
		return true;
	}
}
