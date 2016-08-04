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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.titan.codegenerator.javagen.*;

public abstract class ComponentDef extends ModuleDef{
    public boolean created = false; //state variable
    
    public BlockingQueue<Boolean> donequeue = new ArrayBlockingQueue<Boolean>(1024);
    public String name;
    public Thread thread;
	public abstract void prepareforconnection(String thisport, int thisportnum);
	public abstract void domap(String thisport, String remotecomp, String remoteport);
	public abstract void connect(String port, String ip, String portnum);
    public HCType hc; 
    public String compid;
	public BlockingQueue<Boolean> queue = new ArrayBlockingQueue<Boolean>(1024);
    private int verdict; //0-none, 1-pass, 2-inconc, 3-fail, 4-error
    public BlockingQueue<Boolean> lockqueue = new ArrayBlockingQueue<Boolean>(1024);
    
    public synchronized void lock(){
    	while(!lockqueue.isEmpty()){
    		try{
    			lockqueue.take();
    		}catch(Exception e){}
    	}
    }
    
    public void unlock(){
    	lockqueue.add(true);
    }
    
    public ComponentDef(String n) {
    	name = n;
        setVerdict("none");
    }

    public void setVerdict(String s) {
        int v = -1;
        if (s == "none") v = 0;
        if (s == "pass") v = 1;
        if (s == "inconc") v = 2;
        if (s == "fail") v = 3;
        if (s == "error") v = 4;
        if (v > verdict) verdict = v;
    }
    
    public int getVerdictInt(){
    	return verdict;
    }
    
    public String getVerdict(){
    	if(verdict==0) return "none";
    	if(verdict==1) return "pass";
    	if(verdict==2) return "inconc";
    	if(verdict==3) return "fail";
    	if(verdict==4) return "error";
    	return "";
    }
}
