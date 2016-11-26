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
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.EnumerationItems;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SequenceOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SetOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Enumerated_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.Range_ParsedSubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
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
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

import static org.eclipse.titan.designer.AST.IType.Type_type.TYPE_COMPONENT;
import static org.eclipse.titan.designer.AST.IType.Type_type.TYPE_PORT;
import static org.eclipse.titan.designer.AST.IType.Type_type.TYPE_SEQUENCE_OF;
import static org.eclipse.titan.designer.AST.IType.Type_type.TYPE_SET_OF;
import static org.eclipse.titan.designer.AST.IType.Type_type.TYPE_TTCN3_CHOICE;
import static org.eclipse.titan.designer.AST.IType.Type_type.TYPE_TTCN3_SEQUENCE;
import static org.eclipse.titan.designer.AST.IType.Type_type.TYPE_TTCN3_SET;

public class Def_Type_Visit_Handler {

	private static String setOfFieldType = null;
	private static String recordOfFieldType = null;
	private static String currentPortName;
	private static String charstringValue = null;
	private static String parentName = null;

	private static boolean waitForSetOfFieldType = false;
	private static boolean waitForRecordOfFieldType = false;
	private static boolean isPortTypeAReferencedType = false;
	private static boolean waitingForPortAttriburtes = false;
	private static boolean waitForCompReference = false;
	private static boolean waitForDefType = false;
	private static boolean waitForValue = false;

	public static List<String> portTypeList = new ArrayList<String>();
	private static List<String> componentPortTypes = new ArrayList<String>();
	private static List<String> componentPortNames = new ArrayList<String>();
	private static List<String> componentVarTypes = new ArrayList<String>();
	private static List<String> componentVarNames = new ArrayList<String>();
	private static List<String> compFieldTypes = new ArrayList<String>();
	private static List<String> compFieldNames = new ArrayList<String>();
	private static List<String> enumItems = new ArrayList<String>();
	private static List<String> enumItemValues = new ArrayList<String>();
	private static List<String> inMessageName = new ArrayList<String>();
	private static List<String> outMessageName = new ArrayList<String>();
	private static List<String> inOutMessageName = new ArrayList<String>();

	private static List<String> expressionValue = new ArrayList<String>();
	private boolean isInteger = false;

	CompilationTimeStamp compilationCounter = CompilationTimeStamp.getNewCompilationCounter();
	
