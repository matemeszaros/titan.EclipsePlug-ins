/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a template for the superset matching mechanism.
 * 
 * @author Kristof Szabados
 * */
public final class SupersetMatch_Template extends CompositeTemplate {

	public SupersetMatch_Template(final ListOfTemplates templates) {
		super(templates);
	}

	@Override
	public Template_type getTemplatetype() {
		return Template_type.SUPERSET_MATCH;
	}

	@Override
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous superset match";
		}

		return "superset match";
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		return Type_type.TYPE_SET_OF;
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of a superset match");
	}

	@Override
	protected void checkTemplateSpecificLengthRestriction(final CompilationTimeStamp timestamp, final Type_type typeType) {
		if (Type_type.TYPE_SET_OF.equals(typeType)) {
			lengthRestriction.checkNofElements(timestamp, getNofTemplatesNotAnyornone(timestamp), true, false, true, this);
		}
	}

	@Override
	protected String getNameForStringRep() {
		return "superset";
	}
}
