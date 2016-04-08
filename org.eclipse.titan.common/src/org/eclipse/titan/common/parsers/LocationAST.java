/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

//TODO: rename to CfgParseTree, inherit from ParserRuleContext
public class LocationAST {

	private String mText;
	private ParserRuleContext mRule;
	private CommonHiddenStreamToken mHiddenAfter;
	private CommonHiddenStreamToken mHiddenBefore;
	
	public LocationAST(final String aText) {
		mRule = new ParserRuleContext();
		setText( aText );
	}

	public LocationAST(final ParserRuleContext aRule) {
		mRule = aRule;
	}

	public LocationAST(final Token aToken) {
		mRule = new ParserRuleContext();
		mRule.addChild(aToken);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		sb.append(mText+", ");
		if ( mRule != null ) {
			sb.append("{ " + mRule.start + ", ");
			sb.append("" + mRule.stop + ", ");
			sb.append("" + mRule.getChildCount() + ", " );
			sb.append(mRule.getText() + "}" );
		} else {
			sb.append("null");
		}
		sb.append(", " );
		
		sb.append(mHiddenAfter+", ");
		sb.append(mHiddenBefore+"}");
		return sb.toString();
	}

	public String getText() {
		if ( mText != null ) {
			return mText;
		}

		return mRule != null ? mRule.getText() : null;
	}
	
	public void setText(final String aText) {
		mText = aText;
	}
	
	public ParserRuleContext getRule() {
		return mRule;
	}
	
	public LocationAST getParent() {
		final ParserRuleContext parentRule = mRule != null ? mRule.getParent() : null;

		return new LocationAST( parentRule );
	}

	public LocationAST getFirstChild() {
		if ( mRule == null ) {
			return null;
		}
		
		final ParserRuleContext parent = mRule.getParent();
		if ( parent == null || parent.getChildCount() == 0 ) {
			return null;
		}
		
		final ParseTree firstParseTree = parent.getChild(0);
		if ( ! ( firstParseTree instanceof ParserRuleContext ) ) {
			return null;
		}
		
		final ParserRuleContext firstRule = (ParserRuleContext) firstParseTree;

		return new LocationAST( firstRule );
	}

	public void setFirstChild(final LocationAST aNode) {
		if ( mRule == null ) {
			return;
		}
		
		if ( mRule.children == null ) {
			mRule.children = new ArrayList<ParseTree>();
		}

		final List<ParseTree> children = mRule.children;
		children.set(0, aNode.getRule());
	}

	public LocationAST getNextSibling() {
		final int childIndex = getChildIndex();
		if ( childIndex < 0 ) {
			// mRule or mRule.getParent() or mRule.getParent().children is null
			return null;
		}
		
		final ParserRuleContext parent = mRule.getParent();
		if ( childIndex + 1 >= parent.getChildCount() ) {
			// current node is the last child, there is no next sibling
			return null;
		}
		
		final ParseTree nextParseTree = parent.getChild( childIndex + 1 );
		if ( ! ( nextParseTree instanceof ParserRuleContext ) ) {
			return null;
		}
		
		final ParserRuleContext nextRule = (ParserRuleContext) nextParseTree;

		return new LocationAST( nextRule );

	}

	public void setNextSibling(final LocationAST aNode ) {
		final int childIndex = getChildIndex();
		if ( childIndex < 0 ) {
			// mRule or mRule.getParent() or mRule.getParent().children is null
			return;
		}
		
		final List<ParseTree> children = mRule.getParent().children;
		children.set(childIndex + 1, aNode.getRule());
	}

	public void addChild(final LocationAST aNode) {
		if ( mRule == null ) {
			return;
		}
		
		if ( mRule.children == null ) {
			mRule.children = new ArrayList<ParseTree>();
		}

		final List<ParseTree> children = mRule.children;
		children.add(aNode.getRule());
	}

	public void removeChildren() {
		if ( mRule == null ) {
			return;
		}
		
		final List<ParseTree> children = mRule.children;
		if ( children != null ) {
			children.clear();
		}
	}

	public CommonHiddenStreamToken getHiddenBefore() {
		return mHiddenBefore;
	}

	public void setHiddenBefore(final CommonHiddenStreamToken aToken) {
		mHiddenBefore = aToken;
	}

	public CommonHiddenStreamToken getHiddenAfter() {
		return mHiddenAfter;
	}

	public void setHiddenAfter(final CommonHiddenStreamToken aToken) {
		mHiddenAfter = aToken;
	}
	
	/**
	 * @return the index of the current node in its parent's child list
	 *         -1 if there is no parent (root node) 
	 */
	private int getChildIndex() {
		if ( mRule == null ) {
			return -1;
		}
		
		final ParserRuleContext parent = mRule.getParent();
		if ( parent == null ) {
			return -1;
		}
		
		if ( parent.children == null ) {
			return -1;
		}
		
		for ( int i = 0; i < parent.getChildCount(); i++ ) {
			final ParseTree child = parent.getChild(i);
			if ( this.mRule == child ) {
				return i;
			}
		}
		
		// it should not happen, program error:
		//   children list is empty, or current node is not listed in the children list of its parent, or wrong parent
		return -1;
		
	}

	public int getType() {
		// TODO: implement
		return 0;
	}

}
