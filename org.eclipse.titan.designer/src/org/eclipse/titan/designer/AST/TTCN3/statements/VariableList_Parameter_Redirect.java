/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureFormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureFormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the parameter redirection of a getcall/getreply operation.
 * <p>
 * Provided with variable list notation
 * 
 * @author Kristof Szabados
 * */
public final class VariableList_Parameter_Redirect extends Parameter_Redirect {
	private static final String FULLNAMEPART = ".parametervariables";

	private final Variable_Entries entries;

	public VariableList_Parameter_Redirect(final Variable_Entries entries) {
		this.entries = entries;

		if (entries != null) {
			entries.setFullNameParent(this);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (entries == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (entries != null) {
			entries.setMyScope(scope);
		}
	}

	@Override
	public void checkErroneous(final CompilationTimeStamp timestamp) {
		for (int i = 0, size = entries.getNofEntries(); i < size; i++) {
			Variable_Entry entry = entries.getEntryByIndex(i);
			checkVariableReference(timestamp, entry.getReference(), null);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp, final Signature_Type signature, final boolean isOut) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		SignatureFormalParameterList parameterList = signature.getParameterList();
		if (parameterList.getNofParameters() == 0) {
			getLocation().reportSemanticError(MessageFormat.format(SIGNATUREWITHOUTPARAMETERS, signature.getTypename()));
			checkErroneous(timestamp);
			return;
		}

		int nofVariableEntries = entries.getNofEntries();
		int nofParameters = isOut ? parameterList.getNofOutParameters() : parameterList.getNofInParameters();
		if (nofVariableEntries != nofParameters) {
			getLocation().reportSemanticError(
					MessageFormat.format(
							"Too {0} variable entries compared to the number of {1}/inout parameters in signature `{2}'': {3} was expected instead of {4}",
							(nofVariableEntries > nofParameters) ? "many" : "few", isOut ? "out" : "in",
							signature.getTypename(), nofParameters, nofVariableEntries));
		}

		for (int i = 0; i < nofVariableEntries; i++) {
			Variable_Entry entry = entries.getEntryByIndex(i);
			if (i < nofParameters) {
				SignatureFormalParameter parameter = isOut ? parameterList.getOutParameterByIndex(i) : parameterList
						.getInParameterByIndex(i);
				checkVariableReference(timestamp, entry.getReference(), parameter.getType());
			} else {
				checkVariableReference(timestamp, entry.getReference(), null);
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		entries.updateSyntax(reparser, isDamaged);
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (entries == null) {
			return;
		}

		entries.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (entries != null && !entries.accept(v)) {
			return false;
		}
		return true;
	}
}
