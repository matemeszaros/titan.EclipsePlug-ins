/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3LexerTokenTypes;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Done_Statement extends Statement {
	private static final String FULLNAMEPART1 = "componentreference";
	private static final String FULLNAMEPART2 = "donematch";
	private static final String FULLNAMEPART3 = "redirection";
	private static final String STATEMENT_NAME = "done";

	private final Value componentreference;
	private final TemplateInstance doneMatch;
	private final Reference redirect;

	public Done_Statement(final Value componentreference, final TemplateInstance doneMatch, final Reference redirect) {
		this.componentreference = componentreference;
		this.doneMatch = doneMatch;
		this.redirect = redirect;

		if (componentreference != null) {
			componentreference.setFullNameParent(this);
		}
		if (doneMatch != null) {
			doneMatch.setFullNameParent(this);
		}
		if (redirect != null) {
			redirect.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_DONE;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (componentreference == child) {
			return builder.append(FULLNAMEPART1);
		} else if (doneMatch == child) {
			return builder.append(FULLNAMEPART2);
		} else if (redirect == child) {
			return builder.append(FULLNAMEPART3);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (componentreference != null) {
			componentreference.setMyScope(scope);
		}
		if (doneMatch != null) {
			doneMatch.setMyScope(scope);
		}
		if (redirect != null) {
			redirect.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		Port_Utility.checkComponentReference(timestamp, this, componentreference, false, false, false);

		if (componentreference == null) {
			lastTimeChecked = timestamp;
			return;
		}

		if (doneMatch != null) {
			final boolean[] valueRedirectChecked = new boolean[] { false };
			IType returnType = Port_Utility.getIncomingType(timestamp, doneMatch, redirect, valueRedirectChecked);
			if (returnType == null) {
				doneMatch.getLocation().reportSemanticError("Cannot determine the return type for value returning done");
			} else {
				IType lastType = returnType;
				boolean returnTypeCorrect = false;
				while (!returnTypeCorrect) {
					if (lastType.hasDoneAttribute()) {
						returnTypeCorrect = true;
						break;
					}
					if (lastType instanceof IReferencingType) {
						IReferenceChain refChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
						IType refd = ((IReferencingType) lastType).getTypeRefd(timestamp, refChain);
						refChain.release();
						if (lastType != refd) {
							lastType = refd;
						} else {
							break;
						}
					} else {
						break;
					}
				}

				if (!returnTypeCorrect) {
					location.reportSemanticError(MessageFormat.format(
							"Return type `{0}'' does not have `done'' extension attibute", returnType.getTypename()));
					returnType.setIsErroneous(true);
				}

				doneMatch.check(timestamp, returnType);
				if (!valueRedirectChecked[0]) {
					Port_Utility.checkValueRedirect(timestamp, redirect, returnType);
				}
			}

		} else if (redirect != null) {
			redirect.getLocation().reportSemanticError("Redirect cannot be used for the return value without a matching template");
			Port_Utility.checkValueRedirect(timestamp, redirect, null);
			redirect.setUsedOnLeftHandSide();
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (redirect != null) {
			return null;
		}

		List<Integer> result = new ArrayList<Integer>();
		result.add(TTCN3LexerTokenTypes.PORTREDIRECTSYMBOL);

		if (doneMatch != null) {
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

		if (componentreference != null) {
			componentreference.updateSyntax(reparser, false);
			reparser.updateLocation(componentreference.getLocation());
		}

		if (doneMatch != null) {
			doneMatch.updateSyntax(reparser, false);
			reparser.updateLocation(doneMatch.getLocation());
		}

		if (redirect != null) {
			redirect.updateSyntax(reparser, false);
			reparser.updateLocation(redirect.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentreference != null) {
			componentreference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (doneMatch != null) {
			doneMatch.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirect != null) {
			redirect.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (componentreference != null && !componentreference.accept(v)) {
			return false;
		}
		if (doneMatch != null && !doneMatch.accept(v)) {
			return false;
		}
		if (redirect != null && !redirect.accept(v)) {
			return false;
		}
		return true;
	}
}
