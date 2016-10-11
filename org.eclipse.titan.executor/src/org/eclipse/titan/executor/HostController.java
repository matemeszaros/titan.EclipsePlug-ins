/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor;

/**
 * Contains the data describing a Host Controller.
 * 
 * @author Kristof Szabados
 * */
public final class HostController implements Cloneable {
	private String host;
	private String workingdirectory;
	private String executable;
	private String command;

	public HostController(final String host, final String workingdirectory, final String executable, final String loginCommand) {
		configure(host, workingdirectory, executable, loginCommand);
	}

	/**
	 * Creates a clone of the Host Controller having the exact same values.
	 *
	 * @return the clone of this Host Controller data structure
	 * */
	@Override
	public HostController clone() {
		return new HostController(host, workingdirectory, executable, command);
	}

	/**
	 * Sets all of the data of a Host Controller at the same time.
	 *
	 * @param host the name of the host
	 * @param workingdirectory the working directory to use when executing commands
	 * @param executable the Host Controller's executable to start
	 * @param command the real command to execute in the shell, to start up the Host Controller fully configured.
	 * */
	public void configure(final String host, final String workingdirectory, final String executable, final String command) {
		this.host = host;
		this.workingdirectory = workingdirectory;
		this.executable = executable;
		this.command = command;
	}

	/**
	 * @return the name of this Host Controller
	 * */
	public String host() {
		return host;
	}

	/**
	 * @return the working directory of this Host Controller
	 * */
	public String workingdirectory() {
		return workingdirectory;
	}

	/**
	 * @return the executable set to represent his Host Controller
	 * */
	public String executable() {
		return executable;
	}

	/**
	 * @return the actual command to execute in a shell to start up the Host Controller fuly configured
	 * */
	public String command() {
		return command;
	}
}
