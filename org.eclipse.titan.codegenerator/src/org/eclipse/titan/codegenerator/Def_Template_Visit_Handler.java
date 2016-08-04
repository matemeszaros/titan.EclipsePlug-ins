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
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Omit_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;

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
	

	public void visit(IVisitableNode node) {

		if (node instanceof Def_Template) {
			myASTVisitor.currentFileName = "Templates";

			Def_Template_Writer.getInstance(((Def_Template) node));

			myASTVisitor.nodeNameNodeTypeHashMap.put(((Def_Template) node)
					.getIdentifier().toString(), "template");

			waitForModifierValue = true;
			waitForTemplateValues = true;

		}

		visitTemplateNodes(node);
	}

	public void visitTemplateNodes(IVisitableNode node) {
		if (waitForTemplateValues && (node instanceof Reference)) {
			Def_Const_Visit_Handler.constNodeType = ((Reference) node).getId()
					.toString();
		}

		//ListOfTemplates is returned twice, should be recorded only once
		if (waitForTemplateValues && !blockTemplateListing&&(node instanceof ListOfTemplates)) {
			waitForTemplateList = true;
		}

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

			if (waitForTemplateList) {
				templateListValues.add(value);
			} else {
				myASTVisitor.templateIdValuePairs.put(lastTemplateName, value);
			}

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
				&& !myASTVisitor.blockIdListing) {
			templateAllIdentifiers.add(((Identifier) node).toString());
			lastTemplateName = ((Identifier) node).toString();
			myASTVisitor.templateIDs.add(lastTemplateName);

			if (!isTemplateNameSet) {
				currentTemplateNodeName = ((Identifier) node).toString();
				isTemplateNameSet = true;
			}

		}

		if (waitForSepcValTemplate && (node instanceof Referenced_Value)) {
			templateRefdValCounter++;
			waitForTemplateRefdVals = true;

		}// Referenced_Value Reference

		if (waitForTemplateRefdVals && (node instanceof FieldSubReference)) {
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
			}
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

		if (waitForSepcValTemplate && (node instanceof Referenced_Value)) {
			myASTVisitor.templateIdValuePairs.put(currentTemplateNodeName,
					templateRefdVals.get(templateRefdValCounter));

			waitForSepcValTemplate = false;
			// waitForTemplateRefdVals = false;

		}
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
