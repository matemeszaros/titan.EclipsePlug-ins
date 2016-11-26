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

import java.util.HashMap;

public class ENUMERATED extends Relational<ENUMERATED> {

	protected String value;
	public HashMap<String,Integer> values;
	
	public void setValue(String v) throws IndexOutOfBoundsException{
		if(values.keySet().contains(v)) value=v;
		else{
			throw new IndexOutOfBoundsException("Illegal ENUMERATED value.");
		}
	}
	
	public ENUMERATED(){
		values = new HashMap<String,Integer>();
	}
	
	public String getValue(){
		return value;
	}
	
    public BOOLEAN isGreaterThan(ENUMERATED enumerated) {
        return BOOLEAN.FALSE;
    }

    public BOOLEAN isGreaterOrEqualThan(ENUMERATED enumerated) {
        return BOOLEAN.FALSE;
    }

    public BOOLEAN isLessThan(ENUMERATED enumerated) {
        return BOOLEAN.FALSE;
    }

    public BOOLEAN isLessOrEqualThan(ENUMERATED enumerated) {
        return BOOLEAN.FALSE;
    }

    public BOOLEAN equalsWith(ENUMERATED enumerated) {
        return BOOLEAN.FALSE;
    }
    
    public static boolean match(ENUMERATED pattern, Object message){
    	if(!(message instanceof ENUMERATED)) return false;
    	if(pattern.omitField&&((ENUMERATED)message).omitField) return true;
    	if(pattern.anyOrOmitField) return true;
    	if(pattern.anyField&&!((ENUMERATED)message).omitField) return true;
    	if(pattern.omitField&&!((ENUMERATED)message).omitField) return false;
    	if(pattern.anyField&&((ENUMERATED)message).omitField) return false;
    	return (pattern.getValue().equals(((ENUMERATED)message).getValue()));
    }
    
	public BOOLEAN equals(ENUMERATED v){
		return BOOLEAN.valueOf(this.value.equals(v.getValue()));
	}
	
	public String toString() {
		return toString("");
	}
	
	public String toString(String tabs){
		return getValue();
	}
	
    public void checkValue() throws IndexOutOfBoundsException {
    	return;
    }

    
}
