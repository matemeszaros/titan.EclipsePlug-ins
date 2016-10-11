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
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody.OperationModes;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureExceptions;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TypeSet;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Raise_Statement extends Statement {
	private static final String RAISEONPORT = "Procedure-based operation `raise'' is not applicable to a message-based port of type `{0}''";
	private static final String SIGNATURENOTPRESENT = "Signature `{0}'' is not present on the incoming list of port type `{1}''";
	private static final String NOINCOMINGSIGNATURES = "Port type `{0}'' does not have any incoming signatures";
	private static final String SIGNATUREWITHOUTEXCEPTIONS = "Signature `{0}'' does not have exceptions";
	private static final String UNKNOWNEXCEPTIONTYPE = "Cannot determine the type of the exception";
	private static final String TYPENOTONEXCEPTIONLIST = "Type `{0}'' is not present on the exception list of signature `{1}''";
	private static final String AMBIGUOUSEXCEPTION = "Type of the exception is ambiguous:"
			+ " `{0}'' is compatible with more than one exception type of signature `{1}''";
	private static final String SIGNATUREEXCEPTION = "The type of raise parameter is signature `{0}'', which cannot be an exception type";
	private static final String PORTEXCEPTION = "The type of raise parameter is port type `{0}'', which cannot be an exception type";
	private static final String DEFAULTEXCEPTION = "The type of raise parameter is the `default'' type, which cannot be an exception type";

	private static final String FULLNAMEPART1 = ".portreference";
	private static final String FULLNAMEPART2 = ".signature";
	private static final String FULLNAMEPART3 = ".sendparameter";
	private static final String FULLNAMEPART4 = ".to";
	private static final String STATEMENT_NAME = "raise";

	private final Reference portReference;
	private final Reference signatureReference;
	private final TemplateInstance parameter;
	private final IValue toClause;

	public Raise_Statement(final Reference portReference, final Reference signatureReference, final TemplateInstance parameter,
			final IValue toClause) {
		this.portReference = portReference;
		this.signatureReference = signatureReference;
		this.parameter = parameter;
		this.toClause = toClause;

		if (portReference != null) {
			portReference.setFullNameParent(this);
		}
		if (signatureReference != null) {
			signatureReference.setFullNameParent(this);
		}
		if (parameter != null) {
			parameter.setFullNameParent(this);
		}
		if (toClause != null) {
			toClause.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_RAISE;
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
		} else if (signatureReference == child) {
			return builder.append(FULLNAMEPART2);
		} else if (parameter == child) {
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
		if (signatureReference != null) {
			signatureReference.setMyScope(scope);
		}
		if (parameter != null) {
			parameter.setMyScope(scope);
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

		IType signature = Port_Utility.checkSignatureReference(timestamp, signatureReference);

		if (portType != null) {
			final PortTypeBody portTypeBody = portType.getPortBody();
			final TypeSet inSignatures = portTypeBody.getInSignatures();

			if (OperationModes.OP_Message.equals(portTypeBody.getOperationMode())) {
				portReference.getLocation().reportSemanticError(MessageFormat.format(RAISEONPORT, portType.getTypename()));
			} else if (inSignatures != null) {
				if (signature != null) {
					if (!inSignatures.hasType(timestamp, signature)) {
						portReference.getLocation().reportSemanticError(
								MessageFormat.format(SIGNATURENOTPRESENT, signature.getTypename(),
										portType.getTypename()));
					}
				} else if (inSignatures.getNofTypes() == 1) {
					signature = inSignatures.getTypeByIndex(0).getTypeRefdLast(timestamp);
				}
			} else {
				portReference.getLocation().reportSemanticError(MessageFormat.format(NOINCOMINGSIGNATURES, portType.getTypename()));
			}
		}

		IType exception = null;
		boolean exceptionDetermined = false;
		if (signature != null) {
			final SignatureExceptions exceptions = ((Signature_Type) signature).getSignatureExceptions();

			if (exceptions == null) {
				signatureReference.getLocation().reportSemanticError(
						MessageFormat.format(SIGNATUREWITHOUTEXCEPTIONS, signature.getTypename()));
			} else {
				if (exceptions.getNofExceptions() == 1) {
					exception = exceptions.getExceptionByIndex(0);
				} else {
					exception = Port_Utility.getOutgoingType(timestamp, parameter);

					if (exception == null) {
						parameter.getLocation().reportSemanticError(UNKNOWNEXCEPTIONTYPE);
					} else {
						final int nofCompatibleTypes = exceptions.getNofCompatibleExceptions(timestamp, exception);
						if (nofCompatibleTypes == 0) {
							parameter.getLocation().reportSemanticError(
									MessageFormat.format(TYPENOTONEXCEPTIONLIST, exception.getTypename(),
											signature.getTypename()));
						} else if (nofCompatibleTypes > 1) {
							parameter.getLocation().reportSemanticError(
									MessageFormat.format(AMBIGUOUSEXCEPTION, exception.getTypename(),
											signature.getTypename()));
						}
					}
				}

				exceptionDetermined = true;
			}
		}

		if (!exceptionDetermined) {
			exception = Port_Utility.getOutgoingType(timestamp, parameter);
		}

		if (exception != null) {
			parameter.check(timestamp, exception);

			exception = exception.getTypeRefdLast(timestamp);
			switch (exception.getTypetype()) {
			case TYPE_SIGNATURE:
				parameter.getLocation().reportSemanticError(MessageFormat.format(SIGNATUREEXCEPTION, exception.getTypename()));
				break;
			case TYPE_PORT:
				parameter.getLocation().reportSemanticError(MessageFormat.format(PORTEXCEPTION, exception.getTypename()));
				break;
			case TYPE_DEFAULT:
				parameter.getLocation().reportSemanticError(MessageFormat.format(DEFAULTEXCEPTION, exception.getTypename()));
				break;
			default:
				break;
			}
		}

		parameter.getTemplateBody().checkSpecificValue(timestamp, false);

		Port_Utility.checkToClause(timestamp, this, portType, toClause);

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

		if (signatureReference != null) {
			signatureReference.updateSyntax(reparser, false);
			reparser.updateLocation(signatureReference.getLocation());
		}

		if (parameter != null) {
			parameter.updateSyntax(reparser, false);
			reparser.updateLocation(parameter.getLocation());
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
		if (signatureReference != null) {
			signatureReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (parameter != null) {
			parameter.findReferences(referenceFinder, foundIdentifiers);
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
		if (signatureReference != null && !signatureReference.accept(v)) {
			return false;
		}
		if (parameter != null && !parameter.accept(v)) {
			return false;
		}
		if (toClause != null && !toClause.accept(v)) {
			return false;
		}
		return true;
	}
}
