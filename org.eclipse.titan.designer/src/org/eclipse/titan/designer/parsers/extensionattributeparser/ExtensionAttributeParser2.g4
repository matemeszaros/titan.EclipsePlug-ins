parser grammar ExtensionAttributeParser2;

@header {
import java.util.ArrayList;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.AST.*;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.IType.Encoding_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.*;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute.ExtensionAttribute_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function.EncodingPrototype_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.PrintingType.PrintingTypeEnum;
import org.eclipse.titan.designer.AST.TTCN3.types.*;
}

/*
******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
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
}

/*
 * @author Kristof Szabados
 * 
 * FIXME location information is incorrect yet
 * */
options {
tokenVocab=ExtensionAttributeLexer2;
}
 
pr_ExtensionAttributeRoot returns[ArrayList<ExtensionAttribute> list]
@init { $list = new ArrayList<ExtensionAttribute>(); }:
(	
	a = pr_ExtensionAttribute	{ if($a.attribute != null) {$list.add($a.attribute); } }
)*
EOF
;

pr_ExtensionAttribute returns[ExtensionAttribute attribute]:
(	pr_PrototypeAttribute		{$attribute = $pr_PrototypeAttribute.attribute;}
|	pr_TransparentAttribute		{$attribute = $pr_TransparentAttribute.attribute;}
|	pr_EncodeAttribute			{$attribute = $pr_EncodeAttribute.attribute;}
|	pr_DecodeAttribute			{$attribute = $pr_DecodeAttribute.attribute;}
|	pr_ErrorBehaviorAttribute	{$attribute = $pr_ErrorBehaviorAttribute.attribute;}
|	pr_PrintingAttribute		{$attribute = $pr_PrintingAttribute.attribute;}
|	pr_PortTypeAttribute		{$attribute = $pr_PortTypeAttribute.attribute;}
|	pr_ExtendsAttribute			{$attribute = $pr_ExtendsAttribute.attribute;}
|	pr_AnyTypeAttribute			{$attribute = $pr_AnyTypeAttribute.attribute;}
|	pr_DoneAttribute			{$attribute = $pr_DoneAttribute.attribute;}
|	pr_VersionAttribute			{$attribute = $pr_VersionAttribute.attribute;}
|	pr_RequiresAttribute		{$attribute = $pr_RequiresAttribute.attribute;}
|	pr_TitanVersionAttribute	{$attribute = $pr_TitanVersionAttribute.attribute;}
);

// Module's own version
pr_VersionAttribute returns[ModuleVersionAttribute attribute]
	locals[Token endCol]
	:
(	
	VERSION
	(
		LANGLE
		pr_Identifier	{$attribute  = new ModuleVersionAttribute($pr_Identifier.identifier, true);}
		RANGLE  		{$endCol = $RANGLE;}
	|	pr_Version		{$attribute  = new ModuleVersionAttribute($pr_Version.identifier, false); $endCol =  $pr_Version.stop;}
	)
)
{
	if ($attribute != null) {
		$attribute.setLocation(getLocation($start, $endCol));
	}
};

// Required version for an imported module
pr_RequiresAttribute returns [VersionRequirementAttribute attribute]
	:
(	
	REQUIRES
	pr_Identifier
	pr_Version
)
{
	if ($pr_Identifier.identifier != null && $pr_Version.identifier != null) {
		$attribute = new VersionRequirementAttribute($pr_Identifier.identifier, $pr_Version.identifier);
		$attribute.setLocation(getLocation($start, $pr_Version.stop));
	}
};

// Required Titan version
pr_TitanVersionAttribute returns[TitanVersionAttribute attribute]
	:
(	
	REQ_TITAN
	pr_Version
)
{
	if ($pr_Version.identifier != null) {
		$attribute = new TitanVersionAttribute($pr_Version.identifier);
		$attribute.setLocation(getLocation($start, $pr_Version.stop));
	}
};

pr_Version returns [Identifier identifier]
	locals[String temp, Token endCol]
	:
(  
	a = IDENTIFIER	{$temp = $a.getText(); $endCol = $a;}
	(  
   		b = NUMBER	{$temp += " " + $b.getText();}
      	c = NUMBER	{$temp += " " + $c.getText();}
    	( 
      		SLASH
        	d = NUMBER	{$temp += "/" + $d.getText();}
      	)?
    	e = IDENTIFIER	{$temp += " " + $e.getText(); $endCol = $e;} // CNL 113 200 R9A
   	)?
)
{
	$identifier = new Identifier(Identifier_type.ID_TTCN, $temp, getLocation($start, $endCol));
};

