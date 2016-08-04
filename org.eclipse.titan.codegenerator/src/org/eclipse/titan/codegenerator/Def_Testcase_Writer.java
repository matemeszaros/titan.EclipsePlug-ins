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

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Assignment_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Connect_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Definition_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Disconnect_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Done_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Map_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Send_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Setverdict_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Start_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Stop_Statement;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Verdict_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AddExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ComponentCreateExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.DivideExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ModuloExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.MultiplyExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.RemainderExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.SubstractExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.UnaryMinusExpression;

public class Def_Testcase_Writer {
	private Def_Testcase testCaseNode;
	private StringBuilder testCaseString = new StringBuilder("");
	/*
	 * private CompilationTimeStamp compilationCounter = CompilationTimeStamp
	 * .getNewCompilationCounter();
	 */

	public String nodeName = null;

	private static Map<String, Object> testcaseHashes = new LinkedHashMap<String, Object>();
	public String testCaseRunsOn = null;
	private String currentTimerName = null;

	public StatementBlock tcMainStatementBlock;

	public int sendCounter = -1;
	public int assignCounter = -1;
	private int createCounter = -1;
	private int startStatementCounter = -1;
	private int stopStatementCounter = -1;
	private int connectCounter = -1;
	private int currentCounterValue = 0;
	private int logCreateCounter = 0;
	private int currentMapValueIndex = 0;
	private int defCounter = -1;
	private int defValueCounter = -1;
	private int mapCounter = 0;
	private int tcIfConditionCounter = -1;

	private int disconnectCounter = -1;
	private int altStatementCounter = -1;

	private boolean blockWriter = false;
	private boolean isThereAStartedTimer = false;

	public static List<String> sendPortReference = new ArrayList<String>();
	public static List<String> sendParameter = new ArrayList<String>();
	public static List<String> sendParameterType = new ArrayList<String>();
	public List<String> tcVars = new ArrayList<String>();
	public List<String> tcVarTypes = new ArrayList<String>();
	public List<String> tcVarValues = new ArrayList<String>();
	public List<String> tcAssignIdentifiers = new ArrayList<String>();
	public List<String> tcAssignValues = new ArrayList<String>();
	public List<String> tcCreateValues = new ArrayList<String>();
	public List<String> tcCreateCounter = new ArrayList<String>();
	public List<String> tcConnectValues = new ArrayList<String>();
	public List<String> tcStartIdentifiers = new ArrayList<String>();
	public List<String> tcStartCounter = new ArrayList<String>();
	public List<String> tcMapValues = new ArrayList<String>();
	public List<String> tcMapCounter = new ArrayList<String>();
	public List<String> tcIfConditions = new ArrayList<String>();
	public List<String> testCaseStartValueParameters = new ArrayList<String>();
	public List<String> tcDisconnectValues = new ArrayList<String>();
	public List<Boolean> tcVarIsConstant = new ArrayList<Boolean>();
	public List<Boolean> tcVarIsTemplate = new ArrayList<Boolean>();
	public List<Boolean> tcValueIsAValueReference = new ArrayList<Boolean>();
	public List<Boolean> tcValueTypeIsAReference = new ArrayList<Boolean>();

	public List<String> tcStopIdentifiers = new ArrayList<String>();
	public List<String> tcStopCounter = new ArrayList<String>();
	public List<String> testCaseStopValueParameters = new ArrayList<String>();

	public List<Def_AltStatement_Writer> altStatements = new ArrayList<Def_AltStatement_Writer>();

	public int receiveCounter = -1;

	public List<String> receivePortReference = new ArrayList<String>();
	public List<String> receiveValue = new ArrayList<String>();
	public List<String> receiveAnyValValue = new ArrayList<String>();
	public List<String> receiveType = new ArrayList<String>();
	public List<StatementBlock> receiveStatements = new ArrayList<StatementBlock>();

	public List<Boolean> nodeVarIsRecord = new ArrayList<Boolean>();

	public int structSize = 0;
	public int currentRecordCounter = 0;
	private boolean blockStatementBlockStatementWriter = false;

	private Def_Testcase_Writer(Def_Testcase typeNode) {
		super();
		this.testCaseNode = typeNode;
		nodeName = typeNode.getIdentifier().toString();

	}

	public static Def_Testcase_Writer getInstance(Def_Testcase typeNode) {
		if (!testcaseHashes.containsKey(typeNode.getIdentifier().toString())) {
			testcaseHashes.put(typeNode.getIdentifier().toString(),
					new Def_Testcase_Writer(typeNode));
		}
		return (Def_Testcase_Writer) testcaseHashes.get(typeNode
				.getIdentifier().toString());
	}

