package org.eclipse.titan.designer.editors.asn1editor;

import org.eclipse.titan.designer.editors.GeneralPairMatcher;
import org.eclipse.titan.designer.editors.Pair;

public class ASN1ReferencePairMatcher extends GeneralPairMatcher {
	public ASN1ReferencePairMatcher() {
		this.pairs = new Pair[] { new Pair('(', ')'), new Pair('[', ']') };
	}

	@Override
	protected String getPartitioning() {
		return PartitionScanner.ASN1_PARTITIONING;
	}
}
