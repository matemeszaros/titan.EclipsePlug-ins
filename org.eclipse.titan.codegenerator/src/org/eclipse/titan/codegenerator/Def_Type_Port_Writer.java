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

//only supports inout messages
public class Def_Type_Port_Writer {
	private SourceCode code = new SourceCode();

	public List<String> inMessageName = new ArrayList<String>();
	public List<String> outMessageName = new ArrayList<String>();
	public List<String> inOutMessageName = new ArrayList<String>();

	private boolean isPortTypeAReferencedType = false;
	private boolean isPortInternal = false;
	private String nodeName = null;

	private static Map<String, Object> portHashes = new LinkedHashMap<String, Object>();

	private Def_Type_Port_Writer(Def_Type typeNode) {
		super();
		nodeName = typeNode.getIdentifier().toString();

	}

	public static Def_Type_Port_Writer getInstance(Def_Type typeNode) {
		if (!portHashes.containsKey(typeNode.getIdentifier().toString())) {
			portHashes.put(typeNode.getIdentifier().toString(),
					new Def_Type_Port_Writer(typeNode));
		}
		return (Def_Type_Port_Writer) portHashes.get(typeNode.getIdentifier()
				.toString());
	}

	public void setPortTypeAReferencedType(boolean isPortTypeAReferencedType) {
		this.isPortTypeAReferencedType = isPortTypeAReferencedType;
	}

	private void writeExternalPorts() {
		code.indent(1).line("private TP_", nodeName, " testport = new TP_", nodeName, "(this);");
	}

	private void writeReceive() {
		for (String type : inMessageName) {
			code.newLine();
			receiveMatching(type);
		}
		for (String type : outMessageName) {
			code.newLine();
			receiveMatching(type);
		}
		for (String type : inOutMessageName) {
			code.newLine();
			receiveMatching(type);
		}
	}

	private void receiveMatching(String type) {
		code.indent(1).line("public ", type, " receive(", type, " s, boolean take) {");
		code.indent(2).line("while (!mapped) {");
		code.indent(3).line("if (component.hc.debugmode) TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Port not mapped, retrying RECEIVE operation\", false);");
		code.indent(3).line("try { Thread.sleep(1000); } catch (Exception e) {}");
		code.indent(2).line("}");
		code.indent(2).line("if (inBuffer.isEmpty()) return null;");
		if (isPortTypeAReferencedType) {
			code.indent(2).line("if (!(inBuffer.get(0) instanceof ", type, ")) return null;");
		}
		code.indent(2).line("boolean matches = ", type, ".match(s, inBuffer.get(0));");
		code.indent(2).line("if (matches) {");
		code.indent(3).line(type, " msg = (", type, ") inBuffer.get(0);");
		code.indent(3).line("if (take) {");
		code.indent(4).line("TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Received \" + msg, false);");
		code.indent(4).line("inBuffer.remove(0);");
		code.indent(3).line("}");
		code.indent(3).line("return msg;");
		code.indent(2).line("}");
		code.indent(2).line("return null;");
		code.indent(1).line("}");
	}

	private void writeTypedReceive() {
		for (String type : inMessageName) {
			code.newLine();
			receiveType(type);
		}
		for (String type : outMessageName) {
			code.newLine();
			receiveType(type);
		}
		for (String type : inOutMessageName) {
			code.newLine();
			receiveType(type);
		}
	}

	private void receiveType(String type) {
		code.indent(1).line("public ", type, " receive_", type, "(boolean take) {");
		code.indent(2).line("while (!mapped) {");
		code.indent(3).line("if (component.hc.debugmode) TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Port not mapped, retrying RECEIVE operation\", false);");
		code.indent(3).line("try { Thread.sleep(1000); } catch (Exception e) {}");
		code.indent(2).line("}");
		code.indent(2).line("if (inBuffer.isEmpty()) return null;");
		code.indent(2).line("if (inBuffer.get(0) instanceof ", type, ") {");
		code.indent(3).line(type, " msg = (", type, ") inBuffer.get(0);");
		code.indent(3).line("if (take) {");
		code.indent(4).line("TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Received \" + msg, false);");
		code.indent(4).line("inBuffer.remove(0);");
		code.indent(3).line("}");
		code.indent(3).line("return msg;");
		code.indent(2).line("}");
		code.indent(2).line("return null;");
		code.indent(1).line("}");
	}

	private void writeObjectReceive() {
		code.newLine();
		code.indent(1).line("public Object receive(boolean take){");
		code.indent(2).line("while (!mapped) {");
		code.indent(3).line("if (component.hc.debugmode) TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Port not mapped, retrying RECEIVE operation\", false);");
		code.indent(3).line("try { Thread.sleep(1000); } catch (Exception e) {}");
		code.indent(2).line("}");
		code.indent(2).line("if (!inBuffer.isEmpty()) {");
		code.indent(3).line("Object msg = inBuffer.get(0);");
		code.indent(3).line("if (take) {");
		code.indent(4).line("TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Received \" + msg, false);");
		code.indent(4).line("inBuffer.remove(0);");
		code.indent(3).line("}");
		code.indent(3).line("return msg;");
		code.indent(2).line("}");
		code.indent(2).line("return null;");
		code.indent(1).line("}");
	}

