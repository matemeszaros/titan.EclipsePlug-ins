/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.notification;

/**
 * @author Kristof Szabados
 * */
public final class Notification {
	private final String timestamp;
	private final String type;
	private final String component;
	private final String information;

	public Notification(final String timestamp, final String type, final String component, final String information) {
		this.timestamp = timestamp;
		this.type = type;
		this.component = component;
		this.information = information;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getType() {
		return type;
	}

	public String getComponent() {
		return component;
	}

	public String getInformation() {
		return information;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(timestamp);
		if (!"".equals(type)) {
			builder.append(' ').append(type);
		}
		if (!"".equals(component)) {
			builder.append(' ').append(component);
		}
		builder.append(' ').append(information);
		return builder.toString();
	}
}
