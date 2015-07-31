/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Error_Setting;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class Erroneous_FieldSpecification extends FieldSpecification {

	private ISetting settingError;
	private final boolean hasDefaultFlag;

	public Erroneous_FieldSpecification(final Identifier identifier, final boolean isOptional, final boolean hasDefault) {
		super(identifier, isOptional);
		hasDefaultFlag = hasDefault;
	}

	@Override
	public Fieldspecification_types getFieldSpecificationType() {
		return Fieldspecification_types.FS_ERROR;
	}

	@Override
	public boolean hasDefault() {
		return hasDefaultFlag;
	}

	@Override
	public ISetting getDefault() {
		if (null == settingError && hasDefaultFlag) {
			settingError = new Error_Setting();
		}
		return settingError;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subreferences = declarationCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() == i + 1) {
				declarationCollector.addDeclaration(identifier.getDisplayName(), getLocation(), this);
			}
		}
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		final List<ISubReference> subreferences = propCollector.getReference().getSubreferences();
		if (subreferences.size() <= i) {
			return;
		}

		final ISubReference subreference = subreferences.get(i);
		if (Subreference_type.fieldSubReference.equals(subreference.getReferenceType())) {
			if (subreferences.size() == i + 1) {
				propCollector.addProposal(identifier, " - unknown fieldspeciication", null, "unknown fieldspeciication");
			}
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		return true;
	}
}
