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

import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.statements.Assignment_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Connect_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Definition_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Done_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Start_Statement;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ComponentCreateExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

public class Def_Testcase_Writer {
	private Def_Testcase testCaseNode;
	private StringBuilder testCaseString = new StringBuilder("");
	private CompilationTimeStamp compilationCounter = CompilationTimeStamp
			.getNewCompilationCounter();

	private String nodeName=null;
		
	private static Map<String, Object> testcaseHashes = new LinkedHashMap<String, Object>();
	private String testCaseRunsOn=null;
	private StatementBlock tcStatementBlock;
	
	private  List<String> tcVars = new ArrayList<String>();
	private  List<String> tcVarTypes = new ArrayList<String>();
	private  List<String> tcAssignValues = new ArrayList<String>();
	private  List<String> tcCreateValues = new ArrayList<String>();
	private  List<String> tcCreateCounter = new ArrayList<String>();
	
	private int createCounter=-1;
	
	private  List<String> tcConnectValues = new ArrayList<String>();
	private  List<String> tcConnectCounter = new ArrayList<String>();
	private int connectCounter=-1;
	
	private  List<String> tcStartValues = new ArrayList<String>();
	private  List<String> tcStartCounter = new ArrayList<String>();
	private int startCounter=-1;

	private Def_Testcase_Writer(Def_Testcase typeNode) {
		super();
		this.testCaseNode = typeNode;
		nodeName = typeNode.getIdentifier().toString();
	}

	public static Def_Testcase_Writer getInstance(Def_Testcase typeNode) {
		if (!testcaseHashes.containsKey(typeNode.getIdentifier().toString())) {
			testcaseHashes.put(typeNode.getIdentifier().toString(),  new Def_Testcase_Writer(typeNode));
		}
		return (Def_Testcase_Writer) testcaseHashes.get(typeNode.getIdentifier().toString());
	}

	public String writeTestcaseFile(Def_Testcase testNode) {
		StringBuilder testCaseString = new StringBuilder("\r\n");

		testCaseString.append("public class " + nodeName
				+ " implements Runnable{ " + "\r\n");
		testCaseString.append("	" + "\r\n");
		testCaseString.append("	private " + testCaseRunsOn
				+ " component;" + "\r\n");
		testCaseString.append("		public "
				+ nodeName + "("
				+ testCaseRunsOn + " c){" + "\r\n");
		testCaseString.append("	component = c;" + "\r\n");
		testCaseString.append("	}" + "\r\n");
		testCaseString.append("		public void run(){" + "\r\n");
		testCaseString.append("			component."
				+ nodeName + "();" + "\r\n");
		testCaseString.append("			component.hc.finished(component, \""
				+ nodeName + "\");" + "\r\n");
		testCaseString.append("		}" + "\r\n");
		testCaseString.append("	}" + "\r\n");

		return testCaseString.toString();
	}
	