	public String writeTestcaseFile(Def_Testcase testNode) {
		StringBuilder testCaseString = new StringBuilder("\r\n");

		testCaseString.append("public class " + nodeName
				+ " implements Runnable{ " + "\r\n");
		testCaseString.append("	" + "\r\n");
		testCaseString.append("	private " + testCaseRunsOn + " component;"
				+ "\r\n");
		testCaseString.append("		public " + nodeName + "(" + testCaseRunsOn
				+ " c){" + "\r\n");
		testCaseString.append("	component = c;" + "\r\n");
		testCaseString.append("	}" + "\r\n");
		testCaseString.append("		public void run(){" + "\r\n");
		testCaseString.append("			component." + nodeName + "();" + "\r\n");
		testCaseString.append("			component.hc.finished(component, \""
				+ nodeName + "\");" + "\r\n");
		testCaseString.append("		}" + "\r\n");
		testCaseString.append("	}" + "\r\n");

		return testCaseString.toString();
	}

	public void writeTestCaseFunctionHeader(StatementBlock tcStatementBlock) {
		testCaseString.append("\r\n");
		testCaseString.append("public void " + nodeName + "(){" + "\r\n");

		testCaseString.append("String sourcefilename = \""
				+ testCaseNode.getLocation().getFile().getFullPath()
						.lastSegment() + "\";" + "\r\n");
		testCaseString.append("int rownum="
				+ tcStatementBlock.getLocation().getLine() + ";\r\n");
		testCaseString.append("while(!created);" + "\r\n");

		testCaseString
				.append("TTCN3Logger.writeLog(\"mtc\", \"PARALLEL\", sourcefilename, rownum, \"testcase\", \""
						+ nodeName
						+ "\", \"Testcase started on mtc\", false);"
						+ "\r\n");
	}

	public String getJavaSource() {
		testCaseString.setLength(0);
		AstWalkerJava.logToConsole("	Starting processing:  Testcase "
				+ nodeName);

		this.writeTestCaseFunctionHeader(tcMainStatementBlock);

		this.writeTestCaseFunction(tcMainStatementBlock);

		String returnString = testCaseString.toString();
		testCaseString.setLength(0);

		AstWalkerJava.logToConsole("	Finished processing:  Testcase "
				+ nodeName);

		return returnString;
	}

	public String writeTestCaseFunction(StatementBlock tcStatementBlock) {

		int testcaseSize = tcStatementBlock.getSize();

		for (int j = 0; j < testcaseSize; j++) {

			if (tcStatementBlock.getStatementByIndex(j) instanceof Definition_Statement) {

				Definition_Statement tc_defStatement = (Definition_Statement) tcStatementBlock
						.getStatementByIndex(j);
				defCounter++;
				defValueCounter++;
				writeDefinitionStatement(tc_defStatement);

			} else if (tcStatementBlock.getStatementByIndex(j) instanceof Assignment_Statement) {
				Assignment_Statement tc_assignStatement = (Assignment_Statement) tcStatementBlock
						.getStatementByIndex(j);

				assignCounter++;

				testCaseString
						.append(writeAssignmentStatement(tc_assignStatement));

			} else if (tcStatementBlock.getStatementByIndex(j) instanceof Connect_Statement) {
				Connect_Statement tc_connectStatement = (Connect_Statement) tcStatementBlock
						.getStatementByIndex(j);
				connectCounter++;

				writeConnectStatement(tc_connectStatement);

			} else if (tcStatementBlock.getStatementByIndex(j) instanceof Unknown_Start_Statement) {
				Unknown_Start_Statement tc_startStatement = (Unknown_Start_Statement) tcStatementBlock
						.getStatementByIndex(j);

				writeUnknownStartStatement(tc_startStatement);

			} else if (tcStatementBlock.getStatementByIndex(j) instanceof Done_Statement) {

				testCaseString.append("hc.done(\"all component\");" + "\r\n");
				// TODO: where is all coming from?

			} else if (tcStatementBlock.getStatementByIndex(j) instanceof Map_Statement) {
				Map_Statement tc_mapStatement = (Map_Statement) tcStatementBlock
						.getStatementByIndex(j);
				mapCounter++;

				writeMapStatement(tc_mapStatement);

			} else if (tcStatementBlock.getStatementByIndex(j) instanceof If_Statement) {
				If_Statement tc_ifStatement = (If_Statement) tcStatementBlock
						.getStatementByIndex(j);

				tcIfConditionCounter++;

				writeIfStatement(tc_ifStatement);

			} else if (tcStatementBlock.getStatementByIndex(j) instanceof Setverdict_Statement) {

				Setverdict_Statement tc_setVerdictStatement = (Setverdict_Statement) tcStatementBlock
						.getStatementByIndex(j);

				writeSetVerdictStatement(tc_setVerdictStatement);

			} else if (tcStatementBlock.getStatementByIndex(j) instanceof Send_Statement) {
				Send_Statement tc_SendStatement = (Send_Statement) tcStatementBlock
						.getStatementByIndex(j);

				sendCounter++;

				testCaseString.append(writeSendStatement(tc_SendStatement));

			} else if (tcStatementBlock.getStatementByIndex(j) instanceof Receive_Port_Statement) {
				Receive_Port_Statement tc_ReceiveStatement = (Receive_Port_Statement) tcStatementBlock
						.getStatementByIndex(j);

				receiveCounter++;

				blockStatementBlockStatementWriter = true;

				testCaseString
						.append(writeReceiveStatement(tc_ReceiveStatement));

			} else if (tcStatementBlock.getStatementByIndex(j) instanceof Unknown_Stop_Statement) {
				Unknown_Stop_Statement tc_StopStatement = (Unknown_Stop_Statement) tcStatementBlock
						.getStatementByIndex(j);

				writeUnknownStopStatement(tc_StopStatement);

			} else if (tcStatementBlock.getStatementByIndex(j) instanceof Alt_Statement) {

				altStatementCounter++;
				altStatements.get(altStatementCounter).setTimerInfo(
						isThereAStartedTimer, currentTimerName);

				testCaseString.append(altStatements.get(altStatementCounter)
						.getJavaSource());

			} else if (tcStatementBlock.getStatementByIndex(j) instanceof Disconnect_Statement) {
				Disconnect_Statement tc_disconnectStatement = (Disconnect_Statement) tcStatementBlock
						.getStatementByIndex(j);
				disconnectCounter++;

				writeDisconnectStatement(tc_disconnectStatement);

			} else if ((tcStatementBlock.getStatementByIndex(j) instanceof StatementBlock_Statement)
					&& !blockStatementBlockStatementWriter) {
				StatementBlock_Statement tc_statementBlockStatement = (StatementBlock_Statement) tcStatementBlock
						.getStatementByIndex(j);
				blockStatementBlockStatementWriter = false;
				testCaseString.append("{\r\n");
				blockWriter = true;
				writeTestCaseFunction(tc_statementBlockStatement
						.getStatementBlock());
				blockWriter = false;
				testCaseString.append("}\r\n");

			}
		}

		if (!blockWriter) {
			testCaseString.append("}" + "\r\n");
		}

		return testCaseString.toString();
	}

