/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.io.Reader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.common.parsers.SyntacticErrorStorage;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.common.parsers.TitanListener;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IAppendableSyntax;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErroneousAttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErroneousAttributeSpecification.Indicator_Type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.AttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErroneousAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifier;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifiers;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3CharstringLexer;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Reparser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The definition class represents general TTCN3 definitions.
 * 
 * @author Kristof Szabados
 * */
public abstract class Definition extends Assignment implements IAppendableSyntax, IIncrementallyUpdateable {
	private static final String SHOULD_BE_PRIVATE = "{0} is referenced only locally, it should be private";

	protected WithAttributesPath withAttributesPath = null;
	protected ErroneousAttributes erroneousAttributes = null;

	/** The visibility modifier of the definition */
	private VisibilityModifier visibilityModifier;

	private Location commentLocation = null;
	
	/**
	 * The cumulative location of a definition is the location of the definition if it is stand alone,
	 *  or the whole short hand notation it is enclosed into.
	 * 
	 * TTCN-3 allows to write some definitions with a shorthand notation.
	 * for example: const integer a1:=1,a2:=2;
	 * In these cases the locations of the definitions overlap, creating various problems.
	 * 
	 * To make the situation clear cumulative location stores the location of the whole shorthand notation
	 *  the definition is listed in.
	 * This way it becomes easy to identify which definitions can be affected by a change in the file.
	 * */
	private Location cumulativeDefinitionLocation = null;

	//TODO this should be removed as the same functionality is present in Titanium as codesmell.
	public List<String> referingHere = new ArrayList<String>();

	private static boolean markOccurrences;

	// The actual value of the severity level to report unused definitions
	// on.
	private static String unusedLocalDefinitionSeverity;
	private static String unusedGlobalDefinitionSeverity;
	private static String nonPrivatePrivateSeverity;

	protected static String getUnusedLocalDefinitionSeverity() {
		return unusedLocalDefinitionSeverity;
	}

