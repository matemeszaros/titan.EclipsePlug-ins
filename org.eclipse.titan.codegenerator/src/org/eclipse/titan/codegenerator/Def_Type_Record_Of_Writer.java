/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;

public class Def_Type_Record_Of_Writer implements JavaSourceProvider {

	private static Map<String, Def_Type_Record_Of_Writer> writers = new LinkedHashMap<>();

	public static Def_Type_Record_Of_Writer getInstance(Def_Type typeNode) {
		String id = typeNode.getIdentifier().toString();
		if (writers.containsKey(id)) {
			return writers.get(id);
		}
		Def_Type_Record_Of_Writer writer = new Def_Type_Record_Of_Writer(typeNode);
		writers.put(id, writer);
		return writer;
	}

	private SourceCode code = new SourceCode();

	private final String typeName;
	private String fieldType;

	public Def_Type_Record_Of_Writer(Def_Type typeNode) {
		typeName = typeNode.getIdentifier().toString();
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	@Override
	public String getJavaSource() {
		code.clear();
		AstWalkerJava.logToConsole("	Starting processing: RecordOf " + typeName);
		code.line("import java.util.Arrays;");
		code.line();
		code.line("class ", typeName, " extends RecordOfDef<", fieldType, "> {");
		writeTemplateObjects();
		code.line();
		writeConstructors();
		code.line();
		writeMatcher();
		code.line();
		writeEquals();
		code.line();
		this.writeCheckValue();
		code.line();
		code.line("}");
		AstWalkerJava.logToConsole("	Finishing processing: RecordOf " + typeName);
		return code.toString();
	}

	private void writeTemplateObjects() {
		code.indent(1).line("public static final ", typeName, " ANY = new ", typeName, "();");
		code.indent(1).line("public static final ", typeName, " OMIT = new ", typeName, "();");
		code.indent(1).line("public static final ", typeName, " ANY_OR_OMIT = new ", typeName, "();");
		code.newLine();
		code.indent(1).line("static {");
		code.indent(2).line("ANY.anyField = true;");
		code.indent(2).line("OMIT.omitField = true;");
		code.indent(2).line("ANY_OR_OMIT.anyOrOmitField = true;");
		code.indent(1).line("}");
	}

	private void writeConstructors() {
		code.indent(1).line("public ", typeName, "(", fieldType, "... values) {");
		code.indent(2).line("this(Arrays.asList(values));");
		code.indent(1).line("}");
		code.line();
		code.indent(1).line("public ", typeName, "(List<", fieldType, "> list) {");
		code.indent(2).line("value = list;");
		code.indent(1).line("}");
	}

	private void writeMatcher() {
		code.indent(1).line("public static boolean match(", typeName, " pattern, Object message) {");
		code.indent(2).line("if (!(message instanceof ", typeName, ")) return false;");
		// TODO : introduce a type-safe variable instead of casting it each time
		// TODO : simplify the if statements into one boolean expression (eg.: a && b || c)
		code.indent(2).line("if (pattern.omitField && ((", typeName, ")message).omitField) return true;");
		code.indent(2).line("if (pattern.anyOrOmitField) return true;");
		code.indent(2).line("if (pattern.anyField && !((", typeName, ")message).omitField) return true;");
		code.indent(2).line("if (pattern.anyField && !((", typeName, ")message).omitField) return true;");
		code.indent(2).line("if (pattern.omitField && !((", typeName, ")message).omitField) return false;");
		code.indent(2).line("if (pattern.anyField && ((", typeName, ")message).omitField) return false;");
		code.indent(2).line("return pattern.equals((", typeName, ")message).getValue();");
		code.indent(1).line("}");
	}

	private void writeEquals() {
		code.indent(1).line("public BOOLEAN equals(RecordOfDef<", fieldType, "> v) {");
		code.indent(2).line("if (value.size() != v.value.size()) return BOOLEAN.FALSE;");
		code.indent(2).line("for (int i = 0; i < value.size(); ++i) {");
		code.indent(3).line("if (value.get(i) != v.value.get(i)) return BOOLEAN.FALSE;");
		code.indent(2).line("}");
		code.indent(2).line("return BOOLEAN.TRUE;");
		code.indent(1).line("}");
	}
	
	private void writeCheckValue(){
		code.indent(1).line(" public void checkValue() throws IndexOutOfBoundsException {");
		code.indent(2).line("	return;");
		code.indent(1).line("}");
	}
}
