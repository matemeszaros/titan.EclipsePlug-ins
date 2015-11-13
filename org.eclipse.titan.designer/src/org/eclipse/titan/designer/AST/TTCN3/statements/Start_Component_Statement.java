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
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Start_Component_Statement extends Statement {
	private static final String TEMPLATERETURN = "Function `{0}'' returns a template of type `{1}'',"
			+ " which cannot be retrieved when the test component terminates";
	private static final String COMPONENTTYPEMISMATCH = "Component type mismatch:"
			+ " The component reference is of type `{0}'', but {1} runs on `{2}''";
	private static final String REFERENCETOFUNCTIONWASEXPECTED = "Reference to a function was expected in the argument instead of {0}";
	private static final String RETURNWITHOUTDONE = "Return type of {0} is `{1}'', which does not have the `done'' extension attibute. "
			+ "When the test component terminates the returnes value cannot be retrived with a `done'' operation";

	private static final String FULLNAMEPART1 = ".componentreference";
	private static final String FULLNAMEPART2 = ".functionreference";
	private static final String STATEMENT_NAME = "start test component";

	private final IValue componentReference;
	private final Reference functionInstanceReference;

	public Start_Component_Statement(final IValue componentReference, final Reference functionInstanceReference) {
		this.componentReference = componentReference;
		this.functionInstanceReference = functionInstanceReference;

		if (componentReference != null) {
			componentReference.setFullNameParent(this);
		}
		if (functionInstanceReference != null) {
			functionInstanceReference.setFullNameParent(this);
		}
	}

	/** @return the function instance reference to start */
	public Reference getFunctionInstanceReference() {
		return functionInstanceReference;
	}

	public IValue getComponent() {
		return componentReference;
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_START_COMPONENT;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (componentReference == child) {
			return builder.append(FULLNAMEPART1);
		} else if (functionInstanceReference == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (componentReference != null) {
			componentReference.setMyScope(scope);
		}
		if (functionInstanceReference != null) {
			functionInstanceReference.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		Component_Type componentType = Port_Utility.checkComponentReference(timestamp, this, componentReference, false, false);
		Assignment assignment = functionInstanceReference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return;
		}

		switch (assignment.getAssignmentType()) {
		case A_FUNCTION:
		case A_FUNCTION_RTEMP:
		case A_FUNCTION_RVAL:
			break;
		default:
			functionInstanceReference.getLocation().reportSemanticError(
					MessageFormat.format(REFERENCETOFUNCTIONWASEXPECTED, assignment.getDescription()));
			return;
		}

		Def_Function function = (Def_Function) assignment;
		if (!function.checkStartable(timestamp, getLocation())) {
			return;
		}

		IType runsOnType = function.getRunsOnType(timestamp);
		if (componentType == null || runsOnType == null) {
			return;
		}

		if (!runsOnType.isCompatible(timestamp, componentType, null, null, null)) {
			componentReference.getLocation().reportSemanticError(
					MessageFormat.format(COMPONENTTYPEMISMATCH, componentType.getTypename(), function.getDescription(),
							runsOnType.getTypename()));
		}

		switch (function.getAssignmentType()) {
		case A_FUNCTION_RTEMP:
			functionInstanceReference.getLocation().reportSemanticWarning(
					MessageFormat.format(TEMPLATERETURN, function.getFullName(), function.getType(timestamp).getTypename()));
			break;
		case A_FUNCTION_RVAL: {
			IType type = function.getType(timestamp);
			boolean returnTypeCorrect = false;
			while (!returnTypeCorrect) {
				if (type.hasDoneAttribute()) {
					returnTypeCorrect = true;
					break;
				}
				if (type instanceof IReferencingType) {
					IReferenceChain refChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
					IType refd = ((IReferencingType) type).getTypeRefd(timestamp, refChain);
					refChain.release();
					if (type != refd) {
						type = refd;
					} else {
						break;
					}
				} else {
					break;
				}
			}

			if (!returnTypeCorrect) {
				final String message = MessageFormat.format(RETURNWITHOUTDONE, function.getDescription(), function.getType(timestamp)
						.getTypename());
				functionInstanceReference.getLocation().reportSemanticWarning(message);
			}
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (componentReference instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) componentReference).updateSyntax(reparser, false);
			reparser.updateLocation(componentReference.getLocation());
		} else {
			throw new ReParseException();
		}

		if (functionInstanceReference != null) {
			functionInstanceReference.updateSyntax(reparser, false);
			reparser.updateLocation(functionInstanceReference.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentReference != null) {
			componentReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (functionInstanceReference != null) {
			functionInstanceReference.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (componentReference != null && !componentReference.accept(v)) {
			return false;
		}
		if (functionInstanceReference != null && !functionInstanceReference.accept(v)) {
			return false;
		}
		return true;
	}
}
