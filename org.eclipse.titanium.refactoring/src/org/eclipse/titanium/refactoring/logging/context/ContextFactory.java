/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.logging.context;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Log_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titanium.refactoring.logging.ContextLoggingRefactoring.Settings;

/**
 * This class creates the appropriate type of {@link Context} from an {@link IVisitableNode} and
 *  the {@link Settings} object.
 *
 * @author Viktor Varga
 */
public class ContextFactory {

	/** The factory method. */
	public Context createContext(final IVisitableNode node, final Context child, final Settings settings) {
		if (node instanceof If_Statement && settings.getSetting(Settings.SETTING_LOG_IF)) {
			return new IfContext((If_Statement)node, settings);
		}
		if (node instanceof For_Statement && settings.getSetting(Settings.SETTING_LOG_LOOP)) {
			return new ForContext((For_Statement)node, settings);
		}
		if (node instanceof Def_Function && settings.getSetting(Settings.SETTING_LOG_FUNCPAR)) {
			return new FunctionContext((Def_Function)node, settings);
		}
		if (node instanceof StatementBlock && settings.getSetting(Settings.SETTING_LOG_LOCAL_VARS)) {
			if (settings.getSetting(Settings.SETTING_LOG_LOCAL_VARS_PARENT_BLOCK_ONLY)) {
				if (child != null && child.getNode() instanceof Log_Statement) {
					return new StatementBlockContext((StatementBlock)node, settings);
				} else {
					//ignore statement blocks which are not direct parents to the log statements
					return new NullContext(node, settings);
				}
			} else {
				return new StatementBlockContext((StatementBlock)node, settings);
			}
		}
		return new NullContext(node, settings);
	}

}
