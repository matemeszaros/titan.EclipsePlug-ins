/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to report more detailed error messages on type
 * (in)compatibility.
 * 
 * @author Ferenc Kovacs
 */
public final class TypeCompatibilityInfo {
	/**
	 * op1RefStr and op2RefStr store the textual representation of references
	 * down in the type hierarchy. E.g.
	 * "@module.{var|templ}.ref1.ref2[ref3]".
	 */
	private StringBuilder op1RefStr;
	private StringBuilder op2RefStr;
	private IType op1Type;
	private IType op2Type;
	private String errorStr;
	private boolean needsConversion;
	private String subtypeErrorStr = null;
	/** LHS is a reference to a string element */
	private boolean str1Element = false;
	/** RHS is a reference to a string element */
	private boolean str2Element = false;

	public TypeCompatibilityInfo(final IType pOp1Type, final IType pOp2Type, final boolean pAddRefStr) {
		if (pOp1Type != null && pOp2Type != null) {
			needsConversion = false;
			op1Type = pOp1Type;
			op2Type = pOp2Type;
			op1RefStr = new StringBuilder();
			op2RefStr = new StringBuilder();
			if (pAddRefStr) {
				op1RefStr.append(op1Type.getTypename());
				op2RefStr.append(op2Type.getTypename());
			}
		}
	}

	public String getSubtypeError() {
		return subtypeErrorStr;
	}

	public void setSubtypeError(final String errStr) {
		subtypeErrorStr = errStr;
	}

	public void setStr1Elem(final boolean pStr1Elem) {
		str1Element = pStr1Elem;
	}

	public void setStr2Elem(final boolean pStr2Elem) {
		str2Element = pStr2Elem;
	}

	public boolean getStr1Elem() {
		return str1Element;
	}

	public boolean getStr2Elem() {
		return str2Element;
	}

	/**
	 * Extend the reference chain for the first operand.
	 *
	 * @param pRef
	 *            the next chunk of the reference
	 */
	public void appendOp1Ref(final String pRef) {
		if (op1RefStr != null) {
			op1RefStr.append(pRef);
		}
	}

	/**
	 * Extend the reference chain for the second operand.
	 *
	 * @param pRef
	 *            the next chunk of the reference
	 */
	public void appendOp2Ref(final String pRef) {
		if (op2RefStr != null) {
			op2RefStr.append(pRef);
		}
	}

	/**
	 * Types should be set only when type incompatibility is justified. So,
	 * this method should be called only once.
	 *
	 * @param pType
	 *            the type of the first operand
	 */
	public void setOp1Type(final IType pType) {
		op1Type = pType;
	}

	/**
	 * Types should be set only when type incompatibility is justified. So,
	 * this method should be called only once.
	 *
	 * @param pType
	 *            the type of the second operand
	 */
	public void setOp2Type(final IType pType) {
		op2Type = pType;
	}

	/**
	 * @return the textual representation of references
	 *         down in the type hierarchy on the first operand.
	 * */
	public String getOp1RefStr() {
		return op1RefStr.toString();
	}

	/**
	 * @return the textual representation of references
	 *         down in the type hierarchy on the second operand.
	 * */
	public String getOp2RefStr() {
		return op2RefStr.toString();
	}

	/** @return the type on the first operand */
	public IType getOp1Type() {
		return op1Type;
	}

	/** @return the type on the second operand */
	public IType getOp2Type() {
		return op2Type;
	}

	/**
	 * Additional message for errors.
	 *
	 * @param pStr
	 *            the string of the error.
	 * */
	public void setErrorStr(final String pStr) {
		errorStr = pStr;
	}

	public String getErrorStr() {
		return errorStr;
	}

	public void setNeedsConversion(final boolean pNeedsConversion) {
		needsConversion = pNeedsConversion;
	}

	public boolean getNeedsConversion() {
		return needsConversion;
	}

	/**
	 * Assemble the error message. The information string is displayed in
	 * the following line. It's a little bit confusing, but the order of
	 * multiple markers on the same position seems to be undefined. The
	 * information string should always follow the error message. Type
	 * information is displayed only if it's a sub-reference. (The names
	 * should differ.)
	 *
	 * @return the assembled message
	 * */
	@Override
	public String toString() {
		StringBuilder returnValue = new StringBuilder("Type mismatch: ");
		String op1DisplayString = op1RefStr.toString();
		String op2DisplayString = op2RefStr.toString();
		String op1TypeName = null;
		String op2TypeName = null;
		if (op1Type != null) {
			op1TypeName = op1Type.getTypename();
		}
		if (op2Type != null) {
			op2TypeName = op2Type.getTypename();
		}
		returnValue.append('`').append(op1DisplayString).append('\'');
		if (op1TypeName != null && !op1DisplayString.equals(op1TypeName)) {
			returnValue.append(" of type ");
			returnValue.append('`');
			returnValue.append(op1TypeName);
			returnValue.append('\'');
		}
		returnValue.append(" and ");
		returnValue.append('`').append(op2DisplayString).append('\'');
		if (op2TypeName != null && !op2DisplayString.equals(op2TypeName)) {
			returnValue.append(" of type ");
			returnValue.append('`');
			returnValue.append(op2TypeName);
			returnValue.append('\'');
		}
		returnValue.append(" are not compatible");
		if (errorStr != null) {
			returnValue.append(": " + errorStr);
		}
		return returnValue.toString();
	}

	public Chain getChain() {
		return new Chain();
	}

	public static final class Chain {
		private final List<IType> chainLinks = new ArrayList<IType>();
		private final List<Integer> markedStates = new ArrayList<Integer>();
		private int firstDouble = -1;

		public boolean hasRecursion() {
			return firstDouble != -1;
		}

		public void add(final IType chainLink) {
			if (firstDouble == -1) {
				for (int i = 0; i < chainLinks.size(); i++) {
					if (chainLinks.get(i) == chainLink) {
						firstDouble = chainLinks.size();
					}
				}
			}
			chainLinks.add(chainLink);
		}

		public void markState() {
			markedStates.add(chainLinks.size());
		}

		public void previousState() {
			if (markedStates.isEmpty()) {
				return;
			}
			for (int i = chainLinks.size() - 1; i >= markedStates.get(markedStates.size() - 1); i--) {
				chainLinks.remove(i);
			}
			markedStates.remove(markedStates.size() - 1);
			if (chainLinks.size() <= firstDouble) {
				firstDouble = -1;
			}
		}
	}
}
