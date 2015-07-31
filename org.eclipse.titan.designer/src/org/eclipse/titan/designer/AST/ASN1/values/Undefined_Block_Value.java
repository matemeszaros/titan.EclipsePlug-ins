/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.values;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValues;
import org.eclipse.titan.designer.AST.TTCN3.values.ObjectIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Sequence_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Set_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Values;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public abstract class Undefined_Block_Value extends Value {

	private Value realValue;

	public Undefined_Block_Value() {
	}

	@Override
	public Value_type getValuetype() {
		return Value_type.UNDEFINED_BLOCK;
	}

	@Override
	public String createStringRepresentation() {
		return "<unsupported valuetype>";
	}

	@Override
	public void setLocation(final Location location) {
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expected_value) {
		if (null == lastTimeChecked || lastTimeChecked.isLess(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		if (null == realValue || realValue.getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		return realValue.getExpressionReturntype(timestamp, expected_value);
	}

	@Override
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final IReferenceChain refChain) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return this;
		}

		IValue result = getValueRefdLast(timestamp, refChain);
		if (null != result && result != this) {
			return result.getReferencedSubValue(timestamp, reference, actualSubReference, refChain);
		}

		IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp)) {
			return null;
		}

		ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(ArraySubReference.INVALIDVALUESUBREFERENCE, type.getTypename()));
			return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId()
							.getDisplayName(), type.getTypename()));
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
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expected_value,
			final IReferenceChain referenceChain) {
		if (null == lastTimeChecked || lastTimeChecked.isLess(timestamp)) {
			return true;
		}

		if (null == realValue || realValue.getIsErroneous(timestamp)) {
			return true;
		}

		return realValue.isUnfoldable(timestamp, expected_value, referenceChain);
	}

	@Override
	public IValue getValueRefdLast(final CompilationTimeStamp timestamp, final Expected_Value_type expected_value,
			final IReferenceChain referenceChain) {
		if (null == lastTimeChecked || lastTimeChecked.isLess(timestamp)) {
			return this;
		}

		if (null == realValue || realValue.getIsErroneous(timestamp)) {
			return this;
		}

		return realValue.getValueRefdLast(timestamp, expected_value, referenceChain);
	}

	// FIXME can be converted to: charsyms
	@Override
	public Value setValuetype(final CompilationTimeStamp timestamp, final Value_type new_type) {
		lastTimeChecked = timestamp;

		switch (new_type) {
		case NAMED_BITS: {
			Named_Bits namedBits = parseBlockNamedBits();
			if (null == namedBits) {
				namedBits = new Named_Bits();
			}

			namedBits.copyGeneralProperties(this);
			realValue = namedBits;
			break;
		}
		case SEQUENCEOF_VALUE: {
			SequenceOf_Value value = parseBlockSeqofValue();
			if (null == value) {
				Values values = new Values(false);
				value = new SequenceOf_Value(values);
			}

			value.setFullNameParent(getNameParent());
			value.copyGeneralProperties(this);
			realValue = value;
			break;
		}
		case SETOF_VALUE: {
			SetOf_Value value = parseBlockSetofValue();
			if (null == value) {
				Values values = new Values(false);
				value = new SetOf_Value(values);
			}

			value.setFullNameParent(getNameParent());
			value.copyGeneralProperties(this);
			realValue = value;
			break;
		}
		case SEQUENCE_VALUE: {
			Sequence_Value value = parseBlockSequenceValue();
			if (null == value) {
				NamedValues values = new NamedValues();
				value = new Sequence_Value(values);
			}

			value.setFullNameParent(getNameParent());
			value.copyGeneralProperties(this);
			realValue = value;
			break;
		}
		case SET_VALUE: {
			Set_Value value = parseBlockSetValue();
			if (null == value) {
				NamedValues values = new NamedValues();
				value = new Set_Value(values);
			}

			value.setFullNameParent(getNameParent());
			value.copyGeneralProperties(this);
			realValue = value;
			break;
		}
		case OBJECTID_VALUE: {
			ObjectIdentifier_Value value = parseBlockObjectIdentifierValue();
			if (null == value) {
				value = new ObjectIdentifier_Value();
			}

			value.setFullNameParent(getNameParent());
			value.copyGeneralProperties(this);
			realValue = value;
			break;
		}
		case RELATIVEOBJECTIDENTIFIER_VALUE: {
			RelativeObjectIdentifier_Value value = parseBlockRelativeObjectIdentifierValue();
			if (null == value) {
				value = new RelativeObjectIdentifier_Value();
			}

			value.setFullNameParent(getNameParent());
			value.copyGeneralProperties(this);
			realValue = value;
			break;
		}
		default:
			realValue = super.setValuetype(timestamp, new_type);
		}

		return realValue;
	}

	abstract protected Named_Bits parseBlockNamedBits();

	abstract protected SequenceOf_Value parseBlockSeqofValue();
	
	abstract protected SetOf_Value parseBlockSetofValue();

	abstract protected Sequence_Value parseBlockSequenceValue();

	abstract protected Set_Value parseBlockSetValue();
	
	abstract protected ObjectIdentifier_Value parseBlockObjectIdentifierValue();

	abstract protected RelativeObjectIdentifier_Value parseBlockRelativeObjectIdentifierValue();
	
	@Override
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		if (null == realValue || realValue.getIsErroneous(timestamp)) {
			return false;
		}

		return realValue.checkEquality(timestamp, other);
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (null == realValue || realValue.getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
			return;
		}

		realValue.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (realValue != null && !realValue.getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
			if (!realValue.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
