/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
import org.antlr.v4.runtime.WritableToken;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.AddedParseTree;

/**
 * Basic utility class for configuration file AST and parse tree operations.
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
	
	/**
	 * Sets the parent of a child node
	 * @param aChild child node to modify
	 * @param aParent parent rule, NOT null
	 */
	private static void setParent( final ParseTree aChild, final ParserRuleContext aParent ) {
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
	
	/**
	 * Removes child from parent's list.
	 * Parent is get from child data.
	 * @param aChild child element to remove
	 */
	public static void removeChild( final ParseTree aChild ) {
		if ( aChild == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChild( ParseTree ): aChild == null");
			return;
		}
		final ParseTree parent = aChild.getParent();
		removeChild( parent, aChild, false );
	}

	/**
	 * Removes child from parent's list
	 * @param aParent parent node to remove the child from
	 * @param aChild child element to remove
	 */
	public static void removeChild( final ParseTree aParent, final ParseTree aChild ) {
		removeChild( aParent, aChild, true );
	}
	
	/**
	 * Removes child from parent's list
	 * @param aParent parent node to remove the child from
	 * @param aChild child element to remove
	 * @param aRetry true to retry with parent known by the child
	 */
	private static void removeChild( final ParseTree aParent, final ParseTree aChild, final boolean aRetry ) {
		if ( aParent == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChild( ParseTree, ParseTree ): aParent == null");
			return;
		}
		if ( aChild == null ) {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChild( ParseTree, ParseTree ): aChild == null");
			return;
		}
		if ( aParent instanceof ParserRuleContext ) {
			final ParserRuleContext rule = (ParserRuleContext)aParent;
			if ( rule.children != null && aChild.getText() != null ) {
				//delete child by text
				final List<ParseTree> list = rule.children;
				final int size = list.size();
				final String childText = aChild.getText();
				// true, if item to delete is found
				boolean found = false;
				//NOTE: do NOT start from back, because it deletes by text
				//      and the 1st occurrence must be deleted
				for ( int i = 0; i < size; i++ ) {
					if ( childText.equals( list.get( i ).getText() ) ) {
						list.remove( i );
						found = true;
						break;
					}
				}
				// if item to delete is not found, it means, that aParent is not the parent of aChild,
				// but a root of a rule, and the rule has more levels.
				// In this case it must be deleted in the other way. The child knows its parent
				if ( aRetry && !found ) {
					removeChild( aChild );
				}
			}
		} else {
			ErrorReporter.INTERNAL_ERROR("ConfigTreeNodeUtilities.removeChild( ParseTree, ParseTree ): only ParserRuleContext can have children");
		}
	}

	/**
	 * Removes child and a separator before or after the child (if any) from parent's list.
	 * Default case
	 * @param aParent parent node to remove the child from
	 * @param aChild child element to remove
	 */
	public static void removeChildWithSeparator( final ParseTree aParent, final ParseTree aChild ) {
		removeChildWithSeparator( aParent, aChild, "|", 0 );
	}
	/**
	 * Removes child and a separator before or after the child (if any) from parent's list
	 * @param aParent parent node to remove the child from
	 * @param aChild child element to remove
	 * @param aSeparator separator string
	 * @param aStartIndex the start index where the items start
	 *                    aStartIndex > 0 if rule contains some other parse tree nodes than normal list items
	 */
	public static void removeChildWithSeparator( final ParseTree aParent,
												 final ParseTree aChild,
												 final String aSeparator,
												 final int aStartIndex ) {
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
				for ( int i = aStartIndex; i < size; i++ ) {
					if ( childText.equals( list.get( i ).getText() ) ) {
						list.remove( i );
						if ( i > aStartIndex ) {
							final ParseTree previous = list.get( i - 1 );
							if ( aSeparator.equals( previous.getText() ) ) {
								list.remove( i - 1 );
							}
						} else if( size > aStartIndex + 1 ) {
							// i == aStartIndex, but let's check also, if this is not the last item,
							// because in that case there is no more separator, there is nothing to remove
							// NOTE: remember, that list size just decreased by 1 now
							final ParseTree next = list.get( aStartIndex );
							if ( aSeparator.equals( next.getText() ) ) {
								list.remove( aStartIndex );
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

	/**
	 * Creates a new hidden token node, which can be added to a ParseTree
	 * @param aText token text
	 */
	public static TerminalNodeImpl createHiddenTokenNode( final String aText ) {
		return new TerminalNodeImpl( new CommonToken( 0, aText ) );
	}
}
