/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ExecuteSectionHandler extends ConfigSectionHandlerBase {

	public static class ExecuteItem {
		private ParseTree root = null;
		private ParseTree moduleName = null;
		private ParseTree testcaseName = null;

		public ParseTree getRoot() {
			return root;
		}

		public void setRoot(final ParseTree root) {
			this.root = root;
		}

		public ParseTree getModuleName() {
			return moduleName;
		}

		public void setModuleName(final ParseTree moduleName) {
			this.moduleName = moduleName;
		}

		public ParseTree getTestcaseName() {
			return testcaseName;
		}

		public void setTestcaseName(final ParseTree testcaseName) {
			this.testcaseName = testcaseName;
		}
	}

	private List<ExecuteItem> executeitems = new ArrayList<ExecuteItem>();

	public List<ExecuteItem> getExecuteitems() {
		return executeitems;
	}

	public void setExecuteitems(final List<ExecuteItem> executeitems) {
		this.executeitems = executeitems;
	}


}
