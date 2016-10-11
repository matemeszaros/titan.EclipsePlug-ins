/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class EnumerationItems extends ASTNode implements IIncrementallyUpdateable {
	private static final String FULLNAMEPART = ".<unknown_enumeration_item>";

	private final List<EnumItem> items;

	private Location location = NULL_Location.INSTANCE;

	public EnumerationItems() {
		items = new ArrayList<EnumItem>();
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (EnumItem item : items) {
			if (item == child) {
				final Identifier identifier = item.getId();
				if (identifier != null) {
					return builder.append(INamedNode.DOT).append(identifier.getDisplayName());
				}

				return builder.append(FULLNAMEPART);
			}
		}

		return builder;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public void addEnumItem(final EnumItem enumItem) {
		if (enumItem != null && enumItem.getId() != null) {
			items.add(enumItem);
			enumItem.setFullNameParent(this);
		}
	}

	public List<EnumItem> getItems() {
		return items;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		for (EnumItem item : items) {
			item.setMyScope(scope);
		}
	}

	public void addProposal(final ProposalCollector propCollector) {
		for (EnumItem item : items) {
			item.addProposal(propCollector);
		}
	}

	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		for (EnumItem item : items) {
			item.addDeclaration(declarationCollector, i);
		}
	}
	
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i, final Location commentLocation) {
		for (EnumItem item : items) {
			item.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		for (int i = 0, size = items.size(); i < size; i++) {
			EnumItem item = items.get(i);

			item.updateSyntax(reparser, false);
			reparser.updateLocation(item.getLocation());
		}
	}
	
	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (items != null) {
			for (EnumItem ei : items) {
				ei.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (items != null) {
			for (EnumItem ei : items) {
				if (!ei.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
