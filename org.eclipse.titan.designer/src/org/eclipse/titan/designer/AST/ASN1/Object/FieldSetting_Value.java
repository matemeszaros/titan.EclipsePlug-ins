/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Error_Setting;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent value FieldSettings.
 * 
 * @author Kristof Szabados
 */
// FIXME enhance when values become available
public final class FieldSetting_Value extends FieldSetting {

	public FieldSetting_Value(final Identifier name /* , Value setting */) {
		super(name);
	}

	@Override
	public FieldSetting newInstance() {
		return new FieldSetting_Value(name.newInstance());
	}

	@Override
	public Error_Setting getSetting() {
		return new Error_Setting();
	}

	@Override
	public void check(final CompilationTimeStamp timestamp, final FieldSpecification fieldSpecification) {
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (name != null && !name.accept(v)) {
			return false;
		}
		return true;
	}
}
