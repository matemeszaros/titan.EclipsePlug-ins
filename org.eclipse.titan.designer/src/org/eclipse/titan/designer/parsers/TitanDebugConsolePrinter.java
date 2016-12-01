package org.eclipse.titan.designer.parsers;

import org.eclipse.titan.common.parsers.IPrinter;
import org.eclipse.titan.common.parsers.ParserLogger;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;

/**
 * SINGLETON
 * Printer wrapper to use {@link TITANDebugConsole} print methods from {@link ParserLogger}
 * <br>
 * http://stackoverflow.com/questions/70689/what-is-an-efficient-way-to-implement-a-singleton-pattern-in-java
 * <br>
 * Quote from Effective Java: "a single-element enum type is the best way to implement a singleton."
 * @author Arpad Lovassy
 */
public enum TitanDebugConsolePrinter implements IPrinter {

	INSTANCE;

	@Override
	public void print(String aMsg) {
		TITANDebugConsole.print( aMsg );
	}

	@Override
	public void println() {
		TITANDebugConsole.println();
	}

	@Override
	public void println(String aMsg) {
		TITANDebugConsole.println( aMsg );
	}
}
