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
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;

public class Def_Type_Charstring_Writer {
	private SourceCode code = new SourceCode();

	private String charStringValue = null;
	private String nodeName = null;

	private static Map<String, Object> charStringHashes = new LinkedHashMap<>();

	private Def_Type_Charstring_Writer(Def_Type typeNode) {
		super();
		nodeName = typeNode.getIdentifier().toString();

	}

	public static Def_Type_Charstring_Writer getInstance(Def_Type typeNode) {
		if (!charStringHashes.containsKey(typeNode.getIdentifier().toString())) {
			charStringHashes.put(typeNode.getIdentifier().toString(),
					new Def_Type_Charstring_Writer(typeNode));

		}
		return (Def_Type_Charstring_Writer) charStringHashes.get(typeNode
				.getIdentifier().toString());
	}

	public void addCharStringValue(String value) {
		charStringValue = value;
	}

	private void writeTemplateObjects() {
		code.indent(1).line("public static final ", nodeName, " ANY = new ", nodeName, "();");
		code.indent(1).line("public static final ", nodeName, " OMIT = new ", nodeName, "();");
		code.indent(1).line("public static final ", nodeName, " ANY_OR_OMIT = new ", nodeName, "();");
		code.newLine();
		code.indent(1).line("static {");
		code.indent(2).line("ANY.anyField = true;");
		code.indent(2).line("OMIT.omitField = true;");
		code.indent(2).line("ANY_OR_OMIT.anyOrOmitField = true;");
		code.indent(1).line("}");
	}

	private void writeConstructors() {
		code.indent(1).line("public ", nodeName, "(CHARSTRING val) {");
		code.indent(2).line("super(val);");
		if (charStringValue != null) {
			code.indent(2).line("allowedValues.add(new CHARSTRING(\"", charStringValue, "\"));");
		}
		code.indent(1).line("}");
		code.newLine();
		code.indent(1).line("public ", nodeName, "(String val) {");
		code.indent(2).line("this(new CHARSTRING(val));");
		code.indent(1).line("}");
		code.newLine();
		code.indent(1).line("protected ", nodeName, "() {");
		code.indent(2).line("super();");
		code.indent(1).line("}");
	}

	private void writeMatcher() {
		code.indent(1).line("public static boolean match(", nodeName, " pattern, Object message) {");
		code.indent(2).line("if (!(message instanceof ", nodeName, ")) return false;");
		// TODO any / omit / anyOrOmit checking?
		code.indent(2).line("return CHARSTRING.match(pattern.value, ((", nodeName, ")message).value);");
		code.indent(1).line("}");
	}

	private void writeEquals() {
		code.indent(1).line("public BOOLEAN equals(", nodeName, " v) {");
		code.indent(2).line("return value.equals(v.value);");
		code.indent(1).line("}");
	}
	
	public void clearLists(){
		//TODO put lists and fields here which should be initialized
	}
	
	public String getJavaSource() {
		code.clear();
		AstWalkerJava.logToConsole("	Starting processing:  Charstring " + nodeName );
		code.line("public class ", nodeName, " extends SubTypeDef<CHARSTRING> {");
		this.writeTemplateObjects();
		code.newLine();
		this.writeConstructors();
		code.newLine();
		this.writeMatcher();
		code.newLine();
		this.writeEquals();
		code.line("}");
		// TODO why clear charStringValue?
		charStringValue = null;
		AstWalkerJava.logToConsole("	Finished processing:  Charstring " + nodeName );
		return code.toString();
	}

}
