/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   
 *   Keremi, Andras
 *   Eros, Levente
 *   Kovacs, Gabor
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.types.BitString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Boolean_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.OctetString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.Range_ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Omit_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AddExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.DivideExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.EqualsExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.GreaterThanExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.GreaterThanOrEqualExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.LessThanExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.LessThanOrEqualExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Log2StrExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ModuloExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.MultiplyExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.NotExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.NotequalesExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.RemainderExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.Str2IntExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.SubstractExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ValueofExpression;

public class Def_Const_Visit_Handler {

	public static String constNodeType = null;
	private static String lastConstName = null;

	private static boolean waitForConstValues = false;
	private static boolean constValueIsAReference = false;

	// counts the members for each setof
	// has to be -3 because the node's name and type increases it by 2
	// should be increased to 0 only at the first constant value
	private static int constParamCounter = -3;


	private static List<String> constValues = new ArrayList<String>();
	private static List<Integer> constParamCount = new ArrayList<Integer>();
	private static List<String> expressionValue = new ArrayList<String>();
	
	public void visit(IVisitableNode node) {

		if (!myASTVisitor.myFunctionTestCaseVisitHandler.waitForDefStatement
				&& (node instanceof Def_Const)) {
			myASTVisitor.currentFileName = "Constants";

			Def_Const_Writer.getInstance(((Def_Const) node));
			myASTVisitor.nodeNameNodeTypeHashMap.put(((Def_Const) node)
					.getIdentifier().toString(), "constant");
			waitForConstValues = true;
			constParamCount.add(0);
		}

		// constants
		visitConstantNodes(node);
	}

	public void visitConstantNodes(IVisitableNode node) {

		if (waitForConstValues && (node instanceof Identifier)) {
			constParamCounter++;
			if (constParamCounter > 0) {
				constParamCount.add(0);
			}
			lastConstName = ((Identifier) node).toString();
		}

		if (waitForConstValues && (node instanceof Charstring_Value)) {
			String value=((Charstring_Value) node).getValue();
			
			expressionValue.add(value);

			constValueIsAReference = false;

			if (constParamCounter == -1) {
				constParamCounter = 0;
			}
			if (constParamCounter == -2) {
				constParamCounter = 0;
			}

			constParamCount.set(constParamCounter,
					constParamCount.get(constParamCounter) + 1);
		}

		if (waitForConstValues && (node instanceof Integer_Value)) {
			
			String value=((Integer_Value) node).toString();
						
			if (myASTVisitor.isNextIntegerNegative) {
				value="-" + value;
			}
			
			expressionValue.add(value);
			
			constValueIsAReference = false;

			if (constParamCounter == -1) {
				constParamCounter = 0;
			}
			if (constParamCounter == -2) {
				constParamCounter = 0;
			}

			constParamCount.set(constParamCounter,
					constParamCount.get(constParamCounter) + 1);
			myASTVisitor.isNextIntegerNegative = false;

		}

		if (waitForConstValues && (node instanceof Bitstring_Value)) {
			
			String value=((Bitstring_Value) node).getValue();

			expressionValue.add(value);

			constValueIsAReference = false;

			if (constParamCounter == -1) {
				constParamCounter = 0;
			}
			if (constParamCounter == -2) {
				constParamCounter = 0;
			}

			constParamCount.set(constParamCounter,
					constParamCount.get(constParamCounter) + 1);

		}

		if (waitForConstValues && (node instanceof Octetstring_Value)) {
			
			
			String value=((Octetstring_Value) node).getValue();

			expressionValue.add(value);
			
			constValueIsAReference = false;

			if (constParamCounter == -1) {
				constParamCounter = 0;
			}
			if (constParamCounter == -2) {
				constParamCounter = 0;
			}

			constParamCount.set(constParamCounter,
					constParamCount.get(constParamCounter) + 1);

		}

		if (waitForConstValues && (node instanceof Boolean_Value)) {
			
			String value=Boolean.toString(((Boolean_Value) node).getValue());

			expressionValue.add(value);			

			constValueIsAReference = false;

			if (constParamCounter == -1) {
				constParamCounter = 0;
			}
			if (constParamCounter == -2) {
				constParamCounter = 0;
			}

			constParamCount.set(constParamCounter,
					constParamCount.get(constParamCounter) + 1);

		}

		if (waitForConstValues && (node instanceof Omit_Value)) {

			expressionValue.add("omit");			

			constValueIsAReference = false;

			if (constParamCounter == -1) {
				constParamCounter = 0;
			}

			constParamCount.set(constParamCounter,
					constParamCount.get(constParamCounter) + 1);

			myASTVisitor.constOmitHashes.add(lastConstName);

		}
		
		if (waitForConstValues && (node instanceof Undefined_LowerIdentifier_Value)) {
			
			String value=((Undefined_LowerIdentifier_Value) node).getIdentifier().toString();

			expressionValue.add(value);		

			constValueIsAReference = true;

			if (constParamCounter == -1) {
				constParamCounter = 0;
			}
			if (constParamCounter == -2) {
				constParamCounter = 0;
			}

			constParamCount.set(constParamCounter,
					constParamCount.get(constParamCounter) + 1);

		}

		if(waitForConstValues){
		setType(node);
		visitExpressionTypeSelectors(node);
		}
		/*
		// AddExpression
		if (waitForConstValues && (node instanceof AddExpression)) {
			waitForConstAddFirstValue = true;
		}

		if (waitForConstAddSecondValue
				&& (node instanceof Undefined_LowerIdentifier_Value)) {

			String valueIdentifier = ((Undefined_LowerIdentifier_Value) node)
					.getIdentifier().toString();
			if (myASTVisitor.nodeNameNodeTypeHashMap
					.containsKey(valueIdentifier)) {
				if (myASTVisitor.nodeNameNodeTypeHashMap.get(valueIdentifier)
						.equals("constant")) {
					valueIdentifier = valueIdentifier + "()";
				}
			}

			constValueIsAReference = true;

			constValues.set(constAddValueIndex,
					constValues.get(constAddValueIndex) + ".plus("
							+ valueIdentifier + ")");

			waitForConstAddSecondValue = false;
			waitForConstValues = false;
		}

		if (waitForConstAddFirstValue
				&& (node instanceof Undefined_LowerIdentifier_Value)) {
			waitForConstAddFirstValue = false;
			waitForConstAddSecondValue = true;

			String valueIdentifier = ((Undefined_LowerIdentifier_Value) node)
					.getIdentifier().toString();
			if (myASTVisitor.nodeNameNodeTypeHashMap
					.containsKey(valueIdentifier)) {
				if (myASTVisitor.nodeNameNodeTypeHashMap.get(valueIdentifier)
						.equals("constant")) {
					valueIdentifier = valueIdentifier + "()";
				}

			}

			constAddValueIndex = constValues.size();
			constValues.add(valueIdentifier);

			if (constParamCounter == -1) {
				constParamCounter = 0;
			}
			if (constParamCounter == -2) {
				constParamCounter = 0;
			}

			constParamCount.set(constParamCounter,
					constParamCount.get(constParamCounter) + 1);

		}*/

	}

