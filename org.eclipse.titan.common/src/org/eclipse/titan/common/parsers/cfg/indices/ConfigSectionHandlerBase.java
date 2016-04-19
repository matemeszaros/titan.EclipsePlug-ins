package org.eclipse.titan.common.parsers.cfg.indices;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Base class of config section handlers, they are responsible for storing section data,
 * which are edited through the corresponding config editor tab,
 * and are written back to the cfg file.
 * @author Arpad Lovassy
 */
public abstract class ConfigSectionHandlerBase {

	/** The root rule of a section */
	private ParserRuleContext mLastSectionRoot = null;

	public ParserRuleContext getLastSectionRoot() {
		return mLastSectionRoot;
	}

	public void setLastSectionRoot( final ParserRuleContext lastSectionRoot ) {
		this.mLastSectionRoot = lastSectionRoot;
	}
}
