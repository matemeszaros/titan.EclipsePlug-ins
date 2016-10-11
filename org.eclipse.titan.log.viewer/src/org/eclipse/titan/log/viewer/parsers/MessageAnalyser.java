/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers;

import java.util.List;
import java.util.StringTokenizer;

/**
 * Analysis the message
 */
public abstract class MessageAnalyser {

	protected static final String IS_DONE = "is done."; //$NON-NLS-1$
	protected static final String DISCONNECTING_PORTS = "Disconnecting ports"; //$NON-NLS-1$
	private static final String CONNECTION_TERMINATION_REQUEST_WAS_RECEIVED_ON_PORT = "Connection termination request was received on port"; //$NON-NLS-1$
	private static final String DISCONNECT_OPERATION_ON = "Disconnect operation on"; //$NON-NLS-1$
	private static final String CONNECT_OPERATION_ON = "Connect operation on"; //$NON-NLS-1$
	private static final String MESSAGE_ENQUEUED_ON = "enqueued on"; //$NON-NLS-1$
	protected String message;
	 
 	private static final String MAP_OPERATION_OF = "Map operation of"; //$NON-NLS-1$
	private static final String UNMAP_OPERATION_OF = "Unmap operation of"; //$NON-NLS-1$
	protected static final String TTCN_3_PARALLEL_TEST_COMPONENT_STARTED_ON = "TTCN-3 Parallel Test Component started on .*"; //$NON-NLS-1$
	protected static final String LOCAL_VERDICT_OF_MTC = "Local verdict of MTC:"; //$NON-NLS-1$
	protected static final String PTC_WAS_CREATED_COMPONENT_REFERENCE_WITH_COMPONENT_NAME = "PTC was created. Component reference: [0-9]+\\,.*"; //$NON-NLS-1$
	private static final String SEND_VALUE = " to \\S++ \\S++ "; //$NON-NLS-1$
	private static final String SEND_TYPE = " to \\S++ "; //$NON-NLS-1$
	private static final String RECIEVE_VALUE = " from \\S++ \\S++ "; //$NON-NLS-1$
	private static final String RECIEVE_TYPE = " from \\S++ "; //$NON-NLS-1$
	protected static final String COMPONENT_TERMINATION_VERDICT = "component reference \\d++:"; //$NON-NLS-1$
	protected static final String WAS_DISCONNECTED_FROM = "was disconnected from"; //$NON-NLS-1$
	protected static final String PORT = "Port"; //$NON-NLS-1$
	protected static final String WAS_CLOSED_UNEXPECTEDLY_BY_PEER = "was closed unexpectedly by peer"; //$NON-NLS-1$
	protected static final String CONNECTION_OF_PORT = "Connection of port"; //$NON-NLS-1$
	protected static final String WAS_UNMAPPED_FROM = "was unmapped from"; //$NON-NLS-1$
	protected static final String WAS_MAPPED_TO = "was mapped to"; //$NON-NLS-1$
	protected static final String REMOVING_UNTERMINATED_MAPPING = "Removing unterminated mapping between"; //$NON-NLS-1$
	protected static final String REMOVING_UNTERMINATED_CONNECTION = "Removing unterminated connection between port"; //$NON-NLS-1$
	protected static final String ACCEPTED_CONNECTION = "has accepted the connection from"; //$NON-NLS-1$
	protected static final String WAITING_FOR_CONNECTION = "is waiting for connection from"; //$NON-NLS-1$
	protected static final String CONNECTION_ESTABLISHED = "has established the connection with"; //$NON-NLS-1$
	protected static final String HOST_CONTROLLER_STARTED = "Host Controller started"; //$NON-NLS-1$
	protected static final String START_TIMER = "Start timer"; //$NON-NLS-1$
	protected static final String FINAL_VERDICT = "Final verdict"; //$NON-NLS-1$
	protected static final String RECEIVED_ON = "Received on"; //$NON-NLS-1$
	protected static final String SENT_ON = "Sent on"; //$NON-NLS-1$	
	protected static final String ON_COMPONENT = "on component"; //$NON-NLS-1$
	protected static final String STARTING_FUNCTION = "Starting function"; //$NON-NLS-1$
	protected static final String AND = " and "; //$NON-NLS-1$
	protected static final String CONNECTING_PORTS = "Connecting ports"; //$NON-NLS-1$
	protected static final String FROM = " from "; //$NON-NLS-1$
	protected static final String UNMAPPING_PORT = "Unmapping port"; //$NON-NLS-1$
	protected static final String TO = " to "; //$NON-NLS-1$
	protected static final String MAPPING_PORT = "Mapping port"; //$NON-NLS-1$
	protected static final String COMPONENT_REFERENCE = "component reference"; //$NON-NLS-1$
			
