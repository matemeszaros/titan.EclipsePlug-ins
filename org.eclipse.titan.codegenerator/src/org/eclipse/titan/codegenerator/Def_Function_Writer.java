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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Assignment_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Definition_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Operation_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Repeat_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Return_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Send_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Setverdict_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.Timeout_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Start_Statement;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Verdict_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AddExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Int2StrExpression;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

public class Def_Function_Writer {
	private Def_Function functionNode;
	private StringBuilder functionString = new StringBuilder("");
	private CompilationTimeStamp compilationCounter = CompilationTimeStamp
			.getNewCompilationCounter();

	private String nodeName = null;

	private static Map<String, Object> functionHashes = new LinkedHashMap<String, Object>();

	private StatementBlock statementBlock;
	private boolean isThereAStartedTimer = false;
	private String currentTimerName = null;

	public String returnStatementValue = null;
	private String runsOnValue = null;
	private String returnType = null;
	private String timerValue = null;
	private String unknownStartReference = null;
	private List<String> sendPortReference = new ArrayList<String>();
	private List<String> sendParameter = new ArrayList<String>();
	private List<String> sendParameterType = new ArrayList<String>();
	private int sendCounter = -1;
	private List<String> recievePortReference = new ArrayList<String>();
	private List<String> recieveParameter = new ArrayList<String>();
	private List<String> recieveParameterType = new ArrayList<String>();
	private int recieveCounter = -1;
	private List<String> addValues = new ArrayList<String>();

	private Def_Function_Writer(Def_Function typeNode) {
		super();
		this.functionNode = typeNode;
		nodeName = typeNode.getIdentifier().toString();
	}

	public static Def_Function_Writer getInstance(Def_Function typeNode) {
		if (!functionHashes.containsKey(typeNode.getIdentifier().toString())) {
			functionHashes.put(typeNode.getIdentifier().toString(),
					new Def_Function_Writer(typeNode));
		}
		return (Def_Function_Writer) functionHashes.get(typeNode
				.getIdentifier().toString());
	}

	public void setStatementBlock(StatementBlock statementBlock) {
		this.statementBlock = statementBlock;
	}

	public void writeFunction() {

		functionString.append("public void " + nodeName + "(){\r\n");

		/*
		 * if (AstWalkerJava.areCommentsAllowed) {
		 * functionString.append("System.out.println(\"" + nodeName +
		 * " started\");" + "\r\n"); }
		 */

		functionString.append("String sourcefilename = \""
				+ statementBlock.getLocation().getFile().getFullPath()
						.lastSegment() + "\";" + "\r\n");
		functionString.append("int rownum=" + functionNode.getLocation().getLine()
				+ ";\r\n");
		functionString.append("if(!created) return;" + "\r\n");

		functionString
				.append("TTCN3Logger.writeLog(compid, \"PARALLEL\", sourcefilename, rownum, \"function\", \""
						+ nodeName
						+ "\", \"Function started on \" + compid + \".\", false);"
						+ "\r\n");

		for (int i = 0; i < statementBlock.getSize(); i++) {

			if (statementBlock.getStatementByIndex(i) instanceof Definition_Statement) {
				writeDefinitionStatement((Definition_Statement) statementBlock
						.getStatementByIndex(i));
			} else if (statementBlock.getStatementByIndex(i) instanceof Send_Statement) {
				sendCounter++;
				writeSendStatement((Send_Statement) statementBlock
						.getStatementByIndex(i));
			} else if (statementBlock.getStatementByIndex(i) instanceof Unknown_Start_Statement) {
				writeUnknownStartStatement((Unknown_Start_Statement) statementBlock
						.getStatementByIndex(i));
			} else if (statementBlock.getStatementByIndex(i) instanceof Alt_Statement) {
				writeAltStatement((Alt_Statement) statementBlock
						.getStatementByIndex(i));
			} else if (statementBlock.getStatementByIndex(i) instanceof Setverdict_Statement) {
				writeSetVerdictStatement((Setverdict_Statement) statementBlock
						.getStatementByIndex(i));
			}
		}

	}

