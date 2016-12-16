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

package org.eclipse.titan.codegenerator.template;

import org.eclipse.titan.codegenerator.TypeMapper;
import org.eclipse.titan.codegenerator.myASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Bitstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Enumerated_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Hexstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Octetstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Omit_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.Type;

/**
 * Utility class for holding static state-independent helper functions.
 */
public final class Util {
	private Util() {}

	public static boolean isRecordOf(String type) {
		return "recordof".equals(myASTVisitor.nodeNameNodeTypeHashMap.get(type));
	}

	public static boolean isSetOf(String type) {
		return "setof".equals(myASTVisitor.nodeNameNodeTypeHashMap.get(type));
	}

	public static boolean isUnion(String type) {
		return "union".equals(myASTVisitor.nodeNameNodeTypeHashMap.get(type));
	}

	public static boolean isConstant(String type) {
		return "constant".equals(myASTVisitor.nodeNameNodeTypeHashMap.get(type));
	}

	public static boolean isEnumerated(String type) {
		return "enum".equals(myASTVisitor.nodeNameNodeTypeHashMap.get(type));
	}

	/**
	 * Extract the type name from the given AST.Type node.
	 * @param node the node to extract from
	 * @return the name of the type
	 */
	public static String getTypeName(Type node) {
		if (node instanceof Referenced_Type) {
			Referenced_Type ref = (Referenced_Type) node;
			return ref.getReference().getDisplayName();
		} else {
			return TypeMapper.map(node.getTypename());
		}
	}

	/**
	 * Extract the value from given AST node, and construct a Value with given type.
	 * @param type the static type of the value
	 * @param node the node to extract from
	 * @return the extracted value
	 */
	public static Value extract(String type, IVisitableNode node) {
		if (node instanceof Boolean_Value) {
			Boolean_Value b = (Boolean_Value) node;
			return staticValue(type, Boolean.toString(b.getValue()).toUpperCase());
		}
		if (node instanceof Integer_Value) {
			Integer_Value i = (Integer_Value) node;
			return specificValue(type, i.toString());
		}
		if (node instanceof Real_Value) {
			Real_Value r = (Real_Value) node;
			return specificValue(type, r.toString());
		}
		if (node instanceof Charstring_Value) {
			Charstring_Value cs = (Charstring_Value) node;
			return specificValue(type, cs.getValue());
		}
		if (node instanceof Bitstring_Value) {
			Bitstring_Value bs = (Bitstring_Value) node;
			return specificValue(type, bs.getValue());
		}
		if (node instanceof Octetstring_Value) {
			Octetstring_Value os = (Octetstring_Value) node;
			return specificValue(type, os.getValue());
		}
		if (node instanceof Hexstring_Value) {
			Hexstring_Value hs = (Hexstring_Value) node;
			return specificValue(type, hs.getValue());
		}
		if (node instanceof Omit_Value) {
			return Value.OMIT(type);
		}
		// FIXME remove: "Enumerated_Value" is always wrapped into "Undefined_LowerIdentifier_Value"
		if (node instanceof Enumerated_Value) {
			Enumerated_Value e = (Enumerated_Value) node;
			return specificValue(type, e.getValue().getName());
		}
		if (node instanceof Referenced_Value) {
			String name = ((Referenced_Value) node).getReference().getDisplayName();
			if (isConstant(name)) {
				return new Value(type, (code, indent) -> code.append("Constants.", name, "()"));
			} else {
				// is a formal parameter
				return new Value(type, (code, indent) -> code.append(name));
			}
		}
		if (node instanceof Undefined_LowerIdentifier_Value) {
			String name = ((Undefined_LowerIdentifier_Value) node).getIdentifier().toString();
			if (isEnumerated(type)) {
				// TODO enumerated-refactor: use staticValue and a prefix (eg. "_")
				return specificValue(type, name);
			}
			return new Value(type, (code, indent) -> code.append(name));
		}
		return new Value(type);
	}

	private static Value staticValue(String type, String name) {
		return new Value(type, (code, indent) -> code.append(type, ".", name));
	}

	private static Value specificValue(String type, String value) {
		return new Value(type, (code, indent) -> code.append("new ", type, "(\"", value, "\")"));
	}
}
