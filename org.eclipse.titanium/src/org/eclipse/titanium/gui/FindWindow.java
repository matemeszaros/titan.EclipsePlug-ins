/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.titanium.error.GUIErrorHandler;
import org.eclipse.titanium.swt.SWTResourceManager;

/**
 * This class implements a node searching Eclipse window, it is inherited from
 * {@link Dialog}
 * 
 * @author Gabor Jenei
 */
@SuppressWarnings("rawtypes")
public class FindWindow<T extends Comparable> extends Dialog {

	protected Shell shlFind;
	private static final Dimension EXT_SIZE = new Dimension(344, 317);
	private static final Dimension NORM_SIZE = new Dimension(344, 135);
	private final Searchable<T> view;
	private final Collection<T> totalSet;
	private Label label;
	private Label lblResults;
	private Tree tree;
	private SortedSet<T> treeItems;
	protected final GUIErrorHandler errorHandler = new GUIErrorHandler();

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 *            : The parent shell
	 * @param view
	 *            : reference to the opener editor window
	 * @param totalList
	 * 			  : The whole set to search on
	 * @exception On illegal arguments
	 */
	public FindWindow(final Shell parent, final Searchable<T> view, final Collection<T> totalList) throws IllegalArgumentException {
		super(parent);
		if (view==null) {
			throw new IllegalArgumentException("The totalList parameter of FindWindow's constructor mustn't be null!");
		}
		if (totalList==null) {
			throw new IllegalArgumentException("The totalList parameter of FindWindow's constructor mustn't be null!");
		}
		
		this.view = view;
		treeItems = new TreeSet<T>();
		this.totalSet = totalList;
		setText("Find");
	}

	/**
	 * Open the dialog.
	 */
	public void open() {
		createContents();
		shlFind.open();
		shlFind.layout();
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		shlFind.setLocation(new Point(screenSize.width / 2, 20));

		lblResults = new Label(shlFind, SWT.NONE);
		lblResults.setBounds(10, 108, 40, 15);
		lblResults.setText("Results:");

		label = new Label(shlFind, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.CENTER);
		label.setBounds(10, 94, 323, 21);

		tree = new Tree(shlFind, SWT.BORDER);
		tree.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(final SelectionEvent e) {
				view.elemChosen((T) tree.getSelection()[0].getData());
			}
		});
		tree.setBounds(10, 129, 318, 149);
		showResultTable(false);

		final Display display = getParent().getDisplay();
		while (!shlFind.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		view.clearResults();
	}

	/**
	 * Closing the dialog
	 */
	public void close() {
		view.clearResults();
		if (!shlFind.isDisposed()) {
			shlFind.close();
		}
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlFind = new Shell(getParent(), SWT.CLOSE | SWT.TITLE);
		shlFind.setModified(true);
		shlFind.setImage(SWTResourceManager.getImage("resources/icons/search_src.gif"));
		shlFind.setSize(EXT_SIZE.width, EXT_SIZE.height);
		shlFind.setText("Find");
		shlFind.setLayout(null);

		final Text text = new Text(shlFind, SWT.BORDER);
		text.setBounds(10, 23, 323, 21);

		final Button btnExactMatch = new Button(shlFind, SWT.CHECK);
		btnExactMatch.setBounds(10, 50, 93, 16);
		btnExactMatch.setText("Exact match");
		
		final Button btnCaseSensitive = new Button(shlFind, SWT.CHECK);
		btnCaseSensitive.setBounds(10, 72, 93, 16);
		btnCaseSensitive.setText("Case sensitive");
		
		final Button btnFind = new Button(shlFind, SWT.NONE);
		btnFind.setBounds(258, 68, 75, 25);
		btnFind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				String name = text.getText();
				final boolean exactMatch = btnExactMatch.getSelection();
				final boolean caseSensitive = btnCaseSensitive.getSelection();
				boolean noResult = true;
				
				if (!caseSensitive) {
					name = name.toLowerCase();
				}

				for (final T actElem : totalSet) {
					String elemName = actElem.toString();
					if (!caseSensitive) {
						elemName = elemName.toLowerCase();
					}
					
					if (!exactMatch && elemName.contains(name)) {
						treeItems.add(actElem);
					} else if (exactMatch && elemName.equals(name)) {
						treeItems.add(actElem);
					}
				}
				
				for (final T actElem : treeItems) {
					final TreeItem item = new TreeItem(tree, SWT.NONE);
					item.setText(actElem.toString());
					item.setData(actElem);
					noResult = false;
				}

				if (noResult) {
					errorHandler.reportInformation("The search hasn't found such node!");
				} else {
					showResultTable(true);
				}
			}
		});
		btnFind.setText("Find");

		final Label nameLabel = new Label(shlFind, SWT.NONE);
		nameLabel.setBounds(10, 2, 283, 15);
		nameLabel.setText("Name: ");

		final Button btnClearResult = new Button(shlFind, SWT.NONE);
		btnClearResult.setBounds(177, 68, 75, 25);
		btnClearResult.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				showResultTable(false);
				view.clearResults();
			}
		});
		btnClearResult.setText("Clear result");

	}

	private void showResultTable(final boolean show) {
		if (show) {
			shlFind.setSize(EXT_SIZE.width, EXT_SIZE.height);
		} else {
			shlFind.setSize(NORM_SIZE.width, NORM_SIZE.height);
			tree.removeAll();
			treeItems.clear();
		}

		tree.setVisible(show);
		lblResults.setVisible(show);
		label.setVisible(show);
	}
}
