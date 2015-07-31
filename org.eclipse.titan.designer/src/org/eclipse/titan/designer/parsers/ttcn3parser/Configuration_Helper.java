/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.parsers.Parser_Helper;

/**
 * @author Kristof Szabados
 * */
@Parser_Helper
public class Configuration_Helper {
	public Reference runsonReference = null;
	public Reference systemReference = null;
	public boolean runsOnSelf = false;
}
