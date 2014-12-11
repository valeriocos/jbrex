/*******************************************************************************
 * Copyright (c) 2009, 2011 INRIA Rennes Bretagne-Atlantique and others.
 *
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   INRIA Rennes Bretagne-Atlantique - Initial API and implementation
 ******************************************************************************/

package fr.inria.portolan.visualization.prefuse.viewer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import prefuse.util.ColorLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;

public class NodesPathControl extends NodeDoubleSelectionListener {

	private NodeItem start;
	private NodeItem end;
	private Hashtable<EdgeItem, WalkedEdge> colorizedEdges;
	private Hashtable<NodeItem, VisitedNode> visitedNodes;
	private int decr = 30;	// color decrement
	private double width = 4.;
	private int shortestPathColor = ColorLib.rgb(255, 255, 0);
	
	class WalkedEdge {
		EdgeItem edge;
		int red = 0;
		int green = 0;
		int fillColor;
		int strokeColor;
		double edgeWidth;
		WalkedEdge(EdgeItem e) {
			edge = e;
			SwitchableDisplay d = (SwitchableDisplay) e.getVisualization().getDisplay(0);
			strokeColor = d.edgeColorAction.getColor(e);
			fillColor = d.edgeArrowColorAction.getColor(e);
			edgeWidth = e.getSize();
		}
		void restore() {
			edge.setSize(edgeWidth);
			repaint(fillColor, strokeColor);
		}
		void repaint(int fColor, int sColor) {
			edge.setFillColor(fColor);
			edge.setStrokeColor(sColor);
		}
		void setRed(int level) {
			if (level > red) {
				red = level;
				drawEdge();
			}
		}
		void setGreen(int level) {
			if (level > green) {
				green = level;
				drawEdge();
			}
		}
		void drawEdge() {
			int color = ColorLib.rgb(red, green, 0);
			repaint(color, color);
		}
		public void setSize(double d) {
			edge.setSize(d);
		}
	}
	
	class VisitedNode {
		NodeItem node;
		int red = 0;
		int green = 0;
		int textColor;
		int labelColor;
		int strokeColor;
		
		VisitedNode (NodeItem n) {
			node = n;
			SwitchableDisplay d = (SwitchableDisplay) n.getVisualization().getDisplay(0);
			textColor = d.nodeTextColorAction.getColor(n);
			labelColor = d.nodeLabelColorAction.getColor(n);
			strokeColor = 0;	// no stroke by default
		}
		void restore() {
			node.setFillColor(labelColor);
			node.setTextColor(textColor);
			node.setStrokeColor(strokeColor);
		}
	}
	
	@Override
	public void nodesReleased(NodeDoubleSelectionEvent evt) {
		start = null;
		end = null;
		
		// return visited edges to their ordinary color
		Enumeration<WalkedEdge> edges = colorizedEdges.elements();
		while (edges.hasMoreElements()) {
			WalkedEdge edge = edges.nextElement();
			edge.restore();
		}
		colorizedEdges = null;
		
		Enumeration<VisitedNode> nodes = visitedNodes.elements();
		while (nodes.hasMoreElements()) {
			VisitedNode node = (VisitedNode) nodes.nextElement();
			node.restore();
		}
		visitedNodes = null;
	}

	@Override
	public void nodesSelected(NodeDoubleSelectionEvent evt) {
		NodeItem[] couple = evt.getSelectedNodes();
		
		// initialize elements
		start = couple[0];
		end = couple[1];
		colorizedEdges = new Hashtable<EdgeItem, WalkedEdge>();
		visitedNodes = new Hashtable<NodeItem, VisitedNode>();
		
		forwardWalk(start, 255);
		backwardWalk(end, 255);
		clean1DirectionWalks();
		computeShortestPath(start);
	}

	private void forwardWalk(NodeItem node, int stepRedValue) {
		
		if (stepRedValue < 0)
			return;	// we are too far from start node
		
		if (node == end)
			return;	// we have reached the end node
		
		VisitedNode vn = visitedNodes.get(node);
		if (vn == null) {
			vn = new VisitedNode(node);
			visitedNodes.put(node, vn);
		}
		
		// we walked the node and its subtree only if better value
		if (stepRedValue <= vn.red)
			return;

		vn.red = stepRedValue;

		for (@SuppressWarnings("rawtypes")
		Iterator iterator = node.outEdges(); iterator.hasNext();) {
			EdgeItem edge = (EdgeItem) iterator.next();
			
			WalkedEdge we = colorizedEdges.get(edge);
			if (we == null) {
				we = new WalkedEdge(edge);
				colorizedEdges.put(edge, we);
			}
			we.setRed(stepRedValue);
			
			// go to next step
			forwardWalk(edge.getTargetItem(), stepRedValue - decr);
		}
	}

	private void backwardWalk(NodeItem node, int stepGreenValue) {
		
		if (stepGreenValue < 0)
			return;	// we are too far from end node
		
		if (node == start)
			return;	// we have reached the starting node
		
		VisitedNode vn = visitedNodes.get(node);
		if (vn == null) {
			vn = new VisitedNode(node);
			visitedNodes.put(node, vn);
		}
		
		// we walked the node and its subtree only if better value
		if (stepGreenValue <= vn.green)
			return;
		
		vn.green = stepGreenValue;
		
		for (@SuppressWarnings("rawtypes")
		Iterator iterator = node.inEdges(); iterator.hasNext();) {
			EdgeItem edge = (EdgeItem) iterator.next();
			
			WalkedEdge we = colorizedEdges.get(edge);
			if (we == null) {
				we = new WalkedEdge(edge);
				colorizedEdges.put(edge, we);
			}
			we.setGreen(stepGreenValue);
			
			// go to next step
			backwardWalk(edge.getSourceItem(), stepGreenValue - decr);
		}
	}

	private void clean1DirectionWalks() {
		// we remove path color on edges which are walked in only one direction
		Enumeration<WalkedEdge> edges = colorizedEdges.elements();
		while (edges.hasMoreElements()) {
			WalkedEdge edge = edges.nextElement();
			if (edge.red <= 0 || edge.green <= 0) {
				edge.restore();
			}
		}
	}
	
	private void computeShortestPath(NodeItem node) {
		// The highest colors values mean the closest to start/end nodes
		// The edge which combine high values on red (forward walk) and
		// green (backward walk) is (one of) the shortest path(s).
		
		// we underline nodes on the walk
		node.setStrokeColor(shortestPathColor);
		
		// we compute from start node until we reach end node
		if (node == end)
			return;
		
		int maxSum = 0;
		WalkedEdge bestEdge = null;
		
		for (@SuppressWarnings("rawtypes")
		Iterator iterator = node.outEdges(); iterator.hasNext();) {
			EdgeItem edge = (EdgeItem) iterator.next();
			
			WalkedEdge we = colorizedEdges.get(edge);
			if (we != null) {
				if (we.green > 0) {	// no green means no path from end node
					int sum = we.red + we.green;
					if (sum > maxSum) {
						maxSum = sum;
						bestEdge = we;
					}
				}
			}
		}
		if (bestEdge != null) {
			bestEdge.setSize(width);
			bestEdge.repaint(shortestPathColor, shortestPathColor);
			
			// walk along the short path to paint it
			computeShortestPath(bestEdge.edge.getTargetItem());
		}
	}
}
