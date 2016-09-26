/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Operation_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.templates.AnyOrOmit_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.Any_Value_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TypeSet;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class ReceiveAnyTemplate extends BaseModuleCodeSmellSpotter {
	private static final String CAN_RECEIVE_ANY_OF_ONE = "Template instance of the receives statement matches any possible message,"
			+ "which might not be a problem as the port can only send one type of message";
	private static final String CAN_RECEIVE_ANY_OF_MULTIPLE = "Template instance of the receives statement matches any possible message,"
			+ "which might be a problem as the port can send more type of message, thus value redirection is semantically invalid";

	public ReceiveAnyTemplate() {
		super(CodeSmellType.RECEIVE_ANY_TEMPLATE);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof Operation_Altguard) {
			final Operation_Altguard ag = (Operation_Altguard) node;
			final Statement action = ag.getGuardStatement();
			if (action instanceof Receive_Port_Statement) {
				final Receive_Port_Statement receive = (Receive_Port_Statement) action;
				final SuperfluousTemplate st = new SuperfluousTemplate(receive);
				receive.accept(st);
				if (st.canReceiveAny() && st.hasValueRedirect()) {
					if (st.getReceivable() == null) {
						problems.report(receive.getLocation(), CAN_RECEIVE_ANY_OF_MULTIPLE);
					} else {
						problems.report(receive.getLocation(), CAN_RECEIVE_ANY_OF_ONE);
					}

				}
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(AltGuard.class);
		return ret;
	}
}

/**
 * This visitor class is used to find out, whether a receive statement has a
 * superfluous template. Currently these are considered:
 * <ul>
 * <li>an any value template of any type (i.e <code>?</code> or <code>*</code>)</li>
 * <li>an any value template of the only type the given port can receive (e.g.
 * <code>integer:?</code>)</li>
 * </ul>
 * 
 * @author poroszd
 */
final class SuperfluousTemplate extends ASTVisitor {
	// these are initialized according to the receive statement of the
	// constructor
	private IType receivableType;
	private final boolean hasValueRedirection;
	// this is the template of the receive statement, extracted during the
	// visiting
	private TemplateInstance template;
	// these are the results of the visiting
	private boolean receivesAny;
	private boolean receivesAllOfType;

	public SuperfluousTemplate(final Receive_Port_Statement rec) {
		final Port_Type port = rec.getPortType();
		receivableType = null;
		if (port != null) {
			final TypeSet ts = port.getPortBody().getOutMessage();
			if (ts != null && ts.getNofTypes() == 1) {
				receivableType = ts.getTypeByIndex(0);
			}
		}
		hasValueRedirection = (rec.getRedirectValue() != null);
		template = null;
		receivesAny = false;
		receivesAllOfType = false;
	}

	public boolean hasValueRedirect() {
		return hasValueRedirection;
	}

	/**
	 * Get the only type that the port can send (out or inout), or
	 * <code>null</code> if there is none, or more than one.
	 */
	public IType getReceivable() {
		return receivableType;
	}

	/**
	 * Get the template instance of the receive or <code>null</code> if there is
	 * none.
	 */
	public TemplateInstance getTemplate() {
		return template;
	}

	public boolean canReceiveAny() {
		return receivesAny;
	}

	public boolean canReceiveAll() {
		return receivesAllOfType;
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof TemplateInstance) {
			template = (TemplateInstance) node;
			final ITTCN3Template body = template.getTemplateBody();
			if (body instanceof Any_Value_Template || body instanceof AnyOrOmit_Template) {
				final Type type = template.getType();
				if (type == null) {
					// port.receive(?) or port.receive(*)
					receivesAny = true;
				} else if (receivableType != null && type.isIdentical(CompilationTimeStamp.getBaseTimestamp(), receivableType)) {
					// e.g. port.receive(integer:?)
					receivesAllOfType = true;
				}
			}
			// We don't want to investigate nested templates
			return V_SKIP;
		}
		return V_CONTINUE;
	}
}
