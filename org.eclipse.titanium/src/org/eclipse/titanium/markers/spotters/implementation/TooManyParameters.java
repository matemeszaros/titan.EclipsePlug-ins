/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.preferences.PreferenceConstants;

public class TooManyParameters extends BaseModuleCodeSmellSpotter {
	private final int reportTooManyParametersSize;
	private static final String TOOMANYPARAMETERS = "More than {0} parameters: {1}";

	public TooManyParameters() {
		super(CodeSmellType.TOO_MANY_PARAMETERS);
		reportTooManyParametersSize = Platform.getPreferencesService().getInt(Activator.PLUGIN_ID,
				PreferenceConstants.TOO_MANY_PARAMETERS_SIZE, 7, null);
	}

	@Override
	public void process(IVisitableNode node, Problems problems) {
		if (node instanceof FormalParameterList) {
			FormalParameterList s = (FormalParameterList) node;
			if (s.getNofParameters() > reportTooManyParametersSize) {
				String msg = MessageFormat.format(TOOMANYPARAMETERS, reportTooManyParametersSize, s.getNofParameters());
				problems.report(s.getLocation(), msg);
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(FormalParameterList.class);
		return ret;
	}
}
