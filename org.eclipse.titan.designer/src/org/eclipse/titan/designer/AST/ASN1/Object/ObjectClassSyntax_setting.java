/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.Identifier;

/**
 * Class to represent a Setting in the OCS.
 * 
 * @author Kristof Szabados
 */
public final class ObjectClassSyntax_setting extends ObjectClassSyntax_Node {

	public enum SyntaxSetting_types {
		/** undefined. */
		S_UNDEF,
		/** Type. */
		S_T,
		/** Value. */
		S_V,
		/** ValueSet. */
		S_VS,
		/** Object. */
		S_O,
		/** ObjectSet. */
		S_OS
	}

	private final SyntaxSetting_types settingType;
	private final Identifier identifier;

	public ObjectClassSyntax_setting(final SyntaxSetting_types settingType, final Identifier identifier) {
		this.settingType = settingType;
		this.identifier = identifier;
	}

	@Override
	public void accept(final ObjectClassSyntax_Visitor visitor) {
		visitor.visitSetting(this);
	}

	public SyntaxSetting_types getSettingType() {
		return settingType;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	@Override
	public String getDisplayName() {
		final StringBuilder builder = new StringBuilder();
		switch (settingType) {
		case S_T:
			builder.append("<Type>");
			break;
		case S_V:
			builder.append("<Value>");
			break;
		case S_VS:
			builder.append("<ValueSet>");
			break;
		case S_O:
			builder.append("<Object>");
			break;
		case S_OS:
			builder.append("<ObjectSet>");
			break;
		default:
			builder.append("<unknown setting kind>");
			break;
		}
		return builder.toString();
	}
}
