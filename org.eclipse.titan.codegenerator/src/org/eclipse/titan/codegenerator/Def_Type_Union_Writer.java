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
import java.util.List;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;

public class Def_Type_Union_Writer {
	private SourceCode code = new SourceCode();

	private List<Field> fields = new ArrayList<>();

	private String nodeName = null;

	public Def_Type_Union_Writer(Def_Type typeNode) {
		super();
		nodeName = typeNode.getIdentifier().toString();
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

	private String getClassName(Field field) {
		return "SC_" + field.name + "_" + nodeName;
	}

	private void writeMatcher() {
		code.indent(1).line("public static boolean match(", nodeName, " pattern, Object message) {");
		code.indent(2).line("if (!(message instanceof ", nodeName, ")) return false;");
		code.indent(2).line("if (pattern.omitField && ((" + nodeName + ") message).omitField) return true;");
		code.indent(2).line("if (pattern.anyOrOmitField) return true;");
		code.indent(2).line("if (pattern.anyField && !((", nodeName, ") message).omitField) return true;");
		code.indent(2).line("if (pattern.omitField && !((", nodeName, ") message).omitField) return false;");
		code.indent(2).line("if (pattern.anyField && ((", nodeName, ") message).omitField) return false;");
		for (Field f : fields) {
			String className = getClassName(f);
			code.indent(2).line("if (pattern instanceof ", className, " && message instanceof ", className, ")");
			code.indent(3).line("return ", className, ".match((", className, ") pattern, (", className, ") message);");
		}
		code.indent(2).line("return false;");
		code.indent(1).line("}").newLine();
	}

	private void writeEquals() {
		code.indent(1).line("public BOOLEAN equals(" + nodeName + " v) {");
		for (Field f : fields) {
			String className = getClassName(f);
			code.indent(2).line("if (this instanceof ", className, " && v instanceof ", className, ")");
			code.indent(3).line("return ((", className, ") this).equals((", className, ") v);");
		}
		code.indent(2).line("return BOOLEAN.FALSE;");
		code.indent(1).line("}");
	}

	public void writeUnionClasses() {
		String fileNameBackup = myASTVisitor.currentFileName;

		for (Field f : fields) {
			SourceCode code = new SourceCode();
			// set file name
			String className = getClassName(f);
			myASTVisitor.currentFileName = className;
			myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings);

			code.line("public class ", className, " extends ", nodeName, " {");

			String type = f.type;
			String name = f.name;

			code.indent(1).line("public ", type, " ", name, ";").newLine();

			code.indent(1).line("public ", className, "() {}").newLine();
			code.indent(1).line("public ", className, "(", type, " ", name,") {");
			code.indent(2).line("this.", name, " = ", name, ";");
			code.indent(1).line("}").newLine();

			code.indent(1).line("public static boolean match(", className, " pattern, Object message) {");
			code.indent(2).line("if (!(message instanceof ", className, ")) return false;");
			code.indent(2).line("if (pattern.omitField && ((" + className + ") message).omitField) return true;");
			code.indent(2).line("if (pattern.anyOrOmitField) return true;");
			code.indent(2).line("if (pattern.anyField && !((", className, ") message).omitField) return true;");
			code.indent(2).line("if (pattern.omitField && !((", className, ") message).omitField) return false;");
			code.indent(2).line("if (pattern.anyField && ((", className, ") message).omitField) return false;");
			code.indent(2).line("return ", type, ".match(pattern.", name, ", ((", className, ")message).", name, ");");
			code.indent(1).line("}").newLine();

			code.indent(1).line("public BOOLEAN equals(", className, " v) {");
			code.indent(2).line("return this.", name, ".equals(v.", name, ");");
			code.indent(1).line("}").newLine();

			code.indent(1).line("public String toString() {");
			code.indent(2).line("return toString(\"\");");
			code.indent(1).line("}").newLine();

			code.indent(1).line("public String toString(String tabs) {");
			code.indent(2).line("if(anyField) return \"?\";");
			code.indent(2).line("if(omitField) return \"omit\";");
			code.indent(2).line("if(anyOrOmitField) return \"*\";");
			code.indent(2).line("return ", name, ".toString(tabs);");
			code.indent(1).line("}").newLine();

			code.line("}");

			myASTVisitor.visualizeNodeToJava(code.toString());
		}
		myASTVisitor.currentFileName = fileNameBackup;
	}

	public void writeToString() {
		code.indent(1).line("public String toString() {");
		code.indent(2).line("return toString(\"\");");
		code.indent(1).line("}");
	}

	public void writeToStringWithParam() {
		code.indent(1).line("public String toString(String tabs) {");
		code.indent(2).line("if (anyField) return \"?\";");
		code.indent(2).line("if (omitField) return \"omit\";");
		code.indent(2).line("if (anyOrOmitField) return \"*\";");
		for (Field f : fields) {
			String className = getClassName(f);
			code.indent(2).line("if (this instanceof ", className, ")");
			code.indent(3).line("return ((", className, ") this).toString(tabs);");
		}
		code.indent(2).line("return \"\";");
		code.indent(1).line("}");
	}
	
	public void writeCheckValue(){
		code.indent(1).line("public void checkValue() throws IndexOutOfBoundsException {");
		code.indent(1).line("}");
	}

	public String getJavaSource() {
		code.clear();
		AstWalkerJava.logToConsole("	Starting processing:  Union " + nodeName);
		code.line("public class ", nodeName, " extends UnionDef {");
		this.writeMatcher();
		code.newLine();
		this.writeEquals();
		code.newLine();
		this.writeToString();
		code.newLine();
		this.writeCheckValue();
		code.newLine();
		this.writeToStringWithParam();
		code.newLine();
		code.line("}");

		this.writeUnionClasses();

		AstWalkerJava.logToConsole("	Finished processing:  Union " + nodeName);
		return code.toString();
	}

}
