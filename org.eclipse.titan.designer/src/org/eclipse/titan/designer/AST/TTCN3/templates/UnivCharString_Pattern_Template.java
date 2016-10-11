/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString.PatternType;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

//FIXME implement as soon as charstring pattern templates become handled
/**
 * Represents a template that holds a charstring pattern.
 * 
 * @author Kristof Szabados
 * */
public final class UnivCharString_Pattern_Template extends TTCN3Template {

	private PatternString patternstring;

	public UnivCharString_Pattern_Template() {
		patternstring = new PatternString(PatternType.UNIVCHARSTRING_PATTERN);
	}

	public UnivCharString_Pattern_Template(final PatternString ps) {
		patternstring = ps;
	}

	public PatternString getPatternstring() {
		return patternstring;
	}

	@Override
	public Template_type getTemplatetype() {
		return Template_type.USTR_PATTERN;
	}

	@Override
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous universal character string pattern";
		}

		return "universal character string pattern";
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder("pattern \"");
		builder.append(patternstring.getFullString());
		builder.append('"');

		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}

		return builder.toString();
	}

	public boolean patternContainsAnyornoneSymbol() {
		return true;
	}

	public int getMinLengthOfPattern() {
		// TODO maybe we can say something more precise
		return 0;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		return Type_type.TYPE_UCHARSTRING;
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of a universal charstring pattern");
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this) && patternstring != null) {
			patternstring.checkRecursions(timestamp, referenceChain);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (patternstring != null && !patternstring.accept(v)) {
			return false;
		}
		return true;
	}
}