	protected static final String COMPONENT_DONE_REFERENCE = "PTC with component reference"; //$NON-NLS-1$
	protected static final String COMPONENT_DONE = "is done. Return value"; //$NON-NLS-1$
	protected static final String COMPONENT_REFERENCE_CREATE = "Component reference:"; //$NON-NLS-1$
	protected static final String COMPONENT_NAME = "component name:"; //$NON-NLS-1$

	protected static final String MTC_FINISHED = "TTCN-3 Main Test Component finished."; //$NON-NLS-1$
	protected static final String MTC_STARTED = "TTCN-3 Main Test Component started on"; //$NON-NLS-1$
	protected static final String VERDICT = "Verdict:"; //$NON-NLS-1$
	protected static final String FINISHED = "finished"; //$NON-NLS-1$
	protected static final String STARTED = "started"; //$NON-NLS-1$
	protected static final String TEST_CASE = "Test case"; //$NON-NLS-1$
	protected static final String HOST_CONTROLLER_FINISHED = "Host Controller finished"; //$NON-NLS-1$
	protected static final String ETS_STARTUP = "TTCN-3 Test Executor started in single mode."; //$NON-NLS-1$
	protected static final String ETS_TERMINATION = "TTCN-3 Test Executor finished in single mode."; //$NON-NLS-1$
	private static final String SETVERDICT = "setverdict("; //$NON-NLS-1$
	
	private List<String> errorCausedBy;
	private List<String> failCausedBy;

	public abstract String getType();

	/**
	 * Utility method that returns the token after a given string
	 * @param string
	 * @return String token
	 */
	protected String getTokenAfterString(final String string) {
		return getTokenAfterString(string, " ,."); //$NON-NLS-1$
	}

	/**
	 * Utility method that returns the token after a given string
	 * @param string
	 * @param delimiter delimiters
	 * @return String token
	 */
	protected String getTokenAfterString(final String string, final String delimiter) {
		String[] strings = this.message.split(string);
		if ((strings.length > 1) && (strings[1] != null)) {
			StringTokenizer tokenizer = new StringTokenizer(strings[1], delimiter);
			if (tokenizer.hasMoreTokens()) {
				return tokenizer.nextToken();
			}
		}
		return ""; //$NON-NLS-1$ 
	}

	/**
	 * Predicates and access methods for different parts of log lines
	 */
	
	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	protected boolean isSystemCreation() {
		return this.message.contains(HOST_CONTROLLER_STARTED);
	}

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	protected boolean isSystemTermination() {
		return this.message.contains(HOST_CONTROLLER_FINISHED);
	}

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	protected abstract boolean isComponentTermination();
	
	// getComponentTerminationReference
	/**
	 * Getter for component termination reference
	 * @return reference
	 */
	protected abstract String getComponentTerminationReference();
	
	// getMappingPort
	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	protected boolean isTestcaseStart() {
		return this.message.contains(TEST_CASE) && this.message.contains(STARTED);
	}

	/**
	 * Getter for test case name
	 * @return test case name
	 */
	protected String getTestcaseName() {
		return getTokenAfterString(TEST_CASE);
	}

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	protected boolean isTestcaseEnd() {
		return this.message.contains(TEST_CASE) && this.message.contains(FINISHED);
	}

