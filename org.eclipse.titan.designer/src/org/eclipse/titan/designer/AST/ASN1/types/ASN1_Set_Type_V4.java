/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTrackerV4;

/**
 * ANTLR 4 version
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ASN1_Set_Type_V4 extends ASN1_Set_Type {

	public ASN1_Set_Type_V4(final Block aBlockV4) {
		super(aBlockV4);
	}

	@Override
	public IASN1Type newInstance() {
		return new ASN1_Set_Type_V4(mBlock);
	}

	@Override
	protected void parseBlockSet() {
		ASN1Parser2 parserV4 = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock((BlockV4)mBlock);
		if (null == parserV4) {
			return;
		}
		components = null;

		if (null != mBlock) {
			components = parserV4.pr_special_ComponentTypeLists().list;
			List<SyntacticErrorStorage> errors = parserV4.getErrorStorage();
			if (null != errors && !errors.isEmpty()) {
				isErroneous = true;
				components = null;
				for (int i = 0; i < errors.size(); i++) {
					ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlock.getLocation().getFile(), errors.get(i),
							IMarker.SEVERITY_ERROR);
				}
			}
		}

		if (components == null) {
			isErroneous = true;
			return;
		}

		components.setFullNameParent(this);
		components.setMyScope(getMyScope());
		components.setMyType(this);
	}
}
