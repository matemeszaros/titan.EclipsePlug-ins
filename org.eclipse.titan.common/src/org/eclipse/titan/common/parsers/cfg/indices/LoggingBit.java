/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

/**
 * The logging bits of the Titan Runtime listed as an enumeration.
 * 
 * @author Kristof Szabados
 * */
public enum LoggingBit {
	//special options
	LOG_ALL("LOG_ALL"),
	LOG_NOTHING("LOG_NOTHING"),

	//first level categories
	ACTION("ACTION", "TTCN-3 action(...) statements"),
	DEBUG("DEBUG", "Debug messages in Test Ports and external functions"),
	DEFAULTOP("DEFAULTOP", "Default operations\n(activate, deactivate, return)"),
	ERROR("ERROR", "Dynamic test case errors\n(e.g. snapshot matching failures)"),
	EXECUTOR("EXECUTOR", "Internal TITAN events") ,
	FUNCTION("FUNCTION", "Some important function calls"),
	MATCHING("MATCHING", "Analysis of template matching failures in receiving port operations"),
	PARALLEL("PARALLEL", "Parallel test execution and test configuration related operations\n(create, done, connect, map, etc.)"),
	PORTEVENT("PORTEVENT", "Port events\n(send, receive)"),
	STATISTICS("STATISTICS", "Statistics of verdicts at the end of execution"),
	TESTCASE("TESTCASE", "The start, the end and the final verdict of test cases"),
	TIMEROP("TIMEROP", "Timer operations\n(start, stop, timeout, read)"),
	USER("USER", "User log(...) statements"),
	VERDICTOP("VERDICTOP", "Verdict operations\n(setverdict, getverdict)"),
	WARNING("WARNING", "Run-time warnings\n(e.g. stopping of an inactive timer)"),
	
	//second level categories
	ACTION_UNQUALIFIED("ACTION_UNQUALIFIED"),

	DEBUG_ENCDEC("DEBUG_ENCDEC", "Debug information coming from generated functions\n" +
	" of dual faced ports and built-in encoder/decoders."),
	DEBUG_TESTPORT("DEBUG_TESTPORT"),
	DEBUG_UNQUALIFIED("DEBUG_UNQUALIFIED"),

	DEFAULTOP_ACTIVATE("DEFAULTOP_ACTIVATE", "TTCN-3 activate statement\n(activation of a default)"),
	DEFAULTOP_DEACTIVATE("DEFAULTOP_DEACTIVATE", "Deactivation of a default"),
	DEFAULTOP_EXIT("DEFAULTOP_EXIT", "Leaving an invoked default at the end of a branch\n" +
			" (causing leaving the alt statement in which it was invoked)\n" +
			"or calling repeat in an invoked default\n" +
			" (causing new snapshot and evaluation of the alt statement)"),
	DEFAULTOP_UNQUALIFIED("DEFAULTOP_UNQUALIFIED"),

	ERROR_UNQUALIFIED("ERROR_UNQUALIFIED"),
	
	EXECUTOR_COMPONENT("EXECUTOR_COMPONENT", "Starting and stopping MTC and HCs"),
	EXECUTOR_CONFIGDATA("EXECUTOR_CONFIGDATA", "Runtime test configuration data processing"),
	EXECUTOR_EXTCOMMAND("EXECUTOR_EXTCOMMAND", "Running of external command"),
	EXECUTOR_LOGOPTIONS("EXECUTOR_LOGOPTIONS", "When this subcategory is present in the configuration file,\n" +
			" logging options are printed in the second line of each log file."),
	EXECUTOR_RUNTIME("EXECUTOR_RUNTIME", "ETS runtime events\n" +
			"(user control of execution,\n" +
			" control connections between the processes of the ETS,\n" +
			" ETS overloaded messages, etc.)"),
	EXECUTOR_UNQUALIFIED("EXECUTOR_UNQUALIFIED"),
	
	FUNCTION_RND("FUNCTION_RND", "Random number functions in TTCN-3"),
	FUNCTION_UNQUALIFIED("FUNCTION_UNQUALIFIED"),
	
	MATCHING_DONE("MATCHING_DONE", "Matching a TTCN-3 done operation"),
	MATCHING_MCSUCCESS	("MATCHING_MCSUCCESS", "Successful template matching on message-based connected ports"),
	MATCHING_MCUNSUCC("MATCHING_MCUNSUCC", "Unsuccessful template matching on message-based connected ports"),
	MATCHING_MMSUCCESS("MATCHING_MMSUCCESS", "Successful template matching on message-based mapped ports"),
	MATCHING_MMUNSUCC("MATCHING_MMUNSUCC", "Unsuccessful template matching on message-based mapped ports"),
	MATCHING_PCSUCCESS("MATCHING_PCSUCCESS", "Successful template matching on procedure-based connected ports"),
	MATCHING_PCUNSUCC("MATCHING_PCUNSUCC", "Unsuccessful template matching on procedure-based connected ports"),
	MATCHING_PMSUCCESS("MATCHING_PMSUCCESS", "Successful template matching on procedure-based mapped ports"),
	MATCHING_PMUNSUCC("MATCHING_PMUNSUCC", "Unsuccessful template matching on procedure-based mapped ports"),
	MATCHING_PROBLEM("MATCHING_PROBLEM", "Unsuccessful matching"),
	MATCHING_TIMEOUT("MATCHING_TIMEOUT", "Timer in timeout operation is not started\n or not on the list of expired timers"),
	MATCHING_UNQUALIFIED("MATCHING_UNQUALIFIED"),