	/**
	 * Getter for verdict
	 * @return a string with the verdict
	 */
	protected String getTestcaseVerdict() {
		return getTokenAfterString(VERDICT);
	}

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	protected boolean isMTCCreation() {
		return this.message.contains(MTC_STARTED);
	}

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	protected boolean isMTCTermination() {
		return this.message.contains(MTC_FINISHED);
	}
	
	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	protected boolean isMTCDone() {
		return this.message.contains(LOCAL_VERDICT_OF_MTC);
	}
	
	
	/**
	 * Getter for verdict
	 * @return a string with the verdict
	 */
	protected String getMTCVerdict() {
		return getTokenAfterString(LOCAL_VERDICT_OF_MTC);
	}
	
	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	protected abstract boolean isComponentCreation();

	/**
	 * Getter for component
	 * @return component name
	 */
	protected String getComponentCreationName() {
		if (this.message.contains(COMPONENT_NAME)) {
			return getTokenAfterString(COMPONENT_NAME);
		}

		return ""; //$NON-NLS-1$	
	}

	/**
	 * Getter for component reference
	 * @return reference
	 */
	protected abstract String getComponentCreationReference();

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	protected boolean isComponentDone() {
		return this.message.contains(COMPONENT_DONE_REFERENCE) && this.message.contains(IS_DONE);
	}

	/**
	 * Getter for reference to a PTC component
	 * @return reference
	 */
	protected abstract String getComponentDoneReference();

	/**
	 * Getter for termination verdict
	 * @return verdict
	 */
	protected String getComponentTerminationVerdict() {
		return getTokenAfterString(COMPONENT_TERMINATION_VERDICT);
	}

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	public boolean isPortMapping() {
		return this.message.contains(MAPPING_PORT);
	}

	/**
	 * Getter for port mapping
	 * @return port
	 */
	public abstract String getPortMappingSource();

	public String getPortMapping() {
		return getTokenAfterString(MAPPING_PORT, ":").trim(); //$NON-NLS-1$
	}
	
	/**
	 * Getter for port mapping reference
	 * @return reference
	 */
	public String getPortMappingSourceRef() {
		return getTokenAfterString(MAPPING_PORT).split(":")[0]; //$NON-NLS-1$
	}

	/**
	 * Getter for port mapping target
	 * @return target
	 */
	public abstract String getPortMappingTarget();

	/**
	 * Getter for port unmapping target
	 * @return target
	 */
	public abstract String getPortUnMappingTarget();
	/**
	 * Getter for port mapping target reference
	 * @return reference
	 */
	public String getPortMappingTargetRef() {
		return getTokenAfterString(TO).split(":")[0]; //$NON-NLS-1$
	}

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	public boolean isPortUnmapping() {
		return this.message.contains(UNMAPPING_PORT);
	}

	/**
	 * Getter for Un-mapping port
	 * @return port
	 */
	public String getPortUnmappingSource() {
		return getTokenAfterString(UNMAPPING_PORT);
	}

	public abstract String getPortUnMapping();
	
	/**
	 * Getter for un-mapping port target
	 * @return target
	 */
	public String getPortUnmappingTarget() {
		return getTokenAfterString(FROM);
	}

	/**
	 * Check if the message is connecting ports
	 * @return true or false
	 */
	public boolean isPortConnection() {
		return this.message.contains(CONNECTING_PORTS);
	}
	
	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	public abstract boolean isPortDisconnection();

	/**
	 * Getter for connecting ports
	 * @return ports
	 */
	public abstract String getPortConnectionSource();

	/**
	 * Getter for connecting ports reference
	 * @return reference
	 */
	public abstract String getPortConnectionSourceRef();
	
	/**
	 * Getter for disconnecting ports
	 * @return ports
	 */
	public abstract String getPortDisconnectionSource();

	/**
	 * Getter for cdisonnecting ports reference
	 * @return reference
	 */
	public abstract String getPortDisconnectionSourceRef();
	

	/**
	 * Getter for port connection target
	 * @return target
	 */
	public abstract String getPortConnectionTarget();

	/**
	 * Getter for port connection target reference
	 * @return reference
	 */
	public abstract String getPortConnectionTargetRef();
	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	public boolean isStartFunction() {
		return this.message.contains(STARTING_FUNCTION) && this.message.contains(ON_COMPONENT);
	}

