/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class SingleArrayDimension extends ArrayDimension {
	private static final String INTEGERVALUEEXPECTED = "Integer value was expected";
	private static final String OPERANDERROR = "A positive integer value was expected";
	private static final String LARGEINTEGERERROR = "Using a large integer value ({0}) as an array dimension is not supported";

	private Value value;
	private long size;

	public SingleArrayDimension(final Value value) {
		super();
		this.value = value;
		size = 0;

		if (value != null) {
			value.setFullNameParent(this);
		}
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (value != null) {
			value.setMyScope(scope);
		}
	}

	@Override
	public long getOffset() {
		return 0;
	}

	@Override
	public long getSize() {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return size;
	}

	@Override
	public String createStringRepresentation() {
		check(CompilationTimeStamp.getBaseTimestamp());

		StringBuilder builder = new StringBuilder();
		builder.append('[');
		if (value == null) {
			builder.append("<erroneous>");
		} else {
			builder.append(value.createStringRepresentation());
		}
		builder.append(']');

		return builder.toString();
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		setIsErroneous(false);

		if (value == null) {
			return;
		}

		IValue last = value.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_CONSTANT, null);
		if (last == null || last.getIsErroneous(timestamp)) {
			return;
		}

		Type_type ttype = value.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_CONSTANT);
		switch (ttype) {
		case TYPE_INTEGER:
			if (last.isUnfoldable(timestamp)) {
				value.getLocation().reportSemanticError(OPERANDERROR);
				setIsErroneous(true);
			} else if (Value.Value_type.INTEGER_VALUE.equals(last.getValuetype())) {
				if (!((Integer_Value) last).isNative()) {
					value.getLocation().reportSemanticError(MessageFormat.format(LARGEINTEGERERROR, last));
					setIsErroneous(true);
				} else {
					size = ((Integer_Value) last).getValue();
					if (size < 0) {
						value.getLocation().reportSemanticError(OPERANDERROR);
						setIsErroneous(true);
					}
				}
			}
			return;
		default:
			value.getLocation().reportSemanticError(INTEGERVALUEEXPECTED);
			value.setIsErroneous(true);
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}
	}
	
	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (value != null) {
			if (!value.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
