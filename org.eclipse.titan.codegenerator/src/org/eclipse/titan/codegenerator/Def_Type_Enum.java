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

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Enumerated_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

public class Def_Type_Enum {
	private Def_Type enumNode;
	private StringBuilder enumString = new StringBuilder("");
	private CompilationTimeStamp compilationCounter = CompilationTimeStamp
			.getNewCompilationCounter();
	private List<String> enumItems = new ArrayList<String>();
	private String nodeName=null;
	
	private static Map<String, Object> enumStringHashes = new LinkedHashMap<String, Object>();

	private Def_Type_Enum(Def_Type typeNode) {
		super();
		this.enumNode = typeNode;
		nodeName = typeNode.getIdentifier().toString();
	}

	public static Def_Type_Enum getInstance(Def_Type typeNode) {
		if (!enumStringHashes.containsKey(typeNode.getIdentifier().toString())) {
			enumStringHashes.put(typeNode.getIdentifier().toString(),
					new Def_Type_Enum(typeNode));
		}
		return (Def_Type_Enum) enumStringHashes.get(typeNode
				.getIdentifier().toString());
	}

	public void addEnumItem(String value) {
		enumItems.add(value);
	}

	private void writeConstructors() {
		enumString.append("public Method(){"+ "\r\n");
		for(int i=0;i<enumItems.size();i++){
			enumString.append("values.put(\""+enumItems.get(i)+"\","+i+");" + "\r\n");
		}
		enumString.append("}");
		
		enumString.append("public Method(String v){"+ "\r\n");
		for(int i=0;i<enumItems.size();i++){
			enumString.append("values.put(\""+enumItems.get(i)+"\","+i+");" + "\r\n");
		}
		enumString.append("setValue(v);}");
	}

	public String getJavaSource() {
		enumString.append("class " + enumNode.getIdentifier().toString()
				+ " extends ENUMERATED{" + "\r\n");
		this.writeConstructors();
		enumString.append("\r\n}");
		String returnString = enumString.toString();
		enumString.setLength(0);
		enumItems.clear();
		return returnString;
	}

}