	/**
	 * Getter for start function
	 * @return name
	 */
	public String getStartFunctionName() {
		return getComponentRef(getTokenAfterString(STARTING_FUNCTION, " (")); //$NON-NLS-1$
	}

	/**
	 * Getter for start function reference
	 * @return reference
	 */
	public abstract String getStartFunctionReference();

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	public boolean isSend() {
		return this.message.contains(SENT_ON);
	}

	/**
	 * Getter for send source
	 * @return source
	 */
	public String getSendSource() {
		return getComponentRef(getTokenAfterString(SENT_ON));
	}

	/**
	 * Getter for sent target
	 * @return target
	 */
	public String getSendTarget() {
		return getComponentRef(getTokenAfterString(TO, " "));
	}

	/**
	 * Getter for send type
	 * @return type
	 */
	public String getSendType() {
		//Special for message containing system(<text>) 
		if (this.message.contains(org.eclipse.titan.log.viewer.utils.Constants.SUT_REFERENCE + "(")) { //$NON-NLS-1$
			int stopIndex = this.message.indexOf(")"); //$NON-NLS-1$
			String tmp = this.message.substring(stopIndex + 1);
			String type = tmp.split(":")[0]; //$NON-NLS-1$
			return type.trim();
		}

		return getComponentRef(getTokenAfterString(SEND_TYPE, " ")); //$NON-NLS-1$
	}

	/**
	 * Getter for send value
	 * @return value
	 */
	public String getSendValue() {
		return this.message.split(SEND_VALUE)[1];
	}

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	public boolean isReceive() {
		return this.message.contains(RECEIVED_ON);
	}

	/**
	 * Getter for received target
	 * @return target
	 */
	public String getReceiveTarget() {
		return getComponentRef(getTokenAfterString(RECEIVED_ON));
	}

	/**
	 * Getter for receive source
	 * @return source
	 */
	public String getReceiveSource() {
		return getComponentRef(getTokenAfterString(FROM, " ")); //$NON-NLS-1$
	}

	/**
	 * Getter for receive type
	 * @return type
	 */
	public String getReceiveType() {
		if (this.message.contains(org.eclipse.titan.log.viewer.utils.Constants.SUT_REFERENCE + "(")) { //$NON-NLS-1$
			int stopIndex = this.message.indexOf(")"); //$NON-NLS-1$
			String tmp = this.message.substring(stopIndex + 1);
			String type = tmp.split(":")[0]; //$NON-NLS-1$
			return type.trim();
		}
		return getTokenAfterString(RECIEVE_TYPE, " "); //$NON-NLS-1$
	}

	/**
	 * Getter for receive value
	 * @return value
	 */
	public String getReceiveValue() {
		return this.message.split(RECIEVE_VALUE)[1];
	}

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	public boolean isFinalVerdict() {
		return this.message.contains(FINAL_VERDICT);
	}

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	public boolean isTimerStart() {
		return this.message.contains(START_TIMER); 
	}


	public String getMessage() {
		return this.message;
	}


