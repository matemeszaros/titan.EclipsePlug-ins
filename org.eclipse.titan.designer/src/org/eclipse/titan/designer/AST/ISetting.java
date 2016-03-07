/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public interface ISetting extends IASTNode, ILocateableNode {

	public enum Setting_type {
		/** Reference to non-existent stuff. */	S_UNDEF,
		/**ObjectClass. */									S_OC ,
		/** Object. */											S_O,
		/** ObjectSet. */									S_OS,
		/** Reference to non-existent stuff. */	S_ERROR,
		/** Type. */											S_T,
		/**< Template. */									S_TEMPLATE,
		/**< Value. */											S_V,
		/**< ValueSet. */									S_VS
	}

	/**
	 * Checks whether the setting was reported erroneous or not.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @return true if the setting is erroneous, or false otherwise */
	boolean getIsErroneous(final CompilationTimeStamp timestamp);

	/**
	 * Sets the erroneousness of the setting.
	 *
	 * @param isErroneous the value to set.
	 * */
	void setIsErroneous(final boolean isErroneous);

	/**
	 * @return the internal type of the setting
	 * */
	Setting_type getSettingtype();

	@Override
	void setLocation(final Location location);

	@Override
	Location getLocation();

	boolean isAsn();
}
