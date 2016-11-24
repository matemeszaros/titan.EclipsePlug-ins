lexer grammar Asn1Lexer;

@header {
//import org.eclipse.titan.designer.parsers.asn1parser.TokenWithIndexAndSubTokensV4;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.AST.Location;
}

/*
******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************
*/
@members{
	private IFile actualFile = null;
	
	public void setActualFile(IFile file){
      actualFile = file;
    }
  
  protected String id_replace_underscores(String param) {
  	String temp = param.replace('_', '-');
  	if(!temp.equals(param)) {
  		Location location = new Location(actualFile, getLine(), getInputStream().index() - param.length(), getInputStream().index());
  		location.reportSyntacticError("`" + param + "' is not a valid ASN.1 identifier. Did you mean `" + temp + "' ?");
  	}
  	return temp;
  }
}

/*
 * @author Laszlo Baji
 * @author Arpad Lovassy
 */

tokens {
BLOCK //THE great block token
}

WS:
(  ' '
|  '\t'
|  '\n' 
|  '\r'
|  '\u000B'
|  '\u000C'
|  '\r\n'
) -> channel(HIDDEN);

fragment NEWLINE:
(  '\r'
|  '\n'
|  '\r\n'
);

ABSENT : 'ABSENT';    ALL : 'ALL';    ANY : 'ANY';     APPLICATION : 'APPLICATION';     AUTOMATIC : 'AUTOMATIC';
BEGIN : 'BEGIN';     BIT : 'BIT';    BMPSTRING : 'BMPString';    BOOLEAN : 'BOOLEAN';   BY : 'BY';
CHARACTER : 'CHARACTER';   CHOICE : 'CHOICE';   CLASS : 'CLASS';    COMPONENT : 'COMPONENT';
COMPONENTS : 'COMPONENTS';     CONSTRAINED : 'CONSTRAINED';   CONTAINING : 'CONTAINING';
DEFAULT : 'DEFAULT';    DEFINED : 'DEFINED';   DEFINITIONS : 'DEFINITIONS'; ENCODED : 'ENCODED';
EMBEDDED : 'EMBEDDED';   END : 'END';   ENUMERATED : 'ENUMERATED';  EXCEPT : 'EXCEPT';
EXPLICIT : 'EXPLICIT';   EXPORTS : 'EXPORTS';   EXTENSIBILITY : 'EXTENSIBILITY';
EXTERNAL : 'EXTERNAL';
FALSE : 'FALSE';  FROM : 'FROM';
GENERALIZEDTIME : 'GeneralizedTime';  GENERALSTRING : 'GeneralString'; GRAPHICSTRING : 'GraphicString';
IA5STRING : 'IA5String';   IDENTIFIER : 'IDENTIFIER';  IMPLICIT : 'IMPLICIT';  IMPLIED : 'IMPLIED';
IMPORTS : 'IMPORTS';   INCLUDES : 'INCLUDES';   INSTANCE : 'INSTANCE'; INTEGER : 'INTEGER';
INTERSECTION : 'INTERSECTION';   ISO646STRING : 'ISO646String';
MAX : 'MAX';  MIN : 'MIN';  MINUS_INFINITY : 'MINUS-INFINITY';
NOT_A_NUMBER : 'NOT-A-NUMBER';
NULL : 'NULL';  NUMERICSTRING : 'NumericString';
OBJECT : 'OBJECT';   OBJECTDESCRIPTOR : 'ObjectDescriptor';  OCTET : 'OCTET';
OF : 'OF';  OPTIONAL : 'OPTIONAL';
PATTERN : 'PATTERN';  PDV : 'PDV';  PLUS_INFINITY : 'PLUS-INFINITY';  PRESENT : 'PRESENT';
PRINTABLESTRING : 'PrintableString';  PRIVATE : 'PRIVATE';
REAL : 'REAL';   RELATIVE_OID : 'RELATIVE-OID'; OID_IRI : 'OID-IRI'; RELATIVE_OID_IRI : 'RELATIVE-OID-IRI';
SEQUENCE : 'SEQUENCE';  SET : 'SET';  SIZE : 'SIZE';  STRING : 'STRING'; SYNTAX : 'SYNTAX';
T61STRING : 'T61String';   TAGS : 'TAGS';  TELETEXSTRING : 'TeletexString';  TRUE : 'TRUE';
UNION : 'UNION';  UNIQUE : 'UNIQUE';  UNIVERSAL : 'UNIVERSAL';
UNIVERSALSTRING : 'UniversalString';  UTCTIME : 'UTCTime';  UTF8STRING : 'UTF8String';
VIDEOTEXSTRING : 'VideotexString';  VISIBLESTRING : 'VisibleString';
WITH : 'WITH'; DATE : 'DATE'; DATE_TIME : 'DATE-TIME'; DURATION : 'DURATION'; TIME : 'TIME';
TIME_OF_DAY : 'TIME-OF-DAY';

UPPERIDENTIFIER:
(
	[A-Z]([\-_]? [A-Za-z0-9]+)*
) {setText(id_replace_underscores(getText()));};

LOWERIDENTIFIER:
(
	[a-z]([\-_]? [A-Za-z0-9]+)*
) {setText(id_replace_underscores(getText()));};

AMPUPPERIDENTIFIER:
  '&' UPPERIDENTIFIER
;

AMPLOWERIDENTIFIER:
  '&' LOWERIDENTIFIER
;

NUMBER:
(  '0'				// special case for just '0'
|  [1-9] [0-9]*	// non-zero decimal
)
;

REALNUMBER: 
(
	NUMBER ([\.] [0-9]+)? ([eE][+\-]? NUMBER)?
);

SINGLELINECOMMENT:
( // TODO, FIXME not handled yet
	'--' .*? 
	(
		'--'
	|	NEWLINE
	|	EOF
	)
) -> channel(HIDDEN);

MULTILINECOMMENT:
(
	'/*' (.*?) (MULTILINECOMMENT)* '*/'
) -> channel(HIDDEN);

fragment BIN:
(
	[01]
|	WS
);

fragment HEX:
(  
	[0-9A-Fa-f]
|	WS
)
;

BSTRING:
(  
	'\'' (BIN)* '\'' 'B'  
	  
);

HSTRING:
(  
	'\'' (HEX)* '\'' 'H' 
);

CSTRING:
(
'"'
	(
		'\"\"'
	|	~('"')
	)*
'"'
);

ASSIGNMENT:				'::='
;

DOT:					'.'
;

RANGESEPARATOR:			'..'
;

ELLIPSIS:				'...'
;

LEFTVERSIONBRACKETS:	'[['
;

RIGHTVERSIONBRACKETS:	']]'
;

BEGINCHAR:				'{'
;
        
ENDCHAR:				'}'
;

LPAREN:					'('
;

RPAREN:					')'
;

SQUAREOPEN:				'['
;

SQUARECLOSE:			']'
;

COMMA:					','
;

MINUS:					'-'
;

COLON:					':'
;

SEMICOLON:				';'
;

ATSYMBOL:				'@'
;

VERTICAL:				'|'
;

EXCLAMATION:			'!'
;

CARET:					'^'
;

LESSTHAN:				'<'
;

MORETHAN:				'>'
;

DOUBLEQUOTE:			'"'
;

ASTERISK:				'*'
;