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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;

public class Def_Type_Record_Writer implements JavaSourceProvider {

	private SourceCode code = new SourceCode();

	private List<Field> fields = new ArrayList<>();

	private final String nodeName;

	private static Map<String, Object> recordHashes = new LinkedHashMap<>();

	Def_Type_Record_Writer(Def_Type typeNode) {
		this.nodeName = typeNode.getIdentifier().toString();
	}

	public static Def_Type_Record_Writer getInstance(Def_Type typeNode) {
		if (!recordHashes.containsKey(typeNode.getIdentifier().toString())) {
			recordHashes.put(typeNode.getIdentifier().toString(),
					new Def_Type_Record_Writer(typeNode));
		}
		return (Def_Type_Record_Writer) recordHashes.get(typeNode
				.getIdentifier().toString());
	}

	public void add(List<String> fieldTypes, List<String> fieldNames) {
		if (fieldTypes.size() != fieldNames.size()) {
			// TODO : log the error, or throw an exception?
			System.err.println("Record field type-name array size mismatch!");
		}
		for (int i = 0; i < fieldTypes.size(); i++) {
			fields.add(new Field(fieldTypes.get(i), fieldNames.get(i)));
		}
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

	private void writeCompFields() {
		for (Field f : fields) {
			code.indent(1).line(f, ";");
		}
	}

	private void writeMatcher() {
		code.indent(1).line("public static boolean match(", nodeName, " pattern, Object object) {");
		code.indent(2).line("if (!(object instanceof ", nodeName, ")) return false;");
		code.indent(2).line(nodeName, " message = (", nodeName, ") object;");
		code.indent(2).line("if (pattern.omitField && message.omitField) return true;");
		code.indent(2).line("if (pattern.anyOrOmitField) return true;");
		code.indent(2).line("if (pattern.anyField && !message.omitField) return true;");
		code.indent(2).line("if (pattern.omitField && !message.omitField) return false;");
		code.indent(2).line("if (pattern.anyField && message.omitField) return false;");
		code.indent(2).append("return true");
		for (Field f : fields) {
			code.newLine();
			code.indent(4).append("&& ", f.type, ".match(pattern.", f.name, ", message.", f.name, ")");
		}
		code.append(";").newLine();
		code.indent(1).line("}");
	}

	private void writeEquals() {
		code.indent(1).line("public BOOLEAN equals(", nodeName, " v) {");
		for (Field f : fields) {
			code.indent(2).line("if (!", f.name, ".equals(v.", f.name, ").getValue())return BOOLEAN.FALSE;");
		}
		code.indent(2).line("return BOOLEAN.TRUE;");
		code.indent(1).line("}");
	}

	private void writeConstructor() {
		code.indent(1).line(nodeName, "() {");
		code.indent(2).line("super();");
		for (Field f : fields) {
			code.indent(2).line("fieldsInOrder.add(\"", f.name, "\");");
		}
		code.indent(1).line("}");

		if (0 < fields.size()) {
			code.newLine();
			StringJoiner params = new StringJoiner(", ");
			fields.forEach(f -> params.add(f.toString()));
			code.indent(1).line("public ", nodeName, "(", params, ") {");
			code.indent(2).line("this();");
			for (Field f : fields) {
				code.indent(2).line("this.", f.name, " = ", f.name, ";");
			}
			code.indent(1).line("}");
		}
	}

	private void writeToString() {
		code.indent(1).line("public String toString() {");
		code.indent(2).line("return toString(\"\");");
		code.indent(1).line("}");
	}

	private void writeToStringWithParam() {
		code.indent(1).line("public String toString(String tabs) {");
		code.indent(2).line("if (anyField) return \"?\";");
		code.indent(2).line("if (omitField) return \"omit\";");
		code.indent(2).line("if (anyOrOmitField) return \"*\";");
		code.indent(2).line("return \"{\\n\" + ");
		for (int i = 0; i < fields.size(); i++) {
			String name = fields.get(i).name;
			code.indent(4).append("tabs + \"\\t\" + \"", name, " := \" + ", name, ".toString(tabs + \"\\t\") + \"");
			if (i < fields.size() - 1) {
				code.append(",");
			}
			code.line("\\n\" +");
		}
		code.indent(4).line("tabs + \"}\";");
		code.indent(1).line("}");
	}

	public void clearLists() {
		fields.clear();
	}
	
	private void writeCheckValue(){
		code.indent(1).line(" public void checkValue() throws IndexOutOfBoundsException {");
		code.indent(2).line("	return;");
		code.indent(1).line("}");
	}

	@Override
	public String getJavaSource() {
		code.clear();
		AstWalkerJava.logToConsole("	Starting processing:  Record " + nodeName);
		code.line("public class ", nodeName, " extends RecordDef {");
		this.writeTemplateObjects();
		code.newLine();
		this.writeCompFields();
		code.newLine();
		this.writeConstructor();
		code.newLine();
		this.writeMatcher();
		code.newLine();
		this.writeEquals();
		code.newLine();
		this.writeToStringWithParam();
		code.newLine();
		this.writeToString();
		code.line();
		this.writeCheckValue();
		code.line();
		code.line("}");
		AstWalkerJava.logToConsole("	Finished processing:  Record " + nodeName);
		return code.toString();
	}
}
