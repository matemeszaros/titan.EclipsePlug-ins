/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferenceChainElement;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ASN1.Defined_Reference;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet;
import org.eclipse.titan.designer.AST.ASN1.values.Undefined_Block_Value;
import org.eclipse.titan.designer.AST.ISetting.Setting_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent an undefined FieldSpec.
 * 
 * @author Kristof Szabados
 */
public final class Undefined_FieldSpecification extends FieldSpecification implements IReferenceChainElement {
	private static final String CANNOTRECOGNISE = "Cannot recognize this fieldspecification";

	private final Defined_Reference governorReference;
	private final Reference defaultSetting1;
	private final Block mDefaultSetting;
	
	/** The classified field specification. */
	private FieldSpecification fieldSpecification;

	public Undefined_FieldSpecification(final Identifier identifier, final Defined_Reference reference, final boolean isOptional,
			final Reference defaultSetting) {
		super(identifier, isOptional);
		governorReference = reference;
		defaultSetting1 = defaultSetting;
		mDefaultSetting = null;
		
		if (null != governorReference) {
			governorReference.setFullNameParent(this);
		}
		if (null != defaultSetting1) {
			defaultSetting1.setFullNameParent(this);
		}
	}

	public Undefined_FieldSpecification(final Identifier identifier, final Defined_Reference reference, final boolean isOptional,
			final Block aDefaultSetting) {
		super(identifier, isOptional);
		governorReference = reference;
		defaultSetting1 = null;
		mDefaultSetting = aDefaultSetting;
		
		if (null != governorReference) {
			governorReference.setFullNameParent(this);
		}
		if (null != aDefaultSetting) {
			aDefaultSetting.setFullNameParent(this);
		}
	}

	@Override
	public String chainedDescription() {
		return getFullName();
	}

	@Override
	public Location getChainLocation() {
		return getLocation();
	}

	private void classifyFieldSpecification(final CompilationTimeStamp timestamp) {
		final IReferenceChain temporalReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);

		if (isOptional && (null != defaultSetting1 || null != mDefaultSetting)) {
			location.reportSemanticError("OPTIONAL and DEFAULT are mutually exclusive");
			isOptional = false;
		}

		if (temporalReferenceChain.add(this) && null != governorReference) {
			governorReference.setMyScope(myObjectClass.getMyScope());

			if (null != defaultSetting1) {
				defaultSetting1.setMyScope(myObjectClass.getMyScope());
			}

			if (identifier.isvalidAsnObjectSetFieldReference()
					&& governorReference.refersToSettingType(timestamp, Setting_type.S_OC, temporalReferenceChain)) {
				ObjectSet defaultObjectset = null;
				if (null != mDefaultSetting) {
					defaultObjectset = new ObjectSet_definition(mDefaultSetting);
				}
				final ObjectClass_refd oc = new ObjectClass_refd(governorReference);
				oc.setLocation(governorReference.getLocation());
				fieldSpecification = new ObjectSet_FieldSpecification(identifier, oc,
						isOptional, defaultObjectset);
			} else if (identifier.isvalidAsnObjectFieldReference()
					&& governorReference.refersToSettingType(timestamp, Setting_type.S_OC, temporalReferenceChain)) {
				ASN1Object defaultObject = null;
				if (null != defaultSetting1) {
					defaultObject = new ReferencedObject(defaultSetting1);
				} else if (null != mDefaultSetting) {
					defaultObject = new Object_Definition(mDefaultSetting);
				}

				fieldSpecification = new Object_FieldSpecification(identifier, new ObjectClass_refd(governorReference), isOptional,
						defaultObject);
			} else if (identifier.isvalidAsnValueFieldReference()
					&& (governorReference.refersToSettingType(timestamp, Setting_type.S_T, temporalReferenceChain) || governorReference
							.refersToSettingType(timestamp, Setting_type.S_VS, temporalReferenceChain))) {
				IValue defaultValue = null;

				if (null != defaultSetting1) {
					if (defaultSetting1 instanceof Defined_Reference && null == defaultSetting1.getModuleIdentifier()) {
						defaultValue = new Undefined_LowerIdentifier_Value(defaultSetting1.getId().newInstance());
					} else {
						defaultValue = new Referenced_Value(defaultSetting1);
					}
				} else if (null != mDefaultSetting) {
					defaultValue = new Undefined_Block_Value(mDefaultSetting);
				}
				fieldSpecification = new FixedTypeValue_FieldSpecification(identifier, new Referenced_Type(governorReference), false,
						isOptional, null != defaultSetting1 && null != mDefaultSetting, defaultValue);
			}
		}

		if (null == fieldSpecification) {
			location.reportSemanticError(CANNOTRECOGNISE);
			fieldSpecification = new Erroneous_FieldSpecification(identifier, isOptional, null != defaultSetting1
					|| null != mDefaultSetting);
		} else {
			if (null != myObjectClass) {
				fieldSpecification.setMyObjectClass(myObjectClass);
			}
		}

		fieldSpecification.setFullNameParent(getNameParent());
		fieldSpecification.setLocation(location);

		temporalReferenceChain.release();
	}

	@Override
	public Fieldspecification_types getFieldSpecificationType() {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return Fieldspecification_types.FS_UNDEFINED;
	}

	@Override
	public void setMyObjectClass(final ObjectClass_Definition objectClass) {
		super.setMyObjectClass(objectClass);
		if (fieldSpecification != null) {
			fieldSpecification.setMyObjectClass(objectClass);
		}
	}

	@Override
	public boolean hasDefault() {
		if (fieldSpecification != null) {
			return fieldSpecification.hasDefault();
		}

		return (defaultSetting1 != null) || (mDefaultSetting != null);
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		fieldSpecification = null;
		classifyFieldSpecification(timestamp);

		if (fieldSpecification != null) {
			fieldSpecification.check(timestamp);
		}

		lastTimeChecked = timestamp;
	}

	public FieldSpecification getRealFieldSpecification() {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return fieldSpecification;
	}

	@Override
	public ISetting getDefault() {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return fieldSpecification.getDefault();
	}

	@Override
	public FieldSpecification getLast() {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return fieldSpecification.getLast();
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (fieldSpecification != null) {
			fieldSpecification.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (fieldSpecification != null) {
			fieldSpecification.addProposal(propCollector, i);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		if (fieldSpecification != null && !fieldSpecification.accept(v)) {
			return false;
		}
		return true;
	}
}
