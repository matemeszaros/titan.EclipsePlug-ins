/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;
import org.eclipse.titan.designer.AST.ASN1.Ass_pard;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.ValueSet_Assignment;

/**
 * ANTLR 4 version
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ValueSet_Assignment_V4 extends ValueSet_Assignment {

	/** right */
	private final BlockV4 mBlockV4;
	
	public ValueSet_Assignment_V4(final Identifier aId, final Ass_pard aAssPard, final IASN1Type aType, final BlockV4 aBlockV4) {
		super( aId, aAssPard, aType );
		this.mBlockV4 = aBlockV4;

		if (null != aBlockV4) {
			aBlockV4.setFullNameParent(this);
		}
	}

	@Override
	protected ASN1Assignment internalNewInstance(final Identifier identifier) {
		return new ValueSet_Assignment_V4(identifier, null, type.newInstance(), mBlockV4);
	}
}
