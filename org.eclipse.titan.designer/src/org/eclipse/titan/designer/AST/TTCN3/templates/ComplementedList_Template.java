/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a template that matches everything but the elements in a list.
 * 
 * @author Kristof Szabados
 * */
public final class ComplementedList_Template extends CompositeTemplate {
	private static final String ANYOROMITWARNING = "`*'' in complemented list. This template will not match anything.";

	public ComplementedList_Template(final ListOfTemplates templates) {
		super(templates);
	}

	@Override
	public Template_type getTemplatetype() {
		return Template_type.COMPLEMENTED_LIST;
	}

	@Override
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous complemented list match";
		}

		return "complemented list match";
	}

	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			IType type = templates.getTemplateByIndex(i).getExpressionGovernor(timestamp, expectedValue);
			if (type != null) {
				return type;
			}
		}

		return null;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			Type_type type = templates.getTemplateByIndex(i).getExpressionReturntype(timestamp, expectedValue);
			if (!Type_type.TYPE_UNDEFINED.equals(type)) {
				return type;
			}
		}

		return Type_type.TYPE_UNDEFINED;
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of a complemented list match");
	}

	@Override
	public void checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final IType type, final boolean isModified,
			final boolean allowOmit, final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit) {
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			ITemplateListItem component = templates.getTemplateByIndex(i);
			component.setMyGovernor(type);
			ITTCN3Template temporalComponent = type.checkThisTemplateRef(timestamp, component);
			temporalComponent.checkThisTemplateGeneric(timestamp, type, false, allowOmit, true, subCheck, implicitOmit);

			if (Template_type.ANY_OR_OMIT.equals(temporalComponent.getTemplatetype())) {
				component.getLocation().reportSemanticWarning(ANYOROMITWARNING);
			}
		}

		checkLengthRestriction(timestamp, type);
		if (!allowOmit && isIfpresent) {
			location.reportSemanticError("`ifpresent' is not allowed here");
		}
		if (subCheck) {
			type.checkThisTemplateSubtype(timestamp, this);
		}
	}

	@Override
	public boolean checkPresentRestriction(final CompilationTimeStamp timestamp, final String definitionName, final Location usageLocation) {
		checkRestrictionCommon(definitionName, TemplateRestriction.Restriction_type.TR_PRESENT, usageLocation);
		
		//TODO: if ALLOW_OMIT_IN_VALUELIST_TEMPLATE_PROPERTY is switched on and has any or omit in the list then accepted, otherwise not
		//Old solution has been removed	
		
		// some basic check was performed, always needs runtime check
		return true;
	}

	@Override
	protected String getNameForStringRep() {
		return "complement";
	}
}
