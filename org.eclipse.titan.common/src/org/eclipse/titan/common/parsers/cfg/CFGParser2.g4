parser grammar CFGParser2;

@header {
import java.util.HashMap;

import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IFile;
}

@members{
	private ArrayList<TITANMarker> warnings = new ArrayList<TITANMarker>();

	public void reportWarning(TITANMarker marker){
		warnings.add(marker);
	}

	public ArrayList<TITANMarker> getWarnings(){
		return warnings;
	}

	private HashMap<String,CfgDefinitionInformation> definitions = new HashMap<String, CfgDefinitionInformation>();

	public HashMap<String,CfgDefinitionInformation> getDefinitions(){
		return definitions;
	}

	private ArrayList<String> includeFiles = new ArrayList<String>();

	public ArrayList<String> getIncludeFiles(){
		return includeFiles;
	}

	private IFile actualFile = null;

	public void setActualFile(IFile file) {
		actualFile = file;
	}

	private boolean logFileDefined = false;
	
	public boolean isLogFileDefined() {
		return logFileDefined;
	}
}

options{
	tokenVocab=CFGLexer2;
}

pr_ConfigFile:
(pr_section)+
EOF;
 
pr_section:
(
	pr_DefaultSection	
|	pr_MainControllerSection
|	pr_IncludeSection
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
(
	WS
|	LINE_COMMENT
|	BLOCK_COMMENT	
)
;
pr_MainControllerSection:
MAIN_CONTROLLER_SECTION
(
	pr_MainControllerItem (SEMICOLON1)?
)*
;
pr_MainControllerItem:
(
	UNIXSOCKETS1 ASSIGNMENTCHAR1 (YES1 | NO1) (SEMICOLON1)?
|	KILLTIMER1 ASSIGNMENTCHAR1 pr_ArithmeticValueExpression (SEMICOLON1)?
|	LOCALADDRESS1 ASSIGNMENTCHAR1 pr_HostName (SEMICOLON1)?
|	NUMHCS1 ASSIGNMENTCHAR1 pr_IntegerValueExpression (SEMICOLON1)?
|	TCPPORT1 ASSIGNMENTCHAR1 pr_IntegerValueExpression (SEMICOLON1)?
)+ 
;

pr_IncludeSection:
INCLUDE_SECTION
(
	STRING2
)*?
;

pr_OrderedIncludeSection:
ORDERED_INCLUDE_SECTION
(
	STRING4
)*?
;

pr_ExecuteSection:
EXECUTE_SECTION
(
	TEST3 (SEMICOLON3)?
)*
;

pr_DefineSection: 
DEFINE_SECTION
(
	pr_MacroAssignment
)*
;

pr_ExternalCommandsSection:
EXTERNAL_COMMANDS_SECTION
(
	(
		BEGINCONTROLPART6
	|	ENDCONTROLPART6
	|	BEGINTESTCASE6
	|	ENDTESTCASE6 
	)
	ASSIGNMENTCHAR6
	STRING6
	(SEMICOLON6)?
)*
;

pr_TestportParametersSection:
TESTPORT_PARAMETERS_SECTION
(
	pr_ComponentID DOT7 pr_TestportName DOT7 pr_Identifier ASSIGNMENTCHAR7 pr_StringValue (SEMICOLON7)?
)*
;

pr_GroupsSection:
GROUPS_SECTION
(
	pr_GroupItem (SEMICOLON8)?	
)*
;

pr_ModuleParametersSection:
MODULE_PARAMETERS_SECTION
(
	pr_ModuleParam (SEMICOLON9)?
)*
;

pr_ComponentsSection:
COMPONENTS_SECTION
(
	pr_ComponentItem (SEMICOLON10)?
)*
;

pr_LoggingSection:
LOGGING_SECTION
(
	pr_LoggingParam (SEMICOLON11)?
)*
;

pr_ProfilerSection:
PROFILER_SECTION
(
	pr_ProfilerSetting
	(
		SEMICOLON12
	)?
)*
;

pr_ProfilerSetting:
(
	pr_DisableProfiler
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
(
	DISABLEPROFILER
	ASSIGNMENTCHAR12
	(
		TRUE12
	|	FALSE12
	)
)
;

pr_DisableCoverage:
(
	DISABLECOVERAGE
	ASSIGNMENTCHAR12
	(
		TRUE12
	|	FALSE12
	)
)
;

pr_DatabaseFile:
(
	DATABASEFILE
	ASSIGNMENTCHAR12
	(
		STRING12
	|	MACRO12	
	)
	(
		AND12
		(
			STRING12
		|	MACRO12	
		)
	)*
)
;

pr_AggregateData:
(
	AGGREGATEDATA
	ASSIGNMENTCHAR12
	(
		TRUE12
	|	FALSE12
	)
)
;

pr_StatisticsFile:
(
	STATISTICSFILE
	ASSIGNMENTCHAR12
	(
		STRING12
	|	MACRO12	
	)
	(
		AND12
		(
			STRING12
		|	MACRO12	
		)
	)*
)
;

pr_DisableStatistics:
(
	DISABLESTATISTICS
	ASSIGNMENTCHAR12
	(
		TRUE12
	|	FALSE12
	)
)
;

pr_StatisticsFilter:
(
	STATISTICSFILTER
	(
		ASSIGNMENTCHAR12
	|	CONCATCHAR12
	)
	pr_StatisticsFilterEntry
	(
		(
			LOGICALOR12
		|	AND12
		)
		pr_StatisticsFilterEntry
	)*
)
;

pr_StatisticsFilterEntry:
(
	NUMBEROFLINES
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
(
	STARTAUTOMATICALLY
	ASSIGNMENTCHAR12
	(
		TRUE12
	|	FALSE12
	)
)
;

pr_NetLineTimes:
(
	NETLINETIMES
	ASSIGNMENTCHAR12
	(
		TRUE12
	|	FALSE12
	)
)
;

pr_NetFunctionTimes:
(
	NETFUNCTIONTIMES
	ASSIGNMENTCHAR12
	(
		TRUE12
	|	FALSE12
	)
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
(
	pt_TestComponentID DOT11
)?
	LOGGERPLUGINS ASSIGNMENTCHAR11 BEGINCHAR11 pr_LoggerPluginEntry
(
	COMMA11 pr_LoggerPluginEntry
)*
ENDCHAR11
;

pr_PlainLoggingParam:
(
	pt_TestComponentID DOT11
)?
(
	STAR11 DOT11
|	pr_Identifier DOT11
)?
(   
  	(FILEMASK ASSIGNMENTCHAR11 pr_LoggingBitMask)
|	(CONSOLEMASK ASSIGNMENTCHAR11 pr_LoggingBitMask)
|	(DISKFULLACTION ASSIGNMENTCHAR11 pr_DiskFullActionValue)
|	(LOGFILENUMBER ASSIGNMENTCHAR11 pr_Number)
|	(LOGFILESIZE ASSIGNMENTCHAR11 pr_Number)
|	(LOGFILENAME ASSIGNMENTCHAR11 pr_LogfileName)
|	((TIMESTAMPFORMAT | CONSOLETIMESTAMPFORMAT) ASSIGNMENTCHAR11 TIMESTAMPVALUE)
|	SOURCEINFOFORMAT ASSIGNMENTCHAR11
	(  
		SOURCEINFOVALUE
	|	pr_YesNoOrBoolean
	)
|	(APPENDFILE ASSIGNMENTCHAR11 pr_YesNoOrBoolean)
|	(LOGEVENTTYPES ASSIGNMENTCHAR11 pr_LogEventTypesValue)
|	(LOGENTITYNAME ASSIGNMENTCHAR11 pr_YesNoOrBoolean)
|	(MATCHINGHINTS ASSIGNMENTCHAR11 pr_MatchingHintsValue)
|	(TTCN3IDENTIFIER11 ASSIGNMENTCHAR11 pr_StringValue)
|   (EMERGENCYLOGGING ASSIGNMENTCHAR11 pr_Number)
|   (EMERGENCYLOGGINGBEHAVIOUR ASSIGNMENTCHAR11 BUFFERALLORBUFFERMASKED)
|   (EMERGENCYLOGGINGMASK ASSIGNMENTCHAR11 pr_LoggingBitMask)
)
;

pr_DiskFullActionValue:
(
	DISKFULLACTIONVALUE
|	DISKFULLACTIONVALUERETRY
|	DISKFULLACTIONVALUERETRY LPAREN11 NUMBER11 RPAREN11	
)
;

pr_LoggerPluginEntry:
pr_Identifier 
(
	ASSIGNMENTCHAR11 pr_StringValue
)?
;

pt_TestComponentID:
(
	pr_Identifier
|	pr_Number
|	MTCKeyword
|	STAR11
)
;

pr_LoggingBitMask:
pr_LoggingMaskElement
(
	LOGICALOR11	pr_LoggingMaskElement
)*
;

pr_LoggingMaskElement:
(
	pr_LogEventType
|	pr_LogEventTypeSet
|	pr_deprecatedEventTypeSet
)
;

pr_LogfileName:
pr_StringValue
;

pr_YesNoOrBoolean:
	YESNO
|	pr_Boolean
;

pr_LogEventTypesValue:
(
	pr_YesNoOrBoolean
|	pr_Detailed
)
;

pr_MatchingHintsValue:
	COMPACT
|	DETAILED
;

pr_LogEventType:
(
	ACTION_UNQUALIFIED | DEBUG_ENCDEC | DEBUG_TESTPORT | DEBUG_UNQUALIFIED | DEFAULTOP_ACTIVATE | DEFAULTOP_DEACTIVATE | DEFAULTOP_EXIT
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
(
	TTCN_EXECUTOR2 | TTCN_ERROR2 | TTCN_WARNING2 | TTCN_PORTEVENT2 | TTCN_TIMEROP2 | TTCN_VERDICTOP2 | TTCN_DEFAULTOP2 | TTCN_ACTION2	
|	TTCN_TESTCASE2 | TTCN_FUNCTION2 | TTCN_USER2 | TTCN_STATISTICS2 | TTCN_PARALLEL2 | TTCN_MATCHING2 | TTCN_DEBUG2 | LOG_ALL | LOG_NOTHING		
)
;
pr_deprecatedEventTypeSet
locals[String str, Token endCol]:
(
	a = TTCN_EXECUTOR1		{$str = $a.getText(); $endCol = $a;} 
|	b = TTCN_ERROR1			{$str = $b.getText(); $endCol = $b;}
|	c = TTCN_WARNING1		{$str = $c.getText(); $endCol = $c;}
|	d = TTCN_PORTEVENT1		{$str = $d.getText(); $endCol = $d;}
|	e = TTCN_TIMEROP1		{$str = $e.getText(); $endCol = $e;}
|	f = TTCN_VERDICTOP1		{$str = $f.getText(); $endCol = $f;}
|	g = TTCN_DEFAULTOP1		{$str = $g.getText(); $endCol = $g;}
|	h = TTCN_ACTION1		{$str = $h.getText(); $endCol = $h;}	
|	i = TTCN_TESTCASE1		{$str = $i.getText(); $endCol = $i;}
|	j = TTCN_FUNCTION1		{$str = $j.getText(); $endCol = $j;}
|	k = TTCN_USER1			{$str = $k.getText(); $endCol = $k;}
|	l = TTCN_STATISTICS1	{$str = $l.getText(); $endCol = $l;}
|	m = TTCN_PARALLEL1		{$str = $m.getText(); $endCol = $m;}
|	n = TTCN_MATCHING1		{$str = $n.getText(); $endCol = $n;}
|	o = TTCN_DEBUG1			{$str = $o.getText(); $endCol = $o;}
)
{
	if($endCol != null){
		reportWarning(new TITANMarker("Deprecated logging option " + $str, $endCol.getLine(),
			$endCol.getStartIndex(), $endCol.getStopIndex(), IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL));
	}
}
;

pr_Detailed:
	DETAILED
|	SUBCATEGORIES
;

pr_ComponentItem:
pr_ComponentName ASSIGNMENTCHAR10 
(
	pr_HostName
|	IPV6_10	
)
;

pr_ComponentName:
(
	pr_Identifier
|	STAR10
)
;

pr_HostName:
(
	pr_DNSName
|	(TTCN3IDENTIFIER1 | TTCN3IDENTIFIER10)
|	(MACRO_HOSTNAME1 | MACRO_HOSTNAME10)
)
;

//TODO: implement ANTLR V4 version of DefineSectionHandler.Definition and add return value
pr_MacroAssignment
@init {
	String name = null;
	String value = null;
}:
(	col = TTCN3IDENTIFIER5 { name = $col.getText(); }
	ASSIGNMENTCHAR5
	endCol = pr_DefinitionRValue { value = $endCol.text; }
)
{	if(name != null && value != null) {
		ArrayList<CfgLocation> locations = new ArrayList<CfgLocation>();
		locations.add(new CfgLocation_V4(actualFile, $col, $col));
		definitions.put(name, new CfgDefinitionInformation(value, locations));
	}
}
;

pr_DefinitionRValue:
(
	pr_SimpleValue 
|	pr_StructuredValue
)
;

pr_SimpleValue:
(
	TTCN3IDENTIFIER5
|	MACRORVALUE5
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
(
   	pr_MacroAssignment 
|	pr_SimpleValue
)?
;

pr_ComponentID:
(
	pr_Identifier
|	pr_Number
|	MTC7
|	SYSTEM7
|	STAR7
)
;

pr_TestportName:
(
	pr_Identifier
	(
		SQUAREOPEN7 pr_IntegerValueExpression SQUARECLOSE7
	)*
|	STAR7
)
;

pr_Identifier:
(
	(MACRO_ID7 | MACRO_ID8 | MACRO_ID9 | MACRO_ID10 | MACRO_ID11)
|	(TTCN3IDENTIFIER7 | TTCN3IDENTIFIER8 | TTCN3IDENTIFIER9 | TTCN3IDENTIFIER10 | TTCN3IDENTIFIER11)
)
;

pr_IntegerValueExpression:
(
	pr_IntegerAddExpression
)
;

pr_IntegerAddExpression:
(
	pr_IntegerMulExpression
	(  
		(  
			(PLUS1 | PLUS7 | PLUS9)
		|	(MINUS1 | MINUS7 | MINUS9)
		)
		pr_IntegerMulExpression
	)*
)
;

pr_IntegerMulExpression:
(
	pr_IntegerUnaryExpression
	(
		(
			(STAR1 | STAR7 | STAR9)
		|	(SLASH1 | SLASH7 | SLASH9) 
		)	
		pr_IntegerUnaryExpression
	)*
)
;

pr_IntegerUnaryExpression:
(
	(PLUS1 | PLUS7 | PLUS9)
|	(MINUS1 | MINUS7 | MINUS9)
)?
	pr_IntegerPrimaryExpression
;

pr_IntegerPrimaryExpression:
(
	pr_Number
|	LPAREN1 pr_IntegerAddExpression RPAREN1
|	LPAREN7 pr_IntegerAddExpression RPAREN7
|	LPAREN9 pr_IntegerAddExpression RPAREN9
)
;

pr_Number:
(
	(NUMBER1 | NUMBER7 | NUMBER9 | NUMBER11)
|	(MACRO_INT1 | MACRO_INT7 | MACRO_INT9 | MACRO_INT11)	
)
;

pr_StringValue:
	pr_CString
	(
		(STRINGOP1 | STRINGOP7 | STRINGOP9 | STRINGOP11) pr_CString
	)*
;

pr_CString:
(  
	(STRING1 | STRING7 | STRING9 | STRING11)
|	(MACRO1 | MACRO7  | MACRO9 | MACRO11)
|	(MACRO_EXP_CSTR1 | MACRO_EXP_CSTR7 | MACRO_EXP_CSTR9 | MACRO_EXP_CSTR11)
)
; 

pr_GroupItem:
(
	pr_Identifier ASSIGNMENTCHAR8
	(
		STAR8
	|	(
			pr_DNSName
		|	pr_Identifier
		)
		(
			COMMA8
			(
				pr_DNSName
			|	pr_Identifier
			)	
		)*
	)
)
;

pr_DNSName:
(
	(NUMBER1 | NUMBER8 | NUMBER10)
|	(FLOAT1 | FLOAT8 | FLOAT10)
|	(DNSNAME1 | DNSNAME8 | DNSNAME10)
)
;

pr_ModuleParam:
(
	pr_ParameterName
	(
		ASSIGNMENTCHAR9 pr_ParameterValue
	|	CONCATCHAR9 pr_ParameterValue
	)
)
;

pr_ParameterName:
(
	(STAR9 DOT9)?
	pr_Identifier (SQUAREOPEN9 NUMBER9 SQUARECLOSE9)?
	(
		DOT9 pr_Identifier (SQUAREOPEN9 NUMBER9 SQUARECLOSE9)?
	)*
)
;

pr_ParameterValue:
pr_SimpleParameterValue (pr_LengthMatch)? (IFPRESENTKeyword9)?
;

pr_LengthMatch:
LENGTHKeyword9 LPAREN9 pr_LengthBound
(
	RPAREN9
|	DOTDOT9
	(
		pr_LengthBound | INFINITYKeyword9
	)
	RPAREN9
)
;

pr_SimpleParameterValue:
(
   pr_ArithmeticValueExpression
|  pr_Boolean
|  pr_ObjIdValue
|  pr_VerdictValue
|  pr_BStringValue
|  pr_HStringValue
|  pr_OStringValue
|  pr_UniversalOrNotStringValue
|  OMITKeyword9
|  pr_EnumeratedValue
|  pr_NULLKeyword 
|  MTCKeyword9
|  SYSTEMKeyword9
|  pr_CompoundValue
|  ANYVALUE9
|  STAR9
|  pr_IntegerRange
|  pr_FloatRange
|  pr_StringRange
|  PATTERNKeyword9 pr_PatternChunkList
|  pr_BStringMatch
|  pr_HStringMatch
|  pr_OStringMatch
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
(
	(
		(PLUS1 | PLUS9)
	|	(MINUS1 | MINUS9)
	)
	pr_ArithmeticMulExpression
)*
;

pr_ArithmeticMulExpression:
pr_ArithmeticUnaryExpression
(
	(
		(STAR1 | STAR9)
	|	(SLASH1 | SLASH9)
	)
	pr_ArithmeticUnaryExpression
)*
;

pr_ArithmeticUnaryExpression:
(
	(PLUS1 | PLUS9)
|	(MINUS1 | MINUS9)
)*
	pr_ArithmeticPrimaryExpression
;

pr_ArithmeticPrimaryExpression:
(
	pr_Float
|	pr_Number
|	LPAREN1 pr_ArithmeticAddExpression RPAREN1
|	LPAREN9 pr_ArithmeticAddExpression RPAREN9
)
;

pr_Float:
(  
	(FLOAT1 | FLOAT9)
|	(MACRO_FLOAT1 | MACRO_FLOAT9)
)
;

pr_Boolean:
(
	(TRUE9 | TRUE11)
|	(FALSE9 | FALSE11)
|	(MACRO_BOOL9 | MACRO_BOOL11)
)
;

pr_ObjIdValue:
OBJIDKeyword9 BEGINCHAR9 (pr_ObjIdComponent)+ ENDCHAR9
;

pr_ObjIdComponent:
(
	pr_Number
|	pr_Identifier LPAREN9 pr_Number RPAREN9	
)
;

pr_VerdictValue:
(  
	NONE_VERDICT9
|	PASS_VERDICT9
|	INCONC_VERDICT9
|	FAIL_VERDICT9
|	ERROR_VERDICT9
)
;

pr_BStringValue:
pr_BString
(
	STRINGOP9 pr_BString
)*
;

pr_BString:
(
	BITSTRING9
|	MACRO_BSTR9
)

;

pr_HStringValue:
pr_HString
(
	STRINGOP9 pr_HString
)*
;

pr_HString:
(
	HEXSTRING9
|	MACRO_HSTR9
)
;
pr_OStringValue:
pr_OString
(
	STRINGOP9 pr_OString
)*
;

pr_OString:
(
	OCTETSTRING9
|	MACRO_OSTR9
|	MACRO_BINARY9
)
;

pr_UniversalOrNotStringValue:
(	
	pr_CString
|	pr_Quadruple
)
(
	STRINGOP9
	(
		pr_CString
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
(
	BEGINCHAR9
    (
		/* empty */
	|	pr_fieldValue
		(
			COMMA9 pr_fieldValue
		)*
	|	pr_arrayItem
		(
			COMMA9 pr_arrayItem
		)*
    |	pr_IndexValue
		(
			COMMA9 pr_IndexValue
		)*	
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

pr_fieldValue:
pr_fieldName ASSIGNMENTCHAR9 pr_ParameterValueOrNotUsedSymbol
;

pr_fieldName:
pr_Identifier
;

pr_ParameterValueOrNotUsedSymbol:
	MINUS9
|	pr_ParameterValue
;

pr_arrayItem:
	pr_ParameterValueOrNotUsedSymbol
|	PERMUTATIONKeyword9 LPAREN9 pr_TemplateItemList RPAREN9
;

pr_TemplateItemList:
pr_ParameterValue 
(
	COMMA9 pr_ParameterValue
)*
;

pr_IndexValue:
SQUAREOPEN9 pr_IntegerValueExpression SQUARECLOSE9 ASSIGNMENTCHAR9 pr_ParameterValue
;
pr_IntegerRange:
LPAREN9
( 
	MINUS9 INFINITYKeyword9 DOTDOT9 (pr_IntegerValueExpression | INFINITYKeyword9)
|	pr_IntegerValueExpression DOTDOT9 (pr_IntegerValueExpression | INFINITYKeyword9)
)
RPAREN9
;

pr_FloatRange:
LPAREN9
( 
	MINUS9 INFINITYKeyword9 DOTDOT9 (pr_FloatValueExpression | INFINITYKeyword9)
|	pr_FloatValueExpression DOTDOT9 (pr_FloatValueExpression | INFINITYKeyword9)
)
RPAREN9
;

pr_FloatValueExpression:
pr_FloatAddExpression
;

pr_FloatAddExpression:
pr_FloatMulExpression
(  
	(
		PLUS9
	|	MINUS9
	)
	pr_FloatMulExpression
)*
;

pr_FloatMulExpression:
pr_FloatUnaryExpression
(
	(
		STAR9
	|	SLASH9
	)
	pr_FloatUnaryExpression
)*
;

pr_FloatUnaryExpression:
(
	PLUS9
|	MINUS9
)*
	pr_FloatPrimaryExpression
;

pr_FloatPrimaryExpression:
(
	pr_Float
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
