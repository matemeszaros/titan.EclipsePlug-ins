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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public abstract class SubTypeDef<T extends PrimitiveTypeDef> extends PrimitiveTypeDef{
    protected List<T> allowedValues; //allowed values of subtype
    protected List<SubTypeInterval<T>> allowedIntervals; //allowed intervals of subtype

    public T value;

    public SubTypeDef() {
        allowedValues = new ArrayList<T>();
        allowedIntervals = new ArrayList<SubTypeInterval<T>>();
    }

    public SubTypeDef(T val) {
    	this();
    	value = val;
    }

    public void checkValue() throws IndexOutOfBoundsException {
        if (allowedValues.size() == 0 && allowedIntervals.size() == 0)
            return;
        for (SubTypeInterval<T> i : allowedIntervals)
            if(i.checkValue(value)) return;
        for (T i : allowedValues)
            if (i.equals(value)) return;
        throw new IndexOutOfBoundsException("out of intervals!");
    }

    public String toString(){
    	return toString("");
    }
    
    public String toString(String tabs){
    	return value.toString();
    }

}