/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import org.eclipse.titan.common.parsers.CommonHiddenStreamToken;
import org.eclipse.titan.common.parsers.LocationAST;

/**
 * Basic utility class for configuration file AST operations.
 *
 * @author Kristof Szabados
 */
public final class ConfigTreeNodeUtilities {
	
	private ConfigTreeNodeUtilities() {
		// Disable constructor
	}

	/**
	 * Returns the string value of the given AST subtree.
	 * 
	 * @param root
	 *            the root of subtree
	 * @return the String value of the AST
	 * 		
	 */
	public static String toString(final LocationAST root){
		StringBuilder builder = new StringBuilder();

		print(builder, root);
		return builder.toString();
	}
	
	public static String toStringWithhiddenAfter(final LocationAST root){
		StringBuilder builder = new StringBuilder();

		print(builder, root);
		appendHiddenAfter(builder, root);

		return builder.toString();
	}
	
	public static String toStringWithoutChildren(final LocationAST root){
		StringBuilder builder = new StringBuilder();

		appendHiddenBefore(builder, root);
		builder.append(root.getText());
		
		return builder.toString();
	}
	
	public static String getHiddenBefore(LocationAST root){
		StringBuilder builder = new StringBuilder();

		appendHiddenBefore(builder, root);
		
		return builder.toString();
	}
	
	/**
	 * Removes an element from a chain of elements.
	 * <p>
	 * This implementation is based on the fact that the head of the list is the head of the syntactical unit.
	 * And all direct elements of the syntactical list are his siblings.
	 * 
	 * It is VERY IMPORTANT to use the == operator as the equals function is not checking the objects, but their texts.
	 * 
	 * @param chainStart the head of the chain
	 * @param what the element to be removed from the chain
	 * */
	public static final void removeFromChain(final LocationAST chainStart, final LocationAST what){
		if(chainStart == what){
			chainStart.setFirstChild(what.getNextSibling());
			return;
		}
		
		LocationAST node = chainStart;
		while(node != null){
			if(what == node.getNextSibling()){
				node.setNextSibling(what.getNextSibling());
				return;
			}
			
			node = node.getNextSibling();
		}
	}
	
	/**
	 * Moves the hidden before tokens of the source to the hidden before tokens of the target.
	 * */
	public static final void moveHiddenBefore2HiddenBefore(final LocationAST from, final LocationAST to) {
		if(from.getHiddenBefore() == null){
			LocationAST child = from.getFirstChild();
			if(child != null){
				to.setHiddenBefore(child.getHiddenBefore());
				child.setHiddenBefore(null);
			}
		}else{
			to.setHiddenBefore(from.getHiddenBefore());
			from.setHiddenBefore(null);
		}
	}
	
	private static final StringBuilder print(final StringBuilder builder, final LocationAST root){
		appendHiddenBefore(builder, root);
		builder.append(root.getText());
		appendChildren(builder, root);
		
		return builder;
	}
	
	private static final StringBuilder appendHiddenBefore(final StringBuilder builder, final LocationAST root) {
		CommonHiddenStreamToken hidden = root.getHiddenBefore();
		if(hidden != null){
			while(hidden.getHiddenBefore() != null){
				hidden = hidden.getHiddenBefore();
			}
			while(hidden != null){
				builder.append(hidden.getText());
				hidden = hidden.getHiddenAfter();
			}
		}
		
		return builder;
	}
	
	private static final StringBuilder appendHiddenAfter(final StringBuilder builder, final LocationAST root) {
		LocationAST child = root.getFirstChild();
		
		while(child != null && child.getNextSibling() != null){
			child = child.getNextSibling();
		}
		if(child != null) {
			appendHiddenAfter(builder, child);
		} else {
			CommonHiddenStreamToken hidden = root.getHiddenAfter();
			while(hidden != null){
				builder.append(hidden.getText());
				hidden = hidden.getHiddenAfter();
			}
		}
		
		return builder;
	}
	
	private static final StringBuilder appendChildren(final StringBuilder builder, final LocationAST root) {
		LocationAST child = root.getFirstChild();
		StringBuilder internalBuilder = builder;
		while(child != null){
			internalBuilder = print(internalBuilder,child);
			child = child.getNextSibling();
		}

		return internalBuilder;
	}
}
