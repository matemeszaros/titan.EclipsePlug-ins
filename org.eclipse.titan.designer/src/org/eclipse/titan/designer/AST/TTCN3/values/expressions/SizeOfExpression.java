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
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ASN1.values.RelativeObjectIdentifier_Value;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Timer;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.LengthRestriction;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.RangeLenghtRestriction;
import org.eclipse.titan.designer.AST.TTCN3.templates.SingleLenghtRestriction;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.templates.Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.AST.TTCN3.values.Array_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.ObjectIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Sequence_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Set_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class SizeOfExpression extends Expression_Value {
	private final TemplateInstance templateInstance;

	// private Reference reference;

	public SizeOfExpression(final TemplateInstance templateInstance) {
		this.templateInstance = templateInstance;

		if (templateInstance != null) {
			templateInstance.setFullNameParent(this);
		}
	}

	@Override
	public Operation_type getOperationType() {
		return Operation_type.SIZEOF_OPERATION;
	}

	@Override
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("sizeof(");
		builder.append(templateInstance.createStringRepresentation());
		builder.append(')');
		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (templateInstance != null) {
			templateInstance.setMyScope(scope);
		}
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (templateInstance == child) {
			return builder.append(OPERAND);
		}

		return builder;
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_INTEGER;
	}

	@Override
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		return !(lastValue instanceof Integer_Value);
	}

	/**
	 * Helper function for checking the dimensions of time and port arrays.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param ref
	 *                reference to the assignment
	 * @param dimensions
	 *                the dimensions of the port or time array.
	 * @param assignment
	 *                the assignment itself, used to get its name and
	 *                description.
	 * */
	private long checkTimerPort(final CompilationTimeStamp timestamp, final Reference ref, final ArrayDimensions dimensions,
			final Assignment assignment) {
		if (dimensions == null) {
			templateInstance.getLocation().reportSemanticError(
					MessageFormat.format("operation is not applicable to single {0}", assignment.getDescription()));
			setIsErroneous(true);
			return -1;
		}
		dimensions.checkIndices(timestamp, ref, assignment.getAssignmentName(), true, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		List<ISubReference> subreferences = ref.getSubreferences();
		int referencedDimensions;
		if (subreferences.size() > 1) {
			referencedDimensions = subreferences.size() - 1;
			int nofDimensions = dimensions.size();
			if (referencedDimensions < nofDimensions) {
				setIsErroneous(true);
				return -1;
			} else if (referencedDimensions == nofDimensions) {
				templateInstance.getLocation().reportSemanticError(
						MessageFormat.format("Operation is not applicable to a {0}", assignment.getAssignmentName()));
				setIsErroneous(true);
				return -1;
			}
		} else {
			referencedDimensions = 0;
		}

		return dimensions.get(referencedDimensions).getSize();
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
	 * 
	 * @return the size of the expression, or -1 in case of error
	 * */
	private long checkExpressionOperands(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		Expected_Value_type internalExpectedValue;

		if (Expected_Value_type.EXPECTED_DYNAMIC_VALUE.equals(expectedValue)) {
			internalExpectedValue = Expected_Value_type.EXPECTED_TEMPLATE;
		} else {
			internalExpectedValue = expectedValue;
		}

		ITTCN3Template template = templateInstance.getTemplateBody();
		template.getTemplateReferencedLast(timestamp, referenceChain);

		// Timer and port arrays are handled separately
		if (template.getTemplatetype() == Template_type.SPECIFIC_VALUE) {
			SpecificValue_Template specValTempl = (SpecificValue_Template) template;
			IValue val = specValTempl.getSpecificValue();
			val.setMyGovernor(specValTempl.getMyGovernor());
			if (val.getValuetype() == Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE) {
				val = val.setLoweridToReference(timestamp);
			}

			if (val != null && val.getValuetype() == Value_type.REFERENCED_VALUE) {
				Referenced_Value referencedValue = (Referenced_Value) val;
				Reference ref = referencedValue.getReference();
				Assignment temporalAss = ref.getRefdAssignment(timestamp, true);
				if (temporalAss != null) {
					Assignment_type asstype = temporalAss.getAssignmentType();
					ArrayDimensions dimensions;
					if (asstype == Assignment_type.A_PORT) {
						dimensions = ((Def_Port) temporalAss).getDimensions();
						return checkTimerPort(timestamp, ref, dimensions, temporalAss);
					} else if (asstype == Assignment_type.A_TIMER) {
						dimensions = ((Def_Timer) temporalAss).getDimensions();
						return checkTimerPort(timestamp, ref, dimensions, temporalAss);
					}
				}
			}
		}

		IType governor = templateInstance.getExpressionGovernor(timestamp, internalExpectedValue);
		if (governor == null) {
			ITTCN3Template templ = templateInstance.getTemplateBody().setLoweridToReference(timestamp);
			governor = templ.getExpressionGovernor(timestamp, internalExpectedValue);
		}
		if (governor == null) {
			setIsErroneous(true);
			return -1;
		}

		Type_type typetype = templateInstance.getExpressionReturntype(timestamp, internalExpectedValue);
		switch (typetype) {
		case TYPE_TTCN3_SET:
		case TYPE_SET_OF:
		case TYPE_TTCN3_SEQUENCE:
		case TYPE_SEQUENCE_OF:
		case TYPE_ASN1_SEQUENCE:
		case TYPE_ASN1_SET:
		case TYPE_ARRAY:
		case TYPE_OBJECTID:
		case TYPE_ROID:
		case TYPE_UNDEFINED:
			break;
		default:
			templateInstance.getLocation().reportSemanticError(
					"Reference to a value or template of type record, record of, set, set of, objid or array was expected");
			setIsErroneous(true);
			return -1;
		}

		IsValueExpression.checkExpressionTemplateInstance(timestamp, this, templateInstance, governor, referenceChain, internalExpectedValue);
		if (isErroneous) {
			return -1;
		}

		templateInstance.getTemplateBody().setLoweridToReference(timestamp);
		if (template.getTemplatetype() != Template_type.SPECIFIC_VALUE) {
			return -1;
		}

		IValue value = ((SpecificValue_Template) template).getSpecificValue();

		if (value.getValuetype() == Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE) {
			value = value.setLoweridToReference(timestamp);
		}

		if (value.getValuetype() != Value_type.REFERENCED_VALUE) {
			return evaluateValue(value);
		}

		Reference ref = ((Referenced_Value) value).getReference();
		Assignment assignment = ref.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return -1;
		}
		if (assignment.getIsErroneous()) {
			setIsErroneous(true);
			return -1;
		}

		switch (assignment.getAssignmentType()) {
		case A_CONST:
			value = ((Def_Const) assignment).getValue().getValueRefdLast(timestamp, internalExpectedValue, referenceChain);
			return evaluateValue(value);
		case A_TEMPLATE:
			template = ((Def_Template) assignment).getTemplate(timestamp).getTemplateReferencedLast(timestamp, referenceChain);
			return evaluateTemplate(template, timestamp);
		case A_TIMER: {
			ArrayDimensions dimensions = ((Def_Timer) assignment).getDimensions();
			return checkTimerPort(timestamp, ref, dimensions, assignment);
		}
		case A_PORT: {
			ArrayDimensions dimensions = ((Def_Port) assignment).getDimensions();
			return checkTimerPort(timestamp, ref, dimensions, assignment);
		}
		case A_EXT_CONST:
		case A_MODULEPAR:
			if (Expected_Value_type.EXPECTED_CONSTANT.equals(internalExpectedValue)) {
				templateInstance.getLocation().reportSemanticError(
						MessageFormat.format("Reference to an (evaluatable) constant value was expected instead of {0}",
								assignment.getDescription()));
				setIsErroneous(true);
				return -1;
			}
			break;
		case A_VAR:
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
			switch (internalExpectedValue) {
			case EXPECTED_CONSTANT:
				templateInstance.getLocation().reportSemanticError(
						MessageFormat.format("Reference to a constant value was expected instead of {0}",
								assignment.getDescription()));
				setIsErroneous(true);
				return -1;
			case EXPECTED_STATIC_VALUE:
				templateInstance.getLocation().reportSemanticError(
						MessageFormat.format("Reference to a static value was expected instead of {0}",
								assignment.getDescription()));
				setIsErroneous(true);
				return -1;
			default:
				break;
			}
			break;
		case A_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RVAL:
			switch (internalExpectedValue) {
			case EXPECTED_CONSTANT:
				templateInstance.getLocation().reportSemanticError(
						MessageFormat.format("Reference to a constant value was expected instead of the return value of {0}",
								assignment.getDescription()));
				setIsErroneous(true);
				return -1;
			case EXPECTED_STATIC_VALUE:
				templateInstance.getLocation().reportSemanticError(
						MessageFormat.format("Reference to a static value was expected instead of the return value of {0}",
								assignment.getDescription()));
				setIsErroneous(true);
				return -1;
			default:
				break;
			}
			break;
		case A_FUNCTION_RTEMP:
		case A_EXT_FUNCTION_RTEMP:
			if (!Expected_Value_type.EXPECTED_TEMPLATE.equals(internalExpectedValue)) {
				templateInstance.getLocation()
						.reportSemanticError(
								MessageFormat.format(
										"Reference to a value was expected instead of a call of {0}, which returns a template",
										assignment.getDescription()));
				setIsErroneous(true);
				return -1;
			}
			break;
		case A_VAR_TEMPLATE:
		case A_PAR_TEMP_IN:
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT:
			if (!Expected_Value_type.EXPECTED_TEMPLATE.equals(internalExpectedValue)) {
				templateInstance.getLocation()
						.reportSemanticError(
								MessageFormat.format(Type.REFTOVALUEEXPECTED,
										assignment.getDescription()));
				setIsErroneous(true);
				return -1;
			}
			break;
		default:
			return -1;
		}
		return -1;
	}

	/**
	 * Evaluates a checked value
	 * 
	 * @param value
	 *                The value to evaluate.
	 * @return The folded value or -1 if the value is unfoldable.
	 */
	private long evaluateValue(final IValue value) {
		switch (value.getValuetype()) {
		case SEQUENCEOF_VALUE: {
			SequenceOf_Value seqOfValue = (SequenceOf_Value) value;
			if (seqOfValue.isIndexed()) {
				return -1;
			}
			return seqOfValue.getNofComponents();
		}
		case SETOF_VALUE: {
			SetOf_Value setOfValue = (SetOf_Value) value;
			if (setOfValue.isIndexed()) {
				return -1;
			}
			return setOfValue.getNofComponents();
		}
		case ARRAY_VALUE: {
			Array_Value arrayValue = (Array_Value) value;
			if (arrayValue.isIndexed()) {
				return -1;
			}
			return arrayValue.getNofComponents();
		}
		case OBJECTID_VALUE:
			return ((ObjectIdentifier_Value) value).getNofComponents();
		case RELATIVEOBJECTIDENTIFIER_VALUE:
			return ((RelativeObjectIdentifier_Value) value).getNofComponents();
		case SEQUENCE_VALUE: {
			int result = 0;
			Sequence_Value temp = (Sequence_Value) value;
			for (int i = 0, size = temp.getNofComponents(); i < size; i++) {
				if (!Value_type.OMIT_VALUE.equals(temp.getSeqValueByIndex(i).getValue().getValuetype())) {
					result++;
				}
			}
			return result;
		}
		case SET_VALUE: {
			int result = 0;
			Set_Value temp = (Set_Value) value;
			for (int i = 0, size = temp.getNofComponents(); i < size; i++) {
				if (!Value_type.OMIT_VALUE.equals(temp.getSequenceValueByIndex(i).getValue().getValuetype())) {
					result++;
				}
			}
			return result;
		}
		default:
			return -1;
		}
	}

	/**
	 * Evaluates a checked template.
	 * 
	 * @param template
	 *                The template to evaluate
	 * @param timestamp
	 *                The compilation timestamp
	 * @return The folded value or -1 if the template is unfoldable.
	 */
	private long evaluateTemplate(final ITTCN3Template template, final CompilationTimeStamp timestamp) {
		switch (template.getTemplatetype()) {
		case TEMPLATE_LIST: {
			Template_List temp = (Template_List) template;
			if (temp.templateContainsAnyornone()) {
				LengthRestriction lengthRestriction = temp.getLengthRestriction();
				if (lengthRestriction == null) {
					templateInstance.getLocation()
							.reportSemanticError(
									"`sizeof' operation is not applicable for templates containing `*' without length restriction");
					setIsErroneous(true);
					return -1;
				}

				if (lengthRestriction instanceof RangeLenghtRestriction) {
					IValue upper = ((RangeLenghtRestriction) lengthRestriction).getUpperValue(timestamp);
					if (Value_type.REAL_VALUE.equals(upper.getValuetype()) && ((Real_Value) upper).isPositiveInfinity()) {
						templateInstance.getLocation()
								.reportSemanticError(
										"`sizeof' operation is not applicable for templates containing `*' without upper boundary in the length restriction");
						setIsErroneous(true);
						return -1;
					}

					if (Value_type.INTEGER_VALUE.equals(upper.getValuetype())) {
						int nofComponents = temp.getNofTemplatesNotAnyornone(timestamp);
						if (nofComponents == ((Integer_Value) upper).intValue()) {
							return nofComponents;
						}

						IValue lower = ((RangeLenghtRestriction) lengthRestriction).getLowerValue(timestamp);
						if (lower != null && Value_type.INTEGER_VALUE.equals(lower.getValuetype())
								&& ((Integer_Value) upper).intValue() == ((Integer_Value) lower).intValue()) {
							return ((Integer_Value) upper).intValue();
						}

						templateInstance.getLocation().reportSemanticError(
								"`sizeof' operation is not applicable for templates without exact size");
						setIsErroneous(true);
						return -1;
					}
				} else {
					IValue restriction = ((SingleLenghtRestriction) lengthRestriction).getRestriction(timestamp);
					if (Value_type.INTEGER_VALUE.equals(restriction.getValuetype())) {
						return ((Integer_Value) restriction).intValue();
					}
				}
			} else {
				int result = 0;
				for (int i = 0, size = temp.getNofTemplates(); i < size; i++) {
					ITTCN3Template tmp = temp.getTemplateByIndex(i);
					switch (tmp.getTemplatetype()) {
					case SPECIFIC_VALUE:
						if (tmp.getValue().getValuetype() != Value_type.OMIT_VALUE) {
							++result;
						}
						break;
					default:
						++result;
					}
				}
				return result;
			}
			break;
		}
		case NAMED_TEMPLATE_LIST: {
			int result = 0;
			Named_Template_List temp = (Named_Template_List) template;
			for (int i = 0, size = temp.getNofTemplates(); i < size; i++) {
				ITTCN3Template tmp = temp.getTemplateByIndex(i).getTemplate();
				switch (tmp.getTemplatetype()) {
				case SPECIFIC_VALUE:
					if (tmp.getValue().getValuetype() != Value_type.OMIT_VALUE) {
						++result;
					}
					break;
				default:
					++result;
				}
			}
			return result;
		}
		default:
			return -1;
		}
		return -1;
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

		if (templateInstance == null) {
			return lastValue;
		}

		long i = checkExpressionOperands(timestamp, expectedValue, referenceChain);
		if (i != -1) {
			lastValue = new Integer_Value(i);
			lastValue.copyGeneralProperties(this);
		}

		if (getIsErroneous(timestamp) || isUnfoldable(timestamp, referenceChain)) {
			return lastValue;
		}

		return lastValue;
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
