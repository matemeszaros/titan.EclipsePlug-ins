/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public abstract class InternalLogArgument {
	public enum ArgumentType {
		TemplateInstance, Value, Match, Macro, Reference, String
	}

	private ArgumentType argumentType;

	protected InternalLogArgument(final ArgumentType argumentType) {
		this.argumentType = argumentType;
	}

	public final ArgumentType getArgumentType() {
		return argumentType;
	}

	/**
	 * Checks whether this log argument is defining itself in a recursive
	 * way. This can happen for example if a constant is using itself to
	 * determine its initial value.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references.
	 * */
	public abstract void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);
}
