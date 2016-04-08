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
public final class GroupSectionHandler {

	public static class Group {
		private LocationAST root = null;
		private LocationAST groupName = null;
		private List<GroupItem> groupItems = new ArrayList<GroupItem>();

		public LocationAST getRoot() {
			return root;
		}

		public void setRoot(final LocationAST root) {
			this.root = root;
		}

		public LocationAST getGroupName() {
			return groupName;
		}

		public void setGroupName(final LocationAST groupName) {
			this.groupName = groupName;
		}

		public List<GroupItem> getGroupItems() {
			return groupItems;
		}

		public void setGroupItems(final List<GroupItem> groupItems) {
			this.groupItems = groupItems;
		}
	}

	public static class GroupItem {
		private LocationAST item = null;

		public GroupItem(final LocationAST item) {
			this.item = item;
		}

		public LocationAST getItem() {
			return item;
		}

		public void setItem(final LocationAST item) {
			this.item = item;
		}
	}

	private LocationAST lastSectionRoot = null;
	private List<Group> groups = new ArrayList<Group>();

	public LocationAST getLastSectionRoot() {
		return lastSectionRoot;
	}

	public void setLastSectionRoot(final LocationAST lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(final List<Group> groups) {
		this.groups = groups;
	}

}
