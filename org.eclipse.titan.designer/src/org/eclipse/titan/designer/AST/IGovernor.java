/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public interface IGovernor extends ISetting {

	/**
	 * Does the semantic checking of the governor.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * */
	void check(final CompilationTimeStamp timestamp);
	
	void check(final CompilationTimeStamp timestamp, IReferenceChain refChain);
}