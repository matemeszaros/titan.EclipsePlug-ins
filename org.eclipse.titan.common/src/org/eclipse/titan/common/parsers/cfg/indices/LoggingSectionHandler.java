/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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

import org.eclipse.titan.common.parsers.LocationAST;

/**
 * @author Kristof Szabados
 * */
public final class LoggingSectionHandler {

	public static class PluginSpecificParam {
		private LocationAST root = null;
		private LocationAST param = null;
		private LocationAST value = null;
		private String paramName = null;

		public PluginSpecificParam(final LocationAST root, final LocationAST param, final LocationAST value, final String paramName) {
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

		public LocationAST getRoot() {
			return root;
		}

		public void setRoot(final LocationAST root) {
			this.root = root;
		}

		public LocationAST getParam() {
			return param;
		}

		public void setParam(final LocationAST param) {
			this.param = param;
		}

		public LocationAST getValue() {
			return value;
		}

		public void setValue(final LocationAST value) {
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
		private LocationAST logFile = null;
		private LocationAST logFileRoot = null;
		private LocationAST appendFile = null;
		private LocationAST appendFileRoot = null;
		private LocationAST timestampFormat = null;
		private LocationAST timestampFormatRoot = null;
		private LocationAST logeventTypes = null;
		private LocationAST logeventTypesRoot = null;
		private LocationAST sourceInfoFormat = null;
		private LocationAST sourceInfoFormatRoot = null;
		private LocationAST logEntityName = null;
		private LocationAST logEntityNameRoot = null;
		private LocationAST matchingHints = null;
		private LocationAST matchingHintsRoot = null;

		private LocationAST logfileNumber = null;
		private LocationAST logfileNumberRoot = null;
		private LocationAST logfileSize = null;
		private LocationAST logfileSizeRoot = null;
		private LocationAST diskFullAction = null;
		private LocationAST diskFullActionRoot = null;

		private Map<LoggingBit, LocationAST> fileMaskBits = new EnumMap<LoggingBit, LocationAST>(LoggingBit.class);
		private LocationAST fileMask = null;
		private LocationAST fileMaskRoot = null;

		private Map<LoggingBit, LocationAST> consoleMaskBits = new EnumMap<LoggingBit, LocationAST>(LoggingBit.class);
		private LocationAST consoleMask = null;
		private LocationAST consoleMaskRoot = null;

		private List<PluginSpecificParam> pluginSpecificParam = new ArrayList<PluginSpecificParam>();

		private String pluginPath = null;

		private LocationAST emergencyLogging = null;
		private LocationAST emergencyLoggingBehaviour = null;
		private LocationAST emergencyLoggingMask =null;

		public LocationAST getLogFile() {
			return logFile;
		}

		public void setLogFile(final LocationAST logFile) {
			this.logFile = logFile;
		}

		public LocationAST getLogFileRoot() {
			return logFileRoot;
		}

		public void setLogFileRoot(final LocationAST logFileRoot) {
			this.logFileRoot = logFileRoot;
		}

		public LocationAST getAppendFile() {
			return appendFile;
		}

		public void setAppendFile(final LocationAST appendFile) {
			this.appendFile = appendFile;
		}

		public LocationAST getAppendFileRoot() {
			return appendFileRoot;
		}

		public void setAppendFileRoot(final LocationAST appendFileRoot) {
			this.appendFileRoot = appendFileRoot;
		}

		public LocationAST getTimestampFormat() {
			return timestampFormat;
		}

		public void setTimestampFormat(final LocationAST timestampFormat) {
			this.timestampFormat = timestampFormat;
		}

		public LocationAST getTimestampFormatRoot() {
			return timestampFormatRoot;
		}

		public void setTimestampFormatRoot(final LocationAST timestampFormatRoot) {
			this.timestampFormatRoot = timestampFormatRoot;
		}

		public LocationAST getLogeventTypes() {
			return logeventTypes;
		}

		public void setLogeventTypes(final LocationAST logeventTypes) {
			this.logeventTypes = logeventTypes;
		}

		public LocationAST getLogeventTypesRoot() {
			return logeventTypesRoot;
		}

		public void setLogeventTypesRoot(final LocationAST logeventTypesRoot) {
			this.logeventTypesRoot = logeventTypesRoot;
		}

		public LocationAST getSourceInfoFormat() {
			return sourceInfoFormat;
		}

		public void setSourceInfoFormat(final LocationAST sourceInfoFormat) {
			this.sourceInfoFormat = sourceInfoFormat;
		}

		public LocationAST getSourceInfoFormatRoot() {
			return sourceInfoFormatRoot;
		}

		public void setSourceInfoFormatRoot(final LocationAST sourceInfoFormatRoot) {
			this.sourceInfoFormatRoot = sourceInfoFormatRoot;
		}

		public LocationAST getLogEntityName() {
			return logEntityName;
		}

		public void setLogEntityName(final LocationAST logEntityName) {
			this.logEntityName = logEntityName;
		}

		public LocationAST getLogEntityNameRoot() {
			return logEntityNameRoot;
		}

		public void setLogEntityNameRoot(final LocationAST logEntityNameRoot) {
			this.logEntityNameRoot = logEntityNameRoot;
		}

		public LocationAST getMatchingHints() {
			return matchingHints;
		}

		public void setMatchingHints(final LocationAST matchingHints) {
			this.matchingHints = matchingHints;
		}

		public LocationAST getMatchingHintsRoot() {
			return matchingHintsRoot;
		}

		public void setMatchingHintsRoot(final LocationAST matchingHintsRoot) {
			this.matchingHintsRoot = matchingHintsRoot;
		}

		public LocationAST getLogfileNumber() {
			return logfileNumber;
		}

		public void setLogfileNumber(final LocationAST logfileNumber) {
			this.logfileNumber = logfileNumber;
		}

		public LocationAST getLogfileNumberRoot() {
			return logfileNumberRoot;
		}

		public void setLogfileNumberRoot(final LocationAST logfileNumberRoot) {
			this.logfileNumberRoot = logfileNumberRoot;
		}

		public LocationAST getLogfileSize() {
			return logfileSize;
		}

		public void setLogfileSize(final LocationAST logfileSize) {
			this.logfileSize = logfileSize;
		}

		public LocationAST getLogfileSizeRoot() {
			return logfileSizeRoot;
		}

		public void setLogfileSizeRoot(final LocationAST logfileSizeRoot) {
			this.logfileSizeRoot = logfileSizeRoot;
		}

		public LocationAST getDiskFullAction() {
			return diskFullAction;
		}

		public void setDiskFullAction(final LocationAST diskFullAction) {
			this.diskFullAction = diskFullAction;
		}

		public LocationAST getDiskFullActionRoot() {
			return diskFullActionRoot;
		}

		public void setDiskFullActionRoot(final LocationAST diskFullActionRoot) {
			this.diskFullActionRoot = diskFullActionRoot;
		}

		public Map<LoggingBit, LocationAST> getFileMaskBits() {
			return fileMaskBits;
		}

		public void setFileMaskBits(final Map<LoggingBit, LocationAST> fileMaskBits) {
			this.fileMaskBits = fileMaskBits;
		}

		public LocationAST getFileMask() {
			return fileMask;
		}

		public void setFileMask(final LocationAST fileMask) {
			this.fileMask = fileMask;
		}

		public LocationAST getFileMaskRoot() {
			return fileMaskRoot;
		}

		public void setFileMaskRoot(final LocationAST fileMaskRoot) {
			this.fileMaskRoot = fileMaskRoot;
		}

		public Map<LoggingBit, LocationAST> getConsoleMaskBits() {
			return consoleMaskBits;
		}

		public void setConsoleMaskBits(final Map<LoggingBit, LocationAST> consoleMaskBits) {
			this.consoleMaskBits = consoleMaskBits;
		}

		public LocationAST getConsoleMask() {
			return consoleMask;
		}

		public void setConsoleMask(final LocationAST consoleMask) {
			this.consoleMask = consoleMask;
		}

		public LocationAST getConsoleMaskRoot() {
			return consoleMaskRoot;
		}

		public void setConsoleMaskRoot(final LocationAST consoleMaskRoot) {
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

		public LocationAST getEmergencyLogging() {
			return emergencyLogging;
		}

		public void setEmergencyLogging(final LocationAST emergencyLogging) {
			this.emergencyLogging = emergencyLogging;
		}

		public LocationAST getEmergencyLoggingBehaviour() {
			return emergencyLoggingBehaviour;
		}

		public void setEmergencyLoggingBehaviour(final LocationAST emergencyLoggingBehaviour) {
			this.emergencyLoggingBehaviour = emergencyLoggingBehaviour;
		}

		public LocationAST getEmergencyLoggingMask() {
			return emergencyLoggingMask;
		}

		public void setEmergencyLoggingMask(final LocationAST emergencyLoggingMask) {
			this.emergencyLoggingMask = emergencyLoggingMask;
		}
	}

	public static class LoggerPluginEntry {
		private LocationAST loggerPluginRoot = null;
		private String name = null;
		private String path = null;

		public LocationAST getLoggerPluginRoot() {
			return loggerPluginRoot;
		}

		public void setLoggerPluginRoot(final LocationAST loggerPluginRoot) {
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
		private LocationAST loggerPluginsRoot = null;
		private LocationAST loggerPluginsListRoot = null;
		private Map<String, LoggerPluginEntry> pluginRoots = null;

		public LocationAST getLoggerPluginsRoot() {
			return loggerPluginsRoot;
		}

		public void setLoggerPluginsRoot(final LocationAST loggerPluginsRoot) {
			this.loggerPluginsRoot = loggerPluginsRoot;
		}

		public LocationAST getLoggerPluginsListRoot() {
			return loggerPluginsListRoot;
		}

		public void setLoggerPluginsListRoot(final LocationAST loggerPluginsListRoot) {
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

	private LocationAST lastSectionRoot = null;

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

	public LocationAST getLastSectionRoot() {
		return lastSectionRoot;
	}

	public void setLastSectionRoot(final LocationAST lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}
}
