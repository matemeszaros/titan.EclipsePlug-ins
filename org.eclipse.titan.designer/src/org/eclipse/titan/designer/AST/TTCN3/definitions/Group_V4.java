/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Class to represent pr_GroupDef nodes.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Group_V4 extends Group {

	public Group_V4(Identifier identifier) {
		super(identifier);
	}

	@Override
	protected int reparseIdentifier(TTCN3ReparseUpdater aReparser) {
		//TODO: implement
		return 0;
	}

	@Override
	protected int reparseOptionalWithStatement(TTCN3ReparseUpdater aReparser) {
		//TODO: implement
		return 0;
	}

	@Override
	protected int reparseModuleDefinitionsList(TTCN3ReparseUpdater aReparser) {
		//TODO: implement
		return 0;
	}
}
