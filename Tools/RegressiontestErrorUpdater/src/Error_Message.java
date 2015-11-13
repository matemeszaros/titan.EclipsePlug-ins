/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

public class Error_Message {

	String text;
	int line;
	String filename;
	boolean isempty = false;
	
	public Error_Message(String p_text, int p_line, String p_filename, boolean p_isempty)
	{
		text = p_text;
		line = p_line;
		filename = p_filename;
		isempty = p_isempty; 
	}
		

}
