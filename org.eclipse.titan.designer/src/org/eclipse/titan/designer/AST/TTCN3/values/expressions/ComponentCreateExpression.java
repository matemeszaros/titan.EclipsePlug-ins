/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class ComponentCreateExpression extends Expression_Value {
	private static final String FIRSTOPERANDERROR = "The first operand of operation `create()' should be a charstring value";
	private static final String SECONDOPERANDERROR = "The second operand of operation `create()' should be a charstring value";
	private static final String COMPONENTEXPECTED = "Operation `create'' should refer to a component type instead of {0}";
	private static final String TYPEMISMATCH1 = "Type mismatch: reference to a component type was expected in operation `create'' instead of `{0}''";
	private static final String TYPEMISMATCH2 = "Incompatible component type: operation `create'' should refer to `{0}'' instaed of `{1}''";
	private static final String OPERATIONNAME = "create()";

	private final Reference componentReference;
	private final Value name;
	private final Value location;
	private final boolean isAlive;

	private CompilationTimeStamp checkCreateTimestamp;
	private Component_Type checkCreateCache;

	public ComponentCreateExpression(final Reference componentReference, final Value name, final Value location, final boolean isAlive) {
		this.componentReference = componentReference;
		this.name = name;
		this.location = location;
		this.isAlive = isAlive;

		if (componentReference != null) {
			componentReference.setFullNameParent(this);
		}
		if (name != null) {
			name.setFullNameParent(this);
		}
		if (location != null) {
			location.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.COMPONENT_CREATE_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		if (componentReference == null) {
			return "<erroneous value>";
		}

		final StringBuilder builder = new StringBuilder();
		builder.append(componentReference.getDisplayName());
		builder.append(".create");
		if (name != null || location != null) {
			builder.append('(');
			if (name != null) {
				builder.append(name.createStringRepresentation());
			} else {
				builder.append('-');
			}
			if (location != null) {
				builder.append(", ");
				builder.append(location.createStringRepresentation());
			}
			builder.append(')');
		}
		if (isAlive) {
			builder.append(" alive");
		}

		return null;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (componentReference != null) {
			componentReference.setMyScope(scope);
		}
		if (name != null) {
			name.setMyScope(scope);
		}
		if (location != null) {
			location.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (componentReference == child) {
			return builder.append(OPERAND1);
		} else if (name == child) {
			return builder.append(OPERAND2);
		} else if (location == child) {
			return builder.append(OPERAND3);
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_COMPONENT;
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		return true;
	}

	@Override
	public Type getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return checkCreate(timestamp);
	}

	/**
	 * Checks the parameters of the expression and if they are valid in
	 * their position in the expression or not.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 * */
	private void checkExpressionOperands(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		checkCreate(timestamp);
		if (name != null) {
			IValue last = name.setLoweridToReference(timestamp);
			Type_type typeType = last.getExpressionReturntype(timestamp, expectedValue);
			if (!last.getIsErroneous(timestamp)) {
				switch (typeType) {
				case TYPE_CHARSTRING:
					last.getValueRefdLast(timestamp, referenceChain);
					break;
				case TYPE_UNDEFINED:
					break;
				default:
					name.getLocation().reportSemanticError(FIRSTOPERANDERROR);
					setIsErroneous(true);
					break;
				}
			}
		}
		if (location != null) {
			IValue last = location.setLoweridToReference(timestamp);
			Type_type typeType = last.getExpressionReturntype(timestamp, expectedValue);
			if (!last.getIsErroneous(timestamp)) {
				switch (typeType) {
				case TYPE_CHARSTRING:
					last.getValueRefdLast(timestamp, referenceChain);
					break;
				case TYPE_UNDEFINED:
					break;
				default:
					name.getLocation().reportSemanticError(SECONDOPERANDERROR);
					setIsErroneous(true);
					break;
				}
			}
		}
		checkExpressionDynamicPart(expectedValue, OPERATIONNAME, false, true, false);
	}

	@Override
	public IValue evaluateValue(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return lastValue;
		}

		isErroneous = false;
		lastTimeChecked = timestamp;
		lastValue = this;

		if (componentReference == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		return lastValue;
	}

	private Component_Type checkCreate(final CompilationTimeStamp timestamp) {
		if (checkCreateTimestamp != null && !checkCreateTimestamp.isLess(timestamp)) {
			return checkCreateCache;
		}

		checkCreateTimestamp = timestamp;
		checkCreateCache = null;

		Assignment assignment = componentReference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			setIsErroneous(true);
			return null;
		}

		if (!Assignment_type.A_TYPE.equals(assignment.getAssignmentType())) {
			componentReference.getLocation().reportSemanticError(MessageFormat.format(COMPONENTEXPECTED, assignment.getDescription()));
			setIsErroneous(true);
			return null;
		}

		IType type = ((Def_Type) assignment).getType(timestamp).getFieldType(timestamp, componentReference, 1,
				Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		if (type == null) {
			setIsErroneous(true);
			return null;
		}

		if (!Type_type.TYPE_COMPONENT.equals(type.getTypetype())) {
			componentReference.getLocation().reportSemanticError(MessageFormat.format(TYPEMISMATCH1, type.getTypename()));
			setIsErroneous(true);
			return null;
		}

		if (myGovernor != null) {
			IType last = myGovernor.getTypeRefdLast(timestamp);

			if (Type_type.TYPE_COMPONENT.equals(last.getTypetype()) && !last.isCompatible(timestamp, type, null, null, null)) {
				componentReference.getLocation().reportSemanticError(
						MessageFormat.format(TYPEMISMATCH2, last.getTypename(), type.getTypename()));
				setIsErroneous(true);
				return null;
			}
		}

		checkCreateCache = (Component_Type) type;
		return checkCreateCache;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (componentReference != null) {
			componentReference.updateSyntax(reparser, false);
			reparser.updateLocation(componentReference.getLocation());
		}

		if (name != null) {
			name.updateSyntax(reparser, false);
			reparser.updateLocation(name.getLocation());
		}

		if (location != null) {
			location.updateSyntax(reparser, false);
			reparser.updateLocation(location.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentReference != null) {
			componentReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (name != null) {
			name.findReferences(referenceFinder, foundIdentifiers);
		}
		if (location != null) {
			location.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (componentReference != null && !componentReference.accept(v)) {
			return false;
		}
		if (name != null && !name.accept(v)) {
			return false;
		}
		if (location != null && !location.accept(v)) {
			return false;
		}
		return true;
	}
}
