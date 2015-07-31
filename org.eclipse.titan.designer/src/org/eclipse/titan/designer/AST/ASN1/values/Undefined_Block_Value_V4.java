/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.values;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ASN1.BlockV4;
import org.eclipse.titan.designer.AST.ASN1.values.Named_Bits;
import org.eclipse.titan.designer.AST.ASN1.values.RelativeObjectIdentifier_Value;
import org.eclipse.titan.designer.AST.ASN1.values.Undefined_Block_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.ObjectIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Sequence_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SetOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Set_Value;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport_V4;
import org.eclipse.titan.designer.parsers.asn1parser.ASN1Parser2;
import org.eclipse.titan.designer.parsers.asn1parser.BlockLevelTokenStreamTrackerV4;

/**
 * Undefined_Block_Value for ANTLR V4
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class Undefined_Block_Value_V4 extends Undefined_Block_Value {

	private BlockV4 mBlockV4;
	
	public Undefined_Block_Value_V4(final BlockV4 aBlockV4) {
		this.mBlockV4 = aBlockV4;
	}

	@Override
	public Location getLocation() {
		if (null != mBlockV4) {
			return mBlockV4.getLocation();
		} else {
			return null;
		}
	}

	@Override
	protected Named_Bits parseBlockNamedBits() {
		ASN1Parser2 parserV4 = null;
		parserV4 = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4);
		if (null == parserV4) {
			return null;
		}
		Named_Bits namedBits = null;
		namedBits = parserV4.pr_special_NamedBitListValue().named_bits;
		List<SyntacticErrorStorage> errors = parserV4.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			namedBits = null;
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}
		return namedBits;
	}

	@Override
	protected SequenceOf_Value parseBlockSeqofValue() {
		ASN1Parser2 parserV4 = null;
		parserV4 = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4);
		if (null == parserV4) {
			return null;
		}
		SequenceOf_Value value = null;
		value = parserV4.pr_special_SeqOfValue().value;
		List<SyntacticErrorStorage> errors = parserV4.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			value = null;

			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}
		return value;
	}

	@Override
	protected SetOf_Value parseBlockSetofValue() {
		ASN1Parser2 parserV4 = null;
		parserV4 = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4);
		if (null == parserV4) {
			return null;
		}
		SetOf_Value value = null;
		value = parserV4.pr_special_SetOfValue().value;
		List<SyntacticErrorStorage> errors = parserV4.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			value = null;
		
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}	
		return value;
	}

	@Override
	protected Sequence_Value parseBlockSequenceValue() {
		ASN1Parser2 parserV4 = null;
		parserV4 = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4);
		if (null == parserV4) {
			return null;
		}
		Sequence_Value value = null;
		value = parserV4.pr_special_SequenceValue().value;
		List<SyntacticErrorStorage> errors = parserV4.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			value = null;
		
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}	
		return value;
	}

	@Override
	protected Set_Value parseBlockSetValue() {
		ASN1Parser2 parserV4 = null;
		parserV4 = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4);
		if (null == parserV4) {
			return null;
		}
		Set_Value value = null;

		value = parserV4.pr_special_SetValue().value;
		List<SyntacticErrorStorage> errors = parserV4.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			value = null;
		
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}	
		return value;
	}

	@Override
	protected ObjectIdentifier_Value parseBlockObjectIdentifierValue() {
		ASN1Parser2 parserV4 = null;
		parserV4 = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4);
		if (null == parserV4) {
			return null;
		}
		ObjectIdentifier_Value value = null;
		value = parserV4.pr_special_ObjectIdentifierValue().value;
		List<SyntacticErrorStorage> errors = parserV4.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			value = null;
		
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}	
		return value;
	}

	@Override
	protected RelativeObjectIdentifier_Value parseBlockRelativeObjectIdentifierValue() {
		ASN1Parser2 parserV4 = null;
		parserV4 = BlockLevelTokenStreamTrackerV4.getASN1ParserForBlock(mBlockV4);
		if (null == parserV4) {
			return null;
		}
		RelativeObjectIdentifier_Value value = null;
		value = parserV4.pr_special_RelativeObjectIdentifierValue().value;
		List<SyntacticErrorStorage> errors = parserV4.getErrorStorage();
		if (null != errors && !errors.isEmpty()) {
			isErroneous = true;
			value = null;
		
			for (int i = 0; i < errors.size(); i++) {
				ParserMarkerSupport_V4.createOnTheFlyMixedMarker((IFile) mBlockV4.getLocation().getFile(), errors.get(i),
						IMarker.SEVERITY_ERROR);
			}
		}
		return value;
	}
}
