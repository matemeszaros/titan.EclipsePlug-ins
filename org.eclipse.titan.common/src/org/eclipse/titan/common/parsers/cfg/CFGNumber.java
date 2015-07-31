/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import java.math.BigInteger;

/**
 * @author Kristof Szabados
 * */
public final class CFGNumber {
	private static final String DIV_BY_ZERO = "Division by zero";
	private boolean isFloatNumber;
	private float floatNumber;
	private BigInteger intNumber;
	
	public CFGNumber(final String text) {
		try {
			floatNumber = Float.parseFloat(text);
			isFloatNumber = true;
			intNumber = new BigInteger(text);
			isFloatNumber = false;
		} catch (NumberFormatException e) {
			isFloatNumber = true;
		}
	}

	public boolean isFloat() {
		return isFloatNumber;
	}

	public void add(final CFGNumber num) {
		if (isFloatNumber) {
			if (num.isFloatNumber) {
				floatNumber += num.floatNumber;
			} else {
				floatNumber += num.intNumber.floatValue();
			}
		} else {
			if (num.isFloatNumber) {
				isFloatNumber = true;
				floatNumber = intNumber.floatValue() + num.floatNumber;
			} else {
				intNumber = intNumber.add(num.intNumber);
			}
		}
	}

	public void mul(final int num) {
		if (isFloatNumber) {
			floatNumber *= num;
		} else {
			intNumber = intNumber.multiply(new BigInteger(String.valueOf(num)));
		}
	}

	public void mul(final CFGNumber num) {
		if (isFloatNumber) {
			if (num.isFloatNumber) {
				floatNumber *= num.floatNumber;
			} else {
				floatNumber *= num.intNumber.floatValue();
			}
		} else {
			if (num.isFloatNumber) {
				isFloatNumber = true;
				floatNumber = intNumber.floatValue() * num.floatNumber;
			} else {
				intNumber = intNumber.multiply(num.intNumber);
			}
		}
	}

	public void div(final CFGNumber num) throws ArithmeticException {
		if (isFloatNumber) {
			if (num.isFloatNumber) {
				if (num.floatNumber == 0) {
					throw new ArithmeticException(DIV_BY_ZERO);
				}
				floatNumber /= num.floatNumber;
			} else {
				if (num.intNumber.compareTo(BigInteger.ZERO) == 0) {
					throw new ArithmeticException(DIV_BY_ZERO);
				}
				floatNumber /= num.intNumber.floatValue();
			}
		} else {
			if (num.isFloatNumber) {
				if (num.floatNumber == 0) {
					throw new ArithmeticException(DIV_BY_ZERO);
				}
				isFloatNumber = true;
				floatNumber = intNumber.floatValue() / num.floatNumber;
			} else {
				if (num.intNumber.compareTo(BigInteger.ZERO) == 0) {
					throw new ArithmeticException(DIV_BY_ZERO);
				}
				intNumber = intNumber.divide(num.intNumber);
			}
		}
	}
	
	@Override
	public String toString() {
		if (isFloatNumber) {
			// "Infinity" is not a valid value in a configuration file.
			if ((floatNumber == Float.NEGATIVE_INFINITY) || (floatNumber == Float.POSITIVE_INFINITY)) {
				return "0.0";
			}
			return Float.toString(floatNumber);
		}
		
		return intNumber.toString();
	}
}
