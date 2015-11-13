/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Adam Delic
 * */
public final class Pattern_ParsedSubType extends ParsedSubType {
	private PatternString pattern;

	public Pattern_ParsedSubType(final PatternString pattern) {
		this.pattern = pattern;
	}

	@Override
	public ParsedSubType_type getSubTypetype() {
		return ParsedSubType_type.PATTERN_PARSEDSUBTYPE;
	}

	public PatternString getPattern() {
		return pattern;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		/*
		 * if (pattern != null) { pattern.updateSyntax(reparser, false);
		 * reparser.updateLocation(pattern.getLocation()); }
		 */
	}

	@Override
	public Location getLocation() {
		// FIXME: call pattern.getLocation() when it's done
		return null;
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (pattern != null) {
			if (!pattern.accept(v)) {
				return false;
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
