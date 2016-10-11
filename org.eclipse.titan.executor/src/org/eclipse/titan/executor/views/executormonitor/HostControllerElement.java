/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.executormonitor;

import org.eclipse.titan.executor.executors.ITreeBranch;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.executors.TreeLeaf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Kristof Szabados
 * */
public final class HostControllerElement extends TreeLeaf implements ITreeBranch {

	private InformationElement ipAddressInfo;
	private InformationElement ipNumberInfo;
	private InformationElement hostNameInfo;
	private InformationElement operatingSystemInfo;
	private InformationElement stateInfo;

	private List<ComponentElement> components;

	public HostControllerElement(final String name) {
		super(name);
		components = new ArrayList<ComponentElement>(1);
	}

	public void setIPAddressInfo(final InformationElement ipAddressInfo) {
		this.ipAddressInfo = ipAddressInfo;
		ipAddressInfo.parent(this);
	}

	public void setIPNumberInfo(final InformationElement ipNumberInfo) {
		this.ipNumberInfo = ipNumberInfo;
		ipNumberInfo.parent(this);
	}

	public void setHostNameInfo(final InformationElement hostNameInfo) {
		this.hostNameInfo = hostNameInfo;
		hostNameInfo.parent(this);
	}

	public void setOperatingSystemInfo(final InformationElement operatingSystemInfo) {
		this.operatingSystemInfo = operatingSystemInfo;
		operatingSystemInfo.parent(this);
	}

	public void setStateInfo(final InformationElement stateInfo) {
		this.stateInfo = stateInfo;
		stateInfo.parent(this);
	}

	public void addComponent(final ComponentElement component) {
		components.add(component);
		component.parent(this);
	}

	@Override
	public List<ITreeLeaf> children() {
		List<ITreeLeaf> result = new ArrayList<ITreeLeaf>();

		if (null != ipAddressInfo) {
			result.add(ipAddressInfo);
		}
		if (null != ipNumberInfo) {
			result.add(ipNumberInfo);
		}
		if (null != hostNameInfo) {
			result.add(hostNameInfo);
		}
		if (null != operatingSystemInfo) {
			result.add(operatingSystemInfo);
		}
		if (null != stateInfo) {
			result.add(stateInfo);
		}

		result.addAll(components);

		return result;
	}

	/**
	 * Checks if the provided host controller is the same as the actual one.
	 *
	 * @param other the host controller to check
	 * @return true if it might be the same with a few differences, false otherwise.
	 * */
	public boolean isSame(final HostControllerElement other) {
		return null != ipAddressInfo && null != other.ipAddressInfo
				&& ipAddressInfo.name().equals(other.ipAddressInfo.name());

	}

	/**
	 * Transfers the data of a host controller into the actual one.
	 *
	 * @param other the other host controller
	 * */
	public void transferData(final HostControllerElement other) {
		ipAddressInfo = other.ipAddressInfo;
		if (null != ipAddressInfo) {
			ipAddressInfo.parent(this);
		}
		ipNumberInfo = other.ipNumberInfo;
		if (null != ipNumberInfo) {
			ipNumberInfo.parent(this);
		}
		hostNameInfo = other.hostNameInfo;
		if (null != hostNameInfo) {
			hostNameInfo.parent(this);
		}
		operatingSystemInfo = other.operatingSystemInfo;
		if (null != operatingSystemInfo) {
			operatingSystemInfo.parent(this);
		}
		stateInfo = other.stateInfo;
		if (null != stateInfo) {
			stateInfo.parent(this);
		}

		final List<ComponentElement> oldComponents = components;
		components = new ArrayList<ComponentElement>(other.components.size());
		for (ComponentElement tempElement : other.components) {
			boolean found = false;
			for (Iterator<ComponentElement> iterator = oldComponents.iterator(); iterator.hasNext() && !found;) {
				final ComponentElement oldElement = iterator.next();
				if (oldElement.isSame(tempElement)) {
					oldElement.transferData(tempElement);
					components.add(oldElement);
					iterator.remove();
					found = true;
				}
			}

			if (!found) {
				components.add(tempElement);
				tempElement.parent(this);
			}
		}
		other.components.clear();
	}
}
