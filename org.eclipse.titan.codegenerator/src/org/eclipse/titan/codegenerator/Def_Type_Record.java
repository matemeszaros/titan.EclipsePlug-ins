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
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

public class Def_Type_Record {
	private Def_Type typeNode;
	private StringBuilder recordString = new StringBuilder("");
	private CompilationTimeStamp compilationCounter = CompilationTimeStamp
			.getNewCompilationCounter();
	private List<String> compFieldTypes = new ArrayList<String>();
	private List<String> compFieldNames = new ArrayList<String>();
	private String nodeName = null;

	private static Map<String, Object> recordHashes = new LinkedHashMap<String, Object>();

	private Def_Type_Record(Def_Type typeNode) {
		super();
		this.typeNode = typeNode;
		nodeName = typeNode.getIdentifier().toString();
	}

	public static Def_Type_Record getInstance(Def_Type typeNode) {
		if (!recordHashes.containsKey(typeNode.getIdentifier().toString())) {
			recordHashes.put(typeNode.getIdentifier().toString(),
					new Def_Type_Record(typeNode));
		}
		return (Def_Type_Record) recordHashes.get(typeNode.getIdentifier()
				.toString());
	}

	public void addCompFields(String type, String name) {
		compFieldTypes.add(type);
		compFieldNames.add(name);
	}

	public void writeCompFields() {

		for (int i = 0; i < compFieldTypes.size(); i++) {
			recordString.append(compFieldTypes.get(i) + " "
					+ compFieldNames.get(i) + ";\r\n");
		}
	}

	public void writeMatcher() {

		recordString.append("public static boolean match(" + nodeName
				+ " pattern, " + "Object" + " message){" + "\r\n");

		recordString.append("if(!(message instanceof " + nodeName
				+ ")) return false;" + "\r\n");

		recordString.append("if(pattern.omitField&&((" + nodeName
				+ ")message).omitField) return true;" + "\r\n");
		recordString.append("if(pattern.anyOrOmitField) return true;" + "\r\n");
		recordString.append("if(pattern.anyField&&!((" + nodeName
				+ ")message).omitField) return true;;" + "\r\n");
		recordString.append("if(pattern.omitField&&!((" + nodeName
				+ ")message).omitField) return false;" + "\r\n");
		recordString.append("if(pattern.anyField&&((" + nodeName
				+ ")message).omitField) return false;" + "\r\n");


		recordString.append("	return "); //

		for (int i = 0; i < compFieldTypes.size(); i++) {

			recordString.append(compFieldTypes.get(i) + ".match(pattern."
					+ compFieldNames.get(i) + ", ((" + nodeName + ")message)."
					+ compFieldNames.get(i) + ")");

			if ((i + 1 < compFieldTypes.size())) {
				recordString.append("&&");
			}
			if ((i + 1) == compFieldTypes.size()) {
				recordString.append(";\r\n");
			}

		}
		recordString.append("}" + "\r\n");

	}

	public void writeEquals() {
		recordString.append("public boolean equals(" + nodeName + " v){ "
				+ "\r\n");// 0901

		for (int i = 0; i < compFieldTypes.size(); i++) {

			recordString.append("	if(!" + compFieldNames.get(i) + ".equals(v."
					+ compFieldNames.get(i) + "))return false;" + "\r\n");

		}

		recordString.append("	return true;" + "\r\n");
		recordString.append("}" + "\r\n");
	}

	public void writeConstructor() {

		recordString.append(nodeName + "(){\r\n");

		recordString.append("super();\r\n");

		for (int i = 0; i < compFieldTypes.size(); i++) {

			recordString.append("fieldsInOrder.add(\"" + compFieldNames.get(i)
					+ "\");\r\n");

		}

		recordString.append("}\r\n");
	}

	public void writeToString() {
		recordString.append("public String toString(){" + "\r\n");
		recordString.append("return toString(\"\");" + "\r\n");
		recordString.append("}\r\n");

	}

	public void writeToStringWithParam() {
		recordString.append("public String toString(String tabs){" + "\r\n");
		recordString.append("if(anyField) return \"?\";" + "\r\n");
		recordString.append("if(omitField) return \"omit\";" + "\r\n");
		recordString.append("if(anyOrOmitField) return \"*\";" + "\r\n");
		recordString.append("return \"{\\n\" + " + "\r\n");

		for (int i = 0; i < compFieldTypes.size(); i++) {
			if((i+1)<compFieldTypes.size()){
			recordString.append("tabs + \"\\t\" + \"" + compFieldNames.get(i)
					+ " := \" + " + compFieldNames.get(i)
					+ ".toString(tabs + \"\\t\") + \",\\n\" +" + "\r\n");
			}else{
				recordString.append("tabs + \"\\t\" + \"" + compFieldNames.get(i)
						+ " := \" + " + compFieldNames.get(i)
						+ ".toString(tabs + \"\\t\") + \"\\n\" +" + "\r\n");
			}
		}

		recordString.append("tabs + \"}\";" + "\r\n");
		recordString.append("}\r\n");

	}

	public String getJavaSource() {
		recordString.append("class " + nodeName + " extends RecordDef{"
				+ "\r\n");
		this.writeCompFields();
		this.writeMatcher();
		this.writeEquals();
		this.writeConstructor();
		this.writeToString();
		this.writeToStringWithParam();
		recordString.append("\r\n}");
		String returnString = recordString.toString();
		recordString.setLength(0);
		compFieldTypes.clear();
		compFieldNames.clear();
		return returnString;
	}
}
