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
		hcString.append("	public static void main(String args[]){" + "\r\n");
		hcString.append("		hc=new HCType();" + "\r\n");
		hcString.append("		hc.starthc(MCIP,SERVERPORTNUM);" + "\r\n");
		hcString.append("		System.out.println(\"HC stopped\");" + "\r\n");
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
		hcTypeString.append("	" + "\r\n");
		hcTypeString.append("	public HCType(){" + "\r\n");
		hcTypeString.append("		componentpool = new Vector<ComponentDef>();"
				+ "\r\n");
		hcTypeString.append("		portlistenerpool = new Vector<Thread>();"
				+ "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		hcTypeString
				.append("	public void connect(String comp1, String port1, String comp2, String port2){"
						+ "\r\n");
		hcTypeString.append("		this.waitingforconnect=true;" + "\r\n");
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
		hcTypeString
				.append("	public void start(String component, String function){"
						+ "\r\n");
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
		hcTypeString.append("	public void sendtomc(String message){" + "\r\n"); // hc.sendtomc-kent
																				// hivodik,
																				// igy
																				// nem
																				// hc.writer-kent
																				// hivatkozik
																				// a
																				// writerre.
																				// itt
																				// mar
																				// hc-ban
																				// (az
																				// instance-ban)
																				// vagyunk
		hcTypeString.append("		try{" + "\r\n");
		hcTypeString.append("			writer.write(message + \"\\r\\n\");" + "\r\n");
		hcTypeString.append("			writer.flush();" + "\r\n");
		hcTypeString.append("			System.out.println(\"HC->MC: \" + message);"
				+ "\r\n");
		hcTypeString.append("		}catch(Exception e){}" + "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		hcTypeString
				.append("	public void registercomponent(String name, String type){"
						+ "\r\n");


		for (int i = 0; i < myASTVisitor.componentList.size(); i++) {
			hcTypeString.append("		if(type.equals(\"" + myASTVisitor.componentList.get(i)
					+ "\")) componentpool.add(new " + myASTVisitor.componentList.get(i)
					+ "(this,name));" + "\r\n");
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
		hcTypeString
				.append("			System.out.println(\"DQ2 \" + getcomponent(\"mtc\").donequeue.size());"
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
		hcTypeString.append("		System.out.println(\"HC started.\");" + "\r\n");
		hcTypeString.append("		try{" + "\r\n");
		hcTypeString.append("			sock = new Socket(mcIp,serverportnum);"
				+ "\r\n"); // for outgoing signal messages
		hcTypeString.append("			System.out.println(\"Connected to MC.\");"
				+ "\r\n");
		hcTypeString
				.append("			writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));"
						+ "\r\n"); // ezt bekommentezed, elszall
		hcTypeString
				.append("			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));"
						+ "\r\n");
		hcTypeString
				.append("			System.out.println(\"HC waiting for message...\");"
						+ "\r\n");
		hcTypeString.append("			for(;;){" + "\r\n");
		hcTypeString.append("				String msg = reader.readLine();" + "\r\n");
		hcTypeString
				.append("				System.out.println(\"HC received message: \" + msg);"
						+ "\r\n");
		hcTypeString.append("				if(msg.equals(\"quit\")) break;" + "\r\n");
		hcTypeString.append("				if(msg.equals(\"connected\")){" + "\r\n");
		hcTypeString.append("					this.waitingforconnect=false;" + "\r\n");
		hcTypeString.append("				}" + "\r\n");
		hcTypeString.append("				String command = msg.split(\" \")[0];"
				+ "\r\n");
		hcTypeString.append("				if(command.equals(\"execute\")){" + "\r\n");
		hcTypeString.append("					String tcasename = msg.split(\" \")[1];"
				+ "\r\n");


		
		for (int i = 0; i < myASTVisitor.testCaseList.size(); i++) {
			hcTypeString.append("					if(tcasename.equals(\""
					+ myASTVisitor.testCaseList.get(i) + "\")){" + "\r\n"); // minden egyes
																// testcase-re
																// kell ez az if
			hcTypeString.append("						registercomponent(\"mtc\", \""
					+ myASTVisitor.testCaseRunsOnList.get(i) + "\");" + "\r\n"); // masodik
																	// parameter
																	// az adott
																	// tc
																	// runsonja
			hcTypeString.append("						Thread testcasethread = new Thread(new "
					+ myASTVisitor.testCaseList.get(i) + "((" + myASTVisitor.testCaseRunsOnList.get(i)
					+ ")getcomponent(\"mtc\")));" + "\r\n");
			hcTypeString
					.append("						getcomponent(\"mtc\").thread=testcasethread;"
							+ "\r\n");
			hcTypeString.append("						testcasethread.start();" + "\r\n");
			hcTypeString.append("					}" + "\r\n");
		}

		hcTypeString.append("				}" + "\r\n");
		hcTypeString.append("				if(command.equals(\"create\")){" + "\r\n");
		hcTypeString.append("					String compname = msg.split(\" \")[1];"
				+ "\r\n");
		hcTypeString.append("					String comptype = msg.split(\" \")[2];"
				+ "\r\n");
		hcTypeString.append("					registercomponent(compname, comptype);"
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
		hcTypeString
				.append("					getcomponent(component).connect(port,ip,portnum);"
						+ "\r\n"); // cast az adott nevu komponens tipusara megy
									// mindig
		hcTypeString.append("					sendtomc(\"connected\");" + "\r\n");
		hcTypeString.append("				}" + "\r\n");
		hcTypeString.append("				if(command.equals(\"start\")){" + "\r\n");
		hcTypeString
				.append("					ComponentDef component = getcomponent(msg.split(\" \")[1]);"
						+ "\r\n");
		hcTypeString.append("					String function = msg.split(\" \")[2];"
				+ "\r\n");


		for (int i = 0; i < myASTVisitor.functionList.size(); i++) {
			hcTypeString.append("					if(function.equals(\""
					+ myASTVisitor.functionList.get(i) + "\")){ " + "\r\n");// minden letezo
																// fuggvenynevre
																// kell ilyen if
			hcTypeString
					.append("						if(component.thread!=null) component.thread.join();"
							+ "\r\n"); // ha fut rajta valami, elõbb joinolni
										// kell
			hcTypeString.append("						Thread functionthread = new Thread(new "
					+ myASTVisitor.functionList.get(i) + "((" + myASTVisitor.functionRunsOnList.get(i)
					+ ")component));" + "\r\n");
			hcTypeString.append("						component.thread=functionthread;"
					+ "\r\n");
			hcTypeString.append("						functionthread.start();" + "\r\n");
			hcTypeString.append("					}" + "\r\n");
		}

		hcTypeString.append("				}" + "\r\n");
		hcTypeString.append("				if(command.equals(\"done\")){" + "\r\n");
		hcTypeString.append("					if(msg.equals(\"done all component\")){"
				+ "\r\n");
		hcTypeString.append("						for(ComponentDef c:componentpool)" + "\r\n");
		hcTypeString.append("							if(!c.name.equals(\"mtc\")){" + "\r\n");
		hcTypeString
				.append("								System.out.println(\"Waiting for component \" + c.name + \" to be done\");"
						+ "\r\n");
		hcTypeString.append("								c.thread.join();" + "\r\n");
		hcTypeString
				.append("								System.out.println(\"Component \" + c.name + \" done\");"
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
		hcTypeString.append("		for(Thread t:portlistenerpool) t.interrupt();"
				+ "\r\n");
		hcTypeString.append("	}" + "\r\n");
		hcTypeString.append("	" + "\r\n");
		hcTypeString.append("}" + "\r\n");

		return hcTypeString.toString();
	}

}
