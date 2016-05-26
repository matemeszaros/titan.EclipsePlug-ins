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
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TestcaseFormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Assignment_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Connect_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Definition_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Done_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Return_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Send_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Setverdict_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Start_Statement;
import org.eclipse.titan.designer.AST.TTCN3.templates.AnyOrOmit_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Any_Value_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.EnumerationItems;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SetOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Enumerated_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.Range_ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.Single_ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Omit_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AddExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ComponentCreateExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Int2StrExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

public class myASTVisitor extends ASTVisitor {

	public static String currentFileName = "";

	private List<String> compFieldTypes = new ArrayList<String>();
	private List<String> compFieldNames = new ArrayList<String>();

	private List<String> enumItems = new ArrayList<String>();

	private String charstringValue = null;

	private int minValue;
	private int maxValue;

	private boolean waitForSetOfReference = false;
	private String setOfReferenceName = null;

	private List<String> inMessageName = new ArrayList<String>();
	private List<String> outMessageName = new ArrayList<String>();
	private List<String> inOutMessageName = new ArrayList<String>();
	private boolean isPortTypeAReferencedType = false;
	private boolean waitingForPortAttriburtes = false;

	private boolean waitForCompReference = false;
	private List<String> compPortTypes = new ArrayList<String>();
	private List<String> compPortNames = new ArrayList<String>();

	private String parentName = null;
	public static Map<String, String[]> nodeNameChildrenNamesHashMap = new LinkedHashMap<String, String[]>();
	public static Map<String, String[]> nodeNameChildrenTypesHashMap = new LinkedHashMap<String, String[]>();
	public static Map<String, String> nodeNameSetOfTypesHashMap = new LinkedHashMap<String, String>();
	public static Map<String, String> nodeNameNodeTypeHashMap = new LinkedHashMap<String, String>();

	private boolean waitForConstValues = false;
	private String constNodeType = null;
	private List<String> constValues = new ArrayList<String>();
	public static Set<String> constOmitHashes = new HashSet<String>();
	// counts the members for each setof
	// has to be -3 because the node's name and type increases it by 2
	// should be increased to 0 only at the first constant value
	private int constSetOfCounter = -3;
	private List<Integer> constSetOfAmount = new ArrayList<Integer>();
	private String lastConstName = null;

	private List<String> templateValues = new ArrayList<String>();
	private List<String> templateAllIdentifiers = new ArrayList<String>();
	private boolean waitForTemplateValues = false;
	private boolean waitForModifierValue = false;
	private String modifierValue=null;
	private String lastTemplateName = null;
	public static Map<String, String> templateIdValuePairs = new LinkedHashMap<String, String>();
	public static Set<String> templateIDs = new HashSet<String>();

	public static List<String> componentList = new ArrayList<String>();
	public static List<String> testCaseList = new ArrayList<String>();
	public static List<String> testCaseRunsOnList = new ArrayList<String>();
	public static List<String> functionList = new ArrayList<String>();
	public static List<String> functionRunsOnList = new ArrayList<String>();

	private boolean blockIdListing = false;

	private boolean waitForStatementBlock = false;
	private StatementBlock statementBlock = null;
	private boolean waitForRunsOnValue=false;
	private boolean waitForReturnType=false;
	private String runsOnValue=null;
	private String returnType=null;
	
	private boolean waitForReturnStatementValue=false;
	private String returnStatementValue=null;
	
	private boolean waitForTimerValue=false;
	private String timerValue=null;
	
	private boolean waitForSendStatement=false;
	private List<String> sendPortReference = new ArrayList<String>();
	private List<String> sendParameter = new ArrayList<String>();
	private List<String> sendParameterType = new ArrayList<String>();
	
	private boolean waitForRecieveStatement=false;
	private List<String> recievePortReference = new ArrayList<String>();
	private List<String> recieveParameter = new ArrayList<String>();
	private List<String> recieveParameterType = new ArrayList<String>();
	private List<String> receiveTypedParam = new ArrayList<String>();
	private boolean checkAnyport=false;
	private boolean waitForReceivePortStatement=false;
	private boolean waitForUnknownStartStatement=false;
	private String unknownStartReference=null;
	private boolean waitForTimeoutStatement=false;
	private boolean waitForTypedParam=false;
	private boolean waitForInt2StrExpression=false;
	
	private boolean waitForAddExpression=false;
	private List<String> addValues = new ArrayList<String>();
	
	private boolean waitForTcRunsOn=false;
	private String testCaseRunsOn=null;
	private boolean waitForTcStatementBlock=false;
	private StatementBlock tcStatementBlock=null;
	private List<String> testCaseVars = new ArrayList<String>();
	private List<String> testCaseVarTypes = new ArrayList<String>();

	private CompilationTimeStamp compilationCounter = CompilationTimeStamp
			.getNewCompilationCounter();

	private boolean waitForTcDefStatement=false;

	private boolean waitForTC=false;

	private boolean waitForTcAssignmentValues=false;
	private List<String> testCaseAssignValues = new ArrayList<String>();
	private List<String> testCaseCreateValues = new ArrayList<String>();
	private List<String> testCaseCreateCounter = new ArrayList<String>();
	private List<String> testCaseCreateRefValues = new ArrayList<String>();
	private List<String> testCaseCreateCharValues = new ArrayList<String>();
	
	private int tcCreateCounter=-1;
	private boolean waitForTcCreateValues=false;

