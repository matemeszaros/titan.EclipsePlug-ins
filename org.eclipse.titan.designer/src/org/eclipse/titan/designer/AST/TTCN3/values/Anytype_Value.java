/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Anytype_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Anytype value.
 * <p>
 * This value kind can not be parsed,
 * it is converted from a sequence when it is checked against an anytype type.
 * 
 * @author Kristof Szabados
 * */
public final class Anytype_Value extends Value {
	private static final String ONEFIELDEXPECTED1 = "Anytype value must have one active field";
	private static final String ONEFIELDEXPECTED2 = "Only one field was expected in anytype value istead of {0}";
	private static final String NONEXISTENTFIELD = "Reference to non-existent union field `{0}'' in type `{1}''";
	private static final String INACTIVEFIELD = "Reference to inactive field `{0}'' in a value of union type `{1}''. The active field is `{2}''";

	private final Identifier name;
	private final IValue value;

	public Anytype_Value(final CompilationTimeStamp timestamp, final Sequence_Value value) {
		copyGeneralProperties(value);
		int valueSize = value.getNofComponents();
		if (valueSize < 1) {
			this.name = null;
			this.value = null;
			value.getLocation().reportSemanticError(ONEFIELDEXPECTED1);
			setIsErroneous(true);
			lastTimeChecked = timestamp;
		} else if (valueSize > 1) {
			this.name = null;
			this.value = null;
			value.getLocation().reportSemanticError(MessageFormat.format(ONEFIELDEXPECTED2, valueSize));
			setIsErroneous(true);
			lastTimeChecked = timestamp;
		} else {
			NamedValue namedValue = value.getSeqValueByIndex(0);
			this.name = namedValue.getName();
			this.value = namedValue.getValue();
		}
	}

	@Override
	public Value_type getValuetype() {
		return Value_type.ANYTYPE_VALUE;
	}

	public Identifier getName() {
		return name;
	}

	public IValue getValue() {
		return value;
	}

	@Override
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append('{').append(name.getDisplayName()).append(" := ");
		builder.append(value.createStringRepresentation()).append('}');

		return builder.toString();
	}

	public boolean hasComponentWithName(final Identifier name) {
		return this.name.getDisplayName().equals(name.getDisplayName());
	}

	public boolean fieldIsChosen(final CompilationTimeStamp timestamp, final Identifier name) {
		if (value == null) {
			return false;
		}

		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = value.getValueRefdLast(timestamp, chain);
		chain.release();

		if (Value_type.ANYTYPE_VALUE.equals(last.getValuetype())) {
			return ((Choice_Value) last).getName().equals(name);
		}

		return false;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_UNDEFINED;
	}

	@Override
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final IReferenceChain refChain) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return this;
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
			Identifier fieldId = ((FieldSubReference) subreference).getId();
			switch (type.getTypetype()) {
			case TYPE_ANY:
				if (!((Anytype_Type) type).hasComponentWithName(fieldId.getDisplayName())) {
					subreference.getLocation().reportSemanticError(MessageFormat.format(NONEXISTENTFIELD, fieldId.getDisplayName(), type.getTypename()));
					return null;
				}
				break;
			default:
				return null;
			}

			if (name.getDisplayName().equals(fieldId.getDisplayName())) {
				return value.getReferencedSubValue(timestamp, reference, actualSubReference + 1, refChain);
			}

			subreference.getLocation().reportSemanticError(MessageFormat.format(
					INACTIVEFIELD, fieldId.getDisplayName(), type.getTypename(), name.getDisplayName()));
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
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue, final IReferenceChain referenceChain) {
		if (value == null) {
			return true;
		}

		return value.isUnfoldable(timestamp, expectedValue, referenceChain);
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (value != null) {
			value.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (value == child) {
			builder.append('.').append(name.getDisplayName());
		}

		return builder;
	}

	@Override
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (!Value_type.ANYTYPE_VALUE.equals(last.getValuetype())) {
			return false;
		}

		Anytype_Value otherAny = (Anytype_Value) last;
		if (!name.equals(otherAny.name)
				|| !value.checkEquality(timestamp, otherAny.value)) {
			return false;
		}

		return true;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		// all members are converted, so their processing is done on their original location
		if (isDamaged) {
			throw new ReParseException();
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value == null) {
			return;
		}

		value.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (name!=null && !name.accept(v)) {
			return false;
		}
		if (value!=null && !value.accept(v)) {
			return false;
		}
		return true;
	}
}
