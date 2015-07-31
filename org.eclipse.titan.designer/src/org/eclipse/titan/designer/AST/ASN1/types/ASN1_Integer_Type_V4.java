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
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Integer_Type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTrackerV4;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;

/**
 * ANTLR 4 version
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ASN1_Integer_Type_V4 extends ASN1_Integer_Type {

	private final BlockV4 mBlockV4;

	public ASN1_Integer_Type_V4(final BlockV4 aBlockV4) {
		this.mBlockV4 = aBlockV4;
	}

	public ASN1_Integer_Type_V4() {
		this.mBlockV4 = null;
	}

	@Override
	public IASN1Type newInstance() {
		return new ASN1_Integer_Type_V4(mBlockV4);
	}

	@Override
	protected void parseBlockInt() {
		ASN1Parser2 parserV4 = null;
		if (null != mBlockV4) {
			parserV4 = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4);
		}
		if (null == parserV4) {
			return;
		}
		namedNumbers = null;
		if (null != mBlockV4) {
			namedNumbers = parserV4.pr_special_NamedNumberList().namedValues;
			List<SyntacticErrorStorage> errors = parserV4.getErrorStorage();
			if (null != errors && !errors.isEmpty()) {
				isErroneous = true;
				namedNumbers = null;
				for (int i = 0; i < errors.size(); i++) {
					ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
							IMarker.SEVERITY_ERROR);
				}
			}
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (namedNumbers != null) {
			namedNumbers.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (namedNumbers != null && !namedNumbers.accept(v)) {
			return false;
		}
		return true;
	}
}
