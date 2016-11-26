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

import java.util.List;

public class FLOAT extends Arithmetical<FLOAT> {

	public static final FLOAT ANY = new FLOAT();
	public static final FLOAT OMIT = new FLOAT();
	public static final FLOAT ANY_OR_OMIT = new FLOAT();

	static {
		ANY.anyField = true;
		OMIT.omitField = true;
		ANY_OR_OMIT.anyOrOmitField = true;
	}

    protected List<FLOAT> allowedValues; //allowed values of subtype
    protected List<SubTypeInterval<FLOAT>> allowedIntervals; //allowed intervals of subtype

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
        return BOOLEAN.valueOf(value > aFloat.value);
    }

    public BOOLEAN isGreaterOrEqualThan(FLOAT aFloat) {
    	return BOOLEAN.valueOf(value >= aFloat.value);
    }

    public BOOLEAN isLessThan(FLOAT aFloat) {
    	return BOOLEAN.valueOf(value < aFloat.value);
    }

    public BOOLEAN isLessOrEqualThan(FLOAT aFloat) {
    	return BOOLEAN.valueOf(value <= aFloat.value);
    }

    public BOOLEAN equalsWith(FLOAT aFloat) {
    	return BOOLEAN.valueOf(value.equals(aFloat.value));
    }

    public BOOLEAN notEqualsWith(FLOAT aFloat) {
    	return BOOLEAN.valueOf(!value.equals(aFloat.value));
    }

	public static boolean match(FLOAT pattern, Object object) {
		if (!(object instanceof FLOAT)) return false;
		FLOAT message = (FLOAT) object;
		if (pattern.omitField && message.omitField) return true;
		if (pattern.anyOrOmitField) return true;
		if (pattern.anyField && !message.omitField) return true;
		if (pattern.omitField && !message.omitField) return false;
		if (pattern.anyField && message.omitField) return false;
		return (pattern.value.equals(message.value));
	}
    
	public BOOLEAN equals(FLOAT aFloat) {
		return BOOLEAN.valueOf(value.equals(aFloat.value));
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
        for (SubTypeInterval<FLOAT> i : allowedIntervals)
            if(i.checkValue(this)) return;
        for (FLOAT i : allowedValues)
            if (i.equals(value)) return;
        throw new IndexOutOfBoundsException("out of intervals!");
    }
    
}
