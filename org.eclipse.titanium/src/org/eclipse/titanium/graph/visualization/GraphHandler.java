/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.visualization;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.collections15.Transformer;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titanium.error.GUIErrorHandler;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.EdgeStroke;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.gui.common.CustomSatelliteViewer;
import org.eclipse.titanium.graph.gui.common.CustomVisualizationViewer;
import org.eclipse.titanium.graph.gui.common.Layouts;
import org.eclipse.titanium.graph.gui.menus.NodePopupMenu;
import org.eclipse.titanium.graph.gui.utils.LayoutEntry;
import org.eclipse.titanium.graph.utils.GraphVizWriter;
import org.eclipse.titanium.metrics.IMetricEnum;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.io.PajekNetWriter;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * This class provides tools for jung graph visualization
 * 
 * @author Gabor Jenei
 */
public class GraphHandler {
	public enum ImageExportType {
		/**
		 * Select this mode to export the whole graph, in this case the
		 * image file maybe bigger than the set screen size
		 */
		EXPORT_WHOLE_GRAPH,
		/** Select this mode to export only the seen part of the graph */
		EXPORT_SEEN_GRAPH,
		/** Select this mode to export the Satellite View */
		EXPORT_SATELLITE
	}

	private DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> g;
	private static final Transformer<NodeDescriptor, String> NODE_LABELER=new ToStringLabeller<NodeDescriptor>();
	
	private CustomVisualizationViewer actVisualisator;
	private Layout<NodeDescriptor, EdgeDescriptor> layout;
	private GraphRenderer<NodeDescriptor, EdgeDescriptor> renderer;
	protected NodePopupMenu popupMenu;
	protected IMetricEnum chosenLayoutMetric = null;
	private CustomSatelliteViewer satView;
	private Set<Set<NodeDescriptor>> clusters;



	/**
	 * This method creates an empty GraphHandler, it can be used to oversee the
	 * graph drawing mechanism, like Layout changes, storing graph on the disk,
	 * colorize and etc.
	 * 
	 * @see The public methods of {@link GraphHandler}
	 */
	public GraphHandler() {
		popupMenu = new NodePopupMenu(this);
	}

	/**
	 * This function changes the layout for the graph set in the
	 * {@link GraphHandler} class
	 * 
	 * @param newLayout
	 *            : The chosen layout's code
	 * @param newWindowSize
	 *            : The size of the parent window where to draw (or any
	 *            resolution bigger than this)
	 * @throws BadLayoutException On wrong layout code or bad graph
	 */
	public void changeLayout(LayoutEntry newLayout, Dimension newWindowSize) throws BadLayoutException {
		if (g == null) {
			throw new BadLayoutException("You must draw a graph before!", ErrorType.NO_OBJECT);
		}
		Dimension extSize = null;

		if (g.getVertexCount() >= 20) {
			extSize = new Dimension(newWindowSize.height * (g.getVertexCount() / 20), newWindowSize.width * (g.getVertexCount() / 20));
		} else {
			extSize = newWindowSize;
		}
		
		layout = new LayoutBuilder(g, newLayout, extSize).clusters(clusters).build();
		
		actVisualisator = new CustomVisualizationViewer(layout, popupMenu);
		actVisualisator.setPreferredSize(new Dimension(newWindowSize.width, newWindowSize.height));
		actVisualisator.getRenderContext().setVertexLabelTransformer(NODE_LABELER);

		GraphRenderer<NodeDescriptor, EdgeDescriptor> rnd = new GraphRenderer<NodeDescriptor, EdgeDescriptor>(NODE_LABELER,
				actVisualisator.getPickedVertexState(), actVisualisator.getPickedEdgeState());
		setNodeRenderer(rnd, actVisualisator);
		renderer = rnd;
		actVisualisator.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		actVisualisator.setBackground(Color.white);
		actVisualisator.setDoubleBuffered(false);

		initSatView();
	}

	/**
	 * This method makes displayable components from a memory stored graph.
	 * 
	 * @param g
	 *            : A Jung graph to draw
	 * @param NODE_LABELER
	 *            : A node name <-> node label translator
	 * @param windowSize
	 *            : The size of the parent window (maybe more)
	 * @param layout
	 *            : The layout to use for the drawing
	 * @return A {@link Component} containing the graph parameter graph drawn.
	 * @throws Exception
	 */
	public void drawGraph(DirectedSparseGraph<NodeDescriptor, EdgeDescriptor> g, Dimension windowSize, LayoutEntry layout) 
			throws BadLayoutException {
		if (g == null) {
			throw new BadLayoutException("There is no graph (it is set null)", ErrorType.NO_OBJECT);
		}
		if (g.getVertexCount() == 0) {
			throw new BadLayoutException("The graph doesn't contain any node", ErrorType.EMPTY_GRAPH);
		}
		this.g = g;
		changeLayout(layout, windowSize);
	}

