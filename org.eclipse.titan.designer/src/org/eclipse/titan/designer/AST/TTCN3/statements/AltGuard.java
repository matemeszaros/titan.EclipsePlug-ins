/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * The AltGuard class represents a TTCN3 altstep/alt/interleave branch.
 * 
 * @see AltGuards
 * 
 * @author Kristof Szabados
 * */
public abstract class AltGuard extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	public enum altguard_type {
		AG_OP, AG_REF, AG_INVOKE, AG_ELSE
	}

	private final altguard_type altguardType;

	protected final StatementBlock statementblock;

	/**
	 * The location of the whole altguard. This location encloses the
	 * altguard fully, as it is used to report errors to.
	 **/
	private Location location = NULL_Location.INSTANCE;

	/** the time when this altguard was checked the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	public AltGuard(final altguard_type altguardType, final StatementBlock statementblock) {
		super();
		this.altguardType = altguardType;
		this.statementblock = statementblock;
	}

	public final altguard_type getType() {
		return altguardType;
	}

	public final StatementBlock getStatementBlock() {
		return statementblock;
	}

	public abstract void setMyStatementBlock(StatementBlock statementBlock, int index);

	public abstract void setMyDefinition(Definition definition);

	@Override
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public final Location getLocation() {
		return location;
	}

	public abstract void setMyAltguards(AltGuards altGuards);

	/**
	 * Checks whether the altguard has a return statement, either directly
	 * or embedded.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return the return status of the altguard.
	 * */
	public abstract StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp);

	/**
	 * Does the semantic checking of this branch.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public abstract void check(final CompilationTimeStamp timestamp);

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public abstract void checkAllowedInterleave();

	/**
	 * Checks the properties of the statement, that can only be checked
	 * after the semantic check was completely run.
	 */
	public abstract void postCheck();
}
