/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.eclipse.titan.common.parsers.cfg.CfgInterval;

/**
 * USED FOR LOGGING PURPOSES.<br>
 * Logger utility to print parse tree and interval tree.
 * @author Arpad Lovassy
 */
public class ParserLogger {
	
	/**
	 * Logs a parse tree
	 * @param aRoot parse tree
	 * @param aParser parser to get rule names
	 * @param aTokens token list to get tokens by index (for getting tokens of a rule) 
	 * @param aLexerLogUtil resolver to get token name by method 1, see TokenNameResolver
	 */
	public static void log( final ParseTree aRoot,
							final Parser aParser,
							final List<Token> aTokens,
							final ILexerLogUtil aLexerLogUtil ) {
		log( aRoot, aParser, aTokens, new TokenNameResolver( aLexerLogUtil ) );
	}
	
	/**
	 * Logs a parse tree
	 * @param aRoot parse tree
	 * @param aParser parser to get rule names
	 * @param aTokens token list to get tokens by index (for getting tokens of a rule) 
	 * @param aLexer resolver to get token name by method 2, see TokenNameResolver
	 */
	public static void log( final ParseTree aRoot,
							final Parser aParser,
							final List<Token> aTokens,
							final Lexer aLexer ) {
		log( aRoot, aParser, aTokens, new TokenNameResolver( aLexer ) );
	}
	
	/**
	 * Logs a parse tree, token name is not resolved, only token type index is displayed
	 * @param aRoot parse tree
	 * @param aParser parser to get rule names
	 * @param aTokens token list to get tokens by index (for getting tokens of a rule) 
	 */
	public static void log( final ParseTree aRoot,
							final Parser aParser,
							final List<Token> aTokens ) {
		log( aRoot, aParser, aTokens, new TokenNameResolver() );
	}
	
	/**
	 * Logs a parse tree
	 * @param aRoot parse tree
	 * @param aParser parser to get rule names
	 * @param aTokens token list to get tokens by index (for getting tokens of a rule) 
	 * @param aTokenNameResolver resolver to get token name
	 */
	private static void log( final ParseTree aRoot,
							 final Parser aParser,
							 final List<Token> aTokens,
							 final TokenNameResolver aTokenNameResolver ) {
		log( aRoot, aParser, aTokens, aTokenNameResolver, 0, false );
	}
	
	/**
	 * Logs a parse tree.
	 * Internal version.
	 * RECURSIVE
	 * @param aRoot parse tree
	 * @param aParser parser to get rule name
	 * @param aTokens token list to get tokens by index (for getting tokens of a rule) 
	 * @param aTokenNameResolver resolver to get token name
	 * @param aLevel indentation level
	 * @param aParentOneChild parent has 1 child
	 */
	private static void log( final ParseTree aRoot,
							 final Parser aParser,
							 final List<Token> aTokens,
							 final TokenNameResolver aTokenNameResolver,
							 final int aLevel,
							 final boolean aParentOneChild ) {
		if ( aRoot == null ) {
			println( "ERROR: ParseTree root is null" );
			return;
		}
		if ( !aParser.getBuildParseTree() ) {
			println( "ERROR: ParseTree is not build. Call Parser.setBuildParseTree( true ); BEFORE parsing. Or do NOT call Parser.setBuildParseTree( false );" );
			return;
		}
		if ( aRoot instanceof ParserRuleContext ) {
			final ParserRuleContext rule = (ParserRuleContext)aRoot;
			final String ruleInfo = getRuleInfo( rule, aParser, aTokenNameResolver ); 
			if ( aParentOneChild ) {
				printArrow( ruleInfo );
			} else {
				printIndent( ruleInfo, aLevel );
			}
			final int count = rule.getChildCount();
			final boolean oneChild = count == 1 && rule.exception == null;
			if ( !oneChild ) {
				print( ": '" + getEscapedRuleText( rule, aTokens ) + "'" );
				println();
			}
			for( int i = 0; i < count; i++ ) {
				final ParseTree child = rule.getChild( i );
				log( child, aParser, aTokens, aTokenNameResolver, aLevel + ( aParentOneChild ? 0 : 1 ), oneChild );
			}
		}
		else if ( aRoot instanceof TerminalNodeImpl ) {
			final TerminalNodeImpl tn = (TerminalNodeImpl)aRoot;
			if ( aParentOneChild ) {
				print( ": '" + getEscapedTokenText( tn.getSymbol() ) + "'" );
				println();
			}
			printIndent( getTokenInfo( tn.getSymbol(), aTokenNameResolver ), aLevel );
			if ( tn.parent == null ) {
				print(", parent == null <-------------------------------------------------------------- ERROR");
			}
			println();
		}
		else if ( aRoot instanceof AddedParseTree ) {
			final AddedParseTree apt = (AddedParseTree)aRoot;
			if ( aParentOneChild ) {
				print( ": '" + getEscapedText( apt.getText() ) + "'" );
				println();
			}
			printIndent( "AddedParseString: " + getEscapedText( apt.getText() ), aLevel );
			if ( apt.getParent() == null ) {
				print(", parent == null <-------------------------------------------------------------- ERROR");
			}
			println();
		}
		else {
			println( "ERROR: INVALID ParseTree type" );
		}
	}
	
