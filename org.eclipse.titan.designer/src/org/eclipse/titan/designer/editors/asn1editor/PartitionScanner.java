/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;

/**
 * @author Kristof Szabados
 * */
public final class PartitionScanner extends RuleBasedPartitionScanner {
	public static final String ASN1_PARTITIONING = "__asn1_partitioning";

	static final String[] PARTITION_TYPES = new String[] { IDocument.DEFAULT_CONTENT_TYPE, };

	public PartitionScanner() {
		fRules = new IPredicateRule[0];
	}
}