	private void send() {
		code.newLine();
		code.indent(1).line("public void send(Object o) {");
		code.indent(2).line("while (!mapped) {");
		code.indent(3).line("if (component.hc.debugmode) TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Port not mapped, retrying RECEIVE operation\", false);");
		code.indent(3).line("try { Thread.sleep(1000); } catch (Exception e) {}");
		code.indent(2).line("}");
		code.indent(2).line("try {");
		if (!isPortInternal) {
			code.indent(3).line("testport.user_send(o);");
		} else {
			code.indent(3).line("owriter.writeObject(o);");
		}
		code.indent(2).line("} catch (Exception e) { e.printStackTrace(); }");
		code.indent(1).line("}");
	}

	private void writeConstructor() {
		code.newLine();
		code.indent(1).line("public ", nodeName, "(ComponentDef c, String name) {");
		code.indent(2).line("super(c, name);");
		code.indent(2).line("inBuffer = new ArrayList<Object>();");
		for (String i : inMessageName) {
			code.indent(2).line("inMessages.add(\"" + i + "\");");
		}
		for (String o : outMessageName) {
			code.indent(2).line("outMessages.add(\"" + o + "\");");
		}
		for (String io : inOutMessageName) {
			code.indent(2).line("inMessages.add(\"" + io + "\");");
			code.indent(2).line("outMessages.add(\"" + io + "\");");
		}
		code.indent(2).line("created=true;");
		code.indent(1).line("}");
	}

	private void writePrepareforconnection() {
		code.newLine();
		code.indent(1).line("ObjectOutputStream owriter;");
		code.indent(1).line("Thread listener;");
		code.newLine();
		code.indent(1).line("public void prepareforconnection (int thisportnum) {");
		code.indent(2).line("if (component.hc.debugmode) TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Preparing for port connection -- Creating buffer daemon\", false);");
		code.indent(2).line("listener = new BufferDaemon(this, thisportnum, false);");
		code.indent(2).line("component.hc.portlistenerpool.add(listener);");
		code.indent(2).line("if (component.hc.debugmode) TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Preparing for port connection -- Starting buffer daemon thread\", false);");
		code.indent(2).line("listener.start();");
		code.indent(1).line("}");
	}

	private void writeConnect() {
		code.newLine();
		code.indent(1).line("public void connect(String ip, int remoteportnum) {");
		code.indent(2).line("if (component.hc.debugmode) TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Establishing port connection -- Creating buffer daemon\", false);");
		code.indent(2).line("listener = new BufferDaemon(this, ip, remoteportnum, true);");
		code.indent(2).line("component.hc.portlistenerpool.add(listener);");
		code.indent(2).line("if (component.hc.debugmode) TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Establishing port connection -- Creating buffer daemon\", false);");
		code.indent(2).line("listener.start();");
		code.indent(1).line("}");
	}
	
	private void writeDisconnect() {
		code.newLine();
		code.indent(1).line("public void disconnect(String comp2, String port2) {");
		code.indent(1).line("try {");
		code.indent(2).line("owriter.writeObject(null);");
		code.indent(2).line("component.hc.portlistenerpool.remove(listener);");
		code.indent(1).line("} catch (Exception e) { e.printStackTrace(); }");
		code.indent(1).line("}");
	}

	private void writeMap() {
		code.newLine();
		code.indent(1).line("public void map(String remotecomp, String remoteport) {");
		if (!isPortInternal) { // TODO check if needed!
			code.indent(2).line("testport.user_map(remotecomp, remoteport);");
		}
		code.indent(1).line("}");
	}

