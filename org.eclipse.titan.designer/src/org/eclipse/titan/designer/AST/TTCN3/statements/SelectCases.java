/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The SelectCases class is helper class for the SelectCase_Statement class.
 * Holds a list of the select cases that were parsed from the source code.
 * 
 * @see SelectCase_Statement {@link SelectCase_Statement}
 * @see SelectCase
 * 
 * @author Kristof Szabados
 * */
public final class SelectCases extends ASTNode implements IIncrementallyUpdateable {
	private static final String FULLNAMEPART = ".select_case_";

	private final List<SelectCase> select_cases;

	public SelectCases() {
		select_cases = new ArrayList<SelectCase>();
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = select_cases.size(); i < size; i++) {
			if (select_cases.get(i) == child) {
				return builder.append(FULLNAMEPART).append(Integer.toString(i + 1));
			}
		}

		return builder;
	}

	/**
	 * Adds a select case branch.
	 * <p>
	 * The parameter can not be null, that case is handled in the parser.
	 * 
	 * @param selectCase
	 *                the select case to be added.
	 * */
	public void addSelectCase(final SelectCase selectCase) {
		select_cases.add(selectCase);
		selectCase.setFullNameParent(this);
	}

	/**
	 * Sets the scope of the contained select case branches.
	 * 
	 * @param scope
	 *                the scope to be set.
	 * */
	@Override
	public void setMyScope(final Scope scope) {
		for (int i = 0, size = select_cases.size(); i < size; i++) {
			select_cases.get(i).setMyScope(scope);
		}
	}

	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		for (int i = 0, size = select_cases.size(); i < size; i++) {
			select_cases.get(i).setMyStatementBlock(statementBlock, index);
		}
	}

	public void setMyDefinition(final Definition definition) {
		for (int i = 0, size = select_cases.size(); i < size; i++) {
			select_cases.get(i).setMyDefinition(definition);
		}
	}

	public void setMyAltguards(final AltGuards altGuards) {
		for (int i = 0, size = select_cases.size(); i < size; i++) {
			select_cases.get(i).setMyAltguards(altGuards);
		}
	}

	/**
	 * Checks whether the select cases have a return statement, either
	 * directly or embedded.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return the return status of the select cases.
	 * */
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		StatementBlock.ReturnStatus_type result = StatementBlock.ReturnStatus_type.RS_MAYBE;
		boolean hasElse = false;
		for (int i = 0, size = select_cases.size(); i < size; i++) {
			SelectCase selectCase = select_cases.get(i);
			switch (selectCase.hasReturn(timestamp)) {
			case RS_NO:
				if (result == StatementBlock.ReturnStatus_type.RS_YES) {
					return StatementBlock.ReturnStatus_type.RS_MAYBE;
				}

				result = StatementBlock.ReturnStatus_type.RS_NO;
				break;
			case RS_YES:
				if (result == StatementBlock.ReturnStatus_type.RS_NO) {
					return StatementBlock.ReturnStatus_type.RS_MAYBE;
				}

				result = StatementBlock.ReturnStatus_type.RS_YES;
				break;
			default:
				return StatementBlock.ReturnStatus_type.RS_MAYBE;
			}

			if (selectCase.hasElse()) {
				hasElse = true;
				break;
			}
		}

		if (!hasElse && result == StatementBlock.ReturnStatus_type.RS_YES) {
			return StatementBlock.ReturnStatus_type.RS_MAYBE;
		}

		return result;
	}

	/**
	 * Does the semantic checking of the select case list.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param governor
	 *                the governor of the select expression, to check the
	 *                cases against.
	 * */
	public void check(final CompilationTimeStamp timestamp, final IType governor) {
		boolean unrechable = false;
		for (int i = 0, size = select_cases.size(); i < size; i++) {
			unrechable = select_cases.get(i).check(timestamp, governor, unrechable);
		}
	}

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public void checkAllowedInterleave() {
		for (int i = 0, size = select_cases.size(); i < size; i++) {
			select_cases.get(i).checkAllowedInterleave();
		}
	}

	/**
	 * Checks the properties of the statement, that can only be checked
	 * after the semantic check was completely run.
	 */
	public void postCheck() {
		for (int i = 0, size = select_cases.size(); i < size; i++) {
			select_cases.get(i).postCheck();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		SelectCase branch;
		for (int i = 0, size = select_cases.size(); i < size; i++) {
			branch = select_cases.get(i);

			branch.updateSyntax(reparser, false);
			reparser.updateLocation(branch.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (select_cases == null) {
			return;
		}

		for (SelectCase sc : select_cases) {
			sc.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (select_cases != null) {
			for (SelectCase sc : select_cases) {
				if (!sc.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}

	public List<SelectCase> getSelectCaseArray() {
		return select_cases;
	}
}
