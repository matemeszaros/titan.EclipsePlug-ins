parser grammar PreprocessorDirectiveParser;

@header {
import java.util.ArrayList;
import java.util.Map;
import java.text.MessageFormat;
import org.eclipse.titan.designer.parsers.preprocess.PreprocessorDirective;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.core.resources.IFile;
}

/*
 * @author Laszlo Baji
 * @author Arpad Lovassy
 */

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
	protected boolean isActiveCode;
	protected Map<String,String> macros;
	protected int line;
	private IFile actualFile = null;
	private int offset = 0;
	
	public void setIsActiveCode(boolean isActiveCode) {
		this.isActiveCode = isActiveCode;
	}
	
	public void setMacros(Map<String,String> macros) {
		this.macros = macros;
	}
	
	public void setLine(int line) {
		this.line = line;
	}

	private ArrayList<SyntacticErrorStorage> errorsStored = new ArrayList<SyntacticErrorStorage>();
	
	public ArrayList<SyntacticErrorStorage> getErrorStorage() {
		return errorsStored;
	}
	
  	private ArrayList<TITANMarker> warnings = new ArrayList<TITANMarker>();

  	public void reportWarning(TITANMarker marker) {
    	warnings.add(marker);
  	}
  	public ArrayList<TITANMarker> getWarnings() {
    	return warnings;
  	}

  	private ArrayList<TITANMarker> unsupportedConstructs = new ArrayList<TITANMarker>();
  	public void reportUnsupportedConstruct(TITANMarker marker) {
    	unsupportedConstructs.add(marker);
  	}
  	public ArrayList<TITANMarker> getUnsupportedConstructs() {
    	return unsupportedConstructs;
  	}
}

options {
tokenVocab=PreprocessorDirectiveLexer;
}

pr_Directive returns [PreprocessorDirective ppDirective]
@init { $ppDirective = null; }:
(
	a = pr_Ifdef		{ $ppDirective = $a.ppDirective; }
|	b = pr_Ifndef		{ $ppDirective = $b.ppDirective; }
|	c = pr_If			{ $ppDirective = $c.ppDirective; }
|	d = pr_Else			{ $ppDirective = $d.ppDirective; }
|	e = pr_Elif			{ $ppDirective = $e.ppDirective; }
|	f = pr_Endif		{ $ppDirective = $f.ppDirective; }
|	g = pr_Define		{ $ppDirective = $g.ppDirective; }
|	h = pr_Undef		{ $ppDirective = $h.ppDirective; }
|	i = pr_Include		{ $ppDirective = $i.ppDirective; }
|	j = pr_Error		{ $ppDirective = $j.ppDirective; }
|	k = pr_Warning		{ $ppDirective = $k.ppDirective; }
|	l = pr_LineMarker	{ $ppDirective = $l.ppDirective; }
|	m = pr_LineControl	{ $ppDirective = $m.ppDirective; }
|	n = pr_Pragma		{ $ppDirective = $n.ppDirective; }
|	{ 
		$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.NULL);
	}
)
EOF
;

pr_Ifdef returns [PreprocessorDirective ppDirective]
@init { $ppDirective = null; }:
(
	DIRECTIVE_IFDEF id = IDENTIFIER
) 
{
	boolean condition = macros != null && macros.containsKey($id.getText());
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.IFDEF, condition);
}
;

pr_Ifndef returns [PreprocessorDirective ppDirective]
@init { $ppDirective = null; }:
(
	DIRECTIVE_IFNDEF id = IDENTIFIER
)
{
	boolean condition = macros!=null && macros.containsKey($id.getText());
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.IFNDEF, condition);
}
;

pr_Else returns [PreprocessorDirective ppDirective]
@init { $ppDirective = null; }:
(
	DIRECTIVE_ELSE
)
{
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.ELSE);
}
;

