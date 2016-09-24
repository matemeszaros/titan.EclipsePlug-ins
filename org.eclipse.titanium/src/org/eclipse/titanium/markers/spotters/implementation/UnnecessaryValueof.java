/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ValueofExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class UnnecessaryValueof extends BaseModuleCodeSmellSpotter {
	public static final String TEXT = "Applying the `valueof' operation to '{0}' will result in the original value";

	public UnnecessaryValueof() {
		super(CodeSmellType.UNNECESSARY_VALUEOF);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof ValueofExpression) {
			final ValueofExpression exp = (ValueofExpression) node;
			final CompilationTimeStamp stamp = CompilationTimeStamp.getBaseTimestamp();
			if (exp.getIsErroneous(stamp) || exp.isUnfoldable(stamp, null)) {
				return;
			}
			final TemplateInstance inst = exp.getTemplateInstance();
			if (inst != null && inst.getDerivedReference() == null && inst.getTemplateBody().isValue(stamp)) {
				final String msg = MessageFormat.format(TEXT, inst.getTemplateBody().getValue().createStringRepresentation());
				problems.report(exp.getLocation(), msg);
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(ValueofExpression.class);
		return ret;
	}
}