	public void setType(IVisitableNode node){

		if (node instanceof Reference) {
			constNodeType = ((Reference) node).getId().toString();
		}

		if (node instanceof Integer_Type) {
			constNodeType = "INTEGER";
		}

		if (node instanceof CharString_Type) {
			constNodeType = "CHARSTRING";
		}

		if (node instanceof BitString_Type) {
			constNodeType = "BITSTRING";
		}

		if (node instanceof OctetString_Type) {
			constNodeType = "OCTETSTRING";
		}

		if (node instanceof Boolean_Type) {
			constNodeType = "BOOLEAN";
		}
	}

	public void evaluateExpression() {
		int size = expressionValue.size() - 1;
		boolean operatorFound = false;
		boolean unaryOperatorFound = false;
		String rightHand = "";
		String leftHand = "";

		for (int i = size; i >= 0; i--) {

			if (myASTVisitor.nodeNameNodeTypeHashMap.containsKey(expressionValue.get(i))) {

				if (myASTVisitor.nodeNameNodeTypeHashMap.get(expressionValue.get(i)).equals("constant")) {
					expressionValue.set(i, "Constants." + expressionValue.get(i) + "().value");

				} else if (myASTVisitor.nodeNameNodeTypeHashMap.get(expressionValue.get(i)).equals("template")) {
					expressionValue.set(i, "Templates." + expressionValue.get(i) + "()");

				}
			}

			if (i <= size - 2) {
				leftHand = expressionValue.get(i + 1);
				rightHand = expressionValue.get(i + 2);
			}

			//
			if (expressionValue.get(i).equals("Str2IntExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + "str2int())");
				unaryOperatorFound = true;
			} else if (expressionValue.get(i).equals("Log2StrExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + "log2str())");
				unaryOperatorFound = true;
			} else if (expressionValue.get(i).equals("ValueofExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ").value");
				unaryOperatorFound = true;
			} else if (expressionValue.get(i).equals("NotExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ").not()");
				unaryOperatorFound = true;
			} else if (expressionValue.get(i).equals("AddExpression")) {
				expressionValue.set(i, "(" + leftHand + ").plus(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("SubstractExpression")) {
				expressionValue.set(i, "(" + leftHand + ").minus(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("MultiplyExpression")) {
				expressionValue.set(i, "(" + leftHand + ").multipleBy(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("DivideExpression")) {
				expressionValue.set(i, "(" + leftHand + ").divideBy(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("ModuloExpression")) {
				expressionValue.set(i, "(" + leftHand + ").mod(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("RemainderExpression")) {
				expressionValue.set(i, "(" + leftHand + ").rem(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("NotequalesExpression")) {
				expressionValue.set(i, "(" + leftHand + ").equalsWith(" + rightHand + ").not()");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("LessThanExpression")) {
				expressionValue.set(i, "(" + leftHand + ").isLessThan(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("LessThanOrEqualExpression")) {
				expressionValue.set(i, "(" + leftHand + ").isLessOrEqualThan(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("GreaterThanExpression")) {
				expressionValue.set(i, "(" + leftHand + ").isGreaterThan(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("GreaterThanOrEqualExpression")) {
				expressionValue.set(i, "(" + leftHand + ").isGreaterOrEqualThan(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("EqualsExpression")) {
				expressionValue.set(i, "(" + leftHand + ").equals(" + rightHand + ")");
				operatorFound = true;
			} else if (expressionValue.get(i).equals("Range_ParsedSubType")) {
				expressionValue.set(i, "new SubTypeInterval<INTEGER>(" + expressionValue.get(i + 1) + ","
						+ expressionValue.get(i + 2) + ")");
				operatorFound = true;
			}

			if (unaryOperatorFound) {
				unaryOperatorFound = false;
				expressionValue.remove(i + 1);
				size = expressionValue.size() - 1;
				i = size;
			} else if (operatorFound) {
				operatorFound = false;
				expressionValue.remove(i + 2);
				expressionValue.remove(i + 1);
				size = expressionValue.size() - 1;
				i = size;
			}

		}

		constValues.addAll(expressionValue);

		expressionValue.clear();
	}


	public void visitExpressionTypeSelectors(IVisitableNode node) {

		if (node instanceof AddExpression) {
			expressionValue.add("AddExpression");
		}

		if (node instanceof SubstractExpression) {
			expressionValue.add("SubstractExpression");
		}

		if (node instanceof MultiplyExpression) {
			expressionValue.add("MultiplyExpression");
		}

		if (node instanceof DivideExpression) {
			expressionValue.add("DivideExpression");
		}

		if (node instanceof ModuloExpression) {
			expressionValue.add("ModuloExpression");
		}

		if (node instanceof RemainderExpression) {
			expressionValue.add("RemainderExpression");
		}

		if (node instanceof NotequalesExpression) {
			expressionValue.add("NotequalesExpression");
		}

		if (node instanceof LessThanExpression) {
			expressionValue.add("LessThanExpression");
		}

		if (node instanceof LessThanOrEqualExpression) {
			expressionValue.add("LessThanOrEqualExpression");
		}

		if (node instanceof GreaterThanExpression) {
			expressionValue.add("GreaterThanExpression");
		}

		if (node instanceof GreaterThanOrEqualExpression) {
			expressionValue.add("GreaterThanOrEqualExpression");
		}

		if (node instanceof EqualsExpression) {
			expressionValue.add("EqualsExpression");
		}

		if (node instanceof NotExpression) {
			expressionValue.add("NotExpression");
		}

		if (node instanceof Range_ParsedSubType) {
			expressionValue.add("Range_ParsedSubType");
		}

		if (node instanceof Str2IntExpression) {
			expressionValue.add("Str2IntExpression");
		}

		if (node instanceof Log2StrExpression) {
			expressionValue.add("Log2StrExpression");
		}

		if (node instanceof ValueofExpression) {
			expressionValue.add("ValueofExpression");
		}

	}
	
	public void leave(IVisitableNode node) {
		if (!myASTVisitor.myFunctionTestCaseVisitHandler.waitForDefStatement
				&& (node instanceof Def_Const)) {
			evaluateExpression();
			handleConst(node);

		}
	}

	public void handleConst(IVisitableNode node) {
		Def_Const_Writer constNode = Def_Const_Writer
				.getInstance(((Def_Const) node));
		constNode.clearLists();
		
		constNode.setConstNodeType(constNodeType);
		constNode.constValues.addAll(constValues);
		constNode.constParamCount.addAll(constParamCount);
		constNode.constValueIsAReference = constValueIsAReference;

		constValues.clear();
		constValueIsAReference = false;
		waitForConstValues = false;
		constParamCounter = -3;
		constParamCount.clear();


		myASTVisitor.deleteLastBracket(myASTVisitor.currentFileName);
		myASTVisitor.visualizeNodeToJava(constNode.getJavaSource() + "\r\n}");
		myASTVisitor.constOmitHashes.clear();
	}

}
