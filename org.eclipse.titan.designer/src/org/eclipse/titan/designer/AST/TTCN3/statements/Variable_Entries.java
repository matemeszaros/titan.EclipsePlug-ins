/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Variable_Entries extends ASTNode implements IIncrementallyUpdateable {
	private static final String FULLNAMEPART = ".ve_";

	private final List<Variable_Entry> entries;

	public Variable_Entries() {
		super();
		entries = new ArrayList<Variable_Entry>();
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i) == child) {
				return builder.append(FULLNAMEPART).append(Integer.toString(i + 1));
			}
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		for (int i = 0; i < entries.size(); i++) {
			entries.get(i).setMyScope(scope);
		}
	}

	public void add(final Variable_Entry entry) {
		if (entry != null) {
			entries.add(entry);
			entry.setFullNameParent(this);
		}
	}

	public int getNofEntries() {
		return entries.size();
	}

	public Variable_Entry getEntryByIndex(final int index) {
		return entries.get(index);
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		for (int i = 0, size = entries.size(); i < size; i++) {
			Variable_Entry entry = entries.get(i);

			entry.updateSyntax(reparser, isDamaged);
			reparser.updateLocation(entry.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (entries == null) {
			return;
		}

		for (Variable_Entry ve : entries) {
			ve.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (entries != null) {
			for (Variable_Entry ve : entries) {
				if (!ve.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
