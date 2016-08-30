parser grammar Ttcn3Parser;

/*
 ******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************
*/

/*
NOTE:

 pr_(Bit|Hex|Octet)String(Match?) rules are for [BHO]STRING(MATCH)? to remove
 the beginning "'" and ending "'[BHO]" strings,
 as lexer cannot remove them as it was done in the ANTLR V2 Parser.
 Do NOT use [BHO]STRING(MATCH)? in any rule, use these instead.

 Rule pr_CString is for CSTRING to remove the beginning and ending '"' characters,
 as lexer cannot remove them as it was done in the ANTLR V2 Parser.
 Do NOT use CSTRING in any rule, use this instead.

-------------------------- 
Precedence of Operators with precedence levels (higher number is higher precedence)

15 ( ... )
14 +, - (unary)
13 *, /, mod, rem
12 +, -, &
11 not4b (unary)
10 and4b
 9 xor4b
 8 or4b
 7 <<, >>, <@, @>
 6 <, >, <=, >=
 5 ==, !=
 4 not (unary)
 3 and
 2 xor
 1 or

Simplified* expression rules using precedence climbing method:

E1 : E2 ( OR E2 )*
E2 : E3 ( XOR E3 )*
E3 : E4 ( AND E4 )*
E4 : NOT E4 | E5
E5 : E6 ( ( '==' | '!=' ) E6 )*
E6 : E7 ( ( '<' | '>' | '<=' | '>=' ) E7 )*
E7 : E8 ( ( '<<' | '>>' | '<@' | '@>' ) E8 )*
E8 : E9 ( OR4B E9 )*
E9 : E10 ( XOR4B E10 )*
E10: E11 ( AND4B E11 )*
E11: NOT4B E11 | E12
E12: E13 ( ( '+' | '-' | '&' ) E13 )*
E13: P ( ( '*' | '/' | MOD | REM ) P )*
P : NOT E4 | NOT4B E11 | ( '+' | '-' ) P | '(' E1 ')' | v

*: without java code, but the structure is identical to the used rules, where
  En: rule for the n-th precedence level
  E1: pr_SingleExpression in case of Ttcn3Parser.g4
  P: last rule with unary operations
  v: atomic expression without any operators, pr_Primary in case of Ttcn3Parser.g4

NOTE: unary operators, which are not on the highest priority level, are used twice, because they must be handled at their precendence level,
      and also at the last (highest precedence) level, if there is no match to other operators.
      If any of these unary operators was matched in the last rule, control must jump back to the rule of its precedence level ( NOT E4, NOT4B E11 ).
      Example case:
        not4b a & not4b b  <=>  not4b ( a & ( not4b b ) )
        1st not4b matches at rule E4, but the 2nd not4b matches at rule P.
*/

options {
tokenVocab=Ttcn3Lexer;
}

@header
{
import java.util.List;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;

import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.common.parsers.TitanListener;

import org.eclipse.titan.designer.AST.*;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.ASN1.values.ASN1_Null_Value;
import org.eclipse.titan.designer.AST.TTCN3.*;
import org.eclipse.titan.designer.AST.TTCN3.attributes.*;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErroneousAttributeSpecification.Indicator_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.*;
import org.eclipse.titan.designer.AST.TTCN3.statements.*;
import org.eclipse.titan.designer.AST.TTCN3.types.*;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody.OperationModes;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.*;
import org.eclipse.titan.designer.AST.TTCN3.templates.*;
import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString.PatternType;
import org.eclipse.titan.designer.AST.TTCN3.values.*;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.*;

}

@members
{

private IFile actualFile = null;
private int line = 1;
private int offset = 0;

@SuppressWarnings("unused")

/**
 * List of all the exceptions which are thrown by the rules
 */
private List<RecognitionException> exceptions = new ArrayList<RecognitionException>();

/**
 * Handles an exception which is thrown by a rule.
 * Search for "catch [RecognitionException ex]" to see where it is used.
 * @param ex new exception to handle
 */
public void reportError(RecognitionException ex) {
	exceptions.add(ex);
}

private List<TITANMarker> warnings = new ArrayList<TITANMarker>();

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
		(aStartToken != null) ? line - 1 + aStartToken.getLine() : -1,
		(aStartToken != null) ? offset + aStartToken.getStartIndex() : -1,
		(aEndToken != null) ? offset + aEndToken.getStopIndex() + 1 : -1,
		aSeverity, aPriority );
	return marker;
}

/**
 * Adds a warning marker.
 * Locations of input tokens are not moved by offset and line yet, this function does this conversion.
 * @param aMessage marker message
 * @param aStartToken the 1st token, its line and start position will be used for the location
 *                  NOTE: start position is the column index of the tokens 1st character.
 *                        Column index starts with 0.
 * @param aEndToken the last token, its end position will be used for the location.
 *                  NOTE: end position is the column index after the token's last character.
 */
public void reportWarning( final String aMessage, final Token aStartToken, final Token aEndToken ) {
	TITANMarker marker = createMarker( aMessage, aStartToken, aEndToken, IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL );
	warnings.add(marker);
}

/**
 * Adds a warning marker, where the start and end token is the same.
 * Locations of input tokens are not moved by offset and line yet, this function does this conversion.
 * @param aMessage marker message
 * @param aToken the start and end token
 */
public void reportWarning( final String aMessage, final Token aToken ) {
	reportWarning( aMessage, aToken, aToken );
}

public List<TITANMarker> getWarnings() {
	return warnings;
}

private ArrayList<TITANMarker> unsupportedConstructs = new ArrayList<TITANMarker>();

/**
 * Adds a marker for configurable unsupported constructs.
 * Locations of input tokens are not moved by offset and line yet, this function does this conversion.
 * @param aMessage marker message
 * @param aStartToken the 1st token, its line and start position will be used for the location
 *                  NOTE: start position is the column index of the tokens 1st character.
 *                        Column index starts with 0.
 * @param aEndToken the last token, its end position will be used for the location.
 *                  NOTE: end position is the column index after the token's last character.
 */
public void reportUnsupportedConstruct( final String aMessage, final Token aStartToken, final Token aEndToken ) {
	TITANMarker marker = createMarker( aMessage, aStartToken, aEndToken, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL );
	unsupportedConstructs.add(marker);
}

public ArrayList<TITANMarker> getUnsupportedConstructs() {
	return unsupportedConstructs;
}

/**
 * Adds an error marker for unsupported constructs.
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
	warnings.add(marker);
}

public void setActualFile(IFile file) {
	actualFile = file;
}

public void setLine(final int line) {
	this.line = line;
}

public void setOffset(final int offset) {
	this.offset = offset;
}

private IProject project = null;

public void setProject(IProject project) {
	this.project = project;
}

private TTCN3Module act_ttcn3_module;

public TTCN3Module getModule() {
	return act_ttcn3_module;
}

/**
 * Interface for lexer to get and clear last comment location
 * It was introduced with ANTLR 4, but it is ANTLR 4 independent
 */
private interface ILexer {
	public Location getLastCommentLocation();
	public void clearLastCommentLocation();
}

private ILexer lexer = null;

public void setLexer(final Ttcn3Lexer aLexer) {
	this.lexer = new ILexer() {
		@Override
		public Location getLastCommentLocation() {
			return aLexer.getLastCommentLocation();
		}
		@Override
		public void clearLastCommentLocation() {
			aLexer.clearLastCommentLocation();
		}
	};
}

public void setLexer(final Ttcn3KeywordlessLexer aLexer) {
	this.lexer = new ILexer() {
		@Override
		public Location getLastCommentLocation() {
			return aLexer.getLastCommentLocation();
		}
		@Override
		public void clearLastCommentLocation() {
			aLexer.clearLastCommentLocation();
		}
	};
}

public void reset() {
	super.reset();
	if(exceptions != null && warnings != null && unsupportedConstructs != null) {
		exceptions.clear();
		warnings.clear();
		unsupportedConstructs.clear();
	}
}


/** Used to preload the class, also loading the TTCN-3 parser. */
public static void preLoad() {
}

/**
 * Gets the last token of the current rule.
 * This is used inside the rule, because \$stop is filled only
 * in the finally block in the generated java code, so it does
 * NOT have the correct value in @after and @finally actions.
 * This method can be used in any part of the rule.
 * @return last consumed token
 */
public Token getStopToken() {
	return _input.get( _input.index() - 1 );
}

/**
 * Create new location, which modified by the parser offset and line,
 * where the start and end token is the same
 * @param aToken the start and end token
 */
private Location getLocation(final Token aToken) {
	return getLocation(aToken, aToken);
}

/**
 * Create new location, which modified by the parser offset and line
 * @param aStartToken the 1st token, its line and start position will be used for the location
 *                  NOTE: start position is the column index of the tokens 1st character.
 *                        Column index starts with 0.
 * @param aEndToken the last token, its end position will be used for the location.
 *                  NOTE: end position is the column index after the token's last character.
 */
private Location getLocation(final Token aStartToken, final Token aEndToken) {
	final Token endToken = (aEndToken != null) ? aEndToken : aStartToken; 
	return new Location(actualFile, line - 1 + aStartToken.getLine(), offset + aStartToken.getStartIndex(), offset + endToken.getStopIndex() + 1);
}

/**
 * Create new large location, which modified by the parser offset and line
 * @param aStartToken the 1st token, its line and start position will be used for the location
 *                  NOTE: start position is the column index of the tokens 1st character.
 *                        Column index starts with 0.
 * @param aEndToken the last token, its end position will be used for the location.
 *                  NOTE: end position is the column index after the token's last character.
 */
private LargeLocation getLargeLocation(final Token aStartToken, final Token aEndToken) {
	final Token endToken = (aEndToken != null) ? aEndToken : aStartToken; 
	return new LargeLocation(actualFile, line - 1 + aStartToken.getLine(), line - 1 + endToken.getLine(), offset + aStartToken.getStartIndex(), offset + endToken.getStopIndex() + 1);
}

/**
 * Create new location, which modified by the parser offset and line 
 * @param aBaseLocation location of the start token, location is already modified by offset and line
 * @param aEndToken end token, NOT null, not modified by offset and line yet
 */
private Location getLocation(final Location aBaseLocation, final Token aEndToken) {
	return new Location(actualFile, aBaseLocation.getLine(), aBaseLocation.getOffset(), offset + aEndToken.getStopIndex() + 1);
}
	
/**
 * @return list of errors, collected by the TitanListener
 *         or null if there is no TitanListener (this case should NOT happen)
 */
public List<SyntacticErrorStorage> getErrors() {
	TitanListener titanListener = null;
	for ( ParseTreeListener listener : this.getParseListeners() ) {
		// there should be only 1 listener, which is a TitanListener, but let's make it safe
		if ( listener instanceof TitanListener ) {
			titanListener = (TitanListener)listener;
			return titanListener.getErrorsStored();
		}
	}
	return null;
}

/**
 * @return true if error list is empty
 */
public boolean isErrorListEmpty() {
	for ( ParseTreeListener listener : this.getParseListeners() ) {
		if(listener instanceof TitanListener && !((TitanListener) listener).getErrorsStored().isEmpty()) {
			return false;
		}
	}
	for(ANTLRErrorListener listener : getErrorListeners()) {
		if(listener instanceof TitanListener && !((TitanListener) listener).getErrorsStored().isEmpty()) {
			return false;
		}
	}
	return true;
}
}

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */

pr_TTCN3File:
	pr_TTCN3Module?
	EOF
;

//-----------------------------------------------------
// erroneous attribute specification syntax for negative testing
//-----------------------------------------------------

pr_ErroneousAttributeSpec returns [ErroneousAttributeSpecification errAttrSpec]
@init {
	$errAttrSpec = null;
	boolean isRaw = false;
	boolean hasAllKeyword = false;
}:
(	indicator = pr_ErroneousIndicator
	(	pr_LParen
		raw_id = pr_Identifier
//TODO: create token
		{	if ( $raw_id.identifier != null && "raw".equals( $raw_id.identifier.getName() ) ) {
				isRaw = true;
			} else {
				reportUnsupportedConstruct( "Invalid keyword, only the optional `raw' keyword can be used here.", $raw_id.start, $raw_id.stop );
			}
		}
		pr_RParen
	)?
	pr_AssignmentChar
	t = pr_TemplateInstance
	( pr_AllKeyword { hasAllKeyword = true; } )?
	z = EOF // end of string
)
{
	$errAttrSpec = new ErroneousAttributeSpecification($indicator.indicator, isRaw, $t.templateInstance, hasAllKeyword);
	$errAttrSpec.setLocation(getLocation( $indicator.start, $z));
};

pr_ErroneousIndicator returns [Indicator_Type indicator]
@init {
	$indicator = null;
}:
(	pr_ValueKeyword { $indicator = Indicator_Type.Value_Indicator; }
|	id = pr_Identifier
		{
//TODO: create token
			if ( $id.identifier !=null && "before".equals( $id.identifier.getName() ) ) {
				$indicator = Indicator_Type.Before_Indicator;
//TODO: create token
			} else if ( $id.identifier!=null && "after".equals( $id.identifier.getName() ) ) {
				$indicator = Indicator_Type.After_Indicator;
			} else {
				$indicator = Indicator_Type.Invalid_Indicator;
				reportUnsupportedConstruct( "Invalid indicator. Valid indicators are: `before', `value' and `after'", $id.start, $id.stop );
			}
		}
);

//-----------------------------------------------------
//                    TTCN Module  1.6.0
//-----------------------------------------------------

pr_TTCN3Module
@init {
	Token col = null;
	Token endcol = null;
	Token definitionsEnd = null;
	ControlPart controlpart = null;
	MultipleWithAttributes attributes = null;
	Location commentLocation = lexer.getLastCommentLocation();
	lexer.clearLastCommentLocation();
}:
(	m = pr_TTCN3ModuleKeyword	{ col = $m.start; }
	i = pr_TTCN3ModuleId		{ act_ttcn3_module = new TTCN3Module( $i.identifier, project ); }
	begin = pr_BeginChar
	pr_ModuleDefinitionsList[null]
	( c = pr_ModuleControlPart	{ definitionsEnd = $c.stop; controlpart = $c.controlpart; } )?
	enda = pr_EndChar			{ endcol = $enda.stop; if ( definitionsEnd == null ) { definitionsEnd = $enda.stop; } }
	( a = pr_WithStatement		{ endcol = $a.stop; attributes = $a.attributes; } )?
	( endb = pr_SemiColon		{ endcol = $endb.stop; } )?
)
{
	act_ttcn3_module.setLocation( getLargeLocation( col, endcol ) );
	act_ttcn3_module.setDefinitionsLocation( getLocation( $begin.start, definitionsEnd ) );
	act_ttcn3_module.setWithAttributes( attributes );
	act_ttcn3_module.setCommentLocation( commentLocation );
	if ( controlpart != null ) {
		act_ttcn3_module.addControlpart( controlpart );
		controlpart.setAttributeParentPath( act_ttcn3_module.getAttributePath() );
	}
};

pr_TTCN3ModuleKeyword returns[String stringValue]:
	MODULE
{
	$stringValue = $MODULE.getText();
};

// ASN.1 with TTCN-3 BNF Extension Start

pr_ObjectIdentifierKeyword:
	OBJECTIDENTIFIERKEYWORD
;

pr_ObjectIdentifierValue returns [ObjectIdentifier_Value value]:
	OBJECTIDENTIFIERKEYWORD
	pr_BeginChar
	v = pr_ObjIdComponentList
	endcol = pr_EndChar
{
	$value = $v.value;
	$value.setLocation(getLocation( $OBJECTIDENTIFIERKEYWORD, $endcol.stop));
};

pr_ObjIdComponentList returns [ObjectIdentifier_Value value]
@init {
	$value = new ObjectIdentifier_Value();
}:
(	o = pr_ObjIdComponent	{if($o.objidComponent != null) { $value.addObjectIdComponent($o.objidComponent); } }
)+
;

pr_ObjIdComponent returns [ObjectIdentifierComponent objidComponent]
@init {
	$objidComponent = null;
}:
(	i = pr_Identifier
		{	if ($i.identifier != null) {
				$objidComponent = new ObjectIdentifierComponent($i.identifier, null);
				$objidComponent.setLocation(getLocation( $i.start, $i.stop));
			}
		}
|	v = pr_ReferencedValue
		{	if ($v.value != null) {
				$objidComponent = new ObjectIdentifierComponent($v.value);
				$objidComponent.setLocation(getLocation( $v.start, $v.stop));
			}
		}
|	o = pr_NumberForm { $objidComponent = $o.objidComponent;}
|	o2 = pr_NameAndNumberForm { $objidComponent = $o2.objidComponent;}
);

pr_NumberForm returns [ObjectIdentifierComponent objidComponent]
@init {
	$objidComponent = null;
}:
	NUMBER
{
	Value value = new Integer_Value($NUMBER.getText());
	value.setLocation(getLocation( $NUMBER));
	$objidComponent = new ObjectIdentifierComponent(null, value);
	$objidComponent.setLocation(getLocation( $NUMBER));
};

pr_NameAndNumberForm returns [ObjectIdentifierComponent objidComponent]
@init {
	$objidComponent = null;
}:
	i = pr_Identifier
	LPAREN
	NUMBER
	RPAREN
{
	Value value = new Integer_Value($NUMBER.getText());
	value.setLocation(getLocation( $NUMBER));
	$objidComponent = new ObjectIdentifierComponent($i.identifier, value);
	$objidComponent.setLocation(getLocation( $i.start, $RPAREN));
};

// ASN.1 with TTCN-3 BNF Extension End

pr_TTCN3ModuleId returns[Identifier identifier]
@init {
	$identifier = null;
}:
(	i = pr_OwnGlobalModuleId
	pr_LanguageSpec?
)
{
	$identifier = $i.identifier;
};

pr_OwnGlobalModuleId returns [Identifier identifier]
@init {
	$identifier = null;
}:
(	id = pr_Identifier
	(	DOT
		pr_ObjectIdentifierValue
	)?
)
{
	$identifier = $id.identifier;
};


//------------------------------------------------------
//          Module Definitions Part      1.6.1  136
//------------------------------------------------------

pr_ModuleDefinitionsList [Group parent_group]:
(	(	col = pr_ModuleDefinition[parent_group]
		pr_SemiColon?
	)*
);

pr_ModuleDefinition [Group parent_group]:
(	d = pr_ModuleDef
		{
			List<Definition> defs = $d.definitions;
			act_ttcn3_module.addDefinitions(defs);
			if(parent_group == null) {
				for(Definition def : defs) {
					def.setAttributeParentPath(act_ttcn3_module.getAttributePath());
				}
			} else {
				parent_group.addDefinitions(defs);
				for(Definition def : defs) {
					def.setAttributeParentPath(parent_group.getAttributePath());
				}
			}
		}
|	pr_ImportDef[parent_group]
|	pr_GroupDef[parent_group]
|	pr_FriendModuleDef[parent_group]
);

pr_ModuleDef returns [List<Definition> definitions]
@init {
	$definitions = new ArrayList<Definition>();
	VisibilityModifier modifier = null;
	MultipleWithAttributes attributes = null;
	Location commentLocation = lexer.getLastCommentLocation();
	if (commentLocation != null) {
			lexer.clearLastCommentLocation();
	}
}:
(	(	m = pr_Visibility { modifier = $m.modifier; }	)?
	(	d1 = pr_TypeDef { if($d1.def_type != null) { $definitions.add($d1.def_type); } }
	|	d2 = pr_ConstDef { if($d2.array != null) { $definitions.addAll($d2.array); } }
	|	d3 = pr_TemplateDef { if($d3.def_template != null) { $definitions.add($d3.def_template); } }
	|	d4 = pr_FunctionDef { if($d4.def_func != null) { $definitions.add($d4.def_func); } }
	|	d5 = pr_SignatureDef { if($d5.def_type != null) { $definitions.add($d5.def_type); } }
	|	d6 = pr_TestcaseDef { if($d6.def_testcase != null) { $definitions.add($d6.def_testcase); } }
	|	d7 = pr_AltstepDef { if($d7.def_altstep != null) { $definitions.add($d7.def_altstep); } }
	|	d8 = pr_ExtFunctionDef { if($d8.def_extfunction != null) { $definitions.add($d8.def_extfunction); } }
	|	d9 = pr_ExtConstDef { $definitions.addAll($d9.definitions); }
	|	d10 = pr_ModuleParDef { if($d10.parameters != null) { $definitions.addAll($d10.parameters); } }
	)
	(	a = pr_WithStatement { attributes = $a.attributes; }	)?
)
{
	for ( int i = 0; i < $definitions.size(); i++ ) {
		Definition definition = $definitions.get(i);
		if ( modifier != null ) {
			definition.setVisibility( modifier );
			Location loc = definition.getLocation();
			loc.setOffset( offset + $start.getStartIndex() );
		}
		if( attributes != null ) {
			definition.setWithAttributes( attributes );
			Location loc = definition.getLocation();
			loc.setEndOffset(offset + getStopToken().getStopIndex() + 1);
		}
		definition.setCumulativeDefinitionLocation(getLocation( $start, getStopToken()));
	}
	if (commentLocation == null) {
		commentLocation = lexer.getLastCommentLocation();
	}

	for ( Definition definition : $definitions ) {
		definition.setCommentLocation(commentLocation);
	}
};

//------------------------------------------------------
//   Typdef Definitions  1.6.1.1  136
// -----------------------------------------------------

pr_TypeDef returns[Def_Type def_type]
@init {
	$def_type = null;
}:
(	pr_TypeDefKeyword
	(	d1 = pr_StructuredTypeDef { $def_type = $d1.def_type; }
	|	d2 = pr_SubTypeDef { $def_type = $d2.def_type; }
	)
)
{	if ( $def_type != null ) {
		$def_type.setLocation(getLocation( $start, getStopToken() ));
	}
};

pr_TypeDefKeyword:
	TYPE
;

pr_StructuredTypeDef returns[Def_Type def_type]
@init {
	$def_type = null;
}:
(	t1 = pr_RecordOfDef { $def_type = $t1.def_type; }
|	t2 = pr_RecordDef { $def_type = $t2.def_type; }
|	t3 = pr_UnionDef { $def_type = $t3.def_type; }
|	t4 = pr_SetOfDef { $def_type = $t4.def_type; }
|	t5 = pr_SetDef { $def_type = $t5.def_type; }
|	t6 = pr_EnumDef { $def_type = $t6.def_type; }
|	t7 = pr_PortDef { $def_type = $t7.def_type; }
|	t8 = pr_ComponentDef { $def_type = $t8.def_type; }
|	t9 = pr_FunctionTypeDef { $def_type = $t9.def_type; }
|	t10 = pr_AltstepTypeDef { $def_type = $t10.def_type; }
|	t11 = pr_TestcaseTypeDef { $def_type = $t11.def_type; }
);

pr_RecordDef returns[Def_Type def_type]
@init {
	$def_type = null;
	CompFieldMap compFieldMap = new CompFieldMap();
}:
(	col = pr_RecordKeyword
	i = pr_StructDefBody[compFieldMap]
)
{
	if($i.identifier != null) {
		TTCN3_Sequence_Type type = new TTCN3_Sequence_Type(compFieldMap);
		type.setLocation(getLocation( $col.start, $i.stop));
		$def_type = new Def_Type($i.identifier, type);
	}
	lexer.clearLastCommentLocation();
};

pr_RecordKeyword:
	RECORD
;

pr_StructDefBody[CompFieldMap compFieldMap] returns[Identifier identifier]
@init {
	$identifier = null;
	lexer.clearLastCommentLocation();
}:
(	(	i = pr_Identifier { $identifier = $i.identifier; }
		pr_StructDefFormalParList?
	|	ADDRESS  { $identifier = new Identifier(Identifier_type.ID_TTCN, "ADDRESS", getLocation( $ADDRESS)); }
	)
	begin = pr_BeginChar
	(	c = pr_StructFieldDef { compFieldMap.addComp($c.compField); }
		(	pr_Comma
			c = pr_StructFieldDef { compFieldMap.addComp($c.compField); }
		)*
	)?
	endcol = pr_EndChar
)
{
	if( $compFieldMap != null ) { $compFieldMap.setLocation(getLocation( $begin.start, $endcol.stop)); }
};

pr_StructDefFormalParList:
(	col = pr_LParen
	pr_StructDefFormalPar
	(	pr_Comma
		pr_StructDefFormalPar
	)*
	endcol = pr_RParen
)
{
	reportUnsupportedConstruct( "Type parameterization is not currently supported", $col.start, $endcol.stop );
};

pr_StructDefFormalPar:
	pr_FormalValuePar
;

pr_StructFieldDef returns[CompField compField]
@init {
	$compField = null;
	Type type = null;
	ArrayDimensions dimensions = null;
	List<ParsedSubType> parsedSubTypes = null;
	boolean optional = false;
}:
(	(	t = pr_Type { type = $t.type; }
	|	t2 = pr_NestedTypeDef { type = $t2.type; }
	)
	i = pr_Identifier
	( d = pr_ArrayDef { dimensions = $d.dimensions; } )?
	( p = pr_SubTypeSpec { parsedSubTypes = $p.parsedSubTypes; } )?
	( pr_OptionalKeyword { optional = true; } )?
)
{
	if($i.identifier != null && type != null) {
		if(parsedSubTypes != null) {
			IType t = type;
			boolean seof = true;
			while(seof) {
				seof = t instanceof AbstractOfType;
				if (seof) {
					t = ((AbstractOfType) t).getOfType();
				}
			}
			t.setParsedRestrictions(parsedSubTypes);
		}
		if( dimensions != null ) {
			for ( int i = dimensions.size() - 1; i >= 0; i-- ) {
				type = new Array_Type(type, dimensions.get(i), true);
				type.setLocation(getLocation( $d.start, $d.stop));
			}
		}
		$compField = new CompField($i.identifier, type, optional, null);
		$compField.setLocation(getLocation( $start, getStopToken()));
		Location commentLocation = lexer.getLastCommentLocation();
		$compField.setCommentLocation(commentLocation);
		lexer.clearLastCommentLocation();

	}
	lexer.clearLastCommentLocation();
};

pr_NestedTypeDef returns[Type type]
@init {
	$type = null;
}:
(	t1 = pr_NestedRecordDef			{ $type = $t1.type; }
|	t2 = pr_NestedUnionDef			{ $type = $t2.type; }
|	t3 = pr_NestedSetDef			{ $type = $t3.type; }
|	t4 = pr_NestedRecordOfDef		{ $type = $t4.type; }
|	t5 = pr_NestedSetOfDef			{ $type = $t5.type; }
|	t6 = pr_NestedEnumDef			{ $type = $t6.type; }
|	t7 = pr_NestedFunctionTypeDef	{ $type = $t7.type; }
|	t8 = pr_NestedAltstepTypeDef	{ $type = $t8.type; }
|	t9 = pr_NestedTestcaseTypeDef	{ $type = $t9.type; }
);

