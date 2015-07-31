/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Call_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Catch_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Check_Catch_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Check_Getcall_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Check_Getreply_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Check_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Check_Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Done_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Getcall_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Getreply_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Killed_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.statements.Timeout_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Trigger_Port_Statement;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks the following code smell:
 * A shorthand timeout/receive/trigger/getcall/catch/check/check_receive/
 * check_getcall/check_getreply/check_catch/getreply/done/killed statement
 * is used inside a function, testcase, or altstep without the 'runs on'
 * clause, except for when the statement is located inside an alt statement.
 * 
 * @author Viktor Varga
 */
public class Shorthand extends BaseModuleCodeSmellSpotter {
	
	private static final String ERROR_MESSAGE_PREFIX = "The shorthand ";
	private static final String ERROR_MESSAGE_SUFFIX = " statement should not be used, an activated default can change its behaviour";
	
	private static final String NAME_TIMEOUT = "timeout";
	private static final String NAME_RECEIVE = "receive";
	private static final String NAME_TRIGGER = "trigger";
	private static final String NAME_GETCALL = "getcall";
	private static final String NAME_CATCH = "catch";
	private static final String NAME_CHECK = "check";
	private static final String NAME_CHECK_RECEIVE = "check-receive"; 
	private static final String NAME_CHECK_GETCALL = "check-getcall";
	private static final String NAME_CHECK_GETREPLY = "check-getreply";
	private static final String NAME_CHECK_CATCH = "check-catch";
	private static final String NAME_GETREPLY = "getreply";
	private static final String NAME_DONE = "done";
	private static final String NAME_KILLED = "killed";
	
	private String typename = "";
	
	private final CompilationTimeStamp timestamp;
	
	protected Shorthand() {
		super(CodeSmellType.SHORTHAND);
		timestamp = CompilationTimeStamp.getBaseTimestamp();
	}
	
	@Override
	protected void process(IVisitableNode node, Problems problems) {
		if (node instanceof Timeout_Statement) {
			typename = NAME_TIMEOUT;
		} else if (node instanceof Receive_Port_Statement) {
			typename = NAME_RECEIVE;
		} else if (node instanceof Trigger_Port_Statement) {
			typename = NAME_TRIGGER;
		} else if (node instanceof Getcall_Statement) {
			typename = NAME_GETCALL;
		} else if (node instanceof Catch_Statement) {
			typename = NAME_CATCH;
		} else if (node instanceof Check_Port_Statement) {
			typename = NAME_CHECK;
		} else if (node instanceof Check_Receive_Port_Statement) {
			typename = NAME_CHECK_RECEIVE;
		} else if (node instanceof Check_Getcall_Statement) {
			typename = NAME_CHECK_GETCALL;
		} else if (node instanceof Check_Catch_Statement) {
			typename = NAME_CHECK_CATCH;
		} else if (node instanceof Check_Getreply_Statement) {
			typename = NAME_CHECK_GETREPLY;
		} else if (node instanceof Getreply_Statement) {
			typename = NAME_GETREPLY;
		} else if (node instanceof Done_Statement) {
			typename = NAME_DONE;
		} else if (node instanceof Killed_Statement) {
			typename = NAME_KILLED;
		} else {
			return;
		}
		Statement s = (Statement)node;
		check(s, problems);
	}
	
	protected void check(Statement s, Problems problems) {
		if (s == null) {
			return;
		}
		//shorthand statements are ignored inside alt statements
		INamedNode curr = s;
		while (curr != null) {
			if (curr instanceof Alt_Statement || curr instanceof Call_Statement) {
				return;
			}
			curr = curr.getNameParent();
		}
		StatementBlock sb = s.getMyStatementBlock();
		if (sb == null) {
			return;
		}
		Definition d = sb.getMyDefinition();
		if (d == null) {
			return;
		}
		//shorthand statements are marked in functions, test cases and altsteps that have a 'runs on' clause
		if (d instanceof Def_Function && ((Def_Function)d).getRunsOnType(timestamp) != null) {
			problems.report(s.getLocation(), ERROR_MESSAGE_PREFIX + typename + ERROR_MESSAGE_SUFFIX);
			return;
		}
		if (d instanceof Def_Altstep || d instanceof Def_Testcase) {
			problems.report(s.getLocation(), ERROR_MESSAGE_PREFIX + typename + ERROR_MESSAGE_SUFFIX);
		}
	}
	
	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(13);
		ret.add(Timeout_Statement.class);
		ret.add(Receive_Port_Statement.class);
		ret.add(Trigger_Port_Statement.class);
		ret.add(Getcall_Statement.class);
		ret.add(Catch_Statement.class);
		ret.add(Check_Port_Statement.class);
		ret.add(Check_Receive_Port_Statement.class);
		ret.add(Check_Getcall_Statement.class);
		ret.add(Check_Getreply_Statement.class);
		ret.add(Check_Catch_Statement.class);
		ret.add(Getreply_Statement.class);
		ret.add(Done_Statement.class);
		ret.add(Killed_Statement.class);
		return ret;
	}
	

}
