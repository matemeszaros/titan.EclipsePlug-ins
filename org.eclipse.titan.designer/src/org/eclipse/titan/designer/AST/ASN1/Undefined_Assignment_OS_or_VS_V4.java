/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;
import org.eclipse.titan.designer.AST.ASN1.Ass_pard;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.AST.ASN1.Undefined_Assignment_OS_or_VS;
import org.eclipse.titan.designer.AST.ASN1.ValueSet_Assignment;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition_V4;

/**
 * An undefined assignment.
 * <p>
 * Can only be ObjectSet or ValueSet assignment because of the syntax
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Undefined_Assignment_OS_or_VS_V4 extends Undefined_Assignment_OS_or_VS {

	private final BlockV4 mBlockV4;
	
	public Undefined_Assignment_OS_or_VS_V4(final Identifier id, final Ass_pard ass_pard, final Reference reference, final BlockV4 aBlockV4) {
		super(id, ass_pard, reference);
		this.mBlockV4 = aBlockV4;
		if (null != aBlockV4) {
			aBlockV4.setFullNameParent(this);
		}
	}

	@Override
	protected ASN1Assignment internalNewInstance(final Identifier identifier) {
		return new Undefined_Assignment_OS_or_VS_V4(identifier, null, reference.newInstance(), mBlockV4);
	}

	@Override
	protected ObjectSet_definition newObjectSetDefinitionInstance() {
		return new ObjectSet_definition_V4(mBlockV4);
	}

	@Override
	protected ValueSet_Assignment newValueSetAssignmentInstance( final Referenced_Type aType ) {
		return new ValueSet_Assignment_V4(identifier, ass_pard, aType, mBlockV4);
	}
}
