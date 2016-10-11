/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.util;

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.titan.log.viewer.views.msc.ui.core.Lifeline;
import org.eclipse.titan.log.viewer.views.msc.ui.core.MSCNode;

public class MSCNodeComparator implements Comparator<MSCNode>, Serializable {

	@Override
	public int compare(final MSCNode nodeA, final MSCNode nodeB) {
		if (!(nodeA instanceof Lifeline) || !(nodeB instanceof Lifeline)) {
			return 0;
		}

		if (((Lifeline) nodeA).getIndex() < ((Lifeline) nodeB).getIndex()) {
			return -1;
		} else if (((Lifeline) nodeA).getIndex() > ((Lifeline) nodeB).getIndex()) {
			return 1;
		} else {
			return 0;
		}
	}
}
