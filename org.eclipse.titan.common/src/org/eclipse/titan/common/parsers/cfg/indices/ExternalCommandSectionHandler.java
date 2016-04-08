/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

import org.eclipse.titan.common.parsers.LocationAST;

/**
 * @author Kristof Szabados
 * */
public final class ExternalCommandSectionHandler {

	private LocationAST lastSectionRoot = null;

	private LocationAST beginControlPart = null;
	private LocationAST beginControlPartRoot = null;
	private LocationAST endControlPart = null;
	private LocationAST endControlPartRoot = null;
	private LocationAST beginTestcase = null;
	private LocationAST beginTestcaseRoot = null;
	private LocationAST endTestcase = null;
	private LocationAST endTestcaseRoot = null;

	public LocationAST getLastSectionRoot() {
		return lastSectionRoot;
	}

	public void setLastSectionRoot(final LocationAST lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}

	public LocationAST getBeginControlPart() {
		return beginControlPart;
	}

	public void setBeginControlPart(final LocationAST beginControlPart) {
		this.beginControlPart = beginControlPart;
	}

	public LocationAST getBeginControlPartRoot() {
		return beginControlPartRoot;
	}

	public void setBeginControlPartRoot(final LocationAST beginControlPartRoot) {
		this.beginControlPartRoot = beginControlPartRoot;
	}

	public LocationAST getEndControlPart() {
		return endControlPart;
	}

	public void setEndControlPart(final LocationAST endControlPart) {
		this.endControlPart = endControlPart;
	}

	public LocationAST getEndControlPartRoot() {
		return endControlPartRoot;
	}

	public void setEndControlPartRoot(final LocationAST endControlPartRoot) {
		this.endControlPartRoot = endControlPartRoot;
	}

	public LocationAST getBeginTestcase() {
		return beginTestcase;
	}

	public void setBeginTestcase(final LocationAST beginTestcase) {
		this.beginTestcase = beginTestcase;
	}

	public LocationAST getBeginTestcaseRoot() {
		return beginTestcaseRoot;
	}

	public void setBeginTestcaseRoot(final LocationAST beginTestcaseRoot) {
		this.beginTestcaseRoot = beginTestcaseRoot;
	}

	public LocationAST getEndTestcase() {
		return endTestcase;
	}

	public void setEndTestcase(final LocationAST endTestcase) {
		this.endTestcase = endTestcase;
	}

	public LocationAST getEndTestcaseRoot() {
		return endTestcaseRoot;
	}

	public void setEndTestcaseRoot(final LocationAST endTestcaseRoot) {
		this.endTestcaseRoot = endTestcaseRoot;
	}
}
