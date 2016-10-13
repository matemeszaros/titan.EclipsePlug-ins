/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

/**
 * Represents a single encdecvalue attribute on an external function, used to
 * automatically generate the encoding function, according to the encoding type
 * and options passed as parameters..
 * 
 * @author Arpad Lovassy
 */
public final class EncDecValueAttribute extends ExtensionAttribute implements IInOutTypeMappingAttribute {

	/** The in-mappings, can be null */
	private TypeMappings mInMappings;

	/** The out-mappings, can be null */
	private TypeMappings mOutMappings;

	
	public EncDecValueAttribute() {
	}

	@Override
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.ENCDECVALUE;
	}

	public TypeMappings getInMappings() {
		return mInMappings;
	}

	public void setInMappings(final TypeMappings aMappings) {
		if ( mInMappings == null ) {
			mInMappings = aMappings;
			return;
		}

		mInMappings.copyMappings( aMappings );
	}

	public TypeMappings getOutMappings() {
		return mOutMappings;
	}

	public void setOutMappings(final TypeMappings aMappings) {
		if ( mOutMappings == null ) {
			mOutMappings = aMappings;
			return;
		}

		mOutMappings.copyMappings( aMappings );
	}
}
