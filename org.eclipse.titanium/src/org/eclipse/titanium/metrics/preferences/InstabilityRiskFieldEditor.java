/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.preferences;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.titanium.metrics.IMetricEnum;

/**
 * The instability risk field editor is a simple risk field editor.
 * With the only difference, that values are between 0 and 1.
 *   
 *   @see SimpleRiskFieldEditor
 * */
public class InstabilityRiskFieldEditor extends SimpleRiskFieldEditor {

	public InstabilityRiskFieldEditor(final Composite parent, final IMetricEnum owner) {
		super(parent, owner);
	}

	@Override
	public void store() {
		final Number[] n = new Number[spinners.length];
		for (int i = 0; i < spinners.length; ++i) {
			n[i] = new Double(((double) spinners[i].getSelection()) / 100);
		}
		PreferenceManager.storeRisk(owner, old, n);
	}

	@Override
	protected void updateSpinners(final RiskMethod m, final boolean toDefault) {
		for (final Spinner s : spinners) {
			s.dispose();
		}
		final Number[] limits = getLimits(m, toDefault);

		switch (m) {
		case NEVER:
			spinners = new Spinner[0];
			break;
		case NO_HIGH: //$FALL-THROUGH$
		case NO_LOW:
			spinners = new Spinner[1];
			spinners[0] = new Spinner(shell, 0);
			spinners[0].setDigits(2);
			spinners[0].setMaximum(50);
			spinners[0].setSelection((int) Math.round(limits[0].doubleValue() * 100));
			spinners[0].addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(final ModifyEvent e) {
					firePropChanged();
				}
			});

			break;
		case NO_LOW_HIGH:
			spinners = new Spinner[2];
			for (int i = 0; i < 2; ++i) {
				spinners[i] = new Spinner(shell, 0);
				spinners[i].setDigits(2);
				spinners[i].setMaximum(50);
				spinners[i].setSelection((int) Math.round(limits[i].doubleValue() * 100));
				spinners[i].addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(final ModifyEvent e) {
						firePropChanged();
					}
				});
			}
			break;
		}
		postUpdateSpinners();
	}
}
