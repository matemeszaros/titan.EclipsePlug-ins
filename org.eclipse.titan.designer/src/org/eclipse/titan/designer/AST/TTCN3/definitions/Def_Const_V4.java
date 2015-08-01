/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.parsers.ttcn3parser.ITTCN3ReparseBase_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Reparser4;

/**
 * The Def_Const class represents TTCN3 constant definitions.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Def_Const_V4 extends Def_Const {

	public Def_Const_V4(Identifier identifier, Type type, Value value) {
		super(identifier, type, value);
	}

	@Override
	protected int reparse(TTCN3ReparseUpdater aReparser) {
		return ((TTCN3ReparseUpdater_V4)aReparser).parse(new ITTCN3ReparseBase_V4() {
			@Override
			public void reparse(final TTCN3Reparser4 parser) {
				Value newValue = parser.pr_Expression().value;
				parser.pr_EndOfFile();
				if ( parser.isErrorListEmpty() ) {
					if (newValue != null) {
						value = newValue;
					}
				}
			}
		});
	}
}
