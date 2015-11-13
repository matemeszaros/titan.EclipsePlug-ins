/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.TITANMarker;

public class CfgResolverListener extends CfgParserBaseListener {

	private List<TITANMarker> mWarnings = new ArrayList<TITANMarker>();

	private Map<String, CfgDefinitionInformation> mDefinitions = new HashMap<String, CfgDefinitionInformation>();
	private Map<String, String> mEnvVariables = new HashMap<String, String>();
	
	private Integer mTcpPort = null;
	private String mLocalAddress = null;
	private Double mKillTimer = null;
	private Integer mNumHcs = null;
	private Boolean mUnixDomainSocket = null;
	private Map<String, String> mComponents = new HashMap<String, String>();
	private Map<String, String[]> mGroups = new HashMap<String, String[]>();
	private List<String> mExecuteElements = new ArrayList<String>();
	
	private int mLine = 1;
	private int mOffset = 0;
	
	public void setDefinitions( Map<String,CfgDefinitionInformation> aDefs ) {
		mDefinitions = aDefs;
	}

	public void setEnvironmentalVariables( Map<String, String> aEnvVariables ) {
		mEnvVariables = aEnvVariables;
	}

	public Boolean getUnixDomainSocket() {
		return mUnixDomainSocket;
	}

	public Double getKillTimer() {
		return mKillTimer;
	}

	public String getLocalAddress() {
		return mLocalAddress;
	}

	public Integer getNumHcs() {
		return mNumHcs;
	}

	public Integer getTcpPort() {
		return mTcpPort;
	}

	public Map<String, String[]> getGroups() {
		return mGroups;
	}

	public Map<String, String> getComponents() {
		return mComponents;
	}

	public List<String> getExecuteElements() {
		return mExecuteElements;
	}
	
	/**
	 * Creates a marker.
	 * Locations of input tokens are not moved by offset and line yet, this function does this conversion.
	 * @param aMessage marker message
	 * @param aStartToken the 1st token, its line and start position will be used for the location
	 *                  NOTE: start position is the column index of the tokens 1st character.
	 *                        Column index starts with 0.
	 * @param aEndToken the last token, its end position will be used for the location.
	 *                  NOTE: end position is the column index after the token's last character.
	 * @param aSeverity severity (info/warning/error)
	 * @param aPriority priority (low/normal/high)
	 * @return new marker
	 */
	public TITANMarker createMarker( final String aMessage, final Token aStartToken, final Token aEndToken, final int aSeverity, final int aPriority ) {
		TITANMarker marker = new TITANMarker(
			aMessage,
			(aStartToken != null) ? mLine - 1 + aStartToken.getLine() : -1,
			(aStartToken != null) ? mOffset + aStartToken.getStartIndex() : -1,
			(aEndToken != null) ? mOffset + aEndToken.getStopIndex() + 1 : -1,
			aSeverity, aPriority );
		return marker;
	}

	/**
	 * Adds an error marker.
	 * Locations of input tokens are not moved by offset and line yet, this function does this conversion.
	 * @param aMessage marker message
	 * @param aStartToken the 1st token, its line and start position will be used for the location
	 *                  NOTE: start position is the column index of the tokens 1st character.
	 *                        Column index starts with 0.
	 * @param aEndToken the last token, its end position will be used for the location.
	 *                  NOTE: end position is the column index after the token's last character.
	 */
	public void reportError( final String aMessage, final Token aStartToken, final Token aEndToken ) {
		TITANMarker marker = createMarker( aMessage, aStartToken, aEndToken, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL );
		mWarnings.add(marker);
	}

	/**
	 * Gets the value of a macro or an environment variable
	 * @param aDefinition macro or environment variable
	 * @return macro or environment variable value, or null if there is no such definition
	 */
	private String getDefinitionValue(String aDefinition){
		if ( mDefinitions != null && mDefinitions.containsKey( aDefinition ) ) {
			return mDefinitions.get( aDefinition ).getValue();
		} else if ( mEnvVariables != null && mEnvVariables.containsKey( aDefinition ) ) {
			return mEnvVariables.get( aDefinition );
		} else {
			return null;
		}
	}

//TODO: remove BEGIN
//      already filled in parser
	@Override
	public void exitPr_ExecuteSectionItem(@NotNull CfgParser.Pr_ExecuteSectionItemContext ctx) {
		mExecuteElements.add( ctx.t.getText() );
	}
	
