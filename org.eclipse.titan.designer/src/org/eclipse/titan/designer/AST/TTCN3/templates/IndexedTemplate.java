/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Class to represent an IndexedTemplate.
 * 
 * @author Kristof Szabados
 */
public final class IndexedTemplate extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {

	private final ArraySubReference index;
	private final ITemplateListItem template;

	/**
	 * The location of the whole template. This location encloses the
	 * template fully, as it is used to report errors to.
	 **/
	private Location location;

	public IndexedTemplate(final ArraySubReference index, final ITemplateListItem template) {
		super();
		this.index = index;
		this.template = template;
		location = NULL_Location.INSTANCE;

		if (index != null) {
			index.setFullNameParent(this);
		}
		if (template != null) {
			template.setFullNameParent(this);
		}
	}

	public ArraySubReference getIndex() {
		return index;
	}

	public ITemplateListItem getTemplate() {
		return template;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (index != null) {
			index.setMyScope(scope);
		}
		if (template != null) {
			template.setMyScope(scope);
		}
	}

	/**
	 * Handles the incremental parsing of this indexed template.
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

		if (index != null) {
			index.updateSyntax(reparser, false);
			reparser.updateLocation(index.getLocation());
		}

		if (template != null) {
			template.updateSyntax(reparser, false);
			reparser.updateLocation(template.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (template == null) {
			return;
		}

		template.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (index != null && !index.accept(v)) {
			return false;
		}
		if (template != null && !template.accept(v)) {
			return false;
		}
		return true;
	}
}
