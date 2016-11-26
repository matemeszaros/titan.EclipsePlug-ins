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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;

public class Def_Const_Writer {
	private Def_Const constNode;

	private StringBuilder constString = new StringBuilder("");
	
	private String nodeName = null;

	public List<String> constValues = new ArrayList<String>();
	private String constNodeType = null;
	private int valueCounter = 0;
	public List<Integer> constParamCount = new ArrayList<Integer>();

	public boolean constValueIsAReference = false;
	private boolean isSetOf = false;
	private static Map<String, Object> constHashes = new LinkedHashMap<String, Object>();

	
	private Def_Const_Writer(Def_Const node) {
		super();
		this.constNode = node;
		nodeName = constNode.getIdentifier().toString();
	}

	public static Def_Const_Writer getInstance(Def_Const node) {
		if (!constHashes.containsKey(node.getIdentifier().toString())) {
			constHashes.put(node.getIdentifier().toString(),
					new Def_Const_Writer(node));
		}
		return (Def_Const_Writer) constHashes.get(node.getIdentifier()
				.toString());
	}



	public void setConstNodeType(String value) {
		constNodeType = value;
	}

	public void writeConstConstructor(String rootNodeType,
			String prefix) {
		//type with children, child values need to be assigned
		if (myASTVisitor.nodeNameChildrenNamesHashMap.containsKey(rootNodeType)) {

			if (!isSetOf) {
				constString.append(prefix + "=new " + rootNodeType + "();\r\n");
			}
			
			String[] childrenNodeNames = myASTVisitor.nodeNameChildrenNamesHashMap
					.get(rootNodeType);
			String[] childrenNodeTypes = myASTVisitor.nodeNameChildrenTypesHashMap
					.get(rootNodeType);

			for (int i = 0; i < childrenNodeNames.length; i++) {

				if (childrenNodeTypes[i].equals("CHARSTRING")) {

					// printvalue
					if (constValues.get(valueCounter).equals("omit")) {
						constString.append(prefix + "." + childrenNodeNames[i]
								+ "= new " + childrenNodeTypes[i] + "();\r\n");
						constString.append(prefix + "." + childrenNodeNames[i]
								+ ".omitField=true;\r\n");

					} else {
						constString.append(prefix + "." + childrenNodeNames[i]
								+ "= new " + childrenNodeTypes[i] + "(\""
								+ constValues.get(valueCounter) + "\");\r\n");
					}

					valueCounter++;
				} else if (childrenNodeTypes[i].equals("INTEGER")) {

					// printvalue
					if (constValues.get(valueCounter).equals("omit")) {
						constString.append(prefix + "." + childrenNodeNames[i]
								+ "= new " + childrenNodeTypes[i] + "();\r\n");
						constString
								.append(prefix + "." + "omitField=true;\r\n");

					} else {

						constString.append(prefix + "." + childrenNodeNames[i]
								+ "= new " + childrenNodeTypes[i] + "(\""
								+ constValues.get(valueCounter) + "\");\r\n");
						
					}

					valueCounter++;

				} else if (childrenNodeTypes[i].equals("FLOAT")) {

					// printvalue
					if (constValues.get(valueCounter).equals("omit")) {
						constString.append(prefix + "." + childrenNodeNames[i]
								+ "= new " + childrenNodeTypes[i] + "();\r\n");
						constString
								.append(prefix + "." + "omitField=true;\r\n");

					} else {

						constString.append(prefix + "." + childrenNodeNames[i]
								+ "= new " + childrenNodeTypes[i] + "("
								+ constValues.get(valueCounter) + ");\r\n");
						
					}

					valueCounter++;

				}else if (myASTVisitor.nodeNameChildrenNamesHashMap
						.containsKey(childrenNodeTypes[i])) {

					// child, novalue

					String prefixBackup = prefix;
					prefix = prefix.concat("." + childrenNodeNames[i]);
					writeConstConstructor(childrenNodeTypes[i],
							 prefix);
					prefix = prefixBackup;

				} else if (myASTVisitor.nodeNameSetOfTypesHashMap
						.containsKey(childrenNodeTypes[i])) {

					// setof

					constString.append(prefix + "." + childrenNodeNames[i]
							+ "=new " + childrenNodeTypes[i] + "();" + "\r\n");

					String nameOfSet = myASTVisitor.nodeNameSetOfTypesHashMap
							.get(childrenNodeTypes[i]);

					String prefixBackup = prefix;
					prefix = "TV_" + nameOfSet;

					if (!myASTVisitor.constOmitHashes
							.contains(childrenNodeNames[i])) {
						constString.append(nameOfSet + " TV_" + nameOfSet
								+ "= new " + nameOfSet + "();\r\n");
						isSetOf=true;
						writeConstConstructor(nameOfSet, 
								prefix);
						isSetOf=false;
						constString.append(prefixBackup+"."+childrenNodeNames[i] + ".value.add(TV_"
								+ nameOfSet + ");\r\n");
					} else {
						isSetOf=true;
						constString.append(prefixBackup + "." + childrenNodeNames[i]+".omitField=true;\r\n");
						isSetOf=false;
					}

					prefix = prefixBackup;

				} else {// nochild, notsimple, notset

					if(myASTVisitor.nodeNameNodeTypeHashMap
							.get(childrenNodeTypes[i]).equals("enum")){
						constString.append(prefix
								+ "."
								+ childrenNodeNames[i]
								+ "= new "
								+ childrenNodeTypes[i]
								+ " (\""
								+  constValues.get(valueCounter) + "\");\r\n");
					} else{
					// printvalue
					constString.append(prefix
							+ "."
							+ childrenNodeNames[i]
							+ "=new "
							+ childrenNodeTypes[i]
							+ "(new "
							+ myASTVisitor.nodeNameNodeTypeHashMap
									.get(childrenNodeTypes[i]) + "(\""
							+ childrenNodeNames[i] + "\"));" + "\r\n");
					}
					valueCounter++;
				}

			}
		} else { //simple type, no child value assignments
			if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(rootNodeType)) {

				String type = myASTVisitor.nodeNameNodeTypeHashMap
						.get(rootNodeType);
				
				
				if (type.equals("CHARSTRING") || type.equals("INTEGER")) {

					if(constValueIsAReference){
						if(myASTVisitor.nodeNameNodeTypeHashMap.containsKey(constValues.get(valueCounter))){
							if(myASTVisitor.nodeNameNodeTypeHashMap.get(constValues.get(valueCounter)).equals("constant")){
								constString.append("value = new "+constNodeType+"("+constValues.get(valueCounter)+".value)"
								+ ";\r\n");
							}
						}

						
						constString.append("value="+constValues.get(valueCounter)
								+ ";\r\n");
					}else{

					constString.append("value=new " + constNodeType + "(new "
							+ type + "(\"" + constValues.get(valueCounter)
							+ "\"));\r\n");
					}
					valueCounter++;
				}
			} else if(constNodeType.equals("INTEGER")){

				if(constValueIsAReference){
					constString.append("value="+constValues.get(valueCounter)
							+ ";\r\n");
				}else{
				constString.append("value=new " + constNodeType + "(\"" + constValues.get(valueCounter)
						+ "\");\r\n");
				}

				valueCounter++;
			} else if(constNodeType.equals("BITSTRING")){
				constString.append("value=new "
						+ "BITSTRING" + "(\"" + constValues.get(valueCounter)
						+ "\");\r\n");

				valueCounter++;
			} else if(constNodeType.equals("CHARSTRING")){
				constString.append("value=new "
						+ "CHARSTRING" + "(\"" + constValues.get(valueCounter)
						+ "\");\r\n");

				valueCounter++;
			} else if(constNodeType.equals("OCTETSTRING")){
				constString.append("value=new "
						+ "OCTETSTRING" + "(\"" + constValues.get(valueCounter)
						+ "\");\r\n");

				valueCounter++;
			} else if(constNodeType.equals("BOOLEAN")){
				constString.append("value=new "
						+ "BOOLEAN" + "(" + constValues.get(valueCounter)
						+ ");\r\n");

				valueCounter++;
			} else if(constNodeType.equals("FLOAT")){
				constString.append("value=new "
						+ "FLOAT" + "(" + constValues.get(valueCounter)
						+ ");\r\n");

				valueCounter++;
			}
		}
	}
	
	public void clearLists(){
		valueCounter = 0;
		
		constValueIsAReference = false;
		isSetOf = false;
		
		constParamCount.clear();
		constValues.clear();
	}

	public String getJavaSource() {

		AstWalkerJava.logToConsole("	Starting processing:  Constant " + nodeName );
		
		constString.append("public static " + constNodeType + " " + nodeName
				+ "(){\r\n");
		constString.append(constNodeType + " value;\r\n");

		writeConstConstructor(constNodeType, "value");
		constString.append("return value;\r\n");
		constString.append("}\r\n");
		String returnString = constString.toString();
		valueCounter = 0;
		constString.setLength(0);
		
		AstWalkerJava.logToConsole("	Finished processing:  Constant " + nodeName );
		return returnString;
	}
}