	@Override
	public void exitPr_MainControllerItemUnixDomainSocket(@NotNull CfgParser.Pr_MainControllerItemUnixDomainSocketContext ctx) {
		mUnixDomainSocket = Boolean.parseBoolean( ctx.u.getText() );
	}
	
	@Override
	public void exitPr_MainControllerItemKillTimer(@NotNull CfgParser.Pr_MainControllerItemKillTimerContext ctx) {
		mKillTimer = Double.parseDouble( ctx.k.getText() );
	}
	
	@Override
	public void exitPr_MainControllerItemLocalAddress(@NotNull CfgParser.Pr_MainControllerItemLocalAddressContext ctx) {
		mLocalAddress = ctx.l.getText();
	}
	
	@Override
	public void exitPr_MainControllerItemNumHcs(@NotNull CfgParser.Pr_MainControllerItemNumHcsContext ctx) {
		mNumHcs = Integer.parseInt( ctx.n.getText() );
	}
	
	@Override
	public void exitPr_MainControllerItemTcpPort(@NotNull CfgParser.Pr_MainControllerItemTcpPortContext ctx) {
		mTcpPort = Integer.parseInt( ctx.t.getText() );
	}
	
	@Override
	public void exitPr_ComponentItem(@NotNull CfgParser.Pr_ComponentItemContext ctx) {
		if ( ctx.h != null ) {
			mComponents.put( ctx.n.getText(), ctx.h.getText() );
		}
		else if ( ctx.i != null ) {
			mComponents.put( ctx.n.getText(), ctx.i.getText() );
		}
	}
//TODO: remove END

	@Override
	public void exitPr_CString(@NotNull CfgParser.Pr_CStringContext ctx) {
		/*
		if( ctx.macro1 != null ) {
			int commaPosition = ctx.macro1.getText().indexOf(',');
			String definition = ctx.macro1.getText().substring(2,commaPosition);
			LocationAST tempAST = new LocationAST();
			String value = getDefinitionValue(definition);
			if ( value != null ) {
				tempAST.initialize(STRING,"\""+value+"\"");
			} else {
				tempAST.initialize(STRING,"\"\"");
				reportError( "Could not resolve definition: " + definition + " using \"\" as a replacement.", ctx.macro1.start, ctx.macro1.stop );
			}
		}
		else if( ctx.macro2 != null ) {
			String definition = ctx.macro2.getText().substring(1, ctx.macro2.getText().length());
			LocationAST tempAST = new LocationAST();
			String value = getDefinitionValue(definition);
			if ( value != null ) {
				tempAST.initialize(STRING,"\""+value+"\"");
			} else {
				tempAST.initialize(STRING,"\"\"");
				reportError( "Could not resolve definition: " + definition + " using \"\" as a replacement.", ctx.macro2.start, ctx.macro2.stop );
			}
		}
		*/
		//TODO: implement
	}
/*
	// nem jok a helyettesitesek !!! (kornyezeti valtozok kimaradtak)
	pr_CString:
	(  STRING
	|  !macro1:MACRO_EXPLICITE_CSTR
	   {  
	     int commaPosition = macro1.getText().indexOf(','); 
	     String definition = macro1.getText().substring(2,commaPosition);
	     LocationAST tempAST = new LocationAST();
	     String value = getDefinitionValue(definition);
	     if(value != null){
	       tempAST.initialize(STRING,"\""+value+"\"");
	       ## = #(tempAST);
	     }else{
	       tempAST.initialize(STRING,"\"\"");
	       ## = #(tempAST);
	       RecognitionException ex =
	         new RecognitionException("Could not resolve definition: " + definition + " using \"\" as a replacement.");
	       ex.column = macro1.getOffset();
	       ex.line = macro1.getLine();
	       throw ex;
	     }
	   }
	|  !macro2:MACRO_CSTR
	   {  
	     String definition = macro2.getText().substring(1,macro2.getText().length());
	     LocationAST tempAST = new LocationAST();
	     String value = getDefinitionValue(definition);
	     if(value != null){
	       tempAST.initialize(STRING,"\""+value+"\"");
	       ## = #(tempAST);
	     }else{
	       tempAST.initialize(STRING,"\"\"");
	       ## = #(tempAST);
	       RecognitionException ex =
	         new RecognitionException("Could not resolve definition: " + definition + " using \"\" as a replacement.");
	       ex.column = macro2.getOffset();
	       ex.line = macro2.getLine();
	       throw ex;
	     }
	   }
	);
*/
	
