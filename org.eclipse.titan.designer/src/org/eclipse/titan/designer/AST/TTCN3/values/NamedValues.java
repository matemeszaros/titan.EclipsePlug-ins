/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a list of named values.
 * 
 * @author Kristof Szabados
 * */
public final class NamedValues extends ASTNode implements IIncrementallyUpdateable {
	public static final String DUPLICATEIDENTIFIERFIRST = "Duplicate field name `{0}'' was first used here";
	public static final String DUPLICATEIDENTIFIERREPEATED = "Duplicate identifier `{0}''";

	private ArrayList<NamedValue> values;

	private Map<String, NamedValue> namedValuesMap;
	private List<NamedValue> duplicatedNames;
	private CompilationTimeStamp lastUniquenessCheck;

	public NamedValues() {
		values = new ArrayList<NamedValue>();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		values.trimToSize();
		for (int i = 0; i < values.size(); i++) {
			values.get(i).setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		NamedValue nv;
		for (int i = 0; i < values.size(); i++) {
			nv = values.get(i);
			if (nv == child) {
				return builder.append(INamedNode.DOT).append(nv.getName().getDisplayName());
			}
		}

		return builder;
	}

	public void addNamedValue(final NamedValue value) {
		if (value == null || value.getName() == null) {
			return;
		}

		values.add(value);
		value.setFullNameParent(this);
	}

	/**
	 * Remove all named values that were not parsed, but generated during
	 * previous semantic checks.
	 * */
	public void removeGeneratedValues() {
		if (values != null) {
			NamedValue temp;
			for (Iterator<NamedValue> iterator = values.iterator(); iterator.hasNext();) {
				temp = iterator.next();
				if (!temp.isParsed()) {
					iterator.remove();
				}
			}
		}
	}

	public int getSize() {
		return values.size();
	}

	public NamedValue getNamedValueByIndex(final int index) {
		return values.get(index);
	}

	public boolean hasNamedValueWithName(final Identifier name) {
		if (lastUniquenessCheck == null) {
			checkUniqueness(CompilationTimeStamp.getBaseTimestamp());
		}

		return namedValuesMap.containsKey(name.getName());
	}

	public NamedValue getNamedValueByName(final Identifier name) {
		if (lastUniquenessCheck == null) {
			checkUniqueness(CompilationTimeStamp.getBaseTimestamp());
		}

		return namedValuesMap.get(name.getName());
	}

	/**
	 * Checks the uniqueness of the named values.
	 *
	 * @param timestamp the timestamp of the actual build cycle
	 * */
	public void checkUniqueness(final CompilationTimeStamp timestamp) {
		if (lastUniquenessCheck != null && !lastUniquenessCheck.isLess(timestamp)) {
			return;
		}

		Identifier identifier;
		String name;

		if (lastUniquenessCheck == null) {
			namedValuesMap = new HashMap<String, NamedValue>(values.size());
			duplicatedNames = new ArrayList<NamedValue>();

			for (NamedValue value : values) {
				identifier = value.getName();
				name = identifier.getName();
				if (namedValuesMap.containsKey(name)) {
					if (duplicatedNames == null) {
						duplicatedNames = new ArrayList<NamedValue>();
					}
					duplicatedNames.add(value);
				} else {
					namedValuesMap.put(name, value);
				}
			}

			if (duplicatedNames != null) {
				for (NamedValue value : duplicatedNames) {
					values.remove(value);
				}
			}
		}

		if (duplicatedNames != null) {
			for (NamedValue value : duplicatedNames) {
				identifier = value.getName();
				name = identifier.getName();
				namedValuesMap.get(name).getName().getLocation().reportSingularSemanticError(
						MessageFormat.format(DUPLICATEIDENTIFIERFIRST, identifier.getDisplayName()));
				value.getLocation().reportSemanticError(MessageFormat.format(DUPLICATEIDENTIFIERREPEATED, identifier.getDisplayName()));
			}
		}

		lastUniquenessCheck = timestamp;
	}

	/**
	 * Handles the incremental parsing of this list of named values.
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

		NamedValue value;
		for (int i = 0, size = values.size(); i < size; i++) {
			value = values.get(i);

			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}

		if (duplicatedNames != null) {
			for (int i = 0, size = duplicatedNames.size(); i < size; i++) {
				value = duplicatedNames.get(i);

				value.updateSyntax(reparser, false);
				reparser.updateLocation(value.getLocation());
			}
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (values == null) {
			return;
		}

		for (NamedValue nv : values) {
			nv.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (values!=null) {
			for (NamedValue nv : values) {
				if (!nv.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