	public void writeDefinitionStatement(Definition_Statement tc_defStatement) {
		if (tc_defStatement.getDefinition() instanceof Def_Timer) {
			Def_Timer def_Timer = (Def_Timer) tc_defStatement.getDefinition();

			testCaseString.append("rownum=" + def_Timer.getLocation().getLine()
					+ ";\r\n");
			testCaseString.append("Timer "
					+ def_Timer.getIdentifier().toString()
					+ " = new Timer (new FLOAT(" + tcVarValues.get(defCounter)
					+ "));\r\n");

			testCaseString
					.append("TTCN3Logger.writeLog(\"mtc\", \"TIMEROP\", sourcefilename, rownum, \"function\", \""
							+ nodeName
							+ "\", \"Timer "
							+ def_Timer.getIdentifier().toString()
							+ " set to "
							+ tcVarValues.get(defCounter) + ".\", false);" + "\r\n");

		} else if (tcVarTypes.get(defCounter).equals("BITSTRING")) {

			testCaseString.append("rownum="
					+ tc_defStatement.getLocation().getLine() + ";\r\n");

			if (tcVarIsConstant.get(defCounter)) {
				testCaseString.append("final ");
			}

			if (tcVarIsTemplate.get(defCounter)) {
				testCaseString.append("template ");
			}

			if (tcVarValues.get(defValueCounter) == null) {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "=new BITSTRING();\r\n");
				// TODO: add logging here
			} else if (tcValueIsAValueReference.get(defCounter)) {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "="
						+ tcVarValues.get(defValueCounter) + ";\r\n");
				// TODO: add logging here
			} else {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "=new BITSTRING(\""
						+ tcVarValues.get(defValueCounter) + "\");\r\n");
				// TODO: add logging here
			}

		} else if (tcVarTypes.get(defCounter).equals("INTEGER")) {

			testCaseString.append("rownum="
					+ tc_defStatement.getLocation().getLine() + ";\r\n");
			if (tcVarIsConstant.get(defCounter)) {
				testCaseString.append("final ");
			}

			if (tcVarIsTemplate.get(defCounter)) {
				testCaseString.append("template ");
			}

			if (tcVarValues.get(defValueCounter) == null) {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "=new INTEGER();\r\n");
				// TODO: add logging here
			} else if (tcValueIsAValueReference.get(defCounter)) {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "="
						+ tcVarValues.get(defValueCounter) + ";\r\n");
				// TODO: add logging here
			} else {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter)
						+ "=new INTEGER(new BigInteger(\""
						+ tcVarValues.get(defValueCounter) + "\"));\r\n");
				// TODO: add logging here
			}

		} else if (tcVarTypes.get(defCounter).equals("CHARSTRING")) {

			testCaseString.append("rownum="
					+ tc_defStatement.getLocation().getLine() + ";\r\n");

			if (tcVarIsConstant.get(defCounter)) {
				testCaseString.append("final ");
			}

			if (tcVarIsTemplate.get(defCounter)) {
				testCaseString.append("template ");
			}

			if (tcVarValues.get(defValueCounter) == null) {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "=new CHARSTRING();\r\n");
				// TODO: add logging here
			} else if (tcValueIsAValueReference.get(defCounter)) {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "="
						+ tcVarValues.get(defValueCounter) + ";\r\n");
				// TODO: add logging here
			} else {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "=new CHARSTRING(\""
						+ tcVarValues.get(defValueCounter) + "\");\r\n");
				// TODO: add logging here
			}

		} else if (tcVarTypes.get(defCounter).equals("OCTETSTRING")) {
			testCaseString
					.append("rownum = ")
					.append(tc_defStatement.getLocation().getLine())
					.append(";")
					.append("\r\n");
			// TODO : replace each "\r\n" with System.lineSeparator() to make it cross-platform

			if (tcVarIsConstant.get(defCounter)) {
				testCaseString.append("final ");
			}

			if (tcVarIsTemplate.get(defCounter)) {
				testCaseString.append("template ");
			}

			if (tcVarValues.get(defValueCounter) == null) {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "=new OCTETSTRING();\r\n");
				// TODO: add logging here
			} else if (tcValueIsAValueReference.get(defCounter)) {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "="
						+ tcVarValues.get(defValueCounter) + ";\r\n");
				// TODO: add logging here
			} else {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "=new OCTETSTRING(\""
						+ tcVarValues.get(defValueCounter) + "\");\r\n");
				// TODO: add logging here
			}

		} else if (tcVarTypes.get(defCounter).equals("BOOLEAN")) {

			testCaseString.append("rownum="
					+ tc_defStatement.getLocation().getLine() + ";\r\n");

			if (tcVarIsConstant.get(defCounter)) {
				testCaseString.append("final ");
			}

			if (tcVarIsTemplate.get(defCounter)) {
				testCaseString.append("template ");
			}

			if (tcVarValues.get(defValueCounter) == null) {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "=new BOOLEAN();\r\n");
				// TODO: add logging here
			} else if (tcValueIsAValueReference.get(defCounter)) {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "="
						+ tcVarValues.get(defValueCounter) + ";\r\n");
				// TODO: add logging here
			} else {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "=new BOOLEAN("
						+ tcVarValues.get(defValueCounter) + ");\r\n");
				// TODO: add logging here
			}

		} else if (nodeVarIsRecord.get(defCounter)) {

			testCaseString.append("rownum="
					+ tc_defStatement.getLocation().getLine() + ";\r\n");

			testCaseString.append(tcVarTypes.get(defCounter) + " "
					+ tcVars.get(defCounter) + "= new "
					+ tcVarTypes.get(defCounter) + "();\r\n");

			int childSize = myASTVisitor.nodeNameChildrenNamesHashMap
					.get(tcVarTypes.get(defCounter)).length;

			for (int i = 0; i < childSize; i++) {

				String childType = myASTVisitor.nodeNameChildrenTypesHashMap
						.get(tcVarTypes.get(defCounter))[i];
				String childName = myASTVisitor.nodeNameChildrenNamesHashMap
						.get(tcVarTypes.get(defCounter))[i];

				testCaseString.append("rownum="
						+ tc_defStatement.getLocation().getLine() + ";\r\n");

				writeRecordChildren(childType, childName);

				if (i + 1 < childSize) {
					defValueCounter++;
				}
			}

		} else if (tcValueTypeIsAReference.get(defCounter)) {
			testCaseString.append("rownum="
					+ tc_defStatement.getLocation().getLine() + ";\r\n");

			if (tcVarIsConstant.get(defCounter)) {
				testCaseString.append("final ");
			}

			if (tcVarIsTemplate.get(defCounter)) {
				testCaseString.append("template ");
			}

			if (tcVarValues.get(defValueCounter) == null) {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + ";\r\n");
				// TODO: add logging here
			} else if (tcValueIsAValueReference.get(defCounter)) {

				if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(tcVarTypes
						.get(defCounter))) {
					if (myASTVisitor.nodeNameNodeTypeHashMap.get(
							tcVarTypes.get(defCounter)).equals("enum")) {
						testCaseString
								.append(tcVarTypes.get(defCounter) + " "
										+ tcVars.get(defCounter) + "= new "
										+ tcVarTypes.get(defCounter) + "(\""
										+ tcVarValues.get(defValueCounter)
										+ "\");\r\n");
					}
				} else {

					testCaseString.append(tcVarTypes.get(defCounter) + " "
							+ tcVars.get(defCounter) + "="
							+ tcVarValues.get(defValueCounter) + ";\r\n");
				}
				// TODO: add logging here
			} else {
				testCaseString.append(tcVarTypes.get(defCounter) + " "
						+ tcVars.get(defCounter) + "="
						+ tcVarValues.get(defValueCounter) + ";\r\n");
				// TODO: add logging here
			}
		}

	}

	public void writeRecordChildren(String childType, String childName) {

		if (childType.equals("INTEGER")) {
			testCaseString.append(tcVars.get(defCounter) + "." + childName
					+ "="
					+ tcVarValues.get(defValueCounter) + ";\r\n");
		} else if (childType.equals("CHARSTRING")) {

		} else if (childType.equals("BITSTRING")) {

		} else if (childType.equals("BOOLEAN")) {

		}
	}

	public String writeAssignmentStatement(
			Assignment_Statement tc_assignStatement) {

		StringBuilder testCaseString = new StringBuilder("");

		if (tc_assignStatement.getTemplate() instanceof SpecificValue_Template) {
			SpecificValue_Template specValTemplate = (SpecificValue_Template) tc_assignStatement
					.getTemplate();
			if (specValTemplate.getSpecificValue() instanceof ComponentCreateExpression) {
				ComponentCreateExpression componenetCreateExp = (ComponentCreateExpression) specValTemplate
						.getSpecificValue();

				createCounter++;
				logCreateCounter++;
				int logSizeValue = 1;
				while (tcCreateCounter.get(logCreateCounter).equals(
						String.valueOf(createCounter))) {
					logCreateCounter++;
					logSizeValue++;
					if (tcCreateCounter.size() == (logCreateCounter)) {
						break;
					}

				}
				String[] logValues = new String[logSizeValue];
				int logWriteCounter = 0;
				testCaseString
						.append("rownum="
								+ componenetCreateExp.getLocation().getLine()
								+ ";\r\n");
				testCaseString.append("hc.create(" + "\""
						+ tcCreateValues.get(currentCounterValue) + "\"");
				logValues[logWriteCounter] = tcCreateValues
						.get(currentCounterValue);
				currentCounterValue++;

				logWriteCounter++;
				while (tcCreateCounter.get(currentCounterValue).equals(
						String.valueOf(createCounter))) {
					testCaseString.append(",\""
							+ tcCreateValues.get(currentCounterValue) + "\"");

					logValues[logWriteCounter] = tcCreateValues
							.get(currentCounterValue);
					logWriteCounter++;
					currentCounterValue++;

					if (tcCreateCounter.size() == (currentCounterValue)) {
						break;
					}

				}
				testCaseString.append("); " + "\r\n");

				testCaseString
						.append("TTCN3Logger.writeLog(\"mtc\", \"PARALLEL\", sourcefilename, rownum, \"testcase\", \""
								+ nodeName
								+ "\", \"Starting PTC "
								+ logValues[0]
								+ " type "
								+ logValues[1]
								+ " on "
								+ logValues[2]
								+ "\", false);"
								+ "\r\n");

			} else if ((specValTemplate.getSpecificValue() instanceof Bitstring_Value)
					|| (specValTemplate.getSpecificValue() instanceof Integer_Value)
					|| (specValTemplate.getSpecificValue() instanceof Charstring_Value)
					|| (specValTemplate.getSpecificValue() instanceof Boolean_Value)
					||(specValTemplate.getSpecificValue() instanceof Octetstring_Value)
					|| (specValTemplate.getSpecificValue() instanceof Undefined_LowerIdentifier_Value)
					|| (specValTemplate.getSpecificValue() instanceof Referenced_Value)
					|| (specValTemplate.getSpecificValue() instanceof AddExpression)
					|| (specValTemplate.getSpecificValue() instanceof SubstractExpression)
					|| (specValTemplate.getSpecificValue() instanceof MultiplyExpression)
					|| (specValTemplate.getSpecificValue() instanceof DivideExpression)
					|| (specValTemplate.getSpecificValue() instanceof ModuloExpression)
					|| (specValTemplate.getSpecificValue() instanceof RemainderExpression)
					||(specValTemplate.getSpecificValue() instanceof UnaryMinusExpression)) {
				// TODO assignments for indexed bitstrings
				testCaseString.append("rownum="
						+ specValTemplate.getLocation().getLine() + ";\r\n");


				testCaseString.append(tcAssignIdentifiers.get(assignCounter)
						 + tcAssignValues.get(assignCounter) + ";\r\n");

				

				// TODO: add logging here
			}

		}

		return testCaseString.toString();
	}

	public void writeConnectStatement(Connect_Statement tc_connectStatement) {
		testCaseString.append("rownum="
				+ tc_connectStatement.getLocation().getLine() + ";\r\n");

		testCaseString.append("hc.connect(" + "\""
				+ tcConnectValues.get(connectCounter * 4) + "\"," + "\""
				+ tcConnectValues.get(connectCounter * 4 + 1) + "\"," + "\""
				+ tcConnectValues.get(connectCounter * 4 + 2) + "\"," + "\""
				+ tcConnectValues.get(connectCounter * 4 + 3) + "\"); "
				+ "\r\n");

		testCaseString
				.append("TTCN3Logger.writeLog(\"mtc\", \"PARALLEL\", sourcefilename, rownum, \"testcase\", \""
						+ nodeName
						+ "\", \"Connecting port "
						+ tcConnectValues.get(connectCounter * 4 + 1)
						+ " of "
						+ tcConnectValues.get(connectCounter * 4)
						+ " to port "
						+ tcConnectValues.get(connectCounter * 4 + 3)
						+ " of "
						+ tcConnectValues.get(connectCounter * 4 + 2)
						+ "\", false);" + "\r\n");

	}

	public void writeUnknownStartStatement(
			Unknown_Start_Statement tc_startStatement) {
		startStatementCounter++;
		testCaseString.append("rownum="
				+ tc_startStatement.getLocation().getLine() + ";\r\n");

		if (testCaseStartValueParameters.get(startStatementCounter) != null) {
			testCaseString.append("hc.start(" + "\""
					+ tcStartIdentifiers.get(startStatementCounter) + "\",\""
					+ testCaseStartValueParameters.get(startStatementCounter)
					+ "\"); " + "\r\n");
		} else {
			testCaseString.append(tcStartIdentifiers.get(startStatementCounter)
					+ ".start()" + ";\r\n");
		}
		if (testCaseStartValueParameters.get(startStatementCounter) != null) {
			testCaseString
					.append("TTCN3Logger.writeLog(\"mtc\", \"PARALLEL\", sourcefilename, rownum, \"testcase\", \""
							+ nodeName
							+ "\", \"Starting function "
							+ testCaseStartValueParameters
									.get(startStatementCounter)
							+ " on component "
							+ tcStartIdentifiers.get(startStatementCounter)
							+ "\", false);" + "\r\n");
		} else {// timer
			testCaseString
					.append("TTCN3Logger.writeLog(\"mtc\", \"TIMEROP\", sourcefilename, rownum, \"function\", \""
							+ nodeName
							+ "\", \"Timer "
							+ tcStartIdentifiers.get(startStatementCounter)
							+ " started.\", false);" + "\r\n");
			isThereAStartedTimer = true;
			currentTimerName = tcStartIdentifiers.get(startStatementCounter);

		}

	}

	public void writeUnknownStopStatement(
			Unknown_Stop_Statement tc_stopStatement) {
		stopStatementCounter++;
		testCaseString.append("rownum="
				+ tc_stopStatement.getLocation().getLine() + ";\r\n");

		if (testCaseStopValueParameters.get(stopStatementCounter) != null) {
			testCaseString.append("hc.stop(" + "\""
					+ tcStopIdentifiers.get(stopStatementCounter) + "\",\""
					+ testCaseStopValueParameters.get(stopStatementCounter)
					+ "\"); " + "\r\n");
		} else {
			testCaseString.append(tcStopIdentifiers.get(stopStatementCounter)
					+ ".stop()" + ";\r\n");
		}
		if (testCaseStopValueParameters.get(stopStatementCounter) != null) {
			testCaseString
					.append("TTCN3Logger.writeLog(\"mtc\", \"PARALLEL\", sourcefilename, rownum, \"testcase\", \""
							+ nodeName
							+ "\", \"Stopping function "
							+ testCaseStopValueParameters
									.get(stopStatementCounter)
							+ " on component "
							+ tcStopIdentifiers.get(stopStatementCounter)
							+ "\", false);" + "\r\n");
		} else {// timer
			testCaseString
					.append("TTCN3Logger.writeLog(\"mtc\", \"TIMEROP\", sourcefilename, rownum, \"function\", \""
							+ nodeName
							+ "\", \"Timer "
							+ tcStopIdentifiers.get(stopStatementCounter)
							+ " stopped.\", false);" + "\r\n");
			isThereAStartedTimer = true;
			currentTimerName = tcStopIdentifiers.get(stopStatementCounter);

		}

	}

	public void writeMapStatement(Map_Statement tc_mapStatement) {
		StringBuilder mapValueString = new StringBuilder();

		while (Integer.toString(mapCounter).equals(
				tcMapCounter.get(currentMapValueIndex))) {

			mapValueString.append(tcMapValues.get(currentMapValueIndex) + " ");

			currentMapValueIndex++;
			if (currentMapValueIndex == tcMapCounter.size()) {
				break;
			}

		}

		String[] mapValues = mapValueString.toString().split(" ");

		testCaseString.append("rownum="
				+ tc_mapStatement.getLocation().getLine() + ";\r\n");
		testCaseString.append("hc.map(" + "\"" + mapValues[0] + "\",\""
				+ mapValues[1] + "\",\"" + mapValues[2] + "\",\""
				+ mapValues[3] + "\"" + ");\r\n");

		testCaseString
				.append("TTCN3Logger.writeLog(\"mtc\", \"PARALLEL\", sourcefilename, rownum, \"testcase\", \""
						+ nodeName
						+ "\", \"Mapping port "
						+ mapValues[1]
						+ " of "
						+ mapValues[0]
						+ " to port "
						+ mapValues[3]
						+ " of " + mapValues[2] + "\", false);" + "\r\n");
	}

	public void writeIfStatement(If_Statement tc_ifStatement) {
		testCaseString.append("rownum="
				+ tc_ifStatement.getLocation().getLine() + ";\r\n");

		testCaseString.append("if(" + tcIfConditions.get(tcIfConditionCounter)
				+ ".getValue()){\r\n");

		// TODO check if several IfClauses are possible
		writeTestCaseFunction(tc_ifStatement.getIfClauses().getClauses().get(0)
				.getStatementBlock());
		if (blockWriter) {
			testCaseString.append("}" + "\r\n");
		}

		if (tc_ifStatement.getStatementBlock() != null) {
			testCaseString.append("else{\r\n");
			writeTestCaseFunction(tc_ifStatement.getStatementBlock());
			if (blockWriter) {
				testCaseString.append("}" + "\r\n");
			}

		}
	}

	public void writeSetVerdictStatement(
			Setverdict_Statement tc_setVerdictStatement) {
		Verdict_Value tc_VerdictValue = (Verdict_Value) tc_setVerdictStatement
				.getVerdictValue();

		testCaseString.append("rownum="
				+ tc_setVerdictStatement.getLocation().getLine() + ";\r\n");

		if (tc_VerdictValue.getValue().toString().equals("PASS")) {
			testCaseString.append("setVerdict(\"pass\")" + ";\r\n");
		} else if (tc_VerdictValue.getValue().toString().equals("FAIL")) {
			testCaseString.append("setVerdict(\"fail\")" + ";\r\n");
		}

	}

	public String writeSendStatement(Send_Statement tc_SendStatement) {

		StringBuilder testCaseString = new StringBuilder("");

		if (sendParameterType.get(sendCounter).equals("INTEGER")) {
			String parameterValue =sendParameter.get(sendCounter);
			testCaseString.append("rownum="
					+ tc_SendStatement.getLocation().getLine() + ";\r\n");
			testCaseString.append(sendPortReference.get(sendCounter) + ".send("
					+ parameterValue + ");" + "\r\n");
			testCaseString
					.append("TTCN3Logger.writeLog(\"mtc\", \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
							+ nodeName
							+ "\", \"SEND event on port "
							+ sendPortReference.get(sendCounter)
							+ ":INTEGER \""
							+ " + "
							+ parameterValue
							+ ".toString(), false);" + "\r\n");

		}
		if (sendParameterType.get(sendCounter).equals("IDENTIFIER")) {
			String parameterValue = sendParameter.get(sendCounter);
			testCaseString.append("rownum="
					+ tc_SendStatement.getLocation().getLine() + ";\r\n");
			testCaseString.append(sendPortReference.get(sendCounter) + ".send("
					+ parameterValue + ");" + "\r\n");
			testCaseString
					.append("TTCN3Logger.writeLog(\"mtc\", \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
							+ nodeName
							+ "\", \"SEND event on port "
							+ sendPortReference.get(sendCounter)
							+ ": \""
							+ " + "
							+ parameterValue
							+ ".toString(), false);"
							+ "\r\n");

		}

		return testCaseString.toString();
	}

	public String writeReceiveStatement(Receive_Port_Statement currentStatement) {
		StringBuilder testCaseString = new StringBuilder("");
		String receiveStatement = "";

		// no alt guard && any port recieve
		if (receivePortReference.get(receiveCounter).equals("any port")) {
			receiveStatement = "anyPortReceive(true)";

		} else {
			// no alt guard && typed port recieve
			if (receiveType.get(receiveCounter).equals("_TYPED_PARAM_")) {

				if (receiveAnyValValue.get(receiveCounter) != null) {
					receiveStatement = "("
							+ receiveAnyValValue.get(receiveCounter) + "="
							+ receivePortReference.get(receiveCounter)
							+ ".receive_" + receiveValue.get(receiveCounter)
							+ "(true))!=null";

				} else {

				}

			} else { // no alt guard && normal port recieve
				receiveStatement = receivePortReference.get(receiveCounter)
						+ ".receive(" + receiveValue.get(receiveCounter)
						+ ",true)!=null";
			}
			if (receiveValue.get(receiveCounter).startsWith("Templates")) {
				receiveType.set(receiveCounter, "Templates");
			}
		}

		testCaseString.append("if(" + receiveStatement + "){\r\n");
		testCaseString.append("rownum="
				+ currentStatement.getLocation().getLine() + ";\r\n");

		if (receiveType.get(receiveCounter).equals("Templates")) {
			String methodName = receiveValue.get(receiveCounter);
			if (methodName.endsWith("()")
					&& methodName.startsWith("Templates.")) {

				methodName = (String) methodName.subSequence(10,
						methodName.length() - 2);
			}

			testCaseString
					.append("	TTCN3Logger.writeLog(\"mtc\", \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
							+ nodeName
							+ "\", \"RECEIVE event on port "
							+ receivePortReference.get(receiveCounter)
							+ ":\\n "
							+ methodName
							+ ":=\" + "
							+ receiveValue.get(receiveCounter)
							+ ".toString(), true);" + "\r\n");
		} else if (receiveType.get(receiveCounter).equals("any port")) {
			testCaseString
					.append("	TTCN3Logger.writeLog(\"mtc\", \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
							+ nodeName
							+ "\", \"RECEIVE event on port any port\", true);"
							+ "\r\n");
		} else if (receiveType.get(receiveCounter).equals("_TYPED_PARAM_")) {
			testCaseString
					.append("	TTCN3Logger.writeLog(\"mtc\", \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
							+ nodeName
							+ "\", \"RECEIVE event on port "
							+ receivePortReference.get(receiveCounter)
							+ ": type "
							+ receiveValue.get(receiveCounter)
							+ "\", true);" + "\r\n");

		} else {
			testCaseString
					.append("	TTCN3Logger.writeLog(\"mtc\", \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
							+ nodeName
							+ "\", \"RECEIVE event on port "
							+ receivePortReference.get(receiveCounter)
							+ ":\" + "
							+ receiveValue.get(receiveCounter)
							+ ".toString(), true);" + "\r\n");
		}

		for (int i = 0; i < receiveStatements.get(receiveCounter).getSize(); i++) {
			if (receiveStatements.get(receiveCounter).getStatementByIndex(i) instanceof Setverdict_Statement) {

				Setverdict_Statement setVerdictStatement = (Setverdict_Statement) receiveStatements
						.get(receiveCounter).getStatementByIndex(i);
				String verdict = "";

				if (setVerdictStatement.getVerdictValue() instanceof Verdict_Value) {
					Verdict_Value verdictValue = (Verdict_Value) setVerdictStatement
							.getVerdictValue();
					if (verdictValue.getValue().toString().equals("PASS")) {
						verdict = "pass";
					} else if (verdictValue.getValue().toString()
							.equals("INCONC")) {
						verdict = "inconc";
					} else {
						verdict = "fail";
					}
				}
				testCaseString
						.append("rownum="
								+ setVerdictStatement.getLocation().getLine()
								+ ";\r\n");
				testCaseString
						.append("TTCN3Logger.writeLog(\"mtc\", \"VERDICTOP\", sourcefilename, rownum, \"function\", \""
								+ nodeName
								+ "\", \"setverdict("
								+ verdict
								+ "): \" + getVerdict() + \" -> "
								+ verdict
								+ "\", true);" + "\r\n");
				testCaseString.append("setVerdict(\"" + verdict + "\");"
						+ "\r\n");

			}
		}

		testCaseString.append("}\r\n");

		return testCaseString.toString();
	}

	public void writeDisconnectStatement(
			Disconnect_Statement tc_disconnectStatement) {

		testCaseString.append("rownum="
				+ tc_disconnectStatement.getLocation().getLine() + ";\r\n");

		testCaseString.append("hc.disconnect(" + "\""
				+ tcDisconnectValues.get(disconnectCounter * 4) + "\"," + "\""
				+ tcDisconnectValues.get(disconnectCounter * 4 + 1) + "\","
				+ "\"" + tcDisconnectValues.get(disconnectCounter * 4 + 2)
				+ "\"," + "\""
				+ tcDisconnectValues.get(disconnectCounter * 4 + 3) + "\"); "
				+ "\r\n");

		testCaseString
				.append("TTCN3Logger.writeLog(\"mtc\", \"PARALLEL\", sourcefilename, rownum, \"testcase\", \""
						+ nodeName
						+ "\", \"Disconnecting port "
						+ tcDisconnectValues.get(disconnectCounter * 4 + 1)
						+ " of "
						+ tcDisconnectValues.get(disconnectCounter * 4)
						+ " to port "
						+ tcDisconnectValues.get(disconnectCounter * 4 + 3)
						+ " of "
						+ tcDisconnectValues.get(disconnectCounter * 4 + 2)
						+ "\", false);" + "\r\n");
	}

	public void clearLists() {

		logCreateCounter = 0;
		createCounter = -1;
		connectCounter = -1;
		disconnectCounter = -1;
		currentCounterValue = 0;
		defCounter = -1;
		defValueCounter = -1;
		assignCounter = -1;
		mapCounter = 0;
		tcIfConditionCounter = -1;
		sendCounter = -1;
		startStatementCounter = -1;
		stopStatementCounter = -1;
		currentMapValueIndex = 0;
		receiveCounter = -1;

		tcConnectValues.clear();
		tcStartIdentifiers.clear();
		tcStartCounter.clear();
		tcMapValues.clear();
		tcMapCounter.clear();
		tcIfConditions.clear();
		tcVars.clear();
		tcVarTypes.clear();
		tcVarValues.clear();
		tcAssignIdentifiers.clear();
		tcAssignValues.clear();
		tcValueIsAValueReference.clear();
		tcCreateValues.clear();
		tcCreateCounter.clear();
		sendPortReference.clear();
		sendParameter.clear();
		sendParameterType.clear();

		tcVarIsConstant.clear();
		tcVarIsTemplate.clear();
		tcDisconnectValues.clear();
		testCaseStartValueParameters.clear();
		tcValueTypeIsAReference.clear();
		tcStopIdentifiers.clear();
		tcStopCounter.clear();
		testCaseStopValueParameters.clear();

		receivePortReference.clear();
		receiveValue.clear();
		receiveAnyValValue.clear();
		receiveType.clear();
		nodeVarIsRecord.clear();
		receiveStatements.clear();
	}

	public void clearAltLists() {
		altStatementCounter = -1;

		altStatements.clear();
	}

}
