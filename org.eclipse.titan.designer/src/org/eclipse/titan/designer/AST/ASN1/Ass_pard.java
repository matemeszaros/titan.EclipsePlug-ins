/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IIdentifierContainer;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTracker;
import org.eclipse.titan.designer.parsers.asn1parser.FormalParameter_Helper;

/**
 * Parameterized assignment.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Ass_pard implements IIdentifierContainer, IVisitableNode {
	/** parameter list. */
	private final Block mParameterList;

	/** The list of pre-processed formal parameters. */
	private ArrayList<FormalParameter_Helper> mParameters;

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

	public Ass_pard(final Block aParameterListV4) {
		this.mParameterList = aParameterListV4;
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
	 * Returns the list of formal parameter helpers.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 *
	 * @return the list of formal parameter helpers.
	 * */
	public List<FormalParameter_Helper> getFormalParameters(final CompilationTimeStamp timestamp) {
		check(timestamp);

		if (null == mParameters) {
			return new ArrayList<FormalParameter_Helper>();
		}

		return mParameters;
	}

	/**
	 * Pre-process the block of formal parameters into a list. This list
	 * together with actual parameters can be used to identify the
	 * assignments correctly.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && (!isErroneous || !lastTimeChecked.isLess(timestamp))) {
			return;
		}

		lastTimeChecked = timestamp;
		isErroneous = false;

		if (null == mParameterList) {
			isErroneous = true;
			return;
		}

		if (null != mParameters) {
			return;
		}

		final Asn1Parser parser = BlockLevelTokenStreamTracker.getASN1ParserForBlock(mParameterList);
		mParameters = parser.pr_special_FormalParameterList().parameters;
		List<SyntacticErrorStorage> errors = parser.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			mParameters = null;

			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport.createOnTheFlyMixedMarker((IFile) mParameterList.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}
		
		if (null == mParameters) {
			isErroneous = true;
		} else {
			mParameters.trimToSize();
		}
	}
	
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