	/**
	 * Rule exception info in string format for logging purpose
	 * @param aRule rule
	 * @param aTokenNameResolver resolver to get token name
	 * @return exception stack trace + some other info from the exception object
	 */
	private static String getExceptionInfo( final ParserRuleContext aRule, final TokenNameResolver aTokenNameResolver ) {
		final RecognitionException e = aRule.exception;
		if ( e == null ) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		sb.append( "\naRule.getText() == " + aRule.getText() );
		sb.append( "\ngetOffendingState() == " + e.getOffendingState() );
		sb.append( "\ngetExpectedTokens() == [" );
		final List<Integer> expectedTokens = e.getExpectedTokens().toList();
		for ( int i = 0; i < expectedTokens.size(); i++ ) {
			if ( i > 0 ) {
				sb.append(", ");
			}
			final int tokenType = expectedTokens.get( i );
			sb.append( getTokenName( tokenType, aTokenNameResolver ) );
		}
		sb.append("]");
		if ( e instanceof NoViableAltException ) {
			NoViableAltException nvae = (NoViableAltException)e;
			sb.append( "\ngetStartToken() == " + getTokenInfo( nvae.getStartToken(), aTokenNameResolver ) );
			sb.append( "\ngetDeadEndConfigs() == " + nvae.getDeadEndConfigs() );
		}
		
		final StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		sb.append( "\n" + errors.toString() );
		return sb.toString();
	}

	/**
	 * Rule info in string format for logging purpose
	 * @param aRule rule
	 * @param aParser parser to get rule name
	 * @param aTokenNameResolver resolver to get token name (used only if rule is erroneous) 
	 * @return rule name and exception and some other info if rule is erroneous
	 */
	private static String getRuleInfo( final ParserRuleContext aRule, final Parser aParser, final TokenNameResolver aTokenNameResolver ) {
		// only rule name
		String info = aParser.getRuleInvocationStack( aRule ).get( 0 );
		info += getExceptionInfo( aRule, aTokenNameResolver );
		return info;
	}
	
