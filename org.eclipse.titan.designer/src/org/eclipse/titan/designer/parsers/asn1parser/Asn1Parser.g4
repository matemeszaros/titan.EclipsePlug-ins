parser grammar Asn1Parser;

@header {
	import java.util.ArrayList;
	import java.util.List;
	import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
	import org.eclipse.core.resources.IFile;
	import org.eclipse.core.resources.IMarker;
	import org.eclipse.core.resources.IProject;
	import org.eclipse.titan.common.parsers.TITANMarker;
	import org.eclipse.titan.designer.AST.*;
	import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
	import org.eclipse.titan.designer.AST.ASN1.*;
	import org.eclipse.titan.designer.AST.ASN1.definitions.*;
	import org.eclipse.titan.designer.AST.ASN1.Object.*;
	import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_setting.SyntaxSetting_types;
	import org.eclipse.titan.designer.AST.ASN1.types.*;
	import org.eclipse.titan.designer.AST.ASN1.values.*;
	import org.eclipse.titan.designer.AST.TTCN3.types.*; //maybe this should not be here
	import org.eclipse.titan.designer.AST.TTCN3.values.*;
	import org.eclipse.titan.designer.preferences.PreferenceConstants;
	import org.eclipse.titan.designer.AST.ASN1.values.Undefined_Block_Value;
	import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition;
	import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_Definition;
	import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Enumerated_Type;
	import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
	import org.eclipse.titan.designer.AST.ASN1.types.ASN1_BitString_Type;
	import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
	import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
	import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Integer_Type;
	import org.eclipse.titan.designer.AST.ASN1.Parameterised_Reference;
	import org.eclipse.titan.designer.AST.ASN1.InformationFromObj;
	import org.eclipse.titan.designer.AST.ASN1.TableConstraint;
	import org.eclipse.titan.designer.AST.ASN1.Undefined_Assignment_OS_or_VS;
	import org.eclipse.titan.designer.AST.ASN1.ValueSet_Assignment;
}

