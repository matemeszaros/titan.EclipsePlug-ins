/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.values.Notused_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a template that matches for not used elements.
 * 
 * @author Kristof Szabados
 * */
public final class NotUsed_Template extends TTCN3Template {

	// cache storing the value form of this if already created, or null
	private Notused_Value asValue = null;

	@Override
	public Template_type getTemplatetype() {
		return Template_type.TEMPLATE_NOTUSED;
	}

	@Override
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous not used symbol";
		}

		return "not used symbol";
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder("-");
		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}
		return builder.toString();
	}

	@Override
	public boolean isValue(final CompilationTimeStamp timestamp) {
		return true;
	}

	@Override
	public IValue getValue() {
		if (asValue != null) {
			return asValue;
		}

		asValue = new Notused_Value();
		asValue.setLocation(getLocation());
		asValue.setMyScope(getMyScope());
		asValue.setFullNameParent(getNameParent());
		return asValue;
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		// unfoldable at this point
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		// nothing to be done
	}

	@Override
	public boolean checkValueomitRestriction(final CompilationTimeStamp timestamp, final String definitionName, final boolean omitAllowed) {
		if (omitAllowed) {
			checkRestrictionCommon(definitionName, TemplateRestriction.Restriction_type.TR_OMIT);
		} else {
			checkRestrictionCommon(definitionName, TemplateRestriction.Restriction_type.TR_VALUE);
		}

		if (baseTemplate != null) {
			return baseTemplate.checkValueomitRestriction(timestamp, definitionName, omitAllowed);
		}

		return true;
	}
}
