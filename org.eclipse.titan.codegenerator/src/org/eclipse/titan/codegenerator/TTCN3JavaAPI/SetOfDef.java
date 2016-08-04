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

import java.util.HashSet;
import java.util.Iterator;

public abstract class SetOfDef<T extends TypeDef> extends StructuredTypeDef {
    public HashSet<T> value;
    
    public String toString(){
    	return toString("");
    }
    
    public String toString(String tabs){
		if(anyField) return "?";
		if(omitField) return "omit";
		if(anyOrOmitField) return "*";
    	String retv = "[";
    	Iterator<T> i = value.iterator();
    	while(i.hasNext()){
    		retv += i.next().toString(tabs);
    		if(i.hasNext()) retv += ",";
    	}
    	retv += "]";
    	return retv;
    }
}