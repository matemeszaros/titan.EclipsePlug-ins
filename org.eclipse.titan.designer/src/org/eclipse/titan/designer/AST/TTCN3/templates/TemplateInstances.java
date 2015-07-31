/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a list of template instances.
 * 
 * @author Kristof Szabados
 */
public final class TemplateInstances extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {

	private final List<TemplateInstance> instances;

	/** The location of the template instances. */
	private Location location;

	public TemplateInstances() {
		super();
		instances = new ArrayList<TemplateInstance>();
		location = NULL_Location.INSTANCE;
	}

	public TemplateInstances(final TemplateInstances other) {
		super();
		instances = new ArrayList<TemplateInstance>(other.instances.size());
		for (int i = 0, size = other.instances.size(); i < size; i++) {
			instances.add(other.instances.get(i));
		}
		location = other.location;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		for (int i = 0, size = instances.size(); i < size; i++) {
			instances.get(i).setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = instances.size(); i < size; i++) {
			if (instances.get(i) == child) {
				return builder.append(INamedNode.SQUAREOPEN).append(String.valueOf(i + 1)).append(INamedNode.SQUARECLOSE);
			}
		}

		return builder;
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
	 * Adds a new template instance to the list.
	 * 
	 * @param instance
	 *                the template instance to add.
	 * */
	public void addTemplateInstance(final TemplateInstance instance) {
		if (instance != null) {
			instances.add(instance);
			instance.setFullNameParent(this);
		}
	}

	/** @return the number of template instances in the list */
	public int getNofTis() {
		return instances.size();
	}

	/**
	 * @param index
	 *                the index of the element to return.
	 * 
	 * @return the template instance on the indexed position.
	 * */
	public TemplateInstance getInstanceByIndex(final int index) {
		return instances.get(index);
	}

	/**
	 * Handles the incremental parsing of this list of template instances.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		for (int i = 0, size = instances.size(); i < size; i++) {
			TemplateInstance instance = instances.get(i);

			instance.updateSyntax(reparser, false);
			reparser.updateLocation(instance.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (instances == null) {
			return;
		}

		for (TemplateInstance ti : instances) {
			ti.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (instances != null) {
			for (TemplateInstance ti : instances) {
				if (!ti.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}

	public String createStringRepresentation() {
		StringBuilder sb = new StringBuilder();
		for (TemplateInstance ti : instances) {
			sb.append(ti.createStringRepresentation() + ", ");
		}
		if (!instances.isEmpty()) {
			sb.setLength(sb.length() - 2);
		}
		return sb.toString();
	}
}
