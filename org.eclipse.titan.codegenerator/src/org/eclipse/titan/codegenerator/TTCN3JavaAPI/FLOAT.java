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

public class FLOAT extends Arithmetical<FLOAT> {

    public Double value;

    public FLOAT() {

    }

    public FLOAT(Double value) {
        this.value = value;
    }

    public FLOAT plus(FLOAT aFloat) {
        return new FLOAT(value + aFloat.value);
    }

    public FLOAT minus(FLOAT aFloat) {
        return new FLOAT(value - aFloat.value);
    }

    public FLOAT multipleBy(FLOAT aFloat) {
        return new FLOAT(value * aFloat.value);
    }

    public FLOAT divideBy(FLOAT aFloat) {
        return new FLOAT(value / aFloat.value);
    }

    public BOOLEAN isGreaterThan(FLOAT aFloat) {
        return new BOOLEAN(value > aFloat.value);
    }

    public BOOLEAN isGreaterOrEqualThan(FLOAT aFloat) {
    	return new BOOLEAN(value >= aFloat.value);
    }

    public BOOLEAN isLessThan(FLOAT aFloat) {
    	return new BOOLEAN(value < aFloat.value);
    }

    public BOOLEAN isLessOrEqualThan(FLOAT aFloat) {
    	return new BOOLEAN(value <= aFloat.value);
    }

    public BOOLEAN equalsWith(FLOAT aFloat) {
    	return new BOOLEAN(value.equals(aFloat.value));
    }

    public BOOLEAN notEqualsWith(FLOAT aFloat) {
    	return new BOOLEAN(!value.equals(aFloat.value));
    }

    public static boolean match(FLOAT pattern, FLOAT message){
    	if(!(message instanceof FLOAT)) return false;
    	if(pattern.omitField&&((FLOAT)message).omitField) return true;
    	if(pattern.anyOrOmitField) return true;
    	if(pattern.anyField&&!((FLOAT)message).omitField) return true;
    	if(pattern.omitField&&!((FLOAT)message).omitField) return false;
    	if(pattern.anyField&&((FLOAT)message).omitField) return false;
    	return (pattern.value.equals(((FLOAT)message).value));
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
