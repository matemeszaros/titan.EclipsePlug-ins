package org.eclipse.titan.designer.editors.ttcn3editor;

import org.eclipse.titan.designer.editors.GeneralPairMatcher;
import org.eclipse.titan.designer.editors.Pair;

public class TTCN3ReferencePairMatcher extends GeneralPairMatcher {
	public TTCN3ReferencePairMatcher() {
		this.pairs = new Pair[] { new Pair('(', ')'), new Pair('[', ']') };
	}

	@Override
	protected String getPartitioning() {
		return PartitionScanner.TTCN3_PARTITIONING;
	}
}
