/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.brokenpartsanalyzers;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * Factory class to create instance of IBaseAnalyzer.
 * 
 * @author Peter Olah
 */
public final class AnalyzerFactory {

	private AnalyzerFactory() {
	}

	public static IBaseAnalyzer getAnalyzer(final CompilationTimeStamp timestamp) {

		SelectionAlgorithm selectionAlgorithm = SelectionAlgorithm.fromString(Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.MODULESELECTIONALGORITHM, PreferenceConstantValues.MODULESELECTIONORIGINAL, null));
		
		if (selectionAlgorithm == null) {
			selectionAlgorithm = SelectionAlgorithm.getDefaultValue();
		}
		
		switch (selectionAlgorithm) {
			case MODULESELECTIONORIGINAL:
				return new OriginalModuleSelection();
			case BROKENREFERENCESINVERTED:
				return new BrokenPartsViaReferences(SelectionAlgorithm.BROKENREFERENCESINVERTED, timestamp);
		}

		return null;
	}


}
