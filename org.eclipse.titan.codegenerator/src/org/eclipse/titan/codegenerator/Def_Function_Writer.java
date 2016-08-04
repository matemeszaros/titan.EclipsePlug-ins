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

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Assignment_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Definition_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Send_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Setverdict_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Start_Statement;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Verdict_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AddExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.DivideExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ModuloExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.MultiplyExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.RemainderExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.SubstractExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.UnaryMinusExpression;

public class Def_Function_Writer {
	private Def_Function functionNode;
	private StringBuilder functionString = new StringBuilder("");

	public String nodeName = null;

	private static Map<String, Object> functionHashes = new LinkedHashMap<String, Object>();

	private StatementBlock statementBlock;
	private boolean isThereAStartedTimer = false;
	private String currentTimerName = null;

	public String returnStatementValue = null;
	public String runsOnValue = null;
	public String returnType = null;

	public String unknownStartReference = null;
	public String unknownStopReference = null;
	public static List<String> sendPortReference = new ArrayList<String>();
	public static List<String> sendParameter = new ArrayList<String>();
	public static List<String> sendParameterType = new ArrayList<String>();
	public int sendCounter = -1;

	public List<Def_AltStatement_Writer> altStatements = new ArrayList<Def_AltStatement_Writer>();
	private int altStatementCounter = -1;

	public List<String> functionVars = new ArrayList<String>();
	public List<String> functionVarTypes = new ArrayList<String>();
	public List<String> functionVarValues = new ArrayList<String>();
	public List<String> functionAssignIdentifiers = new ArrayList<String>();
	public List<String> functionAssignValues = new ArrayList<String>();

	public int receiveCounter = -1;

	public List<String> receivePortReference = new ArrayList<String>();
	public List<String> receiveValue = new ArrayList<String>();
	public List<String> receiveAnyValValue = new ArrayList<String>();
	public List<String> receiveType = new ArrayList<String>();
	public List<StatementBlock> receiveStatements = new ArrayList<StatementBlock>();

	public List<Boolean> functionValueIsAValueReference = new ArrayList<Boolean>();
	public List<Boolean> functionVarIsConstant = new ArrayList<Boolean>();
	private int defCounter = -1;
	public int assignCounter = -1;

	private Def_Function_Writer(Def_Function typeNode) {
		super();
		this.functionNode = typeNode;
		nodeName = functionNode.getIdentifier().toString();
	}

	public static Def_Function_Writer getInstance(Def_Function typeNode) {
		if (!functionHashes.containsKey(typeNode.getIdentifier().toString())) {
			functionHashes.put(typeNode.getIdentifier().toString(), new Def_Function_Writer(typeNode));
		}
		return (Def_Function_Writer) functionHashes.get(typeNode.getIdentifier().toString());
	}

	public void setStatementBlock(StatementBlock statementBlock) {
		this.statementBlock = statementBlock;
	}

	public void writeFunction() {

		if (returnType != null) {
			if (returnType.equals("Integer_Type")) {
				functionString.append("public static INTEGER " + nodeName + "(){\r\n");
			} else if (returnType.equals("Boolean_Type")) {
				functionString.append("public static BOOLEAN " + nodeName + "(){\r\n");
			}
		} else {
			functionString.append("public void " + nodeName + "(){\r\n");

			functionString.append("String sourcefilename = \""
					+ statementBlock.getLocation().getFile().getFullPath().lastSegment() + "\";" + "\r\n");
			functionString.append("int rownum=" + statementBlock.getLocation().getLine() + ";\r\n");
			functionString.append("if(!created) return;" + "\r\n");

			functionString.append("TTCN3Logger.writeLog(compid, \"PARALLEL\", sourcefilename, rownum, \"function\", \""
					+ nodeName + "\", \"Function started on \" + compid + \".\", false);" + "\r\n");

		}

		for (int i = 0; i < statementBlock.getSize(); i++) {

			if (statementBlock.getStatementByIndex(i) instanceof Definition_Statement) {
				writeDefinitionStatement((Definition_Statement) statementBlock.getStatementByIndex(i));
			} else if (statementBlock.getStatementByIndex(i) instanceof Assignment_Statement) {
				assignCounter++;

				functionString
						.append(writeAssignmentStatement((Assignment_Statement) statementBlock.getStatementByIndex(i)));

			} else if (statementBlock.getStatementByIndex(i) instanceof Send_Statement) {
				sendCounter++;
				functionString.append(writeSendStatement((Send_Statement) statementBlock.getStatementByIndex(i)));
			} else if (statementBlock.getStatementByIndex(i) instanceof Unknown_Start_Statement) {
				writeUnknownStartStatement((Unknown_Start_Statement) statementBlock.getStatementByIndex(i));
			} else if (statementBlock.getStatementByIndex(i) instanceof Alt_Statement) {

				altStatementCounter++;

				altStatements.get(altStatementCounter).setTimerInfo(isThereAStartedTimer, currentTimerName);
				functionString.append(altStatements.get(altStatementCounter).getJavaSource());

			} else if (statementBlock.getStatementByIndex(i) instanceof Setverdict_Statement) {
				writeSetVerdictStatement((Setverdict_Statement) statementBlock.getStatementByIndex(i));
			}
		}

		if (returnStatementValue != null) {
			functionString.append("return " + returnStatementValue + ";\r\n");
		}

	}

