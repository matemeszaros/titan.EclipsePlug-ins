/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.utils.environment;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvironmentVariableResolver {

	private Pattern pattern;
	private String variableStart;
	private String variableEnd;

	private EnvironmentVariableResolver(Pattern pattern, String variableStart, String variableEnd) {
		this.pattern = pattern;
		this.variableStart = variableStart;
		this.variableEnd = variableEnd;
	}

	public static EnvironmentVariableResolver eclipseStyle() {
		return new EnvironmentVariableResolver(Pattern.compile("\\[.*?\\]"), "[", "]");
	}

	public static EnvironmentVariableResolver unixStyle() {
		return new EnvironmentVariableResolver(Pattern.compile("\\$\\{.*?\\}"), "${", "}");
	}

	public String resolve(final String original, final Map<?, ?> envVariables) throws VariableNotFoundException {
		try {
			return resolve(original, envVariables, false);
		} catch (IllegalArgumentException e) {
			throw new VariableNotFoundException(e.getMessage());
		}
	}

	public String resolveIgnoreErrors(final String original, final Map<?, ?> envVariables) {
		return resolve(original, envVariables, true);
	}

	private String resolve(final String original, final Map<?, ?> envVariables, boolean ignoreErrors) {
		final Matcher matcher = pattern.matcher(original);
		final StringBuffer builder = new StringBuffer(original.length());
		boolean result2 = matcher.find();
		while (result2) {
			String keyWithStartEnd = matcher.group();
			String key = keyWithStartEnd.substring(variableStart.length(), keyWithStartEnd.length() - 1);
			if (envVariables.containsKey(key)) {
				String result3 = (String) envVariables.get(key);
				matcher.appendReplacement(builder, result3.replace("\\", "\\\\").replace("$", "\\$"));
			} else {
				if (ignoreErrors) {
					matcher.appendReplacement(builder, Matcher.quoteReplacement(variableStart) + key + Matcher.quoteReplacement(variableEnd));
				} else {
					throw new IllegalArgumentException(keyWithStartEnd);
				}
			}
			result2 = matcher.find();
		}
		matcher.appendTail(builder);
		return builder.toString();
	}

	public static class VariableNotFoundException extends Exception {
		public VariableNotFoundException(String variableName) {
			super("Variable " + variableName + " cannot be resolved.");
		}
	}
}
