/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.List;

import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.ParsedActualParameters;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The ParameterisedSubReference class represents a part of a TTCN3 or ASN.1
 * reference, which was given in a parameterized notation ('name(value1,
 * value2)').
 * 
 * @author Kristof Szabados
 * */
public final class ParameterisedSubReference extends ASTNode implements ISubReference, ILocateableNode {
	public static final String INVALIDSUBREFERENCE = "The type `{0}'' cannot be parameterised.";
	public static final String INVALIDVALUESUBREFERENCE = "Invalid reference: internal parameterisation is not supported";

	private final Identifier identifier;
	private ParsedActualParameters parsedParameters;
	private ActualParameterList actualParameters;

	private Location location;

	public ParameterisedSubReference(final Identifier identifier, final ParsedActualParameters parsedParameters) {
		this.identifier = identifier;
		this.parsedParameters = parsedParameters;

		if (parsedParameters != null) {
			parsedParameters.setFullNameParent(this);
		}
	}

	@Override
	public Subreference_type getReferenceType() {
		return Subreference_type.parameterisedSubReference;
	}

	@Override
	public Identifier getId() {
		return identifier;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (parsedParameters != null) {
			parsedParameters.setMyScope(scope);
		}
	}

	public ParsedActualParameters getParsedParameters() {
		return parsedParameters;
	}

	public ActualParameterList getActualParameters() {
		return actualParameters;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	public boolean checkParameters(final CompilationTimeStamp timestamp, final FormalParameterList formalParameterList) {
		actualParameters = new ActualParameterList();
		boolean isErroneous = formalParameterList.checkActualParameterList(timestamp, parsedParameters, actualParameters);
		actualParameters.setFullNameParent(this);
		actualParameters.setMyScope(myScope);

		return isErroneous;
	}

	@Override
	public String toString() {
		return "parameterisedSubReference: " + identifier.getDisplayName();
	}

	@Override
	public void appendDisplayName(final StringBuilder builder) {
		if (builder.length() > 0) {
			builder.append('.');
		}
		builder.append(identifier.getDisplayName()).append("()");
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(identifier.getLocation());
		if (parsedParameters != null) {
			parsedParameters.updateSyntax(reparser, false);
			reparser.updateLocation(parsedParameters.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (parsedParameters != null) {
			parsedParameters.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}

		if (parsedParameters != null && !parsedParameters.accept(v)) {
			return false;
		}

		return true;
	}
}
