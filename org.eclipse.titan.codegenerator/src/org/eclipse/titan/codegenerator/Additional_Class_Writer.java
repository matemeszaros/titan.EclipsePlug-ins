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

public class Additional_Class_Writer {

	public String writeHCClass() {
		StringBuilder hcString = new StringBuilder("\r\n");

		hcString.append("public class HC {" + "\r\n");

		hcString.append("	private static int SERVERPORTNUM = "
				+ AstWalkerJava.props.getProperty("serverportnum") + ";" + "\r\n");
		hcString.append("	private static String MCIP=\"127.0.0.1\";" + "\r\n");

		hcString.append("	private static HCType hc;" + "\r\n");
		hcString.append("	private static boolean DEBUGMODE=false;" + "\r\n");
		hcString.append("	public static void main(String args[]){" + "\r\n");
		hcString.append("		hc=new HCType();" + "\r\n");
		hcString.append("		hc.starthc(MCIP,SERVERPORTNUM);" + "\r\n");
		hcString.append("		TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Host Controller stopped.\", false);" + "\r\n");
		hcString.append("		System.exit(0);" + "\r\n"); 
		hcString.append("	}" + "\r\n");

		hcString.append("}" + "\r\n");

		return hcString.toString();
	}

	public String writeHCTypeClass() {

		StringBuilder hcTypeString = new StringBuilder("");

		hcTypeString.append("public class HCType {" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		hcTypeString.append("	boolean waitingforconnect;" + "\r\n");
		hcTypeString.append("	boolean waitingfordisconnect;" + "\r\n");
		hcTypeString.append("	boolean waitingformap;" + "\r\n");
		hcTypeString.append("	Socket sock;" + "\r\n");
		hcTypeString.append("	BufferedWriter writer;" + "\r\n");
		hcTypeString.append("	BufferedReader reader;" + "\r\n");
		hcTypeString.append("	Vector<ComponentDef> componentpool;" + "\r\n"); // components
																				// run
																				// by
																				// HC
		hcTypeString.append("	Vector<Thread> portlistenerpool;" + "\r\n");
		hcTypeString.append("	int NEXTPORTNUM = 6000; " + "\r\n");// port used
																	// for
																	// opening
																	// sockets
		hcTypeString.append("	public boolean debugmode; " + "\r\n");
		hcTypeString.append("	" + "\r\n");
		
		hcTypeString.append("	public HCType(){" + "\r\n");
		hcTypeString.append("		componentpool = new Vector<ComponentDef>();"
				+ "\r\n");
		hcTypeString.append("		portlistenerpool = new Vector<Thread>();"
				+ "\r\n");
		hcTypeString.append("		this.debugmode=debugmode;"+ "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		
		hcTypeString
				.append("	public void connect(String comp1, String port1, String comp2, String port2){"
						+ "\r\n");
		hcTypeString.append("		this.waitingforconnect=true;" + "\r\n");
		hcTypeString.append("		if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Connecting \" + comp1 + \":\" + port1 + \" to \" + comp2 + \":\" + port2, false);" + "\r\n");
		hcTypeString
				.append("		sendtomc(\"connect \"+comp1+\" \"+ port1+\" \"+comp2+\" \"+port2);"
						+ "\r\n");
		hcTypeString.append("		for(;this.waitingforconnect;){" + "\r\n");
		hcTypeString.append("			try{" + "\r\n");
		hcTypeString.append("				Thread.sleep(100);" + "\r\n");
		hcTypeString.append("			}catch(Exception e){}	" + "\r\n");
		hcTypeString.append("		}" + "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		
		hcTypeString.append("public void disconnect(String comp1, String port1, String comp2, String port2){" + "\r\n");
		hcTypeString.append("	this.waitingfordisconnect=true;" + "\r\n");
		hcTypeString.append("	if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Disconnecting \" + comp1 + \":\" + port1 + \" from \" + comp2 + \":\" + port2, false);" + "\r\n");
		hcTypeString.append("	sendtomc(\"disconnect \"+comp1+\" \"+ port1+\" \"+comp2+\" \"+port2);" + "\r\n");
		hcTypeString.append("	for(;this.waitingfordisconnect;){" + "\r\n");
		hcTypeString.append("		try{" + "\r\n");
		hcTypeString.append("			Thread.sleep(100);" + "\r\n");
		hcTypeString.append("		}catch(Exception e){}	" + "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("}" + "\r\n");
		
		hcTypeString.append("public void map(String comp1, String port1, String comp2, String port2){" + "\r\n");
		hcTypeString.append("	this.waitingformap=true;" + "\r\n");
		hcTypeString.append("	sendtomc(\"map \"+comp1+\" \"+ port1+\" \"+comp2+\" \"+port2);" + "\r\n");
		hcTypeString.append("	for(;this.waitingformap;){" + "\r\n");
		hcTypeString.append("		try{" + "\r\n");
		hcTypeString.append("			Thread.sleep(100);" + "\r\n");
		hcTypeString.append("		}catch(Exception e){}" + "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("}" + "\r\n");
		
		hcTypeString
				.append("	public void start(String component, String function){"
						+ "\r\n");
		hcTypeString.append("		if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Starting function \" + function + \" on component \" + component, false);"+ "\r\n");
		hcTypeString
				.append("		sendtomc(\"start \" + component + \" \" + function);"
						+ "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		hcTypeString
				.append("	public void create(String name, String type, String ip){"
						+ "\r\n");
		hcTypeString
				.append("		sendtomc(\"create \" + name + \" \" + type + \" \" + ip);"
						+ "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		hcTypeString.append("	public void sendtomc(String message){" + "\r\n"); 
		hcTypeString.append("		try{" + "\r\n");
		hcTypeString.append("			writer.write(message + \"\\r\\n\");" + "\r\n");
		hcTypeString.append("			writer.flush();" + "\r\n");
		hcTypeString.append("			TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"HC->MC: \" + message, false);"
				+ "\r\n");
		hcTypeString.append("		}catch(Exception e){}" + "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		hcTypeString
				.append("	public void registercomponent(String name, String type, String ID){"
						+ "\r\n");
		hcTypeString
		.append("	TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Creating PTC \" + name + \" with ID \" + ID, false);"+ "\r\n");

		for (int i = 0; i < AstWalkerJava.componentList.size(); i++) {
			hcTypeString.append("		if(type.equals(\"" + AstWalkerJava.componentList.get(i)
					+ "\")) componentpool.add(new " + AstWalkerJava.componentList.get(i)
					+ "(this,name,ID));" + "\r\n");
		}
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		hcTypeString.append("	private ComponentDef getcomponent(String name){"
				+ "\r\n");
		hcTypeString.append("		for(int i=0;i<componentpool.size();i++)"
				+ "\r\n");
		hcTypeString.append("			if(componentpool.get(i).name.equals(name))"
				+ "\r\n");
		hcTypeString.append("				return componentpool.get(i);" + "\r\n");
		hcTypeString.append("		return null;" + "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		hcTypeString.append("	public void done(String message){ " + "\r\n");// can
																			// only
																			// be
																			// called
																			// in
																			// the
																			// HC
																			// of
																			// mtc
		hcTypeString.append("		sendtomc(\"done \" + message);" + "\r\n");
		hcTypeString.append("		try{" + "\r\n");
		hcTypeString.append("			getcomponent(\"mtc\").donequeue.take();"
				+ "\r\n");

		hcTypeString.append("		}catch(Exception e){}" + "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		hcTypeString
				.append("	public void finished(ComponentDef c, String function){"
						+ "\r\n");
		hcTypeString
				.append("		sendtomc(\"finished \" + c.name + \" \" + Integer.toString(c.getVerdictInt()));"
						+ "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		hcTypeString
				.append("	public void starthc(String mcIp,int serverportnum){"
						+ "\r\n");
		hcTypeString.append("		TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Host Controller started\", false);" + "\r\n");
		hcTypeString.append("		try{" + "\r\n");
		hcTypeString.append("			if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Connecting to Main Controller on IP \" + mcIp + \" port \" + serverportnum, false);" + "\r\n");
		hcTypeString.append("			sock = new Socket(mcIp,serverportnum);"
				+ "\r\n"); // for outgoing signal messages
		hcTypeString.append("			if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Connected to Main Controller\", false);"
				+ "\r\n");
		hcTypeString
				.append("			writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));"
						+ "\r\n"); 
		hcTypeString
				.append("			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));"
						+ "\r\n");
		hcTypeString
				.append("			if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Waiting for control message from Main Controller\", false);"
						+ "\r\n");
		hcTypeString.append("			for(;;){" + "\r\n");
		hcTypeString.append("				String msg = reader.readLine();" + "\r\n");
		hcTypeString
				.append("				if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Received control message \" + msg, false);"
						+ "\r\n");
		hcTypeString.append("				if(msg.equals(\"quit\")) break;" + "\r\n");
		hcTypeString.append("				if(msg.equals(\"connected\")){" + "\r\n");
		hcTypeString.append("					this.waitingforconnect=false;" + "\r\n");
		hcTypeString.append("				}" + "\r\n");
		
		hcTypeString.append("				if(msg.equals(\"disconnected\")){" + "\r\n");
		hcTypeString.append("					this.waitingfordisconnect=false;" + "\r\n");
		hcTypeString.append("					}" + "\r\n");
		
		
		hcTypeString.append("				if(msg.equals(\"mapped\")){" + "\r\n");
		hcTypeString.append("					this.waitingformap=false;" + "\r\n");
		hcTypeString.append("				}" + "\r\n");
		hcTypeString.append("				String command = msg.split(\" \")[0];"
				+ "\r\n");
		hcTypeString.append("				//logged on receiver side instead of default sender side (because a test case might be the entry point of execution)"+ "\r\n");
		hcTypeString.append("				if(command.equals(\"execute\")){" + "\r\n");
		hcTypeString.append("					String tcasename = msg.split(\" \")[1];"
				+ "\r\n");


		
		for (int i = 0; i < AstWalkerJava.testCaseList.size(); i++) {
			hcTypeString.append("					if(tcasename.equals(\""
					+ AstWalkerJava.testCaseList.get(i) + "\")){" + "\r\n"); 
			hcTypeString.append("						if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Creating MTC\", false);" + "\r\n");
			hcTypeString.append("						registercomponent(\"mtc\", \""
					+ AstWalkerJava.testCaseRunsOnList.get(i) + "\", \"2\");" + "\r\n"); 
			hcTypeString.append("						if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Starting new Thread for MTC\", false);" + "\r\n");
			hcTypeString.append("						Thread testcasethread = new Thread(new "
					+ AstWalkerJava.testCaseList.get(i) + "((" + AstWalkerJava.testCaseRunsOnList.get(i)
					+ ")getcomponent(\"mtc\")));" + "\r\n");
			hcTypeString
					.append("						getcomponent(\"mtc\").thread=testcasethread;"
							+ "\r\n");
			hcTypeString.append("						testcasethread.start();" + "\r\n");
			hcTypeString.append("					}" + "\r\n");
		}

		hcTypeString.append("				}" + "\r\n");
		hcTypeString.append("				//logged on receiver side instead of default sender side (because of centrally generated IDs)" + "\r\n");
		
		hcTypeString.append("				if(command.equals(\"create\")){" + "\r\n");
		hcTypeString.append("					String compname = msg.split(\" \")[1];"
				+ "\r\n");
		hcTypeString.append("					String comptype = msg.split(\" \")[2];"
				+ "\r\n");
		hcTypeString.append("					String compid = msg.split(\" \")[3];"+ "\r\n");
		hcTypeString.append("					registercomponent(compname, comptype, compid);"
				+ "\r\n");
		hcTypeString.append("				}" + "\r\n");
		
		hcTypeString.append("				if(command.equals(\"prepareforconnection\")){"
				+ "\r\n");
		hcTypeString.append("					String thiscomp = msg.split(\" \")[1];"
				+ "\r\n");
		hcTypeString.append("					String thisport = msg.split(\" \")[2];"
				+ "\r\n");
		hcTypeString.append("					String remotecomp = msg.split(\" \")[3];"
				+ "\r\n");
		hcTypeString.append("					String remoteport = msg.split(\" \")[4];"
				+ "\r\n");
		hcTypeString.append("					if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Preparing for connection of component \" + thiscomp + \" port \" + thisport + \" and component \" + remotecomp + \" port \" + remoteport, false);"+ "\r\n");
		hcTypeString
				.append("					getcomponent(thiscomp).prepareforconnection(thisport,NEXTPORTNUM);"
						+ "\r\n");
		hcTypeString
				.append("					sendtomc(\"preparedforconnection \" + remotecomp + \" \" + remoteport + \" \" + Integer.toString(NEXTPORTNUM) + \" \" + thiscomp);"
						+ "\r\n");
		hcTypeString.append("					NEXTPORTNUM++;" + "\r\n");
		hcTypeString.append("				}" + "\r\n");
		
		hcTypeString.append("				if(command.equals(\"connect\")){" + "\r\n");
		hcTypeString.append("					String component = msg.split(\" \")[1];"
				+ "\r\n");
		hcTypeString.append("					String port = msg.split(\" \")[2];" + "\r\n");
		hcTypeString.append("					String ip = msg.split(\" \")[3];" + "\r\n");
		hcTypeString.append("					String portnum = msg.split(\" \")[4];"
				+ "\r\n");
		hcTypeString.append("					if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Connecting to component \" + component + \" port \" + port + \" on \" + ip + \":\" + portnum, false);"+ "\r\n");
		hcTypeString
				.append("					getcomponent(component).connect(port,ip,portnum);"
						+ "\r\n"); 
		hcTypeString.append("					sendtomc(\"connected\");" + "\r\n");
		hcTypeString.append("				}" + "\r\n");
		
		hcTypeString.append("			if(command.equals(\"disconnect\")){" + "\r\n");
		hcTypeString.append("				String comp1 = msg.split(\" \")[1];" + "\r\n");
		hcTypeString.append("				String port1 = msg.split(\" \")[2];" + "\r\n");
		hcTypeString.append("				String comp2 = msg.split(\" \")[3];" + "\r\n");
		hcTypeString.append("				String port2 = msg.split(\" \")[4];" + "\r\n");
		hcTypeString.append("				if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Disonnecting component \" + comp1 + \" port \" + port1 + \" from component \" + comp2 + \" port \" + port2, false);" + "\r\n");
		hcTypeString.append("				getcomponent(comp1).disconnect(port1,comp2,port2);" + "\r\n");
		hcTypeString.append("				sendtomc(\"disconnected\");" + "\r\n");
		hcTypeString.append("			}" + "\r\n");
		
		
		
		hcTypeString.append("if(command.equals(\"map\")){" + "\r\n");
		hcTypeString.append("	String thiscomp = msg.split(\" \")[1];" + "\r\n");
		hcTypeString.append("	String thisport = msg.split(\" \")[2];" + "\r\n");
		hcTypeString.append("	String remotecomp = msg.split(\" \")[3];" + "\r\n");
		hcTypeString.append("	String remoteport = msg.split(\" \")[4];" + "\r\n");
		hcTypeString.append("	getcomponent(thiscomp).domap(thisport, remotecomp, remoteport);" + "\r\n");
		hcTypeString.append("	sendtomc(\"mapped\");" + "\r\n");
		hcTypeString.append("}" + "\r\n");
		
		
		hcTypeString.append("				if(command.equals(\"start\")){" + "\r\n");
		hcTypeString
				.append("					ComponentDef component = getcomponent(msg.split(\" \")[1]);"
						+ "\r\n");
		hcTypeString.append("					String function = msg.split(\" \")[2];"
				+ "\r\n");


		for (int i = 0; i < AstWalkerJava.functionList.size(); i++) {
			hcTypeString.append("					if(function.equals(\""
					+ AstWalkerJava.functionList.get(i) + "\")){ " + "\r\n");
			hcTypeString
					.append("						if(component.thread!=null) component.thread.join();"
							+ "\r\n"); 
			hcTypeString.append("						Thread functionthread = new Thread(new "
					+ AstWalkerJava.functionList.get(i) + "((" + AstWalkerJava.functionRunsOnList.get(i)
					+ ")component));" + "\r\n");
			hcTypeString.append("						component.thread=functionthread;"
					+ "\r\n");
			hcTypeString.append("						functionthread.start();" + "\r\n");
			hcTypeString.append("						if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Function f_SENDER started on PTC \" + component, false);" + "\r\n");
			hcTypeString.append("					}" + "\r\n");
		}

		hcTypeString.append("				}" + "\r\n");
		hcTypeString.append("				//logged on receiver side instead of default sender side (because of various done arguments processed here)" + "\r\n"); 
		hcTypeString.append("				if(command.equals(\"done\")){" + "\r\n");
		hcTypeString.append("					if(msg.equals(\"done all component\")){"
				+ "\r\n");
		hcTypeString.append("						for(ComponentDef c:componentpool)" + "\r\n");
		hcTypeString.append("							if(!c.name.equals(\"mtc\")){" + "\r\n");
		
		hcTypeString
				.append("								TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Waiting for component \" + c.name + \" to be done\", false);"
						+ "\r\n");
		hcTypeString.append("								if(c.thread!=null) c.thread.join();" + "\r\n");
		hcTypeString.append("								//logged by mc too" + "\r\n");
		hcTypeString
				.append("								if(debugmode)TTCN3Logger.writeLog(\"hc\", \"EXECUTOR\", \"Component \" + c.name + \" is done\", false);"
						+ "\r\n");
		hcTypeString
				.append("								sendtomc(\"finished \" + c.name + \" \" + Integer.toString(c.getVerdictInt()));"
						+ "\r\n");
		hcTypeString.append("							}" + "\r\n");
		hcTypeString.append("					}" + "\r\n");
		hcTypeString.append("					else{" + "\r\n");
		hcTypeString
				.append("						ComponentDef c = getcomponent(msg.split(\" \")[1]);"
						+ "\r\n");
		hcTypeString.append("						if(!c.name.equals(\"mtc\"))c.thread.join();"
				+ "\r\n");
		hcTypeString
				.append("						sendtomc(\"finished\" + c.name + \" \" + Integer.toString(c.getVerdictInt()));"
						+ "\r\n");
		hcTypeString.append("					}					" + "\r\n");
		hcTypeString.append("				}" + "\r\n");
		hcTypeString.append("				if(command.equals(\"finished\")){" + "\r\n"); // only
		// mtc
		// receives
		// this
		// if
		// all
		// components
		// that
		// have
		// to
		// be
		// done
		// are
		// done
		hcTypeString.append("					getcomponent(\"mtc\").donequeue.add(true);"
				+ "\r\n");
		hcTypeString.append("				}" + "\r\n");
		hcTypeString.append("			}" + "\r\n");
		hcTypeString.append("			this.sock.close();" + "\r\n");
		hcTypeString.append("		}catch(Exception e){e.printStackTrace();}"
				+ "\r\n");
		hcTypeString.append("		for(Thread t:portlistenerpool) if(t!=null) t.interrupt(); "
				+ "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		hcTypeString.append("}" + "\r\n");

		return hcTypeString.toString();
	}

	public static void writeExternalPortClass(String nodeName) {
		StringBuilder externalPort=new StringBuilder();
		
		externalPort.append("package org.eclipse.titan.codegenerator.javagen;"+"\r\n");
		externalPort.append("import java.io.ObjectInputStream;"+"\r\n");
		externalPort.append("import java.io.ObjectOutputStream;"+"\r\n");
		externalPort.append("import java.net.ServerSocket;"+"\r\n");
		externalPort.append("import java.net.Socket;"+"\r\n");
		externalPort.append("import java.util.Scanner; "+"\r\n");
		
		externalPort.append("import org.eclipse.titan.codegenerator.TTCN3JavaAPI.*;"+"\r\n");
		
		externalPort.append("public class TP_"+nodeName+" {"+"\r\n");
		externalPort.append("	private "+nodeName+" port;"+"\r\n");
		externalPort.append("	public TP_"+nodeName+"("+nodeName+" p){"+"\r\n");
		externalPort.append("		port=p;"+"\r\n");
		externalPort.append("	}"+"\r\n");
		
		externalPort.append("	//must block until map is done"+"\r\n");
		externalPort.append("	public void user_map(String remotecomp, String remoteport){"+"\r\n");
		externalPort.append("		new TestPortDaemon(this).start();"+"\r\n");
		externalPort.append("	}"+"\r\n");
		
		externalPort.append("	//must block until unmap is done"+"\r\n");
		externalPort.append("	public void user_unmap(String remotecomp, String remoteport){ "+"\r\n");
		externalPort.append("	}"+"\r\n");
		
		externalPort.append("	public void user_send(Object o){"+"\r\n");
		externalPort.append("	}"+"\r\n");
		
		externalPort.append("	class TestPortDaemon extends Thread{"+"\r\n");
		externalPort.append("		private TP_"+nodeName+" testport;"+"\r\n");
		externalPort.append("		public TestPortDaemon(TP_"+nodeName+" p){"+"\r\n");
		externalPort.append("			testport = p;"+"\r\n");
		externalPort.append("		}"+"\r\n");
		externalPort.append("		public void run(){"+"\r\n");
		externalPort.append("			testport.port.mapped=true;"+"\r\n");
		//externalPort.append("			Scanner scanner = new Scanner(System.in);"+"\r\n");
		externalPort.append("			for(;;){//should block until a new message is received"+"\r\n");
		//externalPort.append("				String inmsg = scanner.nextLine();"+"\r\n");
		//externalPort.append("				testport.port.enqueue(new CHARSTRING(inmsg));"+"\r\n");
		externalPort.append("			}"+"\r\n");
		externalPort.append("		}"+"\r\n");
		externalPort.append("	}"+"\r\n");
		externalPort.append("}"+"\r\n");
		
		String backupFilename=myASTVisitor.currentFileName;
		myASTVisitor.currentFileName="TP_"+nodeName;
		myASTVisitor.visualizeNodeToJava(externalPort.toString());
		myASTVisitor.currentFileName=backupFilename;
		
	}

}
