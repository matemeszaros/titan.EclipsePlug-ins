/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.asn1parser;

import org.antlr.v4.runtime.Token;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.parsers.Parser_Helper;

/**
 * ANTLR 4 version
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
@Parser_Helper
public class FormalParameter_Helper_V4 {
	public Token governorToken;
	public Token formalParameterToken;
	public Identifier identifier;
}
