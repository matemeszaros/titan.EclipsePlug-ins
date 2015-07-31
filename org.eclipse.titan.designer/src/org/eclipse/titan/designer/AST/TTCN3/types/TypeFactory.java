/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ASN1.types.NULL_Type;

/**
 * This type factory can be used to create new objects of simple types from
 * corresponding type_type enumeration values. This should only be used when
 * while evaluating an expression we only have the type_type of a part of the
 * expression, or actual parameter, and we need a governor type, but there is no
 * other way to gain access to one.
 *
 * THIS CODE IS EVIL !
 * DO NOT USE, UNLESS THERE IS NO OTHER WAY TO TAKE HOLD OF A TYPE.
 * DO NOT STORE THE GAINED TYPE, AS NOT BEING CREATED FROM PARSED DATA IT
 * MIGHT POISON SEMANTIC CHECKS DONE LATER.
 * 
 * @author Kristof Szabados
 * */
public final class TypeFactory {

	/** Private constructor as this utility class is not be instantiated. */
	private TypeFactory() {

	}

	/**
	 * Creates a new simple type.
	 *
	 * @param typeType the type of the type to be created.
	 *
	 * @return the type created,or null if the type to be created is not simple
	 *         enough
	 * */
	public static Type createType(final Type.Type_type typeType) {
		switch (typeType) {
		case TYPE_NULL:
			return new NULL_Type();
		case TYPE_BOOL:
			return new Boolean_Type();
		case TYPE_INTEGER:
			return new Integer_Type();
		case TYPE_REAL:
			return new Float_Type();
		case TYPE_BITSTRING:
			return new BitString_Type();
		case TYPE_HEXSTRING:
			return new HexString_Type();
		case TYPE_OCTETSTRING:
			return new OctetString_Type();
		case TYPE_CHARSTRING:
			return new CharString_Type();
		case TYPE_UCHARSTRING:
			return new UniversalCharstring_Type();
		case TYPE_OBJECTID:
			return new ObjectID_Type();
		case TYPE_VERDICT:
			return new Verdict_Type();
		case TYPE_COMPONENT:
			return new Component_Type(new ComponentTypeBody(null, null));
		case TYPE_SEQUENCE_OF:
			return new SequenceOf_Type(null);
		case TYPE_DEFAULT:
			return new Default_Type();
		default:
			return null;
		}
	}
}
