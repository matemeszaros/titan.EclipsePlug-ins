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
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Assignment_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Connect_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Definition_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Disconnect_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.DoWhile_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Clause;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Map_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Operation_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Return_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Send_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Setverdict_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.Timeout_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Start_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Stop_Statement;
import org.eclipse.titan.designer.AST.TTCN3.templates.Any_Value_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.BitString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Boolean_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Float_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.OctetString_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AddExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.And4bExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ComponentCreateExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.DivideExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.EqualsExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.GreaterThanExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.GreaterThanOrEqualExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Int2StrExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsBoundExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsChoosenExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsPresentExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsValueExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.LengthofExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.LessThanExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.LessThanOrEqualExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.MTCComponentExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ModuloExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.MultiplyExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Not4bExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.NotExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.NotequalesExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Or4bExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.RemainderExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.RotateLeftExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.RotateRightExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.SelfComponentExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ShiftLeftExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ShiftRightExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.StringConcatenationExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.SubstractExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.SystemComponentExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Xor4bExpression;

public class TestCase_Function_Visit_Handler {

	private static StatementBlock functionStatementBlock = null;
	private StatementBlock tcStatementBlock = null;

	private Def_Testcase_Writer currentTestCaseWriter;
	private Def_Function_Writer currentFunctionWriter;

	private static String returnType = null;
	private static String returnStatementValue = null;
	private List<String> unknownStartReference = new ArrayList<String>();
	private List<String> unknownStopReference = new ArrayList<String>();
	
	private static String runsOnValue = null;
	private static String currentIdentifier = null;

	// Counters
	private int tcCreateCounter = -1;
	private int tcStartCounter = -1;
	private int mapCounter = 0;
	private int mapValueCounter = 0;

	private int currentRecordCounter = 0;

	public boolean waitForDefStatement = false;
	public static boolean waitForStatementBlock = false;
	public static boolean waitForValue = false;
	private static boolean waitForRunsOnValue = false;
	private static boolean waitForReturnType = false;
	private static boolean waitForReturnStatementValue = false;
	private static boolean waitForReceiveStatement = false;
	private static boolean checkAnyport = false;
	private static boolean waitForUnknownStartStatement = false;
	private static boolean waitForTypedParam = false;
	private static boolean waitForInt2StrExpression = false;
	private boolean waitForTC = false;
	private boolean waitForAssignmentIdentifiers = false;
	private boolean waitForMapIdentifiers = false;;
	private boolean waitForTcIfCondition = false;

	private boolean isSendStatement = false;
	private boolean isDoWhileStatement = false;
	private boolean waitForTcStartParameter = false;
	private boolean waitForTcStopParameter = false;
	private boolean isAltGuards = false;

	private boolean waitForAltStatements = false;
	private boolean isReceiveValue = false;
	private boolean waitForAltTimeout = false;
	private boolean isNextValueTemplate = false;
	private boolean isNextValueConstant = false;
	private boolean waitForTcDisconnectValues = false;
	private boolean waitForTcCreateValues = false;
	private boolean waitForTcConnectValues = false;
	private boolean waitForTcStartValues = false;
	private boolean waitForFunction = false;
	private boolean waitForStatements = true;
	private boolean waitForReceiveParameter = false;
	private boolean isAnyValValue = false;
	private boolean waitForReceiveAnyValTemplateValue = false;
	private boolean isDefinition = false;
	private boolean isAssignment = false;
	private boolean isIf = false;
	public static boolean isThereAFormalParameter = false;
	private boolean waitForIndexSetValue = false;

	// Lists
	private List<String> nodeVars = new ArrayList<String>();
	private List<String> nodeVarTypes = new ArrayList<String>();
	private List<String> nodeVarValues = new ArrayList<String>();
	private List<Boolean> nodeVarIsAValueReference = new ArrayList<Boolean>();
	private List<Boolean> nodeVarTypeIsAReference = new ArrayList<Boolean>();
	private List<Boolean> nodeVarIsConstant = new ArrayList<Boolean>();
	private List<Boolean> nodeVarIsTemplate = new ArrayList<Boolean>();
	private List<Boolean> nodeVarIsRecord = new ArrayList<Boolean>();

	private List<String> nodeVarRecordValues = new ArrayList<String>();

	private List<String> nodeAssignIdentifiers = new ArrayList<String>();
	private List<String> nodeAssignValues = new ArrayList<String>();

	private List<String> testCaseCreateValues = new ArrayList<String>();
	private List<String> testCaseCreateCounter = new ArrayList<String>();
	private List<String> testCaseCreateRefValues = new ArrayList<String>();
	private List<String> testCaseCreateCharValues = new ArrayList<String>();

	private List<String> testCaseIfConditions = new ArrayList<String>();

	private List<String> nodeSendPortReference = new ArrayList<String>();
	private List<String> nodeSendParameter = new ArrayList<String>();
	private List<String> nodeSendParameterType = new ArrayList<String>();

	private List<String> altGuardConditions = new ArrayList<String>();
	private List<String> altGuardPortReference = new ArrayList<String>();
	private List<String> altGuardReceiveValue = new ArrayList<String>();
	private List<String> altGuardReceiveAnyValValue = new ArrayList<String>();
	private List<String> altGuardReceiveType = new ArrayList<String>();
	private List<String> altGuardTimeout = new ArrayList<String>();

	private List<String> doWhileExpressions = new ArrayList<String>();
	
	private List<String> receivePortReference = new ArrayList<String>();
	private List<String> receiveValue = new ArrayList<String>();
	private List<String> receiveAnyValValue = new ArrayList<String>();
	private List<String> receiveType = new ArrayList<String>();

	private List<String> testCaseDisconnectValues = new ArrayList<String>();

	private List<String> testCaseMapValues = new ArrayList<String>();
	private List<String> testCaseMapCounter = new ArrayList<String>();

	private List<String> testCaseStartValues = new ArrayList<String>();
	private List<String> testCaseStartValueParameters = new ArrayList<String>();
	private List<String> testCaseStartCounter = new ArrayList<String>();

	private List<String> testCaseStopValues = new ArrayList<String>();
	private List<String> testCaseStopValueParameters = new ArrayList<String>();
	private List<String> testCaseStopCounter = new ArrayList<String>();

	private List<String> testCaseConnectValues = new ArrayList<String>();

	private List<String> operatorList = new ArrayList<String>();

	private List<String> expressionValue = new ArrayList<String>();

	private List<StatementBlock> receiveStatements = new ArrayList<StatementBlock>();

	private boolean waitForTcStopValues = false;
	private int tcStopCounter = -1;
	private boolean waitForUnknownStopStatement = false;

	private boolean waitForRecord = false;
	private boolean isCreate = false;
	private boolean isSendValue = false;
	private boolean blockReferenceListing = false;
	Statement currentAltGuardStatement = null;

