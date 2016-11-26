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

public class BOOLEAN extends Comparable<BOOLEAN>{

	public static final BOOLEAN ANY = new BOOLEAN();
	public static final BOOLEAN OMIT = new BOOLEAN();
	public static final BOOLEAN ANY_OR_OMIT = new BOOLEAN();

	static {
		ANY.anyField = true;
		OMIT.omitField = true;
		ANY_OR_OMIT.anyOrOmitField = true;
	}

    Boolean value;

	public static final BOOLEAN TRUE = new BOOLEAN(true);
	public static final BOOLEAN FALSE = new BOOLEAN(false);

	public static BOOLEAN valueOf(boolean b) {
		return b ? TRUE : FALSE;
	}
    
    public BOOLEAN() {

    }
    
    public BOOLEAN(boolean value){
        this.value = value;
    }
	
    public BOOLEAN(BOOLEAN value){
        this.value = value.getValue();
    }
    
	public BOOLEAN equals(BOOLEAN aBoolean){
		return BOOLEAN.valueOf(this.value.equals(aBoolean.value));
	}
    
    public BOOLEAN equalsWith(BOOLEAN aBoolean) {
        return valueOf(value == aBoolean.value);
    }

    public BOOLEAN not(){
        return valueOf(!value);
    }

    public BOOLEAN and(BOOLEAN b){
        return valueOf(value && b.value);
    }

    public BOOLEAN or(BOOLEAN b){
        return valueOf(value || b.value);
    }

    public BOOLEAN xor(BOOLEAN b) {
        return valueOf(value ^ b.value);
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
	
	public static boolean match(BOOLEAN pattern, Object object) {
		if (!(object instanceof BOOLEAN)) return false;
		BOOLEAN message = (BOOLEAN) object;
		if (pattern.omitField && message.omitField) return true;
		if (pattern.anyOrOmitField) return true;
		if (pattern.anyField && !message.omitField) return true;
		if (pattern.omitField && !message.omitField) return false;
		if (pattern.anyField && message.omitField) return false;
		return (pattern.value.equals(message.value));
	}
    
    public void checkValue() throws IndexOutOfBoundsException {
    	return;
    }

}
