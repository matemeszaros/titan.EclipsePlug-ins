/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.IdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The Def_Var class represents TTCN3 template variables.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Def_Var_Template extends Definition {
	private static final String FULLNAMEPART1 = ".<type>";
	private static final String FULLNAMEPART2 = ".<initial_value>";
	public static final String PORTNOTALLOWED = "Template variable can not be defined for port type `{0}''";
	private static final String PARAMETRIZED_LOCAL_TEMPLATE_VAR = "Code generation for parameterized local template variable `{0}'' is not yet supported";

	private static final String KIND = " template variable definition";

	private final Type type;
	
	/**
	 * Formal parameters.
	 * NOTE: It is not yet supported, so semantic error must be marked if not null 
	 */
	private FormalParameterList mFormalParList;
	private final TTCN3Template initialValue;
	private final TemplateRestriction.Restriction_type templateRestriction;

	private boolean wasAssigned;

	public Def_Var_Template( final TemplateRestriction.Restriction_type templateRestriction,
							 final Identifier identifier,
							 final Type type,
							 final FormalParameterList aFormalParList,
							 final TTCN3Template initialValue) {
		super(identifier);
		this.templateRestriction = templateRestriction;
		this.type = type;
		mFormalParList = aFormalParList;
		this.initialValue = initialValue;

		if (type != null) {
			type.setFullNameParent(this);
		}
		if (initialValue != null) {
			initialValue.setFullNameParent(this);
		}
	}

	@Override
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_VAR_TEMPLATE;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (type == child) {
			return builder.append(FULLNAMEPART1);
		} else if (initialValue == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public String getAssignmentName() {
		return "template variable";
	}

	@Override
	public String getDescription() {
		StringBuilder builder = new StringBuilder();
		builder.append(getAssignmentName()).append(" `");

		if (isLocal()) {
			builder.append(identifier.getDisplayName());
		} else {
			builder.append(getFullName());
		}

		builder.append('\'');
		return builder.toString();
	}

	@Override
	public String getOutlineIcon() {
		return "template_dynamic.gif";
	}

	@Override
	public int category() {
		int result = super.category();
		if (type != null) {
			result += type.category();
		}
		return result;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (type != null) {
			type.setMyScope(scope);
		}
		if (initialValue != null) {
			initialValue.setMyScope(scope);
		}
	}

	@Override
	public Type getType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return type;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		check(timestamp, null);
	}
		
	@Override
	public void check(final CompilationTimeStamp timestamp, IReferenceChain refChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}
		lastTimeChecked = timestamp;
		isUsed = false;
		wasAssigned = false;

		NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_VARTEMPLATE, identifier, this);
		NamingConventionHelper.checkNameContents(identifier, getMyScope().getModuleScope().getIdentifier(), getDescription());

		if (type == null) {
			lastTimeChecked = timestamp;
			return;
		}

		type.check(timestamp);

		if (initialValue == null) {
			return;
		}

		IType lastType = type.getTypeRefdLast(timestamp);
		switch (lastType.getTypetype()) {
		case TYPE_PORT:
			location.reportSemanticError(MessageFormat.format(PORTNOTALLOWED, lastType.getFullName()));
			break;
		default:
			break;
		}

		TTCN3Template realInitialValue = initialValue;

		initialValue.setMyGovernor(type);

		// Needed in case of universal charstring templates
		if (initialValue.getTemplatetype() == Template_type.CSTR_PATTERN && lastType.getTypetype() == Type.Type_type.TYPE_UCHARSTRING) {
			realInitialValue = initialValue.setTemplatetype(timestamp, Template_type.USTR_PATTERN);
			// FIXME implement setting the pattern type, once
			// universal charstring pattern are supported.
		}

		ITTCN3Template temporalValue = type.checkThisTemplateRef(timestamp, realInitialValue);
		temporalValue.checkThisTemplateGeneric(timestamp, type, true, true, true, true, false);
		TemplateRestriction.check(timestamp, this, initialValue, null);

		// Only to follow the pattern, otherwise no such field can exist
		// here
		if (withAttributesPath != null) {
			withAttributesPath.checkGlobalAttributes(timestamp, false);
			withAttributesPath.checkAttributes(timestamp);
		}

		if ( mFormalParList != null ) {
			mFormalParList.reset();
			mFormalParList.check(timestamp, getAssignmentType());
			// template variable is always local
			location.reportSemanticError(MessageFormat.format(PARAMETRIZED_LOCAL_TEMPLATE_VAR, getIdentifier()));
		}
	}

	@Override
	public void postCheck() {
		super.postCheck();
		if (!wasAssigned) {
			location.reportConfigurableSemanticProblem(
					Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.REPORTREADONLY, GeneralConstants.WARNING, null),
					MessageFormat.format("The {0} seems to be never written, maybe it could be a template", getDescription()));
		}
	}

	public TTCN3Template getInitialValue() {
		return initialValue;
	}

	/**
	 * Indicates that this variable template was used in a way where its
	 * value can be changed.
	 * */
	public void setWritten() {
		wasAssigned = true;
	}

	@Override
	public boolean checkIdentical(final CompilationTimeStamp timestamp, final Definition definition) {
		check(timestamp);
		definition.check(timestamp);

		if (!Assignment_type.A_VAR_TEMPLATE.equals(definition.getAssignmentType())) {
			location.reportSemanticError(MessageFormat
					.format("Local definition `{0}'' is a template variable, but the definition inherited from component type `{1}'' is a {2}",
							identifier.getDisplayName(), definition.getMyScope().getFullName(),
							definition.getAssignmentName()));
			return false;
		}

		Def_Var_Template otherVariable = (Def_Var_Template) definition;
		if (!type.isIdentical(timestamp, otherVariable.type)) {
			final String message = MessageFormat
					.format("Local template variable `{0}'' has type `{1}'', but the template variable inherited from component type `{2}'' has type `{3}''",
							identifier.getDisplayName(), type.getTypename(), otherVariable.getMyScope().getFullName(),
							otherVariable.type.getTypename());
			type.getLocation().reportSemanticError(message);
			return false;
		}

		if (initialValue != null) {
			if (otherVariable.initialValue == null) {
				initialValue.getLocation()
						.reportSemanticWarning(
								MessageFormat.format(
										"Local template variable `{0}'' has initial value, but the template variable inherited from component type `{1}'' does not",
										identifier.getDisplayName(), otherVariable.getMyScope().getFullName()));
			}
		} else if (otherVariable.initialValue != null) {
			location.reportSemanticWarning(MessageFormat
					.format("Local template variable `{0}'' does not have initial value, but the template variable inherited from component type `{1}'' has",
							identifier.getDisplayName(), otherVariable.getMyScope().getFullName()));
		}

		return true;
	}

	@Override
	public String getProposalKind() {
		StringBuilder builder = new StringBuilder();
		if (type != null) {
			type.getProposalDescription(builder);
		}
		builder.append(KIND);
		return builder.toString();
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= i) {
			return;
		}

		if (subrefs.size() == i + 1 && identifier.getName().toLowerCase().startsWith(subrefs.get(i).getId().getName().toLowerCase())) {
			super.addProposal(propCollector, i);
		}
		if (subrefs.size() > i + 1 && type != null && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match
			type.addProposal(propCollector, i + 1);
		}
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() > i && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			if (subrefs.size() > i + 1 && type != null) {
				type.addDeclaration(declarationCollector, i + 1);
			} else if (subrefs.size() == i + 1 && Subreference_type.fieldSubReference.equals(subrefs.get(i).getReferenceType())) {
				declarationCollector.addDeclaration(this);
			}
		}
	}

	@Override
	public TemplateRestriction.Restriction_type getTemplateRestriction() {
		return templateRestriction;
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		List<Integer> result = super.getPossibleExtensionStarterTokens();
		
		if (initialValue == null) {
			result.add(Ttcn3Lexer.ASSIGNMENTCHAR);
		}

		return result;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			boolean enveloped = false;

			Location temporalIdentifier = identifier.getLocation();
			if (reparser.envelopsDamage(temporalIdentifier) || reparser.isExtending(temporalIdentifier)) {
				reparser.extendDamagedRegion(temporalIdentifier);
				IIdentifierReparser r = new IdentifierReparser(reparser);
				int result = r.parseAndSetNameChanged();
				identifier = r.getIdentifier();
				// damage handled
				if (result == 0 && identifier != null) {
					enveloped = true;
				} else {
					throw new ReParseException(result);
				}
			}

			if (type != null) {
				if (enveloped) {
					type.updateSyntax(reparser, false);
					reparser.updateLocation(type.getLocation());
				} else if (reparser.envelopsDamage(type.getLocation())) {
					type.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(type.getLocation());
				}
			}

			if (initialValue != null) {
				if (enveloped) {
					initialValue.updateSyntax(reparser, false);
					reparser.updateLocation(initialValue.getLocation());
				} else if (reparser.envelopsDamage(initialValue.getLocation())) {
					initialValue.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(initialValue.getLocation());
				}
			}

			if (withAttributesPath != null) {
				if (enveloped) {
					withAttributesPath.updateSyntax(reparser, false);
					reparser.updateLocation(withAttributesPath.getLocation());
				} else if (reparser.envelopsDamage(withAttributesPath.getLocation())) {
					withAttributesPath.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(withAttributesPath.getLocation());
				}
			}

			if (!enveloped) {
				throw new ReParseException();
			}

			return;
		}

		reparser.updateLocation(identifier.getLocation());
		if (type != null) {
			type.updateSyntax(reparser, false);
			reparser.updateLocation(type.getLocation());
		}

		if (initialValue != null) {
			initialValue.updateSyntax(reparser, false);
			reparser.updateLocation(initialValue.getLocation());
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
		if (initialValue != null) {
			initialValue.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (type != null && !type.accept(v)) {
			return false;
		}
		if (initialValue != null && !initialValue.accept(v)) {
			return false;
		}
		return true;
	}

	public boolean getWritten() {
		return wasAssigned;
	}
}