pr_PrototypeAttribute returns[PrototypeAttribute attribute]
@init { $attribute = null;}:
(	
	PROTOTYPE
	LPAREN 
	type = pr_PrototypeSetting
	RPAREN
)
{
	if($pr_PrototypeSetting.type != null) {
		$attribute = new PrototypeAttribute($pr_PrototypeSetting.type);
		$attribute.setLocation(getLocation($start, $RPAREN));
	}
};

pr_PrototypeSetting returns[EncodingPrototype_type type]
	:
(	
	BACKTRACK	{ $type = EncodingPrototype_type.BACKTRACK; }
|	CONVERT		{ $type = EncodingPrototype_type.CONVERT; }
|	FAST		{ $type = EncodingPrototype_type.FAST; }
|	SLIDING		{ $type = EncodingPrototype_type.SLIDING; }
);

pr_EncodeAttribute returns[EncodeAttribute attribute]
	locals[boolean hasOption]:
(	
	ENCODE
	LPAREN
	pr_EncodingType {$hasOption = false;}
	(
		COLON
		pr_EncodingOptions {$hasOption = true;}
	)?
	RPAREN
)
{
	if($pr_EncodingType.type != null) {
		if ($hasOption) {
			$attribute = new EncodeAttribute($pr_EncodingType.type, $pr_EncodingOptions.text);
		}
		else {
			$attribute = new EncodeAttribute($pr_EncodingType.type, null);
		}
		$attribute.setLocation(getLocation($start, $RPAREN));
	}
};

pr_DecodeAttribute returns[DecodeAttribute attribute]
	locals[boolean hasOption]:
(	
	DECODE
	LPAREN
	pr_EncodingType {$hasOption = false;}
	(
		COLON
		pr_EncodingOptions {$hasOption = true;}
	)?
	RPAREN
)
{
	if($pr_EncodingType.type != null) {
		if ($hasOption) {
			$attribute = new DecodeAttribute($pr_EncodingType.type, $pr_EncodingOptions.text);
		}
		else {
			$attribute = new DecodeAttribute($pr_EncodingType.type, null);
		}
		$attribute.setLocation(getLocation($start, $RPAREN));
	}
};

pr_EncodingType returns[Encoding_type type]
	:
(	
	BER		{$type = Encoding_type.BER;}
|	PER		{$type = Encoding_type.PER;}
|	XER		{$type = Encoding_type.XER;}
|	RAW		{$type = Encoding_type.RAW;}
|	TEXT	{$type = Encoding_type.TEXT;}
|	JSON	{$type = Encoding_type.JSON;}
);

pr_EncodingOptions returns[String text]
	locals[StringBuilder builder, String option]
	:
	{$builder = new StringBuilder();}
(	
	pr_EncodingOption	{if($pr_EncodingOption.text != null) { $builder.append($pr_EncodingOption.text); } }
	(	
		ANDOP		pr_EncodingOption	{if($pr_EncodingOption.text != null) { $builder.append(" & ").append($pr_EncodingOption.text); } }
	|	OROP		pr_EncodingOption	{if($pr_EncodingOption.text != null) { $builder.append(" | ").append($pr_EncodingOption.text); } }
	)*
)
{
	$text = $builder.toString();
};

pr_EncodingOption returns[String text]
	:
	IDENTIFIER
{
	$text = $IDENTIFIER.getText();
};

pr_ErrorBehaviorAttribute returns[ErrorBehaviorAttribute attribute]
	:
(	
	ERRORBEHAVIOR
	LPAREN
	pr_ErrorBehaviorSettingList
	RPAREN
)
{
	if($pr_ErrorBehaviorSettingList.list != null) {
		$attribute = new ErrorBehaviorAttribute($pr_ErrorBehaviorSettingList.list);
		$attribute.setLocation(getLocation($start, $RPAREN));
	}
};

pr_TransparentAttribute returns[TransparentAttribute attribute]
	:
(	
	TRANSPARENT
)
{
		$attribute = new TransparentAttribute();
		$attribute.setLocation(getLocation($start, $TRANSPARENT));
};