	// updated
	public void writeDefinitionStatement(Definition_Statement tc_defStatement) {

		defCounter++;
		if (tc_defStatement.getDefinition() instanceof Def_Timer) {
			Def_Timer def_Timer = (Def_Timer) tc_defStatement.getDefinition();

			functionString.append("rownum=" + def_Timer.getLocation().getLine() + ";\r\n");
			functionString.append("Timer " + def_Timer.getIdentifier().toString() + " = new Timer (new FLOAT("
					+ functionVarValues.get(defCounter) + "));\r\n");

			functionString.append("TTCN3Logger.writeLog(compid, \"TIMEROP\", sourcefilename, rownum, \"function\", \""
					+ nodeName + "\", \"Timer " + def_Timer.getIdentifier().toString() + " set to "
					+ functionVarValues.get(defCounter) + ".\", false);" + "\r\n");

		} else if (functionVarTypes.get(defCounter).equals("BITSTRING")) {

			functionString.append("rownum=" + tc_defStatement.getLocation().getLine() + ";\r\n");

			if (functionVarIsConstant.get(defCounter)) {
				functionString.append("final ");
			}

			if (functionVarValues.get(defCounter).equals("null")) {
				functionString.append(functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter)
						+ "=new BITSTRING();\r\n");

			} else if (functionValueIsAValueReference.get(defCounter)) {
				functionString.append(functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter) + "="
						+ functionVarValues.get(defCounter) + ";\r\n");

			} else {
				functionString.append(functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter)
						+ "=new BITSTRING(\"" + functionVarValues.get(defCounter) + "\");\r\n");

			}
			// TODO: add logging here

		} else if (functionVarTypes.get(defCounter).equals("INTEGER")) {

			functionString.append("rownum=" + tc_defStatement.getLocation().getLine() + ";\r\n");
			if (functionVarIsConstant.get(defCounter)) {
				functionString.append("final ");
			}

			if (functionVarValues.get(defCounter).equals("null")) {
				functionString.append(
						functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter) + "=new INTEGER();\r\n");

			} else if (functionValueIsAValueReference.get(defCounter)) {
				functionString.append(functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter) + "="
						+ functionVarValues.get(defCounter) + ";\r\n");

			} else {
				functionString.append(functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter)
						+ "=new INTEGER(new BigInteger(\"" + functionVarValues.get(defCounter) + "\"));\r\n");

			}
			// TODO: add logging here

		} else if (functionVarTypes.get(defCounter).equals("CHARSTRING")) {

			functionString.append("rownum=" + tc_defStatement.getLocation().getLine() + ";\r\n");

			if (functionVarIsConstant.get(defCounter)) {
				functionString.append("final ");
			}

			if (functionVarValues.get(defCounter).equals("null")) {
				functionString.append(functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter)
						+ "=new CHARSTRING();\r\n");

			} else if (functionValueIsAValueReference.get(defCounter)) {
				functionString.append(functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter) + "="
						+ functionVarValues.get(defCounter) + ";\r\n");

			} else {
				functionString.append(functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter)
						+ "=new CHARSTRING(\"" + functionVarValues.get(defCounter) + "\");\r\n");

			}
			// TODO: add logging here

		} else if (functionVarTypes.get(defCounter).equals("BOOLEAN")) {

			functionString.append("rownum=" + tc_defStatement.getLocation().getLine() + ";\r\n");

			if (functionVarIsConstant.get(defCounter)) {
				functionString.append("final ");
			}

			if (functionVarValues.get(defCounter).equals("null")) {
				functionString.append(
						functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter) + "=new BOOLEAN();\r\n");

			} else if (functionValueIsAValueReference.get(defCounter)) {
				functionString.append(functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter) + "="
						+ functionVarValues.get(defCounter) + ";\r\n");

			} else {
				functionString.append(functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter)
						+ "=new BOOLEAN(" + functionVarValues.get(defCounter) + ");\r\n");

			}
			// TODO: add logging here
		} else if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(functionVarTypes.get(defCounter))) {
			functionString.append("rownum=" + tc_defStatement.getLocation().getLine() + ";\r\n");

			if (functionVarValues.get(defCounter) != null) {
				functionString.append(functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter) + "= new "
						+ functionVarTypes.get(defCounter) + "(" + functionVarValues.get(defCounter) + ")" + ";\r\n");
			} else {
				functionString.append(functionVarTypes.get(defCounter) + " " + functionVars.get(defCounter) + ";\r\n");
			}
		}
	}

	// updated
	public String writeAssignmentStatement(Assignment_Statement tc_assignStatement) {

		StringBuilder functionString = new StringBuilder("");

		if (tc_assignStatement.getTemplate() instanceof SpecificValue_Template) {
			SpecificValue_Template specValTemplate = (SpecificValue_Template) tc_assignStatement.getTemplate();

			if (specValTemplate.getSpecificValue() instanceof Bitstring_Value) {

				functionString.append("rownum=" + specValTemplate.getLocation().getLine() + ";\r\n");

				functionString.append(functionAssignIdentifiers.get(assignCounter) + "=new BITSTRING(\""
						+ functionAssignValues.get(assignCounter) + "\");\r\n");

				// TODO: add logging here

			}

			if (specValTemplate.getSpecificValue() instanceof Integer_Value) {

				functionString.append("rownum=" + specValTemplate.getLocation().getLine() + ";\r\n");

				functionString.append(functionAssignIdentifiers.get(assignCounter) + "=new INTEGER(new BigInteger(\""
						+ functionAssignValues.get(assignCounter) + "\"));\r\n");

				// TODO: add logging here

			}

			if (specValTemplate.getSpecificValue() instanceof Charstring_Value) {

				functionString.append("rownum=" + specValTemplate.getLocation().getLine() + ";\r\n");

				functionString.append(functionAssignIdentifiers.get(assignCounter) + "=new CHARSTRING(\""
						+ functionAssignValues.get(assignCounter) + "\");\r\n");

				// TODO: add logging here

			}

			if (specValTemplate.getSpecificValue() instanceof Boolean_Value) {

				functionString.append("rownum=" + specValTemplate.getLocation().getLine() + ";\r\n");

				functionString.append(functionAssignIdentifiers.get(assignCounter) + "=new BOOLEAN("
						+ functionAssignValues.get(assignCounter) + ");\r\n");

				// TODO: add logging here

			}

			if (specValTemplate.getSpecificValue() instanceof Undefined_LowerIdentifier_Value) {

				functionString.append("rownum=" + specValTemplate.getLocation().getLine() + ";\r\n");

				functionString.append(functionAssignIdentifiers.get(assignCounter) + "="
						+ functionAssignValues.get(assignCounter) + ";\r\n");

				// TODO: add logging here
			}

			if (specValTemplate.getSpecificValue() instanceof Referenced_Value) {

				functionString.append("rownum=" + specValTemplate.getLocation().getLine() + ";\r\n");

				functionString.append(functionAssignIdentifiers.get(assignCounter) + "="
						+ functionAssignValues.get(assignCounter) + ";\r\n");

				// TODO: add logging here
			}

			if ((specValTemplate.getSpecificValue() instanceof AddExpression)
					|| (specValTemplate.getSpecificValue() instanceof SubstractExpression)
					|| (specValTemplate.getSpecificValue() instanceof MultiplyExpression)
					|| (specValTemplate.getSpecificValue() instanceof DivideExpression)
					|| (specValTemplate.getSpecificValue() instanceof ModuloExpression)
					|| (specValTemplate.getSpecificValue() instanceof RemainderExpression)) {

				functionString.append("rownum=" + specValTemplate.getLocation().getLine() + ";\r\n");

				functionString.append(functionAssignIdentifiers.get(assignCounter)
						+ functionAssignValues.get(assignCounter) + ";\r\n");

				// TODO: add logging here

			}

			if (specValTemplate.getSpecificValue() instanceof UnaryMinusExpression) {

				functionString.append("rownum=" + specValTemplate.getLocation().getLine() + ";\r\n");

				functionString.append(functionAssignIdentifiers.get(assignCounter) + "=new INTEGER(new BigInteger(\""
						+ functionAssignValues.get(assignCounter) + "\"));\r\n");

				// TODO: add logging here

			}

		}

		return functionString.toString();
	}

	public String writeSendStatement(Send_Statement sendStatement) {

		StringBuilder functionString = new StringBuilder("");

		String portReferenceName = sendPortReference.get(sendCounter);
		String parameterName = null;
		String parameterType = null;
		String valueType = sendParameterType.get(sendCounter);
		String valueName = sendParameter.get(sendCounter);

		if (valueType.equals("IDENTIFIER")) {

			if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(valueName)) {
				parameterType = myASTVisitor.nodeNameNodeTypeHashMap.get(valueName);

				if (parameterType.equals("template")) {
					parameterType = "Templates";
				}
				parameterName = parameterType + "." + valueName + "()";
			} else {
				parameterName = valueName;
			}

			functionString.append("rownum=" + sendStatement.getLocation().getLine() + ";\r\n");
			functionString.append(portReferenceName + ".send(" + parameterName + ");" + "\r\n");
			functionString.append("TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
					+ nodeName + "\", \"SEND event on port " + portReferenceName + ": " + valueName + ":=\" + "
					+ parameterName + ".toString(), true);" + "\r\n");

		}

		if (valueType.equals("CHARSTRING")) {
			parameterName = valueName;
			functionString.append("rownum=" + sendStatement.getLocation().getLine() + ";\r\n");
			functionString.append(portReferenceName + ".send(" + parameterName + ");" + "\r\n");
			functionString.append("TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
					+ nodeName + "\", \"SEND event on port " + portReferenceName + ":\"+" + parameterName
					+ ".toString(), false);" + "\r\n");

		}

		return functionString.toString();

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
					receiveStatement = "(" + receiveAnyValValue.get(receiveCounter) + "="
							+ receivePortReference.get(receiveCounter) + ".receive_" + receiveValue.get(receiveCounter)
							+ "(true))!=null";

				} else {

				}

			} else { // no alt guard && normal port recieve
				receiveStatement = receivePortReference.get(receiveCounter) + ".receive("
						+ receiveValue.get(receiveCounter) + ",true)!=null";
			}
			if (receiveValue.get(receiveCounter).startsWith("Templates")) {
				receiveType.set(receiveCounter, "Templates");
			}
		}

		testCaseString.append("if(" + receiveStatement + "){\r\n");
		testCaseString.append("rownum=" + currentStatement.getLocation().getLine() + ";\r\n");

		if (receiveType.get(receiveCounter).equals("Templates")) {
			String methodName = receiveValue.get(receiveCounter);
			if (methodName.endsWith("()") && methodName.startsWith("Templates.")) {

				methodName = (String) methodName.subSequence(10, methodName.length() - 2);
			}

			testCaseString
					.append("	TTCN3Logger.writeLog(\"mtc\", \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
							+ nodeName + "\", \"RECEIVE event on port " + receivePortReference.get(receiveCounter)
							+ ":\\n " + methodName + ":=\" + " + receiveValue.get(receiveCounter)
							+ ".toString(), true);" + "\r\n");
		} else if (receiveType.get(receiveCounter).equals("any port")) {
			testCaseString
					.append("	TTCN3Logger.writeLog(\"mtc\", \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
							+ nodeName + "\", \"RECEIVE event on port any port\", true);" + "\r\n");
		} else if (receiveType.get(receiveCounter).equals("_TYPED_PARAM_")) {
			testCaseString
					.append("	TTCN3Logger.writeLog(\"mtc\", \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
							+ nodeName + "\", \"RECEIVE event on port " + receivePortReference.get(receiveCounter)
							+ ": type " + receiveValue.get(receiveCounter) + "\", true);" + "\r\n");

		} else {
			testCaseString
					.append("	TTCN3Logger.writeLog(\"mtc\", \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
							+ nodeName + "\", \"RECEIVE event on port " + receivePortReference.get(receiveCounter)
							+ ":\" + " + receiveValue.get(receiveCounter) + ".toString(), true);" + "\r\n");
		}

		for (int i = 0; i < receiveStatements.get(receiveCounter).getSize(); i++) {
			if (receiveStatements.get(receiveCounter).getStatementByIndex(i) instanceof Setverdict_Statement) {

				Setverdict_Statement setVerdictStatement = (Setverdict_Statement) receiveStatements.get(receiveCounter)
						.getStatementByIndex(i);
				String verdict = "";

				if (setVerdictStatement.getVerdictValue() instanceof Verdict_Value) {
					Verdict_Value verdictValue = (Verdict_Value) setVerdictStatement.getVerdictValue();
					if (verdictValue.getValue().toString().equals("PASS")) {
						verdict = "pass";
					} else if (verdictValue.getValue().toString().equals("INCONC")) {
						verdict = "inconc";
					} else {
						verdict = "fail";
					}
				}
				testCaseString.append("rownum=" + setVerdictStatement.getLocation().getLine() + ";\r\n");
				testCaseString
						.append("TTCN3Logger.writeLog(\"mtc\", \"VERDICTOP\", sourcefilename, rownum, \"function\", \""
								+ nodeName + "\", \"setverdict(" + verdict + "): \" + getVerdict() + \" -> " + verdict
								+ "\", true);" + "\r\n");
				testCaseString.append("setVerdict(\"" + verdict + "\");" + "\r\n");

			}
		}

		testCaseString.append("}\r\n");

		return testCaseString.toString();
	}

	// updated
	public void writeUnknownStartStatement(Unknown_Start_Statement unknownStartStatement) {
		functionString.append("rownum=" + unknownStartStatement.getLocation().getLine() + ";\r\n");
		functionString.append(unknownStartReference + ".start();" + "\r\n");
		isThereAStartedTimer = true;
		currentTimerName = unknownStartReference;
		functionString.append("TTCN3Logger.writeLog(compid, \"TIMEROP\", sourcefilename, rownum, \"function\", \""
				+ nodeName + "\", \"Timer " + unknownStartReference + " started.\", false);" + "\r\n");

	}

	public void writeSetVerdictStatement(Setverdict_Statement setVerdictStatement) {

		String verdict = "";

		if (setVerdictStatement.getVerdictValue() instanceof Verdict_Value) {
			Verdict_Value verdictValue = (Verdict_Value) setVerdictStatement.getVerdictValue();
			if (verdictValue.getValue().toString().equals("PASS")) {
				verdict = "pass";
			} else if (verdictValue.getValue().toString().equals("INCONC")) {
				verdict = "inconc";
			} else {
				verdict = "fail";
			}
		}
		functionString.append("rownum=" + setVerdictStatement.getLocation().getLine() + ";\r\n");
		functionString.append("TTCN3Logger.writeLog(compid, \"VERDICTOP\", sourcefilename, rownum, \"function\", \""
				+ nodeName + "\", \"setverdict(" + verdict + "): \" + getVerdict() + \" -> " + verdict + "\", true);"
				+ "\r\n");
		functionString.append("setVerdict(\"" + verdict + "\");" + "\r\n");
	}

	public String writeFunctionFile() {

		StringBuilder functionFileString = new StringBuilder();

		functionFileString.append("class " + nodeName + " implements Runnable{" + "\r\n");
		functionFileString.append("private " + runsOnValue + " component;" + "\r\n");
		functionFileString.append("public " + nodeName + "(" + runsOnValue + " c){" + "\r\n");
		functionFileString.append("component = c;" + "\r\n");
		functionFileString.append("}" + "\r\n");
		functionFileString.append("public void run(){" + "\r\n");
		functionFileString.append("component." + nodeName + "();" + "\r\n");
		functionFileString.append("}" + "\r\n");
		functionFileString.append("}" + "\r\n");

		return functionFileString.toString();
	}

	public String getJavaSource() {
		AstWalkerJava.logToConsole("	Starting processing:  Function " + nodeName);

		sendCounter = -1;

		this.writeFunction();
		functionString.append("\r\n}");
		String returnString = functionString.toString();
		functionString.setLength(0);

		sendPortReference.clear();
		sendParameter.clear();
		sendParameterType.clear();

		AstWalkerJava.logToConsole("	Finished processing:  Function " + nodeName);
		clearLists();
		return returnString;
	}

	public void clearAltLists() {
		altStatements.clear();
		altStatementCounter = -1;

	}

	public void clearLists() {
		functionString.setLength(0);
		returnType = null;
		returnStatementValue = null;

		isThereAStartedTimer = false;

		sendCounter = -1;

		sendPortReference.clear();
		sendParameter.clear();
		sendParameterType.clear();

		receivePortReference.clear();
		receiveValue.clear();
		receiveAnyValValue.clear();
		receiveType.clear();

		functionVars.clear();
		functionVarTypes.clear();
		functionVarValues.clear();
		functionAssignIdentifiers.clear();
		functionAssignValues.clear();

		functionValueIsAValueReference.clear();
		functionVarIsConstant.clear();
		defCounter = -1;
		assignCounter = -1;

		receiveStatements.clear();
	}

}
