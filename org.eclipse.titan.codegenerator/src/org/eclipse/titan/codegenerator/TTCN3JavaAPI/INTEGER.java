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

import java.math.BigInteger;

public class INTEGER extends Arithmetical<INTEGER> {

	BigInteger value;

    public INTEGER() {

    }
    
    public INTEGER(BigInteger value) {
        this.value = value;
    }

    public INTEGER negate() {
        return new INTEGER(value.multiply(new BigInteger("-1")));
    }

    public INTEGER rem(INTEGER divider){
    	BigInteger val = value.divide(divider.value.multiply((value.divide(divider.value))));
        return new INTEGER(val);
    }

    public INTEGER mod(INTEGER divider){
        if(value.compareTo( BigInteger.valueOf(0)) >= 0) {
            if (divider.value.compareTo( BigInteger.valueOf(0)) < 0) {
                return rem(new INTEGER(divider.value.negate()));
            } else {
                return rem(divider);
            }
        } else if (value.compareTo( BigInteger.valueOf(0)) < 0 && rem(this).equalsWith(new INTEGER(BigInteger.valueOf(0))).value) {
            return new INTEGER(BigInteger.valueOf(0));
        } else {
            return new INTEGER(divider.value.negate()).plus(rem(new INTEGER(divider.value.negate())));
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
    		return new BOOLEAN(true);
    	}else{
    		return new BOOLEAN(false);
    	}
    }

    public BOOLEAN isGreaterOrEqualThan(INTEGER integer) {
    	if(value.compareTo(integer.value)>=0){
    		return new BOOLEAN(true);
    	}else{
    		return new BOOLEAN(false);
    	}
    }

    public BOOLEAN isLessThan(INTEGER integer) {
    	if(value.compareTo(integer.value)==-1){
    		return new BOOLEAN(true);
    	}else{
    		return new BOOLEAN(false);
    	}
    }

    public BOOLEAN isLessOrEqualThan(INTEGER integer) {
    	if(value.compareTo(integer.value)<=0){
    		return new BOOLEAN(true);
    	}else{
    		return new BOOLEAN(false);
    	}
    }

    public BOOLEAN equalsWith(INTEGER integer) {
        return new BOOLEAN(value.equals(integer.value));
    }

    public BOOLEAN notEqualsWith(INTEGER integer) {
        return new BOOLEAN(!value.equals(integer.value));
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
		return new BOOLEAN(this.value.equals(v.value));
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
    
}
