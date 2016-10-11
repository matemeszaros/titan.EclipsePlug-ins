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
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.ParsedActualParameters;
import org.eclipse.titan.designer.AST.TTCN3.types.Altstep_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Invoke_Altguard extends AltGuard {
	private static final String BOOLEANEXPECTED = "A value or expression of type boolean was expected";

	private static final String FULLNAMEPART1 = ".expr";
	private static final String FULLNAMEPART2 = ".function";
	private static final String FULLNAMEPART3 = ".<parameters>";
	private static final String FULLNAMEPART4 = ".block";

	private final Value expression;
	private final Value value;
	private final ParsedActualParameters parsedParameters;

	public Invoke_Altguard(final Value expression, final Value value, final ParsedActualParameters actualParameterList,
			final StatementBlock statementblock) {
		super(altguard_type.AG_INVOKE, statementblock);
		this.expression = expression;
		this.value = value;
		this.parsedParameters = actualParameterList;

		if (expression != null) {
			expression.setFullNameParent(this);
		}
		if (value != null) {
			value.setFullNameParent(this);
		}
		if (actualParameterList != null) {
			actualParameterList.setFullNameParent(this);
		}
		if (statementblock != null) {
			statementblock.setFullNameParent(this);
			statementblock.setOwnerIsAltguard();
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (expression == child) {
			return builder.append(FULLNAMEPART1);
		} else if (value == child) {
			return builder.append(FULLNAMEPART2);
		} else if (parsedParameters == child) {
			return builder.append(FULLNAMEPART3);
		} else if (statementblock == child) {
			return builder.append(FULLNAMEPART4);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (expression != null) {
			expression.setMyScope(scope);
		}
		if (value != null) {
			value.setMyScope(scope);
		}
		if (parsedParameters != null) {
			parsedParameters.setMyScope(scope);
		}
		if (statementblock != null) {
			statementblock.setMyScope(scope);
		}
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		if (statementblock != null) {
			statementblock.setMyStatementBlock(statementBlock, index);
		}
	}

	@Override
	public void setMyDefinition(final Definition definition) {
		if (statementblock != null) {
			statementblock.setMyDefinition(definition);
		}
	}

	@Override
	public void setMyAltguards(final AltGuards altGuards) {
		if (statementblock != null) {
			statementblock.setMyAltguards(altGuards);
		}
	}

	@Override
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (statementblock != null) {
			return statementblock.hasReturn(timestamp);
		}

		return StatementBlock.ReturnStatus_type.RS_NO;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (expression != null) {
			final IValue last = expression.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);

			final Type_type temporalType = last.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (!last.getIsErroneous(timestamp) && !Type_type.TYPE_BOOL.equals(temporalType)) {
				last.getLocation().reportSemanticError(BOOLEANEXPECTED);
				expression.setIsErroneous(true);
			}
		}

		IType type = value.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		if (type == null) {
			lastTimeChecked = timestamp;
			return;
		}

		type = type.getTypeRefdLast(timestamp);
		if (type == null || type.getIsErroneous(timestamp)) {
			lastTimeChecked = timestamp;
			return;
		}

		if (!Type_type.TYPE_ALTSTEP.equals(type.getTypetype())) {
			value.getLocation().reportSemanticError(
					MessageFormat.format("A value of type altstep was expected instead of `{0}''", type.getTypename()));
			lastTimeChecked = timestamp;
			return;
		}

		value.getMyScope().checkRunsOnScope(timestamp, type, this, "call");
		final FormalParameterList formalParmaterList = ((Altstep_Type) type).getFormalParameters();
		final ActualParameterList actualParameterList = new ActualParameterList();
		formalParmaterList.checkActualParameterList(timestamp, parsedParameters, actualParameterList);

		if (statementblock != null) {
			statementblock.check(timestamp);
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void checkAllowedInterleave() {
		getLocation().reportSemanticError("Invocation of an altstep is not allowed within an interleave statement");

		if (statementblock != null) {
			statementblock.checkAllowedInterleave();
		}
	}

	@Override
	public void postCheck() {
		if (statementblock != null) {
			statementblock.postCheck();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			if (statementblock != null && reparser.envelopsDamage(statementblock.getLocation())) {
				statementblock.updateSyntax(reparser, true);

				if (expression != null) {
					expression.updateSyntax(reparser, false);
					reparser.updateLocation(expression.getLocation());
				}

				if (parsedParameters != null) {
					parsedParameters.updateSyntax(reparser, false);
					reparser.updateLocation(parsedParameters.getLocation());
				}

				reparser.updateLocation(statementblock.getLocation());

				return;
			}

			throw new ReParseException();
		}

		if (expression != null) {
			expression.updateSyntax(reparser, false);
			reparser.updateLocation(expression.getLocation());
		}

		if (parsedParameters != null) {
			parsedParameters.updateSyntax(reparser, false);
			reparser.updateLocation(parsedParameters.getLocation());
		}

		if (statementblock != null) {
			statementblock.updateSyntax(reparser, false);
			reparser.updateLocation(statementblock.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (expression != null) {
			expression.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
		if (parsedParameters != null) {
			parsedParameters.findReferences(referenceFinder, foundIdentifiers);
		}
		if (statementblock != null) {
			statementblock.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (expression != null) {
			if (!expression.accept(v)) {
				return false;
			}
		}
		if (value != null) {
			if (!value.accept(v)) {
				return false;
			}
		}
		if (parsedParameters != null) {
			if (!parsedParameters.accept(v)) {
				return false;
			}
		}
		if (statementblock != null) {
			if (!statementblock.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