	public void visit(IVisitableNode node) {

		if (node instanceof Def_Function) {

			currentFunctionWriter = Def_Function_Writer.getInstance(((Def_Function) node));
			currentFunctionWriter.clearAltLists();
			myASTVisitor.nodeNameNodeTypeHashMap.put(((Def_Function) node).getIdentifier().toString(), "function");

			runsOnValue = null;

			waitForReturnType = true;
			waitForRunsOnValue = true;
			waitForStatementBlock = true;
			waitForFunction = true;

			expressionValue.clear();
		}

		if (node instanceof Def_Testcase) {

			currentTestCaseWriter = Def_Testcase_Writer.getInstance(((Def_Testcase) node));
			currentTestCaseWriter.clearAltLists();
			myASTVisitor.nodeNameNodeTypeHashMap.put(((Def_Testcase) node).getIdentifier().toString(), "testcase");

			AstWalkerJava.testCaseList.add(((Def_Testcase) node).getIdentifier().toString());

			waitForStatementBlock = true;
			waitForRunsOnValue = true;
			waitForTC = true;

			expressionValue.clear();
		}

		// Check for TC
		visitNodes(node);

		// Check Functions
		visitFunctionNodes(node);

		// Check for TC
		visitTcNodes(node);

		// Check for TC If cases
		visitTcIfCases(node);

		// Check for TC Alt cases
		visitAltCases(node);

		if (node instanceof Reference) {
			checkReference(node);
		}

		if ((currentAltGuardStatement != null) && (currentAltGuardStatement.equals(node))) {
			evaluateExpression();
		}

		// testcases
		// FormalParameter might come up for other types too, leave it here
		if (node instanceof FormalParameter) {
			isThereAFormalParameter = true;
		}

		if (isThereAFormalParameter && (node instanceof Reference)) {
			// TODO: formal parameter reference handling (tpye name)
			isThereAFormalParameter = false;
		}

		if (isThereAFormalParameter && (node instanceof Identifier)) {
			// TODO: formal parameter identifier handling
		}

	}

	public void leave(IVisitableNode node) {

		if (waitForTC || waitForFunction) {
			
			if (node instanceof Definition_Statement) {

				if (!waitForRecord) {
					nodeVarIsRecord.add(false);
				}
				waitForRecord = false;
				waitForValue = false;

				evaluateExpression();
			}

			if (node instanceof Undefined_LowerIdentifier_Value) {
				blockReferenceListing = false;
			}

			if ((node instanceof Assignment_Statement) || ((node instanceof If_Clause))) {
				waitForValue = false;

				// if assignment is a createExpression
				if (node instanceof Assignment_Statement) {
					if (((Assignment_Statement) node).getTemplate() instanceof SpecificValue_Template) {
						SpecificValue_Template specValTemplate = (SpecificValue_Template) ((Assignment_Statement) node)
								.getTemplate();
						if (specValTemplate.getSpecificValue() instanceof ComponentCreateExpression) {
							isCreate = true;
						}
					}
				}

				evaluateExpression();

			}

			if (node instanceof SequenceOf_Value) {
				evaluateExpression();
			}

			if ((waitForFunction && (node instanceof Return_Statement))
					|| (waitForTC && (node instanceof ComponentCreateExpression)) || (node instanceof Send_Statement)
					|| (node instanceof Receive_Port_Statement)) {
				evaluateExpression();
			}

			if (waitForTcStartValues && (node instanceof Identifier) && myASTVisitor.blockIdListing) {
				myASTVisitor.blockIdListing = false;
			}

			if (waitForTcStopValues && (node instanceof Identifier) && myASTVisitor.blockIdListing) {
				myASTVisitor.blockIdListing = false;
			}

			if (node instanceof Connect_Statement) {
				waitForTcConnectValues = false;
			}

			if (node instanceof Disconnect_Statement) {
				waitForTcDisconnectValues = false;

			}
			if (node instanceof Unknown_Start_Statement) {
				waitForTcStartValues = false;
			}

			if (node instanceof Unknown_Stop_Statement) {
				waitForTcStopValues = false;
			}

			if (node instanceof Definition_Statement) {
				waitForDefStatement = false;
				if (waitForValue) {
					nodeVarValues.add(null);
					nodeVarIsAValueReference.add(false);

					waitForValue = false;
				}

			}

			if (node instanceof Map_Statement) {
				waitForMapIdentifiers = false;
			}

			if (node instanceof ComponentCreateExpression) {

				// switch first two values
				testCaseCreateValues.add(testCaseCreateCharValues.get(0));
				testCaseCreateValues.add(testCaseCreateRefValues.get(0));

				for (int i = 1; i < testCaseCreateCharValues.size(); i++) {
					testCaseCreateValues.add(testCaseCreateCharValues.get(i));
				}

				testCaseCreateCharValues.clear();
				testCaseCreateRefValues.clear();
				waitForTcCreateValues = false;
			}

			if (node instanceof Assignment_Statement) {

				waitForAssignmentIdentifiers = false;
				waitForValue = true;
			}

			if (waitForTcIfCondition && (node instanceof If_Clause)) {
				waitForTcIfCondition = false;

			}

			if (waitForTcStartParameter && (node instanceof Unknown_Start_Statement)) {
				testCaseStartValueParameters.add(null);
			}

			if (waitForTcStopParameter && (node instanceof Unknown_Stop_Statement)) {
				testCaseStopValueParameters.add(null);
			}

			if (isAltGuards && (node instanceof Operation_Altguard)) {
				altGuardConditions.add(null);
			}

			if (node instanceof Operation_Altguard) {

				waitForAltStatements = false;
				isAltGuards = false;

				isReceiveValue = false;

			}

			handleAltCases(node);

			if (node instanceof Unknown_Start_Statement) {
				waitForUnknownStartStatement = false;
			}

			if (node instanceof Unknown_Stop_Statement) {
				waitForUnknownStopStatement = false;
			}
			
			
			if (node instanceof Receive_Port_Statement) {
				if(waitForReceiveParameter){
					altGuardReceiveType.add("noparam");
				}
				//TODO checkAnyport = true;
				
				
			}
		}

		if (node instanceof Def_Testcase) {
			handleTestcase(node);
			waitForTC = false;
		}

		if (node instanceof Def_Function) {
			handleFunction(node);
			waitForTC = false;
		}

		if (waitForAssignmentIdentifiers && (node instanceof ArraySubReference)) {
			waitForAssignmentIdentifiers = false;
		}

	}

