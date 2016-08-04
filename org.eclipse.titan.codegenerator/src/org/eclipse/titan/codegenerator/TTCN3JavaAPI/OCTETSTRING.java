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

public class OCTETSTRING extends BINARY_STRING implements Indexable<OCTETSTRING> {

    public OCTETSTRING() {
    }

    public OCTETSTRING(String value) {
        super(value);
        Integer.parseInt(value, 16); //throws an exception if not legal
    }
    
    public OCTETSTRING bitwiseNot(){
    	return new OCTETSTRING(Integer.toOctalString(generalBitwiseNot(fromOctetString(value))));
    }    

    public OCTETSTRING bitwiseAnd(OCTETSTRING b){
    	return new OCTETSTRING(Integer.toOctalString(generalBitwiseAnd(fromOctetString(value), fromOctetString(b.value))));
    }
    
    public OCTETSTRING bitwiseOr(OCTETSTRING b){
    	return new OCTETSTRING(Integer.toOctalString(generalBitwiseOr(fromOctetString(value), fromOctetString(b.value))));
    }
    
    public OCTETSTRING bitwiseXor(OCTETSTRING b){
    	return new OCTETSTRING(Integer.toOctalString(generalBitwiseXor(fromOctetString(value), fromOctetString(b.value))));
    }
    
	public String toString() {
		return toString("");
	}
	
	public String toString(String tabs){
		if(anyField) return "?";
		if(omitField) return "omit";
		if(anyOrOmitField) return "*";
		return "O'" + new String(value) + "'";
	}

	@Override
	public OCTETSTRING get(int index) {
		return new OCTETSTRING(new String(new byte[]{value[2 * index], value[2 * index + 1]}));
	}

	@Override
	public void set(int index, OCTETSTRING octetstring) {
		value[2 * index] = octetstring.value[0];
		value[2 * index + 1] = octetstring.value[1];
	}

	// TODO : create a unit-test from it
	public static void main(String[] args) {
		OCTETSTRING o = new OCTETSTRING("AABBCC");
		System.out.println(o);
		System.out.println(o.get(0));
		System.out.println(o.get(1));
		System.out.println(o.get(2));
		o.set(0, new OCTETSTRING("DD"));
		System.out.println(o.get(0));
		System.out.println(o);
	}
}
