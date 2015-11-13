/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.asn1parser;


import org.eclipse.titan.common.parsers.TitanListener;

public class ASN1Listener extends TitanListener {

	public ASN1Listener() {
		super(); 
	}

	public ASN1Listener(Asn1Parser parser) {
		super.errorsStored = parser.getErrorStorage(); 
	}

}
