/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcnppeditor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * @author Kristof Szabados
 * */
public final class PartitionScanner extends RuleBasedPartitionScanner {
	public static final String TTCN3_PARTITIONING = "__ttcn3_partitioning";
	public static final String MULTI_LINE_COMMENT = "__ttcn3_multi_line_comment";

	public static final String[] PARTITION_TYPES = new String[] { IDocument.DEFAULT_CONTENT_TYPE, PartitionScanner.MULTI_LINE_COMMENT, };

	public PartitionScanner() {
		IToken multiLineComment = new Token(PartitionScanner.MULTI_LINE_COMMENT);
		fRules = new IPredicateRule[] { new MultiLineRule("/*", "*/", multiLineComment, '\0', true) };
	}
}
