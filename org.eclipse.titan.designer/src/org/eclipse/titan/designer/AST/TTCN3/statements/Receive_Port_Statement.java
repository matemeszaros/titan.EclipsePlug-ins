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
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody.OperationModes;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TypeSet;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Lexer4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Receive_Port_Statement extends Statement {
	private static final String MESSAGEBASEOPERATIONONPROCEDUREPORT = "Massage-based operation `{0}'' is not applicable"
			+ " to a procedure-based port of type `{1}''";
	private static final String NOINCOMINGMESSAGETYPES = "Port type `{0}'' does not have any incoming message types";
	private static final String VALUEREDIRECTWITHOUTRECEIVEPARAMETER = "Value redirect cannot be used without receive parameter";
	private static final String RECEIVEONPORT = "Message-based operation `{0}'' is not applicable to a procedure-based port of type `{1}''";
	private static final String UNKNOWNINCOMINGMESSAGE = "Cannot determine the type of the incoming message";
	private static final String TYPENOTPRESENT = "Message type `{0}'' is not present on the incoming list of port of type `{1}''";
	private static final String TYPEISAMBIGUOUS = "The type of the message is ambiguous:"
			+ " `{0}'' is compatible with more than one incoming message types of port type `{1}''";
	private static final String ANYPORTWITHPARAMETER = "Operation `any port.{0}'' cannot have parameter";
	private static final String RECEIVEWITHVALUEREDIRECT = "Operation `any port. {0}'' cannot have value redirect";

	private static final String FULLNAMEPART1 = ".portreference";
	private static final String FULLNAMEPART2 = ".receiveparameter";
	private static final String FULLNAMEPART3 = ".from";
	private static final String FULLNAMEPART4 = ".redirectvalue";
	private static final String FULLNAMEPART5 = ".redirectsender";
	private static final String STATEMENT_NAME = "receive";

	private final Reference portReference;
	private final TemplateInstance receiveParameter;
	private final TemplateInstance fromClause;
	private final Reference redirectValue;
	private final Reference redirectSender;

	public Receive_Port_Statement(final Reference portReference, final TemplateInstance receiveParameter, final TemplateInstance fromClause,
			final Reference redirectValue, final Reference redirectSender) {
		this.portReference = portReference;
		this.receiveParameter = receiveParameter;
		this.fromClause = fromClause;
		this.redirectValue = redirectValue;
		this.redirectSender = redirectSender;

		if (portReference != null) {
			portReference.setFullNameParent(this);
		}
		if (receiveParameter != null) {
			receiveParameter.setFullNameParent(this);
		}
		if (fromClause != null) {
			fromClause.setFullNameParent(this);
		}
		if (redirectValue != null) {
			redirectValue.setFullNameParent(this);
		}
		if (redirectSender != null) {
			redirectSender.setFullNameParent(this);
		}
	}

	public Reference getPort() {
		return portReference;
	}

	/**
	 * @return the type of the port used for this receive, or
	 *         <code>null</code> if used without port.
	 */
	public Port_Type getPortType() {
		return Port_Utility.checkPortReference(CompilationTimeStamp.getBaseTimestamp(), this, portReference);
	}

	/**
	 * @return the reference to redirect the received value, or
	 *         <code>null</code> if the statement does not redirect the
	 *         value.
	 */
	public Reference getRedirectValue() {
		return redirectValue;
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_RECEIVE;
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
		} else if (receiveParameter == child) {
			return builder.append(FULLNAMEPART2);
		} else if (fromClause == child) {
			return builder.append(FULLNAMEPART3);
		} else if (redirectValue == child) {
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
		if (receiveParameter != null) {
			receiveParameter.setMyScope(scope);
		}
		if (fromClause != null) {
			fromClause.setMyScope(scope);
		}
		if (redirectValue != null) {
			redirectValue.setMyScope(scope);
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

		checkReceivingStatement(timestamp, this, "receive", portReference, receiveParameter, fromClause, redirectValue, redirectSender);

		if (redirectValue != null) {
			redirectValue.setUsedOnLeftHandSide();
		}
		if (redirectSender != null) {
			redirectSender.setUsedOnLeftHandSide();
		}

		lastTimeChecked = timestamp;
	}

	/**
	 * Checks a port receiving statement.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param origin
	 *                the original statement.
	 * @param statement_name
	 *                the name of the original statement.
	 * @param portReference
	 *                the port reference.
	 * @param receiveParameter
	 *                the receiving parameter.
	 * @param fromClause
	 *                the from clause of the statement
	 * @param redirectValue
	 *                the redirection value of the statement.
	 * @param redirectSender
	 *                the sender redirection of the statement.
	 * */
	public static void checkReceivingStatement(final CompilationTimeStamp timestamp, final Statement origin, final String statement_name,
			final Reference portReference, final TemplateInstance receiveParameter, final TemplateInstance fromClause,
			final Reference redirectValue, final Reference redirectSender) {
		Port_Type portType = Port_Utility.checkPortReference(timestamp, origin, portReference);

		if (receiveParameter == null) {
			if (portType != null && Type_type.TYPE_PORT.equals(portType.getTypetype())) {
				PortTypeBody body = portType.getPortBody();
				if (OperationModes.OP_Procedure.equals(body.getOperationMode())) {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(MESSAGEBASEOPERATIONONPROCEDUREPORT, statement_name,
									portType.getTypename()));
				} else if (body.getInMessages() == null) {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(NOINCOMINGMESSAGETYPES, portType.getTypename()));
				}
			}

			if (redirectValue != null) {
				redirectValue.getLocation().reportSemanticError(VALUEREDIRECTWITHOUTRECEIVEPARAMETER);
				Port_Utility.checkValueRedirect(timestamp, redirectValue, null);
			}
		} else {
			// determine the type of the incoming message
			IType messageType = null;
			boolean messageTypeDetermined = false;
			final boolean[] valueRedirectChecked = new boolean[] { false };

			if (portType != null) {
				// the port type is known
				PortTypeBody portTypeBody = portType.getPortBody();
				TypeSet inMessages = portTypeBody.getInMessages();
				if (OperationModes.OP_Procedure.equals(portTypeBody.getOperationMode())) {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(RECEIVEONPORT, statement_name, portType.getTypename()));
				} else if (inMessages != null) {
					if (inMessages.getNofTypes() == 1) {
						messageType = inMessages.getTypeByIndex(0);
					} else {
						messageType = Port_Utility.getIncomingType(timestamp, receiveParameter, redirectValue,
								valueRedirectChecked);
						if (messageType == null) {
							receiveParameter.getLocation().reportSemanticError(UNKNOWNINCOMINGMESSAGE);
						} else {
							int nofCompatibleTypes = inMessages.getNofCompatibleTypes(timestamp, messageType);
							if (nofCompatibleTypes == 0) {
								receiveParameter.getLocation().reportSemanticError(
										MessageFormat.format(TYPENOTPRESENT, messageType.getTypename(),
												portType.getTypename()));
							} else if (nofCompatibleTypes > 1) {
								receiveParameter.getLocation().reportSemanticError(
										MessageFormat.format(TYPEISAMBIGUOUS, messageType.getTypename(),
												portType.getTypename()));
							}
						}
					}

					messageTypeDetermined = true;
				} else {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(NOINCOMINGMESSAGETYPES, portType.getTypename()));
				}
			} else if (portReference == null) {
				// any port
				receiveParameter.getLocation().reportSemanticError(MessageFormat.format(ANYPORTWITHPARAMETER, statement_name));
				if (redirectValue != null) {
					redirectValue.getLocation().reportSemanticError(
							MessageFormat.format(RECEIVEWITHVALUEREDIRECT, statement_name));
				}
			}

			if (!messageTypeDetermined) {
				messageType = Port_Utility.getIncomingType(timestamp, receiveParameter, redirectValue, valueRedirectChecked);
			}

			if (messageType != null) {
				receiveParameter.check(timestamp, messageType);
				if (!valueRedirectChecked[0]) {
					Port_Utility.checkValueRedirect(timestamp, redirectValue, messageType);
				}
			}
		}

		Port_Utility.checkFromClause(timestamp, origin, portType, fromClause, redirectSender);
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (redirectSender != null) {
			return null;
		}

		List<Integer> result = new ArrayList<Integer>();
		result.add(TTCN3Lexer4.SENDER);

		if (redirectValue != null) {
			return result;
		}

		result.add(TTCN3Lexer4.PORTREDIRECTSYMBOL);

		if (fromClause != null) {
			return result;
		}

		result.add(TTCN3Lexer4.FROM);

		if (receiveParameter != null) {
			return result;
		}

		result.add(TTCN3Lexer4.LPAREN);

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

		if (receiveParameter != null) {
			receiveParameter.updateSyntax(reparser, false);
			reparser.updateLocation(receiveParameter.getLocation());
		}

		if (fromClause != null) {
			fromClause.updateSyntax(reparser, false);
			reparser.updateLocation(fromClause.getLocation());
		}

		if (redirectValue != null) {
			redirectValue.updateSyntax(reparser, false);
			reparser.updateLocation(redirectValue.getLocation());
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
		if (receiveParameter != null) {
			receiveParameter.findReferences(referenceFinder, foundIdentifiers);
		}
		if (fromClause != null) {
			fromClause.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirectValue != null) {
			redirectValue.findReferences(referenceFinder, foundIdentifiers);
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
		if (receiveParameter != null && !receiveParameter.accept(v)) {
			return false;
		}
		if (fromClause != null && !fromClause.accept(v)) {
			return false;
		}
		if (redirectValue != null && !redirectValue.accept(v)) {
			return false;
		}
		if (redirectSender != null && !redirectSender.accept(v)) {
			return false;
		}
		return true;
	}
}
