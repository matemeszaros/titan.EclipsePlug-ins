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
public final class ComponentSectionHandler {
	private LocationAST lastSectionRoot = null;
	private List<Component> components = new ArrayList<Component>();

	public LocationAST getLastSectionRoot() {
		return lastSectionRoot;
	}

	public void setLastSectionRoot(LocationAST lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}

	public List<Component> getComponents() {
		return components;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}

	public static class Component {
		private LocationAST root = null;
		private LocationAST componentName = null;
		private LocationAST hostName = null;

		public LocationAST getRoot() {
			return root;
		}

		public void setRoot(LocationAST root) {
			this.root = root;
		}

		public LocationAST getComponentName() {
			return componentName;
		}

		public void setComponentName(LocationAST componentName) {
			this.componentName = componentName;
		}

		public LocationAST getHostName() {
			return hostName;
		}

		public void setHostName(LocationAST hostName) {
			this.hostName = hostName;
		}
	}
}
