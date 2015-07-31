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
 * The ImportModule class represents a TTCN3 import statement. This class is
 * used to create a link between the actual mode containing the import statement
 * and the imported module.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ImportModule_V4 extends ImportModule {

	public ImportModule_V4(Identifier identifier) {
		super(identifier);
	}

	@Override
	protected int reparse(TTCN3ReparseUpdater aReparser) {
		//TODO: implement
		return 0;
	}
}
