/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Sequence_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValues;
import org.eclipse.titan.designer.AST.TTCN3.values.Sequence_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represent a list of named templates.
 * 
 * @author Kristof Szabados
 * */
public final class Named_Template_List extends TTCN3Template {
	private static final String ALLARENOTUSED = "All elements of value list notation for type `{0}'' are not used symbols (`-'')";
	private static final String TOOMANYELEMENTS = "Too many elements in value list notation for type `{0}'': {1} was expected instead of {2}";

	private final NamedTemplates namedTemplates;

	// cache storing the value form of this if already created, or null
	private Sequence_Value asValue = null;

	public Named_Template_List(final NamedTemplates namedTemplates) {
		this.namedTemplates = namedTemplates;

		namedTemplates.setFullNameParent(this);
	}

	/**
	 * function used to convert a template written without naming the fields into a template with all field names provided.
	 * 
	 * @param timestamp the timestamp of the actual build cycle
	 * @param other the template to be converted
	 * */
	public static Named_Template_List convert(final CompilationTimeStamp timestamp, final Template_List other) {
		IType lastType = other.getMyGovernor().getTypeRefdLast(timestamp);
		int nofTemplates = other.getNofTemplates();
		int nofTypeComponents = 0;
		switch (lastType.getTypetype()) {
		case TYPE_TTCN3_SEQUENCE:
			nofTypeComponents = ((TTCN3_Sequence_Type) lastType).getNofComponents();
			break;
		case TYPE_ASN1_SEQUENCE:
			nofTypeComponents = ((ASN1_Sequence_Type) lastType).getNofComponents(timestamp);
			break;
		case TYPE_SIGNATURE:
			nofTypeComponents = ((Signature_Type) lastType).getNofParameters();
			break;
		case TYPE_TTCN3_SET:
			nofTypeComponents = ((TTCN3_Set_Type) lastType).getNofComponents();
			break;
		case TYPE_ASN1_SET:
			nofTypeComponents = ((ASN1_Set_Type) lastType).getNofComponents(timestamp);
			break;
		default:
		}

		if (nofTemplates > nofTypeComponents) {
			other.getLocation().reportSemanticError(MessageFormat.format(TOOMANYELEMENTS, lastType.getTypename(), nofTypeComponents, nofTemplates));
			other.setIsErroneous(true);
		}

		int upperLimit;
		boolean allNotUsed;
		if (nofTemplates <= nofTypeComponents) {
			upperLimit = nofTemplates;
			allNotUsed = true;
		} else {
			upperLimit = nofTypeComponents;
			allNotUsed = false;
		}

		NamedTemplates namedTemplates = new NamedTemplates();
		for (int i = 0; i < upperLimit; i++) {
			ITemplateListItem template = other.getTemplateByIndex(i);
			if (!Template_type.TEMPLATE_NOTUSED.equals(template.getTemplatetype())) {
				allNotUsed = false;
				Identifier identifier = null;
				switch (lastType.getTypetype()) {
				case TYPE_TTCN3_SEQUENCE:
					identifier = ((TTCN3_Sequence_Type) lastType).getComponentIdentifierByIndex(i);
					break;
				case TYPE_ASN1_SEQUENCE:
					identifier = ((ASN1_Sequence_Type) lastType).getComponentIdentifierByIndex(i);
					break;
				case TYPE_SIGNATURE:
					identifier = ((Signature_Type) lastType).getParameterIdentifierByIndex(i);
					break;
				case TYPE_TTCN3_SET:
					identifier = ((TTCN3_Set_Type) lastType).getComponentIdentifierByIndex(i);
					break;
				case TYPE_ASN1_SET:
					identifier = ((ASN1_Set_Type) lastType).getComponentIdentifierByIndex(i);
					break;
				default:
					// can not reach here because of a
					// previous check
					break;
				}

				if (identifier != null) {
					NamedTemplate namedTemplate = new NamedTemplate(identifier.newInstance(), template);
					namedTemplate.setLocation(template.getLocation());
					namedTemplates.addTemplate(namedTemplate);
				}
			}
		}

		namedTemplates.setMyScope(other.getMyScope());
		namedTemplates.setFullNameParent(other);

		if (allNotUsed && nofTemplates > 0 && !Type_type.TYPE_SIGNATURE.equals(lastType.getTypetype())) {
			other.getLocation().reportSemanticWarning(MessageFormat.format(ALLARENOTUSED, lastType.getTypename()));
			other.setIsErroneous(true);
		}
		
		Named_Template_List target = new Named_Template_List(namedTemplates);
		target.copyGeneralProperties(other);

		return target;
	}

