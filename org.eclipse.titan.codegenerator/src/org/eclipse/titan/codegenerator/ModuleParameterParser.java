/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.types.BitString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Boolean_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.HexString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.OctetString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;

class ModuleParameterParser implements Scope {

	private final myASTVisitor visitor;

	private String name;
	private String type;
	private String value;

	ModuleParameterParser(myASTVisitor visitor) {
		this.visitor = visitor;
	}

	@Override
	public Scope process(IVisitableNode node) {
		if (node instanceof Identifier) {
			name = node.toString();
			myASTVisitor.nodeNameNodeTypeHashMap.put(name, "modulePar");
		}

		// TODO : generalize type parsing / handling
		if ((node instanceof Boolean_Type)) {
			type = "BOOLEAN";
		}
		if ((node instanceof Integer_Type)) {
			type = "INTEGER";
		}
		if ((node instanceof CharString_Type)) {
			type = "CHARSTRING";
		}
		if ((node instanceof BitString_Type)) {
			type = "BITSTRING";
		}
		if ((node instanceof HexString_Type)) {
			type = "HEXSTRING";
		}
		if ((node instanceof OctetString_Type)) {
			type = "OCTETSTRING";
		}
		if (node instanceof Referenced_Type) {
			type = ((Referenced_Type) node).getReference().toString();
			// TODO : the value should be created as this type of object
			// e.g.:
			// type integer Digit (0..9);
			// modulepar Digit d := 3;
		}

		// TODO : generalize value parsing / handling
		if ((node instanceof Boolean_Value)) {
			value = ((Boolean_Value) node).getValue() ? "BOOLEAN.TRUE" : "BOOLEAN.FALSE";
		}
		if ((node instanceof Integer_Value)) {
			value = "new INTEGER(\"" + node.toString() + "\")";
		}
		if ((node instanceof Charstring_Value)) {
			value = ((Charstring_Value) node).getValue();
			value = "new CHARSTRING(\"" + value + "\")";
		}
		if ((node instanceof Bitstring_Value)) {
			value = ((Bitstring_Value) node).getValue();
			value = "new BITSTRING(\"" + value + "\")";
		}
		if ((node instanceof Hexstring_Value)) {
			value = ((Hexstring_Value) node).getValue();
			value = "new HEXSTRING(\"" + value + "\")";
		}
		if ((node instanceof Octetstring_Value)) {
			value = ((Octetstring_Value) node).getValue();
			value = "new OCTETSTRING(\"" + value + "\")";
		}
		// TODO : handle (referenced) subtype values
		// TODO : handle record, set, record of and set of subtypes and values
		return this;
	}

	@Override
	public Scope finish(IVisitableNode node) {
		if (node instanceof Def_ModulePar) {
			visitor.parameters.add(type, name, value);
			return visitor;
		}
		return this;
	}
}
