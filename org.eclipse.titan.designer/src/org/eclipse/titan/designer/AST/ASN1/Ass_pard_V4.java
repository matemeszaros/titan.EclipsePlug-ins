/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTrackerV4;
import org.eclipse.titan.designer.parsers.asn1parser.FormalParameter_Helper_V4;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;

/**
 * Parameterized assignment.
 * ANTLR 4 version
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Ass_pard_V4 extends Ass_pard {
	/** parameter list. */
	private final BlockV4 mParameterListV4;

	/** The list of pre-processed formal parameters. */
	private ArrayList<FormalParameter_Helper_V4> mParametersV4;

	public Ass_pard_V4(final BlockV4 aParameterListV4) {
		this.mParameterListV4 = aParameterListV4;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && (!isErroneous || !lastTimeChecked.isLess(timestamp))) {
			return;
		}

		lastTimeChecked = timestamp;
		isErroneous = false;

		if (null == mParameterListV4) {
			isErroneous = true;
			return;
		}

		if (null != mParametersV4) {
			return;
		}

		final ASN1Parser2 parser = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mParameterListV4);
		mParametersV4 = parser.pr_special_FormalParameterList().parameters;
		List<SyntacticErrorStorage> errors = parser.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			mParametersV4 = null;

			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mParameterListV4.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}
		
		if (null == mParametersV4) {
			isErroneous = true;
		} else {
			mParametersV4.trimToSize();
		}
	}
	
	/**
	 * Returns the list of formal parameter helpers.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 *
	 * @return the list of formal parameter helpers.
	 * */
	public List<FormalParameter_Helper_V4> getFormalParameters(final CompilationTimeStamp timestamp) {
		check(timestamp);

		if (null == mParametersV4) {
			return new ArrayList<FormalParameter_Helper_V4>();
		}

		return mParametersV4;
	}
}