pr_ErrorBehaviorSettingList returns[ErrorBehaviorList list]
	locals[Token endCol]
	:
	{$list = new ErrorBehaviorList();}
(	
	pr_ErrorBehaviorSetting	{$list.addSetting($pr_ErrorBehaviorSetting.behaviorSetting); $endCol = $pr_ErrorBehaviorSetting.stop;}
	(
		COMMA
		pr_ErrorBehaviorSetting		{$list.addSetting($pr_ErrorBehaviorSetting.behaviorSetting); $endCol = $pr_ErrorBehaviorSetting.stop;}
	)*
)
{
	$list.setLocation(getLocation($start, $endCol));
};

pr_ErrorBehaviorSetting returns[ErrorBehaviorSetting behaviorSetting]
	:
(	
	errorType = pr_ErrorBehaviorString
	COLON
	errorHandling = pr_ErrorBehaviorString
)
{
	$behaviorSetting = new ErrorBehaviorSetting($errorType.string, $errorHandling.string);
	$behaviorSetting.setLocation(getLocation($start, $pr_ErrorBehaviorString.stop));
};

pr_ErrorBehaviorString returns[String string]
	:
(	
	IDENTIFIER
)
{
	$string = $IDENTIFIER.getText();
};

pr_PrintingAttribute returns[PrintingAttribute attribute]
	:
(	
	PRINTING
	LPAREN
	pr_PrintingType
	RPAREN
)
{
	if($pr_PrintingType.printingType != null) {
		$attribute = new PrintingAttribute($pr_PrintingType.printingType);
		$attribute.setLocation(getLocation($start, $RPAREN));
	}
};

pr_PrintingType returns[PrintingType printingType]
	locals[PrintingTypeEnum pte, Token endCol]
	:
	{$pte = PrintingTypeEnum.NONE;}
(	
	COMPACT	{$pte = PrintingTypeEnum.COMPACT; $endCol = $COMPACT;}
|	PRETTY	{$pte = PrintingTypeEnum.PRETTY; $endCol = $PRETTY;}
)
{
	if($pte != PrintingTypeEnum.NONE) {
		$printingType = new PrintingType($pte);
		$printingType.setLocation(getLocation($start, $endCol));
	}
};

pr_PortTypeAttribute returns[PortTypeAttribute attribute]
	locals[Token endCol]
	:
(	
	INTERNAL	{$attribute = new InternalPortTypeAttribute(); $endCol = $INTERNAL;}
|	ADDRESS		{$attribute = new AddressPortTypeAttribute(); $endCol = $ADDRESS;}
|	PROVIDER	{$attribute = new ProviderPortTypeAttribute(); $endCol = $PROVIDER;}
|	pr_UserAttribute	{$attribute = $pr_UserAttribute.attribute; $endCol = $pr_UserAttribute.stop;}
)
{
	$attribute.setLocation(getLocation($start, $endCol));
};

pr_UserAttribute returns[UserPortTypeAttribute attribute]
	:
(	
	USER
	pr_PortTypeReference	{$attribute = new UserPortTypeAttribute($pr_PortTypeReference.reference);}
	pr_InOutTypeMappingList[$attribute]
);

pr_PortTypeReference returns[Reference reference]
	:
	pr_Reference	{$reference = $pr_Reference.reference;}
;

pr_InOutTypeMappingList[UserPortTypeAttribute attribute]
	:
(	
	pr_InOutTypeMapping[$attribute]
)+
;

pr_InOutTypeMapping[UserPortTypeAttribute attribute]
	locals[TypeMappings typeMappings]
	:
(	
	IN
	LPAREN
	pr_TypeMappingList	{$attribute.setInMappings($pr_TypeMappingList.mappings);}
	RPAREN
|	OUT
	LPAREN
	pr_TypeMappingList	{$attribute.setOutMappings($pr_TypeMappingList.mappings);}
	RPAREN
)
{
	if($attribute != null) {
		$attribute.setLocation(getLocation($start, $RPAREN));
	}
};

pr_TypeMappingList returns [TypeMappings mappings]
	:
	{$mappings = new TypeMappings();}
(	
	pr_TypeMapping	{ if($pr_TypeMapping.typeMapping != null) {$mappings.addMapping($pr_TypeMapping.typeMapping); } }
	(
		SEMICOLON
		pr_TypeMapping	{ if($pr_TypeMapping.typeMapping != null) {$mappings.addMapping($pr_TypeMapping.typeMapping); } }
	)*
)
{
	$mappings.setLocation(getLocation($start, $pr_TypeMapping.stop));
};

