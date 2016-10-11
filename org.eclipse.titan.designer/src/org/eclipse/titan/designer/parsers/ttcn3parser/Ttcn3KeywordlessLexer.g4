lexer grammar Ttcn3KeywordlessLexer;
import Ttcn3BaseLexer;

/*
 ******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************
*/

/*
 * author Arpad Lovassy
 */

// NOTE: IDENTIFIER token matches also with keywords

// General macro, used for code completion, it matches any invalid macro that looks like a macro
MACRO:
(	'%' [A-Za-z0-9_]*
|	'__' [A-Za-z0-9_]* '__'
);
