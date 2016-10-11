/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to model a test component in our execution model.
 * Components are created, connected and eventually die. They may also be
 * filtered on request by the user. A single port can be connected to several
 * remote components at once. Connections are always just added, but never
 * removed. We can disconnect and reconnect and all connections will remain.
 * This seems to be a valid problem, but out of the scope of HL31907.
 */
public class TestComponent {
	/** The name of this component. */
	private String name;
	/** The reference of this component. */
	private String reference;
	/** Alternative if filtered.*/
	private String alternative;
	/** The verdict for the component. */
	private String verdict;
	/** Map of port mappings to */
	private Map<String, Set<String>> ports;
	private int recordNumber;

	/**
	 * Constructor.
	 */
	public TestComponent(final EventObject event, final String pAlternative) {
		this.name = event.getName();
		this.reference = event.getReference();
		this.alternative = pAlternative;
		this.ports = new HashMap<String, Set<String>>();
		this.verdict = "none"; //$NON-NLS-1$
		recordNumber = event.getRecordNumber();
	}

	public int getRecordNumber() {
		return recordNumber;
	}

	/**
	 * Get test component name
	 * @return the name of this test component
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get the test component reference
	 * @return Returns the reference of this test component
	 */
	public String getReference() {
		return this.reference;
	}


	/**
	 * Method : getAlternative Desc : If this component is filtered by the user,
	 * this method returns the reference to a components that is its alternative
	 * and is not filtered.
	 * @return alternative
	 */
	public String getAlternative() {
		return this.alternative;
	}

	/**
	 * This method is used to retrieve the verdict of the component
	 * @return
	 */
	public String getVerdict() {
		return this.verdict;
	}
	
	/**
	 * Adds an entry in the hashmap where a local port is mapped to the
	 * reference of a component. By looking up ports in this table, we can
	 * deduce which components are connected to us. Fix for HL31907.
	 * @param localPort the connected port
	 * @param reference the name of the remote component
	 */
	public void addFromEntry(final String localPort, final String reference) {
		Set<String> components = ports.get(localPort);
		if (components == null) {
			components = new HashSet<String>();
			ports.put(localPort, components);
		}
		components.add(reference);
	}

	/**
	 * By providing a local port name, we return a reference to the components
	 * that has created a port mapping or connection to the local port.
	 * @param localPort the components are connected onto this port
	 * @return reference to the set of components connected on localPort
	 */
	public Set<String> getMappedFromReference(final String localPort) {
		return this.ports.get(localPort);
	}

	/**
	 * This method is used to set the verdict of the  component
	 * @param pVerdict
	 */
	public void setVerdict(final String pVerdict) {
		this.verdict = pVerdict;
	}

	/**
	 * Setter for name
	 * @param name
	 */
	public void setName(final String name) {
		this.name = name;
	}
}
