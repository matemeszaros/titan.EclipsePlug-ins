/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.metrics.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.titanium.metrics.IMetricEnum;

/**
 * A simple risk field editor, sufficient to most {@link IValueMetric}.
 * <p>
 * This class provides a widget with two parts:
 * <ul>
 * <li>on the left a combobox presents, where user can select the method of
 * warning to the metric</li>
 * <li>on the right, zero, one or two spinbox presents (according to the
 * combobox selection), where the limits can be adjusted.</li>
 * </ul>
 * </p>
 * Limits are meant in the simplest way, partitioning the real line into
 * sections respective to the combobox selection, and each following section
 * corresponds to higher risk level. </p>
 * <p>
 * For example, when the selection is <code>NO_HIGH</code>, one limit can be
 * set. If the metric value is lower than this limit for a given entity, it will
 * be considered to bear no risk, while when it exceeds this limit, it will be
 * considered to bear high risk.
 * </p>
 * 
 * @author poroszd
 * 
 */
public class SimpleRiskFieldEditor implements IRiskFieldEditor {
	protected final IMetricEnum owner;
	private IRiskEditorPropertyListener propertyListener;
	private boolean valid;
	protected final Composite shell;
	private final Combo method;
	protected RiskMethod old;
	protected Spinner[] spinners;
	protected List<IRiskEditorListener> listeners;

	public SimpleRiskFieldEditor(final Composite parent, final IMetricEnum owner) {
		this.owner = owner;

		shell = new Composite(parent, 0);
		final GridLayout l = new GridLayout();
		l.numColumns = 3;
		shell.setLayout(l);

		method = new Combo(shell, 0);
		for (final RiskMethod m : RiskMethod.values()) {
			method.add(m.getText());
		}

		method.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final RiskMethod newMethod = RiskMethod.myMethod(method.getSelectionIndex());
				if (newMethod != old) {
					old = newMethod;
					updateSpinners(newMethod, false);
					firePropChanged();
				}
			}

		});
		method.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				firePropChanged();
			}
		});

		spinners = new Spinner[0];
		listeners = new ArrayList<IRiskEditorListener>();
	}

	@Override
	public void load() {
		do_load(false);
		old = RiskMethod.myMethod(method.getSelectionIndex());
		checkValid();
	}

	@Override
	public void loadDefault() {
		do_load(true);
		old = RiskMethod.myMethod(method.getSelectionIndex());
		checkValid();
	}

	@Override
	public void store() {
		final Number[] n = new Number[spinners.length];
		for (int i = 0; i < spinners.length; ++i) {
			n[i] = spinners[i].getSelection();
		}
		PreferenceManager.storeRisk(owner, old, n);
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public void setPropListener(final IRiskEditorPropertyListener propertyListener) {
		this.propertyListener = propertyListener;
	}

	protected void firePropChanged() {
		if (propertyListener != null) {
			checkValid();
			propertyListener.propertyChange(valid);
		}
	}

	protected void checkValid() {
		valid = (old != RiskMethod.NO_LOW_HIGH || spinners[0].getSelection() <= spinners[1].getSelection());
	}

	protected void do_load(final boolean toDefault) {
		final RiskMethod m = PreferenceManager.getRiskMethod(owner, toDefault);
		updateSpinners(m, toDefault);
		method.select(m.ordinal());
	}

	protected void updateSpinners(final RiskMethod m, final boolean toDefault) {
		for (final Spinner s : spinners) {
			s.dispose();
		}
		Number[] limits = getLimits(m, toDefault);
		switch (m) {
		case NEVER:
			spinners = new Spinner[0];
			break;
		case NO_HIGH: //$FALL-THROUGH$
		case NO_LOW:
			spinners = new Spinner[1];
			spinners[0] = new Spinner(shell, 0);
			spinners[0].setMaximum(1000);
			spinners[0].setSelection(limits[0].intValue());
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
				spinners[i].setMaximum(1000);
				spinners[i].setSelection(limits[i].intValue());
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

	/**
	 * This function should be run after updating spinners to update the layout and
	 * notify the listeners
	 */
	protected void postUpdateSpinners() {
		shell.layout();
		for (IRiskEditorListener l : listeners) {
			l.editorChanged();
		}
	}

	/**
	 * @param m the risk method to use
	 * @param toDefault if true, than the default value is returned
	 * @return an array containing the limits (or null).
	 */
	protected Number[] getLimits(final RiskMethod m, final boolean toDefault) {
		Number[] limits;
		if (m == PreferenceManager.getRiskMethod(owner, toDefault)) {
			limits = PreferenceManager.getLimits(owner, toDefault);
		} else {
			limits = new Number[] { 0, 0 };
		}
		return limits;
	}

	@Override
	public void addRiskEditorListener(final IRiskEditorListener listener) {
		listeners.add(listener);
	}
}