	PARALLEL_PORTCONN("PARALLEL_PORTCONN", "Port connect and disconnect operations"),
	PARALLEL_PORTMAP("PARALLEL_PORTMAP", "Port map and unmap operations"),
	PARALLEL_PTC("PARALLEL_PTC", "PTC creation and finishing,\n starting and finishing a function started on a PTC"),
	PARALLEL_UNQUALIFIED("PARALLEL_UNQUALIFIED"),

	PORTEVENT_DUALRECV("PORTEVENT_DUALRECV", "Mappings of incoming message\n from the external interface of dual-faced ports\n" +
			" to the internal interface\n (decoding)"),
	PORTEVENT_DUALSEND("PORTEVENT_DUALSEND", "Mappings of outgoing message\n from the internal interface of dual-faced ports\n" +
			" to the external interface\n (encoding)"),
	PORTEVENT_MCRECV("PORTEVENT_MCRECV", "Message-based connected ports:\n incoming data received\n (receive, trigger, check)"),
	PORTEVENT_MCSEND("PORTEVENT_MCSEND", "Message-based connected ports:\n outgoing data sent\n (send)"),
	PORTEVENT_MMRECV	("PORTEVENT_MMRECV", "Message-based mapped ports:\n incoming data received\n (receive, trigger, check)"),
	PORTEVENT_MMSEND("PORTEVENT_MMSEND", "Message-based mapped ports:\n outgoing data sent\n (send)"),
	PORTEVENT_MQUEUE("PORTEVENT_MQUEUE", "Message-based ports:\n message enqueued in the queue of the port\n or extracted from the queue"),
	PORTEVENT_PCIN("PORTEVENT_PCIN", "Procedure-based connected ports:\n incoming data received\n (getcall, getreply, catch, check)"),
	PORTEVENT_PCOUT("PORTEVENT_PCOUT", "Procedure-based connected ports:\n outgoing data sent\n (call, reply, raise)"),
	PORTEVENT_PMIN("PORTEVENT_PMIN", "Procedure-based mapped ports:\n incoming data received\n (getcall, getreply, catch, check)"),
	PORTEVENT_PMOUT("PORTEVENT_PMOUT", "Procedure-based mapped ports:\n outgoing data sent\n (call, reply, raise)"),
	PORTEVENT_PQUEUE("PORTEVENT_PQUEUE", "Procedure-based ports:\n call, reply or exception enqueued in the queue of the port\n" +
			" or extracted from the queue"),
	PORTEVENT_STATE("PORTEVENT_STATE", "Port state changes\n(halt, start, stop, port clear operation finished)"),
	PORTEVENT_UNQUALIFIED("PORTEVENT_UNQUALIFIED"),

	STATISTICS_UNQUALIFIED("STATISTICS_UNQUALIFIED"),
	STATISTICS_VERDICT("STATISTICS_VERDICT", "Verdict statistics of executed test cases\n(% of none, pass, inconc, fail, error)"),

	TESTCASE_FINISH("TESTCASE_FINISH", "Testcase end and final verdict of the testcase"),
	TESTCASE_START("TESTCASE_START", "Testcase start"),
	TESTCASE_UNQUALIFIED("TESTCASE_UNQUALIFIED"),

	TIMEROP_GUARD("TIMEROP_GUARD", "Log events related to\n the guard timer used in TTCN-3 execute statements"),
	TIMEROP_READ("TIMEROP_READ", "TTCN-3 read timer operation"),
	TIMEROP_START("TIMEROP_START", "TTCN-3 start timer operation"),
	TIMEROP_STOP("TIMEROP_STOP", "TTCN-3 stop timer operation"),
	TIMEROP_TIMEOUT("TIMEROP_TIMEOUT", "Successful TTCN-3 timeout operation\n (timer found on the list of expired timers)"),
	TIMEROP_UNQUALIFIED("TIMEROP_UNQUALIFIED"),

	USER_UNQUALIFIED("USER_UNQUALIFIED"),

	VERDICTOP_FINAL("VERDICTOP_FINAL", "Final verdict of a test component\n (MTC or PTC)"),
	VERDICTOP_GETVERDICT("VERDICTOP_GETVERDICT", "TTCN-3 getverdict operation"),
	VERDICTOP_SETVERDICT("VERDICTOP_SETVERDICT", "TTCN-3 setverdict operation"),
	VERDICTOP_UNQUALIFIED	("VERDICTOP_UNQUALIFIED"),
	
	WARNING_UNQUALIFIED("WARNING_UNQUALIFIED");

	private final String name;
	private final String toolTip;
	
	private LoggingBit(final String name){
		this.name = name;
		this.toolTip = name;
	}
	
	private LoggingBit(final String name, final String tooltip){
		this.name = name;
		this.toolTip = tooltip;
	}
	
	public String getName(){
		return name;
	}
	
	public String getToolTip(){
		return toolTip;
	}
}
