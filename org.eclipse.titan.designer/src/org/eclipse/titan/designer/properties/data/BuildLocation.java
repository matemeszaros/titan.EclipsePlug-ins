/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import java.util.List;

/**
 * This class represents a build location, with its name, the paired command to
 * execute, and a flag telling if it will be active or not in the next remote
 * build processes.
 * 
 * @author Kristof Szabados
 */
public final class BuildLocation implements Cloneable {
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String[] REPLACEMENT_TARGETS = new String[] { "\\", "," };
	private static final String[] REPLACEMENTS = new String[] { "\\\\", "\\," };
	private boolean active;
	private String name;
	private String command;

	/**
	 * The constructor of this class.
	 * 
	 * @param active
	 *                tells if this build location should be active or not
	 * @param name
	 *                the name of this build location
	 * @param command
	 *                the command to execute
	 */
	public BuildLocation(final boolean active, final String name, final String command) {
		this.active = active;
		this.name = name;
		this.command = command;
	}

	/**
	 * Initialization based on property values, host properties are
	 * separated by ',' .
	 * 
	 * @param propertyPart
	 *                the property part describing the build location.
	 * */
	public BuildLocation(final String propertyPart) {
		final List<String> temp = ProjectRemoteBuildPropertyData.intelligentSplit(propertyPart, ',', '\\');

		if (temp.size() == 3) {
			if (TRUE.equals(temp.get(0))) {
				active = true;
			} else {
				active = false;
			}
			name = temp.get(1);
			command = temp.get(2);
		}
	}

	@Override
	public BuildLocation clone() {
		return new BuildLocation(active, name, command);
	}

	/**
	 * configures this build location.
	 * 
	 * @param active
	 *                tells if this build location should be active or not
	 * @param name
	 *                the name of this build location
	 * @param command
	 *                the command to execute
	 */
	public void configure(final boolean active, final String name, final String command) {
		this.active = active;
		this.name = name;
		this.command = command;
	}

	public boolean getActive() {
		return active;
	}

	public String getName() {
		return name;
	}

	public String getCommand() {
		return command;
	}

	/**
	 * Creating the string representation of this remote build location's
	 * settings. Properties are separated by ','
	 * 
	 * @return the string representation of this remote build location
	 * */
	public StringBuilder getPropertyValueRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append(active ? TRUE : FALSE).append(',');
		String temp = name.replace(REPLACEMENT_TARGETS[0], REPLACEMENTS[0]);
		builder.append(temp.replace(REPLACEMENT_TARGETS[1], REPLACEMENTS[1])).append(',');
		temp = command.replace(REPLACEMENT_TARGETS[0], REPLACEMENTS[0]);
		builder.append(temp.replace(REPLACEMENT_TARGETS[1], REPLACEMENTS[1]));
		return builder;
	}
}
