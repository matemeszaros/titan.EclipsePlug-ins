/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents BNF element/rule "TemplateBody"
 * TemplateBody and AllElementsFrom are thin wrapper classes of TTCN3Template
 * The only reason of this implementation is to handle in the same manner TemplateBody and AllElementFrom
 * and to give a context dependent property to the template
 * 
 * @author ethbaat
 *
 */
public class TemplateBody implements ITemplateListItem {
	
	protected TTCN3Template template;
	
	public TemplateBody(){
		template = null;
	}
	
	public TemplateBody(final TTCN3Template t){
		template = t;
	}

	@Override
	public void copyGeneralProperties(final ITTCN3Template original) {
		template.copyGeneralProperties(original);
	}

	@Override
	public String getTemplateTypeName() {
		return template.getTemplateTypeName();
	}

	@Override
	public IType getMyGovernor() {
		return template.getMyGovernor();
	}

	@Override
	public void setMyGovernor(final IType governor) {
		template.setMyGovernor(governor);		
	}

	@Override
	public String chainedDescription() {
		return template.chainedDescription();
	}

	@Override
	public String createStringRepresentation() {
		return template.createStringRepresentation();
	}

	@Override
	public ITTCN3Template setLoweridToReference(final CompilationTimeStamp timestamp) {
		return template.setLoweridToReference(timestamp);
	}

	@Override
	public void setLengthRestriction(final LengthRestriction lengthRestriction) {
		template.setLengthRestriction(lengthRestriction);	
	}

	@Override
	public LengthRestriction getLengthRestriction() {
		return template.getLengthRestriction();
	}

	@Override
	public void setIfpresent() {
		template.setIfpresent();
	}

	@Override
	public ITTCN3Template getBaseTemplate() {
		return template.getBaseTemplate();
	}

	@Override
	public void setBaseTemplate(final ITTCN3Template baseTemplate) {
		template.setBaseTemplate(baseTemplate);		
	}

	@Override
	public Completeness_type getCompletenessConditionSeof(
			final CompilationTimeStamp timestamp, final boolean incompleteAllowed) {
		return template.getCompletenessConditionSeof(timestamp, incompleteAllowed);
	}

	@Override
	public Completeness_type getCompletenessConditionChoice(
			final CompilationTimeStamp timestamp, final boolean incompleteAllowed,
			final Identifier fieldName) {
		 
		return template.getCompletenessConditionChoice(timestamp, incompleteAllowed, fieldName);
	}

	@Override
	public TTCN3Template getTemplateReferencedLast(
			final CompilationTimeStamp timestamp) {
		return template.getTemplateReferencedLast(timestamp);
	}

	@Override
	public TTCN3Template getTemplateReferencedLast(
			final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
			return template.getTemplateReferencedLast(timestamp,referenceChain);
	}

	@Override
	public ITTCN3Template setTemplatetype(final CompilationTimeStamp timestamp,
			final Template_type newType) {
		return template.setTemplatetype(timestamp, newType);
	}

	@Override
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp,
			final Expected_Value_type expectedValue) {
		return template.getExpressionReturntype(timestamp, expectedValue);
	}

	@Override
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp,
			final Expected_Value_type expectedValue) {
		return template.getExpressionGovernor(timestamp, expectedValue);
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp,
			final IReferenceChain referenceChain) {
		template.checkRecursions(timestamp, referenceChain);	
	}

	@Override
	public ITTCN3Template getReferencedSubTemplate(
			final CompilationTimeStamp timestamp, final Reference reference,
			final IReferenceChain referenceChain) {
		return template.getReferencedSubTemplate(timestamp, reference, referenceChain);
	}

	@Override
	public boolean isValue(final CompilationTimeStamp timestamp) {
		return template.isValue(timestamp);
	}

	@Override
	public IValue getValue() {
		return template.getValue();
	}

	@Override
	public void checkThisTemplateGeneric(final CompilationTimeStamp timestamp,
			final IType type, final boolean isModified, final boolean allowOmit,
			final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit) {
		template.checkThisTemplateGeneric(
				timestamp, type, isModified, allowOmit, allowAnyOrOmit, subCheck, implicitOmit);
	}

	@Override
	public void checkRestrictionCommon(final CompilationTimeStamp timestamp, final String definitionName,
			final Restriction_type templateRestriction, final Location usageLocation) {
		template.checkRestrictionCommon(timestamp, definitionName, templateRestriction, usageLocation);	
	}

	@Override
	public boolean checkValueomitRestriction(final CompilationTimeStamp timestamp,
			final String definitionName, final boolean omitAllowed, final Location usageLocation) {
		return template.checkValueomitRestriction(timestamp, definitionName, omitAllowed, usageLocation);
	}

	@Override
	public boolean chkRestrictionNamedListBaseTemplate(
			final CompilationTimeStamp timestamp, final String definitionName,
			final Set<String> checkedNames, final int neededCheckedCnt, final Location usageLocation) {
			return template.chkRestrictionNamedListBaseTemplate(timestamp, definitionName, checkedNames, neededCheckedCnt, usageLocation);
	}

	@Override
	public boolean checkPresentRestriction(final CompilationTimeStamp timestamp,
			String definitionName, final Location usageLocation) {
		return template.checkPresentRestriction(timestamp, definitionName, usageLocation);
	}

	@Override
	public boolean getIsErroneous(final CompilationTimeStamp timestamp) {
		return template.getIsErroneous(timestamp);
	}

	@Override
	public void setIsErroneous(final boolean isErroneous) {
		template.setIsErroneous(isErroneous);
		
	}

	@Override
	public Setting_type getSettingtype() {
		return template.getSettingtype();
	}

	@Override
	public void setLocation(final Location location) {
		if(location !=null) {
		  template.setLocation(location);
		}
		
	}

	@Override
	public boolean isAsn() {
		return template.isAsn();
	}

	@Override
	public void setMyScope(final Scope scope) {
		template.setMyScope(scope);
	}

	@Override
	public Scope getMyScope() {
		return template.getMyScope();
	}

	@Override
	public void setFullNameParent(final INamedNode nameParent) {
		template.setFullNameParent(nameParent);	
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		return template.getFullName(child);
	}

	@Override
	public String getFullName() {
		return template.getFullName();
	}

	@Override
	public INamedNode getNameParent() {
		return template.getNameParent();
	}

	@Override
	public boolean accept(final ASTVisitor v) {
		return template.accept(v);
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged)
			throws ReParseException {
		template.updateSyntax(reparser, isDamaged);
		
	}

	@Override
	public Location getLocation() {
		return template.getLocation();
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder,
			final List<Hit> foundIdentifiers) {
		template.findReferences(referenceFinder, foundIdentifiers);
		
	}

	
	
	//@Override
	public Template_type getTemplatetype() {
		//return Template_type.TEMPLATEBODY;
		return template.getTemplatetype();
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		template.checkSpecificValue(timestamp, allowOmit);
		
	}

	/**
	 * This function changes the inside template reference.
	 * This function cannot be found in the class TTCNTemplate
	 */
	public void setTemplate(final TTCN3Template t) {
		template = t;
		
	}

	/**
	 * This function gets the inside template reference.
	 * This function cannot be found in the class TTCNTemplate
	 */
	@Override
	public TTCN3Template getTemplate() {
		return template;
	}
	

}