	@Override public void exitPr_Boolean(@NotNull CfgParser.Pr_BooleanContext ctx) {
		//TODO: implement
	}

/*
	pr_Boolean:
	(  TRUE
	|  FALSE
	|  !macro:MACRO_BOOL
	   {  
	     int commaPosition =macro.getText().indexOf(','); 
	     String definition =macro.getText().substring(2,commaPosition);
	     LocationAST tempAST = new LocationAST();
	     String value = getDefinitionValue(definition);
	     if (value == null) {
	     	RecognitionException ex =
	         new RecognitionException("Could not resolve definition: " + definition + " using \"true\" as a replacement.");
	       ex.column = macro.getOffset();
	       ex.line = macro.getLine();
	       throw ex;
	     }
	     if("false".equals(value)){
	       tempAST.initialize(FALSE,"false");
	     }else{
	       tempAST.initialize(TRUE,"true");
	     }
	     ## = #(tempAST);
	   }
	);
*/

	@Override public void exitPr_Identifier(@NotNull CfgParser.Pr_IdentifierContext ctx) {
		//TODO: implement
	}

/*
	pr_Identifier:
	(  TTCN3IDENTIFIER
	|  !macro:MACRO_ID
	   {  
	     int commaPosition = macro.getText().indexOf(','); 
	     String definition = macro.getText().substring(2,commaPosition);
	     LocationAST tempAST = new LocationAST();
	     String value = getDefinitionValue(definition);
	     if(value != null){
	       tempAST.initialize(TTCN3IDENTIFIER,value);
	       ## = #(tempAST);
	     }else{
	       tempAST.initialize(TTCN3IDENTIFIER,"");
	       ## = #(tempAST);
	       RecognitionException ex =
	         new RecognitionException("Could not resolve definition: " + definition + " using \"\" as a replacement.");
	       ex.column = macro.getOffset();
	       ex.line = macro.getLine();
	       reportError(ex);
	     }
	   }
	)
	;
*/

	@Override public void exitPr_BString(@NotNull CfgParser.Pr_BStringContext ctx) {
		//TODO: implement
	}

/*
	pr_BString:
	(  BITSTRING
	|  !macro:MACRO_BSTR
	   {  
	     int commaPosition = macro.getText().indexOf(','); 
	     String definition = macro.getText().substring(2,commaPosition);
	     LocationAST tempAST = new LocationAST();
	     String value = getDefinitionValue(definition);
	     if(value != null){
	       tempAST.initialize(BITSTRING,"'" + value + "'B");
	       ## = #(tempAST);
	     }else{
	       tempAST.initialize(BITSTRING,"''B");
	       ## = #(tempAST);
	       RecognitionException ex =
	         new RecognitionException("Could not resolve definition: " + definition + " using ''B as a replacement.");
	       ex.column = macro.getOffset();
	       ex.line = macro.getLine();
	       throw ex;
	     }
	   }
	);
*/
	
	@Override public void exitPr_HString(@NotNull CfgParser.Pr_HStringContext ctx) {
		//TODO: implement
	}
	
/*
	pr_HString:
	(  HEXSTRING
	|  !macro:MACRO_HSTR
	   {  
	     int commaPosition = macro.getText().indexOf(','); 
	     String definition = macro.getText().substring(2,commaPosition);
	     LocationAST tempAST = new LocationAST();
	     String value = getDefinitionValue(definition);
	     if(value != null){
	       tempAST.initialize(HEXSTRING,"'" + value + "'H");
	       ## = #(tempAST);
	     }else{
	       tempAST.initialize(HEXSTRING,"''H");
	       ## = #(tempAST);
	       RecognitionException ex =
	         new RecognitionException("Could not resolve definition: " + definition + " using ''H as a replacement.");
	       ex.column = macro.getOffset();
	       ex.line = macro.getLine();
	       throw ex;
	     }
	   }
	);
*/

