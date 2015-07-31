/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ASN1.Block;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClassSyntax_setting.SyntaxSetting_types;

/**
 * OCS visitor to build the OCS. :) It's clear and simple, isn't it?
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class ObjectClassSyntax_Builder extends ObjectClassSyntax_Visitor {

	protected Block mBlock;
	protected final FieldSpecifications fieldSpecifications;

	public ObjectClassSyntax_Builder(final Block aBlock, final FieldSpecifications aFieldSpecifications) {
		this.mBlock = aBlock;
		this.fieldSpecifications = aFieldSpecifications;
	}

	@Override
	public void visitRoot(final ObjectClassSyntax_root parameter) {
		if (parameter.getIsBuilded()) {
			return;
		}

		if (null == mBlock) {
			final ObjectClassSyntax_sequence sequence = parameter.getSequence();
			FieldSpecification fieldSpecification;
			for (int i = 0; i < fieldSpecifications.getNofFieldSpecifications(); i++) {
				fieldSpecification = fieldSpecifications.getFieldSpecificationByIndex(i).getLast();
				ObjectClassSyntax_sequence temporalSequence = new ObjectClassSyntax_sequence(fieldSpecification.getIsOptional()
						|| fieldSpecification.hasDefault(), true);

				ObjectClassSyntax_literal literal = new ObjectClassSyntax_literal(fieldSpecification.getIdentifier().newInstance());
				literal.setLocation(fieldSpecification.getLocation());

				ObjectClassSyntax_setting setting = null;
				switch (fieldSpecification.getFieldSpecificationType()) {
				case FS_T:{
					final Identifier newIdentifier = fieldSpecification.getIdentifier().newInstance();
					setting = new ObjectClassSyntax_setting(SyntaxSetting_types.S_T, newIdentifier);
					break;
				}
				case FS_V_FT:
				case FS_V_VT:{
					final Identifier newIdentifier = fieldSpecification.getIdentifier().newInstance();
					setting = new ObjectClassSyntax_setting(SyntaxSetting_types.S_V, newIdentifier);
					break;
				}
				case FS_VS_FT:
				case FS_VS_VT:{
					final Identifier newIdentifier = fieldSpecification.getIdentifier().newInstance();
					setting = new ObjectClassSyntax_setting(SyntaxSetting_types.S_VS, newIdentifier);
					break;
				}
				case FS_O:{
					final Identifier newIdentifier = fieldSpecification.getIdentifier().newInstance();
					setting = new ObjectClassSyntax_setting(SyntaxSetting_types.S_O, newIdentifier);
					break;
				}
				case FS_OS:{
					final Identifier newIdentifier = fieldSpecification.getIdentifier().newInstance();
					setting = new ObjectClassSyntax_setting(SyntaxSetting_types.S_OS, newIdentifier);
					break;
				}
				case FS_ERROR:
				default:
					break;
				}

				if (null != setting) {
					setting.setLocation(fieldSpecification.getLocation());

					temporalSequence.addNode(literal);
					temporalSequence.addNode(setting);
					temporalSequence.trimToSize();

					sequence.addNode(temporalSequence);
				}
			}

			sequence.trimToSize();
		} else {
			parameter.getSequence().accept(this);
		}

		parameter.setIsBuilded(true);
	}

	@Override
	public void visitLiteral(final ObjectClassSyntax_literal parameter) {
		// FATAL ERROR
	}

	@Override
	public void visitSetting(final ObjectClassSyntax_setting parameter) {
		// FATAL ERROR
	}
}
