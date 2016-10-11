/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.jni;

/**
 * Data structure for each TC.
 * <p>
 * The original C++ structure can be found at TTCNv3\mctr2\mctr\MainController.h
 * 
 * @author Peter Dimitrov
 * */
public final class ComponentStruct {

	public int comp_ref;
	public QualifiedName comp_type;
	public String comp_name;
	public String log_source;
	public HostStruct comp_location;
	public TcStateEnum tc_state;
	public VerdictTypeEnum local_verdict;
	public int tc_fd;
	public byte[] text_buf;
	public QualifiedName tc_fn_name;
	public String return_type;
	public int return_value_len;
	//void *return_value;
	public boolean is_alive;
	public boolean stop_requested;
	public boolean process_killed;

	public ComponentStruct(final int tb) {
		text_buf = new byte[tb];
	}
}
