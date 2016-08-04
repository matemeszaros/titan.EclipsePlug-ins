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

/* Main Controller */
package org.eclipse.titan.codegenerator.TTCN3JavaAPI;

public class MC{

	//these will be retrieved from the config file later.
	private static int SERVERPORTNUM = 5557; 
	private static String MTCIP = "127.0.0.1"; //IP of machine that has to run the MTC
	private static int HCNUM = 1; //later from config file
	private static boolean DEBUGMODE = false;
	private static String TC = "tc_Controller"; //TC to be executed
	
	private static MCType mc;
	
	public static void main(String[] args){
		mc = new MCType(DEBUGMODE);
		mc.startmc(SERVERPORTNUM, MTCIP, TC, HCNUM);
		TTCN3Logger.writeLog("mc", "EXECUTOR", "Main Controller stopped", false);
	}

}
