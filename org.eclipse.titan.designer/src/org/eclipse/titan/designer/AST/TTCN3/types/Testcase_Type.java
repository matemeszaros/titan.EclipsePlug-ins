/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.RunsOnScope;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Testcase_Reference_Value;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * testcase type (TTCN-3).
 * 
 * @author Kristof Szabados
 * */
public final class Testcase_Type extends Type {
	private static final String TESTCASEREFERENCEVALUEEXPECTED = "Reference to a testcase was expected";
	private static final String INCOMPATIBLERUNSONTYPESERROR =
			"Runs on clause mismatch: type `{0}'' expects component type `{1}'', but {2} runs on `{3}''";
	private static final String SYSTEMCLAUSEMISMATCHERROR =
			"System clause mismatch: testcase type `{0}'' expects component type `{1}'', but {2} has `{3}''";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for type `{0}''";

	private static final String FULLNAMEPART1 = ".<formal_parameter_list>";
	private static final String FULLNAMEPART2 = ".<runsOnType>";
	private static final String FULLNAMEPART3 = ".<systemType>";

	private final FormalParameterList formalParList;
	private final Reference runsOnRef;
	private Component_Type runsOnType;
	private final Reference systemRef;
	private Component_Type systemType;

	public Testcase_Type(final FormalParameterList formalParList, final Reference runsOnRef, final Reference systemRef) {
		this.formalParList = formalParList;
		this.runsOnRef = runsOnRef;
		this.systemRef = systemRef;

		formalParList.setFullNameParent(this);
		if (runsOnRef != null) {
			runsOnRef.setFullNameParent(this);
		}
		if (systemRef != null) {
			systemRef.setFullNameParent(this);
		}
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_TESTCASE;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (formalParList == child) {
			return builder.append(FULLNAMEPART1);
		} else if (runsOnRef == child) {
			return builder.append(FULLNAMEPART2);
		} else if (systemRef == child) {
			return builder.append(FULLNAMEPART3);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		formalParList.setMyScope(scope);
		if (runsOnRef != null) {
			runsOnRef.setMyScope(scope);
		}
		if (systemRef != null) {
			systemRef.setMyScope(scope);
		}
		scope.addSubScope(formalParList.getLocation(), formalParList);
	}

	@Override
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		IType temp = otherType.getTypeRefdLast(timestamp);
		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		return Type_type.TYPE_TESTCASE.equals(temp.getTypetype());
	}

	@Override
	public boolean isIdentical(final CompilationTimeStamp timestamp, final IType type) {
		check(timestamp);
		type.check(timestamp);
		IType temp = type.getTypeRefdLast(timestamp);
		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		return this == temp;
	}

	@Override
	public Type_type getTypetypeTtcn3() {
		if (isErroneous) {
			return Type_type.TYPE_UNDEFINED;
		}

		return getTypetype();
	}

	/**
	 * Returns the runs on component type of the actual testcase type.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 *
	 * @return the runs on component type or null if none
	 * */
	public Component_Type getRunsOnType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return runsOnType;
	}

	@Override
	public String getTypename() {
		return getFullName();
	}

	@Override
	public String getOutlineIcon() {
		return "testcase.gif";
	}

	/** @return the formal parameterlist of the type */
	public FormalParameterList getFormalParameters() {
		return formalParList;
	}
	
	@Override
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_TESTCASE;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		runsOnType = null;
		systemType = null;
		lastTimeChecked = timestamp;
		isErroneous = false;

		parseAttributes(timestamp);

		if (runsOnRef != null) {
			runsOnType = runsOnRef.chkComponentypeReference(timestamp);
			if (runsOnType != null) {
				Scope formalParlistPreviosScope = formalParList.getParentScope();
				if (formalParlistPreviosScope instanceof RunsOnScope && ((RunsOnScope) formalParlistPreviosScope).getParentScope() == myScope) {
					((RunsOnScope) formalParlistPreviosScope).setComponentType(runsOnType);
				} else {
					Scope tempScope = new RunsOnScope(runsOnType, myScope);
					formalParList.setMyScope(tempScope);
				}
			}
		}

		if (systemRef != null) {
			systemType = systemRef.chkComponentypeReference(timestamp);
		}

		formalParList.reset();
		formalParList.check(timestamp, Assignment_type.A_TESTCASE);
		
		formalParList.checkNoLazyParams();

		checkSubtypeRestrictions(timestamp);
	}

	@Override
	public void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
		super.checkThisValue(timestamp, value, valueCheckingOptions);

		IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
		if (last == null || last.getIsErroneous(timestamp)) {
			return;
		}

		// already handled ones
		switch (value.getValuetype()) {
		case OMIT_VALUE:
		case REFERENCED_VALUE:
			return;
		case UNDEFINED_LOWERIDENTIFIER_VALUE:
			if (Value_type.REFERENCED_VALUE.equals(last.getValuetype())) {
				return;
			}
			break;
		default:
			break;
		}

		Def_Testcase testcase = null;
		switch (last.getValuetype()) {
		case TESTCASE_REFERENCE_VALUE:
			testcase = ((Testcase_Reference_Value) last).getReferedTestcase();
			if (testcase == null) {
				setIsErroneous(true);
				return;
			}
			testcase.check(timestamp);
			break;
		case TTCN3_NULL_VALUE:
			return;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			return;
		default:
			value.getLocation().reportSemanticError(TESTCASEREFERENCEVALUEEXPECTED);
			value.setIsErroneous(true);
			return;
		}

		Component_Type temporalRunsOnType = testcase.getRunsOnType(timestamp);
		if (temporalRunsOnType != null) {
			if (runsOnType != null && !temporalRunsOnType.isCompatible(timestamp, runsOnType, null, null, null)) {
				value.getLocation().reportSemanticError(
						MessageFormat.format(INCOMPATIBLERUNSONTYPESERROR, getTypename(), runsOnType.getTypename(),
								testcase.getAssignmentName(), temporalRunsOnType.getTypename()));
				value.setIsErroneous(true);
			}
		}

		Component_Type temporalSystemType = testcase.getSystemType(timestamp);
		if (temporalSystemType == null) {
			temporalSystemType = temporalRunsOnType;
		}

		if (systemRef == null) {
			if (temporalSystemType != null && runsOnType != null && !temporalSystemType.isCompatible(timestamp, runsOnType, null, null, null)) {
				value.getLocation().reportSemanticError(
						MessageFormat.format(SYSTEMCLAUSEMISMATCHERROR, getTypename(), runsOnType.getTypename(), testcase.getAssignmentName(),
								temporalSystemType.getTypename()));
				value.setIsErroneous(true);
			}
		} else {
			if (temporalSystemType != null && systemType != null && !temporalSystemType.isCompatible(timestamp, systemType, null, null, null)) {
				value.getLocation().reportSemanticError(
						MessageFormat.format(SYSTEMCLAUSEMISMATCHERROR, getTypename(), systemType.getTypename(), testcase.getAssignmentName(),
								temporalSystemType.getTypename()));
				value.setIsErroneous(true);
			}
		}

		if (valueCheckingOptions.sub_check) {
			//there is no parent type to check
			if (subType != null) {
				subType.checkThisValue(timestamp, value);
			}
		}
	}

	@Override
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit) {
		registerUsage(template);
		template.setMyGovernor(this);

		template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(MessageFormat.format(LENGTHRESTRICTIONNOTALLOWED, getTypename()));
		}
	}

	@Override
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {
		List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return this;
		}

		ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDSUBREFERENCE, getTypename()));
			return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(),
							getTypename()));
			return null;
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference).getId().getDisplayName(),
							getTypename()));
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("testcase type");
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() != i + 1 || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		propCollector.addTemplateProposal("apply", new Template("apply( parameters )", "", propCollector.getContextIdentifier(),
				"apply( ${parameters} )", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (formalParList != null) {
			formalParList.updateSyntax(reparser, false);
			reparser.updateLocation(formalParList.getLocation());
		}

		if (runsOnRef != null) {
			runsOnRef.updateSyntax(reparser, false);
			reparser.updateLocation(runsOnRef.getLocation());
		}

		if (systemRef != null) {
			systemRef.updateSyntax(reparser, false);
			reparser.updateLocation(systemRef.getLocation());
		}

		if (subType != null) {
			subType.updateSyntax(reparser, false);
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}
	
	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (formalParList != null) {
			formalParList.findReferences(referenceFinder, foundIdentifiers);
		}
		if (runsOnRef != null) {
			runsOnRef.findReferences(referenceFinder, foundIdentifiers);
		}
		if (systemRef != null) {
			systemRef.findReferences(referenceFinder, foundIdentifiers);
		}
	}
	
	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (formalParList!=null && !formalParList.accept(v)) {
			return false;
		}
		if (runsOnRef!=null && !runsOnRef.accept(v)) {
			return false;
		}
		if (systemRef!=null && !systemRef.accept(v)) {
			return false;
		}
		return true;
	}
}
