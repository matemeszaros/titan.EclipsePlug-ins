/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.logging;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.titan.common.parsers.AddedParseTree;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingBit;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingBitHelper;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler.LogParamEntry;
import org.eclipse.titan.designer.editors.configeditor.ConfigEditor;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class LoggingBitsSubPage {

	private LoggingSectionHandler loggingSectionHandler;
	private LogParamEntry selectedLogEntry;

	private ConfigEditor editor;
	private LoggingPage loggingPage;

	private CheckboxTreeViewer consoleMaskViewer;
	private CheckboxTreeViewer fileMaskViewer;

	public LoggingBitsSubPage(final ConfigEditor editor, final LoggingPage loggingPage) {
		this.editor = editor;
		this.loggingPage = loggingPage;
	}

	void createSectionLoggingBits(final FormToolkit toolkit, final ScrolledForm form, final Composite parent) {

		Section section = toolkit.createSection(parent, ExpandableComposite.NO_TITLE);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		client.setLayout(layout);
		client.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		section.setClient(client);

		toolkit.paintBordersFor(client);

		createSubSectionConsoleMaskBits(toolkit, form, client);
		createSubSectionFileMaskBits(toolkit, form, client);
	}

	void createSubSectionConsoleMaskBits(final FormToolkit toolkit, final ScrolledForm form, final Composite parent) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		section.setText("Console Log bitmask");
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(false);
			}
		});

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		client.setLayout(layout);
		section.setClient(client);

		toolkit.paintBordersFor(client);

		consoleMaskViewer = new CheckboxTreeViewer(client, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		consoleMaskViewer.setContentProvider(new LoggingBitsContentProvider());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 230;
		consoleMaskViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		consoleMaskViewer.setLabelProvider(new LoggingBitsLabelProvider());
		consoleMaskViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(final CheckStateChangedEvent event) {
				if (event.getChecked() && selectedLogEntry.getConsoleMaskRoot() == null) {
					createConsoleMaskRootNode(loggingPage.getSelectedTreeElement(), selectedLogEntry);
				}

				checkStateChangeHandler(selectedLogEntry.getConsoleMaskBits(), selectedLogEntry.getConsoleMask(), event);
				bitCompression(selectedLogEntry.getConsoleMaskBits(), selectedLogEntry.getConsoleMask());
				evaluateSelection(consoleMaskViewer, selectedLogEntry.getConsoleMaskBits());

				if (!event.getChecked()) {
					if (selectedLogEntry.getConsoleMaskBits().keySet().isEmpty()) {
						selectedLogEntry.setConsoleMaskRoot(null);
					}
				}
			}
		});

		consoleMaskViewer.getTree().addListener(SWT.MouseHover, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				TreeItem item = consoleMaskViewer.getTree().getItem(new Point(event.x, event.y));
				if (item != null) {
					if (item.getData() instanceof LoggingBit) {
						consoleMaskViewer.getTree().setToolTipText(((LoggingBit) item.getData()).getToolTip());
						return;
					}
				}
				consoleMaskViewer.getTree().setToolTipText("");
			}
		});

		consoleMaskViewer.addTreeListener(new ITreeViewerListener() {
			@Override
			public void treeCollapsed(final TreeExpansionEvent event) {
				//Do nothing
			}

			@Override
			public void treeExpanded(final TreeExpansionEvent event) {
				evaluateSelection(consoleMaskViewer, selectedLogEntry.getConsoleMaskBits());
			}
		});

		if (selectedLogEntry == null) {
			consoleMaskViewer.getControl().setEnabled(false);
			consoleMaskViewer.setInput(new EnumMap<LoggingBit, ParseTree>(LoggingBit.class));
		} else {
			consoleMaskViewer.getControl().setEnabled(true);
			consoleMaskViewer.setInput(selectedLogEntry.getConsoleMaskBits());
			bitCompression(selectedLogEntry.getConsoleMaskBits(), selectedLogEntry.getConsoleMask());
			evaluateSelection(consoleMaskViewer, selectedLogEntry.getConsoleMaskBits());
		}
	}

	void createSubSectionFileMaskBits(final FormToolkit toolkit, final ScrolledForm form, final Composite parent) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		section.setText("File Log bitmask");
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(false);
			}
		});

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		client.setLayout(layout);
		section.setClient(client);

		toolkit.paintBordersFor(client);

		fileMaskViewer = new CheckboxTreeViewer(client, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		fileMaskViewer.setContentProvider(new LoggingBitsContentProvider());
		fileMaskViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fileMaskViewer.setLabelProvider(new LoggingBitsLabelProvider());
		fileMaskViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(final CheckStateChangedEvent event) {
				if (event.getChecked() && selectedLogEntry.getFileMaskRoot() == null) {
					createConsoleMaskRootNode(loggingPage.getSelectedTreeElement(), selectedLogEntry);
				}

				checkStateChangeHandler(selectedLogEntry.getFileMaskBits(), selectedLogEntry.getFileMask(), event);
				bitCompression(selectedLogEntry.getFileMaskBits(), selectedLogEntry.getFileMask());
				evaluateSelection(fileMaskViewer, selectedLogEntry.getFileMaskBits());

				if (!event.getChecked()) {
					if (selectedLogEntry.getFileMaskBits().keySet().isEmpty()) {
						selectedLogEntry.setFileMaskRoot(null);
					}
				}
			}
		});

		fileMaskViewer.getTree().addListener(SWT.MouseHover, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				TreeItem item = fileMaskViewer.getTree().getItem(new Point(event.x, event.y));
				if (item != null) {
					if (item.getData() instanceof LoggingBit) {
						fileMaskViewer.getTree().setToolTipText(((LoggingBit) item.getData()).getToolTip());
						return;
					}
				}
				fileMaskViewer.getTree().setToolTipText("");
			}
		});

		fileMaskViewer.addTreeListener(new ITreeViewerListener() {
			@Override
			public void treeCollapsed(final TreeExpansionEvent event) {
				//Do nothing
			}

			@Override
			public void treeExpanded(final TreeExpansionEvent event) {
				evaluateSelection(fileMaskViewer, selectedLogEntry.getFileMaskBits());
			}
		});

		if (selectedLogEntry == null) {
			fileMaskViewer.getControl().setEnabled(false);
			fileMaskViewer.setInput(new EnumMap<LoggingBit, ParseTree>(LoggingBit.class));
		} else {
			fileMaskViewer.getControl().setEnabled(true);
			fileMaskViewer.setInput(selectedLogEntry.getFileMaskBits());
			bitCompression(selectedLogEntry.getFileMaskBits(), selectedLogEntry.getFileMask());
			evaluateSelection(fileMaskViewer, selectedLogEntry.getFileMaskBits());
		}
	}

	private void createConsoleMaskRootNode(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry) {
		ParseTree consoleMaskRoot = new ParserRuleContext();
		logentry.setConsoleMaskRoot( consoleMaskRoot );
		ConfigTreeNodeUtilities.addChild( consoleMaskRoot, new AddedParseTree("\n") );

		StringBuilder name = new StringBuilder();
		lte.writeNamePrefix(name);
		name.append("ConsoleMask := ");

		ParseTree node = new AddedParseTree(name.toString());
		ConfigTreeNodeUtilities.addChild( consoleMaskRoot, node );
		
		ParseTree consoleMask = new ParserRuleContext();
		logentry.setConsoleMask( consoleMask );
		ConfigTreeNodeUtilities.addChild( consoleMaskRoot, consoleMask );
		ConfigTreeNodeUtilities.addChild( loggingSectionHandler.getLastSectionRoot(), consoleMaskRoot );
	}

	private void createFileMaskRootNode(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry) {
		ParseTree fileMaskRoot = new ParserRuleContext();
		logentry.setConsoleMaskRoot( fileMaskRoot );
		ConfigTreeNodeUtilities.addChild( fileMaskRoot, new AddedParseTree("\n") );

		StringBuilder name = new StringBuilder();
		lte.writeNamePrefix(name);
		name.append("FileMask := ");

		ParseTree node = new AddedParseTree(name.toString());
		ConfigTreeNodeUtilities.addChild( fileMaskRoot, node );
		
		ParseTree fileMask = new ParserRuleContext();
		logentry.setFileMask( fileMask );
		ConfigTreeNodeUtilities.addChild( fileMaskRoot, fileMask );
		ConfigTreeNodeUtilities.addChild( loggingSectionHandler.getLastSectionRoot(), fileMaskRoot );
	}

	private void checkStateChangeHandler(final Map<LoggingBit, ParseTree> bitMask, final ParseTree bitmaskRoot,
			final CheckStateChangedEvent event) {
		editor.setDirty();

		LoggingBit bit = (LoggingBit) event.getElement();

		if (LoggingBitHelper.hasChildren(bit)) {
			LoggingBit[] children = LoggingBitHelper.getChildren(bit);

			if (bitMask.containsKey(bit)) {
				removeLoggingBit(bitMask, bit);
			} else {
				addLoggingBit(bitMask, bitmaskRoot, bit);
			}

			for (int i = 0; i < children.length; i++) {
				if (bitMask.containsKey(children[i])) {
					removeLoggingBit(bitMask, children[i]);
				}
			}

			removeFirstSeparator(bitmaskRoot);
		} else {
			if (bitMask.containsKey(bit)) {
				removeLoggingBit(bitMask, bit);
				removeFirstSeparator(bitmaskRoot);
			} else {
				LoggingBit parent = LoggingBitHelper.getParent(bit);
				if (parent == null) {
					return;
				}

				LoggingBit[] children = LoggingBitHelper.getChildren(parent);
				int elementCount = 0;
				for (LoggingBit child : children) {
					if (bitMask.containsKey(child)) {
						elementCount++;
					}
				}

				if (bitMask.containsKey(parent)) {
					removeLoggingBit(bitMask, parent);
					removeFirstSeparator(bitmaskRoot);

					for (int i = 0; i < children.length; i++) {
						if (!bitMask.containsKey(children[i]) && !bit.equals(children[i])) {
							addLoggingBit(bitMask, bitmaskRoot, children[i]);
						}
					}
				} else if (elementCount == children.length - 1) {
					addLoggingBit(bitMask, bitmaskRoot, parent);

					for (int i = 0; i < children.length; i++) {
						if (bitMask.containsKey(children[i])) {
							removeLoggingBit(bitMask, children[i]);
						}
					}

					removeFirstSeparator(bitmaskRoot);
				} else {
					addLoggingBit(bitMask, bitmaskRoot, bit);
				}
			}
		}
	}

	private void bitCompression(final Map<LoggingBit, ParseTree> bitMask, final ParseTree bitmaskRoot) {
		for (LoggingBit parent : LoggingBitHelper.getFirstLevelNodes()) {
			LoggingBit[] children = LoggingBitHelper.getChildren(parent);

			if (bitMask.containsKey(parent)) {
				for (int i = 0; i < children.length; i++) {
					if (bitMask.containsKey(children[i])) {
						removeLoggingBit(bitMask, children[i]);
					}
				}

				removeFirstSeparator(bitmaskRoot);
			} else {
				int childCount = 0;

				for (LoggingBit child : children) {
					if (bitMask.containsKey(child)) {
						childCount++;
					}
				}

				if (childCount == children.length) {
					addLoggingBit(bitMask, bitmaskRoot, parent);

					for (int i = 0; i < children.length; i++) {
						if (bitMask.containsKey(children[i])) {
							removeLoggingBit(bitMask, children[i]);
						}
					}

					removeFirstSeparator(bitmaskRoot);
				}
			}
		}
	}

	private void addLoggingBit(final Map<LoggingBit, ParseTree> bitMask, final ParseTree bitmaskRoot, final LoggingBit bit) {
		ParseTree newBit = new AddedParseTree(bit.getName());
		bitMask.put(bit, newBit);

		if (bitMask.keySet().size() > 1) {
			ParseTree separator = new AddedParseTree("|");
			ConfigTreeNodeUtilities.addChild( bitmaskRoot, separator );
		}

		ConfigTreeNodeUtilities.addChild( bitmaskRoot, newBit );
	}

	private void removeLoggingBit(final Map<LoggingBit, ParseTree> bitMask, final LoggingBit bit) {
		final ParseTree removedBit = bitMask.remove(bit);
		ConfigTreeNodeUtilities.removeChild(removedBit);
	}

	/**
	 * Removes separator if found at position 0 after deletion
	 * @param bitmaskRoot root parse tree
	 */
	private void removeFirstSeparator(final ParseTree bitmaskRoot) {
		final int count = bitmaskRoot.getChildCount();
		if ( count > 0 ) {
			final ParseTree child = bitmaskRoot.getChild( 0 );
			final String childText = child.getText();
			if ( "".equals( childText ) || "|".equals( childText ) ) {
				ConfigTreeNodeUtilities.removeChild( bitmaskRoot, child );
			}
		}
	}

	private void evaluateSelection(final CheckboxTreeViewer viewer, final Map<LoggingBit, ParseTree> bitMask) {
		LoggingBit[] firstLevelnodes = LoggingBitHelper.getFirstLevelNodes();
		LoggingBit[] children;
		List<LoggingBit> toBeGrayed = new ArrayList<LoggingBit>();
		List<LoggingBit> toBeSelected = new ArrayList<LoggingBit>();
		toBeSelected.addAll(bitMask.keySet());

		for (LoggingBit firstLevelnode : firstLevelnodes) {
			children = LoggingBitHelper.getChildren(firstLevelnode);
			int count = 0;
			for (LoggingBit child : children) {
				if (bitMask.containsKey(child)) {
					count++;
				}
			}

			if (count != 0) {
				toBeGrayed.add(firstLevelnode);
				toBeSelected.add(firstLevelnode);
			}

			if (bitMask.containsKey(firstLevelnode)) {
				for (LoggingBit child : children) {
					toBeSelected.add(child);
				}
			}
		}

		viewer.setGrayedElements(toBeGrayed.toArray());
		viewer.setCheckedElements(toBeSelected.toArray());
	}

	private void logAllHandler(final Map<LoggingBit, ParseTree> bitMask, final ParseTree bitmaskRoot) {
		if (bitMask.containsKey(LoggingBit.LOG_ALL)) {
			LoggingBit[] logAllBits = LoggingBitHelper.getLogAllBits();
			for (LoggingBit bit : logAllBits) {
				if (!bitMask.containsKey(bit)) {
					addLoggingBit(bitMask, bitmaskRoot, bit);
				}
			}

			removeLoggingBit(bitMask, LoggingBit.LOG_ALL);
			removeFirstSeparator(bitmaskRoot);
		}
	}

	public void pluginRenamed() {
		if (selectedLogEntry.getConsoleMaskRoot() != null) {
			ParseTree child = selectedLogEntry.getConsoleMask().getChild( 0 );
			ConfigTreeNodeUtilities.removeChild( loggingSectionHandler.getLastSectionRoot(), selectedLogEntry.getConsoleMaskRoot() );
			createConsoleMaskRootNode(loggingPage.getSelectedTreeElement(), selectedLogEntry);
			if (child != null) {
				ConfigTreeNodeUtilities.addChild( selectedLogEntry.getConsoleMask(), child, 0 );
			}
		}
		if (selectedLogEntry.getFileMaskRoot() != null) {
			ParseTree child = selectedLogEntry.getFileMask().getChild( 0 );
			ConfigTreeNodeUtilities.removeChild( loggingSectionHandler.getLastSectionRoot(), selectedLogEntry.getFileMaskRoot() );
			createFileMaskRootNode(loggingPage.getSelectedTreeElement(), selectedLogEntry);
			if (child != null) {
				ConfigTreeNodeUtilities.addChild( selectedLogEntry.getFileMask(), child, 0 );
			}
		}
	}

	private void internalRefresh() {
		consoleMaskViewer.setSubtreeChecked(consoleMaskViewer.getInput(), false);
		fileMaskViewer.setSubtreeChecked(fileMaskViewer.getInput(), false);

		if (selectedLogEntry == null) {
			consoleMaskViewer.getControl().setEnabled(false);
			fileMaskViewer.getControl().setEnabled(false);
		} else {
			Object[] expandedElements = consoleMaskViewer.getExpandedElements();
			consoleMaskViewer.setInput(selectedLogEntry.getConsoleMaskBits());
			consoleMaskViewer.setExpandedElements(expandedElements);
			logAllHandler(selectedLogEntry.getConsoleMaskBits(), selectedLogEntry.getConsoleMask());
			consoleMaskViewer.getControl().setEnabled(true);
			bitCompression(selectedLogEntry.getConsoleMaskBits(), selectedLogEntry.getConsoleMask());
			evaluateSelection(consoleMaskViewer, selectedLogEntry.getConsoleMaskBits());

			expandedElements = fileMaskViewer.getExpandedElements();
			fileMaskViewer.setInput(selectedLogEntry.getFileMaskBits());
			fileMaskViewer.setExpandedElements(expandedElements);
			logAllHandler(selectedLogEntry.getFileMaskBits(), selectedLogEntry.getFileMask());
			fileMaskViewer.getControl().setEnabled(true);
			bitCompression(selectedLogEntry.getFileMaskBits(), selectedLogEntry.getFileMask());
			evaluateSelection(fileMaskViewer, selectedLogEntry.getFileMaskBits());

		}
	}

	public void initializeEntry(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry) {
		createFileMaskRootNode(lte, logentry);
		addLoggingBit(logentry.getFileMaskBits(), logentry.getFileMask(), LoggingBit.LOG_ALL);
		logAllHandler(logentry.getConsoleMaskBits(), logentry.getConsoleMask());

		createConsoleMaskRootNode(lte, logentry);
		addLoggingBit(logentry.getConsoleMaskBits(), logentry.getConsoleMask(), LoggingBit.ERROR);
		addLoggingBit(logentry.getConsoleMaskBits(), logentry.getConsoleMask(), LoggingBit.WARNING);
		addLoggingBit(logentry.getConsoleMaskBits(), logentry.getConsoleMask(), LoggingBit.ACTION);
		addLoggingBit(logentry.getConsoleMaskBits(), logentry.getConsoleMask(), LoggingBit.TESTCASE);
		addLoggingBit(logentry.getConsoleMaskBits(), logentry.getConsoleMask(), LoggingBit.STATISTICS);
	}

	public void refreshData(final LoggingSectionHandler loggingSectionHandler, final LogParamEntry logentry) {
		this.loggingSectionHandler = loggingSectionHandler;
		this.selectedLogEntry = logentry;

		if (consoleMaskViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					internalRefresh();
				}
			});
		}
	}
}
