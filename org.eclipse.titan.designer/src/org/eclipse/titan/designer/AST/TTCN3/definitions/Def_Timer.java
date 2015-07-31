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
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserFactory;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * The Def_Timer class represents TTCN3 timer definitions.
 * <p>
 * Timers in TTCN3 does not have a type.
 * 
 * @author Kristof Szabados
 * */
public final class Def_Timer extends Definition {
	private static final String NEGATIVDURATIONERROR = "A non-negative float value was expected as timer duration instead of {0}";
	private static final String INFINITYDURATIONERROR = "{0} can not be used as the default timer duration";
	private static final String OPERANDERROR = "The default timer duration should be a float value";

	private static final String FULLNAMEPART1 = ".<dimensions>";
	private static final String FULLNAMEPART2 = ".<default_duration>";
	private static final String KIND = "timer";

	private final ArrayDimensions dimensions;
	private final Value default_duration;

	public Def_Timer(final Identifier identifier, final ArrayDimensions dimensions, final Value default_duration) {
		super(identifier);
		this.dimensions = dimensions;
		this.default_duration = default_duration;

		if (dimensions != null) {
			dimensions.setFullNameParent(this);
		}
		if (default_duration != null) {
			default_duration.setFullNameParent(this);
		}
	}

	@Override
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_TIMER;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (dimensions != null) {
			dimensions.setMyScope(scope);
		}
		if (default_duration != null) {
			default_duration.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (dimensions == child) {
			return builder.append(FULLNAMEPART1);
		} else if (default_duration == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public String getAssignmentName() {
		return "timer";
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
		return "timer.gif";
	}

	@Override
	public String getProposalKind() {
		return KIND;
	}

	public ArrayDimensions getDimensions() {
		return dimensions;
	}

	/**
	 * Returns false if it is sure that the timer referred by array indices
	 * reference does not have a default duration. Otherwise it returns
	 * true.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle
	 * @param reference
	 *                might be NULL when examining a single timer.
	 * 
	 * @return true if the timer has a default duration, false otherwise.
	 * */
	public boolean hasDefaultDuration(final CompilationTimeStamp timestamp, final Reference reference) {
		if (default_duration == null) {
			return false;
		} else if (dimensions == null || reference == null) {
			return true;
		}

		IValue v = default_duration;
		List<ISubReference> subreferences = reference.getSubreferences();
		int nofDimensions = dimensions.size();
		int nofReferences = subreferences.size() - 1;
		int upperLimit = (nofDimensions < nofReferences) ? nofDimensions : nofReferences;
		for (int i = 0; i < upperLimit; i++) {
			v = v.getValueRefdLast(timestamp, null);
			if (Value_type.SEQUENCEOF_VALUE.equals(v.getValuetype())) {
				ISubReference ref = subreferences.get(i + 1);
				if (!Subreference_type.arraySubReference.equals(ref.getReferenceType())) {
					return true;
				}

				IValue index = ((ArraySubReference) ref).getValue();
				if (!Value_type.INTEGER_VALUE.equals(index.getValuetype())) {
					return true;
				}

				long realIndex = ((Integer_Value) index).getValue() - dimensions.get(i).getOffset();
				if (realIndex >= 0 && realIndex < ((SequenceOf_Value) v).getNofComponents()) {
					v = ((SequenceOf_Value) v).getValueByIndex((int) realIndex);
				}
			}
		}
		return !Value_type.NOTUSED_VALUE.equals(v.getValuetype());
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		isUsed = false;

		if (getMyScope() instanceof ComponentTypeBody) {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_TIMER, identifier, this);
		} else if (isLocal()) {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_TIMER, identifier, this);
		} else {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_TIMER, identifier, this);
		}
		NamingConventionHelper.checkNameContents(identifier, getMyScope().getModuleScope().getIdentifier(), getDescription());

		if (dimensions != null) {
			dimensions.check(timestamp);
		}

		if (default_duration != null) {
			if (dimensions == null) {
				default_duration.setLoweridToReference(timestamp);
				Type_type tempType = default_duration.getExpressionReturntype(timestamp,
						isLocal() ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE : Expected_Value_type.EXPECTED_STATIC_VALUE);

				switch (tempType) {
				case TYPE_REAL:
					IValue last = default_duration.getValueRefdLast(timestamp, null);
					if (!last.isUnfoldable(timestamp)) {
						Real_Value real = (Real_Value) last;
						double value = real.getValue();
						if (value < 0.0f) {
							default_duration.getLocation().reportSemanticError(
									MessageFormat.format(NEGATIVDURATIONERROR, value));
						} else if (real.isPositiveInfinity()) {
							final String message = MessageFormat.format(INFINITYDURATIONERROR,
									real.createStringRepresentation());
							default_duration.getLocation()
									.reportSemanticError(message
											);
						}
					}
					return;
				case TYPE_UNDEFINED:
					return;
				default:
					location.reportSemanticError(OPERANDERROR);
				}
			} else {
				checkArrayDuration(default_duration, 0);
			}
		}

		if (withAttributesPath != null) {
			withAttributesPath.checkGlobalAttributes(timestamp, false);
			withAttributesPath.checkAttributes(timestamp);
		}

