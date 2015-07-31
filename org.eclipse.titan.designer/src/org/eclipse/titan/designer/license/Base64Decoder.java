/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.license;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * @author Peter Dimitrov
 * */
public final class Base64Decoder {

	private static final byte[] ENCODING_TABLE = { (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G',
			(byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q',
			(byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a',
			(byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k',
			(byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u',
			(byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
			(byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+', (byte) '/' };

	private static byte padding = (byte) '=';

	private static final byte[] DECODING_TABLE = new byte[128];

	static {
		for (int i = 0; i < ENCODING_TABLE.length; i++) {
			DECODING_TABLE[ENCODING_TABLE[i]] = (byte) i;
		}
	}

	/** private constructor to disable instantiation */
	private Base64Decoder() {
	}

	public static byte[] decode(final byte[] data) {
		int len = data.length / 4 * 3;
		ByteArrayOutputStream bOut = new ByteArrayOutputStream(len);

		try {
			decode(data, 0, data.length, bOut);
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("Error while decoding", e);
		}

		return bOut.toByteArray();
	}

	private static boolean ignore(final char c) {
		return c == '\n' || c == '\r' || c == '\t' || c == ' ';
	}

	public static int decode(final byte[] data, final int off, final int length, final OutputStream out) throws IOException {
		int outLen = 0;
		int end = off + length;

		while (end > off) {
			if (!ignore((char) data[end - 1])) {
				break;
			}

			end--;
		}

		int i = off;
		int finish = end - 4;

		i = nextI(data, i, finish);
		byte b1, b2, b3, b4;

		while (i < finish) {
			b1 = DECODING_TABLE[data[i++]];

			i = nextI(data, i, finish);

			b2 = DECODING_TABLE[data[i++]];

			i = nextI(data, i, finish);

			b3 = DECODING_TABLE[data[i++]];

			i = nextI(data, i, finish);

			b4 = DECODING_TABLE[data[i++]];

			out.write((b1 << 2) | (b2 >> 4));
			out.write((b2 << 4) | (b3 >> 2));
			out.write((b3 << 6) | b4);

			outLen += 3;

			i = nextI(data, i, finish);
		}

		outLen += decodeLastBlock(out, (char) data[end - 4], (char) data[end - 3], (char) data[end - 2], (char) data[end - 1]);

		return outLen;
	}

	private static int nextI(final byte[] data, final int i, final int finish) {
		int i2 = i;
		while ((i2 < finish) && ignore((char) data[i2])) {
			i2++;
		}
		return i2;
	}

	private static int decodeLastBlock(final OutputStream out, final char c1, final char c2, final char c3, final char c4) throws IOException {
		byte b1, b2, b3, b4;

		if (c3 == padding) {
			b1 = DECODING_TABLE[c1];
			b2 = DECODING_TABLE[c2];

			out.write((b1 << 2) | (b2 >> 4));

			return 1;
		} else if (c4 == padding) {
			b1 = DECODING_TABLE[c1];
			b2 = DECODING_TABLE[c2];
			b3 = DECODING_TABLE[c3];

			out.write((b1 << 2) | (b2 >> 4));
			out.write((b2 << 4) | (b3 >> 2));

			return 2;
		} else {
			b1 = DECODING_TABLE[c1];
			b2 = DECODING_TABLE[c2];
			b3 = DECODING_TABLE[c3];
			b4 = DECODING_TABLE[c4];

			out.write((b1 << 2) | (b2 >> 4));
			out.write((b2 << 4) | (b3 >> 2));
			out.write((b3 << 6) | b4);

			return 3;
		}
	}
}
