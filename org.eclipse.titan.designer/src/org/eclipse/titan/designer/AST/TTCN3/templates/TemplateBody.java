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
	
	public TemplateBody(TTCN3Template t){
		template = t;
	}
	
	
	
	
	@Override
	public void copyGeneralProperties(ITTCN3Template original) {
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
	public void setMyGovernor(IType governor) {
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
	public ITTCN3Template setLoweridToReference(CompilationTimeStamp timestamp) {
		return template.setLoweridToReference(timestamp);
	}

	@Override
	public void setLengthRestriction(LengthRestriction lengthRestriction) {
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
	public void setBaseTemplate(ITTCN3Template baseTemplate) {
		template.setBaseTemplate(baseTemplate);		
	}

	@Override
	public Completeness_type getCompletenessConditionSeof(
			CompilationTimeStamp timestamp, boolean incompleteAllowed) {
		return template.getCompletenessConditionSeof(timestamp, incompleteAllowed);
	}

	@Override
	public Completeness_type getCompletenessConditionChoice(
			CompilationTimeStamp timestamp, boolean incompleteAllowed,
			Identifier fieldName) {
		 
		return template.getCompletenessConditionChoice(timestamp, incompleteAllowed, fieldName);
	}

	@Override
	public TTCN3Template getTemplateReferencedLast(
			CompilationTimeStamp timestamp) {
		return template.getTemplateReferencedLast(timestamp);
	}

	@Override
	public TTCN3Template getTemplateReferencedLast(
			CompilationTimeStamp timestamp, IReferenceChain referenceChain) {
			return template.getTemplateReferencedLast(timestamp,referenceChain);
	}

	@Override
	public ITTCN3Template setTemplatetype(CompilationTimeStamp timestamp,
			Template_type newType) {
		return template.setTemplatetype(timestamp, newType);
	}

	@Override
	public Type_type getExpressionReturntype(CompilationTimeStamp timestamp,
			Expected_Value_type expectedValue) {
		
		return template.getExpressionReturntype(timestamp, expectedValue);
	}

	@Override
	public IType getExpressionGovernor(CompilationTimeStamp timestamp,
			Expected_Value_type expectedValue) {
		return template.getExpressionGovernor(timestamp, expectedValue);
	}

	@Override
	public void checkRecursions(CompilationTimeStamp timestamp,
			IReferenceChain referenceChain) {
		template.checkRecursions(timestamp, referenceChain);	
	}

	@Override
	public ITTCN3Template getReferencedSubTemplate(
			CompilationTimeStamp timestamp, Reference reference,
			IReferenceChain referenceChain) {
		return template.getReferencedSubTemplate(timestamp, reference, referenceChain);
	}

	@Override
	public boolean isValue(CompilationTimeStamp timestamp) {
		return template.isValue(timestamp);
	}

	@Override
	public IValue getValue() {
		return template.getValue();
	}

	@Override
	public void checkThisTemplateGeneric(CompilationTimeStamp timestamp,
			IType type, boolean isModified, boolean allowOmit,
			boolean allowAnyOrOmit, boolean subCheck, boolean implicitOmit) {
		template.checkThisTemplateGeneric(
				timestamp, type, isModified, allowOmit, allowAnyOrOmit, subCheck, implicitOmit);
	}

	@Override
	public void checkRestrictionCommon(final CompilationTimeStamp timestamp, String definitionName,
			Restriction_type templateRestriction, final Location usageLocation) {
		template.checkRestrictionCommon(timestamp, definitionName, templateRestriction, usageLocation);	
	}

	@Override
	public boolean checkValueomitRestriction(CompilationTimeStamp timestamp,
			String definitionName, boolean omitAllowed, final Location usageLocation) {
		return template.checkValueomitRestriction(timestamp, definitionName, omitAllowed, usageLocation);
	}

	@Override
	public boolean chkRestrictionNamedListBaseTemplate(
			CompilationTimeStamp timestamp, String definitionName,
			Set<String> checkedNames, int neededCheckedCnt, final Location usageLocation) {
			return template.chkRestrictionNamedListBaseTemplate(timestamp, definitionName, checkedNames, neededCheckedCnt, usageLocation);
	}

	@Override
	public boolean checkPresentRestriction(CompilationTimeStamp timestamp,
			String definitionName, final Location usageLocation) {
		return template.checkPresentRestriction(timestamp, definitionName, usageLocation);
	}

	@Override
	public boolean getIsErroneous(CompilationTimeStamp timestamp) {
		return template.getIsErroneous(timestamp);
	}

	@Override
	public void setIsErroneous(boolean isErroneous) {
		template.setIsErroneous(isErroneous);
		
	}

	@Override
	public Setting_type getSettingtype() {
		return template.getSettingtype();
	}

	@Override
	public void setLocation(Location location) {
		if(location !=null) {
		  template.setLocation(location);
		}
		
	}

	@Override
	public boolean isAsn() {
		return template.isAsn();
	}

	@Override
	public void setMyScope(Scope scope) {
		template.setMyScope(scope);
	}

	@Override
	public Scope getMyScope() {
		return template.getMyScope();
	}

	@Override
	public void setFullNameParent(INamedNode nameParent) {
		template.setFullNameParent(nameParent);	
	}

	@Override
	public StringBuilder getFullName(INamedNode child) {
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
	public boolean accept(ASTVisitor v) {
		return template.accept(v);
	}

	@Override
	public void updateSyntax(TTCN3ReparseUpdater reparser, boolean isDamaged)
			throws ReParseException {
		template.updateSyntax(reparser, isDamaged);
		
	}

	@Override
	public Location getLocation() {
		return template.getLocation();
	}

	@Override
	public void findReferences(ReferenceFinder referenceFinder,
			List<Hit> foundIdentifiers) {
		template.findReferences(referenceFinder, foundIdentifiers);
		
	}

	
	
	//@Override
	public Template_type getTemplatetype() {
		//return Template_type.TEMPLATEBODY;
		return template.getTemplatetype();
	}

	@Override
	public void checkSpecificValue(CompilationTimeStamp timestamp, boolean allowOmit) {
		template.checkSpecificValue(timestamp, allowOmit);
		
	}

	/**
	 * This function changes the inside template reference.
	 * This function cannot be found in the class TTCNTemplate
	 */
	public void setTemplate(TTCN3Template t) {
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
