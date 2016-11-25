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

import java.util.List;

public abstract class TypeDef extends ModuleDef{
    public boolean anyField; //stores if the given data instance has special value ?
    public boolean omitField; //stores if the given data instance has special value omit
    public boolean anyOrOmitField; //stores if the given data instance has special value *
    
    public abstract void checkValue(); //subtype correctness verification method
    public abstract String toString(String tabs);
    public abstract String toString();
    
}