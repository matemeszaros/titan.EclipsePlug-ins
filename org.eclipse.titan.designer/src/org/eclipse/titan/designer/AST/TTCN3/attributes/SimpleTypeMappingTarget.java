/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents an simple type mapping target (source == target).
 * 
 * @author Kristof Szabados
 * */
public final class SimpleTypeMappingTarget extends TypeMappingTarget {

	private final Type target_type;

	public SimpleTypeMappingTarget(final Type target_type) {
		this.target_type = target_type;
	}

	@Override
	public TypeMapping_type getTypeMappingType() {
		return TypeMapping_type.SIMPLE;
	}

	@Override
	public String getMappingName() {
		return "simple";
	}

	@Override
	public Type getTargetType() {
		return target_type;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp, final Type source) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (source != null && !source.isIdentical(timestamp, target_type)) {
			target_type.getLocation().reportSemanticError(
					MessageFormat.format("The source and target types must be the same: `{0}'' was expected instead of `{1}''",
							source.getTypename(), target_type.getTypename()));
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (target_type != null) {
			target_type.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (target_type != null && !target_type.accept(v)) {
			return false;
		}
		return true;
	}
}
