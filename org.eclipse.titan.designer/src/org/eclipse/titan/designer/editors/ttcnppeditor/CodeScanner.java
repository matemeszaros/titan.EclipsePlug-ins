/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcnppeditor;

import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.titan.designer.editors.ColorManager;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * @author Kristof Szabados
 * */
public final class CodeScanner extends RuleBasedScanner {

	public CodeScanner(final ColorManager colorManager) {
		List<IRule> rules = org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.getTTCNRules(colorManager);
		// multi-line preprocessor directives:
		IToken preprocessor = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_PREPROCESSOR);
		rules.add(new EndOfLineRule("#", preprocessor, '\\', true));
		setRules(rules.toArray(new IRule[rules.size()]));
	}
}
