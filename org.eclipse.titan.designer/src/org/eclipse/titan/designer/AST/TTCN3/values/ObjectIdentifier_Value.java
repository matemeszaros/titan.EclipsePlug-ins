/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.ObjectIdentifierComponent.oidState_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class ObjectIdentifier_Value extends Value {
	private List<ObjectIdentifierComponent> objectIdComponents = new ArrayList<ObjectIdentifierComponent>();

	@Override
	public Value_type getValuetype() {
		return Value_type.OBJECTID_VALUE;
	}

	public void addObjectIdComponent(final ObjectIdentifierComponent component) {
		if (component == null) {
			return;
		}

		objectIdComponents.add(component);
		component.setFullNameParent(this);
		component.setMyScope(myScope);
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		ObjectIdentifierComponent component;
		for (int i = 0, size = objectIdComponents.size(); i < size; i++) {
			component = objectIdComponents.get(i);
			if (component == child) {
						return builder.append(INamedNode.DOT).append(i + 1);
			}
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_OBJECTID;
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		if (!isAsn()) {
			builder.append("objid ");
		}

		builder.append('{');
		for (int i = 0, size = objectIdComponents.size(); i < size; i++) {
			if (i > 0) {
				builder.append(' ');
			}
			builder.append(objectIdComponents.get(i).createStringRepresentation());
		}
		builder.append('}');
		return builder.toString();
	}

	public int getNofComponents() {
		return objectIdComponents.size();
	}

	/**
	 * Appends its own object identifier component parts to the provided list.
	 *
	 * @param components the list to be extended
	 * */
	public void getOidComponents(final List<Integer> components) {
		for (int i = 0, size = objectIdComponents.size(); i < size; i++) {
			objectIdComponents.get(i).getOidComponents(components);
		}
	}

	@Override
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference,
			final int actualSubReference, final IReferenceChain refChain) {
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
		return true;
	}

	public void checkOID(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (objectIdComponents.isEmpty()) {
			return;
		}

		if (!referenceChain.add(this)) {
			return;
		}

		oidState_type state = oidState_type.START;
		for (int i = 0, size = objectIdComponents.size(); i < size; i++) {
			referenceChain.markState();
			state = objectIdComponents.get(i).checkOID(timestamp, referenceChain, this, state);
			referenceChain.previousState();
		}

		if (!oidState_type.LATER.equals(state) && !oidState_type.ITU_REC.equals(state)) {
			// X.680 (07/2002) 31.10
			getLocation().reportSemanticError("An OBJECT IDENTIFIER value must have at least two components");
		}
	}

	@Override
	public boolean evaluateIsvalue(final boolean fromSequence) {
		return true;
	}

	@Override
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (!Value_type.OBJECTID_VALUE.equals(last.getValuetype())) {
			return false;
		}

		ObjectIdentifier_Value otherObjid = (ObjectIdentifier_Value) last;
		if (objectIdComponents.size() != otherObjid.objectIdComponents.size()) {
			return false;
		}

		for (int i = 0, size = objectIdComponents.size(); i < size; i++) {
			if (objectIdComponents.get(i) != otherObjid.objectIdComponents.get(i)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		for (int i = 0, size = objectIdComponents.size(); i < size; i++) {
			ObjectIdentifierComponent component = objectIdComponents.get(i);

			component.updateSyntax(reparser, false);
			reparser.updateLocation(component.getLocation());
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (objectIdComponents!=null) {
			for (ObjectIdentifierComponent c : objectIdComponents) {
				if (!c.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