pr_Endif returns [PreprocessorDirective ppDirective]
@init { $ppDirective = null; }:
(
	DIRECTIVE_ENDIF
)
{
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.ENDIF);
}
;

pr_Integer returns [long value]
@init { $value = 0;}:
(
	h = HEXINT {
		try {
			$value = Long.parseLong($h.getText().substring(2), 16); // omit leading 0x
		} catch (NumberFormatException e) {
			TITANMarker marker = new TITANMarker(MessageFormat.format("Invalid hexadecimal integer value: {0}", e.getMessage()), 
					line, -1, -1, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL);
			reportUnsupportedConstruct(marker);
		} 
	}		

|	o = OCTINT {
		try {
			$value = Long.parseLong($o.getText(), 8);
		} catch (NumberFormatException e) {
			TITANMarker marker = new TITANMarker(MessageFormat.format("Invalid octal integer value: {0}", e.getMessage()), 
					line, -1, -1, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL);
			reportUnsupportedConstruct(marker);
		}
	}
|	d = DECINT {
		try {
			$value = Long.parseLong($d.getText(), 10);
		} catch (NumberFormatException e) {
			TITANMarker marker = new TITANMarker(MessageFormat.format("Invalid integer value: {0}", e.getMessage()), 
					line, -1, -1, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL);
			reportUnsupportedConstruct(marker);
		}
	}
)
;

pr_PrimaryExpression returns [long value]
@init { $value = 0; }:
(
	v1 = pr_Integer { $value = $v1.value; }
|	id = IDENTIFIER { 
		if (macros != null && macros.containsKey($id.getText())) {
			try{
				$value = Long.parseLong(macros.get($id.getText()));
			}
			catch (NumberFormatException e) {
				TITANMarker marker = new TITANMarker(MessageFormat.format(
					"Macro {0} has value `{1}'' which is not valid in preprocessor conditional expressions", $id.getText(), macros.get($id.getText())), 
					line, -1, -1, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL);
				reportUnsupportedConstruct(marker);
			} 
		} 
	}
|	(
		LPAREN 
		v2 = pr_Expression { $value = $v2.value; } 
		RPAREN
	)
)
;

pr_UnaryExpression returns [long value]
@init { $value = 0; }:
(
	v1 = pr_PrimaryExpression { $value = $v1.value; }
|	(
		(	OP_NOT
		|	NOT
			{	TITANMarker marker = new TITANMarker("Some compiler versions cannot accept keyword `not', use `!' instead", 
					line, -1, -1, IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL);
				reportUnsupportedConstruct(marker);
			}
		)
		v2 = pr_UnaryExpression
	)
	{
		$value = $v2.value; 
		if ($value == 0) { $value = 1; } else { $value = 0; }
	}
|	(
		OP_MINUS v3 = pr_UnaryExpression
	)
	{
		$value = $v3.value;
		$value = -$value;
	}
|	(
		OP_PLUS v4 = pr_UnaryExpression { $value = $v4.value; }
	)
|   (
		OP_DEFINED id1 = IDENTIFIER
	) {
		if (macros != null && macros.containsKey($id1.getText())) {
			$value = 1;
		} else {
			$value = 0;
		}
	}
|	(
		OP_DEFINED LPAREN id2 = IDENTIFIER RPAREN
	)
	{
		if (macros != null && macros.containsKey($id2.getText())) {
			$value = 1;
		} else {
			$value = 0;
		}
	}
)
;

pr_MultiplicativeExpression returns [long value]
locals [long value2]
@init { $value = 0; }:
(
	v1 = pr_UnaryExpression { $value = $v1.value; }
	(
		OP_MUL v2 = pr_UnaryExpression { $value2 = $v2.value; $value *= $value2; }
	|	OP_DIV v3 = pr_UnaryExpression {
			$value2 = $v3.value;
			if ($value2 == 0) {
				TITANMarker marker = new TITANMarker("Division by zero", line, -1, -1, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL);
				reportUnsupportedConstruct(marker);
			} else { $value /= $value2; } 
		}
	|	OP_MOD v4 = pr_UnaryExpression {
			$value2 = $v4.value;
			if ($value2 == 0) {
				TITANMarker marker = new TITANMarker("Modulo by zero", line, -1, -1, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL);
				reportUnsupportedConstruct(marker);
			} else {
				$value %= $value2;
			} 
		}
	)*
)
;

