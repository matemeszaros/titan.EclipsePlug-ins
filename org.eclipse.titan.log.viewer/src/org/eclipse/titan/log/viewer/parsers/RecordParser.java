/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.eclipse.titan.log.viewer.console.TITANDebugConsole;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.parsers.token.ComponentReference;
import org.eclipse.titan.log.viewer.parsers.token.EventType;
import org.eclipse.titan.log.viewer.parsers.token.Message;
import org.eclipse.titan.log.viewer.parsers.token.SourceInfo;
import org.eclipse.titan.log.viewer.parsers.token.TimeStamp;
import org.eclipse.titan.log.viewer.parsers.token.Token;
import org.eclipse.titan.log.viewer.parsers.token.WhiteSpace;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Parse a log record from the file
 *
 */
//FIXME this can be done so much better
public class RecordParser {

	private int bufPointer;
	private int startPointer;
	private List<Token> tokenBuffer;
	private byte[] buffer = null;
	private static final String UNDERSCORE = "_"; //$NON-NLS-1$

	/**
	 * Parses a log record given as a string
	 * 
	 * @param record the log record
	 * @throws ParseException if the log record can not be parsed
	 */
	public LogRecord parse(final byte[] record) throws ParseException {
		this.buffer = record;
		bufPointer = 0;
		return parse();
	}
	
	/**
	 * Parses a log record given as a string
	 * 
	 * @param record the log record
	 * @throws ParseException if the log record can not be parsed
	 */
	public LogRecord parse(final String record) throws ParseException {
		this.buffer = record.getBytes();
		bufPointer = 0;
		return parse();
	}

	/**
	 * Parses the log record and returns a logRecord with the tokens
	 * 
	 * @return LogRecord the log record
	 * @throws ParseException if the log record can not be parsed
	 */
	private LogRecord parse() throws ParseException {
		if ((this.buffer == null) || (this.buffer.length < 0)) {
			throw new ParseException(Messages.getString("RecordParser.0"), //$NON-NLS-1$
					this.bufPointer);
		}

		initialize();

		TimeStamp ts = readTimestamp();
		processToken(ts);

		return extractVector();
	} // parse

	/**
	 * Extracts the vector and sets the LogRecord
	 * @return Log record
	 */
	private LogRecord extractVector() {
		LogRecord thisLogRecord = new LogRecord();

		for (Token thisToken : this.tokenBuffer) {
			switch (thisToken.getType()) {
				case Constants.TIME_STAMP:
					thisLogRecord.setTimestamp(thisToken.getContent());
					break;
				case Constants.COMPONENT_REFERENCE:
					thisLogRecord.setComponentReference(thisToken.getContent().toLowerCase());
					break;
				case Constants.EVENT_TYPE:
					thisLogRecord.setEventType(thisToken.getContent());
					break;
				case Constants.SOURCE_INFORMATION:
					thisLogRecord.setSourceInformation(thisToken.getContent());
					break;
				case Constants.MESSAGE:
					thisLogRecord.setMessage(thisToken.getContent());
					break;
				default:
					break;
			}
		}

		return thisLogRecord;
	} //extractVector

	/**
	 * Initializes the tokenBuffer and startPointer 
	 *
	 */
	private void initialize() {
		this.startPointer = 0;
		this.tokenBuffer = new ArrayList<Token>();
	} //initialize

	/**
	 * The heart of the processing of tokens. 
	 * 
	 * RECURSIVE invoked
	 * 
	 * @param thisToken
	 * @throws ParseException
	 */
	private void processToken(final Token thisToken) throws ParseException {
		addToken(thisToken);
		Token token = readDelimiterToken(thisToken.getDelimiterList());
		addToken(token);

		if (token == Tokens.EOR) {
			return;
		}

		token = readNextToken(thisToken.getTokenList());
		processToken(token);
	} //processToken

	/**	
	 * Adds a token to the vector of Tokens
	 * @param thisToken
	 */
	private void addToken(final Token thisToken) {
		this.tokenBuffer.add(thisToken);
	} //addToken

	/**
	 * Reads a delimiter token 
	 * 
	 * @param expectedTokenMask
	 * @return Token
	 * @throws ParseException
	 */
	private Token readDelimiterToken(final int expectedTokenMask)	throws ParseException {
		if ((expectedTokenMask & Constants.END_OF_RECORD) == Constants.END_OF_RECORD
				&& this.bufPointer == this.buffer.length) {
			return Tokens.EOR;
		}

		if ((expectedTokenMask & Constants.WHITE_SPACE) == Constants.WHITE_SPACE) {
			return readWhitespace();
		}

		throw new ParseException(Messages.getString("RecordParser.1"), expectedTokenMask); //$NON-NLS-1$
	} // readDelimiterToken

