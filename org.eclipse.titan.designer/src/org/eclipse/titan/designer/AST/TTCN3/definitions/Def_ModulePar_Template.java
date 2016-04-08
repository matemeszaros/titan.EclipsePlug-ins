/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Function_Type;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.T3Doc;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.IdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * @author Kristof Szabados
 * */
public final class Def_ModulePar_Template extends Definition {
	private static final String FULLNAMEPART1 = ".<type>";
	private static final String FULLNAMEPART2 = ".<default_template>";
	private static final String KIND = " template module parameter";

	public static String getKind() {
		return KIND;
	}

	private final Type type;
	private final TTCN3Template defaultTemplate;
	private ITTCN3Template realTemplate;

	public Def_ModulePar_Template(final Identifier identifier, final Type type, final TTCN3Template defaultTemplate) {
		super(identifier);
		this.type = type;
		this.defaultTemplate = defaultTemplate;
		if (type != null) {
			type.setFullNameParent(this);
		}
		if (defaultTemplate != null) {
			defaultTemplate.setFullNameParent(this);
		}
	}

	public ITTCN3Template getDefaultTemplate(){
		//FIXME: check() should be call ???
		return defaultTemplate;
	}
	
	public ITTCN3Template getRealTemplate(){
		//FIXME: check() should be call ???
		return realTemplate;
	}

	@Override
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_MODULEPAR_TEMPLATE;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);
		if (type == child) {
			return builder.append(FULLNAMEPART1);
		} else if (defaultTemplate == child) {
			return builder.append(FULLNAMEPART2);
		}
		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (type != null) {
			type.setMyScope(scope);
		}
		if (defaultTemplate != null) {
			defaultTemplate.setMyScope(scope);
		}
	}

	@Override
	public String getAssignmentName() {
		return "template module parameter";
	}

	@Override
	public String getOutlineIcon() {
		return "module_parameter.gif";
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
	public Type getType(final CompilationTimeStamp timestamp) {
		check(timestamp);
		return type;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		T3Doc.check(this.getCommentLocation(), KIND);

		isUsed = false;

		NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_MODULEPAR, identifier, this);
		NamingConventionHelper.checkNameContents(identifier, getMyScope().getModuleScope().getIdentifier(), getDescription());

		if (withAttributesPath != null) {
			withAttributesPath.checkGlobalAttributes(timestamp, false);
			withAttributesPath.checkAttributes(timestamp);
		}

		if (type == null) {
			return;
		}

		type.check(timestamp);
		lastTimeChecked = timestamp;

		IType lastType = type.getTypeRefdLast(timestamp);
		switch (lastType.getTypetype()) {
		case TYPE_PORT:
			location.reportSemanticError(MessageFormat.format(Def_ModulePar.PORTNOTALLOWED, lastType.getFullName()));
			break;
		case TYPE_SIGNATURE:
			location.reportSemanticError(MessageFormat.format(Def_ModulePar.SIGNATURENOTALLOWED, lastType.getFullName()));
			break;
		case TYPE_FUNCTION:
		case TYPE_ALTSTEP:
		case TYPE_TESTCASE:
			if (((Function_Type) lastType).isRunsOnSelf()) {
				location.reportSemanticError(MessageFormat.format(Def_ModulePar.RUNSONSELF_NOT_ALLOWED, lastType.getFullName()));
			}
			break;
		default:
			break;
		}

		if (defaultTemplate != null) {
			realTemplate = defaultTemplate;
			// Needed in case of universal charstring templates
			if (defaultTemplate.getTemplatetype() == Template_type.CSTR_PATTERN
					&& lastType.getTypetype() == Type.Type_type.TYPE_UCHARSTRING) {
				realTemplate = defaultTemplate.setTemplatetype(timestamp, Template_type.USTR_PATTERN);
				// FIXME implement setting the pattern type,
				// once universal charstring pattern are
				// supported.
			}
			ITTCN3Template temporalTemplate = type.checkThisTemplateRef(timestamp, realTemplate);
			temporalTemplate.checkThisTemplateGeneric(timestamp, type, false, true, true, true, false);
			IReferenceChain tempReferenceChain = ReferenceChain.getInstance(Def_Template.CIRCULAREMBEDDEDRECURSION, true);
			tempReferenceChain.add(this);
			temporalTemplate.checkRecursions(timestamp, tempReferenceChain);
			tempReferenceChain.release();
		}
	}

	@Override
	public void postCheck() {
		super.postCheck();
		postCheckPrivateness();
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
	public String getOutlineText() {
		StringBuilder text = new StringBuilder(getIdentifier().getDisplayName());
		text.append(" : ");
		text.append(type.getTypename());
		return text.toString();
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		List<Integer> result = super.getPossibleExtensionStarterTokens();
		
		if (defaultTemplate == null) {
			result.add(Ttcn3Lexer.ASSIGNMENTCHAR);
		}

		return result;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			boolean enveloped = false;

			Location temporalIdentifier = identifier.getLocation();
			if (reparser.envelopsDamage(temporalIdentifier) || reparser.isExtending(temporalIdentifier)) {
				reparser.extendDamagedRegion(temporalIdentifier);
				IIdentifierReparser r = new IdentifierReparser(reparser);
				int result = r.parseAndSetNameChanged();
				identifier = r.getIdentifier();
				// damage handled
				if (result == 0) {
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

			if (defaultTemplate != null) {
				if (enveloped) {
					defaultTemplate.updateSyntax(reparser, false);
					reparser.updateLocation(defaultTemplate.getLocation());
				} else if (reparser.envelopsDamage(defaultTemplate.getLocation())) {
					defaultTemplate.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(defaultTemplate.getLocation());
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

		if (defaultTemplate != null) {
			defaultTemplate.updateSyntax(reparser, false);
			reparser.updateLocation(defaultTemplate.getLocation());
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
		if (defaultTemplate != null) {
			defaultTemplate.findReferences(referenceFinder, foundIdentifiers);
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
		if (defaultTemplate != null && !defaultTemplate.accept(v)) {
			return false;
		}
		return true;
	}
}
