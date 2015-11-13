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
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a single value sub-type restriction as it was parsed.
 * 
 * @author Adam Delic
 * */
public final class Single_ParsedSubType extends ParsedSubType {
	private Value value;

	public Single_ParsedSubType(final Value value) {
		this.value = value;
	}

	@Override
	public ParsedSubType_type getSubTypetype() {
		return ParsedSubType_type.SINGLE_PARSEDSUBTYPE;
	}

	public Value getValue() {
		return value;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}
	}

	@Override
	public Location getLocation() {
		return value.getLocation();
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value == null) {
			return;
		}

		value.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (value != null) {
			if (!value.accept(v)) {
				return false;
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