/*
******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************
*/
@members{
	private IFile actualFile = null;
	private int line = 0;
	private int offset = 0;
	private ASN1Module act_asn1_module = null;
	private IProject project = null;

	private List<SyntacticErrorStorage> errorsStored = new ArrayList<SyntacticErrorStorage>();
	
	public List<SyntacticErrorStorage> getErrorStorage() {
		return errorsStored;
	}
	
	public void setProject(IProject project) {
		this.project = project;
	}

	public ASN1Module getModule() {
		return act_asn1_module;
	}
  
	public void setActualFile(final IFile file) {
		actualFile = file;
	}
  
	public void setLine(final int line) {
		this.line = line;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}
  
	private Location getLocation(final Token token) {
		return new Location(actualFile, line + token.getLine() - 1, offset + token.getStartIndex(), offset + token.getStopIndex() + 1);
	}
  
	private Location getLocation(final Token startToken, final Token endToken) {
		if (endToken == null) {
			return getLocation(startToken);
		}
		return new Location(actualFile, line - 1 + startToken.getLine(), offset + startToken.getStartIndex(), offset + endToken.getStopIndex() + 1);
	}

	public void reportWarning(TITANMarker marker){
		warnings.add(marker);
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

	private List<TITANMarker> warnings = new ArrayList<TITANMarker>();


	public List<TITANMarker> getWarnings(){
		return warnings;
	}
	
	private ArrayList<TITANMarker> unsupportedConstructs = new ArrayList<TITANMarker>();

	public void reportUnsupportedConstruct(TITANMarker marker) {
		unsupportedConstructs.add(marker);
	}

	public ArrayList<TITANMarker> getUnsupportedConstructs() {
		return unsupportedConstructs;
	}
	
	int consume_counter = 0;

	public int nof_consumed_tokens() {
		return consume_counter;
	}

	public Token consume() {
			Token token = super.consume();
			
			if(token.getType() == BLOCK) {
				consume_counter += ((TokenWithIndexAndSubTokens)token).tokenList.size() + 2;
			} else {
				consume_counter++;
			}
			
			return token;
	}
}

/*
 * @author Laszlo Baji
 * 
 * */
options {
tokenVocab=Asn1Lexer;
}

pr_TITAN_special_Assignment [Identifier identifier]
returns [ASN1Assignment assignment]
locals [Token col, ASN1Type type, Reference reference, ObjectClass_Definition objectClass_Definition]
@init { $type = null; $reference = null; $objectClass_Definition = null; $assignment = null; }:
(	
	(	
		a = pr_Type_reg
		{
			$type = $a.type; 
			$assignment = new Type_Assignment($identifier, null, $type);
			$assignment.setLocation(new Location(actualFile, $a.start, $a.start));
		}
	|	b = pr_FromObjs
		{
			$reference = $b.fromObj; 
			$col = $b.start;
			$type = new Referenced_Type($reference);
			$type.setLocation(new Location(actualFile, $b.start, $b.start));
			$assignment = new Type_Assignment($identifier, null, $type);
			$assignment.setLocation(new Location(actualFile, $col, $col));
		}
	|	c = pr_UpperFromObj
		{
			$reference = $c.fromObj;
			$col = $c.start;
			$type = new Referenced_Type($reference);
			$type.setLocation(new Location(actualFile, $c.start, $c.start));
			$assignment = new Type_Assignment($identifier, null, $type);
			$assignment.setLocation(new Location(actualFile, $col, $col));
		}
	|	d = pr_ObjectClassDefn
		{
			$objectClass_Definition = $d.objectClass_Definition; 
			$col = $d.start;
			$assignment = new ObjectClass_Assignment($identifier, null, $objectClass_Definition);
			$assignment.setLocation(new Location(actualFile, $d.start, $d.start));
		}
	)
	EOF
);


pr_ASN1ModuleDefinition
locals [Identifier identifier, Tag_types defaultTagging, boolean extensionImplied, ASN1Assignments assignments]
@init { $identifier = null; $defaultTagging = null; $assignments = null; }:
	a = pr_ModuleIdentifier { $identifier = $a.identifier; }
	(
		pr_DefinitiveIdentifier
	)?
	DEFINITIONS
	b = pr_TagDefault { $defaultTagging = $b.defaultTagging; }
	c = pr_ExtensionDefault { $extensionImplied = $c.implied; }
	ASSIGNMENT
	{
		act_asn1_module = new ASN1Module($identifier, project, $defaultTagging, $extensionImplied);
	}
	d = BEGIN
	e = pr_ModuleBody { $assignments = $e.assignments; }
	f = END
	EOF
{
	act_asn1_module.setLocation(new LargeLocation(actualFile, $start, $f));
	if($assignments != null) {
		$assignments.setLocation(new Location(actualFile, $d, $f));
		act_asn1_module.setAssignments($assignments);
	}
};
pr_ModuleIdentifier returns [Identifier identifier]
@init { $identifier = null; }:
	a = UPPERIDENTIFIER
{
	$identifier = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
};

pr_DefinitiveIdentifier:
(
	BLOCK
);

pr_TagDefault returns [Tag_types defaultTagging]
@init { $defaultTagging = Tag_types.EXPLICIT_TAGS; }:
(
	EXPLICIT	TAGS  { $defaultTagging = Tag_types.EXPLICIT_TAGS; }
|	IMPLICIT	TAGS  { $defaultTagging = Tag_types.IMPLICIT_TAGS; }
|	AUTOMATIC	TAGS  { $defaultTagging = Tag_types.AUTOMATIC_TAGS; }
)?;

pr_ModuleBody returns[ASN1Assignments assignments]
locals [Exports exports, Imports imports]
@init { $exports = null; $imports = null; $assignments = null; }:
(
	(
		a = pr_Exports	{ $exports = $a.exports; }
	|	{ $exports = new Exports(true); }
	)
	(	
		b = pr_Imports	{ $imports = $b.imports; }
	|	{
			$imports = new Imports();
			Location location = new Location(actualFile);
			$imports.setLocation(location);
			reportWarning(new TITANMarker("Missing IMPORTS clause is interpreted as `IMPORTS ; (import nothing) instead of importing all symbols from all modules.",
										0, 0, 0, IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL));
		}
	)
	c = pr_AssignmentList	{ $assignments = $c.assignments; }
|	{	
		$exports = new Exports(false);
		$imports = new Imports();
		$assignments = new ASN1Assignments();
	}
)
{
	act_asn1_module.setExports($exports != null ? $exports : new Exports(false));
	act_asn1_module.setImports($imports != null ? $imports : new Imports());
};

pr_ExtensionDefault returns [boolean implied]
@init { $implied = false; }:
(
	EXTENSIBILITY
	IMPLIED { $implied = true; }
)?;

pr_Exports returns [Exports exports]
@init { $exports = null; }:
(
	EXPORTS
	 a = pr_SymbolsExported { $exports = $a.exports; }
	SEMICOLON
);

pr_Imports returns [Imports imports = null ]
@init { $imports = null; }:
(
	IMPORTS
	(
		a = pr_SymbolsImported { $imports = $a.imports; }
	|	{ $imports = new Imports(); }
	)
	SEMICOLON
);

pr_AssignmentList returns [ASN1Assignments assignments]
locals [ASN1Assignment assignment]
@init { $assignment = null; $assignments = new ASN1Assignments(); }:
(
	a = pr_Assignment { $assignment = $a.assignment;
		if($assignment != null) {
			$assignment.setLocation(new Location(actualFile, $a.start, $a.start));
			$assignments.addAssignment($assignment);
		}
	}
)+;

pr_SymbolsExported returns [Exports exports]
locals [Symbols symbols]
@init { $symbols = null; $exports = null;}:
(
	a = pr_SymbolList  { $symbols = $a.symbols; $exports = new Exports($symbols); }
|	ALL  { $exports = new Exports(true); }
|	{ $exports = new Exports(false); }
);

pr_SymbolsImported returns [Imports imports]
locals [ImportModule importModule]
@init { $importModule = null; $imports = new Imports(); }:
(
	(
		a = pr_SymbolsFromModule  { $importModule = $a.importModule; $imports.addImportModule($importModule); }
	)+
);

pr_Assignment returns [ASN1Assignment assignment]
locals [Token endcol, Token col]:
(
	a = pr_ValueAssignment			{ $assignment = $a.assignment;  $col = $a.start; $endcol = $a.start; }
|	b = pr_ValueSetTypeAssignment	{ $assignment = $b.assignment;  $col = $b.start; $endcol = $b.start; }
|	c = pr_UndefAssignment			{ $assignment = $c.assignment;  $col = $c.start; $endcol = $c.start; }
|	d = pr_ObjectClassAssignment	{ $assignment = $d.assignment;  $col = $d.start; $endcol = $d.start; }
|	e = pr_TypeAssignment			{ $assignment = $e.assignment;  $col = $e.start; $endcol = $e.start; }
)
{
	if ($assignment != null) {
		$assignment.setLocation(new Location(actualFile, $col, $endcol));
	}
};

pr_SymbolList returns [Symbols symbols, Identifier identifier]
@init { $symbols = new Symbols(); $identifier = null; }:
(
	a = pr_Symbol  { $identifier = $a.identifier; $symbols.addSymbol($identifier); }
	(
		COMMA
		b = pr_Symbol  { $identifier = $b.identifier; $symbols.addSymbol($identifier); }
	)*
);

pr_SymbolsFromModule returns [ImportModule importModule]
locals [Symbols symbols]
@init { $symbols = null; $importModule = null; }:
(
	a = pr_SymbolList { $symbols = $a.symbols; }
	FROM
	b = UPPERIDENTIFIER
	(
		pr_AssignedIdentifier
	)?
)
{
	$importModule = new ImportModule(new Identifier(Identifier_type.ID_ASN, $b.getText(), 
	new Location(actualFile, $b, $b)), $symbols);
};

pr_TypeAssignment returns [ASN1Assignment assignment]
locals [Identifier identifier, Ass_pard formalParameters, ASN1Type type, Reference reference]
@init { $identifier = null; $formalParameters = null; $type = null; $reference = null; $assignment = null; }:
(
	id = UPPERIDENTIFIER	{ $identifier = new Identifier(Identifier_type.ID_ASN, $id.getText(), new Location(actualFile, $id, $id)); }
	fp = pr_OptParList		{ $formalParameters = $fp.parameters; }
	ASSIGNMENT
	(
		tr = pr_Type_reg
		{
			$type = $tr.type;
			$assignment = new Type_Assignment($identifier, $formalParameters, $type);	
		}
	)
|	(
		uo = pr_UpperFromObj  
		{
			$reference = $uo.fromObj;
			$type = new Referenced_Type($reference);
			$type.setLocation(new Location(actualFile, $uo.start, $uo.start));
			$assignment = new Type_Assignment($identifier, $formalParameters, $type);
			
		}
	)
|	(
		fo = pr_FromObjs  
		{
			$reference = $fo.fromObj;
			$type = new Referenced_Type($reference);
			$type.setLocation(new Location(actualFile, $fo.start, $fo.start));
			$assignment = new Type_Assignment($identifier, $formalParameters, $type);
		}
	)
);

pr_ObjectClassAssignment returns [ASN1Assignment assignment]
locals [Token endCol, Identifier identifier, Ass_pard formalParameters, ObjectClass_Definition objectClass_Definition]
@init { $identifier = null; $formalParameters = null; $objectClass_Definition = null; $assignment = null; }:
(
	id = UPPERIDENTIFIER	{ $identifier = new Identifier(Identifier_type.ID_ASN, $id.getText(), new Location(actualFile, $id, $id)); }
	fp = pr_OptParList		{ $formalParameters = $fp.parameters; }
	ASSIGNMENT
	cd = pr_ObjectClassDefn
	{
		$objectClass_Definition = $cd.objectClass_Definition;
		$endCol = $cd.start;
		$assignment = new ObjectClass_Assignment($identifier, $formalParameters, $objectClass_Definition);
	}
)
/*{
	if ($objectClass_Definition != null) {
		$assignment.setLocation(new Location(actualFile, $id, $endCol));
	}
}*/
;

pr_UndefAssignment returns [ASN1Assignment assignment]
locals [Identifier identifier, Ass_pard formalParameters, Reference reference, Reference reference2]
@init { $identifier = null; $formalParameters = null; $reference = null; $reference2 = null; $assignment = null; }:
(
	id = UPPERIDENTIFIER	{ $identifier = new Identifier(Identifier_type.ID_ASN, $id.getText(), new Location(actualFile, $id, $id)); }
	fp = pr_OptParList		{ $formalParameters = $fp.parameters; }
	(
		(
			d1  = pr_DefdUpper	{ $reference = $d1.reference; }
			ASSIGNMENT
			b1 = BLOCK			
			{
				$assignment = new Undefined_Assignment_OS_or_VS($identifier, $formalParameters, $reference, new Block($b1));
			}
		)
	|	(
			ASSIGNMENT
			d2 = pr_DefdUpper	
			{
				$reference = $d2.reference;
				if ($reference != null) {
					$assignment = new Undefined_Assignment_T_or_OC($identifier, $formalParameters, $reference);
				}
			}
		)	
	)
|	(
		id = LOWERIDENTIFIER	{ $identifier = new Identifier(Identifier_type.ID_ASN, $id.getText(), new Location(actualFile, $id, $id)); }
		fp = pr_OptParList		{ $formalParameters = $fp.parameters; }
		d3  = pr_DefdUpper		{ $reference = $d3.reference; }
		ASSIGNMENT
		(
			(
				b3 = BLOCK 
				{
					if($reference != null) {
						$assignment = new Undefined_Assignment_O_or_V($identifier, $formalParameters, $reference, new Block($b3));
					}
				}
			)
		|	(
				d4 = pr_RefdLower
				{
					$reference2 = $d4.reference; 
					if($reference != null) {
						$assignment = new Undefined_Assignment_O_or_V($identifier, $formalParameters, $reference, $reference2);
					}
				}
			)
		)
	)
);

pr_ValueSetTypeAssignment returns [ASN1Assignment assignment]
locals [Identifier identifier, Ass_pard formalParameters, ASN1Type type]
@init { $identifier = null; $formalParameters = null; $type = null; $assignment = null; }:
(
	id = UPPERIDENTIFIER	{ $identifier = new Identifier(Identifier_type.ID_ASN, $id.getText(), new Location(actualFile, $id, $id)); }
	fp = pr_OptParList		{ $formalParameters = $fp.parameters; }
	tr = pr_Type_reg 		{ $type = $tr.type; }
	ASSIGNMENT
	b = BLOCK
	{
		$assignment = new ValueSet_Assignment($identifier, $formalParameters, $type, new Block($b));
		reportWarning( "Taking ValueSetTypeAssignment as a TypeAssignment", $id, $id );
	}
);

pr_ValueAssignment returns [ASN1Assignment assignment]
locals [Token endcol, Identifier identifier, Reference reference, Reference reference2, ASN1Type type, Value value, Ass_pard formalParameters]
@init { $assignment = null;  $identifier = null; $reference = null; $reference2 = null; $type = null; $value = null; $formalParameters = null; }: // valuereference Type "::=" Value
(
	id = LOWERIDENTIFIER	{ $identifier = new Identifier(Identifier_type.ID_ASN, $id.getText(), new Location(actualFile, $id, $id)); }
	fp = pr_OptParList		{ $formalParameters = $fp.parameters; }
	(
		(
			a10 = pr_Type_reg { $type = $a10.type; }
			ASSIGNMENT
			(	
				a11 = pr_Value_reg			{ $value = $a11.value; $endcol = $a11.start; }
			|	a12 = pr_NullValue			{ $value = $a12.value; $endcol = $a12.start; }
			|	block1 = BLOCK				{ $value = new Undefined_Block_Value(new Block($block1)); $endcol = $block1;}
			|	a14 = pr_ReferencedValue_reg{ $value = $a14.value; $endcol = $a14.start; }
			|	a15 = pr_LowerIdValue		{ $value = $a15.value; $endcol = $a15.start; }
			)
			{
				$assignment = new Value_Assignment($identifier, $formalParameters, $type, $value);
			}
		)
	|	a20 = pr_FromObjs { $reference = $a20.fromObj; }
		ASSIGNMENT
		a21 = pr_Value { $value = $a21.value; }
		{
			$endcol = $a21.start;
			$type = new Referenced_Type($reference);
			$assignment = new Value_Assignment($identifier, $formalParameters, $type, $value);
		}
	|	a30 = pr_UpperFromObj { $reference = $a30.fromObj; } 
		ASSIGNMENT
		a31 = pr_Value { $value = $a31.value; }
		{
			$endcol = $a31.start;
			$type = new Referenced_Type($reference);
			$assignment = new Value_Assignment($identifier, $formalParameters, $type, $value);
		}
	|	(	
			a40 = pr_DefdUpper { $reference = $a40.reference; }
			ASSIGNMENT
			(	
				a41 = pr_Value_reg { $value = $a41.value; }//value_assignment
				{
					$endcol = $a41.start;
					$type = new Referenced_Type($reference);
					$assignment = new Value_Assignment( $identifier, $formalParameters, $type, $value);
				}
			|	a42 = pr_NullValue { $value = $a42.value; } //value_assignment
				{
					$endcol = $a42.start;
					$type = new Referenced_Type($reference);
					$assignment = new Value_Assignment($identifier, $formalParameters, $type, $value);
				}
			)
		)

	)
)
{
	if ($assignment != null) {
		$assignment.setLocation(new Location(actualFile, $id, $endcol));
	}
};

pr_OptParList returns [Ass_pard parameters]
@init { $parameters = null; }:
(
	(
		b = BLOCK	{ if($b != null) { $parameters = new Ass_pard(new Block($b)); }}
	)?
);

pr_Type_reg returns [ASN1Type type]
locals [ASN1Type subType, NamedType_Helper namedType_Helper, Constraint constraint, Constraints constraints]
@init { $subType = null; $namedType_Helper = null; $constraint = null; $constraints = null; $type = null; }:
(
	(	(	
			set = SET
		|	sequence = SEQUENCE
		)
		(	
			a = pr_Constraint		{ $constraint = $a.constraint; }
		|	b = pr_SizeConstraint	{ $constraint = $b.constraint; }
		)?
		OF
		(	
			c = pr_NamedType{ $namedType_Helper = $c.helper; $subType = $namedType_Helper.type;}
		|	d = pr_Type		{ $subType = $d.type;}
		)   // pr_SeqOfSetOfWithConstraint, pr_SequenceOfType, pr_SetOfType
		{
			if($set != null) {
				$type = new SetOf_Type($subType);
				$type.setLocation(new Location(actualFile, $set, $c.start != null ? $c.start : $d.start));
			} else if($sequence != null) {
				$type = new SequenceOf_Type($subType);
				$type.setLocation(new Location(actualFile, $sequence, $c.start != null ? $c.start : $d.start));
			}
			if ($constraint != null && $type != null) {
				$constraints = new Constraints();
				$constraints.addConstraint($constraint);
				$type.addConstraints($constraints);
			}
		}
	)
|//	(
//		(
			(	
				builtin = pr_BuiltinType_reg { $type = $builtin.type; }
				(
					e = pr_Constraints { $constraints = $e.constraints; } 
				)?  // pr_BuiltinType_reg, pr_ConstrainedType
				{
					if ( $type != null ) {
						$type.setLocation(new Location(actualFile, $builtin.start, $builtin.start));
						$type.addConstraints($constraints);
					}
				}
			|	ref = pr_ReferencedType { $type = $ref.type; }
				f = pr_Constraints
				{
					$constraints = $f.constraints;
					if ( $type != null ) {
						$type.setLocation(new Location(actualFile, $ref.start, $ref.start));
						$type.addConstraints($constraints);
					}
				}
			)
/*		(	(	pr_ReferencedType
			|	pr_BuiltinType_reg
			)  //pr_NakedType_reg
			pr_Constraints
		)=>pr_NakedType_reg pr_Constraints*/
//	|	pr_SeqOfSetOfWithConstraint
//		)  //pr_ConstrainedType
//|	(	pr_BuiltinType_reg
//	|	pr_SequenceOfType
//	|	pr_SetOfType
//	) //pr_BuiltinType
|	pr_Tag
	d = pr_Type	{ $type = $d.type; }
);

pr_ObjectClassDefn returns [ObjectClass_Definition objectClass_Definition]
locals [Token end, Block withBlock]
@init { $withBlock = null; $objectClass_Definition = null; }:
(	
	CLASS
	block1 = BLOCK {  $end = $block1; }
	(	
		block2 = pr_WithSyntaxSpec { $withBlock = $block2.block; $end = $block2.start; }
	)?
)
{
	$objectClass_Definition = new ObjectClass_Definition(new Block($block1), $withBlock);
	$objectClass_Definition.setLocation(new Location(actualFile, $block1, $end));
};

pr_Symbol returns [Identifier identifier]
@init { $identifier = null; }:
(	
	(	
		a = UPPERIDENTIFIER
		{ 
			$identifier = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
		}
	|	b = LOWERIDENTIFIER
		{ 
			$identifier = new Identifier(Identifier_type.ID_ASN, $b.getText(), new Location(actualFile, $b, $b));
		}
	)
	( 
		BLOCK
	)?
);

pr_AssignedIdentifier:
(	
	BLOCK
|	pr_DefinedValue
);

pr_WithSyntaxSpec returns [Block block]
@init { $block = null; }:
(	
	WITH
	SYNTAX
	b = BLOCK
)
{
	$block = new Block($b);
};


pr_special_ObjectSyntaxLiteral returns[ObjectClassSyntax_literal literal]
locals [String text]
@init { $text = _input.LT(1).getText(); }:
(	COMMA | ABSENT | ALL | ANY | APPLICATION | AUTOMATIC | BEGIN | BY | CLASS | COMPONENT | COMPONENTS
|	CONSTRAINED | CONTAINING | DEFAULT | DEFINED | DEFINITIONS | ENCODED | EXCEPT
|	EXPLICIT | EXPORTS | EXTENSIBILITY | FROM | IDENTIFIER | IMPLICIT | IMPLIED | IMPORTS | INCLUDES | MAX | MIN | OF
|	OPTIONAL | PATTERN | PDV | PRESENT | PRIVATE | SIZE | STRING | SYNTAX | TAGS | UNIQUE | UNIVERSAL | WITH
)
{
	$literal = new ObjectClassSyntax_literal($text);
};

pr_special_FieldSpecList returns [FieldSpecifications fieldSpecifications]
@init { $fieldSpecifications = null; }:
(	
	a = pr_FieldSpecList { $fieldSpecifications = $a.fieldSpecifications; }
	EOF
);

pr_FieldSpecList returns [FieldSpecifications fieldSpecifications]
locals [FieldSpecification fieldSpecification]
@init { $fieldSpecification = null; $fieldSpecifications = new FieldSpecifications(); }:
(
	startCol = pr_FieldSpec { $fieldSpecification = $startCol.fieldSpecification; }
	{
		if ($fieldSpecification != null) {
			$fieldSpecification.setLocation(new Location(actualFile, $startCol.start, $startCol.start));
			$fieldSpecifications.addFieldSpecification($fieldSpecification);
		}
	}
	(	
		COMMA
		endCol = pr_FieldSpec { $fieldSpecification = $endCol.fieldSpecification; }
			{	if($fieldSpecification != null) {
					$fieldSpecification.setLocation(new Location(actualFile, $endCol.start, $endCol.start));
					$fieldSpecifications.addFieldSpecification($fieldSpecification);
				}
			}
	)*
);

pr_FieldSpec returns [FieldSpecification fieldSpecification]
@init { $fieldSpecification = null; }:
(	
	a = pr_TypeFieldSpec { $fieldSpecification = $a.fieldspecification; }
|	b = pr_FixedTypeValueFieldSpec { $fieldSpecification = $b.fieldSpecification; }
|	c = pr_UndefFieldSpec { $fieldSpecification = $c.fieldSpecification; }
);

pr_TypeFieldSpec returns [Type_FieldSpecification fieldspecification]
@init { boolean is_optional = false; ASN1Type type = null; $fieldspecification = null; }:
(
	a = AMPUPPERIDENTIFIER
	(
		OPTIONAL	{ is_optional = true; }
	|	DEFAULT
		b = pr_Type	{ type = $b.type; }
	)?
)
{
	Identifier identifier = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
	$fieldspecification = new Type_FieldSpecification(identifier, is_optional, type);
};

pr_UndefFieldSpec returns [Undefined_FieldSpecification fieldSpecification]
locals [Identifier identifier, Defined_Reference reference, boolean is_optional, Block defaultBlock, Reference defaultSetting]
@init { $identifier = null; $reference = null;  $is_optional = false; $defaultBlock = null; $defaultSetting = null; 
		$fieldSpecification = null; } :
(
	(	
		a = AMPLOWERIDENTIFIER	{ $identifier = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a)); }
	|	b = AMPUPPERIDENTIFIER	{ $identifier = new Identifier(Identifier_type.ID_ASN, $b.getText(), new Location(actualFile, $b, $b)); }
	)
	c = pr_SimplDefdUpper		{ $reference = $c.reference; }
	(	
		OPTIONAL	{ $is_optional = true; }
	|	DEFAULT
		(	
			block = BLOCK 	{ $defaultBlock = new Block($block); }//pr_Dflt_Block
		|	e = pr_RefdLower { $defaultSetting = $e.reference; }//pr_Dflt_RefdLower // This did not exist for AmpUpperIdentifier in the original parser
		)
	)?
)
{
	if($defaultBlock == null) {
		$fieldSpecification = new Undefined_FieldSpecification($identifier, $reference, $is_optional, $defaultSetting);
	} else {
		$fieldSpecification = new Undefined_FieldSpecification($identifier, $reference, $is_optional, $defaultBlock);
	}
};

