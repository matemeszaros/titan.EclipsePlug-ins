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

public abstract class Relational<T> extends Comparable<T> {

    public abstract BOOLEAN isGreaterThan(T t);

    public abstract BOOLEAN isGreaterOrEqualThan(T t);

    public abstract BOOLEAN isLessThan(T t);

    public abstract BOOLEAN isLessOrEqualThan(T t);

}
