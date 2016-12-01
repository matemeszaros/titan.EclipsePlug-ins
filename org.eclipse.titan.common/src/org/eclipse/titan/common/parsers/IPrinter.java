package org.eclipse.titan.common.parsers;

/**
 * Used by {@link ParserLogger} for logging to different consoles
 * @author Arpad Lovassy
 */
public interface IPrinter {

	void print( final String aMsg );

	void println();

	void println( final String aMsg );
}
