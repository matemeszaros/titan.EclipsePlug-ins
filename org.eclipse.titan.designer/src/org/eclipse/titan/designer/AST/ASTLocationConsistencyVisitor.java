/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;

/**
 * @author Adam Delic
 * */
public class ASTLocationConsistencyVisitor extends ASTVisitor {
	IDocument document;
	boolean isTtcn;
	
	public ASTLocationConsistencyVisitor(IDocument document, boolean isTtcn) {
		this.document = document;
		this.isTtcn = isTtcn;
	}
	
	@Override
	public int visit(IVisitableNode node) {
		if (node instanceof Identifier) {
			Identifier id = (Identifier)node;
			Location loc = id.getLocation();
			int offset = loc.getOffset();
			int length = loc.getEndOffset()-loc.getOffset();
			String name = isTtcn ? id.getTtcnName() : id.getAsnName();
			if (isTtcn && "anytype".equals(name)) {
				// anytype hack in ttcn-3 
				return V_CONTINUE;
			}

			try {
				String strAtLoc = document.get(offset, length);
				if (!strAtLoc.equals(name)) {		
					TITANDebugConsole.println("AST<->document inconsistency: id=["+name+"] at offset,length="+
							offset+","+length+" doc.content=["+strAtLoc+"]");
					
				}
			} catch (BadLocationException e) {
				TITANDebugConsole.println("AST<->document inconsistency: id=["+name+"] at offset,length="+
						offset+","+length+" BadLocationException: "+e.getMessage());
			}
		}
		return V_CONTINUE;
	}
}
