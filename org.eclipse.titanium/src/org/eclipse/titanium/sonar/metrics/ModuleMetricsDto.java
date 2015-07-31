/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.sonar.metrics;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "module")
@XmlAccessorType(XmlAccessType.FIELD)
class ModuleMetricsDto {

	private String projectRelativePath;

	private int linesOfCode;
	private int lines;
	private int statements;
	private int functions;
	private int altsteps;
	private int testCases;

	private int complexity;

	public ModuleMetricsDto(String projectRelativePath) {
		this.projectRelativePath = projectRelativePath;
	}

	protected ModuleMetricsDto() {
		// For JAXB
	}

	public int getTestCases() {
		return testCases;
	}

	public void setTestCases(int testCases) {
		this.testCases = testCases;
	}

	public int getLinesOfCode() {
		return linesOfCode;
	}

	public void setLinesOfCode(int linesOfCode) {
		this.linesOfCode = linesOfCode;
	}

	public int getLines() {
		return lines;
	}

	public void setLines(int lines) {
		this.lines = lines;
	}

	public String getProjectRelativePath() {
		return projectRelativePath;
	}

	public int getAltsteps() {
		return altsteps;
	}

	public void setAltsteps(int altsteps) {
		this.altsteps = altsteps;
	}

	public int getFunctions() {
		return functions;
	}

	public void setFunctions(int functions) {
		this.functions = functions;
	}

	public int getComplexity() {
		return complexity;
	}

	public void setComplexity(int complexity) {
		this.complexity = complexity;
	}

	public int getStatements() {
		return statements;
	}

	public void setStatements(int statements) {
		this.statements = statements;
	}
}
