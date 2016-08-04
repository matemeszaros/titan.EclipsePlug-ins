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
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.TTCN3JavaAPI;

import java.util.List;

public abstract class RecordOfDef<T extends TypeDef> extends StructuredTypeDef {
    public List<T> value;
    
    public BOOLEAN equals(RecordOfDef<T> v) {
        if (this.value.size() != v.value.size()) return new BOOLEAN(false);
        for (int i = 0; i < this.value.size(); i++)
            if (!(this.value.get(i).equals(v.value.get(i)))) return new BOOLEAN(false);
        return new BOOLEAN(true);
    }
    
    public String toString(){
    	return toString("");
    }
    
    public String toString(String tabs){
		if(anyField) return "?";
		if(omitField) return "omit";
		if(anyOrOmitField) return "*";
    	String retv = "[";
    	for(int i=0;i<value.size();i++){
    		retv += value.get(i).toString(tabs);
    		if(i<value.size()-1) retv += ",";
    	}
    	retv += "]";
    	return retv;
    }
}
