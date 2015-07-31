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
import org.eclipse.titan.designer.AST.ASN1.IObjectSet_Element;
import org.eclipse.titan.designer.AST.ASN1.Object.ASN1Objects;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSetElementVisitor_checker;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTrackerV4;

/**
 * ObjectSet definition.
 * ANTLR 4 specific
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ObjectSet_definition_V4 extends ObjectSet_definition {

	private final BlockV4 mBlockV4;

	public ObjectSet_definition_V4() {
		super();
		mBlockV4 = null;
	}

	public ObjectSet_definition_V4(final BlockV4 aBlockV4) {
		super();
		this.mBlockV4 = aBlockV4;
	}

	public ObjectSet_definition_V4(final ASN1Objects objects) {
		super(objects);
		mBlockV4 = null;
	}

	@Override
	public ObjectSet_definition newInstance() {
		ObjectSet_definition temp;
		if (null != mBlockV4) {
			temp = new ObjectSet_definition_V4(mBlockV4);
		} else if (null != objects) {
			temp = new ObjectSet_definition_V4(objects);
		} else {
			temp = new ObjectSet_definition_V4();
		}

		for (int i = 0; i < getObjectSetElements().size(); i++) {
			temp.addObjectSetElement(getObjectSetElements().get(i).newOseInstance());
		}
		temp.getObjectSetElements().trimToSize();

		return temp;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		isErroneous = false;

		if (null != mBlockV4) {
			parseBlockObjectSetSpecifications();
		}

		final ObjectSetElementVisitor_checker checker = new ObjectSetElementVisitor_checker(timestamp, location, myGovernor);
		getObjectSetElements().trimToSize();
		for (IObjectSet_Element element : getObjectSetElements()) {
			element.accept(checker);
		}

		lastTimeChecked = timestamp;

		createObjects(true);
	}

	@Override
	protected void parseBlockObjectSetSpecifications() {
		ObjectSet_definition temporalDefinition = null;
		if (mBlockV4 != null) {
			ASN1Parser2 parser = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4);
			if (parser != null) {
				temporalDefinition = parser.pr_special_ObjectSetSpec().definition;
				//internalIndex += parser.nof_consumed_tokens();
				List<SyntacticErrorStorage> errors = parser.getErrorStorage();
				if (null != errors && !errors.isEmpty()) {
					for (int i = 0; i < errors.size(); i++) {
						ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
								IMarker.SEVERITY_ERROR);
					}
				}
			}
		}

		if (null == temporalDefinition) {
			isErroneous = true;
			return;
		}

		temporalDefinition.getObjectSetElements().trimToSize();
		for (int i = 0; i < temporalDefinition.getObjectSetElements().size(); i++) {
			addObjectSetElement(temporalDefinition.getObjectSetElements().get(i));
		}
		temporalDefinition.setObjectSetElements(null);
		getObjectSetElements().trimToSize();

		setMyScope(getMyScope());
	}

}
