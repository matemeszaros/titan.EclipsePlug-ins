/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.util.List;

import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;

/**
 * Abstract parent for defined references.
 * <p>
 * Plays the role of many references, for example: ASN1::Ref_defd,
 * ASN1::Ref_defd_simple
 * 
 * @author Kristof Szabados
 */
public class Defined_Reference extends Reference {

	public Defined_Reference(final Identifier modid) {
		super(modid);
		detectedModuleId = true;
	}

	public Defined_Reference(final Identifier modid, final List<ISubReference> subreferences) {
		super(modid, subreferences);
		detectedModuleId = true;
	}

	@Override
	public Defined_Reference newInstance() {
		return new Defined_Reference(modid, subReferences);
	}
}
