package org.eclipse.titan.designer.editors.configeditor;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;

/**
 * Base class for ...ItemTransfer classes,
 * they are responsible for handling drag and drop of config editor tab items.
 * @author Arpad Lovassy
 */
public abstract class ConfigItemTransferBase extends ByteArrayTransfer {

	/**
	 * Converts parse tree to its string representation. Used by javaToNative().
	 * @param aRoot parse tree root to convert
	 * @return the converted string representation of the parse tree
	 */
	protected String convertToString( final ParseTree aRoot ) {
		//TODO: get tokenStream, and use this instead
		/*
		final StringBuilder sb = new StringBuilder();
		// it prints also the hidden token before
		ConfigTreeNodeUtilities.print( aRoot, getTokenStream(), sb, null );
		return sb.toString();
		/*/
		return aRoot.getText();
		//*/
	}
}
