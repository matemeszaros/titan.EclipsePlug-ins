/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.parsers;

import static org.eclipse.titan.common.utils.Assert.notNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.titan.log.viewer.parsers.data.TreeModel;
import org.eclipse.titan.log.viewer.preferences.DecipheringPreferenceHandler;

public class Decipherer {

	private Map<String, List<String>> ruleset = null;

	public Decipherer() {
	}

	public Decipherer(final String ruleSetName, final Map<String, List<String>> ruleSet) {
		notNull(ruleSetName, "The ruleSetName should not be null.");
		notNull(ruleSet, "The ruleSet should not be null.");

		this.ruleset = ruleSet;
	}

	/**
	 * Activates the rule set with the given name.
	 * @param rulesetName The name of the rule set
	 * @return true if succeeded, false otherwise
	 */
	public boolean setDecipheringRuleset(final String rulesetName) {
		if (rulesetName == null) {
			ruleset = null;
			return true;
		}

		ruleset = DecipheringPreferenceHandler.getRuleset(rulesetName);
		if (ruleset == null) {
			return false;
		}

		return true;
	}

	/**
	 * Deciphers the given message using the currently active rule set.
	 * @param msgType The message type of the message
	 * @param msg The message
	 * @return The deciphered message
	 */
	public String decipher(final String msgType, final String msg) {
		// FIXME: this is kinda dumb, but it works according to the description of HP99158
		// a CR should be written for ruleset handling and setting, then the next scrum sprint will be rescoped,
		// a project planning meeting will have to be held, the scrum master and the PO shall discuss it there, etc., haha :)
		if (ruleset == null && findFirstAvailableRuleSet()) {
			return msgType;
		}
		final String msgType2 = msgType.trim();
		List<String> rules = ruleset.get(msgType2);
		if (rules == null || rules.isEmpty()) {
			if (msgType2.startsWith("@")) {
				rules = ruleset.get(msgType2.substring(1));
				if (rules == null || rules.isEmpty()) {
					return null;
				}
			} else {
				return null;
			}
		}

		TreeModel model = new TreeModel();
		model.inputChanged(msg);


		for (String rule : rules) {
			final List<String> splitted = Arrays.asList(rule.split(Pattern.quote(".")));
			final String deciphered = model.getFieldValue(splitted);
			if (deciphered != null) {
				return deciphered;
			}
		}

		return null;
	}

	/** If the rule set was not set and there exists one then set the 1st as default and use it */
	private boolean findFirstAvailableRuleSet() {
		final List<String> ruleSets = DecipheringPreferenceHandler.getAvailableRuleSets();
		if (ruleSets.isEmpty()) {
			return true;
		}
		ruleset = DecipheringPreferenceHandler.getRuleset(ruleSets.get(0));
		return ruleset == null;
	}
}