	/**
	 * Read the next token 
	 * 
	 * @param expectedTokenMask
	 * @return Token
	 * @throws ParseException
	 */
	private Token readNextToken(final int expectedTokenMask) throws ParseException {
		String lookAheadToken = peek();

		if ((expectedTokenMask & Constants.COMPONENT_REFERENCE) == Constants.COMPONENT_REFERENCE) {

			if (isComponentRef(lookAheadToken)) {
				int storedStartPointer = this.startPointer;
				int storedBufPointer = this.bufPointer;

				String token = read();

				String nextWord = peekNextWord();
				//Check that the number is not last in logrecord.
				if (nextWord.length() < 1) {
					// unread -  
					// since it can not be determined if the string is a component reference or
					// if it is belongs to the message, therefor the string will be included
					// in the message
					this.startPointer = storedStartPointer;
					this.bufPointer = storedBufPointer;
				} else {
					return new ComponentReference(token);
				}
			}
			
		}

		if ((expectedTokenMask & Constants.EVENT_TYPE) == Constants.EVENT_TYPE
				&& isEventType(lookAheadToken)) {
			String token = read();
			return new EventType(token);
		}

		if ((expectedTokenMask & Constants.SOURCE_INFORMATION) == Constants.SOURCE_INFORMATION
				&& isSourceInfo(lookAheadToken)) {
			String token = read();
			return new SourceInfo(token);
		}

		if ((expectedTokenMask & Constants.MESSAGE) == Constants.MESSAGE) {
			String token = readUntilEOR();
			if ((token != null) && (token.length() != 0)) {
				return new Message(token);
			}
		}

		throw new ParseException(Messages.getString("RecordParser.2") + lookAheadToken, //$NON-NLS-1$
				expectedTokenMask);

	} //readNextToken

	/**
	 * Reads the buffer until EOR is found
	 * @return String
	 */
	private String readUntilEOR() {
		this.startPointer = this.bufPointer;

		while (this.bufPointer < this.buffer.length) {
			this.bufPointer++;
		}

		return new String(this.buffer, this.startPointer, this.bufPointer - this.startPointer);
	} //readUntilEOR

	/**
	 * Reads the next actual token
	 * @return String
	 */
	private String peek() {
		int localBufPointer = this.bufPointer;
		int localStartPointer = localBufPointer;

		// read next actual token
		while (localBufPointer < this.buffer.length) {
			char thisChar = (char) this.buffer[localBufPointer];
			if (isWhitespaceCharacter(thisChar)) {
				break;
			}

			localBufPointer++;
		}

		return new String(this.buffer, localStartPointer, localBufPointer
				- localStartPointer);
	} //peek

	/**
	 * Reads the buffer until character is whitespace
	 * @return String
	 */
	private String read() {
		this.startPointer = this.bufPointer;

		while (this.bufPointer < this.buffer.length) {
			char thisChar = (char) this.buffer[this.bufPointer];
			if (isWhitespaceCharacter(thisChar)) {
				break;
			}

			this.bufPointer++;
		}

		return new String(this.buffer, this.startPointer, this.bufPointer - this.startPointer);
	} //read

	/**
	 * Reads past the ws and reads the next word
	 * @return
	 */
	private String peekNextWord() {
		int localBufPointer = this.bufPointer;
		int localStartPointer = localBufPointer;

		// read past any WS
		while (localBufPointer < this.buffer.length) {
			char thisChar = (char) this.buffer[localBufPointer];
			if (!isWhitespaceCharacter(thisChar)) {
				break;
			}

			localBufPointer++;
		}

		localStartPointer = localBufPointer;

		while (localBufPointer < this.buffer.length) {
			char thisChar = (char) this.buffer[localBufPointer];
			if (isWhitespaceCharacter(thisChar)) {
				break;
			}

			localBufPointer++;
		}

		return new String(this.buffer, localStartPointer, localBufPointer
				- localStartPointer);
	} //peekNextWord

	/**
	 * Read the timestamp and moves the buffer pointer the number of characters 
	 * according to the time format
	 */
	private TimeStamp readTimestamp() throws ParseException {
		if (this.buffer.length == 0) {
			throw new ParseException(Messages.getString("RecordParser.3"), this.bufPointer); //$NON-NLS-1$
		}

		this.startPointer = this.bufPointer;

		readNumeric();

		switch (getCurrentChar()) {
		case ':':
			//moves the pointer from the first : to the end of the timestamp, used for timestamp in TIME format
			// HH:mm:ss.SSSSSS
			moveBufPointer(13);

			break;
		case '/':
			//moves the pointer from the first / to the end of the timestamp, used for timestamp in DATETIME format
			//yyyy/MMM/dd HH:mm:ss.SSSSSS
			moveBufPointer(23);
			break;
		case '.':
			//moves the pointer from the . to the end of the timestamp, used for timestamp in SECOND format
			//s.SSSSSS
			moveBufPointer(7);
			break;
		default:
			MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
			stream.println("actual buffer: " + new String(buffer));
			stream.println("actual buffer location: " + bufPointer);
			stream.println("actual byte: " + this.buffer[this.bufPointer] + ", as char: " + (char) this.buffer[this.bufPointer]);
			stream.println("the name of your charset: " + Charset.defaultCharset().name());
			throw new ParseException(Messages.getString("RecordParser.4"), this.bufPointer); //$NON-NLS-1$
		}

		return new TimeStamp(new String(this.buffer, this.startPointer, this.bufPointer - this.startPointer));
	} // readTimestamp

