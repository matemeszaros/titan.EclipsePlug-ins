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

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.T3Doc;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ITTCN3ReparseBase;
import org.eclipse.titan.designer.parsers.ttcn3parser.IdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Reparser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * The Def_Const class represents TTCN3 constant definitions.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Def_Const extends Definition {
	private static final String FULLNAMEPART = ".<type>";
	public static final String PORTNOTALLOWED = "Constant can not be defined for port type `{0}''";
	public static final String SIGNATURENOTALLOWED = "Constant can not be defined for signature type `{0}''";

	private static final String KIND = "constant ";

	public static String getKind() {
		return KIND;
	}

	private final Type type;
	protected Value value;

	public Def_Const(final Identifier identifier, final Type type, final Value value) {
		super(identifier);
		this.type = type;
		this.value = value;

		if (type != null) {
			type.setFullNameParent(this);
		}
		if (value != null) {
			value.setFullNameParent(this);
		}
	}

	@Override
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_CONST;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (type == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (type != null) {
			type.setMyScope(scope);
		}
		if (value != null) {
			value.setMyScope(scope);
		}
	}

	@Override
	public Type getType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return type;
	}

	public IValue getValue() {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return value;
	}

	@Override
	public IValue getSetting(final CompilationTimeStamp timestamp) {
		return getValue();
	}

	@Override
	public String getAssignmentName() {
		return "constant";
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
		return "constant.gif";
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
	public void check(final CompilationTimeStamp timestamp) {
		check(timestamp, null);
		}
		
	@Override
	public void check(final CompilationTimeStamp timestamp, IReferenceChain refChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		isUsed = false;
		if (getMyScope() instanceof ComponentTypeBody) {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_CONSTANT, identifier, this);
		} else if (isLocal()) {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_CONSTANT, identifier, this);
		} else {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_CONSTANT, identifier, this);
		}

		NamingConventionHelper.checkNameContents(identifier, getMyScope().getModuleScope().getIdentifier(), getDescription());

		if (type == null) {
			return;
		}

		T3Doc.check(this.getCommentLocation(), KIND, this.type.getTypetype());

		type.check(timestamp);

		if (withAttributesPath != null) {
			withAttributesPath.checkGlobalAttributes(timestamp, true);
			withAttributesPath.checkAttributes(timestamp, type.getTypeRefdLast(timestamp).getTypetype());
		}

		if (value == null) {
			return;
		}

		value.setMyGovernor(type);
		IValue temporalValue = type.checkThisValueRef(timestamp, value);
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

		type.checkThisValue(timestamp, temporalValue, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT, true, false, true,
				hasImplicitOmitAttribute(timestamp), false));

		checkErroneousAttributes(timestamp);

		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		chain.add(this);
		temporalValue.checkRecursions(timestamp, chain);
		chain.release();
	}

	@Override
	public boolean checkIdentical(final CompilationTimeStamp timestamp, final Definition definition) {
		check(timestamp);
		definition.check(timestamp);

		if (!Assignment_type.A_CONST.equals(definition.getAssignmentType())) {
			location.reportSemanticError(MessageFormat.format(
					"Local definition `{0}'' is a constant, but the definition inherited from component type `{1}'' is a {2}",
					identifier.getDisplayName(), definition.getMyScope().getFullName(), definition.getAssignmentName()));
			return false;
		}

		Def_Const otherConstant = (Def_Const) definition;
		if (!type.isIdentical(timestamp, otherConstant.type)) {
			final String message = MessageFormat
					.format("Local constant `{0}'' has type `{1}'', but the constant inherited from component type `{2}'' has type `{3}''",
							identifier.getDisplayName(), type.getTypename(), otherConstant.getMyScope().getFullName(),
							otherConstant.type.getTypename());
			type.getLocation().reportSemanticError(message);
			return false;
		}

		if (!value.checkEquality(timestamp, otherConstant.value)) {
			final String message = MessageFormat.format(
					"Local constant `{0}'' and the constant inherited from component type `{1}'' have different values",
					identifier.getDisplayName(), otherConstant.getMyScope().getFullName());
			value.getLocation().reportSemanticError(message);
			return false;
		}

		return true;
	}

	@Override
	public void postCheck() {
		super.postCheck();
		postCheckPrivateness();
	}

	@Override
	public String getProposalKind() {
		StringBuilder builder = new StringBuilder(KIND);
		if (type != null) {
			type.getProposalDescription(builder);
		}
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
		if (subrefs.size() >= i + 1 && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			if (subrefs.size() > i + 1 && type != null) {
				type.addDeclaration(declarationCollector, i + 1);
			} else if (subrefs.size() == i + 1 && Subreference_type.fieldSubReference.equals(subrefs.get(i).getReferenceType())) {
				declarationCollector.addDeclaration(this);
			}
		}
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		List<Integer> result = super.getPossibleExtensionStarterTokens();
		
		result.add(Ttcn3Lexer.COMMA);
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

			if (value != null) {
				if (enveloped) {
					value.updateSyntax(reparser, false);
					reparser.updateLocation(value.getLocation());
				} else if (reparser.envelopsDamage(value.getLocation())) {
					reparser.extendDamagedRegion(value.getLocation().getOffset(), value.getLocation().getEndOffset());
					int result = reparse( reparser );
					if (result == 0) {
						enveloped = true;
						value.setFullNameParent(this);
						value.setMyScope(getMyScope());
					} else {
						throw new ReParseException();
					}
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

		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	private int reparse(final TTCN3ReparseUpdater aReparser) {
		return aReparser.parse(new ITTCN3ReparseBase() {
			@Override
			public void reparse(final Ttcn3Reparser parser) {
				Value newValue = parser.pr_Expression().value;
				parser.pr_EndOfFile();
				if ( parser.isErrorListEmpty() ) {
					if (newValue != null) {
						value = newValue;
					}
				}
			}
		});
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
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
		if (value != null && !value.accept(v)) {
			return false;
		}
		return true;
	}
}
