/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ValueList_Template;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReparseUtilities;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Return_Statement extends Statement {
	private static final String SPECIFICVALUEEXPECTED = "A specific value without matching symbols was expected as return value";
	private static final String MISSINGTEMPLATE = "Missing return template. The function should return a template of type `{0}''";
	private static final String MISSINGVALUE = "Missing return value. The function should return a value of type `{0}''";
	private static final String UNEXPECTEDRETURNVALUE = "Unexpected return value. The function does not have return type";
	private static final String UNEXPETEDRETURNSTATEMENT = "Return statement cannot be used in a {0}. It is allowed only in functions and altsteps";
	private static final String ALTSTEPRETURNINGVALUE = "An altstep cannot return a value";
	private static final String USAGEINCONTROLPART = "Return statement cannot be used in the control part. It is alowed only in functions and altsteps";
	private static final String FULLNAMEPART = ".returnexpression";
	private static final String STATEMENT_NAME = "return";

	private final TTCN3Template template;

	public Return_Statement(final TTCN3Template template) {
		this.template = template;

		if (template != null) {
			template.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_RETURN;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (template == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (template != null) {
			template.setMyScope(scope);
		}
	}

	@Override
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		return true;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		final Definition definition = myStatementBlock.getMyDefinition();
		if (definition == null) {
			location.reportSemanticError(USAGEINCONTROLPART);
			return;
		}

		switch (definition.getAssignmentType()) {
		case A_FUNCTION:
			if (template != null) {
				template.getLocation().reportSemanticError(UNEXPECTEDRETURNVALUE);
			}
			break;
		case A_FUNCTION_RVAL:
			final Type returnType = ((Def_Function) definition).getType(timestamp);
			if (template == null) {
				location.reportSemanticError(MessageFormat.format(MISSINGVALUE,  returnType.getTypename()));
				break;
			}
			
			switch (template.getTemplatetype()) {
			case VALUE_LIST:
				if (((ValueList_Template) template).getNofTemplates() == 1) {
					// ValueList_Template with one element can be accepted as a
					// hidden expression
					// TODO: if you want to compile this, the type should change
					// for SingleExpression
					break;
				}
			case SPECIFIC_VALUE:
			default:
				if (!template.isValue(timestamp)) {
					template.getLocation().reportSemanticError(SPECIFICVALUEEXPECTED);
					break;
				}
			}

			// General:
			template.setMyGovernor(returnType);
			final ITTCN3Template temporalTemplate = returnType.checkThisTemplateRef(timestamp, template,Expected_Value_type.EXPECTED_DYNAMIC_VALUE,null);
			temporalTemplate.checkThisTemplateGeneric(timestamp, returnType, false, /* isModified */
					false, /* allowOmit */
					true, /* allowAnyOrOmit */ //TODO:false
					true, /* subCheck */
					false /* implicitOmit */);
			TemplateRestriction.check(timestamp, definition, temporalTemplate, null);
			break;

		case A_FUNCTION_RTEMP:
			if (template == null) {
				location.reportSemanticError(MessageFormat.format(MISSINGTEMPLATE, ((Def_Function) definition).getType(timestamp)
						.getTypename()));
			} else {
				final Type returnType1 = ((Def_Function) definition).getType(timestamp);
				template.setMyGovernor(returnType1);
				final ITTCN3Template temporalTemplate1 = returnType1.checkThisTemplateRef(timestamp, template,Expected_Value_type.EXPECTED_TEMPLATE,null);
				temporalTemplate1.checkThisTemplateGeneric(timestamp, returnType1, true, /* isModified */
						true, /* allowOmit */
						true, /* allowAnyOrOmit */
						true, /* subCheck */
						true); /* implicitOmit */
				TemplateRestriction.check(timestamp, definition, temporalTemplate1, null);
			}
			break;
		case A_ALTSTEP:
			if (template != null) {
				template.getLocation().reportSemanticError(ALTSTEPRETURNINGVALUE);
			}
			break;
		default:
			location.reportSemanticError(MessageFormat.format(UNEXPETEDRETURNSTATEMENT, definition.getAssignmentName()));
			break;
		}
	}

	@Override
	public void checkAllowedInterleave() {
		location.reportSemanticError("Return statement is not allowed within an interleave statement");
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (template != null) {
			return null;
		}

		return ReparseUtilities.getAllValidTokenTypes();
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (template != null) {
			template.updateSyntax(reparser, false);
			reparser.updateLocation(template.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (template == null) {
			return;
		}

		template.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (template != null && !template.accept(v)) {
			return false;
		}
		return true;
	}
}
