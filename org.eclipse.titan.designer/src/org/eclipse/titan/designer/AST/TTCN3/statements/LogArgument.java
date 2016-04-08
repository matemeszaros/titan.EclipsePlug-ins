/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ASN1.Value_Assignment;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.statements.InternalLogArgument.ArgumentType;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Macro_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.MatchExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class LogArgument extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {

	private final TemplateInstance templateInstance;

	private InternalLogArgument internalLogArgument;

	private Location location = NULL_Location.INSTANCE;

	/**
	 * indicates if this log argument has already been found erroneous in
	 * the actual checking cycle.
	 */
	private boolean isErroneous;

	/** the time when this argument was check the last time. */
	private CompilationTimeStamp lastTimeChecked;

	public LogArgument(final TemplateInstance templateInstance) {
		super();
		this.templateInstance = templateInstance;

		if (templateInstance != null) {
			templateInstance.setFullNameParent(this);
		}
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (templateInstance != null) {
			templateInstance.setMyScope(scope);
		}
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	/**
	 * Does the semantic checking of the log argument. This is the main
	 * entry point.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (templateInstance == null) {
			return;
		}

		lastTimeChecked = timestamp;
		isErroneous = false;

		ITTCN3Template template = templateInstance.getTemplateBody();
		template = template.getTemplateReferencedLast(timestamp);
		if (template.getIsErroneous(timestamp)) {
			isErroneous = true;
			return;
		}

		template = template.setLoweridToReference(timestamp);
		if (template.getIsErroneous(timestamp)) {
			isErroneous = true;
			return;
		}

		if (templateInstance.getType() == null && templateInstance.getDerivedReference() == null && template.isValue(timestamp)) {
			final IValue value = template.getValue();
			final IType gov = template.getMyGovernor();
			if (gov != null) {
				value.setMyGovernor(gov);
			}
			checkValue(timestamp, value);
		} else {
			IType governor = templateInstance.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);

			if (governor == null) {
				final ITTCN3Template temporalTemplate = template.setLoweridToReference(timestamp);
				governor = temporalTemplate.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
			}

			if (governor == null) {
				getLocation().reportSemanticError("Cannot determine the type of the argument");
				isErroneous = true;
			} else {
				internalLogArgument = new TemplateInstance_InternalLogArgument(templateInstance);
				templateInstance.check(timestamp, governor);
			}
		}
	}

	/**
	 * Does the semantic checking of the log argument. Once it was
	 * determined that it is a value.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param value
	 *                the value contained in the log argument.
	 * */
	private void checkValue(final CompilationTimeStamp timestamp, final IValue value) {
		final IValue temp = value.setLoweridToReference(timestamp);
		switch (temp.getValuetype()) {
		case CHARSTRING_VALUE:
			internalLogArgument = new String_InternalLogArgument(((Charstring_Value) temp).getValue());
			break;
		case REFERENCED_VALUE:
			final Reference reference = ((Referenced_Value) temp).getReference();
			internalLogArgument = new Reference_InternalLogArgument(reference);
			checkReference(timestamp, reference);
			return;
		case EXPRESSION_VALUE:
			final Expression_Value castedValue = (Expression_Value) temp;
			if (Operation_type.MATCH_OPERATION.equals(castedValue.getOperationType())) {
				internalLogArgument = new Match_InternalLogArgument((MatchExpression) castedValue);
			} else {
				internalLogArgument = new Value_InternalLogArgument(temp);
			}
			break;
		case MACRO_VALUE:
			internalLogArgument = new Macro_InternalLogArgument((Macro_Value) temp);
			break;
		default:
			internalLogArgument = new Value_InternalLogArgument(temp);
			break;
		}

		final IType governor = temp.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		if (governor == null) {
			getLocation().reportSemanticError("Cannot determine the type of the argument");
			isErroneous = true;
			return;
		}

		//TODO: Is the next part necessary ???
		temp.setMyGovernor(governor);
		governor.checkThisValue(timestamp, temp, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, true, true, true, true, false));

		final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		if (ArgumentType.Value.equals(internalLogArgument.getArgumentType()) && !temp.isUnfoldable(timestamp)) {
			final 	IValue last = temp.getValueRefdLast(timestamp, chain);
			if (Value_type.CHARSTRING_VALUE.equals(last.getValuetype())) {
				internalLogArgument = new String_InternalLogArgument(((Charstring_Value) last).getValue());
			}
		} else if (ArgumentType.Macro.equals(internalLogArgument.getArgumentType())) {
			final IValue last = temp.getValueRefdLast(timestamp, chain);
			switch (last.getValuetype()) {
			case CHARSTRING_VALUE:
				internalLogArgument = new String_InternalLogArgument(((Charstring_Value) last).getValue());
				break;
			case MACRO_VALUE:
				break;
			default:
				internalLogArgument = new Value_InternalLogArgument(temp);
				break;
			}
		}

		chain.release();
	}

	/**
	 * Does the semantic checking of the log argument. Once it was
	 * determined that it is a reference.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference contained in the log argument.
	 * */
	private void checkReference(final CompilationTimeStamp timestamp, final Reference reference) {
		if (reference == null) {
			return;
		}

		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null || assignment.getIsErroneous()) {
			return;
		}

		switch (assignment.getAssignmentType()) {
		case A_FUNCTION_RVAL:
		case A_FUNCTION_RTEMP:
		case A_EXT_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RTEMP:
			reference.getMyScope().checkRunsOnScope(timestamp, assignment, reference, "call");
			assignment.getType(timestamp).getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
			break;
		case A_CONST:
			if (assignment.getType(timestamp).getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false) != null) {
				final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				if (assignment instanceof Def_Const) {
					((Def_Const) assignment).getValue().getReferencedSubValue(timestamp, reference, 1, chain);
				} else {
					((Value_Assignment) assignment).getValue().getReferencedSubValue(timestamp, reference, 1, chain);
				}
				chain.release();
			}
			break;
		case A_TEMPLATE:
			if (assignment.getType(timestamp).getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false) != null) {
				final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				((Def_Template) assignment).getTemplate(timestamp).getReferencedSubTemplate(timestamp, reference, chain);
				chain.release();
			}
			break;
		case A_MODULEPAR_TEMPLATE:
			assignment.getType(timestamp).getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
			break;
		case A_EXT_CONST:
		case A_MODULEPAR:
		case A_VAR:
		case A_VAR_TEMPLATE:
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
		case A_PAR_TEMP_IN:
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT:
			assignment.getType(timestamp).getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
			break;
		case A_PORT: {
			final ArrayDimensions dimensions = ((Def_Port) assignment).getDimensions();
			if (dimensions != null) {
				dimensions.checkIndices(timestamp, reference, assignment.getAssignmentName(), true,
						Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			} else if (reference.getSubreferences().size() > 1) {
				getLocation().reportSemanticError(
						MessageFormat.format("Reference to single {0} cannot have field or array sub-references",
								assignment.getDescription()));
				isErroneous = true;
			}
			break;
		}
		case A_TIMER: {
			final ArrayDimensions dimensions = ((Def_Timer) assignment).getDimensions();
			if (dimensions != null) {
				dimensions.checkIndices(timestamp, reference, assignment.getAssignmentName(), true,
						Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			} else if (reference.getSubreferences().size() > 1) {
				getLocation().reportSemanticError(
						MessageFormat.format("Reference to single {0} cannot have field or array sub-references",
								assignment.getDescription()));
				isErroneous = true;
			}
			break;
		}
		case A_PAR_TIMER:
		case A_PAR_PORT:
			if (reference.getSubreferences().size() > 1) {
				getLocation().reportSemanticError(
						MessageFormat.format("Reference to {0} cannot have field or array sub-references",
								assignment.getDescription()));
				isErroneous = true;
			}
			break;
		case A_FUNCTION:
		case A_EXT_FUNCTION:
			getLocation().reportSemanticError(
					MessageFormat.format(
							"Reference to a value, template, timer or port was ecpected instead of a call of {0}, which does not have a return type",
							assignment.getDescription()));
			isErroneous = true;
			break;
		default:
			getLocation().reportSemanticError(
					MessageFormat.format("Reference to a value, template, timer or port was expected instead of {0}",
							assignment.getDescription()));
			isErroneous = true;
			break;
		}
	}

	/**
	 * Checks whether this value is defining itself in a recursive way. This
	 * can happen for example if a constant is using itself to determine its
	 * initial value.
	 * 
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references.
	 * */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (internalLogArgument == null || isErroneous) {
			return;
		}

		internalLogArgument.checkRecursions(timestamp, referenceChain);
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (templateInstance != null) {
			templateInstance.updateSyntax(reparser, false);
			reparser.updateLocation(templateInstance.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (templateInstance == null) {
			return;
		}

		templateInstance.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (templateInstance != null && !templateInstance.accept(v)) {
			return false;
		}
		return true;
	}
}
