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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class TTCN3Logger {

	private static File file=null;
	
/*	public static void writeLog(int source, String type, String message, boolean output){
		writeLog(source, type, "", 0, "", "", message, output);
	}*/
	
	public static void writeLog(String source, String type, String message, boolean output){
		writeLog(source, type, "", 0, "", "", message, output);
	}
	
/*	public static void writeLog(int source, String type, String filename, int line, String elementType, String elementName, String message, boolean output){
		writeLog(Integer.toString(source), type, filename, line, elementType, elementName, message, output);
	}*/

	/*
	 * 
	 * Logger method
	 * ===============
	 * 
	 * Parameterization:
	 * 		source		-	source of log entry. mc, hc, mtc or id of ptc
	 * 		type		-	type of log entry. EXECUTOR, TESTCASE, PARALLEL, PORTEVENTE, TIMEROP, USER
	 * 		filename	-	name of ttcn3 file, if applicable (mtc, ptc)
	 * 		line		-	line in ttcn3 file, if applicable (mtc, ptc)
	 * 		elementType	-	element type in ttcn3 file, if applicable (mtc, ptc - function, testcase)
	 * 		elementName	-	element name in ttcn3 file, if applicable (mtc, ptc - function, testcase)
	 * 		message		-	the log message itself
	 * 		output		-	should the message appear on the output too?
	 * 
	 */
	
	public static void writeLog(String source, String type, String filename, int line, String elementType, String elementName, String message, boolean output){
		String codesection = "";
		if(!filename.equals("")) codesection = " " + filename + ":" + Integer.toString(line)+ "("+ elementType + ":" + elementName + ")";
		String s = source + " " + type + codesection + " " + message;
		writeLog(s, output);
	}
	
	private static synchronized void writeLog(String s, boolean output){
    	try{
			if(file==null){
				file = new File("D:\\TTCN3Log.txt");
				if(!file.exists()){
			    		file.createNewFile();
			    }
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			LocalDateTime time = LocalDateTime.now();
			time.getMonth().toString();
			String logmsg = time.toString().replaceAll("T", " ") + " " + s + "\n";
			bw.write(logmsg);
			if(output) System.out.print(logmsg);
			bw.close();
		}catch(IOException e){e.printStackTrace();}
	}
	
}
