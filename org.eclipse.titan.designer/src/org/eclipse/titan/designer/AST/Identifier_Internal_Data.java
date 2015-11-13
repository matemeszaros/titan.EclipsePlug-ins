/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * @author Kristof Szabados
 * */
public final class Identifier_Internal_Data {
	public static final String INVALID_STRING = "<invalid>";

	private String name;
	private String asnName;
	private String ttcnName;

	//ASN kind of identifier
	private int asnKind;

	/** undefined. */
	private static final int ASN_UNDEF = 0;
	/** lower identifier. */
	private static final int ASN_LOWER = 1;
	/** upper identifier. */
	private static final int ASN_UPPER = 2;
	/** all upper identifier. */
	private static final int ASN_ALLUPPER = 3;
	/** word. */
	private static final int ASN_WORD = 4;
	/** ampersand + upper identifier. */
	private static final int ASN_AMPUPPER = 5;
	/** ampersand + lower identifier.  */
	private static final int ASN_AMPLOWER = 6;

	public Identifier_Internal_Data(final String name, final String asnName, final String ttcnName) {
		this.name = name;

		if (name.equals(asnName)) {
			this.asnName = name;
		} else {
			this.asnName = asnName;
		}

		if (name.equals(ttcnName)) {
			this.ttcnName = name;
		} else {
			this.ttcnName = ttcnName;
		}

		asnKind = ASN_UNDEF;
	}

	public static String asnToName(final String from) {
		StringBuilder builder = new StringBuilder(from);
		/* "@aaa" -> "_root_aaa" */
		if (builder.length() > 0 && '@' == builder.charAt(0)) {
			builder.replace(0, 1, "_root_");
		}
		/* "aa.<xxxx>.bb" -> "aa.bb" */
		int index = 0;
		int index2 = 0;
		while (index2 != -1  && index != -1) {
			index = builder.indexOf(".<", index);
			if (index != -1) {
				index2 = builder.indexOf(">.", index);
				if (index2 != -1) {
					builder.replace(index, index2 + 1, "");
				}
			}
		}
		/* "-" -> "__" */
		index = builder.indexOf("-", 0);
		while (index != -1) {
			builder.replace(index, index + 1, "__");
			index += 2;
			index = builder.indexOf("-", index);
		}
		/* "." -> "_" */
		index = builder.indexOf(".", 0);
		while (index != -1) {
			builder.replace(index, index + 1, "_");
			index++;
			index = builder.indexOf(".", index);
		}
		/* "&" -> "" */
		index = builder.indexOf("&", 0);
		while (index != -1) {
			builder.replace(index, index + 1, "");
			index = builder.indexOf("&", index);
		}

		String result = builder.toString();
		if (result.equals(from)) {
			return from;
		}

		return builder.toString();
	}

	public static String nameToAsn(final String from) {
		StringBuilder builder = new StringBuilder(from);
		int index = 0;
		/* remove leading '_'s */
		while (index < builder.length() && '_' == builder.charAt(index)) {
			index++;
		}
		if (index > 0) {
			builder.delete(0, index);
		}
		/* remove trailing '_'s */
		index = builder.length();
		while (index > 0 && '_' == builder.charAt(index - 1)) {
			index--;
		}
		if (index != builder.length()) {
			builder.delete(index, builder.length());
		}
		/* "__" -> "-" */
		index = builder.indexOf("__", 0);
		while (index != -1) {
			builder.replace(index, index + 2, "-");
			index++;
			index = builder.indexOf("__", index);
		}
		/* "_" -> "-" */
		index = builder.indexOf("_", 0);
		while (index != -1) {
			builder.replace(index, index + 1, "-");
			index++;
			index = builder.indexOf("_", index);
		}

		String result = builder.toString();
		if (result.equals(from)) {
			return from;
		}

		return builder.toString();
	}

