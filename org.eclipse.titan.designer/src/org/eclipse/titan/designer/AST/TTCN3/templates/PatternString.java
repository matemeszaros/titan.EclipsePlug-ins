/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

// FIXME: implement
/**
 * @author Balazs Andor Zalanyi
 * */
public final class PatternString implements IVisitableNode {

	public enum PatternType {
		CHARSTRING_PATTERN, UNIVCHARSTRING_PATTERN
	}

	private PatternType patterntype;

	public PatternString() {
		patterntype = PatternType.CHARSTRING_PATTERN;
	}

	public PatternString(final PatternType pt) {
		patterntype = pt;
	}

	public PatternType getPatterntype() {
		return patterntype;
	}

	public void setPatterntype(final PatternType pt) {
		patterntype = pt;
	}

	public String getFullString() {
		return "";
	}

	/**
	 * Checks for circular references within embedded templates.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references,
	 *                must not be null.
	 **/
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		// no members
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
