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
import java.util.List;

import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuards;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Assignment_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Operation_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Repeat_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Send_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Setverdict_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.Timeout_Statement;
import org.eclipse.titan.designer.AST.TTCN3.values.Verdict_Value;

public class Def_AltStatement_Writer {
	private Alt_Statement altStatement;
	private StringBuilder altString = new StringBuilder("");

	private String currentTimerName = null;
	private String nodeName = null;

	private int altReceiveCounter = -1;
	private int timeOutCounter = -1;

	private boolean isFunction = false;
	private boolean isTestCase = false;

	public List<String> altGuardConditions = new ArrayList<String>();
	public List<String> altGuardPortReference = new ArrayList<String>();
	public List<String> altGuardReceiveValue = new ArrayList<String>();
	public List<String> altGuardReceiveType = new ArrayList<String>();
	public List<String> altGuardTimeout = new ArrayList<String>();

	private boolean isThereAStartedTimer = false;
	public List<String> altGuardReceiveAnyValValue = new ArrayList<String>();

	Def_Testcase_Writer testcaseParent;
	Def_Function_Writer functionParent;

	public Def_AltStatement_Writer(Alt_Statement altStatement,
			String parentNodeName) {
		this.altStatement = altStatement;
		this.nodeName = parentNodeName;
	}

