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

public class Def_Type_Union_Writer {
	private Def_Type typeNode;
	private StringBuilder unionString = new StringBuilder("");

	public List<String> compFieldTypes = new ArrayList<String>();
	public List<String> compFieldNames = new ArrayList<String>();
	private String nodeName = null;

	private static Map<String, Object> unionHashes = new LinkedHashMap<String, Object>();

	private Def_Type_Union_Writer(Def_Type typeNode) {
		super();
		this.typeNode = typeNode;
		nodeName = this.typeNode.getIdentifier().toString();

	}

	public static Def_Type_Union_Writer getInstance(Def_Type typeNode) {
		if (!unionHashes.containsKey(typeNode.getIdentifier().toString())) {
			unionHashes.put(typeNode.getIdentifier().toString(),
					new Def_Type_Union_Writer(typeNode));
		}
		return (Def_Type_Union_Writer) unionHashes.get(typeNode.getIdentifier()
				.toString());
	}

	private void writeCompFields() {
		// TODO Auto-generated method stub

	}

	private void writeMatcher() {
		unionString.append("public static boolean match(" + nodeName
				+ " pattern, " + " Object" + " message){" + "\r\n");

		unionString.append("if(!(message instanceof " + nodeName
				+ "))return false;" + "\r\n");

		unionString.append("if(pattern.omitField&&((" + nodeName
				+ ")message).omitField) return true;" + "\r\n");
		unionString.append("if(pattern.anyOrOmitField) return true;" + "\r\n");
		unionString.append("if(pattern.anyField&&!((" + nodeName
				+ ")message).omitField) return true;" + "\r\n");
		unionString.append("if(pattern.omitField&&!((" + nodeName
				+ ")message).omitField) return false;" + "\r\n");
		unionString.append("if(pattern.anyField&&((" + nodeName
				+ ")message).omitField) return false;" + "\r\n");

		for (int l = 0; l < compFieldTypes.size(); l++) {
			unionString.append("if(pattern instanceof " + "SC_" + (l + 1) + "_"
					+ nodeName + " && message instanceof " + "SC_" + (l + 1)
					+ "_" + nodeName + ") return " + "SC_" + (l + 1) + "_"
					+ nodeName + ".match((" + "SC_" + (l + 1) + "_" + nodeName
					+ ")pattern, (" + "SC_" + (l + 1) + "_" + nodeName
					+ ")message);" + "\r\n");
		}

		unionString.append("return false;" + "\r\n");
		unionString.append("}" + "\r\n");

	}

	private void writeEquals() {
		unionString.append("public BOOLEAN equals(" + nodeName + " v){ "
				+ "\r\n");

		for (int l = 0; l < compFieldTypes.size(); l++) {

			unionString.append("if(this instanceof " + "SC_" + (l + 1) + "_"
					+ nodeName + " && v instanceof " + "SC_" + (l + 1) + "_"
					+ nodeName + ") return ((" + "SC_" + (l + 1) + "_"
					+ nodeName + ")this).equals((" + "SC_" + (l + 1) + "_"
					+ nodeName + ")v);" + "\r\n");

		}

		unionString.append("	return new BOOLEAN(false);" + "\r\n");
		unionString.append("}" + "\r\n");

	}

