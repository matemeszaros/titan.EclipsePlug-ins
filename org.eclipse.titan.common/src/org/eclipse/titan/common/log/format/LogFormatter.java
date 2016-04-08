/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.log.format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titan.common.utils.StandardCharsets;
import org.eclipse.titan.common.utils.StringUtils;

/**
 * This class is responsible for formatting log files
 *
 * @author Kristof Szabados
 */
public class LogFormatter {

	private static final int DEFAULT_INDENTATION_SIZE = 100;

	/** The number of bytes represented by a tick in the progress report */
	public static final int TICK_SIZE = 65536;
	private static final int IN_BUFFER_SIZE = 8192;
	private static final int OUT_BUFFER_SIZE = 65536;

	/** indent with 4 spaces */
	private static final int INDENTATION_SIZE = 4;

	private static final byte[] NEWLINE = StringUtils.lineSeparator().getBytes(StandardCharsets.UTF8);

	private enum LastTokenTypes {
		OPEN_BRACE, CLOSE_BRACE, COMMA, WHITE_SPACE, OTHER
	}

	private static byte[] indentation;
	static {
		resizeIndentation(DEFAULT_INDENTATION_SIZE);
	}

	private IProgressMonitor internalMonitor;
	private FileChannel inChannel;
	private FileChannel outChannel;

	private LastTokenTypes lastToken;
	private int indentationLevel;
	private boolean insideString;

	public LogFormatter(final IProgressMonitor internalMonitor, final FileChannel inChannel, final FileChannel outChannel) {
		this.internalMonitor = internalMonitor;
		this.inChannel = inChannel;
		this.outChannel = outChannel;
	}

	public void format() throws IOException {
		long nofProcessedBytes = 0;
		indentationLevel = 0;
		insideString = false;
		boolean cancelled = false;
		lastToken = LastTokenTypes.OTHER;
		final ByteBuffer inBuf = ByteBuffer.allocateDirect(IN_BUFFER_SIZE);
		final ByteBuffer outBuffer = ByteBuffer.allocate(OUT_BUFFER_SIZE);
		inBuf.clear();
		while (!cancelled && inChannel.read(inBuf) != -1) {
			if (internalMonitor.isCanceled()) {
				cancelled = true;
			}

			inBuf.flip();

			processBuffer(inBuf, outBuffer);

			nofProcessedBytes += inBuf.limit();
			inBuf.flip();
			outBuffer.flip();

			outChannel.write(outBuffer);
			outBuffer.clear();

			if (nofProcessedBytes > TICK_SIZE) {
				internalMonitor.worked((int) nofProcessedBytes / TICK_SIZE);
				nofProcessedBytes %= TICK_SIZE;
			}
		}
	}

	private void processBuffer(final ByteBuffer inBuf, final ByteBuffer outBuffer) throws IOException {
		byte temp;
		while (inBuf.hasRemaining()) {
			temp = inBuf.get();
			if (outBuffer.remaining() < indentationLevel * INDENTATION_SIZE + 1) {
				outBuffer.flip();
				outChannel.write(outBuffer);
				outBuffer.clear();
			}

			if (insideString) {
				processInsideString(inBuf, outBuffer, temp);
			} else {
				outsideString(inBuf, outBuffer, temp);
			}
		}
	}

	private void outsideString(final ByteBuffer inBuf, final ByteBuffer outBuffer, final byte temp) {
		switch (temp) {
		case '{':
			if (indentationLevel > 0) {
				switch (lastToken) {
				case OPEN_BRACE:
				case COMMA:
					outBuffer.put(NEWLINE);
					indent(outBuffer, indentationLevel);
					break;
				default:
					outBuffer.put((byte) ' ');
					break;
				}
			}
			outBuffer.put(temp);
			indentationLevel += 1;
			lastToken = LastTokenTypes.OPEN_BRACE;
			break;
		case '}':
			if (indentationLevel > 0) {
				indentationLevel -= 1;
				if (LastTokenTypes.OPEN_BRACE.equals(lastToken)) {
					outBuffer.put((byte) ' ');
				} else {
					outBuffer.put(NEWLINE);
					indent(outBuffer, indentationLevel);
				}
				lastToken = LastTokenTypes.CLOSE_BRACE;
			}
			outBuffer.put(temp);
			break;
		case ',':
			outBuffer.put(temp);
			if (indentationLevel > 0) {
				lastToken = LastTokenTypes.COMMA;
			}
			break;
		case '\"':
			outBuffer.put(temp);
			insideString = true;
			break;
		case ' ':
		case '\t':
			if (indentationLevel > 0) {
				if (LastTokenTypes.OTHER.equals(lastToken)) {
					lastToken = LastTokenTypes.WHITE_SPACE;
				}
			} else {
				outBuffer.put(temp);
			}
			break;
		case '\n':
			if (indentationLevel > 0) {
				if (LastTokenTypes.OTHER.equals(lastToken)) {
					lastToken = LastTokenTypes.WHITE_SPACE;
				}
			} else {
				outBuffer.put(NEWLINE);
			}
			break;
		case '\r':
			if (inBuf.remaining() > 0) {
				final byte temp2 = inBuf.get();
				if ('\n' == temp2) {
					if (indentationLevel > 0) {
						if (LastTokenTypes.OTHER.equals(lastToken)) {
							lastToken = LastTokenTypes.WHITE_SPACE;
						}
					} else {
						outBuffer.put(NEWLINE);
					}
				} else {
					outBuffer.put(NEWLINE);
				}
			} else {
				outBuffer.put(temp);
			}
			break;
		default:
			if (indentationLevel > 0) {
				switch (lastToken) {
				case OPEN_BRACE:
				case COMMA:
					outBuffer.put(NEWLINE);
					indent(outBuffer, indentationLevel);
					break;
				case CLOSE_BRACE:
				case WHITE_SPACE:
					outBuffer.put((byte) ' ');
					break;
				default:
					break;
				}
				lastToken = LastTokenTypes.OTHER;
			}
			outBuffer.put(temp);
			break;
		}
	}

	private void processInsideString(final ByteBuffer inBuf, final ByteBuffer outBuffer, final byte temp) {
		outBuffer.put(temp);
		switch (temp) {
		case '\"':
			insideString = false;
			break;
		case '\\':
			if (inBuf.hasRemaining()) {
				final byte temp2 = inBuf.get();
				outBuffer.put(temp2);
			}
			break;
		default:
			break;
		}
	}

	private void indent(final ByteBuffer outBuffer, final int amount) {
		final int temp = amount * INDENTATION_SIZE;
		if (temp > indentation.length) {
			resizeIndentation(temp);
		}

		outBuffer.put(indentation, 0, temp);
	}

	private static void resizeIndentation(final int newSize) {
		indentation = new byte[newSize];
		for (int i = 0; i < newSize; i++) {
			indentation[i] = (byte) ' ';
		}
	}

}
