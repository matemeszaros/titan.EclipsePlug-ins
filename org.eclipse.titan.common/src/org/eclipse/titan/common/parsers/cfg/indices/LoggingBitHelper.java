/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Kristof Szabados
 * */
public final class LoggingBitHelper {
	
	private static final Map<LoggingBit, LoggingBit[]> CHILD_RELATION = new EnumMap<LoggingBit, LoggingBit[]>(LoggingBit.class);

	private LoggingBitHelper() {
		//Do nothing
	}
	
	static {
		CHILD_RELATION.put(LoggingBit.EXECUTOR, new LoggingBit[] { LoggingBit.EXECUTOR_COMPONENT, LoggingBit.EXECUTOR_CONFIGDATA,
				LoggingBit.EXECUTOR_EXTCOMMAND, LoggingBit.EXECUTOR_LOGOPTIONS, LoggingBit.EXECUTOR_RUNTIME,
				LoggingBit.EXECUTOR_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.ERROR, new LoggingBit[] { LoggingBit.ERROR_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.WARNING, new LoggingBit[] { LoggingBit.WARNING_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.PORTEVENT,
				new LoggingBit[] { LoggingBit.PORTEVENT_DUALRECV, LoggingBit.PORTEVENT_DUALSEND, LoggingBit.PORTEVENT_MCRECV,
						LoggingBit.PORTEVENT_MCSEND, LoggingBit.PORTEVENT_MMRECV, LoggingBit.PORTEVENT_MMSEND,
						LoggingBit.PORTEVENT_MQUEUE, LoggingBit.PORTEVENT_PCIN, LoggingBit.PORTEVENT_PCOUT,
						LoggingBit.PORTEVENT_PMIN, LoggingBit.PORTEVENT_PMOUT, LoggingBit.PORTEVENT_PQUEUE,
						LoggingBit.PORTEVENT_STATE, LoggingBit.PORTEVENT_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.TIMEROP, new LoggingBit[] { LoggingBit.TIMEROP_GUARD, LoggingBit.TIMEROP_READ, LoggingBit.TIMEROP_START,
				LoggingBit.TIMEROP_STOP, LoggingBit.TIMEROP_TIMEOUT, LoggingBit.TIMEROP_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.VERDICTOP, new LoggingBit[] { LoggingBit.VERDICTOP_FINAL, LoggingBit.VERDICTOP_GETVERDICT,
				LoggingBit.VERDICTOP_SETVERDICT, LoggingBit.VERDICTOP_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.DEFAULTOP, new LoggingBit[] { LoggingBit.DEFAULTOP_ACTIVATE, LoggingBit.DEFAULTOP_DEACTIVATE,
				LoggingBit.DEFAULTOP_EXIT, LoggingBit.DEFAULTOP_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.ACTION, new LoggingBit[] { LoggingBit.ACTION_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.TESTCASE, new LoggingBit[] { LoggingBit.TESTCASE_FINISH, LoggingBit.TESTCASE_START,
				LoggingBit.TESTCASE_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.FUNCTION, new LoggingBit[] { LoggingBit.FUNCTION_RND, LoggingBit.FUNCTION_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.USER, new LoggingBit[] { LoggingBit.USER_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.STATISTICS, new LoggingBit[] { LoggingBit.STATISTICS_VERDICT, LoggingBit.STATISTICS_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.PARALLEL, new LoggingBit[] { LoggingBit.PARALLEL_PORTCONN, LoggingBit.PARALLEL_PORTMAP,
				LoggingBit.PARALLEL_PTC, LoggingBit.PARALLEL_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.MATCHING, new LoggingBit[] { LoggingBit.MATCHING_DONE, LoggingBit.MATCHING_MCSUCCESS,
				LoggingBit.MATCHING_MCUNSUCC, LoggingBit.MATCHING_MMSUCCESS, LoggingBit.MATCHING_MMUNSUCC,
				LoggingBit.MATCHING_PCSUCCESS, LoggingBit.MATCHING_PCUNSUCC, LoggingBit.MATCHING_PMSUCCESS,
				LoggingBit.MATCHING_PMUNSUCC, LoggingBit.MATCHING_PROBLEM, LoggingBit.MATCHING_TIMEOUT,
				LoggingBit.MATCHING_UNQUALIFIED });
		CHILD_RELATION.put(LoggingBit.DEBUG, new LoggingBit[] { LoggingBit.DEBUG_ENCDEC, LoggingBit.DEBUG_TESTPORT,
				LoggingBit.DEBUG_UNQUALIFIED });
	}
	
	public static boolean hasChildren(final LoggingBit bit){
		return CHILD_RELATION.containsKey(bit);
	}
	
	public static LoggingBit[] getChildren(final LoggingBit bit){
		if(CHILD_RELATION.containsKey(bit)){
			return CHILD_RELATION.get(bit);
		}
		
		return new LoggingBit[]{};
	}
	
	public static LoggingBit[] getFirstLevelNodes(){
		Set<LoggingBit> temp = CHILD_RELATION.keySet();
		return temp.toArray(new LoggingBit[temp.size()]);
	}
	
	public static LoggingBit[] getLogAllBits(){
		return new LoggingBit[]{LoggingBit.ACTION, LoggingBit.DEFAULTOP, LoggingBit.ERROR, LoggingBit.EXECUTOR,
				LoggingBit.FUNCTION, LoggingBit.PARALLEL, LoggingBit.PORTEVENT, LoggingBit.STATISTICS,
				LoggingBit.TESTCASE, LoggingBit.TIMEROP, LoggingBit.USER, LoggingBit.VERDICTOP, LoggingBit.WARNING};
	}
	
	public static LoggingBit getParent(final LoggingBit bit){
		for(LoggingBit parent: CHILD_RELATION.keySet()){
			LoggingBit[] children = CHILD_RELATION.get(parent);
			
			for(LoggingBit temp: children){
				if(bit.equals(temp)){
					return parent;
				}
			}
		}
		
		return null;
	}
}
