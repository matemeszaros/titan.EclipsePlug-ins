/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.TITANFlagsOptionsData;

/**
 * @author Kristof Szabados
 * */
public abstract class CompositeTemplate extends TTCN3Template {
	private static final String FULLNAMEPART = ".list_item(";

	protected final ListOfTemplates templates;

	public CompositeTemplate(final ListOfTemplates templates) {
		this.templates = templates;

		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			templates.getTemplateByIndex(i).setFullNameParent(this);
		}
	}

	/**
	 * @param index
	 *                the index of the element to return.
	 * 
	 * @return the template on the indexed position.
	 * */
	public ITemplateListItem getTemplateByIndex(final int index) {  //???
		return templates.getTemplateByIndex(index);
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		templates.setMyScope(scope);
	}

	/** @return the number of templates in the list */
	public int getNofTemplates() {
		return templates.getNofTemplates();
	}

	/**
	 * Calculates the number of list members which are not the any or none
	 * symbol.
	 *
	 * @return the number calculated.
	 * */
	public int getNofTemplatesNotAnyornone(final CompilationTimeStamp timestamp) {
		int result = 0;
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			ITTCN3Template template = templates.getTemplateByIndex(i);
			Template_type ttype = template.getTemplatetype();
			
			switch (ttype) {
			case ANY_OR_OMIT:
				break;
			case PERMUTATION_MATCH:
				if(template instanceof TemplateBody){
					template = ((TemplateBody) template).getTemplate();
				}
				result += ((PermutationMatch_Template) template).getNofTemplatesNotAnyornone(timestamp);
				break;
			case ALLELEMENTSFROM:
				result += ((AllElementsFrom) template).getNofTemplatesNotAnyornone(timestamp);
				break;
			default:
				result++;
				break;
			}
		}

		return result;
	}

	/**
	 * Checks if the list of templates has at least one any or none symbol.
	 * 
	 * @return true if an any or none symbol was found, false otherwise.
	 * */
	public boolean templateContainsAnyornone() {
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			ITTCN3Template template = templates.getTemplateByIndex(i);
			switch (template.getTemplatetype()) {
			case ANY_OR_OMIT:
				return true;
			case PERMUTATION_MATCH:
				if(template instanceof TemplateBody){
					template = ((TemplateBody) template).getTemplate();
				}
				if (((PermutationMatch_Template) template).templateContainsAnyornone()) {
					return true;
				}
				break;
			default:
				break;
			}
		}

		return false;
	}
	
	/**
	 * Checks if the list of templates has at least one any or none or permutation symbol
	 * <p> It is prohibited after "all from"
	 * 
	 * @return true if an any or none symbol was found, false otherwise.
	 * */
	public boolean containsAnyornoneOrPermutation() {
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			ITTCN3Template template = templates.getTemplateByIndex(i);
			switch (template.getTemplatetype()) {
			case ANY_OR_OMIT:
			case PERMUTATION_MATCH:
				return true;
			default:
				break;
			}
		}

		return false;
	}
	

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			if (templates.getTemplateByIndex(i) == child) {
				return builder.append(FULLNAMEPART).append(String.valueOf(i)).append(INamedNode.RIGHTPARENTHESES);
			}
		}

		return builder;
	}

	protected abstract String getNameForStringRep();

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append(getNameForStringRep() + "( ");
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			ITTCN3Template template = templates.getTemplateByIndex(i);
			builder.append(template.createStringRepresentation());
		}
		builder.append(" )");

		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}

		return builder.toString();
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
				ITTCN3Template template = templates.getTemplateByIndex(i);
				if (template != null) {
					referenceChain.markState();
					template.checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (lengthRestriction != null) {
			lengthRestriction.updateSyntax(reparser, false);
			reparser.updateLocation(lengthRestriction.getLocation());
		}

		if (baseTemplate instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) baseTemplate).updateSyntax(reparser, false);
			reparser.updateLocation(baseTemplate.getLocation());
		} else if (baseTemplate != null) {
			throw new ReParseException();
		}

		templates.updateSyntax(reparser, false);
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (templates == null) {
			return;
		}

		templates.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (templates != null && !templates.accept(v)) {
			return false;
		}
		return true;
	}
	
	/** Test if omit is allowed in a value list
	 * <p>
	 *  Uses TITANFlagsOptionsData.ALLOW_OMIT_IN_VALUELIST_TEMPLATE_PROPERTY:
	 *  (It is the same as -M flag of makefilegen) <p>
	 *  If it is true the old syntax allowed.
	 *  If it is false then only the new syntax is allowed.<p>
	 *	For example:<p/>
	 *	 ( 1 ifpresent, 2 ifpresent, omit ) //=> allowed in old solution,
	 *                                           not allowed in new solution (3 error markers)<p>
	 *	 ( 1, 2 ) ifpresent //= only this allowed in new solution when this function returns false<p>
	 *
	 * @param allowOmit true if the field is optional field,
	 *                  false if the field is mandatory.<p>
	 *                  Of course in this case omit value and the ifpresent clause is prohibitied=> returns false<p>
	 * @return 	 
	 *   If allowOmit == false it returns false 
	 *   ( quick exit for mandatory fields).
	 *	 If allowOmit == true it returns according to the
	 *	 project property setting 
	 *   TITANFlagsOptionsData.ALLOW_OMIT_IN_VALUELIST_TEMPLATE_PROPERTY
	 */ 
	final protected boolean allowOmitInValueList(final boolean allowOmit) {
			if( !allowOmit ) {
				return false;
			}
	
			Location loc = this.getLocation();
			if(loc == null || (loc instanceof NULL_Location)) {
				return true;
			}

			IResource f = loc.getFile();
			if( f == null) {
				return true;
			}

			IProject project = f.getProject();
			if(project == null) {
				return true;
			}

			QualifiedName qn = new QualifiedName(ProjectBuildPropertyData.QUALIFIER,TITANFlagsOptionsData.ALLOW_OMIT_IN_VALUELIST_TEMPLATE_PROPERTY);
			try {
				String s= project.getPersistentProperty(qn);
				return ( "true".equals(s));
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
				return true;
			}
			
		}

}
