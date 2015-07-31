/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.AtNotations;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.AST.ASN1.TableConstraint;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition_V4;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTrackerV4;

/**
 * Represents a TableConstraint (SimpleTableConstraint and
 * ComponentRelationConstraint)
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class TableConstraint_V4 extends TableConstraint {

	private BlockV4 mObjectSetBlockV4;
	private BlockV4 mAtNotationsBlockV4;

	public TableConstraint_V4(final BlockV4 aObjectSetBlock, final BlockV4 aAtNotationsBlock) {
		super();
		this.mObjectSetBlockV4 = aObjectSetBlock;
		this.mAtNotationsBlockV4 = aAtNotationsBlock;
	}

	@Override
	public TableConstraint newInstance() {
		return new TableConstraint_V4(mObjectSetBlockV4, mAtNotationsBlockV4);
	}

	@Override
	protected void parseBlocks() {
		if (mObjectSetBlockV4 == null) {
			return;
		}

		objectSet = null;
		atNotationList = null;
		if (null != mObjectSetBlockV4) {
			if (mAtNotationsBlockV4 == null) {
				// SimpleTableConstraint
				ASN1Parser2 parser = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mObjectSetBlockV4, 0);
				if (parser != null) {
					objectSet = parser.pr_special_ObjectSetSpec().definition;
					List<SyntacticErrorStorage> errors = parser.getErrorStorage();
					if (null != errors && !errors.isEmpty()) {
						objectSet = null;
						for (int i = 0; i < errors.size(); i++) {
							ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mObjectSetBlockV4.getLocation().getFile(), errors.get(i),
									IMarker.SEVERITY_ERROR);
						}
					}
				}
			} else {
				// ComponentRelationConstraint
				ASN1Parser2 parser = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mObjectSetBlockV4, 0);
				if (parser != null) {
					objectSet = parser.pr_DefinedObjectSetBlock().objectSet;
					List<SyntacticErrorStorage> errors = parser.getErrorStorage();
					if (null != errors && !errors.isEmpty()) {
						objectSet = null;
						for (int i = 0; i < errors.size(); i++) {
							ParserMarkerSupport_V4.createOnTheFlySyntacticMarker((IFile) mObjectSetBlockV4.getLocation().getFile(), errors.get(i),
									IMarker.SEVERITY_ERROR);
						}
					}
				}
				parser = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mAtNotationsBlockV4, 0);
				if (parser != null) {
					atNotationList = parser.pr_AtNotationList().notationList;
					List<SyntacticErrorStorage> errors = parser.getErrorStorage();
					if (null != errors && !errors.isEmpty()) {
						objectSet = null;
						for (int i = 0; i < errors.size(); i++) {
							ParserMarkerSupport_V4.createOnTheFlySyntacticMarker((IFile) mAtNotationsBlockV4.getLocation().getFile(), errors.get(i),
									IMarker.SEVERITY_ERROR);
						}
					}
				}
				if (atNotationList == null) {
					atNotationList = new AtNotations();
				}
			}
		}
		if (objectSet == null) {
			objectSet = new ObjectSet_definition_V4();
		}
	}
}