	public void setMessage(final String message) {
		this.message = message;
	}
	
	
	/**
	 * This method checks if the message should be displayed as a silent event e.g. 
	 * an event that has not been taken care of in any of the other if cases 
	 * and where a component reference can be detected in the message.
	 * It is also a check that the referenced component is alive in the message sequence chart.
	 * @return null or component reference
	 */
	public String isSilentEvent() {
		// TODO check this token handling
		if (this.message.contains(COMPONENT_REFERENCE_CREATE)) {
			//check for candidate with already added components
			return getComponentRef(getTokenAfterString(COMPONENT_REFERENCE_CREATE));
		} else if (this.message.contains(COMPONENT_REFERENCE)) {
			//check for candidate with already added components
			return getComponentRef(getTokenAfterString(COMPONENT_REFERENCE));
		} else if (this.message.contains(CONNECTION_ESTABLISHED)) {
			return getComponentRef(getTokenAfterString(CONNECTION_ESTABLISHED, " :")); //$NON-NLS-1$
		} else if (this.message.contains(WAITING_FOR_CONNECTION)) {
			return getComponentRef(getTokenAfterString(WAITING_FOR_CONNECTION, " :")); //$NON-NLS-1$
		} else if (this.message.contains(ACCEPTED_CONNECTION)) {
			return getComponentRef(getTokenAfterString(ACCEPTED_CONNECTION, " :")); //$NON-NLS-1$
		} else if (this.message.contains(REMOVING_UNTERMINATED_CONNECTION)) {
			//Removing unterminated connection between port <portname> and <component ref | "system">:<portname>
			return getComponentRef(getTokenAfterString(AND, " :")); //$NON-NLS-1$
		} else if (this.message.contains(REMOVING_UNTERMINATED_MAPPING)) {
			//Removing unterminated mapping between port <portname> and <componentref | "system">:<portname>
			return getComponentRef(getTokenAfterString(AND, " :"));	//$NON-NLS-1$	
		} else if (this.message.contains(WAS_MAPPED_TO)) {
			//Port <portname> was mapped to <component ref | "system">:<portname>.
			return getComponentRef(getTokenAfterString(WAS_MAPPED_TO, " :"));  //$NON-NLS-1$	
		} else if (this.message.contains(WAS_UNMAPPED_FROM)) {
			//Port <portname> was unmapped from <component ref | "system">:<portname>.
			return getComponentRef(getTokenAfterString(WAS_UNMAPPED_FROM, " :"));  //$NON-NLS-1$
		} else if (this.message.contains(CONNECTION_OF_PORT) && this.message.contains(WAS_CLOSED_UNEXPECTEDLY_BY_PEER)) {
			//Connection of port <portname> to <component ref | mtc | system>:<portname> was closed unexpectedly by peer.
			return getComponentRef(getTokenAfterString(TO, " :")); //$NON-NLS-1$	
		} else if (this.message.contains(PORT) && this.message.contains(WAS_DISCONNECTED_FROM)) {
			//Port <portname> was disconnected from <component ref | mtc| system>:<portname>.
			return getComponentRef(getTokenAfterString(WAS_DISCONNECTED_FROM, " :")); //$NON-NLS-1$	
		} else if (this.message.contains(MAP_OPERATION_OF) && this.message.contains(FINISHED)) {
			// Map operation of <componentname(ref) | component ref | mtc | system>:<portname> to <componentname(ref) | component ref | mtc | system>:<portname> finished.
			return getComponentRef(getTokenAfterString(MAP_OPERATION_OF, " :")); //$NON-NLS-1$
		} else if (this.message.contains(UNMAP_OPERATION_OF) && this.message.contains(FINISHED)) {
			//Unmap operation of <componentname(ref) | component ref | mtc | system>:<portname> to <componentname(ref) | component ref | mtc | system>:<portname> finished.
			return getComponentRef(getTokenAfterString(UNMAP_OPERATION_OF, " :")); //$NON-NLS-1$
		} else if (this.message.contains(CONNECT_OPERATION_ON) && this.message.contains(FINISHED)) {
			//Connect operation of <componentname(ref) | component ref | mtc | system>:<portname> and <componentname(ref) | component ref | mtc | system>:<portname> finished.
			return getComponentRef(getTokenAfterString(CONNECT_OPERATION_ON, " :")); //$NON-NLS-1$
		} else if (this.message.contains(DISCONNECT_OPERATION_ON) && this.message.contains(FINISHED)) {
			//Disconnect operation of <componentname(ref) | component ref | mtc | system>:<portname> and <componentname(ref) | component ref | mtc | system>:<portname> finished.
			return getComponentRef(getTokenAfterString(DISCONNECT_OPERATION_ON, " :")); //$NON-NLS-1$
		} else if (this.message.contains(DISCONNECTING_PORTS) && this.message.contains(AND)) {
			//Disconnecting ports mtc:Port1 and 3:Port1.
			return getComponentRef(getTokenAfterString(DISCONNECTING_PORTS, " :")); //$NON-NLS-1$
		} else if (this.message.contains(CONNECTION_TERMINATION_REQUEST_WAS_RECEIVED_ON_PORT) && this.message.contains(FROM)) {
			//Connection termination request was received on port <port> from <comp ref>:<port>.<message>
			return getComponentRef(getTokenAfterString(FROM, " :")); //$NON-NLS-1$
		}

		return null;
	} // isSilentEvent

	
	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	public String getMappingPort() {
		return getTokenAfterString(MAPPING_PORT, ":").trim(); //$NON-NLS-1$
	}

	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	public boolean isEnqueued() {
		return this.message.contains(MESSAGE_ENQUEUED_ON);
	}

