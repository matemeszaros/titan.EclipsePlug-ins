/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;

/**
 * A save paticipant that enables us to monitor source changes that happened
 * while the plug-in was not active.
 * 
 * @author Kristof Szabados
 */
public final class SaveParticipant implements ISaveParticipant {

	@Override
	public void doneSaving(final ISaveContext context) {
		// Do nothing
	}

	@Override
	public void prepareToSave(final ISaveContext context) {
		// Do nothing
	}

	@Override
	public void rollback(final ISaveContext context) {
		// Do nothing
	}

	@Override
	public void saving(final ISaveContext context) {
		context.needDelta();
	}

}
