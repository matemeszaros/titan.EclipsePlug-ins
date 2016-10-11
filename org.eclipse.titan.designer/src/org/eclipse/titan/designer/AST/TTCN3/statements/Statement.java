/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.TTCN3.IAppendableSyntax;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock.ReturnStatus_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The Statement class represents a general TTCN3 statement.
 * <p>
 * This class is used a base class for all the specific TTCN3 statements.
 * 
 * @author Kristof Szabados
 * */
public abstract class Statement extends ASTNode implements ILocateableNode, IAppendableSyntax, IIncrementallyUpdateable {

	public enum Statement_type {
		// ambiguous statements
		/** start undefined. */
		S_START_UNKNOWN,
		/** stop undefined. */
		S_STOP_UNKNOWN,
		/** unknown instance. */
		S_UNKNOWN_INSTANCE,
		/** unknonwn applied inst. */
		S_UNKNOWN_APPLIED_INSTANE,
		// basic statements
		/** definition. */
		S_DEF,
		/** assignment. */
		S_ASSIGNMENT,
		/** while loop. */
		S_WHILE,
		/** do-while loop. */
		S_DOWHILE,
		/** for loop. */
		S_FOR,
		/** if statement. */
		S_IF,
		/** statement block. */
		S_BLOCK,
		/** select statement. */
		S_SELECT,
		/** try - catch.*/
		S_TRY_CATCH,
		/** log(...). */
		S_LOG,
		/** label identifier. */
		S_LABEL,
		/** goto identifier. */
		S_GOTO,
		/** function instance. */
		S_FUNCTION_INSTANCE,
		/** function applied. */
		S_FUNCTION_APPLIED,
		/** stop execution. */
		S_STOP_EXECUTION,
		/** testcase.stop */
		S_TESTCASE_STOP,
		/** break. */
		S_BREAK,
		/** continue. */
		S_CONTINUE,
		// behavior statements
		/** repeat. */
		S_REPEAT,
		/** alt construct. */
		S_ALT,
		/** interleave construct. */
		S_INTERLEAVE,
		/** call operation of ports. */
		S_CALL,
		/** altstep instance. */
		S_ALTSTEP_INSTANCE,
		/** altstep applied. */
		S_ALTSTEP_APPLIED,
		/** return. */
		S_RETURN,
		// default statements
		/** activate. */
		S_ACTIVATE,
		/** activate references. */
		S_ACTIVATE_REFERENCED,
		/** deactivate. */
		S_DEACTIVATE,
		// communication (port) statements
		/** send. */
		S_SEND,
		/** reply. */
		S_REPLY,
		/** raise. */
		S_RAISE,
		/** getcall. */
		S_GETCALL,
		/** getreply. */
		S_GETREPLY,
		/** catch. */
		S_CATCH,
		/** check. */
		S_CHECK,
		/** check getcall. */
		S_CHECK_GETCALL,
		/** check getreply. */
		S_CHECK_GETREPLY,
		/** check catch. */
		S_CHECK_CATCH,
		/** trigger. */
		S_TRIGGER,
		/** receive. */
		S_RECEIVE,
		/** check receive. */
		S_CHECK_RECEIVE,
		/** clear. */
		S_CLEAR_PORT,
		/** start port. */
		S_START_PORT,
		/** stop port. */
		S_STOP_PORT,
		/** halt port. */
		S_HALT_PORT,
		// component statements
		/** start component. */
		S_START_COMPONENT,
		/** start refd component. */
		S_START_REFERENCED_COMPONENT,
		/** stop component. */
		S_STOP_COMPONENT,
		/** component done. */
		S_DONE,
		/** kill. */
		S_KILL,
		/** killed. */
		S_KILLED,
		// configuration statements
		/** map. */
		S_MAP,
		/** unmap. */
		S_UNMAP,
		/** connect. */
		S_CONNECT,
		/** disconnect. */
		S_DISCONNECT,
		// timer statements
		/** start timer. */
		S_START_TIMER,
		/** stop timer. */
		S_STOP_TIMER,
		/** timer timeout. */
		S_TIMEOUT,
		// verdict statment
		/** setverdict(value). */
		S_SETVERDICT,
		// SUT statement
		/** action(...). */
		S_ACTION,
		// control statement
		/** testcase instance. */
		S_TESTCASE_INSTANCE,
		/** refd testcase instance */
		S_REFERENCED_TESTCASE_INSTANCE,
		/** string2ttcn predef. func */
		S_STRING2TTCN,
		/** int2enum predef. func. */
		S_INT2ENUM,
		// profiler statement
		/** start profiler. */
		S_START_PROFILER,
		/** stop profiler. */
		S_STOP_PROFILER
	}

	/** the statementblock in which this statement resides. */
	protected StatementBlock myStatementBlock;

	/**
	 * The location of the whole statement. This location encloses the
	 * statement fully, as it is used to report errors to.
	 **/
	protected Location location;

	/** the time when this statement was check the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	protected boolean isErroneous;

	protected Statement() {
		isErroneous = false;
		location = NULL_Location.INSTANCE;
	}

	@Override
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public final Location getLocation() {
		return location;
	}

	/**
	 * @return the exact type of the statement.
	 * */
	public abstract Statement_type getType();

	public final CompilationTimeStamp getLastTimeChecked() {
		return lastTimeChecked;
	}

	public final boolean getIsErroneous() {
		return isErroneous;
	}

	public final void setIsErroneous() {
		isErroneous = true;
	}

	public abstract String getStatementName();

	/**
	 * Sets the statementblock in which this statement was be found.
	 * 
	 * @param statementBlock
	 *                the statementblock containing this statement.
	 * @param index
	 *                the index of this statement in the statement block.
	 * */
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		myStatementBlock = statementBlock;
	}

	/** @return the parent statement block of the actual statement */
	public final StatementBlock getMyStatementBlock() {
		return myStatementBlock;
	}

	/**
	 * Add the provided definition down to all statements.
	 * 
	 * @param definition
	 *                the definition the statement is located within.
	 * */
	public void setMyDefinition(final Definition definition) {
	}

	/**
	 * Sets the altguards for the statement in which the statement actually
	 * is.
	 * 
	 * @param altGuards
	 *                the altguards to which the statement belongs to.
	 * */
	public void setMyAltguards(final AltGuards altGuards) {
	}

	/**
	 * Checks if the statement has a terminating potential.
	 * <p>
	 * Infinite loops also have this potential as the only way to exit them
	 * is to terminate the program.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * 
	 * @return true if the statement can terminate the current statement
	 *         block, false otherwise.
	 * */
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		return false;
	}

	/**
	 * Checks whether the statement has a return statement, either directly
	 * or embedded.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return the return status of the statement.
	 * */
	public ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (isTerminating(timestamp)) {
			return ReturnStatus_type.RS_YES;
		}

		return ReturnStatus_type.RS_NO;
	}

	/**
	 * Does the semantic checking of the statement.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public abstract void check(final CompilationTimeStamp timestamp);

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public void checkAllowedInterleave() {
	}

	/**
	 * Checks the properties of the statement, that can only be checked
	 * after the semantic check was completely run.
	 * <p>
	 * The default behavior does not define any operation.
	 * */
	public void postCheck() {
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		// By default statements can not be extended
		return null;
	}

	@Override
	public List<Integer> getPossiblePrefixTokens() {
		// By default statements can not be prepended
		return new ArrayList<Integer>(0);
	}

	/**
	 * Handles the incremental parsing of this statement.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public abstract void updateSyntax(TTCN3ReparseUpdater reparser, boolean isDamaged) throws ReParseException;
}
