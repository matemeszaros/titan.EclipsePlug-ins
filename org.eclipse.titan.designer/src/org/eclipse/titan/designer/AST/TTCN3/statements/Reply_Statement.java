/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
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
public final class Reply_Statement extends Statement {
	private static final String REPLYONMESSAGEPORT = "Procedure-based operation `reply'' is not applicable to a message-based port of type `{0}''";
	private static final String UNKNOWNINCOMINGSIGNATURE = "Cannot determine the type of the signature";
	private static final String INCOMINGSIGNATURENOTPRESENT = "Signature `{0}'' is not present on the incoming list of port type `{1}''";
	private static final String NOINCOMINGSIGNATURES = "Port type `{0}'' does not have any incoming signatures";

	private static final String FULLNAMEPART1 = ".portreference";
	private static final String FULLNAMEPART2 = ".sendparameter";
	private static final String FULLNAMEPART3 = ".replyvalue";
	private static final String FULLNAMEPART4 = ".to";
	private static final String STATEMENT_NAME = "reply";

	private final Reference portReference;
	private final TemplateInstance parameter;
	private final Value replyValue;
	private final IValue toClause;

	public Reply_Statement(final Reference portReference, final TemplateInstance parameter, final Value replyValue, final IValue toClause) {
		this.portReference = portReference;
		this.parameter = parameter;
		this.replyValue = replyValue;
		this.toClause = toClause;

		if (portReference != null) {
			portReference.setFullNameParent(this);
		}
		if (parameter != null) {
			parameter.setFullNameParent(this);
		}
		if (replyValue != null) {
			replyValue.setFullNameParent(this);
		}
		if (toClause != null) {
			toClause.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_REPLY;
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
		} else if (replyValue == child) {
			return builder.append(FULLNAMEPART3);
		} else if (toClause == child) {
			return builder.append(FULLNAMEPART4);
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
		if (replyValue != null) {
			replyValue.setMyScope(scope);
		}
		if (toClause != null) {
			toClause.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		final Port_Type portType = Port_Utility.checkPortReference(timestamp, this, portReference);

		IType signature = null;
		boolean signatureDetermined = false;
		if (portType != null) {
			// the port type is known
			final PortTypeBody portTypeBody = portType.getPortBody();
			final TypeSet inSignatures = portTypeBody.getInSignatures();

			if (OperationModes.OP_Message.equals(portTypeBody.getOperationMode())) {
				portReference.getLocation().reportSemanticError(MessageFormat.format(REPLYONMESSAGEPORT, portType.getTypename()));
			} else if (inSignatures != null) {
				if (inSignatures.getNofTypes() == 1) {
					signature = inSignatures.getTypeByIndex(0);
				} else {
					signature = Port_Utility.getOutgoingType(timestamp, parameter);
					if (signature == null) {
						parameter.getLocation().reportSemanticError(UNKNOWNINCOMINGSIGNATURE);
					} else {
						if (!inSignatures.hasType(timestamp, signature)) {
							parameter.getLocation().reportSemanticError(
									MessageFormat.format(INCOMINGSIGNATURENOTPRESENT, signature.getTypename(),
											portType.getTypename()));
						}
					}
				}

				signatureDetermined = true;
			} else {
				portReference.getLocation().reportSemanticError(MessageFormat.format(NOINCOMINGSIGNATURES, portType.getTypename()));
			}
		}

		if (!signatureDetermined) {
			signature = Port_Utility.getOutgoingType(timestamp, parameter);
		}

		if (signature != null) {
			parameter.check(timestamp, signature);
			signature = signature.getTypeRefdLast(timestamp);

			Type returnType = null;
			switch (signature.getTypetype()) {
			case TYPE_SIGNATURE:
				if (((Signature_Type) signature).isNonblocking()) {
					getLocation().reportSemanticError(
							MessageFormat.format("Operation `reply'' is not applicable to non-blocking signature `{0}''",
									signature.getTypename()));
				} else {
					returnType = ((Signature_Type) signature).getSignatureReturnType();
				}
				// checking the presence/absence of reply value
				if (replyValue != null) {
					if (returnType == null) {
						final String message = MessageFormat.format(
								"Unexpected return value. Signature `{0}'' does not have return type",
								signature.getTypename());
						replyValue.getLocation().reportSemanticError(message);
					}
				} else if (returnType != null) {
					getLocation().reportSemanticError(
							MessageFormat.format("Missing return value. Signature `{0}'' returns type `{1}''",
									signature.getTypename(), returnType.getTypename()));
				}
				break;
			default:
				parameter.getLocation().reportSemanticError(
						MessageFormat.format("The type of parameter is `{0}'', which is not a signature",
								signature.getTypename()));
				break;
			}

			// checking the reply value if present
			if (replyValue != null && returnType != null) {
				replyValue.setMyGovernor(returnType);
				final IValue temp = returnType.checkThisValueRef(timestamp, replyValue);
				returnType.checkThisValue(timestamp, temp, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE,
						false, false, true, false, false));
			}

			Port_Utility.checkToClause(timestamp, this, portType, toClause);
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (toClause != null) {
			return null;
		}

		final List<Integer> result = new ArrayList<Integer>();
		result.add(Ttcn3Lexer.TO);

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

		if (replyValue != null) {
			replyValue.updateSyntax(reparser, false);
			reparser.updateLocation(replyValue.getLocation());
		}

		if (toClause instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) toClause).updateSyntax(reparser, false);
			reparser.updateLocation(toClause.getLocation());
		} else if (toClause != null) {
			throw new ReParseException();
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
		if (replyValue != null) {
			replyValue.findReferences(referenceFinder, foundIdentifiers);
		}
		if (toClause != null) {
			toClause.findReferences(referenceFinder, foundIdentifiers);
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
		if (replyValue != null && !replyValue.accept(v)) {
			return false;
		}
		if (toClause != null && !toClause.accept(v)) {
			return false;
		}
		return true;
	}
}
