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

	private Def_Type typeNode;
	private StringBuilder portString = new StringBuilder("");

	public List<String> inMessageName = new ArrayList<String>();
	public List<String> outMessageName = new ArrayList<String>();
	public List<String> inOutMessageName = new ArrayList<String>();

	private boolean isPortTypeAReferencedType = false;
	private boolean isPortInternal = false;
	private String nodeName = null;

	private static Map<String, Object> portHashes = new LinkedHashMap<String, Object>();

	private Def_Type_Port_Writer(Def_Type typeNode) {
		super();
		this.typeNode = typeNode;
		nodeName = this.typeNode.getIdentifier().toString();

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
		portString.append("private TP_" + nodeName + " testport = new TP_"
				+ nodeName + "(this); " + "\r\n");

	}

	private void writeReceive() {
		for (int i = 0; i < inMessageName.size(); i++) {
			portString.append("public " + inMessageName.get(i) + " receive("
					+ inMessageName.get(i) + " s, boolean take){ " + "\r\n");

			portString.append("while(!mapped){" + "\r\n");
			portString
					.append("if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Port not mapped, retrying RECEIVE operation\", false);"
							+ "\r\n");
			portString.append("try{Thread.sleep(1000);}catch(Exception e){}"
					+ "\r\n");
			portString.append("}\r\n");

			portString.append("if(inBuffer.isEmpty()) return null;" + "\r\n");

			if (isPortTypeAReferencedType) {
				portString.append("if(!(inBuffer.get(0) instanceof "
						+ inMessageName.get(i) + ")) return null;" + "\r\n");
			}

			portString.append("boolean matches = " + inMessageName.get(i)
					+ ".match(s,inBuffer.get(0));" + "\r\n");

			portString.append("	if(matches){" + "\r\n");
			portString.append("		" + inMessageName.get(i) + " retv=("
					+ inMessageName.get(i) + ")inBuffer.get(0);" + "\r\n");
			portString.append("		if(take)inBuffer.remove(0);" + "\r\n");
			portString.append("		return retv;" + "\r\n");
			portString.append("	}" + "\r\n");
			portString.append("	return null;" + "\r\n");
			portString.append("}" + "\r\n");
		}
		for (int i = 0; i < outMessageName.size(); i++) {
			portString.append("public " + outMessageName.get(i) + " receive("
					+ outMessageName.get(i) + " s, boolean take){ " + "\r\n");

			portString.append("while(!mapped){" + "\r\n");
			portString
					.append("if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Port not mapped, retrying RECEIVE operation\", false);"
							+ "\r\n");
			portString.append("try{Thread.sleep(1000);}catch(Exception e){}"
					+ "\r\n");
			portString.append("}\r\n");
			portString.append("if(inBuffer.isEmpty()) return null;" + "\r\n");

			if (isPortTypeAReferencedType) {
				portString.append("if(!(inBuffer.get(0) instanceof "
						+ outMessageName.get(i) + ")) return null;" + "\r\n");
			}

			portString.append("boolean matches = " + outMessageName.get(i)
					+ ".match(s,inBuffer.get(0));" + "\r\n");

			portString.append("	if(matches){" + "\r\n");
			portString.append("		" + outMessageName.get(i) + " retv=("
					+ outMessageName.get(i) + ")inBuffer.get(0);" + "\r\n");
			portString.append("		if(take)inBuffer.remove(0);" + "\r\n");
			portString.append("		return retv;" + "\r\n");
			portString.append("	}" + "\r\n");
			portString.append("	return null;" + "\r\n");
			portString.append("}" + "\r\n");
		}
		for (int i = 0; i < inOutMessageName.size(); i++) {
			portString.append("public " + inOutMessageName.get(i) + " receive("
					+ inOutMessageName.get(i) + " s, boolean take){ " + "\r\n");

			portString.append("while(!mapped){" + "\r\n");
			portString
					.append("if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Port not mapped, retrying RECEIVE operation\", false);"
							+ "\r\n");
			portString.append("try{Thread.sleep(1000);}catch(Exception e){}"
					+ "\r\n");
			portString.append("}\r\n");
			portString.append("if(inBuffer.isEmpty()) return null;" + "\r\n");

			if (isPortTypeAReferencedType) {
				portString.append("if(!(inBuffer.get(0) instanceof "
						+ inOutMessageName.get(i) + ")) return null;" + "\r\n");
			}

			portString.append("boolean matches = " + inOutMessageName.get(i)
					+ ".match(s,inBuffer.get(0));" + "\r\n");

			portString.append("	if(matches){" + "\r\n");
			portString.append("		" + inOutMessageName.get(i) + " retv=("
					+ inOutMessageName.get(i) + ")inBuffer.get(0);" + "\r\n");
			portString.append("		if(take)inBuffer.remove(0);" + "\r\n");
			portString.append("		return retv;" + "\r\n");
			portString.append("	}" + "\r\n");
			portString.append("	return null;" + "\r\n");
			portString.append("}" + "\r\n");
		}
	}

	private void writeTypedReceive() {
		for (int i = 0; i < inMessageName.size(); i++) {
			portString.append("public " + inMessageName.get(i) + " receive_"
					+ inMessageName.get(i) + "(boolean take){" + "\r\n");
			portString.append("while(!mapped){" + "\r\n");
			portString
					.append("if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Port not mapped, retrying RECEIVE operation\", false);"
							+ "\r\n");
			portString.append("try{Thread.sleep(1000);}catch(Exception e){}"
					+ "\r\n");
			portString.append("}\r\n");
			portString.append("if(inBuffer.isEmpty()) return null;" + "\r\n");
			portString.append("	if(inBuffer.get(0) instanceof "
					+ inMessageName.get(i) + "){" + "\r\n");
			portString.append("		" + inMessageName.get(i) + " retv=("
					+ inMessageName.get(i) + ")inBuffer.get(0);" + "\r\n");
			portString.append("		if(take)inBuffer.remove(0);" + "\r\n");
			portString.append("		return retv;" + "\r\n");
			portString.append("	}" + "\r\n");
			portString.append("	return null;" + "\r\n");
			portString.append("}" + "\r\n");
		}
		for (int i = 0; i < outMessageName.size(); i++) {
			portString.append("public " + outMessageName.get(i) + " receive_"
					+ outMessageName.get(i) + "(boolean take){" + "\r\n");
			portString.append("while(!mapped){" + "\r\n");
			portString
					.append("if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Port not mapped, retrying RECEIVE operation\", false);"
							+ "\r\n");
			portString.append("try{Thread.sleep(1000);}catch(Exception e){}"
					+ "\r\n");
			portString.append("}\r\n");
			portString.append("if(inBuffer.isEmpty()) return null;" + "\r\n");
			portString.append("	if(inBuffer.get(0) instanceof "
					+ outMessageName.get(i) + "){" + "\r\n");
			portString.append("		" + outMessageName.get(i) + " retv=("
					+ outMessageName.get(i) + ")inBuffer.get(0);" + "\r\n");
			portString.append("		if(take)inBuffer.remove(0);" + "\r\n");
			portString.append("		return retv;" + "\r\n");
			portString.append("	}" + "\r\n");
			portString.append("	return null;" + "\r\n");
			portString.append("}" + "\r\n");
		}
		for (int i = 0; i < inOutMessageName.size(); i++) {
			portString.append("public " + inOutMessageName.get(i) + " receive_"
					+ inOutMessageName.get(i) + "(boolean take){" + "\r\n");
			portString.append("while(!mapped){" + "\r\n");
			portString
					.append("if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Port not mapped, retrying RECEIVE operation\", false);"
							+ "\r\n");
			portString.append("try{Thread.sleep(1000);}catch(Exception e){}"
					+ "\r\n");
			portString.append("}\r\n");
			portString.append("if(inBuffer.isEmpty()) return null;" + "\r\n");
			portString.append("	if(inBuffer.get(0) instanceof "
					+ inOutMessageName.get(i) + "){" + "\r\n");
			portString.append("		" + inOutMessageName.get(i) + " retv=("
					+ inOutMessageName.get(i) + ")inBuffer.get(0);" + "\r\n");
			portString.append("		if(take)inBuffer.remove(0);" + "\r\n");
			portString.append("		return retv;" + "\r\n");
			portString.append("	}" + "\r\n");
			portString.append("	return null;" + "\r\n");
			portString.append("}" + "\r\n");
		}
	}

	private void writeObjectReceive() {
		portString.append("public Object receive(boolean take){" + "\r\n");
		portString.append("while(!mapped){" + "\r\n");
		portString
				.append("if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Port not mapped, retrying RECEIVE operation\", false);"
						+ "\r\n");
		portString.append("try{Thread.sleep(1000);}catch(Exception e){}"
				+ "\r\n");
		portString.append("}\r\n");
		portString.append("	if(!inBuffer.isEmpty()){" + "\r\n");
		portString.append("		Object retv = inBuffer.get(0);" + "\r\n");
		portString.append("		if(take)inBuffer.remove(0);" + "\r\n");
		portString.append("		return retv;" + "\r\n");
		portString.append("	}" + "\r\n");
		portString.append("	return null;" + "\r\n");
		portString.append("}" + "\r\n");

	}

	private void send() {
		portString.append("public void send(" + "Object" + " o){" + "\r\n");
		portString.append("while(!mapped){" + "\r\n");
		portString
				.append("if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Port not mapped, retrying RECEIVE operation\", false);"
						+ "\r\n");
		portString.append("try{Thread.sleep(1000);}catch(Exception e){}"
				+ "\r\n");
		portString.append("}\r\n");
		portString.append("try{ " + "\r\n");
		if (!isPortInternal) {
			portString.append("testport.user_send(o);" + "\r\n");
		} else {
			portString.append("owriter.writeObject(o);" + "\r\n");
		}
		portString.append("}catch(Exception e){e.printStackTrace();}" + "\r\n");

		portString.append("}" + "\r\n");
	}

	private void writeConstructor() {
		portString.append("public " + nodeName
				+ "(ComponentDef c, String name){\r\n");
		portString.append("		super(c,name);" + "\r\n");
		portString.append("		inBuffer = new ArrayList<Object>();" + "\r\n");
		for (int counter = 0; counter < inMessageName.size(); counter++) {
			portString.append("		inMessages.add(\""
					+ inMessageName.get(counter) + "\");" + "\r\n");

		}
		for (int counter = 0; counter < outMessageName.size(); counter++) {
			portString.append("		outMessages.add(\""
					+ outMessageName.get(counter) + "\");" + "\r\n");
		}
		for (int counter = 0; counter < inOutMessageName.size(); counter++) {
			portString.append("		inMessages.add(\""
					+ inOutMessageName.get(counter) + "\");" + "\r\n");
			portString.append("		outMessages.add(\""
					+ inOutMessageName.get(counter) + "\");" + "\r\n");
		}

		portString.append("		created=true;" + "\r\n");
		portString.append("}\r\n");
	}

	private void writePrepareforconnection() {

		portString.append("ObjectOutputStream owriter;" + "\r\n");

		portString
				.append("public void prepareforconnection(int thisportnum) { "
						+ "\r\n");

		portString
				.append("if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Preparing for port connection -- Creating buffer daemon\", false);"
						+ "\r\n");

		portString
				.append("	Thread listener = new BufferDaemon(this, thisportnum, false);"
						+ "\r\n");
		portString.append("	component.hc.portlistenerpool.add(listener);"
				+ "\r\n");

		portString
				.append("if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Preparing for port connection -- Starting buffer daemon thread\", false);"
						+ "\r\n");

		portString.append("	listener.start();" + "\r\n");
		portString.append("}" + "\r\n");
		portString.append("" + "\r\n");
	}

	private void writeConnect() {
		portString.append("public void connect(String ip, int remoteportnum){ "
				+ "\r\n");

		portString
				.append("if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Establishing port connection -- Creating buffer daemon\", false);"
						+ "\r\n");

		portString
				.append("	Thread listener =new BufferDaemon(this, ip, remoteportnum, true);"
						+ "\r\n");
		portString.append("	component.hc.portlistenerpool.add(listener);"
				+ "\r\n");

		portString
				.append("if(component.hc.debugmode)TTCN3Logger.writeLog(component.compid + \":\" + this.name, \"PORTEVENT\", \"Establishing port connection -- Creating buffer daemon\", false);"
						+ "\r\n");

		portString.append("	listener.start();" + "\r\n");
		portString.append("}" + "\r\n");

	}

	private void writeMap() {
		portString
				.append("public void map(String remotecomp, String remoteport){"
						+ "\r\n");
		if (!isPortInternal) { // TODO check if needed!
			portString.append("	testport.user_map(remotecomp, remoteport);"
					+ "\r\n");
		}
		portString.append("}" + "\r\n");
	}

	private void writeBufferDaemon() {

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

	}

	public void clearLists() {
		isPortTypeAReferencedType = false;
		isPortInternal = false;

		inMessageName.clear();
		outMessageName.clear();
		inOutMessageName.clear();
	}

	public String getJavaSource() {

		AstWalkerJava.logToConsole("	Starting processing:  Port " + nodeName);

		portString.append("class " + typeNode.getIdentifier().toString()
				+ " extends MessagePortDef{" + "\r\n");

		isPortInternal = myASTVisitor.portNamePortTypeHashMap.get(nodeName)
				.equals("TP_INTERNAL");
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
		this.writeMap();
		this.writeBufferDaemon();
		portString.append("\r\n}");
		String returnString = portString.toString();
		portString.setLength(0);

		AstWalkerJava.logToConsole("	Finished processing:  Port " + nodeName);

		return returnString;
	}

}
