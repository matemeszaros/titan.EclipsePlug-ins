/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReparseUtilities;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The FormalParameter class represents TTCN3 formal parameters.
 * 
 * @author Kristof Szabados
 * */
public final class FormalParameter extends Definition {
	private static final String FULLNAMEPART1 = ".<type>";
	private static final String FULLNAMEPART2 = ".<default_value>";
	public static final String PORTTYPENOTALLOWEDAS = "Port type can not be used as {0}";
	public static final String SIGNATURENOTALLOWEDAS = "Signature types can not be used as {1}";
	private static final String SPECIFICVALUEXPECTED = "A specific value without matching symbols was expected for a {0}";
	private static final String EXPLICITESPECIFICATIONFORTIMER = "Explicit type specification cannot be used for a timer parameter";
	private static final String INLINETEMPLATEFORTIMER = "An in-line modified template cannot be used as timer parameter";
	private static final String SUBREFERENCEERROR1 = "Reference to single {0} cannot have field or array sub-references";
	private static final String SUBREFERENCEERROR2 = "Reference to {0} cannot have sub-references";
	private static final String SUBREFERENCEERROR3 = "Reference to {0} cannot have field or array sub-references";
	private static final String TIMEREXPECTED1 = "Reference to a timer or timer parameter was expected for a timer parameter instead of {0}";
	private static final String TIMEREXPECTED2 = "Reference to a timer or timer parameter was expected for a time parameter";
	private static final String PORTEXPECTED = "Reference to a port or port parameter was expected for a port parameter instead of {0}";
	private static final String TYPEMISMATCH = "Type mismatch: Reference to a port or port parameter of type `{0}'' was expected instead of `{1}''";
	private static final String EXPLICITESPECIFICATIONFORREFERENCE = "Explicit type specification is useless for an {0}";
	private static final String INLINETEMPLATEFORREFERENCE = "An in-line modified template cannot be used as {0}";
	private static final String TYPEMISMATCH2 = "Type mismatch: Reference to a {0} of type `{1}'' was expected instead of `{2}''";
	private static final String SUBTYPEMISMATCH = "Subtype mismatch: subtype {0} has no common value with subtype {1}";
	private static final String REFERENCEEXPECTED1 = "Reference to a {0} was expected for an {1} instead of {2}";
	private static final String REFERENCEEXPECTED2 = "Reference to a {0} was expected for an {1}";
	private static final String REFERENCEEXPECTED3 = "Reference to a string element of type `{0}'' cannot be used in this context";

	private static final String KIND = "formal parameter";

	private final Assignment_type assignmentType;
	private final Type type;

	private TemplateInstance defaultValue;
	// the default parameter after semantic check
	private ActualParameter actualDefaultParameter;
	private Assignment_type realAssignmentType;
	private final TemplateRestriction.Restriction_type templateRestriction;
	private FormalParameterList myParameterList;
	private boolean isLazy;

	private boolean wasAssigned;

	public FormalParameter(final TemplateRestriction.Restriction_type templateRestriction, final Assignment_type assignmentType,
			final Type type, final Identifier identifier, final TemplateInstance defaultValue, final boolean isLazy) {
		super(identifier);
		this.assignmentType = assignmentType;
		realAssignmentType = assignmentType;
		this.type = type;
		this.defaultValue = defaultValue;
		this.templateRestriction = templateRestriction;
		this.isLazy = isLazy;

		if (type != null) {
			type.setFullNameParent(this);
		}
		if (defaultValue != null) {
			defaultValue.setFullNameParent(this);
		}
	}

	private FormalParameter(final Assignment_type assignmentType, final FormalParameter other) {
		super(other.identifier);
		super.setFullNameParent(other.getNameParent());
		this.assignmentType = assignmentType;
		realAssignmentType = assignmentType;
		this.type = other.type;
		this.defaultValue = other.defaultValue;
		this.templateRestriction = other.templateRestriction;
		this.isLazy = other.isLazy;
		this.myScope = other.myScope;
		this.lastTimeChecked = other.lastTimeChecked;
		this.location = other.location;
		this.parentGroup = other.parentGroup;

		if (type != null) {
			type.setFullNameParent(this);
		}
		if (defaultValue != null) {
			defaultValue.setFullNameParent(this);
		}
	}

