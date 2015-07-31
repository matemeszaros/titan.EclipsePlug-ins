/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.jni;

/**
 * Data structure for representing a time value.
 * <p>
 * The original C++ structure can be found at TTCNv3\mctr2\mctr\MainController.h
 * 
 * @author Peter Dimitrov
 * */
public final class Timeval {
	 // seconds
	public long tv_sec;
	 // microSeconds
	public long tv_usec;

	public Timeval(final long tv_sec, final long tv_usec) {
		this.tv_sec = tv_sec;
		this.tv_usec = tv_usec;
	}
}
