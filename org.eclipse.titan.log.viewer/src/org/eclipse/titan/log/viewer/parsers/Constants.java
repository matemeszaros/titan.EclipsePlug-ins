/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers;

/**
 * Constants used by RecordParser and Tokens
 */
public final class Constants {
	public static final int WHITE_SPACE =  1;
	public static final int END_OF_RECORD = 2;
	public static final int COMPONENT_REFERENCE =  4;
	public static final int SOURCE_INFORMATION =  8;
	public static final int EVENT_TYPE =  16;
	public static final int MESSAGE = 64;
	public static final int TIME_STAMP = 128;
	public static final int UNKNOWN = 256;

	static final String[] COMPONENT_REFERENCES = new String[]{"hc", "mc", "mtc"};
	static final char[] WS = " \n\t\r".toCharArray(); //$NON-NLS-1$
	
	public static final String TTCN_FILE_EXT = ".ttcn"; //$NON-NLS-1$
	public static final String THREEMP_FILE_EXT = ".3mp"; //$NON-NLS-1$

	private Constants() {
	}
}
