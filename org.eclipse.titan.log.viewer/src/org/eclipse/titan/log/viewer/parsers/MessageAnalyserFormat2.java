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
public class MessageAnalyserFormat2 extends MessageAnalyser {
	
	private static final String LOCATION = ", location:"; //$NON-NLS-1$
	private static final String PROCESS_ID = ", process id:";  //$NON-NLS-1$
	private static final String TESTCASE_NAME = ", testcase name:"; //$NON-NLS-1$
	private static final String COMPONENT_TYPE = ", component type:";  //$NON-NLS-1$
	private static final String TTCN_3_PARALLEL_TEST_COMPONENT = "TTCN-3 Parallel Test Component started on";  //$NON-NLS-1$
	private static final String WITH_QUEUE_ID = "with queue id \\d+"; //$NON-NLS-1$
	private static final String RECEIVE_OPERATION_ON_PORT = "Receive operation on port"; //$NON-NLS-1$
	private static final String LOCAL_VERDICT_OF_PTC_COMPNAME = "Local verdict of PTC \\S++"; //$NON-NLS-1$
	private static final String PTC_WAS_CREATED_COMPONENT_REFERENCE_ONLY = "PTC was created. Component reference: [0-9]+\\, alive.*"; //$NON-NLS-1$
	private static final String COMPONENT_DONE_VERDICT = "Local verdict of PTC"; //$NON-NLS-1$
	
	@Override
	public String getType() {
		return "MessageAnalyserFormat2"; //$NON-NLS-1$
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
		String componentReference = ""; //$NON-NLS-1$
		String tmp = getTokenAfterString(COMPONENT_DONE_VERDICT, " "); //$NON-NLS-1$
		if ("with".equals(tmp)) { //$NON-NLS-1$
			componentReference = getTokenAfterString(COMPONENT_REFERENCE, " :"); //$NON-NLS-1$
		} else {
			componentReference = getComponentRef(tmp);
		}
		return componentReference;
	}
	
	@Override
	protected String getComponentTerminationVerdict() {
		String componentVerdict = ""; //$NON-NLS-1$
		String tmp = getTokenAfterString(COMPONENT_DONE_VERDICT, " "); //$NON-NLS-1$
		if ("with".equals(tmp)) { //$NON-NLS-1$
			componentVerdict = getTokenAfterString(COMPONENT_TERMINATION_VERDICT);
		} else {
			componentVerdict = getTokenAfterString(LOCAL_VERDICT_OF_PTC_COMPNAME);
		}
		return componentVerdict;
	}
	
	@Override
	protected boolean isComponentTermination() {		
		return this.message.contains(COMPONENT_DONE_VERDICT);
	}
	
	@Override
	public boolean isReceiveOperation() {
		return this.message.contains(RECEIVE_OPERATION_ON_PORT);
	}
	
	@Override
	public String getReceiveOperationTarget() {
		return getTokenAfterString(RECEIVE_OPERATION_ON_PORT);
	}
	
	@Override
	public String getReceiveOperationType() {
		String [] strings = this.message.split(WITH_QUEUE_ID);
		if (strings.length > 1) {
			return getTokenAfterString(WITH_QUEUE_ID + ":", " ");
		}

		strings = this.message.split(":");
		if (strings.length > 2) {
			return strings[1];
		}
		return "";
	}

	@Override
	protected String getComponentCreationReference() {
		 if (this.message.startsWith(TTCN_3_PARALLEL_TEST_COMPONENT)) {
			 return getComponentRef(getReference(COMPONENT_REFERENCE_CREATE, COMPONENT_TYPE));
		 }	
		return getComponentRef(getTokenAfterString(COMPONENT_REFERENCE_CREATE));
	}

	@Override
	protected String getComponentDoneReference() {
		String compRef = getReference(COMPONENT_DONE_REFERENCE, IS_DONE);
		return getComponentRef(compRef);
	}
	
	@Override
	public String getPortConnectionSource() {
		return getReference(CONNECTING_PORTS, AND);
	}

	@Override
	public String getPortConnectionSourceRef() {
		return getComponentRef(getPortConnectionSource());
	}
	
	@Override
	public String getPortDisconnectionSource() {
		return getReference(DISCONNECTING_PORTS, AND);
	}

	@Override
	public String getPortDisconnectionSourceRef() {
		return getComponentRef(getPortDisconnectionSource());
	}
	
	@Override
	public String getPortConnectionTarget() {
		int startIndex = this.message.indexOf(AND) + AND.length();
		if ((startIndex < 0) || (startIndex >= this.message.length())) {
			return "";  //$NON-NLS-1$
		}
		return this.message.substring(startIndex, this.message.length() - 1).trim();
	}

	@Override
	public String getPortConnectionTargetRef() {
		return getComponentRef(getPortConnectionTarget());
	}

	@Override
	public String getStartFunctionReference() {
		int i = this.message.indexOf(ON_COMPONENT);
		String compNameAndRef = this.message.substring(i + ON_COMPONENT.length() + 1, this.message.length() - 1);
		return getComponentRef(compNameAndRef);
	}
	
	@Override
	public String getPortUnMapping() {
		return getReference(UNMAPPING_PORT, FROM);
	}
	
	@Override
	public String getPortMappingTarget() {
		int startIndex = this.message.indexOf(TO) + TO.length();
		if ((startIndex < 0) || (startIndex >= this.message.length())) {
			return "";  //$NON-NLS-1$
		}
		return this.message.substring(startIndex, this.message.length() - 1).trim();
	}

	@Override
	public String getPortUnMappingTarget() {
		int startIndex = this.message.indexOf(FROM) + FROM.length();
		if ((startIndex < 0) || (startIndex >= this.message.length())) {
			return "";  //$NON-NLS-1$
		}
		return this.message.substring(startIndex, this.message.length() - 1).trim();
	}
	
	@Override
	public String getPortMappingSource() {		
		String compRef = getReference(MAPPING_PORT, TO);
		return compRef;
	}
	
	@Override
	public boolean isPortDisconnection() {
		return this.message.contains(DISCONNECTING_PORTS);
	}
	
	private String getReference(final String startMessage, final String stopMessage) {
		int startIndex = this.message.indexOf(startMessage);
		int stopIndex = this.message.indexOf(stopMessage);
		
		//return "" if the messages do not occurs in the message string
		if ((startIndex < 0) || (stopIndex < 0)) {
			return ""; //$NON-NLS-1$
		}
		if (startIndex >= stopIndex) {
			return ""; //$NON-NLS-1$
		}
		return this.message.substring(startIndex + startMessage.length() + 1, stopIndex).trim();
	}
	
	@Override
	protected String getComponentCreationName() {
		if (this.message.contains(COMPONENT_NAME)) {
			String compRef = null;
			if (message.contains(TESTCASE_NAME)) {
				compRef = getReference(COMPONENT_NAME, TESTCASE_NAME);
			} else if (message.contains(PROCESS_ID)) {
				compRef = getReference(COMPONENT_NAME, PROCESS_ID);
			} else if (message.contains(LOCATION)) {
				compRef = getReference(COMPONENT_NAME, LOCATION);
			} else {
				return getTokenAfterString(COMPONENT_NAME);
			}
			return compRef;
		}

		return ""; //$NON-NLS-1$
	}
	
}
