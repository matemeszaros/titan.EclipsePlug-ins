/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.codegenerator.Field;

public class Template implements ValueHolder, Parameterizable, Modifiable {
	private String name;
	private String type;
	private Value value;
	private List<Field> parameters = new ArrayList<>();
	private List<Modification> modifications = new ArrayList<>();

	public Template(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public Value getValue() {
		return value;
	}

	@Override
	public void setValue(Value value) {
		this.value = value;
	}

	@Override
	public void addParameter(String type, String name) {
		parameters.add(new Field(type, name));
	}

	public List<Field> getParameters() {
		return parameters;
	}

	@Override
	public void addModification(Modification m) {
		modifications.add(m);
	}

	public List<Modification> getModifications() {
		return modifications;
	}
}
