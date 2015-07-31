/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.MatchExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class Match_InternalLogArgument extends InternalLogArgument {
	private MatchExpression matchExpression;

	public Match_InternalLogArgument(final MatchExpression matchExpression) {
		super(ArgumentType.Match);
		this.matchExpression = matchExpression;
	}

	public MatchExpression getMatchExpression() {
		return matchExpression;
	}

	@Override
	public void checkRecursions(CompilationTimeStamp timestamp, IReferenceChain referenceChain) {
		if (matchExpression == null) {
			return;
		}

		matchExpression.checkRecursions(timestamp, referenceChain);
	}
}
