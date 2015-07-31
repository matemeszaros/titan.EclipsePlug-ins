/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3;

/**
 * selector for value checking algorithms.
 * 
 * @author Kristof Szabados
 * */
public enum Expected_Value_type {
	/**
	 * The value must be known at compile time (i.e. it may refer
     *  to a TTCN-3 constant or an ASN.1 value).
     *  */
	EXPECTED_CONSTANT,
	/**
	 * The value must be static at execution time, but may be
     *  unknown at compilation time (i.e. it may refer to a TTCN-3
     *  module parameter as well).
     *  */
	EXPECTED_STATIC_VALUE,
	/**
	 * The value is known only at execution time (i.e. it may refer
     *  to a variable in addition to static values).
     *  */
	EXPECTED_DYNAMIC_VALUE,
	/**
	 * The reference may point to a dynamic value or a template
     *  (this selector is also used in template bodies where the
     *  variable references are unaccessible because of the scope
     *  hierarchy).
     *  */
	EXPECTED_TEMPLATE
}
