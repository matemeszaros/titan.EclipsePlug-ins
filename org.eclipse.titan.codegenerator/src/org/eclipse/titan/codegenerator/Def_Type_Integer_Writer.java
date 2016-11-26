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
import java.util.StringJoiner;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;

public class Def_Type_Integer_Writer {
	private SourceCode code = new SourceCode();

	private String nodeName = null;

	public static List<String> allowedValues = new ArrayList<>();

	private static Map<String, Object> integerHashes = new LinkedHashMap<>();

	private Def_Type_Integer_Writer(Def_Type typeNode) {
		super();
		nodeName = typeNode.getIdentifier().toString();
	}

	public static Def_Type_Integer_Writer getInstance(Def_Type typeNode) {
		if (!integerHashes.containsKey(typeNode.getIdentifier().toString())) {
			integerHashes.put(typeNode.getIdentifier().toString(), new Def_Type_Integer_Writer(typeNode));
		}
		return (Def_Type_Integer_Writer) integerHashes.get(typeNode.getIdentifier().toString());
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


	private void writeConstructor() {
		code.indent(1).line("public ", nodeName, "(INTEGER val) {");
		code.indent(2).line("super(val);");

		// TODO move the SymbolDB update elsewhere (parser / visitor)
		if(allowedValues.size()>0){
			StringJoiner values = new StringJoiner(", ");
			allowedValues.forEach(values::add);
			myASTVisitor.nodeNameAllowedValuesHashmap.put(nodeName, values.toString());
		}
		// TODO move data transformation elsewhere (parser / visitor)
		//split values of referenced parameters
		for (int i = 0; i < allowedValues.size(); i++) {
			String[] values=allowedValues.get(i).split(", ");
			allowedValues.remove(i);
			for (int j = values.length-1; j>=0 ; j--){
				if(allowedValues.size()==j){
					allowedValues.add(values[j]);
				} else {
					allowedValues.add(i, values[j]);
				}
			}
		}
		
		//allowedValues=allowedValues.get(0).sp(", ");
		for (String value : allowedValues) {
			if (value.startsWith("new SubTypeInterval")) {
				code.indent(2).line("allowedIntervals.add(", value, ");");
			} else {
				code.indent(2).line("allowedValues.add(", value, ");");
			}
		}
		code.indent(2).line("checkValue();");
		code.indent(1).line("}");
		code.newLine();
		code.indent(1).line("public ", nodeName, "(String val) {");
		code.indent(2).line("this(new INTEGER(val));");
		code.indent(1).line("}");
		code.newLine();
		code.indent(1).line("protected ", nodeName, "() {");
		code.indent(2).line("super();");
		code.indent(1).line("}");
	}

	private void writeMatcher() {
		code.indent(1).line("public static boolean match(", nodeName, " pattern, Object message) {");
		code.indent(2).line("if (!(message instanceof ", nodeName, ")) return false;");
		code.indent(2).line("return INTEGER.match(pattern.value, ((", nodeName, ")message).value);");
		code.indent(1).line("}");
	}

	private void writeEquals() {
		code.indent(1).line("public BOOLEAN equals(", nodeName, " v) {");
		code.indent(2).line("return value.equals(v.value);");
		code.indent(1).line("}");
	}

	public void clearLists() {
		// TODO put lists and fields here which should be initialized
	}

	public String getJavaSource() {
		code.clear();
		AstWalkerJava.logToConsole("	Starting processing:  Integer " + nodeName);
		code.line("public class ", nodeName, " extends SubTypeDef<INTEGER> {");
		this.writeTemplateObjects();
		code.newLine();
		this.writeConstructor();
		code.newLine();
		this.writeMatcher();
		code.newLine();
		this.writeEquals();
		code.line("}");
		return code.toString();
	}

}
