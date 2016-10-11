/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.graph.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;

/**
 * This class implements the save for jung graphs to .dot files
 * @author Gabor Jenei
 *
 * @param <V> The type of nodes
 * @param <E> The type of edges
 */
public class GraphVizWriter<V, E> {
	
	/**
	 * Saves a given graph to a dot file, it also creates the file, or overwrites the old one
	 * @param g
	 * 			  : The jung graph to save
	 * @param filename
	 * 			  : A string that points to the destination of the save
	 * @param labeler
	 * 			  : A node object -> Node name converter object
	 * @param graphName
	 * 			  : The name of the graph to export (usually this is set the project's name)
	 * @throws IOException
	 * 			  On IO error
	 */
	public void save(final Graph<V, E> g, final String filename, final Transformer<V,String> labeler, final String graphName) throws IOException{
		final SortedSet<V> nodes = new TreeSet<V>();
		final Map<V,SortedSet<V>> successors = new HashMap<V, SortedSet<V>>();
		for (final V source : g.getVertices()) {
			final Collection<V> actSuccessors = g.getSuccessors(source);
			final SortedSet<V> successorTree = new TreeSet<V>();
			for (final V destination : actSuccessors) {
				successorTree.add(destination);
			}
			
			nodes.add(source);
			successors.put(source, successorTree);
		}
		
		final BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8"));
		writer.write("digraph \""+graphName+"\" {\n");
		for (final V from : nodes) {
			final Collection<V> actSuccessors = successors.get(from);
			for (final V to : actSuccessors) {
				writer.write("\t\""+labeler.transform(from)+"\" -> \""+labeler.transform(to)+"\";\n");
			}
			
			if (g.getPredecessorCount(from)==0 && actSuccessors.isEmpty()) {
				writer.write("\t\""+labeler.transform(from)+"\";\n");
			}
		}
		
		writer.write("}");
		writer.close();
	}
	
}