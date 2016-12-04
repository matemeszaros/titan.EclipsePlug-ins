/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;

/**
 * A special ASTNode which contains a list of Statement nodes.
 * 
 * @author Viktor Varga
 */
class StatementList extends ASTNode implements ILocateableNode {
	
	protected Location location;
	protected List<Statement> statements;
	protected StatementBlock myStatementBlock;
	
	protected StatementList(List<Statement> statements) {
		this.statements = statements;
		if (statements == null) {
			//TODO check should probably use this. here
			statements = new ArrayList<Statement>();
		}
		if (!statements.isEmpty()) {
			myStatementBlock = statements.get(0).getMyStatementBlock();
			location = new Location(statements.get(0).getLocation().getFile(),
					statements.get(0).getLocation().getLine(),
					statements.get(0).getLocation().getOffset(),
					statements.get(statements.size()-1).getLocation().getEndOffset());
		}
	}
	
	protected boolean isEmpty() {
		return statements.isEmpty();
	}
	
	protected int getSize() {
		return statements.size();
	}
	protected Statement getStatementByIndex(final int ind) {
		return statements.get(ind);
	}
	
	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}
	protected void increaseLocationEndOffset(final int incBy) {
		location.setEndOffset(location.getEndOffset()+incBy);
	}
	@Override
	public Location getLocation() {
		return location;
	}
	protected StatementBlock getMyStatementBlock() {
		return myStatementBlock;
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (statements != null) {
			for (Statement s: statements) {
				if (!s.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public String createDebugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("ExtractToFunctionRefactoring->StatementList debug info:");
		sb.append("\n  Loc: ");
		if (location == null) {
			sb.append("null");
		} else {
			sb.append(location.getFile().getFullPath());
			sb.append(": ");
			sb.append(location.getOffset());
			sb.append("->");
			sb.append(location.getEndOffset());
		}
		sb.append("\n  MySB info: ");
		if (myStatementBlock == null || myStatementBlock.getMyDefinition() == null) {
			sb.append("null");
		} else {
			sb.append(myStatementBlock.getMyDefinition().getIdentifier());
		}
		sb.append("\n  Statements: ");
		if (statements == null) {
			sb.append("null");
		} else {
			sb.append("(count: ");
			sb.append(statements.size());
			sb.append(") \n");
			for (Statement s: statements) {
				sb.append("    ");
				sb.append(s.getStatementName());
				sb.append(", loc: ");
				if (s.getLocation() == null) {
					sb.append("null");
				} else {
					sb.append(s.getLocation().getOffset());
					sb.append("->");
					sb.append(s.getLocation().getEndOffset());
					sb.append(" in line ");
					sb.append(s.getLocation().getLine());
				}
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}
	
}


