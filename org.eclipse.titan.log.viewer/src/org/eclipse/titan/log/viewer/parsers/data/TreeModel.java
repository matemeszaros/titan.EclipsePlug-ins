/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers.data;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.titan.log.viewer.views.details.TreeLeaf;
import org.eclipse.titan.log.viewer.views.details.TreeObject;
import org.eclipse.titan.log.viewer.views.details.TreeParent;

public class TreeModel {

	private String value;

	private TreeParent invisibleRoot;

	public TreeModel() {
		this.invisibleRoot = new TreeParent("");
	}

	public void inputChanged(final String message) {
		if (message == null || message.length() == 0) {
			this.value = "";
			buildEmptyTree();
			return;
		}

		String newValue = message;

		if ((newValue.length() >= 2) && newValue.startsWith(": ")) {
			this.value = newValue.substring(2);
		} else {
			this.value = newValue;
		}

		buildTreeModel("Root node name");
	}

	private void buildEmptyTree() {
		this.invisibleRoot = new TreeParent("");
	}

	/**
	 *  Build the tree hierarchy.
	 * @param namePort
	 */
	private void buildTreeModel(final String namePort) {

		String logString = this.value;
		StringTokenizer tokenizer = new StringTokenizer(logString, "{},", true);
		Deque<TreeParent> parentStack = new ArrayDeque<TreeParent>();
		TreeParent root = new TreeParent(namePort);

		parentStack.add(root);

		if (!logString.startsWith("{")) {
			root.addChild(new TreeObject(logString.trim()));
		} else {

			String token = "";
			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken().trim();

				if (",".equals(token) || "".equals(token)) {
					continue;
				}

				if ("{".equals(token)) {
					continue;
				}


				if (token.endsWith("}")) {
					if (!parentStack.isEmpty()) {
						TreeParent tmpParent = parentStack.peek();
						parentStack.pop();
						if (!parentStack.isEmpty()) {
							parentStack.peek().addChild(tmpParent);
						} else {
							root.addChild(tmpParent);
						}
					}

					continue;
				}

				if (!(token.endsWith(":="))) {
					String[] arr = token.split(":=");

					if (arr.length == 2 && !parentStack.isEmpty()) {
						parentStack.peek().addChild(new TreeLeaf(arr[0].trim(), arr[1].trim()));
						continue;
					}

					root.addChild(new TreeObject(token));
				} else {
					parentStack.push(new TreeParent(token.substring(0, token.length() - ":=".length()).trim()));
				}

			}
		}

		this.invisibleRoot = new TreeParent("");
		this.invisibleRoot.addChild(root);

		parentStack.clear();
	}

	public String getFieldValue(final List<String> qualifiedName) {
		return getFieldValue(invisibleRoot.getChild("Root node name"), qualifiedName, 0);
	}

	private String getFieldValue(final TreeObject tree, final List<String> qualifiedName, final int idx) {
		if (qualifiedName == null || qualifiedName.isEmpty()) {
			return null;
		}
		if (tree instanceof TreeParent) {
			if (idx >= qualifiedName.size()) {
				return null;
			}
			TreeParent parent = (TreeParent) tree;
			TreeObject child = parent.getChild(qualifiedName.get(idx));
			if (child == null) {
				return null;
			}
			return getFieldValue(child, qualifiedName, idx + 1);
		}

		if (tree instanceof TreeLeaf) {
			if (idx != qualifiedName.size()) {
				return null;
			}
			return ((TreeLeaf) tree).getValue();
		}

		return null;
	}

	public String print() {
		return invisibleRoot.toString();
	}
}
