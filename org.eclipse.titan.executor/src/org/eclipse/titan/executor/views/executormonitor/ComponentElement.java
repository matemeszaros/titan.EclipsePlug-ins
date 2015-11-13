/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.executormonitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.executor.executors.ITreeBranch;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.executors.TreeLeaf;

/**
 * @author Kristof Szabados
 * */
public final class ComponentElement extends TreeLeaf implements ITreeBranch {

	private InformationElement referenceInfo;
	private InformationElement typeInfo;
	private InformationElement stateInfo;
	private InformationElement executedInfo;
	private InformationElement localVerdictInfo;

	public ComponentElement(final String name) {
		super(name);
	}

	public ComponentElement(final String name, final InformationElement referenceInfo) {
		super(name);
		this.referenceInfo = referenceInfo;
		referenceInfo.parent(this);
	}

	public void setTypeInfo(final InformationElement typeInfo) {
		this.typeInfo = typeInfo;
		typeInfo.parent(this);
	}

	public void setStateInfo(final InformationElement stateInfo) {
		this.stateInfo = stateInfo;
		stateInfo.parent(this);
	}

	public void setExecutedInfo(final InformationElement executedInfo) {
		this.executedInfo = executedInfo;
		executedInfo.parent(this);
	}

	public void setLocalVerdictInfo(final InformationElement localVerdictInfo) {
		this.localVerdictInfo = localVerdictInfo;
		localVerdictInfo.parent(this);
	}

	@Override
	public List<ITreeLeaf> children() {
		List<ITreeLeaf> result = new ArrayList<ITreeLeaf>();

		if (null != referenceInfo) {
			result.add(referenceInfo);
		}
		if (null != typeInfo) {
			result.add(typeInfo);
		}
		if (null != stateInfo) {
			result.add(stateInfo);
		}
		if (null != executedInfo) {
			result.add(executedInfo);
		}
		if (null != localVerdictInfo) {
			result.add(localVerdictInfo);
		}

		return result;
	}

	/**
	 * Checks if the provided component is the same as the actual one.
	 *
	 * @param other the component to check
	 * @return true if it might be the same with a few differences, false otherwise.
	 * */
	public boolean isSame(final ComponentElement other) {
		if (null != referenceInfo && null != other.referenceInfo && referenceInfo.name().equals(other.referenceInfo.name())) {
			return true;
		}

		if (null != name() && null != other.name() && name().equals(other.name())) {
			return true;
		}

		return false;
	}

	/**
	 * Transfers the data of a component into the actual one.
	 *
	 * @param other the other component
	 * */
	public void transferData(final ComponentElement other) {
		referenceInfo = other.referenceInfo;
		if (null != referenceInfo) {
			referenceInfo.parent(this);
		}
		typeInfo = other.typeInfo;
		if (null != typeInfo) {
			typeInfo.parent(this);
		}
		stateInfo = other.stateInfo;
		if (null != stateInfo) {
			stateInfo.parent(this);
		}
		executedInfo = other.executedInfo;
		if (null != executedInfo) {
			executedInfo.parent(this);
		}
		localVerdictInfo = other.localVerdictInfo;
		if (null != localVerdictInfo) {
			localVerdictInfo.parent(this);
		}
	}
}
