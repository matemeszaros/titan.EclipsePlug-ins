/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import org.eclipse.titan.designer.editors.ProposalCollector;

/**
 * The TTCN3Keywords class holds the keywords of the TTCN3 language separated
 * according to where and how they can be used.
 * 
 * @author Kristof Szabados
 * */
public final class TTCN3Keywords {
	public static final String KEYWORD = "keyword";
	public static final String MACRO = "macro";

	/**
	 * Stores all of the keywords. This is useful if something goes wrong
	 * and their is no better way to provide the list of available keywords.
	 **/
	public static final String[] ALL_AVAILABLE = new String[] { "activate", "address", "alive", "all", "alt", "altstep", "and", "and4b", "any",
			"break", "case", "component", "const", "continue", "control", "deactivate", "display", "do", "else", "encode", "enumerated",
			"except", "exception", "execute", "extends", "extension", "external", "for", "from", "function", "goto", "group", "if",
			"import", "in", "infinity", "inout", "interleave", "label", "language", "length", "log", "log2str", "match", "message",
			"mixed", "mod", "modifies", "module", "modulepar", "mtc", "noblock", "not", "not4b", "nowait", "of", "on", "optional", "or",
			"or4b", "out", "override", "param", "pattern", "port", "procedure", "record", "recursive", "rem", "repeat", "return", "runs",
			"select", "self", "sender", "set", "signature", "system", "template", "testcase", "to", "type", "union", "value", "valueof",
			"var", "variant", "while", "with", "xor", "xor4b", "complement", "ifpresent", "subset", "superset", "permutation", "anytype",
			"bitstring", "boolean", "char", "charstring", "default", "float", "hexstring", "integer", "objid", "octetstring",
			"universal", "verdicttype", "timer", "start", "stop", "timeout", "read", "running", "call", "catch", "check", "clear",
			"getcall", "getreply", "halt", "raise", "receive", "reply", "send", "trigger", "create", "connect", "disconnect", "done",
			"kill", "killed", "map", "unmap", "getverdict", "setverdict", "action", "apply", "derefers", "refers", "bit2hex", "bit2int",
			"bit2oct", "bit2str", "char2int", "char2oct", "decomp", "float2int", "float2str", "hex2bit", "hex2int", "hex2oct", "hex2str",
			"int2bit", "int2char", "int2float", "int2hex", "int2oct", "int2str", "int2unichar", "isbound", "ischosen", "ispresent",
			"isvalue", "lengthof", "oct2bit", "oct2char", "oct2hex", "oct2int", "oct2str", "regexp", "replace", "rnd", "sizeof",
			"str2bit", "str2float", "str2hex", "str2int", "str2oct", "substr", "unichar2int", "encvalue", "decvalue", "true", "false",
			"none", "pass", "inconc", "fail", "error", "null", "NULL", "omit", "friend", "public", "private",
			"get_stringencoding", "oct2unichar", "remove_bom", "unichar2oct", "encode_base64", "decode_base64" };

	public static final String[] FORMAL_PARAMETER_SCOPE = new String[] { "in", "inout", "out", "template" };

	public static final String[] MODULE_SCOPE = new String[] { "all", "altstep", "component", "const", "control", "display", "encode",
			"enumerated", "except", "exception", "execute", "extends", "extension", "external", "from", "function", "group", "import",
			"in", "inout", "language", "message", "mixed", "modifies", "modulepar", "noblock", "of", "on", "optional", "out", "override",
			"port", "procedure", "record", "recursive", "return", "runs", "set", "signature", "system", "template", "testcase", "type",
			"union", "variant", "with", "friend", "public", "private" };

	public static final String[] STATEMENT_SCOPE = new String[] { "activate", "all", "alt", "any", "break", "case", "complement", "const",
			"continue", "deactivate", "do", "else", "for", "from", "goto", "if", "interleave", "label", "log", "match", "modifies",
			"mtc", "nowait", "param", "port", "record", "repeat", "return", "select", "self", "sender", "system", "template", "to",
			"value", "var", "while", "connect", "disconnect", "map", "unmap", "getverdict", "setverdict", "action", "@try", "@catch" };

	public static final String[] COMPONENT_SCOPE = new String[] { "const", "template", "var", "port", };

	public static final String[] GENERALLY_USABLE = new String[] { "address", "and", "and4b", "infinity", "ifpresent", "length", "lengthof",
			"mod", "not", "not4b", "or", "or4b", "xor", "xor4b", "pattern", "rem", "valueof", "subset", "superset", "permutation",
			"anytype", "bitstring", "boolean", "char", "charstring", "default", "float", "hexstring", "integer", "objid", "octetstring",
			"universal", "verdicttype", "timer", "apply", "derefers", "refers", "ischosen", "ispresent", "regexp", "replace", "rnd",
			"sizeof", "substr", "true", "false", "none", "pass", "inconc", "fail", "error", "null", "NULL", "omit", "bit2hex", "bit2int",
			"bit2oct", "bit2str", "char2int", "char2oct", "float2int", "float2str", "hex2bit", "hex2int", "hex2oct", "hex2str",
			"int2bit", "int2char", "int2float", "int2hex", "int2oct", "int2str", "int2unichar", "log2str", "oct2bit", "oct2char",
			"oct2hex", "oct2int", "oct2str", "str2bit", "str2float", "str2hex", "str2int", "str2oct", "unichar2int", "encvalue",
			"decvalue", "get_stringencoding", "oct2unichar", "remove_bom", "unichar2oct", "encode_base64", "decode_base64" };

	public static final String[] MACROS = new String[] { "%moduleId", "%definitionId", "%testcaseId", "%fileName", "%lineNumber", "__MODULE__",
			"__FILE__", "__BFILE__", "__LINE__", "__SCOPE__", "__TESTCASE__" };

	/** private constructor to disable instantiation. */
	private TTCN3Keywords() {
	}

	/**
	 * Adds every available keyword to the collector. This is useful if
	 * something goes wrong and their is no better way to provide the list
	 * of available keywords.
	 *
	 * @param propCollector
	 *                the proposal collector.
	 * */
	public static void addKeywordProposals(final ProposalCollector propCollector) {
		propCollector.addProposal(ALL_AVAILABLE, null, KEYWORD);
		propCollector.addProposal(MACROS, null, MACRO);
	}
}
