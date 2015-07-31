/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * @author Krisztian Pandi
 * */
public class TextHoverControl extends AbstractInformationControl {

	/**
	 * A wrapper used to deliver content to the hover control, either as
	 * marked-up text or as a URL.
	 */
	public interface IHTMLHoverInfo {
		/**
		 * @return true if the String returned by getHTMLString()
		 *         represents a URL; false if the String contains
		 *         marked-up text.
		 */
		boolean isURL();

		/**
		 * @return The input string to be displayed in the Browser
		 *         widget (either as marked-up text, or as a URL.)
		 */
		String getHTMLString();
	}

	private Browser fBrowser;
	private boolean fIsURL;

	/**
	 * Creates a TextHoverControl with the given shell as parent.
	 * 
	 * @param parent
	 *                the parent shell
	 */
	public TextHoverControl(final Shell parent) {
		super(parent, true);
		create();
	}

	@Override
	protected void createContent(final Composite parent) {

		try {
			fBrowser = new Browser(getShell(), SWT.H_SCROLL | SWT.WRAP | SWT.MULTI);

			Color color = parent.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
			fBrowser.setBackgroundMode(SWT.INHERIT_FORCE);
			fBrowser.setBackground(color);
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	@Override
	public void setInformation(final String content) {
		fBrowser.setBounds(getShell().getClientArea());
		if (fIsURL) {
			fBrowser.setUrl(content);
		} else {
			fBrowser.setText(content);
		}
	}

	@Override
	public Point computeSizeHint() {
		// final int widthHint= 350;
		final int widthHint = SWT.DEFAULT;
		//
		return getShell().computeSize(widthHint, SWT.DEFAULT | SWT.H_SCROLL, true);
	}

	@Override
	public boolean hasContents() {
		return fBrowser.getText().length() > 0;
	}

	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(final Shell parent) {
				return new TextHoverControl(parent);
			}
		};
	}

	public void setInput(final Object input) {
		// Assume that the input is marked-up text, not a URL
		fIsURL = false;
		final String inputString;

		if (input instanceof IHTMLHoverInfo) {
			// Get the input string, then see whether it's a URL
			IHTMLHoverInfo inputInfo = (IHTMLHoverInfo) input;
			inputString = inputInfo.getHTMLString();
			fIsURL = inputInfo.isURL();
		} else if (input instanceof String) {
			// Treat the String as marked-up text to be displayed.
			inputString = (String) input;
		} else {
			// For any other kind of object, just use its string
			// representation as text to be displayed.
			inputString = input.toString();
		}
		setInformation(inputString);
	}
}
