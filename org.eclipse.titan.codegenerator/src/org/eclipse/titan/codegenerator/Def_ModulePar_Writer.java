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

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;

public class Def_ModulePar_Writer {
	private Def_ModulePar moduleParNode;

	private StringBuilder moduleParString = new StringBuilder("");

	private String nodeName = null;

	public List<String> moduleParValues = new ArrayList<String>();
	private String moduleParNodeType = null;

	private static Map<String, Object> moduleParHashes = new LinkedHashMap<String, Object>();

	private Def_ModulePar_Writer(Def_ModulePar node) {
		super();
		this.moduleParNode = node;
		nodeName = moduleParNode.getIdentifier().toString();
	}

	
	
	public static Def_ModulePar_Writer getInstance(Def_ModulePar node) {
		if (!moduleParHashes.containsKey(node.getIdentifier().toString())) {
			moduleParHashes.put(node.getIdentifier().toString(),
					new Def_ModulePar_Writer(node));
		}
		return (Def_ModulePar_Writer) moduleParHashes.get(node.getIdentifier()
				.toString());
	}

	public void setModuleParNodeType(String value) {
		moduleParNodeType = value;
	}

	public void writeModuleParConstructor(String rootNodeType,
			String prefix) {
		
		if(moduleParValues.size()>0){
			for(int i=0;i<moduleParValues.size();i++){
					moduleParString.append("	value=new "+rootNodeType+"(new BigInteger(\""+moduleParValues.get(i)+"\"));"+"\r\n");
			}
		}
		
		

	}
	
	public void clearLists() {
		moduleParValues.clear();
	}

	public String getJavaSource() {

		AstWalkerJava.logToConsole("	Starting processing:  ModulePar " + nodeName );
		
		moduleParString.append("public static " + moduleParNodeType + " " + nodeName
				+ "(){\r\n");
		moduleParString.append(moduleParNodeType + " value;\r\n");

		writeModuleParConstructor(moduleParNodeType, "value");
		moduleParString.append("return value;\r\n");
		moduleParString.append("}\r\n");
		String returnString = moduleParString.toString();

		moduleParString.setLength(0);


		
		AstWalkerJava.logToConsole("	Finished processing:  ModulePar " + nodeName );
		return returnString;
	}
}
