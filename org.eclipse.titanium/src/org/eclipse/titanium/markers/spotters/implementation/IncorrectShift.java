/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ShiftLeftExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ShiftRightExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class IncorrectShift {
	private IncorrectShift() {
		throw new AssertionError("Noninstantiable");
	}
	
	private abstract static class ShiftChecker extends BaseModuleCodeSmellSpotter {
		private static final String NEGATIVESHIFTPROBLEM =
				"Shifting to the {1} should be used instead of shifting to the {0} with a negative value";
		private static final String ZEROSHIFTPROBLEM = "Shifting to the {0} with 0 will not change the original value";
		private static final String TOOBIGSHIFTPROBLEM =
				"Shifting a {1} long string to the {0} with {2} will always result in a string of same size, but filled with '0' -s.";

		private String actualShift;

		private ShiftChecker(final CodeSmellType type, final String actualShift) {
			super(type);
			this.actualShift = actualShift;
		}
		/**
		 * @param problems the handler class where problems should be reported
		 * @param location the location where the problem should be reported to
		 * @param value1 the left operand of the expression
		 * @param value2 the right operand of the expression
		 */
		protected void checkShiftOperands(Problems problems, Location location, Value value1,
				Value value2) {
			if (value1 == null || value2 == null) {
				return;
			}

			CompilationTimeStamp ct = CompilationTimeStamp.getBaseTimestamp();
			long stringSize = getFirstShiftOperandLength(ct, value1);
			Type_type tempType2 = value2.getExpressionReturntype(ct, null);
			if (Type_type.TYPE_INTEGER != tempType2) {
				return;
			}

			IValue tempValue = value2.getValueRefdLast(ct, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
			if (!Value_type.INTEGER_VALUE.equals(tempValue.getValuetype())) {
				return;
			}
			long shiftSize = ((Integer_Value) tempValue).getValue();
			if (!value1.isUnfoldable(ct)) {
				if (shiftSize < 0) {
					problems.report(location, MessageFormat.format(NEGATIVESHIFTPROBLEM,
							actualShift, "left".equals(actualShift)?"right":actualShift));
				} else if (shiftSize == 0) {
					problems.report(location, MessageFormat.format(ZEROSHIFTPROBLEM, actualShift));
				} else if (shiftSize > stringSize) {
					String msg = MessageFormat.format(TOOBIGSHIFTPROBLEM, actualShift, stringSize, shiftSize);
					problems.report(location, msg);
				}
			}
		}
	}

	public static class ShiftLeft extends ShiftChecker {

		public ShiftLeft() {
			super(CodeSmellType.INCORRECT_SHIFT_ROTATE_SIZE, "left");
		}

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (!(node instanceof ShiftLeftExpression)) {
				return;
			}

			ShiftLeftExpression s = (ShiftLeftExpression) node;
			

			Value value1 = s.getValue1();
			Value value2 = s.getValue2();
			checkShiftOperands(problems, s.getLocation(), value1, value2);
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(ShiftLeftExpression.class);
			return ret;
		}
	}

	public static class ShiftRight extends ShiftChecker {

		public ShiftRight() {
			super(CodeSmellType.INCORRECT_SHIFT_ROTATE_SIZE, "right");
		}

		@Override
		public void process(IVisitableNode node, Problems problems) {
			if (!(node instanceof ShiftRightExpression)) {
				return;
			}

			ShiftRightExpression s = (ShiftRightExpression) node;

			Value value1 = s.getValue1();
			Value value2 = s.getValue2();
			checkShiftOperands(problems, s.getLocation(), value1, value2);
		}

		@Override
		public List<Class<? extends IVisitableNode>> getStartNode() {
			List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
			ret.add(ShiftRightExpression.class);
			return ret;
		}
	}

	/**
	 * @param ct the compilation timestamp to be used
	 * @param value the first operand of the shift operation
	 * @return the length of the first operand if it can be determined
	 */
	private static long getFirstShiftOperandLength(CompilationTimeStamp ct, Value value) {
		long stringSize = 0;
		if (value == null) {
			return 0;
		}

		Type_type tempType1 = value.getExpressionReturntype(ct, null);
		IValue refd = value.getValueRefdLast(ct, null);
		switch (tempType1) {
		case TYPE_BITSTRING:
			if (Value_type.BITSTRING_VALUE.equals(refd.getValuetype())) {
				stringSize = ((Bitstring_Value) refd).getValueLength();
			}
			break;
		case TYPE_HEXSTRING:
			if (Value_type.HEXSTRING_VALUE.equals(refd.getValuetype())) {
				stringSize = ((Hexstring_Value) refd).getValueLength();
			}
			break;
		case TYPE_OCTETSTRING:
			if (Value_type.OCTETSTRING_VALUE.equals(refd.getValuetype())) {
				stringSize = ((Octetstring_Value) refd).getValueLength();
			}
			break;
		default:
			break;
		}
		return stringSize;
	}
}
