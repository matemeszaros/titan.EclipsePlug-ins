/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody.OperationModes;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TypeSet;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Getcall_Statement extends Statement {
	private static final String SIGNATUREPARAMETEREXPECTED = "The type of parameter is `{0}'', which is not a signature";
	private static final String ANYWITHREDIRECT = "Operation `any port.{0}'' cannot have parameter redirect";
	private static final String ANYWITHPARAMETER = "operation `any port.{0}'' cannot have parameter";
	private static final String SIGNATURENOTPRESENT = "Signature `{0}'' is not present on the incoming list of port type `{1}''";
	private static final String UNKNOWNSIGNATURETYPE = "Cannot determine the type of the signature";
	private static final String MESSAGEBASEDPORT = "Procedure-based operation `{0}'' is not applicable to a massage-based port of type `{1}''";
	private static final String NOINSIGNATURES = "Port type `{0}'' does not have any incoming signatures";
	private static final String REDIRECTWITHOUTSIGNATURE = "Parameter redirect cannot be used without signature template";

	private static final String FULLNAMEPART1 = ".portreference";
	private static final String FULLNAMEPART2 = ".parameter";
	private static final String FULLNAMEPART3 = ".from";
	private static final String FULLNAMEPART4 = ".parameters";
	private static final String FULLNAMEPART5 = ".redirectSender";
	private static final String STATEMENT_NAME = "getcall";

	private final Reference portReference;
	private final TemplateInstance parameter;
	private final TemplateInstance fromClause;
	private final Parameter_Redirect redirectParameter;
	private final Reference redirectSender;

	public Getcall_Statement(final Reference portReference, final TemplateInstance parameter, final TemplateInstance fromClause,
			final Parameter_Redirect redirectParameter, final Reference redirectSender) {
		this.portReference = portReference;
		this.parameter = parameter;
		this.fromClause = fromClause;
		this.redirectParameter = redirectParameter;
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
		if (redirectParameter != null) {
			redirectParameter.setFullNameParent(this);
		}
		if (redirectSender != null) {
			redirectSender.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_GETCALL;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (portReference == child) {
			return builder.append(FULLNAMEPART1);
		} else if (parameter == child) {
			return builder.append(FULLNAMEPART2);
		} else if (fromClause == child) {
			return builder.append(FULLNAMEPART3);
		} else if (redirectParameter == child) {
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
		if (redirectParameter != null) {
			redirectParameter.setMyScope(scope);
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

		checkGetcallStatement(timestamp, this, "getcall", portReference, parameter, fromClause, redirectParameter, redirectSender);

		if (redirectSender != null) {
			redirectSender.setUsedOnLeftHandSide();
		}

		lastTimeChecked = timestamp;
	}

	public static void checkGetcallStatement(final CompilationTimeStamp timestamp, final Statement statement, final String statementName,
			final Reference portReference, final TemplateInstance parameter, final TemplateInstance fromClause,
			final Parameter_Redirect redirect, final Reference redirectSender) {
		final Port_Type portType = Port_Utility.checkPortReference(timestamp, statement, portReference);

		if (parameter == null) {
			if (portType != null) {
				final PortTypeBody body = portType.getPortBody();
				if (OperationModes.OP_Message.equals(body.getOperationMode())) {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(MESSAGEBASEDPORT, statementName, portType.getTypename()));
				} else if (body.getInSignatures() == null) {
					portReference.getLocation().reportSemanticError(MessageFormat.format(NOINSIGNATURES, portType.getTypename()));
				}
			}

			if (redirect != null) {
				redirect.getLocation().reportSemanticError(REDIRECTWITHOUTSIGNATURE);
				redirect.checkErroneous(timestamp);
			}
		} else {
			IType signature = null;
			boolean signatureDetermined = false;
			if (portType != null) {

				final PortTypeBody body = portType.getPortBody();
				final TypeSet inSignatures = body.getInSignatures();
				if (OperationModes.OP_Message.equals(body.getOperationMode())) {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(MESSAGEBASEDPORT, statementName, portType.getTypename()));
				} else if (inSignatures != null) {
					if (inSignatures.getNofTypes() == 1) {
						signature = inSignatures.getTypeByIndex(0);
					} else {
						signature = Port_Utility.getOutgoingType(timestamp, parameter);

						if (signature == null) {
							parameter.getLocation().reportSemanticError(UNKNOWNSIGNATURETYPE);
						} else {
							if (!inSignatures.hasType(timestamp, signature)) {
								parameter.getLocation().reportSemanticError(
										MessageFormat.format(SIGNATURENOTPRESENT, signature.getTypename(),
												portType.getTypename()));
							}
						}
					}

					signatureDetermined = true;
				} else {
					portReference.getLocation().reportSemanticError(MessageFormat.format(NOINSIGNATURES, portType.getTypename()));
				}
			} else if (portReference == null) {
				// any port is referenced, or there was a syntax
				// error
				parameter.getLocation().reportSemanticError(MessageFormat.format(ANYWITHPARAMETER, statementName));
				if (redirect != null) {
					redirect.getLocation().reportSemanticError(MessageFormat.format(ANYWITHREDIRECT, statementName));
				}
			}

			if (!signatureDetermined) {
				signature = Port_Utility.getOutgoingType(timestamp, parameter);
			}

			if (signature != null) {
				parameter.check(timestamp, signature);

				signature = signature.getTypeRefdLast(timestamp);
				switch (signature.getTypetype()) {
				case TYPE_SIGNATURE:
					if (redirect != null) {
						redirect.check(timestamp, (Signature_Type) signature, false);
					}
					break;
				default:
					parameter.getLocation().reportSemanticError(
							MessageFormat.format(SIGNATUREPARAMETEREXPECTED, signature.getTypename()));
					if (redirect != null) {
						redirect.checkErroneous(timestamp);
					}
					break;
				}
			}
		}

		Port_Utility.checkFromClause(timestamp, statement, portType, fromClause, redirectSender);
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (redirectSender != null) {
			return null;
		}

		final List<Integer> result = new ArrayList<Integer>();
		result.add(Ttcn3Lexer.SENDER);

		if (redirectParameter != null) {
			return result;
		}

		result.add(Ttcn3Lexer.PORTREDIRECTSYMBOL);

		if (fromClause != null) {
			return result;
		}

		result.add(Ttcn3Lexer.FROM);

		if (parameter != null) {
			return result;
		}

		result.add(Ttcn3Lexer.LPAREN);

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

		if (redirectParameter != null) {
			redirectParameter.updateSyntax(reparser, false);
			reparser.updateLocation(redirectParameter.getLocation());
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
		if (redirectParameter != null) {
			redirectParameter.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirectSender != null) {
			redirectSender.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (portReference != null && !portReference.accept(v)) {
			return false;
		}
		if (parameter != null && !parameter.accept(v)) {
			return false;
		}
		if (fromClause != null && !fromClause.accept(v)) {
			return false;
		}
		if (redirectParameter != null && !redirectParameter.accept(v)) {
			return false;
		}
		if (redirectSender != null && !redirectSender.accept(v)) {
			return false;
		}
		return true;
	}
}
