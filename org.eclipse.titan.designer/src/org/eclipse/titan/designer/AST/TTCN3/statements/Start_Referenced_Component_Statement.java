/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.ParsedActualParameters;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Function_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Start_Referenced_Component_Statement extends Statement {
	private static final String FULLNAMEPART1 = ".componentreference";
	private static final String FULLNAMEPART2 = ".functionreference";
	private static final String FULLNAMEPART3 = ".<parameters>";
	private static final String STATEMENT_NAME = "start test component";

	private final Value componentReference;
	private final Value dereferredValue;
	private final ParsedActualParameters parameters;

	public Start_Referenced_Component_Statement(final Value componentReference, final Value dereferredValue,
			final ParsedActualParameters parameters) {
		this.componentReference = componentReference;
		this.dereferredValue = dereferredValue;
		this.parameters = parameters;

		if (componentReference != null) {
			componentReference.setFullNameParent(this);
		}
		if (dereferredValue != null) {
			dereferredValue.setFullNameParent(this);
		}
		if (parameters != null) {
			parameters.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_START_REFERENCED_COMPONENT;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (componentReference == child) {
			return builder.append(FULLNAMEPART1);
		} else if (dereferredValue == child) {
			return builder.append(FULLNAMEPART2);
		} else if (parameters == child) {
			return builder.append(FULLNAMEPART3);
		}

		return builder;
	}

	/** @return the dereferred value */
	public Value getDereferredValue() {
		return dereferredValue;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (componentReference != null) {
			componentReference.setMyScope(scope);
		}
		if (dereferredValue != null) {
			dereferredValue.setMyScope(scope);
		}
		if (parameters != null) {
			parameters.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		Component_Type compType = null;

		if (componentReference != null) {
			compType = Port_Utility.checkComponentReference(timestamp, this, componentReference, false, false);
		}

		if (dereferredValue == null) {
			return;
		}

		switch (dereferredValue.getValuetype()) {
		case EXPRESSION_VALUE:
			if (Operation_type.REFERS_OPERATION.equals(((Expression_Value) dereferredValue).getOperationType())) {
				dereferredValue.getLocation().reportSemanticError(
						"A value of a function type was expected in the argument instead of a `refers' operation,"
								+ " which does not specify any function type.");
				return;
			}
			break;
		case TTCN3_NULL_VALUE:
		case FAT_NULL_VALUE:
			dereferredValue.getLocation()
					.reportSemanticError(
							"A value of a function type was expected in the argument instead of a `null' value, which does not specify any function type.");
			return;
		default:
			break;
		}

		dereferredValue.setLoweridToReference(timestamp);
		IType type = dereferredValue.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		if (type != null) {
			type = type.getTypeRefdLast(timestamp);
		}

		if (type == null || type.getIsErroneous(timestamp)) {
			return;
		}

		if (!Type_type.TYPE_FUNCTION.equals(type.getTypetype())) {
			dereferredValue.getLocation().reportSemanticError(
					MessageFormat.format("A value of type function was expected in the argument of `{0}''", type.getTypename()));
			return;
		}

		final Function_Type functionType = (Function_Type) type;
		if (functionType.isRunsOnSelf()) {
			dereferredValue.getLocation().reportSemanticError("The argument cannot be a function reference with 'runs on self' clause");
			return;
		}

		if (!functionType.checkStartable(timestamp, getLocation())) {
			return;
		}

		final IType runsOnType = functionType.getRunsOnType(timestamp);

		if (compType != null && runsOnType != null && !runsOnType.isCompatible(timestamp, compType, null, null, null)) {
			final String message = MessageFormat
					.format("Component type mismatch: the component reference os of type `{0}'', but functions of type `{1}'' run on `{2}''",
							compType.getTypename(), functionType.getTypename(), runsOnType.getTypename());
			componentReference.getLocation().reportSemanticError(message);
		}

		final IType returnType = functionType.getReturnType();
		if (returnType != null) {
			if (functionType.returnsTemplate()) {
				dereferredValue.getLocation().reportSemanticWarning(
						MessageFormat.format("Function of type `{0}'' return a template of type `{1}'',"
								+ " which cannot be retrieved when the test component terminates",
								functionType.getTypename(), returnType.getTypename()));
			} else {
				IType lastType = returnType;
				boolean returnTypeCorrect = false;
				while (!returnTypeCorrect) {
					if (lastType.hasDoneAttribute()) {
						returnTypeCorrect = true;
						break;
					}
					if (lastType instanceof IReferencingType) {
						final IReferenceChain refChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
						final IType refd = ((IReferencingType) lastType).getTypeRefd(timestamp, refChain);
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
					dereferredValue.getLocation()
							.reportSemanticWarning(
									MessageFormat.format(
											"Return type of function type `{0}'' is `{1}'', which does not have the `done'' extension attibute."
													+ " When the test component terminates the returnes value cannot be retrived with a `done'' operation",
											functionType.getTypename(), returnType.getTypename()));
				}
			}
		}

		final ActualParameterList tempActualParameters = new ActualParameterList();
		final FormalParameterList formalParameters = functionType.getFormalParameters();
		if (!formalParameters.checkActualParameterList(timestamp, parameters, tempActualParameters)) {
			tempActualParameters.setFullNameParent(this);
			tempActualParameters.setMyScope(getMyScope());
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (componentReference != null) {
			componentReference.updateSyntax(reparser, false);
			reparser.updateLocation(componentReference.getLocation());
		}

		if (dereferredValue != null) {
			dereferredValue.updateSyntax(reparser, false);
			reparser.updateLocation(dereferredValue.getLocation());
		}
		if (parameters != null) {
			parameters.updateSyntax(reparser, false);
			reparser.updateLocation(parameters.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentReference != null) {
			componentReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (dereferredValue != null) {
			dereferredValue.findReferences(referenceFinder, foundIdentifiers);
		}
		if (parameters != null) {
			parameters.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (componentReference != null && !componentReference.accept(v)) {
			return false;
		}
		if (dereferredValue != null && !dereferredValue.accept(v)) {
			return false;
		}
		if (parameters != null && !parameters.accept(v)) {
			return false;
		}
		return true;
	}
}
