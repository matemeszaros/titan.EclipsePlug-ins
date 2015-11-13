/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;
/**
 * Represents the transparent attribute 
 * 
 * @author Laszlo Baji
 * */
public final class TransparentAttribute extends ExtensionAttribute {

	@Override
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.TRANSPARENT;
	}

}
