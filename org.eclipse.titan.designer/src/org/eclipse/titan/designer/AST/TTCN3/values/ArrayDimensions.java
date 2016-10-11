/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class ArrayDimensions extends ASTNode implements IIncrementallyUpdateable {
	private static final String ARRAYINDEXEXPECTED = "Reference to a {0} array without array index";
	private static final String INVALIDFIELDREFERENCE = "Invalid field reference `{0}'' in a {1} array";
	private static final String TOOFEWINDICES =
			"Too few indices in a reference to a {0} array: the array has {1} dimensions, but the reference has only {2} array {3}";
	private static final String TOOMANYINDICES =
			"Too many indices in a reference to a {0} array: the reference has {1} array indices, but the array has only {2} dimension{3}";

	private final List<ArrayDimension> dimensions;

	public ArrayDimensions() {
		dimensions = new ArrayList<ArrayDimension>();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		for (int i = 0; i < dimensions.size(); i++) {
			dimensions.get(i).setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0; i < dimensions.size(); i++) {
			if (dimensions.get(i) == child) {
				return builder.append(INamedNode.DOT).append(String.valueOf(i));
			}
		}

		return builder;
	}

	public void add(final ArrayDimension dimension) {
		if (dimension != null) {
			dimensions.add(dimension);
			dimension.setFullNameParent(this);
		}
	}

	public int size() {
		return dimensions.size();
	}

	public ArrayDimension get(final int index) {
		return dimensions.get(index);
	}

	public void check(final CompilationTimeStamp timestamp) {
		for (ArrayDimension dimension : dimensions) {
			dimension.check(timestamp);
		}
	}

	/**
	 * Check if two dimensions are (almost) identical.
     *
     * @param timestamp the timestamp of the actual semantic check cycle.
     * @param dimensions the dimensions to compare against the actual one.
     *
     * @return true if they are (almost) identical, false otherwise.
     */
	public boolean isIdenticial(final CompilationTimeStamp timestamp, final ArrayDimensions other) {
		if (dimensions.size() != other.dimensions.size()) {
			return false;
		}

		for (int i = 0, size = dimensions.size(); i < size; i++) {
			if (!dimensions.get(i).isIdentical(timestamp, other.dimensions.get(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check the array indices against this dimensions of the actual array type.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @param reference the reference to check.
	 * @param definitionName the name of the definition as reported in the error
	 *            message.
	 * @param allowSlicing true if the slicing of the array is allowed
	 * @param expectedValue the kind of value expected here.
	 *
	 * */
	public void checkIndices(final CompilationTimeStamp timestamp, final Reference reference, final String definitionName,
			final boolean allowSlicing, final Expected_Value_type expectedValue) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() == 1) {
			if (!allowSlicing) {
				reference.getLocation().reportSemanticError(MessageFormat.format(ARRAYINDEXEXPECTED, definitionName));
				return;
			}
		}

		int nofSubrefs = subreferences.size() - 1;
		int nofDimensions = dimensions.size();
		int upperLimit = Math.min(nofSubrefs, nofDimensions);
		ISubReference subreference;

		for (int i = 0; i < upperLimit; i++) {
			subreference = subreferences.get(i + 1);
			if (!ISubReference.Subreference_type.arraySubReference.equals(subreference.getReferenceType())) {
				subreference.getLocation().reportSemanticError(
						MessageFormat.format(INVALIDFIELDREFERENCE, subreference.getId().getDisplayName(), definitionName));
				return;
			}

			dimensions.get(i).checkIndex(timestamp, ((ArraySubReference) subreference).getValue(), expectedValue);
		}

		if (nofSubrefs < nofDimensions) {
			if (!allowSlicing) {
				reference.getLocation().reportSemanticError(
						MessageFormat.format(TOOFEWINDICES, definitionName, nofDimensions, nofSubrefs, nofSubrefs > 1 ? "indices" : "index"));
			}
		} else if (nofSubrefs > nofDimensions) {
			reference.getLocation().reportSemanticError(
					MessageFormat.format(TOOMANYINDICES, definitionName, nofDimensions, nofSubrefs, nofDimensions > 1 ? "s" : ""));
		}
	}

	/**
	 * Handles the incremental parsing of this list of array dimensions.
	 *
	 * @param reparser the parser doing the incremental parsing.
	 * @param isDamaged true if the location contains the damaged area, false if
	 *            only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		ArrayDimension dimension;
		for (int i = 0, size = dimensions.size(); i < size; i++) {
			dimension = dimensions.get(i);

			dimension.updateSyntax(reparser, false);
			reparser.updateLocation(dimension.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (dimensions == null) {
			return;
		}

		for (ArrayDimension dim : dimensions) {
			dim.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (dimensions != null) {
			for (ArrayDimension dim : dimensions) {
				if (!dim.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
