/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.common.parsers.LocationAST;

/**
 * @author Kristof Szabados
 * */
public final class ExecuteSectionHandler {

	public static class ExecuteItem {
		private LocationAST root = null;
		private LocationAST moduleName = null;
		private LocationAST testcaseName = null;

		public LocationAST getRoot() {
			return root;
		}

		public void setRoot(final LocationAST root) {
			this.root = root;
		}

		public LocationAST getModuleName() {
			return moduleName;
		}

		public void setModuleName(final LocationAST moduleName) {
			this.moduleName = moduleName;
		}

		public LocationAST getTestcaseName() {
			return testcaseName;
		}

		public void setTestcaseName(final LocationAST testcaseName) {
			this.testcaseName = testcaseName;
		}
	}

	private LocationAST lastSectionRoot = null;
	private List<ExecuteItem> executeitems = new ArrayList<ExecuteItem>();

	public LocationAST getLastSectionRoot() {
		return lastSectionRoot;
	}

	public void setLastSectionRoot(final LocationAST lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}

	public List<ExecuteItem> getExecuteitems() {
		return executeitems;
	}

	public void setExecuteitems(final List<ExecuteItem> executeitems) {
		this.executeitems = executeitems;
	}


}
