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

public class HEXSTRING extends BINARY_STRING {

    public HEXSTRING() {
    }

    public HEXSTRING(String value) {
        super(value);
        Integer.parseInt(value, 16); //throws an exception if not legal
    }
    
    public HEXSTRING bitwiseNot(){
    	return new HEXSTRING(Integer.toHexString(generalBitwiseNot(fromHexString(value))));
    }

    public HEXSTRING bitwiseAnd(HEXSTRING b){
    	return new HEXSTRING(Integer.toHexString(generalBitwiseAnd(fromHexString(value), fromHexString(b.value))));
    }
    
    public HEXSTRING bitwiseOr(HEXSTRING b){
    	return new HEXSTRING(Integer.toHexString(generalBitwiseOr(fromHexString(value), fromHexString(b.value))));
    }
    
    public HEXSTRING bitwiseXor(HEXSTRING b){
    	return new HEXSTRING(Integer.toHexString(generalBitwiseXor(fromHexString(value), fromHexString(b.value))));
    }
    
	public String toString() {
		return toString("");
	}
	
	public String toString(String tabs){
		if(anyField) return "?";
		if(omitField) return "omit";
		if(anyOrOmitField) return "*";
		return "H'" + new String(value) + "'";
	}

}
