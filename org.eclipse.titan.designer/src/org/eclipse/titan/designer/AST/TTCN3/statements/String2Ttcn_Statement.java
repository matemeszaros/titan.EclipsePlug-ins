/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Adam Delic
 * */
public class String2Ttcn_Statement extends Statement {
	private static final String FULLNAMEPART1 = ".value";
	private static final String FULLNAMEPART2 = ".reference";
	private static final String STATEMENT_NAME = "string2ttcn";
	private static final String OPERANDERROR1 = "The operand of the `string2ttcn' operation should be a charstring value";
	private static final String OPERANDERROR2 = "Could not determine the assignment for second parameter";
	private static final String OPERANDERROR3 = "Second parameter must be a reference to a variable value or template";

	private final Value value;
	private final Reference reference;

	public String2Ttcn_Statement(final Value value, final Reference reference) {
		this.value = value;
		this.reference = reference;
		if (value != null) {
			value.setFullNameParent(this);
		}
		if (reference != null) {
			reference.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_STRING2TTCN;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);
		if (value == child) {
			return builder.append(FULLNAMEPART1);
		} else if (reference == child) {
			return builder.append(FULLNAMEPART2);
		}
		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (value != null) {
			value.setMyScope(scope);
		}
		if (reference != null) {
			reference.setMyScope(scope);
		}
	}

	@Override
	public void check(CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (value != null) {
			value.setLoweridToReference(timestamp);
			Type_type temporalType = value.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			switch (temporalType) {
			case TYPE_CHARSTRING:
				value.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
				break;
			case TYPE_UNDEFINED:
				setIsErroneous();
				break;
			default:
				if (!isErroneous) {
					value.getLocation().reportSemanticError(OPERANDERROR1);
					setIsErroneous();
				}
			}
		}

		if (reference != null) {
			Assignment assignment = reference.getRefdAssignment(timestamp, false);
			if (assignment == null) {
				reference.getLocation().reportSemanticError(OPERANDERROR2);
				setIsErroneous();
			} else {
				switch (assignment.getAssignmentType()) {
				case A_PAR_VAL:
				case A_PAR_VAL_IN:
				case A_PAR_TEMP_IN:
				case A_VAR:
				case A_VAR_TEMPLATE:
				case A_PAR_VAL_OUT:
				case A_PAR_VAL_INOUT:
				case A_PAR_TEMP_OUT:
				case A_PAR_TEMP_INOUT:
					// valid assignment types
					break;
				default:
					reference.getLocation().reportSemanticError(OPERANDERROR3);
					setIsErroneous();
				}
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void updateSyntax(TTCN3ReparseUpdater reparser, boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}
		if (reference != null) {
			reference.updateSyntax(reparser, false);
			reparser.updateLocation(reference.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
		if (reference != null) {
			reference.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (value != null && !value.accept(v)) {
			return false;
		}
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		return true;
	}
}