	public String writeTestCaseFunction() {


		String createLocation = "";
		String createName = "";
		String createType = "";
		testCaseString.append("public void " + nodeName
				+ "(){" + "\r\n");
		testCaseString.append("if(!created) return;" + "\r\n");

	
		int currentCounterValue=0;
		int currentConnectValue=0;
		int currentStartValue=0;
			int testcaseSize = tcStatementBlock.getSize();
			for (int j = 0; j < testcaseSize; j++) {

				if (tcStatementBlock.getStatementByIndex(j) instanceof Definition_Statement) {
					//TODO: tcVars and tcVarTypes hold the def statement values
				}

				if (tcStatementBlock.getStatementByIndex(j) instanceof Assignment_Statement) {
					Assignment_Statement tc_assignStatement = (Assignment_Statement) tcStatementBlock
							.getStatementByIndex(j);

					if (tc_assignStatement.getTemplate() instanceof SpecificValue_Template) {
						SpecificValue_Template specValTemplate = (SpecificValue_Template) tc_assignStatement
								.getTemplate();
						if (specValTemplate.getSpecificValue() instanceof ComponentCreateExpression) {
							ComponentCreateExpression componenetCreateExp = (ComponentCreateExpression) specValTemplate
									.getSpecificValue();

							createCounter++;
							
							
							
							testCaseString.append("hc.create("+"\"" +tcCreateValues.get(currentCounterValue)+"\"");
							currentCounterValue++;

							while(tcCreateCounter.get(currentCounterValue).equals(String.valueOf(createCounter))){
								testCaseString.append(",\"" +tcCreateValues.get(currentCounterValue)+"\"");
								currentCounterValue++;
								if(tcCreateCounter.size()==(currentCounterValue)){break;}
								
							}
							
							testCaseString.append(	"); "+	"\r\n");
							
						}

					}

					

				}

				if (tcStatementBlock.getStatementByIndex(j) instanceof Connect_Statement) {
					Connect_Statement tc_connectStatement = (Connect_Statement) tcStatementBlock
							.getStatementByIndex(j);
					connectCounter++;
					
					testCaseString.append("hc.connect("+"\"" +tcConnectValues.get(currentConnectValue)+"\"");
					currentConnectValue++;

					while(tcConnectCounter.get(currentConnectValue).equals(String.valueOf(connectCounter))){
						testCaseString.append(",\"" +tcConnectValues.get(currentConnectValue)+"\"");
						currentConnectValue++;
						if(tcConnectCounter.size()==(currentConnectValue)){break;}
						
					}
					
					testCaseString.append(	"); "+	"\r\n");
					
					
					
					
					
					
					

				}

				if (tcStatementBlock.getStatementByIndex(j) instanceof Unknown_Start_Statement) {
					
					startCounter++;
					
					testCaseString.append("hc.start("+"\"" +tcStartValues.get(currentStartValue)+"\"");
					currentStartValue++;
					
					while(tcStartCounter.get(currentStartValue).equals(String.valueOf(startCounter))){
						testCaseString.append(",\"" +tcStartValues.get(currentStartValue)+"\"");
						currentStartValue++;
						if(tcStartCounter.size()==(currentStartValue)){break;}
						
					}
					
					testCaseString.append(	"); "+	"\r\n");
		

				}

				if (tcStatementBlock.getStatementByIndex(j) instanceof Done_Statement) {
					Done_Statement tc_doneStatement = (Done_Statement) tcStatementBlock
							.getStatementByIndex(j);

					testCaseString.append("hc.done(\"all component\");"
							+ "\r\n");// TODO: all honnan jön?

				}

			}

		

		testCaseString.append("}" + "\r\n");

		return testCaseString.toString();
	}
	
	public String getJavaSource(){
		createCounter=-1;
		connectCounter=-1;
		startCounter=-1;
		this.writeTestCaseFunction();

		String returnString=testCaseString.toString();
		testCaseString.setLength(0);

		return returnString;
	}

	public void setTcRunsOn(String testCaseRunsOn) {
		this.testCaseRunsOn=testCaseRunsOn;
		
	}

	public void setStatementBlock(StatementBlock tcStatementBlock) {
		this.tcStatementBlock=tcStatementBlock;
		
	}

	public void addVars(String string) {
		tcVars.add(string);
		
	}

	public void addVarTypes(String string) {
		tcVarTypes.add(string);
		
	}

	public void addAssignValues(String string) {
		tcAssignValues.add(string);
		
	}

	public void addCreateValues(String string) {
		tcCreateValues.add(string);
		
	}
	
	public void addCreateCounter(String string) {
		tcCreateCounter.add(string);
		
	}

	public void addConnectValues(String string) {
		tcConnectValues.add(string);
		
	}

	public void addConnectCounter(String string) {
		tcConnectCounter.add(string);
		
	}

	public void addStartValues(String string) {
		tcStartValues.add(string);
		
	}

	public void addStartCounter(String string) {
		tcStartCounter.add(string);
		
	}


}
