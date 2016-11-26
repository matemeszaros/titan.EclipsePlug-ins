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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.titan.codegenerator.constant.ConstantParser;
import org.eclipse.titan.codegenerator.constant.ModuleConstants;
import org.eclipse.titan.codegenerator.template.ModuleTemplates;
import org.eclipse.titan.codegenerator.template.TemplateParser;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.UnaryMinusExpression;

public final class myASTVisitor extends ASTVisitor implements Scope {

	private Scope scope = this;

	ModuleParameters parameters = new ModuleParameters();
	private ModuleTemplates templates = new ModuleTemplates();
	private ModuleConstants constants = new ModuleConstants();

	public static TestCase_Function_Visit_Handler myFunctionTestCaseVisitHandler = new TestCase_Function_Visit_Handler();
	public static Def_Type_Visit_Handler myDefTypeVisitHandler = new Def_Type_Visit_Handler();

	public static String currentFileName = "";

	public static boolean blockIdListing = false;
	public static boolean isNextIntegerNegative = false;


	public static Map<String, String[]> nodeNameChildrenNamesHashMap = new LinkedHashMap<String, String[]>();
	public static Map<String, String[]> nodeNameChildrenTypesHashMap = new LinkedHashMap<String, String[]>();
	public static Map<String, String> nodeNameSetOfTypesHashMap = new LinkedHashMap<String, String>();
	public static Map<String, String> nodeNameRecordOfTypesHashMap = new LinkedHashMap<>();
	public static Map<String, String> nodeNameNodeTypeHashMap = new LinkedHashMap<String, String>();
	public static Map<String, String> portNamePortTypeHashMap = new LinkedHashMap<String, String>();
	public static Map<String, String> templateIdValuePairs = new LinkedHashMap<String, String>();
	public static Map<String, String> nodeNameAllowedValuesHashmap = new LinkedHashMap<String,  String>();
	
	public static Set<String> constOmitHashes = new HashSet<String>();
	public static Set<String> templateIDs = new HashSet<String>();

	public static String importListStrings = "package org.eclipse.titan.codegenerator.javagen;"
			+ "\r\n"
			+ "import org.eclipse.titan.codegenerator.TTCN3JavaAPI.*;"
			+ "\r\n"
			+ "import java.util.ArrayList;"
			+ "\r\n"
			+ "import java.util.HashSet;"
			+ "\r\n"
			+ "import java.util.List;"
			+ "\r\n"
			+ "import java.util.Vector;"
			+ "\r\n"
			+ "import java.util.concurrent.TimeUnit;"
			+ "\r\n"
			+ "import java.io.ObjectInputStream;"
			+ "\r\n"
			+ "import java.io.ObjectOutputStream;"
			+ "\r\n"
			+ "import java.io.BufferedReader;"
			+ "\r\n"
			+ "import java.io.BufferedWriter;"
			+ "\r\n"
			+ "import java.io.InputStreamReader;"
			+ "\r\n"
			+ "import java.io.OutputStreamWriter;"
			+ "\r\n"
			+ "import java.math.BigInteger;"
			+ "\r\n"
			+ "import java.net.Socket;"
			+ "\r\n"
			+ "import java.net.ServerSocket;" + "\r\n" + "\r\n";

	public static void visualizeNodeToJava(String string) {

		File file = new File(AstWalkerJava.props.getProperty("javafile.path")
				+ currentFileName + ".java");
		FileWriter writer;
		try {
			writer = new FileWriter(file, true);
			PrintWriter printer = new PrintWriter(writer);

			printer.append(string);

			printer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String changeTypeToJava(String string) {
		// TODO : why only CHARSTRING? does it belong here?
		if (string.equals("CHARSTRING")) {
			return "SubTypeDef<CHARSTRING>";
		}
		return TypeMapper.map(string);
	}

	public static String cutModuleNameFromBeginning(String string) {
		String[] parts = string.split("\\.");

		if (parts.length > 1) {
			String part1 = parts[0];
			String part2 = parts[1];

			if (part1.equals(AstWalkerJava.moduleElementName)) {
				string = part2;
			}
		}
		return changeTypeToJava(string);
	}

	public static void deleteLastBracket(String currentFileName) {
		try {

			RandomAccessFile file = new RandomAccessFile(
					AstWalkerJava.props.getProperty("javafile.path")
							+ currentFileName + ".java", "rw");

			long length = file.length();
			file.setLength(length - 3);
			file.close();

		} catch (Exception ex) {

			System.out.println("ERROE :" + ex);
		}
	}

	@Override
	public int visit(IVisitableNode node) {
		scope = scope.process(node);
		return V_CONTINUE;
	}

	@Override
	public Scope process(IVisitableNode node) {
		// visit* submethods only serve increased readability and
		// categorization

		myDefTypeVisitHandler.visit(node);

		if (node instanceof Def_Const) {
			return new ConstantParser(this, constants);
		}

		if (node instanceof Def_Template) {
			return new TemplateParser(this, templates);
		}

		if (node instanceof Def_ModulePar) {
			return new ModuleParameterParser(this);
		}

		if (node instanceof UnaryMinusExpression) {
			myASTVisitor.isNextIntegerNegative = true;
		}

		if (node instanceof Def_Extfunction) {//FormalParameterList Boolean_Type
			
			String nodeName=((Def_Extfunction)node).getIdentifier().toString();
			nodeName.toString();
			//((Def_Extfunction)node).getFormalParameterList();
			// return type is the next *_Type after the Formalparamlist
			//TODO process 
		}

		// Check for Function & TC nodes
		myFunctionTestCaseVisitHandler.visit(node);

		return this;
	}

	@Override
	public int leave(IVisitableNode node) {
		scope = scope.finish(node);
		return V_CONTINUE;
	}

	@Override
	public Scope finish(IVisitableNode node) {

		myDefTypeVisitHandler.leave(node);

		if (node instanceof UnaryMinusExpression) {
			myASTVisitor.isNextIntegerNegative = false;
		}

		myFunctionTestCaseVisitHandler.leave(node);

		return this;
	}

	/**
	 * Finish the visitor by writing the static parts:
	 * - module parameters
	 * - templates
	 * - constants
	 */
	// TODO : move Constants and Templates here
	void finish() {
		currentFileName = parameters.getClassName();
		visualizeNodeToJava(importListStrings);
		visualizeNodeToJava(parameters.getJavaSource());
		currentFileName = templates.getClassName();
		visualizeNodeToJava(importListStrings);
		visualizeNodeToJava(templates.getJavaSource());
		currentFileName = constants.getClassName();
		visualizeNodeToJava(importListStrings);
		visualizeNodeToJava(constants.getJavaSource());
	}
}
