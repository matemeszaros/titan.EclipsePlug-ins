/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.text.MessageFormat;

import org.eclipse.titan.common.product.ProductIdentity;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.core.ProductIdentityHelper;

/**
 * Represents module version information (i.e. the module's own version)
 * 
 * @author Csaba Raduly
 */
public class ModuleVersionAttribute extends ExtensionAttribute {
	// raw (unparsed) data
	/** The Identifier whose name represents the version. */
	private final Identifier versionToken;
	/** True if the version information was in the <RnXnn> format. */
	private final boolean isTemplate;
	// processed data
	private ProductIdentity versionNumber;

	/**
	 * Constructor, just stores the data.
	 * 
	 * @param id
	 *                identifier representing the version info
	 * @param template
	 *                true if it was between angle brackets
	 */
	public ModuleVersionAttribute(final Identifier id, final boolean template) {
		versionToken = id;
		isTemplate = template;
	}

	/**
	 * @return the version number parsed from this attributed, or null if
	 *         there was an error.
	 * */
	public final ProductIdentity getVersionNumber() {
		return versionNumber;
	}

	/**
	 * Convert the raw data.
	 */
	public final void parse() {
		String versionString = versionToken.getTtcnName();
		if (isTemplate) {
			if (!"RnXnn".equals(versionString)) {
				getLocation().reportSemanticError("Version template must be exactly <RnXnn>");
			}
			// <RnXnn> counts as no version info, which satisfies
			// all
			versionNumber = new ProductIdentity();
			return;
		}

		versionNumber = ProductIdentityHelper.getProductIdentity(versionString, getLocation());
	}

	public static String versionString(final int major, final int minor, final int patch) {
		int[] one = { minor + "A".codePointAt(0) };
		String rev = new String(one, 0, 1);
		return MessageFormat.format("R{0}{1}{2}", major, rev, (patch == Integer.MAX_VALUE) ? "" : patch);
	}

	@Override
	public final String toString() {
		if (versionNumber == null) {
			return "<erroneous version number>";
		}

		return versionNumber.toString();
	}

	@Override
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.VERSION;
	}
}
