/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.extensionattributeparser;

import org.eclipse.titan.designer.AST.TTCN3.attributes.ErrorBehaviorAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute;

/**
 * Just a helper class to store information
 * 
 * @author Kristof Szabados
 * */
public class EncodeMappingHelper {

	public ExtensionAttribute encodeAttribute;
	public ErrorBehaviorAttribute errorBehaviorAttribute;
	
	public EncodeMappingHelper(final ExtensionAttribute encodeAttribute, final ErrorBehaviorAttribute errorBehaviorAttribute){
		this.encodeAttribute = encodeAttribute;
		this.errorBehaviorAttribute = errorBehaviorAttribute;
	}
}