	/**
	 * Moves the buffer pointer the given number of characters. 
	 * Also checks if the buffer contains enough characters, 
	 * if not an exception is thrown
	 * @param counter
	 * @throws ParseException
	 */
	private void moveBufPointer(final int counter) throws ParseException {
		int tempCounter = counter;
		while ((tempCounter != 0) && (this.bufPointer < this.buffer.length)) {
			tempCounter--;
			this.bufPointer++;
		}

		if (tempCounter != 0) {
			throw new ParseException(Messages.getString("RecordParser.5"), this.bufPointer); //$NON-NLS-1$
		}
	} //moveBufPointer

	/**
	 * Reads the character the buffer pointer points at
	 * @return char
	 */
	private char getCurrentChar() {
		return (char) this.buffer[this.bufPointer];
	} //getCurrentChar

	/**
	 * Read the buffer as long as the character is numeric. 
	 * Moves the buffer pointer with the number of numeric characters
	 */
	private void readNumeric() {
		while (this.bufPointer < this.buffer.length) {
			char thisChar = (char) this.buffer[this.bufPointer];
			if (!isDigit(thisChar)) {
				break;
			}

			this.bufPointer++;
		}
	} //readNumeric

	/**
	 * Checks if the character is a digit
	 * @param aChar char
	 * @return boolean
	 */
	private boolean isDigit(final char aChar) {
		return (aChar >= '0') && (aChar <= '9');
	} //isDigit

	/**
	 * Checks if the character is one of the predefined whitespaces
	 * @param aChar char
	 * @return boolean
	 */
	private boolean isWhitespaceCharacter(final char aChar) {
		for (int i = 0; i < Constants.WS.length; i++) {
			if (aChar == Constants.WS[i]) {
				return true;
			}
		}
		return false;
	} //isWhitespaceCharacter

	/**
	 * Reads whitespace, can be one or several
	 * @return Token
	 * @throws ParseException
	 */
	private Token readWhitespace() throws ParseException {
		int isWhiteSpaceCounter = 0;
		this.startPointer = this.bufPointer;
		while (this.bufPointer < this.buffer.length) {
			char thisChar = (char) this.buffer[this.bufPointer];
			if (isWhitespaceCharacter(thisChar)) {
				isWhiteSpaceCounter++;
				this.bufPointer++;
			} else {
				break;
			}
		}
		if (isWhiteSpaceCounter != 0) {
			return new WhiteSpace(new String(this.buffer, this.startPointer, this.bufPointer
					- this.startPointer));
		}

		throw new ParseException(Messages.getString("RecordParser.6"), isWhiteSpaceCounter); //$NON-NLS-1$
	} //readWhitespace

	/**
	 * Checks if the token is a component reference
	 * @param token 
	 * @return boolean
	 */
	private boolean isComponentRef(final String token) {
		// if the token is a single char - the only valid i 0..9
		if (token.length() == 1) {
			char tkn = token.charAt(0);
			if ((tkn >= '0') && (tkn <= '9')) {
				return true;
			}
		}

		// check predefines strings
		for (int i = 0; i < Constants.COMPONENT_REFERENCES.length; i++) {
			if (token.equalsIgnoreCase(Constants.COMPONENT_REFERENCES[i])) {
				return true;
			}
		}

		// Checks digits. First must be 1..9 reset 0..9
		char char1 = token.charAt(0);
		if (!((char1 >= '1') && (char1 <= '9'))) {
			return false;
		}

		for (int i = 1; i < token.length(); i++) {
			char char2 = token.charAt(i);
			if (!((char2 >= '0') && (char2 <= '9'))) {
				return false;
			}
		}

		return true;
	} //isComponentRef

	/**
	 * Check if the Token is a sourceInfo
	 * @param token
	 * @return boolean
	 */
	private boolean isSourceInfo(final String token) {
		// a single dash is a valid source info
		if ((token.length() == 1) && (token.charAt(0) == '-')) {
			return true;
		}

		// if the token contains predefined string the token is a source info
		return token.contains(Constants.TTCN_FILE_EXT)
				|| token.contains(Constants.THREEMP_FILE_EXT);

	}

	/**
	 * Checks if the Token is one of the predefined Event types
	 * @param token the token
	 * @return boolean true is token is a predefined Event type, otherwise false
	 */
	private boolean isEventType(final String token) {
		SortedMap<String, String[]> eventCategories = org.eclipse.titan.log.viewer.utils.Constants.EVENT_CATEGORIES;
		if (token.contains(UNDERSCORE)) {
			// new format
			String cat = token.split(UNDERSCORE)[0];
			String subCat = token.split(UNDERSCORE)[1];
			if (eventCategories.keySet().contains(cat)) {
				String[] subCategories = eventCategories.get(cat);
				for (String subCategory : subCategories) {
					if (subCategory.equals(subCat)) {
						return true;
					}
				}
			}
		} else {
			// possibly old format
			if (eventCategories.keySet().contains(token)) {
				return true;
			}
		}
		return false;
	} //isEventType
}
