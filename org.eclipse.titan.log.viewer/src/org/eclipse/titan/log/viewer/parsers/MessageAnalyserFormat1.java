/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers;

import java.util.regex.Pattern;

/**
 * Parse a log file in parallel execution mode with file mask n
 */
public class MessageAnalyserFormat1 extends MessageAnalyser {
	
	protected static final String COMPONENT_DONE_VERDICT = "verdict of PTC with component reference"; //$NON-NLS-1$
	private static final String PTC_WAS_CREATED_COMPONENT_REFERENCE_ONLY = "PTC was created. Component reference: [0-9]+\\."; //$NON-NLS-1$

	@Override
	public String getType() {
		return "MessageAnalyserFormat1"; //$NON-NLS-1$
	}
	
	@Override
	protected boolean isComponentCreation() {

		String regexpComponent = PTC_WAS_CREATED_COMPONENT_REFERENCE_WITH_COMPONENT_NAME;
		if (Pattern.matches(regexpComponent, this.message)) {
			return true;
		}

		regexpComponent = PTC_WAS_CREATED_COMPONENT_REFERENCE_ONLY;

		if (Pattern.matches(regexpComponent, this.message)) {
			return true;
		}

		regexpComponent = TTCN_3_PARALLEL_TEST_COMPONENT_STARTED_ON;

		return Pattern.matches(regexpComponent, this.message);
	}
	
	@Override
	protected String getComponentTerminationReference() {
		return getTokenAfterString(COMPONENT_REFERENCE, " :"); //$NON-NLS-1$
	}

	@Override
	protected boolean isComponentTermination() {
		return this.message.contains(COMPONENT_DONE_VERDICT);
	}
	
	//This message can never occur in 7.0
	@Override
	public boolean isReceiveOperation() {
		return false;
	}
	
	@Override
	public String getReceiveOperationTarget() {
		return ""; //$NON-NLS-1$
	}
	
	@Override
	public String getReceiveOperationType() {
		return ""; //$NON-NLS-1$
	}
	
	@Override
	protected String getComponentCreationReference() {
		return getComponentRef(getTokenAfterString(COMPONENT_REFERENCE_CREATE));
	}
	
	@Override
	protected String getComponentDoneReference() {
		return getComponentRef(getTokenAfterString(COMPONENT_DONE_REFERENCE));
	}
	
	@Override
	public String getPortConnectionSource() {
		return getTokenAfterString(CONNECTING_PORTS);
	}
	
	@Override
	public boolean isPortDisconnection() {
		//disconnecting ports is not taken care of in versions before 7.1
		return false;
	}
	
	@Override
	public String getPortConnectionSourceRef() {
		return getTokenAfterString(CONNECTING_PORTS).split(":")[0]; //$NON-NLS-1$
	}
	
	@Override
	public String getPortDisconnectionSourceRef() {
		return getTokenAfterString(DISCONNECTING_PORTS).split(":")[0]; //$NON-NLS-1$
	}
	
	@Override
	public String getPortDisconnectionSource() {
		return getTokenAfterString(DISCONNECTING_PORTS);
	}
	
	@Override
	public String getPortConnectionTarget() {
		return getTokenAfterString(AND); 
	}
	
	@Override
	public String getPortConnectionTargetRef() {
		return getTokenAfterString(AND).split(":")[0]; //$NON-NLS-1$
	}
	
	@Override
	public String getStartFunctionReference() {
		return getComponentRef(getTokenAfterString(ON_COMPONENT)); 
	}
	
	@Override
	public String getPortUnMapping() {
		return getTokenAfterString(UNMAPPING_PORT, ":").trim(); //$NON-NLS-1$
	}
	
	@Override
	public String getPortMappingTarget() {
		return getTokenAfterString(TO);
	}
	
	@Override
	public String getPortUnMappingTarget() {
		return getTokenAfterString(FROM);
	}
	
	@Override
	public String getPortMappingSource() {
		return getTokenAfterString(MAPPING_PORT);
	}
}

