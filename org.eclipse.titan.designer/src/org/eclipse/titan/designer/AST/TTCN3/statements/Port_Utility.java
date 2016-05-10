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

import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.PortReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Module.module_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TypeFactory;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody.PortType_type;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ValueofExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class Port_Utility {
	private static final String VALUEREDIRECTTYPEMISSMATCH = "Type missmatch in value redirect:"
			+ " A variable of type `{0}'' was expected instead of `{1}''";
	private static final String PORTINCONTROLPART = "Port operation is not allowed in the control part";
	private static final String PORTREFERENCEEXPECTED = "Reference to a port or port parameter was expected instead of {0}";
	private static final String SIGNATUREEXPECTED1 = "Reference to a signature was expceted instead of {0}";
	private static final String SIGNATUREEXPECTED2 = "Reference to a signature was expected instead of port type `{0}''";
	private static final String SIGNATUREEXPECTED3 = "Reference to a signature was expected instead of data type `{0}''";
	private static final String COMPONENTOPINCONTROLPART = "Component operation is not allowed in the control part";
	private static final String NULLCOMPONENTREFERENCE = "The `null'' component reference shall not be used in a {0} operation";
	private static final String VALUERETURNEXPECTED = "A component reference was expected as return value";
	private static final String MTCCOMPONENTREFERENCE = "The `mtc'' component reference shall not be used in a {0} operation";
	private static final String SYSTEMCOMPONENTREFERENCE = "The `system'' component reference shall not be used in a {0} operation";
	private static final String COMPONENTREFERENCEEXPECTED = "A component reference was expected as operand";
	private static final String COMPONENTTYPEMISMATCH = "Type mismatch: The type of the operand should be a component type instead of `{0}''";
	private static final String NOPORTWITHNAME = "Component type `{0}'' does not have a port with name `{1}''";
	private static final String DEFINITIONNOTPORT = "Definition `{0}'' in component type `{1}'' is a {2} and not a port";

	/** private constructor to disable instantiation */
	private Port_Utility() {
	}

	/**
	 * Checks a port reference.
	 *
	 * @param timestamp
	 *                the timestamp of the actual build cycle.
	 * @param source
	 *                the source statement of the reference (for reporting
	 *                an error to)
	 * @param portReference
	 *                the port reference to be checked
	 *
	 * @return the port type of the reference if it is a correct port
	 *         reference, or null otherwise
	 * */
	public static Port_Type checkPortReference(final CompilationTimeStamp timestamp, final Statement source, final Reference portReference) {
		if (source.getMyStatementBlock() != null && source.getMyStatementBlock().getMyDefinition() == null) {
			source.getLocation().reportSemanticError(PORTINCONTROLPART);
		}

		if (portReference == null) {
			return null;
		}

		final Assignment assignment = portReference.getRefdAssignment(timestamp, true);
		if (assignment == null || assignment.getIsErroneous()) {
			return null;
		}

		IType result = null;
		switch (assignment.getAssignmentType()) {
		case A_PORT:
			final ArrayDimensions dimensions = ((Def_Port) assignment).getDimensions();
			if (dimensions != null) {
				dimensions.checkIndices(timestamp, portReference, "port", false, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			} else if (portReference.getSubreferences().size() > 1) {
				portReference.getLocation().reportSemanticError(
						MessageFormat.format("Reference to single {0} cannot have field or array sub-references",
								assignment.getDescription()));
			}
			result = ((Def_Port) assignment).getType(timestamp);
			break;
		case A_PAR_PORT:
			if (portReference.getSubreferences().size() > 1) {
				portReference.getLocation().reportSemanticError(
						MessageFormat.format("Reference to {0} cannot have field or array sub-references",
								assignment.getDescription()));
			}
			result = ((FormalParameter) assignment).getType(timestamp);
			break;
		default:
			portReference.getLocation().reportSemanticError(MessageFormat.format(PORTREFERENCEEXPECTED, assignment.getAssignmentName()));
			break;
		}

		if (result == null) {
			return null;
		}

		result = result.getTypeRefdLast(timestamp);
		if (Type_type.TYPE_PORT.equals(result.getTypetype())) {
			return (Port_Type) result;
		}

		return null;
	}

	/**
	 * Checks a reference to see if it really references a valid signature
	 * type.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference to check.
	 *
	 * @return the referenced signature type, or null if there were
	 *         problems.
	 * */
	public static Signature_Type checkSignatureReference(final CompilationTimeStamp timestamp, final Reference reference) {
		if (reference == null) {
			return null;
		}

		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return null;
		}

		if (!Assignment_type.A_TYPE.equals(assignment.getAssignmentType())) {
			reference.getLocation().reportSemanticError(MessageFormat.format(SIGNATUREEXPECTED1, assignment.getAssignmentName()));
			return null;
		}

		IType result = ((Def_Type) assignment).getType(timestamp);
		if (result == null) {
			return null;
		}

		result = result.getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		if (result == null) {
			return null;
		}

		result = result.getTypeRefdLast(timestamp);
		switch (result.getTypetype()) {
		case TYPE_SIGNATURE:
			return (Signature_Type) result;
		case TYPE_PORT:
			reference.getLocation().reportSemanticError(MessageFormat.format(SIGNATUREEXPECTED2, result.getTypename()));
			return null;
		default:
			reference.getLocation().reportSemanticError(MessageFormat.format(SIGNATUREEXPECTED3, result.getTypename()));
			return null;
		}
	}

	/**
	 * Checks a reference to see if it really references a valid component
	 * type.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param source
	 *                the source statement to report errors to.
	 * @param value
	 *                the value reference to check.
	 * @param allowMtc
	 *                tells if the mtc component is allowed or not.
	 * @param allowSystem
	 *                tells if the system component is allowed or not.
	 *
	 * @return the referenced component type, or null if there were
	 *         problems.
	 * */
	public static Component_Type checkComponentReference(final CompilationTimeStamp timestamp, final Statement source, final IValue value,
			final boolean allowMtc, final boolean allowSystem) {
		if (source.getMyStatementBlock() != null && source.getMyStatementBlock().getMyDefinition() == null) {
			source.getLocation().reportSemanticError(COMPONENTOPINCONTROLPART);
		}

		if (value == null || value.getIsErroneous(timestamp)) {
			return null;
		}

		final IValue last = value.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
		switch (last.getValuetype()) {
		case REFERENCED_VALUE:
			break;
		case TTCN3_NULL_VALUE:
			value.getLocation().reportSemanticError(MessageFormat.format(NULLCOMPONENTREFERENCE, source.getStatementName()));
			break;
		case EXPRESSION_VALUE:
			final Expression_Value expression = (Expression_Value) last;
			switch (expression.getOperationType()) {
			case APPLY_OPERATION:
				if (!Type_type.TYPE_COMPONENT.equals(last.getExpressionReturntype(timestamp,
						Expected_Value_type.EXPECTED_DYNAMIC_VALUE))) {
					value.getLocation().reportSemanticError(VALUERETURNEXPECTED);
				}
				break;
			case COMPONENT_NULL_OPERATION:
				value.getLocation().reportSemanticError(MessageFormat.format(NULLCOMPONENTREFERENCE, source.getStatementName()));
				break;
			case MTC_COMPONENT_OPERATION:
				if (!allowMtc) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(MTCCOMPONENTREFERENCE, source.getStatementName()));
				}
				break;
			case SYSTEM_COMPONENT_OPERATION:
				if (!allowSystem) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(SYSTEMCOMPONENTREFERENCE, source.getStatementName()));
				}
				break;
			case SELF_COMPONENT_OPERATION:
				break;
			case COMPONENT_CREATE_OPERATION:
				break;
			case VALUEOF_OPERATION:
				final IReferenceChain referenceChain = ReferenceChain.getInstance(
								IReferenceChain.CIRCULARREFERENCE, true);
				((ValueofExpression) expression).evaluateValue(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, referenceChain);
				referenceChain.release();
				break;
			default:
				value.getLocation().reportSemanticError(COMPONENTREFERENCEEXPECTED);
				return null;
			}
			break;
		default:
			value.getLocation().reportSemanticError(COMPONENTREFERENCEEXPECTED);
			return null;
		}

		IType result = value.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		if (result == null) {
			return null;
		}
		result = result.getTypeRefdLast(timestamp);

		if (result.getIsErroneous(timestamp)) {
			return null;
		} else if (Type_type.TYPE_COMPONENT.equals(result.getTypetype())) {
			return (Component_Type) result;
		}

		value.getLocation().reportSemanticError(MessageFormat.format(COMPONENTTYPEMISMATCH, result.getTypename()));
		return null;
	}

	/**
	 * Checks a reference to see if it really references a valid component
	 * type.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param source
	 *                the statement to report an error to, in case it is in
	 *                a control part.
	 * @param componentReference
	 *                the reference of the component to be checked.
	 * @param portReference
	 *                the reference to a port of the component to be
	 *                checked.
	 * @param allowSystem
	 *                tells if the system component should be allowed or not
	 *                as an endpoint.
	 *
	 * @return the referenced component type, or null if there were
	 *         problems.
	 * */
	public static IType checkConnectionEndpoint(final CompilationTimeStamp timestamp, final Statement source, final Value componentReference,
			final PortReference portReference, final boolean allowSystem) {
		final IType componentType = checkComponentReference(timestamp, source, componentReference, true, allowSystem);
		if (portReference == null) {
			return componentType;
		}

		if (componentType == null) {
			// the component type can not be determined
			final List<ISubReference> subreferences = portReference.getSubreferences();
			if (subreferences.size() > 1) {
				// check array indices
				for (int i = 0; i < subreferences.size(); i++) {
					final ISubReference subreference = subreferences.get(i);
					if (subreference instanceof ArraySubReference) {
						final Value value = ((ArraySubReference) subreference).getValue();
						value.setLoweridToReference(timestamp);
						final Type_type temporalType1 = value.getExpressionReturntype(timestamp,
								Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

						switch (temporalType1) {
						case TYPE_INTEGER: {
							final IReferenceChain referenceChain = ReferenceChain.getInstance(
									IReferenceChain.CIRCULARREFERENCE, true);
							final IValue last1 = value.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE,
									referenceChain);
							referenceChain.release();
							if (!last1.isUnfoldable(timestamp)
									&& Value.Value_type.INTEGER_VALUE.equals(last1.getValuetype())) {
								if (((Integer_Value) last1).signum() < 0) {
									value.getLocation().reportSemanticError(
											ArraySubReference.NATIVEINTEGEREXPECTED);
									value.setIsErroneous(true);
								}
							}
							break;
						}
						case TYPE_UNDEFINED:
							value.setIsErroneous(true);
							break;
						default:
							if (!value.getIsErroneous(timestamp)) {
								value.getLocation().reportSemanticError(ArraySubReference.INTEGERINDEXEXPECTED);
								value.setIsErroneous(true);
							}
							break;
						}
					}
				}
			}

			return null;
		}

		final ComponentTypeBody componentBody = ((Component_Type) componentType).getComponentBody();
		portReference.setBaseScope(componentBody);
		portReference.setComponent((Component_Type) componentType);
		final Identifier portIdentifier = portReference.getId();

		if (!componentBody.hasLocalAssignmentWithId(portIdentifier)) {
			portReference.getLocation().reportSemanticError(
					MessageFormat.format(NOPORTWITHNAME, componentType.getTypename(), portIdentifier.getDisplayName()));
			return null;
		}

		final Assignment assignment = componentBody.getLocalAssignmentById(portIdentifier);
		if (assignment != null && !Assignment_type.A_PORT.equals(assignment.getAssignmentType())) {
			portReference.getLocation().reportSemanticError(
					MessageFormat.format(DEFINITIONNOTPORT, portIdentifier.getDisplayName(), componentType.getTypename(),
							assignment.getAssignmentName()));
			return null;
		}

		final ArrayDimensions dimensions = ((Def_Port) assignment).getDimensions();
		if (dimensions != null) {
			dimensions.checkIndices(timestamp, portReference, "port", false, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		} else if (portReference.getSubreferences().size() > 1) {
			portReference.getLocation().reportSemanticError(
					MessageFormat.format("Port `{0}'' is not an array. The reference cannot have field or array sub-references",
							portIdentifier.getDisplayName()));
		}

		Port_Type portType = ((Def_Port) assignment).getType(timestamp);
		if (portType != null) {
			final PortTypeBody portBody = portType.getPortBody();
			if (PortType_type.PT_USER.equals(portBody.getPortType())) {
				final IType providerType = portBody.getProviderType();
				if (providerType instanceof Port_Type) {
					portType = (Port_Type) providerType;
				}
			}
		}

		return portType;
	}

	/**
	 * Checks a sender redirect to see if it really references a valid
	 * component type.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param addressType
	 *                the type of address, this must be identical to the
	 *                type of the sender redirect target.
	 * @param redirectSender
	 *                the sender redirect to check.
	 *
	 * @return the referenced component type, or null if there were
	 *         problems.
	 * */
	public static IType checkSenderRedirect(final CompilationTimeStamp timestamp, final IType addressType, final Reference redirectSender) {
		if (redirectSender == null) {
			return null;
		}

		final IType variableType = redirectSender.checkVariableReference(timestamp);
		if (variableType == null) {
			return null;
		}

		if (addressType == null || !addressType.isIdentical(timestamp, variableType)) {
			// the variableType must be a component type
			final IType last = variableType.getTypeRefdLast(timestamp);

			if (last.getIsErroneous(timestamp)) {
				return null;
			}

			if (!Type_type.TYPE_COMPONENT.equals(last.getTypetype())) {
				redirectSender.getLocation().reportSemanticError(
						MessageFormat.format("The type of the variable should be a component type {0}instead of `{1}''",
								(addressType == null) ? "" : "or the `address' type ", variableType.getTypename()));
				return null;
			}
		}

		return variableType;
	}

	/**
	 * Checks a from clause reference and a sender redirect to see if they
	 * really references valid component types.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param source
	 *                the source statement to report errors to.
	 * @param portType
	 *                the type of the port used in the statement. Used to
	 *                find the address type in effect.
	 * @param fromClause
	 *                the from clause to check
	 * @param redirectSender
	 *                the sender redirect to check.
	 * */
	public static void checkFromClause(final CompilationTimeStamp timestamp, final Statement source, final Port_Type portType,
			final TemplateInstance fromClause, final Reference redirectSender) {
		IType addressType = null;
		if (portType != null) {
			addressType = portType.getPortBody().getAddressType(timestamp);
		} else if (source != null && source.getMyStatementBlock() != null) {
			final Module module = source.getMyStatementBlock().getModuleScope();
			if (module != null && module_type.TTCN3_MODULE.equals(module.getModuletype())) {
				addressType = ((TTCN3Module) module).getAddressType(timestamp);
			}
		}

		boolean senderRedirectChecked = false;
		IType fromClauseType = null;
		if (fromClause != null) {
			fromClauseType = fromClause.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
			ITTCN3Template templateBody = fromClause.getTemplateBody();

			if (fromClauseType == null) {
				if (addressType != null) {
					templateBody = addressType.checkThisTemplateRef(timestamp, templateBody);
				} else {
					templateBody = templateBody.setLoweridToReference(timestamp);
				}
				fromClauseType = templateBody.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
			}

			if (fromClauseType == null) {
				fromClauseType = checkSenderRedirect(timestamp, addressType, redirectSender);
				senderRedirectChecked = true;
			}

			if (fromClauseType == null) {
				// trying to figure out whether the template is
				// a component reference or an SUT address
				boolean isComponentReference;
				if (Type_type.TYPE_COMPONENT.equals(templateBody.getExpressionReturntype(timestamp,
						Expected_Value_type.EXPECTED_TEMPLATE))) {
					isComponentReference = true;
				} else {
					switch (templateBody.getTemplatetype()) {
					case SPECIFIC_VALUE:
						// treat 'null' as component
						// reference
						isComponentReference = Value_type.TTCN3_NULL_VALUE.equals(((SpecificValue_Template) templateBody)
								.getSpecificValue().getValuetype());
						break;
					case ANY_VALUE:
					case ANY_OR_OMIT:
						isComponentReference = true;
						break;
					default:
						isComponentReference = false;
						break;
					}
				}

				if (isComponentReference) {
					// the argument is a component
					// reference: get a pool type
					fromClauseType = TypeFactory.createType(Type_type.TYPE_COMPONENT);
				} else if (addressType != null) {
					// the argument is not a component
					// reference: try the address type
					fromClauseType = addressType;
				}
			}

			if (fromClauseType != null) {
				fromClause.check(timestamp, fromClauseType);
				if (addressType == null || !addressType.isCompatible(timestamp, fromClauseType, null, null, null)) {
					// from_clause_type must be a component
					// type
					final IType last = fromClauseType.getTypeRefdLast(timestamp);

					if (last.getIsErroneous(timestamp)) {
						fromClauseType = null;
					} else if (Type_type.TYPE_COMPONENT.equals(last.getTypetype())) {
						if (Template_type.SPECIFIC_VALUE.equals(templateBody.getTemplatetype())) {
							checkComponentReference(timestamp, source,
									((SpecificValue_Template) templateBody).getSpecificValue(), true, true);
						}
					} else {
						final String message = MessageFormat.format(
								"The type of the template should be a component type {0} instead of `{1}''",
								(addressType == null) ? "" : "or the `address' type ", fromClauseType.getTypename());
						fromClause.getLocation().reportSemanticError(message);
						fromClauseType = null;
					}

				}
			} else {
				fromClause.getLocation().reportSemanticError("Cannot determine the type of the template");
			}
		}

		if (!senderRedirectChecked) {
			final IType senderRedirectType = checkSenderRedirect(timestamp, addressType, redirectSender);
			if (fromClauseType != null && senderRedirectType != null && !fromClauseType.isIdentical(timestamp, senderRedirectType)) {
				final String message = MessageFormat
						.format("The types in `from'' clause and `sender'' redirect are not the same: `{0}'' was expected instead of `{1}''",
								fromClauseType.getTypename(), senderRedirectType.getTypename());
				senderRedirectType.getLocation().reportSemanticError(message);
			}
		}
	}

	/**
	 * Checks a to clause reference to see if it really references valid
	 * component type.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param source
	 *                the source statement of this check.
	 * @param portType
	 *                the type of the port used in the statement. Used to
	 *                find the address type in effect.
	 * @param toClause
	 *                the to clause to check
	 * */
	public static void checkToClause(final CompilationTimeStamp timestamp, final Statement source, final Port_Type portType, final IValue toClause) {
		if (toClause == null) {
			return;
		}

		IType addressType = null;
		if (portType != null) {
			addressType = portType.getPortBody().getAddressType(timestamp);
		} else if (source != null) {
			final Module module = source.getMyStatementBlock().getModuleScope();
			if (module != null && module_type.TTCN3_MODULE.equals(module.getModuletype())) {
				addressType = ((TTCN3Module) module).getAddressType(timestamp);
			}
		}

		if (addressType == null) {
			checkComponentReference(timestamp, source, toClause, true, true);
		} else {
			// detect possible enumerated values (address may be an
			// enumerated type)
			final IValue temp = addressType.checkThisValueRef(timestamp, toClause);
			// try to figure out whether the argument is a component
			// reference or an SUT address
			boolean isAddress;
			final IType governor = temp.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (governor == null) {
				isAddress = !Type_type.TYPE_COMPONENT.equals(temp.getExpressionReturntype(timestamp,
						Expected_Value_type.EXPECTED_DYNAMIC_VALUE));
			} else {
				isAddress = addressType.isCompatible(timestamp, governor, null, null, null);
			}

			if (isAddress) {
				addressType.checkThisValue(timestamp, temp, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE,
						false, false, true, false, false));
			} else {
				checkComponentReference(timestamp, source, temp, true, true);
			}
		}
	}

	/**
	 * Calculates the type of a variable when it was to be used as a value
	 * redirect target.
	 *
	 * @param timestamp
	 *                the timestamp of the actual build cycle.
	 * @param redirectValue
	 *                the reference to which the redirection is targeted
	 * @param type
	 *                the expected type of the value redirection.
	 *
	 * @return the the type of a variable referenced when it was to be used
	 *         as a value redirection target.
	 * */
	public static IType checkValueRedirect(final CompilationTimeStamp timestamp, final Reference redirectValue, final IType type) {
		if (redirectValue == null) {
			return null;
		}

		final IType variableType = redirectValue.checkVariableReference(timestamp);
		if (type != null && variableType != null && !type.isCompatible(timestamp, variableType, null, null, null)) {
			redirectValue.getLocation().reportSemanticError(
					MessageFormat.format(VALUEREDIRECTTYPEMISSMATCH, type.getTypename(), variableType.getTypename()));
		}
		
		final Assignment assignment = redirectValue.getRefdAssignment(timestamp, true);
		if (assignment != null) {
			switch (assignment.getAssignmentType()) {
			case A_PAR_VAL:
			case A_PAR_VAL_OUT:
			case A_PAR_VAL_INOUT:
				((FormalParameter) assignment).setWritten();
				break;
			case A_VAR:
				((Def_Var) assignment).setWritten();
				break;
			case A_PAR_TEMP_OUT:
			case A_PAR_TEMP_INOUT:
				((FormalParameter) assignment).setWritten();
				break;
			case A_VAR_TEMPLATE:
				((Def_Var_Template) assignment).setWritten();
				break;
			default:
				break;
			}
		}

		return variableType;
	}

	/**
	 * Calculates the type of a template instance when it was to be used as
	 * a parameter of a receiving statement (receive / trigger /
	 * check-receive).
	 *
	 * @param timestamp
	 *                the timestamp of the actual build cycle.
	 * @param templateInstance
	 *                the template instance whose type needs to be
	 *                calculated.
	 * @param valueRedirect
	 *                the value redirect of the receiving statement to help
	 *                the identification of the type.
	 * @param valueRedirectChecked
	 *                after the function executed this will store whether
	 *                the execution has called the checking of value
	 *                redirect. This has to be an array of 1 in length.
	 *
	 * @return the the type of a template instance when it was to be used as
	 *         a parameter of a receiving statement
	 * */
	public static IType getIncomingType(final CompilationTimeStamp timestamp, final TemplateInstance templateInstance,
			final Reference valueRedirect, final boolean[] valueRedirectChecked) {
		IType result = templateInstance.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);

		if (result != null) {
			return result;
		}

		result = checkValueRedirect(timestamp, valueRedirect, null);
		valueRedirectChecked[0] = true;

		if (result != null) {
			return result;
		}

		final ITTCN3Template template = templateInstance.getTemplateBody();

		final ITTCN3Template temp = template.setLoweridToReference(timestamp);

		return temp.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
	}

	/**
	 * Calculates the type of a template instance when it was to be used as
	 * a parameter of a send statement.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check.
	 * @param templateInstance
	 *                the template instance whose type needs to be
	 *                calculated.
	 *
	 * @return the the type of a template instance when it was to be used as
	 *         a parameter of a sending statement
	 * */
	public static IType getOutgoingType(final CompilationTimeStamp timestamp, final TemplateInstance templateInstance) {
		final IType result = templateInstance.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);

		if (result != null) {
			return result.getTypeRefdLast(timestamp);
		}

		ITTCN3Template template = templateInstance.getTemplateBody();
		template = template.setLoweridToReference(timestamp);

		return template.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
	}
}