	public void writeUnionClasses() {

		StringBuilder unionChildString = new StringBuilder("");
		String fileNameBackup = myASTVisitor.currentFileName;

		for (int i = 0; i < compFieldTypes.size(); i++) {
			// set file name
			myASTVisitor.currentFileName = "SC_" + (i + 1) + "_" + nodeName;
			myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings);

			unionChildString.append("\r\nclass SC_" + (i + 1) + "_" + nodeName
					+ " extends " + nodeName + "{\r\n");

			unionChildString.append("	");

			unionChildString.append("public " + compFieldTypes.get(i) + " "
					+ compFieldNames.get(i) + ";\r\n");

			unionChildString.append("public static boolean match(" + "SC_"
					+ (i + 1) + "_" + nodeName + " pattern, " + "Object"
					+ " message){" + "\r\n");

			/*
			 * if (AstWalkerJava.areCommentsAllowed) {
			 * unionChildString.append("System.out.println(\"SC_" + (i + 1) +
			 * "\");" + "\r\n"); }
			 */

			unionChildString.append("if(!(message instanceof " + "SC_"
					+ (i + 1) + "_" + nodeName + ")) return false;" + "\r\n");
			unionChildString.append("if(pattern.omitField&&((" + "SC_"
					+ (i + 1) + "_" + nodeName
					+ ")message).omitField) return true;" + "\r\n");
			unionChildString.append("if(pattern.anyOrOmitField) return true;"
					+ "\r\n");
			unionChildString.append("if(pattern.anyField&&!((" + "SC_"
					+ (i + 1) + "_" + nodeName
					+ ")message).omitField) return true;" + "\r\n");
			unionChildString.append("if(pattern.omitField&&!((" + "SC_"
					+ (i + 1) + "_" + nodeName
					+ ")message).omitField) return false;" + "\r\n");
			unionChildString.append("if(pattern.anyField&&((" + "SC_" + (i + 1)
					+ "_" + nodeName + ")message).omitField) return false;"
					+ "\r\n");

			unionChildString.append("	return " + compFieldTypes.get(i)
					+ ".match(pattern." + compFieldNames.get(i) + ", (("
					+ "SC_" + (i + 1) + "_" + nodeName + ")message)."
					+ compFieldNames.get(i) + ");" + "\r\n");

			unionChildString.append("}\r\n");

			unionChildString.append("public BOOLEAN equals(" + "SC_" + (i + 1)
					+ "_" + nodeName + " v){" + "\r\n");

			unionChildString.append("	return this." + compFieldNames.get(i)
					+ ".equals(v." + compFieldNames.get(i) + ");" + "\r\n");

			unionChildString.append("}" + "\r\n");

			unionChildString.append("public String toString(){" + "\r\n");
			unionChildString.append("	return toString(\"\");" + "\r\n");
			unionChildString.append("}" + "\r\n");
			unionChildString.append("public String toString(String tabs){"
					+ "\r\n");
			unionChildString.append("	if(anyField) return \"?\";" + "\r\n");
			unionChildString.append("	if(omitField) return \"omit\";" + "\r\n");
			unionChildString.append("	if(anyOrOmitField) return \"*\";"
					+ "\r\n");
			unionChildString.append("	return " + compFieldNames.get(i)
					+ ".toString(tabs);" + "\r\n");
			unionChildString.append("}" + "\r\n");

			unionChildString.append("}" + "\r\n");
			myASTVisitor.visualizeNodeToJava(unionChildString.toString());
			unionChildString.delete(0, unionChildString.length());

		}
		myASTVisitor.currentFileName = fileNameBackup;

	}

	public void writeToString() {
		unionString.append("public String toString(){" + "\r\n");
		unionString.append("return toString(\"\");" + "\r\n");
		unionString.append("}\r\n");
	}

	public void writeToStringWithParam() {
		unionString.append("public String toString(String tabs){" + "\r\n");
		unionString.append("if(anyField) return \"?\";" + "\r\n");
		unionString.append("if(omitField) return \"omit\";" + "\r\n");
		unionString.append("if(anyOrOmitField) return \"*\";" + "\r\n");

		for (int l = 0; l < compFieldTypes.size(); l++) {

			unionString.append("if(this instanceof " + "SC_" + (l + 1) + "_"
					+ nodeName + ") return ((" + "SC_" + (l + 1) + "_"
					+ nodeName + ")this).toString(tabs);" + "\r\n");

		}

		unionString.append("	return \"\";" + "\r\n");
		unionString.append("}\r\n");
	}

	public void clearLists() {
		compFieldTypes.clear();
		compFieldNames.clear();
	}

	public String getJavaSource() {

		AstWalkerJava.logToConsole("	Starting processing:  Union " + nodeName);

		unionString.append("class " + nodeName + " extends UnionDef{" + "\r\n");
		this.writeCompFields();
		this.writeMatcher();
		this.writeEquals();
		this.writeToString();
		this.writeToStringWithParam();
		this.writeUnionClasses();

		unionString.append("\r\n}");
		String returnString = unionString.toString();
		unionString.setLength(0);

		AstWalkerJava.logToConsole("	Finished processing:  Union " + nodeName);

		return returnString;
	}

}