	public void addNamedValue(final NamedTemplate template) {
		if (template != null) {
			namedTemplates.addTemplate(template);
			template.setMyScope(myScope);
		}
	}

	/**
	 * Checks if there is a named template in the list, with a given name.
	 * 
	 * @param name
	 *                the name to search for.
	 * @return true if the list has a template with the provided name, false
	 *         otherwise.
	 */
	public boolean hasNamedTemplate(final Identifier name) {
		if (name == null || namedTemplates == null) {
			return false;
		}

		return namedTemplates.hasNamedTemplateWithName(name);
	}

	/**
	 * Returns the named template referred by the identifier.
	 * 
	 * @param name
	 *                the name to identify the namedTemplate
	 * 
	 * @return the named template with the provided name, or null if there
	 *         is none with that name.
	 * */
	public NamedTemplate getNamedTemplate(final Identifier name) {
		if (name == null || namedTemplates == null) {
			return null;
		}

		return namedTemplates.getNamedTemplateByName(name);
	}

	@Override
	public Template_type getTemplatetype() {
		return Template_type.NAMED_TEMPLATE_LIST;
	}

	@Override
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous assignment notation";
		}

		return "assignment notation";
	}

	@Override
	public String createStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append("{ ");
		for (int i = 0, size = namedTemplates.getNofTemplates(); i < size; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			NamedTemplate namedTemplate = namedTemplates.getTemplateByIndex(i);
			builder.append(namedTemplate.getName().getDisplayName());
			builder.append(" := ");
			builder.append(namedTemplate.getTemplate().createStringRepresentation());
		}
		builder.append(" }");

		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}

		return builder.toString();
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (namedTemplates != null) {
			namedTemplates.setMyScope(scope);
		}
	}

	/**
	 * Remove all named templates that were not parsed, but generated during
	 * previous semantic checks.
	 * */
	public void removeGeneratedValues() {
		if (namedTemplates != null) {
			namedTemplates.removeGeneratedValues();
		}
	}

	/** @return the number of templates in the list */
	public int getNofTemplates() {
		return namedTemplates.getNofTemplates();
	}

	/**
	 * @param index
	 *                the index of the element to return.
	 * 
	 * @return the template on the indexed position.
	 * */
	public NamedTemplate getTemplateByIndex(final int index) {
		return namedTemplates.getTemplateByIndex(index);
	}

	@Override
	public boolean isValue(final CompilationTimeStamp timestamp) {
		if (lengthRestriction != null || isIfpresent || getIsErroneous(timestamp)) {
			return false;
		}

		if (baseTemplate != null) {
			// TODO: optimization possibility;
			// if(baseTemplate.isValue()) then rework this function
			// and getValue()
			return false;
		}

		for (int i = 0, size = getNofTemplates(); i < size; i++) {
			if (!namedTemplates.getTemplateByIndex(i).getTemplate().isValue(timestamp)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public IValue getValue() {
		if (asValue != null) {
			return asValue;
		}

		NamedValues values = new NamedValues();
		for (int i = 0, size = getNofTemplates(); i < size; i++) {
			NamedTemplate namedTemplate = namedTemplates.getTemplateByIndex(i);
			NamedValue namedValue = new NamedValue(namedTemplate.getName(), namedTemplate.getTemplate().getValue());
			namedValue.setLocation(namedTemplate.getLocation());
			values.addNamedValue(namedValue);
		}
		asValue = new Sequence_Value(values);
		asValue.setLocation(getLocation());
		asValue.setMyScope(getMyScope());
		asValue.setFullNameParent(getNameParent());
		return asValue;
	}

	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		ITTCN3Template temp;
		for (int i = 0, size = namedTemplates.getNofTemplates(); i < size; i++) {
			temp = namedTemplates.getTemplateByIndex(i).getTemplate();
			if (temp != null) {
				temp.checkSpecificValue(timestamp, true);
			}
		}
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			for (int i = 0; i < namedTemplates.getNofTemplates(); i++) {
				NamedTemplate template = namedTemplates.getTemplateByIndex(i);
				if (template != null) {
					referenceChain.markState();
					template.getTemplate().checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
		}
	}

	@Override
	public boolean chkRestrictionNamedListBaseTemplate(final CompilationTimeStamp timestamp, final String definitionName,
			final Set<String> checkedNames, final int neededCheckedCnt, final Location usageLocation) {
		boolean needsRuntimeCheck = false;
		if (checkedNames.size() >= neededCheckedCnt) {
			return needsRuntimeCheck;
		}

		for (int i = 0, size = getNofTemplates(); i < size; i++) {
			ITTCN3Template tmpl = namedTemplates.getTemplateByIndex(i).getTemplate();
			final String name = namedTemplates.getTemplateByIndex(i).getName().getName();
			if (!checkedNames.contains(name)) {
				if (tmpl.checkValueomitRestriction(timestamp, definitionName, true, usageLocation)) {
					needsRuntimeCheck = true;
				}
				checkedNames.add(name);
			}
		}
		if (baseTemplate instanceof Named_Template_List) {
			if (((Named_Template_List) baseTemplate).chkRestrictionNamedListBaseTemplate(timestamp, definitionName, checkedNames,
					neededCheckedCnt, usageLocation)) {
				needsRuntimeCheck = true;
			}
		}
		return needsRuntimeCheck;
	}

	@Override
	public boolean checkValueomitRestriction(final CompilationTimeStamp timestamp, final String definitionName, final boolean omitAllowed, final Location usageLocation) {
		if (omitAllowed) {
			checkRestrictionCommon(definitionName, TemplateRestriction.Restriction_type.TR_OMIT, usageLocation);
		} else {
			checkRestrictionCommon(definitionName, TemplateRestriction.Restriction_type.TR_VALUE, usageLocation);
		}

		boolean needsRuntimeCheck = false;
		int neededCheckedCnt = 0;
		if (baseTemplate != null && myGovernor != null) {
			HashSet<String> checkedNames = new HashSet<String>();
			switch (myGovernor.getTypetype()) {
			case TYPE_TTCN3_SEQUENCE:
				neededCheckedCnt = ((TTCN3_Sequence_Type) myGovernor).getNofComponents();
				break;
			case TYPE_ASN1_SEQUENCE:
				neededCheckedCnt = ((ASN1_Sequence_Type) myGovernor).getNofComponents(timestamp);
				break;
			case TYPE_TTCN3_SET:
				neededCheckedCnt = ((TTCN3_Set_Type) myGovernor).getNofComponents();
				break;
			case TYPE_ASN1_SET:
				neededCheckedCnt = ((ASN1_Set_Type) myGovernor).getNofComponents(timestamp);
				break;
			case TYPE_SIGNATURE:
				neededCheckedCnt = ((Signature_Type) myGovernor).getNofParameters();
				break;
			default:
				// can not reach here because of a previous
				// check
				break;
			}

			for (int i = 0, size = getNofTemplates(); i < size; i++) {
				NamedTemplate temp = namedTemplates.getTemplateByIndex(i);
				if (temp.getTemplate().checkValueomitRestriction(timestamp, definitionName, true, usageLocation)) {
					needsRuntimeCheck = true;
				}

				if (neededCheckedCnt > 0) {
					checkedNames.add(temp.getName().getName());
				}
			}
			if (neededCheckedCnt > 0) {
				if (baseTemplate instanceof Named_Template_List
						&& ((Named_Template_List) baseTemplate).chkRestrictionNamedListBaseTemplate(timestamp,
								definitionName, checkedNames, neededCheckedCnt, usageLocation)) {
					needsRuntimeCheck = true;
				}
			}
		}
		return needsRuntimeCheck;
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

		namedTemplates.updateSyntax(reparser, false);
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (asValue != null) {
			asValue.findReferences(referenceFinder, foundIdentifiers);
			return;
		}
		if (namedTemplates == null) {
			return;
		}
		if (referenceFinder.assignment.getAssignmentType() == Assignment_type.A_TYPE && referenceFinder.fieldId != null && myGovernor != null) {
			// check if this is the type and field we are searching
			// for
			IType govLast = myGovernor.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
			if (referenceFinder.type == govLast) {
				NamedTemplate nt = namedTemplates.getNamedTemplateByName(referenceFinder.fieldId);
				if (nt != null) {
					foundIdentifiers.add(new Hit(nt.getName()));
				}
			}
		}
		namedTemplates.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (asValue != null) {
			if (!asValue.accept(v)) {
				return false;
			}
		} else if (namedTemplates != null) {
			if (!namedTemplates.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
