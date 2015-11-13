/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.definitions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * SymbolList. Used only while parsing a module (and building the AST).
 * 
 * @author Kristof Szabados
 */
public final class Symbols implements IVisitableNode {
	public static final String DUPLICATESYMBOLFIRST = "Duplicate symbol with name `{0}'' was first declared here";
	public static final String DUPLICATESYMBOLREPEATED = "Duplicate symbol with name `{0}'' was declared here again";

	/** The list of symbols contained here. */
	private final ArrayList<Identifier> symbols_v = new ArrayList<Identifier>();

	/**
	 * A hashmap of symbols, used to find multiple symbols, and to speed up
	 * searches.
	 */
	private final HashMap<String, Identifier> symbols_map = new HashMap<String, Identifier>();

	private CompilationTimeStamp lastTimeChecked;

	public void addSymbol(final Identifier id) {
		if (null != id && null != id.getName()) {
			symbols_v.add(id);
		}
	}

	/** @return the number of symbols contained in the list */
	public int size() {
		return symbols_v.size();
	}

	/**
	 * Returns the identifier of the symbol at the specified position in
	 * this list.
	 * 
	 * @param index
	 *                index of the symbol to return
	 * @return the identifier of the symbol at the specified position in
	 *         this list
	 */
	public Identifier getNthElement(final int index) {
		return symbols_v.get(index);
	}

	/**
	 * Returns <tt>true</tt> if this list contains a symbol with the given
	 * name.
	 * 
	 * @param name
	 *                The name of the symbol whose presence is to be tested
	 * @return <tt>true</tt> if this list contains a symbol with the
	 *         specified name
	 */
	public boolean hasSymbol(final String name) {
		if (null == lastTimeChecked) {
			checkUniqueness(CompilationTimeStamp.getBaseTimestamp());
		}

		return symbols_map.containsKey(name);
	}

	/**
	 * Checks the uniqueness of the symbols, and also builds a hashmap of
	 * them to speed up further searches.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void checkUniqueness(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		symbols_map.clear();
		symbols_v.trimToSize();

		for (Identifier id : symbols_v) {
			String name = id.getName();
			if (symbols_map.containsKey(name)) {
				final Location location = symbols_map.get(name).getLocation();
				location.reportSingularSemanticError(MessageFormat.format(DUPLICATESYMBOLFIRST, id.getDisplayName()));
				id.getLocation().reportSemanticError(MessageFormat.format(DUPLICATESYMBOLREPEATED, id.getDisplayName()));
			} else {
				symbols_map.put(name, id);
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (symbols_v != null) {
			for (Identifier id : symbols_v) {
				if (!id.accept(v)) {
					return false;
				}
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
