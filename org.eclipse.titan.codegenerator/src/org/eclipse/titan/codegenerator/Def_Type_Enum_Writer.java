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

public class Def_Type_Enum_Writer {
	private Def_Type typeNode;
	private StringBuilder enumString = new StringBuilder("");

	public List<String> enumItems = new ArrayList<String>();
	public List<String> enumItemValues = new ArrayList<String>();
	private String nodeName = null;

	private static Map<String, Object> enumStringHashes = new LinkedHashMap<String, Object>();

	private Def_Type_Enum_Writer(Def_Type typeNode) {
		super();
		this.typeNode = typeNode;
		nodeName = this.typeNode.getIdentifier().toString();
	}

	public static Def_Type_Enum_Writer getInstance(Def_Type typeNode) {
		if (!enumStringHashes.containsKey(typeNode.getIdentifier().toString())) {
			enumStringHashes.put(typeNode.getIdentifier().toString(),
					new Def_Type_Enum_Writer(typeNode));
		}
		return (Def_Type_Enum_Writer) enumStringHashes.get(typeNode
				.getIdentifier().toString());
	}

	private void writeConstructors() {
		enumString.append("public " + nodeName + "(){" + "\r\n");
		int enumValueCounter = 0;
		for (int i = 0; i < enumItems.size(); i++) {
			enumString.append("values.put(\"" + enumItems.get(i) + "\",");
			if (enumItemValues.get(i) != null) {
				enumString.append(enumItemValues.get(i) + ");" + "\r\n");
			} else {
				while (enumItemValues.contains(Integer
						.toString(enumValueCounter))) {
					enumValueCounter++;
				}
				enumString.append(+enumValueCounter + ");" + "\r\n");
				enumValueCounter++;
			}

		}
		enumString.append("}" + "\r\n");

		enumString.append("public " + nodeName + "(String v){" + "\r\n");
		enumString.append("this();" + "\r\n");
		enumString.append("setValue(v);}");
	}

	public void clearLists() {
		enumItems.clear();
		enumItemValues.clear();
	}

	public String getJavaSource() {

		AstWalkerJava.logToConsole("	Starting processing:  Enum " + nodeName);

		enumString
				.append("class " + nodeName + " extends ENUMERATED{" + "\r\n");
		this.writeConstructors();
		enumString.append("\r\n}");
		String returnString = enumString.toString();
		enumString.setLength(0);

		AstWalkerJava.logToConsole("	Finished processing:  Enum " + nodeName);

		return returnString;
	}

}
