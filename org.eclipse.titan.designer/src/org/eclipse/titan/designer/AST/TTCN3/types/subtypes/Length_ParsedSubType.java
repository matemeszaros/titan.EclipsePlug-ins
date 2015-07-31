/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.TTCN3.templates.LengthRestriction;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a length sub-type restriction as it was parsed.
 * 
 * @author Adam Delic
 * */
public final class Length_ParsedSubType extends ParsedSubType {
	private LengthRestriction length;

	public Length_ParsedSubType(final LengthRestriction length) {
		this.length = length;
	}

	@Override
	public ParsedSubType_type getSubTypetype() {
		return ParsedSubType_type.LENGTH_PARSEDSUBTYPE;
	}

	public LengthRestriction getLength() {
		return length;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (length != null) {
			length.updateSyntax(reparser, false);
			reparser.updateLocation(length.getLocation());
		}
	}

	@Override
	public Location getLocation() {
		return length.getLocation();
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (length == null) {
			return;
		}

		length.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (length != null) {
			if (!length.accept(v)) {
				return false;
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
