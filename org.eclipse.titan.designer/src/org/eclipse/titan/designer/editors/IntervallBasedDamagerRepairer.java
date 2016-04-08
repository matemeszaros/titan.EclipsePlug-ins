/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.common.parsers.Interval.interval_type;

/**
 * @author Kristof Szabados
 * */
public final class IntervallBasedDamagerRepairer extends DefaultDamagerRepairer {

	public IntervallBasedDamagerRepairer(final ITokenScanner scanner) {
		super(scanner);
	}

	@Override
	public IRegion getDamageRegion(final ITypedRegion partition, final DocumentEvent e, final boolean documentPartitioningChanged) {
		Interval interval = GlobalIntervalHandler.getInterval(fDocument);
		if (interval != null) {
			int maxLength = Math.max(0, (e.getText() == null ? e.getLength() : e.getText().length()));
			int endoffset = e.getOffset() + maxLength;
			Interval smallest = interval.getSmallestEnclosingInterval(e.getOffset(), endoffset);
			Interval canaryInterval = smallest.getSmallestEnclosingInterval(e.getOffset());
			if (interval_type.MULTILINE_COMMENT.equals(canaryInterval.getType())) {
				return new Region(canaryInterval.getStartOffset(), Math.min(smallest.getEndOffset() - canaryInterval.getStartOffset()
						+ maxLength, fDocument.getLength()));
			}
		}
		return super.getDamageRegion(partition, e, documentPartitioningChanged);
	}
}