pr_TypeMapping returns[TypeMapping typeMapping]
	:
(	
	pr_Type
	REDIRECTSYMBOL
	pr_TypeMappingTargetList
)
{
	$typeMapping = new TypeMapping($pr_Type.type, $pr_TypeMappingTargetList.targets);
	$typeMapping.setLocation(getLocation($start, $pr_TypeMappingTargetList.stop));
};

pr_TypeMappingTargetList returns[TypeMappingTargets targets]
	:
	{$targets = new TypeMappingTargets();}
(	
	pr_TypeMappingTarget	{if($pr_TypeMappingTarget.mappingTarget != null) { $targets.addMappingTarget($pr_TypeMappingTarget.mappingTarget); } }
	(	
		COMMA
		pr_TypeMappingTarget	{if($pr_TypeMappingTarget.mappingTarget != null) { $targets.addMappingTarget($pr_TypeMappingTarget.mappingTarget); } }
	)*
);

pr_TypeMappingTarget returns[TypeMappingTarget mappingTarget]
	locals[EncodeMappingHelper helper, Token endCol]
	:
(	
	DASH
	COLON
	DISCARD		{$mappingTarget = new DiscardTypeMappingTarget(); $endCol = $DISCARD;} 
|	pr_Type
	COLON
	(	
		SIMPLE				{$mappingTarget = new SimpleTypeMappingTarget($pr_Type.type);}
	|	pr_FunctionMapping	{$mappingTarget = new FunctionTypeMappingTarget($pr_Type.type, $pr_FunctionMapping.reference);}
	|	pr_EncodeMapping	{if($pr_EncodeMapping.helper != null)
							{$mappingTarget = new EncodeTypeMappingTarget($pr_Type.type, $pr_EncodeMapping.helper.encodeAttribute, $pr_EncodeMapping.helper.errorBehaviorAttribute);}
							}
	|	pr_DecodeMapping	{if($pr_DecodeMapping.helper != null)
							{$mappingTarget = new DecodeTypeMappingTarget($pr_Type.type, $pr_DecodeMapping.helper.encodeAttribute, $pr_DecodeMapping.helper.errorBehaviorAttribute);}
							}
	)
	{$endCol = $pr_Type.stop;}
)
{
	if ($mappingTarget != null) {
		$mappingTarget.setLocation(getLocation($start, $endCol));	
	}
};

pr_FunctionMapping returns[Reference reference]
	:
(	
	FUNCTION
	LPAREN
	pr_FunctionReference	{$reference = $pr_FunctionReference.reference;}
	RPAREN
);

pr_FunctionReference returns[Reference reference]:
	pr_Reference	{$reference = $pr_Reference.reference;}
;

pr_EncodeMapping returns[EncodeMappingHelper helper]
	:
(	
	encodeAttribute = pr_EncodeAttribute
	(	
		errorBehaviorAttribute = pr_ErrorBehaviorAttribute
	)?
	(	
		printingAttribute = pr_PrintingAttribute
	)?
);

pr_DecodeMapping returns[EncodeMappingHelper helper]
	locals[boolean hasErrorBehaviorAttribute]:
(	
	decodeAttribute = pr_DecodeAttribute {$hasErrorBehaviorAttribute = false;}
	(	
		errorBehaviorAttribute = pr_ErrorBehaviorAttribute {$hasErrorBehaviorAttribute = true;}
	)?
)
{
	if ($hasErrorBehaviorAttribute) {
		$helper = new EncodeMappingHelper($decodeAttribute.attribute, $errorBehaviorAttribute.attribute);
	}
	else {
		$helper = new EncodeMappingHelper($decodeAttribute.attribute, null);
	}
};

pr_ExtendsAttribute returns[ExtensionsAttribute attribute]
	:
(	
	EXTENDS
	types = pr_ExtensionList
)
{
	if($types.types != null) {
		$attribute = new ExtensionsAttribute($types.types);
		$attribute.setLocation(getLocation($start, $types.stop));
	}
};

pr_ExtensionList returns[Types types]
	: 
	{$types = new Types();}
