/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IIdentifierContainer;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Adam Delic
 * */
public final class ErroneousAttributeSpecification implements ILocateableNode, IIncrementallyUpdateable, IIdentifierContainer, IVisitableNode {

	public enum Indicator_Type {
		Before_Indicator("before"), Value_Indicator("value"), After_Indicator("after"), Invalid_Indicator("<invalid>");

		private final String name;

		private Indicator_Type(final String name) {
			this.name = name;
		}

		public String getDisplayName() {
			return name;
		}
	}

	private final Indicator_Type indicator;
	private final boolean isRaw;
	private final TemplateInstance templateInst;
	private final boolean hasAllKeyword;
	// set by check() or null if tmpl_inst is invalid or omit
	private IType type = null;
	// set by check() or null if tmpl_inst is invalid or omit
	private IValue value = null;

	/**
	 * The location of the whole specification. This location encloses the
	 * specification fully, as it is used to report errors to.
	 **/
	private Location location = NULL_Location.INSTANCE;

	public ErroneousAttributeSpecification(final Indicator_Type indicator, final boolean isRaw, final TemplateInstance templateInst,
			final boolean hasAllKeyword) {
		this.indicator = indicator;
		this.isRaw = isRaw;
		this.templateInst = templateInst;
		this.hasAllKeyword = hasAllKeyword;
	}

	public Indicator_Type getIndicator() {
		return indicator;
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	public boolean isOmit() {
		final ITTCN3Template templateBody = templateInst.getTemplateBody();
		switch (templateBody.getTemplatetype()) {
		case OMIT_VALUE:
			return true;
		case SPECIFIC_VALUE:
			return (((SpecificValue_Template) templateBody).getSpecificValue().getValuetype() == IValue.Value_type.OMIT_VALUE);
		default:
			return false;
		}
	}

	public void check(final CompilationTimeStamp timestamp, final Scope scope) {
		templateInst.setMyScope(scope);
		if (isOmit()) {
			// special case, no type needed
			if (indicator == Indicator_Type.Before_Indicator || indicator == Indicator_Type.After_Indicator) {
				if (!hasAllKeyword) {
					final String message = MessageFormat.format(
							"Keyword `all'' is expected after `omit'' when omitting all fields {0} the specified field",
							indicator.getDisplayName());
					templateInst.getLocation().reportSemanticError(message);
				}
			} else {
				if (hasAllKeyword) {
					templateInst.getLocation().reportSemanticError(
							"Unexpected `all' keyword after `omit' when omitting one field");
				}
			}
			type = null;
			return;
		}
		if (hasAllKeyword) {
			templateInst.getLocation().reportSemanticError("Unexpected `all' keyword after the in-line template");
		}
		// determine the type of the tmpl_inst
		type = templateInst.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
		if (type == null) {
			templateInst.getTemplateBody().setLoweridToReference(timestamp);
			type = templateInst.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
		}
		if (type == null) {
			templateInst.getLocation().reportSemanticError("Cannot determine the type of the in-line template");
			return;
		}
		type.check(timestamp);
		final IType typeLast = type.getTypeRefdLast(timestamp);
		if (typeLast == null || typeLast.getIsErroneous(timestamp)) {
			type = null;
			return;
		}
		if (isRaw) {
			switch (typeLast.getTypetypeTtcn3()) {
			case TYPE_BITSTRING:
			case TYPE_OCTETSTRING:
			case TYPE_CHARSTRING:
			case TYPE_UCHARSTRING:
				break;
			default:
				templateInst.getLocation().reportSemanticError(
						MessageFormat.format("An in-line template of type `{0}'' cannot be used as a `raw'' erroneous value",
								typeLast.getTypename()));
			}
		}
		if (templateInst.getDerivedReference() != null) {
			templateInst.getLocation().reportSemanticError(
					"Reference to a constant value was expected instead of an in-line modified template");
			type = null;
			return;
		}

		final ITTCN3Template templ = templateInst.getTemplateBody();
		if (templ.isValue(timestamp)) {
			value = templ.getValue();
			value.setMyGovernor(type);
			type.checkThisValueRef(timestamp, value);
			type.checkThisValue(timestamp, value, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT, false, false, true,
					false, false));
		} else {
			templateInst.getLocation().reportSemanticError("A specific value without matching symbols was expected");
			type = null;
			return;
		}
	}

	/**
	 * Handles the incremental parsing of this attribute specification.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException { // TODO
		if (isDamaged) {
			throw new ReParseException();
		}

		templateInst.updateSyntax(reparser, false);
		reparser.updateLocation(templateInst.getLocation());
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (templateInst != null) {
			templateInst.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	public boolean accept(final ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (templateInst != null) {
			if (!templateInst.accept(v)) {
				return false;
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
