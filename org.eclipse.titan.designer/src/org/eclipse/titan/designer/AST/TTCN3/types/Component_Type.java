/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Const;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ExternalConst;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * component type (TTCN-3).
 * 
 * @author Kristof Szabados
 * */
public final class Component_Type extends Type {
	private static final String COMPONENT_GIF = "component.gif";
	private static final String COMPONENTVALUEEXPECTED = "Component value was expected";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for type `{0}''";
	private static final String INVALIDSUBREFERENCE = "Referencing fields of a component is not allowed";
	

	private static final String[] SIMPLE_COMPONENT_PROPOSALS = new String[] {"alive", "create;", "create alive;", "done", "kill;", "killed",
			"running", "stop;" };
	private static final String[] ANY_COMPONENT_PROPOSALS = new String[] {"running", "alive", "done", "killed" };
	private static final String[] ALL_COMPONENT_PROPOSALS = new String[] {"running", "alive", "done", "killed", "stop;", "kill;" };

	private final ComponentTypeBody componentBody;

	public Component_Type(final ComponentTypeBody component) {
		this.componentBody = component;

		componentBody.setFullNameParent(this);
		componentBody.setMyType(this);
	}

	@Override
	public Type_type getTypetype() {
		return Type_type.TYPE_COMPONENT;
	}

	/**
	 * @return the body of this component type.
	 * */
	public ComponentTypeBody getComponentBody() {
		return componentBody;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		componentBody.setMyScope(scope);
	}

	@Override
	public void setAttributeParentPath(final WithAttributesPath parent) {
		super.setAttributeParentPath(parent);
		componentBody.setAttributeParentPath(withAttributesPath);
	}

	@Override
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("component");
	}

	@Override
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		IType temp = otherType.getTypeRefdLast(timestamp);

		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp) || this == temp) {
			return true;
		}

		return Type_type.TYPE_COMPONENT.equals(temp.getTypetype()) && componentBody.isCompatible(timestamp, ((Component_Type) temp).componentBody);
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

	@Override
	public String getTypename() {
		return getFullName();
	}

	@Override
	public String getOutlineIcon() {
		return COMPONENT_GIF;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		parseAttributes(timestamp);

		componentBody.check(timestamp);

		lastTimeChecked = timestamp;
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

		switch (last.getValuetype()) {
		case TTCN3_NULL_VALUE:
			value.setValuetype(timestamp, Value_type.EXPRESSION_VALUE);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(COMPONENTVALUEEXPECTED);
			value.setIsErroneous(true);
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
					MessageFormat.format(INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(),
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

	/**
	 * Searches and adds a completion proposal to the provided collector if a
	 * valid one is found.
	 * <p>
	 * handles the following proposals:
	 * <ul>
	 * <li>create, create alive, create(name), create(name) alive
	 * <li>start(function_instance)
	 * <li>stop
	 * <li>kill
	 * <li>alive
	 * <li>running
	 * <li>done
	 * <li>killed
	 * </ul>
	 *
	 * @param propCollector the proposal collector to add the proposal to, and
	 *            used to get more information
	 * @param i index, used to identify which element of the reference (used by
	 *            the proposal collector) should be checked for completions.
	 * */
	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= i || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		componentBody.addProposal(propCollector, i);

		if (subrefs.size() == i + 1) {
			for (String proposal : SIMPLE_COMPONENT_PROPOSALS) {
				propCollector.addProposal(proposal, proposal, ImageCache.getImage(getOutlineIcon()), "");
			}
			propCollector.addTemplateProposal("create", new Template("create( name )", "", propCollector.getContextIdentifier(),
					"create( ${name} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			propCollector.addTemplateProposal("create", new Template("create( name ) alive", "", propCollector.getContextIdentifier(),
					"create( ${name} ) alive;", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			propCollector.addTemplateProposal("create", new Template("create( name, location )", "", propCollector.getContextIdentifier(),
					"create( ${name}, ${location} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			propCollector.addTemplateProposal("create", new Template("create( name, location ) alive", "", propCollector.getContextIdentifier(),
					"create( ${name}, ${location} ) alive;", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			propCollector.addTemplateProposal("start", new Template("start( function name )", "", propCollector.getContextIdentifier(),
					"start( ${functionName} );", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
		}
	}

	public static void addAnyorAllProposal(final ProposalCollector propCollector, final int i) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (i != 0 || subrefs.isEmpty() || Subreference_type.arraySubReference.equals(subrefs.get(0).getReferenceType())) {
			return;
		}

		String fakeModuleName = propCollector.getReference().getModuleIdentifier().getDisplayName();

		if ("any component".equals(fakeModuleName)) {
			for (String proposal : ANY_COMPONENT_PROPOSALS) {
				propCollector.addProposal(proposal, proposal, ImageCache.getImage(COMPONENT_GIF), "");
			}
		} else if ("all component".equals(fakeModuleName)) {
			for (String proposal : ALL_COMPONENT_PROPOSALS) {
				propCollector.addProposal(proposal, proposal, ImageCache.getImage(COMPONENT_GIF), "");
			}
		}
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() <= i || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		componentBody.addDeclaration(declarationCollector, i);
	}

	@Override
	public Object[] getOutlineChildren() {
		return componentBody.getDefinitions().toArray();
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			boolean handled = false;

			if (componentBody != null) {
				if (reparser.envelopsDamage(componentBody.getLocation())) {
					componentBody.updateSyntax(reparser, true);
					reparser.updateLocation(componentBody.getLocation());
					handled = true;
				}
			}

			if (subType != null) {
				subType.updateSyntax(reparser, false);
				handled = true;
			}

			if (handled) {
				return;
			}

			throw new ReParseException();
		}

		componentBody.updateSyntax(reparser, false);
		reparser.updateLocation(componentBody.getLocation());

		if (subType != null) {
			subType.updateSyntax(reparser, false);
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	/**
	 * Checks if the provided value is a reference to a component or not.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @param value the value to be checked
	 * @param expected_value the value kind expected from the actual parameter.
	 * */
	public static void checkExpressionOperandComponentRefernce(final CompilationTimeStamp timestamp,
			final IValue value, final String operationName) {
		switch (value.getValuetype()) {
		case EXPRESSION_VALUE: {
			Expression_Value expression = (Expression_Value) value;
			if (Operation_type.APPLY_OPERATION.equals(expression.getOperationType())) {
				IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				IValue last = value.getValueRefdLast(timestamp, chain);
				chain.release();
				if (last == null || last.getIsErroneous(timestamp)) {
					value.setIsErroneous(true);
					return;
				}

				IType type = last.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
				if (type == null) {
					value.setIsErroneous(true);
					return;
				}

				type = type.getTypeRefdLast(timestamp);
				if( type.getIsErroneous(timestamp)) {
					value.setIsErroneous(true);
					return; //don't let spread an earlier mistake
				}
				if (!Type_type.TYPE_COMPONENT.equals(type.getTypetype())) {
					value.getLocation().reportSemanticError(MessageFormat.format(
							"The first operand of operation `{0}'': Type mismatch: component reference was expected instead of `{1}''",
							operationName, type.getTypename()));
					value.setIsErroneous(true);
					return;
				}
			}
			break; }
		case REFERENCED_VALUE: {
			Reference reference = ((Referenced_Value) value).getReference();
			Assignment assignment = reference.getRefdAssignment(timestamp, true);
			if (assignment == null) {
				value.setIsErroneous(true);
				return;
			}


			switch (assignment.getAssignmentType()) {
			case A_CONST: {
				IType type = ((Def_Const) assignment).getType(timestamp).getFieldType(
						timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
				if (type == null) {
					value.setIsErroneous(true);
					return;
				}
				type = type.getTypeRefdLast(timestamp);
				
				if( type.getIsErroneous(timestamp)) {
					value.setIsErroneous(true);
					return; //don't let spread an earlier mistake
				}
				
				if (!Type_type.TYPE_COMPONENT.equals(type.getTypetype())) {
					reference.getLocation().reportSemanticError(MessageFormat.format(
							"The first operand of operation `{0}'': Type mismatch: component reference was expected instead of `{1}''",
							operationName, type.getTypename()));
					value.setIsErroneous(true);
					return;
				}

				IValue tempValue = ((Def_Const) assignment).getValue();
				if (tempValue == null) {
					return;
				}
				IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				tempValue = tempValue.getReferencedSubValue(timestamp, reference, 1, chain);
				chain.release();
				if (tempValue == null) {
					return;
				}
				chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				tempValue = tempValue.getValueRefdLast(timestamp, chain);
				chain.release();
				if (Value_type.TTCN3_NULL_VALUE.equals(tempValue.getValuetype())) {
					reference.getLocation().reportSemanticError(MessageFormat.format(
							"The first operand of operation `{0}'' refers to the `null'' component reference", operationName));
					value.setIsErroneous(true);
					return;
				}
				if (!Value_type.EXPRESSION_VALUE.equals(tempValue.getValuetype())) {
					return;
				}
				switch (((Expression_Value) tempValue).getOperationType()) {
				case MTC_COMPONENT_OPERATION:
					reference.getLocation().reportSemanticError(MessageFormat.format(
							"The first operand of operation `{0}'' refers to the component reference of the `mtc''", operationName));
					value.setIsErroneous(true);
					return;
				case COMPONENT_NULL_OPERATION:
					reference.getLocation().reportSemanticError(MessageFormat.format(
							"The first operand of operation `{0}'' refers to the `null'' component reference", operationName));
					value.setIsErroneous(true);
					return;
				case SYSTEM_COMPONENT_OPERATION:
					reference.getLocation().reportSemanticError(MessageFormat.format(
							"The first operand of operation `{0}'' refers to the component reference of the `system''", operationName));
					value.setIsErroneous(true);
					return;
				default:
					break;
				}
				break; }
			case A_EXT_CONST: {
				IType type = ((Def_ExternalConst) assignment).getType(timestamp).getFieldType(
						timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
				if (type == null) {
					value.setIsErroneous(true);
					return;
				}
				type = type.getTypeRefdLast(timestamp);
				
				if( type.getIsErroneous(timestamp)) {
					value.setIsErroneous(true);
					return; //don't let spread an earlier mistake
				}
				
				if (!Type_type.TYPE_COMPONENT.equals(type.getTypetype())) {
					reference.getLocation().reportSemanticError(MessageFormat.format(
							"The first operand of operation `{0}'': Type mismatch: component reference was expected instead of `{1}''",
							operationName, type.getTypename()));
					value.setIsErroneous(true);
					return;
				}
				break; }
			case A_MODULEPAR: {
				IType type = ((Def_ModulePar) assignment).getType(timestamp).getFieldType(
						timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
				if (type == null) {
					value.setIsErroneous(true);
					return;
				}
				type = type.getTypeRefdLast(timestamp);
				if( type.getIsErroneous(timestamp)) {
					value.setIsErroneous(true);
					return; //don't let spread an earlier mistake
				}
				if (!Type_type.TYPE_COMPONENT.equals(type.getTypetype())) {
					reference.getLocation().reportSemanticError(MessageFormat.format(
							"The first operand of operation `{0}'': Type mismatch: component reference was expected instead of `{1}''",
							operationName, type.getTypename()));
					value.setIsErroneous(true);
					return;
				}
				break; }
			case A_VAR: {
				IType type = ((Def_Var) assignment).getType(timestamp).getFieldType(
						timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
				if (type == null) {
					value.setIsErroneous(true);
					return;
				}
				type = type.getTypeRefdLast(timestamp);
				
				if( type.getIsErroneous(timestamp)) {
					value.setIsErroneous(true);
					return; //don't let spread an earlier mistake
				}
				
				if (!Type_type.TYPE_COMPONENT.equals(type.getTypetype())) {
					reference.getLocation().reportSemanticError(MessageFormat.format(
							"The first operand of operation `{0}'': Type mismatch: component reference was expected instead of `{1}''",
							operationName, type.getTypename()));
					value.setIsErroneous(true);
					return;
				}
				break; }
			case A_FUNCTION_RVAL: {
				IType type = ((Def_Function) assignment).getType(timestamp).getFieldType(
						timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
				if (type == null) {
					value.setIsErroneous(true);
					return;
				}
				type = type.getTypeRefdLast(timestamp);
				
				if( type.getIsErroneous(timestamp)) {
					value.setIsErroneous(true);
					return; //don't let spread an earlier mistake
				}
				
				if (!Type_type.TYPE_COMPONENT.equals(type.getTypetype())) {
					reference.getLocation().reportSemanticError(MessageFormat.format(
							"The first operand of operation `{0}'': Type mismatch: component reference was expected instead of `{1}''",
							operationName, type.getTypename()));
					value.setIsErroneous(true);
					return;
				}
				break; }
			case A_EXT_FUNCTION_RVAL: {
				IType type = ((Def_Extfunction) assignment).getType(timestamp).getFieldType(
						timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
				if (type == null) {
					value.setIsErroneous(true);
					return;
				}
				type = type.getTypeRefdLast(timestamp);
				
				if( type.getIsErroneous(timestamp)) {
					value.setIsErroneous(true);
					return; //don't let spread an earlier mistake
				}
				
				if (!Type_type.TYPE_COMPONENT.equals(type.getTypetype())) {
					reference.getLocation().reportSemanticError(MessageFormat.format(
							"The first operand of operation `{0}'': Type mismatch: component reference was expected instead of `{1}''",
							operationName, type.getTypename()));
					value.setIsErroneous(true);
					return;
				}
				break; }
			case A_PAR_VAL:
			case A_PAR_VAL_IN:
			case A_PAR_VAL_OUT:
			case A_PAR_VAL_INOUT: {
				IType type = ((FormalParameter) assignment).getType(timestamp).getFieldType(
						timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
				if (type == null) {
					value.setIsErroneous(true);
					return;
				}
				type = type.getTypeRefdLast(timestamp);
				
				if( type.getIsErroneous(timestamp)) {
					value.setIsErroneous(true);
					return; //don't let spread an earlier mistake
				}
				if (!Type_type.TYPE_COMPONENT.equals(type.getTypetype())) {
					reference.getLocation().reportSemanticError(MessageFormat.format(
							"The first operand of operation `{0}'': Type mismatch: component reference was expected instead of `{1}''",
							operationName, type.getTypename()));
					value.setIsErroneous(true);
					return;
				}
				break; }
			default:
				reference.getLocation().reportSemanticError(MessageFormat.format(
						"The first operand of operation `{0}'' should be a component reference instead of `{1}''",
						operationName, assignment.getDescription()));
				value.setIsErroneous(true);
				return;
			}
			break; }
		default:
			// the error was already reported if possible.
			return;
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (componentBody != null) {
			componentBody.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (componentBody!=null && !componentBody.accept(v)) {
			return false;
		}
		return true;
	}
}
