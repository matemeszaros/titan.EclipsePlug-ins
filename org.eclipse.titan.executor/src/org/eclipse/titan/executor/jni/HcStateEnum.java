/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.jni;

/**
 * Possible states of a HC.
 * <p>
 * The original C++ structure can be found at TTCNv3\mctr2\mctr\MainController.h
 * 
 * @author Peter Dimitrov
 * */
public final class HcStateEnum {

	public static final HcStateEnum HC_IDLE = new HcStateEnum(0);
	public static final HcStateEnum HC_CONFIGURING = new HcStateEnum(1);
	public static final HcStateEnum HC_ACTIVE = new HcStateEnum(2);
	public static final HcStateEnum HC_OVERLOADED = new HcStateEnum(3);
	public static final HcStateEnum HC_CONFIGURING_OVERLOADED = new HcStateEnum(4);

	public static final HcStateEnum HC_EXITING = new HcStateEnum(5);
	public static final HcStateEnum HC_DOWN = new HcStateEnum(6);

	private int enum_value;

	private HcStateEnum(final int value) {
		enum_value = value;
	}

	public int getValue() {
		return enum_value;
	}
}
