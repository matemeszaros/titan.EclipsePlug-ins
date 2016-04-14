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

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

//TODO: rename to CfgParseTree, inherit from ParserRuleContext
/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class LocationAST {

	private TokenStream mTokenStream;
	private ParserRuleContext mRule;
	private CommonHiddenStreamToken mHiddenAfter;
	private CommonHiddenStreamToken mHiddenBefore;
	
	public LocationAST( final String aText ) {
		setText( aText );
	}

	public LocationAST( final ParserRuleContext aRule, TokenStream aTokenStream ) {
		setRule( aRule );
		mTokenStream = aTokenStream;
	}

	public LocationAST( final ParserRuleContext aRule ) {
		setRule( aRule );
	}

	public LocationAST( final Token aToken ) {
		setToken( aToken );
	}
	
	private void setToken( Token aToken ) {
		ParserRuleContext rule = new ParserRuleContext();
		rule.addChild( aToken );
		setRule( rule );
	}
	
	private void setRule( ParserRuleContext aRule ) {
		mRule = aRule;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("{ ");
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
		String text = mRule != null ? mRule.getText() : null;
		return text;
	}
	
	public void setText( final String aText ) {
		CommonToken token = new CommonToken( 0, aText );
		setToken( token );
	}
	
	public ParserRuleContext getRule() {
		return mRule;
	}
	
	public LocationAST getParent() {
		final ParserRuleContext parentRule = mRule != null ? mRule.getParent() : null;

		return new LocationAST( parentRule, mTokenStream );
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

		return new LocationAST( firstRule, mTokenStream );
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

		return new LocationAST( nextRule, mTokenStream );

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
			// no parent (root node)
			return -1;
		}
		
		if ( parent.children == null ) {
			// it should not happen, program error:
			//   parent's children list is not filled
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
		//TODO: implement
		return 0;
	}

	public TokenStream getTokenStream() {
		return mTokenStream;
	}

	public void setTokenStream(TokenStream mTokenStream) {
		this.mTokenStream = mTokenStream;
	}

}
