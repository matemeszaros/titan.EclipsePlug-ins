/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.types;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.titanium.Activator;

/**
 * Enum for unified handling of todo and fixme markers
 * 
 * @author poroszd
 * 
 */
public enum TaskType implements ProblemType{
	//TODO What are the default times?
	FIXME("FIXME tags", 0.0, 0.0, 0.0) {
		@Override
		public boolean equalType(IMarker m) throws CoreException {
			return IMarker.PRIORITY_HIGH == m.getAttribute(IMarker.PRIORITY, IMarker.PRIORITY_LOW);
		}
	},
	//TODO What are the default times?
	TODO("TODO tags", 0.0, 0.0, 0.0) {
		@Override
		public boolean equalType(IMarker m) throws CoreException {
			return IMarker.PRIORITY_NORMAL == m.getAttribute(IMarker.PRIORITY, IMarker.PRIORITY_LOW);
		}
	};


	private double defaultMinTime;
	private double defaultAvgTime;
	private double defaultMaxTime;
	private String readableName;
	private String innerName;
	
	TaskType(String name, Double minTime, Double avgTime, Double maxTime) {
		readableName = name;
		defaultMinTime = minTime;
		defaultAvgTime = avgTime;
		defaultMaxTime = maxTime;
		innerName = name();
	}
	
	@Override
	public int getBaseLine() {
		return getInt(ProblemNameToPreferenceMapper.nameSmellBaseLine(innerName));
	}
	
	@Override
	public int getImpact() {
		return getInt(ProblemNameToPreferenceMapper.nameSmellImpact(innerName));
	}
	
	@Override
	public double getAvgRepairTime() {
		return getDouble(ProblemNameToPreferenceMapper.nameSmellAvgTime(innerName), defaultAvgTime);
	}
	
	@Override
	public double getMinRepairTime() {
		return getDouble(ProblemNameToPreferenceMapper.nameSmellMinTime(innerName), defaultMinTime);
	}
	
	@Override
	public double getMaxRepairTime() {
		return getDouble(ProblemNameToPreferenceMapper.nameSmellMaxTime(innerName), defaultMaxTime);
	}
	
	 @Override
	public double getAvgDefaultTime() {
		 return defaultAvgTime;
	 }
	
	 @Override
	public double getMinDefaultTime() {
		 return defaultMinTime;
	 }
	
	 @Override
	public double getMaxDefaultTime() {
		 return defaultMaxTime;
	 }
	
	@Override
	public String getHumanReadableName() {
		return readableName;
	}
	
	@Override
	public String toString() {
		return innerName;
	}
	
	
	private int getInt(String id) {
		int val = Platform.getPreferencesService().getInt(Activator.PLUGIN_ID, id, -1, null);
		if (val == -1) {
			throw new IllegalArgumentException("The requested field for " + readableName + " is not found in the preference store. "
					+ "Probably you forgot to add it in the PreferenceInitializer or in the RiskFactorPreferencePage.");
		} else {
			return val;
		}
	}
	
	private double getDouble(String id, double defaultValue) {
		return Platform.getPreferencesService().getDouble(Activator.PLUGIN_ID, id, defaultValue, null);
	}

	public abstract boolean equalType(IMarker m) throws CoreException;
}
