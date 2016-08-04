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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MessagePortDef extends PortDef {
    public Set<String> inMessages; //input type names of port type 
    public Set<String> outMessages; //output type names of port type
    protected boolean created = false;
    public boolean mapped = false;
    public MessagePortDef mappedto;

    public List<Object> inBuffer; //input buffer

    public MessagePortDef(ComponentDef c, String name) {
        component = c;
        this.name=name;
        inMessages = new TreeSet<String>();
        outMessages = new TreeSet<String>();
    }

    public void enqueue(Object o){ //putting incoming messages to inBuffer
    	component.lock();
    	if (!mapped){
    		TTCN3Logger.writeLog(component.name + ":" + name, "PORTEVENT", "Cannot enqueue message--Port not mapped", false);//!uj
        	return;
    	}
    	inBuffer.add(o);
    	component.queue.add(true);
    	component.unlock();
    }
}