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

public class MMEfferentCoupling extends BaseModuleMetric {
	private final Map<Module, Set<Assignment>> efferentCoupling = new HashMap<Module, Set<Assignment>>();

	public MMEfferentCoupling() {
		super(ModuleMetric.EFFERENT_COUPLING);
	}

	@Override
	public void init(MetricData data) {
		efferentCoupling.clear();
		for (final Module module : data.getModules()) {
			efferentCoupling.put(module, new HashSet<Assignment>());
		}
		for (final Module module : data.getModules()) {
			module.accept(new ASTVisitor() {
				@Override
				public int visit(final IVisitableNode node) {
					if (node instanceof Assignment) {
						final Assignment assignment = (Assignment) node;
						final Module myModule = assignment.getMyScope().getModuleScope();
						node.accept(new EffCouplingDetector(myModule, efferentCoupling));
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
		return efferentCoupling.get(module).size();
	}
}

class EffCouplingDetector extends ASTVisitor {
	private final Module ownModule;
	private final Map<Module, Set<Assignment>> efferentCoupling;

	public EffCouplingDetector(final Module ownModule, final Map<Module, Set<Assignment>> efferentCoupling) {
		this.ownModule = ownModule;
		this.efferentCoupling = efferentCoupling;
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof Reference) {
			final Reference reference = (Reference) node;
			final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
			if (assignment != null) {
				final Module referencedModule = assignment.getMyScope().getModuleScope();
				if (!ownModule.equals(referencedModule)) {
					efferentCoupling.get(ownModule).add(assignment);
				}
			}
		}
		return V_CONTINUE;
	}
}