	public static String ttcnToName(final String from) {
		StringBuilder builder = new StringBuilder(from);
		/* "_" -> "__" */
		int index = builder.indexOf("_", 0);
		while (index != -1) {
			builder.replace(index, index + 1, "__");
			index += 2;
			index = builder.indexOf("_", index);
		}

		String result = builder.toString();
		if (result.equals(from)) {
			return from;
		}

		return builder.toString();
	}

	public static String nameToTtcn(final String from) {
		StringBuilder builder = new StringBuilder(from);
		int index = 0;
		/* remove leading '_'s */
		while (index < builder.length() && '_' == builder.charAt(index)) {
			index++;
		}
		if (index > 0) {
			builder.delete(0, index);
		}
		/* remove trailing '_'s */
		index = builder.length();
		while (index > 0 && '_' == builder.charAt(index - 1)) {
			index--;
		}
		if (index != builder.length()) {
			builder.delete(index, builder.length());
		}
		/* "__" -> "_" */
		index = builder.indexOf("__", 0);
		while (index != -1) {
			builder.deleteCharAt(index);
			index = builder.indexOf("__", index);
		}

		String result = builder.toString();
		if (result.equals(from)) {
			return from;
		}

		return builder.toString();
	}

	public String getName() {
		return name;
	}

	public String getAsnName() {
		if (asnName == null) {
			String temp = nameToAsn(name);
			if (name.equals(temp)) {
				asnName = name;
			} else {
				asnName = temp;
			}
		}
		return asnName;
	}

	public String getTtcnName() {
		if (ttcnName == null) {
			String temp = nameToTtcn(name);
			if (name.equals(temp)) {
				ttcnName = name;
			} else {
				ttcnName = temp;
			}
		}
		return ttcnName;
	}

	private void decideAsnKind() {
		if (asnKind != ASN_UNDEF) {
			return;
		}

		if (INVALID_STRING.equals(asnName)) {
			return;
		}

		if (asnName.charAt(0) == '&') {
			if (asnName.length() > 2) {
				if (Character.isUpperCase(asnName.charAt(1))) {
					asnKind = ASN_AMPUPPER;
				} else if (Character.isLowerCase(asnName.charAt(1))) {
					asnKind = ASN_AMPLOWER;
				}
			}
		} else if (Character.isLowerCase(asnName.charAt(0))) {
			asnKind = ASN_LOWER;
		} else if (Character.isUpperCase(asnName.charAt(0))) {
			asnKind = ASN_UPPER;
			boolean hasLower = false;
			boolean hasDigit = false;
			for (int i = 0; i < asnName.length(); i++) {
				if (Character.isLowerCase(asnName.charAt(i))) {
					hasLower = true;
				} else if (Character.isDigit(asnName.charAt(i))) {
					hasDigit = true;
				}
			}

			if (!hasLower) {
				asnKind = ASN_ALLUPPER;
				if (!hasDigit) {
					asnKind = ASN_WORD;
				}
			}
		}
	}

	public boolean isvalidAsnTyperef() {
		decideAsnKind();

		return (asnKind == ASN_UPPER) || (asnKind == ASN_ALLUPPER) || (asnKind == ASN_WORD);
	}

	public boolean isvalidAsnValueReference() {
		decideAsnKind();

		return asnKind == ASN_LOWER;
	}

	public boolean isvalidAsnObjectClassReference() {
		decideAsnKind();

		return (asnKind == ASN_ALLUPPER) || (asnKind == ASN_WORD);
	}

	public boolean isvalidAsnObjectReference() {
		decideAsnKind();

		return asnKind == ASN_LOWER;
	}

	public boolean isvalidAsnValueFieldReference() {
		decideAsnKind();

		return asnKind == ASN_AMPLOWER;
	}

	public boolean isvalidAsnObjectFieldReference() {
		decideAsnKind();

		return asnKind == ASN_AMPLOWER;
	}

	public boolean isvalidAsnObjectSetFieldReference() {
		decideAsnKind();

		return asnKind == ASN_AMPUPPER;
	}

	public boolean isvalidAsnWord() {
		decideAsnKind();

		return asnKind == ASN_WORD;
	}
}
