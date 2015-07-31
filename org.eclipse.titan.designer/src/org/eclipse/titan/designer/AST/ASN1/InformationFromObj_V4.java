/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.ASN1.Defined_Reference;
import org.eclipse.titan.designer.AST.ASN1.InformationFromObj;
import org.eclipse.titan.designer.AST.ASN1.Object.ASN1Objects;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldName;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition_V4;

/**
 * Class to represent InformationFromObjects.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class InformationFromObj_V4 extends InformationFromObj {

	public InformationFromObj_V4(final Defined_Reference reference, final FieldName fieldName) {
		super(reference, fieldName);
	}

	@Override
	public InformationFromObj newInstance() {
		return new InformationFromObj_V4(reference, fieldName.newInstance());
	}

	@Override
	protected ObjectSet_definition newObjectSetDefinitionInstance() {
		return new ObjectSet_definition_V4();
	}

	@Override
	protected ObjectSet_definition newObjectSetDefinitionInstance( ASN1Objects aObjects ) {
		return new ObjectSet_definition_V4( aObjects );
	}
}