	@Override public void exitPr_OString(@NotNull CfgParser.Pr_OStringContext ctx) {
		//TODO: implement
	}
	
/*
	pr_OString:
	(  OCTETSTRING
	|  macro1:MACRO_OSTR!
	   {  
	     int commaPosition = macro1.getText().indexOf(','); 
	     String definition = macro1.getText().substring(2,commaPosition);
	     LocationAST tempAST = new LocationAST();
	     String value = getDefinitionValue(definition);
	     if(value != null){
	       tempAST.initialize(OCTETSTRING,"'" + value + "'O");
	       ## = #(tempAST);
	     }else{
	       tempAST.initialize(OCTETSTRING,"''O");
	       ## = #(tempAST);
	       RecognitionException ex =
	         new RecognitionException("Could not resolve definition: " + definition + " using ''O as a replacement.");
	       ex.column = macro1.getOffset();
	       ex.line = macro1.getLine();
	       throw ex;
	     }
	   }
	|  macro2:MACRO_BINARY!
	   {  
	     int commaPosition = macro2.getText().indexOf(','); 
	     String definition = macro2.getText().substring(2,commaPosition);
	     LocationAST tempAST = new LocationAST();
	     String value = getDefinitionValue(definition);
	     if(value != null){
	       tempAST.initialize(OCTETSTRING,value);
	       ## = #(tempAST);
	     }else{
	       tempAST.initialize(OCTETSTRING,"");
	       ## = #(tempAST);
	       RecognitionException ex =
	         new RecognitionException("Could not resolve definition: " + definition + " using \"\" as a replacement.");
	       ex.column = macro2.getOffset();
	       ex.line = macro2.getLine();
	       throw ex;
	     }
	   }
	);
*/
	
	@Override public void exitPr_HostName(@NotNull CfgParser.Pr_HostNameContext ctx) {
		//TODO: implement
	}
/*
	pr_HostName:
	(  pr_DNSName
	|  TTCN3IDENTIFIER
	|  !macro:MACRO_HOSTNAME
	   {  
	     int commaPosition = macro.getText().indexOf(','); 
	     String definition = macro.getText().substring(2,commaPosition);
	     LocationAST tempAST = new LocationAST();
	     String value = getDefinitionValue(definition);
	     if(value != null){
	       tempAST.initialize(TTCN3IDENTIFIER,value);
	       ## = #(tempAST);
	     }else{
	       tempAST.initialize(TTCN3IDENTIFIER,"");
	       ## = #(tempAST);
	       RecognitionException ex =
	         new RecognitionException("Could not resolve definition: " + definition + " using \"\" as a replacement.");
	       ex.column = macro.getOffset();
	       ex.line = macro.getLine();
	       throw ex;
	     }
	   }
	);
*/

	@Override public void exitPr_Number(@NotNull CfgParser.Pr_NumberContext ctx) {
		//TODO: implement
	}
	
/*
	pr_Number:
	(  NUMBER
	|  !macro:MACRO_INT
	   {  
	     int commaPosition = macro.getText().indexOf(','); 
	     String definition = macro.getText().substring(2,commaPosition);
	     LocationAST tempAST = new LocationAST();
	     String value = getDefinitionValue(definition);
	     if(value != null){
	       tempAST.initialize(NUMBER,value);
	       ## = #(tempAST);
	     }else{
	       tempAST.initialize(NUMBER,"0");
	       ## = #(tempAST);
	       RecognitionException ex =
	         new RecognitionException("Could not resolve integer definition: " + definition + " using 0 as replacement.");
	       ex.column = macro.getOffset();
	       ex.line = macro.getLine();
	       throw ex;
	     }
	   }
	);
*/

	@Override public void exitPr_Float(@NotNull CfgParser.Pr_FloatContext ctx) {
		//TODO: implement
	}

/*
	pr_Float:
	(  FLOAT
	|  !macro:MACRO_FLOAT
	   {
	     int commaPosition = macro.getText().indexOf(','); 
	     String definition = macro.getText().substring(2,commaPosition);
	     LocationAST tempAST = new LocationAST();
	     String value = getDefinitionValue(definition);
	     if(value != null){
	       tempAST.initialize(FLOAT,value);
	       ## = #(tempAST);
	     }else{
	       tempAST.initialize(FLOAT,"0.0");
	       ## = #(tempAST);
	       RecognitionException ex =
	         new RecognitionException("No macro or environmental variable defined " +
						definition + " could be found, using 0.0 as a replacement value.");
	       ex.column = macro.getOffset();
	       ex.line = macro.getLine();
	       throw ex;
	     }
	   }
	);
*/
	
}