pr_AdditiveExpression returns [long value]
locals [long value2]
@init { $value = 0; }:
(
	v1 = pr_MultiplicativeExpression { $value = $v1.value; }
	(
		OP_PLUS  v2 = pr_MultiplicativeExpression { $value2 = $v2.value; $value += $value2; }
	|	OP_MINUS v3 = pr_MultiplicativeExpression { $value2 = $v3.value; $value -= $value2; }
	)*
)
;

pr_ShiftExpression returns [long value]
locals [long value2]
@init { $value = 0; }:
(
	v1 = pr_AdditiveExpression  { $value = $v1.value; }
	(
		OP_LSHIFT v2 = pr_AdditiveExpression { $value2 = $v2.value; $value <<= $value2; }
	|	OP_RSHIFT v3 = pr_AdditiveExpression { $value2 = $v3.value; $value >>= $value2; }
	)*
)
;

pr_RelationalExpression returns [long value]
locals [long value2]
@init { $value = 0; }:
(
	v1 = pr_ShiftExpression { $value = $v1.value; }
	(
		OP_GT v2 = pr_ShiftExpression { $value2 = $v2.value; if ($value > $value2)  { $value = 1; } else { $value = 0; }}
	|	OP_LT v3 = pr_ShiftExpression { $value2 = $v3.value; if ($value < $value2)  { $value = 1; } else { $value = 0; }}
	|	OP_GE v4 = pr_ShiftExpression { $value2 = $v4.value; if ($value >= $value2) { $value = 1; } else { $value = 0; }}
	|	OP_LE v5 = pr_ShiftExpression { $value2 = $v5.value; if ($value <= $value2) { $value = 1; } else { $value = 0; }}
	)*
)
;

pr_EqualityExpression returns [long value]
locals [long value2]
@init { $value = 0; }:
(
	v1 = pr_RelationalExpression { $value = $v1.value; }
	(
		OP_EQ v2 = pr_RelationalExpression { $value2 = $v2.value; if ($value == $value2) { $value = 1; } else { $value = 0; }}
	|	OP_NE v3 = pr_RelationalExpression { $value2 = $v3.value; if ($value != $value2) { $value = 1; } else { $value = 0; }}
	)*
)
;

pr_BitAndExpression returns [long value]
locals [long value2]
@init { $value = 0; }:
(
	v1 = pr_EqualityExpression { $value = $v1.value; }
	(
		OP_BITAND v2 = pr_EqualityExpression { $value2 = $v2.value; $value &= $value2; }
	)*
)
;

pr_XorExpression returns [long value]
locals [long value2]
@init { $value = 0; }:
(
	v1 = pr_BitAndExpression { $value = $v1.value; }
	(
		OP_XOR v2 = pr_BitAndExpression { $value2 = $v2.value; $value ^= $value2; }
	)*
)
;

pr_BitOrExpression returns [long value]
locals [long value2]
@init { $value = 0; }:
(	v1 = pr_XorExpression { $value = $v1.value; }
	(
		OP_BITOR v2 = pr_XorExpression { $value2 = $v2.value; $value |= $value2; }
	)*
)
;

pr_AndExpression returns [long value]
locals [long value2]
@init { $value = 0; }:
(
	v1 = pr_BitOrExpression { $value = $v1.value; }
	(
		OP_AND v2 = pr_BitOrExpression { $value2 = $v2.value; if (($value != 0) && ($value2 != 0)) { $value = 1; } else { $value = 0; }}
	)*
)
;

