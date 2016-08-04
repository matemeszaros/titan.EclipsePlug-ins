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

import java.util.ArrayList;
import java.util.List;

//class for registering all test components taking part in text execution
public class AllTestComponents {
	
    private static List<ComponentDef> components;

    public static List<ComponentDef> getComponents() {
        return components;
    }

    public static void addComponent(ComponentDef c) {
        if (components == null) components = new ArrayList<ComponentDef>();
        components.add(c);
    }
    
    public static String getGlobalVerdict(){
    	int verdict=-1;
    	for(int i=0;i<components.size();i++){
    		int v;
    		if(i==0) verdict=components.get(i).getVerdictInt();
    		else if((v=components.get(i).getVerdictInt())>verdict) verdict=v;
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
    
}