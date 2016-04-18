/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.implementation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;

public class MMAfferentCoupling extends BaseModuleMetric {
	private final Map<Module, Set<Assignment>> afferentCoupling = new HashMap<Module, Set<Assignment>>();

	public MMAfferentCoupling() {
		super(ModuleMetric.AFFERENT_COUPLING);
	}

	@Override
	public void init(final MetricData data) {
		afferentCoupling.clear();
		for (final Module module : data.getModules()) {
			afferentCoupling.put(module, new HashSet<Assignment>());
		}
		for (Module module : data.getModules()) {
			module.accept(new ASTVisitor() {
				@Override
				public int visit(final IVisitableNode node) {
					if (node instanceof Assignment) {
						final Assignment assignment = (Assignment) node;
						node.accept(new AfferentCouplingDetector(assignment, afferentCoupling));
						return V_SKIP;
					} else if (node instanceof Definition) {
						return V_SKIP;
					} else {
						return V_CONTINUE;
					}
				}
			});
		}
	}

	@Override
	public Number measure(final MetricData data, final Module module) {
		return afferentCoupling.get(module).size();
	}
}

class AfferentCouplingDetector extends ASTVisitor {
	private final Assignment ownAssignment;
	private final Map<Module, Set<Assignment>> afferentCoupling;
	private final Module ownModule;

	public AfferentCouplingDetector(final Assignment ownAssignment, final Map<Module, Set<Assignment>> afferentCoupling) {
		this.ownAssignment = ownAssignment;
		this.afferentCoupling = afferentCoupling;
		ownModule = ownAssignment.getMyScope().getModuleScope();
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof Reference) {
			final Reference reference = (Reference) node;
			final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
			if (assignment != null) {
				final Module referredModule = assignment.getMyScope().getModuleScope();
				if (!ownModule.equals(referredModule)) {
					final Set<Assignment> assignments = afferentCoupling.get(referredModule);
					if (assignments != null) {
						assignments.add(ownAssignment);
					}
				}
			}
		}
		return V_CONTINUE;
	}
}
