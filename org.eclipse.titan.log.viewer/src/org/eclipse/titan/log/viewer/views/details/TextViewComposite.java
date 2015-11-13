/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.details;

import static org.eclipse.titan.common.utils.StringUtils.isNullOrEmpty;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.preferences.PreferencesHandler;
import org.eclipse.titan.log.viewer.preferences.PreferencesHolder;

/**
 * Text view composite
 *
 */
public class TextViewComposite extends Composite {

	private StyledText styledText;
	private String currentText;
	private String currentName;
	private boolean useFormatting;
	private Font font;
	private LogFileMetaData logFileMetaData;

	/**
	 * Constructor
	 * @param parent the parent to this composite
	 */
	public TextViewComposite(final Composite parent) {
		super(parent, SWT.NO_BACKGROUND);
		styledText = new StyledText(this, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		font = Display.getCurrent().getSystemFont();
		//font = new Font(Display.getCurrent(), "Courier", 8, SWT.NORMAL); //$NON-NLS-1$
		styledText.setFont(font);
		useFormatting = true;
	}
	
	@Override
	public void dispose() {
		if (styledText != null && !styledText.isDisposed()) {
			styledText.dispose();
		}
		if (font != null && !font.isDisposed()) {
			font.dispose();
		}
		super.dispose();
	}
	
	/**
	 * Indicates if formatting of the text should be used
	 * @param useFormatting the flag indicating if formatting should be used
	 */
	public void setUseFormatting(final boolean useFormatting) {
		this.useFormatting = useFormatting;
	}
	
	/**
	 * Indents and sets a new text input
	 * @param newInput the new text input
	 */
	public void inputChanged(final DetailData newInput) {
		if (newInput == null) {
			clearText();
			return;
		}
		// If message is null or empty
		String newText = newInput.getLine();
		String newName = newInput.getName();
		
		if (isNullOrEmpty(newText)) {
			currentText = null;
			styledText.setText(""); //$NON-NLS-1$
			return;
		}
		
		if (isNullOrEmpty(newName)) {
			currentName = null;
		}
		
		// If message has not changed
		if ((currentText != null && currentText.contentEquals(newText))
				&& currentName != null && currentName.equals(newName)) {
			return;
		}
		newText = removeEndingColon(newText);


		currentText = newText;
		currentName = newName;
		int newLineIndex = currentText.indexOf("\n"); //$NON-NLS-1$
		// index should be greater or equal than zero (if not found indexOf returns -1
		// index should not be length -1 (last char), which is often \n
		boolean hasNewLine = (newLineIndex >= 0) && (newLineIndex != (currentText.length() - 1));
		String text = formatText(newInput, hasNewLine);


		//So that the sourceInfo is included in the scrollbar
		String messageText = newInput.getSourceInfo().trim() + "\n" + (text);
		styledText.setText(messageText);
		colorKeywords(text, messageText);
	}

	private void colorKeywords(String text, String messageText) {
		PreferencesHolder preferences = PreferencesHandler.getInstance().getPreferences(this.logFileMetaData.getProjectName());
		boolean useKeywordColoring = preferences.getUseColoringKeywords();

		if (useKeywordColoring) {
			Map<String, RGB> coloringKeywords = preferences.getColoringKeywords();
			for (Map.Entry<String, RGB> entry : coloringKeywords.entrySet()) {
				String currentKeyword = entry.getKey();
				int messagePosition = messageText.indexOf(text);
				int startPosition = messageText.indexOf(currentKeyword, messagePosition);
				while (startPosition > -1) {
					Color textColor = (Color) Activator.getDefault().getCachedResource(entry.getValue());
					StyleRange styleRange = new StyleRange();
					styleRange.start = startPosition;
					styleRange.length = currentKeyword.length();
					styleRange.foreground = textColor;
					styleRange.strikeout = false;
					styleRange.underline = false;
					styledText.setStyleRange(styleRange);

					startPosition = messageText.indexOf(currentKeyword, startPosition + currentKeyword.length());
				}
			}
		}
	}

	private String formatText(DetailData newInput, boolean hasNewLine) {
		// Formatting is set to be used and the text does not contain new line char
		if (!useFormatting || hasNewLine) {
			// formatting not used
			return currentText;
		}

		StringBuilder text = new StringBuilder();
		String port = newInput.getPort();
		if (port != null && port.trim().length() > 0) {
			text.append(newInput.getName());
			text.append("(");
			text.append(newInput.getPort());
			text.append(") "); //$NON-NLS-1$
		} else {
			text.setLength(0);
			text.append(newInput.getName());
			text.append(" "); //$NON-NLS-1$
		}

		final char startBracket = '{';
		// If first char is not a start bracket '{' -> do not use formatting
		if (!currentText.contains("{")) { //$NON-NLS-1$
			text.append(currentText);
		} else {
			final char endBracket = '}';
			final char comma = ',';
			final String newLine = "\n"; //$NON-NLS-1$
			final String spaces = "  "; //$NON-NLS-1$
			String indent = ""; //$NON-NLS-1$
			StringCharacterIterator ci = new StringCharacterIterator(currentText);
			for (char currentChar = ci.first(); currentChar != CharacterIterator.DONE; currentChar = ci.next()) {
				switch (currentChar) {
				case startBracket:
					indent += spaces;
					text.append(startBracket);
					text.append(newLine);
					text.append(indent.substring(0, indent.length() - 1));
					break;
				case endBracket:
					if ((indent.length() - spaces.length()) > 0) {
						indent = indent.substring(0, indent.length() - spaces.length());
					}
					text.append(newLine);
					text.append(indent);
					text.append(endBracket);
					break;
				case comma:
					if (indent.length() > 1) {
						text.append(currentChar);
						text.append(newLine);
						text.append(indent.substring(0, indent.length() - 1)); // Compensate for space after ','
					} else {
						text.append(currentChar);
						text.append(newLine);
					}
					break;
				default:
					text.append(currentChar);
				}
			}
		}
		return text.toString();
	}

	private String removeEndingColon(String newText) {
		// If message starts with ": "
		if ((newText.length() >= 2) && newText.startsWith(": ")) { //$NON-NLS-1$
			return newText.substring(2);
		}
		return newText;
	}

	private void clearText() {
		currentText = null;
		currentName = null;
		styledText.setText("");
	}


	public LogFileMetaData getLogFileMetaData() {
		return logFileMetaData;
	}

	public void setLogFileMetaData(final LogFileMetaData logFileMetaData) {
		this.logFileMetaData = logFileMetaData;
	}
	
}

