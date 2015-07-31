/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ControlPart;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReparseUtilities;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The Definition_Statement class represents the statements of TTCN3
 * definitions.
 * 
 * @author Kristof Szabados
 * */
public final class Definition_Statement extends Statement {
	private static final String FULLNAMEPART = ".def";
	private static final String STATEMENT_NAME = "definition";

	/**
	 * the definition created with this statement.
	 * <p>
	 * This can be null
	 * */
	private final Definition definition;

	/**
	 * The definition in which this statement is declared.
	 * <p>
	 * This can be null.
	 * */
	private Definition myDefinition;

	public Definition_Statement(final Definition definition) {
		this.definition = definition;
		if (definition != null) {
			definition.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_DEF;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (definition == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	public Definition getDefinition() {
		return definition;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (definition != null) {
			definition.setMyScope(scope);
		}
	}

	@Override
	public void setMyDefinition(final Definition definition) {
		myDefinition = definition;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (definition != null) {
			if (myDefinition == null) {
				ControlPart controlPart = myScope.getControlPart();
				if (controlPart != null) {
					definition.setAttributeParentPath(controlPart.getAttributePath());
				}
			} else {
				definition.setAttributeParentPath(myDefinition.getAttributePath());
			}
			definition.check(timestamp);

			if (myStatementBlock != null) {
				myStatementBlock.registerDefinition(timestamp, definition);
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void postCheck() {
		if (definition != null) {
			definition.postCheck();
		}
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		return ReparseUtilities.getAllValidTokenTypes();
	}

	@Override
	public List<Integer> getPossiblePrefixTokens() {
		if (definition != null) {
			return definition.getPossiblePrefixTokens();
		}

		return new ArrayList<Integer>(0);
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (definition != null) {
			definition.updateSyntax(reparser, false);
			reparser.updateLocation(definition.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (definition != null) {
			definition.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (definition != null && !definition.accept(v)) {
			return false;
		}
		return true;
	}
}
