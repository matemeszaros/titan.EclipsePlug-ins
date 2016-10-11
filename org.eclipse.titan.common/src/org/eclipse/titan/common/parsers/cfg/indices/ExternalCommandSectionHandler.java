/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ExternalCommandSectionHandler {

	private ParseTree lastSectionRoot = null;

	private ParseTree beginControlPart = null;
	private ParseTree beginControlPartRoot = null;
	private ParseTree endControlPart = null;
	private ParseTree endControlPartRoot = null;
	private ParseTree beginTestcase = null;
	private ParseTree beginTestcaseRoot = null;
	private ParseTree endTestcase = null;
	private ParseTree endTestcaseRoot = null;

	public ParseTree getLastSectionRoot() {
		return lastSectionRoot;
	}

	public void setLastSectionRoot(final ParseTree lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}

	public ParseTree getBeginControlPart() {
		return beginControlPart;
	}

	public void setBeginControlPart(final ParseTree beginControlPart) {
		this.beginControlPart = beginControlPart;
	}

	public ParseTree getBeginControlPartRoot() {
		return beginControlPartRoot;
	}

	public void setBeginControlPartRoot(final ParseTree beginControlPartRoot) {
		this.beginControlPartRoot = beginControlPartRoot;
	}

	public ParseTree getEndControlPart() {
		return endControlPart;
	}

	public void setEndControlPart(final ParseTree endControlPart) {
		this.endControlPart = endControlPart;
	}

	public ParseTree getEndControlPartRoot() {
		return endControlPartRoot;
	}

	public void setEndControlPartRoot(final ParseTree endControlPartRoot) {
		this.endControlPartRoot = endControlPartRoot;
	}

	public ParseTree getBeginTestcase() {
		return beginTestcase;
	}

	public void setBeginTestcase(final ParseTree beginTestcase) {
		this.beginTestcase = beginTestcase;
	}

	public ParseTree getBeginTestcaseRoot() {
		return beginTestcaseRoot;
	}

	public void setBeginTestcaseRoot(final ParseTree beginTestcaseRoot) {
		this.beginTestcaseRoot = beginTestcaseRoot;
	}

	public ParseTree getEndTestcase() {
		return endTestcase;
	}

	public void setEndTestcase(final ParseTree endTestcase) {
		this.endTestcase = endTestcase;
	}

	public ParseTree getEndTestcaseRoot() {
		return endTestcaseRoot;
	}

	public void setEndTestcaseRoot(final ParseTree endTestcaseRoot) {
		this.endTestcaseRoot = endTestcaseRoot;
	}
}
