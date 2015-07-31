/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * */
public final class ExpressionUtilities {
	private static final String UNDETERMINABLEOPERANDSERROR = "Cannot determine the type of the operands";
	private static final String INCOMPATIBLEOPERANDERROR = "The operands should be of compatible types";
	private static final String PLEASEUSEREFERENCES = "Please use references as operands";
	private static final String TYPECOMPATWARNING = "Type compatibility between `{0}'' and `{1}''";

	// The actual value of the severity level to report type compatibility
	// on.
	private static String typeCompatibilitySeverity;

	static {
		typeCompatibilitySeverity = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.REPORTTYPECOMPATIBILITY, GeneralConstants.WARNING, null);

		final Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					final String property = event.getProperty();
					if (PreferenceConstants.REPORTTYPECOMPATIBILITY.equals(property)) {
						typeCompatibilitySeverity = Platform.getPreferencesService().getString(
								ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.REPORTTYPECOMPATIBILITY,
								GeneralConstants.WARNING, null);
					}
				}
			});
		}
	}

	/** private constructor to disable instantiation */
	private ExpressionUtilities() {
	}

	/**
	 * Checks the compatibility of expression operands in cases where
	 * operands are compared (and one or both of them can be enumerations).
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expression
	 *                the original expression this check should be performed
	 *                on (used to report errors if there are any)
	 * @param referenceChain
	 *                an initialized reference chain to help detecting
	 *                circular references.
	 * @param expectedValue
	 *                the kind of the value to be expected.
	 * @param operand1
	 *                the first operand.
	 * @param operand2
	 *                the second operand.
	 * */
	public static void checkExpressionOperatorCompatibility(final CompilationTimeStamp timestamp, final Expression_Value expression,
			final IReferenceChain referenceChain, final Expected_Value_type expectedValue, final Value operand1, final Value operand2) {
		if (expression == null || operand1 == null || operand2 == null) {
			return;
		}

		operand1.setIsErroneous(false);
		operand2.setIsErroneous(false);

		// if there was no governor in the beginning there shall be no
		// governor in the end (but midway we need to set them)
		boolean governor1set = operand1.getMyGovernor() != null;
		boolean governor2set = operand2.getMyGovernor() != null;

		checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue, operand1, operand2);

		if (!governor1set) {
			operand1.setMyGovernor(null);
		}
		if (!governor2set) {
			operand2.setMyGovernor(null);
		}
	}

	/**
	 * Checks the compatibility of expression operands in cases where
	 * operands are compared (and one or both of them can be enumerations).
	 * This function shall be used to test operands of match()
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expression
	 *                the original expression this check should be performed
	 *                on (used to report errors if there are any)
	 * @param referenceChain
	 *                an initialized reference chain to help detecting
	 *                circular references.
	 * @param expectedValue
	 *                the kind of the value to be expected.
	 * @param operand1
	 *                the first operand.
	 * @param operand2
	 *                the second operand. It's type is TemplateInstance.
	 * */

	public static void checkExpressionOperatorCompatibility(final CompilationTimeStamp timestamp, final Expression_Value expression,
			final IReferenceChain referenceChain, final Expected_Value_type expectedValue, final Value operand1,
			final TemplateInstance operand2) {
		if (expression == null || operand1 == null || operand2 == null) {
			return;
		}

		operand1.setIsErroneous(false);
		operand2.getTemplateBody().setIsErroneous(false);

		// if there was no governor in the beginning there shall be no
		// governor in the end (but midway we need to set them)
		boolean governor1set = operand1.getMyGovernor() != null;
		boolean governor2set = operand2.getTemplateBody().getMyGovernor() != null;

		checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue, operand1, operand2);

		if (!governor1set) {
			operand1.setMyGovernor(null);
		}
		if (!governor2set) {
			operand2.getTemplateBody().setMyGovernor(null);
		}
	}

	/**
	 * Checks the compatibility of expression operands in cases where
	 * operands are compared (and one or both of them can be enumerations).
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expression
	 *                the original expression this check should be performed
	 *                on (used to report errors if there are any)
	 * @param referenceChain
	 *                an initialized reference chain to help detecting
	 *                circular references.
	 * @param expectedValue
	 *                the kind of the value to be expected.
	 * @param param1
	 *                the first operand.
	 * @param param2
	 *                the second operand.
	 * */
	private static void checkExpressionOperatorCompatibilityInternal(final CompilationTimeStamp timestamp, final Expression_Value expression,
			final IReferenceChain referenceChain, final Expected_Value_type expectedValue, final IValue param1, final IValue param2) {
		if (expression == null || param1 == null || param2 == null) {
			return;
		}

		if (param1.getIsErroneous(timestamp) || param2.getIsErroneous(timestamp)) {
			expression.setIsErroneous(true);
			return;
		}

		IValue operand1 = param1;
		IValue operand2 = param2;

		Type_type tempType1 = operand1.getExpressionReturntype(timestamp, expectedValue);
		Type_type tempType2 = operand2.getExpressionReturntype(timestamp, expectedValue);

		if (Type_type.TYPE_UNDEFINED.equals(tempType1)) {
			if (Type_type.TYPE_UNDEFINED.equals(tempType2)) {
				if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(operand1.getValuetype())) {
					if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(operand2.getValuetype())) {
						Scope scope = expression.getMyScope();
						Module module = scope.getModuleScope();
						Identifier identifier = ((Undefined_LowerIdentifier_Value) operand1).getIdentifier();
						if (scope.hasAssignmentWithId(timestamp, identifier)
								|| module.hasImportedAssignmentWithID(timestamp, identifier)) {
							operand1 = operand1.setLoweridToReference(timestamp);
							checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain,
									expectedValue, operand1, operand2);
							return;
						}

						Identifier identifier2 = ((Undefined_LowerIdentifier_Value) operand2).getIdentifier();
						if (scope.hasAssignmentWithId(timestamp, identifier2)
								|| module.hasImportedAssignmentWithID(timestamp, identifier2)) {
							operand2 = operand2.setLoweridToReference(timestamp);
							checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain,
									expectedValue, operand1, operand2);
							return;
						}

					} else {
						operand1 = operand1.setLoweridToReference(timestamp);
						checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue,
								operand1, operand2);
						return;
					}
				} else if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(operand2.getValuetype())) {
					operand2 = operand2.setLoweridToReference(timestamp);
					checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue, operand1,
							operand2);
					return;
				}

				if (operand1.getIsErroneous(timestamp) || operand2.getIsErroneous(timestamp)) {
					expression.setIsErroneous(true);
					return;
				}

				expression.getLocation().reportSemanticError(UNDETERMINABLEOPERANDSERROR);
				expression.setIsErroneous(true);
				return;
			}

			if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(operand1.getValuetype())
					&& !Type_type.TYPE_TTCN3_ENUMERATED.equals(tempType2)) {
				operand1 = operand1.setLoweridToReference(timestamp);
				checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue, operand1, operand2);
				return;
			}
		} else if (Type_type.TYPE_UNDEFINED.equals(tempType2)) {
			if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(operand2.getValuetype())
					&& !Type_type.TYPE_TTCN3_ENUMERATED.equals(tempType1)) {
				operand2 = operand2.setLoweridToReference(timestamp);
				checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue, operand1, operand2);
				return;
			}
		}

		IType type1 = operand1.getExpressionGovernor(timestamp, expectedValue);
		IType type2 = operand2.getExpressionGovernor(timestamp, expectedValue);

		if (operand1.getIsErroneous(timestamp) || operand2.getIsErroneous(timestamp)) {
			expression.setIsErroneous(true);
			return;
		}

		if (type1 != null) {
			if (type2 != null) {
				TypeCompatibilityInfo info1 = new TypeCompatibilityInfo(type1, type2, true);
				TypeCompatibilityInfo info2 = new TypeCompatibilityInfo(type2, type1, true);
				boolean retVal1 = type1.isCompatible(timestamp, type2, info1, null, null);
				boolean retVal2 = type2.isCompatible(timestamp, type1, info2, null, null);
				if (!retVal1 && !retVal2) {
					expression.getLocation().reportSemanticError(info1.toString());
					expression.setIsErroneous(true);
					return;
				}

				if (GeneralConstants.WARNING.equals(typeCompatibilitySeverity)) {
					if (info1.getNeedsConversion()) {
						expression.getLocation().reportSemanticWarning(
								MessageFormat.format(TYPECOMPATWARNING, type1.getTypename(), type2.getTypename()));
					} else if (info2.getNeedsConversion()) {
						expression.getLocation().reportSemanticWarning(
								MessageFormat.format(TYPECOMPATWARNING, type2.getTypename(), type1.getTypename()));
					}
				}
			} else {
				operand2.setMyGovernor(type1);
				IValue tempValue = type1.checkThisValueRef(timestamp, operand2);

				if (Value_type.OMIT_VALUE.equals(operand2.getValuetype())) {
					operand1.checkExpressionOmitComparison(timestamp, expectedValue);
				} else {
					type1.checkThisValue(timestamp, tempValue, new ValueCheckingOptions(expectedValue, false, false, false,
							false, false));
					checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue, operand1,
							tempValue);
					return;
				}
			}
		} else if (type2 != null) {
			operand1.setMyGovernor(type2);
			IValue tempValue = type2.checkThisValueRef(timestamp, operand1);
			if (Value_type.OMIT_VALUE.equals(operand1.getValuetype())) {
				operand2.checkExpressionOmitComparison(timestamp, expectedValue);
			} else {
				type2.checkThisValue(timestamp, tempValue, new ValueCheckingOptions(expectedValue, false, false, false, false, false));
				checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue, tempValue,
						operand2);
				return;
			}
		} else {
			if (Type_type.TYPE_UNDEFINED.equals(tempType1) || Type_type.TYPE_UNDEFINED.equals(tempType2)) {
				expression.getLocation().reportSemanticError(PLEASEUSEREFERENCES);
				expression.setIsErroneous(true);
				return;
			}

			if (!Type.isCompatible(timestamp, tempType1, tempType2, false, false)
					&& !Type.isCompatible(timestamp, tempType1, tempType2, false, false)) {
				expression.getLocation().reportSemanticError(INCOMPATIBLEOPERANDERROR);
				expression.setIsErroneous(true);
			}
		}
	}

	// the same as the previous but the last arg is template
	private static void checkExpressionOperatorCompatibilityInternal(final CompilationTimeStamp timestamp, final Expression_Value expression,
			final IReferenceChain referenceChain, final Expected_Value_type expectedValue, final IValue param1,
			final TemplateInstance param2) {
		if (expression == null || param1 == null || param2 == null) {
			return;
		}

		if (param1.getIsErroneous(timestamp) || param2.getTemplateBody().getIsErroneous(timestamp)) {
			expression.setIsErroneous(true);
			return;
		}

		IValue operand1 = param1;
		TemplateInstance operand2 = param2;

		Type_type tempType1 = operand1.getExpressionReturntype(timestamp, expectedValue);
		Type_type tempType2 = operand2.getExpressionReturntype(timestamp, expectedValue);
		ITTCN3Template temp2 = operand2.getTemplateBody();

		if (Type_type.TYPE_UNDEFINED.equals(tempType1)) {
			if (Type_type.TYPE_UNDEFINED.equals(tempType2)) {
				if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(operand1.getValuetype())) {

					if (Template_type.SPECIFIC_VALUE.equals(temp2.getTemplatetype())
							&& Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(((SpecificValue_Template) temp2)
									.getSpecificValue().getValuetype())) {
						Scope scope = expression.getMyScope();
						Module module = scope.getModuleScope();
						Identifier identifier = ((Undefined_LowerIdentifier_Value) operand1).getIdentifier();
						if (scope.hasAssignmentWithId(timestamp, identifier)
								|| module.hasImportedAssignmentWithID(timestamp, identifier)) {
							operand1 = operand1.setLoweridToReference(timestamp);
							checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain,
									expectedValue, operand1, operand2);
							return;
						}

						Identifier identifier2 = ((Undefined_LowerIdentifier_Value) ((SpecificValue_Template) temp2)
								.getSpecificValue()).getIdentifier();
						if (scope.hasAssignmentWithId(timestamp, identifier2)
								|| module.hasImportedAssignmentWithID(timestamp, identifier2)) {
							temp2 = temp2.setLoweridToReference(timestamp);
							checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain,
									expectedValue, operand1, operand2);
							return;
						}

					} else {
						operand1 = operand1.setLoweridToReference(timestamp);
						checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue,
								operand1, operand2);
						return;
					}
				} else if ( Template_type.SPECIFIC_VALUE.equals(temp2.getTemplatetype())
						&& Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(((SpecificValue_Template) temp2)
								.getSpecificValue().getValuetype())) {
					temp2 = temp2.setLoweridToReference(timestamp);
					
					//To avoid infinite loop:
					TemplateInstance tempTemplateInstance2 = new TemplateInstance(operand2.getType(),
							operand2.getDerivedReference(), (TTCN3Template) temp2);
					
					if (operand2 == tempTemplateInstance2) {
						return;
					}
					
					checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue, operand1,
							tempTemplateInstance2);
					return;
				}

				// else

				if (operand1.getIsErroneous(timestamp) || temp2.getIsErroneous(timestamp)) {
					expression.setIsErroneous(true);
					return;
				}

				expression.getLocation().reportSemanticError(UNDETERMINABLEOPERANDSERROR);
				expression.setIsErroneous(true);
				return;
			}

			if (Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(operand1.getValuetype())
					&& !Type_type.TYPE_TTCN3_ENUMERATED.equals(tempType2)) {
				operand1 = operand1.setLoweridToReference(timestamp);
				checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue, operand1, operand2);
				return;
			}
		} else if (Type_type.TYPE_UNDEFINED.equals(tempType2)) {
			if ( Template_type.SPECIFIC_VALUE.equals(temp2.getTemplatetype())
					&& Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE.equals(((SpecificValue_Template) temp2).getSpecificValue()
							.getValuetype()) && !Type_type.TYPE_TTCN3_ENUMERATED.equals(tempType1)) {
				temp2 = temp2.setLoweridToReference(timestamp);
				
				//To avoid infinite loop:
				TemplateInstance tempTemplateInstance2 = new TemplateInstance(operand2.getType(),
						operand2.getDerivedReference(), (TTCN3Template) temp2);
				if (operand2 == tempTemplateInstance2) {
					return;
				}
				
				checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue, operand1, tempTemplateInstance2);
				return;
			} 
		}

		IType type1 = operand1.getExpressionGovernor(timestamp, expectedValue);
		IType type2 = operand2.getExpressionGovernor(timestamp, expectedValue);
		// ITTCN3Template temp2 = operand2.getTemplateBody();

		if (operand1.getIsErroneous(timestamp) || temp2.getIsErroneous(timestamp)) {
			expression.setIsErroneous(true);
			return;
		}

		if (type1 != null) {
			if (type2 != null) {
				TypeCompatibilityInfo info1 = new TypeCompatibilityInfo(type1, type2, true);
				TypeCompatibilityInfo info2 = new TypeCompatibilityInfo(type2, type1, true);
				boolean retVal1 = type1.isCompatible(timestamp, type2, info1, null, null);
				boolean retVal2 = type2.isCompatible(timestamp, type1, info2, null, null);
				if (!retVal1 && !retVal2) {
					expression.getLocation().reportSemanticError(info1.toString());
					expression.setIsErroneous(true);
					return;
				}

				if (GeneralConstants.WARNING.equals(typeCompatibilitySeverity)) {
					if (info1.getNeedsConversion()) {
						expression.getLocation().reportSemanticWarning(
								MessageFormat.format(TYPECOMPATWARNING, type1.getTypename(), type2.getTypename()));
					} else if (info2.getNeedsConversion()) {
						expression.getLocation().reportSemanticWarning(
								MessageFormat.format(TYPECOMPATWARNING, type2.getTypename(), type1.getTypename()));
					}
				}
			} else {
				temp2.setMyGovernor(type1);
				ITTCN3Template tempValue = type1.checkThisTemplateRef(timestamp, temp2);

				if (Template_type.OMIT_VALUE.equals(temp2.getTemplatetype())
						|| (Template_type.SPECIFIC_VALUE.equals(temp2.getTemplatetype())
								&& Value_type.OMIT_VALUE.equals(((SpecificValue_Template) temp2).getSpecificValue()
								.getValuetype()))) {
					operand1.checkExpressionOmitComparison(timestamp, expectedValue);
				} else {
					type1.checkThisTemplate(timestamp, (TTCN3Template) tempValue, false, false);
					TemplateInstance tempTemplateInstance2 = new TemplateInstance(operand2.getType(),
							operand2.getDerivedReference(), (TTCN3Template) tempValue);
					if (operand2 == tempTemplateInstance2) {
						return;
					}
					checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue, operand1,
							tempTemplateInstance2);
					return;
				}
			}
		} else if (type2 != null) {
			operand1.setMyGovernor(type2);
			IValue tempValue = type2.checkThisValueRef(timestamp, operand1);
			if (Value_type.OMIT_VALUE.equals(operand1.getValuetype())) {
				// temp2.check_expression_omit_comparison(timestamp,
				// expectedValue); ???
			} else {
				type2.checkThisValue(timestamp, tempValue, new ValueCheckingOptions(expectedValue, false, false, false, false, false));
				checkExpressionOperatorCompatibilityInternal(timestamp, expression, referenceChain, expectedValue, tempValue,
						operand2);
				return;
			}
		} else {
			if (Type_type.TYPE_UNDEFINED.equals(tempType1) || Type_type.TYPE_UNDEFINED.equals(tempType2)) {
				expression.getLocation().reportSemanticError(PLEASEUSEREFERENCES);
				expression.setIsErroneous(true);
				return;
			}

			if (!Type.isCompatible(timestamp, tempType1, tempType2, false, false)
					&& !Type.isCompatible(timestamp, tempType1, tempType2, false, false)) {
				expression.getLocation().reportSemanticError(INCOMPATIBLEOPERANDERROR);
				expression.setIsErroneous(true);
			}
		}
	}

}
