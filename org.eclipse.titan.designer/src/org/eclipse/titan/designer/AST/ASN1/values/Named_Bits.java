/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.values;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Named_Bits extends Value {

	private List<Identifier> identifierList = new ArrayList<Identifier>();
	private Map<String, Identifier> identifierMap = new HashMap<String, Identifier>();

	private Bitstring_Value realValue;

	@Override
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("{");
		boolean first = true;
		for (Entry<String, Identifier> entry : identifierMap.entrySet()) {
			if (!first) {
				builder.append(' ');
			}
			builder.append(entry.getValue().getDisplayName());
			first = false;
		}
		builder.append('}');

		return builder.toString();
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (null == lastTimeChecked || lastTimeChecked.isLess(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		if (null == realValue || realValue.getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		return realValue.getExpressionReturntype(timestamp, expectedValue);
	}

	public int getNofIds() {
		return identifierMap.size();
	}

	public Identifier getIdByIndex(final int i) {
		return identifierList.get(i);
	}

	public void addId(final Identifier id) {
		if (identifierMap.containsKey(id.getName())) {
			id.getLocation().reportSyntacticError(MessageFormat.format("Duplicate named bit `{0}''", id.getDisplayName()));
		} else {
			identifierMap.put(id.getName(), id);
			identifierList.add(id);
		}
	}

	public boolean hasId(final Identifier id) {
		return identifierMap.containsKey(id.getName());
	}

	public void setRealValue(final Bitstring_Value realValue) {
		this.realValue = realValue;
	}

	@Override
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final IReferenceChain refChain) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return this;
		}

		final IValue result = getValueRefdLast(timestamp, refChain);
		if (null != result && result != this) {
			return result.getReferencedSubValue(timestamp, reference, actualSubReference, refChain);
		}

		final IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp)) {
			return null;
		}

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(ArraySubReference.INVALIDVALUESUBREFERENCE, type.getTypename()));
			return null;
		case fieldSubReference:
			location.reportSemanticError(MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference)
					.getId().getDisplayName(), type.getTypename()));
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
	public Value_type getValuetype() {
		return Value_type.NAMED_BITS;
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (null == realValue || realValue.getIsErroneous(timestamp)) {
			return true;
		}

		return realValue.isUnfoldable(timestamp, expectedValue, referenceChain);
	}

	@Override
	public IValue getValueRefdLast(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (null == realValue || realValue.getIsErroneous(timestamp)) {
			return this;
		}

		return realValue.getValueRefdLast(timestamp, expectedValue, referenceChain);
	}

	@Override
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		if (null == realValue) {
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
	protected boolean memberAccept(ASTVisitor v) {
		if (identifierList != null) {
			for (Identifier id : identifierList) {
				if (!id.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
