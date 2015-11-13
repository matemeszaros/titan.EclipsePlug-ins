/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents  the BNF element "ListOfTemplates" 
 * Ref: ttcn3 standard "ETSI ES 201 873-1 V4.6.1 (2014-06)"
 * A.1.6.1.3 Template definitions/125.
 *
 * Replaces the obsolete class Templates which had a field templates of a different type "TTCN3Template"
 * 
 * @author Jeno Balasko
 *
 */
public class ListOfTemplates extends ASTNode implements IIncrementallyUpdateable, Iterable<ITemplateListItem>{
	
	private final List<ITemplateListItem> templates;

	public ListOfTemplates() {
		templates = new ArrayList<ITemplateListItem>();
	}
	
	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		for (int i = 0, size = templates.size(); i < size; i++) {
			templates.get(i).setMyScope(scope);
		}
	}
	/**
	 * Adds a new template to the list.
	 * 
	 * @param template
	 *                the template to be added.
	 * */
	public void addTemplate(final ITemplateListItem template) {
		templates.add(template);
	}

	/** @return the number of templates in the list */
	public int getNofTemplates() {
		return templates.size();
	}

	/**
	 * @param index
	 *                the index of the element to return.
	 * @return the template on the indexed position.
	 * */
	public ITemplateListItem getTemplateByIndex(final int index) {
		return templates.get(index);
	}
	
	

	/**
	 * Handles the incremental parsing of this list of templates.
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

		ITemplateListItem template;
		for (int i = 0, size = templates.size(); i < size; i++) {
			template = templates.get(i);
			template.updateSyntax(reparser, false);
			reparser.updateLocation(template.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (templates == null) {
			return;
		}

		for ( ITemplateListItem  template : templates) {
			template.findReferences(referenceFinder, foundIdentifiers);
		}
	}
	
	
	
	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (templates != null) {
			for (ITemplateListItem t : templates) {
				if (!t.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public Iterator<ITemplateListItem> iterator() {
		return templates.iterator();
	}
	
}
