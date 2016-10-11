/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Type;

/**
 * This class represents those types which can be reached/used in ASN.1 .
 * 
 * @author Kristof Szabados
 * */
// TODO shouldn't this be an interface?
public abstract class ASN1Type extends Type implements IASN1Type {

	@Override
	public abstract IASN1Type newInstance();

	// TODO: remove when location is fixed
	@Override
	public Location getLikelyLocation() {
		return getLocation();
	}
}
