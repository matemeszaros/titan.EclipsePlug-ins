/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The SelectUnionCases class is helper class for the SelectUnionCase_Statement class.
 * Holds a list of the select union cases that were parsed from the source code.
 * 
 * @see SelectUnionCase_Statement {@link SelectCase_Statement}
 * @see SelectUnionCase
 * 
 * @author Arpad Lovassy
 */
public final class SelectUnionCases extends ASTNode implements IIncrementallyUpdateable {
	private static final String FULLNAMEPART = ".select_union_case_";

	private final List<SelectUnionCase> mSelectUnionCases;

	public SelectUnionCases() {
		mSelectUnionCases = new ArrayList<SelectUnionCase>();
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = mSelectUnionCases.size(); i < size; i++) {
			if (mSelectUnionCases.get(i) == child) {
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
	public void addSelectUnionCase(final SelectUnionCase selectUnionCase) {
		mSelectUnionCases.add(selectUnionCase);
		selectUnionCase.setFullNameParent(this);
	}

	/**
	 * Sets the scope of the contained select case branches.
	 * 
	 * @param scope
	 *                the scope to be set.
	 * */
	@Override
	public void setMyScope(final Scope scope) {
		for (int i = 0, size = mSelectUnionCases.size(); i < size; i++) {
			mSelectUnionCases.get(i).setMyScope(scope);
		}
	}

	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		for (int i = 0, size = mSelectUnionCases.size(); i < size; i++) {
			mSelectUnionCases.get(i).setMyStatementBlock(statementBlock, index);
		}
	}

	public void setMyDefinition(final Definition definition) {
		for (int i = 0, size = mSelectUnionCases.size(); i < size; i++) {
			mSelectUnionCases.get(i).setMyDefinition(definition);
		}
	}

	public void setMyAltguards(final AltGuards altGuards) {
		for (int i = 0, size = mSelectUnionCases.size(); i < size; i++) {
			mSelectUnionCases.get(i).setMyAltguards(altGuards);
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
		for (int i = 0, size = mSelectUnionCases.size(); i < size; i++) {
			final SelectUnionCase selectUnionCase = mSelectUnionCases.get(i);
			switch (selectUnionCase.hasReturn(timestamp)) {
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

			if (selectUnionCase.hasElse()) {
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
	 * @param aTimestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param aUnionType
	 *                the referenced union type of the select expression, to check the cases against.
	 *                It can be null. In this case no check needs to be done,
	 *                because type check was done in SelectUnionCase_Statement.check() and it failed
	 * @param aFieldNames
	 *                union field names, which are not covered yet.
	 *                If a field name is found, it is removed from the list.
	 *                If case else is found, all the filed names are removed from the list, because all the cases are covered.
	 */
	public void check( final CompilationTimeStamp aTimestamp, final TTCN3_Choice_Type aUnionType, final List<String> aFieldNames ) {
		boolean unrechable = false;
		for (int i = 0, size = mSelectUnionCases.size(); i < size; i++) {
			unrechable = mSelectUnionCases.get(i).check( aTimestamp, aUnionType, unrechable, aFieldNames );
		}
	}

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public void checkAllowedInterleave() {
		for (int i = 0, size = mSelectUnionCases.size(); i < size; i++) {
			mSelectUnionCases.get(i).checkAllowedInterleave();
		}
	}

	/**
	 * Checks the properties of the statement, that can only be checked
	 * after the semantic check was completely run.
	 */
	public void postCheck() {
		for (int i = 0, size = mSelectUnionCases.size(); i < size; i++) {
			mSelectUnionCases.get(i).postCheck();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		SelectUnionCase branch;
		for (int i = 0, size = mSelectUnionCases.size(); i < size; i++) {
			branch = mSelectUnionCases.get(i);

			branch.updateSyntax(reparser, false);
			reparser.updateLocation(branch.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (mSelectUnionCases == null) {
			return;
		}

		for (final SelectUnionCase sc : mSelectUnionCases) {
			sc.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (mSelectUnionCases != null) {
			for (final SelectUnionCase sc : mSelectUnionCases) {
				if (!sc.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}

	public List<SelectUnionCase> getSelectUnionCaseArray() {
		return mSelectUnionCases;
	}
}
