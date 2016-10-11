/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

public interface ITtcn3FileReparser {
	
	/**
	 * Runs the reparsing process
	 * @return true if syntactically outdated
	 *         false otherwise 
	 */
	public boolean parse();
}
