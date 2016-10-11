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
public final class ModuleParameterSectionHandler {

	public static class ModuleParameter {
		private ParseTree root = null;
		private ParseTree moduleName = null;
		
		/**
		 * Separator between module and parameter names,
		 * it is "." if module name is NOT empty,
		 * "" if module name is empty
		 */
		private ParseTree mSeparator = null;
		
		private ParseTree parameterName = null;
		private ParseTree value = null;

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

		public ParseTree getSeparator() {
			return mSeparator;
		}

		public void setSeparator( final ParseTree aSeparator ) {
			this.mSeparator = aSeparator;
		}

		public ParseTree getParameterName() {
			return parameterName;
		}

		public void setParameterName(final ParseTree parameterName) {
			this.parameterName = parameterName;
		}

		public ParseTree getValue() {
			return value;
		}

		public void setValue(final ParseTree value) {
			this.value = value;
		}
	}

	private ParseTree lastSectionRoot = null;
	private List<ModuleParameter> moduleParameters = new ArrayList<ModuleParameter>();

	public ParseTree getLastSectionRoot() {
		return lastSectionRoot;
	}

	public void setLastSectionRoot(final ParseTree lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}

	public List<ModuleParameter> getModuleParameters() {
		return moduleParameters;
	}

	public void setModuleParameters(final List<ModuleParameter> moduleParameters) {
		this.moduleParameters = moduleParameters;
	}
}
