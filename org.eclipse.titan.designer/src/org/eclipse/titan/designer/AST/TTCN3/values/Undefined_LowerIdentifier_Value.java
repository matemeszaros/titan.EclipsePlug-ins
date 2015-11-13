/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.values.Named_Integer_Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a value who's kind could not be identified yet. The semantic
 * checking must convert it to some other kind.
 * 
 * @author Kristof Szabados
 * */
public final class Undefined_LowerIdentifier_Value extends Value {

	private final Identifier identifier;

	private Value realValue;
	private Reference asReference;

	public Undefined_LowerIdentifier_Value(final Identifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public Value_type getValuetype() {
		return Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE;
	}

	@Override
	public String createStringRepresentation() {
		return identifier.getName();
	}

	@Override
	// Location is optimized not to store an object at it is not needed
	public Location getLocation() {
		return new Location(identifier.getLocation());
	}

	@Override
	public void setLocation(final Location location) {
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (lastTimeChecked == null || lastTimeChecked.isLess(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		if (realValue == null || realValue.getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		return realValue.getExpressionReturntype(timestamp, expectedValue);
	}

	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (lastTimeChecked == null || lastTimeChecked.isLess(timestamp)) {
			return null;
		}

		if (realValue == null || realValue.getIsErroneous(timestamp)) {
			return null;
		}

		return realValue.getExpressionGovernor(timestamp, expectedValue);
	}

	@Override
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference,
			final int actualSubReference, final IReferenceChain refChain) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return this;
		}

		IValue result = getValueRefdLast(timestamp, refChain);
		if (result != null && result != this) {
			result = result.getReferencedSubValue(timestamp, reference, actualSubReference, refChain);
			if (result != null && result.getIsErroneous(timestamp)) {
				setIsErroneous(true);
			}
			return result;
		}

		IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp)) {
			return null;
		}

		ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDVALUESUBREFERENCE, type.getTypename()));
			return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(
					FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(), type.getTypename()));
			return null;
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(ParameterisedSubReference.INVALIDVALUESUBREFERENCE);
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (lastTimeChecked == null || lastTimeChecked.isLess(timestamp)) {
			return true;
		}

		if (realValue == null || realValue.getIsErroneous(timestamp)) {
			return true;
		}

		return realValue.isUnfoldable(timestamp, expectedValue, referenceChain);
	}

	@Override
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		if (realValue == null || realValue.getIsErroneous(timestamp)) {
			return false;
		}

		return realValue.checkEquality(timestamp, other);
	}

	@Override
	public IValue getValueRefdLast(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (myGovernor != null && myGovernor.getIsErroneous(timestamp)) {
			setIsErroneous(true);
			return this;
		}

		setLoweridToReference(timestamp);

		return realValue.getValueRefdLast(timestamp, expectedValue, referenceChain);
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the reference form of the lower identifier value.
	 * <p>
	 * Almost the same as steel_ttcn_ref_base.
	 *
	 * @return the reference created from the identifier.
	 * */
	public Reference getAsReference() {
		if (asReference != null) {
			return asReference;
		}
		asReference = new Reference(null);
		asReference.addSubReference(new FieldSubReference(identifier));
		asReference.setLocation(getLocation());
		asReference.setFullNameParent(this);
		asReference.setMyScope(myScope);
		return asReference;
	}

	@Override
	public Value setValuetype(final CompilationTimeStamp timestamp, final Value_type newType) {
		lastTimeChecked = timestamp;

		switch (newType) {
		case ENUMERATED_VALUE:
			realValue = new Enumerated_Value(this);
			realValue.copyGeneralProperties(this);
			break;
		case REFERENCED_VALUE:
			realValue = new Referenced_Value(this);
			// FIXME: this seems redundant; the constructor already called it -no, e.g. location is set here
			realValue.copyGeneralProperties(this);
			break;
		case NAMED_INTEGER_VALUE:
			realValue = new Named_Integer_Value(this);
			realValue.copyGeneralProperties(this);
			break;
		default:
			realValue = super.setValuetype(timestamp, newType);
			break;
		}

		return realValue;
	}

	@Override
	public IValue setLoweridToReference(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return realValue;
		}

		lastTimeChecked = timestamp;
		realValue = setValuetype(timestamp, Value_type.REFERENCED_VALUE);

		return realValue;
	}

	@Override
	public boolean evaluateIsvalue(final boolean fromSequence) {
		if (realValue == null) {
			return false;
		}

		return realValue.evaluateIsvalue(fromSequence);
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(identifier.getLocation());
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (realValue != null) {
			realValue.findReferences(referenceFinder, foundIdentifiers);
		} else if (asReference != null) {
			asReference.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (realValue != null) {
			if (!realValue.accept(v)) {
				return false;
			}
		} else if (asReference != null) {
			if (!asReference.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
