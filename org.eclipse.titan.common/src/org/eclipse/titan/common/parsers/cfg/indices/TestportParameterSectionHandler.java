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
public final class TestportParameterSectionHandler {

	public static class TestportParameter {
		private ParseTree root = null;
		private ParseTree componentName = null;
		private ParseTree testportName = null;
		private ParseTree parameterName = null;
		private ParseTree value = null;

		public ParseTree getRoot() {
			return root;
		}

		public void setRoot(final ParseTree root) {
			this.root = root;
		}

		public ParseTree getComponentName() {
			return componentName;
		}

		public void setComponentName(final ParseTree componentName) {
			this.componentName = componentName;
		}

		public ParseTree getTestportName() {
			return testportName;
		}

		public void setTestportName(final ParseTree testportName) {
			this.testportName = testportName;
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
	private List<TestportParameter> testportParameters = new ArrayList<TestportParameter>();

	public ParseTree getLastSectionRoot() {
		return lastSectionRoot;
	}

	public void setLastSectionRoot(final ParseTree lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}

	public List<TestportParameter> getTestportParameters() {
		return testportParameters;
	}

	public void setTestportParameters(final List<TestportParameter> testportParameters) {
		this.testportParameters = testportParameters;
	}
}
