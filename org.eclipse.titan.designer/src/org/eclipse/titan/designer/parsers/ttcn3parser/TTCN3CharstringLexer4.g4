lexer grammar TTCN3CharstringLexer4;

/*
******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************
*/

@header {
import java.io.Reader;
import java.io.StringReader;

import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.HeadlessStorage;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3Editor;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.core.LoadBalancingUtilities;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;
}

/*
 * author Kristof Szabados
 * author Arpad Lovassy
 */

@members {

private int offset = 0;
private IFile actualFile = null;
private StringBuilder stringBuilder = new StringBuilder();

public void setActualFile(IFile file) {
	actualFile = file;
}

public String getString() {
	return stringBuilder.toString();
}

public int getOffset() {
	return offset;
}

public void setOffset(int offset) {
	this.offset = offset;
}

public static String parseCharstringValue(final String strValue, Location location) {
	Reader reader = new StringReader(strValue);
	CharStream charStream = new UnbufferedCharStream(reader);
	TTCN3CharstringLexer4 lexer = new TTCN3CharstringLexer4(charStream);
	lexer.setTokenFactory( new CommonTokenFactory( true ) );
	lexer.setOffset(location.getOffset() + 1); //needs to be shifted by one because of the \" of the string
	lexer.setLine(location.getLine());
	lexer.setCharPositionInLine(0);
	lexer.setActualFile((IFile)location.getFile());
	while (lexer.nextToken().getType()!=Token.EOF) {}
	String retVal = lexer.getString();
	return retVal;
}

}

/* Two consecutive doublequotes -> one doublequotequote */
DOUBLEQUOTES: '""' { stringBuilder.append('"'); }
;

/* Backslash-escaped singlequote, doublequote, question mark or backslash */
BACKSLASHESCAPED:  '\\' ( '\'' | '"' | '?' | '\\' ) { stringBuilder.append(getText().charAt(1)); }
;

/* TODO: add all escape stuff */

IGNORE:
    ( '\r\n' | '\r' | '\n' ) 
    { 
    	Location location = new Location(actualFile, getLine(), offset - getText().length(), offset);
        location.reportSyntacticWarning("Unescaped newline character");
    	stringBuilder.append(getText()); 
    }
|   . { stringBuilder.append(getText()); }
;
