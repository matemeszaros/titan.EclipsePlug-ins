/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.performance;

import org.eclipse.swt.widgets.Composite;

/**
 * @author Kristof Szabados
 * */
public final class SinglePerformanceSettingsTab extends BasePerformanceSettingsTab {

	@Override
	public void executorSpecificControls(final Composite pageComposite) {
		createKeepConfigfileArea(pageComposite);
	}
	
}
