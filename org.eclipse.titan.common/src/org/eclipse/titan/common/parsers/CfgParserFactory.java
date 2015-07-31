package org.eclipse.titan.common.parsers;

import org.eclipse.titan.common.parsers.cfg.ConfigFileHandler;
import org.eclipse.titan.common.parsers.cfg.ConfigFileHandler_V4;

/**
 * Factory to create ANTLR 4 specific CFG parsers and data types, values
 * @author Arpad Lovassy
 */
public class CfgParserFactory {
	
	public static ConfigFileHandler createConfigFileHandler() {
		return new ConfigFileHandler_V4();
	}
	
	public static void CfgEditorAddExtraPages() {
		
	}
}
