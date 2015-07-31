/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a Module.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class TTCN3Module_V4 extends TTCN3Module {

	public TTCN3Module_V4(Identifier identifier, IProject project) {
		super(identifier, project);
		definitions = new Definitions_V4();
		definitions.setParentScope(this);
		definitions.setFullNameParent(this);
	}

	@Override
	protected int reparseAfterModule(TTCN3ReparseUpdater aReparser) {
		//TODO: implement
		return 0;
	}

	@Override
	protected int reparseInsideAttributelist(TTCN3ReparseUpdater aReparser) {
		//TODO: implement
		return 0;
	}
}
