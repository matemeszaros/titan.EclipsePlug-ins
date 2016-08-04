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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;

public class Def_Type_Integer_Writer {
	private Def_Type typeNode;
	private StringBuilder integerString = new StringBuilder("");

	private String nodeName = null;
	public static List<String> allowedValues = new ArrayList<String>();

	private static Map<String, Object> integerHashes = new LinkedHashMap<String, Object>();

	private Def_Type_Integer_Writer(Def_Type typeNode) {
		super();
		this.typeNode = typeNode;
		nodeName = this.typeNode.getIdentifier().toString();
	}

	public static Def_Type_Integer_Writer getInstance(Def_Type typeNode) {
		if (!integerHashes.containsKey(typeNode.getIdentifier().toString())) {
			integerHashes.put(typeNode.getIdentifier().toString(), new Def_Type_Integer_Writer(typeNode));
		}
		return (Def_Type_Integer_Writer) integerHashes.get(typeNode.getIdentifier().toString());
	}

	private void writeConstructor() {
		integerString.append(nodeName + "(" + "INTEGER" + " val){" + "\r\n");
		integerString.append("super(val);\r\n");

		for (int i = 0; i < allowedValues.size(); i++) {

			if (allowedValues.get(i).startsWith("new SubTypeInterval")) {
				integerString.append("allowedIntervals.add(" + allowedValues.get(i) + ");\r\n");
			} else {
				integerString.append("allowedValues.add(" + allowedValues.get(i) + ");\r\n");
			}
		}

		integerString.append("\r\n" + "	" + "	" + "checkValue();");
		integerString.append("\r\n	}\r\n");
	}

	private void writeMatcher() {
		integerString
				.append("public static boolean match(" + nodeName + " pattern, " + "Object " + " message){" + "\r\n");
		integerString.append("if(!(message instanceof " + nodeName + ")) return false;" + "\r\n");
		integerString.append("	return INTEGER.match(pattern.value, ((" + nodeName + ")message).value);" + "\r\n");
		integerString.append("}" + "\r\n");

	}

	private void writeEquals() {
		integerString.append("public BOOLEAN equals(" + nodeName + " v){\r\n");
		integerString.append("	return value.equals(v.value);\r\n");
		integerString.append("}\r\n");

	}

	public void clearLists() {
		// TODO put lists and fields here which should be initialized
	}

	public String getJavaSource() {

		AstWalkerJava.logToConsole("	Starting processing:  Integer " + nodeName);

		integerString.append("class " + nodeName + " extends SubTypeDef<INTEGER>{" + "\r\n");
		this.writeConstructor();
		this.writeMatcher();
		this.writeEquals();
		integerString.append("\r\n}");
		String returnString = integerString.toString();
		integerString.setLength(0);
		allowedValues.clear();
		AstWalkerJava.logToConsole("	Finished processing:  Integer " + nodeName);

		return returnString;
	}

}
