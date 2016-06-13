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
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard.altguard_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody.OperationModes;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TypeSet;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The Call_Statement class represents a TTCN3 port call operation statement.
 * 
 * @author Kristof Szabados
 * */
public final class Call_Statement extends Statement {
	private static final String SENDONPORT = "Procedure-based operation `call'' is not applicable to a message-based port of type `{0}''";
	private static final String UNKNOWNSIGNATURE = "Cannot determine the type of the signature";
	private static final String TYPENOTPRESENT = "Signature type `{0}'' is not present on the outgoing list of port type `{1}''";
	private static final String NOOUTGOINGSIGNATURETYPES = "Port type `{0}'' does not have any outgoing signature types";
	private static final String CALLPARAMETERNOTSIGNATURE = "The type of call parameter is `{0}'' type, which is not a signature";
	private static final String NONBLOCKINGWITHTIMER = "A call of a non-blocking signature `{0}'' cannot have a call timer";
	private static final String NONBLOCKINGWITHNOWAIT = "A call of a non-blocking signature `{0}'' cannot use the `nowait'' keyword";
	private static final String NONBLOCKINGWITHRESPONSEPART = "A call of a non-blocking signature `{0}'' cannot have response"
			+ " and exception handling part";
	private static final String NOWAITWITHRESPONSEPART = "A call with `nowait' keyword cannot have response and exception handling part";
	private static final String RESPONSEPARTMISSING = "Response and exception handling part is missing from blocking call operation";
	private static final String CALLTIMERNEGATIVE = "The call timer has negative duration: `{0}''";
	private static final String FLOATTIMEREXPECTED = "The timer operand of the `call' operation should be a float value";
	private static final String GETRPELYTOWRONGSIGNATURE = "The `getreply'' operation refers to a different signature"
			+ " than the previous `call'' statement: `{0}'' was expected instead of `{1}''";

	private static final String FULLNAMEPART1 = ".port_reference";
	private static final String FULLNAMEPART2 = ".call_parameter";
	private static final String FULLNAMEPART3 = ".timer";
	private static final String FULLNAMEPART4 = ".to";
	private static final String FULLNAMEPART5 = ".body";
	private static final String STATEMENT_NAME = "call";

	// The reference pointing to the port.
	private final Reference portReference;

	// The parameter of the call operation.
	private final TemplateInstance parameter;

	// The value of the timer parameter.
	private final Value timerValue;

	// The no wait option set or not.
	private final boolean noWait;

	// The to clause of the statement.
	private final IValue toClause;

	/**
	 * The guards found in the statementblock of the port call statement.
	 * <p>
	 * This can be null
	 * */
	private final AltGuards altGuards;