	public String[] writeAltCommands(Operation_Altguard altGuard) {
		int rownum = 0;
		StringBuilder altString = new StringBuilder();

		int guardBlockSize = altGuard.getStatementBlock().getSize();
		for (int j = 0; j < guardBlockSize; j++) {

			if (altGuard.getStatementBlock().getStatementByIndex(j) instanceof Setverdict_Statement) {

				if (altGuard.getStatementBlock().getStatementByIndex(j) instanceof Setverdict_Statement) {
					Setverdict_Statement verdictAltGuard = (Setverdict_Statement) altGuard
							.getStatementBlock().getStatementByIndex(j);

					String verdict = "";
					if (verdictAltGuard.getVerdictValue() instanceof Verdict_Value) {
						Verdict_Value verdictValue = (Verdict_Value) verdictAltGuard
								.getVerdictValue();
						if (verdictValue.getValue().toString().equals("PASS")) {
							verdict = "pass";
						} else if (verdictValue.getValue().toString()
								.equals("INCONC")) {
							verdict = "inconc";
						} else {
							verdict = "fail";

						}
						altString.append("rownum="
								+ verdictValue.getLocation().getLine()
								+ ";\r\n");
						altString
								.append("TTCN3Logger.writeLog(compid, \"VERDICTOP\", sourcefilename, rownum, \"function\", \""
										+ nodeName
										+ "\", \"setverdict("
										+ verdict
										+ "): \" + getVerdict() + \" -> "
										+ verdict + "\", true);" + "\r\n");

						altString.append("setVerdict(\"" + verdict
								+ "\");break;" + "\r\n");
						rownum = verdictValue.getLocation().getLine();
					}
				}

			}

			if (altGuard.getStatementBlock().getStatementByIndex(j) instanceof Assignment_Statement) {
				Assignment_Statement assignmentGuard = (Assignment_Statement) altGuard
						.getStatementBlock().getStatementByIndex(j);

				String assignmentName = assignmentGuard.getReference()
						.getSubreferences().get(0).getId().toString();

				if (assignmentGuard.getTemplate().getValue() instanceof AddExpression) {
					altString.append("rownum="+assignmentGuard.getLocation().getLine()+";\r\n");
					altString.append(assignmentName + "=" + addValues.get(0)
							+ ".plus(new INTEGER(" + addValues.get(1) + ")); \r\n");
				}

			}

			if (altGuard.getStatementBlock().getStatementByIndex(j) instanceof Repeat_Statement) {
				// TODO
			}

			if (altGuard.getStatementBlock().getStatementByIndex(j) instanceof Send_Statement) {
				sendCounter++;
				Send_Statement sendAltGuard = (Send_Statement) altGuard
						.getStatementBlock().getStatementByIndex(j);

				String portReferenceName = sendPortReference.get(sendCounter);
				String parameterName = null;
				String parameterType = null;
				String valueType = sendParameterType.get(sendCounter);
				String valueName = sendParameter.get(sendCounter);

				if (valueType.equals("Undefined_LowerIdentifier_Value")) {

					parameterName = valueName;
				}

				if (valueType.equals("Charstring_Value")) {
					parameterName = "new CHARSTRING(\"" + valueName + "\")";
				}

				if (valueType.equals("Int2StrExpression")) {
					parameterName = valueName+".int2str()";
				}

				altString.append("rownum="+sendAltGuard.getLocation().getLine()+";\r\n");
				altString.append(portReferenceName + ".send(" + parameterName
						+ ");" + "\r\n");

				altString.append("TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""+nodeName+"\", \"SEND event on port "+portReferenceName+":\" + "+parameterName+".toString(), true);"+"\r\n");

			}
		}
		String[] returnvalue = new String[2];
		returnvalue[0] = altString.toString();
		returnvalue[1] = Integer.toString(rownum);
		return returnvalue;
	}

