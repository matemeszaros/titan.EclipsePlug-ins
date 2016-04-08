/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

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
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * a value assignment.
 * 
 * @author Kristof Szabados
 * */
public final class Value_Assignment extends ASN1Assignment {
	private static final String UNKNOWNASSIGNMENT = "unknown value assignment";

	/** left. */
	private final IASN1Type type;
	/** right. */
	private final Value value;

	public Value_Assignment(final Identifier id, final Ass_pard assPard, final IASN1Type type, final Value value) {
		super(id, assPard);
		this.type = type;
		this.value = value;

		if (null != type) {
			type.setFullNameParent(this);
		}
		if (null != value) {
			value.setFullNameParent(this);
		}
	}

	@Override
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_CONST;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (type == child) {
			return builder.append(".<type>");
		} else if (value == child) {
			return builder;
		}

		return builder;
	}

	@Override
	protected ASN1Assignment internalNewInstance(final Identifier identifier) {
		// FIXME ASN.1 values can be cloned
		return new Value_Assignment(identifier, null, type.newInstance(), value);
	}

	@Override
	public void setRightScope(final Scope rightScope) {
		if (null != value) {
			value.setMyScope(rightScope);
		}
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != type) {
			type.setMyScope(scope);
		}
		if (null != value) {
			value.setMyScope(scope);
		}
	}

	@Override
	public IType getType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return type;
	}

	public IValue getValue() {
		if (null != assPard) {
			location.reportSemanticError(MessageFormat.format("`{0}'' is a parameterized value assignment", getFullName()));
			return null;
		}

		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return value;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (null != assPard) {
			assPard.check(timestamp);
			return;
		}

		checkTTCNIdentifier();
		if (null == type) {
			return;
		}

		type.check(timestamp);

		if (null == value) {
			return;
		}

		value.setMyGovernor(type);
		final IValue tempValue = type.checkThisValueRef(timestamp, value);
		type.checkThisValue(timestamp, tempValue, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT, false, false, true, true,
				false));

		final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		tempValue.checkRecursions(timestamp, chain);
		chain.release();
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() >= i + 1 && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			if (subrefs.size() > i + 1 && null != type) {
				type.addDeclaration(declarationCollector, i + 1);
			} else if (subrefs.size() == i + 1 && Subreference_type.fieldSubReference.equals(subrefs.get(i).getReferenceType())) {
				declarationCollector.addDeclaration(identifier.getDisplayName(), identifier.getLocation(), this);
			}
		}
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		final List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= i) {
			return;
		}

		if (subrefs.size() == i + 1 && identifier.getName().toLowerCase().startsWith(subrefs.get(i).getId().getName().toLowerCase())) {
			final String proposalKind = UNKNOWNASSIGNMENT;
			propCollector.addProposal(identifier, " - " + proposalKind, ImageCache.getImage(getOutlineIcon()), proposalKind);
		} else if (subrefs.size() > i + 1 && null != type && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match
			type.addProposal(propCollector, i + 1);
		}
	}

	@Override
	public String getAssignmentName() {
		return "value";
	}

	@Override
	public String getOutlineIcon() {
		return "constant.gif";
	}

	// TODO: remove when location is fixed
	@Override
	public Location getLikelyLocation() {
		if (value != null) {
			return Location.interval(super.getLikelyLocation(), value.getLocation());
		}

		return super.getLikelyLocation();
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