	public Call_Statement(final Reference portReference, final TemplateInstance parameter, final Value timerValue, final boolean noWait,
			final IValue toClause, final AltGuards altGuards) {
		this.portReference = portReference;
		this.parameter = parameter;
		this.timerValue = timerValue;
		this.noWait = noWait;
		this.toClause = toClause;
		this.altGuards = altGuards;

		if (portReference != null) {
			portReference.setFullNameParent(this);
		}
		if (parameter != null) {
			parameter.setFullNameParent(this);
		}
		if (timerValue != null) {
			timerValue.setFullNameParent(this);
		}
		if (toClause != null) {
			toClause.setFullNameParent(this);
		}
		if (altGuards != null) {
			altGuards.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_CALL;
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
		} else if (timerValue == child) {
			return builder.append(FULLNAMEPART3);
		} else if (toClause == child) {
			return builder.append(FULLNAMEPART4);
		} else if (altGuards == child) {
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
		if (timerValue != null) {
			timerValue.setMyScope(scope);
		}
		if (toClause != null) {
			toClause.setMyScope(scope);
		}
		if (altGuards != null) {
			altGuards.setMyScope(scope);
		}
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		if (altGuards != null) {
			altGuards.setMyStatementBlock(statementBlock, index);
		}
	}

	@Override
	public void setMyDefinition(final Definition definition) {
		if (altGuards != null) {
			altGuards.setMyDefinition(definition);
		}
	}

	@Override
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (altGuards != null) {
			altGuards.hasReturn(timestamp);
		}

		return StatementBlock.ReturnStatus_type.RS_NO;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		final Port_Type portType = Port_Utility.checkPortReference(timestamp, this, portReference);

		if (parameter == null) {
			return;
		}

		IType signatureType = null;
		boolean signatureTypeDetermined = false;
		if (portType != null) {
			// the port type is known
			final PortTypeBody portTypeBody = portType.getPortBody();
			final TypeSet outSignatures = portTypeBody.getOutSignatures();
			if (OperationModes.OP_Message.equals(portTypeBody.getOperationMode())) {
				portReference.getLocation().reportSemanticError(MessageFormat.format(SENDONPORT, portType.getTypename()));
			} else if (outSignatures != null) {
				if (outSignatures.getNofTypes() == 1) {
					signatureType = outSignatures.getTypeByIndex(0);
				} else {
					signatureType = Port_Utility.getOutgoingType(timestamp, parameter);
					if (signatureType == null) {
						parameter.getLocation().reportSemanticError(UNKNOWNSIGNATURE);
					} else {
						if (!outSignatures.hasType(timestamp, signatureType)) {
							parameter.getLocation().reportSemanticError(
									MessageFormat.format(TYPENOTPRESENT, signatureType.getTypename(),
											portType.getTypename()));
						}
					}
				}

				signatureTypeDetermined = true;
			} else {
				portReference.getLocation().reportSemanticError(
						MessageFormat.format(NOOUTGOINGSIGNATURETYPES, portType.getTypename()));
			}
		}

		if (!signatureTypeDetermined) {
			signatureType = Port_Utility.getOutgoingType(timestamp, parameter);
		}

		boolean isNonblocking = false;
		if (signatureType != null) {
			parameter.check(timestamp, signatureType);
			signatureType = signatureType.getTypeRefdLast(timestamp);

			switch (signatureType.getTypetype()) {
			case TYPE_SIGNATURE:
				((Signature_Type) signatureType).checkThisTemplate(timestamp, parameter.getTemplateBody(), false, false);
				isNonblocking = ((Signature_Type) signatureType).isNonblocking();
				break;
			default:
				parameter.getLocation().reportSemanticError(
						MessageFormat.format(CALLPARAMETERNOTSIGNATURE, signatureType.getTypename()));
				break;
			}

			if (isNonblocking) {
				if (timerValue != null) {
					timerValue.getLocation().reportSemanticError(
							MessageFormat.format(NONBLOCKINGWITHTIMER, signatureType.getTypename()));
				} else if (noWait) {
					location.reportSemanticError(MessageFormat.format(NONBLOCKINGWITHNOWAIT, signatureType.getTypename()));
				}
				if (altGuards != null) {
					location.reportSemanticError(MessageFormat.format(NONBLOCKINGWITHRESPONSEPART, signatureType.getTypename()));
				}
			} else if (noWait) {
				if (altGuards != null) {
					location.reportSemanticError(NOWAITWITHRESPONSEPART);
				}
			} else {
				if (!getIsErroneous() && altGuards == null) {
					location.reportSemanticError(RESPONSEPARTMISSING);
				}
			}
		}

		if (timerValue != null) {
			timerValue.setLoweridToReference(timestamp);
			final Type_type temporalType = timerValue.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

			switch (temporalType) {
			case TYPE_REAL:
				final IValue last = timerValue.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
				if (Value_type.REAL_VALUE.equals(last.getValuetype()) && !last.getIsErroneous(timestamp)) {
					final double temp = ((Real_Value) last).getValue();
					if (temp < 0) {
						timerValue.getLocation().reportSemanticError(MessageFormat.format(CALLTIMERNEGATIVE, temp));
					}
				}
				break;
			case TYPE_UNDEFINED:
				setIsErroneous();
				break;
			default:
				if (!isErroneous) {
					location.reportSemanticError(FLOATTIMEREXPECTED);
				}
				break;
			}
		}

		Port_Utility.checkToClause(timestamp, this, portType, toClause);

		if (altGuards != null) {
			checkCallBody(timestamp, portType, signatureType);
		}

		lastTimeChecked = timestamp;
	}

