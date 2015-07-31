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
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTrackerV4;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Enumerated_Type;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;

/**
 * ANTLR 4 version
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ASN1_Enumerated_Type_V4 extends ASN1_Enumerated_Type {
	
	private final BlockV4 mBlockV4;

	public ASN1_Enumerated_Type_V4(final BlockV4 aBlockV4) {
		this.mBlockV4 = aBlockV4;
	}

	@Override
	public IASN1Type newInstance() {
		return new ASN1_Enumerated_Type_V4(mBlockV4);
	}
	
	@Override
	protected void parseBlockEnumeration() {
		ASN1Parser2 parserV4 = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4);
		if (null == parserV4) {
			return;
		}
		enumerations = null;
		enumerations = parserV4.pr_special_Enumerations().enumeration;
		List<SyntacticErrorStorage> errors = parserV4.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			enumerations = null;
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}
	}

	// TODO: remove this when the location is properly set
	@Override
	public Location getLikelyLocation() {
		return location;
	}
}
