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
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3LexerTokenTypes;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Check_Getcall_Statement extends Statement {
	private static final String FULLNAMEPART1 = ".portreference";
	private static final String FULLNAMEPART2 = ".parameter";
	private static final String FULLNAMEPART3 = ".from";
	private static final String FULLNAMEPART4 = ".parameters";
	private static final String FULLNAMEPART5 = ".redirectSender";
	private static final String STATEMENT_NAME = "check-getcall";

	private final Reference portReference;
	private final TemplateInstance parameter;
	private final TemplateInstance fromClause;
	private final Parameter_Redirect redirect;
	private final Reference redirectSender;

	public Check_Getcall_Statement(final Reference portReference, final TemplateInstance parameter, final TemplateInstance fromClause,
			final Parameter_Redirect redirect, final Reference redirectSender) {
		this.portReference = portReference;
		this.parameter = parameter;
		this.fromClause = fromClause;
		this.redirect = redirect;
		this.redirectSender = redirectSender;

		if (portReference != null) {
			portReference.setFullNameParent(this);
		}
		if (parameter != null) {
			parameter.setFullNameParent(this);
		}
		if (fromClause != null) {
			fromClause.setFullNameParent(this);
		}
		if (redirect != null) {
			redirect.setFullNameParent(this);
		}
		if (redirectSender != null) {
			redirectSender.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_CHECK_GETCALL;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (portReference == child) {
			return builder.append(FULLNAMEPART1);
		} else if (parameter == child) {
			return builder.append(FULLNAMEPART2);
		} else if (fromClause == child) {
			return builder.append(FULLNAMEPART3);
		} else if (redirect == child) {
			return builder.append(FULLNAMEPART4);
		} else if (redirectSender == child) {
			return builder.append(FULLNAMEPART5);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (portReference != null) {
			portReference.setMyScope(scope);
		}
		if (parameter != null) {
			parameter.setMyScope(scope);
		}
		if (fromClause != null) {
			fromClause.setMyScope(scope);
		}
		if (redirect != null) {
			redirect.setMyScope(scope);
		}
		if (redirectSender != null) {
			redirectSender.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		Getcall_Statement.checkGetcallStatement(timestamp, this, "check-getcall", portReference, parameter, fromClause, redirect,
				redirectSender);

		if (redirectSender != null) {
			redirectSender.setUsedOnLeftHandSide();
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (redirectSender != null) {
			return null;
		}

		List<Integer> result = new ArrayList<Integer>();
		result.add(TTCN3LexerTokenTypes.SENDER);

		if (redirect != null) {
			return result;
		}

		result.add(TTCN3LexerTokenTypes.PORTREDIRECTSYMBOL);

		if (fromClause != null) {
			return result;
		}

		result.add(TTCN3LexerTokenTypes.FROM);

		if (parameter != null) {
			return result;
		}

		result.add(TTCN3LexerTokenTypes.LPAREN);

		return result;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (portReference != null) {
			portReference.updateSyntax(reparser, false);
			reparser.updateLocation(portReference.getLocation());
		}

		if (parameter != null) {
			parameter.updateSyntax(reparser, false);
			reparser.updateLocation(parameter.getLocation());
		}

		if (fromClause != null) {
			fromClause.updateSyntax(reparser, false);
			reparser.updateLocation(fromClause.getLocation());
		}

		if (redirect != null) {
			redirect.updateSyntax(reparser, false);
			reparser.updateLocation(redirect.getLocation());
		}

		if (redirectSender != null) {
			redirectSender.updateSyntax(reparser, false);
			reparser.updateLocation(redirectSender.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (portReference != null) {
			portReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (parameter != null) {
			parameter.findReferences(referenceFinder, foundIdentifiers);
		}
		if (fromClause != null) {
			fromClause.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirect != null) {
			redirect.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirectSender != null) {
			redirectSender.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (portReference != null && !portReference.accept(v)) {
			return false;
		}
		if (parameter != null && !parameter.accept(v)) {
			return false;
		}
		if (fromClause != null && !fromClause.accept(v)) {
			return false;
		}
		if (redirect != null && !redirect.accept(v)) {
			return false;
		}
		if (redirectSender != null && !redirectSender.accept(v)) {
			return false;
		}
		return true;
	}
}