	public void writeTestCaseAltStatement(Alt_Statement altStatement) {

		AltGuards currentAltGuards = altStatement.getAltGuards();

		String[] negativeConditions = new String[currentAltGuards
				.getNofAltguards()];
		String[] positiveConditions = new String[currentAltGuards
				.getNofAltguards()];

		// initialize values
		for (int i = 0; i < currentAltGuards.getNofAltguards(); i++) {

			AltGuard currentAltGuard = currentAltGuards.getAltguardByIndex(i);
			if (currentAltGuard instanceof Operation_Altguard) {

				Statement currentStatement = ((Operation_Altguard) currentAltGuard)
						.getGuardStatement();

				if (currentStatement instanceof Receive_Port_Statement) {

					altReceiveCounter++;
					if (altGuardConditions.get(i) != null) {// alt guard present

						// alt guard present && any port receive
						if (altGuardPortReference.get(altReceiveCounter)
								.equals("any port")) {
							positiveConditions[i] = altGuardConditions.get(altReceiveCounter)
									+ ".getValue()" + "&&("
									+ "anyPortReceive(true))";

							negativeConditions[i] = altGuardConditions.get(altReceiveCounter)
									+ ".getValue()" + "&&("
									+ "anyPortReceive(false))";

						} else {// alt guard present && normal port receive
							positiveConditions[i] = altGuardConditions.get(altReceiveCounter)
									+ ".getValue()"
									+ "&&("
									+ altGuardPortReference
											.get(altReceiveCounter)
									+ ".receive("
									+ altGuardReceiveValue
											.get(altReceiveCounter)
									+ ",true)!=null)";

							negativeConditions[i] = altGuardConditions.get(altReceiveCounter)
									+ ".getValue()"
									+ "&&("
									+ altGuardPortReference
											.get(altReceiveCounter)
									+ ".receive("
									+ altGuardReceiveValue
											.get(altReceiveCounter)
									+ ",false)!=null)";

							if (altGuardReceiveValue.get(altReceiveCounter)
									.startsWith("Templates")) {
								altGuardReceiveType.set(altReceiveCounter, "Templates");
							}
						}

					} else {// no alt guard

						// no alt guard && any port recieve
						if (altGuardPortReference.get(altReceiveCounter)
								.equals("any port")) {
							positiveConditions[i] = "anyPortReceive(true)";
							negativeConditions[i] = "anyPortReceive(false)";
						} else {
							// no alt guard && typed port recieve
							if (altGuardReceiveType.get(altReceiveCounter).equals(
									"_TYPED_PARAM_")) {
								negativeConditions[i] = altGuardPortReference
										.get(altReceiveCounter)
										+ ".receive_"
										+ altGuardReceiveValue
												.get(altReceiveCounter)
										+ "(false)!=null";

								if (altGuardReceiveAnyValValue.get(altReceiveCounter) != null) {
									positiveConditions[i] = "("
											+ altGuardReceiveAnyValValue.get(altReceiveCounter)
											+ "="
											+ altGuardPortReference
													.get(altReceiveCounter)
											+ ".receive_"
											+ altGuardReceiveValue
													.get(altReceiveCounter)
											+ "(true))!=null";

								} else {

								}

							} else { // no alt guard && normal port recieve
								positiveConditions[i] = altGuardPortReference
										.get(altReceiveCounter)
										+ ".receive("
										+ altGuardReceiveValue
												.get(altReceiveCounter)
										+ ",true)!=null";

								negativeConditions[i] = altGuardPortReference
										.get(altReceiveCounter)
										+ ".receive("
										+ altGuardReceiveValue
												.get(altReceiveCounter)
										+ ",false)!=null";
							}
							if (altGuardReceiveValue.get(altReceiveCounter)
									.startsWith("Templates")) {
								altGuardReceiveType.set(altReceiveCounter, "Templates");
							}
						}
					}
				}

				if (currentStatement instanceof Timeout_Statement) {
					timeOutCounter++;
					if (altGuardConditions.get(i) != null) {
						positiveConditions[i] = altGuardConditions.get(i)
								+ "&&(" + altGuardTimeout.get(timeOutCounter)
								+ ".timeout()";
						negativeConditions[i] = altGuardConditions.get(i)
								+ "&&(!" + altGuardTimeout.get(i)
								+ ".timeout()";

					} else {
						positiveConditions[i] = altGuardTimeout
								.get(timeOutCounter) + ".timeout()";
						negativeConditions[i] = "!"
								+ altGuardTimeout.get(timeOutCounter)
								+ ".timeout()";
					}

				}

			}
		}

		// write
		altString.append("rownum=" + altStatement.getLocation().getLine()
				+ ";\r\n");
		altString.append("for(;;){" + "\r\n");

		altString.append("if(!(");
		for (int j = 0; j < negativeConditions.length; j++) {
			altString.append(negativeConditions[j]);
			if (j + 1 < negativeConditions.length) {
				altString.append("||");
			}
		}

		altString.append(")){" + "\r\n");

		// timer
		if (isThereAStartedTimer) {
			altString.append("long timeout = -1;" + "\r\n");
			altString.append("long newtimeout;" + "\r\n");

			altString
					.append("if("
							+ currentTimerName
							+ ".running)if((newtimeout=(long)("
							+ currentTimerName
							+ ".read().value*1000.0))<timeout || timeout == -1) timeout=newtimeout;"
							+ "\r\n");
			altString
					.append("if(timeout>0) try{queue.poll(timeout,TimeUnit.MILLISECONDS);}catch(InterruptedException e){} "
							+ "\r\n");

		} else {

			altString
					.append("try{queue.take();}catch(InterruptedException e){}"
							+ "\r\n");

		}
		// endoftimer

		altString.append("}" + "\r\n");
		altString.append("this.lock();" + "\r\n");
		// ifwriter

		altReceiveCounter = -1;
		boolean isFirstIf = true;
		for (int i = 0; i < currentAltGuards.getNofAltguards(); i++) {

			AltGuard currentAltGuard = currentAltGuards.getAltguardByIndex(i);
			if (currentAltGuard instanceof Operation_Altguard) {

				Statement currentStatement = ((Operation_Altguard) currentAltGuard)
						.getGuardStatement();
				StatementBlock currentStatementBlock = ((Operation_Altguard) currentAltGuard)
						.getStatementBlock();

				if (isFirstIf) {
					altString.append("if(" + positiveConditions[i] + "){\r\n");
					isFirstIf = false;
				} else {
					altString.append("else if(" + positiveConditions[i]
							+ "){\r\n");
				}

				if (currentStatement instanceof Receive_Port_Statement) {
					altReceiveCounter++;

					altString.append("rownum="
							+ currentStatement.getLocation().getLine()
							+ ";\r\n");

					if (altGuardReceiveType.get(altReceiveCounter).equals(
							"Templates")) {
						String methodName = altGuardReceiveValue
								.get(altReceiveCounter);
						if (methodName.endsWith("()")
								&& methodName.startsWith("Templates.")) {

							methodName = (String) methodName.subSequence(10,
									methodName.length() - 2);
						}

						altString
								.append("	TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
										+ nodeName
										+ "\", \"RECEIVE event on port "
										+ altGuardPortReference
												.get(altReceiveCounter)
										+ ":\\n "
										+ methodName
										+ ":=\" + "
										+ altGuardReceiveValue
												.get(altReceiveCounter)
										+ ".toString(), true);" + "\r\n");
					} else if (altGuardReceiveType.get(altReceiveCounter)
							.equals("any port")) {
						altString
								.append("	TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
										+ nodeName
										+ "\", \"RECEIVE event on port any port\", true);"
										+ "\r\n");
					} else if (altGuardReceiveType.get(altReceiveCounter)
							.equals("_TYPED_PARAM_")) {
						altString
								.append("	TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
										+ nodeName
										+ "\", \"RECEIVE event on port "
										+ altGuardPortReference
												.get(altReceiveCounter)
										+ ": type "
										+ altGuardReceiveValue
												.get(altReceiveCounter)
										+ "\", true);" + "\r\n");

					} else {
						altString
								.append("	TTCN3Logger.writeLog(compid, \"PORTEVENT\", sourcefilename, rownum, \"function\", \""
										+ nodeName
										+ "\", \"RECEIVE event on port "
										+ altGuardPortReference
												.get(altReceiveCounter)
										+ ":\" + "
										+ altGuardReceiveValue
												.get(altReceiveCounter)
										+ ".toString(), true);" + "\r\n");
					}
				} else if (currentStatement instanceof Timeout_Statement) {
					altString
							.append("rownum="
									+ currentStatement.getLocation().getLine()
									+ ";\r\n"
									+ "TTCN3Logger.writeLog(compid, \"TIMEROP\", sourcefilename, rownum, \"function\", \""
									+ nodeName + "\", \"Timeout on timer "
									+ currentTimerName + ".\", false);"
									+ "\r\n");

				}

				boolean isThereARepeatStatement = false;
				for (int j = 0; j < currentStatementBlock.getSize(); j++) {
					Statement currentStatementBlockStatement = currentStatementBlock
							.getStatementByIndex(j);

					if (currentStatementBlockStatement instanceof Setverdict_Statement) {
						Setverdict_Statement setVerdictStatement = (Setverdict_Statement) currentStatementBlockStatement;
						String verdict = "";

						if (setVerdictStatement.getVerdictValue() instanceof Verdict_Value) {
							Verdict_Value verdictValue = (Verdict_Value) setVerdictStatement
									.getVerdictValue();
							if (verdictValue.getValue().toString()
									.equals("PASS")) {
								verdict = "pass";
							} else if (verdictValue.getValue().toString()
									.equals("INCONC")) {
								verdict = "inconc";
							} else {
								verdict = "fail";
							}
						}
						altString.append("rownum="
								+ setVerdictStatement.getLocation().getLine()
								+ ";\r\n");
						altString
								.append("TTCN3Logger.writeLog(compid, \"VERDICTOP\", sourcefilename, rownum, \"function\", \""
										+ nodeName
										+ "\", \"setverdict("
										+ verdict
										+ "): \" + getVerdict() + \" -> "
										+ verdict + "\", true);" + "\r\n");
						altString.append("setVerdict(\"" + verdict + "\");"
								+ "\r\n");

					}

					if (currentStatementBlockStatement instanceof Assignment_Statement) {

						if (isFunction) {
							functionParent.assignCounter++;

							altString
									.append(functionParent
											.writeAssignmentStatement((Assignment_Statement) currentStatementBlockStatement));

							String test = "";
							test.toString();

						} else if (isTestCase) {
							testcaseParent.assignCounter++;
							altString
									.append(testcaseParent
											.writeAssignmentStatement((Assignment_Statement) currentStatementBlockStatement));

						}

					}

					if (currentStatementBlockStatement instanceof Send_Statement) {

						if (isFunction) {
							functionParent.sendCounter++;

							altString
									.append(functionParent
											.writeSendStatement((Send_Statement) currentStatementBlockStatement));

						} else if (isTestCase) {
							testcaseParent.sendCounter++;
							altString
									.append(testcaseParent
											.writeSendStatement((Send_Statement) currentStatementBlockStatement));

						}
					}

					if (currentStatementBlockStatement instanceof Repeat_Statement) {
						isThereARepeatStatement = true;
					}

				}
				if (isThereARepeatStatement) {
					isThereARepeatStatement = false;
				} else {
					altString.append("break;\r\n");
				}

				altString.append("}\r\n");

			}
		}

		altString.append("this.unlock();" + "\r\n");
		altString.append("}" + "\r\n");

	}

	public void clearLists() {
		altGuardConditions.clear();
		altGuardPortReference.clear();
		altGuardReceiveValue.clear();
		altGuardReceiveType.clear();
		altGuardTimeout.clear();

		altReceiveCounter = -1;
		timeOutCounter = -1;

	}

	public String getJavaSource() {

		this.writeTestCaseAltStatement(altStatement);

		String returnString = altString.toString();
		altString.setLength(0);
		clearLists();

		return returnString;
	}


	public void setTimerInfo(boolean isThereAStartedTimer2,
			String currentTimerName2) {
		isThereAStartedTimer = isThereAStartedTimer2;
		currentTimerName = currentTimerName2;

	}


	public void setType(String string) {
		if (string.equals("Function")) {
			isFunction = true;
		} else if (string.equals("TestCase")) {
			isTestCase = true;
		}
	}

	public void setParent(Object parent) {
		if (parent instanceof Def_Function_Writer) {
			functionParent = (Def_Function_Writer) parent;
		} else if (parent instanceof Def_Testcase_Writer) {
			testcaseParent = (Def_Testcase_Writer) parent;
		}
	}

}
