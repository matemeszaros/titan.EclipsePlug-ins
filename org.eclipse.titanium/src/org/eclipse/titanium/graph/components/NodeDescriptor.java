/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.components;

import java.awt.Color;
import java.awt.Font;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Location;

/**
 * This class describes a module graph node (vertex)
 * 
 * @author Gabor Jenei
 */
public class NodeDescriptor implements Comparable<NodeDescriptor> {
	protected Color nodeColour = NodeColours.NO_VALUE_COLOUR;
	protected Color fontColour;
	protected Font fontType;
	protected String nodeName;
	protected String displayName;
	protected IProject project;
	protected boolean isMissingModule;
	protected Set<NodeDescriptor> cluster;
	protected final Location referencedLocation;

	/**
	 * It constructs a new graph node that can be added to the module graph.
	 * 
	 * @param displayName
	 *            : The node title to display
	 * @param name
	 *            : The node's ID
	 * @param colour
	 *            : The node's colour
	 * @param proj
	 *            : The project that contains this module
	 * @param missing
	 *            : true if this module is a missing module
	 * @param loc
	 *            : An object describing the point of declaration
	 */
	public NodeDescriptor(String displayName, String name, Color colour, IProject proj, boolean missing, Location loc) {
		this.displayName = displayName;
		nodeName = name;
		nodeColour = colour;
		project = proj;
		isMissingModule = missing;
		cluster = null;
		fontColour = Color.white;
		fontType = new Font("Arial", Font.PLAIN, 12);
		referencedLocation = loc;

		// TODO Why do the fonts appear bad?
		/*
		 * try{ Font type=Font.createFont(Font.TRUETYPE_FONT, new
		 * FileInputStream
		 * (PluginUtils.getResource("fonts/Arial.ttf").getFile()));
		 * FontType=type.deriveFont(12); } catch(Exception ex){ new
		 * GUIErrorHandler().reportException(ex); }
		 */
	}

	/**
	 * It constructs a new graph node that can be added to the module graph, the
	 * node's colour will be {@link NodeColours#NO_VALUE_COLOUR}
	 * 
	 * @param displayName
	 *            : The node title to display
	 * @param name
	 *            : The node's ID
	 * @param proj
	 *            : The project that contains this module
	 * @param missing
	 *            : true if this module is a missing module
	 * @param loc
	 *            : An object describing the point of declaration
	 */
	public NodeDescriptor(String displayName, String name, IProject proj, boolean missing, Location loc) {
		this(displayName, name, NodeColours.NO_VALUE_COLOUR, proj, missing, loc);
	}

	/**
	 * It constructs a new graph node that can be added to the module graph, the
	 * colour is {@link NodeColours#NO_VALUE_COLOUR} and the containing project
	 * will be unknown <code>null</code>. <b>It is not recommended, as metrics
	 * will not be calculated on such modules. It can only be a non-missing
	 * module</b>
	 * 
	 * @param displayName
	 *            : The node title to display
	 * @param name
	 *            : The node's ID
	 */
	public NodeDescriptor(String displayName, String name) {
		this(displayName, name, NodeColours.NO_VALUE_COLOUR, null, false, null);
	}

	/**
	 * It constructs a new graph node that can be added to the module graph, the
	 * colour is {@link NodeColours#NO_VALUE_COLOUR} and the containing project
	 * will be unknown <code>null</code>, the name to be displayed will be the
	 * Vertex ID. <b>It is not recommended, as metrics will not be calculated on
	 * such modules. It can only be a non-missing module</b>
	 * 
	 * @param name
	 *            : The node's ID
	 */
	public NodeDescriptor(String name) {
		this(name, name, NodeColours.NO_VALUE_COLOUR, null, false, null);
	}

	/**
	 * @return returns the node's ID
	 */
	public String getName() {
		return nodeName;
	}

	/**
	 * @return returns the currently set node colour
	 */
	public Color getColor() {
		return nodeColour;
	}

	/**
	 * @return returns the name to display upon graph drawing
	 */
	public String getDisplayName() {
		if (displayName != null && displayName.length() > 0) {
			return displayName;
		}
		return nodeName;
	}

	/**
	 * @return returns the containing project
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * Sets the node's colour to a new one
	 * 
	 * @param c
	 *            : the new colour
	 */
	public void setNodeColour(Color c) {
		nodeColour = c;
	}

	/**
	 * Change font colour for the represented node
	 * 
	 * @param c
	 */
	public void setFontColour(Color c) {
		fontColour = c;
	}

	/**
	 * @return returns the font colour
	 */
	public Color getFontColour() {
		return fontColour;
	}

	/**
	 * Sets the font type
	 * 
	 * @param font
	 *            : The font type to set
	 */
	public void setFontType(Font font) {
		fontType = font;
	}

	/**
	 * Returns the font type of the represented node
	 * 
	 * @return the set font type
	 */
	public Font getFontType() {
		return fontType;
	}

	/**
	 * @return returns a boolean whether the module is missing (there is no
	 *         source file on the disk)
	 */
	public boolean isMissing() {
		return isMissingModule;
	}

	/**
	 * Overrides the {@link Object#toString()} method, it is only for debugging
	 * purposes.
	 * 
	 * @return The name to display
	 */
	@Override
	public String toString() {
		return getDisplayName();
	}

	/**
	 * Overrides the {@link Object#equals(Object)} method, it implements a
	 * comparison of two instances.
	 * 
	 * @param obj
	 *            : The other instance to compare with
	 * @return A boolean that indicates whether the two objects represent the
	 *         same module
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NodeDescriptor)) {
			return false;
		}
		NodeDescriptor instance = (NodeDescriptor) obj;
		return instance.getName().equals(this.nodeName);
	}

	@Override
	public int hashCode() {
		return nodeName.hashCode();
	}

	/**
	 * @return The cluster this node belongs to
	 */
	public Set<NodeDescriptor> getCluster() {
		return cluster;
	}

	/**
	 * Setter for the cluster of this node.
	 * 
	 * @param cluster
	 *            The set of nodes this one belongs to
	 */
	public void setCluster(Set<NodeDescriptor> cluster) {
		this.cluster = cluster;
	}

	/**
	 * @return Returns a reference to the location of the represented module in
	 *         the source code
	 */
	public Location getLocation() {
		return referencedLocation;
	}

	@Override
	public int compareTo(NodeDescriptor other) {
		return displayName.compareTo(other.displayName);
	}

}