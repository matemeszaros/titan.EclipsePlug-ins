/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.TTCN3.values.Macro_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class Macro_InternalLogArgument extends InternalLogArgument {
	private Macro_Value value;

	public Macro_InternalLogArgument(final Macro_Value value) {
		super(ArgumentType.Macro);
		this.value = value;
	}

	public Macro_Value getMacro() {
		return value;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (value == null) {
			return;
		}

		value.checkRecursions(timestamp, referenceChain);
	}
}
