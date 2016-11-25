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

public class Def_Type_Component_Writer {
	private Def_Type typeNode;
	private StringBuilder compString = new StringBuilder("");

	public List<String> compFieldPortTypes = new ArrayList<String>();
	public List<String> compFieldPortNames = new ArrayList<String>();
	public List<String> compFieldVarTypes = new ArrayList<String>();
	public List<String> compFieldVarNames = new ArrayList<String>();
	private String nodeName = null;

	private static Map<String, Object> compHashes = new LinkedHashMap<String, Object>();

	private Def_Type_Component_Writer(Def_Type typeNode) {
		super();
		this.typeNode = typeNode;
		nodeName = this.typeNode.getIdentifier().toString();
	}

	public static Def_Type_Component_Writer getInstance(Def_Type typeNode) {
		if (!compHashes.containsKey(typeNode.getIdentifier().toString())) {
			compHashes.put(typeNode.getIdentifier().toString(),
					new Def_Type_Component_Writer(typeNode));
		}
		return (Def_Type_Component_Writer) compHashes.get(typeNode
				.getIdentifier().toString());
	}

	public void writeCompFields() {

		for (int i = 0; i < compFieldPortTypes.size(); i++) {
			compString.append(compFieldPortTypes.get(i) + " "
					+ compFieldPortNames.get(i) + ";\r\n");
		}
		
		for (int i = 0; i < compFieldVarNames.size(); i++) {

			compString.append(compFieldVarTypes.get(i) +" "+compFieldVarNames.get(i) + ";" + "\r\n");
		}
	}

	public void writeConstructor() {
		compString.append("public " + nodeName
				+ "(HCType hcont, String name, String ID){" + "\r\n");
		compString.append("super(name);" + "\r\n");
		compString.append("hc=hcont;" + "\r\n");

		compString
				.append("if(hc.debugmode)TTCN3Logger.writeLog(name, \"PARALLEL\", \"Test component \" + name + \" created.\", false);"
						+ "\r\n");

		for (int i = 0; i < compFieldPortNames.size(); i++) {

			compString.append(compFieldPortNames.get(i) + " =new "
					+ compFieldPortTypes.get(i) + "(this,\""
					+ compFieldPortNames.get(i) + "\");" + "\r\n");
		}
		
		for (int i = 0; i < compFieldVarNames.size(); i++) {

			compString.append(compFieldVarNames.get(i) + " =new "
					+ compFieldVarTypes.get(i) + "();" + "\r\n");
		}

		compString.append("created = true;" + "\r\n");
		compString.append("compid = ID;" + "\r\n");
		compString.append("}" + "\r\n");
	}

	public void writeAnyPortReceive() {

		compString.append("public boolean anyPortReceive(boolean take){"
				+ "\r\n");
		for (int i = 0; i < compFieldPortNames.size(); i++) {

			compString.append("	if(" + compFieldPortNames.get(i)
					+ ".receive(take)!=null)return true;" + "\r\n");

		}

		compString.append("	return false;" + "\r\n");
		compString.append("}" + "\r\n");
	}

	public void writePrepareForConnection() {
		compString.append("@Override" + "\r\n");
		compString
				.append("public void prepareforconnection(String thisport, int thisportnum) {"
						+ "\r\n");
		for (int i = 0; i < compFieldPortNames.size(); i++) {
			compString.append("if(thisport.equals(\"" + compFieldPortNames.get(i)
					+ "\")) " + compFieldPortNames.get(i)
					+ ".prepareforconnection(thisportnum);" + "\r\n");

		}

		compString.append("	" + "\r\n");
		compString.append("}" + "\r\n");
	}

	public void writeConnect() {
		compString.append("" + "\r\n");
		compString.append("@Override" + "\r\n");
		compString
				.append("public void connect(String port, String ip, String portnum) {"
						+ "\r\n");

		for (int i = 0; i < compFieldPortNames.size(); i++) {
			compString.append("if(port.equals(\"" + compFieldPortNames.get(i)
					+ "\")) " + compFieldPortNames.get(i)
					+ ".connect(ip, Integer.parseInt(portnum));" + "\r\n");

		}

		compString.append("	" + "\r\n");
		compString.append("}" + "\r\n");
	}
	
	public void writeDisconnect() {
		compString.append("" + "\r\n");
		compString.append("@Override" + "\r\n");
		compString
				.append("public void disconnect(String port1, String comp2, String port2) {"
						+ "\r\n");

		for (int i = 0; i < compFieldPortNames.size(); i++) {
			compString.append("if(port1.equals(\"" + compFieldPortNames.get(i)
					+ "\")) " + compFieldPortNames.get(i)
					+ ".disconnect(comp2, port2);" + "\r\n");

		}

		compString.append("	" + "\r\n");
		compString.append("}" + "\r\n");
	}


	public void writeDomap() {
		compString.append("" + "\r\n");
		compString.append("@Override" + "\r\n");
		compString
				.append("public void domap(String thisport, String remotecomp, String remoteport) {"
						+ "\r\n");

		// TODO
		for (int i = 0; i < compFieldPortTypes.size(); i++) {
			if (myASTVisitor.nodeNameNodeTypeHashMap.get(compFieldPortTypes.get(i))
					.equals("port")) {
				if (!(myASTVisitor.portNamePortTypeHashMap.get(compFieldPortTypes
						.get(i)).equals("TP_INTERNAL"))) {
					compString.append("if(thisport.equals(\""
							+ compFieldPortNames.get(i) + "\")) "
							+ compFieldPortNames.get(i)
							+ ".map(remotecomp, remoteport);" + "\r\n");
				}
			}
		}

		compString.append("	" + "\r\n");
		compString.append("}" + "\r\n");
	}

	public void clearLists() {
		compFieldPortTypes.clear();
		compFieldPortNames.clear();
		compFieldVarTypes.clear();
		compFieldVarNames.clear();
	}

	public String getJavaSource() {

		AstWalkerJava.logToConsole("	Starting processing:  Component "
				+ nodeName);

		compString.append("class " + nodeName + " extends ComponentDef{"
				+ "\r\n");
		this.writeCompFields();
		this.writeConstructor();
		this.writeAnyPortReceive();
		this.writePrepareForConnection();
		this.writeConnect();
		this.writeDisconnect();
		this.writeDomap();
		compString.append("\r\n}");

		String returnString = compString.toString();
		compString.setLength(0);

		AstWalkerJava.logToConsole("	Finished processing:  Component "
				+ nodeName);

		return returnString;
	}
}
