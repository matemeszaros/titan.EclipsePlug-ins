package org.eclipse.titan.common.parsers;

/**
 * Simple printer, that prints on the standard output stream
 * @author Arpad Lovassy
 */
public class ConsolePrinter implements IPrinter {

	@Override
	public void print(String aMsg) {
		System.out.print( aMsg );
	}

	@Override
	public void println() {
		System.out.println();
	}

	@Override
	public void println(String aMsg) {
		System.out.println( aMsg );
	}
}
