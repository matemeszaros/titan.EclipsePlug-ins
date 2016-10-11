lexer grammar PreprocessorDirectiveLexer;
/*
******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************
*/

/**
 * @author Laszlo Baji
 * @author Arpad Lovassy
 */

DIRECTIVE_IFDEF : 'ifdef'; DIRECTIVE_IFNDEF : 'ifndef'; DIRECTIVE_IF : 'if';
DIRECTIVE_ELSE : 'else'; DIRECTIVE_ELIF : 'elif'; DIRECTIVE_ENDIF : 'endif';
DIRECTIVE_DEFINE : 'define'; DIRECTIVE_UNDEF : 'undef'; OP_DEFINED : 'defined';	
DIRECTIVE_INCLUDE : 'include';
DIRECTIVE_LINECONTROL : 'line';

WS:
(  ' '
|  '\t'
|  '\n' 
|  '\u000B'
|  '\u000C'
) -> skip
;

SL_COMMENT:
'//' (~'\n')* -> skip
;


ML_COMMENT:
(
	'/*' (.*?) (ML_COMMENT)* '*/'
) -> skip
;

CSTRING:
(
'"'
	(
		'\"\"'
	|	~('"')
	)*
'"'
)
;

OCTINT:
'0' [0-7]*
;

DECINT:
[1-9][0-9]*
;

HEXINT:
'0x' [0-9a-fA-F]+
;

DIRECTIVE_PRAGMA : 'pragma' .*? EOF; 
DIRECTIVE_ERROR : 'error' .*? EOF; 
DIRECTIVE_WARNING :	'warning' .*? EOF;

NOT :	'not'|'NOT';

IDENTIFIER:
[A-Za-z_][A-Za-z0-9_]*
;

OP_EQ :		'==';
OP_NE :		'!=';
OP_GT :		'>';
OP_LT :		'<';
OP_GE :		'>=';
OP_LE :		'<=';

OP_MINUS :	'-';
OP_PLUS :	'+';
OP_MUL :	'*';
OP_DIV :	'/';
OP_MOD :	'%';

OP_AND :	'&&';
OP_OR :		'||';
OP_NOT :	'!';
OP_BITAND :	'&';
OP_BITOR :	'|';
OP_XOR :	'^';

OP_LSHIFT :	'<<';
OP_RSHIFT :	'>>';

LPAREN :	'(';
RPAREN :	')';

QUESTIONMARK :'?';
COLON :		':';