pr_OrExpression returns [long value]
locals [long value2]
@init { $value = 0; }:
(
	v1 = pr_AndExpression { $value = $v1.value; }
	(
		OP_OR v2 = pr_AndExpression { $value2 = $v2.value; if (($value!= 0) || ($value2 != 0)) { $value = 1; } else { $value = 0; }}
	)*
)
;

pr_TernaryConditionalExpression returns [long value]
locals [long value2, long value3]
@init { $value = 0; }:
(
	v1 = pr_OrExpression { $value = $v1.value; }
	(
		QUESTIONMARK 
		v2 = pr_Expression COLON 
		v3 = pr_TernaryConditionalExpression
		{
			$value2 = $v2.value;
			$value3 = $v3.value;
			if ( $value != 0) { $value = $value2; } else { $value = $value3; }
		}
	)?
)
;

pr_Expression returns [long value]
@init { $value = 0; }:
(
	v = pr_TernaryConditionalExpression { $value = $v.value; }
)
;

pr_If returns [PreprocessorDirective ppDirective]
locals [long condition]
@init { $ppDirective = null; $condition = 0; }:
(
	DIRECTIVE_IF c = pr_Expression { $condition = $c.value; }
)
{
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.IF, $condition != 0);
}
;

pr_Elif returns [PreprocessorDirective ppDirective]
locals [long condition]
@init { $ppDirective = null; $condition = 0; }:
(
	DIRECTIVE_ELIF c = pr_Expression { $condition = $c.value; }
)
{
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.ELIF, $condition != 0);
}
;

pr_Define returns [PreprocessorDirective ppDirective]
locals [long value, String macro_value]
@init { $ppDirective = null; $value = 0; $macro_value = ""; }:
(
	DIRECTIVE_DEFINE id = IDENTIFIER 
	(
		v = pr_Expression
		{
			$value = $v.value;
			$macro_value = String.valueOf($value);
		} 
	)?
)
{
	if (isActiveCode && macros != null) {
		macros.put($id.getText(), $macro_value);
	}
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.DEFINE);
}
;

pr_Undef returns [PreprocessorDirective ppDirective]
@init { $ppDirective = null; }:
(
	DIRECTIVE_UNDEF id = IDENTIFIER
)
{
	if (isActiveCode && macros != null) {
		macros.remove($id.getText());
	}
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.UNDEF);
}
;

pr_Include returns [PreprocessorDirective ppDirective]
@init { $ppDirective = null; }:
(
	DIRECTIVE_INCLUDE 
	str = CSTRING
)
{
	int end = $str.getText().length() - 1;
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.INCLUDE, $str.getText().substring(1, end));
}
;

pr_Error returns [PreprocessorDirective ppDirective]
@init { $ppDirective = null; }:
(
	str = DIRECTIVE_ERROR
)
{
	String err = "error";
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.ERROR, $str.getText().substring(err.length()));
}
;

pr_Warning returns [PreprocessorDirective ppDirective]
@init { $ppDirective = null; }:
(
	str = DIRECTIVE_WARNING
)
{
	String warn = "warning";
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.WARNING, $str.getText().substring(warn.length()));
}
;

pr_LineMarker returns [PreprocessorDirective ppDirective]
@init { $ppDirective = null; }:
(
	pr_Integer CSTRING ( pr_Integer )*
)
{
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.LINEMARKER);
}
;

pr_LineControl returns [PreprocessorDirective ppDirective]
@init { $ppDirective = null; }:
(
	DIRECTIVE_LINECONTROL
	DECINT
	(CSTRING)?
)
{
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.LINECONTROL);
}
;

pr_Pragma returns [PreprocessorDirective ppDirective]
@init { $ppDirective = null; }:
(
	DIRECTIVE_PRAGMA
)
{
	$ppDirective = new PreprocessorDirective(PreprocessorDirective.Directive_type.PRAGMA);
}
;
