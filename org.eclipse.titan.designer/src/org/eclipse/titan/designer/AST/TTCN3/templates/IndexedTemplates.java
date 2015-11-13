/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Class to represent a list of indexed templates.
 * 
 * @author Kristof Szabados
 */
public final class IndexedTemplates extends ASTNode implements IIncrementallyUpdateable {

	private final ArrayList<IndexedTemplate> indexed_templates;

	public IndexedTemplates() {
		super();
		indexed_templates = new ArrayList<IndexedTemplate>();
	}

	/**
	 * Adds a new template to the list.
	 * 
	 * @param template
	 *                the template to be added.
	 * */
	public void addTemplate(final IndexedTemplate template) {
		indexed_templates.add(template);
		template.setFullNameParent(this);
	}

	/** @return the number of templates in the list */
	public int getNofTemplates() {
		return indexed_templates.size();
	}

	/**
	 * @param index
	 *                the index of the element to return.
	 * 
	 * @return the template on the indexed position.
	 * */
	public IndexedTemplate getTemplateByIndex(final int index) {
		return indexed_templates.get(index);
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		indexed_templates.trimToSize();
		for (int i = 0, size = indexed_templates.size(); i < size; i++) {
			indexed_templates.get(i).setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = indexed_templates.size(); i < size; i++) {
			IndexedTemplate template = indexed_templates.get(i);
			if (template == child) {
				IValue index = template.getIndex().getValue();
				return builder.append(INamedNode.SQUAREOPEN).append(index.createStringRepresentation())
						.append(INamedNode.SQUARECLOSE);
			}
		}

		return builder;
	}

	/**
	 * Handles the incremental parsing of this list of indexed templates.
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

		for (int i = 0, size = indexed_templates.size(); i < size; i++) {
			IndexedTemplate template = indexed_templates.get(i);

			template.updateSyntax(reparser, false);
			reparser.updateLocation(template.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (indexed_templates == null) {
			return;
		}

		for (IndexedTemplate indexedTemp : indexed_templates) {
			indexedTemp.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (indexed_templates != null) {
			for (IndexedTemplate it : indexed_templates) {
				if (!it.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
