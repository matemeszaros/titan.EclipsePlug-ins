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

public class BITSTRING extends BINARY_STRING implements Indexable<BITSTRING> {

    public BITSTRING(){
    }

    public BITSTRING(String inputvalue) {
        super(inputvalue);
        Integer.parseInt(inputvalue, 2); //Throws an exception if not legal
    }
    
    public BITSTRING bitwiseNot(){
    	return new BITSTRING(Integer.toBinaryString(generalBitwiseNot(fromBitString(value))));
    }    

    public BITSTRING bitwiseAnd(BITSTRING b){
    	return new BITSTRING(Integer.toBinaryString(generalBitwiseAnd(fromBitString(value), fromBitString(b.value))));
    }
    
    public BITSTRING bitwiseOr(BITSTRING b){
    	return new BITSTRING(Integer.toBinaryString(generalBitwiseOr(fromBitString(value), fromBitString(b.value))));
    }
    
    public BITSTRING bitwiseXor(BITSTRING b){
    	return new BITSTRING(Integer.toBinaryString(generalBitwiseXor(fromBitString(value), fromBitString(b.value))));
    }
    
	public String toString() {
		return toString("");
	}
	
	public String toString(String tabs){
		if(anyField) return "?";
		if(omitField) return "omit";
		if(anyOrOmitField) return "*";
		return "'" + new String(value) + "'B";
	}

	@Override
	public BITSTRING get(int index) {
		return new BITSTRING(new String(new byte[]{value[index]}));
	}

	@Override
	public void set(int index, BITSTRING bitstring) {
		value[index] = bitstring.value[0];
	}
}
