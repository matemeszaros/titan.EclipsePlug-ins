/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Array_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.RotateLeftExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.RotateRightExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class IncorrectRotate {
	private IncorrectRotate() {
		throw new AssertionError("Noninstantiable");
	}

	private abstract static class RotationChecker extends BaseModuleCodeSmellSpotter {
		private static final String EFFECTLESSROTATION = "Rotating will not change the value";
		private static final String NEGATIVEROTATEPROBLEM =
				"Rotating to the {1} should be used instead of rotating to the {0} with a negative value";
		private static final String ZEROROTATEPROBLEM = "Rotating to the {0} with 0 will not change the original value";
		private static final String TOOBIGROTATEPROBLEM =
				"Rotating a {1} long value to the {0} with {2} will have the same effect as rotating by {3}";

		private String actualRotation;

		private RotationChecker(final CodeSmellType type, final String actualRotation) {
			super(type);
			this.actualRotation = actualRotation;
		}
		/**
		 * @param problems the handler class where problems should be reported
		 * @param location the location where the problem should be reported to
		 * @param value1 the left operand of the expression
		 * @param value2 the right operand of the expression
		 */
		protected void checkRotationOperands(Problems problems, Location location, Value value1,
				Value value2) {
			if (value1 == null || value2 == null) {
				return;
			}

			CompilationTimeStamp ct = CompilationTimeStamp.getBaseTimestamp();
			long valueSize = getFirstRotateOperandLength(ct, value1);
			Type_type tempType2 = value2.getExpressionReturntype(ct, null);
			if (Type_type.TYPE_INTEGER != tempType2) {
				return;
			}

			IValue tempValue = value2.getValueRefdLast(ct, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
			if (!Value_type.INTEGER_VALUE.equals(tempValue.getValuetype())) {
				return;
			}

			long rotationSize = ((Integer_Value) tempValue).getValue();
			if (!value1.isUnfoldable(ct)) {
				if (valueSize == 0 || valueSize == 1) {
					problems.report(location, EFFECTLESSROTATION);
				} else if (rotationSize < 0) {
					problems.report(location,MessageFormat.format(NEGATIVEROTATEPROBLEM,
							actualRotation, "left".equals(actualRotation)?"right":actualRotation));
				} else if (rotationSize == 0) {
					problems.report(location, MessageFormat.format(ZEROROTATEPROBLEM, actualRotation));
				} else if (rotationSize > valueSize) {
					String msg = MessageFormat.format(TOOBIGROTATEPROBLEM,
							actualRotation, valueSize, rotationSize, rotationSize % valueSize);
					problems.report(location, msg);
				}
			}
		}
	}

	public static class RotateLeft extends RotationChecker {

		public RotateLeft() {
			super(CodeSmellType.INCORRECT_SHIFT_ROTATE_SIZE, "left");
		}

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (!(node instanceof RotateLeftExpression)) {
				return;
			}

			RotateLeftExpression s = (RotateLeftExpression) node;
			Value value1 = s.getValue1();
			Value value2 = s.getValue2();

			checkRotationOperands(problems, s.getLocation(), value1, value2);
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(RotateLeftExpression.class);
			return ret;
		}
	}

	public static class RotateRight extends RotationChecker {

		public RotateRight() {
			super(CodeSmellType.INCORRECT_SHIFT_ROTATE_SIZE, "right");
		}

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (!(node instanceof RotateRightExpression)) {
				return;
			}

			RotateRightExpression s = (RotateRightExpression) node;
			Value value1 = s.getValue1();
			Value value2 = s.getValue2();

			checkRotationOperands(problems, s.getLocation(), value1, value2);
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(RotateRightExpression.class);
			return ret;
		}
	}

	/**
	 * @param ct the compilation timestamp to be used
	 * @param value the first operand of the rotate operation
	 * @return the length of the first operand if it can be determined
	 */
	private static long getFirstRotateOperandLength(CompilationTimeStamp ct, Value value) {
		long valueSize = 0;
		if (value == null) {
			return 0;
		}
		Type_type tempType = value.getExpressionReturntype(ct, null);
		IValue refd = value.getValueRefdLast(ct, null);
		switch (tempType) {
		case TYPE_BITSTRING:
			if (Value_type.BITSTRING_VALUE.equals(refd.getValuetype())) {
				valueSize = ((Bitstring_Value) refd).getValueLength();
			}
			break;
		case TYPE_HEXSTRING:
			if (Value_type.HEXSTRING_VALUE.equals(refd.getValuetype())) {
				valueSize = ((Hexstring_Value) refd).getValueLength();
			}
			break;
		case TYPE_OCTETSTRING:
			if (Value_type.OCTETSTRING_VALUE.equals(refd.getValuetype())) {
				valueSize = ((Octetstring_Value) refd).getValueLength();
			}
			break;
		case TYPE_CHARSTRING:
			if (Value_type.CHARSTRING_VALUE.equals(refd.getValuetype())) {
				valueSize = ((Charstring_Value) refd).getValueLength();
			}
			break;
		case TYPE_UCHARSTRING:
			if (Value_type.UNIVERSALCHARSTRING_VALUE.equals(refd.getValuetype())) {
				valueSize = ((UniversalCharstring_Value) refd).getValueLength();
			}
			break;
		case TYPE_SET_OF:
			if (Value_type.SEQUENCEOF_VALUE.equals(value.getValuetype())) {
				valueSize = ((SequenceOf_Value) value).getNofComponents();
			} else if (Value_type.SETOF_VALUE.equals(value.getValuetype())) {
				valueSize = ((SetOf_Value) value).getNofComponents();
			}
			break;
		case TYPE_SEQUENCE_OF:
			if (Value_type.SEQUENCEOF_VALUE.equals(value.getValuetype())) {
				valueSize = ((SequenceOf_Value) value).getNofComponents();
			} else if (Value_type.SETOF_VALUE.equals(value.getValuetype())) {
				valueSize = ((SetOf_Value) value).getNofComponents();
			}
			break;
		case TYPE_ARRAY:
			if (Value_type.SEQUENCEOF_VALUE.equals(value.getValuetype())) {
				valueSize = ((SequenceOf_Value) value).getNofComponents();
			}
			if (Value_type.ARRAY_VALUE.equals(value.getValuetype())) {
				valueSize = ((Array_Value) value).getNofComponents();
			}
			break;
		default:
			break;
		}

		return valueSize;
	}
}
