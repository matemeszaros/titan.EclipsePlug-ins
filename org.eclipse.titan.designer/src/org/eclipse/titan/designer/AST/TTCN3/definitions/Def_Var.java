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

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
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
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserFactory;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The Def_Var class represents TTCN3 variable definitions.
 * 
 * @author Kristof Szabados
 * */
public final class Def_Var extends Definition {
	private static final String FULLNAMEPART1 = ".<type>";
	private static final String FULLNAMEPART2 = ".<initial_value>";
	public static final String PORTNOTALLOWED = "Variable can not be defined for port type `{0}''";
	public static final String SIGNATURENOTALLOWED = "Variable can not be defined for signature `{0}''";

	private static final String KIND = " variable definition";

	private final Type type;
	private final Value initial_value;

	private boolean wasAssigned;

	public Def_Var(final Identifier identifier, final Type type, final Value initial_value) {
		super(identifier);
		this.type = type;
		this.initial_value = initial_value;

		if (type != null) {
			type.setFullNameParent(this);
		}
		if (initial_value != null) {
			initial_value.setFullNameParent(this);
		}
	}

	@Override
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_VAR;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (type == child) {
			return builder.append(FULLNAMEPART1);
		} else if (initial_value == child) {
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
		if (initial_value != null) {
			initial_value.setMyScope(scope);
		}
	}

	@Override
	public String getAssignmentName() {
		return "variable";
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
		return "variable.gif";
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

	public Value getInitialValue() {
		return initial_value;
	}

	@Override
	public void setWithAttributes(final MultipleWithAttributes attributes) {
		// variable should not have with attributes
	}

	@Override
	public void setAttributeParentPath(final WithAttributesPath parent) {
		// variable should not have with attributes
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		isUsed = false;
		wasAssigned = false;

		if (getMyScope() instanceof ComponentTypeBody) {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_VARIABLE, identifier, this);
		} else {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_VARIABLE, identifier, this);
		}
		NamingConventionHelper.checkNameContents(identifier, getMyScope().getModuleScope().getIdentifier(), getDescription());

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
		default:
			break;
		}

		if (initial_value != null) {
			initial_value.setMyGovernor(type);
			IValue temporalValue = type.checkThisValueRef(timestamp, initial_value);
			if (isLocal()) {
				type.checkThisValue(timestamp, temporalValue, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE,
						true, false, true, false, false));
			} else {
				type.checkThisValue(timestamp, temporalValue, new ValueCheckingOptions(Expected_Value_type.EXPECTED_STATIC_VALUE,
						true, false, true, false, false));
			}
		}
	}

	@Override
	public void postCheck() {
		super.postCheck();
		if (!wasAssigned) {
			if (initial_value != null && !initial_value.getIsErroneous(lastTimeChecked) && !initial_value.isUnfoldable(lastTimeChecked)) {
				final String message = MessageFormat.format("The {0} seems to be never written, maybe it could be a constant",
						getDescription());
				final String option = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
						PreferenceConstants.REPORTREADONLY, GeneralConstants.WARNING, null);
				location.reportConfigurableSemanticProblem(option, message);
			}
		}
	}

	/**
	 * Indicates that this variable was used in a way where its value can be
	 * changed.
	 * */
	public void setWritten() {
		wasAssigned = true;
	}

	@Override
	public boolean checkIdentical(final CompilationTimeStamp timestamp, final Definition definition) {
		check(timestamp);
		definition.check(timestamp);

		if (!Assignment_type.A_VAR.equals(definition.getAssignmentType())) {
			location.reportSemanticError(MessageFormat.format(
					"Local definition `{0}'' is a variable, but the definition inherited from component type `{1}'' is a {2}",
					identifier.getDisplayName(), definition.getMyScope().getFullName(), definition.getAssignmentName()));
			return false;
		}

		Def_Var otherVariable = (Def_Var) definition;
		if (!type.isIdentical(timestamp, otherVariable.type)) {
			final String message = MessageFormat
					.format("Local variable `{0}'' has type `{1}'', but the variable inherited from component type `{2}'' has type `{3}''",
							identifier.getDisplayName(), type.getTypename(), otherVariable.getMyScope().getFullName(),
							otherVariable.type.getTypename());
			type.getLocation().reportSemanticError(message);
			return false;
		}

		if (initial_value != null) {
			if (otherVariable.initial_value != null) {
				if (!initial_value.isUnfoldable(timestamp) && !otherVariable.initial_value.isUnfoldable(timestamp)
						&& !initial_value.checkEquality(timestamp, otherVariable.initial_value)) {
					final String message = MessageFormat
							.format("Local variable `{0}'' and the variable inherited from component type `{1}'' have different values",
									identifier.getDisplayName(), otherVariable.getMyScope().getFullName());
					initial_value.getLocation().reportSemanticWarning(message);
				}
			} else {
				initial_value.getLocation()
						.reportSemanticWarning(
								MessageFormat.format(
										"Local variable `{0}'' has initial value, but the variable inherited from component type `{1}'' does not",
										identifier.getDisplayName(), otherVariable.getMyScope().getFullName()));
			}
		} else if (otherVariable.initial_value != null) {
			location.reportSemanticWarning(MessageFormat
					.format("Local variable `{0}'' does not have initial value, but the variable inherited from component type `{1}'' has",
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
		} else if (subrefs.size() > i + 1 && type != null && identifier.getName().equals(subrefs.get(i).getId().getName())) {
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
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			reparser.moduleToBeReanalysed.addAll(referingHere);
			reparser.moduleToBeReanalysed.add(getMyScope().getModuleScope().getName());

			boolean enveloped = false;
			int result = 1;

			Location temporalIdentifier = identifier.getLocation();
			if (reparser.envelopsDamage(temporalIdentifier) || reparser.isExtending(temporalIdentifier)) {
				reparser.fullAnalysysNeeded = true;
				reparser.extendDamagedRegion(temporalIdentifier);
				IIdentifierReparser r = ParserFactory.createIdentifierReparser(reparser);
				result = r.parseAndSetNameChanged();
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

			if (initial_value != null) {
				if (enveloped) {
					initial_value.updateSyntax(reparser, false);
					reparser.updateLocation(initial_value.getLocation());
				} else if (reparser.envelopsDamage(initial_value.getLocation())) {
					initial_value.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(initial_value.getLocation());
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

		if (initial_value != null) {
			initial_value.updateSyntax(reparser, false);
			reparser.updateLocation(initial_value.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
		if (initial_value != null) {
			initial_value.findReferences(referenceFinder, foundIdentifiers);
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
		if (initial_value != null && !initial_value.accept(v)) {
			return false;
		}
		return true;
	}

	public boolean getWritten() {
		return wasAssigned;
	}
}
