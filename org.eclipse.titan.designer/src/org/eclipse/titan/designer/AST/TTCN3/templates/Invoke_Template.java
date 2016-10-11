/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.types.Function_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ApplyExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an invoke template. <br>
 * This kind of template is not parsed, but transformed from a single value
 * template
 * 
 * @author Kristof Szabados
 * */
public final class Invoke_Template extends TTCN3Template {
	private static final String FUNCTIONEXPECTED = "A value of type function expected instead of `{0}''";
	private static final String TYPEMISSMATCHERROR = "Type mismatch: a value or template of type `{0}'' was expected instead of `{1}''";
	private static final String VALUEXPECTED1 = "A value of type function was expected";
	private static final String VALUEXPECTED2 = "Reference to a value was expected, but functions of type `{0}'' return a template of type `{1}''";

	// the first part of the expression, the function reference value to be
	// invoked
	private Value value;

	private ParsedActualParameters actualParameterList;

	// private ActualParameterList actualParameter_list;

	Invoke_Template(final CompilationTimeStamp timestamp, final SpecificValue_Template original) {
		copyGeneralProperties(original);
		IValue v = original.getValue();

		if (v == null || !Value_type.EXPRESSION_VALUE.equals(v.getValuetype())) {
			return;
		}

		Expression_Value expressionValue = (Expression_Value) v;
		if (!Operation_type.APPLY_OPERATION.equals(expressionValue.getOperationType())) {
			return;
		}

		ApplyExpression expression = (ApplyExpression) expressionValue;
		value = expression.getValue();
		actualParameterList = expression.getParameters();
	}

	@Override
	public Template_type getTemplatetype() {
		return Template_type.TEMPLATE_INVOKE;
	}

	@Override
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous template returning invoke";
		}

		return "template returning invoke";
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append(value.createStringRepresentation());
		builder.append(".invoke(");
		if (actualParameterList != null) {
			// TODO implement more precise
			// create_StringRepresentation
			builder.append("...");
		}
		builder.append(')');

		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}

		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (value != null) {
			value.setMyScope(scope);
		}
		if (actualParameterList != null) {
			actualParameterList.setMyScope(scope);
		}
		/*
		 * if(actualParameter_list != null){
		 * actualParameter_list.set_my_scope(scope); }
		 */
	}

	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		if (value == null) {
			setIsErroneous(true);
			return null;
		}

		IType type = value.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		if (type == null) {
			if (!value.getIsErroneous(timestamp)) {
				value.getLocation().reportSemanticError(VALUEXPECTED1);
			}
			setIsErroneous(true);
			return null;
		}

		type = type.getTypeRefdLast(timestamp);
		switch (type.getTypetype()) {
		case TYPE_FUNCTION:
			Type result = ((Function_Type) type).getReturnType();
			if (!Expected_Value_type.EXPECTED_TEMPLATE.equals(expectedValue) && ((Function_Type) type).returnsTemplate()) {
				location.reportSemanticError(MessageFormat.format(VALUEXPECTED2, type.getTypename(), result.getTypename()));
			}
			return result;
		case TYPE_ALTSTEP:
			setIsErroneous(true);
			return null;
		default:
			value.getLocation().reportSemanticError(MessageFormat.format(FUNCTIONEXPECTED, type.getTypename()));
		}

		return null;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp) || value == null) {
			return Type_type.TYPE_UNDEFINED;
		}

		IType type = value.getExpressionGovernor(timestamp, expectedValue);
		if (type == null) {
			return Type_type.TYPE_UNDEFINED;
		}

		return type.getTypeRefdLast(timestamp).getTypetypeTtcn3();
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		checkInvoke(timestamp);
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		// nothing to be done here, as template references can not
		// appear here
	}

	@Override
	public void checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final IType type, final boolean isModified,
			final boolean allowOmit, final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit) {
		checkInvoke(timestamp);
		IType governor = getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		if (governor == null) {
			setIsErroneous(true);
		} else if (!type.isCompatible(timestamp, governor, null, null, null)) {
			location.reportSemanticError(MessageFormat.format(TYPEMISSMATCHERROR, type.getTypename(), governor.getTypename()));
			setIsErroneous(true);
		}

		checkLengthRestriction(timestamp, type);
		if (!allowOmit && isIfpresent) {
			location.reportSemanticError("`ifpresent' is not allowed here");
		}
		if (subCheck) {
			type.checkThisTemplateSubtype(timestamp, this);
		}
	}

	public void checkInvoke(final CompilationTimeStamp timestamp) {
		if (getIsErroneous(timestamp) || actualParameterList == null || value == null) {
			return;
		}

		value.setLoweridToReference(timestamp);
		IType type = value.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		if (type != null) {
			type = type.getTypeRefdLast(timestamp);
		}

		if (type == null) {
			if (!value.getIsErroneous(timestamp)) {
				value.getLocation().reportSemanticError("A value of type function was expected in the argument");
			}
			setIsErroneous(true);
			return;
		}

		if (!Type_type.TYPE_FUNCTION.equals(type.getTypetype())) {
			value.getLocation().reportSemanticError(
					MessageFormat.format("A value of type function was expected in the argument instead of `{0}''",
							type.getTypename()));
			setIsErroneous(true);
			return;
		}

		if (myScope == null) {
			return;
		}

		myScope.checkRunsOnScope(timestamp, type, this, "call");
		FormalParameterList formalParameterList = ((Function_Type) type).getFormalParameters();
		ActualParameterList actualParameters = new ActualParameterList();
		if (!formalParameterList.checkActualParameterList(timestamp, actualParameterList, actualParameters)) {
			actualParameters.setFullNameParent(this);
			actualParameters.setMyScope(getMyScope());
		}
	}

	@Override
	public boolean checkValueomitRestriction(final CompilationTimeStamp timestamp, final String definitionName, final boolean omitAllowed, final Location usageLocation) {
		if (omitAllowed) {
			checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_OMIT, usageLocation);
		} else {
			checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_VALUE, usageLocation);
		}

		return false;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (lengthRestriction != null) {
			lengthRestriction.updateSyntax(reparser, false);
			reparser.updateLocation(lengthRestriction.getLocation());
		}

		if (baseTemplate instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) baseTemplate).updateSyntax(reparser, false);
			reparser.updateLocation(baseTemplate.getLocation());
		} else if (baseTemplate != null) {
			throw new ReParseException();
		}

		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}

		if (actualParameterList != null) {
			actualParameterList.updateSyntax(reparser, false);
			reparser.updateLocation(actualParameterList.getLocation());
		}

		/*
		 * if(actualParameter_list != null){
		 * actualParameter_list.updateSyntax(reparser, false); }
		 */
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
		if (actualParameterList != null) {
			actualParameterList.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (value != null && !value.accept(v)) {
			return false;
		}
		if (actualParameterList != null && !actualParameterList.accept(v)) {
			return false;
		}
		return true;
	}
}
