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
import org.eclipse.titan.designer.AST.IReferencingElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an ASN.1 enumerated value.
 * <p>
 * This value can not be parsed, but can only be converted from an undefined identifier
 * 
 * @author Kristof Szabados
 * */
public final class Enumerated_Value extends Value implements IReferencingElement {

	private final Identifier value;

	public Enumerated_Value(final Identifier value) {
		this.value = value;
	}

	protected Enumerated_Value(final Undefined_LowerIdentifier_Value original) {
		copyGeneralProperties(original);
		this.value = original.getIdentifier();
	}

	@Override
	public Value_type getValuetype() {
		return Value_type.ENUMERATED_VALUE;
	}

	@Override
	public String createStringRepresentation() {
		return value.getName();
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_TTCN3_ENUMERATED;
	}

	@Override
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final IReferenceChain refChain) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return this;
		}

		IType type = myGovernor.getTypeRefdLast(timestamp);
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
		return false;
	}

	public Identifier getValue() {
		return value;
	}

	@Override
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		return Value_type.ENUMERATED_VALUE.equals(last.getValuetype()) && value.equals(((Enumerated_Value) last).getValue());
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(value.getLocation());
	}

	@Override
	public boolean evaluateIsvalue(final boolean fromSequence) {
		return true;
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value == null || myGovernor == null) {
			return;
		}

		if (referenceFinder.assignment.getAssignmentType() == Assignment_type.A_TYPE
				&& referenceFinder.type == myGovernor && value.equals(referenceFinder.fieldId)) {
			foundIdentifiers.add(new Hit(value));
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (value!=null && !value.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	public Declaration getDeclaration() {

		IType type = getMyGovernor();
		if (type == null) {
			return null;
		}
		type = type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());

		if (type instanceof ITypeWithComponents) {
			Identifier resultId = ((ITypeWithComponents) type).getComponentIdentifierByName(value);
			return Declaration.createInstance(type.getDefiningAssignment(), resultId);
		}

		return null;
	}
}