	/**
	 * Checks the response and exception handling part of a call operation.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual build cycle.
	 * @param portType
	 *                the port type of the actual call statement.
	 * @param signature
	 *                the signature type of the actual call statement.
	 * */
	private void checkCallBody(final CompilationTimeStamp timestamp, final Port_Type portType, final IType signature) {
		boolean hasCatchTimeout = false;
		for (int i = 0; i < altGuards.getNofAltguards(); i++) {
			final AltGuard altGuard = altGuards.getAltguardByIndex(i);
			if (!altguard_type.AG_OP.equals(altGuard.getType())) {
				continue;
			}

			final Statement statement = ((Operation_Altguard) altGuard).getGuardStatement();
			if (Statement_type.S_CATCH.equals(statement.getType())) {
				((Catch_Statement) statement).setCallSettings(true, timerValue != null);
				hasCatchTimeout |= ((Catch_Statement) statement).hasTimeout();
			}
		}

		altGuards.setMyAltguards(altGuards);
		altGuards.check(timestamp);

		if (portType != null) {
			// checking whether getreply/catch operations refer to
			// the same port and same signature as the call
			// operation.
			for (int i = 0; i < altGuards.getNofAltguards(); i++) {
				final AltGuard altguard = altGuards.getAltguardByIndex(i);
				if (!altguard_type.AG_OP.equals(altguard.getType())) {
					continue;
				}

				final Statement statement = ((Operation_Altguard) altguard).getGuardStatement();
				if (statement.getIsErroneous()) {
					continue;
				}

				switch (statement.getType()) {
				case S_GETREPLY: {
					final Reference tempPortReference = ((Getreply_Statement) statement).getPortReference();
					if (tempPortReference == null) {
						final String message = MessageFormat
								.format("The `{0}'' operation must refer to the same port as the previous `call'' statement: `{1}'' was expected instead of `any port''",
										statement.getStatementName(), portReference.getId().getDisplayName());
						statement.getLocation().reportSemanticError(message);
					} else if (!portReference.getId().equals(tempPortReference.getId())) {
						final String message = MessageFormat
								.format("The `{0}'' operation refers to a different port than the previous `call'' statement: `{1}'' was expected instead of `{2}''",
										statement.getStatementName(), portReference.getId().getDisplayName(),
										tempPortReference.getId().getDisplayName());
						tempPortReference.getLocation().reportSemanticError(message);
					}

					final TemplateInstance instance = ((Getreply_Statement) statement).getReceiveParameter();
					if (instance != null) {
						final IType tempSignature = instance.getExpressionGovernor(timestamp,
								Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
						if (signature != null && !signature.isCompatible(timestamp, tempSignature, null, null, null)) {
							final String message = MessageFormat.format(GETRPELYTOWRONGSIGNATURE,
									signature.getTypename(), tempSignature.getTypename());
							instance.getLocation().reportSemanticError(message);
						}
					}
					break;
				}
				case S_CATCH:
					final Reference tempPortReference = ((Catch_Statement) statement).getPortReference();
					if (tempPortReference == null) {
						final String message = MessageFormat
								.format("The `{0}'' operation must refer to the same port as the previous `call'' statement: `{1}'' was expected instead of `any port''",
										statement.getStatementName(), portReference.getId().getDisplayName());
						statement.getLocation().reportSemanticError(message);
					} else if (!portReference.getId().equals(tempPortReference.getId())) {
						final String message = MessageFormat
								.format("The `{0}'' operation refers to a different port than the previous `call'' statement: `{1}'' was expected instead of `{2}''",
										statement.getStatementName(), portReference.getId().getDisplayName(),
										tempPortReference.getId().getDisplayName());
						tempPortReference.getLocation().reportSemanticError(message);
					}

					final Signature_Type tempSignature = ((Catch_Statement) statement).getSignatureType();
					if (tempSignature != null && signature != null
							&& !signature.isCompatible(timestamp, tempSignature, null, null, null)) {
						final String message = MessageFormat
								.format("The `catch'' operation refers to a different signature than the previous `call'' statement: `{0}'' was expected instead of `{1}''",
										signature.getTypename(), tempSignature.getTypename());
						statement.getLocation().reportSemanticError(message);
					}
					break;
				default:
					break;
				}
			}
		}

		if (timerValue != null && !hasCatchTimeout) {
			location.reportSemanticWarning("The call operation has a timer, but the timeout expection is not cought");
		}
	}

	@Override
	public void checkAllowedInterleave() {
		if (altGuards != null) {
			altGuards.checkAllowedInterleave();
		}
	}

	@Override
	public void postCheck() {
		if (altGuards != null) {
			altGuards.postCheck();
		}
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (altGuards != null) {
			return null;
		}

		final List<Integer> result = new ArrayList<Integer>();
		result.add(Ttcn3Lexer.BEGINCHAR);

		if (toClause != null) {
			return result;
		}

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
		if (timerValue != null) {
			timerValue.updateSyntax(reparser, false);
			reparser.updateLocation(timerValue.getLocation());
		}

		if (toClause instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) toClause).updateSyntax(reparser, false);
			reparser.updateLocation(toClause.getLocation());
		} else if (toClause != null) {
			throw new ReParseException();
		}

		if (altGuards != null) {
			altGuards.updateSyntax(reparser, false);
			reparser.updateLocation(altGuards.getLocation());
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
		if (timerValue != null) {
			timerValue.findReferences(referenceFinder, foundIdentifiers);
		}
		if (toClause != null) {
			toClause.findReferences(referenceFinder, foundIdentifiers);
		}
		if (altGuards != null) {
			altGuards.findReferences(referenceFinder, foundIdentifiers);
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
		if (timerValue != null && !timerValue.accept(v)) {
			return false;
		}
		if (toClause != null && !toClause.accept(v)) {
			return false;
		}
		if (altGuards != null && !altGuards.accept(v)) {
			return false;
		}
		return true;
	}
}
