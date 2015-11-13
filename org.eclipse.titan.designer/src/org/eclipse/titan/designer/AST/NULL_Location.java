/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The NULL_Location class represents a location which was not set, or was set
 * incorrectly.
 * <p>
 * This class is used to handle cases when the location of an object can not be
 * determined, but is referred to frequently. For example the location of an
 * identifier might not be determinable if there are syntactic errors near by.
 * <p>
 * Please note that because of the purpose of this class, none of its methods
 * does any real work.
 * 
 * @author Kristof Szabados
 * */
public final class NULL_Location extends Location {
	/** The only instance. */
	public static final NULL_Location INSTANCE = new NULL_Location();

	private NULL_Location() {
	}

	@Override
	public void reportSemanticError(final String reason) {
		final boolean debug = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);
		if (GeneralConstants.DEBUG && debug) {
			ErrorReporter.INTERNAL_ERROR("The following semantic error was reported on a non-existent location: " + reason);
		}
	}

	@Override
	public void reportSemanticWarning(final String reason) {
		final boolean debug = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);
		if (GeneralConstants.DEBUG && debug) {
			ErrorReporter.INTERNAL_ERROR("The following semantic warning was reported on a non-existent location: " + reason);
		}
	}

	@Override
	public void reportSyntacticError(final String reason) {
		final boolean debug = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);
		if (GeneralConstants.DEBUG && debug) {
			ErrorReporter.INTERNAL_ERROR("The following syntactic error was reported on a non-existent location: " + reason);
		}
	}

	@Override
	public void reportSyntacticWarning(final String reason) {
		final boolean debug = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);
		if (GeneralConstants.DEBUG && debug) {
			ErrorReporter.INTERNAL_ERROR("The following syntactic warning was reported on a non-existent location: " + reason);
		}
	}
}
