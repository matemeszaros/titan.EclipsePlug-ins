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

import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

public class Def_Type_Integer {
	private Def_Type typeNode;
	private StringBuilder integerString = new StringBuilder("");
	private CompilationTimeStamp compilationCounter = CompilationTimeStamp
			.getNewCompilationCounter();
	private boolean isValueRangeSet=false;
	private int minValue;
	private int maxValue;
	private String nodeName=null;
		
	private static Map<String, Object> integerHashes = new LinkedHashMap<String, Object>();


	private Def_Type_Integer(Def_Type typeNode) {
		super();
		this.typeNode = typeNode;
		nodeName = typeNode.getIdentifier().toString();
	}

	public static Def_Type_Integer getInstance(Def_Type typeNode) {
		if (!integerHashes.containsKey(typeNode.getIdentifier().toString())) {
			integerHashes.put(typeNode.getIdentifier().toString(),  new Def_Type_Integer(typeNode));
		}
		return (Def_Type_Integer) integerHashes.get(typeNode.getIdentifier().toString());
	}

	public void addMinMaxFields(int min, int max){
		isValueRangeSet=true;
		minValue=min;
		maxValue=max;
	}
	
	private void writeConstructor() {
		integerString.append(nodeName + "("
				+ "INTEGER" + " val){");
		integerString.append("\r\n" +"super(val);");

		integerString.append("\r\n"	+ "allowedIntervals.add(new SubTypeInterval<INTEGER>(new INTEGER("+minValue+"),new INTEGER("+maxValue+")));\r\n");
		integerString.append("\r\n" + "	" + "	" + "checkValue();");
		integerString.append("\r\n	}\r\n");
	}
	private void writeMatcher() {
		integerString.append("public static boolean match("
				+ nodeName + " pattern, " + "Object "
				+ " message){" + "\r\n"); 
		integerString.append("if(!(message instanceof "
				+ nodeName + ")) return false;"
				+ "\r\n");
		integerString.append("	return INTEGER.match(pattern.value, (("
				+ nodeName + ")message).value);"
				+ "\r\n");
		integerString.append("}" + "\r\n");
		
	}

	private void writeEquals() {
		integerString.append("public boolean equals("
				+ nodeName + " v){\r\n"); 
		integerString.append("	return value.equals(v.value);\r\n");
		integerString.append("}\r\n");
		
	}
	
	public String getJavaSource(){
		integerString.append("class " + nodeName
				+ " extends SubTypeDef<INTEGER>{"
				+ "\r\n");
		this.writeConstructor();
		this.writeMatcher();
		this.writeEquals();
		integerString.append("\r\n}");
		String returnString=integerString.toString();
		integerString.setLength(0);
		minValue=0;
		maxValue=0;
		isValueRangeSet=false;
		return returnString;
	}



}