	/**
	 * Getter for sent target
	 * @return target
	 */
	public String getEnqueuedTarget() {
		String port = getTokenAfterString(MESSAGE_ENQUEUED_ON);
		
		return port; 
	}
	
	/**
	 * Getter for send type
	 * @return type
	 */
	protected String getEnqueuedType() {
		return getComponentRef(getTokenAfterString(SEND_TYPE, " ")); //$NON-NLS-1$
	}

	/**
	 * Getter for receive type
	 * @return type
	 */
	protected String getEnqueuedReceiveType() {
		String[] tmpstrings = this.message.split(" "); //$NON-NLS-1$
		return tmpstrings[0];
	}

	/**
	 * Check if the message is a setverdict(fail) or serverdict(error)
	 * @return
	 */
	protected boolean isSetverdict() {
		return this.message.contains(SETVERDICT) && this.message.contains("):"); //$NON-NLS-1$
	}
	
	protected String getSetverdictName() {		
		return this.message.split(":")[0]; //$NON-NLS-1$
	}
	
	protected String getSetverdictType() {
		int startpos = this.message.indexOf("("); //$NON-NLS-1$
		int stoppos = this.message.indexOf(")"); //$NON-NLS-1$
		return this.message.substring(startpos + 1, stoppos);
	}

	
	/**
	 * Line checker for the log file
	 * @return true or false
	 */
	public abstract boolean isReceiveOperation();
	public abstract String getReceiveOperationTarget();
	public abstract String getReceiveOperationType();

	/**
	 * parse the component ref from a string with <component name>(<component ref>)
	 * @param compNameAndRef
	 * @return
	 */
	protected String getComponentRef(final String compNameAndRef) {
		if (compNameAndRef.startsWith(org.eclipse.titan.log.viewer.utils.Constants.SUT_REFERENCE + "(")) {
			int startRef = compNameAndRef.indexOf("(");
			return compNameAndRef.substring(0, startRef);
		} else if (compNameAndRef.contains("(") && compNameAndRef.contains(")")) {
			int startRef = compNameAndRef.indexOf("(");
			int stopRef = compNameAndRef.indexOf(")");
			return compNameAndRef.substring(startRef + 1, stopRef);
		} else if (compNameAndRef.contains(":")) {
			String[] component = compNameAndRef.split(":");
			return component[0];
		} else {
			return compNameAndRef;
		}
	}

	/**
	 * Check if the message is a Dynamic test case error
	 */
	protected boolean isDynamicTestCaseError() {
		for (String anErrorCausedBy : this.errorCausedBy) {
			if (this.message.contains(anErrorCausedBy)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check if the message is a fail message
	 */
	protected boolean isFailMessages() {

		for (String aFailCausedBy : this.failCausedBy) {
			if (this.message.contains(aFailCausedBy)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Getter for un-mapping port target
	 * @return target
	 */
	public String getPort(final String componentAndPort) {
		if (componentAndPort.contains(":")) { //$NON-NLS-1$ 
			int index = componentAndPort.lastIndexOf(":"); //$NON-NLS-1$
			if (index < 0) {
				return "";  //$NON-NLS-1$
			}
			return componentAndPort.substring(index + 1).trim();
		}
		return "";
	}

	public List<String> getErrorCausedBy() {
		return this.errorCausedBy;
	}

	public void setErrorCausedBy(final List<String> errorCausedBy) {
		this.errorCausedBy = errorCausedBy;
	}

	public List<String> getFailCausedBy() {
		return this.failCausedBy;
	}

	public void setFailCausedBy(final List<String> failCausedBy) {
		this.failCausedBy = failCausedBy;
	}
}
