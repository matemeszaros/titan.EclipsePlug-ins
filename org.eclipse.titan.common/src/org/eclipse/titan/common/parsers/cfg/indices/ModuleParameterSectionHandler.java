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
public final class ModuleParameterSectionHandler {

	public static class ModuleParameter {
		private LocationAST root = null;
		private LocationAST moduleName = null;
		private LocationAST parameterName = null;
		private LocationAST value = null;

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

		public LocationAST getParameterName() {
			return parameterName;
		}

		public void setParameterName(final LocationAST parameterName) {
			this.parameterName = parameterName;
		}

		public LocationAST getValue() {
			return value;
		}

		public void setValue(final LocationAST value) {
			this.value = value;
		}
	}

	private LocationAST lastSectionRoot = null;
	private List<ModuleParameter> moduleParameters = new ArrayList<ModuleParameter>();

	public LocationAST getLastSectionRoot() {
		return lastSectionRoot;
	}

	public void setLastSectionRoot(final LocationAST lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}

	public List<ModuleParameter> getModuleParameters() {
		return moduleParameters;
	}

	public void setModuleParameters(final List<ModuleParameter> moduleParameters) {
		this.moduleParameters = moduleParameters;
	}
}