	private boolean waitForTcConnectValues=false;;
	private List<String> testCaseConnectValues = new ArrayList<String>();
	private List<String> testCaseConnectCounter = new ArrayList<String>();
	private int tcConnectCounter=-1;

	private boolean waitForTcStartValues=false;;
	private List<String> testCaseStartValues = new ArrayList<String>();
	private List<String> testCaseStartCounter = new ArrayList<String>();
	private int tcStartCounter=-1;

	

	public static String importListStrings = "package org.eclipse.titan.javagen;"
			+ "\r\n"
			+ "import org.eclipse.titan.ttcn3java.TTCN3JavaAPI.*;"
			+ "\r\n"
			+ "import java.util.HashSet;"
			+ "\r\n"
			+ "import java.util.concurrent.TimeUnit;"
			+ "\r\n"
			+ "import java.io.ObjectInputStream;"
			+ "\r\n"
			+ "import java.io.ObjectOutputStream;"
			+ "\r\n"
			// + "com.ericsson.titan.ttcn3java.SIP_Definitions.*"
			+ "\r\n"
			+ "import java.util.ArrayList;"
			+ "\r\n"
			+ "import java.io.BufferedReader;"
			+ "\r\n"
			+ "import java.io.BufferedWriter;"
			+ "\r\n"
			+ "import java.io.InputStreamReader;"
			+ "\r\n"
			+ "import java.io.OutputStreamWriter;"
			+ "\r\n"
			+ "import java.net.Socket;"
			+ "\r\n"
			+ "import java.util.Vector;"
			+ "\r\n" + "import java.net.ServerSocket;" + "\r\n" + "\r\n";
/*
	static {
		currentFileName = "Constants";
		visualizeNodeToJava(importListStrings); // erre
		visualizeNodeToJava("class Constants{\r\n}\r\n");

		currentFileName = "Templates";

		visualizeNodeToJava(importListStrings); // erre
		visualizeNodeToJava("class Templates{\r\n}\r\n");

		currentFileName = "TTCN_functions";

		visualizeNodeToJava(importListStrings); // erre
		visualizeNodeToJava("class TTCN_functions{\r\n}\r\n");

	}*/

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// logger.severe(string);
	}

	public String changeTypeToJava(String string) {

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

	public String cutModuleNameFromBeginning(String string) {
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

	public void deleteLastBracket(String currentFileName) {
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

		if (node instanceof Def_Type) {
			Def_Type typeNode = (Def_Type) node;

			CompilationTimeStamp compilationCounter = CompilationTimeStamp
					.getNewCompilationCounter();

			currentFileName = typeNode.getIdentifier().toString();

			visualizeNodeToJava(importListStrings); // erre

			if (typeNode.getType(compilationCounter).getTypetype().toString()
					.equals("TYPE_TTCN3_SEQUENCE")) {// record

				Def_Type_Record recordNode = Def_Type_Record
						.getInstance(typeNode);

				nodeNameNodeTypeHashMap.put(
						typeNode.getIdentifier().toString(), "record");
				parentName = typeNode.getIdentifier().toString();
				parentName.toString();

			} else if (typeNode.getType(compilationCounter).getTypetype()
					.toString().equals("TYPE_TTCN3_SET")) {

				Def_Type_Set setdNode = Def_Type_Set.getInstance(typeNode);

				nodeNameNodeTypeHashMap.put(
						typeNode.getIdentifier().toString(), "set");
				parentName = typeNode.getIdentifier().toString();

			} else if (typeNode.getType(compilationCounter).getTypetype()
					.toString().equals("TYPE_TTCN3_CHOICE")) {

				Def_Type_Union uniondNode = Def_Type_Union
						.getInstance(typeNode);

				nodeNameNodeTypeHashMap.put(
						typeNode.getIdentifier().toString(), "union");
				parentName = typeNode.getIdentifier().toString();

			} else if (typeNode.getType(compilationCounter) instanceof Integer_Type) {

				Def_Type_Integer integerNode = Def_Type_Integer
						.getInstance(typeNode);

				nodeNameNodeTypeHashMap.put(
						typeNode.getIdentifier().toString(), "INTEGER");
				parentName = typeNode.getIdentifier().toString();

			} else if (typeNode.getType(compilationCounter) instanceof CharString_Type) {

				Def_Type_Charstring charstringNode = Def_Type_Charstring
						.getInstance(typeNode);

				nodeNameNodeTypeHashMap.put(
						typeNode.getIdentifier().toString(), "CHARSTRING");
				parentName = typeNode.getIdentifier().toString();

			} else if (typeNode.getType(compilationCounter) instanceof TTCN3_Enumerated_Type) {

				Def_Type_Enum enumTypeNode = Def_Type_Enum
						.getInstance(typeNode);

				nodeNameNodeTypeHashMap.put(
						typeNode.getIdentifier().toString(), "enum");
				parentName = typeNode.getIdentifier().toString();

			} else if (typeNode.getType(compilationCounter).getTypetype()
					.toString().equals("TYPE_SET_OF")) {

				Def_Type_Set_Of setOfNode = Def_Type_Set_Of
						.getInstance(typeNode);
				waitForSetOfReference = true;

				nodeNameNodeTypeHashMap.put(
						typeNode.getIdentifier().toString(), "setof");

				nodeNameSetOfTypesHashMap.put(typeNode.getIdentifier()
						.toString(), ((Referenced_Type) ((SetOf_Type) typeNode
						.getType(compilationCounter)).getOfType())
						.getReference().toString());
				parentName = typeNode.getIdentifier().toString();

			} else if (typeNode.getType(compilationCounter).getTypetype()
					.toString().equals("TYPE_PORT")) {

				Def_Type_Port portNode = Def_Type_Port.getInstance(typeNode);

				waitingForPortAttriburtes = true;

				nodeNameNodeTypeHashMap.put(
						typeNode.getIdentifier().toString(), "port");
				parentName = typeNode.getIdentifier().toString();

			} else if (typeNode.getType(compilationCounter).getTypetype()
					.toString().equals("TYPE_COMPONENT")) {

				Def_Type_Component compNode = Def_Type_Component
						.getInstance(typeNode);
				waitForCompReference = true;

				componentList.add(typeNode.getIdentifier().toString());
				nodeNameNodeTypeHashMap.put(
						typeNode.getIdentifier().toString(), "component");
				parentName = typeNode.getIdentifier().toString();
			}

		}

		if (node instanceof Def_Const) {
			currentFileName = "Constants";

			Def_Const_Writer constNode = Def_Const_Writer
					.getInstance(((Def_Const) node));
			nodeNameNodeTypeHashMap.put(((Def_Const) node).getIdentifier()
					.toString(), "constant");
			waitForConstValues = true;
			constSetOfAmount.add(0);
		}

		if (node instanceof Def_Template) {
			currentFileName = "Templates";

			Def_Template_Writer tempNode = Def_Template_Writer
					.getInstance(((Def_Template) node));

			nodeNameNodeTypeHashMap.put(((Def_Template) node).getIdentifier()
					.toString(), "template");

			waitForModifierValue=true;
			waitForTemplateValues = true;
			
		}

		if (node instanceof Def_Function) {
			
			Def_Function_Writer functionNode = Def_Function_Writer
					.getInstance(((Def_Function) node));

			nodeNameNodeTypeHashMap.put(((Def_Function) node).getIdentifier()
					.toString(), "function");

			runsOnValue=null;

			
			waitForReturnType=true;
			waitForRunsOnValue=true;
			waitForStatementBlock = true;

		}

		if (node instanceof Def_Testcase) {

			Def_Testcase_Writer testcaseNode = Def_Testcase_Writer
					.getInstance(((Def_Testcase) node));

			nodeNameNodeTypeHashMap.put(((Def_Testcase) node).getIdentifier()
					.toString(), "testcase");

			testCaseList.add(((Def_Testcase) node).getIdentifier().toString());
			
			waitForStatementBlock = true;
			waitForTC=true;

		}

		// ----------children nodes
		if (waitForSetOfReference && (node instanceof Reference)) {

			setOfReferenceName = ((Reference) node).getId().toString();
			waitForSetOfReference = false;
		}

		if (node instanceof CompField) { // component

			CompField compFieldNode = (CompField) node;

			if (compFieldNode.getType() instanceof Referenced_Type) {

				compFieldTypes.add(((Referenced_Type) compFieldNode.getType())
						.getReference().getId().toString());
			} else {
				compFieldTypes.add(cutModuleNameFromBeginning(compFieldNode
						.getType().getTypename()));
			}
			compFieldNames.add(compFieldNode.getIdentifier().toString());

		}

		if (node instanceof Charstring_Value) {// charstring

			Charstring_Value singleValuedNode = (Charstring_Value) node;

			charstringValue = singleValuedNode.getValue();

		}

		if (node instanceof Range_ParsedSubType) {// integer

			Range_ParsedSubType rangeValueNode = (Range_ParsedSubType) node;

			minValue = Integer.parseInt(rangeValueNode.getMin().toString());
			maxValue = Integer.parseInt(rangeValueNode.getMax().toString());
		}

		if (node instanceof EnumerationItems) {// enum

			for (int i = 0; i < ((EnumerationItems) node).getItems().size(); i++) {
				enumItems.add(((EnumerationItems) node).getItems().get(i)
						.getId().toString());

			}
		}

		if (waitingForPortAttriburtes && (node instanceof Referenced_Type)) {
			isPortTypeAReferencedType = true;

		}
		
		if (waitingForPortAttriburtes && (node instanceof PortTypeBody)) {
			PortTypeBody body = (PortTypeBody) node;
			int inCount = body.getInMessages().getNofTypes();
			int outCount = body.getOutMessage().getNofTypes();

			for (int i = 0; i < inCount; i++) {
				inMessageName.add(cutModuleNameFromBeginning(body
						.getInMessages().getTypeByIndex(i).getTypename()));
			}
			for (int i = 0; i < outCount; i++) {
				outMessageName.add(cutModuleNameFromBeginning(body
						.getOutMessage().getTypeByIndex(i).getTypename()));
			}

			int shorterListSize = inMessageName.size() <= outMessageName.size() ? inMessageName
					.size() : outMessageName.size();

			// check if one of the messages is inout
			// if inout delete from both lists and add to inout
			for (int i = 0; i < shorterListSize; i++) {
				if (inMessageName.size() == shorterListSize) {
					for (int j = 0; j < outMessageName.size(); j++) {
						if (inMessageName.get(i).equals(outMessageName.get(j))) {
							inOutMessageName.add(inMessageName.get(i));
							inMessageName.remove(i);
							outMessageName.remove(j);
						}
					}
				} else {
					for (int j = 0; j < outMessageName.size(); j++) {
						if (outMessageName.get(i).equals(inMessageName.get(j))) {
							inOutMessageName.add(outMessageName.get(i));
							inMessageName.remove(i);
							outMessageName.remove(j);
						}
					}
				}
			}
		}

		// component ports
		if (node instanceof Def_Port) {
			Def_Port port = (Def_Port) node;
			compPortNames.add(port.getIdentifier().toString());

		}
		if (waitForCompReference && (node instanceof Reference)) {

			compPortTypes.add(((Reference) node).getId().toString());

		}

		// constants

		if (waitForConstValues && (node instanceof Identifier)) {
			constSetOfCounter++;
			if (constSetOfCounter > 0) {
				constSetOfAmount.add(0);
			}
			lastConstName = ((Identifier) node).toString();
		}

		if (waitForConstValues && (node instanceof Charstring_Value)) {
			constValues.add(((Charstring_Value) node).getValue());

			if (constSetOfCounter == -1) {
				constSetOfCounter = 0;
			}

			constSetOfAmount.set(constSetOfCounter,
					constSetOfAmount.get(constSetOfCounter) + 1);
		}

		if (waitForConstValues && (node instanceof Integer_Value)) {
			constValues.add(Long.toString(((Integer_Value) node).getValue()));

			if (constSetOfCounter == -1) {
				constSetOfCounter = 0;
			}

			constSetOfAmount.set(constSetOfCounter,
					constSetOfAmount.get(constSetOfCounter) + 1);

		}

		if (waitForConstValues && (node instanceof Omit_Value)) {
			constValues.add("omit");

			if (constSetOfCounter == -1) {
				constSetOfCounter = 0;
			}

			constSetOfAmount.set(constSetOfCounter,
					constSetOfAmount.get(constSetOfCounter) + 1);

			constOmitHashes.add(lastConstName);

		}

		if (waitForConstValues && (node instanceof Reference)) {
			constNodeType = ((Reference) node).getId().toString();
		}

		// templates
		if (waitForTemplateValues && (node instanceof Reference)) {
			constNodeType = ((Reference) node).getId().toString();
		}

		if (waitForTemplateValues
				&& (node instanceof Undefined_LowerIdentifier_Value)) {

			templateIdValuePairs.put(lastTemplateName,
					((Undefined_LowerIdentifier_Value) node).getIdentifier()
							.toString());
			blockIdListing = true;
		}

		if (waitForTemplateValues && (node instanceof Charstring_Value)) {

			templateIdValuePairs.put(lastTemplateName,
					((Charstring_Value) node).getValue());
			blockIdListing = true;
		}

		if (waitForTemplateValues && (node instanceof Omit_Value)) {

			templateIdValuePairs.put(lastTemplateName, "omit");
			blockIdListing = true;
		}

		if (waitForTemplateValues && (node instanceof Any_Value_Template)) {

			templateIdValuePairs.put(lastTemplateName, "?");
			blockIdListing = true;
		}

		if (waitForTemplateValues && (node instanceof AnyOrOmit_Template)) {

			templateIdValuePairs.put(lastTemplateName, "*");
			blockIdListing = true;
		}

		if (waitForTemplateValues && (node instanceof Reference)) {

			templateIdValuePairs.put(lastTemplateName, ((Reference) node)
					.toString().toString());
			blockIdListing = true;
		}

		if (waitForTemplateValues && (node instanceof Identifier)
				&& !blockIdListing) {
			templateAllIdentifiers.add(((Identifier) node).toString());
			lastTemplateName = ((Identifier) node).toString();
			templateIDs.add(lastTemplateName);

		}
		if (waitForModifierValue && (node instanceof Named_Template_List)) {
			waitForModifierValue=false;;

		}
		if (waitForModifierValue && (node instanceof Reference)) {
			modifierValue = ((Reference)node).getId().toString();

		}
//functions
		if (waitForStatementBlock && (node instanceof StatementBlock)) {
			statementBlock = (StatementBlock) node;
			waitForStatementBlock = false;
		}
		
		if (waitForReturnType && (node instanceof StatementBlock)) {
			waitForReturnType=false;;

		}
		if (waitForReturnType && (node instanceof Integer_Type)) {
			returnType="Integer_Type";
		}
		
		if (waitForRunsOnValue && (node instanceof StatementBlock)) {
			waitForRunsOnValue=false;;

		}
		if (waitForRunsOnValue && (node instanceof Reference)) {
			runsOnValue = ((Reference)node).getId().toString();
			functionRunsOnList.add(((Reference)node).getId().toString());

		}
		
		if(node instanceof Return_Statement){
			waitForReturnStatementValue=true;
		}
		
		if (waitForReturnStatementValue && (node instanceof Integer_Value)) {
			returnStatementValue = String.valueOf(((Integer_Value)node).getValue());

		}
		
		if(node instanceof Def_Timer){
			waitForTimerValue=true;
		}
		if (waitForTimerValue && (node instanceof Real_Value)) {
			timerValue=((Real_Value)node).createStringRepresentation();
		}
		
		if(node instanceof Send_Statement){
			waitForSendStatement=true;
		}
		
		if (waitForSendStatement && (node instanceof Reference)) {
			sendPortReference.add(((Reference)node).getId().toString());
			

		}
				
		if (waitForSendStatement && (node instanceof SpecificValue_Template)) {
			if(((SpecificValue_Template)node).getSpecificValue() instanceof Undefined_LowerIdentifier_Value){
				
				Undefined_LowerIdentifier_Value tempValue=(Undefined_LowerIdentifier_Value) ((SpecificValue_Template)node).getSpecificValue();
				sendParameter.add(tempValue.getIdentifier().toString());
				sendParameterType.add("Undefined_LowerIdentifier_Value");
			}
			
			if(((SpecificValue_Template)node).getSpecificValue() instanceof Charstring_Value){
				Charstring_Value tempValue=(Charstring_Value) ((SpecificValue_Template)node).getSpecificValue();
				sendParameter.add(tempValue.getValue());
				sendParameterType.add("Charstring_Value");
			}
		}
		
		if(node instanceof Unknown_Start_Statement){
			waitForUnknownStartStatement=true;
		}
		if (waitForUnknownStartStatement && (node instanceof Reference)) {
			unknownStartReference=((Reference)node).getId().toString();
			

		}
		

		if(node instanceof Receive_Port_Statement){
			waitForRecieveStatement=true;
			checkAnyport=true;
		}
		
		if(node instanceof TemplateInstance){
			
			
			if(((TemplateInstance)node).getType()!=null){
				waitForTypedParam=true;
				checkAnyport=true;
			}
		}
		if (waitForRecieveStatement && !waitForTypedParam&&(node instanceof Reference)) {
			recievePortReference.add(((Reference)node).getId().toString());
			checkAnyport=false;
			

		}
		if(waitForTypedParam&&(node instanceof Reference)) {
			recieveParameter.add(((Reference)node).getId().toString());
			recieveParameterType.add("_TYPED_PARAM_");
			waitForTypedParam=false;
		}
		
		
		if(checkAnyport &&(node instanceof Setverdict_Statement)){
			recievePortReference.add("_ANY_");
			recieveParameter.add("_ANY_");
			recieveParameterType.add("_ANY_");
			checkAnyport=false;
		}
		
		
		
		
		if (waitForRecieveStatement && (node instanceof Any_Value_Template)) {
			recieveParameter.add("Any_Value_Template");
			recieveParameterType.add("Any_Value_Template");
			checkAnyport=false;	
		}
		
		
		if (waitForRecieveStatement && (node instanceof SpecificValue_Template)) {
			checkAnyport=false;
			if(((SpecificValue_Template)node).getSpecificValue() instanceof Undefined_LowerIdentifier_Value){
				
				Undefined_LowerIdentifier_Value tempValue=(Undefined_LowerIdentifier_Value) ((SpecificValue_Template)node).getSpecificValue();
				recieveParameter.add(tempValue.getIdentifier().toString());
				recieveParameterType.add("Undefined_LowerIdentifier_Value");
			}
			
			if(((SpecificValue_Template)node).getSpecificValue() instanceof Charstring_Value){
				Charstring_Value tempValue=(Charstring_Value) ((SpecificValue_Template)node).getSpecificValue();
				recieveParameter.add(tempValue.getValue());
				recieveParameterType.add("Charstring_Value");
			}
		}
		
		if(node instanceof AddExpression){
			waitForAddExpression=true;
		}
		
		if (waitForAddExpression && (node instanceof Undefined_LowerIdentifier_Value)) {
			addValues.add(((Undefined_LowerIdentifier_Value)node).getIdentifier().toString());

		}
		if (waitForAddExpression && (node instanceof Integer_Value)) {
			addValues.add(String.valueOf(((Integer_Value)node).getValue()));
			

		}
		
		if(node instanceof Int2StrExpression){
			waitForInt2StrExpression=true;
		}
		if (waitForInt2StrExpression && (node instanceof Undefined_LowerIdentifier_Value)) {
			sendParameter.add(((Undefined_LowerIdentifier_Value)node).getIdentifier().toString());
			sendParameterType.add("Int2StrExpression");
			waitForInt2StrExpression=false;
			

		}
		
		if(node instanceof TestcaseFormalParameterList){
			waitForTcRunsOn=true;
		}
		if(waitForTcRunsOn&&(node instanceof Reference)) {
			testCaseRunsOn=((Reference)node).getId().toString();
			waitForTcRunsOn=false;
			waitForTcStatementBlock=true;
		}
		if(waitForTcStatementBlock&&(node instanceof StatementBlock)) {
			tcStatementBlock=((StatementBlock)node);
			waitForTcStatementBlock=false;
		}
		
		if(waitForTC&&(node instanceof Definition_Statement)){
			waitForTcDefStatement=true;
		}
		if(waitForTcDefStatement&&(node instanceof Def_Var)) {
			testCaseVars.add(((Def_Var)node).getIdentifier().toString());
		}
		if(waitForTcDefStatement&&(node instanceof Reference)) {
			testCaseVarTypes.add(((Reference)node).getId().toString());
			waitForTcDefStatement=false;
		}
		
		if(waitForTC&&(node instanceof Assignment_Statement)){
			waitForTcAssignmentValues=true;
		}
		
		if(waitForTcAssignmentValues&&(node instanceof Reference)) {
			testCaseAssignValues.add(((Reference)node).getId().toString());
			waitForTcAssignmentValues=false;
		}

		if(waitForTC&&(node instanceof ComponentCreateExpression)){
			waitForTcCreateValues=true;
			tcCreateCounter++;
		}
		
		if(waitForTcCreateValues&&(node instanceof Reference)) {
			testCaseCreateRefValues.add(((Reference)node).getId().toString());
			testCaseCreateCounter.add(String.valueOf(tcCreateCounter));

		}
		if(waitForTcCreateValues&&(node instanceof Charstring_Value)) {
			testCaseCreateCharValues.add(((Charstring_Value)node).getValue());
			testCaseCreateCounter.add(String.valueOf(tcCreateCounter));
		}
		
		if(waitForTC&&(node instanceof Connect_Statement)){
			waitForTcConnectValues=true;
			tcConnectCounter++;
			
		}
		
		if(waitForTcConnectValues&&(node instanceof Identifier)){
			testCaseConnectValues.add(((Identifier)node).toString());
			testCaseConnectCounter.add(String.valueOf(tcConnectCounter));
		}

		if(waitForTC&&(node instanceof Unknown_Start_Statement)){
			waitForTcStartValues=true;
			tcStartCounter++;
			
		}
		
		if(waitForTcStartValues&&(node instanceof Identifier)){
			testCaseStartValues.add(((Identifier)node).toString());
			testCaseStartCounter.add(String.valueOf(tcStartCounter));
		}
		
		
		return V_CONTINUE;
	}
//TODO
	public int leave(IVisitableNode node) {

		/*
		 * For every type the compFieldTypes and compFieldNames list must be
		 * emptied of evaluated
		 */

		if (node instanceof Def_Type) {

			Def_Type typeNode = (Def_Type) node;

			CompilationTimeStamp compilationCounter = CompilationTimeStamp
					.getNewCompilationCounter();

			currentFileName = typeNode.getIdentifier().toString();

			if (typeNode.getType(compilationCounter).getTypetype().toString()
					.equals("TYPE_TTCN3_SEQUENCE")) {// record

				Def_Type_Record recordNode = Def_Type_Record
						.getInstance(typeNode);

				// add component fields
				for (int i = 0; i < compFieldTypes.size(); i++) {

					recordNode.addCompFields(compFieldTypes.get(i),
							compFieldNames.get(i));

				}
				String[] typeArray = (String[]) compFieldTypes
						.toArray(new String[compFieldTypes.size()]);
				String[] nameArray = (String[]) compFieldNames
						.toArray(new String[compFieldNames.size()]);

				nodeNameChildrenTypesHashMap.put(parentName, typeArray);
				nodeNameChildrenNamesHashMap.put(parentName, nameArray);

				compFieldTypes.clear();
				compFieldNames.clear();

				visualizeNodeToJava(recordNode.getJavaSource());

			} else if (typeNode.getType(compilationCounter).getTypetype()
					.toString().equals("TYPE_TTCN3_SET")) {// set

				Def_Type_Set setdNode = Def_Type_Set.getInstance(typeNode);

				// add component fields
				for (int i = 0; i < compFieldTypes.size(); i++) {

					setdNode.addCompFields(compFieldTypes.get(i),
							compFieldNames.get(i));

				}

				String[] typeArray = (String[]) compFieldTypes
						.toArray(new String[compFieldTypes.size()]);
				String[] nameArray = (String[]) compFieldNames
						.toArray(new String[compFieldNames.size()]);

				nodeNameChildrenTypesHashMap.put(parentName, typeArray);
				nodeNameChildrenNamesHashMap.put(parentName, nameArray);

				compFieldTypes.clear();
				compFieldNames.clear();

				visualizeNodeToJava(setdNode.getJavaSource());

			} else if (typeNode.getType(compilationCounter).getTypetype()
					.toString().equals("TYPE_TTCN3_CHOICE")) {// union

				Def_Type_Union uniondNode = Def_Type_Union
						.getInstance(typeNode);

				// add component fields
				for (int i = 0; i < compFieldTypes.size(); i++) {

					uniondNode.addCompFields(compFieldTypes.get(i),
							compFieldNames.get(i));

				}

				String[] typeArray = (String[]) compFieldTypes
						.toArray(new String[compFieldTypes.size()]);
				String[] nameArray = (String[]) compFieldNames
						.toArray(new String[compFieldNames.size()]);

				nodeNameChildrenTypesHashMap.put(parentName, typeArray);
				nodeNameChildrenNamesHashMap.put(parentName, nameArray);

				compFieldTypes.clear();
				compFieldNames.clear();

				visualizeNodeToJava(uniondNode.getJavaSource());

			} else if (typeNode.getType(compilationCounter) instanceof Integer_Type) {

				Def_Type_Integer integerNode = Def_Type_Integer
						.getInstance(typeNode);
				integerNode.addMinMaxFields(minValue, maxValue);
				minValue = 0;
				maxValue = 0;

				visualizeNodeToJava(integerNode.getJavaSource());

			} else if (typeNode.getType(compilationCounter) instanceof CharString_Type) {

				Def_Type_Charstring charstringNode = Def_Type_Charstring
						.getInstance(typeNode);
				charstringNode.addCharStringValue(charstringValue);
				charstringValue = null;

				visualizeNodeToJava(charstringNode.getJavaSource());

			} else if (typeNode.getType(compilationCounter) instanceof TTCN3_Enumerated_Type) {

				Def_Type_Enum enumTypeNode = Def_Type_Enum
						.getInstance(typeNode);
				for (int i = 0; i < enumItems.size(); i++) {
					enumTypeNode.addEnumItem(enumItems.get(i));
				}
				enumItems.clear();

				visualizeNodeToJava(enumTypeNode.getJavaSource());

			} else if (typeNode.getType(compilationCounter).getTypetype()
					.toString().equals("TYPE_SET_OF")) {

				Def_Type_Set_Of setOfNode = Def_Type_Set_Of
						.getInstance(typeNode);

				setOfNode.addField(setOfReferenceName);
				setOfReferenceName = null;
				visualizeNodeToJava(setOfNode.getJavaSource());

			} else if (typeNode.getType(compilationCounter).getTypetype()
					.toString().equals("TYPE_PORT")) {

				Def_Type_Port portNode = Def_Type_Port.getInstance(typeNode);

				for (int i = 0; i < inMessageName.size(); i++) {
					portNode.addInMessage(inMessageName.get(i));
				}
				for (int i = 0; i < outMessageName.size(); i++) {
					portNode.addOutMessage(outMessageName.get(i));
				}
				for (int i = 0; i < inOutMessageName.size(); i++) {
					portNode.addInOutMessage(inOutMessageName.get(i));
				}
				portNode.setPortTypeAReferencedType(isPortTypeAReferencedType);

				waitingForPortAttriburtes = false;
				isPortTypeAReferencedType = false;
				inMessageName.clear();
				outMessageName.clear();
				inOutMessageName.clear();
				visualizeNodeToJava(portNode.getJavaSource());

			} else if (typeNode.getType(compilationCounter).getTypetype()
					.toString().equals("TYPE_COMPONENT")) {

				Def_Type_Component compNode = Def_Type_Component
						.getInstance(typeNode);
				// add component fields
				for (int i = 0; i < compPortTypes.size(); i++) {

					compNode.addCompFields(compPortTypes.get(i),
							compPortNames.get(i));

				}

				compPortTypes.clear();
				compPortNames.clear();
				waitForCompReference = false;
				visualizeNodeToJava(compNode.getJavaSource());

			}
			parentName = null;
		}
		if (node instanceof Def_Const) {
			Def_Const_Writer constNode = Def_Const_Writer
					.getInstance(((Def_Const) node));

			constNode.setConstNodeType(constNodeType);

			for (int i = 0; i < constValues.size(); i++) {
				constNode.addConstValues(constValues.get(i));
			}

			for (int i = 0; i < constSetOfAmount.size(); i++) {
				constNode.setConstSetOfAmount(constSetOfAmount.get(i));
			}

			constValues.clear();
			waitForConstValues = false;
			constSetOfCounter = -3;
			constSetOfAmount.clear();

			deleteLastBracket(currentFileName);
			visualizeNodeToJava(constNode.getJavaSource() + "\r\n}");
			constOmitHashes.clear();

		}

		if (waitForTemplateValues
				&& (node instanceof Undefined_LowerIdentifier_Value)) {
			blockIdListing = false;
		}

		if (waitForTemplateValues && (node instanceof Charstring_Value)) {
			blockIdListing = false;
		}

		if (waitForTemplateValues && (node instanceof Omit_Value)) {
			blockIdListing = false;
		}

		if (waitForTemplateValues && (node instanceof Any_Value_Template)) {
			blockIdListing = false;
		}

		if (waitForTemplateValues && (node instanceof AnyOrOmit_Template)) {
			blockIdListing = false;
		}

		if (waitForTemplateValues && (node instanceof Reference)) {
			blockIdListing = false;
		}
		
		if(node instanceof Send_Statement){
			waitForSendStatement=false;
		}
		
		if(node instanceof Receive_Port_Statement){
			waitForRecieveStatement=false;
		}
		
		if(node instanceof Unknown_Start_Statement){
			waitForUnknownStartStatement=false;
		}
		if(node instanceof Receive_Port_Statement){
			waitForAddExpression=false;
		}
		
		if(waitForTC&&(node instanceof Connect_Statement)){
			waitForTcConnectValues=false;
			
			
		}
		if(waitForTC&&(node instanceof Unknown_Start_Statement)){
			waitForTcStartValues=false;
			
			
		}
		if(waitForTC&&(node instanceof ComponentCreateExpression)){
			//switch first two values
			testCaseCreateValues.add(testCaseCreateCharValues.get(0));
			testCaseCreateValues.add(testCaseCreateRefValues.get(0));
			
			for(int i=1;i<testCaseCreateCharValues.size();i++){
				testCaseCreateValues.add(testCaseCreateCharValues.get(i));
			}
			
			testCaseCreateCharValues.clear();
			testCaseCreateRefValues.clear();
			waitForTcCreateValues=false;
		}
		

		if (node instanceof Def_Template) {
			Def_Template_Writer tempNode = Def_Template_Writer
					.getInstance(((Def_Template) node));
			tempNode.toString();

			for (int i = 0; i < templateAllIdentifiers.size(); i++) {
				tempNode.addTemplateIdentifiers(templateAllIdentifiers.get(i));
			}

			tempNode.setModifierValue(modifierValue);
			
			templateAllIdentifiers.clear();
			templateValues.clear();
			waitForTemplateValues = false;
			waitForModifierValue=false;
			

			deleteLastBracket(currentFileName);
			visualizeNodeToJava(tempNode.getJavaSource() + "\r\n}");
			templateIDs.clear();
			templateIdValuePairs.clear();
		}

		if (node instanceof Def_Function) {

			Def_Function_Writer functionNode = Def_Function_Writer
					.getInstance(((Def_Function) node));

			functionNode.setStatementBlock(statementBlock);
			if(runsOnValue!=null){
				functionNode.setRunsOnValue(runsOnValue);
			}
			if(returnType!=null){
				functionNode.setReturnType(returnType);
			}
			
			if(timerValue!=null){
				functionNode.setTimerValue(timerValue);
			}
			if(returnStatementValue!=null){
				functionNode.setReturnStatementValue(returnStatementValue);
			}
			
			currentFileName = ((Def_Function) node).getIdentifier().toString();
			
			for(int i=0; i<sendPortReference.size();i++){
				functionNode.addPortReference(sendPortReference.get(i));
			}
			
			for(int i=0; i<sendParameter.size();i++){
				functionNode.addParameter(sendParameter.get(i));
			}
			
			for(int i=0; i<sendParameterType.size();i++){
				functionNode.addParameterType(sendParameterType.get(i));
			}
			if(unknownStartReference!=null){
				functionNode.setUnknownStartReference(unknownStartReference);
			}
			
			
			for(int i=0; i<recievePortReference.size();i++){
				functionNode.addRecievePortReference(recievePortReference.get(i));
			}
			
			for(int i=0; i<recieveParameter.size();i++){
				functionNode.addRecieveParameter(recieveParameter.get(i));
			}
			
			for(int i=0; i<recieveParameterType.size();i++){
				functionNode.addRecieveParameterType(recieveParameterType.get(i));
			}
			for(int i=0; i<addValues.size();i++){
				functionNode.addAddValues(addValues.get(i));
			}
			
			if(runsOnValue!=null){
				functionList.add(((Def_Function) node).getIdentifier()
						.toString());
			}
			
			sendParameter.clear();
			sendParameterType.clear();
			sendPortReference.clear();
			
			recieveParameter.clear();
			recieveParameterType.clear();
			recievePortReference.clear();
			
			
			if (runsOnValue!=null) {
				visualizeNodeToJava(importListStrings); // erre
				visualizeNodeToJava(functionNode.writeFunctionFile());
				currentFileName = runsOnValue;
				deleteLastBracket(currentFileName);
				visualizeNodeToJava(functionNode.getJavaSource() + "\r\n}");
			} else {
				currentFileName = "TTCN_functions";
				deleteLastBracket(currentFileName);
				visualizeNodeToJava(functionNode.getJavaSourceWithoutRunOn());

			}
			
			waitForReturnStatementValue=false;
			waitForSendStatement=false;
			waitForReceivePortStatement=false;
			waitForUnknownStartStatement=false;
			waitForTimeoutStatement=false;
			
			
			
			returnStatementValue=null;
			returnType=null;
			runsOnValue=null;
			timerValue=null;
			statementBlock = null;
			waitForStatementBlock = false;

		}

		if (node instanceof Def_Testcase) {

			Def_Testcase_Writer testNode = Def_Testcase_Writer
					.getInstance(((Def_Testcase) node));
			testNode.toString();

			currentFileName = ((Def_Testcase) node).getIdentifier().toString();

			testNode.setTcRunsOn(testCaseRunsOn);
			testNode.setStatementBlock(tcStatementBlock);
			testCaseRunsOnList.add(testCaseRunsOn);
			for(int i=0; i<testCaseVars.size();i++){
				testNode.addVars(testCaseVars.get(i));
			}
			for(int i=0; i<testCaseVarTypes.size();i++){
				testNode.addVarTypes(testCaseVarTypes.get(i));
			}
			for(int i=0; i<testCaseCreateValues.size();i++){
				testNode.addCreateValues(testCaseCreateValues.get(i));
			}
			for(int i=0; i<testCaseAssignValues.size();i++){
				testNode.addAssignValues(testCaseAssignValues.get(i));
			}
			for(int i=0; i<testCaseCreateCounter.size();i++){
				testNode.addCreateCounter(testCaseCreateCounter.get(i));
			}
			
			for(int i=0; i<testCaseConnectValues.size();i++){
				testNode.addConnectValues(testCaseConnectValues.get(i));
			}
			for(int i=0; i<testCaseConnectCounter.size();i++){
				testNode.addConnectCounter(testCaseConnectCounter.get(i));
			}
			
			for(int i=0; i<testCaseStartValues.size();i++){
				testNode.addStartValues(testCaseStartValues.get(i));
			}
			for(int i=0; i<testCaseStartCounter.size();i++){
				testNode.addStartCounter(testCaseStartCounter.get(i));
			}
			
			

			visualizeNodeToJava(importListStrings); // erre
			visualizeNodeToJava(testNode.writeTestcaseFile((Def_Testcase) node));

			currentFileName = testCaseRunsOn;
			deleteLastBracket(currentFileName);
			visualizeNodeToJava(testNode.getJavaSource() + "\r\n}");
			
			testCaseVars.clear();
			testCaseVarTypes.clear();
			testCaseAssignValues.clear();
			testCaseCreateValues.clear();
			testCaseCreateCounter.clear();
			testCaseCreateRefValues.clear();
			testCaseCreateCharValues.clear();
			testCaseConnectValues.clear();
			testCaseConnectCounter.clear();
			testCaseStartValues.clear();
			testCaseStartCounter.clear();
		}

		// System.out.print("-------"+node.toString()+"\r\n");
		return V_CONTINUE;
	}
}
