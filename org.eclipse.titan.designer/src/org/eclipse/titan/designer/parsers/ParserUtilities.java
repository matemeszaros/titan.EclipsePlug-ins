package org.eclipse.titan.designer.parsers;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.ILexerLogUtil;
import org.eclipse.titan.common.parsers.IPrinter;
import org.eclipse.titan.common.parsers.ParserLogger;
import org.eclipse.titan.common.parsers.TokenNameResolver;
import org.eclipse.titan.common.parsers.cfg.CfgParser;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.parsers.asn1parser.Asn1Parser;
import org.eclipse.titan.designer.parsers.extensionattributeparser.ExtensionAttributeParser;
import org.eclipse.titan.designer.parsers.ttcn3parser.PreprocessorDirectiveParser;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Parser;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Reparser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.preferences.SubscribedBoolean;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * Utility functions for Parser classes.<br>
 * Features:
 * <ul>
 * <li> logging
 * <li> calculating hidden tokens (comments)
 * </ul>
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
		final IPrinter p = TitanDebugConsolePrinter.INSTANCE;
		final TokenNameResolver tokenNameResolver = getTokenNameResolver( lexerLogUtilClassName, p );
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
	 * @param aPrinter printer for error message
	 * @return object to resolve lexer token names from token index
	 */
	private static TokenNameResolver getTokenNameResolver( final String aLexerLogUtilClassName, IPrinter aPrinter ) {
		try {
			// ...LexerLogUtil is generated, it may not exist
			final Class<?> c = Class.forName( aLexerLogUtilClassName );
			final ILexerLogUtil o = (ILexerLogUtil)c.newInstance();
			return new TokenNameResolver( o );
		} catch ( final Exception e ) {
			aPrinter.println("ERROR: File does NOT exist: " + aLexerLogUtilClassName);
			aPrinter.println("       Run <titan.EclipsePlug-ins root>/Tools/antlr4_generate_lexerlogutil.pl to generate");
			return new TokenNameResolver();
		}
	}

	/**
	 * Get comments before a token (TTCN-3)
	 * @param aToken the token, this will NOT be printed
	 * @param aParser parser to get the token list tokenized by the lexer
	 * @param aFile parsed file 
	 * @return location, which contains all of the comments before the given token
	 */
	public static Location getCommentsBefore( final Token aToken,
											  final Parser aParser,
											  final IFile aFile ) {
		if ( aToken == null ) {
			return null;
		}

		final TokenStream tokenStream = aParser.getTokenStream();
		if ( ! ( tokenStream instanceof CommonTokenStream ) ) {
			ErrorReporter.INTERNAL_ERROR("tokenStream is not CommonTokenStream");
			return null;
		}
		final CommonTokenStream commonTokenStream = (CommonTokenStream)tokenStream; 
		final List<Token> tokens = commonTokenStream.getTokens();

		List<Token> comments = new ArrayList<Token>();
		final int start = aToken.getTokenIndex();
		// a token is hidden if Token.getChannel() > 0
		for ( int i = start - 1; i >= 0 && tokens.get( i ).getChannel() > 0; i-- ) {
			final Token t = tokens.get( i );
			final int tokenType = t.getType();
			if ( Ttcn3Lexer.LINE_COMMENT == tokenType ||
				 Ttcn3Lexer.BLOCK_COMMENT == tokenType ) {
				// add new elements to the beginning of the list, because its index is smaller
				comments.add( 0, t );
			}
		}
		if ( comments.isEmpty() ) {
			return null;
		}
		final Location loc = new Location( aFile, comments.get( 0 ), comments.get( comments.size() - 1 ) );
		return loc;
	}
}