pr_special_ObjectClassSyntax_Builder [FieldSpecifications fieldSpecifications]
returns [ArrayList<ObjectClassSyntax_Node> nodes]
locals [Token endcol, Identifier identifier, ObjectClassSyntax_literal literal]
@init { $identifier = null; $literal = null; $nodes = new ArrayList<ObjectClassSyntax_Node>(); }:
(	
	(
		block = BLOCK
		{
			ObjectClassSyntax_Builder builder = new ObjectClassSyntax_Builder(new Block($block), $fieldSpecifications);
			ObjectClassSyntax_sequence sequence = new ObjectClassSyntax_sequence(true, false);
			$nodes.add(sequence);
			sequence.accept(builder);
		}
	|	a = pr_special_ObjectSyntaxLiteral
			{
				$literal = $a.literal;
				if($literal != null){
					$nodes.add($literal);
				}
			}
	|	b = UPPERIDENTIFIER //literal_id
			{
				$identifier = new Identifier(Identifier_type.ID_ASN, $b.getText(), new Location(actualFile, $b, $b));
				if($identifier.isvalidAsnWord()){
					$nodes.add(new ObjectClassSyntax_literal($identifier));
				}
			}
	|	(	
			c = AMPUPPERIDENTIFIER { $identifier = new Identifier(Identifier_type.ID_ASN, $c.getText(), new Location(actualFile, $c, $c)); $endcol = $c; }
		|	d = AMPLOWERIDENTIFIER { $identifier = new Identifier(Identifier_type.ID_ASN, $d.getText(), new Location(actualFile, $d, $d)); $endcol = $d; }
		)	//ampId
		{
			if ($identifier != null) {
				if ($fieldSpecifications.hasFieldSpecificationWithId($identifier)) {
					FieldSpecification fieldSpec = $fieldSpecifications.getFieldSpecificationByIdentifier($identifier).getLast();
					ObjectClassSyntax_setting setting;
					switch (fieldSpec.getFieldSpecificationType()) {
					case FS_T:
						$nodes.add(new ObjectClassSyntax_setting(SyntaxSetting_types.S_T, $identifier));
						break;
					case FS_V_FT:
					case FS_V_VT:
						$nodes.add(new ObjectClassSyntax_setting(SyntaxSetting_types.S_V, $identifier));
						break;
					case FS_VS_FT:
					case FS_VS_VT:
						$nodes.add(new ObjectClassSyntax_setting(SyntaxSetting_types.S_VS, $identifier));
						break;
					case FS_O:
						$nodes.add(new ObjectClassSyntax_setting(SyntaxSetting_types.S_O, $identifier));
						break;
					case FS_OS:
						$nodes.add(new ObjectClassSyntax_setting(SyntaxSetting_types.S_OS, $identifier));
						break;
					case FS_ERROR:
						break;
					case FS_UNDEFINED:
					default:
						// TODO mark as error
						break;
					}
				} else {
					(new Location(actualFile, $endcol, $endcol)).reportSemanticError("No field with name `" + $identifier.getDisplayName() + "'");
				}
			}
		}
	)*
	EOF
);

pr_special_NamedBitListValue returns [Named_Bits named_bits]
@init { $named_bits = new Named_Bits(); }:
(
	pr_IdentifierList [$named_bits]
	EOF
);

pr_special_Type returns [ASN1Type type]
@init { $type = null; }:
(
	a = pr_Type { $type = $a.type; }
);

pr_special_Object returns [ASN1Object object]
@init { $object = null; }:
(
	a = pr_Object { $object = $a.object; }
);

pr_special_ObjectSet returns [ObjectSet objectSet]
@init { $objectSet = null; }:
(
	a = pr_ObjectSet  { $objectSet = $a.objectSet; }
);

pr_special_Value returns [Value value]
@init { $value = null; }:
(	
	a = pr_special_Value_subrule { $value = $a.value; }
);

pr_special_Value_subrule returns [Value value]
@init { $value = null; }:
(	// Originally pr_FixedTypeFieldValue, but moved to a separate branch not to have invalid lookaheads
	a = pr_BuiltinValue { $value = $a.value; }
|	pr_ReferencedValue_reg
|	b = pr_BuiltinValue { $value = $b.value; }
);

pr_special_Value_endCorrectingSubrule:
(	
	pr_special_Value_subrule
	( . | EOF )
	( . | EOF )
);

// SPECIAL RULE to parse any kind of references for code completion or open declaration features.

pr_parseReference returns [Reference reference]
locals [Token endCol, FieldSubReference subReference]
@init { $subReference = null; $reference = new Reference(null); }:
(	
	(
		(	a1 = LOWERIDENTIFIER
			{
				$subReference = new FieldSubReference(new Identifier(Identifier_type.ID_ASN, $a1.getText(), new Location(actualFile, $a1, $a1)));
				$subReference.setLocation(getLocation($a1));
			}
		|	b1 = UPPERIDENTIFIER
			{
				$subReference = new FieldSubReference(new Identifier(Identifier_type.ID_ASN, $b1.getText(), new Location(actualFile, $b1, $b1)));
				$subReference.setLocation(getLocation($b1));
			}
		|	c1 = AMPLOWERIDENTIFIER
			{
				$subReference = new FieldSubReference(new Identifier(Identifier_type.ID_ASN, $c1.getText(), new Location(actualFile, $c1, $c1)));
				$subReference.setLocation(getLocation($c1));
			}
		|	d1 = AMPUPPERIDENTIFIER
			{
				$subReference = new FieldSubReference(new Identifier(Identifier_type.ID_ASN, $d1.getText(), new Location(actualFile, $d1, $d1)));
				$subReference.setLocation(getLocation($d1));
			}
		)
		( BLOCK )?
		{ $reference.addSubReference($subReference); }
	)
	(	
		DOT
		(	a2 = LOWERIDENTIFIER
			{
				$subReference = new FieldSubReference(new Identifier(Identifier_type.ID_ASN, $a2.getText(), new Location(actualFile, $a2, $a2)));
				$subReference.setLocation(getLocation($a2));
			}
		|	b2 = UPPERIDENTIFIER
			{
				$subReference = new FieldSubReference(new Identifier(Identifier_type.ID_ASN, $b2.getText(), new Location(actualFile, $b2, $b2)));
				$subReference.setLocation(getLocation($b2));
			}
		|	c2 = AMPLOWERIDENTIFIER
			{
				$subReference = new FieldSubReference(new Identifier(Identifier_type.ID_ASN, $c2.getText(), new Location(actualFile, $c2, $c2)));
				$subReference.setLocation(getLocation($c2));
			}
		|	d2 = AMPUPPERIDENTIFIER
			{
				$subReference = new FieldSubReference(new Identifier(Identifier_type.ID_ASN, $d2.getText(), new Location(actualFile, $d2, $d2)));
				$subReference.setLocation(getLocation($d2));
			}
		)
		( BLOCK )?
		{ $reference.addSubReference($subReference); }
	)*
	(	i = DOT
		{
			$subReference = new FieldSubReference(new Identifier(Identifier_type.ID_ASN,""));
			$subReference.setLocation(getLocation($i));
			$reference.addSubReference($subReference);
		}
	)?
	j = EOF { $endCol = $j; }
|	k = EOF { $endCol = $k; }
	{
		$subReference = new FieldSubReference(new Identifier(Identifier_type.ID_ASN,""));
		$subReference.setLocation(getLocation($k));
		$reference.addSubReference($subReference);
	}
)
{
	$reference.setLocation(getLocation($start,$endCol));
};


pr_special_NamedNumberList returns [NamedValues namedValues]
@init { $namedValues = new NamedValues(); }:
(
	pr_NamedNumberList[$namedValues]
	EOF
);

pr_special_Enumerations returns [ASN1_Enumeration enumeration]
@init { $enumeration = new ASN1_Enumeration(); }:
(
	pr_Enumerations [$enumeration]
	EOF
);

pr_Enumerations [ASN1_Enumeration enumeration]
locals [EnumerationItems enumItems, ExceptionSpecification exceptionSpecification]
@init	{ $enumItems = null; $exceptionSpecification = null; }:
(	
	a = pr_Enumeration	{ $enumItems = $a.enumItems; $enumeration.enumItems1 = $enumItems; }
	(	
		COMMA
		ELLIPSIS	{ $enumeration.hasEllipses = true; }
		b = pr_ExceptionSpec { $exceptionSpecification = $b.exceptionSpecification; } 
		(	
			COMMA
			c = pr_Enumeration	{ $enumItems = $c.enumItems; $enumeration.enumItems2 = $enumItems; }
		)?
	)?
);

pr_Enumeration returns [EnumerationItems enumItems]
locals [EnumItem enumItem]
@init { $enumItem = null; $enumItems = new EnumerationItems(); }:
(
	a = pr_EnumerationItem 
	{
		$enumItem = $a.enumItem;
		if($enumItem != null) { $enumItems.addEnumItem($enumItem); }
	}
	(	
		COMMA
		b = pr_EnumerationItem
		{
			$enumItem = $b.enumItem;
			if($enumItem != null) { $enumItems.addEnumItem($enumItem); }
		}
	)*
);

pr_EnumerationItem returns [EnumItem enumItem]
locals [Value value]
@init { $value = null; $enumItem = null; }:
(
	a = LOWERIDENTIFIER
	(
		LPAREN
		(
			b = pr_SignedNumber	{ $value = $b.value; }
		|	c = pr_DefinedValue	{ $value = $c.value; }
		)
		d = RPAREN
	)?
)
{
	Identifier identifier = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
	$enumItem = new EnumItem(identifier, $value);
	$enumItem.setLocation(new Location(actualFile, $a, $d == null ? $a : $d));
};

pr_NamedBitList [NamedValues namedValues]
locals [NamedValue namedValue]
@init { $namedValue = null; }:
(	
	a = pr_NamedBit 
	{
		$namedValue = $a.namedValue;
		if($namedValue != null) { $namedValues.addNamedValue($namedValue); }
	}
	(
		COMMA
		b = pr_NamedBit
		{
			$namedValue = $b.namedValue;
			if($namedValue != null) { $namedValues.addNamedValue($namedValue); }
		}
	)*
);

pr_NamedBit returns [NamedValue namedValue]
locals [Value value]
@init { $value = null; $namedValue = null; }:
(	
	a = LOWERIDENTIFIER
	LPAREN
	(	
		b = pr_Val_Number	{ $value = $b.value; }
	|	c = pr_DefinedValue	{ $value = $c.value; }
	)
	d = RPAREN
)
{
	Identifier identifier = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
	$namedValue = new NamedValue(identifier, $value);
	$namedValue.setLocation(new Location(actualFile, $a, $d == null ? $a : $d));
};

pr_IdentifierList [Named_Bits named_bits]:
(
	pr_IdentifierList1[named_bits]

)?;

pr_IdentifierList1 [Named_Bits named_bits]:
(
	a = LOWERIDENTIFIER
	{
		Identifier identifier1 = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
		$named_bits.addId(identifier1);
	}
	(	
		COMMA
		b = LOWERIDENTIFIER
		{
			Identifier identifier2 = new Identifier(Identifier_type.ID_ASN, $b.getText(), new Location(actualFile, $b, $b));
			$named_bits.addId(identifier2);
		}
	)*
)
{
	$named_bits.setLocation(new Location(actualFile, $a, $b == null ? $a : $b));
};


pr_NamedNumberList [NamedValues namedValues]
locals [NamedValue namedValue]
@init { $namedValue = null; }:
(	a = pr_NamedNumber { if($a.namedValue != null) { $namedValues.addNamedValue($a.namedValue); }}
	(
		COMMA
		b = pr_NamedNumber { if($b.namedValue != null) { $namedValues.addNamedValue($b.namedValue); }}
	)*
);

pr_NamedNumber returns [NamedValue namedValue]
@init { Value value = null; $namedValue = null; }:
(	
	a = LOWERIDENTIFIER
	LPAREN
	(
		b = pr_SignedNumber { value = $b.value; }
	|	c = pr_DefinedValue { value = $c.value; }
	)
	endcol = RPAREN
)
{
	Identifier identifier = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
	$namedValue = new NamedValue(identifier, value);
	$namedValue.setLocation(new Location(actualFile, $a, $endcol == null ? $a : $endcol));
};

pr_SignedNumber returns[Integer_Value value]
@init {	$value = null; }:
(
	a = MINUS
	b = pr_Val_Number { $value = $b.value; $value.changeSign(); }
|	c = pr_Val_Number { $value = $c.value; }
);

pr_DefinedValue returns[Referenced_Value value]:
pr_DefdLower
{
	$value = new Referenced_Value($pr_DefdLower.reference);
	$value.setLocation(new Location(actualFile, $start, $pr_DefdLower.stop));
};

pr_Val_Number returns[Integer_Value value]
@init { $value = null; }:
a = NUMBER
{
	$value = new Integer_Value($a.getText());
	$value.setLocation(new Location(actualFile, $start, $a));
};


