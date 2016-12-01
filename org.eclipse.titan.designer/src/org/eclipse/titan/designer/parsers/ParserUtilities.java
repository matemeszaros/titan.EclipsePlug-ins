package org.eclipse.titan.designer.parsers;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.titan.common.parsers.ILexerLogUtil;
import org.eclipse.titan.common.parsers.IPrinter;
import org.eclipse.titan.common.parsers.ParserLogger;
import org.eclipse.titan.common.parsers.TokenNameResolver;
import org.eclipse.titan.common.parsers.cfg.CfgParser;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.extensionattributeparser.ExtensionAttributeParser;
import org.eclipse.titan.designer.parsers.ttcn3parser.PreprocessorDirectiveParser;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Parser;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Reparser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.preferences.SubscribedBoolean;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * Utility functions for Parser classes for logging
 * @author Arpad Lovassy
 */
public class ParserUtilities {

	/**
	 * true, if and only if parse tree logging is switched on in Titan Preferences Debug Console
	 */
	private static SubscribedBoolean sParseTreeLogged = new SubscribedBoolean( ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DEBUG_CONSOLE_PARSE_TREE, false );

	/**
	 * @return true if parse tree logging is set in TITAN Preferences, which can be switched on at
	 *         Preferences/TITAN Preferences/Debug/Debug console/Enable debug console/Log parse tree
	 *         NOTE: Preferences/TITAN Preferences/Display debug preferences must be switched on to see Debug console menu
	 */
	public static final boolean isParseTreeLogged() {
		return sParseTreeLogged.getValue();
	}

	/**
	 * Sets Parser._buildParseTree according to TITAN Preferences: true, if parse tree logging is switched on
	 * @param aParser parser
	 */
	public static final void setBuildParseTree( final Parser aParser ) {
		aParser.setBuildParseTree( isParseTreeLogged() );
	}

	/**
	 * Logs a parse tree.
	 * Token name is resolved if ...LexerLogUtil.java is generated,
	 * otherwise only token type index is displayed
	 * @param aRoot parse tree
	 * @param aParser parser to get rule names
	 */
	public static void logParseTree( final ParseTree aRoot,
									 final Parser aParser ) {
		if ( !aParser.getBuildParseTree() ) {
			// Parse tree logging is not requested
			return;
		}
		final String description = getDescription( aParser );
		final String lexerLogUtilClassName = getLexerLogUtilClassName( aParser );
		final TokenNameResolver tokenNameResolver = getTokenNameResolver( lexerLogUtilClassName );
		final IPrinter p = TitanDebugConsolePrinter.INSTANCE;
		ParserLogger.log( aRoot, aParser, tokenNameResolver, p, description );

	}

	/**
	 * @param aParser parser instance
	 * @return gets the description for the parse tree logging header
	 */
	private static String getDescription( final Parser aParser ) {
		if ( aParser == null ) {
			return null;
		} else if ( aParser instanceof Ttcn3Parser ) {
			return "TTCN-3";
		} else if ( aParser instanceof Ttcn3Reparser ) {
			return "TTCN-3 incremental parsing";
		} else if ( aParser instanceof Asn1Parser ) {
			return "ASN.1";
		} else if ( aParser instanceof ExtensionAttributeParser ) {
			return "Extension Attribute";
		} else if ( aParser instanceof PreprocessorDirectiveParser ) {
			return "Preprocessor Directive";
		} else if ( aParser instanceof CfgParser ) {
			return "CFG";
		} else {
			return null;
		}
	}

	/**
	 * @param aParser parser instance
	 * @return gets the ...LexerLogUtil class name for the parser instance for token name resolving
	 */
	private static String getLexerLogUtilClassName( final Parser aParser ) {
		if ( aParser == null ) {
			return null;
		} else if ( aParser instanceof Ttcn3Parser ) {
			return "org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3LexerLogUtil";
		} else if ( aParser instanceof Ttcn3Reparser ) {
			return "org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3LexerLogUtil";
		} else if ( aParser instanceof Asn1Parser ) {
			return "org.eclipse.titan.designer.parsers.asn1parser.Asn1LexerLogUtil";
		} else if ( aParser instanceof ExtensionAttributeParser ) {
			return "org.eclipse.titan.designer.parsers.extensionattributeparser.ExtensionAttributeLexerLogUtil";
		} else if ( aParser instanceof PreprocessorDirectiveParser ) {
			return "org.eclipse.titan.designer.parsers.ttcn3parser.PreprocessorDirectiveLexerLogUtil";
		} else if ( aParser instanceof CfgParser ) {
			return "org.eclipse.titan.common.parsers.cfg.CfgLexerLogUtil";
		} else {
			return null;
		}
	}

	/**
	 * @param aLexerLogUtilClassName the full class name of the ...LexerLogUtil class. It is instantiated dynamically if exists.
	 * @return object to resolve lexer token names from token index
	 */
	private static TokenNameResolver getTokenNameResolver( final String aLexerLogUtilClassName ) {
		try {
			// ...LexerLogUtil is generated, it may not exist
			final Class<?> c = Class.forName( aLexerLogUtilClassName );
			final ILexerLogUtil o = (ILexerLogUtil)c.newInstance();
			return new TokenNameResolver( o );
		} catch ( final Exception e ) {
			return new TokenNameResolver();
		}
	}
}