(	
	pr_ReferencedType	{ if ($pr_ReferencedType.type != null) { $types.addType($pr_ReferencedType.type); } }
	(
		COMMA
		pr_ReferencedType	{ if ($pr_ReferencedType.type != null) { $types.addType($pr_ReferencedType.type); } }
	)*
);

pr_AnyTypeAttribute returns[AnytypeAttribute attribute]
	:
(	
	ANYTYPE
	pr_TypeList
)
{
	if($pr_TypeList.types != null) {
		$attribute = new AnytypeAttribute($pr_TypeList.types);
		$attribute.setLocation(getLocation($start, $pr_TypeList.stop));
	}
};

pr_TypeList returns[Types types]
	: 
	{$types = new Types();}
(	
	pr_Type	{ if ($pr_Type.type != null) { $types.addType($pr_Type.type); } }
	(	
		COMMA
		pr_Type	{ if ($pr_Type.type != null) { $types.addType($pr_Type.type); } }
	)*
);

pr_Type returns[Type type]:
(	
	pr_PredefinedType	{$type = $pr_PredefinedType.type;}
|	pr_ReferencedType	{$type = $pr_ReferencedType.type;}
);

pr_PredefinedType returns[Type type]
	locals[Token endCol]
	:
(	
	BITSTRING	{ $type = new BitString_Type(); $endCol = $BITSTRING; }
|	BOOLEAN		{ $type = new Boolean_Type(); $endCol = $BOOLEAN; }
|	CHARSTRING	{ $type = new CharString_Type(); $endCol = $CHARSTRING; }
|	UNIVERSAL	{ }
	CHARSTRING	{ $type = new UniversalCharstring_Type(); $endCol = $CHARSTRING; }
|	INTEGER		{ $type = new Integer_Type(); $endCol = $INTEGER; }
|	OCTETSTRING	{ $type = new OctetString_Type(); $endCol = $OCTETSTRING; }
|	HEXSTRING	{ $type = new HexString_Type(); $endCol = $HEXSTRING; }
|	VERDICTTYPE	{ $type = new Verdict_Type(); $endCol = $VERDICTTYPE; }
|	FLOAT		{ $type = new Float_Type(); $endCol = $FLOAT; }
|	ADDRESS		{ $type = new Address_Type(); $endCol = $ADDRESS; }
|	DEFAULT		{ $type = new Default_Type(); $endCol = $DEFAULT; }
|	ANYTYPE		{ $type = new Boolean_Type(); $type.setIsErroneous(true); $endCol = $ANYTYPE; }
|	OBJECTIDENTIFIER	{ $type = new ObjectID_Type(); $endCol = $OBJECTIDENTIFIER; }
)
{
	$type.setLocation(getLocation($start, $endCol));
};

pr_ReferencedType returns [Type type]
	:
(	
	reference = pr_Reference
)
{
	if ($pr_Reference.reference != null) {
		$type = new Referenced_Type($pr_Reference.reference);
		$type.setLocation(getLocation($start, $reference.stop));
	}
};

pr_Reference returns[Reference reference]
	locals[Identifier identifier1, Identifier identifier2]
	:
(	
	pr_Identifier	{$identifier1 = $pr_Identifier.identifier;}
	(
		DOT
		pr_Identifier {$identifier2 = $pr_Identifier.identifier;}
	)?
)
{
	if ($identifier1 != null) {
		if ($identifier2 == null) {
			$reference = new Reference(null);
			FieldSubReference subReference = new FieldSubReference($identifier1);
			subReference.setLocation(getLocation($start, $pr_Identifier.stop));
			$reference.addSubReference(subReference);
		} else {
			$reference = new Reference($identifier1);
			FieldSubReference subReference = new FieldSubReference($identifier2);
			subReference.setLocation(getLocation($start, $pr_Identifier.stop));
			$reference.addSubReference(subReference);
		}
	}
};

pr_Identifier returns [Identifier identifier]
	:
(	
	IDENTIFIER
)
{
	if($IDENTIFIER.getText() != null) {
		$identifier = new Identifier(Identifier_type.ID_TTCN, $IDENTIFIER.getText(), getLocation($IDENTIFIER));
	}
};

pr_DoneAttribute returns[DoneAttribute attribute]
	:
(	
	DONE
)
{
	$attribute=new DoneAttribute();
	$attribute.setLocation(getLocation($start, $DONE));
};
