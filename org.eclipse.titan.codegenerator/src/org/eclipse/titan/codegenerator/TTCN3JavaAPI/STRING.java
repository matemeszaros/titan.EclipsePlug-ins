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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class STRING extends Comparable<STRING>{

    public byte[] value;
    protected List<STRING> allowedValues; //allowed values of subtype
    protected List<SubTypeInterval<STRING>> allowedIntervals; //allowed intervals of subtype
    public int minlength; //minimal length of subtype
    public int maxlength; //maximal length of subtype, remains -1 for infinity
    //length of subtype is always set as an upper and lower bound, even if only 

    public STRING() {
    	value = null;
    	minlength=-1;
    	maxlength=-1;
        allowedValues = new ArrayList<STRING>();
        allowedIntervals = new ArrayList<SubTypeInterval<STRING>>();
    }

    public STRING(String s) {
    	this();
    	this.value = s.getBytes();
    }

    public STRING concatenate(STRING other){
    	return new STRING(this.value.toString() + other.value.toString());
    }

    public BOOLEAN equalsWith(STRING string) {
    	return BOOLEAN.valueOf(this.value.equals(string.value));
    }

    //converts the input from INTEGER to int!!!
    public STRING rotateLeft(INTEGER by){
        byte[] copy = new byte[value.length];
        int byValue = by.value.intValue() % value.length;
        System.arraycopy(value,byValue,copy,0,value.length-byValue);
        System.arraycopy(value,0,copy,value.length-byValue,byValue);
        return new STRING(new String(copy));
    }

    //converts the input from INTEGER to int!!!
    public STRING rotateRight(INTEGER by) {
        int byValue = by.value.intValue() % value.length;
        return rotateLeft(new INTEGER(BigInteger.valueOf(value.length-byValue)));
    }
    
    public static boolean match(STRING pattern, Object message){
    	if(!(message instanceof STRING)) return false;
    	if(pattern.omitField&&((STRING)message).omitField) return true;
    	if(pattern.anyOrOmitField) return true;
    	if(pattern.anyField&&!((STRING)message).omitField) return true;
    	if(pattern.omitField&&!((STRING)message).omitField) return false;
    	if(pattern.anyField&&((STRING)message).omitField) return false;
        return (pattern.equals(((STRING)message))).getValue();
    }
    
	public BOOLEAN equals(STRING v){
		for(int i=0;i<value.length;i++)
			if(this.value[i]!=v.value[i]) return BOOLEAN.FALSE;
		return BOOLEAN.TRUE;
	}

	/* these are never used, only needed because STRING cannot be an abstract class due to its operator methods
	 * the results of which on the other hand, will always be cast to the corresponding child classes
	 */
	
	public String toString(String tabs) {
		return null;
	}

	public String toString() {
		return null;
	}
	
    public void checkValue() throws IndexOutOfBoundsException {
        if (minlength!=-1)
        	if(value.length<minlength||value.length>maxlength&&maxlength!=-1)
                throw new IndexOutOfBoundsException("inappropriate subtype length!");
    	if (allowedValues.size() == 0 && allowedIntervals.size() == 0)
            return;
        for (SubTypeInterval<STRING> i : allowedIntervals)
            if(i.checkValue(this)) return;
        for (STRING i : allowedValues)
            if (i.equals(value)) return;
        throw new IndexOutOfBoundsException("out of intervals!");
    }

}
