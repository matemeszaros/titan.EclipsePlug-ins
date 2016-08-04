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

//maybe this should be abstract and the fromXXX methods should be removed.

public class BINARY_STRING extends STRING{

	public BINARY_STRING(){
	}

	public BINARY_STRING(String s){
		super(s);
	}
	
	protected int fromBitString(byte[] b){
		String str = new String(b);
		return Integer.parseInt(str, 2);
	}
	
	protected int fromOctetString(byte[] b){
		String str = new String(b);
		return Integer.parseInt(str, 8);
	}
	
	protected int fromHexString(byte[] b){
		String str = new String(b);
		return Integer.parseInt(str, 16);
	}
	
    public int generalBitwiseNot(int o1){
        return ~o1;
    }

    public int generalBitwiseAnd(int o1, int o2){
        return o1 & o2;
    }

    public int generalBitwiseOr(int o1, int o2){
        return o1 | o2;
    }

    public int generalBitwiseXor(int o1, int o2){
        return o1 ^ o2;
    }

    //converts the input from INTEGER to int!!!
    public BINARY_STRING shiftLeft(INTEGER by){
        byte[] copy = new byte[value.length];
        for(int i=0;i<copy.length;i++){
            copy[i]=0;
        }
        int byValue = by.value.intValue();
        if(byValue >= value.length){
            return new BINARY_STRING(new String(copy));
        }
        System.arraycopy(value,byValue,copy,0,value.length-byValue);
        return new BINARY_STRING(new String(copy));
    }
    
    //converts the input from INTEGER to int!!!
    public BINARY_STRING shiftRight(INTEGER by){
        byte[] copy = new byte[value.length];
        for(int i=0;i<copy.length;i++){
            copy[i]=0;
        }
        int byValue = by.value.intValue();
        if(byValue >= value.length){
            return new BINARY_STRING(new String(copy));
        }
        System.arraycopy(value,0,copy,byValue,value.length-byValue);
        return new BINARY_STRING(new String(copy));
    }
    
}
