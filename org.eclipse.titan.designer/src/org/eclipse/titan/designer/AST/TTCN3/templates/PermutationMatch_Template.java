/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a template for permutation matching.
 * 
 * @author Kristof Szabados
 * */
public final class PermutationMatch_Template extends CompositeTemplate {

	public PermutationMatch_Template(final ListOfTemplates templates) {
		super(templates);
	}

	@Override
	public Template_type getTemplatetype() {
		return Template_type.PERMUTATION_MATCH;
	}

	@Override
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous permutation match";
		}

		return "permutation match";
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allow_omit) {
		getLocation().reportSemanticError("A specific value expected instead of a permutation match");
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			for (int i = 0; i < templates.getNofTemplates(); i++) {
				ITTCN3Template template = templates.getTemplateByIndex(i);
				if (template != null) {
					referenceChain.markState();
					template.checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
		}
	}

	@Override
	protected String getNameForStringRep() {
		return "permutation";
	}
}