	private void writeBufferDaemon() {
		// TODO migrate this to SourceCode
		StringBuilder portString = new StringBuilder();

		portString.append("class BufferDaemon extends Thread{ " + "\r\n");
		portString.append("	private " + nodeName + " port;" + "\r\n");
		portString.append("	private int portnum;" + "\r\n");
		portString.append("	private String ip;" + "\r\n");
		portString.append("	private boolean isinitiator;" + "\r\n");
		portString.append("	public BufferDaemon(" + nodeName
				+ " p, int pnum, boolean init){" + "\r\n");
		portString.append("		port = p;" + "\r\n");
		portString.append("		portnum = pnum;" + "\r\n");
		portString.append("		isinitiator = init;" + "\r\n");
		portString
				.append("		if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + port.name + \"--buffer-daemon\", \"PORTEVENT\", \"Buffer daemon started. Listening on port \" + portnum + \", in \" + (isinitiator?\"INITIATOR\":\"RESPONDER\") + \" mode\", false);"
						+ "\r\n");

		portString.append("	}" + "\r\n");
		portString.append("	public BufferDaemon(" + nodeName
				+ " p, String ipaddr, int pnum, boolean init){" + "\r\n");
		portString.append("		this(p,pnum,init);" + "\r\n");
		portString.append("		ip=ipaddr;" + "\r\n");
		portString.append("	}" + "\r\n");
		portString.append("	public void run(){" + "\r\n");
		portString.append("		ServerSocket ssock = null;" + "\r\n");
		portString.append("		Socket sock = null;" + "\r\n");
		portString.append("		if(!isinitiator){" + "\r\n");
		portString.append("			try{" + "\r\n");
		portString
				.append("				if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + port.name + \"--buffer-daemon\", \"PORTEVENT\", \"Buffer daemon waiting for connection on port \" + portnum, false);"
						+ "\r\n");
		portString.append("				ssock = new ServerSocket(portnum);" + "\r\n");
		portString.append("				sock = ssock.accept();" + "\r\n");
		portString
				.append("				if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + port.name + \"--buffer-daemon\", \"PORTEVENT\", \"Buffer daemon accepted connection on port \" + portnum, false);"
						+ "\r\n");
		portString.append("			}catch(Exception e){e.printStackTrace();}"
				+ "\r\n");
		portString.append("		}else{" + "\r\n");
		portString.append("			boolean scanning = true;" + "\r\n");
		portString
				.append("			if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + port.name + \"--buffer-daemon\", \"PORTEVENT\", \"Buffer daemon initiating connection on port \" + portnum, false);"
						+ "\r\n");
		portString.append("			while(scanning){" + "\r\n");
		portString.append("				try{" + "\r\n");
		portString.append("					sock = new Socket(ip,portnum);" + "\r\n");
		portString.append("					scanning = false;" + "\r\n");
		portString
				.append("					if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + port.name + \"--buffer-daemon\", \"PORTEVENT\", \"Buffer daemon connectied on port \" + portnum, false);"
						+ "\r\n");
		portString.append("				}catch(Exception e){e.printStackTrace();}"
				+ "\r\n");
		portString.append("			}" + "\r\n");
		portString.append("		}" + "\r\n");
		portString.append("		try{" + "\r\n");
		portString
				.append("			port.owriter = new ObjectOutputStream(sock.getOutputStream());"
						+ "\r\n");
		portString
				.append("			ObjectInputStream oreader = new ObjectInputStream(sock.getInputStream());"
						+ "\r\n");
		portString.append("			port.mapped = true;" + "\r\n");
		portString.append("			for(;;){" + "\r\n");
		portString
				.append("				if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + port.name + \"--buffer-daemon\", \"PORTEVENT\", \"Buffer daemon waiting\", false);"
						+ "\r\n");

		portString.append("				Object o = oreader.readObject(); " + "\r\n");
		portString.append("				if(o==null) break; " + "\r\n");
		portString
				.append("				if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + port.name + \"--buffer-daemon\", \"PORTEVENT\", \"Buffer daemon received message\", false);"
						+ "\r\n");
		portString.append("				port.enqueue(o); " + "\r\n");

		portString.append("			}" + "\r\n");
		portString.append("			if(!isinitiator) ssock.close();" + "\r\n");
		portString.append("			sock.close();" + "\r\n");
		portString.append("		}catch(Exception e){e.printStackTrace();}"
				+ "\r\n");
		portString.append("	}" + "\r\n");
		portString.append("}" + "\r\n");

		code.newLine();
		code.append(portString);
	}

	public void clearLists() {
		isPortTypeAReferencedType = false;
		isPortInternal = false;

		inMessageName.clear();
		outMessageName.clear();
		inOutMessageName.clear();
	}

	public String getJavaSource() {
		code.clear();
		AstWalkerJava.logToConsole("	Starting processing:  Port " + nodeName);
		code.line("public class ", nodeName, " extends MessagePortDef {");

		isPortInternal = myASTVisitor.portNamePortTypeHashMap.get(nodeName).equals("TP_INTERNAL");
		if (!isPortInternal) {
			this.writeExternalPorts();
			Additional_Class_Writer.writeExternalPortClass(nodeName);
		}
		this.writeReceive();
		this.writeTypedReceive();
		this.writeObjectReceive();
		this.send();
		this.writeConstructor();
		this.writePrepareforconnection();
		this.writeConnect();
		this.writeDisconnect();
		this.writeMap();
		this.writeBufferDaemon();
		code.line("}");
		AstWalkerJava.logToConsole("	Finished processing:  Port " + nodeName);
		return code.toString();
	}

}
