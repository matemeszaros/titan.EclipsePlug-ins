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
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

public class Def_Type_Set {
	private Def_Type typeNode;
	private StringBuilder setString = new StringBuilder("");
	private CompilationTimeStamp compilationCounter = CompilationTimeStamp
			.getNewCompilationCounter();
	private List<String> compFieldTypes = new ArrayList<String>();
	private List<String> compFieldNames = new ArrayList<String>();
	private String nodeName=null;
	
	
	private static Map<String, Object> setHashes = new LinkedHashMap<String, Object>();


	private Def_Type_Set(Def_Type typeNode) {
		super();
		this.typeNode = typeNode;
		nodeName = typeNode.getIdentifier().toString();

	}

	public static Def_Type_Set getInstance(Def_Type typeNode) {
		if (!setHashes.containsKey(typeNode.getIdentifier().toString())) {
			setHashes.put(typeNode.getIdentifier().toString(),  new Def_Type_Set(typeNode));
		}
		return (Def_Type_Set) setHashes.get(typeNode.getIdentifier().toString());
	}

	public void addCompFields(String type, String name){
		compFieldTypes.add(type);
		compFieldNames.add(name);
	}
	
	public void writeCompFields(){
		
		for(int i=0;i<compFieldTypes.size();i++){
			setString.append(compFieldTypes.get(i)+" "+compFieldNames.get(i)+";\r\n");
		}
	}
	
	public void writeMatcher(){

		setString.append("public static boolean match("
				+ nodeName + " pattern, " + "Object"
				+ " message){" + "\r\n");

		setString.append("if(!(message instanceof "
				+ nodeName + ")) return false;" + "\r\n");

		setString.append("if(pattern.omitField&&(("
				+ nodeName
				+ ")message).omitField) return true;" + "\r\n");
		setString.append("if(pattern.anyOrOmitField) return true;" + "\r\n");
		setString.append("if(pattern.anyField&&!(("
				+ nodeName
				+ ")message).omitField) return true;;" + "\r\n"); 
		setString.append("if(pattern.omitField&&!(("
				+ nodeName
				+ ")message).omitField) return false;" + "\r\n");
		setString.append("if(pattern.anyField&&(("
				+ nodeName
				+ ")message).anyField) return false;" + "\r\n");



		setString.append("	return "); //

		for (int i = 0; i < compFieldTypes.size(); i++) {

				setString.append(compFieldTypes.get(i)
						+ ".match(pattern."
						+ compFieldNames.get(i)
						+ ", (("
						+ nodeName
						+ ")message)."
						+compFieldNames.get(i) + ")");

				if ((i + 1 < compFieldTypes.size())){
					setString.append("&&");
				}
				if ((i + 1) == compFieldTypes.size()) {
					setString.append(";\r\n");
				}
			
		}
		setString.append("}" + "\r\n");

		
	}
	
	public void writeEquals(){
		setString.append("public boolean equals("
				+ nodeName + " v){ " + "\r\n");// 0901

		for (int i = 0; i < compFieldTypes.size(); i++) {
	
				setString.append("	if(!" + compFieldNames.get(i)
						+ ".equals(v." + compFieldNames.get(i)
						+ "))return false;" + "\r\n");
			
		}

		setString.append("	return true;" + "\r\n");
		setString.append("}" + "\r\n");
	}
	public void writeToString() {
		setString.append("public String toString(){" + "\r\n");
		setString.append("return toString(\"\");" + "\r\n");
		setString.append("}\r\n");

	}

	public void writeToStringWithParam() {
		setString.append("public String toString(String tabs){" + "\r\n");
		setString.append("if(anyField) return \"?\";" + "\r\n");
		setString.append("if(omitField) return \"omit\";" + "\r\n");
		setString.append("if(anyOrOmitField) return \"*\";" + "\r\n");
		setString.append("return \"{\\n\" + " + "\r\n");

		for (int i = 0; i < compFieldTypes.size(); i++) {
			if((i+1)<compFieldTypes.size()){
				setString.append("tabs + \"\\t\" + \"" + compFieldNames.get(i)
					+ " := \" + " + compFieldNames.get(i)
					+ ".toString(tabs + \"\\t\") + \",\\n\" +" + "\r\n");
			}else{
				setString.append("tabs + \"\\t\" + \"" + compFieldNames.get(i)
						+ " := \" + " + compFieldNames.get(i)
						+ ".toString(tabs + \"\\t\") + \"\\n\" +" + "\r\n");
			}
		}

		setString.append("tabs + \"}\";" + "\r\n");
		setString.append("}\r\n");

	}
	public String getJavaSource(){
		
		setString.append("class " + nodeName
				+ " extends SetDef{"
				+ "\r\n");
		
		this.writeCompFields();
		this.writeMatcher();
		this.writeEquals();
		this.writeToString();
		this.writeToStringWithParam();

		setString.append("\r\n}");
		String returnString=setString.toString();
		setString.setLength(0);
		compFieldTypes.clear();
		compFieldNames.clear();
		return returnString;
	}
}


