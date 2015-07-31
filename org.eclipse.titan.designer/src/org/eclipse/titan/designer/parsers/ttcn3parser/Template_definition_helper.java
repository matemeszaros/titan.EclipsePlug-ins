/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.parsers.Parser_Helper;

/**
 * @author Kristof Szabados
 * */
@Parser_Helper
public class Template_definition_helper {
	public Identifier identifier = null;
	public Type type = null;
	public FormalParameterList formalParList = null;
}
