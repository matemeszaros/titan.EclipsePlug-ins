/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Function_Type;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.T3Doc;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserFactory;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * The Def_ModulePar class represents TTCN3 module parameter definitions.
 * 
 * @author Kristof Szabados
 * */
public final class Def_ModulePar extends Definition {
	private static final String FULLNAMEPART1 = ".<type>";
	private static final String FULLNAMEPART2 = ".<default_value>";
	public static final String PORTNOTALLOWED = "Module parameter can not be of port type `{0}''";
	public static final String SIGNATURENOTALLOWED = "Module parameter can not be of signature type `{0}''";
	public static final String RUNSONSELF_NOT_ALLOWED = "Module parameter can not be of function reference type `{0}'' which has runs on self clause";

	private static final String KIND = " module parameter";

	public static String getKind() {
		return KIND;
	}

	private final Type type;
	private final Value default_value;

	public Def_ModulePar(final Identifier identifier, final Type type, final Value default_value) {
		super(identifier);
		this.type = type;
		this.default_value = default_value;

		if (type != null) {
			type.setFullNameParent(this);
		}
		if (default_value != null) {
			default_value.setFullNameParent(this);
		}
	}

	@Override
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_MODULEPAR;
	}
	
	public Value getDefaultValue() {
		return default_value;
	}
	

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (type == child) {
			return builder.append(FULLNAMEPART1);
		} else if (default_value == child) {
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
		if (default_value != null) {
			default_value.setMyScope(scope);
		}
	}

	@Override
	public String getAssignmentName() {
		return "module parameter";
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
			location.reportSemanticError(MessageFormat.format(PORTNOTALLOWED, lastType.getFullName()));
			break;
		case TYPE_SIGNATURE:
			location.reportSemanticError(MessageFormat.format(SIGNATURENOTALLOWED, lastType.getFullName()));
			break;
		case TYPE_FUNCTION:
		case TYPE_ALTSTEP:
		case TYPE_TESTCASE:
			if (((Function_Type) lastType).isRunsOnSelf()) {
				location.reportSemanticError(MessageFormat.format(RUNSONSELF_NOT_ALLOWED, lastType.getFullName()));
			}
			break;
		default:
			break;
		}

		if (default_value != null) {
			default_value.setMyGovernor(type);
			IValue temporalValue = type.checkThisValueRef(timestamp, default_value);
			type.checkThisValue(timestamp, temporalValue, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT, true, false,
					true, hasImplicitOmitAttribute(timestamp), false));
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
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			reparser.moduleToBeReanalysed.add(getMyScope().getModuleScope().getName());
			boolean enveloped = false;

			Location temporalIdentifier = identifier.getLocation();
			if (reparser.envelopsDamage(temporalIdentifier) || reparser.isExtending(temporalIdentifier)) {
				reparser.fullAnalysysNeeded = true;
				reparser.extendDamagedRegion(temporalIdentifier);
				IIdentifierReparser r = ParserFactory.createIdentifierReparser(reparser);
				int result = r.parseAndSetNameChanged();
				identifier = r.getIdentifier();
				// damage handled
				if (result == 0) {
					enveloped = true;
					reparser.moduleToBeReanalysed.addAll(referingHere);
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
					reparser.moduleToBeReanalysed.addAll(referingHere);
				}
			}

			if (default_value != null) {
				if (enveloped) {
					default_value.updateSyntax(reparser, false);
					reparser.updateLocation(default_value.getLocation());
				} else if (reparser.envelopsDamage(default_value.getLocation())) {
					default_value.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(default_value.getLocation());
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
					reparser.moduleToBeReanalysed.addAll(referingHere);
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

		if (default_value != null) {
			default_value.updateSyntax(reparser, false);
			reparser.updateLocation(default_value.getLocation());
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
		if (default_value != null) {
			default_value.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (type != null && !type.accept(v)) {
			return false;
		}
		if (default_value != null && !default_value.accept(v)) {
			return false;
		}
		return true;
	}
}