	public void writeDefinitionStatement(
			Definition_Statement definitionStatement) {

		if (definitionStatement.getDefinition() instanceof Def_Timer) {
			String statementName = definitionStatement.getDefinition()
					.getIdentifier().toString();
			Def_Timer currentTimer = (Def_Timer) definitionStatement
					.getDefinition();

			functionString.append("rownum="
					+ definitionStatement.getDefinition().getLocation()
							.getLine() + ";\r\n");
			functionString.append("Timer " + statementName
					+ " = new Timer (new FLOAT(" + timerValue + "));\r\n");

			functionString
					.append("TTCN3Logger.writeLog(compid, \"TIMEROP\", sourcefilename, rownum, \"function\", \""
							+ nodeName
							+ "\", \"Timer "
							+ statementName
							+ " set to " + timerValue + ".\", false);" + "\r\n");

		}

		if (definitionStatement.getDefinition() instanceof Def_Var) {
			String variableType = "";
			String variableName = "";
			String variableValue = "";

			Def_Var currentVar = (Def_Var) definitionStatement.getDefinition();
			variableName = definitionStatement.getDefinition().getIdentifier()
					.toString();

			if (currentVar.getType(compilationCounter) instanceof Referenced_Type) {
				Referenced_Type ref = (Referenced_Type) currentVar
						.getType(compilationCounter);
				variableType = ref.getReference().getSubreferences().get(0)
						.getId().toString();
				
				
			}

			if (currentVar.getType(compilationCounter) instanceof Integer_Type) {
				Integer_Type ref = (Integer_Type) currentVar
						.getType(compilationCounter);
				variableType = "INTEGER";
				if (currentVar.getInitialValue() instanceof Integer_Value) {
					Integer_Value varValue = (Integer_Value) currentVar
							.getInitialValue();
					variableValue = " = new INTEGER(" + varValue.getValueValue().toString()+")";
				}

			}
			functionString.append("rownum="
					+ currentVar.getLocation().getLine() + ";\r\n");
			functionString.append(variableType + " " + variableName
					+ variableValue + ";\r\n");

		}

	}

	public void writeSendStatement(Send_Statement sendStatement) {

		String portReferenceName = sendPortReference.get(sendCounter);
		String parameterName = null;
		String parameterType = null;
		String valueType = sendParameterType.get(sendCounter);
		String valueName = sendParameter.get(sendCounter);

		if (valueType.equals("Undefined_LowerIdentifier_Value")) {

			if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(valueName)) {
				parameterType = myASTVisitor.nodeNameNodeTypeHashMap
						.get(valueName);
			}

			if (parameterType.equals("template")) {
				parameterType = "Templates";
			}

			parameterName = parameterType + "." + valueName + "()";
			
			functionString.append("rownum=" + sendStatement.getLocation().getLine()
					+ ";\r\n");
			functionString.append(portReferenceName + ".send(" + parameterName
					+ ");" + "\r\n");
			functionString
					.append("TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
							+ nodeName
							+ "\", \"SEND event on port "
							+ portReferenceName
							+ ": "
							+ valueName
							+ ":=\" + "
							+ parameterName + ".toString(), true);" + "\r\n");
			
		}

