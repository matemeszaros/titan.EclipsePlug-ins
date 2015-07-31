/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titanium.error.GUIErrorHandler;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeColours;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.generators.GraphGenerator;
import org.eclipse.titanium.graph.gui.common.CustomVisualizationViewer;
import org.eclipse.titanium.graph.gui.common.Layouts;
import org.eclipse.titanium.graph.gui.dialogs.ExportPreferencesDialog;
import org.eclipse.titanium.graph.gui.utils.LayoutEntry;
import org.eclipse.titanium.graph.utils.CheckParallelPaths;
import org.eclipse.titanium.graph.utils.CircleCheck;
import org.eclipse.titanium.graph.visualization.BadLayoutException;
import org.eclipse.titanium.graph.visualization.ErrorType;
import org.eclipse.titanium.graph.visualization.GraphHandler;
import org.eclipse.titanium.graph.visualization.GraphHandler.ImageExportType;
import org.eclipse.titanium.gui.FindWindow;
import org.eclipse.titanium.gui.Searchable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.EditorPart;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * This class is an {@link EditorPart} that shows the dependency graph.<br />
 * <b>Important note: </b> You must set the {@link #handler} and
 * {@link #generator} attributes in the {@link #createPartControl(Composite)}
 * method of subclasses, otherwise <b>you may have null pointer exceptions</b>
 * 
 * @author Gabor Jenei
 */
public abstract class GraphEditor extends EditorPart implements Searchable<NodeDescriptor>{
	protected DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> graph;
	protected GraphHandler handler;
	protected Transformer<NodeDescriptor, String> labeler;
	protected JPanel drawArea;
	protected Frame window;
	protected Dimension windowSize;
	protected LayoutEntry chosenLayout;
	protected IProject project;
	protected SatelliteView satView = null;
	protected Composite editorComposite = null;
	protected FindWindow<NodeDescriptor> wndFind = null;
	protected IHandlerService handlerService = null;
	protected Set<AbstractHandler> handlers = null;
	protected JMenu layoutMenu;
	protected ActionListener layoutListener;
	protected ButtonGroup layoutGroup;
	protected JMenuBar menuBar;
	protected GraphGenerator generator;
	protected final GUIErrorHandler errorHandler;

	private static final String ID = "org.eclipse.titanium.graph.editors.GraphEditor";
	public static final String GRAPH_CONTEXT_ID = "org.eclipse.titanium.contexts.GraphContext";
	public static final String GRAPH_SAVECMD_ID = "org.eclipse.titanium.commands.GraphSave";
	public static final String GRAPH_EXPORTCMD_ID = "org.eclipse.titanium.commands.GraphExport";
	public static final String GRAPH_SEARCHCMD_ID = "org.eclipse.titanium.commands.GraphSearch";
	protected static final String LOGENTRYNOTE = " (see error log for further information)";

	/**
	 * It creates a module dependency graph window, this method mustn't be
	 * called invidually.
	 */
	public GraphEditor() {
		super();
		handlers = new HashSet<AbstractHandler>();
		chosenLayout = Layouts.LAYOUT_ISOM.clone();
		errorHandler = new GUIErrorHandler();
	}

	/**
	 * We have to dispose the windows shown on our Editor
	 */
	@Override
	public void dispose() {
		super.dispose();
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (window != null) {
					window.dispose();
				}
			}
		});

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (wndFind != null) {
					wndFind.close();
				}
			}
		});

		IWorkbenchWindow wind = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (wind == null) {
			return;
		}
		IEditorReference[] editors = wind.getActivePage().findEditors(null, GraphEditor.ID, IWorkbenchPage.MATCH_ID);
		if (editors != null && editors.length == 0 && satView != null) {
			satView.setEditor(null);
			satView.clear();
		}

		for (AbstractHandler hnd : handlers) {
			hnd.dispose();
		}
	}

	/**
	 * {@link #doSave(IProgressMonitor)} method is empty, not used to do save
	 */
	@Override
	public void doSave(final IProgressMonitor monitor) {
	}

	/**
	 * {@link #doSaveAs()} method is empty, not used for saving operations
	 */
	@Override
	public void doSaveAs() {
	}

	/**
	 * We just store the input and site to catch events
	 * 
	 * @param input
	 *            : the input to set
	 * @param site
	 *            : the site to set
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		IContextService contextService = (IContextService) site.getService(IContextService.class);
		contextService.activateContext(GRAPH_CONTEXT_ID);
		handlerService = (IHandlerService) site.getService(IHandlerService.class);
		setSite(site);
		setInput(input);
	}

	/**
	 * This {@link EditorPart} is never dirty, so return value is constant false
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/**
	 * As the save methods are empty this functions returns constant false
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * This method generates the dependency graph and sets an {@link EditorPart}
	 * to show the graph's window.
	 */
	@Override
	public void createPartControl(final Composite parent) {
		initGeneratorAndHandler(parent);
		editorComposite = new Composite(parent, SWT.NO_BACKGROUND | SWT.EMBEDDED);
		window = SWT_AWT.new_Frame(editorComposite);
		windowSize = new Dimension(parent.getSize().x, parent.getSize().y);

		parent.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				Point tmpSize = parent.getSize();
				windowSize = new Dimension(tmpSize.x, tmpSize.y);
				if (handler != null) {
					handler.changeWindowSize(windowSize);
				}
				if (window != null && drawArea != null) {
					drawArea.setPreferredSize(windowSize);
					window.setPreferredSize(windowSize);
					window.repaint();
				}
			}
		});

		project = ((IFileEditorInput) getEditorInput()).getFile().getProject();
		setPartName(getPartName() + " - " + project.getName());

		// get a reference to the satellite viewer
		satView = (SatelliteView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(SatelliteView.ID);
		if (satView != null) {
			satView.setEditor(this);
		} else {
			try {
				satView = (SatelliteView) PlatformUI.getWorkbench().
						getActiveWorkbenchWindow().getActivePage().showView(SatelliteView.ID);
				satView.setEditor(this);
			} catch (PartInitException e) {
				errorHandler.reportException("Error while opening the view", e);
			}
		}
		initWindow();
	}

	/**
	 * This method does the necessary activities to make the editor window
	 * active
	 */
	@Override
	public void setFocus() {
		IWorkbenchWindow tmpWnd = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (tmpWnd != null) {
			satView = (SatelliteView) tmpWnd.getActivePage().findView(SatelliteView.ID);
		}
		final GraphEditor thisEditor = this;
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (handler != null && satView != null) {
					satView.add(handler.getSatelliteViewer());
					satView.setEditor(thisEditor);
				}
			}
		});
	}

	/**
	 * This method creates the items to show on the {@link Frame} , and adds
	 * actions
	 */
	protected void initWindow() {
		drawArea = new JPanel();
		window.add(drawArea, BorderLayout.CENTER);
		drawArea.setSize(windowSize.width, windowSize.height);
		drawArea.setPreferredSize(new Dimension(windowSize.width, windowSize.height));

		menuBar = new JMenuBar();
		window.add(menuBar, BorderLayout.NORTH);

		JMenu mnFile = new JMenu("File");

		ActionListener saveGraph = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = "";
				try {
					path = project.getPersistentProperty(
							new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "Graph_Save_Path"));
				} catch (CoreException exc) {
					errorHandler.reportException("Error while reading persistent property", exc);
				}
				final String oldPath = path;
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						FileDialog dialog = new FileDialog(editorComposite.getShell(), SWT.SAVE);
						dialog.setText("Save Pajek file");
						dialog.setFilterPath(oldPath);
						dialog.setFilterExtensions(new String[] { "*.net", "*.dot" });
						String graphFilePath = dialog.open();
						if (graphFilePath == null) {
							return;
						}
						String newPath = graphFilePath.substring(0, graphFilePath.lastIndexOf(File.separator) + 1);
						try {
							QualifiedName name = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "Graph_Save_Path");
							project.setPersistentProperty(name, newPath);
							
							if ("dot".equals(graphFilePath.substring(graphFilePath.lastIndexOf('.') + 1,
									graphFilePath.length()))) {
								GraphHandler.saveGraphToDot(graph, graphFilePath, project.getName());
							} else {
								GraphHandler.saveGraphToPajek(graph, graphFilePath);
							}
							
						} catch (BadLayoutException be) {
							ErrorReporter.logExceptionStackTrace("Error while saving image to " + newPath, be);
							errorHandler.reportErrorMessage("Bad layout\n\n" + be.getMessage());
						} catch (Exception ce) {
							ErrorReporter.logExceptionStackTrace("Error while saving image to " + newPath, ce);
							errorHandler.reportException("Error while setting persistent property", ce);
						}
					}
				});
			}
		};

		final JMenuItem mntmSave = new JMenuItem("Save (Ctrl+S)");
		mntmSave.addActionListener(saveGraph);
		mnFile.add(mntmSave);

		ActionListener exportImage = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = "";
				try {
					path = project.getPersistentProperty(
							new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "Graph_Save_Path"));
				} catch (CoreException exc) {
					errorHandler.reportException("Error while reading persistent property", exc);
				}
				final String oldPath = path;
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						ExportPreferencesDialog prefDialog = new ExportPreferencesDialog(editorComposite.getShell());
						ImageExportType mode = prefDialog.open();

						FileDialog dialog = new FileDialog(editorComposite.getShell(), SWT.SAVE);
						dialog.setText("Export image");
						dialog.setFilterPath(oldPath);
						dialog.setFilterExtensions(new String[] { "*.png" });
						String graphFilePath = dialog.open();
						if (graphFilePath == null) {
							return;
						}
						String newPath = graphFilePath.substring(0, graphFilePath.lastIndexOf(File.separator) + 1);
						try {
							QualifiedName name = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "Graph_Save_Path");
							project.setPersistentProperty(name, newPath);
							handler.saveToImage(graphFilePath, mode);
						} catch (BadLayoutException be) {
							errorHandler.reportException("Error while saving image", be);
							errorHandler.reportErrorMessage(be.getMessage());
						} catch (CoreException ce) {
							errorHandler.reportException("Error while setting persistent property", ce);
						}
					}
				});
			}
		};

		final JMenuItem mntmExportToImage = new JMenuItem("Export to image file (Ctrl+E)");
		mntmExportToImage.addActionListener(exportImage);
		mnFile.add(mntmExportToImage);

		layoutMenu = new JMenu("Layout");
		layoutGroup = new ButtonGroup();

		layoutListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final IProgressMonitor monitor = Job.getJobManager().createProgressGroup();
				monitor.beginTask("Change layout", 100);
				if (!(e.getSource() instanceof LayoutEntry)) {
					errorHandler.reportErrorMessage("Unexpected error\n\nAn unusual error has been logged" + LOGENTRYNOTE);
					ErrorReporter.logError("The layout changing event's source is not of type \"LayoutEntry\"!");
					return;
				}
				final LayoutEntry layout = (LayoutEntry) e.getSource();
				if (handler.getVisualizator() != null) {
					drawArea.remove(handler.getVisualizator());
				}
				try {
					handler.changeLayout(layout, windowSize);
					drawArea.add(handler.getVisualizator());
					if (satView != null) {
						satView.add(handler.getSatelliteViewer());
					}
					window.pack();
				} catch (BadLayoutException exc) {
					layout.setSelected(false);
					chosenLayout.setSelected(true);
					if (exc.getType() == ErrorType.EMPTY_GRAPH || exc.getType() == ErrorType.NO_OBJECT) {
						return;
					}
					try {
						handler.changeLayout(chosenLayout, windowSize);
						drawArea.add(handler.getVisualizator());
						if (satView != null) {
							satView.add(handler.getSatelliteViewer());
						}
						window.pack();
						monitor.done();
					} catch (BadLayoutException exc2) {
						monitor.done();
						if (exc2.getType() != ErrorType.CYCLIC_GRAPH && exc2.getType() != ErrorType.EMPTY_GRAPH) {
							errorHandler.reportException("Error while creating layout", exc2);
						} else {
							errorHandler.reportErrorMessage(exc2.getMessage());
						}
					} catch (IllegalStateException exc3) {
						monitor.done();
						errorHandler.reportException("Error while creating layout", exc3);
					}
					if (exc.getType() != ErrorType.CYCLIC_GRAPH && exc.getType() != ErrorType.EMPTY_GRAPH) {
						errorHandler.reportException("Error while creating layout", exc);
					} else {
						errorHandler.reportErrorMessage(exc.getMessage());
					}
				} catch (IllegalStateException exc) {
					layout.setSelected(false);
					chosenLayout.setSelected(true);
					try{
						handler.changeLayout(chosenLayout, windowSize);
						drawArea.add(handler.getVisualizator());
						if (satView != null) {
							satView.add(handler.getSatelliteViewer());
						}
						window.pack();
						monitor.done();
					} catch (BadLayoutException exc2) {
						monitor.done();
						if (exc2.getType() != ErrorType.CYCLIC_GRAPH && exc2.getType() != ErrorType.EMPTY_GRAPH) {
							errorHandler.reportException("Error while creating layout", exc2);
						} else {
							errorHandler.reportErrorMessage(exc2.getMessage());
						}
					} catch (IllegalStateException exc3) {
						monitor.done();
						errorHandler.reportException("Error while creating layout", exc3);
					}
					errorHandler.reportException("Error while creating layout", exc);
				}
				chosenLayout = layout.clone();
				monitor.done();
			}
		};

		JMenu findMenu = new JMenu("Find");
		final JMenuItem nodeByName = new JMenuItem("Node by name (Ctrl+F)");

		final GraphEditor thisEditor = this;
		nodeByName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (wndFind != null) {
							wndFind.close();
						}
						try {
							wndFind = new FindWindow<NodeDescriptor>(editorComposite.getShell(), thisEditor, graph.getVertices());
							wndFind.open();
						} catch(IllegalArgumentException e) {
							errorHandler.reportException("", e);
						}
					}
				});
			}
		});

		findMenu.add(nodeByName);

		JMenu tools = new JMenu("Tools");
		final JMenuItem findCircles = new JMenuItem("Show circles");
		final JMenuItem findPaths = new JMenuItem("Show parallel paths");
		final JMenuItem clearResults = new JMenuItem("Clear Results");

		findCircles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				Job circlesJob = new Job("Searching for circles") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						if (graph == null) {
							return null;
						}
						CircleCheck<NodeDescriptor, EdgeDescriptor> checker = 
								new CircleCheck<NodeDescriptor, EdgeDescriptor>(graph);
						if (checker.isCyclic()) {
							for (EdgeDescriptor e : graph.getEdges()) {
								e.setColour(Color.lightGray);
							}
							for (Deque<EdgeDescriptor> st : checker.getCircles()) {
								for (EdgeDescriptor e : st) {
									e.setColour(NodeColours.DARK_RED);
								}
							}
							refresh();
						} else {
							errorHandler.reportInformation("Result:\n\nThis graph is not cyclic!");
						}

						return Status.OK_STATUS;
					} // end run
				}; // end job
				circlesJob.schedule();
			} // end actionPerformed
		});

		findPaths.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				Job pathsJob = new Job("Searching for parallel paths") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						if (graph == null) {
							return null;
						}
						CheckParallelPaths<NodeDescriptor, EdgeDescriptor> checker = null;
						checker = new CheckParallelPaths<NodeDescriptor, EdgeDescriptor>(graph);
						if (checker.hasParallelPaths()) {
							for (EdgeDescriptor e : graph.getEdges()) {
								e.setColour(Color.lightGray);
							}
							for (Deque<EdgeDescriptor> list : checker.getPaths()) {
								for (EdgeDescriptor e : list) {
									e.setColour(NodeColours.DARK_RED);
								}
							}
							refresh();
						} else {
							errorHandler.reportInformation("Result:\n\nThere are no parallel paths in this graph!");
						}

						return Status.OK_STATUS;
					} // end run
				}; // end job
				pathsJob.schedule();
			} // end actionPerformed
		});

		clearResults.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				for (EdgeDescriptor e : graph.getEdges()) {
					e.setColour(Color.black);
				}
				refresh();
			}
		});

		tools.add(findCircles);
		tools.add(findPaths);
		tools.add(clearResults);

		menuBar.add(mnFile);
		menuBar.add(findMenu);
		menuBar.add(tools);
		menuBar.add(layoutMenu);

		// TODO implement refresh action
		/*
		 * JMenuItem RefreshMenu=new JMenuItem("Refresh"); ActionListener
		 * RefreshAction=new ActionListener() { public void
		 * actionPerformed(ActionEvent ev) { GraphGenerator.schedule(); } };
		 * RefreshMenu.addActionListener(RefreshAction);
		 * 
		 * menuBar.add(RefreshMenu);
		 */

		handlerService.activateHandler(GRAPH_SEARCHCMD_ID, new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) throws ExecutionException {
				nodeByName.getActionListeners()[0].actionPerformed(null);
				handlers.add(this);
				return null;
			}
		});

		handlerService.activateHandler(GRAPH_SAVECMD_ID, new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) throws ExecutionException {
				mntmSave.getActionListeners()[0].actionPerformed(null);
				handlers.add(this);
				return null;
			}
		});

		handlerService.activateHandler(GRAPH_EXPORTCMD_ID, new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) throws ExecutionException {
				mntmExportToImage.getActionListeners()[0].actionPerformed(null);
				handlers.add(this);
				return null;
			}
		});

		try {
			generator.generateGraph();
			setLabeller(generator.getLabeler());
			setGraph(generator.getGraph());
		} catch (InterruptedException ex) {
			errorHandler.reportException("Error while creating the graph", ex);
		}

	}

	/**
	 * This method is used to set the initial graph, or set the refreshed graph
	 * 
	 * @param g
	 *            : The graph to set
	 */
	public void setGraph(DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> g) {
		this.graph = g;
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if(drawArea == null) {
					return;
				}

				drawArea.setPreferredSize(windowSize);
				try {
					drawArea.removeAll();
					handler.drawGraph(graph, windowSize, chosenLayout);
					drawArea.add(handler.getVisualizator());
					if (satView != null) {
						satView.add(handler.getSatelliteViewer());
					}
					window.pack();
					recolour(graph.getVertices());
				} catch (BadLayoutException be) {
					ErrorReporter.logExceptionStackTrace("Error while drawing graph", be);
					errorHandler.reportErrorMessage(be.getMessage());
				}
			}
		});
	}

	/**
	 * sets a new labeler
	 * 
	 * @param labeler
	 *            : the labeler to set
	 */
	public void setLabeller(Transformer<NodeDescriptor, String> labeler) {
		this.labeler = labeler;
	}

	/**
	 * This method is called to set the {@link SatelliteView}, this is needed to
	 * be able to run modifying actions.
	 * 
	 * @param sat
	 *            : The satellite viewer (<b>this should be unique in every
	 *            workspace</b>)
	 */
	public void setSatellite(SatelliteView sat) {
		satView = sat;
		if (satView != null && handler != null) {
			satView.add(handler.getSatelliteViewer());
		}
	}

	/**
	 * @return returns the graph shown in the editor
	 */
	public DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> getGraph() {
		return graph;
	}

	/**
	 * This method causes both the main graph window and the satellite view to
	 * refresh. This method is thread safe!
	 */
	public void refresh() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				drawArea.repaint();
				if (satView != null) {
					satView.repaint();
				}
			}
		});
	}

	/**
	 * This method should make the changes visible in the inner representation
	 * of the graph. Currently it generates a totally new graph from an empty
	 * one.
	 */
	public void refreshGraph() {
		if (generator == null) {
			return;
		}
		try {
			generator.generateGraph();
			setLabeller(generator.getLabeler());
			setGraph(generator.getGraph());
		} catch (InterruptedException ex) {
			errorHandler.reportException("Error while refreshing the graph", ex);
		}
	}

	/**
	 * @return Returns the shown project
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * @return Returns the graph handler used for drawing the graph
	 */
	public GraphHandler getHandler() {
		return handler;
	}
	
	@Override
	public void setResults(Collection<NodeDescriptor> results){
		clearResults();
		addResults(results);
	}
	
	@Override
	public void clearResults(){
		recolour(graph.getVertices());
	}
	
	@Override
	public void addResults(Collection<NodeDescriptor> results){
		for (NodeDescriptor node : results) {
			node.setNodeColour(NodeColours.RESULT_COLOUR);
		}
	}
	
	@Override
	public void elemChosen(NodeDescriptor element){
		CustomVisualizationViewer visualisator = handler.getVisualizator();
		visualisator.jumpToPlace(visualisator.getGraphLayout().transform(element));
		
		for (NodeDescriptor node : graph.getVertices()) {
			node.setNodeColour(NodeColours.NOT_RESULT_COLOUR);
		}
		element.setNodeColour(NodeColours.RESULT_COLOUR);
	}
	
	

	/**
	 * This method recolours a given set of nodes inside the graph
	 * 
	 * @param nodeSet
	 *            : The set of nodes
	 */
	public abstract void recolour(Collection<NodeDescriptor> nodeSet);

	/**
	 * This method implements the initialization of generator and handler
	 * attributes, for further details see {@link GraphGenerator} and
	 * {@link GraphHandler}
	 * 
	 * @param parent
	 *            : A reference to the parent shell
	 */
	protected abstract void initGeneratorAndHandler(final Composite parent);

}
