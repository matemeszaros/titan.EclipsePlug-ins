/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.jni;

/**
 * For representing the global state of MC.
 * <p>
 * The original C++ structure can be found at TTCNv3\mctr2\mctr\MainController.h
 * 
 * @author Peter Dimitrov
 * */
public final class McStateEnum {

	public static final McStateEnum MC_INACTIVE = new McStateEnum(0);
	public static final McStateEnum MC_LISTENING = new McStateEnum(1);
	public static final McStateEnum MC_LISTENING_CONFIGURED = new McStateEnum(2);
	public static final McStateEnum MC_HC_CONNECTED = new McStateEnum(3);
	public static final McStateEnum MC_CONFIGURING = new McStateEnum(4);

	public static final McStateEnum MC_ACTIVE = new McStateEnum(5);
	public static final McStateEnum MC_SHUTDOWN = new McStateEnum(6);
	public static final McStateEnum MC_CREATING_MTC = new McStateEnum(7);
	public static final McStateEnum MC_READY = new McStateEnum(8);
	public static final McStateEnum MC_TERMINATING_MTC = new McStateEnum(9);

	public static final McStateEnum MC_EXECUTING_CONTROL = new McStateEnum(10);
	public static final McStateEnum MC_EXECUTING_TESTCASE = new McStateEnum(11);
	public static final McStateEnum MC_TERMINATING_TESTCASE = new McStateEnum(12);
	public static final McStateEnum MC_PAUSED = new McStateEnum(13);

	private int enum_value;

	private McStateEnum(final int value) {
		enum_value = value;
	}

	public int getValue() {
		return enum_value;
	}
}
