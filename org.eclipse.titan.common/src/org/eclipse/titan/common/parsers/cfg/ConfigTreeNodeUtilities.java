/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.eclipse.titan.common.parsers.AddedParseTree;
import org.eclipse.titan.common.parsers.CommonHiddenStreamToken;
import org.eclipse.titan.common.parsers.LocationAST;

/**
 * Basic utility class for configuration file AST operations.
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
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
		final StringBuilder builder = new StringBuilder();

		print(builder, root);
		return builder.toString();
	}
	
	public static String toStringWithhiddenAfter(final LocationAST root){
		final StringBuilder builder = new StringBuilder();

		print(builder, root);
		appendHiddenAfter(builder, root);

		return builder.toString();
	}
	
	public static String toStringWithoutChildren(final LocationAST root){
		final StringBuilder builder = new StringBuilder();

		appendHiddenBefore(builder, root);
		builder.append(root.getText());
		
		return builder.toString();
	}
	
	public static String getHiddenBefore(final LocationAST root){
		final StringBuilder builder = new StringBuilder();

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
			final LocationAST child = from.getFirstChild();
			if(child != null){
				to.setHiddenBefore(child.getHiddenBefore());
				child.setHiddenBefore(null);
			}
		}else{
			to.setHiddenBefore(from.getHiddenBefore());
			from.setHiddenBefore(null);
		}
	}
	
	private static final void print(final StringBuilder aSb, final LocationAST aRoot){
		print( aRoot.getRule(), aRoot.getTokenStream(), aSb, null );
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
	
	private static final void appendHiddenBefore( final ParserRuleContext aRule,
												  final CommonTokenStream aTokens ) {
		Token firstToken = aRule.getStart();
		List<Token> hiddenTokens = aTokens.getHiddenTokensToLeft( firstToken.getTokenIndex() );
		StringBuilder sb = new StringBuilder();
		for (Token token : hiddenTokens) {
			sb.append( token.getText() );
		}
		TokenStreamRewriter rewriter = new TokenStreamRewriter( aTokens );
		rewriter.insertBefore( firstToken, sb.toString() );
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
			print(internalBuilder,child);
			child = child.getNextSibling();
		}

		return internalBuilder;
	}
	
	/**
	 * RECURSIVE
	 * Builds parse tree text including hidden tokens (also before the rule)
	 * @param aParseTree parse tree
	 * @param aTokenStream token stream to get the tokens from (all, hidden and not hidden also)
	 * @param aSb (in/out) StringBuilder, where the rule text is written
	 * @param aDisallowedNodes token types, which are not printed (also their children), it can be null
	 */
	public static void print( final ParseTree aParseTree,
							  final TokenStream aTokenStream,
							  final StringBuilder aSb,
							  final List<Integer> aDisallowedNodes ) {
		if ( aParseTree instanceof ParserRuleContext ) {
			final ParserRuleContext rule = (ParserRuleContext)aParseTree;
			if ( aDisallowedNodes != null && aDisallowedNodes.contains( rule.start.getType() ) ) {
				return;
			}
		}
		else if ( aParseTree instanceof TerminalNodeImpl ) {
			final TerminalNodeImpl tn = (TerminalNodeImpl)aParseTree;
			final Token token = tn.getSymbol();
			if ( aDisallowedNodes == null || !aDisallowedNodes.contains( token.getType() ) ) {
				print( token, aTokenStream, aSb, aDisallowedNodes );
			}
		}
		else if ( aParseTree instanceof AddedParseTree ) {
			final AddedParseTree t = (AddedParseTree)aParseTree;
			aSb.append( t.getText() );
		}
		else {
			//TODO: program error: unexpected ParseTree type
		}
		
		for ( int i = 0; i < aParseTree.getChildCount(); i++ ) {
			ParseTree child = aParseTree.getChild( i );
			print( child, aTokenStream, aSb, aDisallowedNodes );
		}
	}
	
	/**
	 * Builds token text including hidden tokens before the token
	 * @param aToken token to print
	 * @param aTokenStream token stream to get the tokens from (all, hidden and not hidden also)
	 * @param aSb (in/out) StringBuilder, where the rule text is written
	 * @param aDisallowedNodes token types, which are not printed (also their children), it can be null
	 */
	public static void print( final Token aToken,
							  final TokenStream aTokenStream,
							  final StringBuilder aSb,
							  final List<Integer> aDisallowedNodes ) {
		final int startIndex = aToken.getTokenIndex();
		int startHiddenIndex = startIndex;
		while ( isHiddenToken( startHiddenIndex - 1, aTokenStream ) ) {
			startHiddenIndex--;
		}
		if ( aTokenStream == null ) {
			//TODO: program error
			return;
		}
		for ( int i = startHiddenIndex; i <= startIndex; i++ ) {
			final Token t = aTokenStream.get( i );
			if ( t != null ) {
				final String tokenText = t.getText();
				aSb.append( tokenText != null ? tokenText : "" );
			} else {
				//TODO: program error
			}
		}
	}

	/**
	 * @param aIndex token index to check
	 * @param aTokenStream token stream, where tokens can be accessed by index
	 * @return true, iff token index is valid AND token is hidden
	 */
	private static boolean isHiddenToken( final int aIndex, final TokenStream aTokenStream ) {
		if ( aTokenStream == null ) {
			//TODO: program error
			return false;
		}
		
		return aIndex >= 0 && aIndex < aTokenStream.size() && aTokenStream.get( aIndex ).getChannel() > 0;
	}
	
	/**
	 * Adds a child to a parse tree as last child
	 * @param aParent parent node to add the child to
	 * @param aChild child to add as parent's sibling
	 */
	public static void addChild( final ParseTree aParent, final ParseTree aChild ) {
		addChild( aParent, aChild, -1 );
	}
	
	/**
	 * Adds a child to a parse tree
	 * @param aParent parent node to add the child to
	 * @param aChild child to add as parent's sibling
	 * @param aIndex index, where child is added in the child list, special case: -1: added as last
	 */
	public static void addChild( final ParseTree aParent, final ParseTree aChild, final int aIndex ) {
		if ( aParent == null ) {
			//TODO: program error
			return;
		}
		if ( aParent instanceof ParserRuleContext ) {
			final ParserRuleContext rule = (ParserRuleContext)aParent;
			if ( rule.children == null ) {
				rule.children = new ArrayList<ParseTree>();
			}
			if ( aIndex >= 0 ) {
				rule.children.set(aIndex, aChild);
			} else {
				rule.children.add(aChild);
			}
		}
		else if ( aParent instanceof AddedParseTree ) {
			final AddedParseTree node = (AddedParseTree)aParent;
			if ( node.children == null ) {
				node.children = new ArrayList<ParseTree>();
			}
			if ( aIndex >= 0 ) {
				node.children.set(aIndex, aChild);
			} else {
				node.children.add(aChild);
			}
		}
		else if ( aParent instanceof TerminalNodeImpl ) {
			//TODO: program error
		}
		else {
			//TODO: program error
		}
	}
	
	/**
	 * Removes child form parent's list
	 * @param aParent parent node to remove the child from
	 * @param aChild child element to remove
	 */
	public static void removeChild( final ParseTree aParent, final ParseTree aChild ) {
		if ( aParent == null ) {
			//TODO: program error
			return;
		}
		if ( aParent instanceof ParserRuleContext ) {
			final ParserRuleContext rule = (ParserRuleContext)aParent;
			if ( rule.children != null ) {
				rule.children.remove(aChild);
			}
		}
		else if ( aParent instanceof AddedParseTree ) {
			final AddedParseTree node = (AddedParseTree)aParent;
			if ( node.children != null ) {
				node.children.remove(aChild);
			}
		}
		else if ( aParent instanceof TerminalNodeImpl ) {
			//TODO: program error
		}
		else {
			//TODO: program error
		}
	}
	
	/**
	 * Changes the text of a parse tree
	 * @param aParseTree parsetree to modify
	 * @param aText new text
	 */
	public static void setText( final ParseTree aParseTree, final String aText ) {
		String text = "\n" + aText;
		if ( aParseTree == null ) {
			//TODO: program error
			System.out.println("ERROR: ConfigTreeNodeUtilities.setText() aParseTree == null");
			return;
		}
		if ( aParseTree instanceof ParserRuleContext ) {
			final ParserRuleContext rule = (ParserRuleContext)aParseTree;
			// in case of a rule we don't want to keep the original sub-tree structure,
			// just delete it and replace the children with an AddedParseTree  
			if ( rule.children != null ) {
				rule.children.clear();
			} else {
				rule.children = new ArrayList<ParseTree>();
			}
			ParseTree newNode = new AddedParseTree( text );
			addChild(rule, newNode);
		}
		else if ( aParseTree instanceof AddedParseTree ) {
			final AddedParseTree node = (AddedParseTree)aParseTree;
			node.setText( text );
		}
		else if ( aParseTree instanceof TerminalNodeImpl ) {
			//TODO: program error
		}
		else {
			//TODO: program error
		}
	}
}
