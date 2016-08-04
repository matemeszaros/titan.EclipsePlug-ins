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

public class BOOLEAN extends Comparable<BOOLEAN>{

    Boolean value;

    public BOOLEAN() {

    }
    
    public BOOLEAN(boolean value){
        this.value = value;
    }
	
    public BOOLEAN(BOOLEAN value){
        this.value = value.getValue();
    }
    
    public BOOLEAN equalsWith(BOOLEAN aBoolean) {
        return new BOOLEAN(value == aBoolean.value);
    }

    public BOOLEAN not(){
        return new BOOLEAN(!value);
    }

    public BOOLEAN and(BOOLEAN b){
        return new BOOLEAN(value && b.value);
    }

    public BOOLEAN or(BOOLEAN b){
        return new BOOLEAN(value || b.value);
    }

    public BOOLEAN xor(BOOLEAN b) {
        return new BOOLEAN(value ^ b.value);
    }
    
	public String toString(){
		return toString("");
	}
	
	public Boolean getValue(){
		return this.value;
	}
	
	public String toString(String tabs){
		if(anyField) return "?";
		if(omitField) return "omit";
		if(anyOrOmitField) return "*";
		return Boolean.toString(value);
	}
	
    public static boolean match(BOOLEAN pattern, BOOLEAN message){
    	if(!(message instanceof BOOLEAN)) return false;
    	if(pattern.omitField&&((BOOLEAN)message).omitField) return true;
    	if(pattern.anyOrOmitField) return true;
    	if(pattern.anyField&&!((BOOLEAN)message).omitField) return true;
    	if(pattern.omitField&&!((BOOLEAN)message).omitField) return false;
    	if(pattern.anyField&&((BOOLEAN)message).omitField) return false;
    	return (pattern.value.equals(((BOOLEAN)message).value));
    }
	
}