	/**
	 * Saves a Jung graph to a Pajek .net file
	 * 
	 * @param g
	 *            : The graph to save
	 * @param path
	 *            : The save path
	 * @throws BadLayoutException
	 *             on file handling error and own inner problems
	 */
	public static void saveGraphToPajek(Graph<NodeDescriptor, EdgeDescriptor> g, String path)
			throws BadLayoutException {
		if (g == null) {
			throw new BadLayoutException("You must draw a graph before!", ErrorType.NO_OBJECT);
		}

		Transformer<EdgeDescriptor, Number> edgeWeights = new Transformer<EdgeDescriptor, Number>() {
			@Override
			public Number transform(EdgeDescriptor e) {
				return e.getWeight();
			}
		};
		
		try{
			PajekNetWriter<NodeDescriptor, EdgeDescriptor> writer = new PajekNetWriter<NodeDescriptor, EdgeDescriptor>();
			writer.save(g, path, NODE_LABELER, edgeWeights);
		} catch (IOException e) {
			throw new BadLayoutException("An error occured during writing to the output file.", ErrorType.IO_ERROR, e);
		}
	}
	
	/**
	 * Saves a Jung graph to a .dot file
	 * 
	 * @param g
	 *            : The graph to save
	 * @param path
	 *            : The save path
	 * @param graphName
	 * 			  : The graph name to use in the output file
	 * @throws Exception
	 *             on file handling error
	 */
	public static void saveGraphToDot(Graph<NodeDescriptor, EdgeDescriptor> g, String path, 
			String graphName) throws BadLayoutException {
		
		if (g == null) {
			throw new BadLayoutException("You must draw a graph before!", ErrorType.NO_OBJECT);
		}

		try {
			new GraphVizWriter<NodeDescriptor, EdgeDescriptor>().save(g, path, NODE_LABELER, graphName);
		} catch (IOException e) {
			throw new BadLayoutException("An error occured during writing to the output file!", ErrorType.IO_ERROR, e);
		}
	}

	/**
	 * Exports the graph set for this class to a PNG file
	 * 
	 * @param path
	 *            : The PNG file's path
	 * @param mode
	 *            : The way of export, see {@link GraphHandler}
	 *            <code>public static</code> fields for possible values (EXPORT_
	 *            named fields)
	 * @param size
	 *            : This parameter sets the size of the exported image in pixels
	 * @throws Exception
	 *             on file handling error
	 */
	public void saveToImage(String path, ImageExportType mode) throws BadLayoutException {
		if (layout == null || actVisualisator == null) {
			throw new BadLayoutException("Either the layout or the visuaizer is not set (is null)", ErrorType.NO_OBJECT);
		}

		VisualizationViewer<NodeDescriptor, EdgeDescriptor> tempVisualisator = null;
		Dimension size = null;
		switch(mode) {
		case EXPORT_SEEN_GRAPH : {
			tempVisualisator = actVisualisator;
			size = actVisualisator.getPreferredSize();
		}
		break;
		case EXPORT_WHOLE_GRAPH: {
			layout = actVisualisator.getGraphLayout();
			if (size == null) {
				size = new Dimension(layout.getSize().width, layout.getSize().height);
			}
			
			Transformer<NodeDescriptor, Point2D> trf = new Transformer<NodeDescriptor, Point2D>() {
				@Override
				public Point2D transform(NodeDescriptor v) {
					return layout.transform(v);
				}
			};
			
			tempVisualisator = new VisualizationViewer<NodeDescriptor, EdgeDescriptor>(
					new LayoutBuilder(g, Layouts.LAYOUT_STATIC, size).transformer(trf).build());
			tempVisualisator.setPreferredSize(size);
			tempVisualisator.setSize(size);
			tempVisualisator.getRenderContext().setVertexLabelTransformer(NODE_LABELER);
			
			GraphRenderer<NodeDescriptor, EdgeDescriptor> rnd = new GraphRenderer<NodeDescriptor, EdgeDescriptor>(NODE_LABELER,
					tempVisualisator.getPickedVertexState(), tempVisualisator.getPickedEdgeState());
			setNodeRenderer(rnd, tempVisualisator);
			tempVisualisator.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
			tempVisualisator.setBackground(Color.white);
			tempVisualisator.setDoubleBuffered(false);
		}
		break;
		case EXPORT_SATELLITE: {
			tempVisualisator = satView;
			size = tempVisualisator.getSize();
		}
		break;
		default:
			ErrorReporter.logError("Unexpected image export type " + mode);
			return;
		}

		BufferedImage image;
		GUIErrorHandler errorHandler = new GUIErrorHandler();
		try {
			image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		} catch (OutOfMemoryError e) {
			long needed = (long) size.width * (long) size.height * 4;
			String temp;
			if (needed < 1024) {
				temp = needed + " bytes";
			} else if (needed < 1024 * 1024) {
				temp = needed / 1024 + " Kbytes";
			} else {
				temp = needed / 1024 / 1024 + " Mbytes";
			}
			String errorText = "Could not save an image of " + size.width + "*" + size.height + 
					" size as there was not enough free memory (" + temp + ")";
			errorHandler.reportErrorMessage(errorText);
			ErrorReporter.logExceptionStackTrace(errorText, e);
			return;
		}
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		tempVisualisator.paint(g2);
		g2.dispose();
		try {
			ImageIO.write(image, "png", new File(path));
		} catch (IOException e) {
			String message = "Error while writing to file" + path;
			ErrorReporter.logExceptionStackTrace(message, e);
			errorHandler.reportException(message, e);
		}
	}

