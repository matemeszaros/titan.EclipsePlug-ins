/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The ControlPart class represents the control parts of TTCN3 modules.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ControlPart_V4 extends ControlPart {

	public ControlPart_V4(StatementBlock statementblock) {
		super();
		if (statementblock == null) {
			this.statementblock = new StatementBlock_V4();
		} else {
			this.statementblock = statementblock;
			setLocation(statementblock.getLocation());
			addSubScope(statementblock.getLocation(), statementblock);
		}
		this.statementblock.setFullNameParent(this);
	}

	@Override
	protected int reparse( final TTCN3ReparseUpdater aReparser ) {
		//TODO: implement
		return 0;
	}
}