pr_NestedRecordDef returns[Type type]
@init {
	$type = null;
	CompFieldMap compFieldMap = new CompFieldMap();
}:
(	col = pr_RecordKeyword
	begin = pr_BeginChar
	(	c = pr_StructFieldDef { compFieldMap.addComp($c.compField); }
		(	pr_Comma
			c = pr_StructFieldDef { compFieldMap.addComp($c.compField); }
		)*
	)?
	endcol = pr_EndChar
)
{
	compFieldMap.setLocation(getLocation( $begin.start, $endcol.stop));
	$type = new TTCN3_Sequence_Type(compFieldMap);
	$type.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_NestedUnionDef returns[Type type]
@init {
	$type = null;
	CompFieldMap compFieldMap = new CompFieldMap();
}:
(	col = pr_UnionKeyword
	begin = pr_BeginChar
	(	c = pr_UnionFieldDef { compFieldMap.addComp($c.compField); }
		(	pr_Comma
			c = pr_UnionFieldDef { compFieldMap.addComp($c.compField); }
		)*
	)
	endcol = pr_EndChar
)
{
	compFieldMap.setLocation(getLocation( $begin.start, $endcol.stop));
	$type = new TTCN3_Choice_Type(compFieldMap);
	$type.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_NestedSetDef returns[Type type]
@init {
	$type = null;
	CompFieldMap compFieldMap = new CompFieldMap();
}:
(	col = pr_SetKeyword
	begin = pr_BeginChar
	(	c = pr_StructFieldDef { compFieldMap.addComp($c.compField); }
		(	pr_Comma
			c = pr_StructFieldDef { compFieldMap.addComp($c.compField); }
		)*
	)?
	endcol = pr_EndChar
)
{
	compFieldMap.setLocation(getLocation( $begin.start, $endcol.stop));
	$type = new TTCN3_Set_Type(compFieldMap);
	$type.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_NestedRecordOfDef returns[SequenceOf_Type type]
@init {
	$type = null;
	LengthRestriction restriction = null;
	Type ofType = null;
}:
(	col = pr_RecordKeyword
	( r = pr_StringLength { restriction = $r.restriction; } )?
	pr_OfKeyword
	(	t1 = pr_Type { ofType = $t1.type; }
	|	t2 = pr_NestedTypeDef { ofType = $t2.type; }
	)
)
{
	$type = new SequenceOf_Type(ofType);
	if(restriction != null) {
		List<ParsedSubType> parsedSubTypes = new ArrayList<ParsedSubType>();
		parsedSubTypes.add(new Length_ParsedSubType(restriction));
		$type.setParsedRestrictions(parsedSubTypes);
	}
	$type.setLocation(getLocation( $col.start, getStopToken()));
};

pr_NestedSetOfDef returns[SetOf_Type type]
@init {
	$type = null;
	LengthRestriction restriction = null;
	Type ofType = null;
}:
(	col = pr_SetKeyword
	( r = pr_StringLength { restriction = $r.restriction; } )?
	pr_OfKeyword
	(	t1 = pr_Type { ofType = $t1.type; }
	|	t2 = pr_NestedTypeDef { ofType = $t2.type; }
	)
)
{
	$type = new SetOf_Type(ofType);
	if(restriction != null) {
		List<ParsedSubType> parsedSubTypes = new ArrayList<ParsedSubType>();
		parsedSubTypes.add(new Length_ParsedSubType(restriction));
		$type.setParsedRestrictions(parsedSubTypes);
	}
	$type.setLocation(getLocation( $col.start, getStopToken()));
};

pr_NestedEnumDef returns[Type type]
@init {
	$type = null;
	EnumerationItems items = null;
}:
(	col = pr_EnumKeyword
	pr_BeginChar
	i = pr_EnumerationList { items = $i.items; }
	endcol = pr_EndChar
)
{
	if(items != null) { items.setLocation(getLocation( $col.start, $endcol.stop)); }
	$type = new TTCN3_Enumerated_Type(items);
	$type.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_NestedFunctionTypeDef returns[Type type]
@init {
	$type = null;
	FormalParameterList parList = null;
	ReturnType_Helper helper = null;
	Type returnType = null;
	boolean returnsTemplate = false;
	TemplateRestriction.Restriction_type templateRestriction = TemplateRestriction.Restriction_type.TR_NONE;
	Configuration_Helper confighelper = new Configuration_Helper();
}:
(	col = pr_FunctionKeyword
	start1 = pr_LParen
	( p = pr_FunctionFormalParList { parList = $p.parList; } )?
	end1 = pr_RParen
	(	end2 = pr_RunsOnReferenceOrSelf[confighelper]	)?
	(	h = pr_ReturnType
			{	helper = $h.helper;
				if( helper != null ) {
					returnType = helper.type;
					returnsTemplate = helper.returnsTemplate;
					templateRestriction = helper.templateRestriction;
				}
			}
	)?
)
{
	if(parList == null) { parList = new FormalParameterList(new ArrayList<FormalParameter>()); }
	parList.setLocation(getLocation( $start1.start, $end1.stop));
	$type = new Function_Type(parList, confighelper.runsonReference, confighelper.runsOnSelf, returnType, returnsTemplate, templateRestriction);
	$type.setLocation(getLocation( $start, getStopToken()));
};

pr_NestedAltstepTypeDef returns[Type type]
@init {
	$type = null;
	FormalParameterList parList = null;
	Configuration_Helper confighelper =  new Configuration_Helper();
}:
(	col = pr_AltstepKeyword
	start1 = pr_LParen
	( p = pr_FunctionFormalParList { parList = $p.parList; } )?
	end1 = pr_RParen
	(	end2 = pr_RunsOnReferenceOrSelf[confighelper]	)?
)
{
	if(parList == null) { parList = new FormalParameterList(new ArrayList<FormalParameter>()); }
	parList.setLocation(getLocation( $start1.start, $end1.stop));
	$type = new Altstep_Type(parList, confighelper.runsonReference, confighelper.runsOnSelf);
	$type.setLocation(getLocation( $start, getStopToken()));
};

pr_NestedTestcaseTypeDef returns[Type type]
@init {
	$type = null;
	FormalParameterList parList = null;
	Configuration_Helper helper = null;
	Reference runsonReference = null;
	Reference systemReference = null;
}:
	col = pr_TestcaseKeyword
	start1 = pr_LParen
	( p = pr_TestcaseFormalParList { parList = $p.parList; } )?
	end = pr_RParen
	h = pr_ConfigSpec
		{	helper = $h.helper;
			if(helper != null) {
				runsonReference = helper.runsonReference;
				systemReference = helper.systemReference;
			}
		}
{
	if(parList == null) { parList = new TestcaseFormalParameterList(new ArrayList<FormalParameter>()); }
	parList.setLocation(getLocation( $start1.start, $end.stop));
	$type = new Testcase_Type(parList, runsonReference, systemReference);
	$type.setLocation(getLocation( $col.start, $h.stop));
};

pr_OptionalKeyword:
	OPTIONAL
;

pr_UnionDef returns[Def_Type def_type]
@init {
	$def_type = null;
	CompFieldMap compFieldMap = new CompFieldMap();
}:
(	col = pr_UnionKeyword
	i = pr_UnionDefBody[compFieldMap]
)
{
	if($i.identifier != null) {
		TTCN3_Choice_Type type = new TTCN3_Choice_Type(compFieldMap);
		type.setLocation(getLocation( $col.start, $i.stop));
		$def_type = new Def_Type($i.identifier, type);
	}
};

pr_UnionKeyword:
	UNION
;

pr_UnionDefBody[CompFieldMap compFieldMap] returns[Identifier identifier]
@init {
	$identifier = null;
	CompField compField = null;
}:
(	(	i = pr_Identifier { $identifier = $i.identifier; }
		pr_StructDefFormalParList?
	|	ADDRESS { $identifier = new Identifier(Identifier_type.ID_TTCN, "ADDRESS", getLocation( $ADDRESS)); }
	)
	(	begin = pr_BeginChar
		c = pr_UnionFieldDef { compFieldMap.addComp($c.compField); }
		(	pr_Comma
			c = pr_UnionFieldDef { compFieldMap.addComp($c.compField); }
		)*
		endcol = pr_EndChar
	)
)
{
	if( $compFieldMap != null ) { $compFieldMap.setLocation(getLocation( $begin.start, $endcol.stop )); }
};

pr_UnionFieldDef returns[CompField compField]
@init {
	$compField = null;
	Type type = null;
	ArrayDimensions dimensions = null;
	List<ParsedSubType> parsedSubTypes = null;
}:
(	(	t1 = pr_Type { type = $t1.type; }
	|	t2 = pr_NestedTypeDef { type = $t2.type; }
	)
	i = pr_Identifier
	( d = pr_ArrayDef { dimensions = $d.dimensions; } )?
	( p = pr_SubTypeSpec { parsedSubTypes = $p.parsedSubTypes; } )?
)
{
	if(type != null && parsedSubTypes != null) {
		IType t = type;
		boolean seof = true;
		while(seof) {
			seof = t instanceof AbstractOfType;
			if (seof) {
				t = ((AbstractOfType) t).getOfType();
			}
		}
		t.setParsedRestrictions(parsedSubTypes);
	}
	if (dimensions != null) {
		for (int i = dimensions.size() - 1; i >= 0; i--) {
			type = new Array_Type(type, dimensions.get(i), true);
			type.setLocation(getLocation( $d.start, $d.stop));
		}
	}
	$compField = new CompField($i.identifier, type, false, null);
	$compField.setLocation(getLocation( $start, getStopToken()));
};

pr_SetDef returns[Def_Type def_type]
@init {
	$def_type = null;
	CompFieldMap compFieldMap = new CompFieldMap();
}:
(	col = pr_SetKeyword
	i = pr_StructDefBody[compFieldMap]
)
{
	if($i.identifier != null) {
		TTCN3_Set_Type type = new TTCN3_Set_Type(compFieldMap);
		type.setLocation(getLocation( $col.start, $i.stop));
		$def_type = new Def_Type($i.identifier, type);
	}
};

pr_SetKeyword:
	SET
;

pr_RecordOfDef returns[Def_Type def_type]
@init {
	$def_type = null;
	Type_Identifier_Helper helper = null;
	LengthRestriction restriction = null;
	Location commentLocation = lexer.getLastCommentLocation();
}:
(	col = pr_RecordKeyword
	( r = pr_StringLength { restriction = $r.restriction; } )?
	pr_OfKeyword
	h = pr_StructOfDefBody { helper = $h.helper; }
)
{
	if(helper.identifier != null && helper.type != null) {
		SequenceOf_Type type = new SequenceOf_Type(helper.type);
		if(restriction != null) {
			List<ParsedSubType> parsedSubTypes = new ArrayList<ParsedSubType>();
			parsedSubTypes.add(new Length_ParsedSubType(restriction));
			type.setParsedRestrictions(parsedSubTypes);
		}
		type.setLocation(getLocation( $col.start, $h.stop));
		$def_type = new Def_Type(helper.identifier, type);
		$def_type.setCommentLocation(commentLocation);
	}
};

pr_OfKeyword:
	OF
;

pr_StructOfDefBody returns[Type_Identifier_Helper helper]
@init {
	$helper = new Type_Identifier_Helper();
	Type type = null;
	Identifier identifier = null;
	List<ParsedSubType> parsedSubTypes = null;
}:
(	(	t1 = pr_Type { type = $t1.type; }
	|	t2 = pr_NestedTypeDef { type = $t2.type; }
	)
	
	(	i = pr_Identifier { identifier = $i.identifier; }
	|	ADDRESS { identifier = new Identifier(Identifier_type.ID_TTCN, "ADDRESS", getLocation( $ADDRESS)); }
	)
	
	( p = pr_SubTypeSpec { parsedSubTypes = $p.parsedSubTypes; } )?
)
{
	if(type != null && parsedSubTypes != null) {
		IType t = type;
		boolean seof = true;
		while(seof) {
			seof = t instanceof AbstractOfType;
			if (seof) {
				t = ((AbstractOfType) t).getOfType();
			}
		}
		t.setParsedRestrictions(parsedSubTypes);
	}
	$helper.type = type;
	$helper.identifier = identifier;
};

pr_SetOfDef returns[Def_Type def_type]
@init {
	$def_type = null;
	Type_Identifier_Helper helper = null;
	LengthRestriction restriction = null;
}:
(	col = pr_SetKeyword
	( r = pr_StringLength { restriction = $r.restriction; } )?
	pr_OfKeyword
	h = pr_StructOfDefBody { helper = $h.helper; }
)
{
	if(helper.identifier != null && helper.type != null) {
		SetOf_Type type = new SetOf_Type(helper.type);
		if(restriction != null) {
			List<ParsedSubType> parsedSubTypes = new ArrayList<ParsedSubType>();
			parsedSubTypes.add(new Length_ParsedSubType(restriction));
			type.setParsedRestrictions(parsedSubTypes);
		}
		type.setLocation(getLocation( $col.start, $h.stop));
		$def_type = new Def_Type(helper.identifier, type);
	}
};

pr_EnumDef returns[Def_Type def_type]
@init {
	$def_type = null;
	Identifier identifier = null;
	EnumerationItems items = null;
}:
(	col = pr_EnumKeyword
	(	id = pr_Identifier { identifier = $id.identifier; }
	|	ADDRESS { identifier = new Identifier(Identifier_type.ID_TTCN, "ADDRESS", getLocation( $ADDRESS)); }
	)
	pr_BeginChar
	e = pr_EnumerationList { items = $e.items; }
	endcol = pr_EndChar
)
{
	if(identifier != null && items != null) {
		Type type = new TTCN3_Enumerated_Type(items);
		type.setLocation(getLocation( $col.start, $endcol.stop));
		$def_type = new Def_Type(identifier, type);
	}
};

pr_EnumKeyword:
	ENUMERATED
;

pr_EnumerationList returns[EnumerationItems items]
@init {
	$items = new EnumerationItems();
}:
(	e = pr_Enumeration { $items.addEnumItem($e.enumItem); }
	(	pr_Comma
		e = pr_Enumeration { $items.addEnumItem($e.enumItem); }
	)*
);

pr_Enumeration returns[EnumItem enumItem]
@init {
	$enumItem = null;
	Integer_Value value = null;
	boolean minus = false;
}:
(	i = pr_Identifier
	(	pr_LParen
		(pr_Minus	{ minus = true; })?
		NUMBER
			{
				value = new Integer_Value($NUMBER.getText());
				if(minus) { (value).changeSign(); }
				value.setLocation(getLocation( $NUMBER));
			}
		pr_RParen
	)?
)
{
	$enumItem = new EnumItem($i.identifier, value);
	$enumItem.setLocation(getLocation( $i.start, $i.stop));
	Location commentLocation = lexer.getLastCommentLocation();
	$enumItem.setCommentLocation(commentLocation);
	if ( commentLocation != null ) {
		lexer.clearLastCommentLocation();
	}
};

pr_SubTypeDef returns[Def_Type def_type]
@init {
	$def_type = null;
	Identifier identifier = null;
	ArrayDimensions dimensions = null;
	List<ParsedSubType> parsedSubTypes = null;
}:
(	t = pr_Type
	(	i = pr_Identifier { identifier = $i.identifier; }
	|	ADDRESS	{ identifier = new Identifier(Identifier_type.ID_TTCN, "ADDRESS", getLocation( $ADDRESS)); }
	)
	( a = pr_ArrayDef { dimensions = $a.dimensions; } )?
	( s = pr_SubTypeSpec { parsedSubTypes = $s.parsedSubTypes; } )?
)
{
	if($t.type != null && parsedSubTypes != null) {
		$t.type.setParsedRestrictions(parsedSubTypes);
	}
	if (dimensions != null) {
		for (int i = dimensions.size() - 1; i >= 0; i--) {
			$t.type = new Array_Type($t.type, dimensions.get(i), true);
			$t.type.setLocation(getLocation( $a.start, $a.stop));
		}
	}
	if(identifier != null && $t.type != null) {
		$def_type = new Def_Type(identifier, $t.type);
	}
};

pr_SubTypeSpec returns[List<ParsedSubType> parsedSubTypes = null]:
(	a = pr_AllowedValues { $parsedSubTypes = $a.parsedSubTypes; }
	( r = pr_StringLength
		{	if($r.restriction != null && $parsedSubTypes != null) {
				$parsedSubTypes.add(new Length_ParsedSubType($r.restriction));
			}
		}
	)?
|	r = pr_StringLength
		{	if($r.restriction != null) {
				$parsedSubTypes = new ArrayList<ParsedSubType>();
				$parsedSubTypes.add(new Length_ParsedSubType($r.restriction));
			}
		}
);

pr_AllowedValues returns[List<ParsedSubType> parsedSubTypes]
@init {
	$parsedSubTypes = new ArrayList<ParsedSubType>();
	PatternString ps = new PatternString();
}:
(	pr_LParen
	(	p = pr_ValueOrRange	{ if( $p.parsedSubType != null ) { $parsedSubTypes.add( $p.parsedSubType ); }}
		(	pr_Comma
			p = pr_ValueOrRange	{ if( $p.parsedSubType != null ) { $parsedSubTypes.add( $p.parsedSubType ); }}
		)*
	|	pr_CharStringMatch[ps]	// an unsupported construct, but must not be reported
	)
	pr_RParen
);

// TODO: Rename for TemplateOrValue and rework its content !
pr_ValueOrRange returns[ParsedSubType parsedSubType]
@init {
	$parsedSubType = null;
}:
(	p = pr_RangeDef { $parsedSubType = $p.parsedSubType; }
|	v = pr_Expression { if($v.value != null) { $parsedSubType = new Single_ParsedSubType($v.value); } }
);

pr_RangeDef returns[ParsedSubType parsedSubType]
@init {
	$parsedSubType = null;
	boolean minExclusive = false;
	boolean maxExclusive = false;
}:
(	( pr_ExcludeBound { minExclusive = true; } )?
	min = pr_LowerBound
	RANGEOP
	( pr_ExcludeBound { maxExclusive = true; } )?
	max = pr_UpperBound
)
{
	if ( $min.value != null && $max.value != null ) {
		$parsedSubType = new Range_ParsedSubType( $min.value, minExclusive, $max.value, maxExclusive );
	}
};

pr_ExcludeBound:
	EXCLAMATIONMARK
;

pr_StringLength returns[LengthRestriction restriction]
@init {
	$restriction = null;
	boolean range = false;
}:
(	col = pr_LengthKeyword
	pr_LParen
	lower = pr_SingleExpression
	(	RANGEOP	{ range = true; }
		upper = pr_UpperBound
	)?
	endcol = pr_RParen
)
{
	if( !range ) {
		$restriction = new SingleLenghtRestriction( $lower.value );
	} else {
		$restriction = new RangeLenghtRestriction( $lower.value, $upper.value );
	}
	$restriction.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_LengthKeyword:
	LENGTH
;

pr_PortType returns[Reference reference]
@init {
	$reference = null;
}:
(	(	r = pr_GlobalModuleId { $reference = $r.reference; }
		pr_Dot
	)?
	id = pr_Identifier
)
{
	if($reference == null) {
		$reference = new Reference(null);
	}
	FieldSubReference subReference = new FieldSubReference($id.identifier);
	subReference.setLocation(getLocation( $id.start, $id.stop));
	$reference.addSubReference(subReference);
	$reference.setLocation(getLocation( $start, $id.stop));
};

pr_PortDef returns[ Def_Type def_type]
@init {
	$def_type = null;
	Location commentLocation = lexer.getLastCommentLocation();
}:
(	col = pr_PortKeyword
	i = pr_Identifier
	b = pr_PortDefAttribs
)
{
	if($i.identifier != null && $b.body != null) {
		Type type = new Port_Type($b.body);
		type.setLocation(getLocation( $col.start, $b.stop));

		$def_type = new Def_Type($i.identifier, type);
		$def_type.setCommentLocation(commentLocation);
	}
	lexer.clearLastCommentLocation();
};

pr_PortKeyword:
	PORT
;

pr_PortDefAttribs returns[PortTypeBody body]
@init {
	$body = null;
}:
(	b = pr_MessageAttribs { $body = $b.body; }
|	b2 = pr_ProcedureAttribs { $body = $b2.body; }
|	b3 = pr_MixedAttribs { $body = $b3.body; }
)
{
	if($body != null) {
		$body.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_MessageAttribs returns[PortTypeBody body]
@init {
	$body = null;
}:
(	pr_MessageKeyword { $body = new PortTypeBody(OperationModes.OP_Message); }
	pr_BeginChar
	(	pr_MessageList[$body]
		pr_SemiColon?
	)+
	pr_EndChar
);

pr_MessageList[PortTypeBody body]:
(  IN		t = pr_AllOrTypeList{ $body.addInTypes($t.types); }
|  OUT		t = pr_AllOrTypeList{ $body.addOutTypes($t.types); }
|  INOUT	t = pr_AllOrTypeList{ $body.addInoutTypes($t.types); }
);

pr_MessageKeyword:
	MESSAGE
;

pr_AllOrTypeList returns[List<IType> types = null]:
(	 pr_AllKeyword
|	t = pr_TypeList { $types = $t.types; }
);

pr_AllKeyword returns[String stringValue]:
	ALL
{
	$stringValue = $ALL.getText();
};

pr_TypeList returns[List<IType> types]
@init {
	$types = new ArrayList<IType>();
}:
(	t = pr_Type { if( $t.type != null ) { $types.add( $t.type ); } }
	(	pr_Comma
		t = pr_Type { if( $t.type != null ) { $types.add( $t.type ); } }
	)*
);

pr_ProcedureAttribs returns[PortTypeBody body]
@init {
	$body = null;
}:
(	pr_ProcedureKeyword { $body = new PortTypeBody(OperationModes.OP_Procedure); }
	pr_BeginChar
	(	pr_ProcedureList[$body]
		pr_SemiColon?
	)+
	pr_EndChar
);

pr_ProcedureKeyword:
	PROCEDURE
;

pr_ProcedureList[PortTypeBody body]:
(  IN		t = pr_AllOrTypeList{ $body.addInTypes($t.types); }
|  OUT		t = pr_AllOrTypeList{ $body.addOutTypes($t.types); }
|  INOUT	t = pr_AllOrTypeList{ $body.addInoutTypes($t.types); }
);

pr_MixedAttribs returns[PortTypeBody body]
@init {
	$body = null;
}:
(	col = pr_MixedKeyword
		{
			$body = new PortTypeBody(OperationModes.OP_Mixed);
			reportWarning( "Mixed ports are deprecated and may be fully removed in a future edition of the TTCN-3 standard ", $col.start, $col.stop );
		}
	pr_BeginChar
	(	pr_MixedList[$body]
		pr_SemiColon?
	)+
	pr_EndChar
);

pr_MixedKeyword:
	MIXED
;

pr_MixedList[PortTypeBody body]:
(  IN		t = pr_AllOrTypeList{ $body.addInTypes($t.types); }
|  OUT		t = pr_AllOrTypeList{ $body.addOutTypes($t.types); }
|  INOUT	t = pr_AllOrTypeList{ $body.addInoutTypes($t.types); }
);

pr_ComponentDef returns[Def_Type def_type]
@init {
	$def_type = null;
	ComponentTypeBody component = null;
	ComponentTypeReferenceList extends_refs = new ComponentTypeReferenceList();
	Location commentLocation = lexer.getLastCommentLocation();
}:
(	col = pr_ComponentKeyword
	i = pr_Identifier
	(	pr_ExtendsKeyword
		r = pr_ComponentType { if($r.reference != null) { extends_refs.addReference($r.reference); } }
		(	pr_Comma
			r = pr_ComponentType { if($r.reference != null) { extends_refs.addReference( $r.reference ); } }
		)*
	)?
	beginComp = pr_BeginChar
	( c = pr_ComponentDefList[$i.identifier, extends_refs] { component = $c.component; } )?
	endcol = pr_EndChar
)
{
	if($i.identifier != null) {
		extends_refs.setLocation(getLocation( $col.start, $endcol.stop));
		if(component == null) {
			component = new ComponentTypeBody($i.identifier, extends_refs);
		}
		component.setLocation(getLocation( $i.identifier.getLocation(), $endcol.stop ) );
	    component.setCommentLocation(commentLocation);
	    lexer.clearLastCommentLocation();
		Type type = new Component_Type(component);
		type.setLocation(getLocation( $col.start, $endcol.stop));
		$def_type = new Def_Type($i.identifier, type);
		$def_type.setLocation(getLocation( $col.start, $endcol.stop));
	}
};

pr_ExtendsKeyword:
	EXTENDS
;

pr_ComponentKeyword:
	COMPONENT
;

pr_ComponentType returns[Reference reference]
@init {
	$reference = null;
}:
(	(	r = pr_GlobalModuleId { $reference = $r.reference; }
		pr_Dot
	)?
	id = pr_ComponentTypeIdentifier
)
{
	if($reference == null) {
		$reference = new Reference(null);
	}
	FieldSubReference subReference = new FieldSubReference($id.identifier);
	subReference.setLocation(getLocation( $id.start, $id.stop));
	$reference.addSubReference(subReference);
	$reference.setLocation(getLocation( $start, $id.stop));
};

pr_ComponentTypeIdentifier returns [Identifier identifier]
@init {
	$identifier = null;
}:
	i = pr_Identifier { $identifier = $i.identifier; }
;

pr_ComponentDefList[Identifier identifier, ComponentTypeReferenceList extends_refs]
	returns[ComponentTypeBody component]
@init {
	$component = new ComponentTypeBody( $identifier, $extends_refs );
}:
(	d = pr_ComponentElementDef { if( $d.definitions != null ) { $component.addAssignments( $d.definitions ); } }
	pr_SemiColon?
	(	d = pr_ComponentElementDef { if( $d.definitions != null ) { $component.addAssignments( $d.definitions ); } }
		pr_SemiColon?
	)*
);

pr_ComponentElementDef returns[List<Definition> definitions = null]
@init {
	VisibilityModifier modifier = null;
}:
(	(  m = pr_ComponentElementVisibility { modifier = $m.modifier; } )?
	(  d = pr_PortInstance { $definitions = $d.definitions; }
	|  d2 = pr_VarInstance { $definitions = $d2.definitions; }
	|  d3 = pr_TimerInstance { $definitions = $d3.definitions; }
	|  d4 = pr_ConstDef { $definitions = $d4.array; }
	)
)
{
	for(int i = 0; i < $definitions.size(); i++) {
		Definition definition = $definitions.get(i);
		if ( modifier != null) {
			definition.setVisibility( modifier );
		}
		definition.setCumulativeDefinitionLocation(getLocation( $start, getStopToken()));
	}
	
};

pr_ComponentElementVisibility returns[VisibilityModifier modifier]
@init {
	$modifier = VisibilityModifier.Public;
}:
(	PUBLIC	{ $modifier = VisibilityModifier.Public; }
|	PRIVATE	{ $modifier = VisibilityModifier.Private; }
|	FRIEND	{ $modifier = VisibilityModifier.Friend; }
);

pr_PortInstance returns[List<Definition> definitions]
@init {
	$definitions = new ArrayList<Definition>();
}:
(	col = pr_PortKeyword
	p = pr_PortType
	e = pr_PortElement[ $p.reference ] { if( $e.def_port != null ) { $definitions.add( $e.def_port ); } }
	(	pr_Comma
		e = pr_PortElement[ $p.reference ] { if( $e.def_port != null ) { $definitions.add( $e.def_port ); } }
	)*
)
{
	if ( $definitions.size() > 0 ) {
		Definition pdef = $definitions.get(0);
		if ( pdef != null ) {
			final Token t = $col.start;
			pdef.getLocation().setLine( line - 1 + t.getLine() );
			Location loc = pdef.getLocation();
			loc.setOffset( offset + t.getStartIndex() );
		}
	}
};

pr_PortElement[Reference portTypeReference]
	returns[ Def_Port def_port]
@init {
	$def_port = null;
	ArrayDimensions dimensions = null;
}:
(	i = pr_Identifier
	( d = pr_ArrayDef { dimensions = $d.dimensions; } )?
)
{
	if($i.identifier != null) {
		$def_port = new Def_Port( $i.identifier, $portTypeReference, dimensions );
		$def_port.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_ConstDef returns[List<Definition> array = null]:
(	col = pr_ConstKeyword
	t = pr_Type
	a = pr_ConstList[ $t.type ] { $array = $a.array; }
)
{	for(int i = 0; i < $array.size(); i++) {
		Definition temp = $array.get(i);
		if(temp != null) {
			if (i==0) { // the location of "const Type" part belongs to the first const, no location overlapping
				temp.getLocation().setLine( line - 1 + $col.start.getLine() );
				Location loc = temp.getLocation();
				loc.setOffset( offset + $col.start.getStartIndex() );
			}
		}
	}
};


pr_ConstList[Type type] returns[List<Definition> array]
@init {
	$array = new ArrayList<Definition>();
}:
(	a = pr_SingleConstDef[type] { if($a.def_const != null) { $array.add($a.def_const); } }
	(	pr_Comma
		a = pr_SingleConstDef[type] { if($a.def_const != null) { $array.add($a.def_const); } }
	)*
);

pr_SingleConstDef[Type type] returns[ Def_Const def_const]
@init {
	$def_const = null;
	ArrayDimensions dimensions = null;
	Type tempType = $type;
}:
(	i = pr_Identifier
	( d = pr_ArrayDef { dimensions = $d.dimensions;} )?
	a = pr_AssignmentChar
	v = pr_Expression
)
{
	if($i.identifier != null && $v.value != null) {
		if (dimensions != null) {
			for (int i = dimensions.size() - 1; i >= 0; i--) {
				tempType = new Array_Type(tempType, dimensions.get(i), false);
				tempType.setLocation(getLocation( $i.stop, $a.start));
			}
		}
		$def_const = new Def_Const($i.identifier, tempType, $v.value);
		$def_const.setLocation(getLocation( $i.start, $v.stop));
	}
};

pr_ConstKeyword:
	CONST
;

pr_FunctionTypeDef returns[Def_Type def_type]
@init {
	$def_type = null;
	FormalParameterList parList = null;
	ReturnType_Helper helper = null;
	Type returnType = null;
	boolean returnsTemplate = false;
	TemplateRestriction.Restriction_type templateRestriction = TemplateRestriction.Restriction_type.TR_NONE;
	Configuration_Helper confighelper = new Configuration_Helper();
}:
(	col = pr_FunctionKeyword
	i = pr_Identifier
	start1 = pr_LParen
	( p = pr_FunctionFormalParList { parList = $p.parList; } )?
	end1 = pr_RParen
	( pr_RunsOnReferenceOrSelf[confighelper] )?
	(	h = pr_ReturnType
			{	helper = $h.helper;
				if(helper != null) {
					returnType = helper.type;
					returnsTemplate = helper.returnsTemplate;
					templateRestriction = helper.templateRestriction;
				}
			}
	)?
)
{
	if($i.identifier != null) {
		if(parList == null) { parList = new FormalParameterList(new ArrayList<FormalParameter>()); }
		parList.setLocation(getLocation( $start1.start, $end1.stop));
		Type type = new Function_Type(parList, confighelper.runsonReference, confighelper.runsOnSelf, returnType, returnsTemplate, templateRestriction);
		type.setLocation(getLocation( $col.start, getStopToken()));
		$def_type = new Def_Type($i.identifier, type);
	}
};

pr_AltstepTypeDef returns[Def_Type def_type]
@init {
	$def_type = null;
	FormalParameterList parList = null;
	Configuration_Helper confighelper = new Configuration_Helper();
	Token endcol = null;
}:
(	col = pr_AltstepKeyword
	i = pr_Identifier
	start1 = pr_LParen
	( p = pr_FunctionFormalParList { parList = $p.parList; } )?
	end1 = pr_RParen { endcol = $end1.stop; }
	( end2 = pr_RunsOnReferenceOrSelf[confighelper] { endcol = $end2.stop; } )?
)
{
	if($i.identifier != null) {
		if(parList == null) { parList = new FormalParameterList(new ArrayList<FormalParameter>()); }
		parList.setLocation(getLocation( $start1.start, $end1.stop));
		Type type = new Altstep_Type(parList, confighelper.runsonReference, confighelper.runsOnSelf);
		type.setLocation(getLocation( $col.start, endcol));
		$def_type = new Def_Type($i.identifier, type);
	}
};

pr_TestcaseTypeDef returns[Def_Type def_type]
@init {
	$def_type = null;
	FormalParameterList parList = null;
	Configuration_Helper helper = null;
	Reference runsonReference = null;
	Reference systemReference = null;
}:
(	col = pr_TestcaseKeyword
	i = pr_Identifier
	start1 = pr_LParen
	( p = pr_TestcaseFormalParList { parList = $p.parList; } )?
	end = pr_RParen
	h = pr_ConfigSpec
		{	helper = $h.helper;
			if(helper != null) {
				runsonReference = helper.runsonReference;
				systemReference = helper.systemReference;
			}
		}
)
{
	if($i.identifier != null) {
		if(parList == null) { parList = new TestcaseFormalParameterList(new ArrayList<FormalParameter>()); }
		parList.setLocation(getLocation( $start1.start, $end.stop));
		Type type = new Testcase_Type(parList, runsonReference, systemReference);
		type.setLocation(getLocation( $col.start, $h.stop));
		$def_type = new Def_Type($i.identifier, type);
	}
};

pr_TemplateDef returns[Def_Template def_template]
@init {
	$def_template = null;
	TemplateRestriction.Restriction_type templateRestriction = TemplateRestriction.Restriction_type.TR_NONE;
	Template_definition_helper helper = new Template_definition_helper();
	Reference derivedReference = null;
	TemplateBody body = null;
}:
(	col = pr_TemplateKeyword
	( t = pr_TemplateRestriction { templateRestriction = $t.templateRestriction; } )?
	pr_BaseTemplate[helper]
	( d = pr_DerivedDef { derivedReference = $d.reference; } )?
	pr_AssignmentChar
	b = pr_TemplateBody
)
{
	if(helper.identifier != null && helper.type != null && $b.body != null) {
		$def_template = new Def_Template(templateRestriction, helper.identifier, helper.type, helper.formalParList, derivedReference, $b.body.getTemplate());
		$def_template.setLocation(getLocation( $col.start, $b.stop));
		$def_template.setCommentLocation(lexer.getLastCommentLocation());
	}
};

pr_BaseTemplate [Template_definition_helper helper]
@init {
	FormalParameterList formalParList = null;
}:
(	t = pr_Type // handles pr_Signature too
	i = pr_Identifier
	(	formalStart = pr_LParen
		p = pr_TemplateFormalParList { formalParList = $p.parList; }
		pr_RParen
	)?
)
{
	$helper.type = $t.type;
	$helper.identifier = $i.identifier;
	if(formalParList != null) {
		helper.formalParList = formalParList;
		helper.formalParList.setLocation(getLocation( $formalStart.start, getStopToken() ));
	}
};

pr_TemplateKeyword:
	TEMPLATE
;

pr_DerivedDef returns[Reference reference]
@init {
	$reference = null;
}:
(	pr_ModifiesKeyword
	r = pr_TemplateRef { $reference = $r.reference; }
);

pr_ModifiesKeyword:
	MODIFIES
{
};

pr_TemplateFormalParList returns[FormalParameterList parList]
@init {
	$parList = null;
	List<FormalParameter> parameters = new ArrayList<FormalParameter>();
}:
(	p = pr_TemplateFormalPar { if( $p.parameter != null ) { parameters.add( $p.parameter ); }}
	(	pr_Comma
		p = pr_TemplateFormalPar { if( $p.parameter != null ) { parameters.add( $p.parameter ); }}
	)*
)
{
	$parList = new FormalParameterList( parameters );
};

pr_TemplateFormalPar  returns[FormalParameter parameter]
@init {
	$parameter = null;
}:
(  p = pr_FormalTemplatePar { $parameter = $p.parameter; }
|  p2 = pr_FormalValuePar { $parameter = $p2.parameter; }
);

pr_ListOfTemplates returns [ListOfTemplates templates]
@init {
	$templates = new ListOfTemplates();
}:
(	pr_LParen
	t = pr_TemplateListItem { if($t.template != null) { $templates.addTemplate($t.template); }}
	(	pr_Comma
		t = pr_TemplateListItem { if($t.template != null) { $templates.addTemplate($t.template); }}
	)*
	pr_RParen
);

pr_TemplateListItem returns [ITemplateListItem template]
@init {
	$template = null;
}:
(	t = pr_AllElementsFrom { $template = $t.template; }
|	t2 = pr_TemplateBody { $template = $t2.body; }
);

pr_AllElementsFrom returns[ AllElementsFrom template]
@init {
	$template = null;
}:
(	pr_AllKeyword
	pr_FromKeyword
	b = pr_TemplateBody { if( $b.body!= null ) { $template = new AllElementsFrom( $b.body.getTemplate() ); } }
);

pr_TemplateBody returns[ TemplateBody body]
@init {
	$body = null;
	TTCN3Template temp = null;
}:
(	(	t1 = pr_FieldSpecList { temp = $t1.template; }
	|	t2 = pr_ArraySpecList  { temp = $t2.indexed_template_list; }
	|	t3 = pr_ArrayValueOrAttrib { temp = $t3.template; }
	|	t4 = pr_SimpleSpec { temp = $t4.template; }
	)
	(	pr_ExtraMatchingAttributes[temp]	)?
)
{
	if( temp != null ) {
		$body = new TemplateBody(temp);
		//TODO: set location
	}
};

pr_SimpleSpec returns[TTCN3Template template]
@init {
	$template = null;
}:
	t = pr_SingleValueOrAttrib { $template = $t.template;}
;

pr_FieldSpecList returns[TTCN3Template template]
@init {
	$template = null;
	NamedTemplates namedTemplates = new NamedTemplates();
}:
(	col = pr_BeginChar
	(	n = pr_FieldSpec   { if($n.namedTemplate != null) { namedTemplates.addTemplate($n.namedTemplate); }}
		(	pr_Comma
			n = pr_FieldSpec   { if($n.namedTemplate != null) { namedTemplates.addTemplate($n.namedTemplate); }}
		)*
	)?
	endcol = pr_EndChar
)
{
	if(namedTemplates.getNofTemplates() == 0) {
		$template = new Template_List(new ListOfTemplates());
	} else {
		$template = new Named_Template_List(namedTemplates);
	}
	$template.setLocation(getLocation( $col.start, $endcol.stop));
};

//From FieldSpec the AllElementsFrom handling is excluded therefore TemplateBody is replaced with its template
pr_FieldSpec returns[NamedTemplate namedTemplate]
@init {
	$namedTemplate = null;
}:
(	name = pr_StructFieldRef
	pr_AssignmentChar
	b = pr_TemplateBody
)
{
	if($name.identifier != null && $b.body != null) {
		$namedTemplate = new NamedTemplate($name.identifier, $b.body.getTemplate());
		$namedTemplate.setLocation(getLocation( $name.start, $b.stop));
	}
};

pr_FieldReference returns[Identifier identifier]
@init {
	$identifier = null;
}:
(	i = pr_StructFieldRef { $identifier = $i.identifier; }
//|	subReference = pr_ArrayOrBitRef // covered by arrayspeclist
//|	pr_ParRef // handled in pr_StructFieldRef
);

pr_StructFieldRef returns[Identifier identifier]
@init {
	$identifier = null;
}:
(	t = pr_PredefinedType
		{	$identifier = new Identifier(Identifier_type.ID_TTCN, $t.type.getTypename(), getLocation( $t.start, $t.stop ));	}
|	(	id = pr_Identifier { $identifier = $id.identifier; }
		pr_TypeActualParList?
	)
);

pr_ArraySpecList returns[Indexed_Template_List indexed_template_list]
@init {
	$indexed_template_list = null;
	IndexedTemplates templates = new IndexedTemplates();
}:
(	col = pr_BeginChar
	(	i = pr_ArraySpec  { if ( $i.indexedTemplate != null ) { templates.addTemplate($i.indexedTemplate); } }
		(	pr_Comma
			i = pr_ArraySpec  { if ( $i.indexedTemplate != null ) { templates.addTemplate($i.indexedTemplate); } }
		)*
	)?
	endcol = pr_EndChar
)
{
	$indexed_template_list = new Indexed_Template_List(templates);
	$indexed_template_list.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_ArraySpec returns[IndexedTemplate indexedTemplate]
@init {
	$indexedTemplate = null;
}:
(	index = pr_ArrayOrBitRef
	pr_AssignmentChar
	b = pr_TemplateBody
)
{
	if ($index.subReference != null && $b.body != null) {
		$indexedTemplate = new IndexedTemplate($index.subReference, $b.body);
		$indexedTemplate.setLocation(getLocation( $index.start, $b.stop));
	}
};

pr_ArrayOrBitRefOrDash returns[ArraySubReference subReference]
@init {
	$subReference = null;
	Value value = null;
}:
(	col = pr_SquareOpen
	(	v = pr_SingleExpression { value = $v.value; }
	|	d = pr_Dash
			{
				value = new Notused_Value();
				value.setLocation(getLocation( $d.start, $d.stop));
			}
	)
	endcol = pr_SquareClose
)
{
	$subReference = new ArraySubReference(value);
	$subReference.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_ArrayOrBitRef returns[ArraySubReference subReference]
@init {
	$subReference = null;
}:
(	col = pr_SquareOpen
	v = pr_SingleExpression
	endcol = pr_SquareClose
)
{
	$subReference = new ArraySubReference($v.value);
	$subReference.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_SingleValueOrAttrib returns[TTCN3Template template]
@init {
	$template = null;
}:
(		v = pr_SingleExpression {
		if( $v.value != null ) { $template = new SpecificValue_Template($v.value); }
	}
|   t = pr_MatchingSymbol { $template = $t.template; }
|	pr_NotUsedSymbol { $template = new NotUsed_Template(); }
)
{
	if ($template != null) {
		$template.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_ArrayValueOrAttrib returns[TTCN3Template template]
@init {
	$template = null;
}:
(	col = pr_BeginChar
	t = pr_ArrayElementSpecList
	endcol = pr_EndChar
)
{
	if($t.templates == null) {
		$template = new Template_List(new ListOfTemplates());
	} else {
		$template = new Template_List($t.templates);
	}
	$template.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_ArrayElementSpecList returns[ListOfTemplates templates]
@init {
	$templates = new ListOfTemplates();
}:
(	b = pr_ArrayElementSpec { if($b.body != null) { $templates.addTemplate($b.body); }}
	(	pr_Comma
		b = pr_ArrayElementSpec { if($b.body != null) { $templates.addTemplate($b.body); }}
	)*
);

pr_ArrayElementSpec returns[ TemplateBody body]
@init {
	$body = null;
	ListOfTemplates templates = null;
}:
(	t = pr_PermutationMatch
		{	templates = $t.templates;
			if ( templates!=null ) {
				$body = new TemplateBody( new PermutationMatch_Template(templates));
			}
		}
|	pr_NotUsedSymbol { $body = new TemplateBody(new NotUsed_Template()); }
|	b = pr_TemplateBody { $body = $b.body; }
|	t1 = pr_AllElementsFrom { $body = $t1.template; }
)
{
	if($body != null) {
		  $body.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_NotUsedSymbol:
	pr_Dash
;

//Replaced in the next pr_ : |  templates = pr_ValueOrAttribList { if( templates!= null) { template = new ValueList_Template(templates);}; }
//by:
pr_MatchingSymbol returns[TTCN3Template template]
@init {
	$template = null;
	PatternString ps = new PatternString();
}:
(	t1 = pr_Complement { $template = new ComplementedList_Template($t1.templates); }
|	v1 = pr_Range { $template = new Value_Range_Template($v1.valueRange); }
|	t2 = pr_ListOfTemplates { if( $t2.templates!= null ) { $template = new ValueList_Template($t2.templates);} }
|	pr_AnyValue { $template = new Any_Value_Template(); }
|	pr_AnyOrOmit { $template = new AnyOrOmit_Template(); }
|	p1 = pr_BitStringMatch { $template = new BitString_Pattern_Template($p1.pattern); }
|	p2 = pr_HexStringMatch { $template = new HexString_Pattern_Template($p2.pattern); }
|	p3 = pr_OctetStringMatch { $template = new OctetString_Pattern_Template($p3.pattern); }
|	pr_CharStringMatch[ps]
		{
			if (ps.getPatterntype() == PatternType.UNIVCHARSTRING_PATTERN) {
				$template = new UnivCharString_Pattern_Template(ps);
			} else {
				$template = new CharString_Pattern_Template(ps);
			}
		}
|	t3 = pr_SubsetMatch { $template = new SubsetMatch_Template($t3.templates); }
|	t4 = pr_SupersetMatch { $template = new SupersetMatch_Template($t4.templates); }
)
{
	if($template != null) {
		$template.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_ExtraMatchingAttributes [TTCN3Template template]:
(	l = pr_LengthMatch
	d = pr_IfPresentMatch	{ if($template != null) { $template.setLengthRestriction($l.restriction); $template.setIfpresent(); } }
|	l = pr_LengthMatch		{ if($template != null) { $template.setLengthRestriction($l.restriction); } }
|   b = pr_IfPresentMatch	{ if($template != null) { $template.setIfpresent(); }}
);

pr_BitString returns[String string]:
	BSTRING
{
	$string = $start.getText().replaceAll("^\'|\'B$", "");
};

pr_HexString returns[String string]:
	HSTRING
{
	$string = $start.getText().replaceAll("^\'|\'H$", "");
};

pr_OctetString returns[String string]:
	OSTRING
{
	$string = $start.getText().replaceAll("^\'|\'O$", "");
};

pr_BitStringMatch returns[String pattern]:
	BSTRINGMATCH
{
	$pattern = $start.getText().replaceAll("^\'|\'B$", "");
};

pr_HexStringMatch returns[String pattern]:
	HSTRINGMATCH
{
	$pattern = $start.getText().replaceAll("^\'|\'H$", "");
};

pr_OctetStringMatch returns[String pattern]:
	OSTRINGMATCH
{
	$pattern = $start.getText().replaceAll("^\'|\'O$", "");
};

pr_SubsetMatch returns[ListOfTemplates templates]
@init {
	$templates = null;
}:
(	pr_SubsetKeyword
	t = pr_ListOfTemplates { $templates = $t.templates; }
);

pr_SubsetKeyword:
	SUBSET
;

pr_SupersetMatch returns[ListOfTemplates templates]
@init {
	$templates = null;
}:
(	pr_SupersetKeyword
	t = pr_ListOfTemplates { $templates = $t.templates; }
);

pr_SupersetKeyword:
	SUPERSET
;

pr_PermutationMatch returns[ListOfTemplates templates]
@init {
	$templates = null;
}:
(	pr_PermutationKeyword
	t = pr_ListOfTemplates { $templates = $t.templates; }
);

pr_PermutationKeyword:
	PERMUTATION
;

pr_CharStringMatch[PatternString ps]
@init {
	StringBuilder builder = new StringBuilder();
	boolean[] uni = new boolean[1];
	uni[0] = false;
}:
(	pr_PatternKeyword
	pr_PatternChunk[builder, uni] { if (uni[0]) { $ps.setPatterntype(PatternType.UNIVCHARSTRING_PATTERN); } }
	(	STRINGOP
		pr_PatternChunk[builder, uni]
	)*
);

pr_PatternKeyword:
	PATTERNKEYWORD
;

pr_PatternChunk[StringBuilder builder, boolean[] uni]:
(	a = pr_CString { $builder.append($a.string); }
|	v = pr_ReferencedValue
		{	$builder.append('{');
			$builder.append($v.text);
			$builder.append('}');
		}
|	ustring_value = pr_Quadruple
		{
			UniversalChar uc = null;
			for (int i = 0; i < $ustring_value.string.length(); i++) {
				uc = $ustring_value.string.get(i);
	    		if (uc.group() != 0 || uc.plane() != 0 || uc.row() != 0 || uc.cell() > 127) {
	      			uni[0] = true;
	      		}
	    		$builder.append("\\q{");
	    		$builder.append(uc.group()).append(',').append(uc.plane()).append(',').append(uc.row()).append(',').append(uc.cell());
	    		$builder.append('}');
	  		}
		}
);

pr_Complement returns[ListOfTemplates templates]
@init {
	$templates = null;
}:
(	pr_ComplementKeyword
	t = pr_ListOfTemplates { $templates = $t.templates; }
);

pr_ComplementKeyword:
	COMPLEMENTKEYWORD
;

pr_AnyValue:
	QUESTIONMARK
;

pr_AnyOrOmit:
	STAR
;

pr_LengthMatch returns[LengthRestriction restriction]
@init {
	$restriction = null;
}:
	r = pr_StringLength { $restriction = $r.restriction; }
;

pr_IfPresentMatch:
	IFPRESENT
;

pr_Range returns[ValueRange valueRange]
@init {
	$valueRange = null;
}:
(	col = pr_LParen
	min = pr_LowerBound
	RANGEOP
	max = pr_UpperBound
	endcol = pr_RParen
)
{
	$valueRange = new ValueRange($min.value, $max.value);
};

pr_LowerBound returns[Value value]
@init {
	$value = null;
}:
	v = pr_SingleExpression { $value = $v.value; }
;

pr_UpperBound returns[Value value]
@init {
	$value = null;
}:
	v = pr_SingleExpression { $value = $v.value; }
;

pr_TemplateInstance returns[TemplateInstance templateInstance]
@init {
	$templateInstance = null;
}:
	t = pr_InLineTemplate { $templateInstance = $t.templateInstance; }
;

pr_TemplateRefWithParList returns[Reference reference]
@init {
	$reference = null;
	ParsedActualParameters parameters = null;
}:
(	(	r = pr_GlobalModuleId { $reference = $r.reference; }
		pr_Dot
	)?
	i = pr_Identifier
	(	p = pr_TemplateActualParList { parameters = $p.parsedParameters; }	)?
)
{
	if( $reference == null) {
		$reference = new Reference(null);
	}
	if(parameters == null) {
		$reference.addSubReference(new FieldSubReference($i.identifier));
	} else {
		ParameterisedSubReference subReference = new ParameterisedSubReference($i.identifier, parameters);
		subReference.setLocation(new Location(parameters.getLocation()));
		$reference.addSubReference(subReference);
	}
	$reference.setLocation(getLocation( $start, getStopToken()));
};

pr_TemplateRef returns[Reference reference]
@init {
	$reference = null;
}:
(	(	r = pr_GlobalModuleId { $reference = $r.reference; }
		pr_Dot
	)?
	id = pr_Identifier //covers templateparIdentifier too
)
{
	if($reference == null) {
		$reference = new Reference(null);
	}
	$reference.addSubReference(new FieldSubReference($id.identifier));
	$reference.setLocation(getLocation( $start, $id.stop));
};

pr_InLineTemplate returns[TemplateInstance templateInstance]
@init {
	$templateInstance = null;
	Type type = null;
	Reference derived = null;
}:
(	(	// ReferencedType ==> Signature
		t = pr_Type { type = $t.type; }
		pr_Colon
	)?
	(	r = pr_DerivedRefWithParList { derived = $r.reference; }
		pr_AssignmentChar
	)?
	b = pr_TemplateBody
)
{
	if($b.body != null) {
		$templateInstance = new TemplateInstance(type, derived, $b.body.getTemplate());
		$templateInstance.setLocation(getLocation( $start, $b.stop));
	}
};

pr_DerivedRefWithParList returns[Reference reference]
@init {
	$reference = null;
}:
(	pr_ModifiesKeyword
	r = pr_TemplateRefWithParList { $reference = $r.reference; }
);

pr_TemplateActualParList returns [ParsedActualParameters parsedParameters]
@init {
	$parsedParameters = new ParsedActualParameters();
}:
(	col = pr_LParen
	i = pr_TemplateActualPar	{ if($i.instance != null) { $parsedParameters.addUnnamedParameter($i.instance); }}
	(	pr_Comma
		i = pr_TemplateActualPar	{ if($i.instance != null) { $parsedParameters.addUnnamedParameter($i.instance); }}
	)*
	endcol = pr_RParen
)
{
	$parsedParameters.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_TemplateActualPar returns[TemplateInstance instance]
@init {
	$instance = null;
}:
(	pr_NotUsedSymbol
	{
		TTCN3Template template = new NotUsed_Template();
		template.setLocation(getLocation( $start, getStopToken()));
		$instance = new TemplateInstance(null, null, template);
		$instance.setLocation(getLocation( $start, getStopToken()));
	}
|	t = pr_TemplateInstance { $instance = $t.templateInstance; }
);

pr_TemplateOps returns[Value value]
@init {
	$value = null;
}:

(	v = pr_MatchOp { $value = $v.value; }
|	v2 = pr_ValueofOp { $value = $v2.value; }
);

pr_MatchOp returns[MatchExpression value]
@init {
	$value = null;
}:
(	col = pr_MatchKeyword
	pr_LParen
	v = pr_Expression
	pr_Comma
	t = pr_TemplateInstance
	endcol = pr_RParen
)
{
	$value = new MatchExpression($v.value, $t.templateInstance);
	$value.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_MatchKeyword:
	MATCH
;

pr_ValueofOp returns[ValueofExpression value]
@init {
	$value = null;
}:
(	col = pr_ValueofKeyword
	pr_LParen
	t = pr_TemplateInstance
	endcol = pr_RParen
)
{
	$value = new ValueofExpression($t.templateInstance);
	$value.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_ValueofKeyword returns[String stringValue]:
	VALUEOF
{
	$stringValue = $VALUEOF.getText();
};

pr_FunctionDef returns[Def_Function def_func]
@init {
	$def_func = null;
	FormalParameterList parameters = null;
	StatementBlock statementBlock = null;
	Configuration_Helper runsonHelper = new Configuration_Helper();
	ReturnType_Helper returnHelper = null;
	Type returnType = null;
	boolean returnsTemplate = false;
	TemplateRestriction.Restriction_type templateRestriction = TemplateRestriction.Restriction_type.TR_NONE;
	Location commentLocation = lexer.getLastCommentLocation();
	if (commentLocation != null) {
		lexer.clearLastCommentLocation();
	}
}:
(	col = pr_FunctionKeyword
	i = pr_Identifier
	start1 = pr_LParen
	( p = pr_FunctionFormalParList { parameters = $p.parList; } )?
	end = pr_RParen
	( pr_RunsOnSpec[runsonHelper] )?
	( rh = pr_ReturnType
		{	returnHelper = $rh.helper;
			if(returnHelper != null) {
				returnType = returnHelper.type;
				returnsTemplate = returnHelper.returnsTemplate;
				templateRestriction = returnHelper.templateRestriction;
			}
		}
	)?
	s = pr_StatementBlock { statementBlock = $s.statementblock; }
)
{
	if($i.identifier != null && statementBlock != null) {
		if(parameters == null) { parameters = new FormalParameterList(new ArrayList<FormalParameter>()); }
		parameters.setLocation(getLocation( $start1.start, $end.stop));
		$def_func = new Def_Function($i.identifier, parameters, runsonHelper.runsonReference, returnType, returnsTemplate, templateRestriction, statementBlock);
		$def_func.setLocation(getLocation( $col.start, $s.stop));
		$def_func.setCommentLocation(commentLocation);
	}
};

pr_FunctionKeyword:
	FUNCTION
;

pr_FunctionFormalParList returns[FormalParameterList parList]
@init {
	$parList = null;
	List<FormalParameter> parameters = new ArrayList<FormalParameter>();
}:
(	p = pr_FunctionFormalPar { if($p.parameter != null) { parameters.add($p.parameter); }}
	(	pr_Comma
		p=pr_FunctionFormalPar { if($p.parameter != null) { parameters.add($p.parameter); }}
	)*
)
{
	$parList = new FormalParameterList(parameters);
};

pr_FunctionFormalPar returns[FormalParameter parameter]
@init {
	$parameter = null;
}:
(	p1 = pr_FormalTimerPar { $parameter = $p1.parameter; }
|	p2 = pr_FormalTemplatePar { $parameter = $p2.parameter; }
|	p3 = pr_FormalValuePar { $parameter = $p3.parameter; }
//|	 p4 = pr_FormalPortPar { $parameter = $p4.parameter; } // handled by pr_FormalValuePar
);

pr_ReturnType returns[ReturnType_Helper helper]
@init {
	$helper = new ReturnType_Helper();
	boolean returnsTemplate = false;
	TemplateRestriction.Restriction_type templateRestriction = TemplateRestriction.Restriction_type.TR_NONE;
}:
(	col = RETURN
	( tr = pr_TemplateOptRestricted { returnsTemplate = true; templateRestriction = $tr.templateRestriction; } )?
	t = pr_Type
)
{
	$helper.type = $t.type;
	$helper.returnsTemplate = returnsTemplate;
	$helper.templateRestriction = templateRestriction;
};

pr_RunsOnSpec [Configuration_Helper helper]:
(	pr_RunsKeyword
	pr_OnKeyword
	runsonReference = pr_ComponentType
)
{
	$helper.runsonReference = $runsonReference.reference;
};

pr_RunsOnReferenceOrSelf [Configuration_Helper helper]:
(	col = pr_RunsKeyword
	pr_OnKeyword
	(	runsonReference = pr_ComponentType { $helper.runsonReference = $runsonReference.reference; }
	|	pr_SelfKeyword { $helper.runsOnSelf = true; }
	)
);

pr_RunsKeyword:
	RUNS
;

pr_OnKeyword:
	ON
;

pr_SelfKeyword:
	SELF
;

pr_FunctionStatementOrDefList returns[List<Statement> statements = null]:
(
	(	s = pr_FunctionStatementOrDef
			{	if($statements == null) {$statements = $s.statements;}
				else if ($s.statements != null) { $statements.addAll($s.statements); }
			}
		pr_SemiColon?
	)+
);

pr_FunctionStatementOrDef returns[List<Statement> statements]
@init {
	$statements = new ArrayList<Statement>();
	List<Definition> definitions = null;
	Statement statement = null;
}:
(	d1 = pr_FunctionLocalDef { definitions =  $d1.definitions; }
|	d2 = pr_FunctionLocalInst { definitions =  $d2.definitions; }
|	s = pr_FunctionStatement { statement = $s.statement; }
)
{
	if(definitions != null) {
		for(Definition definition : definitions) {
			definition.setCumulativeDefinitionLocation(getLocation( $start, getStopToken()));
			Statement temp_statement = new Definition_Statement(definition);
			temp_statement.setLocation(getLocation( $start, getStopToken()));
			$statements.add(temp_statement);
		}
	} else if(statement != null) {
		$statements.add(statement);
	}
};

pr_FunctionLocalInst returns[List<Definition> definitions = null]:
(	d = pr_VarInstance { $definitions = $d.definitions; }
|	d2 = pr_TimerInstance { $definitions = $d2.definitions; }
);

pr_FunctionLocalDef returns[List<Definition> definitions = null]:
(	d = pr_ConstDef { $definitions = $d.array; }
|	def = pr_TemplateDef
		{	if($def.def_template != null) {
				$definitions = new ArrayList<Definition>();
				$definitions.add($def.def_template);
			}
		}
);

pr_FunctionStatement returns[Statement statement]
@init {
	$statement = null;
}:
( s1 = pr_BehaviourStatements		{ $statement = $s1.statement; }
| s2 = pr_TimerStatements			{ $statement = $s2.statement; }
| s3 = pr_ConfigurationStatements	{ $statement = $s3.statement; }
| s4 = pr_CommunicationStatements	{ $statement = $s4.statement; }
| s5 = pr_BasicStatements			{ $statement = $s5.statement; }
| s6 = pr_VerdictStatements			{ $statement = $s6.statement; }
| s7 = pr_SUTStatements				{ $statement = $s7.statement; }
| s8 = pr_TestcaseStopStatement		{ $statement = $s8.statement; }
);

pr_TestcaseStopStatement returns[TestcaseStop_Statement statement]
@init {
	$statement = null;
	LogArguments logArguments = null;
}:
(	TESTCASE DOT STOP
	( pr_LParen
	  l = pr_LogArguments { logArguments = $l.logArguments; }
	  pr_RParen
	)?
)
{
	$statement = new TestcaseStop_Statement(logArguments);
	$statement.setLocation(getLocation( $start, getStopToken()));
};

pr_FunctionInstance returns[Reference temporalReference]
@init {
	$temporalReference = null;
	ParsedActualParameters parameters = null;
}:
(	t = pr_FunctionRef { $temporalReference = $t.reference; }
	a = pr_LParen
	( p = pr_FunctionActualParList { parameters = $p.parsedParameters; } )?
	endcol = pr_RParen
)
{
	if($temporalReference != null) {
		ISubReference subReference = $temporalReference.removeLastSubReference();
		if(parameters == null) {
			parameters = new ParsedActualParameters();
		}
		parameters.setLocation(getLocation( $a.start, $endcol.stop));
		subReference = new ParameterisedSubReference(subReference.getId(), parameters);
		((ParameterisedSubReference) subReference).setLocation(getLocation( $start, $endcol.stop));
		$temporalReference.addSubReference(subReference);
		$temporalReference.setLocation(getLocation( $start, $endcol.stop));
	}
};

pr_FunctionRef returns[Reference reference]
@init {
	$reference = null;
}:
(	i1 = pr_Identifier
	(	DOT
		pr_ObjectIdentifierValue
		DOT
		i2 = pr_Identifier
		{	$reference = new Reference($i1.identifier);
			FieldSubReference subReference = new FieldSubReference($i2.identifier);
			subReference.setLocation(getLocation( $i2.start, $i2.stop));
			$reference.addSubReference(subReference);
		}
	|	DOT
		i2 = pr_Identifier
		{	$reference = new Reference($i1.identifier);
			FieldSubReference subReference = new FieldSubReference($i2.identifier);
			subReference.setLocation(getLocation( $i2.start, $i2.stop));
			$reference.addSubReference(subReference);
		}
	|	{	$reference = new Reference(null);
			FieldSubReference subReference = new FieldSubReference($i1.identifier);
			subReference.setLocation(getLocation( $i1.start, $i1.stop));
			$reference.addSubReference(subReference);
		}
	)
)
{
	$reference.setLocation(getLocation( $start, getStopToken()));
};

pr_FunctionActualParList returns [ParsedActualParameters parsedParameters]
@init {
	$parsedParameters = new ParsedActualParameters();
	boolean isStillUnnamed = true;
}:
(	i = pr_FunctionActualorNamedPar[isStillUnnamed, $parsedParameters] { isStillUnnamed = $i.isUnnamed; }
	(	pr_Comma
		i = pr_FunctionActualorNamedPar[isStillUnnamed, $parsedParameters] { isStillUnnamed = $i.isUnnamed; }
	)*
);

pr_FunctionActualorNamedPar [boolean isStillUnnamed, ParsedActualParameters parsedParameters]
	returns [boolean isUnnamed]
@init {
	$isUnnamed = true;
}:
(		i = pr_Identifier
		pr_AssignmentChar
		ins = pr_FunctionActualPar
			{	$isUnnamed = false;
				if($i.identifier != null) {
					NamedParameter named = new NamedParameter($i.identifier, $ins.instance);
					named.setLocation(getLocation( $start, $ins.stop));
					parsedParameters.addNamedParameter(named);
				}
			}
|	{ $isStillUnnamed }? ins = pr_FunctionActualPar
	{	$isUnnamed = true;
		$parsedParameters.addUnnamedParameter($ins.instance);
	}
);

pr_FunctionActualPar returns[TemplateInstance instance]
@init {
	$instance = null;
}:
(	n = pr_NotUsedSymbol
		{	TTCN3Template template = new NotUsed_Template();
			template.setLocation(getLocation( $n.start, $n.stop));
			$instance = new TemplateInstance(null, null, template);
			$instance.setLocation(getLocation( $n.start, $n.stop));
		}
|	t = pr_TemplateInstance { $instance = $t.templateInstance; }
);

pr_ApplyOpEnd returns [ParsedActualParameters parsedParameters]
@init {
	$parsedParameters = new ParsedActualParameters();
}:
(	col = pr_Dot
	pr_ApplyKeyword
	l = pr_LParen
	( p = pr_FunctionActualParList { $parsedParameters = $p.parsedParameters; } )?
	endcol = pr_RParen
)
{
	if ($parsedParameters != null) {
		$parsedParameters.setLocation(getLocation( $l.start, $endcol.stop));
	}
};

pr_ApplyKeyword:
	APPLY
;

pr_DereferOp returns[Value value]
@init {
	$value = null;
}:
(	pr_DerefersKeyword
	pr_LParen
	v = pr_Expression { $value = $v.value; }
	pr_RParen
);

pr_DerefersKeyword:
	DEREFERS
;

pr_SignatureDef returns[ Def_Type def_type]
@init {
	$def_type = null;
	SignatureFormalParameterList parameters = null;
	Type returnType = null;
	boolean no_block = false;
	SignatureExceptions exceptions = null;
}:
(	col = pr_SignatureKeyword
	i = pr_Identifier
	beginpar = pr_LParen
	( p = pr_SignatureFormalParList { parameters = $p.parList; } )?
	endpar = pr_RParen
	(	RETURN r = pr_Type { returnType = $r.type; }
	|	pr_NoBlockKeyword { no_block = true; }
	)?
	( e = pr_ExceptionSpec { exceptions = $e.exceptions; } )?
)
{
	if($i.identifier != null) {
		if(parameters == null) {
			parameters = new SignatureFormalParameterList(new ArrayList<SignatureFormalParameter>());
			parameters.setLocation(getLocation( $beginpar.start, $endpar.stop));
		}

		Type type = new Signature_Type(parameters, returnType, no_block, exceptions);
		type.setLocation(getLocation( $col.start, getStopToken()));
		$def_type = new Def_Type($i.identifier, type);
		$def_type.setLocation(getLocation( $col.start, getStopToken()));
		Location commentLocation = lexer.getLastCommentLocation();
		$def_type.setCommentLocation(commentLocation);
		if (commentLocation != null) {
			lexer.clearLastCommentLocation();
		}
	}
};

pr_SignatureKeyword:
	SIGNATURE
;

pr_SignatureFormalParList returns[SignatureFormalParameterList parList]
@init {
	$parList = null;
	SignatureFormalParameter parameter = null;
	List<SignatureFormalParameter> parameters = new ArrayList<SignatureFormalParameter>();
}:
(	p = pr_SignatureFormalPar { if($p.parameter != null) { parameters.add($p.parameter); }}
	(	pr_Comma
		p = pr_SignatureFormalPar { if($p.parameter != null) { parameters.add($p.parameter); }}
	)*
)
{
	$parList = new SignatureFormalParameterList(parameters);
};

pr_SignatureFormalPar returns[SignatureFormalParameter parameter]
@init {
	$parameter = null;
	Token startcol = null;
	int parameterType = SignatureFormalParameter.PARAM_IN;
}:
(	(	cola = IN { startcol = $cola; parameterType = SignatureFormalParameter.PARAM_IN; }
	|	colb = INOUT { startcol = $colb; parameterType = SignatureFormalParameter.PARAM_INOUT; }
	|	colc = OUT { startcol = $colc; parameterType = SignatureFormalParameter.PARAM_OUT; }
	)?
	t = pr_Type { if( startcol == null) { startcol = $t.start; }}
	i = pr_Identifier
)
{
	$parameter = new SignatureFormalParameter(parameterType, $t.type, $i.identifier);
	$parameter.setLocation(getLocation( startcol, $i.stop));
};

pr_ExceptionSpec returns[SignatureExceptions exceptions]
@init {
	$exceptions = null;
}:
(	col = pr_ExceptionKeyword
	pr_LParen
	t = pr_ExceptionTypeList
	endcol = pr_RParen
)
{
	$exceptions = new SignatureExceptions($t.types);
	$exceptions.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_ExceptionKeyword:
	EXCEPTION
;

pr_ExceptionTypeList returns[List<Type> types]
@init {
	$types = new ArrayList<Type>();
}:
(	t = pr_Type { if($t.type != null) { $types.add($t.type); }}
	(	pr_Comma
		t = pr_Type { if($t.type != null) { $types.add($t.type); }}
	)*
);

pr_NoBlockKeyword:
	NOBLOCK
;

pr_Signature returns[Reference reference]
@init {
	$reference = null;
}:
(	(	r = pr_GlobalModuleId { $reference = $r.reference; }
		pr_Dot
	)?
	id = pr_Identifier
)
{
	if($reference == null) {
		$reference = new Reference(null);
	}
	FieldSubReference subReference = new FieldSubReference($id.identifier);
	subReference.setLocation(getLocation( $id.start, $id.stop));
	$reference.addSubReference(subReference);
	$reference.setLocation(getLocation( $start, $id.stop));
};

pr_TestcaseDef returns[ Def_Testcase def_testcase]
@init {
	$def_testcase = null;
	FormalParameterList parameters = null;
	Configuration_Helper helper = null;
	Reference runsonReference = null;
	Reference systemReference = null;
	StatementBlock statementBlock = null;
}:
(	col = pr_TestcaseKeyword
	i = pr_Identifier
	start1 = pr_LParen
	( p = pr_TestcaseFormalParList { parameters = $p.parList; } )?
	end = pr_RParen
	h = pr_ConfigSpec
		{	helper = $h.helper;
			if(helper != null) { runsonReference = helper.runsonReference; systemReference = helper.systemReference; }
		}
	s = pr_StatementBlock { statementBlock = $s.statementblock; }
)
{
	Location commentLocation = lexer.getLastCommentLocation();
	if($i.identifier != null) {
		if(parameters == null) { parameters = new TestcaseFormalParameterList(new ArrayList<FormalParameter>()); }
		parameters.setLocation(getLocation( $start1.start, $end.stop));
		$def_testcase = new Def_Testcase($i.identifier, parameters, runsonReference, systemReference, statementBlock);
		$def_testcase.setLocation(getLocation( $col.start, $s.stop));
		$def_testcase.setCommentLocation(commentLocation);
	}
	lexer.clearLastCommentLocation();
};

pr_TestcaseKeyword:
	TESTCASE
;

pr_TestcaseFormalParList returns[TestcaseFormalParameterList parList]
@init {
	$parList = null;
	List<FormalParameter> parameters = new ArrayList<FormalParameter>();
}:
(	p = pr_TestcaseFormalPar { if( $p.parameter != null ) { parameters.add( $p.parameter ); } }
	(	pr_Comma
		p = pr_TestcaseFormalPar { if( $p.parameter != null ) { parameters.add( $p.parameter ); } }
	)*
)
{
	$parList = new TestcaseFormalParameterList( parameters );
};

pr_TestcaseFormalPar returns[FormalParameter parameter]
@init {
	$parameter = null;
}:
(	p = pr_FormalTemplatePar { $parameter = $p.parameter; }
|	p2 = pr_FormalValuePar { $parameter = $p2.parameter; }
);


pr_ConfigSpec returns[Configuration_Helper helper]
@init {
	$helper = new Configuration_Helper();
}:
(	pr_RunsOnSpec[ $helper ]
	( pr_SystemSpec[ $helper ] )?
);

pr_SystemSpec [Configuration_Helper helper]:
(	pr_SystemKeyword
	r = pr_ComponentType
)
{
	$helper.systemReference = $r.reference;
};

pr_SystemKeyword:
	SYSTEM
;

pr_TestcaseInstanceOp returns[Value value]
@init {
	$value = null;
	Value dereferredValue = null;
	boolean isDereferred = false;
	Reference temporalReference = null;
	ParsedActualParameters parameters = null;
	Value timerValue = null;
}:
(	col = EXECUTE
	pr_LParen
	(	dv = pr_DereferOp	{ dereferredValue = $dv.value; isDereferred = true; }
	|	tr = pr_FunctionRef { temporalReference = $tr.reference; }
	)
	parstart = pr_LParen
	( p = pr_TestcaseActualParList { parameters = $p.parsedParameters; } )?
	parend = pr_RParen
	(	pr_Comma
		tv = pr_TimerValue { timerValue = $tv.value; }
	)?
	endcol = pr_RParen
)
{
	if(parameters == null) {
		parameters = new ParsedActualParameters();
	}
	parameters.setLocation(getLocation( $parstart.start, $parend.stop));

	if(isDereferred) {
		$value = new ExecuteDereferedExpression(dereferredValue, parameters, timerValue);
	} else if(temporalReference != null) {
		Location temporalLocation = temporalReference.getLocation();
		ISubReference subReference = temporalReference.removeLastSubReference();
		subReference = new ParameterisedSubReference(subReference.getId(), parameters);
		((ParameterisedSubReference) subReference).setLocation(getLocation(temporalLocation, $parend.stop));
		temporalReference.addSubReference(subReference);
		temporalReference.setLocation(getLocation(temporalReference.getLocation(), $parend.stop));
		$value = new ExecuteExpression(temporalReference, timerValue);
	}
	$value.setLocation(getLocation( $col, $endcol.stop));

};

pr_TestcaseInstanceStatement returns[Statement statement]
@init {
	$statement = null;
	Value dereferredValue = null;
	boolean isDereferred = false;
	Reference temporalReference = null;
	ParsedActualParameters parameters = null;
	Value timerValue = null;
}:
(	col = EXECUTE
	pr_LParen
	(	dv = pr_DereferOp	{ dereferredValue = $dv.value; isDereferred = true; }
	|	tr = pr_FunctionRef { temporalReference = $tr.reference; }
	)
	parstart = pr_LParen
	( p = pr_TestcaseActualParList { parameters = $p.parsedParameters; } )?
	parend = pr_RParen
	(	pr_Comma
		tv = pr_TimerValue { timerValue = $tv.value; }
	)?
	endcol = pr_RParen
)
{
	if(parameters == null) {
		parameters = new ParsedActualParameters();
	}
	parameters.setLocation(getLocation( $parstart.start, $parend.stop));

	if(isDereferred) {
		$statement = new Referenced_Testcase_Instance_Statement(dereferredValue, parameters, timerValue);
		$statement.setLocation(getLocation( $col, $endcol.stop));
	} else if(temporalReference != null) {
		Location temporalLocation = temporalReference.getLocation();
		ISubReference subReference = temporalReference.removeLastSubReference();
		subReference = new ParameterisedSubReference(subReference.getId(), parameters);
		((ParameterisedSubReference) subReference).setLocation(getLocation(temporalLocation, $parend.stop));
		temporalReference.addSubReference(subReference);
		temporalReference.setLocation(getLocation(temporalLocation, $parend.stop));
		$statement = new Testcase_Instance_Statement(temporalReference, timerValue);
		$statement.setLocation(getLocation( $col, $endcol.stop));
	}
};

pr_TestcaseActualParList returns [ParsedActualParameters parsedParameters]
@init {
	$parsedParameters = new ParsedActualParameters();
	boolean isStillUnnamed = true;
}:
(	p = pr_TestcaseActualorNamedPar[isStillUnnamed, $parsedParameters] { isStillUnnamed = $p.isUnnamed; }
	(	pr_Comma
		p = pr_TestcaseActualorNamedPar[isStillUnnamed, $parsedParameters] { isStillUnnamed = $p.isUnnamed; }
	)*
);

pr_TestcaseActualorNamedPar [boolean isStillUnnamed, ParsedActualParameters parsedParameters]
	returns [boolean isUnnamed]
@init {
	$isUnnamed = true;
}:
(	id = pr_Identifier
	pr_AssignmentChar
	ins = pr_TestcaseActualPar
		{
			$isUnnamed = false;
			NamedParameter named = new NamedParameter($id.identifier, $ins.instance);
			named.setLocation(getLocation( $id.start, $ins.stop));
			$parsedParameters.addNamedParameter(named);
		}
|	{ $isStillUnnamed }? ins = pr_TestcaseActualPar
		{
			$isUnnamed = true;
			$parsedParameters.addUnnamedParameter($ins.instance);
		}
);

pr_TestcaseActualPar returns[TemplateInstance instance]
@init {
	$instance = null;
}:
(	n = pr_NotUsedSymbol
		{
			TTCN3Template template = new NotUsed_Template();
			template.setLocation(getLocation( $n.start, $n.stop));
			$instance = new TemplateInstance(null, null, template);
			$instance.setLocation(getLocation( $n.start, $n.stop));
		}
|	i = pr_TemplateInstance { $instance = $i.templateInstance; }
);

pr_AltstepDef returns[ Def_Altstep  def_altstep]
@init {
	$def_altstep = null;
	FormalParameterList parameters = null;
	Configuration_Helper runsonHelper = new Configuration_Helper();
	AltGuards altGuards = null;
	List<Definition> definitions = null;
}:
(	col = pr_AltstepKeyword
	i = pr_Identifier
	start1 = pr_LParen
	( p = pr_FunctionFormalParList { parameters = $p.parList;} )?
	end = pr_RParen
	( pr_RunsOnSpec[ runsonHelper ] )?
	blockstart = pr_BeginChar
	( d = pr_AltstepLocalDefList { definitions = $d.definitions; } )?
	a = pr_AltGuardList { altGuards = $a.altGuards; }
	endcol = pr_EndChar
)
{
	if($i.identifier != null && altGuards != null ) {
		StatementBlock statementBlock = new StatementBlock();
		statementBlock.setLocation(getLargeLocation( $blockstart.start, $endcol.stop));
		if(definitions != null) {
			for(Definition definition : definitions) {
				Statement statement = new Definition_Statement(definition);
				statement.setLocation(getLocation( $d.start, $d.stop));
				statementBlock.addStatement(statement);
			}
		}
		if(parameters == null) { parameters = new FormalParameterList(new ArrayList<FormalParameter>()); }
		parameters.setLocation(getLocation( $start1.start, $end.stop));
		altGuards.setLocation(getLocation( $a.start, $a.stop));
		$def_altstep = new Def_Altstep($i.identifier, parameters, runsonHelper.runsonReference, statementBlock, altGuards);
		$def_altstep.setLocation(getLocation( $col.start, $endcol.stop));
	}
};

pr_AltstepKeyword:
	ALTSTEP
;

pr_AltstepLocalDefList returns[ List<Definition> definitions = null]:
(	d = pr_AltstepLocalDef { $definitions = $d.definitions; }
	(	pr_SemiColon?
		d2 = pr_AltstepLocalDef { if( $definitions != null && $d2.definitions != null) { $definitions.addAll($d2.definitions); }}
	)*
	pr_SemiColon?
);

pr_AltstepLocalDef returns[ List<Definition> definitions = null]:
(	d1 = pr_VarInstance { $definitions = $d1.definitions; }
|	d2 = pr_TimerInstance { $definitions = $d2.definitions; }
|	d3 = pr_ConstDef { $definitions = $d3.array; }
|	def = pr_TemplateDef
		{
			if($def.def_template != null) {
				$definitions = new ArrayList<Definition>();
				$definitions.add($def.def_template);
			}
		}
)
{
	for ( int i = 0; i < $definitions.size(); i++ ) {
		Definition definition = $definitions.get(i);
		definition.setCumulativeDefinitionLocation(getLocation( $start, getStopToken()));
	}
};

pr_AltstepInstance returns[Reference temporalReference]
@init {
	$temporalReference = null;
}:
(	t = pr_FunctionInstance { $temporalReference = $t.temporalReference; }
);

pr_ImportDef [Group parent_group]
@init {
	Token endcol = null;
	VisibilityModifier modifier = null;
	boolean selective = false;
	ImportModule impmod = null;
	MultipleWithAttributes attributes = null;
}:
(	( m = pr_ComponentElementVisibility { modifier = $m.modifier; } )?
	col = IMPORT
	i = pr_ImportFromSpec { impmod = $i.impmod; }
	(	enda = pr_AllWithExcepts { endcol = $enda.stop; if( impmod != null ) { impmod.setHasNormalImports(); } }
	|	pr_BeginChar
		pr_ImportSpec[ impmod ]
		endb = pr_EndChar { endcol = $endb.stop; selective = true; }
	)
	( a = pr_WithStatement { endcol = $a.stop; attributes = $a.attributes; } )?
)
{
	if(impmod != null) {
		if(modifier != null){
			impmod.setVisibility(modifier);
		}
		impmod.setWithAttributes(attributes);
		impmod.setLocation(getLocation( modifier != null ? $m.start : $col, endcol));
		if(parent_group == null) {
	  		impmod.setAttributeParentPath(act_ttcn3_module.getAttributePath());
		} else {
	  		parent_group.addImportedModule(impmod);
	  		impmod.setAttributeParentPath(parent_group.getAttributePath());
	    }
	    act_ttcn3_module.addImportedModule(impmod);
	}
};

pr_AllWithExcepts:
(	col = pr_AllKeyword
	( endcol = pr_ExceptsDef )?
);

pr_ExceptsDef:
	col = pr_ExceptKeyword
	pr_BeginChar
	pr_ExceptSpec
	endcol = pr_EndChar
{
	reportWarning( "Selective importation is not yet supported, importing all definitions",	$col.start, $endcol.stop );
};

pr_ExceptSpec:
(	col = pr_ExceptElement
	( endcol = pr_SemiColon )?
)*
;

pr_ExceptKeyword:
	EXCEPT
;

pr_ExceptElement:
(	pr_ExceptGroupSpec
|	pr_ExceptTypeDefSpec
|	pr_ExceptTemplateSpec
|	pr_ExceptConstSpec
|	pr_ExceptTestcaseSpec
|	pr_ExceptFunctionOrAltstepSpec
|	pr_ExceptSignatureSpec
|	pr_ExceptModuleParSpec
);

pr_ExceptGroupSpec:
(	pr_GroupKeyword
	(	pr_ExceptGroupRefList
	|	pr_AllKeyword
	)
);

pr_ExceptTypeDefSpec:
(	pr_TypeDefKeyword
	(	pr_TypeRefList
	|	pr_AllKeyword
	)
);

pr_ExceptTemplateSpec:
(	pr_TemplateKeyword
	(	pr_TemplateRefList
	|	pr_AllKeyword
	)
);

pr_ExceptConstSpec:
(	pr_ConstKeyword
	(	pr_ConstRefList
	|	pr_AllKeyword
	)
);

pr_ExceptTestcaseSpec:
(	pr_TestcaseKeyword
	(	pr_TestcaseRefList
	|	pr_AllKeyword
	)
);

pr_ExceptFunctionOrAltstepSpec:
(	(	pr_AltstepKeyword
	|	pr_FunctionKeyword
	)

	(	pr_FunctionRefList
	|	pr_AllKeyword
	)
);

pr_ExceptSignatureSpec:
(	pr_SignatureKeyword
	(	pr_SignatureRefList
	|	pr_AllKeyword
	)
);

pr_ExceptModuleParSpec:
(	pr_ModuleParKeyword
	(	a = pr_ModuleParRefList
	|	b = pr_AllKeyword
	)
);

pr_ImportSpec [ImportModule impmod]:
(	pr_ImportElement[impmod]
	pr_SemiColon?
)*
;

pr_ImportElement [ImportModule impmod]:
(	pr_ImportModuleParSpec	{if($impmod != null) {$impmod.setHasNormalImports();}}
|	pr_ImportGroupSpec		{if($impmod != null) {$impmod.setHasNormalImports();}}
|	pr_ImportTypeDefSpec	{if($impmod != null) {$impmod.setHasNormalImports();}}
|	pr_ImportTemplateSpec	{if($impmod != null) {$impmod.setHasNormalImports();}}
|	pr_ImportConstSpec		{if($impmod != null) {$impmod.setHasNormalImports();}}
|	pr_ImportTestcaseSpec	{if($impmod != null) {$impmod.setHasNormalImports();}}
|	pr_ImportFunctionOrAltstepSpec	{if($impmod != null) {$impmod.setHasNormalImports();}}
|	pr_ImportSignatureSpec	{if($impmod != null) {$impmod.setHasNormalImports();}}
|	pr_ImportImportSpec		{if($impmod != null) {$impmod.setHasImportOfImports();}}
);

pr_ImportImportSpec:
	IMPORT	pr_AllKeyword
;

pr_ImportModuleParSpec:
(	pr_ModuleParKeyword
	(	pr_ModuleParRefList
	|	pr_AllModuleParWithExcept
	)
)
{
	reportWarning( "Selective importation is not yet supported, importing all definitions", $start, getStopToken() );
};

pr_ModuleParRefList:
	pr_Identifier
	(	pr_Comma
		pr_Identifier
	)*
;

pr_AllModuleParWithExcept:
	pr_AllKeyword (pr_ExceptKeyword pr_ModuleParRefList)?
;

pr_ImportFromSpec returns [ImportModule impmod]
@init {
	$impmod = null;
}:
(	pr_FromKeyword
	i = pr_ImportModuleId { $impmod = $i.impmod; }
	pr_RecursiveKeyword?
);

pr_RecursiveKeyword:
	RECURSIVE
{
	reportWarning( "Recursive importation is deprecated and may be fully removed in a future edition of the TTCN-3 standard", $RECURSIVE );
};

pr_ImportModuleId returns [ImportModule impmod]
@init {
	$impmod = null;
}:
(	i = pr_ImportGlobalModuleId { $impmod = new ImportModule($i.identifier); }
	pr_LanguageSpec?
);

pr_LanguageKeyword returns[String stringValue]:
	LANGUAGE
{
	$stringValue = $LANGUAGE.getText();
};

pr_LanguageSpec:
(	pr_LanguageKeyword
	pr_FreeText
);

pr_ImportGlobalModuleId returns [Identifier identifier]
@init {
	$identifier = null;
}:
(	i = pr_Identifier
	(	DOT
		pr_ObjectIdentifierValue
	)?
) {
	if($i.identifier != null) {
		$identifier = $i.identifier;
	}
};

pr_GlobalModuleId returns [Reference reference]
@init {
	$reference = null;
}:
(	i = pr_Identifier
	(		DOT
			pr_ObjectIdentifierValue
			{ if ($i.identifier != null) { $reference = new Reference($i.identifier); } }
	|	{	if($i.identifier != null) {
			$reference = new Reference(null);
			FieldSubReference subReference = new FieldSubReference($i.identifier);
			subReference.setLocation(getLocation( $i.start, $i.stop));
			$reference.addSubReference(subReference);} }
	)
);

pr_ImportGroupSpec:
	pr_GroupKeyword
	(	pr_GroupRefListWithExcept
	|	pr_AllGroupsWithExcept
	)
{
	reportWarning( "Selective importation is not yet supported, importing all definitions", $start, getStopToken() );
};

pr_GroupRefList:
	pr_FullGroupIdentifier
	(	pr_Comma
		pr_FullGroupIdentifier
	)*
;

pr_GroupRefListWithExcept:
	pr_FullGroupIdentifierWithExcept
	(	pr_Comma
		pr_FullGroupIdentifierWithExcept
	)*
;

pr_AllGroupsWithExcept:
	pr_AllKeyword
	(	pr_ExceptKeyword
		pr_GroupRefList
	)?
;

pr_FullGroupIdentifier returns [Qualifier qualifier]
@init {
	$qualifier = null;
}:
	i = pr_Identifier { $qualifier = new Qualifier(new FieldSubReference($i.identifier)); }
	(	pr_Dot
		i = pr_Identifier  { $qualifier.addSubReference(new FieldSubReference($i.identifier)); }
	)*
{
	$qualifier.setLocation(getLocation( $start, getStopToken()));
};

pr_FullGroupIdentifierWithExcept:
	pr_FullGroupIdentifier
	pr_ExceptsDef?
;

pr_ImportTypeDefSpec:
	pr_TypeDefKeyword
	(	pr_TypeRefList
	|	pr_AllTypesWithExcept
	)
{
	reportWarning( "Selective importation is not yet supported, importing all definitions", $start, getStopToken() );
};

pr_ExceptGroupRefList:
	pr_ExceptFullGroupIdentifier
	(	pr_Comma
		pr_ExceptFullGroupIdentifier
	)*
;

pr_ExceptFullGroupIdentifier:
	pr_FullGroupIdentifier
;

pr_TypeRefList:
	pr_Identifier
	(	pr_Comma
		pr_Identifier
	)*
;

pr_AllTypesWithExcept:
	pr_AllKeyword
	(	pr_ExceptKeyword
		pr_TypeRefList
	)?
;

pr_ImportTemplateSpec:
	pr_TemplateKeyword
	(	pr_TemplateRefList
	|	pr_AllTemplsWithExcept
	)
{
	reportWarning( "Selective importation is not yet supported, importing all definitions", $start, getStopToken() );
};

pr_TemplateRefList:
(	pr_Identifier
	(	pr_Comma
		pr_Identifier
	)*
);

pr_AllTemplsWithExcept:
	pr_AllKeyword
	(	pr_ExceptKeyword
		pr_TemplateRefList
	)?
;

pr_ImportConstSpec:
	pr_ConstKeyword
	(	pr_ConstRefList
	|	pr_AllConstsWithExcept
	)
{
	reportWarning( "Selective importation is not yet supported, importing all definitions", $start, getStopToken() );
};

pr_ConstRefList:
(	pr_Identifier
	(	pr_Comma
		pr_Identifier
	)*
);

pr_AllConstsWithExcept:
	pr_AllKeyword (pr_ExceptKeyword pr_ConstRefList)?
;

pr_ImportTestcaseSpec:
	pr_TestcaseKeyword
	(	pr_TestcaseRefList
	|	pr_AllTestcasesWithExcept
	)
{
	reportWarning( "Selective importation is not yet supported, importing all definitions", $start, getStopToken() );
};

pr_TestcaseRefList:
	pr_Identifier
	(	pr_Comma
		pr_Identifier
	)*
;

pr_AllTestcasesWithExcept:
	pr_AllKeyword
	(	pr_ExceptKeyword
		pr_TestcaseRefList
	)?
;

pr_ImportFunctionOrAltstepSpec:
(	(	pr_AltstepKeyword
	|	pr_FunctionKeyword
	)

	(	pr_FunctionRefList
	|	pr_AllFunctionsWithExcept
	)
)
{
	reportWarning( "Selective importation is not yet supported, importing all definitions", $start, getStopToken() );
};

pr_ImportFunctionSpec:
	pr_FunctionKeyword
	(	pr_FunctionRefList
	|	pr_AllFunctionsWithExcept
	)
;

pr_FunctionRefList:
	pr_Identifier
	(	pr_Comma
		pr_Identifier
	)*
;

pr_AllFunctionsWithExcept:
	pr_AllKeyword
	(	pr_ExceptKeyword
		pr_FunctionRefList
	)?
;

pr_ImportSignatureSpec:
	pr_SignatureKeyword
	(	pr_SignatureRefList
	|	pr_AllSignaturesWithExcept
	)
{
	reportWarning( "Selective importation is not yet supported, importing all definitions", $start, getStopToken() );
};

pr_SignatureRefList:
(	pr_Identifier
	(	pr_Comma
		pr_Identifier
	)*
);

pr_AllSignaturesWithExcept:
	pr_AllKeyword
	(	pr_ExceptKeyword
		pr_SignatureRefList
	)?
;

pr_GroupDef[Group parent_group]
@init {
	Group group = null;
	MultipleWithAttributes attributes = null;
	Location commentLocation = lexer.getLastCommentLocation();
	lexer.clearLastCommentLocation();
}:
(	PUBLIC?
	col = pr_GroupKeyword
	i = pr_Identifier
		{	group = new Group($i.identifier);
			group.setCommentLocation(commentLocation);
		}
	begin = pr_BeginChar
	pr_ModuleDefinitionsList[group]
	end1 = pr_EndChar
	( a = pr_WithStatement { attributes = $a.attributes; } )?
)
{
	if (group != null) {
		group.setWithAttributes(attributes);
		group.setParentGroup(parent_group);
		group.setLocation(getLocation( $start, getStopToken()));
		group.setInnerLocation(getLocation( $begin.start, getStopToken()));
		group.setCommentLocation(commentLocation);
		if ($parent_group != null) {
			$parent_group.addGroup(group);
			group.setAttributeParentPath(parent_group.getAttributePath());
		} else {
			group.setAttributeParentPath(act_ttcn3_module.getAttributePath());
			act_ttcn3_module.addGroup(group);
		}
	}
};

pr_GroupIdentifier returns [ Identifier identifier]
@init {
	$identifier = null;
}:
(	pr_GroupKeyword
	i = pr_Identifier { $identifier = $i.identifier; }
);

pr_GroupKeyword:
	GROUP
;

pr_Visibility returns[VisibilityModifier modifier]
@init {
	$modifier = VisibilityModifier.Public;
}:
(	PUBLIC	{$modifier = VisibilityModifier.Public;}
|	PRIVATE	{$modifier = VisibilityModifier.Private;}
|	FRIEND	{$modifier = VisibilityModifier.Friend;}
);

pr_FriendModuleDef[Group parent_group]
@init {
	List<Identifier> identifiers = new ArrayList<Identifier>();
	MultipleWithAttributes attributes = null;
}:
(	PRIVATE?
	FRIEND
	MODULE
	i = pr_TTCN3ModuleId  { if ($i.identifier != null) {identifiers.add($i.identifier);}}
	(	pr_Comma
		i = pr_TTCN3ModuleId  { if ($i.identifier != null) {identifiers.add($i.identifier);}}
	)*
	( a = pr_WithStatement { attributes = $a.attributes; } )?
)
{
	for (Identifier identifier2 : identifiers) {
		FriendModule friend = new FriendModule(identifier2);
		friend.setWithAttributes(attributes);
		friend.setLocation(getLocation( $start, getStopToken()));
		if($parent_group == null) {
			friend.setAttributeParentPath(act_ttcn3_module.getAttributePath());
		} else {
	  		$parent_group.addFriendModule(friend);
	  		friend.setAttributeParentPath(parent_group.getAttributePath());
	    }
	    act_ttcn3_module.addFriendModule(friend);
	}
};

pr_ExtFunctionDef returns [Def_Extfunction def_extfunction]
@init {
	$def_extfunction = null;
	FormalParameterList parameters = null;
	Type returnType = null;
	boolean returnsTemplate = false;
	TemplateRestriction.Restriction_type templateRestriction = TemplateRestriction.Restriction_type.TR_NONE;
}:
(	col = pr_ExtKeyword
	pr_FunctionKeyword
	i = pr_Identifier
	start1 = pr_LParen
	( p = pr_FunctionFormalParList { parameters = $p.parList; } )?
	enda = pr_RParen
	(	h = pr_ReturnType
			{	ReturnType_Helper helper = $h.helper;
				if(helper != null) {
					returnType = helper.type;
					returnsTemplate = helper.returnsTemplate;
					templateRestriction = helper.templateRestriction;
				}
			}
	)?
)
{
	if($i.identifier != null) {
		if(parameters == null) { parameters = new FormalParameterList(new ArrayList<FormalParameter>()); }
		parameters.setLocation(getLocation( $start1.start, $enda.stop));
		$def_extfunction = new Def_Extfunction($i.identifier, parameters,  returnType, returnsTemplate, templateRestriction);
		$def_extfunction.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_ExtKeyword:
	EXTERNAL
;

pr_ExtConstDef returns[List<Definition> definitions]
@init {
	$definitions = new ArrayList<Definition>();
}:
(	col = pr_ExtKeyword
	pr_ConstKeyword
	t = pr_Type
	i = pr_Identifier
		{	if($i.identifier != null && $t.type != null) {
				Definition def = new Def_ExternalConst($i.identifier, $t.type);
				def.setLocation(getLocation( $col.start, $i.stop));
				$definitions.add(def);
			}
		}
	(	pr_Comma
		i = pr_Identifier
			{	if($i.identifier != null && $t.type != null) {
					Definition def = new Def_ExternalConst($i.identifier, $t.type);
					def.setLocation(getLocation( $i.start, $i.stop));
					$definitions.add(def);
				}
			}
	)*
);

//------------------------------------------------------
//   Module parameter definitions    1.6.1.12
//------------------------------------------------------

pr_ModuleParDef returns [List<Definition> parameters = null]
@init {
	Token endcol = null;
	List<Definition> multitypedModulePar = null;
}:
(	col = pr_ModuleParKeyword
	(	p1 = pr_ModulePar { $parameters = $p1.parameters; endcol = $p1.stop; }
	|	p2 = pr_TemplateModulePar { $parameters = $p2.parameters; endcol = $p2.stop; }
	|	(	pr_BeginChar
			(	(	p31 = pr_ModulePar { multitypedModulePar = $p31.parameters; }
				|	p32 = pr_TemplateModulePar { multitypedModulePar = $p32.parameters; }
				)
					{	if ( multitypedModulePar != null ) {
							if ( $parameters == null ) {
								$parameters = new ArrayList<Definition>();
							}
							$parameters.addAll( multitypedModulePar );
						}
					}
				pr_SemiColon?
			)+
			b = pr_EndChar{ endcol = $b.stop; }
		)
	)
)
{
	if ( $parameters != null ) {
		for (int i = 0; i < $parameters.size(); i++) {
			Definition def = $parameters.get(i);
			if ( def != null ) {
				def.getLocation().setLine($col.start.getLine());
				Location loc = def.getLocation();
				loc.setOffset( offset + $col.start.getStartIndex() );
				loc.setEndOffset( offset + endcol.getStopIndex() + 1 );
			}
		}
	}
};

pr_ModuleParKeyword:
	MODULEPAR
;

pr_ModulePar returns [List<Definition> parameters = null]:
(	t = pr_Type
	p = pr_ModuleParList[ $t.type ] { $parameters = $p.parameters; }
);

pr_TemplateModulePar returns [List<Definition> parameters = null]:
(	pr_TemplateKeyword
	t = pr_Type
	p = pr_TemplateModuleParList[ $t.type ] { $parameters = $p.parameters; }
);

pr_ModuleParList [Type type]
	returns [List<Definition> parameters]
@init {
	$parameters = new ArrayList<Definition>();
	Value value = null;
}:
	i = pr_Identifier
	(	pr_AssignmentChar
		v = pr_Expression { value = $v.value; }
	)?
		{	if($i.identifier != null && $type != null) {
				Definition def = new Def_ModulePar($i.identifier, $type, value);
				def.setLocation(getLocation( $i.start, getStopToken()));
				$parameters.add(def);
			}
		}
	(	pr_Comma
		i2 = pr_Identifier { value = null; }
		(	pr_AssignmentChar
			v2 = pr_Expression { value = $v2.value; }
		)?
			{	if($i2.identifier != null && $type != null) {
					Definition def = new Def_ModulePar($i2.identifier, $type, value);
					def.setLocation(getLocation( $i2.start, getStopToken()));
					$parameters.add(def);
				}
			}
	)*
;

pr_TemplateModuleParList [Type type]
	returns [List<Definition> parameters]
@init {
	$parameters = new ArrayList<Definition>();
	Token endcol = null;
	Definition def;
	TemplateBody body = null;
}:
	i = pr_Identifier { endcol = $i.start; }
	(	pr_AssignmentChar
		b = pr_TemplateBody { endcol = $b.stop; body = $b.body; }
	)?
		{	if($i.identifier != null && $type != null) {
				if(body != null ) {
					def = new Def_ModulePar_Template($i.identifier, $type, body.getTemplate());
				} else {
					def = new Def_ModulePar_Template($i.identifier, $type, null);
				}
			def.setLocation(getLocation( $i.start, endcol));
			$parameters.add(def);
			}
		}
	(	pr_Comma
		i2 = pr_Identifier { endcol = $i2.stop; body = null; }
		(	pr_AssignmentChar
			b2 = pr_TemplateBody { endcol = $b2.stop; body = $b2.body; }
		)?
			{	if($i2.identifier != null && type != null) {
					if(body != null) {
						def = new Def_ModulePar_Template($i2.identifier, $type, body.getTemplate());
				  	} else {
				  		def = new Def_ModulePar_Template($i2.identifier, $type, null);
				  	}
				  	def.setLocation(getLocation( $i2.start, endcol));
					$parameters.add(def);
				}
			}
	)*
;

pr_ModuleControlPart returns [ControlPart controlpart]
@init {
	$controlpart = null;
	Token endcol = null;
	StatementBlock statementblock = null;
	MultipleWithAttributes attributes = null;
}:
(	col = pr_ControlKeyword
	blockstart = pr_BeginChar
	b = pr_ModuleControlBody { statementblock = $b.block; }
	blockend = pr_EndChar { endcol = $blockend.stop; }
	( a = pr_WithStatement { endcol = $a.stop; attributes = $a.attributes; } )?
	( endb = pr_SemiColon{ endcol = $endb.stop; } )?
)
{
	if(statementblock == null) {
		statementblock = new StatementBlock();
	}
	statementblock.setLocation(getLocation( $blockstart.start, $blockend.stop));
	$controlpart = new ControlPart(statementblock);
	$controlpart.setLocation(getLocation( $col.start, endcol));
	Location commentLocation = lexer.getLastCommentLocation();
	$controlpart.setCommentLocation(commentLocation);
	if (commentLocation != null) {
		lexer.clearLastCommentLocation();
	}
	$controlpart.setWithAttributes(attributes);
};

pr_ControlKeyword:
	CONTROL
;

pr_ModuleControlBody returns [StatementBlock block]
@init {
	$block = null;
}:
	( b = pr_ControlStatementOrDefList { $block = $b.statementblock; } )?
{
	if ( $block == null ) { $block = new StatementBlock(); }
};

pr_ControlStatementOrDefList returns [StatementBlock statementblock]:
(	s = pr_ControlStatementOrDef
	pr_SemiColon?
	(	s2 = pr_ControlStatementOrDef
			{
				if ( $s.statements != null && $s2.statements != null ) {
					$s.statements.addAll( $s2.statements );
				}
			}
		pr_SemiColon?
	)*
)
{
	$statementblock = new StatementBlock();
	if($s.statements != null) {
		for(Statement statement: $s.statements) {
			$statementblock.addStatement(statement);
		}
	}
};

pr_ControlStatementOrDef returns [List<Statement> statements]
@init {
	List<Definition> definitions = null;
	Statement statement = null;
}:
(	d1 = pr_FunctionLocalInst { definitions = $d1.definitions; }
|	s = pr_ControlStatement { statement = $s.statement;}
|	d2 = pr_FunctionLocalDef { definitions = $d2.definitions; }
)
{
	$statements = new ArrayList<Statement>();
	if(definitions != null) {
		for(Definition definition : definitions) {
			if(definition != null) {
				definition.setCumulativeDefinitionLocation(getLocation( $start, getStopToken()));
				Statement temp_statement = new Definition_Statement(definition);
				temp_statement.setLocation(getLocation( $start, getStopToken()));
				$statements.add(temp_statement);
			}
		}
	} else if(statement != null) {
		$statements.add(statement);
	}
};

pr_ControlStatement returns[Statement statement]
@init {
	$statement = null;
}:
(	s1 = pr_BasicStatements { $statement = $s1.statement; }
|	s2 = pr_SUTStatements { $statement = $s2.statement; }
|	s3 = pr_TimerStatements { $statement = $s3.statement; }
|	s4 = pr_BehaviourStatements { $statement = $s4.statement; }
|	STOP	{	$statement = new Stop_Execution_Statement();
				$statement.setLocation(getLocation( $start, getStopToken())); }
);

pr_VarInstance returns[List<Definition> definitions]
@init {
	$definitions = new ArrayList<Definition>();
	List<Identifier> identifiers = null;
	TemplateRestriction.Restriction_type templateRestriction = TemplateRestriction.Restriction_type.TR_NONE;
}:
(	col = pr_VarKeyword
	(	tr = pr_TemplateOptRestricted { templateRestriction = $tr.templateRestriction; }
		t = pr_Type
		pr_TempVarList[ $definitions, $t.type, templateRestriction ]
	|	t2 = pr_Type
		pr_VarList[ $definitions, $t2.type ]
	)
)
{
	if ( $definitions.size() > 0 ) {
		// the location of "var [[restr]template] Type" part belongs to the first variable, no location overlapping
		Definition def = $definitions.get(0);
		if (def!=null) {
			final Token t = $col.start;
			def.getLocation().setLine( t.getLine() + line - 1);
			Location loc = def.getLocation();
			loc.setOffset( offset + t.getStartIndex() );
		}
	}
};

pr_TempVarList [List<Definition> definitions, Type type, TemplateRestriction.Restriction_type templateRestriction]:
(	pr_SingleTempVarInstance[definitions, type, templateRestriction]
	(	pr_Comma
		pr_SingleTempVarInstance[definitions, type, templateRestriction]
	)*
);

pr_SingleTempVarInstance [List<Definition> definitions, Type type, TemplateRestriction.Restriction_type templateRestriction]
@init {
	TemplateBody body = null;
	ArrayDimensions dimensions = null;
}:
(	i = pr_Identifier
	( d = pr_ArrayDef { dimensions = $d.dimensions; } )?
	(	pr_AssignmentChar
		b = pr_TemplateBody { body = $b.body; }
	)?
)
{
	if($i.identifier != null) {
		Type tempType = $type;
		if (dimensions != null) {
			for (int i = dimensions.size() - 1; i >= 0; i--) {
				tempType = new Array_Type(tempType, dimensions.get(i), false);
				tempType.setLocation(getLocation( $d.start, $d.stop));
			}
		}

		Definition definition = new Def_Var_Template( $templateRestriction, $i.identifier, tempType, body != null ? body.getTemplate() : null );
		definition.setLocation(getLocation( $start, getStopToken()));
		$definitions.add(definition);
	}
};


pr_VarList[List<Definition> definitions, Type type]:
(	d = pr_SingleVarInstance[type] { if($d.definition != null) { $definitions.add($d.definition); } }
	(	pr_Comma
			d = pr_SingleVarInstance[type] { if($d.definition != null) { $definitions.add($d.definition); } }
	)*
);

pr_SingleVarInstance[Type type] returns[Def_Var definition]
@init {
	$definition = null;
	Value value = null;
	ArrayDimensions dimensions = null;
}:
(	i = pr_Identifier
	(	d = pr_ArrayDef { dimensions = $d.dimensions; })?
	(	pr_AssignmentChar
		v = pr_VarInitialValue { value = $v.value; }
	)?
)
{
	if ($i.identifier != null) {
		Type type2 = type;
		if (dimensions != null) {
			for (int i = dimensions.size() - 1; i >= 0; i--) {
				type2 = new Array_Type(type2, dimensions.get(i), false);
				type2.setLocation(getLocation( $d.start, $d.stop));
			}
		}
		$definition = new Def_Var($i.identifier, type2, value);
		$definition.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_VarInitialValue returns[Value value]
@init {
	$value = null;
}:
	v = pr_Expression { $value = $v.value; }
;

pr_VarKeyword:
	VAR
;

pr_VariableRef returns[Reference reference]
@init {
	$reference = null;
	List<ISubReference> subReferences = null;
}:
(	r = pr_ValueReference { $reference = $r.reference; }
	( sr = pr_ExtendedFieldReference { subReferences = $sr.subReferences; } )?
)
{
	if(subReferences != null && $reference != null) {
		for(ISubReference subReference: subReferences) {
			$reference.addSubReference(subReference);
		}
	}
};

pr_TimerInstance returns[List<Definition> definitions = null]:
(	col = pr_TimerKeyword
	d = pr_TimerList { $definitions = $d.definitions; }
)
{
	if ( $definitions.size() > 0 ) {
		Definition tdef = $definitions.get(0);
		if ( tdef != null ) {
			final Token t = $col.start;
			tdef.getLocation().setLine( line - 1 + t.getLine() );
			Location loc = tdef.getLocation();
			loc.setOffset( offset + t.getStartIndex() );
		}
	}
};

pr_TimerList returns[List<Definition> definitions]
@init {
	$definitions = new ArrayList<Definition>();
}:
(	d = pr_SingleTimerInstance { if($d.def_timer != null) { $definitions.add($d.def_timer); } }
	(	pr_Comma
		d = pr_SingleTimerInstance { if($d.def_timer != null) { $definitions.add($d.def_timer); } }
	)*
);

pr_SingleTimerInstance returns[Def_Timer def_timer]
@init {
	$def_timer = null;
	ArrayDimensions dimensions = null;
	Value value = null;
}:
(   i = pr_Identifier
	(	d = pr_ArrayDef { dimensions = $d.dimensions; } )?
	(	pr_AssignmentChar
		v = pr_TimerValue { value = $v.value; }
	)?
)
{
	if($i.identifier != null) {
		$def_timer = new Def_Timer($i.identifier, dimensions, value);
		$def_timer.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_TimerKeyword:
	TIMER
;

pr_TimerValue returns[Value value]
@init {
	$value = null;
}:
	v = pr_Expression { $value = $v.value; }
;

pr_TimerRef returns[Reference reference]
@init {
	$reference = null;
}:
	r = pr_VariableRef { $reference = $r.reference; }
;

pr_ConfigurationStatements returns[Statement statement]
@init {
	$statement = null;
	TemplateInstance doneMatch = null;
	Reference reference = null;
}:
(	s1 = pr_ConnectStatement	{ $statement = $s1.statement; }
|	s2 = pr_MapStatement		{ $statement = $s2.statement; }
|	s3 = pr_DisconnectStatement	{ $statement = $s3.statement; }
|	s4 = pr_UnmapStatement		{ $statement = $s4.statement; }
|	(	componentValue = pr_ComponentId
		pr_Dot
		(	k = pr_KilledKeyword
				{
					//pr_KilledStatement
					$statement = new Killed_Statement( $componentValue.value );
					$statement.setLocation(getLocation( $componentValue.start, $k.stop));
				}
		|	pr_DoneKeyword	//pr_DoneStatement
			(	pr_LParen
				t = pr_TemplateInstance { doneMatch = $t.templateInstance; }
				pr_RParen
			)?
			(	col = pr_PortRedirectSymbol
				r = pr_ValueSpec { reference = $r.reference; }
			)?
				{
					$statement = new Done_Statement($componentValue.value, doneMatch, reference);
					$statement.setLocation(getLocation( $componentValue.start, getStopToken()));
				}
		)
	)
|	s5 = pr_StopTCStatement		{ $statement = $s5.statement; }
|	s6 = pr_KillTCStatement		{ $statement = $s6.statement; }
|	s7 = pr_StartTCStatement	{ $statement = $s7.statement; }
);

pr_ConfigurationOps returns[Value value]
@init {
	$value = null;
}:
(	v1 = pr_SelfOp { $value = $v1.value; }
|	v2 = pr_SystemOp { $value = $v2.value; }
|	v3 = pr_MTCOp { $value = $v3.value; }
);

pr_CreateOpEnd	[Reference temporalReference]
	returns [ComponentCreateExpression value]
@init {
	$value = null;
	Token endcol = null;
	Value name = null;
	Value location = null;
	boolean isAlive = false;
}:
(	col = pr_Dot
	a = pr_CreateKeyword { endcol = $a.stop; }
	(	pr_LParen
		(	pr_NotUsedSymbol
			pr_Comma
			l = pr_SingleExpression { location = $l.value; }
		|	n = pr_SingleExpression { name = $n.value; }
			(	pr_Comma
				l2 = pr_SingleExpression { location = $l2.value; }
			)?
		)
		b = pr_RParen { endcol = $b.stop; }
	)?
	(	c = ALIVE { endcol = $c; isAlive = true; }	)?
)
{
	$value = new ComponentCreateExpression($temporalReference, name, location, isAlive);
	$value.setLocation( getLocation( $temporalReference.getLocation(), endcol ) );
};

pr_SystemOp returns[SystemComponentExpression value]
@init {
	$value = null;
}:
	SYSTEM
{
	$value = new SystemComponentExpression();
	$value.setLocation(getLocation( $SYSTEM));
};

pr_SelfOp returns[SelfComponentExpression value]
@init {
	$value = null;
}:
	SELF
{
	$value = new SelfComponentExpression();
	$value.setLocation(getLocation( $SELF));
};

pr_MTCOp returns[MTCComponentExpression value]
@init {
	$value = null;
}:
	MTC
{
	$value = new MTCComponentExpression();
	$value.setLocation(getLocation( $MTC));
};

pr_KillTCStatement returns[Kill_Statement statement]
@init {
	$statement = null;
	Value value = null;
}:
(	(	(	v = pr_ComponentReferenceOrLiteral { value = $v.value; }
		|	b = pr_AllKeyword pr_ComponentKeyword
		)
		pr_Dot
	)?
	endcol = pr_KillKeyword
)
{
	$statement = new Kill_Statement(value);
	$statement.setLocation(getLocation( $start, $endcol.stop));
};

pr_KilledKeyword:
	KILLED
;

pr_KillKeyword:
	KILL
;

pr_ComponentId returns[Value value]
@init {
	$value = null;
}:
(	(	pr_AnyKeyword
	|	pr_AllKeyword
	)
	pr_ComponentKeyword
|	v = pr_ComponentOrDefaultReference { $value = $v.value; }
);

pr_ComponentOrDefaultReference returns[Value value]
@init {
	$value = null;
}:
(	(	r = pr_FunctionInstance
			{	$value = new Referenced_Value( $r.temporalReference );
				$value.setLocation( getLocation( $r.start, $r.stop ) );
			}
		( p = pr_ApplyOpEnd
			{	$value = new ApplyExpression( $value, $p.parsedParameters );
				$value.setLocation(getLocation( $r.start, $p.stop ) );
			}
		)?
	)
|	(	r2 = pr_VariableRef
			{	$value = new Referenced_Value( $r2.reference );
				$value.setLocation( getLocation( $r2.start, $r2.stop ) );
			}
		( p2 = pr_ApplyOpEnd
			{	$value = new ApplyExpression( $value, $p2.parsedParameters );
				$value.setLocation(getLocation( $r2.start, $p2.stop ) );
			}
		)*
	)
);

pr_DoneKeyword:
	DONE
;

pr_CreateKeyword:
	CREATE
;

pr_ConnectStatement returns[Connect_Statement statement]
@init {
	$statement = null;
}:
(	col = pr_ConnectKeyword
	h = pr_SingleConnectionSpec
)
{
	if($h.helper != null) {
		$statement = new Connect_Statement(	$h.helper.componentReference1, new PortReference($h.helper.portReference1),
											$h.helper.componentReference2, new PortReference($h.helper.portReference2) );
		$statement.setLocation(getLocation( $col.start, $h.stop));
	}
};

pr_SingleConnectionSpec returns[Connection_Helper helper]
@init {
	$helper = null;
}:
(	pr_LParen
	h1 = pr_PortRef
	pr_Comma
	h2 = pr_PortRef
	pr_RParen
)
{
	if($h1.helper != null && $h2.helper != null) {
		$helper = new Connection_Helper($h1.helper, $h2.helper);
	}
};

pr_ConnectKeyword:
	CONNECT
;

pr_PortRef returns[PortReference_Helper helper]
@init {
	$helper = null;
}:
(	c = pr_ComponentRef
	pr_Colon
	p = pr_Port
)
{
	if($c.value != null && $p.reference != null) {
		$helper = new PortReference_Helper($c.value, $p.reference);
	}
};

pr_ComponentRef returns[Value value]
@init {
	$value = null;
}:
(	v1 = pr_ComponentOrDefaultReference { $value = $v1.value; }
|	v2 = pr_SystemOp { $value = $v2.value; }
|	v3 = pr_SelfOp { $value = $v3.value; }
|	v4 = pr_MTCOp { $value = $v4.value; }
);

pr_DisconnectStatement returns[Disconnect_Statement statement]
@init {
	$statement = null;
	Connection_Helper helper = null;
}:
(	col = pr_DisconnectKeyword
	(	h = pr_SingleOrMultiConnectionSpec { helper = $h.helper; }
	|	{	reportUnsupportedConstruct( "Disconnect operation on multiple connections is not currently supported", $col.start, $col.stop );	}
	)
)
{
	if(helper != null && helper.componentReference1 != null &&
		helper.portReference1 != null && helper.componentReference2 != null &&
		helper.portReference2 != null) {
		$statement = new Disconnect_Statement(helper.componentReference1, new PortReference(helper.portReference1), helper.componentReference2, new PortReference(helper.portReference2));
		$statement.setLocation(getLocation( $col.start, $h.stop));
	} else {
		reportUnsupportedConstruct( "Disconnect operation on multiple connections is not currently supported", $col.start, $col.stop );
	}
};

pr_DisconnectKeyword:
	DISCONNECT
;

pr_SingleOrMultiConnectionSpec returns[Connection_Helper helper]
@init {
	$helper = null;
}:
(	h1 = pr_SingleConnectionSpec { $helper = $h1.helper; }
|	h2 = pr_AllConnectionsSpec { $helper = $h2.helper; }
|	h3 = pr_AllPortsSpec { $helper = $h3.helper; }
|	h4 = pr_AllCompsAllPortsSpec { $helper = $h4.helper; }
);

pr_AllConnectionsSpec returns[Connection_Helper helper]
@init {
	$helper = null;
}:
(	pr_LParen
	h = pr_PortRef
	pr_RParen
)
{
	$helper = new Connection_Helper($h.helper, new PortReference_Helper(null, null));
};

pr_AllPortsSpec returns[Connection_Helper helper]
@init {
	$helper = null;
}:
(	pr_LParen
	v = pr_ComponentRef
	pr_Colon
	pr_AllKeyword
	pr_PortKeyword
	pr_RParen
)
{
	$helper = new Connection_Helper( new PortReference_Helper($v.value, null), new PortReference_Helper(null, null));
};

pr_AllCompsAllPortsSpec returns[Connection_Helper helper]
@init {
	$helper = null;
}:
(	pr_LParen
	pr_AllKeyword
	pr_ComponentKeyword
	pr_Colon
	pr_AllKeyword
	pr_PortKeyword
	pr_RParen
)
{
	$helper = new Connection_Helper( new PortReference_Helper(null, null), new PortReference_Helper(null, null));
};

pr_MapStatement returns[Map_Statement statement]
@init {
	$statement = null;
}:
(	col = pr_MapKeyword
	h = pr_SingleConnectionSpec
)
{
	if($h.helper != null) {
		$statement = new Map_Statement( $h.helper.componentReference1, new PortReference($h.helper.portReference1),
									   $h.helper.componentReference2, new PortReference($h.helper.portReference2) );
		$statement.setLocation(getLocation( $col.start, $h.stop));
	}
};

pr_MapKeyword:
	MAP
;

pr_UnmapStatement returns[Unmap_Statement statement]
@init {
	$statement = null;
	Connection_Helper helper = null;
}:
(	col = pr_UnmapKeyword
	(	h = pr_SingleOrMultiConnectionSpec { helper = $h.helper; }
	|	{	reportUnsupportedConstruct( "Unmap operation on multiple mappings is not currently supported", $col.start, $col.stop );	}
	)
)
{
	if(helper != null && helper.componentReference1 != null &&
		helper.portReference1 != null && helper.componentReference2 != null &&
		helper.portReference2 != null) {
		$statement = new Unmap_Statement(helper.componentReference1, new PortReference(helper.portReference1), helper.componentReference2, new PortReference(helper.portReference2));
		$statement.setLocation(getLocation( $col.start, $h.stop));
	} else {
		reportUnsupportedConstruct( "Unmap operation on multiple mappings is not currently supported", $col.start, $col.stop );
	}
};

pr_UnmapKeyword:
	UNMAP
;

pr_StartTCStatement returns[Statement statement]
@init {
	$statement = null;
	Value component = null;
	Value dereferredValue = null;
	Reference functionref = null;
	ParsedActualParameters parameters = null;
}:
(	c = pr_ComponentOrDefaultReference { component = $c.value; }
	pr_Dot
	START
	pr_LParen
	(	(	dv = pr_DereferOp { dereferredValue = $dv.value; }
			a1=pr_LParen
			( p = pr_FunctionActualParList { parameters = $p.parsedParameters; } )?
			a2=pr_RParen
			{	if(parameters == null) { parameters = new ParsedActualParameters();	}
				parameters.setLocation(getLocation( $a1.start, $a2.stop));
				$statement = new Start_Referenced_Component_Statement( component, dereferredValue, parameters );
			}
		)		
		|	f = pr_FunctionInstance
			{
				functionref = $f.temporalReference;
				$statement = new Start_Component_Statement(component, functionref);
			}
	)
	endcol = pr_RParen
)
{
	if($statement != null) {
		$statement.setLocation(getLocation( $c.start, $endcol.stop));
	}
};

pr_StopTCStatement returns[Statement statement]
@init {
	$statement = null;
	Value componentRef = null;
	boolean all_component = false;
}:
(	(	(	v = pr_ComponentReferenceOrLiteral { componentRef = $v.value; }
		|	pr_AllKeyword pr_ComponentKeyword {all_component = true;}
		)
		pr_Dot
	)?
	endcol = STOP
)
{
	if (componentRef != null || all_component) {
		$statement = new Stop_Component_Statement(componentRef);
		$statement.setLocation(getLocation( $start, $endcol));
	} else {
		$statement = new Stop_Execution_Statement();
		$statement.setLocation(getLocation( $endcol));
	}
};

pr_ComponentReferenceOrLiteral returns[Value value]
@init {
	$value = null;
}://addtest
(	v1 = pr_ComponentOrDefaultReference { $value = $v1.value; }
|	v2 = pr_MTCOp { $value = $v2.value; }
|	v3 = pr_SelfOp { $value = $v3.value; }
);

pr_Port returns[Reference reference]
@init {
	$reference = null;
}:
	r = pr_VariableRef { $reference = $r.reference; }
;

pr_CommunicationStatements returns[Statement statement]
@init {
	$statement = null;
}:
(	(	r = pr_Port
		pr_Dot
		(	s1 = pr_PortSendOp[$r.reference]				{ $statement = $s1.statement; }	//pr_SendStatement
		|	s2 = pr_PortCallOp[$r.reference]				{ $statement = $s2.statement; }	//pr_CallStatement
		|	s3 = pr_PortReplyOp[$r.reference]				{ $statement = $s3.statement; }	//pr_ReplyStatement
		|	s4 = pr_PortRaiseOp[$r.reference]				{ $statement = $s4.statement; }	//pr_RaiseStatement
		|	s5 = pr_PortReceiveOp[$r.reference, false]		{ $statement = $s5.statement; }	//pr_ReceiveStatement
		|	s6 = pr_PortTriggerOp[$r.reference]				{ $statement = $s6.statement; }	//pr_TriggerStatement
		|	s7 = pr_PortGetCallOp[$r.reference, false]		{ $statement = $s7.statement; }	//pr_GetCallStatement
		|	s8 = pr_PortGetReplyOp[$r.reference, false]		{ $statement = $s8.statement; }	//pr_GetReplyStatement
		|	s9 = pr_PortCatchOp[$r.reference, false]		{ $statement = $s9.statement; }	//pr_CatchStatement
		|	s10 = pr_PortCheckOp[$r.reference]				{ $statement = $s10.statement; }	//pr_CheckStatement
		|	CLEAR	{ $statement = new Clear_Statement($r.reference); } //pr_ClearStatement
		|	START 	{ $statement = new Start_Port_Statement($r.reference); } //pr_StartStatement
		|	STOP 	{ $statement = new Stop_Port_Statement($r.reference); } //pr_StopStatement
		|	HALT 	{ $statement = new Halt_Statement($r.reference); } //pr_HaltStatement
		)
	)
|	(	pr_AnyKeyword
		pr_PortKeyword
		pr_Dot
		(	s11 = pr_PortReceiveOp[null, false]		{ $statement = $s11.statement; }	//pr_ReceiveStatement
		|	s12 = pr_PortTriggerOp[null]			{ $statement = $s12.statement; }	//pr_TriggerStatement
		|	s13 = pr_PortGetCallOp[null, false]		{ $statement = $s13.statement; }	//pr_GetCallStatement
		|	s14 = pr_PortGetReplyOp[null, false]	{ $statement = $s14.statement; }	//pr_GetReplyStatement
		|	s15 = pr_PortCatchOp[null, false]		{ $statement = $s15.statement; }	//pr_CatchStatement
		|	s16 = pr_PortCheckOp[null]				{ $statement = $s16.statement; }	//pr_CheckStatement
		)
	)
|	(	pr_AllKeyword
		pr_PortKeyword
		pr_Dot
		(	CLEAR 	{ $statement = new Clear_Statement(null); }			//pr_ClearStatement
		|	START	{ $statement = new Start_Port_Statement(null); }	//pr_StartStatement
		|	STOP 	{ $statement = new Stop_Port_Statement(null); }		//pr_StopStatement
		|	HALT 	{ $statement = new Halt_Statement(null); }			//pr_HaltStatement
		)
	)
)
{
	if( $statement != null ) {
		$statement.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_PortSendOp [Reference reference]
	returns[Statement statement]
@init {
	$statement = null;
	TemplateInstance parameter = null;
	IValue toClause = null;
}:
(	col = pr_SendOpKeyword
	pr_LParen
	p = pr_SendParameter { parameter = $p.templateInstance; }
	a = pr_RParen
	( t = pr_ToClause { toClause = $t.value; } )?
)
{
	$statement = new Send_Statement($reference, parameter, toClause);
};

pr_SendOpKeyword:
	SEND
;

pr_SendParameter returns[TemplateInstance templateInstance]
@init {
	$templateInstance = null;
}:
	t = pr_TemplateInstance { $templateInstance = $t.templateInstance; }
;

pr_ToClause returns[IValue value]
@init {
	$value = null;
}:
(	a = pr_ToKeyword
	(	b = pr_AddressRef
			{	TemplateInstance templateInstance = $b.templateInstance;
				if ( templateInstance != null ) {
					ITTCN3Template template = templateInstance.getTemplateBody();
					if ( templateInstance.getType() == null && templateInstance.getDerivedReference() == null && template.getValue() != null ) {
					$value = template.getValue();
				} else {
					reportUnsupportedConstruct( "Multicast communication is not currently supported", $b.start, $b.stop );
				}
			}
			}
//	|	c = pr_AddressRefLis // covered by the previous rule
	|	d = pr_AllKeyword
		e = pr_ComponentKeyword
			{	reportUnsupportedConstruct( "Broadcast communication is not currently supported", $d.start, $e.stop );	}
	)
);

pr_ToKeyword:
	TO
;

pr_AddressRef returns[TemplateInstance templateInstance]
@init {
	$templateInstance = null;
}:
(	t = pr_TemplateInstance { $templateInstance = $t.templateInstance; }
);

pr_PortCallOp [Reference reference]
	returns[Statement statement]
@init {
	$statement = null;
	AltGuards altGuards = null;
	Value timerValue = null;
	boolean noWait = false;
	IValue toClause = null;
}:
(	col = pr_CallOpKeyword
	pr_LParen
	t = pr_TemplateInstance
	(	pr_Comma
		(	tv = pr_TimerValue { timerValue = $tv.value; }
		|	pr_NowaitKeyword { noWait = true; }
		)  // pr_CallTimerValue
	)? // pr_CallParameters
	pr_RParen
	( tc = pr_ToClause { toClause = $tc.value; } )?
	(	pr_BeginChar
		a = pr_CallBodyStatementList { altGuards = $a.altGuards; }
		pr_EndChar
	)? // pr_PortCallBody
)
{
	$statement = new Call_Statement($reference, $t.templateInstance, timerValue, noWait, toClause, altGuards);
};

pr_CallOpKeyword:
	CALL
;

pr_NowaitKeyword:
	NOWAIT
;

pr_CallBodyStatementList returns[AltGuards altGuards]
@init {
	$altGuards = new AltGuards();
}:
(	( a = pr_CallBodyStatement { $altGuards.addAltGuard($a.altGuard); } )+
);

pr_CallBodyStatement returns[AltGuard altGuard]
@init {
	$altGuard = null;
	StatementBlock statementBlock = null;
}:
(	v = pr_AltGuardChar
	s = pr_CallBodyOps
	(	pr_SemiColon?
		sb = pr_StatementBlock { statementBlock = $sb.statementblock; }
	|	pr_SemiColon
	)
	pr_SemiColon?
)
{
	$altGuard = new Operation_Altguard($v.value, $s.statement, statementBlock);
	$altGuard.setLocation(getLocation( $v.start, getStopToken()));
};

pr_CallBodyOps returns[Statement statement]
@init {
	$statement = null;
}:
(	r = pr_PortOrAny
	pr_Dot
	(	s = pr_PortGetReplyOp[$r.reference, false] { $statement = $s.statement; } //pr_GetReplyStatement
	|	s2 = pr_PortCatchOp[$r.reference, false] { $statement = $s2.statement; }  //pr_CatchStatement
	)
);

pr_PortReplyOp [Reference reference]
	returns[Statement statement]
@init {
	$statement = null;
	IValue toClause = null;
	Value replyValue = null;
}:
(	pr_ReplyKeyword
	pr_LParen
	parameter = pr_TemplateInstance
	( rv = pr_ReplyValue { replyValue = $rv.value; } )?
	pr_RParen
	( tc = pr_ToClause { toClause = $tc.value; } )?
)
{
	$statement = new Reply_Statement($reference, $parameter.templateInstance, replyValue, toClause);
	$statement.setLocation(getLocation( $start, getStopToken()));
};


pr_ReplyKeyword:
	REPLY
;

pr_ReplyValue returns[Value value]
@init {
	$value = null;
}:
(	pr_ValueKeyword
	v = pr_Expression { $value = $v.value; }
);

pr_PortRaiseOp [Reference reference]
	returns[Statement statement]
@init {
	$statement = null;
	IValue toClause = null;
}:
(	pr_RaiseKeyword
	pr_LParen
	signature = pr_Signature
	pr_Comma
	parameter = pr_TemplateInstance
	pr_RParen
	( tc = pr_ToClause { toClause = $tc.value; } )?
)
{
	$statement = new Raise_Statement($reference, $signature.reference, $parameter.templateInstance, toClause);
	$statement.setLocation(getLocation( $start, getStopToken()));
};

pr_RaiseKeyword:
	RAISE
;

pr_PortOrAny returns[Reference reference]
@init {
	$reference = null;
}:
(	r = pr_Port { $reference = $r.reference; }
|	pr_AnyKeyword
	pr_PortKeyword
);

pr_PortReceiveOp [Reference reference, boolean is_check]
	returns[Statement statement]
@init {
	$statement = null;
	TemplateInstance parameter = null;
	TemplateInstance from = null;
	PortRedirect_Helper helper = null;
}:
(	pr_ReceiveOpKeyword
	(	pr_LParen
		t = pr_TemplateInstance { parameter = $t.templateInstance; }
		pr_RParen
	)?
	( fc = pr_FromClause { from = $fc.templateInstance; } )?
	( h = pr_PortRedirect { helper = $h.helper; } )?
)
{
	if( helper == null ) {
		if( $is_check ) {
			$statement = new Check_Receive_Port_Statement( $reference, parameter, from, null, null );
		} else {
			$statement = new Receive_Port_Statement( $reference, parameter, from, null, null );
		}
	} else {
		if( $is_check ) {
			$statement = new Check_Receive_Port_Statement( $reference, parameter, from, helper.redirectValue, helper.redirectSender );
		} else {
			$statement = new Receive_Port_Statement( $reference, parameter, from, helper.redirectValue, helper.redirectSender );
		}
	}
	$statement.setLocation(getLocation( $start, getStopToken()));
};

pr_ReceiveOpKeyword:
	RECEIVE
;

pr_FromClause returns[TemplateInstance templateInstance]
@init {
	$templateInstance = null;
}:
(	pr_FromKeyword
	t = pr_AddressRef { $templateInstance = $t.templateInstance; }
);

pr_FromKeyword:
	FROM
;

pr_PortRedirect returns[PortRedirect_Helper helper]
@init {
	$helper = null;
	Reference value = null;
	Reference sender = null;
}:
(	pr_PortRedirectSymbol
	(	vs = pr_ValueSpec { value = $vs.reference; }
		( ss = pr_SenderSpec { sender = $ss.reference; } )?
	|	ss2 = pr_SenderSpec { sender = $ss2.reference; }
	)
)
{
	$helper = new PortRedirect_Helper(value, sender);
};

pr_PortRedirectSymbol:
	PORTREDIRECTSYMBOL
;

pr_ValueSpec returns[Reference reference]
@init {
	$reference = null;
}:
(	pr_ValueKeyword
	r = pr_VariableRef { $reference = $r.reference; }
);

pr_ValueKeyword returns[String stringValue]:
	VALUE
{
	$stringValue = $VALUE.getText();
};

pr_SenderSpec returns[Reference reference]
@init {
	$reference = null;
}:
(	pr_SenderKeyword
	r = pr_VariableRef { $reference = $r.reference; }
);

pr_SenderKeyword:
	SENDER
;

pr_PortTriggerOp [Reference reference]
	returns[Trigger_Port_Statement statement]
@init {
	$statement = null;
	TemplateInstance parameter = null;
	TemplateInstance from = null;
	PortRedirect_Helper helper = null;
}:
(	col = TRIGGER
	(	pr_LParen
		p = pr_TemplateInstance { parameter = $p.templateInstance; }
		pr_RParen
	)?
	( f = pr_FromClause { from = $f.templateInstance; } )?
	( h = pr_PortRedirect	{ helper = $h.helper; } )?
)
{
	if(helper == null) {
		$statement = new Trigger_Port_Statement(reference, parameter, from, null, null);
	} else {
		$statement = new Trigger_Port_Statement(reference, parameter, from, helper.redirectValue, helper.redirectSender);
	}
	$statement.setLocation(getLocation( $start, getStopToken()));
};

pr_PortGetCallOp [Reference reference, boolean is_check]
	returns[Statement statement]
@init {
	$statement = null;
	TemplateInstance parameter = null;
	TemplateInstance from = null;
	Redirection_Helper helper = null;
}:
(	GETCALL
	(	pr_LParen
		t = pr_TemplateInstance { parameter = $t.templateInstance; }
		pr_RParen
	)?
	( fc = pr_FromClause { from = $fc.templateInstance; } )?
	( h = pr_PortRedirectWithParam { helper = $h.helper; } )?
)
{
	if(helper == null) {
		if($is_check) {
			$statement = new Check_Getcall_Statement($reference, parameter, from, null, null);
		} else {
			$statement = new Getcall_Statement($reference, parameter, from, null, null);
		}
	} else {
		if($is_check) {
			$statement = new Check_Getcall_Statement($reference, parameter, from, helper.redirectParameters, helper.senderReference);
		} else {
			$statement = new Getcall_Statement($reference, parameter, from, helper.redirectParameters, helper.senderReference);
		}
	}
	$statement.setLocation(getLocation( $start, getStopToken()));
};

pr_PortRedirectWithParam returns[Redirection_Helper helper]:
(	pr_PortRedirectSymbol
	h = pr_RedirectWithParamSpec { $helper = $h.helper; }
);

pr_RedirectWithParamSpec returns[Redirection_Helper helper]
@init {
	$helper = new Redirection_Helper(null, null, null);
	Parameter_Redirect redirectParameters = null;
	Reference sender_reference = null;
}:
(	r = pr_ParamSpec { redirectParameters = $r.redirect; }
	( s = pr_SenderSpec { sender_reference = $s.reference; } )?
|	s = pr_SenderSpec { sender_reference = $s.reference; }
)
{
	$helper.redirectParameters = redirectParameters;
	$helper.senderReference = sender_reference;
};

pr_ParamSpec returns[Parameter_Redirect redirect]
@init {
	$redirect = null;
}:
(	PARAM
	r = pr_ParamAssignmentList { $redirect = $r.redirect; }
);

pr_ParamAssignmentList returns[Parameter_Redirect redirect]
@init {
	$redirect = null;
}:
(	col = pr_LParen
	(	assignments = pr_AssignmentList	{ $redirect = new AssignmentList_Parameter_Redirect($assignments.parameterAssignments); }
	|	entries = pr_VariableList	{ $redirect = new VariableList_Parameter_Redirect($entries.entries); }
	)
	endcol = pr_RParen
)
{
	if($redirect != null) {
		$redirect.setLocation(getLocation( $col.start, $endcol.stop));
	}
};

pr_AssignmentList returns[Parameter_Assignments parameterAssignments]
@init {
	$parameterAssignments = null;
}:
(	p = pr_VariableAssignment
		{	$parameterAssignments = new Parameter_Assignments();
			if( $p.param_assignment != null ) { $parameterAssignments.add( $p.param_assignment ); }
		}
	(	pr_Comma
		p = pr_VariableAssignment
			{ if( $p.param_assignment != null ) { $parameterAssignments.add( $p.param_assignment ); } }
	)*
);

pr_VariableAssignment returns[Parameter_Assignment param_assignment]
@init {
	$param_assignment = null;
}:
(	r = pr_VariableRef
	pr_AssignmentChar
	i = pr_Identifier
)
{
	$param_assignment = new Parameter_Assignment($r.reference, $i.identifier);
	$param_assignment.setLocation(getLocation( $r.start, $i.stop));
};

pr_PortRedirectWithValueAndParam returns[Redirection_Helper helper]
@init {
	$helper = null;
}:
(	pr_PortRedirectSymbol
	h = pr_RedirectWithValueAndParamSpec { $helper = $h.helper; }
);

pr_RedirectWithValueAndParamSpec returns[Redirection_Helper helper]
@init {
	$helper = null;
	Parameter_Redirect redirect = null;
	Reference sender = null;
}:
(	vs = pr_ValueSpec
	( r = pr_ParamSpec { redirect = $r.redirect; } )?
	( s = pr_SenderSpec { sender = $s.reference; } )?
		{ $helper = new Redirection_Helper($vs.reference, redirect, sender); }
|	h = pr_RedirectWithParamSpec	{ $helper = $h.helper;}
);

pr_VariableList returns[Variable_Entries entries]
@init {
	$entries = null;
}:
(	e = pr_VariableEntry	{ $entries = new Variable_Entries(); if( $e.entry != null ) { $entries.add( $e.entry ); } }
	(	pr_Comma
		e = pr_VariableEntry	{ if ( $e.entry != null ) { $entries.add( $e.entry ); } }
	)*
);

pr_VariableEntry returns[Variable_Entry entry]
@init {
	$entry = null;
	Reference reference = null;
}:
(	r = pr_VariableRef { reference = $r.reference; }
|	pr_NotUsedSymbol
)
{
	$entry = new Variable_Entry(reference);
	$entry.setLocation(getLocation( $start, getStopToken()));
};

pr_PortGetReplyOp [Reference reference, boolean is_check]
	returns[Statement statement]
@init {
	$statement = null;
	TemplateInstance parameter = null;
	TemplateInstance valueMatch = null;
	TemplateInstance from = null;
	Redirection_Helper helper = null;
}:
(	pr_GetReplyOpKeyword
	(	pr_LParen
		t = pr_TemplateInstance { parameter = $t.templateInstance; }
		( t2 = pr_ValueMatchSpec { valueMatch = $t2.templateInstance; } )?
		pr_RParen
	)?
	( fc = pr_FromClause { from = $fc.templateInstance; } )?
	( h = pr_PortRedirectWithValueAndParam { helper = $h.helper; } )?
)
{
	if(helper == null) {
		if($is_check) {
			$statement = new Check_Getreply_Statement($reference, parameter, valueMatch, from, null, null, null);
		} else {
			$statement = new Getreply_Statement($reference, parameter, valueMatch, from, null, null, null);
		}
	} else {
		if($is_check) {
			$statement = new Check_Getreply_Statement($reference, parameter, valueMatch, from, helper.redirectValue, helper.redirectParameters, helper.senderReference);
		} else {
			$statement = new Getreply_Statement($reference, parameter, valueMatch, from, helper.redirectValue, helper.redirectParameters, helper.senderReference);
		}
	}
	$statement.setLocation(getLocation( $start, getStopToken()));
};

pr_GetReplyOpKeyword:
	GETREPLY
;

pr_ValueMatchSpec returns[TemplateInstance templateInstance]
@init {
	$templateInstance = null;
}:
(	pr_ValueKeyword
	t = pr_TemplateInstance { $templateInstance = $t.templateInstance; }
);

pr_PortCheckOp [Reference reference]
	returns[Statement statement]
@init {
	$statement = null;
}:
(	col = pr_CheckOpKeyword
	(	pr_LParen
		s = pr_CheckParameter[reference] { $statement = $s.statement; }
		pr_RParen
	|	{
			$statement = new Check_Port_Statement(reference, null, null);
			$statement.setLocation(getLocation( $col.start, $col.stop)); }
	)
);

pr_CheckOpKeyword:
	CHECK
;

pr_CheckParameter [Reference reference]
	returns[Statement statement]
@init {
	$statement = null;
}:
(	s1 = pr_CheckPortOpsPresent[$reference] { $statement = $s1.statement;}
|	s2 = pr_FromClausePresent[$reference] { $statement = $s2.statement;}
|	s3 = pr_RedirectPresent[$reference] { $statement = $s3.statement;}
);

pr_FromClausePresent [Reference reference]
	returns[Statement statement]
@init {
	$statement = null;
	TemplateInstance fromClause = null;
	Reference redirectSender = null;
}:
(	f = pr_FromClause { fromClause = $f.templateInstance; }
	(	pr_PortRedirectSymbol
		r = pr_SenderSpec { redirectSender = $r.reference; }
	)?
)
{
	$statement = new Check_Port_Statement(reference, fromClause, redirectSender);
	$statement.setLocation(getLocation( $f.start, getStopToken()));
};

pr_CheckPortOpsPresent [Reference reference]
	returns[Statement statement]
@init {
	$statement = null;
}:
(	s1 = pr_PortReceiveOp[$reference, true] { $statement = $s1.statement; }
|	s2 = pr_PortGetCallOp[$reference, true] { $statement = $s2.statement; }
|	s3 = pr_PortGetReplyOp[$reference, true] { $statement = $s3.statement; }
|	s4 = pr_PortCatchOp[$reference, true] { $statement = $s4.statement; }
)
{
	if ($statement != null) {
		$statement.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_RedirectPresent [Reference reference]
	returns[Statement statement]
@init {
	$statement = null;
}:
(	col = pr_PortRedirectSymbol
	redirectSender = pr_SenderSpec
)
{
	$statement = new Check_Port_Statement( $reference, null, $redirectSender.reference );
	$statement.setLocation( getLocation( $col.start, $redirectSender.stop ) );
};

pr_PortCatchOp [Reference reference, boolean is_check]
	returns[Statement statement]
@init {
	$statement = null;
	CatchOp_Helper catchopHelper = null;
	TemplateInstance from = null;
	PortRedirect_Helper redirectHelper = null;
}:
(	pr_CatchOpKeyword
	(	pr_LParen
		c = pr_CatchOpParameter { catchopHelper = $c.helper; }
		pr_RParen
	)?
	( f = pr_FromClause { from = $f.templateInstance; } )?
	( rh = pr_PortRedirect { redirectHelper = $rh.helper; } )?
)
{
	if(catchopHelper == null) {
		catchopHelper = new CatchOp_Helper(null, null, false);
	}
	if(redirectHelper == null) {
		redirectHelper = new PortRedirect_Helper(null, null);
	}
	if(is_check) {
		$statement = new Check_Catch_Statement(reference, catchopHelper.signatureReference, catchopHelper.parameter, catchopHelper.timeout,
		from, redirectHelper.redirectValue, redirectHelper.redirectSender);
	} else {
		$statement = new Catch_Statement(reference, catchopHelper.signatureReference, catchopHelper.parameter, catchopHelper.timeout,
		from, redirectHelper.redirectValue, redirectHelper.redirectSender);
	}
	$statement.setLocation(getLocation( $start, getStopToken()));
};

pr_CatchOpKeyword:
	CATCH
;

pr_CatchOpParameter returns[CatchOp_Helper helper]
@init {
	$helper = null;
	Reference signatureReference = null;
	TemplateInstance parameter = null;
	boolean timeout = false;
}:
(	s = pr_Signature { signatureReference = $s.reference; }
	pr_Comma
	p = pr_TemplateInstance { parameter =  $p.templateInstance; }
|	pr_TimeoutKeyword	{ timeout = true; }
)
{
	$helper = new CatchOp_Helper(signatureReference, parameter, timeout);
};

pr_PortOrAll:
(	pr_Port
|	pr_AllPort
);

pr_AllPort:
(	pr_AllKeyword
	pr_PortKeyword
);

pr_AnyKeyword:
	ANY
;

pr_TimerStatements returns[Statement statement]
@init {
	$statement = null;
	Value timerValue = null;
	Value dereferredValue = null;
    ParsedActualParameters parameters = null;
}:
(	r = pr_TimerRef
	pr_Dot
	(	STOP	{ $statement = new Unknown_Stop_Statement( $r.reference ); } //pr_StopTimerStatement
	|	pr_TimeoutKeyword	{ $statement = new Timeout_Statement( $r.reference ); } //pr_TimeoutStatement
	|	START
		(	pr_LParen
			(	tv = pr_TimerValue { timerValue = $tv.value; }
			| 	dv = pr_DereferOp { dereferredValue = $dv.value; }
                a1=pr_LParen
                	( p = pr_FunctionActualParList { parameters = $p.parsedParameters; } )?
                a2=pr_RParen
                {   if(parameters == null) { parameters = new ParsedActualParameters();  }
                    parameters.setLocation(getLocation( $a1.start, $a2.stop));
                }
			)
			pr_RParen
		)?
		{      if(dereferredValue != null) {
                           Value component = new Referenced_Value( $r.reference );
                           component.setLocation( getLocation( $r.start, $r.stop ) );
                           $statement = new Start_Referenced_Component_Statement( component, dereferredValue, parameters );
                     } else {
                           $statement = new Unknown_Start_Statement( $r.reference, timerValue );
                     }
        }
		//pr_StartTimerStatement
	)
|	pr_AllKeyword
	pr_TimerKeyword
	pr_Dot
	STOP
	{ $statement = new Stop_Timer_Statement(null); } //pr_StopTimerStatement
|	pr_AnyKeyword
	pr_TimerKeyword
	pr_Dot
	pr_TimeoutKeyword
	{ $statement = new Timeout_Statement(null); } //pr_TimeoutStatement
)
{
	if($statement != null) {
		$statement.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_TimerOps returns[AnyTimerRunningExpression value]
@init {
	$value = null;
}:
(	col = pr_AnyKeyword
	pr_TimerKeyword
	pr_Dot
	endcol = RUNNING
)
{
	$value = new AnyTimerRunningExpression();
	$value.setLocation(getLocation( $col.start, $endcol));
};

pr_TimeoutKeyword:
	TIMEOUT
;

pr_Type returns[Type type]
@init {
	$type = null;
}:
(	t1 = pr_PredefinedType { $type = $t1.type; }
|	t2 = pr_ReferencedType { $type = $t2.type; }
)
{
	if ( $type != null ) {
		$type.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_PredefinedType returns[Type type]
@init {
	$type = null;
}:
(	BITSTRING	{ $type = new BitString_Type(); }
|	HEXSTRING	{ $type = new HexString_Type(); }
|	OCTETSTRING	{ $type = new OctetString_Type(); }
|	BOOLEAN		{ $type = new Boolean_Type(); }
|	CHARSTRING	{ $type = new CharString_Type(); }
|	INTEGER		{ $type = new Integer_Type(); }
|	VERDICTTYPE	{ $type = new Verdict_Type(); }
|	FLOAT		{ $type = new Float_Type(); }
|	ADDRESS		{ $type = new Address_Type(); }
|	DEFAULT		{ $type = new Default_Type(); }
|	ANYTYPE
		{
			Reference reference = new Reference(null);
			FieldSubReference subReference = new FieldSubReference(new Identifier(Identifier_type.ID_TTCN, "anytype", getLocation( $ANYTYPE)));
			subReference.setLocation(getLocation( $ANYTYPE));
			reference.addSubReference(subReference);
			$type = new Referenced_Type(reference);
		}
|	pr_ObjectIdentifierKeyword { $type = new ObjectID_Type(); }
|	CHARKEYWORD
		{	$type = new CharString_Type();
			reportWarning( "Obsolete type `char' is taken as `charstring' ", $CHARKEYWORD );
		}
|	UNIVERSAL
	(	CHARSTRING
			{ $type = new UniversalCharstring_Type(); }
	|	CHARKEYWORD
			{	$type = new UniversalCharstring_Type();
				reportWarning( "Obsolete type `universal char' is taken as `universal charstring' ", $UNIVERSAL, $CHARKEYWORD );
			}
	)
);

// TODO: this is very different from TITAN, check why
pr_ReferencedType returns[Type type]
@init {
	$type = null;
	Reference reference = null;
	List<ISubReference> subReferences = null;
}:
(	(	r = pr_GlobalModuleId { reference = $r.reference; }
		pr_Dot
	)?
	id = pr_TypeReference
	(	s = pr_ExtendedFieldReference { subReferences = $s.subReferences; }	)?
)
{
	if(reference == null) {
		reference = new Reference(null);
	}
	FieldSubReference subReference = new FieldSubReference($id.identifier);
	subReference.setLocation(getLocation( $id.start, $id.stop));
	reference.addSubReference(subReference);
	if(subReferences != null) {
		for(ISubReference subReference2: subReferences) {
			reference.addSubReference(subReference2);
		}
	}
	reference.setLocation(getLocation( $start, getStopToken()));
	$type = new Referenced_Type(reference);
};

pr_TypeReference returns[ Identifier identifier]
@init {
	$identifier = null;
}:
(	i = pr_Identifier { $identifier = $i.identifier; }
	(	t = pr_TypeActualParList
			{	reportUnsupportedConstruct( "Reference to parameterized type is not currently supported ", $t.start, $t.stop );	}
	)?
);

pr_TypeActualParList:
	pr_LParen
	pr_TypeActualPar
	( pr_Comma pr_TypeActualPar )*
	pr_RParen
;

pr_TypeActualPar:
	pr_Expression
;

pr_ArrayDef returns[ArrayDimensions dimensions]
@init {
	$dimensions = new ArrayDimensions();
}:
(	(	pr_SquareOpen
		(	d1 = pr_ArrayDefRange	{ if($d1.dimension != null) { $dimensions.add($d1.dimension); }}
		|	d2 = pr_ArrayBounds		{ if($d2.dimension != null) { $dimensions.add($d2.dimension); }}
		)
		pr_SquareClose
	)+
);

pr_ArrayDefRange returns[RangedArrayDimension dimension]
@init {
	$dimension = null;
}:
(	lower_boundary = pr_SingleExpression
	RANGEOP
	upper_boundary = pr_SingleExpression
)
{
	$dimension = new RangedArrayDimension($lower_boundary.value, $upper_boundary.value);
	$dimension.setLocation(getLocation( $lower_boundary.start, $upper_boundary.stop));
};

pr_ArrayBounds returns[SingleArrayDimension dimension]
@init {
	$dimension = null;
}:
	v = pr_SingleExpression
{
	$dimension = new SingleArrayDimension($v.value);
	$dimension.setLocation(getLocation( $v.start, $v.stop));
};

pr_Value returns[Value value]
@init {
	$value = null;
}:
(	v1 = pr_PredefinedValue { $value = $v1.value; }
|	v2 = pr_ReferencedValue { $value = $v2.value; }
);

pr_PredefinedValue returns [Value value]
@init {
	$value = null;
}:
(	h = pr_HexString	{	$value = new Hexstring_Value($h.string);
							$value.setLocation(getLocation( $h.start, $h.stop)); }
|	b = pr_BitString	{	$value = new Bitstring_Value($b.string);
							$value.setLocation(getLocation( $b.start, $b.stop)); }
|	o = pr_OctetString	{	$value = new Octetstring_Value($o.string);
							$value.setLocation(getLocation( $o.start, $o.stop)); }
|	v1 = pr_BooleanValue { $value = $v1.value; }
|	v2 = pr_CharStringValue { $value = $v2.value; }
|	NUMBER	{ 	$value = new Integer_Value( $NUMBER.getText() );
				$value.setLocation(getLocation( $NUMBER)); }
|	v3 = pr_VerdictTypeValue { $value = $v3.value; }
//|	v4 = pr_EnumeratedValue { $value = $v4.value; }  // covered by pr_ReferencedValue
|	v5 = pr_FloatValue { $value = $v5.value; }
|	v6 = pr_AddressValue { $value = $v6.value; }
|	OMIT	{	$value = new Omit_Value();
				$value.setLocation(getLocation( $OMIT)); }
|	v7 = pr_ObjectIdentifierValue { $value = $v7.value; }
|	v8 = pr_Macro { $value = $v8.value; }
);

pr_FloatValue returns[Real_Value value]
@init {
	$value = null;
}:
(   FLOATVALUE		{ $value = new Real_Value( Float.parseFloat( $FLOATVALUE.getText() ) ); }
|   INFINITY		{ $value = new Real_Value( Float.POSITIVE_INFINITY ); }
|   NOT_A_NUMBER	{ $value = new Real_Value( Float.NaN ); }
)
{
	if($value != null) { $value.setLocation(getLocation( $start, getStopToken())); }
};

pr_BooleanValue returns[Boolean_Value value]
@init {
	$value = null;
}:
(	TRUE	{ $value = new Boolean_Value(true); }
|	FALSE	{ $value = new Boolean_Value(false); }
)
{
	if($value != null) { $value.setLocation(getLocation( $start, getStopToken())); }
};

pr_VerdictTypeValue returns[Verdict_Value value]
@init {
	$value = null;
}:
(	PASS	{ $value = new Verdict_Value( Verdict_Value.Verdict_type.PASS   ); }
|	FAIL	{ $value = new Verdict_Value( Verdict_Value.Verdict_type.FAIL   ); }
|	INCONC	{ $value = new Verdict_Value( Verdict_Value.Verdict_type.INCONC ); }
|	NONE	{ $value = new Verdict_Value( Verdict_Value.Verdict_type.NONE   ); }
|	ERROR	{ $value = new Verdict_Value( Verdict_Value.Verdict_type.ERROR  ); }
)
{
	if($value != null) { $value.setLocation(getLocation( $start, getStopToken())); }
};

pr_CharStringValue returns[Value value]
@init {
	$value = null;
}:
(	string_value = pr_FreeText
		{	if(UniversalCharstring.isCharstring($string_value.string)) {
				$value = new Charstring_Value($string_value.string);
			} else {
				$value = new UniversalCharstring_Value( new UniversalCharstring($string_value.string));
			}
		}
|	ustring_value = pr_Quadruple { $value = new UniversalCharstring_Value($ustring_value.string); }
)
{
	if($value != null) { $value.setLocation(getLocation( $start, getStopToken())); }
};

pr_Quadruple returns[UniversalCharstring string]
@init {
	$string = null;
}:
(	CHARKEYWORD
	pr_LParen
	group = NUMBER
	pr_Comma
	plane = NUMBER
	pr_Comma
	row = NUMBER
	pr_Comma
	cell = NUMBER
	pr_RParen
)
{
	$string = new UniversalCharstring( new UniversalChar(	Integer.parseInt($group.getText()),
															Integer.parseInt($plane.getText()),
															Integer.parseInt($row.getText()),
															Integer.parseInt($cell.getText())	) );
};

pr_ReferencedValue returns[Value value]
@init {
	$value = null;
	Reference temporalReference = null;
	List<ISubReference> subReferences = null;
}:
(	t = pr_ValueReference { temporalReference = $t.reference; }
	(	e = pr_ExtendedFieldReference { subReferences = $e.subReferences; }	)?
)
{
	if(temporalReference != null) {
		if(subReferences != null) {
			for(ISubReference subReference: subReferences) {
				temporalReference.addSubReference(subReference);
			}
			temporalReference.setLocation(getLocation( $t.start, $e.stop == null ? $t.stop : $e.stop));
		}

		if(subReferences == null && temporalReference.getModuleIdentifier() == null && temporalReference.getSubreferences().size() == 1) {
			$value = new Undefined_LowerIdentifier_Value(temporalReference.getId());
		} else {
			$value = new Referenced_Value(temporalReference);
		}
		$value.setLocation(getLocation( $t.start, $e.stop == null ? $t.stop : $e.stop));
	}
};

pr_ValueReference returns[Reference reference]
@init {
	$reference = null;
}:
(	(	r = pr_GlobalModuleId { $reference = $r.reference; }
		pr_Dot
	)?
	id = pr_Identifier
)
{
	if ( $id.identifier != null ) {
		if ( $reference == null ) {
			$reference = new Reference(null);
		}
		FieldSubReference subReference = new FieldSubReference($id.identifier);
		subReference.setLocation(getLocation( getStopToken(), getStopToken()));
		$reference.addSubReference(subReference);
		$reference.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_AddressValue returns[Value value]
@init {
	$value = null;
}:
(	NULL1 { $value = new TTCN3_Null_Value(); }
|	NULL2 { $value = new ASN1_Null_Value(); }
)
{
	$value.setLocation(getLocation( $start, $start));
};

pr_Macro returns[Macro_Value value]
@init {
	$value = null;
}:
(	MACRO_MODULEID			{ $value = new Macro_Value(Macro_Value.Macro_type.MODULEID); }
|	MACRO_DEFINITION_ID		{ $value = new Macro_Value(Macro_Value.Macro_type.DEFINITIONID); }
|	MACRO_TESTCASEID		{ $value = new Macro_Value(Macro_Value.Macro_type.TESTCASEID); }
|	MACRO_FILENAME			{ $value = new Macro_Value(Macro_Value.Macro_type.FILENAME); }
|	MACRO_BFILENAME			{ $value = new Macro_Value(Macro_Value.Macro_type.BFILENAME); }
|	MACRO_FILEPATH			{ $value = new Macro_Value(Macro_Value.Macro_type.FILEPATH); }
|	MACRO_LINENUMBER		{ $value = new Macro_Value(Macro_Value.Macro_type.LINENUMBER); }
|	MACRO_LINENUMBER_C		{ $value = new Macro_Value(Macro_Value.Macro_type.LINENUMBER_C); }
|	MACRO_SCOPE				{ $value = new Macro_Value(Macro_Value.Macro_type.SCOPE); }
|	MACRO
)
{
	if($value != null) {
		$value.setLocation(getLocation( $start));
	}
};

pr_FormalValuePar returns[FormalParameter parameter]
@init {
	$parameter = null;
	Assignment_type assignmentType = Assignment_type.A_PAR_VAL;
	boolean isLazy = false;
	TemplateInstance default_value = null;
	Location commentLocation = lexer.getLastCommentLocation();
}:
(	(	IN { assignmentType = Assignment_type.A_PAR_VAL_IN; }
		( TITANSPECIFICLAZY { isLazy = true; } )?
	|	INOUT { assignmentType = Assignment_type.A_PAR_VAL_INOUT; }
	|	OUT { assignmentType = Assignment_type.A_PAR_VAL_OUT; }
	|	TITANSPECIFICLAZY { isLazy = true; }
	)?
	t = pr_Type
	i = pr_Identifier
	(   pr_AssignmentChar
	    (   n = pr_NotUsedSymbol
	    		{
	    			TTCN3Template template = new NotUsed_Template();
	    			template.setLocation(getLocation( $n.start, $n.stop));
	    			default_value = new TemplateInstance(null, null, template);
	    			default_value.setLocation(getLocation( $n.start, $n.stop));
	    		}
		|   dv = pr_TemplateInstance { default_value = $dv.templateInstance; }
		)
	)?
)
{
	$parameter = new FormalParameter(TemplateRestriction.Restriction_type.TR_NONE, assignmentType, $t.type, $i.identifier, default_value, isLazy);
	$parameter.setCommentLocation(commentLocation);
	$parameter.setLocation(getLocation( $start, getStopToken()));
	lexer.clearLastCommentLocation();
};

pr_FormalTimerPar returns[FormalParameter parameter]
@init {
	$parameter = null;
	Token startcol = null;
	TemplateInstance default_value = null;
}:
(	( INOUT { startcol = $INOUT; } )?
	t = pr_TimerKeyword	{ if(startcol == null) { startcol = $t.start; }}
	i = pr_Identifier
	(	pr_AssignmentChar
		dv = pr_TemplateInstance { default_value = $dv.templateInstance; }
	)?
)
{
	$parameter = new FormalParameter( TemplateRestriction.Restriction_type.TR_NONE, Assignment_type.A_PAR_TIMER, null,
		$i.identifier, default_value, false );
	$parameter.setLocation(getLocation( startcol, getStopToken()));
};

pr_FormalTemplatePar returns[FormalParameter parameter = null]
	locals [ Assignment_type assignmentType ]
@init {
	$assignmentType = Assignment_type.A_PAR_TEMP_IN;
	TemplateRestriction.Restriction_type templateRestriction = TemplateRestriction.Restriction_type.TR_NONE;
	boolean isLazy = false;
	TemplateInstance default_value = null;
}:
(	(	IN { $assignmentType = Assignment_type.A_PAR_TEMP_IN; }
	|	OUT { $assignmentType = Assignment_type.A_PAR_TEMP_OUT; }
	|	INOUT { $assignmentType = Assignment_type.A_PAR_TEMP_INOUT; }
	)?
	tr = pr_TemplateOptRestricted { templateRestriction = $tr.templateRestriction; }
	( { $assignmentType == Assignment_type.A_PAR_TEMP_IN }? TITANSPECIFICLAZY { isLazy = true; } )?
	t = pr_Type
	i = pr_Identifier
	(   pr_AssignmentChar
	    (   n = pr_NotUsedSymbol
	    		{
					TTCN3Template template = new NotUsed_Template();
					template.setLocation(getLocation( $n.start, $n.stop));
					default_value = new TemplateInstance(null, null, template);
					default_value.setLocation(getLocation( $n.start, $n.stop));
				}
		|	ti = pr_TemplateInstance { default_value = $ti.templateInstance; }
	    )
	)?
)
{
	$parameter = new FormalParameter(templateRestriction, $assignmentType, $t.type, $i.identifier, default_value, isLazy);
	$parameter.setLocation(getLocation( $start, getStopToken()));
};

pr_TemplateOptRestricted returns[TemplateRestriction.Restriction_type templateRestriction]
@init {
	$templateRestriction = TemplateRestriction.Restriction_type.TR_NONE;
}:
(	pr_TemplateKeyword
	( t = pr_TemplateRestriction { $templateRestriction = $t.templateRestriction; } )?
|	OMIT { $templateRestriction = TemplateRestriction.Restriction_type.TR_OMIT; }
);

pr_TemplateRestriction returns[TemplateRestriction.Restriction_type templateRestriction]
@init {
	$templateRestriction = TemplateRestriction.Restriction_type.TR_NONE;
}:
(	pr_LParen
	(	OMIT    { $templateRestriction = TemplateRestriction.Restriction_type.TR_OMIT; }
	|	VALUE   { $templateRestriction = TemplateRestriction.Restriction_type.TR_VALUE; }
	|	PRESENT { $templateRestriction = TemplateRestriction.Restriction_type.TR_PRESENT; }
	)
	pr_RParen
);

pr_WithStatement returns[MultipleWithAttributes attributes]
@init {
	$attributes = null;
}:
(	WITH
	a = pr_WithAttribList { $attributes = $a.attributes;}
)
{
	if( $attributes != null) {
		$attributes.setLocation(getLocation( $a.start, $a.stop));
	}
};

pr_WithAttribList returns[MultipleWithAttributes attributes]
@init {
	$attributes = null;
}:
(	pr_BeginChar
	a = pr_MultiWithAttrib { $attributes = $a.attributes;}
	pr_EndChar
);

pr_MultiWithAttrib returns[MultipleWithAttributes attributes]
@init {
	$attributes = new MultipleWithAttributes();
}:
(	(	s = pr_SingleWithAttrib { $attributes.addAttribute( $s.singleWithAttrib ); }
		pr_SemiColon?
	)*
);

pr_SingleWithAttrib returns [ SingleWithAttribute singleWithAttrib]
@init {
	$singleWithAttrib = null;
	boolean hasOverride = false;
	Qualifiers qualifiers = null;
}:
(	t = pr_AttribKeyword
	( OVERRIDEKEYWORD  { hasOverride = true; } )?
	( q = pr_AttribQualifier { qualifiers = $q.qualifiers; } )?
	s = pr_AttribSpec
)
{
	$singleWithAttrib = new SingleWithAttribute( $t.attributeType, hasOverride, qualifiers, $s.attributeSpecficiation );
	$singleWithAttrib.setLocation(getLocation( $t.start, $s.stop));
};

pr_AttribKeyword returns [Attribute_Type attributeType]
@init {
	$attributeType = null;
}:
(	ENCODE		{ $attributeType = Attribute_Type.Encode_Attribute; }
|	DISPLAY		{ $attributeType = Attribute_Type.Display_Attribute; }
|	EXTENSION	{ $attributeType = Attribute_Type.Extension_Attribute; }
|	VARIANT		{ $attributeType = Attribute_Type.Variant_Attribute; }
|   OPTIONAL	{ $attributeType = Attribute_Type.Optional_Attribute; }
|   i = pr_Identifier
//TODO: create token
		{	if (!"erroneous".equals($i.identifier.getName())) {
				$attributeType = Attribute_Type.Invalid_Attribute;
				//TODO: handle it differently
				//throw new RecognitionException("Invalid attribute. Valid attributes are: `encode', `variant', `display', `extension', `optional' and `erroneous'");
				reportUnsupportedConstruct( "Invalid attribute. Valid attributes are: `encode', `variant', `display', `extension', `optional' and `erroneous'",
					$i.start, $i.stop );
			} else {
				$attributeType = Attribute_Type.Erroneous_Attribute;
			}
		}
);

pr_AttribQualifier returns [Qualifiers qualifiers]
@init {
	$qualifiers = null;
}:
(	pr_LParen
	q = pr_DefOrFieldRefList { $qualifiers = $q.qualifiers; }
	pr_RParen
);

pr_DefOrFieldRefList returns [Qualifiers qualifiers]
@init {
	$qualifiers = null;
}:
(	q = pr_DefOrFieldRef  { $qualifiers = new Qualifiers($q.qualifier); }
	(	pr_Comma
		q = pr_DefOrFieldRef  { $qualifiers.addQualifier($q.qualifier); }
	)*
);

pr_DefOrFieldRef returns[Qualifier qualifier]
@init {
	$qualifier = null;
}:
(	(	i = pr_Identifier
			{	$qualifier = new Qualifier(new FieldSubReference($i.identifier));
				$qualifier.setLocation(getLocation( $i.start, $i.stop));
			}
	|	s = pr_ArrayOrBitRefOrDash //TODO: could be more precise
			{	$qualifier = new Qualifier($s.subReference);
				$qualifier.setLocation(getLocation( $s.start, $s.stop));
			}
	)
	(	s2 = pr_ExtendedFieldReference
			{	if ($s2.subReferences != null && $qualifier != null) {
					for (int i = 0; i < $s2.subReferences.size(); i++) {
						$qualifier.addSubReference($s2.subReferences.get(i));
					}
				}
			}
	)?
|	c = pr_AllRef
		{	reportUnsupportedConstruct( "Reference to multiple definitions in attribute qualifiers is not currently supported", $c.start, $c.stop );	}
);

pr_AllRef:
(	pr_GroupKeyword
	pr_AllKeyword
	( pr_ExceptKeyword	pr_BeginChar	pr_GroupRefList		pr_EndChar )?
|	pr_TypeDefKeyword
	pr_AllKeyword
	( pr_ExceptKeyword	pr_BeginChar	pr_TypeRefList		pr_EndChar )?
|	pr_TemplateKeyword
	pr_AllKeyword
	( pr_ExceptKeyword	pr_BeginChar	pr_TemplateRefList	pr_EndChar )?
|	pr_ConstKeyword
	pr_AllKeyword
	( pr_ExceptKeyword	pr_BeginChar	pr_ConstRefList		pr_EndChar )?
|	pr_TestcaseKeyword
	pr_AllKeyword
	( pr_ExceptKeyword	pr_BeginChar	pr_TestcaseRefList	pr_EndChar )?
|	( pr_FunctionKeyword | pr_AltstepKeyword )
	pr_AllKeyword
	( pr_ExceptKeyword	pr_BeginChar	pr_FunctionRefList	pr_EndChar )?
|	pr_SignatureKeyword
	pr_AllKeyword
	( pr_ExceptKeyword	pr_BeginChar	pr_SignatureRefList	pr_EndChar )?
|	pr_ModuleParKeyword
	pr_AllKeyword
	( pr_ExceptKeyword	pr_BeginChar	pr_ModuleParRefList	pr_EndChar )?
);

pr_AttribSpec returns [AttributeSpecification attributeSpecficiation]
@init {
	$attributeSpecficiation = null;
}:
( s = pr_FreeText )
{
	$attributeSpecficiation = new AttributeSpecification($s.string);
	$attributeSpecficiation.setLocation(getLocation( $s.start, $s.stop));
};

pr_BehaviourStatements returns[Statement statement]
@init {
	$statement = null;
}:
(	s1 = pr_TestcaseInstanceStatement	{ $statement = $s1.statement; }
|	s2 = pr_ReturnStatement				{ $statement = $s2.statement; }
|	s3 = pr_AltConstruct				{ $statement = $s3.statement; }
|	s4 = pr_InterleavedConstruct		{ $statement = $s4.statement; }
|	s5 = pr_LabelStatement				{ $statement = $s5.statement; }
|	s6 = pr_GotoStatement				{ $statement = $s6.statement; }
|	s7 = pr_DeactivateStatement			{ $statement = $s7.statement; }
|	s8 = pr_ActivateStatement			{ $statement = $s8.statement; }
|	REPEAT
		{	$statement = new Repeat_Statement();
			$statement.setLocation(getLocation( $REPEAT));
		}
|	(	t = pr_FunctionInstance
		(	p = pr_ApplyOpEnd
				{	Value value = new Referenced_Value($t.temporalReference);
					value.setLocation(getLocation( $t.start, $p.stop));
					$statement = new Unknown_Applied_Statement(value, $p.parsedParameters);
					$statement.setLocation(getLocation( $t.start, $p.stop));
				}
		|		{	$statement = new Unknown_Instance_Statement($t.temporalReference);
					$statement.setLocation(getLocation( $t.start, $t.stop));
				}
		)
	)
|	(	v = pr_ReferencedValue
		(	p2 = pr_ApplyOpEnd
				{	$statement = new Unknown_Applied_Statement($v.value, $p2.parsedParameters);
					$statement.setLocation(getLocation( $v.start, $p2.stop));
				}
		|	pr_LParen	// this is a syntactically erroneous state only used to report better error messages
		)
	)
|	BREAK
		{	$statement = new Break_Statement();
			$statement.setLocation(getLocation( $BREAK));
		}
|	CONTINUE
		{	$statement = new Continue_Statement();
			$statement.setLocation(getLocation( $CONTINUE));
		}
);

pr_VerdictStatements returns[Statement statement]
@init {
	$statement = null;
}:
	s = pr_SetLocalVerdict { $statement = $s.statement; }
;

pr_VerdictOps returns[GetverdictExpression value]
@init {
	$value = null;
}:
	GETVERDICT
{
	$value = new GetverdictExpression();
	$value.setLocation(getLocation( $GETVERDICT));
};

pr_SetLocalVerdict returns[Setverdict_Statement statement]
@init {
	$statement = null;
	LogArguments logarguments = null;
}:
(	col = SETVERDICT
	pr_LParen
	s = pr_SingleExpression
	(	pr_Comma
		l = pr_LogArguments { logarguments = $l.logArguments; }
	)?
	endcol = pr_RParen
)
{
	$statement = new Setverdict_Statement($s.value, logarguments);
	$statement.setLocation(getLocation( $col, $endcol.stop));
};

pr_SUTStatements returns[Action_Statement statement]
@init {
	$statement = null;
	LogArguments logArguments = null;
}:
(	col = ACTION
	pr_LParen
	(	l = pr_LogArguments { logArguments = $l.logArguments; }
	|	{ logArguments = new LogArguments(); }
	)
	endcol = pr_RParen
)
{
	$statement = new Action_Statement(logArguments);
	$statement.setLocation(getLocation( $col, $endcol.stop));
};

//TODO: Update it!! In 6.1: 478.ReturnStatement ::= ReturnKeyword [Expression | InLineTemplate] (4.4.1 is the same)
//pr_Expression or pr_InlineTemplate
pr_ReturnStatement returns[Return_Statement statement]
@init {
	$statement = null;
	TemplateBody body = null;
}:
(	RETURN
	( b = pr_TemplateBody { body = $b.body; } )?
)
{
	if( body != null ) {
		$statement = new Return_Statement( body.getTemplate() );
	} else {
		$statement = new Return_Statement( null );
	}
	$statement.setLocation(getLocation( $start, getStopToken()));
};

pr_AltConstruct returns[Statement statement]
@init {
	$statement = null;
}:
(	col = ALT
	pr_BeginChar
	a = pr_AltGuardList
	endcol = pr_EndChar
)
{
	$statement = new Alt_Statement($a.altGuards);
	$statement.setLocation(getLocation( $col, $endcol.stop));
};

pr_AltGuardList returns [AltGuards altGuards]
@init {
	$altGuards = new AltGuards();
}:
(	(	a1 = pr_GuardStatement	{ $altGuards.addAltGuard( $a1.altGuard ); }
	|	a2 = pr_ElseStatement	{ $altGuards.addAltGuard( $a2.altGuard ); }
	)+
);

pr_GuardStatement returns [AltGuard altGuard]
@init {
	$altGuard = null;
	Value value2 = null;
	boolean invoked = false;
	ParsedActualParameters parsedParameters = null;
	StatementBlock statementBlock = null;
}:
(	v = pr_AltGuardChar
	(	(	ref1 = pr_FunctionInstance
				{	value2 = new Referenced_Value( $ref1.temporalReference );
					value2.setLocation(getLocation( $ref1.start, $ref1.stop)); }
		|	ref2 = pr_VariableRef
				{	value2 = new Referenced_Value($ref2.reference);
					value2.setLocation(getLocation( $ref2.start, $ref2.stop)); }
		)
		p1 = pr_ApplyOpEnd
		(	pr_SemiColon?
			sb1 = pr_StatementBlock { statementBlock = $sb1.statementblock; }
		)?
		pr_SemiColon?
		{	$altGuard = new Invoke_Altguard($v.value, value2, $p1.parsedParameters, statementBlock);
			$altGuard.setLocation(getLocation( $start, getStopToken())); }
	|	t = pr_FunctionInstance
		( p2 = pr_ApplyOpEnd { invoked = true; parsedParameters = $p2.parsedParameters; } )?
		(	pr_SemiColon?
			sb2 = pr_StatementBlock { statementBlock = $sb2.statementblock; }
		)?
		pr_SemiColon?
		{	if(invoked) {
				value2 = new Referenced_Value($t.temporalReference);
				value2.setLocation(getLocation( $t.start, $t.stop));
				$altGuard = new Invoke_Altguard($v.value, value2, parsedParameters, statementBlock);
			} else {
				$altGuard = new Referenced_Altguard($v.value, $t.temporalReference, statementBlock);
			}
			$altGuard.setLocation(getLocation( $start, getStopToken()));
		}
	|	s = pr_GuardOp
		(	pr_SemiColon?
			sb3 = pr_StatementBlock { statementBlock = $sb3.statementblock; }
		)?
		pr_SemiColon?
		{	$altGuard = new Operation_Altguard($v.value, $s.statement, statementBlock);
			$altGuard.setLocation(getLocation( $start, getStopToken())); }
	)
);

pr_StatementBlock returns [StatementBlock statementblock]
@init {
	$statementblock = new StatementBlock();
	List<Statement> statements = null;
}:
(	pr_BeginChar
	( s = pr_FunctionStatementOrDefList { statements = $s.statements; } )?
	pr_EndChar
)
{
	$statementblock.setLocation(getLargeLocation( $start, getStopToken()));
	if(statements != null) {
		for(Statement statement : statements) {
			$statementblock.addStatement(statement);
		}
	}
};

pr_ElseStatement returns [Else_Altguard altGuard]
@init {
	$altGuard = null;
}:
(	pr_SquareOpen
	ELSE
	pr_SquareClose
	s = pr_StatementBlock
	pr_SemiColon?
)
{
	$altGuard = new Else_Altguard($s.statementblock);
	$altGuard.setLocation(getLocation( $start, getStopToken()));
};

pr_AltGuardChar returns[Value value]
@init {
	$value = null;
}:
(	pr_SquareOpen
	(	v = pr_BooleanExpression { $value = $v.value; }	)?
	pr_SquareClose
);

pr_GuardOp returns[Statement statement]
@init {
	$statement = null;
	TemplateInstance doneMatch = null;
	Reference reference = null;
}:
(	v = pr_ComponentId
		pr_Dot
		(	pr_KilledKeyword	{ $statement = new Killed_Statement($v.value); }		//pr_KilledStatement
		|	pr_DoneKeyword	//pr_DoneStatement
			(	pr_LParen
				t = pr_TemplateInstance { doneMatch = $t.templateInstance; }
				pr_RParen
			)?
			(	pr_PortRedirectSymbol
				vs = pr_ValueSpec { reference = $vs.reference; }
			)?
			{ $statement = new Done_Statement($v.value, doneMatch, reference); } //Done_Statement
		)
|	pr_AnyKeyword
	pr_TimerKeyword pr_Dot pr_TimeoutKeyword		{ $statement = new Timeout_Statement(null); }
|	pr_AnyKeyword
	pr_PortKeyword
	pr_Dot
	(	s1 = pr_PortReceiveOp[null, false]			{ $statement = $s1.statement; }
	|	s2 = pr_PortTriggerOp[null]					{ $statement = $s2.statement; }
	|	s3 = pr_PortGetCallOp[null, false]			{ $statement = $s3.statement; }
	|	s4 = pr_PortCatchOp[null, false]			{ $statement = $s4.statement; }
	|	s5 = pr_PortCheckOp[null]					{ $statement = $s5.statement; }
	|	s6 = pr_PortGetReplyOp[null, false]			{ $statement = $s6.statement; }
	)
|	r = pr_VariableRef
	pr_Dot
	(	pr_TimeoutKeyword								{ $statement = new Timeout_Statement($r.reference); }
	|	s7 = pr_PortReceiveOp[$r.reference, false]		{ $statement = $s7.statement; }
	|	s8 = pr_PortTriggerOp[$r.reference]				{ $statement = $s8.statement; }
	|	s9 = pr_PortGetCallOp[$r.reference, false]		{ $statement = $s9.statement; }
	|	s10 = pr_PortCatchOp[$r.reference, false]		{ $statement = $s10.statement; }
	|	s11 = pr_PortCheckOp[$r.reference]				{ $statement = $s11.statement; }
	|	s12 = pr_PortGetReplyOp[$r.reference, false]	{ $statement = $s12.statement; }
	)
)
{
	if($statement != null) {
		$statement.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_InterleavedConstruct returns[Statement statement]
@init {
	$statement = null;
}:
(	INTERLEAVE
	pr_BeginChar
	a = pr_InterleavedGuardList
	pr_EndChar
)
{
	$statement = new Interleave_Statement($a.altGuards);
};

pr_InterleavedGuardList returns[AltGuards altGuards]
@init {
	$altGuards = new AltGuards();
}:
(	( a = pr_InterleavedGuardElement { $altGuards.addAltGuard($a.altGuard); } )+
);

pr_InterleavedGuardElement returns[AltGuard altGuard]
@init {
	$altGuard = null;
	StatementBlock statementBlock = null;
}:
(	pr_SquareOpen
	pr_SquareClose
	g = pr_GuardOp
	(	pr_SemiColon?
		s = pr_StatementBlock { statementBlock = $s.statementblock; }
		pr_SemiColon?
	|	pr_SemiColon
		pr_SemiColon?
	)?
)
{
	$altGuard = new Operation_Altguard(null, $g.statement, statementBlock);
	$altGuard.setLocation(getLocation( $start, getStopToken()));
};

pr_LabelStatement returns[Label_Statement statement]
@init {
	$statement = null;
}:
(	col = LABEL
	i = pr_Identifier
)
{
	$statement = new Label_Statement($i.identifier);
	$statement.setLocation(getLocation( $col, $i.stop));
};

pr_GotoStatement returns[Goto_statement statement]
@init {
	$statement = null;
	Identifier identifier = null;
}:
(	GOTO
	(	i = pr_Identifier { identifier = $i.identifier; }
	|	ALT	{	reportWarning( "Obsolete statement `goto alt' will be substituted with `repeat' ", $ALT );	}
	)
)
{
	if(identifier != null) {
		$statement = new Goto_statement(identifier);
		$statement.setLocation(getLocation( $GOTO, getStopToken()));
	}
};

pr_ActivateOp returns [Value value]
@init {
	$value = null;
	ParsedActualParameters parameters = null;
}:
(	ACTIVATE
	pr_LParen
	(	v = pr_DereferOp
		a1 = pr_LParen
		( p = pr_FunctionActualParList { parameters = $p.parsedParameters; } )?
		a2 = pr_RParen
			{
				if(parameters == null) {
					parameters = new ParsedActualParameters();
				}
				parameters.setLocation(getLocation( $a1.start, $a2.stop));
				$value = new ActivateDereferedExpression($v.value, parameters);
			}
	|	t = pr_AltstepInstance	{ $value = new ActivateExpression($t.temporalReference); }
	)
	pr_RParen
)
{
	if($value != null) {
		$value.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_ActivateStatement returns[Statement statement]
@init {
	$statement = null;
	ParsedActualParameters parameters = null;
}:
(	ACTIVATE
	pr_LParen
	(	v = pr_DereferOp
		a1 = pr_LParen
		( p = pr_FunctionActualParList { parameters = $p.parsedParameters; } )?
		a2 = pr_RParen
			{	if(parameters == null) {
					parameters = new ParsedActualParameters();
				}
				parameters.setLocation(getLocation( $a1.start, $a2.stop));
				$statement = new Activate_Referenced_Statement($v.value, parameters);
			}
	|	ai = pr_AltstepInstance	{ $statement = new Activate_Statement($ai.temporalReference); }
	)
	pr_RParen
)
{
	if($statement != null) {
		$statement.setLocation(getLocation( $start, getStopToken()));
	}
};

pr_ReferOp returns[RefersExpression value]
@init {
	$value = null;
}:
(	REFERS
	pr_LParen
	r = pr_FunctionRef
	pr_RParen
)
{
	$value = new RefersExpression($r.reference);
	$value.setLocation(getLocation( $start, getStopToken()));
};

pr_DeactivateStatement returns[Deactivate_Statement statement]
@init {
	$statement = null;
	Value value = null;
}:
(	DEACTIVATE
	(	pr_LParen
		v = pr_ComponentOrDefaultReference { value = $v.value; }
		pr_RParen
	)?
)
{
	$statement = new Deactivate_Statement(value);
	$statement.setLocation(getLocation( $start, getStopToken()));
};

pr_BasicStatements returns[Statement statement]
@init {
	$statement = null;
}:
(       s1 = pr_Assignment				{ $statement = $s1.statement; }
|       s2 = pr_LogStatement			{ $statement = $s2.statement; }
|       s3 = pr_String2TtcnStatement	{ $statement = $s3.statement; }
|		s4 = pr_Int2EnumStatement		{ $statement = $s4.statement; }
|       s5 = pr_LoopConstruct			{ $statement = $s5.statement; }
|       s6 = pr_ConditionalConstruct	{ $statement = $s6.ifStatement; }
|       s7 = pr_SelectCaseConstruct		{ $statement = $s7.statement; }
|       s8 = pr_TryCatchConstruct		{ $statement = $s8.statement; }
|       s9 = pr_StatementBlock
				{	if ($s9.statementblock != null) {
						$statement = new StatementBlock_Statement($s9.statementblock);
						$statement.setLocation(getLocation( $start, getStopToken()));
					}
				}
);

pr_Expression returns[Value value]
@init {
	$value = null;
}:
(	v = pr_SingleExpression { $value = $v.value; }
);

pr_CompoundExpression returns[Value value]
@init {
	$value = null;
}:
(	v1 = pr_FieldExpressionList { $value = $v1.value; }
|	v2 = pr_ArrayExpressionList { $value = $v2.value; }
|	v3 = pr_ArrayExpression { $value = $v3.value; }
);

pr_FieldExpressionList returns[Sequence_Value value]
@init {
	$value = null;
	NamedValues values = new NamedValues();
}:
(	col = pr_BeginChar
	v = pr_FieldExpressionSpec	{ if($v.namedValue != null) { values.addNamedValue($v.namedValue); }}
	(	pr_Comma
		v = pr_FieldExpressionSpec	{ if($v.namedValue != null) { values.addNamedValue($v.namedValue); }}
	)*
	endcol = pr_EndChar
)
{
	$value = new Sequence_Value(values);
	$value.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_FieldExpressionSpec returns[NamedValue namedValue]
@init {
	$namedValue = null;
}:
(	i = pr_FieldReference
	pr_AssignmentChar
	v = pr_NotUsedOrExpression
)
{
	$namedValue = new NamedValue($i.identifier, $v.value);
	$namedValue.setLocation(getLocation( $i.start, $v.stop));
};

pr_ArrayExpressionList returns[SequenceOf_Value value]
@init {
	$value = null;
	Values values = new Values(true);
}:
(	col = pr_BeginChar
	v = pr_ArrayExpressionSpec {if ($v.indexedValue != null) {values.addIndexedValue($v.indexedValue); } }
	(	pr_Comma
		v = pr_ArrayExpressionSpec {if ($v.indexedValue != null) {values.addIndexedValue($v.indexedValue); } }
	)*
	endcol = pr_EndChar
)
{
	$value = new SequenceOf_Value(values);
	$value.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_ArrayExpressionSpec returns[IndexedValue indexedValue]
@init {
	$indexedValue = null;
}:
(	s = pr_ArrayOrBitRef
	pr_AssignmentChar
	v = pr_Expression
)
{
	if($s.subReference != null && $v.value != null) {
		$indexedValue = new IndexedValue($s.subReference, $v.value);
		$indexedValue.setLocation(getLocation( $s.start, $v.stop));
	}
};

pr_ArrayExpression returns[SequenceOf_Value value]
@init {
	$value = null;
	Values values = null;
}:
(	col = pr_BeginChar
	( v = pr_ArrayElementExpressionList { values = $v.values; } )?
	endcol = pr_EndChar
)
{
	if(values == null) {
		values = new Values(false);
	}
	$value = new SequenceOf_Value(values);
	$value.setLocation(getLocation( $col.start, $endcol.stop));
};

pr_ArrayElementExpressionList returns[Values values]
@init {
	$values = new Values(false);
}:
(	v = pr_NotUsedOrExpression { if($v.value != null) { $values.addValue($v.value); }}
	(	pr_Comma
		v = pr_NotUsedOrExpression  { if($v.value != null) { $values.addValue($v.value); }}
	)*
);

pr_NotUsedOrExpression returns[Value value]
@init {
	$value = null;
}:
(	v = pr_NotUsedSymbol
		{	$value = new Notused_Value();
			$value.setLocation(getLocation( $v.start, $v.stop));
		}
|	v2 = pr_Expression { $value = $v2.value; }
);

pr_BooleanExpression returns [Value value]
@init {
	$value = null;
}:
	v = pr_Expression { $value = $v.value; }
;

pr_Assignment returns[Assignment_Statement statement]
@init {
	$statement = null;
}:
(	r = pr_VariableRef
	pr_AssignmentChar
	b = pr_TemplateBody
)
{
	if( $b.body != null ) {
		$statement = new Assignment_Statement($r.reference, $b.body.getTemplate());
	} else {
		$statement = new Assignment_Statement($r.reference, null);
	}

	$statement.setLocation(getLocation( $r.start, $b.stop));
};

pr_SingleExpression returns[Value value]
@init {
	$value = null;
}:
(	v = pr_XorExpression{ $value = $v.value; }
	(	OR	v2 = pr_XorExpression
			{	$value = new OrExpression( $value, $v2.value);
				$value.setLocation(getLocation( $v.start, $v2.stop));
			}
	)*
);

pr_XorExpression returns[Value value]
@init {
	$value = null;
}:
(	v = pr_AndExpression{ $value = $v.value; }
	(	XOR	v2 = pr_AndExpression
			{	$value = new XorExpression($value, $v2.value);
				$value.setLocation(getLocation( $v.start, $v2.stop));
			}
	)*
);

pr_AndExpression returns[Value value]
@init {
	$value = null;
}:
(	v = pr_NotExpression{ $value = $v.value; }
	(	AND v2 = pr_NotExpression
			{	$value = new AndExpression($value, $v2.value);
				$value.setLocation(getLocation( $v.start, $v2.stop));
			}
	)*
);

pr_NotExpression returns[Value value]
@init {
	$value = null;
}:
(	NOT
	v1 = pr_NotExpression	{	$value = new NotExpression($v1.value);
								$value.setLocation(getLocation( $NOT, $v1.stop)); }
|	v2 = pr_EqualExpression	{ $value = $v2.value; }
);

pr_EqualExpression returns[Value value]
@init {
	$value = null;
}:
(	v = pr_RelExpression	{ $value = $v.value; }
	(	EQUAL	v1 = pr_RelExpression	{	$value = new EqualsExpression($value, $v1.value);
											$value.setLocation(getLocation( $v.start, $v1.stop)); }
	|	NOTEQUALS	v2 = pr_ShiftExpression {	$value = new NotequalesExpression($value, $v2.value);
												$value.setLocation(getLocation( $v.start, $v2.stop)); }
	)*
);

pr_RelExpression returns[Value value]
@init {
	$value = null;
}:
(   v = pr_ShiftExpression	{ $value = $v.value; }
	(	LESSTHAN	v1 = pr_ShiftExpression	{	$value = new LessThanExpression($value, $v1.value);
												$value.setLocation(getLocation( $v.start, $v1.stop)); }
	|	MORETHAN	v2 = pr_ShiftExpression {	$value = new GreaterThanExpression($value, $v2.value);
												$value.setLocation(getLocation( $v.start, $v2.stop)); }
	|	MOREOREQUAL	v3 = pr_ShiftExpression {	$value = new GreaterThanOrEqualExpression($value, $v3.value);
												$value.setLocation(getLocation( $v.start, $v3.stop)); }
	|	LESSOREQUAL	v4 = pr_ShiftExpression {	$value = new LessThanOrEqualExpression($value, $v4.value);
												$value.setLocation(getLocation( $v.start, $v4.stop)); }
	)*
);

pr_ShiftExpression returns[Value value]
@init {
	$value = null;
}:
(	v = pr_BitOrExpression { $value = $v.value; }
	(	SHIFTLEFT		v1 = pr_BitOrExpression	{	$value = new ShiftLeftExpression($value, $v1.value);
													$value.setLocation(getLocation( $v.start, $v1.stop)); }
	|	SHIFTRIGHT		v2 = pr_BitOrExpression	{	$value = new ShiftRightExpression($value, $v2.value);
													$value.setLocation(getLocation( $v.start, $v2.stop)); }
	|	ROTATELEFT		v3 = pr_BitOrExpression	{	$value = new RotateLeftExpression($value, $v3.value);
													$value.setLocation(getLocation( $v.start, $v3.stop)); }
	|	ROTATERIGHT		v4 = pr_BitOrExpression	{	$value = new RotateRightExpression($value, $v4.value);
													$value.setLocation(getLocation( $v.start, $v4.stop)); }
	)*
);

pr_BitOrExpression returns[Value value]
@init {
	$value = null;
}:
(	v = pr_BitXorExpression { $value = $v.value; }
	(	OR4B	v2 = pr_BitXorExpression	{	$value = new Or4bExpression($value, $v2.value);
												$value.setLocation(getLocation( $v.start, $v2.stop)); }
	)*
);

pr_BitXorExpression returns[Value value]
@init {
	$value = null;
}:
(	v = pr_BitAndExpression { $value = $v.value; }
	(	XOR4B	v2 = pr_BitAndExpression	{	$value = new Xor4bExpression($value, $v2.value);
												$value.setLocation(getLocation( $v.start, $v2.stop)); }
	)*
);

pr_BitAndExpression returns[Value value]
@init {
	$value = null;
}:
(	v = pr_BitNotExpression { $value = $v.value; }
	(	AND4B	v2 = pr_BitNotExpression	{	$value = new And4bExpression($value, $v2.value);
												$value.setLocation(getLocation( $v.start, $v2.stop)); }
	)*
);

pr_BitNotExpression returns[Value value]
@init {
	$value = null;
}:
(	NOT4B
	v = pr_BitNotExpression {	$value = new Not4bExpression($v.value);
								$value.setLocation(getLocation( $start, getStopToken())); }
|	v2 = pr_AddExpression { $value = $v2.value; }
);

pr_AddExpression returns[Value value]
@init {
	$value = null;
}:
(	v = pr_MulExpression { $value = $v.value; }
	(	PLUS	v1 = pr_MulExpression	{	$value = new AddExpression($value, $v1.value);
											$value.setLocation(getLocation( $v.start, $v1.stop)); }
	|	MINUS	v2 = pr_MulExpression {	$value = new SubstractExpression($value, $v2.value);
										$value.setLocation(getLocation( $v.start, $v2.stop)); }
	|	STRINGOP	v3 = pr_MulExpression	{	$value = new StringConcatenationExpression($value, $v3.value);
												$value.setLocation(getLocation( $v.start, $v3.stop)); }
	)*
);

pr_MulExpression returns[Value value]
@init {
	$value = null;
}:
(	v = pr_UnaryExpression { $value = $v.value; }
	(	STAR	v1 = pr_UnaryExpression	{	$value = new MultiplyExpression($value, $v1.value);
											$value.setLocation(getLocation( $v.start, $v1.stop)); }
	|	SLASH	v2 = pr_UnaryExpression	{	$value = new DivideExpression($value, $v2.value);
											$value.setLocation(getLocation( $v.start, $v2.stop)); }
	|	MOD		v3 = pr_UnaryExpression	{	$value = new ModuloExpression($value, $v3.value);
											$value.setLocation(getLocation( $v.start, $v3.stop)); }
	|	REM		v4 = pr_UnaryExpression	{	$value = new RemainderExpression($value, $v4.value);
											$value.setLocation(getLocation( $v.start, $v4.stop)); }
	)*
);

pr_UnaryExpression returns [Value value]
@init {
	$value = null;
}:
(	NOT
	v1 = pr_NotExpression	{	$value = new NotExpression($v1.value);
								$value.setLocation(getLocation( $NOT, $v1.stop ));	}
|	NOT4B
	v2 = pr_BitNotExpression {	$value = new Not4bExpression($v2.value);
								$value.setLocation(getLocation( $NOT4B, $v2.stop ));	}
|	PLUS
	v3 = pr_Primary	{	$value = new UnaryPlusExpression($v3.value);
						$value.setLocation(getLocation( $PLUS, $v3.stop));	}
|	MINUS
	v4 = pr_Primary	{	$value = new UnaryMinusExpression($v4.value);
						$value.setLocation(getLocation( $MINUS, $v4.stop));	}
|	pr_LParen	v5 = pr_SingleExpression	pr_RParen	{ $value = $v5.value; }
|	v6 = pr_Primary	{	$value = $v6.value; }
);

pr_Primary returns[Value value]
@init {
	$value = null;
	Reference temporalReference = null;
	List<ISubReference> subReferences = null;
	ParsedActualParameters parameters = null;
}:
(	t = pr_ValueReference { temporalReference = $t.reference; }
	(  (    a11 = pr_LParen
		    	(p1 = pr_FunctionActualParList { parameters = $p1.parsedParameters; } )?
			a12 = pr_RParen
			{	ISubReference temp = temporalReference.removeLastSubReference();
				Identifier id = temp.getId();
				if(parameters == null) {
					parameters = new ParsedActualParameters();
				}
				parameters.setLocation(getLocation( $a11.start, $a12.stop));
				ParameterisedSubReference subReference = new ParameterisedSubReference(id, parameters);
				subReference.setLocation(getLocation( $t.start, $a12.stop));
				temporalReference.addSubReference(subReference);
				temporalReference.setLocation(getLocation( $t.start, $a12.stop));
				$value = new Referenced_Value(temporalReference);
				$value.setLocation(getLocation( $t.start, $a12.stop));
			}
			(	sr = pr_ExtendedFieldReference
				{	subReferences = $sr.subReferences;
					if(subReferences != null) {
						for(ISubReference subReference1: subReferences) {
							temporalReference.addSubReference(subReference1);
						}
					}
					temporalReference.setLocation(getLocation( $t.start, $sr.stop));
				}
			)?
			(	p2 = pr_ApplyOpEnd
					{	$value = new ApplyExpression( $value, $p2.parsedParameters );
			 	 		$value.setLocation(getLocation( $t.start, $p2.stop));
					}
			)*
			(	pr_Dot
				(	a14=RUNNING
						{	$value = new ComponentRunnningExpression($value);
							$value.setLocation(getLocation( $t.start, $a14));
						}
				|	a15=ALIVE
						{	$value = new ComponentAliveExpression($value);
							$value.setLocation(getLocation( $t.start, $a15));
						}
				)
			)?
	   )
	|	(	sr = pr_ExtendedFieldReference
				{	subReferences = $sr.subReferences;
					if(subReferences != null) {
						for(ISubReference subReference2: subReferences) {
							temporalReference.addSubReference(subReference2);
						}
					}
					temporalReference.setLocation(getLocation( $t.start, $sr.stop));
				}
		)?
		(	pr_Dot a21 = READ
				{	$value = new TimerReadExpression(temporalReference);
					$value.setLocation(getLocation( $t.start, $a21));
				} //pr_ReadTimerOp
		|	c = pr_CreateOpEnd[ temporalReference ]
				{	$value = $c.value;
					if ( $value != null ) {
					$value.setLocation(getLocation( $t.start, $c.stop));
				}
				}
		|	(	{	$value = new Referenced_Value(temporalReference);
					$value.setLocation(temporalReference.getLocation());
				}
				(	p3 = pr_ApplyOpEnd
					{	$value = new ApplyExpression($value, $p3.parsedParameters);
						$value.setLocation(getLocation( $t.start, $p3.stop));
					}
				)+
			|	{	if(temporalReference.getSubreferences().size() == 1 && temporalReference.getModuleIdentifier() == null) {
						$value = new Undefined_LowerIdentifier_Value(temporalReference.getId());
					} else {
						$value = new Referenced_Value(temporalReference);
					}
					$value.setLocation(temporalReference.getLocation());
				}
			)
			(	pr_Dot
				(	a24=RUNNING	{	$value = new UndefRunningExpression(temporalReference);
									$value.setLocation(getLocation( $t.start, $a24));	}
				|	a25=ALIVE	{	$value = new ComponentAliveExpression($value);
									$value.setLocation(getLocation( $t.start, $a25));	}
				)
			)?
		)
	)
|	(	h1=pr_AnyKeyword pr_ComponentKeyword pr_Dot
		(	h11=RUNNING
				{	$value = new AnyComponentRunningExpression();
					$value.setLocation(getLocation( $h1.start, $h11));
				}
		|	h12=ALIVE
				{	$value = new AnyComponentAliveExpression();
					$value.setLocation(getLocation( $h1.start, $h12));
				}
		)
	|	h2=pr_AllKeyword pr_ComponentKeyword pr_Dot
		(	h21=RUNNING
				{	$value = new AllComponentRunningExpression();
					$value.setLocation(getLocation( $h2.start, $h21));
				}
		|	h22=ALIVE
				{	$value = new AllComponentAliveExpression();
					$value.setLocation(getLocation( $h2.start, $h22));
				}
		)
	)
|	v1 = pr_ConfigurationOps { $value = $v1.value; }
|	v2 = pr_TimerOps { $value = $v2.value; }
|	v3 = pr_OpCall { $value = $v3.value; }
|	v4 = pr_Value { $value = $v4.value; }
|	pr_LParen v5 = pr_SingleExpression { $value = $v5.value; } pr_RParen
|	v6 = pr_CompoundExpression { $value = $v6.value; }
);

pr_ExtendedFieldReference returns[List<ISubReference> subReferences]
@init {
	$subReferences = new ArrayList<ISubReference>();
}:
(	a = pr_Dot
	(	structFieldId = pr_Identifier
			{	FieldSubReference tempReference = new FieldSubReference($structFieldId.identifier);
				tempReference.setLocation(getLocation( $structFieldId.start, $structFieldId.stop));
				$subReferences.add(tempReference);
			}
	|	t = pr_PredefinedType
			{	Identifier identifier = new Identifier(Identifier_type.ID_TTCN, $t.type.getTypename(), getLocation( $t.start, $t.stop));
				FieldSubReference tempReference = new FieldSubReference(identifier);
				tempReference.setLocation(getLocation( $t.start, $t.stop));
				$subReferences.add(tempReference);
			}
	)
|	(	abd = pr_ArrayOrBitRefOrDash
			{	ArraySubReference tempReference = $abd.subReference;
				if ( tempReference != null ) {
					tempReference.setLocation(getLocation( $abd.start, $abd.stop));
					$subReferences.add( tempReference );
				}
			}
	)
)+
;

pr_OpCall returns[Value value]
@init {
	$value = null;
}:
(	v1 = pr_VerdictOps			{ $value = $v1.value; }
|	v2 = pr_TestcaseInstanceOp	{ $value = $v2.value; }
|	v3 = pr_TemplateOps			{ $value = $v3.value; }
|	v4 = pr_PredefinedOps		{ $value = $v4.value; }
|	v5 = pr_ActivateOp			{ $value = $v5.value; }
|	v6 = pr_ReferOp				{ $value = $v6.value; }
);

pr_PredefinedOps returns[Value value]
@init {
	$value = null;
	LogArguments logArguments = null;
}:
(	//The ones with only one standard operand
	v1 = pr_PredefinedOps1	{ $value = $v1.value; }
|	v2 = pr_PredefinedOps2	{ $value = $v2.value; }
|	v3 = pr_PredefinedOps3	{ $value = $v3.value; }

	//The ones with non standard operands
|	ISVALUE
	pr_LParen
	t = pr_TemplateInstance
	pr_RParen	{ $value = new IsValueExpression($t.templateInstance); }
|   ISBOUND
	pr_LParen
	t = pr_TemplateInstance
	pr_RParen  { $value = new IsBoundExpression($t.templateInstance); }
|	ISCHOSEN
	pr_LParen
	r = pr_VariableRef
	pr_RParen	{ $value = new IsChoosenExpression($r.reference); }
|	ISPRESENT
	pr_LParen
	t = pr_TemplateInstance
	pr_RParen	{ $value = new IsPresentExpression($t.templateInstance); }
|	LENGTHOF
	pr_LParen
	t = pr_TemplateInstance
	pr_RParen	{ $value = new LengthofExpression($t.templateInstance); }
|	RND
	pr_LParen
	(	v = pr_SingleExpression	{ $value = new RNDWithValueExpression($v.value); }
	|	{ $value = new RNDExpression(); }
	)
	pr_RParen
|	SIZEOF
	pr_LParen
	t = pr_TemplateInstance
	pr_RParen	{ $value = new SizeOfExpression($t.templateInstance); }
|	LOG2STR
	pr_LParen
	(	l = pr_LogArguments { logArguments = $l.logArguments; }
	|	{ logArguments = new LogArguments(); }
	)
	pr_RParen { $value = new Log2StrExpression(logArguments); }
|	DECVALUE
	pr_LParen
	r1 = pr_SizeofARG
	pr_Comma
	r2 = pr_SizeofARG
	pr_RParen	{	$value = new DecodeExpression($r1.reference, $r2.reference); }
|   TESTCASENAME
	pr_LParen
	pr_RParen	{	$value = new TestcasenameExpression(); }
|	TTCN2STRING
	pr_LParen
	t = pr_TemplateInstance
	pr_RParen	{	$value = new Ttcn2StringExpression($t.templateInstance); }
)
{ $value.setLocation(getLocation( $start, getStopToken())); };

//The ones with only one standard operand
pr_PredefinedOps1 returns[Value value]
@init {
	$value = null;
}:
(	BIT2HEX
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Bit2HexExpression($v.value); }
|	BIT2INT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Bit2IntExpression($v.value); }
|	BIT2OCT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Bit2OctExpression($v.value); }
|	BIT2STR
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Bit2StrExpression($v.value); }
|	CHAR2INT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Char2IntExpression($v.value); }
|	CHAR2OCT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Char2OctExpression($v.value); }
|	FLOAT2INT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Float2IntExpression($v.value); }
|	FLOAT2STR
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Float2StrExpression($v.value); }
|	HEX2BIT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Hex2BitExpression($v.value); }
|	HEX2INT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Hex2IntExpression($v.value); }
|	HEX2OCT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Hex2OctExpression($v.value); }
|	HEX2STR
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Hex2StrExpression($v.value); }
|	INT2CHAR
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Int2CharExpression($v.value); }
|	INT2FLOAT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Int2FloatExpression($v.value); }
|	INT2STR
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Int2StrExpression($v.value); }
|	INT2UNICHAR
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Int2UnicharExpression($v.value); }
|	OCT2BIT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Oct2BitExpression($v.value); }
|	OCT2CHAR
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Oct2CharExpression($v.value); }
|	OCT2HEX
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Oct2HexExpression($v.value); }
|	OCT2INT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Oct2IntExpression($v.value); }
|	OCT2STR
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Oct2StrExpression($v.value); }
|	STR2BIT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Str2BitExpression($v.value); }
|	STR2FLOAT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Str2FloatExpression($v.value); }
|	STR2HEX
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Str2HexExpression($v.value); }
|	STR2INT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Str2IntExpression($v.value); }
|	STR2OCT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Str2OctExpression($v.value); }
|	UNICHAR2INT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Unichar2IntExpression($v.value); }
|	UNICHAR2CHAR
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Unichar2CharExpression($v.value); }
|	ENCVALUE
	pr_LParen	t = pr_TemplateInstance
	pr_RParen	{	$value = new EncodeExpression($t.templateInstance); }
|	ENUM2INT
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new Enum2IntExpression($v.value); }
|	GET_STRINGENCODING
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new GetStringencodingExpression($v.value); }
|	OCT2UNICHAR
	pr_LParen	v = pr_SingleExpression
	(pr_Comma pr_SingleExpression)?
	pr_RParen	{	$value = new Oct2UnicharExpression($v.value); }
|	REMOVE_BOM
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new RemoveBomExpression($v.value); }
|	UNICHAR2OCT
	pr_LParen	v = pr_SingleExpression
	(pr_Comma pr_SingleExpression)?
	pr_RParen	{	$value = new Unichar2OctExpression($v.value); }
|	ENCODE_BASE64
	pr_LParen	v = pr_SingleExpression
	(pr_Comma pr_BooleanExpression)?
	pr_RParen	{	$value = new EncodeBase64Expression($v.value); }
|	DECODE_BASE64
	pr_LParen	v = pr_SingleExpression
	pr_RParen	{	$value = new DecodeBase64Expression($v.value); }
)
{ $value.setLocation(getLocation( $start, getStopToken())); };

//The ones with 2 standard operands
pr_PredefinedOps2 returns[Value value]
@init {
	$value = null;
}:
(	INT2BIT
	pr_LParen	v1 = pr_SingleExpression
	pr_Comma	v2 = pr_SingleExpression
	pr_RParen	{	$value = new Int2BitExpression($v1.value, $v2.value); }
|	INT2HEX
	pr_LParen	v1 = pr_SingleExpression
	pr_Comma	v2 = pr_SingleExpression
	pr_RParen	{	$value = new Int2HexExpression($v1.value, $v2.value); }
|	INT2OCT
	pr_LParen	v1 = pr_SingleExpression
	pr_Comma	v2 = pr_SingleExpression
	pr_RParen	{	$value = new Int2OctExpression($v1.value, $v2.value); }
)
{ $value.setLocation(getLocation( $start, getStopToken())); };

//The ones with 3 or 4 standard operands
pr_PredefinedOps3 returns[Value value]
@init {
	$value = null;
}:
(	DECOMP
	pr_LParen	v1 = pr_SingleExpression
	pr_Comma	v2 = pr_SingleExpression
	pr_Comma	v3 = pr_SingleExpression
	pr_RParen	{	$value = new DecompExpression($v1.value, $v2.value, $v3.value); }
|	REGEXP
	pr_LParen	t1 = pr_TemplateInstance
	pr_Comma	t2 = pr_TemplateInstance
	pr_Comma	v3 = pr_SingleExpression
	pr_RParen	{	$value = new RegexpExpression($t1.templateInstance, $t2.templateInstance, $v3.value); }
|	SUBSTR
	pr_LParen	t1 = pr_TemplateInstance
	pr_Comma	v2 = pr_SingleExpression
	pr_Comma	v3 = pr_SingleExpression
	pr_RParen	{	$value = new SubstrExpression($t1.templateInstance, $v2.value, $v3.value); }
	//The ones with 4 standard operands
|	REPLACE
	pr_LParen	t1 = pr_TemplateInstance
	pr_Comma	v2 = pr_SingleExpression
	pr_Comma	v3 = pr_SingleExpression
	pr_Comma	t4 = pr_TemplateInstance
	pr_RParen	{	$value = new ReplaceExpression($t1.templateInstance, $v2.value, $v3.value, $t4.templateInstance); }
)
{ $value.setLocation(getLocation( $start, getStopToken())); };

pr_SizeofARG returns[Reference reference]
@init {
	$reference = null;
}:
(	r1 = pr_FunctionInstance { $reference = $r1.temporalReference; }
|	r2 = pr_VariableRef { $reference = $r2.reference; }
);

pr_LogStatement returns[Log_Statement statement]
@init {
	$statement = null;
	LogArguments logArguments = null;
}:
(	col = LOG
	pr_LParen
	(	logargs = pr_LogArguments { logArguments = $logargs.logArguments; }
	|	{ logArguments = new LogArguments(); }
	)
	endcol = pr_RParen
)
{
	$statement = new Log_Statement( logArguments );
	$statement.setLocation(getLocation( $col, $endcol.stop ) );
};

pr_String2TtcnStatement returns[String2Ttcn_Statement statement]
@init {
	$statement = null;
}:
(	col = STRING2TTCN
	pr_LParen
	v = pr_SingleExpression
	pr_Comma
	r = pr_VariableRef
	endcol = pr_RParen
)
{
	$statement = new String2Ttcn_Statement($v.value, $r.reference);
	$statement.setLocation(getLocation( $col, $endcol.stop));
};

pr_Int2EnumStatement returns[Int2Enum_Statement statement]
@init {
	$statement = null;
}:
(	col = INT2ENUM
	pr_LParen
	v = pr_SingleExpression
	pr_Comma
	r = pr_VariableRef
	endcol = pr_RParen
)
{
	$statement = new Int2Enum_Statement($v.value, $r.reference);
	$statement.setLocation(getLocation( $col, $endcol.stop));
};

pr_LogArguments returns[LogArguments logArguments]
@init {
	$logArguments = new LogArguments();
}:
(	l = pr_LogItem	{ if($l.item != null) { $logArguments.add($l.item); }}
	(	pr_Comma
		l = pr_LogItem	{ if($l.item != null) { $logArguments.add($l.item); }}
	)*
);

pr_LogItem returns[LogArgument item]
@init {
	$item = null;
}:
(	t = pr_TemplateInstance
)
{
	$item = new LogArgument($t.templateInstance);
	$item.setLocation(getLocation( $t.start, $t.stop));
};

pr_LoopConstruct returns[Statement statement]
@init {
	$statement = null;
}:
(	s = pr_ForStatement { $statement = $s.for_statement; }
|	s2 = pr_WhileStatement { $statement = $s2.while_statement; }
|	s3 = pr_DoWhileStatement { $statement = $s3.dowhile_statement; }
);

pr_ForStatement returns[Statement for_statement]
@init {
	$for_statement = null;
	For_Loop_Definitions definitions = null;
	Assignment_Statement initialAssignment = null;
}:
(	FOR
	pr_LParen
	(	d = pr_InitialDefinitions { definitions = $d.definitions; }
	|	i = pr_InitialAssignment { initialAssignment = $i.assignment; }
	)
	pr_SemiColon
	v = pr_BooleanExpression
	pr_SemiColon
	incrementAssignment = pr_Assignment
	pr_RParen
	sb = pr_StatementBlock
)
{
	if(definitions == null) {
		$for_statement = new For_Statement(initialAssignment, $v.value, $incrementAssignment.statement, $sb.statementblock);
	} else {
		$for_statement = new For_Statement(definitions, $v.value, $incrementAssignment.statement, $sb.statementblock);
	}
	$for_statement.setLocation( getLocation( $FOR, $sb.stop) );
};

pr_InitialDefinitions returns[For_Loop_Definitions definitions]
@init {
	$definitions = new For_Loop_Definitions();
}:
(	d = pr_VarInstance
)
{
	if ( $d.definitions != null ) {
		$definitions.addDefinitions( $d.definitions );
		for ( int i = 0; i < $definitions.getNofAssignments(); i++ ) {
			Definition definition = $definitions.getAssignmentByIndex(i);
			definition.setCumulativeDefinitionLocation(getLocation( $start, getStopToken()));
		}
	}
	$definitions.setLocation( getLocation( $d.start, $d.stop ) );
};

pr_InitialAssignment returns[Assignment_Statement assignment]
@init {
	$assignment = null;
}:
(	s = pr_Assignment { $assignment = $s.statement; }
);

pr_WhileStatement returns[Statement while_statement]
@init {
	$while_statement = null;
}:
(	col = WHILE
	pr_LParen
	v = pr_BooleanExpression
	pr_RParen
	sb = pr_StatementBlock
)
{
	$while_statement = new While_Statement($v.value, $sb.statementblock);
	$while_statement.setLocation(getLocation( $col, $sb.stop));
};

pr_DoWhileStatement returns[Statement dowhile_statement]
@init {
	$dowhile_statement = null;
}:
(	col = DO
	sb = pr_StatementBlock
	WHILE
	pr_LParen
	v = pr_BooleanExpression
	endcol = pr_RParen
)
{
	$dowhile_statement = new DoWhile_Statement($v.value, $sb.statementblock);
	$dowhile_statement.setLocation(getLocation( $col, $endcol.stop));
};

pr_ConditionalConstruct returns[If_Statement ifStatement]
@init {
	$ifStatement = null;
	If_Clauses ifClauses = new If_Clauses();
	If_Clause ifClause = null;
	StatementBlock statementblock2 = null;
}:
(	IF
	pr_LParen
	v = pr_BooleanExpression
	pr_RParen
	sb = pr_StatementBlock
	(	ei = pr_ElseIfClause { if($ei.ifClause != null) { ifClauses.addIfClause($ei.ifClause); }}	)*
	(	e = pr_ElseClause { statementblock2 = $e.statementblock; }	)?
)
{
	If_Clause firstIfClause = new If_Clause($v.value, $sb.statementblock);
	firstIfClause.setLocation( getLocation( $start, $sb.stop) );
	ifClauses.addFrontIfClause(firstIfClause);
	$ifStatement = new If_Statement(ifClauses, statementblock2);
	$ifStatement.setLocation(getLocation( $start, getStopToken()));
};

pr_ElseIfClause returns[If_Clause ifClause]
@init {
	$ifClause = null;
}:
(	col = ELSE
	IF
	pr_LParen
	v = pr_BooleanExpression
	pr_RParen
	sb = pr_StatementBlock
)
{
	$ifClause = new If_Clause($v.value, $sb.statementblock);
	$ifClause.setLocation( getLocation( $col, $sb.stop) );
};

pr_ElseClause returns[StatementBlock statementblock]
@init {
	$statementblock = null;
}:
(	ELSE
	s = pr_StatementBlock { $statementblock = $s.statementblock; }
);

pr_SelectCaseConstruct returns[Statement statement]
@init {
	$statement = null;
}:
(	col = SELECT
	pr_LParen
	v = pr_SingleExpression
	pr_RParen
	sc = pr_SelectCaseBody
)
{
	$statement = new SelectCase_Statement($v.value, $sc.selectCases);
	$statement.setLocation(getLocation( $col, $sc.stop));
};

pr_SelectCaseBody returns[SelectCases selectCases]
@init {
	$selectCases = new SelectCases();
}:
(	pr_BeginChar
	( s = pr_SelectCase { if ( $s.selectCase != null ) { $selectCases.addSelectCase( $s.selectCase ); } } )+
	pr_EndChar
);

pr_SelectCase returns[SelectCase selectCase]
@init {
	$selectCase = null;
	TemplateInstances templateInstances = null;
}:
(	CASE
	(	a = pr_LParen	{ templateInstances = new TemplateInstances(); }
		t = pr_TemplateInstance	{ if($t.templateInstance != null) { templateInstances.addTemplateInstance($t.templateInstance); } }
		(	pr_Comma
			t = pr_TemplateInstance	{ if($t.templateInstance != null) { templateInstances.addTemplateInstance($t.templateInstance); } }
		)*
		b = pr_RParen  {templateInstances.setLocation( getLocation( $a.start, $b.stop));}
	|	ELSE
	)
	s = pr_StatementBlock
	pr_SemiColon?
)
{
	$selectCase = new SelectCase(templateInstances, $s.statementblock);
	$selectCase.setLocation( getLocation( $start, getStopToken()) );
};

pr_TryCatchConstruct returns[Statement statement]
@init {
	$statement = null;
}:
(	TITANSPECIFICTRY
	sb1 = pr_StatementBlock
	TITANSPECIFICCATCH
	pr_LParen
	id = pr_Identifier
	pr_RParen
	sb2 = pr_StatementBlock
)
{
	$statement = new TryCatch_Statement($sb1.statementblock, $id.identifier, $sb2.statementblock);
	$statement.setLocation(getLocation( $start, getStopToken()));
};

pr_Identifier returns [Identifier identifier]
@init {
		$identifier = null;
}:
	IDENTIFIER
{
	if($IDENTIFIER.getTokenIndex() >= 0) {
		final String text = $IDENTIFIER.text;
		if ( text != null) {
			$identifier = new Identifier( Identifier_type.ID_TTCN, text, getLocation( $IDENTIFIER ) );
		}
	}
};

pr_CString returns[String string]:
	CSTRING
{
	$string = $CSTRING.text.replaceAll("^\"|\"$", "");
};

pr_FreeText returns[String string]:
	s = pr_CString
{
	$string = $s.string;
};

//------------------------------------------------------
// Miscellaneous productions  1.6.9
//------------------------------------------------------

pr_Dot:
	DOT
;

pr_Dash:
	MINUS
;

pr_Minus:
	MINUS
;

pr_SemiColon:
	SEMICOLON
;

pr_Colon:
	COLON
;

pr_Comma:
	COMMA
;

pr_BeginChar:
	BEGINCHAR
;

pr_EndChar:
	ENDCHAR
;

pr_AssignmentChar:
	ASSIGNMENTCHAR
;

// ------------------------------------------------------------------------------------------

pr_LParen:
	LPAREN
;

pr_RParen:
	RPAREN
;

pr_SquareOpen:
	SQUAREOPEN
;

pr_SquareClose:
	SQUARECLOSE
;

pr_EndOfFile:
	EOF
;

//timer, port and component handling as module names is a prohibited by the ttcn3 language !!!!
pr_UnifiedReferenceParser returns[Reference reference]
@init {
	$reference = null;
	ISubReference arraySubReference = null;
	ParsedActualParameters parameters = null;
	Token startcol = null;
	Token lastcol = null;
}:
(	(	r = pr_KeywordLessGlobalModuleId { $reference = $r.reference; startcol = $r.start; }
		dot = pr_Dot { lastcol = $dot.stop; }
	|	i1 = pr_KeywordLessIdentifier
		i2 = pr_KeywordLessIdentifier
		a2 = pr_Dot
		{
			startcol = $i1.start;
			lastcol = $a2.stop;
			final KeywordLessIdentifier id1 = $i1.identifier;
			final KeywordLessIdentifier id2 = $i2.identifier;
			Location location = getLocation( $i1.start, $a2.stop);
			if("any".equals(id1.getName())) {
				if("timer".equals(id2.getName())) {
					$reference = new Reference(new KeywordLessIdentifier(Identifier_type.ID_TTCN, "any timer", location));
				} else if("port".equals(id2.getName())) {
					$reference = new Reference(new KeywordLessIdentifier(Identifier_type.ID_TTCN, "any port", location));
				} else if("component".equals(id2.getName())) {
					$reference = new Reference(new KeywordLessIdentifier(Identifier_type.ID_TTCN, "any component", location));
				} else {
					throw new NoViableAltException(this);
				}
			} else if("all".equals(id1.getName())) {
				if("timer".equals(id2.getName())) {
					$reference = new Reference(new KeywordLessIdentifier(Identifier_type.ID_TTCN, "all timer", location));
				} else if("port".equals(id2.getName())) {
					$reference = new Reference(new KeywordLessIdentifier(Identifier_type.ID_TTCN, "all port", location));
				} else if("component".equals(id2.getName())) {
					$reference = new Reference(new KeywordLessIdentifier(Identifier_type.ID_TTCN, "all component", location));
				} else {
					throw new NoViableAltException(this);
				}
			} else {
				throw new NoViableAltException(this);
			}
		}
	)?
	(
		(	i1 = pr_KeywordLessIdentifier
											{	FieldSubReference subReference = new FieldSubReference($i1.identifier);
												subReference.setLocation( getLocation( $i1.start, $i1.stop ) );
												if($reference == null) { $reference = new Reference(null); }
												$reference.addSubReference(subReference);
												if(startcol == null) { startcol = $i1.start; }
											}
			(	pr_Dot
				(	i1 = pr_KeywordLessIdentifier
										{	FieldSubReference subReference1 = new FieldSubReference($i1.identifier);
											subReference1.setLocation( getLocation( $i1.start, $i1.stop ) );
											$reference.addSubReference(subReference1);
										}
				|	predef = pr_PredefinedType
										{	FieldSubReference subReference2 = new FieldSubReference(new KeywordLessIdentifier(Identifier_type.ID_TTCN, $predef.text));
											subReference2.setLocation( getLocation( $predef.start, $predef.stop ) );
											$reference.addSubReference(subReference2);
										}
				)
			|	asr = pr_ArrayOrBitRef { arraySubReference = $asr.subReference; $reference.addSubReference(arraySubReference); }
			)*
		|	i1 = pr_KeywordLessIdentifier
			f1 = pr_LParen
			( pp = pr_FunctionActualParList { parameters = $pp.parsedParameters; } )?
			f2 = pr_RParen
				{	if(parameters == null) {
						parameters = new ParsedActualParameters();
					}
					parameters.setLocation( getLocation( $f1.start, $f2.stop ) );
					ParameterisedSubReference subReference = new ParameterisedSubReference($i1.identifier, parameters);
					subReference.setLocation( getLocation( $i1.start, $f2.stop ) );
					if($reference == null) { $reference = new Reference(null); }
					$reference.addSubReference(subReference);
					if(startcol == null) { startcol = $i1.start; }
				}
		)
		(	col = pr_Dot
			g = pr_ApplyKeyword
			g1 = pr_LParen
			( pp = pr_FunctionActualParList { parameters = $pp.parsedParameters; } )?
			h = pr_RParen
				{	if(parameters == null) {
						parameters = new ParsedActualParameters();
					}
					parameters.setLocation( getLocation( $g1.start, $h.stop ) );
					ParameterisedSubReference subReference = new ParameterisedSubReference(new KeywordLessIdentifier(Identifier_type.ID_TTCN, "apply"), parameters);
					subReference.setLocation( getLocation( $g.start, $h.stop ) );
					$reference.addSubReference(subReference);
				}
		)*
		(	i = pr_Dot	{	FieldSubReference subReference = new FieldSubReference(new KeywordLessIdentifier(Identifier_type.ID_TTCN, ""));
							int endOffset = offset + $i.stop.getStopIndex() + 1;
							subReference.setLocation( new Location(actualFile, line - 1 + $i.stop.getLine(), endOffset, endOffset) );
							$reference.addSubReference(subReference);
						}
		)?
	|	{	FieldSubReference subReference = new FieldSubReference(new KeywordLessIdentifier(Identifier_type.ID_TTCN, ""));
			subReference.setLocation( getLocation( $start, $start ) );
			if($reference == null) { $reference = new Reference(null); }
			$reference.addSubReference(subReference);
		}
	)
	endcol = EOF { if(startcol == null) { startcol = $endcol; }}
)
{
	if($reference != null) {
		$reference.setLocation(getLocation( startcol, $endcol));
	}
};

pr_KeywordLessIdentifier returns [KeywordLessIdentifier identifier]
@init {
	$identifier = null;
}:
(	IDENTIFIER | pr_Macro // 1 token only in each case
)
{	$identifier = new KeywordLessIdentifier(Identifier_type.ID_TTCN, $start.getText(), getLocation( $start));
};

pr_KeywordLessGlobalModuleId returns [Reference reference]
@init {
	$reference = null;
}:
(	i = pr_KeywordLessIdentifier
	(	DOT
		pr_ObjectIdentifierValue
		{	$reference = new Reference($i.identifier); }
	)
);