	public boolean getIsLazy() {
		return isLazy;
	}

	@Override
	public Assignment_type getAssignmentType() {
		return realAssignmentType;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (type == child) {
			return builder.append(FULLNAMEPART1);
		} else if (defaultValue == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	public FormalParameterList getMyParameterList() {
		return myParameterList;
	}

	public void setFormalParamaterList(final FormalParameterList list) {
		myParameterList = list;
	}

	@Override
	public Type getType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return type;
	}

	/**
	 * @return true if the formal parameter has a default value, false
	 *         otherwise.
	 * */
	public boolean hasDefaultValue() {
		return defaultValue != null;
	}

	/**
	 * Replaces the default parameter, probably a '-'.
	 *
	 * @param otherValue
	 *                the new parameter.
	 * */
	public void setDefaultValue(final TemplateInstance otherValue) {
		defaultValue = new TemplateInstance(otherValue.getType(), otherValue.getDerivedReference(), otherValue.getTemplateBody());
	}

	/**
	 * @return the default formal parameter if exists, null otherwise.
	 * */
	public TemplateInstance getDefaultParameter() {
		return defaultValue;
	}

	/**
	 * @return the actual parameter value of the default parameter if
	 *         exists, null otherwise
	 * */
	public ActualParameter getDefaultValue() {
		return actualDefaultParameter;
	}

	/**
	 * @return true if the formal parameter has a default "-" value, false
	 *         otherwise.
	 * */
	public boolean hasNotusedDefaultValue() {
		if (defaultValue == null || defaultValue.getTemplateBody() == null) {
			return false;
		}

		return ITTCN3Template.Template_type.TEMPLATE_NOTUSED.equals(defaultValue.getTemplateBody().getTemplatetype());
	}

	/**
	 * @return the real assignment type of the formal parameter, as the
	 *         actual one set at parsing time might not be correct.
	 * */
	public Assignment_type getRealAssignmentType() {
		return realAssignmentType;
	}

	@Override
	public String getAssignmentName() {
		switch (realAssignmentType) {
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
			return "value parameter";
		case A_PAR_VAL_OUT:
			return "`out' value parameter";
		case A_PAR_VAL_INOUT:
			return "`inout' value parameter";
		case A_PAR_TEMP_IN:
			return "template parameter";
		case A_PAR_TEMP_OUT:
			return "`out' template parameter";
		case A_PAR_TEMP_INOUT:
			return "`inout' template parameter";
		case A_PAR_TIMER:
			return "timer parameter";
		case A_PAR_PORT:
			return "port parameter";
		default:
			return "<unknown formal parameter>";
		}
	}

	@Override
	public String getDescription() {
		StringBuilder builder = new StringBuilder(getAssignmentName());
		return builder.append(" `").append(identifier.getDisplayName()).append('\'').toString();
	}

	@Override
	public String getOutlineIcon() {
		return "titan.gif";
	}

	@Override
	public String getProposalKind() {
		return KIND;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (type != null) {
			type.setMyScope(scope);
		}
		if (defaultValue != null) {
			defaultValue.setMyScope(scope);
		}
	}

	@Override
	public boolean isLocal() {
		return true;
	}

	/**
	 * Creates a new formal parameters with the very same information, but a
	 * different assignment type.
	 *
	 * @param newType
	 *                the new assignment type for the new formal parameter
	 *
	 * @return the new formal parameter of the provided kind.
	 * */
	public FormalParameter setParameterType(final Assignment_type newType) {
		return new FormalParameter(newType, this);
	}

	/**
	 * Checks whether the value of the parameter may be modified in the body
	 * of the parameterized definition (in assignment, port redirect or
	 * passing it further as 'out' or 'inout' parameter). Meaningful only
	 * for `in' value or template parameters.
	 * */
	public void useAsLValue(final Reference reference) {
		switch (getRealAssignmentType()) {
		case A_PAR_VAL_IN:
		case A_PAR_TEMP_IN:
			break;
		default:
			return;
		}

		Definition definition = myParameterList.getMyDefinition();
		if (definition == null) {
			return;
		}

		if (Assignment_type.A_TEMPLATE.equals(definition.getAssignmentType())) {
			reference.getLocation().reportSemanticError(
					MessageFormat.format(
							"Parameter `{0}'' of the template cannot be passed further as `out'' or `inout'' parameter",
							identifier.getDisplayName()));
		}
	}

	/** reset the properties tracking the use of the formal parameter */
	public void reset() {
		isUsed = false;
		wasAssigned = false;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (type != null) {
			type.check(timestamp);

			IType lastType = type.getTypeRefdLast(timestamp);
			if (lastType == null) {
				NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_FORMAL_PARAMETER, identifier, this);
				return;
			}

			if (Type_type.TYPE_PORT.equals(lastType.getTypetype())) {
				switch (realAssignmentType) {
				case A_PAR_PORT:
					break;
				case A_PAR_VAL:
				case A_PAR_VAL_INOUT:
					realAssignmentType = Assignment_type.A_PAR_PORT;
					break;
				default:
					location.reportSemanticError(MessageFormat.format(PORTTYPENOTALLOWEDAS, getAssignmentName()));
					break;
				}
			} else if (Type_type.TYPE_SIGNATURE.equals(lastType.getTypetype())) {
				switch (realAssignmentType) {
				case A_PAR_TEMP_IN:
				case A_PAR_TEMP_OUT:
				case A_PAR_TEMP_INOUT:
					break;
				default:
					location.reportSemanticError(MessageFormat.format(SIGNATURENOTALLOWEDAS, getAssignmentName()));
					break;
				}
			}
		}

		if (defaultValue != null) {
			actualDefaultParameter = checkActualParameter(timestamp, defaultValue, Expected_Value_type.EXPECTED_STATIC_VALUE);
			if (actualDefaultParameter != null) {
				actualDefaultParameter.setFullNameParent(this);
				actualDefaultParameter.setLocation(defaultValue.getLocation());
			}
		}

		// in this case the naming convention has to be checked at the
		// end, when the kind of the assignment is already known
		// exactly.
		NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_FORMAL_PARAMETER, identifier, this);
	}

	@Override
	public void postCheck() {
		if (!isUsed) {
			location.reportConfigurableSemanticProblem(getUnusedLocalDefinitionSeverity(),
					MessageFormat.format(LOCALLY_UNUSED, getDescription()));
		}
		if (!wasAssigned) {
			switch (getAssignmentType()) {
			case A_PAR_VAL:
			case A_PAR_VAL_IN:
			case A_PAR_TEMP_IN:
			case A_PAR_TIMER:
			case A_PORT:
				// can never produce this kind of problem.
				break;
			default:
			{
				final String message = MessageFormat.format(
						"The {0} seems to be never written, maybe it could be an `in'' parameter", getDescription());
				location.reportConfigurableSemanticProblem(
						Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.REPORTREADONLY, GeneralConstants.WARNING, null), message);
				break;
			}
			}

		}
	}

	/**
	 * @return whether this formal parameter was used as an LValue.
	 * */
	public boolean getWritten() {
		return wasAssigned;
	}

	/**
	 * Indicates that this formal parameter was used in a way where its
	 * value can be changed.
	 * */
	public void setWritten() {
		wasAssigned = true;
	}

	/**
	 * Checks if the actual parameter paired with this formal parameter is
	 * semantically correct.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param parameter
	 *                the template instance assigned as actual parameter to
	 *                this formal parameter
	 * @param expectedValue
	 *                the value kind expected from the actual parameter.
	 *
	 * @return the actual parameter made from the provided parameter.
	 * */
	public ActualParameter checkActualParameter(final CompilationTimeStamp timestamp, final TemplateInstance parameter,
			final Expected_Value_type expectedValue) {
		check(timestamp);

		switch (realAssignmentType) {
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
			return checkActualParameterValue(timestamp, parameter, expectedValue);
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
			return checkActualParameterByReference(timestamp, parameter, false, expectedValue);
		case A_PAR_TEMP_IN:
			return checkActualParameterTemplate(timestamp, parameter);
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT:
			return checkActualParameterByReference(timestamp, parameter, true, expectedValue);
		case A_PAR_TIMER:
			return checkActualParameterTimer(timestamp, parameter, expectedValue);
		case A_PAR_PORT:
			return checkActualParameterPort(timestamp, parameter, expectedValue);
		default:
			break;
		}

		ActualParameter temp = new Value_ActualParameter(null);
		temp.setIsErroneous();
		return temp;
	}

	/**
	 * Checks if the actual parameter paired with this formal parameter is
	 * semantically correct as a value parameter.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param actualParameter
	 *                the template instance assigned as actual parameter to
	 *                this formal parameter
	 * @param expectedValue
	 *                the value kind expected from the actual parameter.
	 *
	 * @return the actual parameter made from the provided parameter.
	 * */
	private ActualParameter checkActualParameterValue(final CompilationTimeStamp timestamp, final TemplateInstance actualParameter,
			final Expected_Value_type expectedValue) {
		actualParameter.checkType(timestamp, type);
		Reference derivedReference = actualParameter.getDerivedReference();
		if (derivedReference != null) {
			actualParameter.checkDerivedReference(timestamp, type);
		}

		ITTCN3Template template = actualParameter.getTemplateBody();
		if (template.isValue(timestamp) && type != null) {
			IValue value = template.getValue();
			value.setMyGovernor(type);
			IValue temp = type.checkThisValueRef(timestamp, value);
			if(!Value_type.NOTUSED_VALUE.equals(temp.getValuetype())) {
			  type.checkThisValue(timestamp, temp, new ValueCheckingOptions(expectedValue, false, false, true, false, false));
			}
			return new Value_ActualParameter(temp);
		}

		actualParameter.getLocation().reportSemanticError(MessageFormat.format(SPECIFICVALUEXPECTED, getAssignmentName()));
		ActualParameter temp = new Value_ActualParameter(null);
		temp.setIsErroneous();
		return temp;
	}

	private ActualParameter checkActualParameterTemplate(final CompilationTimeStamp timestamp, final TemplateInstance actualParameter) {
		actualParameter.check(timestamp, type);
		TemplateInstance instance = new TemplateInstance(actualParameter.getType(), actualParameter.getDerivedReference(),
				actualParameter.getTemplateBody());
		ActualParameter returnValue = new Template_ActualParameter(instance);

		if (!Restriction_type.TR_NONE.equals(templateRestriction)) {
			instance.checkRestriction(timestamp, this);
		}

		return returnValue;
	}

	/**
	 * Checks if the actual parameter paired with this formal parameter is
	 * semantically correct as a timer parameter.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param actualParameter
	 *                the template instance assigned as actual parameter to
	 *                this formal parameter
	 * @param expectedValue
	 *                the value kind expected from the actual parameter.
	 *
	 * @return the actual parameter created from the value, or null if there
	 *         was an error.
	 * */
	private ActualParameter checkActualParameterTimer(final CompilationTimeStamp timestamp, final TemplateInstance actualParameter,
			final Expected_Value_type expectedValue) {
		IType parameterType = actualParameter.getType();
		if (parameterType != null) {
			actualParameter.getLocation().reportSemanticError(EXPLICITESPECIFICATIONFORTIMER);
			actualParameter.checkType(timestamp, null);
		}

		Reference derivedReference = actualParameter.getDerivedReference();
		if (derivedReference != null) {
			derivedReference.getLocation().reportSemanticError(INLINETEMPLATEFORTIMER);
			actualParameter.checkDerivedReference(timestamp, null);
		}

		ITTCN3Template template = actualParameter.getTemplateBody();
		if (Template_type.SPECIFIC_VALUE.equals(template.getTemplatetype()) && ((SpecificValue_Template) template).isReference()) {
			Reference reference = ((SpecificValue_Template) template).getReference();
			Assignment assignment = reference.getRefdAssignment(timestamp, true);

			if (assignment == null) {
				ActualParameter temp = new Value_ActualParameter(null);
				temp.setIsErroneous();
				return temp;
			}

			switch (assignment.getAssignmentType()) {
			case A_TIMER:
				ArrayDimensions dimensions = ((Def_Timer) assignment).getDimensions();
				if (dimensions != null) {
					dimensions.checkIndices(timestamp, reference, "timer", false, expectedValue);
				} else if (reference.getSubreferences().size() > 1) {
					reference.getLocation().reportSemanticError(
							MessageFormat.format(SUBREFERENCEERROR1, assignment.getDescription()));
				}
				break;
			case A_PAR_TIMER:
				if (reference.getSubreferences().size() > 1) {
					reference.getLocation().reportSemanticError(
							MessageFormat.format(SUBREFERENCEERROR2, assignment.getDescription()));
				}
				break;
			default:
				reference.getLocation().reportSemanticError(MessageFormat.format(TIMEREXPECTED1, assignment.getAssignmentName()));
				break;
			}

			return new Referenced_ActualParameter(reference);
		}

		actualParameter.getLocation().reportSemanticError(TIMEREXPECTED2);
		ActualParameter temp = new Value_ActualParameter(null);
		temp.setIsErroneous();
		return temp;
	}

	/**
	 * Checks if the actual parameter paired with this formal parameter is
	 * semantically correct as a port parameter.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param actualParameter
	 *                the template instance assigned as actual parameter to
	 *                this formal parameter
	 * @param expectedValue
	 *                the value kind expected from the actual parameter.
	 *
	 * @return the actual parameter created from the value, or null if there
	 *         was an error.
	 * */
	private ActualParameter checkActualParameterPort(final CompilationTimeStamp timestamp, final TemplateInstance actualParameter,
			final Expected_Value_type expectedValue) {
		Type parameterType = actualParameter.getType();
		if (parameterType != null) {
			parameterType.getLocation().reportSemanticWarning("Explicit type specification is useless for a port parameter");
			actualParameter.checkType(timestamp, type);
		}

		Reference derivedReference = actualParameter.getDerivedReference();
		if (derivedReference != null) {
			derivedReference.getLocation().reportSemanticError("An in-line modified temlate cannot be used as port parameter");
			actualParameter.checkDerivedReference(timestamp, type);
		}

		ITTCN3Template parameterTemplate = actualParameter.getTemplateBody();
		if (!(parameterTemplate instanceof SpecificValue_Template) || !((SpecificValue_Template) parameterTemplate).isReference()) {
			actualParameter.getLocation().reportSemanticError("Reference to a port or port parameter was expected for a port parameter");
			ActualParameter temp = new Value_ActualParameter(null);
			temp.setIsErroneous();
			return temp;
		}

		Reference reference = ((SpecificValue_Template) parameterTemplate).getReference();
		Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			ActualParameter temp = new Value_ActualParameter(null);
			temp.setIsErroneous();
			return temp;
		}

		Type referredType;
		switch (assignment.getAssignmentType()) {
		case A_PORT:
			ArrayDimensions dimensions = ((Def_Port) assignment).getDimensions();
			if (dimensions != null) {
				dimensions.checkIndices(timestamp, reference, "port", false, expectedValue);
			} else if (reference.getSubreferences().size() > 1) {
				reference.getLocation().reportSemanticError(MessageFormat.format(SUBREFERENCEERROR1, assignment.getDescription()));
			}
			referredType = ((Def_Port) assignment).getType(timestamp);
			break;
		case A_PAR_PORT:
			if (reference.getSubreferences().size() > 1) {
				reference.getLocation().reportSemanticError(MessageFormat.format(SUBREFERENCEERROR3, assignment.getDescription()));
			}
			referredType = ((FormalParameter) assignment).getType(timestamp);
			break;
		default:
			reference.getLocation().reportSemanticError(MessageFormat.format(PORTEXPECTED, assignment.getDescription()));
			ActualParameter temp = new Value_ActualParameter(null);
			temp.setIsErroneous();
			return temp;
		}

		if (referredType != null && type != null && !type.isIdentical(timestamp, referredType)) {
			reference.getLocation().reportSemanticError(
					MessageFormat.format(TYPEMISMATCH, type.getTypename(), referredType.getTypename()));
		}

		return new Referenced_ActualParameter(reference);
	}

	/**
	 * Checks if the actual parameter paired with this formal parameter is
	 * semantically correct as a reference parameter.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param parameter
	 *                the template instance assigned as actual parameter to
	 *                this formal parameter
	 * @param isTemplate
	 *                true if the formal parameter is template, false
	 *                otherwise
	 * @param expectedValue
	 *                the value kind expected from the actual parameter.
	 *
	 * @return the actual parameter created from the value, or null if there
	 *         was an error.
	 * */
	private ActualParameter checkActualParameterByReference(final CompilationTimeStamp timestamp, final TemplateInstance parameter,
			final boolean isTemplate, final Expected_Value_type expectedValue) {
		Type parameterType = parameter.getType();
		if (parameterType != null) {
			parameterType.getLocation().reportSemanticWarning(
					MessageFormat.format(EXPLICITESPECIFICATIONFORREFERENCE, getAssignmentName()));
			parameter.checkType(timestamp, type);
		}

		Reference derivedReference = parameter.getDerivedReference();
		if (derivedReference != null) {
			derivedReference.getLocation().reportSemanticError(MessageFormat.format(INLINETEMPLATEFORREFERENCE, getAssignmentName()));
			parameter.checkDerivedReference(timestamp, type);
		}

		String expectedString;
		if (isTemplate) {
			expectedString = "template variable or template parameter";
		} else {
			expectedString = "variable or value parameter";
		}

		ITTCN3Template template = parameter.getTemplateBody();
		if (Template_type.SPECIFIC_VALUE.equals(template.getTemplatetype()) && ((SpecificValue_Template) template).isReference()) {
			Reference reference = ((SpecificValue_Template) template).getReference();
			reference.setUsedOnLeftHandSide();
			Assignment assignment = reference.getRefdAssignment(timestamp, true);
			if (assignment == null) {
				ActualParameter temp = new Value_ActualParameter(null);
				temp.setIsErroneous();
				return temp;
			}

			boolean assignmentTypeIsCorrect;
			switch (assignment.getAssignmentType()) {
			case A_PAR_VAL_IN:
				((FormalParameter) assignment).useAsLValue(reference);
				assignmentTypeIsCorrect = !isTemplate;
				break;
			case A_PAR_VAL:
			case A_PAR_VAL_OUT:
			case A_PAR_VAL_INOUT:
				((FormalParameter) assignment).setWritten();
				assignmentTypeIsCorrect = !isTemplate;
				break;
			case A_VAR:
				((Def_Var) assignment).setWritten();
				assignmentTypeIsCorrect = !isTemplate;
				break;
			case A_PAR_TEMP_IN:
				assignmentTypeIsCorrect = isTemplate;
				((FormalParameter) assignment).useAsLValue(reference);
				break;
			case A_PAR_TEMP_OUT:
			case A_PAR_TEMP_INOUT:
				((FormalParameter) assignment).setWritten();
				assignmentTypeIsCorrect = isTemplate;
				break;
			case A_VAR_TEMPLATE:
				((Def_Var_Template) assignment).setWritten();
				assignmentTypeIsCorrect = isTemplate;
				break;
			default:
				assignmentTypeIsCorrect = false;
				break;
			}

			if (assignmentTypeIsCorrect) {
				IType fieldType = assignment.getType(timestamp).getFieldType(timestamp, reference, 1,
						Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
				if (fieldType != null) {
					if (type != null && !type.isIdentical(timestamp, fieldType)) {
						reference.getLocation().reportSemanticError(
								MessageFormat.format(TYPEMISMATCH2, expectedString, type.getTypename(),
										fieldType.getTypename()));
					} else if (type != null && type.getSubtype() != null && fieldType.getSubtype() != null
							&& !type.getSubtype().isCompatible(timestamp, fieldType.getSubtype())) {
						reference.getLocation().reportSemanticError(
								MessageFormat.format(SUBTYPEMISMATCH, type.getSubtype().toString(), fieldType
										.getSubtype().toString()));
					}
					if (!reference.getSubreferences().isEmpty() && reference.refersToStringElement()) {
						reference.getLocation().reportSemanticError(
								MessageFormat.format(REFERENCEEXPECTED3, fieldType.getTypename()));
					}
				}
			} else {
				reference.getLocation().reportSemanticError(
						MessageFormat.format(REFERENCEEXPECTED1, expectedString, getAssignmentName(),
								assignment.getDescription()));
			}

			ActualParameter returnActualParameter = new Referenced_ActualParameter(reference);

			if (isTemplate && assignmentTypeIsCorrect) {
				TemplateRestriction.Restriction_type refdRestriction;
				switch (assignment.getAssignmentType()) {
				case A_VAR_TEMPLATE: {
					Def_Var_Template temp = (Def_Var_Template) assignment;
					refdRestriction = temp.getTemplateRestriction();
					break;
				}
				case A_PAR_TEMP_IN:
				case A_PAR_TEMP_OUT:
				case A_PAR_TEMP_INOUT: {
					FormalParameter par = (FormalParameter) assignment;
					refdRestriction = par.getTemplateRestriction();
					break;
				}
				default:
					return returnActualParameter;
				}

				TemplateRestriction.getSubRestriction(refdRestriction, timestamp, reference);
				if (templateRestriction != refdRestriction) {
					boolean preCallCheck = TemplateRestriction.isLessRestrictive(templateRestriction, refdRestriction);
					boolean postCallCheck = TemplateRestriction.isLessRestrictive(refdRestriction, templateRestriction);
					if (preCallCheck || postCallCheck) {
						final String message = MessageFormat
								.format("Inadequate restriction on the referenced {0} `{1}'' this may cause a dynamic test case error at runtime",
										assignment.getAssignmentName(), reference.getDisplayName());
						reference.getLocation().reportSemanticWarning(message);
					}
				}

				// for out and inout template parameters of
				// external functions
				// always check because we do not trust user
				// written C++ code
				if (!Restriction_type.TR_NONE.equals(refdRestriction)) {
					switch (myParameterList.getMyDefinition().getAssignmentType()) {
					case A_EXT_FUNCTION:
					case A_EXT_FUNCTION_RVAL:
					case A_EXT_FUNCTION_RTEMP:
						// until code generation we
						// should not set this value
						// here
						break;
					default:
						break;
					}
				}
			}

			return returnActualParameter;
		}

		parameter.getLocation().reportSemanticError(MessageFormat.format(REFERENCEEXPECTED2, expectedString, getAssignmentName()));
		ActualParameter temp = new Value_ActualParameter(null);
		temp.setIsErroneous();
		return temp;
	}

	/**
	 * Creates a representation of this formal parameter for use as the part
	 * of the description of a proposal.
	 *
	 * @param builder
	 *                the StringBuilder to append the representation to.
	 * @return the StringBuilder after appending the representation.
	 * */
	public StringBuilder getAsProposalDesriptionPart(final StringBuilder builder) {
		switch (realAssignmentType) {
		case A_PAR_TEMP_IN:
		case A_PAR_VAL_IN:
			builder.append("in ");
			break;
		case A_PAR_TEMP_OUT:
		case A_PAR_VAL_OUT:
			builder.append("out ");
			break;
		case A_PAR_TEMP_INOUT:
		case A_PAR_VAL_INOUT:
			builder.append("inout ");
			break;
		case A_PAR_TIMER:
			builder.append("timer");
			break;
		default:
			break;
		}

		return builder.append(identifier.getDisplayName());
	}

	/**
	 * Creates a representation of this formal parameter for use as a
	 * proposal part.
	 *
	 * @param builder
	 *                the StringBuilder to append the representation to.
	 * @return the StringBuilder after appending the representation.
	 * */
	public StringBuilder getAsProposalPart(final StringBuilder builder) {
		return builder.append("${").append(identifier.getDisplayName()).append('}');
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= i) {
			return;
		}

		if (subrefs.size() == i + 1 && identifier.getName().toLowerCase().startsWith(subrefs.get(i).getId().getName().toLowerCase())) {
			super.addProposal(propCollector, i);
		} else if (subrefs.size() > i + 1 && type != null && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match
			type.addProposal(propCollector, i + 1);
		}
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() > i && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			if (subrefs.size() > i + 1 && type != null) {
				type.addDeclaration(declarationCollector, i + 1);
			} else if (subrefs.size() == i + 1 && Subreference_type.fieldSubReference.equals(subrefs.get(i).getReferenceType())) {
				declarationCollector.addDeclaration(this);
			}
		}
	}

	@Override
	public TemplateRestriction.Restriction_type getTemplateRestriction() {
		return templateRestriction;
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (defaultValue == null) {
			List<Integer> result = new ArrayList<Integer>();
			result.add(Ttcn3Lexer.ASSIGNMENTCHAR);
			return result;
		}

		return ReparseUtilities.getAllValidTokenTypes();
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(identifier.getLocation());
		if (type != null) {
			type.updateSyntax(reparser, false);
			reparser.updateLocation(type.getLocation());
		}

		if (defaultValue != null) {
			defaultValue.updateSyntax(reparser, false);
			reparser.updateLocation(defaultValue.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
		if (defaultValue != null) {
			defaultValue.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (type != null && !type.accept(v)) {
			return false;
		}
		if (defaultValue != null && !defaultValue.accept(v)) {
			return false;
		}
		return true;
	}
}