		lastTimeChecked = timestamp;
	}

	private void checkArrayDuration(final IValue duration, final int start_dimension) {
		// FIXME implement support for dimension handling
	}

	@Override
	public boolean checkIdentical(final CompilationTimeStamp timestamp, final Definition definition) {
		check(timestamp);
		definition.check(timestamp);

		if (!Assignment_type.A_TIMER.equals(definition.getAssignmentType())) {
			location.reportSemanticError(MessageFormat.format(
					"Local definition `{0}'' is a timer, but the definition inherited from component type `{1}'' is a {2}",
					identifier.getDisplayName(), definition.getMyScope().getFullName(), definition.getAssignmentName()));
			return false;
		}

		Def_Timer otherTimer = (Def_Timer) definition;
		if (dimensions != null) {
			if (otherTimer.dimensions != null) {
				if (!dimensions.isIdenticial(timestamp, otherTimer.dimensions)) {
					location.reportSemanticError(MessageFormat
							.format("Local timer `{0}'' and the timer inherited from component type `{1}'' have different array dimensions",
									identifier.getDisplayName(), otherTimer.getMyScope().getFullName()));
					return false;
				}
			} else {
				location.reportSemanticError(MessageFormat
						.format("Local definition `{0}'' is a timer array, but the definition inherited from component type `{1}'' is a single timer",
								identifier.getDisplayName(), otherTimer.getMyScope().getFullName()));
				return false;
			}
		} else if (otherTimer.dimensions != null) {
			location.reportSemanticError(MessageFormat
					.format("Local definition `{0}'' is a single timer, but the definition inherited from component type `{1}'' is a timer array",
							identifier.getDisplayName(), otherTimer.getMyScope().getFullName()));
			return false;
		}

		if (default_duration != null) {
			if (otherTimer.default_duration != null) {
				if (!default_duration.isUnfoldable(timestamp) && !otherTimer.default_duration.isUnfoldable(timestamp)
						&& !default_duration.checkEquality(timestamp, otherTimer.default_duration)) {
					final String message = MessageFormat
							.format("Local timer `{0}'' and the timer inherited from component type `{1}'' have different default durations",
									identifier.getDisplayName(), otherTimer.getMyScope().getFullName());
					default_duration.getLocation().reportSemanticWarning(message);
				}
			} else {
				final String message = MessageFormat
						.format("Local timer `{0}'' has default duration, but the timer inherited from component type `{1}'' does not",
								identifier.getDisplayName(), otherTimer.getMyScope().getFullName());
				default_duration.getLocation().reportSemanticWarning(message);
			}
		} else if (otherTimer.default_duration != null) {
			location.reportSemanticWarning(MessageFormat.format(
					"Local timer `{0}'' does not have default duration, but the timer inherited from component type `{1}'' has",
					identifier.getDisplayName(), otherTimer.getMyScope().getFullName()));
		}

		return true;
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= i || !Subreference_type.fieldSubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		if (subrefs.size() == i + 1 && identifier.getName().toLowerCase().startsWith(subrefs.get(i).getId().getName().toLowerCase())) {
			super.addProposal(propCollector, i);
		}
		if (identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match
			// do as if timers had a type
			Timer.addProposal(propCollector, i + 1);
		}
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() > i && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			if (subrefs.size() == i + 1 && Subreference_type.fieldSubReference.equals(subrefs.get(i).getReferenceType())) {
				declarationCollector.addDeclaration(this);
			}
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			reparser.moduleToBeReanalysed.addAll(referingHere);
			reparser.moduleToBeReanalysed.add(getMyScope().getModuleScope().getName());

			int result = 1;
			Location tempIdentifier = identifier.getLocation();
			if (reparser.envelopsDamage(tempIdentifier) || reparser.isExtending(tempIdentifier)) {
				reparser.fullAnalysysNeeded = true;
				reparser.extendDamagedRegion(tempIdentifier);
				IIdentifierReparser r = ParserFactory.createIdentifierReparser(reparser);
				result = r.parseAndSetNameChanged();
				identifier = r.getIdentifier();
				if (result != 0) {
					throw new ReParseException(result);
				}

				if (dimensions != null) {
					dimensions.updateSyntax(reparser, false);
				}

				if (default_duration != null) {
					default_duration.updateSyntax(reparser, false);
					reparser.updateLocation(default_duration.getLocation());
				}

				if (withAttributesPath != null) {
					withAttributesPath.updateSyntax(reparser, false);
					reparser.updateLocation(withAttributesPath.getLocation());
				}

				return;
			}

			throw new ReParseException();
		}

		reparser.updateLocation(identifier.getLocation());
		if (dimensions != null) {
			dimensions.updateSyntax(reparser, false);
		}

		if (default_duration != null) {
			default_duration.updateSyntax(reparser, false);
			reparser.updateLocation(default_duration.getLocation());
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (dimensions != null) {
			dimensions.findReferences(referenceFinder, foundIdentifiers);
		}
		if (default_duration != null) {
			default_duration.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (dimensions != null && !dimensions.accept(v)) {
			return false;
		}
		if (default_duration != null && !default_duration.accept(v)) {
			return false;
		}
		return true;
	}
}
