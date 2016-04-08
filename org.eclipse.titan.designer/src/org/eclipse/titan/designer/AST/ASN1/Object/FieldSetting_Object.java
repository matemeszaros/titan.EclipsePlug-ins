/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.ObjectClass;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldSpecification.Fieldspecification_types;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent object FieldSettings.
 * 
 * @author Kristof Szabados
 */
public final class FieldSetting_Object extends FieldSetting {

	private static final String OBJECTEXPECTED = "Object setting was expected";

	private ASN1Object object;

	public FieldSetting_Object(final Identifier identifier, final ASN1Object object) {
		super(identifier);
		this.object = object;

		if (null != object) {
			object.setFullNameParent(this);
		}
	}

	@Override
	public FieldSetting_Object newInstance() {
		return new FieldSetting_Object(name.newInstance(), object.newInstance());
	}

	@Override
	public ASN1Object getSetting() {
		return object;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp, final FieldSpecification fieldSpecification) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (!Fieldspecification_types.FS_O.equals(fieldSpecification.getFieldSpecificationType())) {
			location.reportSemanticError(OBJECTEXPECTED);
			object = new Object_Definition(null);
			object.setFullNameParent(this);
		}

		final Object_FieldSpecification fs = (Object_FieldSpecification) fieldSpecification;
		final ObjectClass oc = fs.getObjectClass();
		object.setMyGovernor(oc);

		lastTimeChecked = timestamp;

		object.check(timestamp);
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (null != object) {
			object.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (null != object) {
			object.addProposal(propCollector, i);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (name != null && !name.accept(v)) {
			return false;
		}
		if (object != null && !object.accept(v)) {
			return false;
		}
		return true;
	}
}
