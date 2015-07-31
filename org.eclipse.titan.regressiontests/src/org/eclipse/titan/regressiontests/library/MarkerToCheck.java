/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.library;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;

public class MarkerToCheck {
	private final HashMap<Object, Object> marker = new HashMap<Object, Object>();
	
	public MarkerToCheck(final String messageToCheck, final int lineNumber, final int messageSeverity)
	{
		marker.put(IMarker.MESSAGE, messageToCheck);
		marker.put(IMarker.LINE_NUMBER, lineNumber);
		marker.put(IMarker.SEVERITY, messageSeverity);
	}

	public Map<?, ?> getMarkerMap() {
	  return marker;	
	}
}