		if (valueType.equals("Charstring_Value")) {
			parameterName = "new CHARSTRING(\"" + valueName + "\")";
			functionString.append("rownum=" + sendStatement.getLocation().getLine()
					+ ";\r\n");
			functionString.append(portReferenceName + ".send(" + parameterName
					+ ");" + "\r\n");
			functionString
					.append("TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
							+ nodeName
							+ "\", \"SEND event on port "
							+ portReferenceName
							+ ":\"+"
							+ parameterName + ".toString(), false);" + "\r\n");
		
		
		
		
		
		}
		

	}

	public void writeUnknownStartStatement(
			Unknown_Start_Statement unknownStartStatement) {
		functionString.append("rownum="
				+ unknownStartStatement.getLocation().getLine() + ";\r\n");
		functionString.append(unknownStartReference + ".start();" + "\r\n");
		isThereAStartedTimer = true;
		currentTimerName = unknownStartReference;
		functionString
				.append("TTCN3Logger.writeLog(compid, \"TIMEROP\", sourcefilename, rownum, \"function\", \""
						+ nodeName
						+ "\", \"Timer "
						+ unknownStartReference
						+ " started.\", false);" + "\r\n");

	}

	public void writeAltStatement(Alt_Statement altStatement) {
		functionString.append("rownum=" + altStatement.getLocation().getLine()
				+ ";\r\n");
		functionString.append("for(;;){" + "\r\n");

		int altSize = altStatement.getAltGuards().getNofAltguards();

		String[] allPortname = new String[altSize];
		String[] allPortRecieveType = new String[altSize];
		String[] negativeConditions = new String[altSize];
		String[] positiveConditions = new String[altSize];
		String[] altCommands = new String[altSize];
		String[] altRowNum = new String[altSize];
		String[] logReceiveType = new String[altSize];
		String[] logReceiveValue = new String[altSize];
		boolean[] isTheRecieveTyped = new boolean[altSize];
		int[] receiveRowNum = new int[altSize];
		String[] logTimeout = new String[altSize];
		
		// portsetter
		for (int i = 0; i < altSize; i++) {

			String[] portType = new String[altSize];

			if (altStatement.getAltGuards().getAltguardByIndex(i) instanceof Operation_Altguard) {

				Operation_Altguard altGuard = (Operation_Altguard) altStatement
						.getAltGuards().getAltguardByIndex(i);

				// if recieves from a specific prot
				if (altGuard.getGuardStatement() instanceof Receive_Port_Statement) {
					recieveCounter++;
					Receive_Port_Statement recievePortAltGuard = (Receive_Port_Statement) altGuard
							.getGuardStatement();
					if (recieveParameterType.get(recieveCounter).equals(
							"Any_Value_Template")) {
						recieveCounter++;
					}
					if ((!recievePortReference.get(recieveCounter).equals(
							"_ANY_"))) {

						String valueType = recieveParameterType
								.get(recieveCounter);
						String valueName = recieveParameter.get(recieveCounter);

						if (valueType.equals("Undefined_LowerIdentifier_Value")) {

							String parameterType = null;

							if (myASTVisitor.nodeNameNodeTypeHashMap
									.containsKey(valueName)) {
								parameterType = myASTVisitor.nodeNameNodeTypeHashMap
										.get(valueName);
							}

							if (parameterType.equals("template")) {
								parameterType = "Templates";
							}

							allPortRecieveType[i] = parameterType + "."
									+ valueName + "()";
							logReceiveValue[i] = valueName;
						}
						if (valueType.equals("Charstring_Value")) {

							allPortRecieveType[i] = "new CHARSTRING(" + "\""
									+ valueName + "\")";
						}
						receiveRowNum[i]=recievePortAltGuard.getLocation().getLine();
						allPortname[i] = recievePortAltGuard.getPort()
								.getSubreferences().get(0).getId().toString();

					} else {
						receiveRowNum[i]=recievePortAltGuard.getLocation().getLine();
						allPortname[i] = "any port";

					}

				}

			}
		}
		int recieveParamCounter = -1;
		for (int i = 0; i < altSize; i++) {
			String portname = allPortname[i];
			String portRecieveType = allPortRecieveType[i];
			String verdict = "";
			String timerName = "";
			String portType = "";

			if (altStatement.getAltGuards().getAltguardByIndex(i) instanceof Operation_Altguard) {
				Operation_Altguard altGuard = (Operation_Altguard) altStatement
						.getAltGuards().getAltguardByIndex(i);

				// if recieves from a specific prot
				if (altGuard.getGuardStatement() instanceof Receive_Port_Statement) {
					Receive_Port_Statement recievePortAltGuard = (Receive_Port_Statement) altGuard
							.getGuardStatement();
					recieveParamCounter++;

					if (!recievePortReference.get(recieveParamCounter).equals(
							"_ANY_")) {

						if (recieveParameterType.get(recieveParamCounter)
								.equals("_TYPED_PARAM_")) {
							portType = recieveParameter
									.get(recieveParamCounter);
							recieveCounter++;
						}

					}

					if (recievePortAltGuard.getPort() != null) {
						portname = recievePortAltGuard.getPort()
								.getSubreferences().get(0).getId().toString();
						// if the port is set

						if (portType.equals("")) {
							negativeConditions[i] = portname + ".receive("
									+ portRecieveType + ",false)!=null";
							positiveConditions[i] = portname + ".receive("
									+ portRecieveType + ",true)!=null";

							isTheRecieveTyped[i] = false;
						} else {
							negativeConditions[i] = portname + ".receive" + "_"
									+ portType + "(false)!=null";

							String redirectString = recievePortAltGuard
									.getRedirectValue().getSubreferences()
									.get(0).getId().toString();
							positiveConditions[i] = "(" + redirectString + "="
									+ portname + ".receive" + "_" + portType
									+ "(true))!=null";
							isTheRecieveTyped[i] = true;
							logReceiveType[i] = portType;
						}

					} else {
						// if no port is set
						negativeConditions[i] = "anyPortReceive(false)";
						positiveConditions[i] = "anyPortReceive(true)";

					}

				}

				if (altGuard.getGuardStatement() instanceof Timeout_Statement) {
					Timeout_Statement timeOutAltGuard = (Timeout_Statement) altGuard
							.getGuardStatement();

					isThereAStartedTimer = true;
					negativeConditions[i] = ("!" + currentTimerName + ".timeout()");
					positiveConditions[i] = ("" + currentTimerName + ".timeout()");
					
					logTimeout[i]="rownum="+timeOutAltGuard.getLocation().getLine()+";\r\n"+"TTCN3Logger.writeLog(compid, \"TIMEROP\", sourcefilename, rownum, \"function\", \""+nodeName+"\", \"Timeout on timer "+currentTimerName+".\", false);"+"\r\n";
					
					
				}
				String[] commandBlock=writeAltCommands(altGuard);
				altCommands[i] = commandBlock[0];
				altRowNum[i] = commandBlock[1];

			}

		}

		functionString.append("if(!(");
		for (int condStepper = 0; condStepper < altSize; condStepper++) {
			functionString.append(negativeConditions[condStepper]);
			if (condStepper + 1 < altSize) {
				functionString.append("||");
			}
		}
		functionString.append(")){" + "\r\n");

		// timer
		if (isThereAStartedTimer) {
			functionString.append("long timeout = -1;" + "\r\n");
			functionString.append("long newtimeout;" + "\r\n");

			functionString
					.append("if("
							+ currentTimerName
							+ ".running)if((newtimeout=(long)("
							+ currentTimerName
							+ ".read().value*1000.0))<timeout || timeout == -1) timeout=newtimeout;"
							+ "\r\n");
			functionString
					.append("if(timeout>0) try{queue.poll(timeout,TimeUnit.MILLISECONDS);}catch(InterruptedException e){} "
							+ "\r\n");
			functionString.append("this.lock();" + "\r\n");
		} else {

			functionString
					.append("try{queue.take();}catch(InterruptedException e){}"
							+ "\r\n");
			functionString.append("this.lock();" + "\r\n");

		}
		// endoftimer

		functionString.append("}" + "\r\n");

		// ifwriter
		for (int condCounter = 0; condCounter < altSize; condCounter++) {

			if (condCounter > 0) {
				functionString.append("else ");
			}
			functionString.append("if(");
			functionString.append(positiveConditions[condCounter]);
			functionString.append("){" + "\r\n");

			if (allPortname[condCounter] != null) {
				if (!isTheRecieveTyped[condCounter]) {
					if (logReceiveValue[condCounter] != null) {
						functionString.append("rownum="+receiveRowNum[condCounter]+";\r\n");
						functionString
								.append("TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
										+ nodeName
										+ "\", \"RECEIVE event on port "
										+ allPortname[condCounter]
										+ ":\\n "
										+ logReceiveValue[condCounter]
										+ ":=\" + "
										+ allPortRecieveType[condCounter]
										+ ".toString(), true);" + "\r\n");
					} else {
						if (allPortRecieveType[condCounter] != null) {
							functionString.append("rownum="+receiveRowNum[condCounter]+";\r\n");
							functionString
									.append("	TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
											+ nodeName
											+ "\", \"RECEIVE event on port "
											+ allPortname[condCounter]
											+ ":\" + "
											+ allPortRecieveType[condCounter]
											+ ".toString(), true);" + "\r\n");
						} else {
							functionString.append("rownum="+receiveRowNum[condCounter]+";\r\n");
							functionString
									.append("TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
											+ nodeName
											+ "\", \"RECEIVE event on port "
											+ allPortname[condCounter]
											+ "\", true);" + "\r\n");
						}
					}
				} else {
					functionString.append("rownum="+receiveRowNum[condCounter]+";\r\n");
					functionString
							.append("TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
									+ nodeName
									+ "\", \"RECEIVE event on port "
									+ allPortname[condCounter]
									+ ": type "
									+ logReceiveType[condCounter]
									+ "\", true);" + "\r\n");
				}
			}
			
			if(logTimeout[condCounter]!=null){
				functionString.append(logTimeout[condCounter]);
			}

			// TODO: ide jön az if log
			functionString.append(altCommands[condCounter]);

			functionString.append("}" + "\r\n");

		}
		functionString.append("this.unlock();" + "\r\n");
		functionString.append("}" + "\r\n");

	}

	public void writeSetVerdictStatement(
			Setverdict_Statement setVerdictStatement) {

		String verdict = "";

		if (setVerdictStatement.getVerdictValue() instanceof Verdict_Value) {
			Verdict_Value verdictValue = (Verdict_Value) setVerdictStatement
					.getVerdictValue();
			if (verdictValue.getValue().toString().equals("PASS")) {
				verdict = "pass";
			} else if (verdictValue.getValue().toString().equals("INCONC")) {
				verdict = "inconc";
			} else {
				verdict = "fail";
			}
		}
		functionString.append("rownum="
				+ setVerdictStatement.getLocation().getLine() + ";\r\n");
		functionString
				.append("TTCN3Logger.writeLog(compid, \"VERDICTOP\", sourcefilename, rownum, \"function\", \""
						+ nodeName
						+ "\", \"setverdict("
						+ verdict
						+ "): \" + getVerdict() + \" -> "
						+ verdict
						+ "\", true);" + "\r\n");
		functionString.append("setVerdict(\"" + verdict + "\");" + "\r\n");
	}

	public String writeFunctionFile() {

		StringBuilder functionFileString = new StringBuilder();

		functionFileString.append("class " + nodeName + " implements Runnable{"
				+ "\r\n");
		functionFileString.append("private " + runsOnValue + " component;"
				+ "\r\n");
		functionFileString.append("public " + nodeName + "(" + runsOnValue
				+ " c){" + "\r\n");
		functionFileString.append("component = c;" + "\r\n");
		functionFileString.append("}" + "\r\n");
		functionFileString.append("public void run(){" + "\r\n");
		functionFileString.append("component." + nodeName + "();" + "\r\n");
		functionFileString.append("}" + "\r\n");
		functionFileString.append("}" + "\r\n");

		return functionFileString.toString();
	}

	public String getJavaSource() {
		recieveCounter = -1;
		sendCounter = -1;
		this.writeFunction();
		functionString.append("\r\n}");
		String returnString = functionString.toString();
		functionString.setLength(0);

		return returnString;
	}

	private void writeFunctionWithoutRunsOn() {
		BigInteger currentValue = BigInteger.ZERO;

		if (returnType.equals("Integer_Type")) {
			functionString.append("public static INTEGER " + nodeName
					+ "(){\r\n");

			functionString.append("return new INTEGER (" + returnStatementValue
					+ ");\r\n");
			functionString.append("}\r\n");
		}

	}

	public String getJavaSourceWithoutRunOn() {

		this.writeFunctionWithoutRunsOn();
		functionString.append("\r\n}");
		String returnString = functionString.toString();
		functionString.setLength(0);
		return returnString;
	}

	public void setRunsOnValue(String runsOnValue) {
		this.runsOnValue = runsOnValue;

	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;

	}

	public void setReturnStatementValue(String returnStatementValue) {
		this.returnStatementValue = returnStatementValue;

	}

	public void setTimerValue(String timerValue) {
		this.timerValue = timerValue;

	}

	public void addPortReference(String portRef) {
		sendPortReference.add(portRef);

	}

	public void addParameter(String param) {
		sendParameter.add(param);

	}

	public void addParameterType(String string) {
		sendParameterType.add(string);

	}

	public void setUnknownStartReference(String unknownStartReference) {
		this.unknownStartReference = unknownStartReference;

	}

	public void addRecievePortReference(String portRef) {
		recievePortReference.add(portRef);

	}

	public void addRecieveParameter(String param) {
		recieveParameter.add(param);

	}

	public void addRecieveParameterType(String string) {
		recieveParameterType.add(string);

	}

	public void addAddValues(String string) {
		addValues.add(string);

	}

}
