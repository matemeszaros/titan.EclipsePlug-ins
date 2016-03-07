/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.brokenpartsanalyzers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Peter Olah
 */
public abstract class ReferencesProcessor extends AssignmentHandler {

	public ReferencesProcessor(Assignment assignment) {
		super(assignment);
	}
	
	public Set<String> computeReferences(final Set<Reference> references) {
		Set<String> result = new HashSet<String>();
		for (Reference reference : references) {
			Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
			
			if (assignment != null && assignment.getIdentifier() != null) {
				// the name of the assignment might differ from the reference in incremental parsing mode
				result.add(assignment.getIdentifier().getDisplayName());
				
			} else {
				// if semantic error occurs, assignment will be null
				// have to get reference id
				result.add(reference.getId().getDisplayName());	
			}
		}
		return result;
	}
	
	public boolean containsErroneousReference(final Set<Reference> references) {
		for (Reference reference : references) {
			if(reference.getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
				return true;
			}
			
			Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
			if(assignment == null) {
				return true;
			}
		}
		
		return false;
	}
}
