/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * This class manages the Color resources, so that we can reach them by provided
 * attributes.
 * 
 * @author Kristof Szabados
 */
public final class ColorManager {
	private static final Map<String, Color> COLOR_TABLE = new HashMap<String, Color>(10);
	private static final Map<String, Token> TOKEN_TABLE = new HashMap<String, Token>();

	/**
	 * Gets foreground color associated with a color preference name.
	 * 
	 * @param aName
	 *                The name of the preference.
	 * @return The background color associated with a color preference name,
	 *         or if one can not be found, the default foreground color of
	 *         the system.
	 */
	public Color getForegroundColor(final String aName) {
		Color color = COLOR_TABLE.get(aName);
		if (color == null) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			RGB rgb = PreferenceConverter.getColor(store, aName);
			if (rgb == null) {
				color = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
			} else {
				color = new Color(Display.getCurrent(), rgb);
			}
			COLOR_TABLE.put(aName, color);
		}
		return color;
	}

	/**
	 * Gets background color associated with a color preference name.
	 * 
	 * @param aName
	 *                The name of the preference.
	 * @return The background color associated with a color preference name,
	 *         or if one can not be found, then white.
	 */
	public Color getBackgroundColor(final String aName) {
		Color color = COLOR_TABLE.get(aName);
		if (color == null) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			RGB rgb = PreferenceConverter.getColor(store, aName);
			if (rgb == null) {
				color = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
			} else {
				color = new Color(Display.getCurrent(), rgb);
			}
			COLOR_TABLE.put(aName, color);
		}
		return color;
	}

	/**
	 * Creates a TextAttribute out of a 'preference group' 's name.
	 * <p>
	 * Actually 3 preference names are use which are postfixed to get the
	 * actual ones needed.
	 * 
	 * @see #getBackgroundColor(String)
	 * @see #getForegroundColor(String)
	 * @see #createTokenFromPreference(String)
	 * @see PreferenceConstants
	 * 
	 * @param key
	 *                The 'preference group' 's name.
	 * @return The TextAttribute created.
	 */
	public TextAttribute createAttributeFromPreference(final String key) {
		Color foregroundColor = getForegroundColor(key + PreferenceConstants.FOREGROUND);
		Color backgroundColor;
		if (Activator.getDefault().getPreferenceStore().getBoolean(key + PreferenceConstants.USEBACKGROUNDCOLOR)) {
			backgroundColor = getBackgroundColor(key + PreferenceConstants.BACKGROUND);
		} else {
			backgroundColor = null;
		}
		boolean isBold = Activator.getDefault().getPreferenceStore().getBoolean(key + PreferenceConstants.BOLD);
		return new TextAttribute(foregroundColor, backgroundColor, isBold ? SWT.BOLD : SWT.NORMAL);
	}

	/**
	 * Creates a Token out of a 'preference group' 's name.
	 * 
	 * @see #createAttributeFromPreference(String)
	 * 
	 * @param key
	 *                The 'preference group' 's name.
	 * @return The Token created.
	 */
	public Token createTokenFromPreference(final String key) {
		if (TOKEN_TABLE.containsKey(key)) {
			return TOKEN_TABLE.get(key);
		}

		Token temp = new Token(createAttributeFromPreference(key));
		TOKEN_TABLE.put(key, temp);
		return temp;
	}

	/**
	 * Updates the Token that handles the data related to the provided key.
	 * It does this by, removing the previously defined attributes from the
	 * related Token, and recalculates them.
	 * 
	 * @param key
	 *                the PreferenceConstant element, who's attributes are
	 *                to be re-evaluated
	 * */
	public void update(final String key) {
		String baseKey = null;
		if (key.endsWith(PreferenceConstants.FOREGROUND)) {
			baseKey = key.substring(0, key.length() - PreferenceConstants.FOREGROUND.length());
		} else if (key.endsWith(PreferenceConstants.BACKGROUND)) {
			baseKey = key.substring(0, key.length() - PreferenceConstants.BACKGROUND.length());
		} else if (key.endsWith(PreferenceConstants.USEBACKGROUNDCOLOR)) {
			baseKey = key.substring(0, key.length() - PreferenceConstants.USEBACKGROUNDCOLOR.length());
		} else if (key.endsWith(PreferenceConstants.BOLD)) {
			baseKey = key.substring(0, key.length() - PreferenceConstants.BOLD.length());
		}

		if (baseKey != null && TOKEN_TABLE.containsKey(baseKey)) {
			Token tempToken = TOKEN_TABLE.get(baseKey);
			COLOR_TABLE.remove(key);
			tempToken.setData(createAttributeFromPreference(baseKey));
		}
	}
}
