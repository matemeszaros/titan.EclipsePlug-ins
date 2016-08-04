/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   
 *   Keremi, Andras
 *   Eros, Levente
 *   Kovacs, Gabor
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
//TODO: duplicate of charstring, ONLY A PLACEHOLDER YET
public class Def_Type_Bitstring_Writer {
	private Def_Type typeNode;
	private StringBuilder charStringString = new StringBuilder("");

	private String charStringValue = null;
	private String nodeName = null;

	private static Map<String, Object> charStringHashes = new LinkedHashMap<String, Object>();

	private Def_Type_Bitstring_Writer(Def_Type typeNode) {
		super();
		this.typeNode = typeNode;
		nodeName = this.typeNode.getIdentifier().toString();

	}

	public static Def_Type_Bitstring_Writer getInstance(Def_Type typeNode) {
		if (!charStringHashes.containsKey(typeNode.getIdentifier().toString())) {
			charStringHashes.put(typeNode.getIdentifier().toString(),
					new Def_Type_Bitstring_Writer(typeNode));

		}
		return (Def_Type_Bitstring_Writer) charStringHashes.get(typeNode
				.getIdentifier().toString());
	}

	public void addCharStringValue(String value) {
		charStringValue = value;
	}

	private void writeConstructor() {
		charStringString.append(nodeName + "(" + "CHARSTRING" + " val){");
		charStringString.append("\r\n" + "super(val);");

		if (charStringValue != null) {

			charStringString.append("\r\n" + "allowedValues.add"
					+ "(new CHARSTRING(\"" + charStringValue + "\"));");

		}
		charStringString.append("\r\n	}\r\n");

	}

	private void writeMatcher() {
		charStringString.append("public static boolean match(" + nodeName
				+ " pattern, " + "Object " + " message){" + "\r\n");
		charStringString.append("if(!(message instanceof " + nodeName
				+ ")) return false;" + "\r\n");
		charStringString.append("	return CHARSTRING.match(pattern.value, (("
				+ nodeName + ")message).value);" + "\r\n");
		charStringString.append("}" + "\r\n");
	}

	private void writeEquals() {
		charStringString.append("public boolean equals(" + nodeName
				+ " v){\r\n");
		charStringString.append("	return value.equals(v.value);\r\n");
		charStringString.append("}\r\n");

	}

	public void clearLists(){
		//TODO put lists and fields here which should be initialized
	}
	
	public String getJavaSource() {
		
		AstWalkerJava.logToConsole("	Starting processing:  Bitstring " + nodeName );
		
		charStringString.append("class " + nodeName
				+ " extends SubTypeDef<CHARSTRING>{" + "\r\n");
		this.writeConstructor();
		this.writeMatcher();
		this.writeEquals();
		charStringString.append("\r\n}");
		String returnString = charStringString.toString();
		charStringString.setLength(0);
		charStringValue = null;
		
		AstWalkerJava.logToConsole("	Finished processing:  Bitstring " + nodeName );
		
		return returnString;
	}

}
