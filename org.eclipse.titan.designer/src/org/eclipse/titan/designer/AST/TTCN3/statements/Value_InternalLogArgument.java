/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class Value_InternalLogArgument extends InternalLogArgument {
	private IValue value;

	public Value_InternalLogArgument(final IValue value) {
		super(ArgumentType.Value);
		this.value = value;
	}

	public IValue getValue() {
		return value;
	}

	@Override
	public void checkRecursions(CompilationTimeStamp timestamp, IReferenceChain referenceChain) {
		if (value == null) {
			return;
		}

		value.checkRecursions(timestamp, referenceChain);
	}
}