	public void evaluateExpression() {
		int size = expressionValue.size() - 1;
		boolean arrayOperatorFound = false;
		boolean operatorFound = false;
		boolean unaryOperatorFound = false;
		String rightHand = "";
		String leftHand = "";
		String currentType = "";

		if (nodeVars.contains(currentIdentifier)) {

				currentType = nodeVarTypes.get(nodeVars.indexOf(currentIdentifier));
			
		}
		
		if(isSendStatement){
			currentType=nodeSendParameterType.get(nodeSendParameterType.size()-1);
			if(currentType.equals("IDENTIFIER")){
				currentType="CHARSTRING";
			}
			
		}
		
		for (int i = size; i >= 0; i--) {

			if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(expressionValue.get(i))) {

				if (myASTVisitor.nodeNameNodeTypeHashMap.get(expressionValue.get(i)).equals("constant")) {
					expressionValue.set(i, "Constants." + expressionValue.get(i) + "()");

				} else if (myASTVisitor.nodeNameNodeTypeHashMap.get(expressionValue.get(i)).equals("template")) {
					expressionValue.set(i, "Templates." + expressionValue.get(i) + "()");

				}
			}

			if (i <= size - 2) {
				leftHand = expressionValue.get(i + 1);
				rightHand = expressionValue.get(i + 2);
			}

			if (expressionValue.get(i).equals("ArraySubReference")) {
				String[] index = expressionValue.get(i + 1).split("\"");

				expressionValue.set(i - 1, expressionValue.get(i - 1) + ".get(" + index[1] + ")");
				arrayOperatorFound = true;
			} else if (expressionValue.get(i).equals("LengthofExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ").lengthof()");
				unaryOperatorFound = true;
			} else if (expressionValue.get(i).equals("IsChoosenExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ").isChosen()");
				unaryOperatorFound = true;
			} else if (expressionValue.get(i).equals("IsPresentExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ").isPresent()");
				unaryOperatorFound = true;
			} else if (expressionValue.get(i).equals("IsValueExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ").isValue()");
				unaryOperatorFound = true;
			} else if (expressionValue.get(i).equals("IsBoundExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ").isBound()");
				unaryOperatorFound = true;
			} else if (expressionValue.get(i).equals("NotExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ").not()");
				unaryOperatorFound = true;
			} else if (expressionValue.get(i).equals("Not4bExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ").bitwiseNot()");
				unaryOperatorFound = true;
			} else if (expressionValue.get(i).equals("Xor4bExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ").bitwiseXor(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("Or4bExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ").bitwiseOr(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("And4bExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ").bitwiseAnd(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("ShiftRightExpression")) {
				expressionValue.set(i, "((" + currentType + ")(" + leftHand + ").shiftRight(" + rightHand + "))");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("ShiftLeftExpression")) {
				expressionValue.set(i, "((" + currentType + ")(" + leftHand + ").shiftLeft(" + rightHand + "))");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("RotateRightExpression")) {
				expressionValue.set(i, "((" + currentType + ")(" + leftHand + ").rotateRight(" + rightHand + "))");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("RotateLeftExpression")) {
				expressionValue.set(i, "((" + currentType + ")(" + leftHand + ").rotateLeft(" + rightHand + "))");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("StringConcatenationExpression")) {
				expressionValue.set(i, "((" + currentType + ")(" + leftHand + ").concatenate(" + rightHand + "))");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("AddExpression")) {
				expressionValue.set(i, "(" + leftHand + ").plus(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("SubstractExpression")) {
				expressionValue.set(i, "(" + leftHand + ").minus(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("MultiplyExpression")) {
				expressionValue.set(i, "(" + leftHand + ").multipleBy(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("DivideExpression")) {
				expressionValue.set(i, "(" + leftHand + ").divideBy(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("ModuloExpression")) {
				expressionValue.set(i, "(" + leftHand + ").mod(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("RemainderExpression")) {
				expressionValue.set(i, "(" + leftHand + ").rem(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("NotequalesExpression")) {
				expressionValue.set(i, "(" + leftHand + ").equalsWith(" + rightHand + ").not()");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("LessThanExpression")) {
				expressionValue.set(i, "(" + leftHand + ").isLessThan(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("LessThanOrEqualExpression")) {
				expressionValue.set(i, "(" + leftHand + ").isLessOrEqualThan(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("GreaterThanExpression")) {
				expressionValue.set(i, "(" + leftHand + ").isGreaterThan(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("GreaterThanOrEqualExpression")) {
				expressionValue.set(i, "(" + leftHand + ").isGreaterOrEqualThan(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("EqualsExpression")) {
				expressionValue.set(i, "(" + leftHand + ").equalsWith(" + rightHand + ")");
				operatorFound = true;
			}

			if (arrayOperatorFound) {
				arrayOperatorFound = false;
				expressionValue.remove(i + 1);
				expressionValue.remove(i);
				size = expressionValue.size() - 1;
				i = size;
			} else if (unaryOperatorFound) {
				unaryOperatorFound = false;
				expressionValue.remove(i + 1);
				size = expressionValue.size() - 1;
				i = size;
			} else if (operatorFound) {
				operatorFound = false;
				expressionValue.remove(i + 2);
				expressionValue.remove(i + 1);
				size = expressionValue.size() - 1;
				i = size;
			}

		}

		String value = null;

		if (expressionValue.size() > 0) {
			value = expressionValue.get(0);
		}

		if (waitForIndexSetValue) {
			value = value + ")";
			waitForIndexSetValue = false;

		}

		// Set value for the appropriate list
		if (isCreate) {
			testCaseCreateCharValues.addAll(expressionValue);
			testCaseCreateCounter.add(String.valueOf(tcCreateCounter));
		} else if(isDoWhileStatement) {
			doWhileExpressions.add(value);
		//	isDoWhileStatement=false;
		} else if (isDefinition) {

			if (isNextValueConstant) {
				nodeVarIsConstant.add(true);
			} else {
				nodeVarIsConstant.add(false);
			}

			if (isNextValueTemplate) {
				nodeVarIsTemplate.add(true);
			} else {
				nodeVarIsTemplate.add(false);
			}

			if (waitForRecord) {
				nodeVarValues.addAll(expressionValue);
				// waitForRecord = false;
			} else {
				nodeVarValues.add(value);
			}

			nodeVarIsAValueReference.add(true);

		} else if (isAssignment) {

			nodeAssignValues.add(value);

		} else if (isIf) {

			testCaseIfConditions.add(value);

		} else if (isSendStatement) {
			nodeSendParameter.add(value);
		} else if (isReceiveValue) {
			if (waitForAltStatements) {
				if (!waitForReceiveAnyValTemplateValue) {
					altGuardReceiveValue.add(value);
				}
			} else {
				receiveValue.add(value);
			}
			//waitForReceiveParameter=false;
		} else if (isAltGuards) {
			altGuardConditions.add(value);
			isAltGuards = false;

		} else if (waitForReturnStatementValue) {
			returnStatementValue = value;
		}

		if (isAltGuards) {
			altGuardConditions.add(null);
			isAltGuards = false;
		}

		isNextValueTemplate = false;
		isNextValueConstant = false;

		waitForReceiveAnyValTemplateValue = false;

		isSendStatement = false;
		isSendValue = false;
		isReceiveValue = false;
		isDefinition = false;
		isAssignment = false;

		isCreate = false;
		expressionValue.clear();
	}

	public void visitNodes(IVisitableNode node) {

		if (waitForStatementBlock && (node instanceof StatementBlock)) {
			functionStatementBlock = (StatementBlock) node;
			tcStatementBlock = ((StatementBlock) node);

			waitForRunsOnValue = false;
			waitForReturnType = false;
			waitForStatementBlock = false;

			waitForStatements = true;
		}

		if (waitForAltStatements && (node instanceof StatementBlock)) {
			evaluateExpression();
			isReceiveValue = false;

			if (!isAnyValValue) {
				altGuardReceiveAnyValValue.add(null);

			}
			isAnyValValue = false;
		}

		if (waitForStatements) {
			visitStatementSelector(node);
		}

		if (waitForDefStatement) {
			visitDefStatements(node);
		}

		if (waitForTC || waitForFunction) {
			// Check expression operators

			// Check expression values
			if (!waitForAssignmentIdentifiers) {
				visitExpressionTypeSelectors(node);
				visitExpressionValueSetters(node);
			}
		}
		if (isIf && (node instanceof StatementBlock)) {
			// has to be here, If always has a statemnt block
			evaluateExpression();
			isIf = false;

		}
		if (isDoWhileStatement && (node instanceof StatementBlock)) {
			// has to be here, If always has a statemnt block
			evaluateExpression();
			isDoWhileStatement = false;

		}


		if (waitForTC && (node instanceof Unknown_Start_Statement)) {
			waitForTcStartValues = true;
			tcStartCounter++;
		}

		if (waitForTC && (node instanceof Unknown_Stop_Statement)) {
			waitForTcStopValues = true;
			tcStopCounter++;
		}

		if (waitForTcStartValues) {
			visitStartValueSetters(node);
		}

		if (waitForTcStopValues) {
			visitStopValueSetters(node);
		}

		if (waitForTC && (node instanceof Map_Statement)) {
			waitForMapIdentifiers = true;
			mapCounter++;
			mapValueCounter = 0;
		}

		if (waitForMapIdentifiers) {
			visitMapIdentifiers(node);
		}

	/*	if (isSendStatement) {
			visitSendStatements(node);
		}*/

		if (waitForTcDisconnectValues) {
			visitDisconnectValueSetters(node);
		}

	}

	public void visitStatementSelector(IVisitableNode node) {

		if (waitForStatements && (node instanceof Definition_Statement)) {
			waitForDefStatement = true;
			waitForValue = true;
			isDefinition = true;
		}

		if (waitForStatements && (node instanceof Send_Statement)) {
			isSendStatement = true;
		}
		
		if (waitForStatements && (node instanceof DoWhile_Statement)) {
			isDoWhileStatement = true;
			waitForValue=true;
		}
		
		

		if (waitForStatements && (node instanceof Operation_Altguard)) {
			isAltGuards = true;
			waitForAltStatements = true;
			currentAltGuardStatement = ((Operation_Altguard) node).getGuardStatement();
			waitForValue = true;

		}

		if (waitForStatements && (node instanceof Assignment_Statement)) {
			waitForAssignmentIdentifiers = true;
			waitForValue = true;
			isAssignment = true;
		}

		if (waitForStatements && (node instanceof Disconnect_Statement)) {
			waitForTcDisconnectValues = true;
		}
	}

	public void visitDefStatements(IVisitableNode node) {

		if (waitForDefStatement && (node instanceof Def_Var)) {
			nodeVars.add(((Def_Var) node).getIdentifier().toString());
			currentIdentifier = ((Def_Var) node).getIdentifier().toString();
		}

		if (waitForDefStatement && (node instanceof Def_Var_Template)) {
			nodeVars.add(((Def_Var_Template) node).getIdentifier().toString());
			isNextValueTemplate = true;
			currentIdentifier = ((Def_Var_Template) node).getIdentifier().toString();
		}

		if (waitForDefStatement && (node instanceof Def_Const)) {
			// TODO const spec
			nodeVars.add(((Def_Const) node).getIdentifier().toString());
			isNextValueConstant = true;
			currentIdentifier = ((Def_Const) node).getIdentifier().toString();
		}

		if (waitForDefStatement && (node instanceof BitString_Type)) {
			nodeVarTypes.add("BITSTRING");
			nodeVarTypeIsAReference.add(false);

		}
		if (waitForDefStatement && (node instanceof Integer_Type)) {
			nodeVarTypes.add("INTEGER");
			nodeVarTypeIsAReference.add(false);
		}
		
		if (waitForDefStatement && (node instanceof Float_Type)) {
			nodeVarTypes.add("FLOAT");
			nodeVarTypeIsAReference.add(false);
		}

		if (waitForDefStatement && (node instanceof CharString_Type)) {
			nodeVarTypes.add("CHARSTRING");
			nodeVarTypeIsAReference.add(false);
		}

		if (waitForDefStatement && (node instanceof OctetString_Type)) {
			nodeVarTypes.add("OCTETSTRING");
			waitForValue = true;
			nodeVarTypeIsAReference.add(false);
		}

		if (waitForDefStatement && (node instanceof Boolean_Type)) {
			nodeVarTypes.add("BOOLEAN");
			nodeVarTypeIsAReference.add(false);
		}

		if (waitForDefStatement && (node instanceof Def_Timer)) {
			nodeVars.add(((Def_Timer) node).getIdentifier().toString());
			currentIdentifier = ((Def_Timer) node).getIdentifier().toString();
			nodeVarTypes.add("TIMER");
			waitForDefStatement = false;
			nodeVarTypeIsAReference.add(false);

		}

	}

	public void visitExpressionTypeSelectors(IVisitableNode node) {

		if (waitForValue && (node instanceof If_Clause)) {
			isIf = true;
		}

		if (waitForValue && !waitForAssignmentIdentifiers && (node instanceof ArraySubReference)) {
			expressionValue.add("ArraySubReference");
		}

		if (waitForValue && (node instanceof AddExpression)) {
			expressionValue.add("AddExpression");
		}

		if (waitForValue && (node instanceof IsValueExpression)) {
			expressionValue.add("IsValueExpression");
		}

		if (waitForValue && (node instanceof IsChoosenExpression)) {
			expressionValue.add("IsChoosenExpression");
		}

		if (waitForValue && (node instanceof IsBoundExpression)) {
			expressionValue.add("IsBoundExpression");
		}

		if (waitForValue && (node instanceof IsPresentExpression)) {
			expressionValue.add("IsPresentExpression");
		}

		if (waitForValue && (node instanceof Not4bExpression)) {
			expressionValue.add("Not4bExpression");
		}

		if (waitForValue && (node instanceof And4bExpression)) {
			expressionValue.add("And4bExpression");
		}

		if (waitForValue && (node instanceof Or4bExpression)) {
			expressionValue.add("Or4bExpression");
		}

		if (waitForValue && (node instanceof Xor4bExpression)) {
			expressionValue.add("Xor4bExpression");
		}

		if (waitForValue && (node instanceof ShiftLeftExpression)) {
			expressionValue.add("ShiftLeftExpression");
		}

		if (waitForValue && (node instanceof ShiftRightExpression)) {
			expressionValue.add("ShiftRightExpression");
		}

		if (waitForValue && (node instanceof RotateRightExpression)) {
			expressionValue.add("RotateRightExpression");
		}

		if (waitForValue && (node instanceof RotateLeftExpression)) {
			expressionValue.add("RotateLeftExpression");
		}

		if (waitForValue && (node instanceof StringConcatenationExpression)) {
			expressionValue.add("StringConcatenationExpression");
		}

		if (waitForValue && (node instanceof LengthofExpression)) {
			expressionValue.add("LengthofExpression");
		}

		if (waitForValue && (node instanceof SubstractExpression)) {
			expressionValue.add("SubstractExpression");
		}

		if (waitForValue && (node instanceof MultiplyExpression)) {
			expressionValue.add("MultiplyExpression");
		}

		if (waitForValue && (node instanceof DivideExpression)) {
			expressionValue.add("DivideExpression");
		}

		if (waitForValue && (node instanceof ModuloExpression)) {
			expressionValue.add("ModuloExpression");
		}

		if (waitForValue && (node instanceof RemainderExpression)) {
			expressionValue.add("RemainderExpression");
		}

		if (waitForValue && (node instanceof NotequalesExpression)) {
			expressionValue.add("NotequalesExpression");
		}

		if (waitForValue && (node instanceof LessThanExpression)) {
			expressionValue.add("LessThanExpression");
		}

		if (waitForValue && (node instanceof LessThanOrEqualExpression)) {
			expressionValue.add("LessThanOrEqualExpression");
		}

		if (waitForValue && (node instanceof GreaterThanExpression)) {
			expressionValue.add("GreaterThanExpression");
		}

		if (waitForValue && (node instanceof GreaterThanOrEqualExpression)) {
			expressionValue.add("GreaterThanOrEqualExpression");
		}

		if (waitForValue && (node instanceof EqualsExpression)) {
			expressionValue.add("EqualsExpression");
		}

		if (waitForValue && (node instanceof NotExpression)) {
			expressionValue.add("NotExpression");
		}

	}

	public void visitExpressionValueSetters(IVisitableNode node) {

		if (node instanceof Real_Value) {

			String value = ((Real_Value) node).createStringRepresentation();

			value = "new FLOAT(" + value + ")";
			
			expressionValue.add(value);

		}

		if (node instanceof Integer_Value) {

			String value = ((Integer_Value) node).toString();

			if (myASTVisitor.isNextIntegerNegative) {
				value = "-" + ((Integer_Value) node).toString();
			}
			value = "new INTEGER(\"" + value + "\")";

			expressionValue.add(value);

			if (isSendStatement) {
				nodeSendParameterType.add("INTEGER");
			}

			if (isReceiveValue) {
				if (waitForAltStatements) {
					altGuardReceiveType.add("INTEGER");
				} else {
					receiveType.add("INTEGER");
				}
				waitForReceiveParameter=false;
			}

			myASTVisitor.isNextIntegerNegative = false;

		}

		if (node instanceof Undefined_LowerIdentifier_Value) {
			String value = ((Undefined_LowerIdentifier_Value) node).getIdentifier().toString();

			if (myASTVisitor.isNextIntegerNegative) {
				value = value + ".negate()";
			}

			if (waitForInt2StrExpression) {
				value = value + ".int2str()";
				waitForInt2StrExpression = false;
			}

			expressionValue.add(value);

			if (isSendStatement) {
				nodeSendParameterType.add("IDENTIFIER");
			}

			if (isReceiveValue) {
				if (waitForAltStatements) {
					altGuardReceiveType.add("IDENTIFIER");
				} else {
					receiveType.add("IDENTIFIER");
				}
				waitForReceiveParameter=false;
			}

			myASTVisitor.isNextIntegerNegative = false;
			/*
			 * if (nodeVarTypes.size() > 0) { if
			 * (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(nodeVarTypes.
			 * get(nodeVarTypes.size() - 1))) {
			 * 
			 * if (myASTVisitor.nodeNameNodeTypeHashMap.get(nodeVarTypes.get(
			 * nodeVarTypes.size() - 1)) .equals("record")) {
			 * 
			 * if (currentRecordCounter <
			 * myASTVisitor.nodeNameChildrenNamesHashMap
			 * .get(nodeVarTypes.get(nodeVarTypes.size() - 1)).length) {
			 * waitForRecord = true; nodeVarIsRecord.add(true);
			 * 
			 * } } } }
			 */
			blockReferenceListing = true;

		}

		if (node instanceof Boolean_Value) {

			String value = ((Boolean_Value) node).getValue() ? "BOOLEAN.TRUE" : "BOOLEAN.FALSE";
			expressionValue.add(value);
		}

		if (node instanceof Charstring_Value) {
			String value = "";
			if (isCreate) {
				value = ((Charstring_Value) node).getValue();
			} else {
				value = "new CHARSTRING(\"" + ((Charstring_Value) node).getValue() + "\")";
			}
			expressionValue.add(value);

			if (isSendStatement) {
				nodeSendParameterType.add("CHARSTRING");
			}

			if (isReceiveValue) {
				if (waitForAltStatements) {
					altGuardReceiveType.add("CHARSTRING");
				} else {
					receiveType.add("CHARSTRING");
				}
				waitForReceiveParameter=false;
			}
		}

		if (node instanceof Octetstring_Value) {
			String value = "";
			if (isCreate) {
				value = ((Octetstring_Value) node).getValue();
			} else {
				value = "new OCTETSTRING(\"" + ((Octetstring_Value) node).getValue() + "\")";
			}
			expressionValue.add(value);

			if (isSendStatement) {
				nodeSendParameterType.add("OCTETSTRING");
			}

			if (isReceiveValue) {
				if (waitForAltStatements) {
					altGuardReceiveType.add("OCTETSTRING");
				} else {
					receiveType.add("OCTETSTRING");
				}
				waitForReceiveParameter=false;
			}
		}

		if (node instanceof Bitstring_Value) {
			String value = "(new BITSTRING(\"" + ((Bitstring_Value) node).getValue().toString() + "\"))";

			expressionValue.add(value);
		}

	}

	public void visitStartValueSetters(IVisitableNode node) {
		if (waitForTcStartValues && (node instanceof ParameterisedSubReference)) {
			testCaseStartValueParameters.add(((ParameterisedSubReference) node).getId().toString());
			myASTVisitor.blockIdListing = true;
			waitForTcStartParameter = false;
		}

		if (waitForTcStartValues && (node instanceof Identifier) && !myASTVisitor.blockIdListing) {
			myASTVisitor.blockIdListing = false;
			testCaseStartValues.add(((Identifier) node).toString());
			testCaseStartCounter.add(String.valueOf(tcStartCounter));
			waitForTcStartParameter = true;
		}
	}

	public void visitStopValueSetters(IVisitableNode node) {
		if (waitForTcStopValues && (node instanceof ParameterisedSubReference)) {
			testCaseStopValueParameters.add(((ParameterisedSubReference) node).getId().toString());
			myASTVisitor.blockIdListing = true;
			waitForTcStopParameter = false;
		}

		if (waitForTcStopValues && (node instanceof Identifier) && !myASTVisitor.blockIdListing) {
			myASTVisitor.blockIdListing = false;
			testCaseStopValues.add(((Identifier) node).toString());
			testCaseStopCounter.add(String.valueOf(tcStopCounter));
			waitForTcStopParameter = true;
		}
	}

	public void visitDisconnectValueSetters(IVisitableNode node) {

		if (waitForTcDisconnectValues && (node instanceof Identifier)) {
			testCaseDisconnectValues.add(((Identifier) node).toString());
		}

		if (waitForTcDisconnectValues && (node instanceof SelfComponentExpression)) {
			testCaseDisconnectValues.add("self");
		}
	}

	public void visitMapIdentifiers(IVisitableNode node) {

		if (waitForMapIdentifiers && (node instanceof SystemComponentExpression) && (mapValueCounter <= 4)) {
			testCaseMapValues.add("system");
			testCaseMapCounter.add(Integer.toString(mapCounter));
			mapValueCounter++;
		}
		
		if (waitForMapIdentifiers && (node instanceof MTCComponentExpression) && (mapValueCounter <= 4)) {
			testCaseMapValues.add("mtc");
			testCaseMapCounter.add(Integer.toString(mapCounter));
			mapValueCounter++;
		}
	}

	public void checkReference(IVisitableNode node) {
		String value = ((Reference) node).getId().toString();
		
		List<ISubReference> subrefs=((Reference) node).getSubreferences();
		if(subrefs!=null){
			value="";
			for(int i=0;i<subrefs.size();i++){
				value+=subrefs.get(i).getId();
				if(i<subrefs.size()-1){
					if(!(subrefs.get(i+1) instanceof ArraySubReference)){
						value+=".";
					}
				}
			}
		}
		
		if (waitForMapIdentifiers && (mapValueCounter <= 4)) {
			testCaseMapValues.add(value);
			testCaseMapCounter.add(Integer.toString(mapCounter));
			mapValueCounter++;
		} else if (isSendStatement && !isSendValue) {
			nodeSendPortReference.add(value);
			isSendValue = true;

		} else if (waitForTcCreateValues) {
			testCaseCreateRefValues.add(value);
			testCaseCreateCounter.add(String.valueOf(tcCreateCounter));

		} else if (waitForReceiveStatement && !waitForTypedParam && !waitForReceiveAnyValTemplateValue
				&& !isReceiveValue) {

			if (waitForAltStatements) {
				altGuardPortReference.add(value);
			} else {
				receivePortReference.add(value);
			}

			waitForValue = true;
			isReceiveValue = true;
			waitForReceiveParameter = true;
			checkAnyport = false;
		} else if (waitForTypedParam) {
			if (waitForAltStatements) {
				altGuardReceiveValue.add(value);
				altGuardReceiveType.add("_TYPED_PARAM_");
				
			} else {
				receiveValue.add(value);
				receiveType.add("_TYPED_PARAM_");
			}
			waitForTypedParam = false;
			waitForReceiveParameter=false;
		} else if (waitForReceiveAnyValTemplateValue && !waitForTypedParam) {

			if (waitForAltStatements) {
				altGuardReceiveAnyValValue.add(value);
			} else {
				receiveAnyValValue.add(value);
			}
			isAnyValValue = true;

		} else if (waitForUnknownStartStatement) {
			unknownStartReference.add(value);
		} else if (waitForRunsOnValue && !isThereAFormalParameter) {
			runsOnValue = value;

			waitForRunsOnValue = false;

			if (waitForFunction) {
				AstWalkerJava.functionRunsOnList.add(value);
			}
		} else if (waitForUnknownStopStatement) {
			unknownStopReference.add(value);
		} else if (waitForAssignmentIdentifiers) {

			nodeAssignIdentifiers.add(value);
			currentIdentifier = value;

			if (((Reference) node).getSubreferences().size() > 1) {
				if (((Reference) node).getSubreferences().get(1) instanceof ArraySubReference) {
					ArraySubReference subref = (ArraySubReference) ((Reference) node).getSubreferences().get(1);
					nodeAssignIdentifiers.set(nodeAssignIdentifiers.size() - 1,
							nodeAssignIdentifiers.get(nodeAssignIdentifiers.size() - 1) + ".set("
									+ subref.getValue().toString() + ",");
					waitForIndexSetValue = true;
				}
			} else {

				nodeAssignIdentifiers.set(nodeAssignIdentifiers.size() - 1,
						nodeAssignIdentifiers.get(nodeAssignIdentifiers.size() - 1) + "=");

				// Only set to false if there are no arraysubrefs
				// For arraysubrefs the flag is set to false in the
				// arraysubref leave part
				waitForAssignmentIdentifiers = false;
			}

		} else if (waitForDefStatement && !blockReferenceListing) {

			nodeVarTypes.add(value);
			nodeVarTypeIsAReference.add(true);

			if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(nodeVarTypes.get(nodeVarTypes.size() - 1))) {

				if (myASTVisitor.nodeNameNodeTypeHashMap.get(nodeVarTypes.get(nodeVarTypes.size() - 1))
						.equals("record")) {

					if (currentRecordCounter < myASTVisitor.nodeNameChildrenNamesHashMap
							.get(nodeVarTypes.get(nodeVarTypes.size() - 1)).length) {
						waitForRecord = true;
						nodeVarIsRecord.add(true);

					}
				}
			}

		} else if (waitForValue && !isReceiveValue && !blockReferenceListing) {
			// has to be the last one
			// sends the Reference value to be processed as an assignment
			// identifier
			expressionValue.add(value);
		} /*
			 * else if (nodeVarTypes.size() > 0) { if
			 * (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(value)) {
			 * 
			 * if (myASTVisitor.nodeNameNodeTypeHashMap.get(nodeVarTypes.get(
			 * nodeVarTypes.size() - 1)) .equals("record")) {
			 * 
			 * if (currentRecordCounter <
			 * myASTVisitor.nodeNameChildrenNamesHashMap
			 * .get(nodeVarTypes.get(nodeVarTypes.size() - 1)).length) {
			 * waitForRecord = true; nodeVarIsRecord.add(true);
			 * 
			 * } } } }
			 */

	}

	public void visitSendStatements(IVisitableNode node) {

	}

	public void visitTcNodes(IVisitableNode node) {

		if (waitForTC && (node instanceof ComponentCreateExpression)) {
			isCreate = true;
			waitForTcCreateValues = true;
			tcCreateCounter++;
		}

		if (waitForTC && (node instanceof Connect_Statement)) {
			waitForTcConnectValues = true;
		}

		if (waitForTcConnectValues && (node instanceof Identifier)) {
			testCaseConnectValues.add(((Identifier) node).toString());

		}

		if (waitForTcConnectValues && (node instanceof SelfComponentExpression)) {
			testCaseConnectValues.add("self");

		}

	}

	public void visitTcIfCases(IVisitableNode node) {

		if (waitForTC && (node instanceof If_Statement)) {

			waitForValue = true;
			isIf = true;

		}

	}

	public void visitAltCases(IVisitableNode node) {

		if (node instanceof Receive_Port_Statement) {
			waitForReceiveStatement = true;
			checkAnyport = true;
			
			
		}

		if (waitForReceiveStatement && (node instanceof StatementBlock)) {

			if (!waitForAltStatements) {
				receiveStatements.add((StatementBlock) node);
			}
			waitForReceiveStatement = false;
		}

		if (node instanceof TemplateInstance) {
			if (((TemplateInstance) node).getType() != null) {
				waitForTypedParam = true;
				checkAnyport = true;
			}
		}

		if (checkAnyport && (node instanceof Setverdict_Statement)) {
			if (waitForAltStatements) {
				altGuardPortReference.add("any port");
				altGuardReceiveValue.add(null);
				altGuardReceiveType.add("any port");
			} else {
				receivePortReference.add("any port");
				receiveValue.add("null");
				receiveType.add("any port");
			}

			waitForReceiveStatement = false;

			checkAnyport = false;
		}

		if ((waitForTC || waitForFunction) && (node instanceof Any_Value_Template)) {

			waitForReceiveAnyValTemplateValue = true;

			checkAnyport = false;
		}

		if (waitForAltStatements && (node instanceof Timeout_Statement)) {

			waitForAltTimeout = true;
		}

		if (waitForAltTimeout && (node instanceof Identifier)) {
			altGuardTimeout.add(((Identifier) node).toString());
			waitForAltTimeout = false;

		}

	}

	public void visitFunctionNodes(IVisitableNode node) {

		if (waitForReturnType && (node instanceof Integer_Type)) {
			returnType = "Integer_Type";
		}

		if (waitForReturnType && (node instanceof Boolean_Type)) {
			returnType = "Boolean_Type";
		}

		if (waitForFunction && (node instanceof Return_Statement)) {
			waitForReturnStatementValue = true;
		}

		if (node instanceof Unknown_Start_Statement) {
			waitForUnknownStartStatement = true;
		}

		if (node instanceof Unknown_Stop_Statement) {
			waitForUnknownStopStatement = true;
		}

		if (node instanceof Int2StrExpression) {
			waitForInt2StrExpression = true;
		}

	}

	public void handleFunction(IVisitableNode node) {
		Def_Function_Writer functionNode = Def_Function_Writer.getInstance(((Def_Function) node));
		functionNode.clearLists();
		functionNode.setStatementBlock(functionStatementBlock);

		functionNode.runsOnValue = runsOnValue;
		functionNode.returnType = returnType;
		functionNode.returnStatementValue = returnStatementValue;

		returnType = null;
		returnStatementValue = null;

		myASTVisitor.currentFileName = ((Def_Function) node).getIdentifier().toString();

		functionNode.functionVars.addAll(nodeVars);
		functionNode.functionVarTypes.addAll(nodeVarTypes);
		functionNode.functionVarValues.addAll(nodeVarValues);

		functionNode.functionVarIsConstant.addAll(nodeVarIsConstant);
		functionNode.functionValueIsAValueReference.addAll(nodeVarIsAValueReference);
		functionNode.functionAssignIdentifiers.addAll(nodeAssignIdentifiers);
		functionNode.functionAssignValues.addAll(nodeAssignValues);

		functionNode.receivePortReference.addAll(receivePortReference);
		functionNode.receiveValue.addAll(receiveValue);
		functionNode.receiveAnyValValue.addAll(receiveAnyValValue);
		functionNode.receiveType.addAll(receiveType);

		functionNode.doWhileExpressions.addAll(doWhileExpressions);
		
		functionNode.receiveStatements.addAll(receiveStatements);

		Def_Function_Writer.sendPortReference.addAll(nodeSendPortReference);
		Def_Function_Writer.sendParameter.addAll(nodeSendParameter);
		Def_Function_Writer.sendParameterType.addAll(nodeSendParameterType);

		functionNode.unknownStartReference.addAll( unknownStartReference);
		functionNode.unknownStopReference = unknownStopReference;

		if (runsOnValue != null) {

			AstWalkerJava.functionList.add(((Def_Function) node).getIdentifier().toString());

			myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings);
			myASTVisitor.visualizeNodeToJava(functionNode.writeFunctionFile());
			myASTVisitor.currentFileName = runsOnValue;
			myASTVisitor.deleteLastBracket(myASTVisitor.currentFileName);

		} else {
			myASTVisitor.currentFileName = "TTCN_functions";
			myASTVisitor.deleteLastBracket(myASTVisitor.currentFileName);
		}

		myASTVisitor.visualizeNodeToJava(functionNode.getJavaSource() + "\r\n}");

		clearEverything();

	}

	public void handleTestcase(IVisitableNode node) {
		Def_Testcase_Writer testNode = Def_Testcase_Writer.getInstance(((Def_Testcase) node));

		testNode.clearLists();

		myASTVisitor.currentFileName = ((Def_Testcase) node).getIdentifier().toString();

		testNode.testCaseRunsOn = runsOnValue;
		testNode.tcMainStatementBlock = tcStatementBlock;
		AstWalkerJava.testCaseRunsOnList.add(runsOnValue);

		testNode.tcConnectValues.addAll(testCaseConnectValues);
		testNode.tcStartIdentifiers.addAll(testCaseStartValues);
		testNode.tcStartCounter.addAll(testCaseStartCounter);
		testNode.tcMapValues.addAll(testCaseMapValues);
		testNode.tcMapCounter.addAll(testCaseMapCounter);
		testNode.tcIfConditions.addAll(testCaseIfConditions);
		testNode.tcVars.addAll(nodeVars);
		testNode.tcVarTypes.addAll(nodeVarTypes);
		testNode.tcVarValues.addAll(nodeVarValues);
		testNode.tcAssignIdentifiers.addAll(nodeAssignIdentifiers);
		testNode.tcAssignValues.addAll(nodeAssignValues);
		testNode.tcValueIsAValueReference.addAll(nodeVarIsAValueReference);
		testNode.tcCreateValues.addAll(testCaseCreateValues);
		testNode.tcCreateCounter.addAll(testCaseCreateCounter);
		Def_Testcase_Writer.sendPortReference.addAll(nodeSendPortReference);
		Def_Testcase_Writer.sendParameter.addAll(nodeSendParameter);
		Def_Testcase_Writer.sendParameterType.addAll(nodeSendParameterType);

		testNode.tcVarIsConstant.addAll(nodeVarIsConstant);
		testNode.tcVarIsTemplate.addAll(nodeVarIsTemplate);
		testNode.tcDisconnectValues.addAll(testCaseDisconnectValues);
		testNode.testCaseStartValueParameters.addAll(testCaseStartValueParameters);
		testNode.tcValueTypeIsAReference.addAll(nodeVarTypeIsAReference);
		testNode.tcStopIdentifiers.addAll(testCaseStopValues);
		testNode.tcStopCounter.addAll(testCaseStopCounter);
		testNode.testCaseStopValueParameters.addAll(testCaseStopValueParameters);

		testNode.receivePortReference.addAll(receivePortReference);
		testNode.receiveValue.addAll(receiveValue);
		testNode.receiveAnyValValue.addAll(receiveAnyValValue);
		testNode.receiveType.addAll(receiveType);

		testNode.receiveStatements.addAll(receiveStatements);
		testNode.nodeVarIsRecord.addAll(nodeVarIsRecord);

		testNode.doWhileExpressions.addAll(doWhileExpressions);
		
		myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings);
		myASTVisitor.visualizeNodeToJava(testNode.writeTestcaseFile((Def_Testcase) node));

		myASTVisitor.currentFileName = runsOnValue;
		myASTVisitor.deleteLastBracket(myASTVisitor.currentFileName);
		myASTVisitor.visualizeNodeToJava(testNode.getJavaSource() + "\r\n}");

		clearEverything();

	}

	public void handleAltCases(IVisitableNode node) {

		if (waitForTC && (node instanceof Alt_Statement)) {

			currentTestCaseWriter.altStatements
					.add(new Def_AltStatement_Writer((Alt_Statement) node, currentTestCaseWriter.nodeName));

			currentTestCaseWriter.altStatements.get(currentTestCaseWriter.altStatements.size() - 1).setType("TestCase");

			currentTestCaseWriter.altStatements.get(currentTestCaseWriter.altStatements.size() - 1)
					.setParent(currentTestCaseWriter);

			currentTestCaseWriter.altStatements.get(currentTestCaseWriter.altStatements.size() - 1).altGuardConditions
					.addAll(altGuardConditions);

			currentTestCaseWriter.altStatements
					.get(currentTestCaseWriter.altStatements.size() - 1).altGuardPortReference
							.addAll(altGuardPortReference);

			currentTestCaseWriter.altStatements.get(currentTestCaseWriter.altStatements.size() - 1).altGuardReceiveValue
					.addAll(altGuardReceiveValue);

			currentTestCaseWriter.altStatements.get(currentTestCaseWriter.altStatements.size() - 1).altGuardReceiveType
					.addAll(altGuardReceiveType);

			currentTestCaseWriter.altStatements.get(currentTestCaseWriter.altStatements.size() - 1).altGuardTimeout
					.addAll(altGuardTimeout);

			currentTestCaseWriter.altStatements
			.get(currentTestCaseWriter.altStatements.size() - 1).altGuardReceiveAnyValValue
					.addAll(altGuardReceiveAnyValValue);
			
			clearAltLists();

		}

		if (waitForFunction && (node instanceof Alt_Statement)) {

			currentFunctionWriter.altStatements
					.add(new Def_AltStatement_Writer((Alt_Statement) node, currentFunctionWriter.nodeName));

			currentFunctionWriter.altStatements.get(currentFunctionWriter.altStatements.size() - 1).setType("Function");
			currentFunctionWriter.altStatements.get(currentFunctionWriter.altStatements.size() - 1)
					.setParent(currentFunctionWriter);

			currentFunctionWriter.altStatements.get(currentFunctionWriter.altStatements.size() - 1).altGuardConditions
					.addAll(altGuardConditions);

			currentFunctionWriter.altStatements
					.get(currentFunctionWriter.altStatements.size() - 1).altGuardPortReference
							.addAll(altGuardPortReference);

			currentFunctionWriter.altStatements.get(currentFunctionWriter.altStatements.size() - 1).altGuardReceiveValue
					.addAll(altGuardReceiveValue);

			currentFunctionWriter.altStatements.get(currentFunctionWriter.altStatements.size() - 1).altGuardReceiveType
					.addAll(altGuardReceiveType);

			currentFunctionWriter.altStatements.get(currentFunctionWriter.altStatements.size() - 1).altGuardTimeout
					.addAll(altGuardTimeout);

			currentFunctionWriter.altStatements
					.get(currentFunctionWriter.altStatements.size() - 1).altGuardReceiveAnyValValue
							.addAll(altGuardReceiveAnyValValue);

			clearAltLists();
		}

	}

	public void clearAltLists() {
		altGuardConditions.clear();
		altGuardPortReference.clear();
		altGuardReceiveValue.clear();
		altGuardReceiveType.clear();
		altGuardTimeout.clear();
		altGuardReceiveAnyValValue.clear();
	}

	public void clearEverything() {

		tcCreateCounter = -1;
		tcStartCounter = -1;
		mapCounter = 0;
		mapValueCounter = 0;

		tcStopCounter = -1;

		waitForTcStopValues = false;
		waitForDefStatement = false;
		waitForStatementBlock = false;
		waitForValue = false;
		waitForRunsOnValue = false;
		waitForReturnType = false;
		waitForReturnStatementValue = false;
		waitForReceiveStatement = false;
		checkAnyport = false;
		waitForUnknownStartStatement = false;
		waitForTypedParam = false;
		waitForInt2StrExpression = false;
		waitForTC = false;

		waitForAssignmentIdentifiers = false;
		waitForMapIdentifiers = false;
		waitForTcIfCondition = false;
		isSendStatement = false;
		waitForTcStartParameter = false;
		isAltGuards = false;
		waitForAltStatements = false;
		isReceiveValue = false;
		waitForAltTimeout = false;
		isNextValueConstant = false;
		waitForTcDisconnectValues = false;
		waitForTcCreateValues = false;
		waitForTcConnectValues = false;
		waitForTcStartValues = false;
		waitForFunction = false;

		waitForStatements = false;
		waitForValue = false;
		isAnyValValue = false;
		waitForReceiveAnyValTemplateValue = false;

		nodeVars.clear();
		nodeVarTypes.clear();
		nodeVarValues.clear();
		nodeVarIsAValueReference.clear();
		nodeVarTypeIsAReference.clear();
		nodeAssignIdentifiers.clear();
		nodeVarIsConstant.clear();
		nodeVarIsTemplate.clear();
		nodeAssignValues.clear();
		testCaseCreateValues.clear();
		testCaseCreateCounter.clear();
		testCaseCreateRefValues.clear();
		nodeSendParameter.clear();
		testCaseCreateCharValues.clear();
		testCaseIfConditions.clear();
		nodeSendPortReference.clear();
		nodeSendParameterType.clear();
		altGuardConditions.clear();
		altGuardPortReference.clear();
		altGuardReceiveValue.clear();
		altGuardReceiveAnyValValue.clear();
		altGuardReceiveType.clear();
		altGuardTimeout.clear();
		testCaseDisconnectValues.clear();
		testCaseStartValues.clear();
		testCaseMapValues.clear();
		testCaseMapCounter.clear();
		testCaseStartCounter.clear();
		testCaseStartValueParameters.clear();
		testCaseConnectValues.clear();
		operatorList.clear();

		doWhileExpressions.clear();
		
		testCaseStopValues.clear();
		testCaseStopValueParameters.clear();
		testCaseStopCounter.clear();

		receiveStatements.clear();
		receivePortReference.clear();
		receiveValue.clear();
		receiveAnyValValue.clear();
		receiveType.clear();

		nodeVarRecordValues.clear();
		nodeVarIsRecord.clear();
		
		unknownStartReference.clear();
		unknownStopReference.clear();
		

		isIf = false;
		isDefinition = false;
		isAssignment = false;

	}
}
