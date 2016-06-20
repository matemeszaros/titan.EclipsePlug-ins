parser grammar CfgParser;

@header {
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.common.parsers.cfg.indices.ComponentSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.DefineSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ExecuteSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ExecuteSectionHandler.ExecuteItem;
import org.eclipse.titan.common.parsers.cfg.indices.ExternalCommandSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.GroupSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.IncludeSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingBit;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler.LogParamEntry;
import org.eclipse.titan.common.parsers.cfg.indices.MCSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.ModuleParameterSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.TestportParameterSectionHandler;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IFile;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
}

@members{
	// format strings for error messages if definition (macro or environment variable) cannot be resolved
	// %s : definition
	private static final String DEFINITION_NOT_FOUND_STRING  = "Could not resolve definition: %s using \"\" as a replacement.";
	private static final String DEFINITION_NOT_FOUND_BSTR    = "Could not resolve definition: %s using ''B as a replacement.";
	private static final String DEFINITION_NOT_FOUND_HSTR    = "Could not resolve definition: %s using ''H as a replacement.";
	private static final String DEFINITION_NOT_FOUND_OSTR    = "Could not resolve definition: %s using ''O as a replacement.";
	private static final String DEFINITION_NOT_FOUND_INT     = "Could not resolve integer definition: %s using 0 as replacement.";
	private static final String DEFINITION_NOT_FOUND_FLOAT   = "No macro or environmental variable defined %s could be found, using 0.0 as a replacement value.";
	private static final String DEFINITION_NOT_FOUND_BOOLEAN = "Could not resolve definition: %s using \"true\" as a replacement.";

	// pattern for matching macro string, for example: \$a, \${a}
	private final static Pattern PATTERN_MACRO = Pattern.compile("\\$\\s*\\{?\\s*([A-Za-z][A-Za-z0-9_]*)\\s*\\}?");

	// pattern for matching typed macro string, for example: ${a, float}
	private final static Pattern PATTERN_TYPED_MACRO = Pattern.compile("\\$\\s*\\{\\s*([A-Za-z][A-Za-z0-9_]*)\\s*,\\s*[A-Za-z][A-Za-z0-9_]*\\s*\\}");
	
	private List<TITANMarker> mWarnings = new ArrayList<TITANMarker>();
	
	private List<ISection> mSections = new ArrayList<ISection>();

	private Map<String, CfgDefinitionInformation> mDefinitions = new HashMap<String, CfgDefinitionInformation>();
  
	private List<String> mIncludeFiles = new ArrayList<String>();

	private IFile mActualFile = null;

	private Map<String, String> mEnvVariables;
	
	private CfgParseResult mCfgParseResult = new CfgParseResult();
	
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
	
	/**
	 * Parsed macro info, collected during parsing, it will be processed after the parsing.
	 */
	private class Macro {
		/** parsed macro text */
		private String mMacroName;
		
		/** macro token, needed for the text and position */
		private Token mMacroToken;
		
		/** error message if macro is not found */
		private String mErrorMessage;
		
		public Macro( final String aMacroName, final Token aMacroToken, final String aErrorMessage ) {
			mMacroName = aMacroName;
			mMacroToken = aMacroToken;
			mErrorMessage = aErrorMessage;
		}
		
		public String getMacroName() {
			return mMacroName;
		}

		public Token getMacroToken() {
			return mMacroToken;
		}

		public String getErrorMessage() {
			return mErrorMessage;
		}
	}
	
	private List<Macro> mMacros = new ArrayList<Macro>();
	
	private void addMacro( final String aMacroName, final Token aMacroToken, final String aErrorMessage ) {
		mMacros.add( new Macro( aMacroName, aMacroToken, aErrorMessage ) );
	}
	
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

	public CfgParseResult getCfgParseResult() {
		return mCfgParseResult;
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
	
	/**
	 * Extracts macro name from macro string
	 * @param aMacroString macro string, for example: \$a, \${a}
	 * @return extracted macro name without extra characters, for example: a
	 */
	private String getMacroName( final String aMacroString ) {
		final Matcher m = PATTERN_MACRO.matcher( aMacroString );
		if ( m.find() ) {
			return m.group(1);
		} else {
			return null;
		}
	}
	
	/**
	 * Extracts macro name from typed macro string
	 * @param aMacroString macro string, for example: \${a, float}
	 * @return extracted macro name without extra characters, for example: a
	 */
	private String getTypedMacroName( final String aMacroString ) {
		final Matcher m = PATTERN_TYPED_MACRO.matcher( aMacroString );
		if ( m.find() ) {
			return m.group(1);
		} else {
			return null;
		}
	}
		
	/**
	 * Gets the macro value string of a macro (without type)
	 * @param aMacroToken the macro token
	 * @param aErrorFormatStr format strings for error messages if definition (macro or environment variable) cannot be resolved
	 *                        %s : definition
	 * @return the macro value string
	 *         or "" if macro is invalid. In this case an error marker is also created
	 */
	private String getMacroValue( final Token aMacroToken, final String aErrorFormatStr ) {
		final String definition = getMacroName( aMacroToken.getText() );
		final String errorMsg = String.format( aErrorFormatStr, definition );
		addMacro( definition, aMacroToken, errorMsg );
		final String value = getDefinitionValue( definition );
		if ( value == null ) {
			//TODO: remove, macro errors are processed later
			//reportError( errorMsg, aMacroToken, aMacroToken );
			return "";
		}
		return value;
	}
	
	/**
	 * Gets the macro value string of a macro (with type)
	 * @param aMacroToken the macro token
	 * @param aErrorFormatStr format strings for error messages if definition (macro or environment variable) cannot be resolved
	 *                        %s : definition
	 * @return the macro value string
	 *         or "" if macro is invalid. In this case an error marker is also created
	 */
	private String getTypedMacroValue( Token aMacroToken, String aErrorFormatStr ) {
		final String definition = getTypedMacroName( aMacroToken.getText() );
		final String errorMsg = String.format( aErrorFormatStr, definition );
		addMacro( definition, aMacroToken, errorMsg );
		final String value = getDefinitionValue( definition );
		if ( value == null ) {
			//TODO: remove, macro errors are processed later
			//reportError( errorMsg, aMacroToken, aMacroToken );
			return "";
		}
		return value;
	}
	
	/**
	 * Checks if all the collected macros are valid,
	 * puts error markers if needed
	 */
	public void checkMacroErrors() {
		for ( final Macro macro : mMacros ) {
			final String value = getDefinitionValue( macro.getMacroName() );
			if ( value == null ) {
				reportError( macro.getErrorMessage(), macro.getMacroToken(), macro.getMacroToken() );
			}
		}
	}
	
	/**
	 * Creates a wrapper TerminalNode around the Token to use it as ParseTree
	 * @param aToken token to wrap
	 * @param aParent parent parse tree of the new node
	 * @return the created TerminalNodeImpl object
	 */
	private TerminalNodeImpl newTerminalNode( final Token aToken, final ParseTree aParent ) {
		TerminalNodeImpl node = new TerminalNodeImpl( aToken );
		node.parent = aParent;
		return node;
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
(	mc = pr_MainControllerSection		{ mcSectionHandler.setLastSectionRoot( $mc.ctx ); }
|	i = pr_IncludeSection				{ $section = $i.includeSection;
										  includeSectionHandler.setLastSectionRoot( $i.ctx ); }
|	oi = pr_OrderedIncludeSection		{ orderedIncludeSectionHandler.setLastSectionRoot( $oi.ctx ); }
|	e = pr_ExecuteSection				{ executeSectionHandler.setLastSectionRoot( $e.ctx ); }
|	d = pr_DefineSection				{ defineSectionHandler.setLastSectionRoot( $d.ctx ); }
|	ec = pr_ExternalCommandsSection		{ externalCommandsSectionHandler.setLastSectionRoot( $ec.ctx ); }
|	tp = pr_TestportParametersSection	{ testportParametersHandler.setLastSectionRoot( $tp.ctx ); }
|	g = pr_GroupsSection				{ groupSectionHandler.setLastSectionRoot( $g.ctx ); }
|	mp = pr_ModuleParametersSection		{ moduleParametersHandler.setLastSectionRoot( $mp.ctx ); }
|	c = pr_ComponentsSection			{ componentSectionHandler.setLastSectionRoot( $c.ctx ); }
|	l = pr_LoggingSection				{ loggingSectionHandler.setLastSectionRoot( $l.ctx ); }
|	p = pr_ProfilerSection				//TODO:{ profilerSectionHandler.setLastSectionRoot( $p.ctx ); }
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
	UNIXSOCKETS1
	ASSIGNMENTCHAR1
	u = pr_MainControllerItemUnixDomainSocketValue
	SEMICOLON1?
	{	if ( $u.text != null ) {
			mCfgParseResult.setUnixDomainSocket( "yes".equalsIgnoreCase( $u.text ) );
			mcSectionHandler.setUnixDomainSocketRoot( $ctx );
			mcSectionHandler.setUnixDomainSocket( $u.ctx );
		}
	}
;

pr_MainControllerItemUnixDomainSocketValue:
	(YES1 | NO1)
;

pr_MainControllerItemKillTimer:
	KILLTIMER1
	ASSIGNMENTCHAR1
	k = pr_ArithmeticValueExpression
	SEMICOLON1?
	{	if ( $k.number != null ) {
			mCfgParseResult.setKillTimer( $k.number.getValue() );
			mcSectionHandler.setKillTimerRoot( $ctx );
			mcSectionHandler.setKillTimer( $k.ctx );
		}
	}
;

pr_MainControllerItemLocalAddress:
	LOCALADDRESS1
	ASSIGNMENTCHAR1
	l = pr_HostName
	SEMICOLON1?
	{	mCfgParseResult.setLocalAddress( $l.text );
		mcSectionHandler.setLocalAddressRoot( $ctx );
		mcSectionHandler.setLocalAddress( $l.ctx );
	}
;

pr_MainControllerItemNumHcs:
	NUMHCS1
	ASSIGNMENTCHAR1
	n = pr_IntegerValueExpression
	SEMICOLON1?
	{	if ( $n.number != null ) {
			mCfgParseResult.setNumHcs( $n.number.getIntegerValue() );
			mcSectionHandler.setNumHCsTextRoot( $ctx );
			mcSectionHandler.setNumHCsText( $n.ctx );
		}
	}
;

pr_MainControllerItemTcpPort:
	TCPPORT1
	ASSIGNMENTCHAR1
	t = pr_IntegerValueExpression
	SEMICOLON1?
	{	if ( $t.number != null ) {
			mCfgParseResult.setTcpPort( $t.number.getIntegerValue() );
			mcSectionHandler.setTcpPortRoot( $ctx );
			mcSectionHandler.setTcpPort( $t.ctx );
		}
	}
;

pr_IncludeSection returns [ IncludeSection includeSection ]:
{	$includeSection = new IncludeSection();
}
	INCLUDE_SECTION
	( f = STRING2
		{	String fileName = $f.getText().substring( 1, $f.getText().length() - 1 );
			$includeSection.addIncludeFileName( fileName );
			mIncludeFiles.add( fileName );
			final TerminalNodeImpl node = new TerminalNodeImpl( $f );
			node.parent = $ctx;
			//another solution for the same thing
			//node.parent = includeSectionHandler.getLastSectionRoot();
			includeSectionHandler.getFiles().add( node );
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

pr_ExecuteSectionItem
@init {
	String executeElement = "";
	ExecuteItem item = new ExecuteItem();
}:
	module = pr_ExecuteSectionItemModuleName
		{
			executeElement += $module.name;
			item.setModuleName( $module.ctx );
		}
	(	DOT3
		testcase = pr_ExecuteSectionItemTestcaseName
			{
				executeElement += $testcase.name;
				item.setTestcaseName( $testcase.ctx );
			}
	)?
	{	mCfgParseResult.getExecuteElements().add( executeElement );
		item.setRoot( $ctx );
		executeSectionHandler.getExecuteitems().add( item );
	}
	SEMICOLON3?
;

pr_ExecuteSectionItemModuleName returns [ String name ]:
	t = TTCN3IDENTIFIER3
{	$name = $t.text != null ? $t.text : "";
};

pr_ExecuteSectionItemTestcaseName returns [ String name ]:
	t = ( TTCN3IDENTIFIER3 | STAR3 )
{	$name = $t.text != null ? $t.text : "";
};

pr_DefineSection: 
	DEFINE_SECTION
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
	(	pr_ExternalCommand
		SEMICOLON6?
	)*
;

pr_ExternalCommand:
	(	BEGINCONTROLPART6
		ASSIGNMENTCHAR6
		v = pr_ExternalCommandValue
			{	externalCommandsSectionHandler.setBeginControlPart( $v.ctx );
				externalCommandsSectionHandler.setBeginControlPartRoot( $ctx );
			}
	|	ENDCONTROLPART6
		ASSIGNMENTCHAR6
		v = pr_ExternalCommandValue
			{	externalCommandsSectionHandler.setEndControlPart( $v.ctx );
				externalCommandsSectionHandler.setEndControlPartRoot( $ctx );
			}
	|	BEGINTESTCASE6
		ASSIGNMENTCHAR6
		v = pr_ExternalCommandValue
			{	externalCommandsSectionHandler.setBeginTestcase( $v.ctx );
				externalCommandsSectionHandler.setBeginTestcaseRoot( $ctx );
			}
	|	ENDTESTCASE6 
		ASSIGNMENTCHAR6
		v = pr_ExternalCommandValue
			{	externalCommandsSectionHandler.setEndTestcase( $v.ctx );
				externalCommandsSectionHandler.setEndTestcaseRoot( $ctx );
			}
	)
;

pr_ExternalCommandValue:
	pr_StringValue
;

pr_TestportParametersSection:
	TESTPORT_PARAMETERS_SECTION
	(	pr_TestportParameter
		SEMICOLON7?
	)*
;

pr_TestportParameter:
	a = pr_ComponentID
	DOT7
	b = pr_TestportName
	DOT7
	c = pr_Identifier
	ASSIGNMENTCHAR7
	d = pr_StringValue
{	TestportParameterSectionHandler.TestportParameter parameter = new TestportParameterSectionHandler.TestportParameter();
	parameter.setComponentName( $a.ctx );
	parameter.setTestportName( $b.ctx );
	parameter.setParameterName( $c.ctx );
	parameter.setValue( $d.ctx );
	parameter.setRoot( $ctx );
	testportParametersHandler.getTestportParameters().add( parameter );
}
;

pr_GroupsSection:
	GROUPS_SECTION
	(	pr_GroupItem SEMICOLON8?	
	)*
;

pr_ModuleParametersSection:
	MODULE_PARAMETERS_SECTION
	(	param = pr_ModuleParam
			{	if ( $param.parameter != null ) {
					moduleParametersHandler.getModuleParameters().add( $param.parameter );
					$param.parameter.setRoot( $param.ctx );
				}
			}
		SEMICOLON9?
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
	pr_DatabaseFilePart
	(	AND12
		pr_DatabaseFilePart
	)*
;

pr_DatabaseFilePart:
(	STRING12
|	macro = MACRO12
		{	String value = getMacroValue( $macro, DEFINITION_NOT_FOUND_STRING );
			//TODO: implement: use value if needed
		}
);

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
	pr_StatisticsFilePart
	(	AND12
		pr_StatisticsFilePart
	)*
;

// currently it is the same as pr_DatabaseFilePart,
// but it will be different if value is used
pr_StatisticsFilePart:
(	STRING12
|	macro = MACRO12
		{	String value = getMacroValue( $macro, DEFINITION_NOT_FOUND_STRING );
			//TODO: implement: use value if needed
		}
);

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

pr_LoggerPluginsPart
@init {
	String componentName = "*";
}:
	(	cn = pt_TestComponentID DOT11 { componentName = $cn.text; }
	)?
	LOGGERPLUGINS
	ASSIGNMENTCHAR11
	BEGINCHAR11
	lpl = pr_LoggerPluginsList
	ENDCHAR11
{
	for (LoggingSectionHandler.LoggerPluginEntry item : $lpl.entries) {
		LoggingSectionHandler.LogParamEntry lpe = loggingSectionHandler.componentPlugin(componentName, item.getName());
		lpe.setPluginPath(item.getPath());
	}
	LoggingSectionHandler.LoggerPluginsEntry entry = new LoggingSectionHandler.LoggerPluginsEntry();
	entry.setLoggerPluginsRoot( $ctx );
	entry.setLoggerPluginsListRoot( $lpl.ctx );
	entry.setPluginRoots( new HashMap<String, LoggingSectionHandler.LoggerPluginEntry>( $lpl.entries.size() ) );
	for ( LoggingSectionHandler.LoggerPluginEntry item : $lpl.entries ) {
		entry.getPluginRoots().put(item.getName(), item);
	}
	loggingSectionHandler.getLoggerPluginsTree().put(componentName, entry);
}
;

pr_LoggerPluginsList returns [ List<LoggingSectionHandler.LoggerPluginEntry> entries ]
@init {
	$entries = new ArrayList<LoggingSectionHandler.LoggerPluginEntry>();
}:
	lpe = pr_LoggerPluginEntry { $entries.add( $lpe.entry ); }
	(	COMMA11 lpe = pr_LoggerPluginEntry  { $entries.add( $lpe.entry ); }
	)*
;

pr_PlainLoggingParam
@init {
	String componentName = "*";
	String pluginName = "*";
}:
(	cn = pt_TestComponentID DOT11 { componentName = $cn.text; }
)?
(	STAR11 DOT11
|	pn = pr_Identifier DOT11 { pluginName = $pn.text; }
)?
{	LogParamEntry logParamEntry = loggingSectionHandler.componentPlugin(componentName, pluginName);
}
(   FILEMASK ASSIGNMENTCHAR11 fileMask = pr_LoggingBitMask
		{	logParamEntry.setFileMaskRoot( $ctx );
			logParamEntry.setFileMask( $fileMask.ctx );
			Map<LoggingBit, ParseTree> loggingBitMask = $fileMask.loggingBitMask;
			logParamEntry.setFileMaskBits( loggingBitMask );
		}
|	CONSOLEMASK ASSIGNMENTCHAR11 consoleMask = pr_LoggingBitMask
		{	logParamEntry.setConsoleMaskRoot( $ctx );
			logParamEntry.setConsoleMask( $consoleMask.ctx );
			Map<LoggingBit, ParseTree> loggingBitMask = $consoleMask.loggingBitMask;
			logParamEntry.setConsoleMaskBits( loggingBitMask );
		}
|	DISKFULLACTION ASSIGNMENTCHAR11 dfa = pr_DiskFullActionValue
		{	logParamEntry.setDiskFullActionRoot( $ctx );
			logParamEntry.setDiskFullAction( $dfa.ctx );
		}
|	LOGFILENUMBER ASSIGNMENTCHAR11 lfn = pr_Number
		{	logParamEntry.setLogfileNumberRoot( $ctx );
			logParamEntry.setLogfileNumber( $lfn.ctx );
		}
|	LOGFILESIZE ASSIGNMENTCHAR11 lfs = pr_Number
		{	logParamEntry.setLogfileSizeRoot( $ctx );
			logParamEntry.setLogfileSize( $lfs.ctx );
		}
|	LOGFILENAME ASSIGNMENTCHAR11 f = pr_LogfileName
	{	mCfgParseResult.setLogFileDefined( true );
		String logFileName = $f.text;
		if ( logFileName != null ) {
			// remove quotes
			logFileName = logFileName.replaceAll("^\"|\"$", "");
			mCfgParseResult.setLogFileName( logFileName );
		}
		logParamEntry.setLogFileRoot( $ctx );
		logParamEntry.setLogFile( $f.ctx );
	}
|	(TIMESTAMPFORMAT | CONSOLETIMESTAMPFORMAT) ASSIGNMENTCHAR11 ttv = pr_TimeStampValue
	{	logParamEntry.setTimestampFormatRoot( $ctx );
		logParamEntry.setTimestampFormat( $ttv.ctx );
	}
|	SOURCEINFOFORMAT ASSIGNMENTCHAR11
	(	siv1 = pr_SourceInfoValue
		{	logParamEntry.setSourceInfoFormatRoot( $ctx );
			logParamEntry.setSourceInfoFormat( $siv1.ctx );
		}
	|	siv2 = pr_YesNoOrBoolean
		{	logParamEntry.setSourceInfoFormatRoot( $ctx );
			logParamEntry.setSourceInfoFormat( $siv2.ctx );
		}
	)
|	APPENDFILE ASSIGNMENTCHAR11 af = pr_YesNoOrBoolean
	{	logParamEntry.setAppendFileRoot( $ctx );
		logParamEntry.setAppendFile( $af.ctx );
	}
|	LOGEVENTTYPES ASSIGNMENTCHAR11 let = pr_LogEventTypesValue
	{	logParamEntry.setLogeventTypesRoot( $ctx );
		logParamEntry.setLogeventTypes( $let.ctx );
	}
|	LOGENTITYNAME ASSIGNMENTCHAR11 len = pr_YesNoOrBoolean
	{	logParamEntry.setLogEntityNameRoot( $ctx );
		logParamEntry.setLogEntityName( $len.ctx );
	}
|	MATCHINGHINTS ASSIGNMENTCHAR11 mh = pr_MatchingHintsValue
	{	logParamEntry.setMatchingHintsRoot( $ctx );
		logParamEntry.setMatchingHints( $mh.ctx );
	}
|	o1 = pr_PluginSpecificParamName ASSIGNMENTCHAR11 o2 = pr_StringValue
	{	logParamEntry.getPluginSpecificParam().add(
			new LoggingSectionHandler.PluginSpecificParam( $ctx, $o1.ctx, $o2.ctx, $o1.text ) );
	}
|   EMERGENCYLOGGING ASSIGNMENTCHAR11 el = pr_Number
	{	logParamEntry.setLogEntityNameRoot( $ctx );
		logParamEntry.setEmergencyLogging( $el.ctx );
	}
|   EMERGENCYLOGGINGBEHAVIOUR ASSIGNMENTCHAR11 elb = pr_BufferAllOrMasked
	{	logParamEntry.setLogEntityNameRoot( $ctx );
		logParamEntry.setEmergencyLoggingBehaviour( $elb.ctx );
	}
|   EMERGENCYLOGGINGMASK ASSIGNMENTCHAR11 elm = pr_LoggingBitMask
	{	logParamEntry.setLogEntityNameRoot( $ctx );
		logParamEntry.setEmergencyLoggingMask( $elm.ctx );
		Map<LoggingBit, ParseTree> loggingBitMask = $elm.loggingBitMask;
		//TODO: use loggingBitMask if needed
	}
)
;

pr_TimeStampValue:
	TIMESTAMPVALUE
;

pr_SourceInfoValue:
	SOURCEINFOVALUE
;

pr_PluginSpecificParamName:
	TTCN3IDENTIFIER11
;

pr_BufferAllOrMasked:
	BUFFERALLORBUFFERMASKED
;

pr_DiskFullActionValue:
(	DISKFULLACTIONVALUE
|	DISKFULLACTIONVALUERETRY ( LPAREN11 NUMBER11 RPAREN11 )?	
)
;

pr_LoggerPluginEntry returns [ LoggingSectionHandler.LoggerPluginEntry entry ]
@init {
	$entry = new LoggingSectionHandler.LoggerPluginEntry();
}:
	i = pr_Identifier {	$entry.setName( $i.identifier );
						$entry.setPath("");	} 
	(	ASSIGNMENTCHAR11
		s = pr_StringValue { $entry.setPath( $s.string ); }
	)?
{	$entry.setLoggerPluginRoot( $ctx );
}
;

pt_TestComponentID:
(	pr_Identifier
|	pr_Number
|	MTCKeyword
|	STAR11
)
;

pr_LoggingBitMask returns [ Map<LoggingBit, ParseTree> loggingBitMask ]
@init {
	$loggingBitMask = new EnumMap<LoggingBit, ParseTree>( LoggingBit.class );
}:
	pr_LoggingMaskElement [ $loggingBitMask ]
	(	LOGICALOR11	pr_LoggingMaskElement [ $loggingBitMask ]
	)*
;

pr_LoggingMaskElement [ Map<LoggingBit, ParseTree> loggingBitMask ]:
	pr_LogEventType [ $loggingBitMask ]
|	pr_LogEventTypeSet [ $loggingBitMask ]
|	pr_deprecatedEventTypeSet [ $loggingBitMask ]
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

pr_LogEventType [ Map<LoggingBit, ParseTree> loggingBitMask ]:
(  a1 = ACTION_UNQUALIFIED		{ loggingBitMask.put(LoggingBit.ACTION_UNQUALIFIED, newTerminalNode( $a1, $ctx ) );}
|  a2 = DEBUG_ENCDEC			{ loggingBitMask.put(LoggingBit.DEBUG_ENCDEC, newTerminalNode( $a2, $ctx ) );}
|  a3 = DEBUG_TESTPORT			{ loggingBitMask.put(LoggingBit.DEBUG_TESTPORT, newTerminalNode( $a3, $ctx ) );}
|  a4 = DEBUG_UNQUALIFIED		{ loggingBitMask.put(LoggingBit.DEBUG_UNQUALIFIED, newTerminalNode( $a4, $ctx ) );}
|  a5 = DEFAULTOP_ACTIVATE		{ loggingBitMask.put(LoggingBit.DEFAULTOP_ACTIVATE, newTerminalNode( $a5, $ctx ) );}
|  a6 = DEFAULTOP_DEACTIVATE	{ loggingBitMask.put(LoggingBit.DEFAULTOP_DEACTIVATE, newTerminalNode( $a6, $ctx ) );}
|  a7 = DEFAULTOP_EXIT			{ loggingBitMask.put(LoggingBit.DEFAULTOP_EXIT, newTerminalNode( $a7, $ctx ) );}
|  a8 = DEFAULTOP_UNQUALIFIED	{ loggingBitMask.put(LoggingBit.DEFAULTOP_UNQUALIFIED, newTerminalNode( $a8, $ctx ) );}
|  a9 = ERROR_UNQUALIFIED		{ loggingBitMask.put(LoggingBit.ERROR_UNQUALIFIED, newTerminalNode( $a9, $ctx ) );}
|  a10 = EXECUTOR_COMPONENT		{ loggingBitMask.put(LoggingBit.EXECUTOR_COMPONENT, newTerminalNode( $a10, $ctx ) );}
|  a11 = EXECUTOR_CONFIGDATA	{ loggingBitMask.put(LoggingBit.EXECUTOR_CONFIGDATA, newTerminalNode( $a11, $ctx ) );}
|  a12 = EXECUTOR_EXTCOMMAND	{ loggingBitMask.put(LoggingBit.EXECUTOR_EXTCOMMAND, newTerminalNode( $a12, $ctx ) );}
|  a13 = EXECUTOR_LOGOPTIONS	{ loggingBitMask.put(LoggingBit.EXECUTOR_LOGOPTIONS, newTerminalNode( $a13, $ctx ) );}
|  a14 = EXECUTOR_RUNTIME		{ loggingBitMask.put(LoggingBit.EXECUTOR_RUNTIME, newTerminalNode( $a14, $ctx ) );}
|  a15 = EXECUTOR_UNQUALIFIED	{ loggingBitMask.put(LoggingBit.EXECUTOR_UNQUALIFIED, newTerminalNode( $a15, $ctx ) );}
|  a16 = FUNCTION_RND			{ loggingBitMask.put(LoggingBit.FUNCTION_RND, newTerminalNode( $a16, $ctx ) );}
|  a17 = FUNCTION_UNQUALIFIED	{ loggingBitMask.put(LoggingBit.FUNCTION_UNQUALIFIED, newTerminalNode( $a17, $ctx ) );}
|  a18 = MATCHING_DONE			{ loggingBitMask.put(LoggingBit.MATCHING_DONE, newTerminalNode( $a18, $ctx ) );}
|  a19 = MATCHING_MCSUCCESS		{ loggingBitMask.put(LoggingBit.MATCHING_MCSUCCESS, newTerminalNode( $a19, $ctx ) );}
|  a20 = MATCHING_MCUNSUCC		{ loggingBitMask.put(LoggingBit.MATCHING_MCUNSUCC, newTerminalNode( $a20, $ctx ) );}
|  a21 = MATCHING_MMSUCCESS		{ loggingBitMask.put(LoggingBit.MATCHING_MMSUCCESS, newTerminalNode( $a21, $ctx ) );}
|  a22 = MATCHING_MMUNSUCC		{ loggingBitMask.put(LoggingBit.MATCHING_MMUNSUCC, newTerminalNode( $a22, $ctx ) );}
|  a23 = MATCHING_PCSUCCESS		{ loggingBitMask.put(LoggingBit.MATCHING_PCSUCCESS, newTerminalNode( $a23, $ctx ) );}
|  a24 = MATCHING_PCUNSUCC		{ loggingBitMask.put(LoggingBit.MATCHING_PCUNSUCC, newTerminalNode( $a24, $ctx ) );}
|  a25 = MATCHING_PMSUCCESS		{ loggingBitMask.put(LoggingBit.MATCHING_PMSUCCESS, newTerminalNode( $a25, $ctx ) );}
|  a26 = MATCHING_PMUNSUCC		{ loggingBitMask.put(LoggingBit.MATCHING_PMUNSUCC, newTerminalNode( $a26, $ctx ) );}
|  a27 = MATCHING_PROBLEM		{ loggingBitMask.put(LoggingBit.MATCHING_PROBLEM, newTerminalNode( $a27, $ctx ) );}
|  a28 = MATCHING_TIMEOUT		{ loggingBitMask.put(LoggingBit.MATCHING_TIMEOUT, newTerminalNode( $a28, $ctx ) );}
|  a29 = MATCHING_UNQUALIFIED	{ loggingBitMask.put(LoggingBit.MATCHING_UNQUALIFIED, newTerminalNode( $a29, $ctx ) );}
|  a30 = PARALLEL_PORTCONN		{ loggingBitMask.put(LoggingBit.PARALLEL_PORTCONN, newTerminalNode( $a30, $ctx ) );}
|  a31 = PARALLEL_PORTMAP		{ loggingBitMask.put(LoggingBit.PARALLEL_PORTMAP, newTerminalNode( $a31, $ctx ) );}
|  a32 = PARALLEL_PTC			{ loggingBitMask.put(LoggingBit.PARALLEL_PTC, newTerminalNode( $a32, $ctx ) );}
|  a33 = PARALLEL_UNQUALIFIED	{ loggingBitMask.put(LoggingBit.PARALLEL_UNQUALIFIED, newTerminalNode( $a33, $ctx ) );}
|  a34 = PORTEVENT_DUALRECV		{ loggingBitMask.put(LoggingBit.PORTEVENT_DUALRECV, newTerminalNode( $a34, $ctx ) );}
|  a35 = PORTEVENT_DUALSEND		{ loggingBitMask.put(LoggingBit.PORTEVENT_DUALSEND, newTerminalNode( $a35, $ctx ) );}
|  a36 = PORTEVENT_MCRECV		{ loggingBitMask.put(LoggingBit.PORTEVENT_MCRECV, newTerminalNode( $a36, $ctx ) );}
|  a37 = PORTEVENT_MCSEND		{ loggingBitMask.put(LoggingBit.PORTEVENT_MCSEND, newTerminalNode( $a37, $ctx ) );}
|  a38 = PORTEVENT_MMRECV		{ loggingBitMask.put(LoggingBit.PORTEVENT_MMRECV, newTerminalNode( $a38, $ctx ) );}
|  a39 = PORTEVENT_MMSEND		{ loggingBitMask.put(LoggingBit.PORTEVENT_MMSEND, newTerminalNode( $a39, $ctx ) );}
|  a40 = PORTEVENT_MQUEUE		{ loggingBitMask.put(LoggingBit.PORTEVENT_MQUEUE, newTerminalNode( $a40, $ctx ) );}
|  a41 = PORTEVENT_PCIN			{ loggingBitMask.put(LoggingBit.PORTEVENT_PCIN, newTerminalNode( $a41, $ctx ) );}
|  a42 = PORTEVENT_PCOUT		{ loggingBitMask.put(LoggingBit.PORTEVENT_PCOUT, newTerminalNode( $a42, $ctx ) );}
|  a43 = PORTEVENT_PMIN			{ loggingBitMask.put(LoggingBit.PORTEVENT_PMIN, newTerminalNode( $a43, $ctx ) );}
|  a44 = PORTEVENT_PMOUT		{ loggingBitMask.put(LoggingBit.PORTEVENT_PMOUT, newTerminalNode( $a44, $ctx ) );}
|  a45 = PORTEVENT_PQUEUE		{ loggingBitMask.put(LoggingBit.PORTEVENT_PQUEUE, newTerminalNode( $a45, $ctx ) );}
|  a46 = PORTEVENT_STATE		{ loggingBitMask.put(LoggingBit.PORTEVENT_STATE, newTerminalNode( $a46, $ctx ) );}
|  a47 = PORTEVENT_UNQUALIFIED	{ loggingBitMask.put(LoggingBit.PORTEVENT_UNQUALIFIED, newTerminalNode( $a47, $ctx ) );}
|  a48 = STATISTICS_UNQUALIFIED	{ loggingBitMask.put(LoggingBit.STATISTICS_UNQUALIFIED, newTerminalNode( $a48, $ctx ) );}
|  a49 = STATISTICS_VERDICT		{ loggingBitMask.put(LoggingBit.STATISTICS_VERDICT, newTerminalNode( $a49, $ctx ) );}
|  a50 = TESTCASE_FINISH		{ loggingBitMask.put(LoggingBit.TESTCASE_FINISH, newTerminalNode( $a50, $ctx ) );}
|  a51 = TESTCASE_START			{ loggingBitMask.put(LoggingBit.TESTCASE_START, newTerminalNode( $a51, $ctx ) );}
|  a52 = TESTCASE_UNQUALIFIED	{ loggingBitMask.put(LoggingBit.TESTCASE_UNQUALIFIED, newTerminalNode( $a52, $ctx ) );}
|  a53 = TIMEROP_GUARD			{ loggingBitMask.put(LoggingBit.TIMEROP_GUARD, newTerminalNode( $a53, $ctx ) );}
|  a54 = TIMEROP_READ			{ loggingBitMask.put(LoggingBit.TIMEROP_READ, newTerminalNode( $a54, $ctx ) );}
|  a55 = TIMEROP_START			{ loggingBitMask.put(LoggingBit.TIMEROP_START, newTerminalNode( $a55, $ctx ) );}
|  a56 = TIMEROP_STOP			{ loggingBitMask.put(LoggingBit.TIMEROP_STOP, newTerminalNode( $a56, $ctx ) );}
|  a57 = TIMEROP_TIMEOUT		{ loggingBitMask.put(LoggingBit.TIMEROP_TIMEOUT, newTerminalNode( $a57, $ctx ) );}
|  a58 = TIMEROP_UNQUALIFIED	{ loggingBitMask.put(LoggingBit.TIMEROP_UNQUALIFIED, newTerminalNode( $a58, $ctx ) );}
|  a59 = USER_UNQUALIFIED		{ loggingBitMask.put(LoggingBit.USER_UNQUALIFIED, newTerminalNode( $a59, $ctx ) );}
|  a60 = VERDICTOP_FINAL		{ loggingBitMask.put(LoggingBit.VERDICTOP_FINAL, newTerminalNode( $a60, $ctx ) );}
|  a61 = VERDICTOP_GETVERDICT	{ loggingBitMask.put(LoggingBit.VERDICTOP_GETVERDICT, newTerminalNode( $a61, $ctx ) );}
|  a62 = VERDICTOP_SETVERDICT	{ loggingBitMask.put(LoggingBit.VERDICTOP_SETVERDICT, newTerminalNode( $a62, $ctx ) );}
|  a63 = VERDICTOP_UNQUALIFIED	{ loggingBitMask.put(LoggingBit.VERDICTOP_UNQUALIFIED, newTerminalNode( $a63, $ctx ) );}
|  a64 = WARNING_UNQUALIFIED	{ loggingBitMask.put(LoggingBit.WARNING_UNQUALIFIED, newTerminalNode( $a64, $ctx ) );}
)	
;

pr_LogEventTypeSet [ Map<LoggingBit, ParseTree> loggingBitMask ]:
(  a1 = TTCN_EXECUTOR2		{ loggingBitMask.put(LoggingBit.EXECUTOR, newTerminalNode( $a1, $ctx ) );}
|  a2 = TTCN_ERROR2			{ loggingBitMask.put(LoggingBit.ERROR, newTerminalNode( $a2, $ctx ) );}
|  a3 = TTCN_WARNING2		{ loggingBitMask.put(LoggingBit.WARNING, newTerminalNode( $a3, $ctx ) );}
|  a4 = TTCN_PORTEVENT2		{ loggingBitMask.put(LoggingBit.PORTEVENT, newTerminalNode( $a4, $ctx ) );}
|  a5 = TTCN_TIMEROP2		{ loggingBitMask.put(LoggingBit.TIMEROP, newTerminalNode( $a5, $ctx ) );}
|  a6 = TTCN_VERDICTOP2		{ loggingBitMask.put(LoggingBit.VERDICTOP, newTerminalNode( $a6, $ctx ) );}
|  a7 = TTCN_DEFAULTOP2		{ loggingBitMask.put(LoggingBit.DEFAULTOP, newTerminalNode( $a7, $ctx ) );}
|  a8 = TTCN_ACTION2		{ loggingBitMask.put(LoggingBit.ACTION, newTerminalNode( $a8, $ctx ) );}
|  a9 = TTCN_TESTCASE2		{ loggingBitMask.put(LoggingBit.TESTCASE, newTerminalNode( $a9, $ctx ) );}
|  a10 = TTCN_FUNCTION2		{ loggingBitMask.put(LoggingBit.FUNCTION, newTerminalNode( $a10, $ctx ) );}
|  a11 = TTCN_USER2			{ loggingBitMask.put(LoggingBit.USER, newTerminalNode( $a11, $ctx ) );}
|  a12 = TTCN_STATISTICS2	{ loggingBitMask.put(LoggingBit.STATISTICS, newTerminalNode( $a12, $ctx ) );}
|  a13 = TTCN_PARALLEL2		{ loggingBitMask.put(LoggingBit.PARALLEL, newTerminalNode( $a13, $ctx ) );}
|  a14 = TTCN_MATCHING2		{ loggingBitMask.put(LoggingBit.MATCHING, newTerminalNode( $a14, $ctx ) );}
|  a15 = TTCN_DEBUG2		{ loggingBitMask.put(LoggingBit.DEBUG, newTerminalNode( $a15, $ctx ) );}
|  a16 = LOG_ALL			{ loggingBitMask.put(LoggingBit.LOG_ALL, newTerminalNode( $a16, $ctx ) );}
|  a17 = LOG_NOTHING		{ loggingBitMask.put(LoggingBit.LOG_NOTHING, newTerminalNode( $a17, $ctx ) );}
)
;

pr_deprecatedEventTypeSet [ Map<LoggingBit, ParseTree> loggingBitMask ]:
(  a1 = TTCN_EXECUTOR1		{ loggingBitMask.put(LoggingBit.EXECUTOR, newTerminalNode( $a1, $ctx ) );}
|  a2 = TTCN_ERROR1			{ loggingBitMask.put(LoggingBit.ERROR, newTerminalNode( $a2, $ctx ) );}
|  a3 = TTCN_WARNING1		{ loggingBitMask.put(LoggingBit.WARNING, newTerminalNode( $a3, $ctx ) );}
|  a4 = TTCN_PORTEVENT1		{ loggingBitMask.put(LoggingBit.PORTEVENT, newTerminalNode( $a4, $ctx ) );}
|  a5 = TTCN_TIMEROP1		{ loggingBitMask.put(LoggingBit.TIMEROP, newTerminalNode( $a5, $ctx ) );}
|  a6 = TTCN_VERDICTOP1		{ loggingBitMask.put(LoggingBit.VERDICTOP, newTerminalNode( $a6, $ctx ) );}
|  a7 = TTCN_DEFAULTOP1		{ loggingBitMask.put(LoggingBit.DEFAULTOP, newTerminalNode( $a7, $ctx ) );}
|  a8 = TTCN_ACTION1		{ loggingBitMask.put(LoggingBit.ACTION, newTerminalNode( $a8, $ctx ) );}
|  a9 = TTCN_TESTCASE1		{ loggingBitMask.put(LoggingBit.TESTCASE, newTerminalNode( $a9, $ctx ) );}
|  a10 = TTCN_FUNCTION1		{ loggingBitMask.put(LoggingBit.FUNCTION, newTerminalNode( $a10, $ctx ) );}
|  a11 = TTCN_USER1			{ loggingBitMask.put(LoggingBit.USER, newTerminalNode( $a11, $ctx ) );}
|  a12 = TTCN_STATISTICS1	{ loggingBitMask.put(LoggingBit.STATISTICS, newTerminalNode( $a12, $ctx ) );}
|  a13 = TTCN_PARALLEL1		{ loggingBitMask.put(LoggingBit.PARALLEL, newTerminalNode( $a13, $ctx ) );}
|  a14 = TTCN_MATCHING1		{ loggingBitMask.put(LoggingBit.MATCHING, newTerminalNode( $a14, $ctx ) );}
|  a15 = TTCN_DEBUG1		{ loggingBitMask.put(LoggingBit.DEBUG, newTerminalNode( $a15, $ctx ) );}
)
{	reportWarning(new TITANMarker("Deprecated logging option " + $start.getText(), $start.getLine(),
		$start.getStartIndex(), $start.getStopIndex(), IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL));
}
;

pr_Detailed:
	DETAILED
|	SUBCATEGORIES
;

pr_ComponentItem:
{	ComponentSectionHandler.Component component = new ComponentSectionHandler.Component();
}
	n = pr_ComponentName
		{	component.setComponentName( $n.ctx );	}
	ASSIGNMENTCHAR10 
	(	h = pr_HostName {	mCfgParseResult.getComponents().put( $n.text, $h.text );
							component.setHostName( $h.ctx );
						}
	|	i = pr_HostNameIpV6	{	mCfgParseResult.getComponents().put( $n.text, $i.text );
								component.setHostName( $i.ctx );
							}
	)
{	component.setRoot( $ctx );
	componentSectionHandler.getComponents().add( component );
}
;

pr_ComponentName:
(	pr_Identifier
|	STAR10
)
;

pr_HostName:
(	pr_DNSName
|	TTCN3IDENTIFIER1 | TTCN3IDENTIFIER10
|	macro1 = (MACRO_HOSTNAME1 | MACRO_HOSTNAME10)
		{	String value = getTypedMacroValue( $macro1, DEFINITION_NOT_FOUND_STRING );
			//TODO: implement: use value if needed
		}
|	macro2 = (MACRO1 | MACRO10)
		{	String value = getMacroValue( $macro2, DEFINITION_NOT_FOUND_STRING );
			//TODO: implement: use value if needed
		}
)
;

pr_HostNameIpV6:
	IPV6_10
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
|	MACRO_HOSTNAME5
|	MACRO5
|	IPV6_5
|	STRING5
|	BITSTRING5
|	HEXSTRING5
|	OCTETSTRING5
|	BITSTRINGMATCH5
|	HEXSTRINGMATCH5
|	OCTETSTRINGMATCH5
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

pr_Identifier returns [String identifier]:
(	macro = (MACRO_ID7 | MACRO_ID8 | MACRO_ID9 | MACRO_ID10 | MACRO_ID11)
		{	String value = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_STRING );
			$identifier = value;
		}
|	a = (TTCN3IDENTIFIER7 | TTCN3IDENTIFIER8 | TTCN3IDENTIFIER9 | TTCN3IDENTIFIER10 | TTCN3IDENTIFIER11)
		{	$identifier = $a.getText();	}
)
;

pr_IntegerValueExpression returns [CFGNumber number]:
	a = pr_IntegerAddExpression	{	$number = $a.number;	}
;

pr_IntegerAddExpression returns [CFGNumber number]:
	a = pr_IntegerMulExpression	{	$number = $a.number;	}
	(	(	PLUS1 | PLUS7 | PLUS9	)	b1 = pr_IntegerMulExpression	{	$number.add($b1.number);	}
	|	(	MINUS1 | MINUS7 | MINUS9	)	b2 = pr_IntegerMulExpression	{	$b2.number.mul(-1); $number.add($b2.number);	}
	)*
;

pr_IntegerMulExpression returns [CFGNumber number]:
	a = pr_IntegerUnaryExpression	{	$number = $a.number;	}
	(	(	STAR1 | STAR7 | STAR9	)	b1 = pr_IntegerUnaryExpression	{	$number.mul($b1.number);	}
	|	(	SLASH1 | SLASH7 | SLASH9	)	b2 = pr_IntegerUnaryExpression
		{	try {
				$number.div($b2.number);
			} catch ( ArithmeticException e ) {
				// division by 0
				reportError( e.getMessage(), $a.start, $b2.stop );
				$number = new CFGNumber( "0" );
			}
		}
	)*
;

pr_IntegerUnaryExpression returns [CFGNumber number]:
{	boolean negate = false;
}
	(	(PLUS1 | PLUS7 | PLUS9)
	|	(MINUS1 | MINUS7 | MINUS9)	{	negate = !negate;	}
	)*
	a = pr_IntegerPrimaryExpression
		{	$number = $a.number;
			if ( negate ) {
				$number.mul( -1 );
			}
		}
;

pr_IntegerPrimaryExpression returns [CFGNumber number]:
(	a = pr_Number	{	$number = $a.number;	}
|	LPAREN1 b = pr_IntegerAddExpression RPAREN1	{	$number = $b.number;	}
|	LPAREN7 c = pr_IntegerAddExpression RPAREN7	{	$number = $c.number;	}
|	LPAREN9 d = pr_IntegerAddExpression RPAREN9	{	$number = $d.number;	}
)
;

pr_Number returns [CFGNumber number]:
(	a = (NUMBER1 | NUMBER7 | NUMBER9 | NUMBER11)	{$number = new CFGNumber($a.text);}
|	macro = pr_MacroNumber { $number = $macro.number; }
|	TTCN3IDENTIFIER9 // module parameter name
		{	$number = new CFGNumber( "1" ); // value is unknown yet, but it should not be null
		}
)
;

pr_MacroNumber returns [CFGNumber number]:
(	macro1 = (MACRO_INT1 | MACRO_INT7 | MACRO_INT9 | MACRO_INT11)	
		{	String value = getTypedMacroValue( $macro1, DEFINITION_NOT_FOUND_INT );
			$number = new CFGNumber( value.length() > 0 ? value : "0" );
		}
|	macro2 = (MACRO1 | MACRO7 | MACRO9 | MACRO11)	
		{	String value = getMacroValue( $macro2, DEFINITION_NOT_FOUND_INT );
			$number = new CFGNumber( value.length() > 0 ? value : "0" );
		}
)
;

pr_StringValue returns [String string]
@init {
	$string = "";
}:
	a = pr_CString
		{	if ( $a.string != null ) {
				$string = $a.string.replaceAll("^\"|\"$", "");
			}
		}
	(	(STRINGOP1 | STRINGOP6 | STRINGOP7 | STRINGOP9 | STRINGOP11) b = pr_CString
			{	if ( $b.string != null ) {
					$string = $string + $b.string.replaceAll("^\"|\"$", "");
				}
			}
	)*
	{	if ( $string != null ) {
			$string = "\"" + $string + "\"";
		}
	}
;

pr_CString returns [String string]:
(	a = (STRING1 | STRING6 | STRING7 | STRING9 | STRING11)
		{	
			$string = $a.text;
		}
|	macro2 = pr_MacroCString			{	$string = "\"" + $macro2.string + "\"";	}
|	macro1 = pr_MacroExpliciteCString	{	$string = "\"" + $macro1.string + "\"";	}
|	TTCN3IDENTIFIER9 // module parameter name
		{	$string = "\"\""; // value is unknown yet, but it should not be null
		}
)
;

pr_MacroCString returns [String string]:
	macro = (MACRO1 | MACRO6 | MACRO7 | MACRO9 | MACRO11)
		{	$string = getMacroValue( $macro, DEFINITION_NOT_FOUND_STRING );	}
;

pr_MacroExpliciteCString returns [String string]:
	macro = (MACRO_EXP_CSTR1 | MACRO_EXP_CSTR6 | MACRO_EXP_CSTR7 | MACRO_EXP_CSTR9 | MACRO_EXP_CSTR11)
		{	$string = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_STRING );	}
;

pr_GroupItem:
{	List<String> memberlist = new ArrayList<String>();
	GroupSectionHandler.Group group = new GroupSectionHandler.Group();
}
(	a = pr_Identifier
	ASSIGNMENTCHAR8
	(	STAR8 {  memberlist.add("*");  }
	|	(	c = pr_DNSName	{	memberlist.add( $c.text );	
								group.getGroupItems().add( new GroupSectionHandler.GroupItem( $c.ctx ) );
							}
		|	d = pr_Identifier	{	memberlist.add( $d.text );
									group.getGroupItems().add( new GroupSectionHandler.GroupItem( $d.ctx ) );
								}
		)
		(	COMMA8
			(	e = pr_DNSName	{	memberlist.add( $e.text );
									group.getGroupItems().add( new GroupSectionHandler.GroupItem( $e.ctx ) );
								}
			|	f = pr_Identifier	{	memberlist.add( $f.text );
										group.getGroupItems().add( new GroupSectionHandler.GroupItem( $f.ctx ) );
									}
			)
		)*
	)
)
{	mCfgParseResult.getGroups().put( $a.text, memberlist.toArray( new String[ memberlist.size() ] ) );
	group.setGroupName( $a.ctx );
	group.setRoot( $ctx );
	groupSectionHandler.getGroups().add( group );
}
;

pr_DNSName:
(	NUMBER1 | NUMBER8 | NUMBER10
|	FLOAT1 | FLOAT8 | FLOAT10
|	DNSNAME1 | DNSNAME8 | DNSNAME10
)
;

pr_ModuleParam returns[ModuleParameterSectionHandler.ModuleParameter parameter = null]:
	name = pr_ParameterName	{$parameter = $name.parameter;}
	(	ASSIGNMENTCHAR9
		val1 = pr_ParameterValue	{$parameter.setValue($val1.ctx);}
	|	CONCATCHAR9
		val2 = pr_ParameterValue	{$parameter.setValue($val2.ctx);}
	)
;

pr_ParameterName returns[ModuleParameterSectionHandler.ModuleParameter parameter = null]:
{	$parameter = new ModuleParameterSectionHandler.ModuleParameter();
	$parameter.setRoot( $ctx );
}
(	id1 = pr_Identifier
	(	separator = pr_Dot
		id2 = pr_Identifier {	$parameter.setModuleName( $id1.ctx );
								$parameter.setSeparator( $separator.ctx );
								$parameter.setParameterName( $id2.ctx );
							}
	|	{	$parameter.setParameterName( $id1.ctx );
		}
	)
|	star = pr_StarModuleName
	DOT9
	id3 = pr_Identifier
	{	$parameter.setModuleName($star.ctx);
		$parameter.setParameterName($id3.ctx);
	}
)
;

pr_Dot:
	DOT9
;

pr_StarModuleName:
	STAR9
;

pr_ParameterValue:
	pr_ParameterExpression pr_LengthMatch? IFPRESENTKeyword9?
;

//module parameter expression, it can contain previously defined module parameters
pr_ParameterExpression:
	pr_SimpleParameterValue
|	pr_ParameterReference
|	pr_ParameterExpression
	(	(	PLUS9
		|	MINUS9
		|	STAR9
		|	SLASH9
		|	STRINGOP9
		)
		pr_ParameterExpression
	)+
|	(	PLUS9
	|	MINUS9
	)
	pr_ParameterExpression
|	LPAREN9
	pr_ParameterExpression
	RPAREN9
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
pr_ParameterReference:
	// enumerated values are also treated as references by the parser,
	// these will be sorted out later during set_param()
	pr_ParameterNameSegment
;

pr_ParameterNameSegment:
	pr_ParameterNameSegment
	pr_Dot
	pr_Identifier
|	pr_ParameterNameSegment
	pr_IndexItemIndex
|	pr_Identifier
;

pr_IndexItemIndex:
	SQUAREOPEN9
	pr_IntegerValueExpression
	SQUARECLOSE9
;

pr_LengthBound:
	pr_IntegerValueExpression
;

pr_ArithmeticValueExpression returns [CFGNumber number]:
	a = pr_ArithmeticAddExpression	{	$number = $a.number;	}
;

pr_ArithmeticAddExpression returns [CFGNumber number]:
	a = pr_ArithmeticMulExpression	{	$number = $a.number;	}
	(	(	PLUS1 | PLUS9	)	b1 = pr_ArithmeticMulExpression	{	$number.add($b1.number);	}
	|	(	MINUS1 | MINUS9	)	b2 = pr_ArithmeticMulExpression	{	$b2.number.mul(-1); $number.add($b2.number);	}
	)*
;

pr_ArithmeticMulExpression returns [CFGNumber number]:
	a = pr_ArithmeticUnaryExpression	{	$number = $a.number;	}
	(	(	STAR1 | STAR9	)	b1 = pr_ArithmeticUnaryExpression	{	$number.mul($b1.number);	}
	|	(	SLASH1 | SLASH9	)	b2 = pr_ArithmeticUnaryExpression
		{	try {
				$number.div($b2.number);
			} catch ( ArithmeticException e ) {
				// division by 0
				reportError( e.getMessage(), $a.start, $b2.stop );
				$number = new CFGNumber( "0.0" );
			}
		}
	)*
;

pr_ArithmeticUnaryExpression returns [CFGNumber number]:
{	boolean negate = false;
}
	(	(	PLUS1 | PLUS9	)
	|	(	MINUS1 | MINUS9	)	{	negate = !negate;	}
	)*
	a = pr_ArithmeticPrimaryExpression
		{	$number = $a.number;
			if ( negate ) {
				$number.mul( -1 );
			}
		}
;

pr_ArithmeticPrimaryExpression returns [CFGNumber number]:
(	a = pr_Float	{$number = $a.number;}
|	b = pr_Number	{$number = $b.number;}
|	LPAREN1 c = pr_ArithmeticAddExpression RPAREN1 {$number = $c.number;}
|	LPAREN9 d = pr_ArithmeticAddExpression RPAREN9 {$number = $d.number;}
)
;

pr_Float returns [CFGNumber number]:
(	a = (FLOAT1 | FLOAT9) {$number = new CFGNumber($a.text);}
|	macro = (MACRO_FLOAT1 | MACRO_FLOAT9)
		{	String value = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_FLOAT );
			$number = new CFGNumber( value.length() > 0 ? value : "0.0" );
		}
|	TTCN3IDENTIFIER9 // module parameter name
		{	$number = new CFGNumber( "1.0" ); // value is unknown yet, but it should not be null
		}
)
;

pr_Boolean returns [String string]:
(	t = (TRUE9 | TRUE11) { $string = $t.getText(); }
|	f = (FALSE9 | FALSE11) { $string = $f.getText(); }
|	macro = (MACRO_BOOL9 | MACRO_BOOL11)
		{	String value = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_BOOLEAN );
			if ( "false".equals( value ) ) {
				$string = "false";
			} else {
				$string = "true";
			}
		}
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

pr_BString returns [String string]:
(	b = BITSTRING9 { $string = $b.getText(); }
|	macro = MACRO_BSTR9
		{	String value = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_BSTR );
			$string = "'" + value + "'B";
		}
)
;

pr_HStringValue:
	pr_HString	(	STRINGOP9 pr_HString	)*
;

pr_HString returns [String string]:
(	h = HEXSTRING9 { $string = $h.getText(); }
|	macro = MACRO_HSTR9
		{	String value = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_HSTR );
			$string = "'" + value + "'H";
		}
)
;

pr_OStringValue:
	pr_OString	(	STRINGOP9 pr_OString	)*
;

pr_OString returns [String string]:
(	o = OCTETSTRING9 { $string = $o.getText(); }
|	macro = MACRO_OSTR9
		{	String value = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_OSTR );
			$string = "'" + value + "'0";
		}
|	macro_bin = MACRO_BINARY9
		{	String value = getTypedMacroValue( $macro_bin, DEFINITION_NOT_FOUND_STRING );
			$string = value;
		}
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
