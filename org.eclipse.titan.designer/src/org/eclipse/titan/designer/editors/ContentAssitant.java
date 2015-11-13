/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * @author Kristof Szabados
 * */
public class ContentAssitant extends ContentAssistant {
	private static final String NO_PROPOSAL = "No Default proposals";

	private IPropertyChangeListener listener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(final PropertyChangeEvent event) {
			String property = event.getProperty();

			if (PreferenceConstants.CONTENTASSISTANT_AUTO_ACTIVATION.equals(property)) {
				enableAutoActivation((Boolean) event.getNewValue());
			} else if (PreferenceConstants.CONTENTASSISTANT_AUTO_ACTIVATION_DELAY.equals(property)) {
				setAutoActivationDelay((Integer) event.getNewValue());
			} else if (PreferenceConstants.CONTENTASSISTANT_SINGLE_PROPOSAL_INSERTION.equals(property)) {
				enableAutoInsert((Boolean) event.getNewValue());
			} else if (PreferenceConstants.CONTENTASSISTANT_COMMON_PREFIX_INSERTION.equals(property)) {
				enablePrefixCompletion((Boolean) event.getNewValue());
			}
		}
	};

	public ContentAssitant() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		enableAutoActivation(store.getBoolean(PreferenceConstants.CONTENTASSISTANT_AUTO_ACTIVATION));
		setAutoActivationDelay(store.getInt(PreferenceConstants.CONTENTASSISTANT_AUTO_ACTIVATION_DELAY));
		enableAutoInsert(store.getBoolean(PreferenceConstants.CONTENTASSISTANT_SINGLE_PROPOSAL_INSERTION));
		enablePrefixCompletion(store.getBoolean(PreferenceConstants.CONTENTASSISTANT_COMMON_PREFIX_INSERTION));

		setShowEmptyList(true);
		setEmptyMessage(NO_PROPOSAL);
	}

	@Override
	protected void install() {
		super.install();
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(listener);
	}

	@Override
	public void uninstall() {
		super.uninstall();
		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(listener);
	}
}
