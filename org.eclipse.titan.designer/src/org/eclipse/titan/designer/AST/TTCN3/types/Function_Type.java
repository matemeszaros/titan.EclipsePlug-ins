/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
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
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.RunsOnScope;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Function_Reference_Value;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * function type (TTCN-3).
 * 
 * @author Kristof Szabados
 * */
public final class Function_Type extends Type {
	private static final String FUNCTIONREFERENCEVALUEEXPECTED = "Reference to a function or external function was expected";
	private static final String RUNSONLESSEXPECTED = "Type `{0}'' does not have a `runs on'' clause, but {1} runs on `{2}''.";
	private static final String INCOMPATIBLERUNSONTYPESERROR =
			"Runs on clause mismatch: type `{0}'' expects component type `{1}'', but {2} runs on `{3}''";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for type `{0}''";

	private static final String FULLNAMEPART1 = ".<formal_parameter_list>";
	private static final String FULLNAMEPART2 = ".<runsOnType>";
	private static final String FULLNAMEPART3 = ".<return_type>";

	private final FormalParameterList formalParList;
	private final Reference runsOnRef;
	private Component_Type runsOnType;
	private final boolean runsOnSelf;
	private final Type returnType;
	private final boolean returnsTemplate;
	private final TemplateRestriction.Restriction_type templateRestriction;

	// stores whether the values of this function can be started or not
	private boolean isStartable;

	public Function_Type(final FormalParameterList formalParList, final Reference runsOnRef, final boolean runsOnSelf, final Type returnType,
			final boolean returnsTemplate, final TemplateRestriction.Restriction_type templateRestriction) {
		this.formalParList = formalParList;
		this.runsOnRef = runsOnRef;
		this.runsOnSelf = runsOnSelf;
		this.returnType = returnType;
		this.returnsTemplate = returnsTemplate;
		this.templateRestriction = templateRestriction;

		formalParList.setFullNameParent(this);
		if (runsOnRef != null) {
			runsOnRef.setFullNameParent(this);
		}
		if (returnType != null) {
			returnType.setFullNameParent(this);
		}
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_FUNCTION;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (formalParList == child) {
			return builder.append(FULLNAMEPART1);
		} else if (runsOnRef == child) {
			return builder.append(FULLNAMEPART2);
		} else if (returnType == child) {
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
		if (returnType != null) {
			returnType.setMyScope(scope);
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

		return Type_type.TYPE_FUNCTION.equals(temp.getTypetype());
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

	/** @return true if the function type returns a template, false otherwise */
	public boolean returnsTemplate() {
		return returnsTemplate;
	}

	public Type getReturnType() {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return returnType;
	}

	@Override
	public Type_type getTypetypeTtcn3() {
		if (isErroneous) {
			return Type_type.TYPE_UNDEFINED;
		}

		return getTypetype();
	}

	/** @return the formal parameterlist of the type */
	public FormalParameterList getFormalParameters() {
		return formalParList;
	}

	/**
	 * Returns the runs on component type of the actual function type.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 *
	 * @return the runs on component type or null if none
	 * */
	public Component_Type getRunsOnType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return runsOnType;
	}

	/** @return true if the type has the runs on self clause, false otherwise */
	public boolean isRunsOnSelf() {
		return runsOnSelf;
	}

	public boolean isStartable(final CompilationTimeStamp timestamp) {
		check(timestamp);
		return isStartable;
	}

	@Override
	public String getTypename() {
		return getFullName();
	}

	@Override
	public String getOutlineIcon() {
		if (returnType == null) {
			return "function.gif";
		}

		return "function_return.gif";
	}

	@Override
	public boolean isComponentInternal(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return runsOnSelf;
	}

	@Override
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_FUNCTION;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		runsOnType = null;
		lastTimeChecked = timestamp;
		isErroneous = false;
		isStartable = true;

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

		formalParList.reset();
		formalParList.check(timestamp, Assignment_type.A_FUNCTION);

		formalParList.checkNoLazyParams();

		isStartable = isStartable && formalParList.getStartability();

		if (returnType != null && location != null) {
			IType returnedType = returnType.getTypeRefdLast(timestamp);
			if (Type_type.TYPE_PORT.equals(returnedType.getTypetype())) {
				location.reportSemanticError("Functions can not return ports");
			}

			isStartable = isStartable && !Type_type.TYPE_DEFAULT.equals(returnedType.getTypetype());
		}

		checkSubtypeRestrictions(timestamp);
	}

	@Override
	public void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		if (runsOnSelf) {
			location.reportSemanticError(MessageFormat.format("Function type `{0}'' with `runs on self'' clause cannot be {1}", getTypename(), operation));
		}
	}

	/**
	 * Checks and returns whether the function is startable.
	 * Reports the appropriate error messages.
	 *
	 * @param timestamp the timestamp of the actual build cycle.
	 * @param errorLocation the location to report the error to, if needed.
	 *
	 * @return true if startable, false otherwise
	 * */
	public boolean checkStartable(final CompilationTimeStamp timestamp, final Location errorLocation) {
		check(timestamp);

		if (isStartable) {
			return true;
		}

		if (runsOnRef == null) {
			errorLocation.reportSemanticError(MessageFormat.format(
					"Functions of type `{0}'' cannot be started on a parallel test component because the type does not have `runs on'' clause",
					getTypename()));
		}

		formalParList.checkStartability(timestamp, "Functions of type", this, errorLocation);

		if (returnType != null && returnType.isComponentInternal(timestamp)) {
			Set<IType> typeSet = new HashSet<IType>();
			String operation = "the return type or embedded in the return type of function type `"
					+ getTypename() + "' if it is started on parallel test component";
			returnType.checkComponentInternal(timestamp, typeSet, operation);
		}

		return false;
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

		Assignment assignment = null;
		switch (last.getValuetype()) {
		case FUNCTION_REFERENCE_VALUE:
			assignment = ((Function_Reference_Value) last).getReferredFunction();
			if (assignment == null) {
				value.setIsErroneous(true);
				return;
			}
			assignment.check(timestamp);
			break;
		case TTCN3_NULL_VALUE:
			return;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			return;
		default:
			value.getLocation().reportSemanticError(FUNCTIONREFERENCEVALUEEXPECTED);
			value.setIsErroneous(true);
			return;
		}

		
		// external functions do not have runs on clauses
		if (assignment instanceof Def_Function) {
			final IType tempRunsOnType = ((Def_Function) assignment).getRunsOnType(timestamp);

			if (tempRunsOnType != null) {
				if (runsOnSelf) {
					//check against the runs on component type of the scope of the value
					Scope valueScope = value.getMyScope();
					if (valueScope == null) {
						value.setIsErroneous(true);
						return;
					}
					RunsOnScope runsOnScope =  valueScope.getScopeRunsOn();
					if (runsOnScope != null) {
						Component_Type componentType = runsOnScope.getComponentType();
						if (!tempRunsOnType.isCompatible(timestamp, componentType, null, null, null)) {
							value.getLocation().reportSemanticError(MessageFormat.format(
									"Runs on clause mismatch: type `{0}'' has a `runs on self'' clause and the current scope expects "
									+ "component type `{1}'', but {2} runs on `{3}''",
									getTypename(), componentType.getTypename(), assignment.getDescription(), tempRunsOnType.getTypename()));
						}
					} else {
						// does not have 'runs on' clause
						// if the value's scope is a component body then check the runs on
						// compatibility using this component type as the scope
						if (valueScope instanceof ComponentTypeBody) {
							ComponentTypeBody body = (ComponentTypeBody) valueScope;
							if (!tempRunsOnType.isCompatible(timestamp, body.getMyType(), null, null, null)) {
								value.getLocation().reportSemanticError(MessageFormat.format(
										"Runs on clause mismatch: type `{0}'' has a `runs on self'' clause and the current component definition "
										+ "is of type `{1}'', but {2} runs on `{3}''",
										getTypename(), body.getMyType().getTypename(), assignment.getDescription(), tempRunsOnType.getTypename()));
							}
						} else {
							value.getLocation().reportSemanticError(MessageFormat.format("Type `{0}'' has a `runs on self'' "
									+ "clause and the current scope does not have a `runs on'' clause, but {1} runs on `{2}''",
									getTypename(), assignment.getDescription(), tempRunsOnType.getTypename()));
						}
					}
				} else {
					if (runsOnRef == null) {
						value.getLocation().reportSemanticError(
								MessageFormat.format(RUNSONLESSEXPECTED, getTypename(), assignment.getAssignmentName(), tempRunsOnType.getTypename()));
						value.setIsErroneous(true);
					} else {
						if (runsOnType != null && !tempRunsOnType.isCompatible(timestamp, runsOnType, null, null, null)) {
							value.getLocation().reportSemanticError(
									MessageFormat.format(INCOMPATIBLERUNSONTYPESERROR, getTypename(), runsOnType.getTypename(), assignment
											.getAssignmentName(), tempRunsOnType.getTypename()));
							value.setIsErroneous(true);
						}
					}
				}
			}
		}

		switch (assignment.getAssignmentType()) {
		case A_FUNCTION:
		case A_EXT_FUNCTION:
			if (returnType != null) {
				value.getLocation().reportSemanticError(MessageFormat.format(
						"Type `{0}'' expects a function or external function that returns a {1} of type `{2}'', but {3} does not have a return type"
						, getTypename(), returnsTemplate ? "template" : "value", returnType.getTypename(), assignment.getDescription()));
			}
			break;
		case A_FUNCTION_RTEMP: {
			Restriction_type restriction = ((Def_Function) assignment).getTemplateRestriction();
			if (!templateRestriction.equals(restriction)) {
				value.getLocation().reportSemanticError(MessageFormat.format(
						"Type `{0}'' expects a function or external function that returns a template with {1} restriction, "
						+ "but {2} returns a template with {3} restriction",
						getTypename(), Restriction_type.TR_NONE.equals(templateRestriction) ? "no" : templateRestriction.getDisplayName(),
								assignment.getDescription(), Restriction_type.TR_NONE.equals(restriction) ? "no" : restriction.getDisplayName()));
			}
			if (returnType != null) {
				IType tempReturnType = assignment.getType(timestamp);
				if (!returnType.isIdentical(timestamp, tempReturnType)) {
					value.getLocation().reportSemanticError(MessageFormat.format(
							"Return type missmatch: type `{0}'' expects a function or external function that returns a {1} of type `{2}'', "
							+ "but {3} returns a template of type `{3}''"
							, getTypename(), returnsTemplate ? "template" : "value", returnType.getTypename(),
									assignment.getDescription(), tempReturnType.getTypename()));
				} else if (!returnsTemplate) {
					value.getLocation().reportSemanticError(MessageFormat.format(
							"Type `{0}'' expects a function or external function that returns a value of type `{1}'', but {2} returns a template"
							, getTypename(), returnType.getTypename(), assignment.getDescription()));
				}
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(
						"Type `{0}'' expects a function or external function without return type, but {1} returns a template of type `{2}''"
						, getTypename(), assignment.getDescription(), assignment.getType(timestamp).getTypename()));
			}
			break;
		}
		case A_EXT_FUNCTION_RTEMP: {
			Restriction_type restriction = ((Def_Extfunction) assignment).getTemplateRestriction();
			if (!templateRestriction.equals(restriction)) {
				value.getLocation().reportSemanticError(MessageFormat.format(
						"Type `{0}'' expects a function or external function that returns a template with {1} restriction, "
						+ "but {2} returns a template with {3} restriction", getTypename(),
						Restriction_type.TR_NONE.equals(templateRestriction) ? "no" : templateRestriction.getDisplayName(),
								assignment.getDescription(), Restriction_type.TR_NONE.equals(restriction) ? "no" : restriction.getDisplayName()));
			}
			if (returnType != null) {
				IType tempReturnType = assignment.getType(timestamp);
				if (!returnType.isIdentical(timestamp, tempReturnType)) {
					value.getLocation().reportSemanticError(MessageFormat.format(
							"Return type missmatch: type `{0}'' expects a function or external function that returns a {1} of type `{2}'', "
							+ "but {3} returns a template of type `{3}''"
							, getTypename(), returnsTemplate ? "template" : "value", returnType.getTypename(),
									assignment.getDescription(), tempReturnType.getTypename()));
				} else if (!returnsTemplate) {
					value.getLocation().reportSemanticError(MessageFormat.format(
							"Type `{0}'' expects a function or external function that returns a value of type `{1}'', but {2} returns a template"
							, getTypename(), returnType.getTypename(), assignment.getDescription()));
				}
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(
						"Type `{0}'' expects a function or external function without return type, but {1} returns a template of type `{2}''"
						, getTypename(), assignment.getDescription(), assignment.getType(timestamp).getTypename()));
			}
			break;
		}
		case A_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RVAL:
			if (returnType != null) {
				IType tempReturnType = assignment.getType(timestamp);
				if (!returnType.isIdentical(timestamp, tempReturnType)) {
					value.getLocation().reportSemanticError(MessageFormat.format(
							"Return type missmatch: type `{0}'' expects a function or external function that returns a {1} of type `{2}'',"
							+ " but {3} returns a value of type `{3}''"
							, getTypename(), returnsTemplate ? "template" : "value", returnType.getTypename(),
									assignment.getDescription(), tempReturnType.getTypename()));
				} else if (returnsTemplate) {
					value.getLocation().reportSemanticError(MessageFormat.format(
							"Type `{0}'' expects a function or external function that returns a template of type `{1}'', but {2} returns a value"
							, getTypename(), returnType.getTypename(), assignment.getDescription()));
				}
			} else {
				value.getLocation().reportSemanticError(MessageFormat.format(
						"Type `{0}'' expects a function or external function without return type, but {1} returns a value of type `{2}''"
						, getTypename(), assignment.getDescription(), assignment.getType(timestamp).getTypename()));
			}
			break;
		default:
			break;
		}

		if (valueCheckingOptions.sub_check) {
			//there is no parent type to check
			if (subType != null) {
				subType.checkThisValue(timestamp, last);
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
		return builder.append("function type");
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

		if (returnType != null) {
			returnType.updateSyntax(reparser, false);
			reparser.updateLocation(returnType.getLocation());
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
		if (runsOnType != null) {
			runsOnType.findReferences(referenceFinder, foundIdentifiers);
		}
		if (returnType != null) {
			returnType.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (formalParList!=null && !formalParList.accept(v)) {
			return false;
		}
		if (runsOnRef!=null && !runsOnRef.accept(v)) {
			return false;
		}
		if (runsOnType!=null && !runsOnType.accept(v)) {
			return false;
		}
		if (returnType!=null && !returnType.accept(v)) {
			return false;
		}
		return true;
	}
}
