/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_Builder;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_Builder_V4;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_root;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_Definition;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTrackerV4;

/**
 * Class to represent ObjectClassDefinition.
 * ANTLR V4 specific
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ObjectClass_Definition_V4 extends ObjectClass_Definition {

	private final BlockV4 fieldSpecsBlock;
	private final BlockV4 withSyntaxBlock;

	public ObjectClass_Definition_V4(final BlockV4 fieldSpecsBlock, final BlockV4 withSyntaxBlock) {
		this.fieldSpecsBlock = fieldSpecsBlock;
		this.withSyntaxBlock = withSyntaxBlock;
	}

	public ObjectClass_Definition_V4() {
		this.fieldSpecsBlock = null;
		this.withSyntaxBlock = null;
	}

	@Override
	public ObjectClass_Definition newInstance() {
		return new ObjectClass_Definition_V4(fieldSpecsBlock, withSyntaxBlock);
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		isErroneous = false;

		if (null != ocsRoot) {
			return;
		}

		if (null == fieldSpecifications) {
			parseBlockFieldSpecs();
		}

		if (getIsErroneous(timestamp) || null == fieldSpecifications) {
			return;
		}

		fieldSpecifications.check(timestamp);

		if (null == ocsRoot) {
			ocsRoot = new ObjectClassSyntax_root();
			ObjectClassSyntax_Builder builder = null;
			builder = new ObjectClassSyntax_Builder_V4(withSyntaxBlock, fieldSpecifications);
			ocsRoot.accept(builder);
		}
	}

	@Override
	protected void parseBlockFieldSpecs() {
		ASN1Parser2 parser2 = null;
		parser2 = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(fieldSpecsBlock);
		if (null == parser2) {
			return;
		}
		
		fieldSpecifications = parser2.pr_special_FieldSpecList().fieldSpecifications;
		List<SyntacticErrorStorage> errors = parser2.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			fieldSpecifications = null;
		
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) fieldSpecsBlock.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}
		if (null == fieldSpecifications) {
			isErroneous = true;
			return;
		}

		fieldSpecifications.setFullNameParent(this);
		fieldSpecifications.setMyObjectClass(this);
	}
}
