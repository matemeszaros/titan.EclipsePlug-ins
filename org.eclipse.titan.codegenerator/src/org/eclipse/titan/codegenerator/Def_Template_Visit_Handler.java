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

import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.AnyOrOmit_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Any_Value_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ListOfTemplates;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplates;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.Range_ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Omit_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
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

public class Def_Template_Visit_Handler {

	private static String modifierValue = null;
	private static String lastTemplateName = null;
	private static String currentTemplateNodeName = null;

	private static int templateRefdValCounter = -1;

	private static boolean waitForModifierValue = false;
	private static boolean isTemplateNameSet = false;
	private static boolean waitForTemplateRefdVals = false;
	private static boolean waitForTemplateValues = false;
	private static boolean waitForSepcValTemplate = false;

	private static List<String> templateListValues = new ArrayList<String>();
	private static List<String> templateAllIdentifiers = new ArrayList<String>();
	private static List<String> templateRefdVals = new ArrayList<String>();
	private boolean waitForTemplateList = false;
	private boolean blockTemplateListing = false;
	private boolean waitForNamedTemplates = false;
	public static boolean isTemplate=false;
	
	private static List<String> expressionValue = new ArrayList<String>();
	private boolean isInteger = false;
	private String exressionResult="";
	
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

				} else if (myASTVisitor.nodeNameNodeTypeHashMap.get(expressionValue.get(i)).equals("INTEGER")
						||myASTVisitor.nodeNameNodeTypeHashMap.get(expressionValue.get(i)).equals("CHARSTRING")
						||myASTVisitor.nodeNameNodeTypeHashMap.get(expressionValue.get(i)).equals("BITSTRING")
						||myASTVisitor.nodeNameNodeTypeHashMap.get(expressionValue.get(i)).equals("BOOLEAN")
						||myASTVisitor.nodeNameNodeTypeHashMap.get(expressionValue.get(i)).equals("OCTETSTRING")
						||myASTVisitor.nodeNameNodeTypeHashMap.get(expressionValue.get(i)).equals("HEXSTRING")
						) {
					if (myASTVisitor.nodeNameAllowedValuesHashmap.containsKey(expressionValue.get(i))) {

						expressionValue.set(i, myASTVisitor.nodeNameAllowedValuesHashmap.get(expressionValue.get(i)));

					}
				}

			}

			if (i <= size - 2) {
				leftHand = expressionValue.get(i + 1);
				rightHand = expressionValue.get(i + 2);
			}

			//
			if (expressionValue.get(i).equals("Str2IntExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ".str2int())");
				unaryOperatorFound = true;
			} else if (expressionValue.get(i).equals("Log2StrExpression")) {
				expressionValue.set(i, "(" + expressionValue.get(i + 1) + ".log2str())");
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
				expressionValue.set(i, "(" + leftHand + ").equalsWith(" + rightHand + ")");
				operatorFound = true;
			}/* else if (expressionValue.get(i).equals("Range_ParsedSubType")) {
				expressionValue.set(i, "new SubTypeInterval<INTEGER>(" + expressionValue.get(i + 1) + ","
						+ expressionValue.get(i + 2) + ")");
				operatorFound = true;

			}*/

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
if(expressionValue.size()>0){
		exressionResult=expressionValue.get(0);
}
		expressionValue.clear();
	}

	public void visitExpressionTypeSelectors(IVisitableNode node) {

		if (waitForSepcValTemplate && (node instanceof AddExpression)) {
			expressionValue.add("AddExpression");
			isTemplate=false;
		}

		if (waitForSepcValTemplate && (node instanceof SubstractExpression)) {
			expressionValue.add("SubstractExpression");
		}

		if (waitForSepcValTemplate && (node instanceof MultiplyExpression)) {
			expressionValue.add("MultiplyExpression");
		}

		if (waitForSepcValTemplate && (node instanceof DivideExpression)) {
			expressionValue.add("DivideExpression");
		}

		if (waitForSepcValTemplate && (node instanceof ModuloExpression)) {
			expressionValue.add("ModuloExpression");
		}

		if (waitForSepcValTemplate && (node instanceof RemainderExpression)) {
			expressionValue.add("RemainderExpression");
		}

		if (waitForSepcValTemplate && (node instanceof NotequalesExpression)) {
			expressionValue.add("NotequalesExpression");
		}

		if (waitForSepcValTemplate && (node instanceof LessThanExpression)) {
			expressionValue.add("LessThanExpression");
		}

		if (waitForSepcValTemplate && (node instanceof LessThanOrEqualExpression)) {
			expressionValue.add("LessThanOrEqualExpression");
		}

		if (waitForSepcValTemplate && (node instanceof GreaterThanExpression)) {
			expressionValue.add("GreaterThanExpression");
		}

		if (waitForSepcValTemplate && (node instanceof GreaterThanOrEqualExpression)) {
			expressionValue.add("GreaterThanOrEqualExpression");
		}

		if (waitForSepcValTemplate && (node instanceof EqualsExpression)) {
			expressionValue.add("EqualsExpression");
		}

		if (waitForSepcValTemplate && (node instanceof NotExpression)) {
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
	
	
	
	

	public void visit(IVisitableNode node) {

		if (node instanceof Def_Template) {
			myASTVisitor.currentFileName = "Templates";

			Def_Template_Writer.getInstance(((Def_Template) node));

			myASTVisitor.nodeNameNodeTypeHashMap.put(((Def_Template) node)
					.getIdentifier().toString(), "template");

			isTemplate=true;
			waitForTemplateRefdVals=false;
			
			templateRefdValCounter=-1;
			waitForModifierValue = true;
			waitForTemplateValues = true;

		}
 
		visitTemplateNodes(node);//SpecificValueTemplate AddExpression Undefined_LowerIdentifier_Value Referenced_Value Reference 
	}

	public void visitTemplateNodes(IVisitableNode node) {
		
		visitExpressionTypeSelectors(node);
		

		
		if (waitForTemplateValues && (node instanceof Reference)) {
			Def_Const_Visit_Handler.constNodeType = ((Reference) node).getId()
					.toString();
		}
		
		if(node instanceof Named_Template_List){
			waitForNamedTemplates=true;
		}

		//ListOfTemplates is returned twice, should be recorded only once
		if (waitForTemplateValues && !blockTemplateListing&&(node instanceof ListOfTemplates)) {
			waitForTemplateList = true;
		}
		
		/*if (waitForTemplateValues && !blockTemplateListing&&(node instanceof NamedTemplates)) {
			waitForTemplateList = true;
		}*/


		if (waitForTemplateValues && (node instanceof SpecificValue_Template)) {
			waitForSepcValTemplate = true;

		}
		
		if (waitForTemplateValues
				&& (node instanceof Undefined_LowerIdentifier_Value)) {
			String value = ((Undefined_LowerIdentifier_Value) node)
					.getIdentifier().toString();

			if (waitForTemplateList) {
				templateListValues.add(value);
			} else {
				myASTVisitor.templateIdValuePairs.put(lastTemplateName, value);
			}
			myASTVisitor.blockIdListing = true;
		}

		if (waitForTemplateValues && (node instanceof Charstring_Value)) {

			String value = ((Charstring_Value) node).getValue();

			if (waitForTemplateList) {
				templateListValues.add(value);
			} else {
				myASTVisitor.templateIdValuePairs.put(lastTemplateName, value);
			}

			myASTVisitor.blockIdListing = true;
		}
		
		if (waitForTemplateValues && (node instanceof Integer_Value)) {

			String value = ((Integer_Value) node).toString();

			if (myASTVisitor.isNextIntegerNegative) {
				value = "-" + value;
			}

			if (waitForTemplateList) {
				templateListValues.add(value);
			} else {
				myASTVisitor.templateIdValuePairs.put(lastTemplateName, value);
			}

			myASTVisitor.isNextIntegerNegative = false;
			myASTVisitor.blockIdListing = true;
		}

		if (waitForTemplateValues && (node instanceof Omit_Value)) {

			if (waitForTemplateList) {
				templateListValues.add("omit");
			} else {
				myASTVisitor.templateIdValuePairs.put(lastTemplateName, "omit");
			}
			myASTVisitor.blockIdListing = true;
		}

		if (waitForTemplateValues && (node instanceof Any_Value_Template)) {

			if (waitForTemplateList) {
				templateListValues.add("?");
			} else {
				myASTVisitor.templateIdValuePairs.put(lastTemplateName, "?");
			}
			myASTVisitor.blockIdListing = true;
		}

		if (waitForTemplateValues && (node instanceof AnyOrOmit_Template)) {

			if (waitForTemplateList) {
				templateListValues.add("*");
			} else {
				myASTVisitor.templateIdValuePairs.put(lastTemplateName, "*");
			}
			myASTVisitor.blockIdListing = true;
		}

		if (waitForTemplateValues && (node instanceof Reference)) {

			String value = ((Reference) node).toString().toString();

			if (waitForTemplateList) {
				templateListValues.add(value);
			} else if (!waitForSepcValTemplate) {
				myASTVisitor.templateIdValuePairs.put(lastTemplateName, value);
			}

			myASTVisitor.blockIdListing = true;
		}

		if (waitForTemplateValues && (node instanceof Identifier)
				&& (!myASTVisitor.blockIdListing|| waitForNamedTemplates)) {
			templateAllIdentifiers.add(((Identifier) node).toString());
			lastTemplateName = ((Identifier) node).toString();
			myASTVisitor.templateIDs.add(lastTemplateName);

			if (!isTemplateNameSet) {
				currentTemplateNodeName = ((Identifier) node).toString();
				isTemplateNameSet = true;
			}

		}

		
		if (waitForSepcValTemplate&& isTemplate && (node instanceof Referenced_Value)) {
			templateRefdValCounter++;
			waitForTemplateRefdVals = true;

		}
		
		if (waitForSepcValTemplate && (node instanceof Reference)) {
			expressionValue.add(((Reference)node).getId().toString());
		}


		if (waitForTemplateRefdVals&& isTemplate  && (node instanceof FieldSubReference)) {
			if (templateRefdVals.size() == templateRefdValCounter) {
				templateRefdVals.add(((FieldSubReference) node).getId()
						.toString());
			} else {
				templateRefdVals
						.set(templateRefdValCounter,
								templateRefdVals.get(templateRefdValCounter)
										+ "."
										+ ((FieldSubReference) node).getId()
												.toString());
				waitForTemplateRefdVals=false; //TODO
			}
		}

		if (waitForModifierValue && (node instanceof Named_Template_List)) {
			waitForModifierValue = false;
			
		}

		if (waitForModifierValue && (node instanceof Reference)) {
			modifierValue = ((Reference) node).getId().toString();
		}
	}

	public void leave(IVisitableNode node) {
		if (node instanceof Def_Template) {
			handleTemplate(node);
		}

		if(node instanceof Named_Template_List){
			waitForNamedTemplates=false;;
		}

		
		if (waitForTemplateValues && (node instanceof SpecificValue_Template)) {
			evaluateExpression();
			if(waitForTemplateRefdVals){
			myASTVisitor.templateIdValuePairs.put(currentTemplateNodeName,
					templateRefdVals.get(templateRefdValCounter));
			} else {
				myASTVisitor.templateIdValuePairs.put(currentTemplateNodeName,
						exressionResult);
			}
			
			waitForSepcValTemplate = false;

		}
		
		if (waitForTemplateValues
				&& ((node instanceof Undefined_LowerIdentifier_Value)
						|| (node instanceof Charstring_Value)
						|| (node instanceof Omit_Value)
						|| (node instanceof Any_Value_Template)
						|| (node instanceof AnyOrOmit_Template) || (node instanceof Reference))) {
			myASTVisitor.blockIdListing = false;
		}

		if (waitForTemplateValues && (node instanceof ListOfTemplates)) {
			waitForTemplateList = false;
			//ListOfTemplates is returned twice, should be recorded only once
			blockTemplateListing= true;

		}
		
		if (waitForTemplateValues && (node instanceof NamedTemplates)) {
			waitForTemplateList = false;

			
		}

		/*if (waitForSepcValTemplate && isTemplate && (node instanceof Referenced_Value)) {
			myASTVisitor.templateIdValuePairs.put(currentTemplateNodeName,
					templateRefdVals.get(templateRefdValCounter));

			waitForSepcValTemplate = false;
			// waitForTemplateRefdVals = false;

		}*/
	}

	public void handleTemplate(IVisitableNode node) {
		Def_Template_Writer tempNode = Def_Template_Writer
				.getInstance(((Def_Template) node));

		tempNode.clearLists();
		tempNode.init();
		myASTVisitor.blockIdListing = false;

		tempNode.templateIdentifiers.addAll(templateAllIdentifiers);
		tempNode.templateRefdVals.addAll(templateRefdVals);
		tempNode.templateListValues.addAll(templateListValues);

		if (modifierValue != null) {
			tempNode.setModifierValue(modifierValue);
		}
		isTemplate=false;
		templateAllIdentifiers.clear();
		templateListValues.clear();
		templateRefdVals.clear();
		isTemplateNameSet = false;
		waitForTemplateValues = false;
		waitForModifierValue = false;
		templateRefdValCounter = -1;
		waitForTemplateRefdVals = false;
		blockTemplateListing= false;

		myASTVisitor.deleteLastBracket(myASTVisitor.currentFileName);
		myASTVisitor.visualizeNodeToJava(tempNode.getJavaSource() + "\r\n}");
		myASTVisitor.templateIDs.clear();
		myASTVisitor.templateIdValuePairs.clear();
	}

}
