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

package org.eclipse.titan.codegenerator.TTCN3JavaAPI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class MCType {
	
	//tables: HCs(listeners, writers, ready, hcips); TComps(componentnames, componentips) 
	
	int ptcid;
	Vector<HCListener> listeners; //listener threads
	Vector<BufferedWriter> writers; //writers for writing outsockets
	Vector<String> componentnames; //component names belonging to listeners/writers
	Vector<String> componentips; //component IP addresses
	Vector<Boolean> ready; //readers are initialised for all threads
	Vector<String> hcips; //host controller IP addresses
	Vector<String> compstobedone; //Number of those components that have to be done.
	Vector<Integer> verdicts;
	boolean allHCsConnected = false;
	boolean debugmode;

	private class HCListener extends Thread {
		private MCType mc;
		private Socket sock; 
		private BufferedReader reader;
		private boolean debugmode;
				
		public HCListener(MCType mcont, Socket socket, boolean debugmode){
			mc=mcont;
			sock=socket;
			this.debugmode=debugmode;
		}
		
		private void sendtocomponenthc(String compname, String message){
			try{
				int index = getcomponenthcindex(compname);
				writers.get(index).write(message + "\r\n");
				writers.get(index).flush();
			}catch(Exception e){e.printStackTrace();}
		}

		private int getcomponenthcindex(String compname){
			for(int i=0;i<componentnames.size();i++)
				if(componentnames.get(i).equals(compname)){
					for(int j=0;j<hcips.size();j++)
						if(hcips.get(j).equals(componentips.get(i))){
							return j;					
						}
				}
			return -1;
		}
				
		private void sendtohc(String ipaddr, String message){
			for(int i=0;i<hcips.size();i++)
				if(hcips.get(i).equals(ipaddr)){
					try{
						if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "MC->HC: " + message, false);
						writers.get(i).write(message + "\r\n");
						writers.get(i).flush();
					}catch(Exception e){e.printStackTrace();}
				}
		}
		
		public void run(){
			try{
				reader=new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
			}catch(Exception e){}
			mc.ready.add(true);
			try{
				for(;;){
					if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "Main Controller waiting for message", false);
					String message = reader.readLine();
					if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "Main Controller received " + message, false);
					if(message==null) break; //for some reason it does not break when receiving finished mtc but reads another line which is null.
					if(message.equals("quit")) break;
					if(message.equals("connected")){
						sendtocomponenthc("mtc", "connected");
					}
					if(message.equals("disconnected")){ //NEW
						sendtocomponenthc("mtc", "disconnected");
					}
					if(message.equals("mapped")){
						sendtocomponenthc("mtc", "mapped");
					}
					String command = message.split(" ")[0];
					if(command.equals("create")){
						String compname = message.split(" ")[1];
						String comptype = message.split(" ")[2];
						String ipaddr = message.split(" ")[3];
						sendtohc(ipaddr, "create " + compname + " " + comptype + " " + Integer.toString(mc.getPTCID()));
						if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "CREATE command forwarded to  Host Controller on " + ipaddr, false);
						componentnames.add(compname);
						componentips.add(ipaddr);
					}
					if(command.equals("connect")){
						String comp1 = message.split(" ")[1];
						String port1 = message.split(" ")[2];
						String comp2 = message.split(" ")[3];
						String port2 = message.split(" ")[4];
						if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "Preparing test component port " + comp1 + ":" + port1 + " for connection as RESPONDER", false);
						sendtocomponenthc(comp1, "prepareforconnection " + comp1 + " " + port1 + " " + comp2 + " " + port2);
					}
					if(command.equals("disconnect")){ //NEW
						String comp1 = message.split(" ")[1];
						String port1 = message.split(" ")[2];
						String comp2 = message.split(" ")[3];
						String port2 = message.split(" ")[4];
						if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "D test component port " + comp1 + ":" + port1 + " from " + comp2 + ":" + port2, false);
						sendtocomponenthc(comp1, "disconnect " + comp1 + " " + port1 + " " + comp2 + " " + port2);
					}
					if(command.equals("map")){
						String comp1 = message.split(" ")[1];
						String port1 = message.split(" ")[2];
						String comp2 = message.split(" ")[3];
						String port2 = message.split(" ")[4];
						if(comp1.equals("system"))sendtocomponenthc(comp2, "map " + comp2 + " " + port2 + " " + comp1 + " " + port1);
						if(comp2.equals("system"))sendtocomponenthc(comp1, "map " + comp1 + " " + port1 + " " + comp2 + " " + port2);
					}
					if(command.equals("preparedforconnection")){ //this case is defined separately since a different thread will get this message back than the one that has initiated it. 
						String comp2 = message.split(" ")[1];
						String port2 = message.split(" ")[2];
						String portnum = message.split(" ")[3];
						String comp1 = message.split(" ")[4];
						String comp1ip = hcips.get(getcomponenthcindex(comp1));
						if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "Preparing test component port " + comp2 + ":" + port2 + " for connection as INITIATOR", false);
						sendtocomponenthc(comp2, "connect " + comp2 + " " + port2 + " " + comp1ip + " " + portnum);
					}
					if(command.equals("start")){
						String component = message.split(" ")[1];
						if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "Starting function on component " + component, false);
						sendtocomponenthc(component, message);
					}
					if(command.equals("done")){ //mtc hcjatol
						if(message.equals("done all component")){
							for(String cn:componentnames)
								compstobedone.add(cn);
							for(String ip:hcips)
								sendtohc(ip, message);
							if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "Adding components to be done " + componentnames, false);
						}//otherwise a single component is waited for to be done at one time. this results in blocking
						else{
							String what = message.split(" ")[1];
							sendtocomponenthc(what, message);
							compstobedone.add(what);
							if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "Adding component to be done " + what, false);
						}
					}
					if(command.equals("finished")){
						String component = message.split(" ")[1];
						int verdict = Integer.parseInt(message.split(" ")[2]);
						verdicts.add(verdict);
						if(component.equals("mtc")){
							//stopping hcs
							for(String ip:hcips){
								sendtohc(ip, "quit");
							}
							TTCN3Logger.writeLog("mc", "EXECUTOR", "MTC finished. Global verdict: " + mc.getGlobalVerdict(), true);
							break;
						}else{
							compstobedone.remove(component);
							TTCN3Logger.writeLog("mc", "EXECUTOR", "Component " + component + " is done. Remaining components to be done: " + compstobedone, false);
							if(compstobedone.size()==1) if(compstobedone.get(0).equals("mtc")) sendtocomponenthc("mtc", "finished"); //all components which had to be done are done
						}
					}
				}
				sock.close();
			}catch(Exception e){e.printStackTrace();}
		}

	}
	
	public synchronized int getPTCID(){
		ptcid++;
		return ptcid-1;
	}
	
	public MCType(boolean debugmode){
		listeners = new Vector<HCListener>();
		writers = new Vector<BufferedWriter>();
		componentnames = new Vector<String>();
		componentips = new Vector<String>();
		ready = new Vector<Boolean>();
		hcips = new Vector<String>();
		compstobedone = new Vector<String>();
		verdicts = new Vector<Integer>();
		ptcid=3; //IDs of PTCs start from 3
		this.debugmode=debugmode;
	}
	
    public String getGlobalVerdict(){
    	int verdict=-1;
    	for(int i=0;i<verdicts.size();i++){
    		int v;
    		if(i==0) verdict=verdicts.get(i);
    		else if((v=verdicts.get(i))>verdict) verdict=v;
    	}
    	switch (verdict){
    	case 0: return "none";
    	case 1: return "pass";
    	case 2: return "inconc";
    	case 3: return "fail";
    	case 4: return "error";
    	}
    	return null;
    }

		
	public void startmc(int serverportnum, String mtcip, String tc, int hcnum){
		TTCN3Logger.writeLog("mc", "EXECUTOR", "Main Controller started.", false);
		try{
			ServerSocket ssock = new ServerSocket(serverportnum);
			if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "Waiting for " + hcnum + " Host Controller(s) to connect on port " + serverportnum, false);
			for(int i=0;i<hcnum;i++){ //waiting for connecting HCs
				Socket sock = ssock.accept();
				HCListener hcl = new HCListener(this,sock,debugmode);
				this.listeners.add(hcl);
				String ia = sock.getInetAddress().toString().substring(1);
				hcips.add(ia);
				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
				writers.add(w);
				hcl.start();
				if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "Host Controller " + Integer.toString(i) + " connected.", false);
			}
			for(;this.ready.size()<hcnum;); //blocks until all listeners are initialized
			for(int i=0;i<hcips.size();i++){
				if(hcips.get(i).equals(mtcip)){
					writers.get(i).write("execute " + tc + "\r\n"); //sending execute to the given IP
					writers.get(i).flush();
					if(debugmode)TTCN3Logger.writeLog("mc", "EXECUTOR", "Sending execute for test case " + tc + " to Host Controller on " + mtcip, false);
					componentnames.add("mtc");
					componentips.add(hcips.get(i));
					break;
				}
			}

			for(HCListener l:listeners) l.join();
			ssock.close();
		}catch(Exception e){e.printStackTrace();}
	}
	
}

