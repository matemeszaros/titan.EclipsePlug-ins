/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a list of error behavior settings in the codec API of the run-time
 * environment.
 * 
 * @author Kristof Szabados
 */
public final class ErrorBehaviorList extends ASTNode implements ILocateableNode {
	private static final String[] VALID_TYPES = { "UNBOUND", "INCOMPL_ANY", "ENC_ENUM", "INCOMPL_MSG", "LEN_FORM", "INVAL_MSG", "REPR",
			"CONSTRAINT", "TAG", "SUPERFL", "EXTENSION", "DEC_ENUM", "DEC_DUPFLD", "DEC_MISSFLD", "DEC_OPENTYPE", "DEC_UCSTR", "LEN_ERR",
			"SIGN_ERR", "INCOMP_ORDER", "TOKEN_ERR", "LOG_MATCHING", "FLOAT_TR", "FLOAT_NAN", "OMITTED_TAG" };
	private static final String[] VALID_HANDLINGS = { "DEFAULT", "ERROR", "WARNING", "IGNORE" };

	// TODO could be optimized using real-life data
	private final List<ErrorBehaviorSetting> settings = new ArrayList<ErrorBehaviorSetting>(1);
	private final Map<String, ErrorBehaviorSetting> settingMap = new HashMap<String, ErrorBehaviorSetting>();
	private ErrorBehaviorSetting settingAll;

	/** the time when this error behavior list was checked the last time. */
	private CompilationTimeStamp lastTimeChecked;
	private Location location;

	public ErrorBehaviorList() {
		// do nothing
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = settings.size(); i < size; i++) {
			final ErrorBehaviorSetting setting = settings.get(i);
			if (setting == child) {
				return builder.append(".<setting ").append(i + 1).append('>');
			}
		}

		return builder;
	}

	public void addSetting(final ErrorBehaviorSetting setting) {
		if (setting != null) {
			settings.add(setting);
		}
	}

	public void addAllBehaviors(final ErrorBehaviorList other) {
		if (other != null) {
			settings.addAll(other.settings);
		}
	}

	public int getNofErrorBehaviors() {
		return settings.size();
	}

	public ErrorBehaviorSetting getBehaviorByIndex(final int index) {
		return settings.get(index);
	}

	public boolean hasSetting(final CompilationTimeStamp timestamp, final String errorType) {
		check(timestamp);
		return settingAll != null || settingMap.containsKey(errorType);
	}

	public String getHandling(final CompilationTimeStamp timestamp, final String errorType) {
		check(timestamp);
		if (settingMap.containsKey(errorType)) {
			return settingMap.get(errorType).getErrorHandling();
		} else if (settingAll != null) {
			return settingAll.getErrorHandling();
		}

		return "DEFAULT";
	}

	/**
	 * Does the semantic checking of the error behavior list..
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		for (int i = 0, size = settings.size(); i < size; i++) {
			final ErrorBehaviorSetting setting = settings.get(i);
			final String errorType = setting.getErrorType();
			if ("ALL".equals(errorType)) {
				if (settingAll != null) {
					setting.getLocation().reportSemanticWarning("Duplicate setting for error type `ALL'");
					settingAll.getLocation().reportSemanticWarning("The previous setting is ignored");
				}
				settingAll = setting;
				if (!settingMap.isEmpty()) {
					setting.getLocation().reportSemanticWarning("All setting before `ALL' are ignored");
					settingMap.clear();
				}
			} else {
				if (settingMap.containsKey(errorType)) {
					setting.getLocation().reportSemanticWarning(
							MessageFormat.format("Duplicate setting for error type `{0}''", errorType));
					settingMap.get(errorType).getLocation().reportSemanticWarning("The previous setting is ignored");
					settingMap.put(errorType, setting);
				} else {
					settingMap.put(errorType, setting);
				}

				boolean typeFound = false;
				for (String validType : VALID_TYPES) {
					if (validType.equals(errorType)) {
						typeFound = true;
						break;
					}
				}
				if (!typeFound) {
					setting.getLocation().reportSemanticWarning(
							MessageFormat.format("String `{0}'' is not a valid error type", errorType));
				}
			}

			final String errorHandling = setting.getErrorHandling();
			boolean handlingFound = false;
			for (String validHandling : VALID_HANDLINGS) {
				if (validHandling.equals(errorHandling)) {
					handlingFound = true;
					break;
				}
			}
			if (!handlingFound) {
				setting.getLocation().reportSemanticWarning(
						MessageFormat.format("String `{0}'' is not a valid error handling", errorHandling));
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (settings != null) {
			for (ErrorBehaviorSetting s : settings) {
				if (!s.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
