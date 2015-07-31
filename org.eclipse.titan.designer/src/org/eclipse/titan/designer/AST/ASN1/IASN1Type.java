/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;

/**
 * @author Kristof Szabados
 * */
public interface IASN1Type extends IType {

	/** @return a new instance of this ASN.1 type */
	IASN1Type newInstance();

	// TODO: remove when location is fixed
	Location getLikelyLocation();
}
