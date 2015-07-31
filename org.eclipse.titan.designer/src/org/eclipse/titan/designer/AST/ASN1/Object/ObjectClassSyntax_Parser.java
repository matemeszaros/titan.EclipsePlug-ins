/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;


import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet;

/**
 * OCS visitor to parse an object definition.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class ObjectClassSyntax_Parser extends ObjectClassSyntax_Visitor {

	protected final Object_Definition myObject;

	/**
	 * Stores whether the parsing was successful. If it is false, the
	 * parsing cannot be continued.
	 * */
	protected boolean success;
	/** Stores whether the previous parsing was successful. */
	protected boolean previousSuccess;

	/** the actual index till which the tokens are already parsed. */
	protected int internalIndex;

	public ObjectClassSyntax_Parser(final Object_Definition myObject) {
		this.myObject = myObject;
	}

	@Override
	public void visitSetting(final ObjectClassSyntax_setting parameter) {
		FieldSetting fieldSetting = null;
		switch (parameter.getSettingType()) {
		case S_T:
			final ASN1Type type = parseType();
			if (null != type) {
				fieldSetting = new FieldSetting_Type(parameter.getIdentifier().newInstance(), type);
				fieldSetting.setLocation(type.getLocation());
			}
			break;
		case S_V:
			final boolean parseSuccess = parseValue();
			if (parseSuccess) {
				fieldSetting = new FieldSetting_Value(parameter.getIdentifier().newInstance());
				fieldSetting.setLocation(parameter.getIdentifier().getLocation());
			}
			break;
		case S_VS:
			// TODO mark as NOT SUPPORTED
			break;
		case S_O:
			final ASN1Object object = parseObject();
			if (null != object) {
				fieldSetting = new FieldSetting_Object(parameter.getIdentifier().newInstance(), object);
				fieldSetting.setLocation(object.getLocation());
			}
			break;
		case S_OS:
			final ObjectSet objectSet = parseObjectSet();
			if (null != objectSet) {
				fieldSetting = new FieldSetting_ObjectSet(parameter.getIdentifier().newInstance(), objectSet);
				fieldSetting.setLocation(objectSet.getLocation());
			}
			break;
		case S_UNDEF:
			// FATAL ERROR
		default:
			break;
		}

		previousSuccess = null != fieldSetting;
		myObject.addFieldSetting(fieldSetting);
	}

	abstract protected  ASN1Type parseType();
	
	abstract protected boolean parseValue();
	
	abstract protected ASN1Object parseObject();
	
	abstract protected ObjectSet parseObjectSet();
}
