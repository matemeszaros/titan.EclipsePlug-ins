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
public final class ComponentSectionHandler extends ConfigSectionHandlerBase {
	private List<Component> components = new ArrayList<Component>();

	public List<Component> getComponents() {
		return components;
	}

	public void setComponents(final List<Component> components) {
		this.components = components;
	}

	public static class Component {
		private ParseTree root = null;
		private ParseTree componentName = null;
		private ParseTree hostName = null;

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

		public ParseTree getHostName() {
			return hostName;
		}

		public void setHostName(final ParseTree hostName) {
			this.hostName = hostName;
		}
	}
}
