/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.util.List;

import org.eclipse.titan.designer.parsers.ttcn3parser.ITTCN3ReparseBase_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater_V4;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3Reparser4;

/**
 * Map of component fields.
 * <p>
 * This class is used to represent the fields of a structured type.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class CompFieldMap_V4 extends CompFieldMap {

	@Override
	protected int reparse(TTCN3ReparseUpdater aReparser) {
		return ((TTCN3ReparseUpdater_V4)aReparser).parse(new ITTCN3ReparseBase_V4() {
			@Override
			public void reparse(final TTCN3Reparser4 parser) {
				List<CompField> tempFields = parser.pr_reparse_StructFieldDefs().fields;
				lastUniquenessCheck = null;
				if ( parser.isErrorListEmpty() ) {
					if (tempFields != null) {
						addFieldsOrdered(tempFields);
					}
				}
			}
		});
	}
}