	/**
	 * Escaped rule text including hidden tokens
	 * For logging purposes
	 * @param aRule rule
	 * @param aTokens token list to get the tokens (all, hidden and not hidden also) from
	 * @return escaped rule text
	 */
	private static String getEscapedRuleText( final ParserRuleContext aRule,
											  final List<Token> aTokens ) {
		final Token startToken = aRule.start;
		if ( startToken == null ) {
			println( "ERROR: ParseLogger.getEscapedRuleText() startToken == null" );
			return "";
		}
		final int startIndex = startToken.getTokenIndex();
		final Token stopToken = aRule.stop;
		if ( stopToken == null ) {
			println( "ERROR: ParseLogger.getEscapedRuleText() stopToken == null" );
			return "";
		}
		final int stopIndex = stopToken.getTokenIndex();
		final StringBuilder sb = new StringBuilder();
		for ( int i = startIndex; i <= stopIndex; i++ ) {
			try {
				sb.append( getEscapedTokenText( aTokens.get( i ) ) );
			} catch ( IndexOutOfBoundsException e ) {
				sb.append("_");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Rule text including hidden tokens
	 * For logging purposes
	 * @param aRule rule
	 * @param aTokens token list to get the tokens (all, hidden and not hidden also) from
	 * @return rule text including hidden tokens. First and last tokens are non-hidden.
	 */
	public static String getRuleText( final ParserRuleContext aRule,
									  final List<Token> aTokens ) {
		final Token startToken = aRule.start;
		if ( startToken == null ) {
			println( "ERROR: ParseLogger.getEscapedRuleText() startToken == null" );
			return "";
		}
		final int startIndex = startToken.getTokenIndex();
		final Token stopToken = aRule.stop;
		if ( stopToken == null ) {
			println( "ERROR: ParseLogger.getEscapedRuleText() stopToken == null" );
			return "";
		}
		final int stopIndex = stopToken.getTokenIndex();
		final StringBuilder sb = new StringBuilder();
		for ( int i = startIndex; i <= stopIndex; i++ ) {
			sb.append( aTokens.get( i ) );
		}
		return sb.toString();
	}
	
	/**
	 * Token info in string format for logging purpose
	 * @param aToken token
	 * @param aTokenNameResolver resolver to get token name
	 * @return &lt;token name&gt;: '&lt;token text&gt;', @&lt;token index&gt;, &lt;line&gt;:&lt;column&gt;[, channel=&lt;channel&gt;]
	 *         <br>where
	 *         <br>&lt;token index&gt; starts  from 0,
	 *         <br>&lt;line&gt; starts from 1,
	 *         <br>&lt;column&gt; starts from 0,
	 *         <br>channel info is provided if &lt;channel&gt; > 0 (hidden channel)
	 */
	private static String getTokenInfo( final Token aToken, final TokenNameResolver aTokenNameResolver ) {
		final StringBuilder sb = new StringBuilder();
		final int tokenType = aToken.getType();
		final String tokenName = getTokenName( tokenType, aTokenNameResolver );
		sb.append( tokenName );
		sb.append( ": " );
		
		sb.append( "'" );
		sb.append( getEscapedTokenText( aToken ) );
		sb.append( "'" );
		
		sb.append( ", @" + aToken.getTokenIndex() );
		sb.append( ", " + aToken.getLine() + ":" + aToken.getCharPositionInLine() );
		if ( aToken.getChannel() > 0 ) {
			sb.append( ", channel=" );
			sb.append( aToken.getChannel() );
		}
		return sb.toString();
	}
	
	/**
	 * Gets escaped token text
	 * Escaped chars are converted to printable strings.
	 * @param aToken input token
	 * @return escaped token text
	 */
	private static String getEscapedTokenText( final Token aToken ) {
		return getEscapedText( aToken.getText() );
	}

	/**
	 * Gets escaped text
	 * Escaped chars are converted to printable strings.
	 * @param aText input text
	 * @return escaped text
	 */
	private static String getEscapedText( final String aText ) {
		String txt = aText;
		if ( txt == null ) {
			return "";
		}
		txt = txt.replace( "\n", "\\n" );
		txt = txt.replace( "\r", "\\r" );
		txt = txt.replace( "\t", "\\t" );
		return txt;
	}

	/**
	 * @param aTokenType token type index
	 * @param aTokenNameResolver token name resolver
	 * @return resolved token name
	 */
	private static String getTokenName( final int aTokenType, TokenNameResolver aTokenNameResolver ) {
		if ( aTokenNameResolver == null ) {
			aTokenNameResolver = new TokenNameResolver();
		}
		return aTokenNameResolver.getTokenName( aTokenType );
	}
	
	/**
	 * Logs an interval tree.
	 * @param aRootInterval the root of the interval tree
	 */
	public static void logInterval( final Interval aRootInterval ) {
		logInterval( aRootInterval, 0 );
	}
	
	/**
	 * Logs an interval tree.
	 * Internal version.
	 * RECURSIVE
	 * @param aRootInterval the root of the interval tree
	 * @param aLevel indentation level
	 */
	private static void logInterval( final Interval aRootInterval, final int aLevel ) {
		// root interval info
		StringBuilder sb = new StringBuilder();
		sb.append( "" + aRootInterval.getDepth() );
		sb.append( ", " + aRootInterval.getStartOffset() );
		sb.append( ", " + aRootInterval.getStartLine() );
		sb.append( ", " + aRootInterval.getEndOffset() );
		sb.append( ", " + aRootInterval.getEndLine() );
		sb.append( ", " + aRootInterval.getType() );
		if ( aRootInterval instanceof CfgInterval ) {
			sb.append( ", " + ( ( CfgInterval )aRootInterval).getSectionType() );
		}
		if ( aRootInterval.getErroneous() ) {
			sb.append( ", ERRONEOUS" );
		}
		printIndent( sb.toString(), aLevel );
		println();
		if ( aRootInterval.getSubIntervals() != null ) {
			for ( Interval interval : aRootInterval.getSubIntervals() ) {
				logInterval( interval, aLevel + 1 );
			}
		}
	}
	
	/**
	 * -> aMsg
	 * @param aMsg
	 */
	private static void printArrow( final String aMsg ) {
		print( " -> " );
		print( aMsg );
	}
	
	/**
	 * Prints message to console for logging purpose
	 * with level dependent indentation
	 * @param aMsg message
	 * @param aLevel indentation level
	 */
	private static void printIndent( final String aMsg, final int aLevel ) {
		final StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < aLevel; i++ ) {
			sb.append( "  " );
		}
		print( sb.toString() );
		print( aMsg );
	}

	private static void print( final String aMsg ) {
		System.out.print( aMsg );
	}

	private static void println() {
		System.out.println();
	}
	
	private static void println( final String aMsg ) {
		System.out.println( aMsg );
	}
	
}
