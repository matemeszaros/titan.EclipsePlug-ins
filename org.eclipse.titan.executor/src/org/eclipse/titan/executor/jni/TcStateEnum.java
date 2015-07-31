/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.jni;

/**
 * Possible states of a TC (MTC or PTC).
 * <p>
 * The original C++ structure can be found at TTCNv3\mctr2\mctr\MainController.h
 * 
 * @author Peter Dimitrov
 * */
public final class TcStateEnum {

	public static final TcStateEnum TC_INITIAL = new TcStateEnum(0);
	public static final TcStateEnum TC_IDLE = new TcStateEnum(1);
	public static final TcStateEnum TC_CREATE = new TcStateEnum(2);
	public static final TcStateEnum TC_START = new TcStateEnum(3);
	public static final TcStateEnum TC_STOP = new TcStateEnum(4);

	public static final TcStateEnum TC_KILL = new TcStateEnum(5);
	public static final TcStateEnum TC_CONNECT = new TcStateEnum(6);
	public static final TcStateEnum TC_DISCONNECT = new TcStateEnum(7);
	public static final TcStateEnum TC_MAP = new TcStateEnum(8);
	public static final TcStateEnum TC_UNMAP = new TcStateEnum(9);

	public static final TcStateEnum TC_STOPPING = new TcStateEnum(10);
	public static final TcStateEnum TC_EXITING = new TcStateEnum(11);
	public static final TcStateEnum TC_EXITED = new TcStateEnum(12);
	public static final TcStateEnum MTC_CONTROLPART = new TcStateEnum(13);
	public static final TcStateEnum MTC_TESTCASE = new TcStateEnum(14);

	public static final TcStateEnum MTC_ALL_COMPONENT_STOP = new TcStateEnum(15);
	public static final TcStateEnum MTC_ALL_COMPONENT_KILL = new TcStateEnum(16);
	public static final TcStateEnum MTC_TERMINATING_TESTCASE = new TcStateEnum(17);
	public static final TcStateEnum MTC_PAUSED = new TcStateEnum(18);
	public static final TcStateEnum PTC_FUNCTION = new TcStateEnum(19);

	public static final TcStateEnum PTC_STARTING = new TcStateEnum(20);
	public static final TcStateEnum PTC_STOPPED = new TcStateEnum(21);
	public static final TcStateEnum PTC_KILLING = new TcStateEnum(22);
	public static final TcStateEnum PTC_STOPPING_KILLING = new TcStateEnum(23);
	public static final TcStateEnum PTC_STALE = new TcStateEnum(24);

	public static final TcStateEnum TC_SYSTEM = new TcStateEnum(25);

	private int enum_value;

	private TcStateEnum(final int value) {
		enum_value = value;
	}

	public int getValue() {
		return enum_value;
	}
}
