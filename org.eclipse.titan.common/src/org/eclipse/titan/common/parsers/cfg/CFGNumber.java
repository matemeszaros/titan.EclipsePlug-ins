/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import java.math.BigInteger;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class CFGNumber {
	private static final String DIV_BY_ZERO = "Division by zero";
	private boolean isFloatNumber;
	private double floatNumber;
	private BigInteger intNumber;
	
	/**
	 * Constructor
	 * @param text the string representation of the number. The constructor will decide if it will be stored as float or integer
	 */
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

	/**
	 * @return this + num
	 */
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

	/**
	 * @return this - num
	 */
	public void sub(final CFGNumber num) {
		if (isFloatNumber) {
			if (num.isFloatNumber) {
				floatNumber -= num.floatNumber;
			} else {
				floatNumber -= num.intNumber.floatValue();
			}
		} else {
			if (num.isFloatNumber) {
				isFloatNumber = true;
				floatNumber = intNumber.floatValue() - num.floatNumber;
			} else {
				intNumber = intNumber.subtract(num.intNumber);
			}
		}
	}

	/**
	 * @return this * num
	 */
	public void mul(final int num) {
		if (isFloatNumber) {
			floatNumber *= num;
		} else {
			intNumber = intNumber.multiply(new BigInteger(String.valueOf(num)));
		}
	}

	/**
	 * @return this * num
	 */
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

	/**
	 * @return this / num
	 * @throws ArithmeticException if num == 0
	 */
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
			if (Double.isInfinite(floatNumber)) {
				return "0.0";
			}
			return Double.toString(floatNumber);
		}
		
		return intNumber.toString();
	}
	
	/**
	 * @return float value of the number
	 */
	public Double getValue() {
		if ( isFloatNumber ) {
			return new Double( floatNumber );
		} else {
			return intNumber.doubleValue();
		}
	}
	
	/**
	 * @return integer value, or null if number is float
	 */
	public Integer getIntegerValue() {
		if ( isFloatNumber ) {
			return null;
		} else {
			return intNumber.intValue();
		}
	}
}