	public void visit(IVisitableNode node) {
		if (node instanceof Def_Type) {
			Def_Type_Integer_Writer.allowedValues.clear();
			expressionValue.clear();// Str2IntExpression Log2StrExpression
									// ValueofExpression
			visitDefTypeNodes(node);
			charstringValue = null;
			waitForDefType = true;
			waitForValue = true;

		}

		if (waitForDefType) {
			visitDefTypeChildrenNodes(node);
			visitExpressionTypeSelectors(node);
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

		if (isInteger) {
			Def_Type_Integer_Writer.allowedValues.addAll(expressionValue);
		}

		expressionValue.clear();
	}

	public void visitExpressionTypeSelectors(IVisitableNode node) {

		if (waitForValue && (node instanceof AddExpression)) {
			expressionValue.add("AddExpression");
		}

		if (waitForValue && (node instanceof SubstractExpression)) {
			expressionValue.add("SubstractExpression");
		}

		if (waitForValue && (node instanceof MultiplyExpression)) {
			expressionValue.add("MultiplyExpression");
		}

		if (waitForValue && (node instanceof DivideExpression)) {
			expressionValue.add("DivideExpression");
		}

		if (waitForValue && (node instanceof ModuloExpression)) {
			expressionValue.add("ModuloExpression");
		}

		if (waitForValue && (node instanceof RemainderExpression)) {
			expressionValue.add("RemainderExpression");
		}

		if (waitForValue && (node instanceof NotequalesExpression)) {
			expressionValue.add("NotequalesExpression");
		}

		if (waitForValue && (node instanceof LessThanExpression)) {
			expressionValue.add("LessThanExpression");
		}

		if (waitForValue && (node instanceof LessThanOrEqualExpression)) {
			expressionValue.add("LessThanOrEqualExpression");
		}

		if (waitForValue && (node instanceof GreaterThanExpression)) {
			expressionValue.add("GreaterThanExpression");
		}

		if (waitForValue && (node instanceof GreaterThanOrEqualExpression)) {
			expressionValue.add("GreaterThanOrEqualExpression");
		}

		if (waitForValue && (node instanceof EqualsExpression)) {
			expressionValue.add("EqualsExpression");
		}

		if (waitForValue && (node instanceof NotExpression)) {
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

		/*
		 * if (waitForValue && ((node instanceof RemainderExpression) || (node
		 * instanceof ModuloExpression) || (node instanceof DivideExpression) ||
		 * (node instanceof MultiplyExpression) || (node instanceof
		 * SubstractExpression) || (node instanceof AddExpression || (node
		 * instanceof NotequalesExpression) || (node instanceof
		 * LessThanExpression) || (node instanceof LessThanOrEqualExpression) ||
		 * (node instanceof GreaterThanExpression) || (node instanceof
		 * GreaterThanOrEqualExpression) || (node instanceof
		 * EqualsExpression)))) {
		 * 
		 * }
		 */

	}

	public void visitDefTypeNodes(IVisitableNode node) {

		Def_Type typeNode = (Def_Type) node;

		

		String nodeName = typeNode.getIdentifier().toString();
		myASTVisitor.currentFileName = nodeName;

		myASTVisitor.visualizeNodeToJava(myASTVisitor.importListStrings);

		Type type = typeNode.getType(compilationCounter);
		if (type.getTypetype().equals(TYPE_TTCN3_SEQUENCE)) {// record

			myASTVisitor.nodeNameNodeTypeHashMap.put(nodeName, "record");
			parentName = nodeName;

		} else if (type.getTypetype().equals(TYPE_TTCN3_SET)) {

			myASTVisitor.nodeNameNodeTypeHashMap.put(nodeName, "set");
			parentName = nodeName;

		} else if (type.getTypetype().equals(TYPE_TTCN3_CHOICE)) {

			myASTVisitor.nodeNameNodeTypeHashMap.put(nodeName, "union");
			parentName = nodeName;

		} else if (type instanceof Integer_Type) {

			Def_Type_Integer_Writer.getInstance(typeNode);

			myASTVisitor.nodeNameNodeTypeHashMap.put(nodeName, "INTEGER");
			parentName = nodeName;
			isInteger = true;

		} else if (type instanceof CharString_Type) {

			Def_Type_Charstring_Writer.getInstance(typeNode);

			myASTVisitor.nodeNameNodeTypeHashMap.put(nodeName, "CHARSTRING");
			parentName = nodeName;

		} else if (type instanceof TTCN3_Enumerated_Type) {

			Def_Type_Enum_Writer.getInstance(typeNode);

			myASTVisitor.nodeNameNodeTypeHashMap.put(nodeName, "enum");
			parentName = nodeName;

		} else if (type.getTypetype().equals(TYPE_SET_OF)) {

			waitForSetOfFieldType = true;
			myASTVisitor.nodeNameNodeTypeHashMap.put(nodeName, "setof");
			parentName = nodeName;

		} else if (type.getTypetype().equals(TYPE_SEQUENCE_OF)) {

			waitForRecordOfFieldType = true;
			myASTVisitor.nodeNameNodeTypeHashMap.put(nodeName, "recordof");
			parentName = nodeName;

		} else if (type.getTypetype().equals(TYPE_PORT)) {

			Def_Type_Port_Writer.getInstance(typeNode);

			waitingForPortAttriburtes = true;
			currentPortName = nodeName;
			myASTVisitor.nodeNameNodeTypeHashMap.put(nodeName, "port");
			parentName = nodeName;

		} else if (type.getTypetype().equals(TYPE_COMPONENT)) {

			Def_Type_Component_Writer.getInstance(typeNode);
			waitForCompReference = true;

			AstWalkerJava.componentList.add(nodeName);
			myASTVisitor.nodeNameNodeTypeHashMap.put(nodeName, "component");
			parentName = nodeName;
		}
	}

	public void visitDefTypeChildrenNodes(IVisitableNode node) {

		if (node instanceof Def_Port) {
			Def_Port port = (Def_Port) node;
			componentPortNames.add(port.getIdentifier().toString());
		}
		
		if (node instanceof Def_Var){
			Def_Var var = (Def_Var) node;
			componentVarNames.add(var.getIdentifier().toString());
			if(var.getType(compilationCounter) instanceof Integer_Type){
				componentVarTypes.add("INTEGER");
			}
		}

		if (waitForCompReference && (node instanceof Reference)) {
			componentPortTypes.add(((Reference) node).getId().toString());
		}

		if (waitForSetOfFieldType) {
			if (node instanceof Reference) {
				setOfFieldType = node.toString();
				myASTVisitor.nodeNameSetOfTypesHashMap.put(parentName, setOfFieldType);
				waitForSetOfFieldType = false;
			} else if (node instanceof Type && !(node instanceof Referenced_Type) && !(node instanceof SetOf_Type)) {
				Type type = (Type) node;
				setOfFieldType = TypeMapper.map(type.getTypename());
				myASTVisitor.nodeNameSetOfTypesHashMap.put(parentName, setOfFieldType);
				waitForSetOfFieldType = false;
			}
		}

		if (waitForRecordOfFieldType) {
			if (node instanceof Reference) {
				recordOfFieldType = node.toString();
				myASTVisitor.nodeNameRecordOfTypesHashMap.put(parentName, recordOfFieldType);
				waitForRecordOfFieldType = false;
			} else if (node instanceof Type && !(node instanceof Referenced_Type)
					&& !(node instanceof SequenceOf_Type)) {
				Type type = (Type) node;
				recordOfFieldType = TypeMapper.map(type.getTypename());
				myASTVisitor.nodeNameRecordOfTypesHashMap.put(parentName, recordOfFieldType);
				waitForRecordOfFieldType = false;
			}
		}

		if (node instanceof CompField) { // component

			CompField compFieldNode = (CompField) node;

			if (compFieldNode.getType() instanceof Referenced_Type) {

				compFieldTypes.add(((Referenced_Type) compFieldNode.getType()).getReference().getId().toString());
			} else {
				compFieldTypes.add(myASTVisitor.cutModuleNameFromBeginning(compFieldNode.getType().getTypename()));
			}
			compFieldNames.add(compFieldNode.getIdentifier().toString());

		}

		if (node instanceof Charstring_Value) {// charstring

			Charstring_Value singleValuedNode = (Charstring_Value) node;

			charstringValue = singleValuedNode.getValue();

		}

		if (node instanceof Integer_Value) {
			String value = ((Integer_Value) node).toString();
			if (myASTVisitor.isNextIntegerNegative) {
				value = "-" + value;
			}

			expressionValue.add("new INTEGER(\"" + value + "\")");
		}

		if (node instanceof Real_Value) {
			String value = ((Real_Value) node).toString();
			if (myASTVisitor.isNextIntegerNegative) {
				value = "-" + value;
			}

			if (value.equals("-Infinity") || value.equals("Infinity")) {
				value = "null";
			}

			expressionValue.add(value);
		}

		if (node instanceof Undefined_LowerIdentifier_Value) {
			String value = ((Undefined_LowerIdentifier_Value) node).getIdentifier().toString();
			if (myASTVisitor.isNextIntegerNegative) {
				value = "-" + value;
			}

			expressionValue.add(value);
		}

		if (node instanceof EnumerationItems) {// enum

			for (int i = 0; i < ((EnumerationItems) node).getItems().size(); i++) {
				enumItems.add(((EnumerationItems) node).getItems().get(i).getId().toString());
				if (((EnumerationItems) node).getItems().get(i).getValue() != null) {
					enumItemValues.add(((EnumerationItems) node).getItems().get(i).getValue().toString());
				} else {
					enumItemValues.add(null);
				}
			}
		}

		if (waitingForPortAttriburtes && (node instanceof Referenced_Type)) {
			isPortTypeAReferencedType = true;

		}

		if (waitingForPortAttriburtes && (node instanceof PortTypeBody)) {
			PortTypeBody body = (PortTypeBody) node;
			int inCount = body.getInMessages().getNofTypes();
			int outCount = body.getOutMessage().getNofTypes();

			for (int i = 0; i < inCount; i++) {
				inMessageName.add(
						myASTVisitor.cutModuleNameFromBeginning(body.getInMessages().getTypeByIndex(i).getTypename()));
			}
			for (int i = 0; i < outCount; i++) {
				outMessageName.add(
						myASTVisitor.cutModuleNameFromBeginning(body.getOutMessage().getTypeByIndex(i).getTypename()));
			}

			int shorterListSize = inMessageName.size() <= outMessageName.size() ? inMessageName.size()
					: outMessageName.size();

			// check if one of the messages is inout
			// if inout delete from both lists and add to inout
			for(int i =0; i<inMessageName.size();i++){
				for(int j =0; j<outMessageName.size();j++){
					if (inMessageName.get(i).equals(outMessageName.get(j))){
						inOutMessageName.add(inMessageName.get(i));
						inMessageName.remove(i);
						if(j==(outMessageName.size()-1)){
							i--;
							}
						outMessageName.remove(j);
						j--;
					}
				}
			}
			
			myASTVisitor.portNamePortTypeHashMap.put(currentPortName, body.getTestportType().toString());
			portTypeList.add(body.getTestportType().toString());
		}
	}

	public void leave(IVisitableNode node) {
		if (node instanceof Def_Type) {
			evaluateExpression();
			handleDefTypeNodes(node);
			waitForDefType = false;
			waitForValue = false;

			isInteger = false;
		}
	}

	public void handleDefTypeNodes(IVisitableNode node) {
		Def_Type typeNode = (Def_Type) node;

		CompilationTimeStamp compilationCounter = CompilationTimeStamp.getNewCompilationCounter();

		myASTVisitor.currentFileName = typeNode.getIdentifier().toString();

		Type type = typeNode.getType(compilationCounter);
		if (type.getTypetype().equals(TYPE_TTCN3_SEQUENCE)) {// record

			Def_Type_Record_Writer recordNode = new Def_Type_Record_Writer(typeNode);
			// add component fields
			recordNode.add(compFieldTypes, compFieldNames);

			String[] typeArray = (String[]) compFieldTypes.toArray(new String[compFieldTypes.size()]);
			String[] nameArray = (String[]) compFieldNames.toArray(new String[compFieldNames.size()]);

			myASTVisitor.nodeNameChildrenTypesHashMap.put(parentName, typeArray);
			myASTVisitor.nodeNameChildrenNamesHashMap.put(parentName, nameArray);

			compFieldTypes.clear();
			compFieldNames.clear();

			myASTVisitor.visualizeNodeToJava(recordNode.getJavaSource());

		} else if (type.getTypetype().equals(TYPE_TTCN3_SET)) {// set

			Def_Type_Set_Writer setNode = new Def_Type_Set_Writer(typeNode);
			// add component fields
			setNode.add(compFieldTypes, compFieldNames);

			String[] typeArray = (String[]) compFieldTypes.toArray(new String[compFieldTypes.size()]);
			String[] nameArray = (String[]) compFieldNames.toArray(new String[compFieldNames.size()]);

			myASTVisitor.nodeNameChildrenTypesHashMap.put(parentName, typeArray);
			myASTVisitor.nodeNameChildrenNamesHashMap.put(parentName, nameArray);

			compFieldTypes.clear();
			compFieldNames.clear();

			myASTVisitor.visualizeNodeToJava(setNode.getJavaSource());

		} else if (type.getTypetype().equals(TYPE_TTCN3_CHOICE)) {// union

			Def_Type_Union_Writer union_writer = new Def_Type_Union_Writer(typeNode);
			// add component fields
			union_writer.add(compFieldTypes, compFieldNames);

			String[] typeArray = compFieldTypes.toArray(new String[compFieldTypes.size()]);
			String[] nameArray = compFieldNames.toArray(new String[compFieldNames.size()]);

			myASTVisitor.nodeNameChildrenTypesHashMap.put(parentName, typeArray);
			myASTVisitor.nodeNameChildrenNamesHashMap.put(parentName, nameArray);

			compFieldTypes.clear();
			compFieldNames.clear();

			myASTVisitor.visualizeNodeToJava(union_writer.getJavaSource());

		} else if (type instanceof Integer_Type) {

			Def_Type_Integer_Writer integerNode = Def_Type_Integer_Writer.getInstance(typeNode);
			integerNode.clearLists();

			myASTVisitor.visualizeNodeToJava(integerNode.getJavaSource());

		} else if (type instanceof CharString_Type) {

			Def_Type_Charstring_Writer charstringNode = Def_Type_Charstring_Writer.getInstance(typeNode);
			charstringNode.clearLists();

			charstringNode.addCharStringValue(charstringValue);
			charstringValue = null;

			myASTVisitor.visualizeNodeToJava(charstringNode.getJavaSource());

		} else if (type instanceof TTCN3_Enumerated_Type) {

			Def_Type_Enum_Writer enumTypeNode = Def_Type_Enum_Writer.getInstance(typeNode);
			enumTypeNode.clearLists();

			enumTypeNode.enumItems.addAll(enumItems);
			enumTypeNode.enumItemValues.addAll(enumItemValues);

			enumItemValues.clear();
			enumItems.clear();

			myASTVisitor.visualizeNodeToJava(enumTypeNode.getJavaSource());

		} else if (type.getTypetype().equals(TYPE_SET_OF)) {

			Def_Type_Set_Of_Writer setOfNode = new Def_Type_Set_Of_Writer(typeNode);
			setOfNode.setFieldType(setOfFieldType);
			setOfFieldType = null;

			myASTVisitor.visualizeNodeToJava(setOfNode.getJavaSource());

		} else if (type.getTypetype().equals(TYPE_SEQUENCE_OF)) {

			Def_Type_Record_Of_Writer writer = new Def_Type_Record_Of_Writer(typeNode);
			writer.setFieldType(recordOfFieldType);
			myASTVisitor.visualizeNodeToJava(writer.getJavaSource());

		} else if (type.getTypetype().equals(TYPE_PORT)) {

			Def_Type_Port_Writer portNode = Def_Type_Port_Writer.getInstance(typeNode);
			portNode.clearLists();

			portNode.inMessageName.addAll(inMessageName);
			portNode.outMessageName.addAll(outMessageName);
			portNode.inOutMessageName.addAll(inOutMessageName);

			portNode.setPortTypeAReferencedType(isPortTypeAReferencedType);

			waitingForPortAttriburtes = false;
			isPortTypeAReferencedType = false;
			inMessageName.clear();
			outMessageName.clear();
			inOutMessageName.clear();
			myASTVisitor.visualizeNodeToJava(portNode.getJavaSource());

		} else if (type.getTypetype().equals(TYPE_COMPONENT)) {

			Def_Type_Component_Writer compNode = Def_Type_Component_Writer.getInstance(typeNode);
			compNode.clearLists();

			// add component fields

			compNode.compFieldPortTypes.addAll(componentPortTypes);
			compNode.compFieldPortNames.addAll(componentPortNames);
			compNode.compFieldVarTypes.addAll(componentVarTypes);
			compNode.compFieldVarNames.addAll(componentVarNames);

			componentPortTypes.clear();
			componentPortNames.clear();
			componentVarTypes.clear();
			componentVarNames.clear();
			waitForCompReference = false;
			myASTVisitor.visualizeNodeToJava(compNode.getJavaSource());

		}
		parentName = null;
	}
}
