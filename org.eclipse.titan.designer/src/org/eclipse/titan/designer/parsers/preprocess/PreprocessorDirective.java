/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.preprocess;

public class PreprocessorDirective {
	public enum Directive_type {
		IFDEF("#ifdef"),
		IFNDEF("#ifndef"),
		IF("#if"),
		ELIF("#elif"),
		ELSE("#else"),
		ENDIF("#endif"),
		DEFINE("#define"),
		UNDEF("#undef"),
		ERROR("#error"),
		WARNING("#warning"),
		INCLUDE("#include"),
		LINEMARKER("linemarker"),
		LINECONTROL("#line"),
		PRAGMA("#pragma"),
		NULL("null");
		private final String name;
		Directive_type(final String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	}
	public Directive_type type;
	public boolean condition;
	public String str;
	public int line;

	// used for #else, #endif, null, linemarker, line, #pragma
	public PreprocessorDirective(final Directive_type type) {
		this.type = type;
		this.condition = true;
	}

	// used for conditional
	public PreprocessorDirective(final Directive_type type, final boolean condition) {
		this.type = type;
		this.condition = condition;
	}

	// used for #include, #error, #warning
	public PreprocessorDirective(final Directive_type type, final String str) {
		this.type = type;
		this.str = str;
	}
	public boolean isConditional() {
		switch (type) {
		case IFDEF:
		case IFNDEF:
		case IF:
		case ELIF:
		case ELSE:
		case ENDIF:
			return true;
		default:
			return false;
		}
	}
}
