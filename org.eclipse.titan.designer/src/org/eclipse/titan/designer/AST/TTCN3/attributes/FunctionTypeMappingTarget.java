/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function.EncodingPrototype_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a function based type mapping target.
 * 
 * @author Kristof Szabados
 * */
public final class FunctionTypeMappingTarget extends TypeMappingTarget {
	private static final String FULLNAMEPART1 = ".<target_type>";
	private static final String FULLNAMEPART2 = ".<function_ref>";

	private final Type targetType;
	private final Reference functionReference;
	private Def_Function functionReferenced;
	private Def_Extfunction extfunctionReferenced;

	public FunctionTypeMappingTarget(final Type targetType, final Reference functionReference) {
		this.targetType = targetType;
		this.functionReference = functionReference;
	}

	@Override
	public TypeMapping_type getTypeMappingType() {
		return TypeMapping_type.FUNCTION;
	}

	@Override
	public String getMappingName() {
		return "function";
	}

	@Override
	public Type getTargetType() {
		return targetType;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (targetType == child) {
			return builder.append(FULLNAMEPART1);
		} else if (functionReference == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (targetType != null) {
			targetType.setMyScope(scope);
		}
		if (functionReference != null) {
			functionReference.setMyScope(scope);
		}
	}

	public Def_Function getFunction() {
		return functionReferenced;
	}

	public Def_Extfunction getExternalFunction() {
		return extfunctionReferenced;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp, final Type source) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		functionReferenced = null;
		extfunctionReferenced = null;

		if (functionReference == null) {
			return;
		}

		final Assignment assignment = functionReference.getRefdAssignment(timestamp, false);
		if (assignment == null) {
			return;
		}

		assignment.check(timestamp);

		EncodingPrototype_type referencedPrototype;
		Type inputType;
		Type outputType;
		switch (assignment.getAssignmentType()) {
		case A_FUNCTION:
		case A_FUNCTION_RVAL:
		case A_FUNCTION_RTEMP:
			functionReferenced = (Def_Function) assignment;
			referencedPrototype = functionReferenced.getPrototype();
			inputType = functionReferenced.getInputType();
			outputType = functionReferenced.getOutputType();
			break;
		case A_EXT_FUNCTION:
		case A_EXT_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RTEMP:
			extfunctionReferenced = (Def_Extfunction) assignment;
			referencedPrototype = extfunctionReferenced.getPrototype();
			inputType = extfunctionReferenced.getInputType();
			outputType = extfunctionReferenced.getOutputType();
			break;
		default:
			functionReference.getLocation().reportSemanticError(
					MessageFormat.format("Reference to a function or external function was expected instead of {0}",
							assignment.getDescription()));
			return;
		}

		if (EncodingPrototype_type.NONE.equals(referencedPrototype)) {
			functionReference.getLocation().reportSemanticError(
					MessageFormat.format("The referenced {0} does not have `prototype'' attribute", assignment.getDescription()));
			return;
		}

		if (inputType != null && source != null && !source.isIdentical(timestamp, inputType)) {
			final String message = MessageFormat
					.format("The input type of {0} must be the same as the source type of the mapping: `{1}'' was expected instead of `{2}''",
							assignment.getDescription(), source.getTypename(), inputType.getTypename());
			source.getLocation().reportSemanticError(message);
		}
		if (outputType != null && !targetType.isIdentical(timestamp, outputType)) {
			final String message = MessageFormat
					.format("The output type of {0} must be the same as the target type of the mapping: `{1}'' was expected instead of `{2}''",
							assignment.getDescription(), targetType.getTypename(), outputType.getTypename());
			targetType.getLocation().reportSemanticError(message);
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (targetType != null) {
			targetType.findReferences(referenceFinder, foundIdentifiers);
		}
		if (functionReference != null) {
			functionReference.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (targetType != null && !targetType.accept(v)) {
			return false;
		}
		if (functionReference != null && !functionReference.accept(v)) {
			return false;
		}
		return true;
	}
}
