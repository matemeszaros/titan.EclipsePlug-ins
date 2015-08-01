/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.parsers.ttcn3parser.ITTCN3ReparseBase_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Reparser4;

/**
 * The StatementBlock class represents TTCN3 statement block (the scope unit).
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class StatementBlock_V4 extends StatementBlock {
	
	@Override
	protected int reparse( final TTCN3ReparseUpdater aReparser ) {
		return ((TTCN3ReparseUpdater_V4)aReparser).parse(new ITTCN3ReparseBase_V4() {
			@Override
			public void reparse(final TTCN3Reparser4 parser) {
				List<Statement> statements = parser.pr_reparse_FunctionStatementOrDefList().statements;
				if ( parser.isErrorListEmpty() ) {
					if (statements != null) {
						addStatementsOrdered(statements);
					}
				}
			}
		});
	}
}
