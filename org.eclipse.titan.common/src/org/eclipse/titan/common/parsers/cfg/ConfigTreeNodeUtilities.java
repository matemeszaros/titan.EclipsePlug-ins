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

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.WritableToken;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.AddedParseTree;

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
	 * @param aRoot the root of subtree
	 * @return the String value of the AST
	 */
	public static String toString( final ParseTree aRoot ) {
		return aRoot.getText();
	}
	

	public static String toStringWithhiddenAfter( final ParserRuleContext aParseTreeRoot,
												  final TokenStream aTokenStream ) {
		final StringBuilder builder = new StringBuilder();

		print(builder, aParseTreeRoot, aTokenStream );
		// there are no hidden tokens after the last token

		return builder.toString();
	}
	
	private static final void print( final StringBuilder aSb,
									 final ParserRuleContext aParseTreeRoot,
									 final TokenStream aTokenStream ){
		print( aParseTreeRoot, aTokenStream, aSb, null );
	}
	
	//TODO: remove if not needed
	/**
	 * Builds hidden tokens after a rule
	 * @param aSb (in/out) StringBuilder, where the rule text is written
	 * @param aParseTreeRoot the rule (this will NOT be printed, only the hidden tokens after it)
	 * @param aTokenStream token stream to get the tokens from (all, hidden and not hidden also)
	 */
	private static final void appendHiddenAfter( final StringBuilder aSb,
												 final ParserRuleContext aParseTreeRoot,
												 final TokenStream aTokenStream ) {
		printHiddenAfter( aParseTreeRoot.stop, aTokenStream, aSb ); 
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
		if ( aParseTree == null ) {
			ErrorReporter.logWarning("ConfigTreeNodeUtilities.print(): aParseTree == null");
			return;
		}
		if ( aParseTree instanceof ParserRuleContext ) {
			final ParserRuleContext rule = (ParserRuleContext)aParseTree;
			if ( aDisallowedNodes != null && aDisallowedNodes.contains( rule.start.getType() ) ) {
				return;
			}
			if ( rule.getChildCount() > 0 && rule.getChild(0) instanceof AddedParseTree ) {
				//special case: if AddedParseTree is the 1st in the rule, it has no information
				// about the hidden tokens, as it has no position in the token stream, but the rule may have
				printHiddenTokensBefore(rule, aTokenStream, aSb);
			}
		}
		else if ( aParseTree instanceof TerminalNodeImpl ) {
			final TerminalNodeImpl tn = (TerminalNodeImpl)aParseTree;
			final Token token = tn.getSymbol();
			if ( aDisallowedNodes == null || !aDisallowedNodes.contains( token.getType() ) ) {
				print( token, aTokenStream, aSb );
			}
		}
		else if ( aParseTree instanceof AddedParseTree ) {
			final AddedParseTree t = (AddedParseTree)aParseTree;
			aSb.append( t.getText() );
		}
		else {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.print(): unexpected ParseTree type");
		}
		
		for ( int i = 0; i < aParseTree.getChildCount(); i++ ) {
			ParseTree child = aParseTree.getChild( i );
			if ( child == aParseTree ) {
				ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.print(): child == aParseTree");
			} else {
				print( child, aTokenStream, aSb, aDisallowedNodes );
			}
		}
	}
	
	/**
	 * Builds token text including hidden tokens before the token
	 * @param aToken token to print
	 * @param aTokenStream token stream to get the tokens from (all, hidden and not hidden also)
	 * @param aSb (in/out) StringBuilder, where the rule text is written
	 */
	public static void print( final Token aToken,
							  final TokenStream aTokenStream,
							  final StringBuilder aSb ) {
		final int startIndex = aToken.getTokenIndex();
		if ( startIndex == -1 ) {
			// Token has no index.
			// If a token is added to the parse tree after parse time, token start index in unknown (-1),
			// because token has no index in the token stream.
			final String tokenText = aToken.getText();
			aSb.append( tokenText != null ? tokenText : "" );
			return;
		}
		int startHiddenIndex = startIndex;
		while ( isHiddenToken( startHiddenIndex - 1, aTokenStream ) ) {
			startHiddenIndex--;
		}
		for ( int i = startHiddenIndex; i <= startIndex; i++ ) {
			final Token t = aTokenStream.get( i );
			final String tokenText = t.getText();
			aSb.append( tokenText != null ? tokenText : "" );
		}
	}

	/**
	 * Builds hidden tokens after a token
	 * @param aToken last token (this will NOT be printed, only the hidden tokens after it)
	 * @param aTokenStream token stream to get the tokens from (all, hidden and not hidden also)
	 * @param aSb (in/out) StringBuilder, where the rule text is written
	 */
	public static void printHiddenAfter( final Token aToken,
										 final TokenStream aTokenStream,
										 final StringBuilder aSb ) {
		final int stopIndex = aToken.getTokenIndex();
		if ( stopIndex == -1 ) {
			// Token has no index.
			// If a token is added to the parse tree after parse time, token start index in unknown (-1),
			// because token has no index in the token stream.
			return;
		}
		int stopHiddenIndex = stopIndex;
		while ( isHiddenToken( stopHiddenIndex + 1, aTokenStream ) ) {
			stopHiddenIndex++;
		}
		for ( int i = stopIndex + 1; i <= stopHiddenIndex; i++ ) {
			final Token t = aTokenStream.get( i );
			final String tokenText = t.getText();
			aSb.append( tokenText != null ? tokenText : "" );
		}
	}

	/**
	 * @param aIndex token index to check
	 * @param aTokenStream token stream, where tokens can be accessed by index
	 * @return true, iff token index is valid AND token is hidden
	 */
	private static boolean isHiddenToken( final int aIndex, final TokenStream aTokenStream ) {
		if ( aTokenStream == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.isHiddenToken(): aTokenStream == null");
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
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.addChild(): aParent == null");
			return;
		}
		if ( aChild == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.addChild(): aChild == null");
			return;
		}
		if ( aParent == aChild ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.addChild(): aParent == aChild");
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
			setParent( aChild, rule);
		} else {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.addChild(): only ParserRuleContext can have children");
		}
	}
	
	public static void setParent( final ParseTree aChild, final ParserRuleContext aParent ) {
		if ( aChild == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.setParent(): aChild == null");
			return;
		}
		if ( aChild == aParent ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.setParent(): aChild == aParent");
			return;
		}
		if ( aChild instanceof ParserRuleContext ) {
			final ParserRuleContext rule = (ParserRuleContext)aChild;
			rule.parent = aParent;
			
		} else if ( aChild instanceof TerminalNodeImpl ) {
			final TerminalNodeImpl tn = (TerminalNodeImpl)aChild;
			tn.parent = aParent;
		} else if ( aChild instanceof AddedParseTree ) {
			final AddedParseTree t = (AddedParseTree)aChild;
			t.setParent( aParent );
		} else {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.setParent(): unhandled ParseTree class type");
		}
	}
	
	//TODO: remove if not needed
	/**
	 * Removes child from parent's list.
	 * Parent is get from child data.
	 * NOTE: Use the 2 parameter version if possible.
	 *       getParent() should always be filled, but it's safer to name the parent implicitly.
	 * @param aChild child element to remove
	 */
	public static void removeChild( final ParseTree aChild ) {
		if ( aChild == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChild( ParseTree ): aChild == null");
			return;
		}
		final ParseTree parent = aChild.getParent();
		removeChild( parent, aChild );
	}
	
	/**
	 * Removes child from parent's list
	 * @param aParent parent node to remove the child from
	 * @param aChild child element to remove
	 */
	public static void removeChild( final ParseTree aParent, final ParseTree aChild ) {
		if ( aParent == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChild( ParseTree, ParseTree ): aParent == null");
			return;
		}
		if ( aChild == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChild( ParseTree, ParseTree ): aChild == null");
			return;
		}
		if ( aParent != aChild.getParent() ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChild( ParseTree, ParseTree ): aParent != aChild.getParent()");
		}
		if ( aParent instanceof ParserRuleContext ) {
			final ParserRuleContext rule = (ParserRuleContext)aParent;
			if ( rule.children != null && aChild != null && aChild.getText() != null ) {
				//delete child by text
				final List<ParseTree> list = rule.children;
				final int size = list.size();
				final String childText = aChild.getText();
				//NOTE: do NOT start from back, because it deletes by text
				//      and the 1st occurrence must be deleted
				for ( int i = 0; i < size; i++ ) {
					if ( childText.equals( list.get( i ).getText() ) ) {
						list.remove( i );
						break;
					}
				}
			}
		} else {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChild( ParseTree, ParseTree ): only ParserRuleContext can have children");
		}
	}
	
	/**
	 * Removes child and a separator ("|") before or after the child (if any) from parent's list
	 * @param aParent parent node to remove the child from
	 * @param aChild child element to remove
	 */
	public static void removeChildWithSeparator( final ParseTree aParent, final ParseTree aChild ) {
		if ( aParent == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChildWithSeparator( ParseTree, ParseTree ): aParent == null");
			return;
		}
		if ( aParent instanceof ParserRuleContext ) {
			final ParserRuleContext rule = (ParserRuleContext)aParent;
			if ( rule.children != null && aChild != null && aChild.getText() != null ) {
				//delete child by text
				final List<ParseTree> list = rule.children;
				final int size = list.size();
				final String childText = aChild.getText();
				//NOTE: do NOT start from back, because it deletes by text
				//      and the 1st occurrence must be deleted
				for ( int i = 0; i < size; i++ ) {
					if ( childText.equals( list.get( i ).getText() ) ) {
						list.remove( i );
						final String separator = "|";
						if ( i > 0 ) {
							final ParseTree previous = list.get( i - 1 );
							if ( separator.equals( previous.getText() ) ) {
								list.remove( i - 1 );
							}
						} else if( size > 1 ) {
							// i == 0, but let's check also, if this is not the last item,
							// because in that case there is no more separator, there is nothing to remove
							// NOTE: remember, that list size just decreased by 1 now
							final ParseTree next = list.get( 0 );
							if ( separator.equals( next.getText() ) ) {
								list.remove( 0 );
							}
						}
						break;
					}
				}
			}
		} else {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChild( ParseTree, ParseTree ): only ParserRuleContext can have children");
		}
	}
	
	//TODO: remove if not needed
	/**
	 * Removes children from parent's list
	 * @param aParent parent node to remove the child from
	 * @param aChild child element to remove
	 */
	public static void removeChildren( final ParseTree aParent ) {
		if ( aParent == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChildren(): aParent == null");
			return;
		}
		if ( aParent instanceof ParserRuleContext ) {
			final ParserRuleContext rule = (ParserRuleContext)aParent;
			rule.children = null;
		} else {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChildren(): only ParserRuleContext can have children");
		}
	}
	
	/**
	 * Changes the text of a parse tree
	 * @param aParseTree parse tree to modify
	 * @param aText new text
	 */
	public static void setText( final ParseTree aParseTree, final String aText ) {
		if ( aParseTree == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.setText(): aParseTree == null");
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
			ParseTree newNode = new AddedParseTree( aText );
			addChild(rule, newNode);
		} else if ( aParseTree instanceof AddedParseTree ) {
			final AddedParseTree node = (AddedParseTree)aParseTree;
			node.setText( aText );
		} else if ( aParseTree instanceof TerminalNodeImpl ) {
			final TerminalNodeImpl node = (TerminalNodeImpl)aParseTree;
			final Token t = node.symbol;
			if ( t instanceof WritableToken ) {
				final WritableToken ct = (WritableToken)t;
				ct.setText(aText);
			} else {
				ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.setText(): unhandled token class type");
			}
		} else {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.setText(): unhandled ParseTree class type");
		}
	}

	private static void printHiddenTokensBefore( final ParserRuleContext aRule,
												 final TokenStream aTokenStream,
												 final StringBuilder aSb) {
		Token startToken = aRule.start;
		final int startIndex = startToken.getTokenIndex();
		if ( startIndex == -1 ) {
			// Token has no index.
			// If a token is added to the parse tree after parse time, token start index in unknown (-1),
			// because token has no index in the token stream.
			return;
		}
		int startHiddenIndex = startIndex;
		while ( isHiddenToken( startHiddenIndex - 1, aTokenStream ) ) {
			startHiddenIndex--;
		}
		for ( int i = startHiddenIndex; i < startIndex; i++ ) {
			final Token t = aTokenStream.get( i );
			final String tokenText = t.getText();
			aSb.append( tokenText != null ? tokenText : "" );
		}
	}

	/**
	 * Creates a new hidden token node, which can be added to a ParseTree
	 * @param aText token text
	 */
	public static TerminalNodeImpl createHiddenTokenNode( final String aText ) {
		return new TerminalNodeImpl( new CommonToken( 0, aText ) );
	}
}
