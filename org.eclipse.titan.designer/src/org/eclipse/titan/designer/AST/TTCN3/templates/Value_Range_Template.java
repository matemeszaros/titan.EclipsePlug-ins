/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a template that matches a range of values.
 * 
 * @author Kristof Szabados
 * */
public final class Value_Range_Template extends TTCN3Template {

	private final ValueRange valueRange;

	public Value_Range_Template(final ValueRange valueRange) {
		this.valueRange = valueRange;
	}

	@Override
	public Template_type getTemplatetype() {
		return Template_type.VALUE_RANGE;
	}

	@Override
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous value range match";
		}

		return "value range match";
	}

	public ValueRange getValueRange() {
		return valueRange;
	}

	@Override
	public String createStringRepresentation() {
		if (valueRange == null) {
			return "<erroneous template>";
		}

		StringBuilder builder = new StringBuilder();
		builder.append(valueRange.createStringRepresentation());

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
		if (valueRange != null) {
			valueRange.setMyScope(scope);
		}
	}

	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		if (valueRange != null) {
			valueRange.getExpressionGovernor(timestamp, expectedValue);
		}

		return null;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp) || valueRange == null) {
			return Type_type.TYPE_UNDEFINED;
		}

		return valueRange.getExpressionReturntype(timestamp, expectedValue);
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of a value range match");
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		// nothing to be done here, as template references can not
		// appear here
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

		if (valueRange != null) {
			valueRange.updateSyntax(reparser, false);
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (valueRange == null) {
			return;
		}

		valueRange.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (valueRange != null && !valueRange.accept(v)) {
			return false;
		}
		return true;
	}
}
