/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;

/**
 * @author Kristof Szabados
 * */
public class ReturnType_Helper {
	public Type type;
	public boolean returnsTemplate;
	public TemplateRestriction.Restriction_type templateRestriction;
}