	/**
	 * This function can set a custom shape, colour etc. for the graph nodes and
	 * edges on a given {@link VisualizationViewer}
	 * 
	 * @param rnd
	 *            - the shape, colour etc. describing class's instance
	 * @param visualisator
	 *            - the visualisator to change
	 * @see GraphRenderer
	 */
	public static void setNodeRenderer(GraphRenderer<NodeDescriptor, EdgeDescriptor> rnd,
			VisualizationViewer<NodeDescriptor, EdgeDescriptor> visualisator) {
		if (visualisator == null || rnd == null) {
			return;
		}
		visualisator.getRenderContext().setVertexShapeTransformer(rnd.getShape());
		visualisator.getRenderContext().setVertexFillPaintTransformer(rnd.getVertexColour());
		visualisator.getRenderContext().setVertexLabelRenderer(rnd.getFont());

		visualisator.getRenderContext().setEdgeDrawPaintTransformer(rnd.getEdgeColour());
		visualisator.getRenderContext().setArrowFillPaintTransformer(rnd.getEdgeColour());
		visualisator.getRenderContext().setArrowDrawPaintTransformer(rnd.getEdgeColour());
		
		EdgeStroke<EdgeDescriptor> stroke = new EdgeStroke<EdgeDescriptor>();
		visualisator.getRenderContext().setEdgeStrokeTransformer(stroke);
	}

	/**
	 * Changes the visualizers' size
	 * 
	 * @param newSize
	 *            : the new window size to set
	 */
	public void changeWindowSize(Dimension newSize) {
		if (actVisualisator != null) {
			actVisualisator.setPreferredSize(new Dimension(newSize.width, newSize.height));
		}
	}

	/**
	 * @return A {@link Component} that contains the whole graph in a small view
	 */
	public CustomSatelliteViewer getSatelliteViewer() {
		return satView;
	}

	private void initSatView() {
		if (actVisualisator == null) {
			return;
		}
		satView = new CustomSatelliteViewer(actVisualisator);
		satView.getRenderContext().setVertexLabelTransformer(NODE_LABELER);
		satView.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		satView.getRenderContext().setVertexShapeTransformer(renderer.getShape());
		satView.getRenderContext().setVertexFillPaintTransformer(renderer.getVertexColour());
		satView.scaleToLayout(new CrossoverScalingControl());
	}

	/**
	 * @return A {@link Component} that contains a graph without scrollbars
	 */
	public CustomVisualizationViewer getVisualizator() {
		return actVisualisator;
	}

	/**
	 * This function does zooming on the graph (both satellite and main views).
	 * 
	 * @param scale
	 *            : The zooming multiplier (zoom out if <1 and zoom in if >1, no
	 *            change in case of 1)
	 */
	public void zoom(float scale) {
		if (actVisualisator == null) {
			return;
		}
		CrossoverScalingControl control = new CrossoverScalingControl();
		control.scale(actVisualisator, scale, actVisualisator.getCenter());
	}

	/**
	 * This method sets a clustering for the stored graph. This modification
	 * will not be automatically shown on the screen.
	 * 
	 * @param clusters
	 */
	public void setClusters(Set<Set<NodeDescriptor>> clusters) {
		this.clusters = clusters;
	}

	/**
	 * Sets node menu entries that are not used in clustering mode
	 * 
	 * @param value
	 *            : True if the menu entries should be enabled
	 */
	public void setMenusEnabled(boolean value) {
		if (popupMenu == null) {
			return;
		}
		popupMenu.enableGoToDefinition(value);
	}

}