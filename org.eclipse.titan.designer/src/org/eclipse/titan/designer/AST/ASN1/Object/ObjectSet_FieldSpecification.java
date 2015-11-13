/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ASN1.ObjectClass;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent an ObjectSetFieldSpec.
 * 
 * @author Kristof Szabados
 */
public final class ObjectSet_FieldSpecification extends FieldSpecification {

	private final ObjectClass objectClass;
	private final ObjectSet defaultObjectSet;

	public ObjectSet_FieldSpecification(final Identifier identifier, final ObjectClass objectClass, final boolean isOptional,
			final ObjectSet defaultObjectSet) {
		super(identifier, isOptional);
		this.objectClass = objectClass;
		this.defaultObjectSet = defaultObjectSet;

		if (null != defaultObjectSet) {
			defaultObjectSet.setFullNameParent(this);
		}
	}

	@Override
	public Fieldspecification_types getFieldSpecificationType() {
		return Fieldspecification_types.FS_OS;
	}

	@Override
	public void setMyObjectClass(final ObjectClass_Definition objectClass) {
		super.setMyObjectClass(objectClass);
		final Scope scope = myObjectClass.getMyScope();
		objectClass.setMyScope(scope);
		if (null != defaultObjectSet) {
			defaultObjectSet.setMyScope(scope);
		}
	}

	@Override
	public boolean hasDefault() {
		return null != defaultObjectSet;
	}

	@Override
	public ISetting getDefault() {
		return defaultObjectSet;
	}

	public ObjectClass getObjectClass() {
		return objectClass;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		objectClass.check(timestamp);
		if (null != defaultObjectSet) {
			defaultObjectSet.setMyGovernor(objectClass);
			defaultObjectSet.check(timestamp);
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (null != objectClass) {
			objectClass.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (null != objectClass) {
			objectClass.addProposal(propCollector, i);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		if (objectClass != null && !objectClass.accept(v)) {
			return false;
		}
		if (defaultObjectSet != null && !defaultObjectSet.accept(v)) {
			return false;
		}
		return true;
	}
}