pr_DefdLower returns [Defined_Reference reference]
@init { Defined_Reference simpleReference = null; $reference = null;}:
(	
	(
		startcol1 = pr_ExtLowerRef	{ simpleReference = $startcol1.reference; }	//pr_SimplDefdLower
	|	startcol2 = pr_LowerRef		{ simpleReference = $startcol2.reference; }	//pr_SimplDefdLower
	)
	(	
		b = BLOCK  //pr_PardLower
	)?
)
{
	if($b == null) {
		$reference = simpleReference;
	} else if (simpleReference != null){
		$reference =  new Parameterised_Reference(simpleReference, new Block($b));
	}
	if($reference != null) {
		$reference.setLocation(new Location(actualFile, $start, $b == null ? $start: $b));
	}
};

pr_ExtLowerRef returns [Defined_Reference reference]
@init { Identifier id1 = null; Identifier id2 = null; $reference = null; }:
(	
	a = UPPERIDENTIFIER
	DOT
	b = LOWERIDENTIFIER
)
{
	id1 = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
	$reference = new Defined_Reference(id1);
	FieldSubReference subReference = new FieldSubReference(new Identifier(Identifier_type.ID_ASN, $b.getText(), new Location(actualFile, $b, $b)));
	subReference.setLocation(new Location(actualFile, $b, $b));
	$reference.addSubReference(subReference);
};

pr_LowerRef returns [Defined_Reference reference]
locals [Identifier id]
@init { $id = null; $reference = null; }:
(  
	a = LOWERIDENTIFIER
)
{
	$id = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
	FieldSubReference subReference = new FieldSubReference($id);
	subReference.setLocation(new Location(actualFile, $a, $a));
	ArrayList<ISubReference> subreferences = new ArrayList<ISubReference>();
	subreferences.add(subReference);
	$reference = new Defined_Reference(null, subreferences);
};

pr_Type returns [ASN1Type type]
locals [Type subType, NamedType_Helper namedType_Helper, Constraint constraint, Constraints constraints]
@init { $subType = null; $namedType_Helper = null; $constraint = null; $constraints = null; $type = null; }:
(	
	(	
		(	
			set = 		SET
		|	sequence =	SEQUENCE
		)
		(
			c1 = pr_Constraint { $constraint = $c1.constraint; }
		|	c2 = pr_SizeConstraint { $constraint = $c2.constraint; }
		)?  // pr_ConstrainedType if exists, pr_NakedType if not
		OF
		(
			(
				namedTypecol = pr_NamedType 
				{
					$namedType_Helper = $namedTypecol.helper;
					$subType = $namedType_Helper.type;
				}
			|	subTypecol = pr_Type
				{
					$subType = $subTypecol.type;
				}
			)   // pr_SeqOfSetOfWithConstraint, pr_SequenceOfType, pr_SetOfType
		)
		{
			if ($set != null) {
				$type = new SetOf_Type($subType);
				$type.setLocation(new Location(actualFile, $set, $namedTypecol.start != null ? $namedTypecol.start : $subTypecol.start));
			} else if ($sequence != null) {
				$type = new SequenceOf_Type($subType);
				$type.setLocation(new Location(actualFile, $sequence, $namedTypecol.start != null ? $namedTypecol.start : $subTypecol.start));
			}
			if ($constraint != null && $type != null) {
				$constraints = new Constraints();
				$constraints.addConstraint($constraint);
				$type.addConstraints($constraints);
			}
		}
	)
|	(
		(	
			builtin = pr_BuiltinType_reg 
			{
				$type = $builtin.type;
				$type.setLocation(new Location(actualFile, $builtin.start, $builtin.start));
			}
		|	referenced = pr_ReferencedType
			{	
				$type = $referenced.type;
				$type.setLocation(new Location(actualFile, $referenced.start, $referenced.start));
			}
		)//pr_NakedType_reg
		(
			a = pr_Constraints
			{
				$constraints = $a.constraints;
				$type.addConstraints($constraints);
			}// pr_ConstrainedType if exists, pr_NakedType if not
		)?
	)
|	pr_Tag
	b = pr_Type { $type = $b.type; }
);

//TODO: antlr2 return null check if it is the intent
pr_SizeConstraint returns[Constraint constraint]
@init { $constraint = null; }:
(	
	SIZE
	//a = pr_Constraint { $constraint = $a.constraint; }
	pr_Constraint
);

pr_Constraint returns[Constraint constraint]
locals [ExceptionSpecification exceptionSpecfication]
@init { $exceptionSpecfication = null; $constraint = null; }:
(	
	LPAREN
	a = pr_ConstraintSpec	{ $constraint = $a.constraint; }
	b = pr_ExceptionSpec	{ $exceptionSpecfication = $b.exceptionSpecification; }
	RPAREN
);

pr_ConstraintSpec returns[Constraint constraint]
@init { $constraint = null; }:
(
	a = pr_GeneralConstraint { $constraint = $a.constraint; }
|	b = pr_SubtypeConstraint { $constraint = $b.constraint; }
);

pr_GeneralConstraint returns[Constraint constraint]
@init { $constraint = null; }:
(	
	a = pr_UserDefinedConstraint{ $constraint = $a.constraint; }
|	b = pr_TableConstraint		{ $constraint = $b.constraint; }
|	c = pr_ContentsConstraint	{ $constraint = $c.constraint; }
);

pr_NamedType returns [NamedType_Helper helper]
@init { ASN1Type type = null; $helper = new NamedType_Helper(); }:
(	
	a = LOWERIDENTIFIER
	b = pr_Type { type = $b.type; }
)
{
	$helper.identifier = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
	$helper.type = type;
};

pr_Object returns [ASN1Object object]
@init { $object = null; }:
(	
	a = pr_DefinedObject	{ $object = $a.object; }
|	b = pr_ObjectDefn		{ $object = $b.object; }
);

pr_ObjectSet  returns [ObjectSet objectSet]
@init { $objectSet = null; }:
(
 	block = BLOCK
)
{
	$objectSet = new ObjectSet_definition( new Block($block) );
	$objectSet.setLocation(new Location(actualFile, $block, $block));
};

pr_BuiltinValue returns[Value value]
@init { $value = null; }:
(	
	a = pr_BuiltinValue_reg	{ $value = $a.value; }
|	b = pr_NullValue		{ $value = $b.value; }
|	c = pr_LowerIdValue		{ $value = $c.value; }
|	block = BLOCK { $value = new Undefined_Block_Value(new Block($block)); }
);

pr_ReferencedValue_reg returns[Referenced_Value value]
locals [Reference reference]
@init { $reference = null; $value = null; }:
(	
	a = pr_RefdLower_reg { $reference = $a.reference; }
)
{
	$value = new Referenced_Value($reference);
	$value.setLocation(new Location(actualFile, $a.start, $a.start));
};

pr_BuiltinType_reg returns [ASN1Type type]
@init { $type = null; }:
(	a = pr_NullType				{ $type = $a.type; }
|	b = pr_BitStringType		{ $type = $b.type; }
|	c = pr_BooleanType			{ $type = $c.type; }
|	d = pr_CharacterStringType	{ $type = $d.type; }
|	e = pr_ChoiceType			{ $type = $e.type; }
|	f = pr_SelectionType		{ $type = $f.type; }
|	g = pr_EnumeratedType		{ $type = $g.type; }
|	h = pr_IntegerType			{ $type = $h.type; }
|	i = pr_ObjectIdentifierType	{ $type = $i.type; }
|	j = pr_RelativeOIDType		{ $type = $j.type; }
|	k = pr_OctetStringType		{ $type = $k.type; }
|	l = pr_RealType				{ $type = $l.type; }
|	m = pr_SequenceType			{ $type = $m.type; }
|	n = pr_SetType				{ $type = $n.type; }
|	o = pr_EmbeddedPDVType		{ $type = $o.type; }
|	p = pr_ExternalType			{ $type = $p.type; }
|	q = pr_AnyType				{ $type = $q.type; }
|	r = pr_UsefulType			{ $type = $r.type; }
);

pr_ReferencedType returns [Referenced_Type type]
locals [Reference reference]
@init { $reference = null; }:
(	
	 a = pr_RefUpper { $reference = $a.reference; }
)
{
	$type = new Referenced_Type($reference);
	$type.setLocation(new Location(actualFile, $a.start, $a.start));
};

pr_Constraints returns[Constraints constraints]
locals [Constraint constraint]
@init { $constraint = null; $constraints = new Constraints(); }:
(	
	a = pr_Constraint
	{
		$constraint = $a.constraint;
		$constraints.addConstraint($constraint);
	}
)+;

pr_Tag:
(
	SQUAREOPEN
	pr_Class
	pr_ClassNumber
	SQUARECLOSE
	(	
		IMPLICIT
	|	EXPLICIT
	)?
);

pr_ExceptionSpec returns [ExceptionSpecification exceptionSpecification]
@init { $exceptionSpecification = null; }:
(
	EXCLAMATION
	a = pr_ExceptionIdentification { $exceptionSpecification = $a.exceptionSpecification; }
)?;

pr_SubtypeConstraint returns[Constraint constraint]
@init { $constraint = null; }:
(
	a = pr_ElementSetSpecs { $constraint = $a.constraint; }
);

// NOT YET SUPPORTED IN SEMANTIC CHECKING
pr_UserDefinedConstraint returns[Constraint constraint]
@init { $constraint = null; }:
(
	CONSTRAINED
	BY
	BLOCK
);

pr_TableConstraint returns[TableConstraint constraint]
@init { $constraint = null; }:
(
	a = pr_ComponentRelationConstraint { $constraint = $a.constraint; }
);

// NOT YET SUPPORTED IN SEMANTIC CHECKING
pr_ContentsConstraint returns[Constraint constraint]
@init {ASN1Type type = null; Value value = null; $constraint = null; }:
(	
	CONTAINING
	a = pr_Type			{ type = $a.type; }
	(	
		ENCODED BY
		b = pr_Value	{ value = $b.value; }
	)?
|	ENCODED
	BY
	c = pr_Value		{ value = $c.value; }
);

pr_DefinedObject returns [ReferencedObject object]
@init { Reference reference = null; $object = null; }:
(
	a = pr_RefdLower { reference = $a.reference; }
)
{
	$object = new ReferencedObject(reference); 
};

pr_ObjectDefn returns [Object_Definition object]
@init { $object = null; }:
(
	block = BLOCK
)
{
	$object = new Object_Definition( new Block($block) );
	$object.setLocation(new Location(actualFile, $block, $block));
};

pr_BuiltinValue_reg returns[Value value]
@init { $value = null; }:
(	
	a = BSTRING
	{
		$value = new Bitstring_Value($a.getText());
		$value.setLocation(new Location(actualFile, $a, $a));
	}
|	b = HSTRING
	{
		$value = new Hexstring_Value($b.getText());
		$value.setLocation(new Location(actualFile, $b, $b));
	}
|	c = pr_Val_CString	{ $value = $c.value; }
|	d = pr_BooleanValue	{ $value = $d.value; }
|	e = pr_ChoiceValue	{ $value = $e.value; }
|	(	
		f1 = MINUS
		(	
			f10 = pr_Val_Number
			{
				$value = $f10.value;
				((Integer_Value)$value).changeSign();
			}
		|	f11 = REALNUMBER	
			{
				$value = new Real_Value(Float.parseFloat( $f11.getText()));
				$value.setLocation(new Location(actualFile, $f11, $f11));
			}
		)
	|	(	f12 = pr_Val_Number	{ $value = $f12.value; }
		|	f13 = REALNUMBER
			{
				$value = new Real_Value(Float.parseFloat($f13.getText()));
				$value.setLocation(new Location(actualFile, $f13, $f13));
			}
		)
	)
|	g = PLUS_INFINITY
	{
		$value = new Real_Value(Float.POSITIVE_INFINITY);
		$value.setLocation(new Location(actualFile, $g, $g));
	}
|	h = MINUS_INFINITY
	{
		$value = new Real_Value(Float.NEGATIVE_INFINITY);
		$value.setLocation(new Location(actualFile, $h, $h));
	}
|   i = NOT_A_NUMBER
	{
		$value = new Real_Value(Float.NaN);
		$value.setLocation(new Location(actualFile, $i, $i));
	}
);

pr_NullValue returns[ASN1_Null_Value value]
@init { $value = null; }:
	NULL
{
	$value = new ASN1_Null_Value();
	$value.setLocation(new Location(actualFile, $start, $start));
};

pr_LowerIdValue returns[Undefined_LowerIdentifier_Value value]
@init { $value = null; }:
	a = LOWERIDENTIFIER
{
	Location location = new Location(actualFile, $a, $a);
	Identifier identifier = new Identifier(Identifier_type.ID_ASN,  $a.getText(), location);
	$value = new Undefined_LowerIdentifier_Value(identifier);
	$value.setLocation(new Location(actualFile, $a, $a));
};

pr_RefdLower_reg returns [Reference reference]
locals [Defined_Reference tempReference]
@init { $tempReference = null; $reference = null; }:
(	
	(
		a = pr_LowerRef		{ $tempReference = $a.reference; }
	|	b = pr_ExtLowerRef	{ $tempReference = $b.reference; }
	)
	(
		BLOCK //pr_PardLower
	)?
	(	
		DOT
		pr_FieldNameLower //pr_LowerFromObj
	|	{ $reference = $tempReference; }
	)
);

