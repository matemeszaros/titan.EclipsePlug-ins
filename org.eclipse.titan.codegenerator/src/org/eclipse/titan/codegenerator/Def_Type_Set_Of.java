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
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

public class Def_Type_Set_Of {
	private Def_Type typeNode;
	private StringBuilder setOfString = new StringBuilder("");
	private CompilationTimeStamp setOfCounter = CompilationTimeStamp
			.getNewCompilationCounter();
	private String nodeName=null;
	
	private String fieldName;

	private static Map<String, Object> setOfHashes = new LinkedHashMap<String, Object>();

	private Def_Type_Set_Of(Def_Type typeNode) {
		super();
		this.typeNode = typeNode;
		nodeName = typeNode.getIdentifier().toString();
	}

	public static Def_Type_Set_Of getInstance(Def_Type typeNode) {
		if (!setOfHashes.containsKey(typeNode.getIdentifier().toString())) {
			setOfHashes.put(typeNode.getIdentifier().toString(),
					new Def_Type_Set_Of(typeNode));
		}
		return (Def_Type_Set_Of) setOfHashes.get(typeNode.getIdentifier()
				.toString());
	}

	public void addField(String field) {
		fieldName = field;
	}

	private void writeConstructor() {
		setOfString.append("public " + nodeName
				+ "(){" + "\r\n");
		setOfString.append("value=new HashSet<" + fieldName + ">();" + "\r\n");
		setOfString.append("}" + "\r\n");
		setOfString.append("public " + nodeName
				+ "(HashSet<" + fieldName + "> v){" + "\r\n");
		setOfString.append("	value=v;" + "\r\n");
		setOfString.append("}" + "\r\n");
	}

	private void writeMatcher() {
		setOfString.append("public static boolean match("
				+ nodeName + " pattern, " + "Object"
				+ " message){" + "\r\n");

		setOfString.append("if(!(message instanceof "
				+ nodeName + ")) return false;"
				+ "\r\n");
		setOfString.append("if(pattern.omitField&&(("
				+ nodeName
				+ ")message).omitField) return true;" + "\r\n");
		setOfString.append("if(pattern.anyOrOmitField) return true;" + "\r\n");
		setOfString.append("if(pattern.anyField&&!(("
				+ nodeName
				+ ")message).omitField) return true;" + "\r\n");
		setOfString.append("if(pattern.omitField&&!(("
				+ nodeName
				+ ")message).omitField) return false;" + "\r\n");
		setOfString.append("if(pattern.anyField&&(("
				+ nodeName
				+ ")message).omitField) return false;" + "\r\n");


		setOfString.append("return pattern.equals(("
				+ nodeName + ")message);" + "\r\n");
		setOfString.append("}\r\n");

	}

	private void writeEquals() {
		setOfString.append("public boolean equals(SetOfDef<" + fieldName
				+ "> v) {" + "\r\n");
		setOfString.append("	if(value.size()!=v.value.size()) return false;"
				+ "\r\n");
		setOfString.append("	for (" + fieldName + " i : value) {" + "\r\n");
		setOfString.append("        boolean found = false;" + "\r\n");
		setOfString.append("        for (" + fieldName + " j : v.value){"
				+ "\r\n");
		setOfString.append("        	if (i.equals(j)) {" + "\r\n");
		setOfString.append("                found = true;" + "\r\n");
		setOfString.append("                break;" + "\r\n");
		setOfString.append("            }" + "\r\n");
		setOfString.append("        }" + "\r\n");
		setOfString.append("        if (!found) return false;" + "\r\n");
		setOfString.append("    }" + "\r\n");
		setOfString.append("    return true;" + "\r\n");
		setOfString.append("}" + "\r\n");

	}

	public String getJavaSource() {
		setOfString.append("class " + nodeName
				+ " extends SetOfDef<" + fieldName + ">{" + "\r\n");
		this.writeConstructor();
		this.writeMatcher();
		this.writeEquals();
		setOfString.append("\r\n}");
		String returnString = setOfString.toString();
		setOfString.setLength(0);

		return returnString;
	}

}
