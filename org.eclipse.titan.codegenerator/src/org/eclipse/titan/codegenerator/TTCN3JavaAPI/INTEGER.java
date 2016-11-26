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

public class INTEGER extends Arithmetical<INTEGER> {

	public static final INTEGER ANY = new INTEGER();
	public static final INTEGER OMIT = new INTEGER();
	public static final INTEGER ANY_OR_OMIT = new INTEGER();

	static {
		ANY.anyField = true;
		OMIT.omitField = true;
		ANY_OR_OMIT.anyOrOmitField = true;
	}

    protected List<INTEGER> allowedValues; //allowed values of subtype
    protected List<SubTypeInterval<INTEGER>> allowedIntervals; //allowed intervals of subtype
	public BigInteger value;

	public INTEGER() {
        allowedValues = new ArrayList<INTEGER>();
        allowedIntervals = new ArrayList<SubTypeInterval<INTEGER>>();
	}

	public INTEGER(BigInteger value) {
		this.value = value;
	}

	public INTEGER(String value) {
		this(new BigInteger(value));
	}

    public INTEGER negate() {
        return new INTEGER(value.multiply(new BigInteger("-1")));
    }

    public INTEGER rem(INTEGER divider){
    	BigInteger val = value.subtract(divider.value.multiply((value.divide(divider.value))));
        return new INTEGER(val);
    }

    public INTEGER mod(INTEGER divider){
    	
    	if(value.equals(divider.value)){
    		return new INTEGER(BigInteger.valueOf(0));
    	}else if(value.compareTo( BigInteger.valueOf(0)) >= 0) {
            if (divider.value.compareTo( BigInteger.valueOf(0)) < 0) {
                return rem(new INTEGER(divider.value.negate()));
            } else {
                return rem(divider);
            }
        } else if (divider.value.compareTo( BigInteger.valueOf(0)) < 0) {
        	
            return rem(new INTEGER(divider.value.negate())).plus(divider.negate());
        } else {
        	return rem(new INTEGER(divider.value.negate())).plus(divider);
        }
        
    }

    public INTEGER plus(INTEGER integer) {
        return new INTEGER(integer.value.add(value));
    }

    public INTEGER minus(INTEGER integer) {
        return new INTEGER(value.subtract(integer.value));
    }

    public INTEGER multipleBy(INTEGER integer) {
        return new INTEGER(value.multiply(integer.value));
    }

    public INTEGER divideBy(INTEGER integer) {
        return new INTEGER(value.divide(integer.value));
    }

    public BOOLEAN isGreaterThan(INTEGER integer) {
    	if(value.compareTo(integer.value)==1){
    		return BOOLEAN.TRUE;
    	}else{
    		return BOOLEAN.FALSE;
    	}
    }

    public BOOLEAN isGreaterOrEqualThan(INTEGER integer) {
    	if(value.compareTo(integer.value)>=0){
    		return BOOLEAN.TRUE;
    	}else{
    		return BOOLEAN.FALSE;
    	}
    }

    public BOOLEAN isLessThan(INTEGER integer) {
    	if(value.compareTo(integer.value)==-1){
    		return BOOLEAN.TRUE;
    	}else{
    		return BOOLEAN.FALSE;
    	}
    }

    public BOOLEAN isLessOrEqualThan(INTEGER integer) {
    	if(value.compareTo(integer.value)<=0){
    		return BOOLEAN.TRUE;
    	}else{
    		return BOOLEAN.FALSE;
    	}
    }

    public BOOLEAN equalsWith(INTEGER integer) {
        return BOOLEAN.valueOf(value.equals(integer.value));
    }

    public BOOLEAN notEqualsWith(INTEGER integer) {
        return BOOLEAN.valueOf(!value.equals(integer.value));
    }

    public static boolean match(INTEGER pattern, Object message){
    	if(!(message instanceof INTEGER)) return false;
    	if(pattern.omitField&&((INTEGER)message).omitField) return true;
    	if(pattern.anyOrOmitField) return true;
    	if(pattern.anyField&&!((INTEGER)message).omitField) return true;
    	if(pattern.omitField&&!((INTEGER)message).omitField) return false;
    	if(pattern.anyField&&((INTEGER)message).omitField) return false;
    	return (pattern.value.equals(((INTEGER)message).value));
    }
    
	public BOOLEAN equals(INTEGER v){
		return BOOLEAN.valueOf(this.value.equals(v.value));
	}
	
	public CHARSTRING int2str(){
		return new CHARSTRING(value.toString());
	}
	
	public String toString() {
		return toString("");
	}
	
	public String toString(String tabs){
		if(anyField) return "?";
		if(omitField) return "omit";
		if(anyOrOmitField) return "*";
    	return value.toString();
    }
     public void checkValue() throws IndexOutOfBoundsException {
        if (allowedValues.size() == 0 && allowedIntervals.size() == 0)
            return;
        for (SubTypeInterval<INTEGER> i : allowedIntervals)
            if(i.checkValue(this)) return;
        for (INTEGER i : allowedValues)
            if (i.equals(value)) return;
        throw new IndexOutOfBoundsException("out of intervals!");
    }

}