pr_NullType returns [NULL_Type type]
@init { $type = null; }:
(	
	NULL)
{
	$type = new NULL_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_BitStringType returns [ASN1_BitString_Type type]
locals [Token endCol]
@init { $type = null; }:
(
	a = BIT
	b = STRING	{ $endCol = $b; }
	(
		c = BLOCK { $endCol = $c; }
	)?
)
{
	$type = new ASN1_BitString_Type($c != null ? new Block($c):null);
	$type.setLocation(new Location(actualFile, $a, $endCol));
};

pr_BooleanType returns [Boolean_Type type]
@init { $type = null; }:
(
	BOOLEAN
)
{
	$type = new Boolean_Type();
	$type.setLocation(new Location(actualFile, $start,  $start));
};

pr_CharacterStringType returns [ASN1Type type]
@init { $type = null; }:
(	
	a = pr_RestrictedCharacterStringType	{ $type = $a.type; }
|	b = pr_UnrestrictedCharacterStringType	{ $type = $b.type; }
);

pr_ChoiceType returns [ASN1_Choice_Type type]
@init { $type = null; }:
(	
	CHOICE
	endCol = BLOCK
)
{
	$type = new ASN1_Choice_Type($endCol != null ? new Block($endCol) : null);
	$type.setLocation(new Location(actualFile, $start, $endCol));
};

pr_SelectionTypeType returns [ASN1Type type]
@init { $type = null; }:
(	a = pr_ReferencedType		{ $type = $a.type; }
|	b = pr_ChoiceType			{ $type = $b.type; }
|	c = pr_SelectionType		{ $type = $c.type; }
|	pr_Tag
	d = pr_SelectionTypeType	{ $type = $d.type; }
);

pr_SelectionType returns [Selection_Type type]
@init { $type = null; }:
(
	LOWERIDENTIFIER
	LESSTHAN
	a = pr_SelectionTypeType
)
{
	Identifier identifier = new Identifier(Identifier_type.ID_ASN, $start.getText(), new Location(actualFile, $start, $start));
	$type = new Selection_Type(identifier, $a.type);
	$type.setLocation(new Location(actualFile, $start, $a.start));
};

pr_EnumeratedType returns [ASN1_Enumerated_Type type]
@init { $type = null; }:
(	
	ENUMERATED
	block = BLOCK
)
{
	$type = new ASN1_Enumerated_Type($block != null ? new Block($block) : null);
	$type.setLocation(new Location(actualFile,  $start, $block));
};

pr_IntegerType returns [ASN1_Integer_Type type]
@init { $type = null; }:
(
	a = INTEGER
	(
		b = BLOCK
	)?
)
{
	$type = new ASN1_Integer_Type($b != null ? new Block($b) : null);
	$type.setLocation(new Location(actualFile, $start, $b == null ? $start:$b));
};

pr_ObjectIdentifierType returns [ObjectID_Type type]
@init { $type = null; }:
(
	OBJECT
	a = IDENTIFIER
)
{
	$type = new ObjectID_Type();
	$type.setLocation(new Location(actualFile, $start, $a));
};

pr_RelativeOIDType returns [RelativeObjectIdentifier_Type type]
@init { $type = null; }:
(
	RELATIVE_OID
)
{
	$type = new RelativeObjectIdentifier_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_OctetStringType returns [OctetString_Type type]
@init { $type = null; }:
(	
	OCTET
	a = STRING
)
{
	$type = new OctetString_Type();
	$type.setLocation(new Location(actualFile, $start, $a));
};

pr_RealType returns[Float_Type type]
@init { $type = null; }:
(
	REAL
)
{
	$type = new Float_Type();
	$type.setLocation(new Location(actualFile, $start,  $start));
};

pr_SequenceType returns [ASN1_Sequence_Type type]
@init { $type = null; }:
(	
	SEQUENCE
	block = BLOCK
)
{
	$type = new ASN1_Sequence_Type($block != null ? new Block($block):null);
	$type.setLocation(getLocation($start, $block));
};

pr_SetType returns [ASN1_Set_Type type]
@init { $type = null; }:
(	
	SET
	block = BLOCK
)
{
	$type = new ASN1_Set_Type($block != null ? new Block($block):null);
	$type.setLocation(new Location(actualFile, $start, $block));
};

pr_EmbeddedPDVType returns [Embedded_PDV_Type type]
@init { $type = null; }:
(
	EMBEDDED PDV
)
{
	$type = new Embedded_PDV_Type();
	$type.setLocation(new Location(actualFile, $start,  $start));
};

pr_ExternalType returns [External_Type type]
@init { $type = null; }:
(
	EXTERNAL
)
{
	$type = new External_Type();
	$type.setLocation(new Location(actualFile, $start,  $start));
};

pr_AnyType returns [Any_Type type]
locals [Token endCol]
@init { $type = null; }:
(
	a = ANY { $endCol = $a; }
	(	
		DEFINED BY
		b = LOWERIDENTIFIER { $endCol = $b; }
	)?
)
{
	$type = new Any_Type();
	$type.setLocation(new Location(actualFile, $a, $endCol));
};

pr_UsefulType returns [ASN1Type type]
locals [Token col]
@init { $type = null;}:
(	
	a = GENERALIZEDTIME		{ $type = new GeneralizedTime_Type(); $col = $a;}
|	b = UTCTIME				{ $type = new UTCTime_Type(); $col = $b;}
|	c = OBJECTDESCRIPTOR	{ $type = new ObjectDescriptor_Type(); $col = $c;}
)
{
	if ($type != null) {
		$type.setLocation(new Location(actualFile, $col, $col));
	}
};

pr_RefUpper returns [Reference reference]
@init { $reference = null; }:
(	
	a = pr_FromObjs		{ $reference = $a.fromObj; }
|	b = pr_UpperFromObj	{ $reference = $b.fromObj; }
|	c = pr_DefdUpper	{ $reference = $c.reference; }
);

pr_Class:
(	
	UNIVERSAL
|	APPLICATION
|	PRIVATE
)?;

pr_ClassNumber:
(
	pr_Val_Number
|	pr_DefinedValue
);

pr_ExceptionIdentification returns [ExceptionSpecification exceptionSpecification]
@init { ASN1Type type = null; Value value = null; $exceptionSpecification = null; }:
(	
	a = pr_SignedNumber
	{
		if ($a.value != null) { $exceptionSpecification = new ExceptionSpecification(null, $a.value); }
	}
|	b = pr_Type
	COLON
	c = pr_Value
	{
		if($c.value != null) { $exceptionSpecification = new ExceptionSpecification($b.type, $c.value); }
	}
|	d = pr_DefinedValue
	{
		if($d.value != null) { $exceptionSpecification = new ExceptionSpecification(null, $d.value); }
	}
);

pr_ComponentRelationConstraint returns[TableConstraint constraint]
@init { $constraint = null; }:
(
	objectSetBlock = BLOCK
	atNotationsBlock = BLOCK
)
{
	$constraint = new TableConstraint(new Block($objectSetBlock), new Block($atNotationsBlock));
};

pr_Value returns[Value value]
@init { $value = null; }:
(
	a = pr_ObjectClassFieldValue { $value = $a.value; }
);

pr_RefdLower returns [Reference reference]
@init {
	Defined_Reference definedReference = null;
	FieldName field = null;
	$reference = null;
}:
(
	a = pr_DefdLower 
	(
		DOT
		b = pr_FieldNameLower //	pr_LowerFromObj
			{
				$reference = new InformationFromObj($a.reference, $b.fieldName);
			}
	|	{ $reference = $a.reference; }
	)
);

pr_Val_CString returns[Charstring_Value value]
@init { $value = null; }:
	CSTRING
{
	$value = new Charstring_Value($start.getText());
	$value.setLocation(new Location(actualFile, $start, $start));
};

pr_BooleanValue returns[Boolean_Value value]
locals [Token col]
@init { $value = null; }:
(
	a = TRUE	{ $col = $a; $value = new Boolean_Value(true); }
|	b = FALSE	{ $col = $b; $value = new Boolean_Value(false); }
)
{
	if ($value != null) {
		$value.setLocation(new Location(actualFile, $col, $col));
	}
};

pr_ChoiceValue returns [Choice_Value value]
@init {Identifier identifier = null; $value = null; }:
(
	LOWERIDENTIFIER
	COLON
	a = pr_Value
)
{
	identifier = new Identifier(Identifier_type.ID_ASN, $start.getText(), new Location(actualFile, $start, $start));
	if ($a.value != null) {
		$value = new Choice_Value(identifier, $a.value);
		$value.setLocation(new Location(actualFile, $start, $a.start));
	}
};

pr_FieldNameLower returns [FieldName fieldName]
@init { $fieldName = new FieldName(); }:
(
	a = AMPLOWERIDENTIFIER
		{
			Location location1 = new Location(actualFile, $a, $a);
			Identifier identifier1 = new Identifier(Identifier_type.ID_ASN, $a.getText(), location1);
			$fieldName.addField(identifier1);
		}
	(
		DOT
		b = AMPLOWERIDENTIFIER
		{
			Location location2 = new Location(actualFile, $b, $b);
			Identifier identifier2 = new Identifier(Identifier_type.ID_ASN, $b.getText(), location2);
			$fieldName.addField(identifier2);
		}
	)*
);

pr_RestrictedCharacterStringType returns [ASN1Type type]
@init { $type = null; }:
(	a = pr_GeneralString	{ $type = $a.type; }
|	b = pr_BMPString		{ $type = $b.type; }
|	c = pr_GraphicString	{ $type = $c.type; }
|	d = pr_IA5String		{ $type = $d.type; }
|	e = pr_NumericString	{ $type = $e.type; }
|	f = pr_PrintableString	{ $type = $f.type; }
|	g = pr_TeletexString	{ $type = $g.type; }
|	h = pr_UniversalString	{ $type = $h.type; }
|	i = pr_UTF8String		{ $type = $i.type; }
|	j = pr_VideoTextString	{ $type = $j.type; }
|	k = pr_VisibleString	{ $type = $k.type; }
);

pr_RestrictedCharacterStringValue:
(
	CSTRING
|	pr_CharacterStringList
|	pr_Quadruple
|	pr_Tuple
);

pr_CharacterStringList:
(
	BEGINCHAR pr_CharSyms ENDCHAR
);

pr_CharSyms:
(
	pr_CharsDefn (COMMA pr_CharsDefn)*
);

pr_CharsDefn:
(
	CSTRING
|	pr_Quadruple
|	pr_Tuple
|	pr_DefinedValue // returns object ????
);

pr_Quadruple:
(
	BEGINCHAR pr_Group COMMA pr_Plane COMMA pr_Row COMMA pr_Cell ENDCHAR
);

pr_Group:
(
	NUMBER	
);

pr_Plane:
(
	NUMBER
);

pr_Row:
(
	NUMBER
);

pr_Cell:
(
	NUMBER
);

pr_Tuple:
(
	BEGINCHAR pr_TableColumn COMMA pr_TableRow ENDCHAR
);

pr_TableColumn:
(
	NUMBER
);

pr_TableRow:
(
	NUMBER
);

pr_UnrestrictedCharacterStringType returns [UnrestrictedString_Type type]
@init { $type = null; }:
(
	CHARACTER
	a = STRING
)
{
	$type = new UnrestrictedString_Type();
	$type.setLocation(new Location(actualFile, $start, $a));
};

pr_FromObjs returns [InformationFromObj fromObj]
@init { $fromObj = null; }:
(
	a = pr_Lower_FromObjs	{ $fromObj = $a.fromObj; }
|	b = pr_Upper_FromObjs	{ $fromObj = $b.fromObj; }
);

pr_UpperFromObj returns [InformationFromObj fromObj]
@init { $fromObj = null; }:
(	
	a = pr_DefdLower
	DOT
	b = pr_FieldNameUpper
)
{
	$fromObj = new InformationFromObj($a.reference, $b.fieldName);
	$fromObj.setLocation(new Location(actualFile, $a.start, $b.start));
};

pr_DefdUpper returns [Reference reference]
@init { $reference = null; }:
(
	a = pr_SimplDefdUpper
	(	
		block = BLOCK
		{
			if($a.reference != null) { $reference = new Parameterised_Reference($a.reference, $block != null ? new Block($block):null); }
		}
	|	{ $reference = $a.reference; }
	)
)
{
	if($reference != null){
		$reference.setLocation(new Location(actualFile, $a.start, $block != null ? $block:$a.start));
	}
};

pr_RootElementSetSpec returns[Constraint constraint]
@init { $constraint = null; }:
(
	a = pr_ElementSetSpec { $constraint = $a.constraint; }
);

pr_AdditionalElementSetSpec:
(
	pr_ElementSetSpec
);

pr_ObjectClassFieldValue returns[Value value]
@init { $value = null; }:
(
	a = pr_FixedTypeFieldValue { $value = $a.value; }
);

pr_GeneralString returns [GeneralString_Type type]
@init { $type = null; }:
(	
	GENERALSTRING
)
{
	$type = new GeneralString_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_BMPString returns [BMPString_Type type]
@init { $type = null; }:
(	
	BMPSTRING
)
{
	$type = new BMPString_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_GraphicString returns [GraphicString_Type type]
@init { $type = null; }:
(
	GRAPHICSTRING
)
{
	$type = new GraphicString_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_IA5String returns [IA5String_Type type]
@init { $type = null; }:
(
	IA5STRING
)
{
	$type = new IA5String_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_NumericString returns [NumericString_Type type]
@init { $type = null; }:
(	
	NUMERICSTRING
)
{
	$type = new NumericString_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_PrintableString returns [PrintableString_Type type]
@init { $type = null; }:
(	
	PRINTABLESTRING
)
{
	$type = new PrintableString_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_TeletexString returns [TeletexString_Type type]
@init { $type = null; }:
(	
	TELETEXSTRING
|	T61STRING
)
{
	$type = new TeletexString_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_UniversalString returns [UniversalString_Type type]
@init { $type = null; }:
(	
	UNIVERSALSTRING
)
{
	$type = new UniversalString_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_UTF8String returns [UTF8String_Type type]
@init { $type = null; }:
(	
	UTF8STRING
)
{
	$type = new UTF8String_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_VideoTextString returns [VideotexString_Type type]
@init { $type = null; }:
(	
	VIDEOTEXSTRING
)
{
	$type = new VideotexString_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_VisibleString returns [VisibleString_Type type]
@init { $type = null; }:
(	
	VISIBLESTRING
|	ISO646STRING
)
{
	$type = new VisibleString_Type();
	$type.setLocation(new Location(actualFile, $start, $start));
};

pr_SimplDefdUpper returns [Defined_Reference reference]
@init { Identifier first = null; $reference = null; }:
(	
	a = UPPERIDENTIFIER
	{	
		first = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
	}
	(
		DOT
		b = UPPERIDENTIFIER
			{
				$reference = new Defined_Reference(first);
				FieldSubReference subReference= new FieldSubReference(new Identifier(Identifier_type.ID_ASN, $b.getText(), new Location(actualFile, $b, $b)));
				subReference.setLocation(new Location(actualFile, $b, $b));
				$reference.addSubReference(subReference);
			}
	|		{
				FieldSubReference subReference= new FieldSubReference(first);
				subReference.setLocation(new Location(actualFile, $a, $a));
				ArrayList<ISubReference> subreferences = new ArrayList<ISubReference>();
				subreferences.add(subReference);
				$reference = new Defined_Reference(null, subreferences);
			}
	)
)
{
	if ($reference != null) {
		$reference.setLocation(new Location(actualFile, $a, $b != null ? $b:$a));
	}
};

pr_Lower_FromObjs returns [InformationFromObj fromObj]
@init { $fromObj = null; }:
(
	a = pr_DefdLower
	DOT
	b = pr_FieldNameUppers
)
{
	$fromObj = new InformationFromObj($a.reference, $b.fieldName);
	$fromObj.setLocation(new Location(actualFile, $a.start, $b.start));
};

pr_Upper_FromObjs  returns[InformationFromObj fromObj]
locals [Token end, Defined_Reference ref, FieldName fieldName]
@init { $ref = null; $fieldName = null; $fromObj = null; }:
(
	a = pr_SimplDefdUpper	{$ref = $a.reference; $end = $a.start;}
	(
		block = BLOCK	//if there is block then pr_PardUpper_FromObjs, if not pr_SimplUpper_FromObjs
		{
			$ref = new Parameterised_Reference($a.reference, new Block($a.start));
			$end = $block;
		}
	)?
	DOT
	(	
		b1 = pr_FieldNameLower	{ $end = $b1.start; $fieldName = $b1.fieldName; }
	|	b2 = pr_FieldNameUpper	{ $end = $b2.start; $fieldName = $b2.fieldName; }
		(	
			DOT
			(	
				c = AMPLOWERIDENTIFIER
				{
					Location location = new Location(actualFile, $c, $c);
					Identifier identifier = new Identifier(Identifier_type.ID_ASN, $c.getText(), location);
					$fieldName.addField(identifier);
					$end = $c;
				}
			|	d = AMPUPPERIDENTIFIER
				{
					Location location = new Location(actualFile, $d, $d);
					Identifier identifier = new Identifier(Identifier_type.ID_ASN, $d.getText(), location);
					$fieldName.addField(identifier);
					$end = $d;
				}
			)
		)*
	)
)
{
	$fromObj = new InformationFromObj($ref, $fieldName);
	$fromObj.setLocation(new Location(actualFile, $a.start, $end));
};

pr_FieldNameUpper returns [FieldName fieldName]
@init { $fieldName = new FieldName(); }:
(
	a = AMPUPPERIDENTIFIER
	{
		Location location1 = new Location(actualFile, $a, $a);
		Identifier identifier1 = new Identifier(Identifier_type.ID_ASN, $a.getText(), location1);
		$fieldName.addField(identifier1);
	}
	(
		DOT
		b = AMPUPPERIDENTIFIER
		{
			Location location2 = new Location(actualFile, $b, $b);
			Identifier identifier2 = new Identifier(Identifier_type.ID_ASN, $b.getText(), location2);
			$fieldName.addField(identifier2);
		}
	)*
);

pr_ElementSetSpecs returns[Constraint constraint]
@init { $constraint = null; }:
(	
	a = pr_RootElementSetSpec	{ $constraint = $a.constraint; }
	(
		COMMA
		ELLIPSIS { $constraint = null; }
		(
			COMMA
			pr_AdditionalElementSetSpec
		)?
	)?
);

pr_ElementSetSpec returns[Constraint constraint]
@init { $constraint = null; }:
(
	a = pr_Unions { $constraint = $a.constraint; }
|	ALL
	pr_Exclusions
);

pr_FixedTypeFieldValue returns[Value value]
@init { $value = null; }:
(
	a = pr_ReferencedValue_reg	{ $value = $a.value ;}
|	b = pr_BuiltinValue			{ $value = $b.value ;}
);

pr_FieldNameUppers returns [FieldName fieldName]
locals [Token endcol]
@init { $fieldName = null; }:
(	
	col = pr_FieldNameUpper { $fieldName = $col.fieldName; }
	(	
		DOT
		(	
			a = AMPLOWERIDENTIFIER
			{
				$endcol = $a;
				Location location = new Location(actualFile, $a, $a);
				Identifier identifier = new Identifier(Identifier_type.ID_ASN, $a.getText(), location);
				$fieldName.addField(identifier);
			}
		|	b = AMPUPPERIDENTIFIER
			{
				$endcol = $b;
				Location location = new Location(actualFile, $b, $b);
				Identifier identifier = new Identifier(Identifier_type.ID_ASN, $b.getText(), location);
				$fieldName.addField(identifier);
			}
		)
	)+
);

pr_Unions returns[Constraint constraint]
@init { $constraint = null; }:
(	
	a = pr_Intersections	{ $constraint = $a.constraint; }
	(	
		pr_UnionMark
		pr_Intersections	{ $constraint = null; }
	)*
);

pr_Exclusions:
(
	EXCEPT
	pr_Elements
);

pr_Intersections returns[Constraint constraint]
@init { $constraint = null; }:
(	
	a = pr_IntersectionElements { $constraint = $a.constraint; }
	(	
		pr_IntersectionMark
		pr_IntersectionElements	{ $constraint = null; }
	)*
);

pr_UnionMark:
(
	VERTICAL
|	UNION
);

pr_Elements returns[Constraint constraint]
@init { $constraint = null; }:
(
	a = pr_SubtypeElements	{ $constraint = $a.constraint; }
|	LPAREN
	pr_ElementSetSpec
	RPAREN
);

pr_IntersectionElements returns[Constraint constraint]
@init { $constraint = null; }:
(	
	a = pr_Elements 	{ $constraint = $a.constraint; }
	(
		pr_Exclusions	{ $constraint = null; }
	)?
);

pr_IntersectionMark:
(
	CARET
|	INTERSECTION
);

pr_SubtypeElements returns[Constraint constraint]
locals [Token col]
@init { $constraint = null; }:
(	
	a = pr_ValueRange			{ $col = $a.start; }
|	b = pr_TypeConstraint		{ $col = $b.start; }
|	c = pr_SingleValue			{ $col = $c.start; $constraint = $c.constraint; }
|	d = pr_ContainedSubtype		{ $col = $d.start; }
|	e = pr_PermittedAlphabet	{ $col = $e.start; }
|	f = pr_SizeConstraint		{ $col = $f.start; }
|	g = pr_InnerTypeConstraints	{ $col = $g.start; }
|	h = pr_PatternConstraint	{ $col = $h.start; }
);

pr_ValueRange:
(
	pr_LowerEndPoint
	RANGESEPARATOR
	pr_UpperEndPoint
);

pr_TypeConstraint:
(
	pr_Type
);

pr_SingleValue returns[Constraint constraint]
locals [Token col]
@init { $constraint = null; }:
(	
	a = pr_Value_reg			{ $col = $a.start; }
|	b = pr_ReferencedValue_reg	{ $col = $b.start; }
|	c = pr_LowerIdValue			{ $col = $c.start; }
|	block = BLOCK	{ $constraint = new TableConstraint(new Block($block), null); $col = $block;}
//|	pr_ReferencedValue_reg
);

pr_ContainedSubtype:
(	
	INCLUDES
	pr_Type
);

pr_PermittedAlphabet:
(
	FROM
	pr_Constraint
);

pr_InnerTypeConstraints:
(
	WITH
	(
		COMPONENT
		pr_SingleTypeConstraint
	|	COMPONENTS
		pr_MultipleTypeConstraint
	)
);

pr_PatternConstraint:
(
	PATTERN
	pr_Value
);

pr_LowerEndPoint:
(
	pr_LowerEndValue
	(
		LESSTHAN
	)?
);

pr_UpperEndPoint:
(
	pr_UpperEndValue
|	LESSTHAN
	pr_UpperEndValue
);

pr_Value_reg returns[Value value]
@init { $value = null; }:
(
	a = pr_BuiltinValue_reg { $value = $a.value; }
);

pr_SingleTypeConstraint:
(
	pr_Constraint
);

pr_MultipleTypeConstraint:
(
	BLOCK
);

pr_LowerEndValue:
(
	pr_Value
|	MIN
);

pr_UpperEndValue:
(	
	pr_Value
|	MAX
);

pr_FixedTypeValueFieldSpec  returns [FixedTypeValue_FieldSpecification fieldSpecification]
locals [Token referenceEnd, Identifier identifier, ASN1Type type, boolean is_unique, boolean is_optional,
		boolean has_default, Value defaultValue, InformationFromObj informationFromObj, Defined_Reference simpleReference,
		Reference reference, FieldName fieldName]
@init { $fieldSpecification = null; $identifier = null; $type = null; $is_unique = false; $is_optional = false;
		$has_default = false; $defaultValue = null; $informationFromObj = null; $simpleReference = null;
		$reference =null; $fieldName = null; }:
(
	id = AMPLOWERIDENTIFIER	{ $identifier = new Identifier(Identifier_type.ID_ASN, $id.getText(), new Location(actualFile, $id, $id));}
	(
		( 
			a = pr_Type_reg		{ $type = $a.type; }
		|	b = pr_UpperFromObj	{ $informationFromObj =$b.fromObj; $type = new Referenced_Type($informationFromObj); }
		)
		a1 = pr_UNIQ_opt		{ $is_unique = $a1.is_unique ; }
		(
			a11 = OPTIONAL		{ $is_optional = true; }
		|	a12 = pr_Dflt_Value	{ $defaultValue = $defaultValue; $has_default = true; }
		)?
	|	(
			referenceStart = pr_SimplDefdUpper	{ $simpleReference = $referenceStart.reference; }//it may be defdUpper too
			(
				block = BLOCK	{ $reference = new Parameterised_Reference($simpleReference, new Block($block)); $referenceEnd = $block; }//pr_PardUpper
				(
					(
						c1 = UNIQUE			{ $is_unique = true; }
						(	
							c11 = OPTIONAL	{ $is_optional = true; }
						|	DEFAULT			{ $has_default = true; }
							(	
								c121 = pr_Value_reg { $defaultValue = $c121.value; }//pr_Dflt_Value_reg
							|	c122 = pr_NullValue { $defaultValue = $c122.value; }//pr_Dflt_NullValue
							|	defaultBlock1 = BLOCK { $defaultValue = new Undefined_Block_Value(new Block($defaultBlock1));
														$defaultValue.setLocation(new Location(actualFile, $defaultBlock1, $defaultBlock1)); }//pr_Dflt_Block
							|	pr_RefdLower_reg //pr_Dflt_RefdLower_reg
							|	lowerid1 = LOWERIDENTIFIER 
								{
									Identifier tempId = new Identifier(	Identifier_type.ID_ASN, $lowerid1.getText(),
																		new Location(actualFile, $lowerid1, $lowerid1));
																		$defaultValue = new Undefined_LowerIdentifier_Value(tempId);
																		$defaultValue.setLocation(new Location(actualFile, $lowerid1, $lowerid1));
								}//pr_Dflt_LowerId
							)
						)?
					)
				|	(
						d1 = OPTIONAL	{ $is_optional = true; }
					|	d2 = DEFAULT	{ $has_default = true; }
						(
							d21 = pr_Value_reg { $defaultValue = $d21.value; }//pr_Dflt_Value_reg
						|	d22 = pr_NullValue { $defaultValue = $d22.value; }//pr_Dflt_NullValue
						|	pr_RefdLower_reg
						|	LOWERIDENTIFIER //pr_Dflt_LowerId
						|	defaultBlock2 = BLOCK {	$defaultValue = new Undefined_Block_Value(new Block($defaultBlock2));
													$defaultValue.setLocation(new Location(actualFile, $defaultBlock2, $defaultBlock2));}//pr_Dflt_Block
						)
					|	pr_RefdLower_reg
					|	DOT
						(
							d41 = pr_FieldNameLower	{ $fieldName = $d41.fieldName; $referenceEnd = $d41.start; }
						|	d42 = pr_FieldNameUpper	{ $fieldName = $d42.fieldName; }
							(
								DOT
								(
									d411 = AMPLOWERIDENTIFIER
									{
										Location location = new Location(actualFile, $d411, $d411);
										Identifier identifier2 = new Identifier(Identifier_type.ID_ASN, $d411.getText(), location);
										$fieldName.addField(identifier2);
										$referenceEnd = $d411;
									}
								|	d412 = AMPUPPERIDENTIFIER
									{
										Location location = new Location(actualFile, $d412, $d412);
										Identifier identifier2 = new Identifier(Identifier_type.ID_ASN, $d412.getText(), location);
										$fieldName.addField(identifier2);
										$referenceEnd = $d412;
									}
								)
							)*
						)  // pr_PardUpper_FromObjs
						{
							$reference = new InformationFromObj((Defined_Reference)$reference, $fieldName);
							$reference.setLocation(new Location(actualFile, $referenceStart.start, $referenceEnd));
						}
						d43 = pr_UNIQ_opt { $is_unique = $d43.is_unique; }
						(
							OPTIONAL	{ $is_optional = true; }
						|	d44 = pr_Dflt_Value	{ $defaultValue = $d44.value; $has_default = true; }
						|
						)
					)?
				)
			|	UNIQUE	{ $is_unique = true; $reference = $simpleReference; }
				(
					OPTIONAL	{ $is_optional = true; }
				|	DEFAULT		{ $has_default = true; }
					(
						e4 = pr_Value_reg { $defaultValue = $e4.value; }//pr_Dflt_Value_reg
					|	e5 = pr_NullValue { $defaultValue = $e5.value; }//pr_Dflt_NullValue
					|	defaultBlock3 = BLOCK { $defaultValue = new Undefined_Block_Value(new Block($defaultBlock3));
												$defaultValue.setLocation(new Location(actualFile, $defaultBlock3, $defaultBlock3)); }//pr_Dflt_Block
					|	pr_RefdLower_reg //pr_Dflt_RefdLower_reg
					|	lowerid2 = LOWERIDENTIFIER { 	Identifier tempId = new Identifier(Identifier_type.ID_ASN, $lowerid2.getText(), 
														new Location(actualFile, $lowerid2, $lowerid2));
														$defaultValue = new Undefined_LowerIdentifier_Value(tempId);
														$defaultValue.setLocation(new Location(actualFile, $lowerid2, $lowerid2)); }//pr_Dflt_LowerId
					)
				)?
			|	(	
					DEFAULT	{ $has_default = true; }
					(
						f11 = pr_Value_reg { $defaultValue = $f11.value; }//pr_Dflt_Value_reg
					|	f12 = pr_NullValue { $defaultValue = $f12.value; }//pr_Dflt_NullValue
					)
				|	DOT
					(	
						a2 = pr_FieldNameLower	{ $fieldName = $a2.fieldName; $referenceEnd = $a2.start; }
					|	a21 = pr_FieldNameUpper { $fieldName = $a21.fieldName; }
						(	DOT
							(	
								b2 = AMPLOWERIDENTIFIER
								{
									Location location = new Location(actualFile, $b2, $b2);
									Identifier identifier2 = new Identifier(Identifier_type.ID_ASN, $b2.getText(), location);
									$fieldName.addField(identifier2);
									$referenceEnd = $b2;
								}
							|	c2 = AMPUPPERIDENTIFIER
								{
									Location location = new Location(actualFile, $c2, $c2);
									Identifier identifier2 = new Identifier(Identifier_type.ID_ASN, $c2.getText(), location);
									$fieldName.addField(identifier2);
									$referenceEnd = $c2;
								}
							)
						)*		// pr_FieldNameUppers if >0 occurrences
					)
					{
						$reference = new InformationFromObj((Defined_Reference)$reference, $fieldName);
						$reference.setLocation(new Location(actualFile, $referenceStart.start, $referenceEnd));
					}
					f2 =pr_UNIQ_opt { $is_unique = $f2.is_unique; }
					(	
						OPTIONAL	{ $is_optional = true; }
					|	f4 = pr_Dflt_Value	{ $defaultValue = $f4.value; $has_default = true; }
					)?
				)
				{
					$reference = $simpleReference;
				}
			)
		)
	)
)
{
	if($reference != null){
		$type = new Referenced_Type($reference);
	}
	$fieldSpecification = new FixedTypeValue_FieldSpecification($identifier, $type, $is_unique, $is_optional, $has_default, $defaultValue);
};

pr_UNIQ_opt returns [boolean is_unique]
@init { $is_unique = false; } :
(
	UNIQUE { $is_unique = true; }
|
);

pr_Dflt_Value returns [Value value]
@init { $value = null; }:
(	
	DEFAULT
	a = pr_Value { $value = $a.value; }
);

pr_special_SeqOfValue returns [SequenceOf_Value value]
@init { $value = null; }:
(
	a = pr_SeqOfValue { $value = $a.value; }
	EOF
);

pr_SeqOfValue returns [SequenceOf_Value value]
locals [Values values]
@init { $values = null; $value = null; }:
(
	a = pr_ValueList0 { $values = $a.values; }
)
{
	if ($values != null) {
		$value = new SequenceOf_Value($values);
		$value.setLocation(new Location(actualFile, $a.start, $a.start));
	}
};

pr_ValueList0 returns [Values values]
@init { $values = null; }:
(
	(
		a = pr_ValueList { $values = $a.values; }
	|	{ $values = new Values(false); }
	)
);

pr_ValueList returns [Values values]
locals [Value value]
@init	{ $value = null; $values = new Values(false); }:
(
	a = pr_Value
	{
		$value = $a.value;
		if ($value != null) { $values.addValue($value); }
	}
	(
		COMMA
		b = pr_Value
		{
			$value = $b.value;
			if ($value != null) { $values.addValue($value); }
		}
	)*
);

pr_special_SetOfValue returns [SetOf_Value value]
@init { $value = null; }:
(
	a = pr_SetOfValue { $value = $a.value; }
	EOF
);

pr_SetOfValue returns [SetOf_Value value]
locals [Values values]
@init { $values = null; $value = null; }:
(
	a = pr_ValueList0 { $values = $a.values; }
)
{
	if ($values != null) {
		$value = new SetOf_Value($values);
		$value.setLocation(new Location(actualFile, $a.start, $a.start));
	}
};

pr_special_SequenceValue returns [Sequence_Value value]
@init { $value = null; }:
(
	a = pr_SequenceValue { $value = $a.value; }
	EOF
);

pr_SequenceValue returns [Sequence_Value value]
locals [NamedValues namedValues]
@init { $namedValues = null; $value = null; }:
(	
	(
		a = pr_ComponentValueList { $namedValues = $a.namedValues; }
	)?
)
{
	if ($namedValues != null) {
		$value = new Sequence_Value($namedValues);
		$value.setLocation(new Location(actualFile, $a.start, $a.start));
	}
};

pr_ComponentValueList returns [NamedValues namedValues]
locals [NamedValue namedValue]
@init { $namedValue = null; $namedValues = new NamedValues(); }:
(
	a = pr_NamedValue 
	{ 
		$namedValue = $a.namedValue;
		if($namedValue != null) {  $namedValues.addNamedValue($namedValue); }
	}
	(	
		COMMA
		b = pr_NamedValue
		{
			$namedValue = $b.namedValue;
			if($namedValue != null) { $namedValues.addNamedValue($namedValue); }
		}
	)*
);

pr_NamedValue returns [NamedValue namedValue]
locals [Value value]
@init	{ $value = null; $namedValue = null; }:
(
	a = LOWERIDENTIFIER
	b = pr_Value { $value = $b.value; }
)
{
	Identifier identifier = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
	$namedValue = new NamedValue(identifier, $value);
	$namedValue.setLocation(new Location(actualFile, $a, $b.start == null ? $a : $b.start));
};

pr_special_SetValue returns [Set_Value value]
@init { $value = null; }:
(
	a = pr_SetValue { $value = $a.value; }
	EOF
);

pr_SetValue returns [Set_Value value]
locals [NamedValues namedValues]
@init	{ $namedValues = null; $value = null; }:
(	
	(
		a = pr_ComponentValueList { $namedValues = $a.namedValues; }
	)?
)
{
	if ($namedValues != null) {
		$value = new Set_Value($namedValues);
		$value.setLocation(new Location(actualFile, $a.start, $a.start));
	}
};

pr_special_ObjectIdentifierValue returns[ObjectIdentifier_Value value]
@init { $value = null; }:
(
	a = pr_ObjectIdentifierValue { $value = $a.value; }
	EOF
);

pr_ObjectIdentifierValue returns[ObjectIdentifier_Value value]
@init { $value = new ObjectIdentifier_Value(); }:
	a = pr_ObjIdComponentList[$value]
{
	$value.setLocation(new Location(actualFile, $a.start, $a.start));
};

pr_ObjIdComponentList [ObjectIdentifier_Value value]
locals [ObjectIdentifierComponent objidComponent]
@init { $objidComponent = null; }:
(
	a = pr_ObjIdComponent { $objidComponent = $a.objidComponent; }
	{
		if($objidComponent != null) {
			$value.addObjectIdComponent($objidComponent);
		}
	}
	
)+;

pr_ObjIdComponent returns [ObjectIdentifierComponent objidComponent]
locals [Value value]
@init { $value = null; $objidComponent = null; }:
(	
	a = pr_ObjIdComponentNumber { $objidComponent = $a.objidComponent; }
|	b = LOWERIDENTIFIER	
	{
		Identifier identifier = new Identifier(Identifier_type.ID_ASN, $b.getText(), 
								new Location(actualFile, $b, $b));
								$objidComponent = new ObjectIdentifierComponent(identifier, null);
								$objidComponent.setLocation(new Location(actualFile, $b, $b));
	}
|	c = pr_DefinedValue_reg
	{
		$value = $c.value;
		if ($value != null) {
			$objidComponent = new ObjectIdentifierComponent($value);
			$objidComponent.setLocation(new Location(actualFile, $c.start, $c.start));
		}
	}
);

pr_ObjIdComponentNumber returns [ObjectIdentifierComponent objidComponent]
locals [Value value]
@init { $value = null; $objidComponent = null; }:
(
	a = pr_NameAndNumberForm	{ $objidComponent = $a.objidComponent; }
|	b = pr_Val_Number			{ $value = $b.value; }
	{
		if ($value != null) {
			$objidComponent = new ObjectIdentifierComponent($value);
			$objidComponent.setLocation(new Location(actualFile, $b.start, $b.start));
		}
	}
);

pr_DefinedValue_reg returns [Referenced_Value value]
locals [Reference reference]
@init { $reference = null; $value = null; }:
(
	a = pr_DefdLower_reg { $reference = $a.reference; }
)
{
	$value = new Referenced_Value($reference);
	$value.setLocation(new Location(actualFile, $a.start, $a.start));
};

pr_NameAndNumberForm returns[ObjectIdentifierComponent objidComponent]
locals [Value value]
@init { $value = null; $objidComponent = null; }:
(
	a = LOWERIDENTIFIER
	LPAREN
	(	
		b = pr_Val_Number	{ $value = $b.value; }	//pr_DefinitiveNameAndNumberForm
	|	c = pr_DefinedValue	{ $value = $c.value; }
	)
	d = RPAREN
)
{
	Identifier identifier = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
	$objidComponent = new ObjectIdentifierComponent(identifier, $value);
	$objidComponent.setLocation(new Location(actualFile, $a, $d));
};

pr_DefdLower_reg returns [Reference reference]
locals [Defined_Reference simpleReference]
@init { $simpleReference = null; $reference = null; }:
(
	a = pr_LowerRef	{ $simpleReference = $a.reference; }				//pr_PardLower -> pr_SimplDefdLower
	block1 = BLOCK	{ $reference = new Parameterised_Reference($simpleReference, new Block($block1) ); }
|	(
		b = pr_ExtLowerRef	{ $simpleReference = $b.reference; }	//pr_SimplDefdLower_reg
		(
			block2 = BLOCK										//pr_PardLower -> pr_SimplDefdLower
			{ $reference = new Parameterised_Reference($simpleReference, new Block($block2) ); }
		|	{ $reference = $simpleReference; }
		)
	)
);

pr_special_RelativeObjectIdentifierValue returns[RelativeObjectIdentifier_Value value]
@init { $value = null; }:
(
	a = pr_RelativeOIDValue { $value = $a.value; }
	EOF
);

pr_RelativeOIDValue returns[RelativeObjectIdentifier_Value value = new RelativeObjectIdentifier_Value()]
@init { $value = new RelativeObjectIdentifier_Value(); } :
	a = pr_RelativeOIDComponentList[$value]
{
	$value.setLocation(new Location(actualFile, $a.start, $a.start));
};

pr_RelativeOIDComponentList [RelativeObjectIdentifier_Value value]
locals [ObjectIdentifierComponent objidComponent]
@init { $objidComponent = null; }:
(
	a = pr_RelativeOIDComponent
	{
		$objidComponent = $a.objidComponent;		
		if($objidComponent != null) {
			$value.addObjectIdComponent($objidComponent);
		}
	}
)+;

pr_RelativeOIDComponent returns [ObjectIdentifierComponent objidComponent]
locals [Value value]
@init { $value = null; $objidComponent = null; }:
(	
	a = pr_ObjIdComponentNumber { $objidComponent = $a.objidComponent; }
|	b = pr_DefinedValue
	{
		$value = $b.value;
		if ($value != null) {
			$objidComponent = new ObjectIdentifierComponent($value);
			$objidComponent.setLocation(new Location(actualFile, $b.start, $b.start));}
		}
);

pr_special_FormalParameterList returns [ArrayList<FormalParameter_Helper> parameters]
locals [FormalParameter_Helper helper]
@init { $helper = null; $parameters = new ArrayList<FormalParameter_Helper>(); }:
(
	a = pr_FormalParameter		{ $helper = $a.formalParameter;  $parameters.add($helper); }
	(	
		COMMA
		b = pr_FormalParameter	{ $helper = $b.formalParameter; $parameters.add($helper); }
	)*
	EOF
);

pr_FormalParameter returns [FormalParameter_Helper formalParameter]
@init { $formalParameter = new FormalParameter_Helper(); }:
(	
	(
		governorToken =  .  { $formalParameter.governorToken = $governorToken; }
		COLON
	)?
	(	a = UPPERIDENTIFIER
		{	
			$formalParameter.identifier = new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a));
			$formalParameter.formalParameterToken = $a;
		}
	|	b = LOWERIDENTIFIER
		{	
			$formalParameter.identifier = new Identifier(Identifier_type.ID_ASN, $b.getText(), new Location(actualFile, $b, $b));
			$formalParameter.formalParameterToken = $b;
		}
	)
);

pr_special_NamedBitList returns [NamedValues namedValues]
@init { $namedValues = new NamedValues(); }:
(
	pr_NamedBitList [$namedValues]
	EOF
);

pr_special_AlternativeTypeLists returns [CTs_EE_CTs list]
@init { $list = null; }:
(
	a = pr_AlternativeTypeLists { $list = $a.list; }
	EOF
);

pr_AlternativeTypeLists returns [CTs_EE_CTs list]
locals [ComponentTypeList ctss1, ExtensionAndException ee, ExtensionAdditions extensionAdditions]
@init { $ctss1 = null; $ee = null; $extensionAdditions = null; $list = null; }:
(
	a = pr_AlternativeTypeList			{ $ctss1 = $a.componentTypeList; }
	(
		COMMA
		b = pr_ExtensionAndException	{ $ee = $b.extensionAndException; }
		(
			pr_OptionalExtensionMarker
		|	COMMA
			c = pr_ExtensionAdditionAlternativesList
			{
				$extensionAdditions = $c.extensionAdditions; $ee.setExtensionAdditions($extensionAdditions);
			}
			pr_OptionalExtensionMarker
		)
	)?
)
{
	$list = new CTs_EE_CTs($ctss1, $ee, null);
};

pr_AlternativeTypeList returns [ComponentTypeList componentTypeList]
locals [NamedType_Helper helper, CompField field]
@init	{ $helper = null; $field = null; $componentTypeList = new ComponentTypeList(); }:
(
	a = pr_NamedType 
	{
		$helper = $a.helper;
		$field = new CompField($helper.identifier, $helper.type, false, null);
		$componentTypeList.addComponentType(new RegularComponentType($field));
	}
	(
		COMMA
		b = pr_NamedType 
		{
			$helper = $b.helper;
			$field = new CompField($helper.identifier, $helper.type, false, null);
		 	$componentTypeList.addComponentType(new RegularComponentType($field));
		}
	)*
);

pr_ExtensionAndException returns [ExtensionAndException extensionAndException]
locals [ExceptionSpecification exeptionSpecification]
@init { $exeptionSpecification = null; $extensionAndException = null; }:
(	
	ELLIPSIS
	a = pr_ExceptionSpec { $exeptionSpecification = $a.exceptionSpecification; }
)
{
	$extensionAndException = new ExtensionAndException($exeptionSpecification, null);
};

pr_OptionalExtensionMarker:
(
	pr_ExtensionEndMarker
)?;

pr_ExtensionAdditionAlternativesList returns [ExtensionAdditions extensionAdditions]
locals [ExtensionAddition extensionAddition]
@init { $extensionAddition = null; $extensionAdditions = new ExtensionAdditions(); }:
(
	a = pr_ExtensionAdditionAlternative  
	{
		$extensionAddition = $a.extensionAddition;
		$extensionAdditions.addExtensionAddition($extensionAddition);
	}
	(
		COMMA
		b = pr_ExtensionAdditionAlternative  
		{
			$extensionAddition = $b.extensionAddition;
			$extensionAdditions.addExtensionAddition($extensionAddition);
		}
	)*
);

pr_ExtensionAdditionAlternative returns [ExtensionAddition extensionAddition]
locals [ComponentTypeList componentTypeList, NamedType_Helper helper]
@init { $componentTypeList = null; $helper = null; $extensionAddition = null; }:
(
	(
		LEFTVERSIONBRACKETS
		a = pr_AlternativeTypeList { $componentTypeList = $a.componentTypeList; }
		RIGHTVERSIONBRACKETS
	)
	{ $extensionAddition = new ExtensionAdditionGroup(null, $componentTypeList); }
|	b = pr_NamedType
	{
		$helper = $b.helper;
		CompField compField = new CompField($helper.identifier, $helper.type, false, null);
		$extensionAddition = new RegularComponentType(compField);
	}
);

pr_ExtensionEndMarker:
(	
	COMMA
	ELLIPSIS
);

pr_special_ComponentTypeLists returns [CTs_EE_CTs list]
@init { $list = null; }:
(
	a = pr_ComponentTypeLists { $list = $a.list; }
	EOF
);

pr_ComponentTypeLists returns [CTs_EE_CTs list]
locals [ComponentTypeList ctss1, ExtensionAndException ee, ExtensionAdditions extensionAdditions, ComponentTypeList ctss2]
@init {	$ctss1 = null; $ee = null; $extensionAdditions = null; $ctss2 = null; $list = null; }:
(
	(
		a = pr_ComponentTypeList	{ $ctss1 = $a.componentTypeList; }
		(	
			COMMA
			b = pr_ExtensionAndException { $ee = $b.extensionAndException; }
			(
				(
					COMMA
					c = pr_ExtensionAdditionList
					{
						$extensionAdditions = $c.extensionAdditions;	
						if($ee != null) { $ee.setExtensionAdditions($extensionAdditions); }
					}
					(	
						pr_ExtensionEndMarker
						(
							COMMA
							d = pr_ComponentTypeList { $ctss2 = $d.componentTypeList; }
						)?
					)?
				)
			|	(	
					pr_ExtensionEndMarker
					(
						COMMA
						e = pr_ComponentTypeList { $ctss2 = $e.componentTypeList; }
					)?
				)
			)?
		)?
	)
|	(	
		f = pr_ExtensionAndException	{ $ee = $f.extensionAndException; }
		(	
			COMMA
			(
				pr_ExtensionEndMarker
				COMMA
				g = pr_ComponentTypeList { $ctss2 = $g.componentTypeList; }
			|	h = pr_ExtensionAdditionList
				{
					$extensionAdditions = $h.extensionAdditions;
					if($ee != null) { $ee.setExtensionAdditions($extensionAdditions); }
				}
				(	
					pr_ExtensionEndMarker
					(
						COMMA
						i = pr_ComponentTypeList { $ctss2 = $i.componentTypeList; }
					)?
				)?
			)
		|	pr_OptionalExtensionMarker
		)
	)
)?
{
	$list = new CTs_EE_CTs($ctss1, $ee, $ctss2);
};

pr_ComponentTypeList returns [ComponentTypeList componentTypeList]
locals [ComponentType componentType]
@init { $componentType = null; $componentTypeList = new ComponentTypeList(); }:
(
	a = pr_ComponentType
	{
		$componentType = $a.componentType;
		$componentTypeList.addComponentType($componentType);
	}
	(	
		COMMA
		b = pr_ComponentType
		{
			$componentType = $b.componentType;
			$componentTypeList.addComponentType($componentType);
		}
	)*
);

pr_ExtensionAdditionList returns [ExtensionAdditions extensionAdditions]
locals [ExtensionAddition extensionAddition]
@init { $extensionAddition = null; $extensionAdditions = new ExtensionAdditions(); }:
(
	a = pr_ExtensionAddition
	{
		$extensionAddition = $a.extensionAddition;
		$extensionAdditions.addExtensionAddition($extensionAddition);
	}
	(
		COMMA
		b = pr_ExtensionAddition
		{
			$extensionAddition = $b.extensionAddition;
			$extensionAdditions.addExtensionAddition($extensionAddition);
		}
	)*
);

pr_ComponentType returns [ComponentType componentType]
locals [CompField compField, ASN1Type type]
@init { CompField compField = null; ASN1Type type = null; $componentType = null; }:
(
	a = pr_ComponentType_reg
	{
		$compField = $a.compField;
		if($compField != null){
			$componentType = new RegularComponentType($compField);
		}
	}
|	COMPONENTS
	OF
	b = pr_Type
	{
		$type = $b.type;
		if ($type != null) {
			$componentType = new ComponentsOfComponentType($type);
			$componentType.setLocation(new Location(actualFile, $b.start, $b.start));
		}
	}
);

pr_ExtensionAddition returns [ExtensionAddition extensionAddition]
@init { $extensionAddition = null; }:
(
	a = pr_ComponentType			{ $extensionAddition = $a.componentType; }
|	b = pr_ExtensionAdditionGroup	{ $extensionAddition = $b.extensionAddition; }
);

pr_ComponentType_reg returns [CompField compField]
locals [NamedType_Helper helper, boolean optional, Value default_value]
@init { $helper = null; $optional = false; $default_value = null; $compField = null; }:
(
	a = pr_NamedType { $helper = $a.helper; }
	(	
		OPTIONAL	{ $optional = true; }
	|	DEFAULT
		b = pr_Value { $default_value = $b.value; }
	)?
)
{
	$compField = new CompField($helper.identifier, $helper.type, $optional, $default_value);
};

pr_ExtensionAdditionGroup  returns [ExtensionAdditionGroup extensionAddition]
locals [ComponentTypeList componentTypeList]
@init { $componentTypeList = null; $extensionAddition = null; }:
(
	LEFTVERSIONBRACKETS
	a = pr_ComponentTypeList { $componentTypeList = $a.componentTypeList; }
	RIGHTVERSIONBRACKETS
)
{
	if ($componentTypeList == null) {
		$componentTypeList = new ComponentTypeList();
	}
	$extensionAddition = new ExtensionAdditionGroup(null, $componentTypeList);
};

pr_special_ObjectSetSpec returns [ObjectSet_definition definition]
@init { $definition = null; }:
(
	a = pr_ObjectSetSpec { $definition = $a.definition; }
	EOF
)
{
	if ($definition != null) { $definition.setLocation(new Location(actualFile, $a.start, $a.start)); }
};

pr_ObjectSetSpec returns [ObjectSet_definition definition]
locals [ObjectSet_definition temporalDefinition]
@init { $temporalDefinition = null; $definition = null; }:
(
	a = pr_ObjectSetSpec0 { $definition = $a.definition; }
	(
		COMMA
		ELLIPSIS
		(
			COMMA
			b = pr_ObjectSetSpec0 { $temporalDefinition = $b.definition; $definition.steelObjectSetElements($temporalDefinition); }
		)?
	)?
|	e = ELLIPSIS
	(	
		COMMA
		f = pr_ObjectSetSpec0 { $definition = $f.definition; }
	|	{ $definition = new ObjectSet_definition(); $definition.setLocation(new Location(actualFile, $e, $e));}
	)
);

pr_ObjectSetSpec0 returns [ObjectSet_definition definition]
locals [IObjectSet_Element element]
@init { $element = null; $definition = null; }:
(	
	 a = pr_ObjectSetElements
	{
		$element = $a.element;
		$definition = new ObjectSet_definition();
		$definition.addObjectSetElement($element);
	}
	(
		pr_UnionMark
		b = pr_ObjectSetElements
		{
			$element = $b.element;			
			if ($definition != null) {
				$definition.addObjectSetElement($element);
			}
		}
	)*
)
{
	if ($definition != null) {
		$definition.setLocation(new Location(actualFile, $a.start, $a.start == null ? $b.start : $a.start));
	}
};

pr_ObjectSetElements returns [IObjectSet_Element element]
locals [Reference reference]
@init { $reference = null; $element = null;}:
(
	a = pr_Object { $element = $a.object; }
|	b = pr_RefdUpper
	{
		$reference = $b.reference;
		Referenced_ObjectSet temp = new Referenced_ObjectSet($reference);
		temp.setLocation(new Location(actualFile, $b.start, $b.start));
		$element = temp;
	}
);

pr_RefdUpper returns [Reference reference]
@init { $reference = null; }:
(
	a = pr_FromObjs		{ $reference = $a.fromObj; }
|	b = pr_UpperFromObj	{ $reference = $b.fromObj; }
|	c = pr_DefdUpper	{ $reference = $c.reference; }
);

pr_DefinedObjectSetBlock returns [Referenced_ObjectSet objectSet]
locals [Reference reference]
@init { Reference reference = null; $objectSet = null; }:
(
	a = pr_RefdUpper { $reference = $a.reference; }
)
{
	$objectSet = new Referenced_ObjectSet($reference);
};

pr_AtNotationList returns[AtNotations notationList]
locals [AtNotation atNotation]
@init { $atNotation = null; $notationList = new AtNotations(); }:
(
	a = pr_AtNotation		{ $atNotation = $a.atNotation; $notationList.addAtNotation($atNotation); }
	(
		COMMA
		b = pr_AtNotation	{  $atNotation = $b.atNotation; $notationList.addAtNotation($atNotation); }
	)*
);

pr_AtNotation returns[AtNotation atNotation]
locals [int levels, FieldName fieldName]
@init { $levels = 0; $fieldName = null; $atNotation = null; }:
(
	ATSYMBOL
	a = pr_Levels			{ $levels = $a.level; }
	b = pr_ComponentIdList	{ $fieldName = $b.fieldName; }
)
{
	if ($fieldName != null) {
		$atNotation = new AtNotation($levels, $fieldName);
	}
};

pr_Levels returns[int level]
@init { $level = 0; }:
(
	(
		a = pr_Level { $level = $a.level; }
	)?
);

pr_ComponentIdList returns[FieldName fieldName]
@init { $fieldName = null; }:
(	
	a = LOWERIDENTIFIER
	{
		$fieldName = new FieldName(); 
		$fieldName.addField(new Identifier(Identifier_type.ID_ASN, $a.getText(), new Location(actualFile, $a, $a)));
	}
	(
		DOT 
		b = LOWERIDENTIFIER
		{
			if ($fieldName != null) {
				$fieldName.addField(new Identifier(Identifier_type.ID_ASN, $b.getText(), new Location(actualFile, $b, $b)));
			}
		}
	)*
);

pr_Level returns[int level]
@init { $level = 0; }:
(	
	(
		DOT					{ $level += 1; }
	|	RANGESEPARATOR		{ $level += 2; }
	|	ELLIPSIS			{ $level += 3; }
	)+
);

pr_special_Assignment returns [ASN1Assignment assignment]
@init { $assignment = null; }:
(	
	a = pr_Assignment { $assignment = $a.assignment; }
	EOF
);

