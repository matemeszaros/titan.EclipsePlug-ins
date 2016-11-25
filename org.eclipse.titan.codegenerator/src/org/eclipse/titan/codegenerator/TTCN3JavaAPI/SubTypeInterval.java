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

public class SubTypeInterval<T> {
    T lowerbound;
    T upperbound;

    public SubTypeInterval(T lower, T upper) {
        lowerbound=lower;
        upperbound=upper;
    }

    boolean checkValue(T value) {
    	if(lowerbound instanceof Relational<?>)
    		return ((Relational<T>)lowerbound).isLessOrEqualThan(value).getValue()&&((Relational<T>)upperbound).isGreaterOrEqualThan(value).getValue();
    	else throw new IndexOutOfBoundsException("bound is not relational");
    }
}
