/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.eclipse.titan.common.parsers.LocationAST;

/**
 * @author Kristof Szabados
 * */
public final class DefineSectionHandler {

	public static class Definition {
		private LocationAST root = null;
		private LocationAST definitionName = null;
		private LocationAST definitionValue = null;

		public LocationAST getRoot() {
			return root;
		}

		public void setRoot(LocationAST root) {
			this.root = root;
		}

		public LocationAST getDefinitionName() {
			return definitionName;
		}

		public void setDefinitionName(LocationAST definitionName) {
			this.definitionName = definitionName;
		}

		public LocationAST getDefinitionValue() {
			return definitionValue;
		}

		public void setDefinitionValue(LocationAST definitionValue) {
			this.definitionValue = definitionValue;
		}

		public void setDefinitionName(Token aToken) {
			ParserRuleContext rule = new ParserRuleContext();
			rule.addChild(aToken);
			this.definitionName = new LocationAST(rule);
		}

		public void setDefinitionValue(ParserRuleContext aRule) {
			this.definitionValue = new LocationAST(aRule);
		}

		public void setRoot(ParserRuleContext aRule) {
			this.root = new LocationAST(aRule);
		}
	}

	private LocationAST lastSectionRoot = null;
	private List<Definition> definitions = new ArrayList<Definition>();

	public LocationAST getLastSectionRoot() {
		return lastSectionRoot;
	}

	public void setLastSectionRoot(LocationAST lastSectionRoot) {
		this.lastSectionRoot = lastSectionRoot;
	}

	public List<Definition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<Definition> definitions) {
		this.definitions = definitions;
	}

	public void setLastSectionRoot(Token aToken) {
		ParserRuleContext rule = new ParserRuleContext();
		rule.addChild(aToken);
		this.lastSectionRoot = new LocationAST(rule);
	}

}
