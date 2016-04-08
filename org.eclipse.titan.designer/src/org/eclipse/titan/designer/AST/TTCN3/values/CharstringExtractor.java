/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

/**
 * Extracts TTCN-3 charstring
 * @author Arpad Lovassy
 */
public class CharstringExtractor {

	// Error messages
	private static final String INVALID_ESCAPE_SEQUENCE = "Invalid escape sequence: ";
	
	/** true, if TTCN-3 string contains error */
	private boolean mErrorneous = false;
	
	/** the value string of the TTCN-3 string */
	private String mExtractedString;
	
	/** the error message (if any) */
	private String mErrorMessage;
	
	/**
	 * Constructor
	 * @param aTccnCharstring the TTCN-3 string with escapes to extract
	 */
	public CharstringExtractor( final String aTccnCharstring ) {
		mExtractedString = extractString( aTccnCharstring );
	}
	
	/** @return the value string of the TTCN-3 string */
	public String getExtractedString() {
		return mExtractedString;
	}
	
	/**
	 * @return if TTCN-3 string contains error
	 */
	public boolean isErrorneous() {
		return mErrorneous;
	}

	/**
	 * @return the error message (if any)
	 */
	public String getErrorMessage() {
		return mErrorMessage;
	}
	
	/**
	 * Converts string with special characters to normal displayable string
	 * Special characters:
	 *   "" -> "
	 *   \['"?\abfnrtv\u000a]
	 *   \x[0-9a-fA-F][0-9a-fA-F]?
	 *   \[0-3]?[0-7][0-7]?
	 * @param aTccnCharstring TTCN-3 charstring representation, it can contain escape characters, NOT NULL
	 * @return extracted string value
	 */
	private String extractString(final String aTccnCharstring) {
		final int slength = aTccnCharstring.length();
		int pointer = 0;
		StringBuilder sb = new StringBuilder();
		while (pointer < slength) {
			// Special characters:
			// Special characters by the TTCNv3 standard:
			// The 2 double-quotes: "" -> it is one double-quote
			if (pointer + 1 < slength && aTccnCharstring.substring(pointer, pointer + 2).equals("\"\"")) {
				sb.append('"');
				pointer += 2;
			}

			// TITAN specific special characters:
			// backslash-escaped character sequences:
			else if (pointer + 1 < slength) {
				char c1 = aTccnCharstring.charAt(pointer);
				if (c1 == '\\') {
					pointer++;
					char c2 = aTccnCharstring.charAt(pointer);
					// backslash-escaped singlequote,
					// doublequote, question mark or
					// backslash:
					if (c2 == '\'' || c2 == '"' || c2 == '?' || c2 == '\\') {
						sb.append(aTccnCharstring.charAt(pointer));
						pointer++;
					} else if (c2 == 'a') { // Audible bell
						sb.append((char) 0x07);
						pointer++;
					} else if (c2 == 'b') { // Backspace
						sb.append((char) 0x08);
						pointer++;
					} else if (c2 == 'f') { // Form feed
						sb.append((char) 0x0c);
						pointer++;
					} else if (c2 == 'n') { // New line
						sb.append((char) 0x0a);
						pointer++;
					} else if (c2 == 'r') { // Carriage return
						sb.append((char) 0x0d);
						pointer++;
					} else if (c2 == 't') { // Horizontal tab
						sb.append((char) 0x09);
						pointer++;
					} else if (c2 == 'v') { // Vertical tab
						sb.append((char) 0x0b);
						pointer++;
					} else if (c2 == 10) { // New line escaped
						sb.append((char) 0x0a);
						pointer++;
					} else if (c2 == 'x') { // hex-notation: \xHH?
						pointer++;
						if (pointer >= slength) {
							// end of string reached
							mErrorMessage = INVALID_ESCAPE_SEQUENCE + "'\\x'";
							mErrorneous = true;
							return null;
						}
						final int hexStart = pointer;
						if (!isHexDigit(aTccnCharstring.charAt(pointer))) {
							// invalid char after \x
							mErrorMessage = INVALID_ESCAPE_SEQUENCE + "'\\x" + aTccnCharstring.charAt(hexStart) + "'";
							mErrorneous = true;
							return null;
						}
						pointer++;
						if (pointer < slength && isHexDigit(aTccnCharstring.charAt(pointer))) {
							// 2nd hex digit is optional
							pointer++;
						}
						sb.append((char) Integer.parseInt(aTccnCharstring.substring(hexStart, pointer), 16));
					} else if (isOctDigit(c2)) { // [0..7] // octal notation: \[0-3]?[0-7][0-7]?
						final int octStart = pointer;
						pointer++;
						while (pointer < slength && pointer - octStart < 3 && isOctDigit(aTccnCharstring.charAt(pointer))) {
							pointer++;
						}
						final int octInt = Integer.parseInt(aTccnCharstring.substring(octStart, pointer), 8);
						if (octInt > 255) { // oct 377
							mErrorMessage = INVALID_ESCAPE_SEQUENCE + "'\\"
									+ aTccnCharstring.substring(octStart, pointer) + "'";
							mErrorneous = true;
							return null;
						} else {
							sb.append((char) octInt);
						}
					} else {
						mErrorMessage = INVALID_ESCAPE_SEQUENCE + "'\\" + String.valueOf(c2) + "'";
						mErrorneous = true;
						return null;
					}
				} else { // End of backslash-escape
					sb.append(aTccnCharstring.charAt(pointer));
					pointer++;
				}
			} else {
				sb.append(aTccnCharstring.charAt(pointer));
				pointer++;
			}
		} // End of While

		return sb.toString();
	}

	/**
	 * @param aChar character to check
	 * @return true if aChar is hexadecimal digit
	 */
	private static boolean isHexDigit( final char aChar ) {
		return (aChar >= '0' && aChar <= '9') || (aChar >= 'a' && aChar <= 'f') || (aChar >= 'A' && aChar <= 'F');
	}
	
	/**
	 * @param aChar character to check
	 * @return true if aChar is octal digit
	 */
	private static boolean isOctDigit( final char aChar ) {
		return aChar >= '0' && aChar <= '7';
	}
}
