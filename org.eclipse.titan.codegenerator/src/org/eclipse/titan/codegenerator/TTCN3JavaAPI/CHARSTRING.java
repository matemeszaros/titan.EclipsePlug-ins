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

public class CHARSTRING extends STRING implements Indexable<CHARSTRING> {

	public static final CHARSTRING ANY = new CHARSTRING();
	public static final CHARSTRING OMIT = new CHARSTRING();
	public static final CHARSTRING ANY_OR_OMIT = new CHARSTRING();

	static {
		ANY.anyField = true;
		OMIT.omitField = true;
		ANY_OR_OMIT.anyOrOmitField = true;
	}

    public CHARSTRING(String value) {
    	super(value);
    	//TODO check whether it really is a charstring
    }

    public CHARSTRING() {
    	super();
    }
    
	public String toString() {
		return toString("");
	}
	
	
	public INTEGER str2int(){
		return new INTEGER(value.toString());
	}
	
	//needed even though tabs is not used, since otherwise the method of STRING would run and return null
	public String toString(String tabs){
		if(anyField) return "?";
		if(omitField) return "omit";
		if(anyOrOmitField) return "*";
		return "\"" + new String(value) + "\"";
	}

	@Override
	public CHARSTRING get(int index) {
		return new CHARSTRING(new String(new byte[]{value[index]}));
	}

	@Override
	public void set(int index, CHARSTRING charstring) {
		value[index] = charstring.value[0];
	}
}
