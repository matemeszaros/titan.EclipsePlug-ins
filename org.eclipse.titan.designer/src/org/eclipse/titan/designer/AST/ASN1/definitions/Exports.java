/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.definitions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ASN1.Defined_Reference;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents the exported symbols of a module.
 * 
 * @author Kristof Szabados
 */
public final class Exports extends ASTNode implements ILocateableNode {
	/** my module. */
	private ASN1Module module;
	/** exported symbols. */
	private final Symbols symbols;
	/**
	 * exports all (true if the module of this export list exports all of
	 * its assignments).
	 */
	private final boolean export_all;

	/**
	 * The location of the whole export list. This location encloses the
	 * export list fully, as it is used to report errors to.
	 **/
	private Location location = NULL_Location.INSTANCE;

	/**
	 * Holds the last time when these exports were checked, or null if
	 * never.
	 */
	private CompilationTimeStamp lastCompilationTimeStamp;

	public Exports(final boolean export_all) {
		this.export_all = export_all;
		if (export_all) {
			symbols = null;
		} else {
			symbols = new Symbols();
		}
	}

	public Exports(final Symbols symbols) {
		export_all = false;
		this.symbols = symbols;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	/**
	 * Sets the module of this export list to be the provided module.
	 * 
	 * @param module
	 *                the module of this export list.
	 * */
	public void setMyModule(final ASN1Module module) {
		this.module = module;
	}

	public boolean exportsSymbol(final CompilationTimeStamp timestamp, final Identifier id) {
		check(timestamp);

		if (export_all) {
			return true;
		}

		return symbols.hasSymbol(id.getName());

	}

	/**
	 * Checks this export list.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastCompilationTimeStamp && !lastCompilationTimeStamp.isLess(timestamp)) {
			return;
		}

		if (export_all) {
			lastCompilationTimeStamp = timestamp;
			return;
		}

		symbols.checkUniqueness(timestamp);

		for (int i = 0; i < symbols.size(); i++) {
			List<ISubReference> list = new ArrayList<ISubReference>();
			list.add(new FieldSubReference(symbols.getNthElement(i)));
			Defined_Reference reference = new Defined_Reference(null, list);

			/* check whether exists or not */
			module.getAssBySRef(timestamp, reference);
		}

		lastCompilationTimeStamp = timestamp;
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		// TODO
		return true;
	}
}
