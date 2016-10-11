/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * 
 * This interface is the common abstraction of TemplateBody and AllElementsFrom used by ListOfTemplates
 * It replaces the TTCN3Template in Templates
 *
 * Represents the BNF element "TemplateListItem" 
 * Ref: ttcn3 standard "ETSI ES 201 873-1 V4.6.1 (2014-06)"
 * A.1.6.1.3 Template definitions/126.
 * @author Jeno Balasko
 *
 */
public interface ITemplateListItem extends ITTCN3Template {
	
	public void setTemplate(TTCN3Template t);
	
	public TTCN3Template getTemplate();

	public boolean accept(final ASTVisitor v);

	public void updateSyntax(TTCN3ReparseUpdater reparser, boolean b) throws ReParseException;

	public Location getLocation();

	public void findReferences(ReferenceFinder referenceFinder, List<Hit> foundIdentifiers);

	public Template_type getTemplatetype();

	public void checkSpecificValue(CompilationTimeStamp timestamp, boolean b);

}
