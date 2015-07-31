
set antlr=java -classpath "C:\lib\antlr.jar" antlr.Tool
set current_path=%~dp0
set workspace_path=%current_path%


cd %workspace_path%\com.ericsson.titan.common\src\com\ericsson\titan\common\parsers\cfg\
%antlr% cfgBaseLexer.g
%antlr% cfgParser.g
%antlr% cfgResolver.g

REM derived grammars
%antlr% -glib cfgBaseLexer.g cfgComponentsSectionLexer.g
%antlr% -glib cfgBaseLexer.g cfgDefineSectionLexer.g
%antlr% -glib cfgBaseLexer.g cfgExecuteSectionLexer.g
%antlr% -glib cfgBaseLexer.g cfgExternalCommandsSectionLexer.g
%antlr% -glib cfgBaseLexer.g cfgGroupsSectionLexer.g
%antlr% -glib cfgBaseLexer.g cfgIncludeSectionLexer.g
%antlr% -glib cfgBaseLexer.g cfgLoggingSectionLexer.g
%antlr% -glib cfgBaseLexer.g cfgMainControllerSectionLexer.g
%antlr% -glib cfgBaseLexer.g cfgModuleParametersSectionLexer.g
%antlr% -glib cfgBaseLexer.g cfgTestportParametersSectionLexer.g

REM Titan Designer
REM ASN1
cd %workspace_path%\com.ericsson.titan.designer\src\com\ericsson\titan\designer\parsers\ASN1parser\
%antlr% ASN1Lexer.g
%antlr% ASN1Parser.g
%antlr% -glib ASN1Parser.g ASN1SpecialParser.g

REM TTCN3
cd %workspace_path%\com.ericsson.titan.designer\src\com\ericsson\titan\designer\parsers\TTCN3parser\
%antlr% PreprocessorDirectiveLexer.g
%antlr% PreprocessorDirectiveParser.g
%antlr% TTCN3CharstringLexer.g
%antlr% TTCN3Lexer.g
%antlr% TTCN3Parser.g

%antlr% -glib TTCN3Parser.g TTCN3Reparser.g
%antlr% -glib TTCN3Lexer.g TTCN3KeywordLessLexer.g

REM Extension attribute
cd %workspace_path%\com.ericsson.titan.designer\src\com\ericsson\titan\designer\parsers\ExtensionAttributeParser\
%antlr% ExtensionAttributeLexer.g
%antlr% ExtensionAttributeParser.g
cd %current_path%


