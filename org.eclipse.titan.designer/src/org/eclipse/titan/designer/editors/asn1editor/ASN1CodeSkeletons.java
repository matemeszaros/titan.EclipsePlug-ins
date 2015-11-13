/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.asn1editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.SkeletonTemplateProposal;
import org.eclipse.titan.designer.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * */
public final class ASN1CodeSkeletons {
	public static final String CONTEXT_IDENTIFIER = "ASN1_SOURCE_CODE";
	public static final String CONTEXT_NAME = "ASN1 Code Skeleton Context";

	private static Image skeletonImage = ImageCache.getImage(SkeletonTemplateProposal.SKELETON_IMAGE);
	private static final String NEWLINE = System.getProperty("line.separator");

	// TODO create a real resolver that can filter out ${cursor}, and
	// replace ${} with ${cursor}
	private static final SkeletonTemplateProposal[] TEMPLATE_PROPOSALS = new SkeletonTemplateProposal[] {
			// of kind type
			new SkeletonTemplateProposal("SEQUENCE", new Template("SEQUENCE", "", CONTEXT_IDENTIFIER, "SEQUENCE {" + NEWLINE
					+ "${field1} ${subType1}," + NEWLINE + "${field2} ${subType2} }", false)),
			new SkeletonTemplateProposal("SEQUENCE", new Template("SEQUENCE OF", "", CONTEXT_IDENTIFIER,
					"SEQUENCE OF ${constraint} OF ${Type}", false)),
			new SkeletonTemplateProposal("SET", new Template("SET", "", CONTEXT_IDENTIFIER, "SET {" + NEWLINE + "${field1} ${subType1},"
					+ NEWLINE + "${field2} ${subType2} }", false)),
			new SkeletonTemplateProposal("SET", new Template("SET OF", "", CONTEXT_IDENTIFIER, "SET OF ${constraint} OF ${Type}", false)),
			new SkeletonTemplateProposal("CHOICE", new Template("CHOICE", "", CONTEXT_IDENTIFIER, "CHOICE {" + NEWLINE
					+ "${field1} ${subType1}," + NEWLINE + "${field2} ${subType2} }", false)),
			new SkeletonTemplateProposal("ENUMERATED", new Template("ENUMERATED", "", CONTEXT_IDENTIFIER,
					"ENUMERATED { ${identifier1}( ${value1} ), ${identifier2}( ${value2} )" + "} ${fieldIdentifier}", false)),
			new SkeletonTemplateProposal("COMPONENTS", new Template("COMPONENTS OF", "", CONTEXT_IDENTIFIER,
					"COMPONENTS OF ${type_assignment}", false)),
			// of kind ObjectClass
			new SkeletonTemplateProposal("CLASS", new Template("Object Class", "", CONTEXT_IDENTIFIER, "${IDENTIFIER} := CLASS {"
					+ NEWLINE + "&${field1} ${type1} ${UNIQUE} ${OPTIONAL}," + NEWLINE
					+ "&${field2} ${type2} ${UNIQUE} ${OPTIONAL}" + NEWLINE + "}", false)),
			new SkeletonTemplateProposal("CLASS", new Template("Object Class", "with user defined syntax", CONTEXT_IDENTIFIER,
					"${IDENTIFIER} := CLASS {" + NEWLINE + "&${field1} ${type1} ${UNIQUE} ${OPTIONAL}," + NEWLINE
							+ "&${field2} ${type2} ${UNIQUE} ${OPTIONAL}" + NEWLINE + "} WITH SYNTAX {" + NEWLINE
							+ "${FIELD1} &${field1}" + NEWLINE + "${FIELD2} &${field2}" + NEWLINE + "}", false)),
			// objectset
			new SkeletonTemplateProposal("OBJECTSET", new Template("Object Set", "", CONTEXT_IDENTIFIER,
					"${Identifier} ${OBJECT-CLASS} := {" + NEWLINE + "${first-object} |" + NEWLINE + "${second-object}" + NEWLINE
							+ "}", false)),
			// valueset
			new SkeletonTemplateProposal("VALUESET", new Template("Value Set", "", CONTEXT_IDENTIFIER, "${Identifier} ${type} := {"
					+ NEWLINE + "${first-value} |" + NEWLINE + "${second-value}" + NEWLINE + "}", false)) };

	private static final String ASN1_MODULE_SKELETON = " DEFINITIONS" + NEWLINE
	+ NEWLINE
	+ "-- [(AUTOMATIC|EXPLICIT|IMPLICIT) TAGS]" + NEWLINE
	+ "-- the default is EXPLICIT TAGS" + NEWLINE
	+ "  AUTOMATIC TAGS ::=" + NEWLINE
	+ NEWLINE
	+ "BEGIN" + NEWLINE
	+ "-- EXPORTS <exports clause>;" + NEWLINE
	+ "-- IMPORTS <import clause>;" + NEWLINE
	+ "-- MODULE-BODY" + NEWLINE
	+ NEWLINE
			+ "END" + NEWLINE;

	/** private constructor to disable instantiation */
	private ASN1CodeSkeletons() {
	}

	/**
	 * Adds the TTCN3 language dependent code skeletons stored in this class
	 * to a proposal collector.
	 * 
	 * @param doc
	 *                the document where the skeletons will be inserted
	 * @param offset
	 *                the offset at which the word to be completed starts
	 * @param collector
	 *                the ProposalCollector which collects the skeletons
	 * */
	public static void addSkeletonProposals(final IDocument doc, final int offset, final ProposalCollector collector) {
		for (SkeletonTemplateProposal templateProposal : TEMPLATE_PROPOSALS) {
			collector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(), skeletonImage);
		}
	}

	/**
	 * Returns a valid ASN1 module skeleton.
	 * 
	 * @param moduleName
	 *                the name of the module to be used to create the
	 *                skeleton
	 * @return the ASN1 module skeleton
	 * */
	public static String getASN1ModuleSkeleton(final String moduleName) {
		StringBuilder buffer = new StringBuilder(moduleName);
		buffer.append(ASN1_MODULE_SKELETON);
		return buffer.toString();
	}
}
