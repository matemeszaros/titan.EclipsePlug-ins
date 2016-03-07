/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a list of type mappings.
 * 
 * @author Kristof Szabados
 * */
public final class TypeMappings extends ASTNode implements ILocateableNode {
	private List<TypeMapping> mappings;
	private HashMap<String, TypeMapping> mappingsMap;

	/** the time when this type mapping was check the last time. */
	private CompilationTimeStamp lastTimeChecked;

	private Location location;

	public TypeMappings() {
		mappings = new ArrayList<TypeMapping>();
		mappingsMap = new HashMap<String, TypeMapping>();
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	public void addMapping(final TypeMapping mapping) {
		mappings.add(mapping);
	}

	public int getNofMappings() {
		return mappings.size();
	}

	public TypeMapping getMappingByIndex(final int index) {
		return mappings.get(index);
	}

	public boolean hasMappingForType(final CompilationTimeStamp timestamp, final IType type) {
		check(timestamp);
		if (type.getIsErroneous(timestamp)) {
			return true;
		}

		return mappingsMap.containsKey(type.getTypename());
	}

	public TypeMapping getMappingForType(final CompilationTimeStamp timestamp, final IType type) {
		check(timestamp);

		return mappingsMap.get(type.getTypename());
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = mappings.size(); i < size; i++) {
			if (mappings.get(i) == child) {
				return builder.append(".<mapping").append(i + 1).append('>');
			}
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		for (int i = 0, size = mappings.size(); i < size; i++) {
			mappings.get(i).setMyScope(scope);
		}
	}

	/**
	 * Copy over the mappings from the provided mapping list.
	 * 
	 * @param otherMappings
	 *                the other list of mappings.
	 * */
	public void copyMappings(final TypeMappings otherMappings) {
		for (int i = 0, size = otherMappings.getNofMappings(); i < size; i++) {
			mappings.add(otherMappings.getMappingByIndex(i));
		}

		// join the locations
		getLocation().setEndOffset(otherMappings.getLocation().getEndOffset());
	}

	/**
	 * Does the semantic checking of the type mapping.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		mappingsMap.clear();
		for (int i = 0, size = mappings.size(); i < size; i++) {
			TypeMapping mapping = mappings.get(i);
			mapping.check(timestamp);
			Type sourceType = mapping.getSourceType();

			if (sourceType != null && !sourceType.getTypeRefdLast(timestamp).getIsErroneous(timestamp)) {
				String sourceName = sourceType.getTypename();
				if (mappingsMap.containsKey(sourceName)) {
					sourceType.getLocation().reportSemanticError(
							MessageFormat.format("Duplicate mapping for type `{0}''", sourceName));
					final String message = MessageFormat.format("The mapping of the type `{0}'' is already given here",
							sourceName);
					mappingsMap.get(sourceName).getLocation().reportSemanticWarning(message);
				} else {
					mappingsMap.put(sourceName, mapping);
				}
			}
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (mappings != null) {
			for (TypeMapping tm : mappings) {
				tm.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (mappings != null) {
			for (TypeMapping tm : mappings) {
				if (!tm.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
