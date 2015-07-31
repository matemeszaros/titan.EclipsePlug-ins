/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IIdentifierContainer;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Parameterized assignment.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class Ass_pard implements IIdentifierContainer, IVisitableNode {
	/**
	 * Instance counters: for each target module a separate counter is
	 * maintained to get deterministic instance numbers in case of
	 * incremental compilation.
	 */
	private final Map<Module, Integer> instance_counters = new HashMap<Module, Integer>();

	/** the time when this assignment was checked the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	protected boolean isErroneous = false;

	/**
	 * The set of all parameterized assignments that were instanced in the
	 * last or actual semantic check cycle.
	 * <p>
	 * Each semantic check cycle starts with clean -ing this storage.
	 * */
	private static final Set<Ass_pard> INSTANCED_PARAMETERIZED_ASSIGNMENTS = new HashSet<Ass_pard>();

	protected Ass_pard() {
	}

	/**
	 * Calculates the next instance number for a new assignment in a module.
	 *
	 * @param module
	 *                the module where the assignment will belong to.
	 *
	 * @return the next instance number for target module.
	 * */
	public int newInstanceNumber(final Module module) {
		int value = 0;
		if (instance_counters.containsKey(module)) {
			value = instance_counters.get(module).intValue();
		}

		value++;
		instance_counters.put(module, Integer.valueOf(value));
		INSTANCED_PARAMETERIZED_ASSIGNMENTS.add(this);

		return value;
	}

	/**
	 * Resets the instance counters of this parameterized assignment.
	 * */
	public void resetInstanceCounter() {
		instance_counters.clear();
	}

	/**
	 * Resets the instance counters of all parameterized assignments, that
	 * were instanced before.
	 * */
	public static void resetAllInstanceCounters() {
		for (Ass_pard pard : INSTANCED_PARAMETERIZED_ASSIGNMENTS) {
			pard.resetInstanceCounter();
		}
		INSTANCED_PARAMETERIZED_ASSIGNMENTS.clear();
	}

	/**
	 * Pre-process the block of formal parameters into a list. This list
	 * together with actual parameters can be used to identify the
	 * assignments correctly.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public abstract void check(final CompilationTimeStamp timestamp);
	
	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		// TODO
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		// TODO
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
