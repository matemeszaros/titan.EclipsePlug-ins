/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public class DefinitionContainer implements Iterable<Definition> {

	private final List<Definition> definitions = new CopyOnWriteArrayList<Definition>();

	private Map<String, Definition> definitionMap = new HashMap<String, Definition>();

	public Map<String, Definition> getDefinitionMap() {
		return definitionMap;
	}

	public void checkAll(final CompilationTimeStamp timestamp) {
		for (Definition def : definitions) {
			def.check(timestamp);
		}
	}

	public int size() {
		return definitions.size();
	}

	public boolean hasDefinition(final String id) {
		return definitionMap.containsKey(id);
	}

	public Definition getDefinition(final String id) {
		return definitionMap.get(id);
	}

	public List<Definition> getDefinitions() {
		return definitions;
	}

	public boolean contains(final Definition definition) {
		return definitions.contains(definition);
	}

	@Override
	public Iterator<Definition> iterator() {
		return definitions.iterator();
	}

	public void add(Definition definition) {
		definitions.add(definition);

		String definitionName = definition.getIdentifier().getName();
		if (definitionMap.containsKey(definitionName)) {
			definitionMap.get(definitionName).getIdentifier().getLocation().reportSingularSemanticError(
					MessageFormat.format(CompFieldMap.DUPLICATEFIELDNAMEFIRST, definition.getIdentifier().getDisplayName()));
			definition.getIdentifier().getLocation().reportSemanticError(
					MessageFormat.format(CompFieldMap.DUPLICATEFIELDNAMEREPEATED, definition.getIdentifier().getDisplayName()));
		} else {
			definitionMap.put(definitionName, definition);
		}
	}

	public boolean isEmpty() {
		return definitions.isEmpty();
	}

}
