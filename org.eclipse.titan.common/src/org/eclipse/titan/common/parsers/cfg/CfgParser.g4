parser grammar CfgParser;

@header {
import java.util.HashMap;

import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.common.parsers.cfg.indices.ComponentSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.DefineSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ExecuteSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ExternalCommandSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.GroupSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.IncludeSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.MCSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ModuleParameterSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.TestportParameterSectionHandler;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IFile;

import java.util.Map;
}

@members{
	private List<TITANMarker> mWarnings = new ArrayList<TITANMarker>();
	
	private List<ISection> mSections = new ArrayList<ISection>();

	private Map<String, CfgDefinitionInformation> mDefinitions = new HashMap<String, CfgDefinitionInformation>();
  
	private List<String> mIncludeFiles = new ArrayList<String>();

	private IFile mActualFile = null;

	private boolean mLogFileDefined = false;
	
	private String mLogFileName = null;
	
	private Map<String, String> mEnvVariables;
	
	private Integer mTcpPort = null;
	private String mLocalAddress = null;
	private Double mKillTimer = null;
	private Integer mNumHcs = null;
	private Boolean mUnixDomainSocket = null;
	private Map<String, String> mComponents = new HashMap<String, String>();
	private Map<String , String[]> mGroups = new HashMap<String, String[]>();
	private List<String> mExecuteElements = new ArrayList<String>();
	private int mLine = 1;
	private int mOffset = 0;
	
	private ModuleParameterSectionHandler moduleParametersHandler = new ModuleParameterSectionHandler();
	private TestportParameterSectionHandler testportParametersHandler = new TestportParameterSectionHandler();
	private ComponentSectionHandler componentSectionHandler = new ComponentSectionHandler();
	private GroupSectionHandler groupSectionHandler = new GroupSectionHandler();
	private MCSectionHandler mcSectionHandler = new MCSectionHandler();
	private ExternalCommandSectionHandler externalCommandsSectionHandler = new ExternalCommandSectionHandler();
	private ExecuteSectionHandler executeSectionHandler = new ExecuteSectionHandler();
	private IncludeSectionHandler includeSectionHandler = new IncludeSectionHandler();
	private IncludeSectionHandler orderedIncludeSectionHandler = new IncludeSectionHandler();
	private DefineSectionHandler defineSectionHandler = new DefineSectionHandler();
	private LoggingSectionHandler loggingSectionHandler = new LoggingSectionHandler();
	
	public void reportWarning(TITANMarker marker){
		mWarnings.add(marker);
	}

	public List<TITANMarker> getWarnings(){
		return mWarnings;
	}
	
	public List<ISection> getSections() {
		return mSections;
	}

	public void setDefinitions( Map<String,CfgDefinitionInformation> aDefs ) {
		mDefinitions = aDefs;
	}
	
	public Map< String, CfgDefinitionInformation > getDefinitions() {
		return mDefinitions;
	}

	public List<String> getIncludeFiles(){
		return mIncludeFiles;
	}

	public void setActualFile(IFile file) {
		mActualFile = file;
	}

	public boolean isLogFileDefined() {
		return mLogFileDefined;
	}
	
	public String getLogFileName() {
		return mLogFileName;
	}
	
	public Integer getTcpPort() {
		return mTcpPort;
	}
  
	public String getLocalAddress() {
		return mLocalAddress;
	}
  
	public Double getKillTimer() {
		return mKillTimer;
	}
  
	public Integer getNumHcs() {
		return mNumHcs;
	}
	
	public Boolean isUnixDomainSocketEnabled() {
		return mUnixDomainSocket;
	}
	
	public Map<String, String> getComponents() {
		return mComponents;
	}
  
	public Map<String, String[]> getGroups() {
		return mGroups;
	}
	
	public List<String> getExecuteElements() {
		return mExecuteElements;
	}
	
	public void setEnvironmentalVariables(Map<String, String> aEnvVariables){
		mEnvVariables = aEnvVariables;
	}
	
	public ModuleParameterSectionHandler getModuleParametersHandler() {
		return moduleParametersHandler;
	}

	public TestportParameterSectionHandler getTestportParametersHandler() {
		return testportParametersHandler;
	}

	public ComponentSectionHandler getComponentSectionHandler() {
		return componentSectionHandler;
	}

	public GroupSectionHandler getGroupSectionHandler() {
		return groupSectionHandler;
	}

	public MCSectionHandler getMcSectionHandler() {
		return mcSectionHandler;
	}

	public ExternalCommandSectionHandler getExternalCommandsSectionHandler() {
		return externalCommandsSectionHandler;
	}

	public ExecuteSectionHandler getExecuteSectionHandler() {
		return executeSectionHandler;
	}

	public IncludeSectionHandler getIncludeSectionHandler() {
		return includeSectionHandler;
	}

	public DefineSectionHandler getDefineSectionHandler() {
		return defineSectionHandler;
	}

	public LoggingSectionHandler getLoggingSectionHandler() {
		return loggingSectionHandler;
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
}

options{
	tokenVocab=CfgLexer;
}

pr_ConfigFile:
	(	s = pr_Section
		{	if ( $s.section != null ) {
				mSections.add( $s.section );
			}
		}
	)+
	EOF
;
 
pr_Section returns [ ISection section ]:
{	$section = null;
}
(	pr_DefaultSection	
|	pr_MainControllerSection
|	i = pr_IncludeSection { $section = $i.includeSection; }
|	pr_OrderedIncludeSection
|	pr_ExecuteSection
|	pr_DefineSection
|	pr_ExternalCommandsSection
|	pr_TestportParametersSection
|	pr_GroupsSection
|	pr_ModuleParametersSection
|	pr_ComponentsSection
|	pr_LoggingSection
|	pr_ProfilerSection
)
;

pr_DefaultSection:
(	WS
|	LINE_COMMENT
|	BLOCK_COMMENT	
)
;

pr_MainControllerSection:
MAIN_CONTROLLER_SECTION
(	pr_MainControllerItem SEMICOLON1?
)*
;

pr_MainControllerItem:
(	pr_MainControllerItemUnixDomainSocket
|	pr_MainControllerItemKillTimer
|	pr_MainControllerItemLocalAddress
|	pr_MainControllerItemNumHcs
|	pr_MainControllerItemTcpPort
)+
;

pr_MainControllerItemUnixDomainSocket:
	UNIXSOCKETS1	ASSIGNMENTCHAR1	u = (YES1 | NO1)					SEMICOLON1?
	{	mUnixDomainSocket = Boolean.parseBoolean( $u.getText() );
	}
;

pr_MainControllerItemKillTimer:
	KILLTIMER1		ASSIGNMENTCHAR1	k = pr_ArithmeticValueExpression	SEMICOLON1?
	{	try {
			mKillTimer = Double.parseDouble( $k.text );
		} catch( NumberFormatException e ) {}
	}
;

pr_MainControllerItemLocalAddress:
	LOCALADDRESS1	ASSIGNMENTCHAR1	l = pr_HostName						SEMICOLON1?
	{	mLocalAddress = $l.text;	}
;

pr_MainControllerItemNumHcs:
	NUMHCS1			ASSIGNMENTCHAR1	n = pr_IntegerValueExpression		SEMICOLON1?
	{	try {
			mNumHcs = Integer.parseInt( $n.text );
		} catch( NumberFormatException e ) {}
	}
;

pr_MainControllerItemTcpPort:
	TCPPORT1		ASSIGNMENTCHAR1	t = pr_IntegerValueExpression		SEMICOLON1?
	{	try {
			mTcpPort = Integer.parseInt( $t.text );
		} catch( NumberFormatException e ) {}
	}
;

pr_IncludeSection returns [ IncludeSection includeSection ]:
{	$includeSection = new IncludeSection();
}
	h = INCLUDE_SECTION	{	includeSectionHandler.setLastSectionRoot( $h );	}
	( f = STRING2
		{	String fileName = $f.getText().substring( 1, $f.getText().length() - 1 );
			$includeSection.addIncludeFileName( fileName );
			mIncludeFiles.add( fileName );
			includeSectionHandler.getFiles().add( new LocationAST($f) );
		}
	)*
;

pr_OrderedIncludeSection:
	ORDERED_INCLUDE_SECTION
	STRING4*
;

pr_ExecuteSection:
	EXECUTE_SECTION
	pr_ExecuteSectionItem*
;

pr_ExecuteSectionItem:
	t = TEST3 { mExecuteElements.add( $t.getText() ); }
	SEMICOLON3?
;

pr_DefineSection: 
	h = DEFINE_SECTION	{	defineSectionHandler.setLastSectionRoot( $h );	}
	(	def = pr_MacroAssignment
			{	if ( $def.definition != null ) {
					defineSectionHandler.getDefinitions().add( $def.definition );
					$def.definition.setRoot($def.ctx);
				}
			}
	)*
;

pr_ExternalCommandsSection:
	EXTERNAL_COMMANDS_SECTION
	(	(	BEGINCONTROLPART6
		|	ENDCONTROLPART6
		|	BEGINTESTCASE6
		|	ENDTESTCASE6 
		)
		ASSIGNMENTCHAR6
		STRING6
		SEMICOLON6?
	)*
;

pr_TestportParametersSection:
	TESTPORT_PARAMETERS_SECTION
	(	pr_ComponentID DOT7 pr_TestportName DOT7 pr_Identifier ASSIGNMENTCHAR7 pr_StringValue SEMICOLON7?
	)*
;

pr_GroupsSection:
	GROUPS_SECTION
	(	pr_GroupItem SEMICOLON8?	
	)*
;

pr_ModuleParametersSection:
	MODULE_PARAMETERS_SECTION
	(	pr_ModuleParam SEMICOLON9?
	)*
;

pr_ComponentsSection:
	COMPONENTS_SECTION
	(	pr_ComponentItem SEMICOLON10?
	)*
;

pr_LoggingSection:
	LOGGING_SECTION
	(	pr_LoggingParam	SEMICOLON11?
	)*
;

pr_ProfilerSection:
	PROFILER_SECTION
	(	pr_ProfilerSetting SEMICOLON12?
	)*
;

pr_ProfilerSetting:
(	pr_DisableProfiler
|	pr_DisableCoverage
|	pr_DatabaseFile
|	pr_AggregateData
|	pr_StatisticsFile
|	pr_DisableStatistics
|	pr_StatisticsFilter
|	pr_StartAutomatically
|	pr_NetLineTimes
|	pr_NetFunctionTimes
)
;

pr_DisableProfiler:
	DISABLEPROFILER
	ASSIGNMENTCHAR12
	(	TRUE12
	|	FALSE12
	)
;

pr_DisableCoverage:
	DISABLECOVERAGE
	ASSIGNMENTCHAR12
	(	TRUE12
	|	FALSE12
	)
;

pr_DatabaseFile:
	DATABASEFILE
	ASSIGNMENTCHAR12
	(	STRING12
	|	MACRO12	
	)
	(	AND12
		(	STRING12
		|	MACRO12	
		)
	)*
;

pr_AggregateData:
	AGGREGATEDATA
	ASSIGNMENTCHAR12
	(	TRUE12
	|	FALSE12
	)
;

pr_StatisticsFile:
	STATISTICSFILE
	ASSIGNMENTCHAR12
	(	STRING12
	|	MACRO12	
	)
	(	AND12
		(	STRING12
		|	MACRO12	
		)
	)*
;

pr_DisableStatistics:
	DISABLESTATISTICS
	ASSIGNMENTCHAR12
	(	TRUE12
	|	FALSE12
	)
;

pr_StatisticsFilter:
	STATISTICSFILTER
	(	ASSIGNMENTCHAR12
	|	CONCATCHAR12
	)
	pr_StatisticsFilterEntry
	(	(	LOGICALOR12
		|	AND12
		)
		pr_StatisticsFilterEntry
	)*
;

pr_StatisticsFilterEntry:
(	NUMBEROFLINES
|	LINEDATARAW
|	FUNCDATARAW
|	LINEAVGRAW
|	FUNCAVGRAW
|	LINETIMESSORTEDBYMOD
|	FUNCTIMESSORTEDBYMOD
|	LINETIMESSORTEDTOTAL
|	FUNCTIMESSORTEDTOTAL
|	LINECOUNTSORTEDBYMOD
|	FUNCCOUNTSORTEDBYMOD
|	LINECOUNTSORTEDTOTAL
|	FUNCCOUNTSORTEDTOTAL
|	LINEAVGSORTEDBYMOD
|	FUNCAVGSORTEDBYMOD
|	LINEAVGSORTEDTOTAL
|	FUNCAVGSORTEDTOTAL
|	TOP10LINETIMES
|	TOP10FUNCTIMES
|	TOP10LINECOUNT
|	TOP10FUNCCOUNT
|	TOP10LINEAVG
|	TOP10FUNCAVG
|	UNUSEDLINES
|	UNUSEDFUNC
|	ALLRAWDATA
|	LINEDATASORTEDBYMOD
|	FUNCDATASORTEDBYMOD
|	LINEDATASORTEDTOTAL
|	FUNCDATASORTEDTOTAL
|	LINEDATASORTED
|	FUNCDATASORTED
|	ALLDATASORTED
|	TOP10LINEDATA
|	TOP10FUNCDATA
|	TOP10ALLDATA
|	UNUSEDATA
|	ALL
|	HEXFILTER12
)
;

pr_StartAutomatically:
	STARTAUTOMATICALLY
	ASSIGNMENTCHAR12
	(	TRUE12
	|	FALSE12
	)
;

pr_NetLineTimes:
	NETLINETIMES
	ASSIGNMENTCHAR12
	(	TRUE12
	|	FALSE12
	)
;

pr_NetFunctionTimes:
	NETFUNCTIONTIMES
	ASSIGNMENTCHAR12
	(	TRUE12
	|	FALSE12
	)
;

pr_LoggingParam:
	pr_ComponentSpecificLoggingParam
;

pr_ComponentSpecificLoggingParam:
	pr_LoggerPluginsPart
|	pr_PlainLoggingParam
;

pr_LoggerPluginsPart:
	(	pt_TestComponentID DOT11
	)?
	LOGGERPLUGINS ASSIGNMENTCHAR11 BEGINCHAR11 pr_LoggerPluginEntry
	(	COMMA11 pr_LoggerPluginEntry
	)*
	ENDCHAR11
;

pr_PlainLoggingParam:
(	pt_TestComponentID DOT11
)?
(	STAR11 DOT11
|	pr_Identifier DOT11
)?
(   FILEMASK ASSIGNMENTCHAR11 pr_LoggingBitMask
|	CONSOLEMASK ASSIGNMENTCHAR11 pr_LoggingBitMask
|	DISKFULLACTION ASSIGNMENTCHAR11 pr_DiskFullActionValue
|	LOGFILENUMBER ASSIGNMENTCHAR11 pr_Number
|	LOGFILESIZE ASSIGNMENTCHAR11 pr_Number
|	LOGFILENAME ASSIGNMENTCHAR11 f = pr_LogfileName
	{	mLogFileDefined = true;
		mLogFileName = $f.text;
		if ( mLogFileName != null ) {
			if ( mLogFileName.length() > 0 && mLogFileName.startsWith( "\"" ) ) {
				mLogFileName = mLogFileName.substring( 1 );
			}
			if ( mLogFileName.length() > 0 && mLogFileName.endsWith( "\"" ) ) {
				mLogFileName = mLogFileName.substring( 0, mLogFileName.length() - 1 );
			}
		}
	}
|	(TIMESTAMPFORMAT | CONSOLETIMESTAMPFORMAT) ASSIGNMENTCHAR11 TIMESTAMPVALUE
|	SOURCEINFOFORMAT ASSIGNMENTCHAR11
	(	SOURCEINFOVALUE
	|	pr_YesNoOrBoolean
	)
|	APPENDFILE ASSIGNMENTCHAR11 pr_YesNoOrBoolean
|	LOGEVENTTYPES ASSIGNMENTCHAR11 pr_LogEventTypesValue
|	LOGENTITYNAME ASSIGNMENTCHAR11 pr_YesNoOrBoolean
|	MATCHINGHINTS ASSIGNMENTCHAR11 pr_MatchingHintsValue
|	TTCN3IDENTIFIER11 ASSIGNMENTCHAR11 pr_StringValue
|   EMERGENCYLOGGING ASSIGNMENTCHAR11 pr_Number
|   EMERGENCYLOGGINGBEHAVIOUR ASSIGNMENTCHAR11 BUFFERALLORBUFFERMASKED
|   EMERGENCYLOGGINGMASK ASSIGNMENTCHAR11 pr_LoggingBitMask
)
;

pr_DiskFullActionValue:
(	DISKFULLACTIONVALUE
|	DISKFULLACTIONVALUERETRY ( LPAREN11 NUMBER11 RPAREN11 )?	
)
;

pr_LoggerPluginEntry:
	pr_Identifier 
	(	ASSIGNMENTCHAR11 pr_StringValue
	)?
;

pt_TestComponentID:
(	pr_Identifier
|	pr_Number
|	MTCKeyword
|	STAR11
)
;

pr_LoggingBitMask:
	pr_LoggingMaskElement
	(	LOGICALOR11	pr_LoggingMaskElement
	)*
;

pr_LoggingMaskElement:
	pr_LogEventType
|	pr_LogEventTypeSet
|	pr_deprecatedEventTypeSet
;

pr_LogfileName:
	pr_StringValue
;

pr_YesNoOrBoolean:
	YESNO
|	pr_Boolean
;

pr_LogEventTypesValue:
	pr_YesNoOrBoolean
|	pr_Detailed
;

pr_MatchingHintsValue:
	COMPACT
|	DETAILED
;

pr_LogEventType:
(	ACTION_UNQUALIFIED | DEBUG_ENCDEC | DEBUG_TESTPORT | DEBUG_UNQUALIFIED | DEFAULTOP_ACTIVATE | DEFAULTOP_DEACTIVATE | DEFAULTOP_EXIT
|	DEFAULTOP_UNQUALIFIED | ERROR_UNQUALIFIED | EXECUTOR_COMPONENT | EXECUTOR_CONFIGDATA | EXECUTOR_EXTCOMMAND | EXECUTOR_LOGOPTIONS
|	EXECUTOR_RUNTIME | EXECUTOR_UNQUALIFIED | FUNCTION_RND | FUNCTION_UNQUALIFIED | MATCHING_DONE | MATCHING_MCSUCCESS | MATCHING_MCUNSUCC
|	MATCHING_MMSUCCESS | MATCHING_MMUNSUCC | MATCHING_PCSUCCESS | MATCHING_PCUNSUCC | MATCHING_PMSUCCESS | MATCHING_PMUNSUCC | MATCHING_PROBLEM
|	MATCHING_TIMEOUT | MATCHING_UNQUALIFIED | PARALLEL_PORTCONN | PARALLEL_PORTMAP | PARALLEL_PTC | PARALLEL_UNQUALIFIED | PORTEVENT_DUALRECV
|	PORTEVENT_DUALSEND | PORTEVENT_MCRECV | PORTEVENT_MCSEND | PORTEVENT_MMRECV | PORTEVENT_MMSEND | PORTEVENT_MQUEUE | PORTEVENT_PCIN
|	PORTEVENT_PCOUT | PORTEVENT_PMIN | PORTEVENT_PMOUT | PORTEVENT_PQUEUE | PORTEVENT_STATE | PORTEVENT_UNQUALIFIED	| STATISTICS_UNQUALIFIED
|	STATISTICS_VERDICT | TESTCASE_FINISH | TESTCASE_START | TESTCASE_UNQUALIFIED | TIMEROP_GUARD | TIMEROP_READ | TIMEROP_START | TIMEROP_STOP
|	TIMEROP_TIMEOUT | TIMEROP_UNQUALIFIED | USER_UNQUALIFIED | VERDICTOP_FINAL | VERDICTOP_GETVERDICT | VERDICTOP_SETVERDICT | VERDICTOP_UNQUALIFIED
|   WARNING_UNQUALIFIED
)	
;

pr_LogEventTypeSet:
(	TTCN_EXECUTOR2 | TTCN_ERROR2 | TTCN_WARNING2 | TTCN_PORTEVENT2 | TTCN_TIMEROP2 | TTCN_VERDICTOP2 | TTCN_DEFAULTOP2 | TTCN_ACTION2	
|	TTCN_TESTCASE2 | TTCN_FUNCTION2 | TTCN_USER2 | TTCN_STATISTICS2 | TTCN_PARALLEL2 | TTCN_MATCHING2 | TTCN_DEBUG2 | LOG_ALL | LOG_NOTHING		
)
;

pr_deprecatedEventTypeSet:
a = 
(	TTCN_EXECUTOR1 
|	TTCN_ERROR1
|	TTCN_WARNING1
|	TTCN_PORTEVENT1
|	TTCN_TIMEROP1
|	TTCN_VERDICTOP1
|	TTCN_DEFAULTOP1
|	TTCN_ACTION1	
|	TTCN_TESTCASE1
|	TTCN_FUNCTION1
|	TTCN_USER1
|	TTCN_STATISTICS1
|	TTCN_PARALLEL1
|	TTCN_MATCHING1
|	TTCN_DEBUG1
)
{	if ( $a != null ) {
		reportWarning(new TITANMarker("Deprecated logging option " + $a.getText(), $a.getLine(),
			$a.getStartIndex(), $a.getStopIndex(), IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL));
	}
}
;

pr_Detailed:
	DETAILED
|	SUBCATEGORIES
;

pr_ComponentItem:
	n = pr_ComponentName
	ASSIGNMENTCHAR10 
	(	h = pr_HostName { mComponents.put( $n.text, $h.text ); }
	|	i = IPV6_10 { mComponents.put( $n.text, $i.getText() ); }
	)
;

pr_ComponentName:
(	pr_Identifier
|	STAR10
)
;

pr_HostName:
(	pr_DNSName
|	TTCN3IDENTIFIER1 | TTCN3IDENTIFIER10
|	MACRO_HOSTNAME1 | MACRO_HOSTNAME10
)
;

pr_MacroAssignment returns [ DefineSectionHandler.Definition definition ]
@init {
	$definition = null;
	String name = null;
	String value = null;
}:
(	col = TTCN3IDENTIFIER5 { name = $col.getText(); }
	ASSIGNMENTCHAR5
	endCol = pr_DefinitionRValue { value = $endCol.text; }
)
{	if(name != null && value != null) {
		ArrayList<CfgLocation> locations = new ArrayList<CfgLocation>();
		locations.add(new CfgLocation(mActualFile, $col, $col));
		mDefinitions.put(name, new CfgDefinitionInformation(value, locations));
	}

	$definition = new DefineSectionHandler.Definition();
	$definition.setDefinitionName($col);
	$definition.setDefinitionValue($endCol.ctx);
}
;

pr_DefinitionRValue:
(	pr_SimpleValue+
|	pr_StructuredValue
)
;

pr_SimpleValue:
(	TTCN3IDENTIFIER5
|	MACRORVALUE5
|	MACRO_ID5
|	MACRO_INT5
|	MACRO_BOOL5
|	MACRO_FLOAT5
|	MACRO_EXP_CSTR5
|	MACRO_BSTR5
|	MACRO_HSTR5
|	MACRO_OSTR5
|	MACRO_BINARY5
|	MACRO5
|	IPV6_5
|	STRING5
)
;

pr_StructuredValue:
	BEGINCHAR5
	(pr_StructuredValue | pr_StructuredValue2)
	ENDCHAR5
;

pr_StructuredValue2:
(  	pr_MacroAssignment 
|	pr_SimpleValue
)?
;

pr_ComponentID:
(	pr_Identifier
|	pr_Number
|	MTC7
|	SYSTEM7
|	STAR7
)
;

pr_TestportName:
(	pr_Identifier
	(	SQUAREOPEN7 pr_IntegerValueExpression SQUARECLOSE7
	)*
|	STAR7
)
;

pr_Identifier:
(	MACRO_ID7 | MACRO_ID8 | MACRO_ID9 | MACRO_ID10 | MACRO_ID11
|	TTCN3IDENTIFIER7 | TTCN3IDENTIFIER8 | TTCN3IDENTIFIER9 | TTCN3IDENTIFIER10 | TTCN3IDENTIFIER11
)
;

pr_IntegerValueExpression:
	pr_IntegerAddExpression
;

pr_IntegerAddExpression:
	pr_IntegerMulExpression
	(	(	PLUS1 | PLUS7 | PLUS9
		|	MINUS1 | MINUS7 | MINUS9
		)
		pr_IntegerMulExpression
	)*
;

pr_IntegerMulExpression:
	pr_IntegerUnaryExpression
	(	(	STAR1 | STAR7 | STAR9
		|	SLASH1 | SLASH7 | SLASH9 
		)	
		pr_IntegerUnaryExpression
	)*
;

pr_IntegerUnaryExpression:
	(	PLUS1 | PLUS7 | PLUS9
	|	MINUS1 | MINUS7 | MINUS9
	)?
	pr_IntegerPrimaryExpression
;

pr_IntegerPrimaryExpression:
(	pr_Number
|	LPAREN1 pr_IntegerAddExpression RPAREN1
|	LPAREN7 pr_IntegerAddExpression RPAREN7
|	LPAREN9 pr_IntegerAddExpression RPAREN9
)
;

pr_Number:
(	NUMBER1 | NUMBER7 | NUMBER9 | NUMBER11
|	MACRO_INT1 | MACRO_INT7 | MACRO_INT9 | MACRO_INT11	
)
;

pr_StringValue:
	pr_CString
	(	(STRINGOP1 | STRINGOP7 | STRINGOP9 | STRINGOP11) pr_CString
	)*
;

pr_CString:
(	STRING1 | STRING7 | STRING9 | STRING11
|	macro2 = pr_MacroCString
		{
//TODO
/*
			String definition = $macro2.text.substring(1, $macro2.text.length());
			LocationAST tempAST = new LocationAST();
			String value = getDefinitionValue(definition);
			if ( value != null ) {
				tempAST.initialize(STRING,"\""+value+"\"");
			} else {
				tempAST.initialize(STRING,"\"\"");
				reportError( "Could not resolve definition: " + definition + " using \"\" as a replacement.", $macro2.start, $macro2.stop );
			}
//*/
		}
|	macro1 = pr_MacroExpliciteCString
		{
//TODO
/*
			int commaPosition = $macro1.text.indexOf(',');
			String definition = $macro1.text.substring(2,commaPosition);
			LocationAST tempAST = new LocationAST();
			String value = getDefinitionValue(definition);
			if ( value != null ) {
				tempAST.initialize(STRING,"\""+value+"\"");
			} else {
				tempAST.initialize(STRING,"\"\"");
				reportError( "Could not resolve definition: " + definition + " using \"\" as a replacement.", $macro1.start, $macro1.stop );
			}
//*/
		}
)
;

pr_MacroCString:
	(MACRO1 | MACRO7 | MACRO9 | MACRO11)
;

pr_MacroExpliciteCString:
	(MACRO_EXP_CSTR1 | MACRO_EXP_CSTR7 | MACRO_EXP_CSTR9 | MACRO_EXP_CSTR11)
;

pr_GroupItem:
{  ArrayList<String> memberlist = new ArrayList<String>();  }
(	a = pr_Identifier
	ASSIGNMENTCHAR8
	(	STAR8 {  memberlist.add("*");  }
	|	(	c = pr_DNSName { memberlist.add( $c.text ); }
		|	d = pr_Identifier { memberlist.add( $d.text ); }
		)
		(	COMMA8
			(	e = pr_DNSName { memberlist.add( $e.text ); }
			|	f = pr_Identifier { memberlist.add( $f.text ); }
			)
		)*
	)
)
{	mGroups.put( $a.text, memberlist.toArray( new String[ memberlist.size() ] ) );
}
;

pr_DNSName:
(	NUMBER1 | NUMBER8 | NUMBER10
|	FLOAT1 | FLOAT8 | FLOAT10
|	DNSNAME1 | DNSNAME8 | DNSNAME10
)
;

pr_ModuleParam:
	pr_ParameterName
	(	ASSIGNMENTCHAR9 pr_ParameterValue
	|	CONCATCHAR9 pr_ParameterValue
	)
;

pr_ParameterName:
(	(STAR9 DOT9)?
	pr_Identifier (SQUAREOPEN9 NUMBER9 SQUARECLOSE9)?
	(	DOT9 pr_Identifier (SQUAREOPEN9 NUMBER9 SQUARECLOSE9)?
	)*
)
;

pr_ParameterValue:
	pr_SimpleParameterValue pr_LengthMatch? IFPRESENTKeyword9?
;

pr_LengthMatch:
	LENGTHKeyword9 LPAREN9 pr_LengthBound
	(	RPAREN9
	|	DOTDOT9
		(	pr_LengthBound | INFINITYKeyword9	)
		RPAREN9
	)
;

pr_SimpleParameterValue:
(	pr_ArithmeticValueExpression
|	pr_Boolean
|	pr_ObjIdValue
|	pr_VerdictValue
|	pr_BStringValue
|	pr_HStringValue
|	pr_OStringValue
|	pr_UniversalOrNotStringValue
|	OMITKeyword9
|	pr_EnumeratedValue
|	pr_NULLKeyword 
|	MTCKeyword9
|	SYSTEMKeyword9
|	pr_CompoundValue
|	ANYVALUE9
|	STAR9
|	pr_IntegerRange
|	pr_FloatRange
|	pr_StringRange
|	PATTERNKeyword9 pr_PatternChunkList
|	pr_BStringMatch
|	pr_HStringMatch
|	pr_OStringMatch
)
;

pr_LengthBound:
	pr_IntegerValueExpression
;

pr_ArithmeticValueExpression:
	pr_ArithmeticAddExpression
;

pr_ArithmeticAddExpression:
	pr_ArithmeticMulExpression
	(	(	PLUS1 | PLUS9
		|	MINUS1 | MINUS9
		)
		pr_ArithmeticMulExpression
	)*
;

pr_ArithmeticMulExpression:
	pr_ArithmeticUnaryExpression
	(	(	STAR1 | STAR9
		|	SLASH1 | SLASH9
		)
		pr_ArithmeticUnaryExpression
	)*
;

pr_ArithmeticUnaryExpression:
	(	PLUS1 | PLUS9
	|	MINUS1 | MINUS9
	)*
	pr_ArithmeticPrimaryExpression
;

pr_ArithmeticPrimaryExpression:
(	pr_Float
|	pr_Number
|	LPAREN1 pr_ArithmeticAddExpression RPAREN1
|	LPAREN9 pr_ArithmeticAddExpression RPAREN9
)
;

pr_Float:
(	FLOAT1 | FLOAT9
|	MACRO_FLOAT1 | MACRO_FLOAT9
)
;

pr_Boolean:
(	TRUE9 | TRUE11
|	FALSE9 | FALSE11
|	MACRO_BOOL9 | MACRO_BOOL11
)
;

pr_ObjIdValue:
	OBJIDKeyword9	BEGINCHAR9	pr_ObjIdComponent+	ENDCHAR9
;

pr_ObjIdComponent:
(	pr_Number
|	pr_Identifier LPAREN9 pr_Number RPAREN9	
)
;

pr_VerdictValue:
(	NONE_VERDICT9
|	PASS_VERDICT9
|	INCONC_VERDICT9
|	FAIL_VERDICT9
|	ERROR_VERDICT9
)
;

pr_BStringValue:
	pr_BString	(	STRINGOP9 pr_BString	)*
;

pr_BString:
(	BITSTRING9
|	MACRO_BSTR9
)
;

pr_HStringValue:
	pr_HString	(	STRINGOP9 pr_HString	)*
;

pr_HString:
(	HEXSTRING9
|	MACRO_HSTR9
)
;

pr_OStringValue:
	pr_OString	(	STRINGOP9 pr_OString	)*
;

pr_OString:
(	OCTETSTRING9
|	MACRO_OSTR9
|	MACRO_BINARY9
)
;

pr_UniversalOrNotStringValue:
(	pr_CString
|	pr_Quadruple
)
(	STRINGOP9
	(	pr_CString
	|	pr_Quadruple
	)
)*
;

pr_Quadruple:
	CHARKeyword9 
	LPAREN9
	pr_IntegerValueExpression COMMA9 pr_IntegerValueExpression COMMA9 pr_IntegerValueExpression COMMA9 pr_IntegerValueExpression
	RPAREN9
;

pr_EnumeratedValue:
	pr_Identifier
;

pr_NULLKeyword:
	NULLKeyword9
;

pr_CompoundValue:
(	BEGINCHAR9
    (	/* empty */
	|	pr_FieldValue	(	COMMA9 pr_FieldValue	)*
	|	pr_ArrayItem	(	COMMA9 pr_ArrayItem		)*
    |	pr_IndexValue	(	COMMA9 pr_IndexValue	)*	
	)
	ENDCHAR9
|	LPAREN9
    /* at least 2 elements to avoid shift/reduce conflicts with IntegerValue and FloatValue rules */
    pr_ParameterValue (COMMA9 pr_ParameterValue)+
    RPAREN9
|	COMPLEMENTKEYWORD9 LPAREN9 pr_ParameterValue (COMMA9 pr_ParameterValue)* RPAREN9
|	SUPERSETKeyword9 LPAREN9 pr_ParameterValue (COMMA9 pr_ParameterValue)* RPAREN9
|	SUBSETKeyword9 LPAREN9 pr_ParameterValue (COMMA9 pr_ParameterValue)* RPAREN9  
)
;

pr_FieldValue:
	pr_FieldName ASSIGNMENTCHAR9 pr_ParameterValueOrNotUsedSymbol
;

pr_FieldName:
	pr_Identifier
;

pr_ParameterValueOrNotUsedSymbol:
	MINUS9
|	pr_ParameterValue
;

pr_ArrayItem:
	pr_ParameterValueOrNotUsedSymbol
|	PERMUTATIONKeyword9 LPAREN9 pr_TemplateItemList RPAREN9
;

pr_TemplateItemList:
	pr_ParameterValue 
	(	COMMA9 pr_ParameterValue
	)*
;

pr_IndexValue:
	SQUAREOPEN9 pr_IntegerValueExpression SQUARECLOSE9 ASSIGNMENTCHAR9 pr_ParameterValue
;

pr_IntegerRange:
	LPAREN9
	(	MINUS9 INFINITYKeyword9 DOTDOT9 (pr_IntegerValueExpression | INFINITYKeyword9)
	|	pr_IntegerValueExpression DOTDOT9 (pr_IntegerValueExpression | INFINITYKeyword9)
	)
	RPAREN9
;

pr_FloatRange:
	LPAREN9
	(	MINUS9 INFINITYKeyword9 DOTDOT9 (pr_FloatValueExpression | INFINITYKeyword9)
	|	pr_FloatValueExpression DOTDOT9 (pr_FloatValueExpression | INFINITYKeyword9)
	)
	RPAREN9
;

pr_FloatValueExpression:
	pr_FloatAddExpression
;

pr_FloatAddExpression:
	pr_FloatMulExpression
	(	(	PLUS9
		|	MINUS9
		)
		pr_FloatMulExpression
	)*
;

pr_FloatMulExpression:
	pr_FloatUnaryExpression
	(	(	STAR9
		|	SLASH9
		)
		pr_FloatUnaryExpression
	)*
;

pr_FloatUnaryExpression:
	(	PLUS9
	|	MINUS9
	)*
	pr_FloatPrimaryExpression
;

pr_FloatPrimaryExpression:
(	pr_Float
|	LPAREN9 pr_FloatAddExpression RPAREN9
)
;

pr_StringRange:
	LPAREN9 pr_UniversalOrNotStringValue DOTDOT9 pr_UniversalOrNotStringValue RPAREN9
;

pr_PatternChunkList:
	pr_PatternChunk (AND9 pr_PatternChunk)*
;

pr_PatternChunk:
	pr_CString
|	pr_Quadruple   
;

pr_BStringMatch:
	BITSTRINGMATCH9
;

pr_HStringMatch:
	HEXSTRINGMATCH9
;

pr_OStringMatch:
	OCTETSTRINGMATCH9
;