	static {
		final IPreferencesService ps = Platform.getPreferencesService();
		if ( ps != null ) {
			markOccurrences = ps.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.MARK_OCCURRENCES_TTCN3_ASSIGNMENTS, false, null);
			unusedLocalDefinitionSeverity = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORTUNUSEDLOCALDEFINITION, GeneralConstants.WARNING, null);
			unusedGlobalDefinitionSeverity = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORTUNUSEDGLOBALDEFINITION, GeneralConstants.WARNING, null);
			nonPrivatePrivateSeverity = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
					PreferenceConstants.REPORT_NONPRIVATE_PRIVATE, GeneralConstants.IGNORE, null);

			final Activator activator = Activator.getDefault();
			if (activator != null) {
				activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent event) {
						final String property = event.getProperty();
						if (PreferenceConstants.MARK_OCCURRENCES_TTCN3_ASSIGNMENTS.equals(property)) {
							markOccurrences = ps.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.MARK_OCCURRENCES_TTCN3_ASSIGNMENTS, false, null);
							return;
						}
						if (PreferenceConstants.REPORTUNUSEDLOCALDEFINITION.equals(property)) {
							unusedLocalDefinitionSeverity = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.REPORTUNUSEDLOCALDEFINITION, GeneralConstants.WARNING, null);
						}
						if (PreferenceConstants.REPORTUNUSEDGLOBALDEFINITION.equals(property)) {
							unusedGlobalDefinitionSeverity = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.REPORTUNUSEDGLOBALDEFINITION, GeneralConstants.WARNING, null);
						}
						if (PreferenceConstants.REPORT_NONPRIVATE_PRIVATE.equals(property)) {
							nonPrivatePrivateSeverity = ps.getString(ProductConstants.PRODUCT_ID_DESIGNER,
									PreferenceConstants.REPORT_NONPRIVATE_PRIVATE, GeneralConstants.IGNORE, null);
						}
					}
				});
			}
		}
	}

	protected Definition(final Identifier identifier) {
		super(identifier);
	}

	/**
	 * Sets the visibility modifier of this definition.
	 * 
	 * @param modifier
	 *                the modifier to be set
	 * */
	public final void setVisibility(final VisibilityModifier modifier) {
		visibilityModifier = modifier;
	}

	/**
	 * @return the visibility modifier of this definition.
	 * */
	public final VisibilityModifier getVisibilityModifier() {
		if(visibilityModifier == null) {
			return VisibilityModifier.Public;
		}

		return visibilityModifier;
	}

	/**
	 * Sets the with attributes for this definition if it has any. Also
	 * creates the with attribute path, to store the attributes in.
	 * 
	 * @param attributes
	 *                the attribute to be added.
	 * */
	public void setWithAttributes(final MultipleWithAttributes attributes) {
		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
		}
		// let the qualifiers know which definition they belong to
		// TODO: this should be set while semantically analyzing
		// qualifiers
		if (attributes != null) {
			withAttributesPath.setWithAttributes(attributes);
			for (int i = 0; i < attributes.getNofElements(); i++) {
				Qualifiers qs = attributes.getAttribute(i).getQualifiers();
				if (qs == null) {
					continue;
				}
				for (int j = 0; j < qs.getNofQualifiers(); j++) {
					qs.getQualifierByIndex(j).setDefinition(this);
				}
			}
		}
	}

	/**
	 * In TTCN-3 it is possible to use a short hand notation
	 *  when defining several constants and variables of the same type.
	 *  for example: const integer x :=5, j:=6;
	 *  
	 * In these cases the location attributed to each definition is their own minimal location.
	 * The ComulativeDefinitionLocation is the location of the whole set of same typed definition.
	 * 
	 * @return the location of the same typed definition list the definition is located in. 
	 * */
	public Location getCumulativeDefinitionLocation() {
		if (cumulativeDefinitionLocation != null) {
			return cumulativeDefinitionLocation;
		}
		
		return getLocation();
	}

	/**
	 * Set the cumulative location of this definition
	 * */
	public void setCumulativeDefinitionLocation(final Location location) {
		this.cumulativeDefinitionLocation = location;
	}

	/**
	 * @return the with attribute path element of this definition. If it did
	 *         not exist it will be created.
	 * */
	public WithAttributesPath getAttributePath() {
		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
		}

		return withAttributesPath;
	}

	/**
	 * Sets the parent path for the with attribute path element of this
	 * definition. Also, creates the with attribute path node if it did not
	 * exist before.
	 * 
	 * @param parent
	 *                the parent to be set.
	 * */
	public void setAttributeParentPath(final WithAttributesPath parent) {
		if (withAttributesPath == null) {
			withAttributesPath = new WithAttributesPath();
		}

		withAttributesPath.setAttributeParent(parent);
	}

	/**
	 * Checks if this definition has an implicit omit attribute or not.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return true if it has an implicit omit attribute, false if no
	 *         attribute is given or explicit omit is specified.
	 * */
	public boolean hasImplicitOmitAttribute(final CompilationTimeStamp timestamp) {
		if (withAttributesPath == null) {
			return false;
		}

		List<SingleWithAttribute> realAttributes = withAttributesPath.getRealAttributes(timestamp);
		SingleWithAttribute tempAttribute;
		for (int i = realAttributes.size() - 1; i >= 0; i--) {
			tempAttribute = realAttributes.get(i);
			if (tempAttribute != null && Attribute_Type.Optional_Attribute.equals(tempAttribute.getAttributeType())) {
				String tempSpecification = tempAttribute.getAttributeSpecification().getSpecification();
				if ("implicit omit".equals(tempSpecification)) {
					return true;
				} else if ("explicit omit".equals(tempSpecification)) {
					return false;
				}
			}
		}

		return false;
	}

	/**
	 * @return The location of the comment assigned to this definition. Or
	 *         null if none.
	 * */
	@Override
	public Location getCommentLocation() {
		return commentLocation;
	}

	/**
	 * Sets the location of the comment that belongs to this definition.
	 * 
	 * @param commentLocation
	 *                the location of the comment
	 * */
	public void setCommentLocation(final Location commentLocation) {
		this.commentLocation = commentLocation;
	}

	/**
	 * @return the kind of the object reported to the proposal collector.
	 * */
	public abstract String getProposalKind();

	@Override
	public String getProposalDescription() {
		return getProposalKind();
	}

	protected void checkErroneousAttributes(final CompilationTimeStamp timestamp) {
		erroneousAttributes = null;
		if (withAttributesPath != null) {
			MultipleWithAttributes attribs = withAttributesPath.getAttributes();
			if (attribs == null) {
				return;
			}
			for (int i = 0; i < attribs.getNofElements(); i++) {
				SingleWithAttribute actualAttribute = attribs.getAttribute(i);
				if (actualAttribute.getAttributeType() == Attribute_Type.Erroneous_Attribute) {
					int nofQualifiers = (actualAttribute.getQualifiers() == null) ? 0 : actualAttribute.getQualifiers()
							.getNofQualifiers();
					List<IType> referencedTypeArray = new ArrayList<IType>(nofQualifiers);
					List<ArrayList<Integer>> subrefsArrayArray = new ArrayList<ArrayList<Integer>>(nofQualifiers);
					List<ArrayList<IType>> typeArrayArray = new ArrayList<ArrayList<IType>>(nofQualifiers);
					if (nofQualifiers == 0) {
						actualAttribute.getLocation().reportSemanticError(
								"At least one qualifier must be specified for the `erroneous' attribute");
					} else {
						// check if qualifiers point to
						// existing fields
						for (int qi = 0; qi < nofQualifiers; qi++) {
							Qualifier actualQualifier = actualAttribute.getQualifiers().getQualifierByIndex(qi);
							IType definitionType = getType(timestamp);
							// construct a reference
							Reference reference = new Reference(null);
							reference.addSubReference(new FieldSubReference(identifier));
							for (int ri = 0; ri < actualQualifier.getNofSubReferences(); ri++) {
								reference.addSubReference(actualQualifier.getSubReferenceByIndex(ri));
							}
							reference.setLocation(actualQualifier.getLocation());
							reference.setMyScope(getMyScope());
							IType fieldType = definitionType.getFieldType(timestamp, reference, 1,
									Expected_Value_type.EXPECTED_CONSTANT, false);
							ArrayList<Integer> subrefsArray = null;
							ArrayList<IType> typeArray = null;
							if (fieldType != null) {
								subrefsArray = new ArrayList<Integer>();
								typeArray = new ArrayList<IType>();
								boolean validIndexes = definitionType.getSubrefsAsArray(timestamp, reference, 1,
										subrefsArray, typeArray);
								if (!validIndexes) {
									fieldType = null;
									subrefsArray = null;
									typeArray = null;
								}
								if (reference.refersToStringElement()) {
									actualQualifier.getLocation()
											.reportSemanticError(
													"Reference to a string element cannot be used in this context");
									fieldType = null;
									subrefsArray = null;
									typeArray = null;
								}
							}
							referencedTypeArray.add(fieldType);
							subrefsArrayArray.add(subrefsArray);
							typeArrayArray.add(typeArray);
						}
					}
					// parse the attr. spec.
					ErroneousAttributeSpecification errAttributeSpecification = parseErrAttrSpecString(actualAttribute
							.getAttributeSpecification());
					if (errAttributeSpecification != null) {
						if (erroneousAttributes == null) {
							erroneousAttributes = new ErroneousAttributes(getType(timestamp));
						}
						erroneousAttributes.addSpecification(errAttributeSpecification);
						errAttributeSpecification.check(timestamp, getMyScope());
						// create qualifier -
						// err.attr.spec. pairs
						for (int qi = 0; qi < nofQualifiers; qi++) {
							if (referencedTypeArray.get(qi) != null
									&& errAttributeSpecification.getIndicator() != Indicator_Type.Invalid_Indicator) {
								Qualifier actualQualifier = actualAttribute.getQualifiers().getQualifierByIndex(qi);
								erroneousAttributes.addFieldErr(actualQualifier, errAttributeSpecification,
										subrefsArrayArray.get(qi), typeArrayArray.get(qi));
							}
						}
					}
				}
			}
			if (erroneousAttributes != null) {
				erroneousAttributes.check(timestamp);
			}
		}
	}

	/**
	 * Check if two definitions are (almost) identical: the type and
	 * dimensions must always be identical, the initial values can be
	 * different depending on the definition type. If error was reported the
	 * return value is false. The initial values (if applicable) may be
	 * present/absent, different or un-foldable. The function must be
	 * overridden to be used.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param definition
	 *                the definition to compare against the actual one.
	 * 
	 * @return true if they are (almost) identical, false otherwise.
	 */
	public boolean checkIdentical(final CompilationTimeStamp timestamp, final Definition definition) {
		return false;
	}

	/**
	 * Checks if the current definition could be set as private even though
	 * it is not declared as one.
	 * */
	protected final void postCheckPrivateness() {
		if (isUsed && referingHere.size() == 1 && !VisibilityModifier.Private.equals(visibilityModifier) && !isLocal()) {
			String moduleName = getMyScope().getModuleScope().getName();
			if (referingHere.get(0).equals(moduleName)) {
				identifier.getLocation().reportConfigurableSemanticProblem(nonPrivatePrivateSeverity,
						MessageFormat.format(SHOULD_BE_PRIVATE, identifier.getDisplayName()));
			}
		}
	}

	@Override
	public void postCheck() {
		if (!isUsed) {
			if (isLocal()) {
				identifier.getLocation().reportConfigurableSemanticProblem(unusedLocalDefinitionSeverity,
						MessageFormat.format(LOCALLY_UNUSED, getDescription()));
			} else {
				identifier.getLocation().reportConfigurableSemanticProblem(unusedGlobalDefinitionSeverity,
						MessageFormat.format(GLOBALLY_UNUSED, getDescription()));
			}
		}
	}

	@Override
	public boolean isLocal() {
		if (myScope == null) {
			return false;
		}

		for (Scope scope = myScope; scope != null; scope = scope.getParentScope()) {
			if (scope instanceof StatementBlock) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Adds the definition to the list completion proposals, with some
	 * description of the definition.
	 * <p>
	 * Extending class only need to implement their
	 * {@link #getProposalKind()} function
	 * 
	 * @param propCollector
	 *                the proposal collector.
	 * @param i
	 *                the index of a part of the full reference, for which
	 *                we wish to find completions.
	 * */
	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		String proposalKind = getProposalKind();
		propCollector.addProposal(identifier, " - " + proposalKind, ImageCache.getImage(getOutlineIcon()), proposalKind);
	}

	/**
	 * Adds the definition to the list declaration proposals.
	 * 
	 * @param declarationCollector
	 *                the declaration collector.
	 * @param i
	 *                the index of a part of the full reference, for which
	 *                we wish to find the declaration, or a definition which
	 *                might point us a step forward to the declaration.
	 * */
	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
	}

	/**
	 * @return template restriction for this definition
	 */
	public TemplateRestriction.Restriction_type getTemplateRestriction() {
		return TemplateRestriction.Restriction_type.TR_NONE;
	}

	@Override
	public List<Integer> getPossibleExtensionStarterTokens() {
		List<Integer> result = new ArrayList<Integer>();

		if (isLocal()) {
			return result;
		}

		if (withAttributesPath == null || withAttributesPath.getAttributes() == null) {
			result.add(Ttcn3Lexer.WITH);
		}

		return result;
	}

	@Override
	public List<Integer> getPossiblePrefixTokens() {
		if (isLocal()) {
			List<Integer> result = new ArrayList<Integer>(2);
			result.add(Ttcn3Lexer.CONST);
			result.add(Ttcn3Lexer.VAR);
			return result;
		}

		if (visibilityModifier == null) {
			List<Integer> result = new ArrayList<Integer>(3);
			result.add(Ttcn3Lexer.PUBLIC);
			result.add(Ttcn3Lexer.FRIEND);
			result.add(Ttcn3Lexer.PRIVATE);
			return result;
		}

		return new ArrayList<Integer>(0);
	}

	/**
	 * Handles the incremental parsing of this definition.
	 * 
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public abstract void updateSyntax(final TTCN3ReparseUpdater reparser, boolean isDamaged) throws ReParseException;

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (withAttributesPath != null) {
			withAttributesPath.findReferences(referenceFinder, foundIdentifiers);
		}
		if (erroneousAttributes != null) {
			erroneousAttributes.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		if (withAttributesPath != null && !withAttributesPath.accept(v)) {
			return false;
		}
		if (erroneousAttributes != null && !erroneousAttributes.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean shouldMarkOccurrences() {
		return markOccurrences;
	}
	
	private static ErroneousAttributeSpecification parseErrAttrSpecString(final AttributeSpecification aAttrSpec) {
		ErroneousAttributeSpecification returnValue = null;
		Location location = aAttrSpec.getLocation();
		String code = aAttrSpec.getSpecification();
		if (code == null) {
			return null;
		}
		// code must be transformed, according to
		// compiler2/ttcn3/charstring_la.l
		code = Ttcn3CharstringLexer.parseCharstringValue(code, location); // TODO
		Reader reader = new StringReader(code);
		CharStream charStream = new UnbufferedCharStream(reader);
		Ttcn3Lexer lexer = new Ttcn3Lexer(charStream);
		lexer.setTokenFactory( new CommonTokenFactory( true ) );
		// needs to be shifted by one because of the \" of the string
		lexer.setCharPositionInLine( 0 );

		// lexer and parser listener
		TitanListener parserListener = new TitanListener();
		// remove ConsoleErrorListener
		lexer.removeErrorListeners();
		lexer.addErrorListener(parserListener);

		// Previously it was UnbufferedTokenStream(lexer), but it was changed to BufferedTokenStream, because UnbufferedTokenStream seems to be unusable. It is an ANTLR 4 bug.
		// Read this: https://groups.google.com/forum/#!topic/antlr-discussion/gsAu-6d3pKU
		// pr_PatternChunk[StringBuilder builder, boolean[] uni]:
		//   $builder.append($v.text); <-- exception is thrown here: java.lang.UnsupportedOperationException: interval 85..85 not in token buffer window: 86..341
		TokenStream tokens = new BufferedTokenStream( lexer );
		
		Ttcn3Reparser parser = new Ttcn3Reparser( tokens );
		IFile file = (IFile) location.getFile();
		parser.setActualFile(file);
		parser.setOffset( location.getOffset() + 1 );
		parser.setLine( location.getLine() );

		// remove ConsoleErrorListener
		parser.removeErrorListeners();
		parser.addErrorListener( parserListener );
		
		MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, location.getFile(), location.getOffset(),
				location.getEndOffset());

		returnValue = parser.pr_ErroneousAttributeSpec().errAttrSpec;
		List<SyntacticErrorStorage> errors = parser.getErrors();
		List<TITANMarker> warnings = parser.getWarnings();
		List<TITANMarker> unsupportedConstructs = parser.getUnsupportedConstructs();

		// add markers
		if (errors != null) {
			for (int i = 0; i < errors.size(); i++) {
				Location temp = new Location(location);
				temp.setOffset(temp.getOffset() + 1);
				ParserMarkerSupport.createOnTheFlySyntacticMarker(file, errors.get(i), IMarker.SEVERITY_ERROR, temp);
			}
		}
		if (warnings != null) {
			for (TITANMarker marker : warnings) {
				if (file.isAccessible()) {
					Location loc = new Location(file, marker.getLine(), marker.getOffset(), marker.getEndOffset());
					loc.reportExternalProblem(marker.getMessage(), marker.getSeverity(),
							GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
				}
			}
		}
		if (unsupportedConstructs != null) {
			for (TITANMarker marker : unsupportedConstructs) {
				if (file.isAccessible()) {
					Location loc = new Location(file, marker.getLine(), marker.getOffset(), marker.getEndOffset());
					loc.reportExternalProblem(marker.getMessage(), marker.getSeverity(),
							GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
				}
			}
		}
		return returnValue;
	}
}
