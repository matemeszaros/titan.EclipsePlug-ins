/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.details;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import org.eclipse.titan.log.viewer.console.TITANDebugConsole;

/**
 * This class builds the tree view 
 *  
 */
public class DetailsViewContentProvider implements ITreeContentProvider {

	private TreeParent invisibleRoot;
	private String value = ""; //$NON-NLS-1$
	private String sourceInfo = "";
	
	@Override
	public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
		if (newInput == null || oldInput == null) {
			this.value = ""; //$NON-NLS-1$
			buildTreeModel2();
			v.refresh();
			return;
		}

		DetailData oI = (DetailData) oldInput;
		DetailData nI = (DetailData) newInput;

		sourceInfo = nI.getSourceInfo();

		String oldValue = oI.getLine();
		String newValue = nI.getLine();

		if (newValue == null) {
			TITANDebugConsole.getConsole().newMessageStream().println("c");
			this.value = ""; //$NON-NLS-1$
			buildTreeModel2();
			v.refresh();
			return;
		}

		if (oldValue == null) {
			oldValue = ""; //$NON-NLS-1$
		}

		if ((newValue.length() >= 2) && newValue.startsWith(": ")) { //$NON-NLS-1$
			this.value = newValue.substring(2);
		} else {
			this.value = newValue;
		}

		String name = nI.getName();
		String port = nI.getPort();
		if ((port != null) && (port.trim().length() > 0)) {
			buildTreeModel(name + '(' + port + ')');
		} else {
			buildTreeModel(name);
		}
		v.refresh();
	}

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public Object[] getElements(final Object parent) {

		if (parent instanceof IViewSite) {
			return getChildren(this.invisibleRoot);

		}

		return getChildren(parent);
	}

	@Override
	public Object getParent(final Object child) {
		if (child instanceof TreeObject) {
			return ((TreeObject) child).getParent();
		}
		return null;
	}

	@Override
	public Object [] getChildren(final Object parent) {
		if (parent instanceof TreeParent) {
			return ((TreeParent) parent).getChildren();
		}
		return new Object[0];
	}

	@Override
	public boolean hasChildren(final Object parent) {
		return parent instanceof TreeParent
				&& ((TreeParent) parent).hasChildren();
	}

	/**
	 *  Build the tree hierarchy.
	 */
	private void buildTreeModel(final String namePort) {

		String logString = this.value;
		StringTokenizer tokenizer = new StringTokenizer(logString, "{},", true);
		Deque<TreeParent> parentStack = new ArrayDeque<TreeParent>();
		TreeParent root = new TreeParent(namePort);
		addSourceInfo(root);

		if (!logString.startsWith("{")) {
			root.addChild(new TreeObject(logString.trim()));
		} else {
			String prev = "";
			String token = "";
			while (tokenizer.hasMoreTokens()) {
				prev = token;
				token = tokenizer.nextToken().trim();

				if (",".equals(token) || "".equals(token)) {
					continue;
				}

	//			Set TreeParent named or unnamed and add to parentStack
				if ("{".equals(token)) {
					if (prev.endsWith(":=")) {
						String parentName = prev.substring(0, prev.indexOf(' '));
						parentStack.push(new TreeParent(parentName));
					} else {
						parentStack.push(new TreeParent(""));
					}
					continue;
				}


	//			End of TreeParent is found if this TreeParent has a parent
	//			we add it as it's child. If there is no parent we add it to
	//			root.
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

	//			add child to parent or root.
				if (!(token.endsWith(":="))) {

					if (!parentStack.isEmpty()) {
						parentStack.peek().addChild(new TreeObject(token));
						continue;
					}

					root.addChild(new TreeObject(token));
				}

			}
		}

		this.invisibleRoot = new TreeParent("");
		this.invisibleRoot.addChild(root);

		parentStack.clear();
	}

	private void addSourceInfo(TreeParent root) {
		if (this.sourceInfo != null && this.sourceInfo.trim().length() > 0) {
			root.addChild(new TreeObject("sourceInfo := " + this.sourceInfo));
		} else {
			root.addChild(new TreeObject("<SourceInfoFormat:=Single>"));
		}
	}

	/**
	 * Build an empty tree hierarchy.
	 */
	private void buildTreeModel2() {
		this.invisibleRoot = new TreeParent("");	//$NON-NLS-1$	
	}
}


