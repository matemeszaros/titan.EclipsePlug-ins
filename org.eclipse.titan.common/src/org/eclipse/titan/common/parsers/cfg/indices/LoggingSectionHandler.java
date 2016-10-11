/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Stores temporary config editor data of the logging section
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class LoggingSectionHandler extends ConfigSectionHandlerBase {

	public static class PluginSpecificParam {
		private ParseTree root = null;
		private ParseTree param = null;
		private ParseTree value = null;
		private String paramName = null;

		public PluginSpecificParam(final ParseTree root, final ParseTree param, final ParseTree value, final String paramName) {
			this.setRoot(root);
			this.setParam(param);
			this.setValue(value);
			this.setParamName(paramName);
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}

			if (o == null) {
				return false;
			}

			if (o.getClass() != getClass()) {
				return false;
			}

			final PluginSpecificParam p = (PluginSpecificParam) o;
			return getParam().equals(p.getParam()) && getValue().equals(p.getValue());
		}

		@Override
		public int hashCode() {
			return getParam().hashCode() + 31 * getValue().hashCode();
		}

		public ParseTree getRoot() {
			return root;
		}

		public void setRoot(final ParseTree root) {
			this.root = root;
		}

		public ParseTree getParam() {
			return param;
		}

		public void setParam(final ParseTree param) {
			this.param = param;
		}

		public ParseTree getValue() {
			return value;
		}

		public void setValue(final ParseTree value) {
			this.value = value;
		}

		public String getParamName() {
			return paramName;
		}

		public void setParamName(final String paramName) {
			this.paramName = paramName;
		}
	}

	public static class LogParamEntry {
		private ParseTree logFile = null;
		private ParseTree logFileRoot = null;
		private ParseTree appendFile = null;
		private ParseTree appendFileRoot = null;
		private ParseTree timestampFormat = null;
		private ParseTree timestampFormatRoot = null;
		private ParseTree logeventTypes = null;
		private ParseTree logeventTypesRoot = null;
		private ParseTree sourceInfoFormat = null;
		private ParseTree sourceInfoFormatRoot = null;
		private ParseTree logEntityName = null;
		private ParseTree logEntityNameRoot = null;
		private ParseTree matchingHints = null;
		private ParseTree matchingHintsRoot = null;

		private ParseTree logfileNumber = null;
		private ParseTree logfileNumberRoot = null;
		private ParseTree logfileSize = null;
		private ParseTree logfileSizeRoot = null;
		private ParseTree diskFullAction = null;
		private ParseTree diskFullActionRoot = null;

		private Map<LoggingBit, ParseTree> fileMaskBits = new EnumMap<LoggingBit, ParseTree>(LoggingBit.class);
		private ParseTree fileMask = null;
		private ParseTree fileMaskRoot = null;

		private Map<LoggingBit, ParseTree> consoleMaskBits = new EnumMap<LoggingBit, ParseTree>(LoggingBit.class);
		private ParseTree consoleMask = null;
		private ParseTree consoleMaskRoot = null;

		private List<PluginSpecificParam> pluginSpecificParam = new ArrayList<PluginSpecificParam>();

		private String pluginPath = null;

		private ParseTree emergencyLogging = null;
		private ParseTree emergencyLoggingBehaviour = null;
		private ParseTree emergencyLoggingMask =null;

		public ParseTree getLogFile() {
			return logFile;
		}

		public void setLogFile(final ParseTree logFile) {
			this.logFile = logFile;
		}

		public ParseTree getLogFileRoot() {
			return logFileRoot;
		}

		public void setLogFileRoot(final ParseTree logFileRoot) {
			this.logFileRoot = logFileRoot;
		}

		public ParseTree getAppendFile() {
			return appendFile;
		}

		public void setAppendFile(final ParseTree appendFile) {
			this.appendFile = appendFile;
		}

		public ParseTree getAppendFileRoot() {
			return appendFileRoot;
		}

		public void setAppendFileRoot(final ParseTree appendFileRoot) {
			this.appendFileRoot = appendFileRoot;
		}

		public ParseTree getTimestampFormat() {
			return timestampFormat;
		}

		public void setTimestampFormat(final ParseTree timestampFormat) {
			this.timestampFormat = timestampFormat;
		}

		public ParseTree getTimestampFormatRoot() {
			return timestampFormatRoot;
		}

		public void setTimestampFormatRoot(final ParseTree timestampFormatRoot) {
			this.timestampFormatRoot = timestampFormatRoot;
		}

		public ParseTree getLogeventTypes() {
			return logeventTypes;
		}

		public void setLogeventTypes(final ParseTree logeventTypes) {
			this.logeventTypes = logeventTypes;
		}

		public ParseTree getLogeventTypesRoot() {
			return logeventTypesRoot;
		}

		public void setLogeventTypesRoot(final ParseTree logeventTypesRoot) {
			this.logeventTypesRoot = logeventTypesRoot;
		}

		public ParseTree getSourceInfoFormat() {
			return sourceInfoFormat;
		}

		public void setSourceInfoFormat(final ParseTree sourceInfoFormat) {
			this.sourceInfoFormat = sourceInfoFormat;
		}

		public ParseTree getSourceInfoFormatRoot() {
			return sourceInfoFormatRoot;
		}

		public void setSourceInfoFormatRoot(final ParseTree sourceInfoFormatRoot) {
			this.sourceInfoFormatRoot = sourceInfoFormatRoot;
		}

		public ParseTree getLogEntityName() {
			return logEntityName;
		}

		public void setLogEntityName(final ParseTree logEntityName) {
			this.logEntityName = logEntityName;
		}

		public ParseTree getLogEntityNameRoot() {
			return logEntityNameRoot;
		}

		public void setLogEntityNameRoot(final ParseTree logEntityNameRoot) {
			this.logEntityNameRoot = logEntityNameRoot;
		}

		public ParseTree getMatchingHints() {
			return matchingHints;
		}

		public void setMatchingHints(final ParseTree matchingHints) {
			this.matchingHints = matchingHints;
		}

		public ParseTree getMatchingHintsRoot() {
			return matchingHintsRoot;
		}

		public void setMatchingHintsRoot(final ParseTree matchingHintsRoot) {
			this.matchingHintsRoot = matchingHintsRoot;
		}

		public ParseTree getLogfileNumber() {
			return logfileNumber;
		}

		public void setLogfileNumber(final ParseTree logfileNumber) {
			this.logfileNumber = logfileNumber;
		}

		public ParseTree getLogfileNumberRoot() {
			return logfileNumberRoot;
		}

		public void setLogfileNumberRoot(final ParseTree logfileNumberRoot) {
			this.logfileNumberRoot = logfileNumberRoot;
		}

		public ParseTree getLogfileSize() {
			return logfileSize;
		}

		public void setLogfileSize(final ParseTree logfileSize) {
			this.logfileSize = logfileSize;
		}

		public ParseTree getLogfileSizeRoot() {
			return logfileSizeRoot;
		}

		public void setLogfileSizeRoot(final ParseTree logfileSizeRoot) {
			this.logfileSizeRoot = logfileSizeRoot;
		}

		public ParseTree getDiskFullAction() {
			return diskFullAction;
		}

		public void setDiskFullAction(final ParseTree diskFullAction) {
			this.diskFullAction = diskFullAction;
		}

		public ParseTree getDiskFullActionRoot() {
			return diskFullActionRoot;
		}

		public void setDiskFullActionRoot(final ParseTree diskFullActionRoot) {
			this.diskFullActionRoot = diskFullActionRoot;
		}

		public Map<LoggingBit, ParseTree> getFileMaskBits() {
			return fileMaskBits;
		}

		public void setFileMaskBits(final Map<LoggingBit, ParseTree> fileMaskBits) {
			this.fileMaskBits = fileMaskBits;
		}

		public ParseTree getFileMask() {
			return fileMask;
		}

		public void setFileMask(final ParseTree fileMask) {
			this.fileMask = fileMask;
		}

		public ParseTree getFileMaskRoot() {
			return fileMaskRoot;
		}

		public void setFileMaskRoot(final ParseTree fileMaskRoot) {
			this.fileMaskRoot = fileMaskRoot;
		}

		public Map<LoggingBit, ParseTree> getConsoleMaskBits() {
			return consoleMaskBits;
		}

		public void setConsoleMaskBits(final Map<LoggingBit, ParseTree> consoleMaskBits) {
			this.consoleMaskBits = consoleMaskBits;
		}

		public ParseTree getConsoleMask() {
			return consoleMask;
		}

		public void setConsoleMask(final ParseTree consoleMask) {
			this.consoleMask = consoleMask;
		}

		public ParseTree getConsoleMaskRoot() {
			return consoleMaskRoot;
		}

		public void setConsoleMaskRoot(final ParseTree consoleMaskRoot) {
			this.consoleMaskRoot = consoleMaskRoot;
		}

		public List<PluginSpecificParam> getPluginSpecificParam() {
			return pluginSpecificParam;
		}

		public void setPluginSpecificParam(final List<PluginSpecificParam> pluginSpecificParam) {
			this.pluginSpecificParam = pluginSpecificParam;
		}

		public String getPluginPath() {
			return pluginPath;
		}

		public void setPluginPath(final String pluginPath) {
			this.pluginPath = pluginPath;
		}

		public ParseTree getEmergencyLogging() {
			return emergencyLogging;
		}

		public void setEmergencyLogging(final ParseTree emergencyLogging) {
			this.emergencyLogging = emergencyLogging;
		}

		public ParseTree getEmergencyLoggingBehaviour() {
			return emergencyLoggingBehaviour;
		}

		public void setEmergencyLoggingBehaviour(final ParseTree emergencyLoggingBehaviour) {
			this.emergencyLoggingBehaviour = emergencyLoggingBehaviour;
		}

		public ParseTree getEmergencyLoggingMask() {
			return emergencyLoggingMask;
		}

		public void setEmergencyLoggingMask(final ParseTree emergencyLoggingMask) {
			this.emergencyLoggingMask = emergencyLoggingMask;
		}
	}

	public static class LoggerPluginEntry {
		private ParseTree loggerPluginRoot = null;
		private String name = null;
		private String path = null;

		public ParseTree getLoggerPluginRoot() {
			return loggerPluginRoot;
		}

		public void setLoggerPluginRoot(final ParseTree loggerPluginRoot) {
			this.loggerPluginRoot = loggerPluginRoot;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getPath() {
			return path;
		}

		public void setPath(final String path) {
			this.path = path;
		}
	}

	public static class LoggerPluginsEntry {
		private ParseTree loggerPluginsRoot = null;
		private ParseTree loggerPluginsListRoot = null;
		private Map<String, LoggerPluginEntry> pluginRoots = null;

		public ParseTree getLoggerPluginsRoot() {
			return loggerPluginsRoot;
		}

		public void setLoggerPluginsRoot(final ParseTree loggerPluginsRoot) {
			this.loggerPluginsRoot = loggerPluginsRoot;
		}

		public ParseTree getLoggerPluginsListRoot() {
			return loggerPluginsListRoot;
		}

		public void setLoggerPluginsListRoot(final ParseTree loggerPluginsListRoot) {
			this.loggerPluginsListRoot = loggerPluginsListRoot;
		}

		public Map<String, LoggerPluginEntry> getPluginRoots() {
			return pluginRoots;
		}

		public void setPluginRoots(final Map<String, LoggerPluginEntry> pluginRoots) {
			this.pluginRoots = pluginRoots;
		}
	}

	private Map<String, LoggerPluginsEntry> loggerPluginsTree = new HashMap<String, LoggingSectionHandler.LoggerPluginsEntry>();

	// component/plugin hashmap
	private Map<String,HashMap<String,LogParamEntry>> loggerTree = new HashMap<String,HashMap<String,LogParamEntry>>();

	public Set<String> getComponents() {
		return loggerTree.keySet();
	}

	public Set<String> getPlugins(final String componentName) {
		final Map<String,LogParamEntry> pluginsMap = loggerTree.get(componentName);
		if (pluginsMap==null) {
			return new HashSet<String>();
		}

		return pluginsMap.keySet();
	}

	/*
	 * if a key does not exist it will be automatically created
	 */
	public LogParamEntry componentPlugin(final String componentName, final String pluginName) {
		String tempComponentName = componentName;
		if (componentName==null) {
			tempComponentName = "*";
		}
		String tempPluginName = pluginName;
		if (pluginName==null) {
			tempPluginName = "*";
		}
		if (!loggerTree.containsKey(tempComponentName)) {
			loggerTree.put(tempComponentName, new HashMap<String,LogParamEntry>());
		}

		final Map<String,LogParamEntry> pluginMap = loggerTree.get(tempComponentName);
		if (!pluginMap.containsKey(tempPluginName)) {
			pluginMap.put(tempPluginName, new LogParamEntry());
		}
		return pluginMap.get(tempPluginName);
	}

	/*
	 * helper class for the SWT tree providers
	 */
	public static class LoggerTreeElement {
		private LoggingSectionHandler lsh = null;
		private String componentName = null;
		private String pluginName = null;
		public LoggerTreeElement(final LoggingSectionHandler lsh, final String componentName, final String pluginName) {
			this.lsh = lsh;
			this.componentName = componentName;
			this.pluginName = pluginName;
		}
		public LoggerTreeElement(final LoggingSectionHandler lsh, final String componentName) {
			this.lsh = lsh;
			this.componentName = componentName;
			this.pluginName = null;
		}
		public void writeNamePrefix(final StringBuilder name) {
			name.append(componentName).append('.');
			if (pluginName!=null) {
				name.append(pluginName).append('.');
			}
		}

		public LoggingSectionHandler getLsh() {
			return lsh;
		}

		public void setLsh(final LoggingSectionHandler lsh) {
			this.lsh = lsh;
		}

		public String getComponentName() {
			return componentName;
		}

		public void setComponentName(final String componentName) {
			this.componentName = componentName;
		}

		public String getPluginName() {
			return pluginName;
		}

		public void setPluginName(final String pluginName) {
			this.pluginName = pluginName;
		}
	}

	public void removeTreeElement(final LoggerTreeElement lte) {
		if (lte.pluginName==null) {
			loggerTree.remove(lte.componentName);
		} else {
			loggerTree.get(lte.componentName).remove(lte.pluginName);
		}
	}

	public Object[] getComponentsTreeElementArray() {
		final List<LoggerTreeElement> rv = new ArrayList<LoggerTreeElement>();
		for (final String s : loggerTree.keySet()) {
			rv.add(new LoggerTreeElement(this,s));
		}

		return rv.toArray();
	}

	public Object[] getPluginsTreeElementArray(final String componentName) {
		final List<LoggerTreeElement> rv = new ArrayList<LoggerTreeElement>();
		final Map<String,LogParamEntry> pluginsMap = loggerTree.get(componentName);
		if (pluginsMap==null) {
			return new Object[] {};
		}

		for (final String s : pluginsMap.keySet()) {
			rv.add(new LoggerTreeElement(this,componentName,s));
		}
		return rv.toArray();
	}

	public Map<String, LoggerPluginsEntry> getLoggerPluginsTree() {
		return loggerPluginsTree;
	}

	public void setLoggerPluginsTree(final Map<String, LoggerPluginsEntry> loggerPluginsTree) {
		this.loggerPluginsTree = loggerPluginsTree;
	}
}
