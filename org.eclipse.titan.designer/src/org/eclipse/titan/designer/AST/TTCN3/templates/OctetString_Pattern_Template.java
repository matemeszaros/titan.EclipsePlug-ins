/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a template that holds a octetstring pattern.
 * 
 * @author Kristof Szabados
 * */
public final class OctetString_Pattern_Template extends TTCN3Template {

	private final String pattern;

	public OctetString_Pattern_Template(final String pattern) {
		this.pattern = pattern;
	}

	@Override
	public Template_type getTemplatetype() {
		return Template_type.OSTR_PATTERN;
	}

	@Override
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous octetstring pattern";
		}

		return "octetstring pattern";
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder("'");
		builder.append(pattern);
		builder.append("'O");

		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}

		return builder.toString();
	}

	public boolean containsAnyornoneSymbol() {
		return pattern.indexOf('*') > -1;
	}

	public int getMinLengthOfPattern() {
		int starCount = 0;
		int index = pattern.indexOf('*', 0);
		while (index != -1) {
			++index;
			++starCount;
			index = pattern.indexOf('*', index);
		}

		return pattern.length() - starCount;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		return Type_type.TYPE_OCTETSTRING;
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of an octetstring pattern");
	}

	@Override
	protected void checkTemplateSpecificLengthRestriction(final CompilationTimeStamp timestamp, final Type_type typeType) {
		if (Type_type.TYPE_OCTETSTRING.equals(typeType)) {
			boolean hasAnyOrNone = containsAnyornoneSymbol();
			lengthRestriction.checkNofElements(timestamp, getMinLengthOfPattern(), false, hasAnyOrNone, hasAnyOrNone, this);
		}
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		// nothing to be done
	}
}
