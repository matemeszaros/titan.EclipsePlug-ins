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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.UnaryMinusExpression;

public final class myASTVisitor extends ASTVisitor {

	public static TestCase_Function_Visit_Handler myFunctionTestCaseVisitHandler = new TestCase_Function_Visit_Handler();
	public static Def_Type_Visit_Handler myDefTypeVisitHandler = new Def_Type_Visit_Handler();
	public static Def_Const_Visit_Handler myDefConstVisitHandler = new Def_Const_Visit_Handler();
	public static Def_Template_Visit_Handler myDefTemplateVisitHandler = new Def_Template_Visit_Handler();

	public static String currentFileName = "";

	private static String moduleParNodeType = null;

	public static boolean blockIdListing = false;
	public static boolean isNextIntegerNegative = false;


	private static boolean waitForModuleParValues = false;

	private static List<String> moduleParValues = new ArrayList<String>();

	public static Map<String, String[]> nodeNameChildrenNamesHashMap = new LinkedHashMap<String, String[]>();
	public static Map<String, String[]> nodeNameChildrenTypesHashMap = new LinkedHashMap<String, String[]>();
	public static Map<String, String> nodeNameSetOfTypesHashMap = new LinkedHashMap<String, String>();
	public static Map<String, String> nodeNameRecordOfTypesHashMap = new LinkedHashMap<>();
	public static Map<String, String> nodeNameNodeTypeHashMap = new LinkedHashMap<String, String>();
	public static Map<String, String> portNamePortTypeHashMap = new LinkedHashMap<String, String>();
	public static Map<String, String> templateIdValuePairs = new LinkedHashMap<String, String>();

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

		if (string.equals("CHARSTRING")) {
			string = "SubTypeDef<CHARSTRING>";
		}

		if (string.equals("charstring")) {
			string = "CHARSTRING";
		}

		if (string.equals("integer")) {
			string = "INTEGER";
		}

		return string;
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

	public int visit(IVisitableNode node) {
		// visit* submethods only serve increased readability and
		// categorization

		myDefTypeVisitHandler.visit(node);

		myDefConstVisitHandler.visit(node);

		myDefTemplateVisitHandler.visit(node);

		if (node instanceof Def_ModulePar) {
			currentFileName = "Constants";

			Def_ModulePar_Writer.getInstance(((Def_ModulePar) node));
			nodeNameNodeTypeHashMap.put(((Def_ModulePar) node).getIdentifier()
					.toString(), "modulePar");
			waitForModuleParValues = true;

		}

		if (node instanceof UnaryMinusExpression) {
			myASTVisitor.isNextIntegerNegative = true;
		}

		// module parameters
		visitModuleParNodes(node);



		// Check for Function & TC nodes
		myFunctionTestCaseVisitHandler.visit(node);

		return V_CONTINUE;
	}

	public void visitModuleParNodes(IVisitableNode node) {
		if (waitForModuleParValues && (node instanceof Integer_Type)) {
			moduleParNodeType = "INTEGER";
		}

		if (waitForModuleParValues && (node instanceof Integer_Value)) {
			moduleParValues.add(((Integer_Value) node).toString());
		}
	}

	public int leave(IVisitableNode node) {

		myDefTypeVisitHandler.leave(node);
		myDefConstVisitHandler.leave(node);
		myDefTemplateVisitHandler.leave(node);
		
		if (node instanceof UnaryMinusExpression) {
			myASTVisitor.isNextIntegerNegative = false;
		}



		if (node instanceof Def_ModulePar) {
			handleModulePar(node);
		}

		myFunctionTestCaseVisitHandler.leave(node);

		return V_CONTINUE;
	}

	public void handleModulePar(IVisitableNode node) {
		Def_ModulePar_Writer moduleParNode = Def_ModulePar_Writer
				.getInstance(((Def_ModulePar) node));
		moduleParNode.clearLists();
		moduleParNode.setModuleParNodeType(moduleParNodeType);

		waitForModuleParValues = false;

		moduleParNode.moduleParValues.addAll(moduleParValues);

		moduleParValues.clear();
		currentFileName = "Constants";
		deleteLastBracket(currentFileName);
		visualizeNodeToJava(moduleParNode.getJavaSource() + "\r\n}");

	}

}
