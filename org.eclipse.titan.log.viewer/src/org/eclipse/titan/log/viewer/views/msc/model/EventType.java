/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.model;

public enum EventType {
	SYSTEM_CREATE,
	SYSTEM_TERMINATE,
	MTC_CREATE,
	MTC_TERMINATE,
	PTC_CREATE,
	PTC_DONE,
	PTC_TERMINATE,
	/**
	 * The test case start event.
	 */
	TC_START,
	TC_END,
	SEND,
	RECEIVE,
	/**
	 * The function call event.
	 */
	FUNCTION,
	SILENT_EVENT,
	ENQUEUED,
	SETVERDICT,
	SETVERDICT_INCONC,
	MAPPING_PORT,
	UNMAPPING_PORT,
	CONNECTING_PORT,
	DISCONNECTING_PORT,
	SETVERDICT_NONE,
	SETVERDICT_PASS,
	HC_CREATE, // TODO this just happened to fall here, HC creation, termination is not detected at all
	HC_TERMINATE, // TODO this just happened to fall here
	UNKNOWN
}
