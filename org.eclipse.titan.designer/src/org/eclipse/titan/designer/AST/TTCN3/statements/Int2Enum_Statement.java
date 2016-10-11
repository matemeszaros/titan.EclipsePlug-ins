/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
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
 * @author Botond Baranyi
 * */
public class Int2Enum_Statement extends Statement {
	private static final String FULLNAMEPART1 = ".value";
	private static final String FULLNAMEPART2 = ".reference";
	private static final String STATEMENT_NAME = "int2enum";
	private static final String OPERANDERROR1 = "The first operand of the `int2enum' operation should be an integer value";
	private static final String OPERANDERROR2 = "A reference to a variable or a value parameter of type enumerated was expected";

	private final Value value;
	private final Reference reference;

	public Int2Enum_Statement(final Value value, final Reference reference) {
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
		return Statement_type.S_INT2ENUM;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

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
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (value != null) {
			value.setLoweridToReference(timestamp);
			final Type_type temporalType = value.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			switch (temporalType) {
			case TYPE_INTEGER:
			case TYPE_INTEGER_A:
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
			final IType refType = reference.checkVariableReference(timestamp).getTypeRefdLast(timestamp);
			if (Type_type.TYPE_TTCN3_ENUMERATED != refType.getTypetype() && !isErroneous) {
				value.getLocation().reportSemanticError(OPERANDERROR2);
				setIsErroneous();
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
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
	protected boolean memberAccept(final ASTVisitor v) {
		if (value != null && !value.accept(v)) {
			return false;
		}
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		return true;
	}
}
