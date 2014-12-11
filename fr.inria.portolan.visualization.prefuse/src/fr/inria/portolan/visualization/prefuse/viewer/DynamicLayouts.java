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

import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.BalloonTreeLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.FruchtermanReingoldLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
//import prefuse.activity.Activity;

public final class DynamicLayouts {

	public static ActionList getForceDirectedLayout() {
		ActionList dynLayout = new ActionList(5000);
		ForceDirectedLayout layout = new ForceDirectedLayout(SwitchableDisplay.GRAPH_GROUP);
		dynLayout.add(layout);
		
		dynLayout.add(new RepaintAction());
		
		return dynLayout;
	}

	public static ActionList getFruchtermanReingoldLayout() {
		ActionList dynLayout = new ActionList(5000);
		Layout layout = new FruchtermanReingoldLayout(SwitchableDisplay.GRAPH_GROUP);
		dynLayout.add(layout);
		
		dynLayout.add(new RepaintAction());
		
		return dynLayout;
	}

	public static ActionList getNodeLinkTreeLayout() {
		ActionList dynLayout = new ActionList(5000);
		Layout layout = new NodeLinkTreeLayout(SwitchableDisplay.GRAPH_GROUP);
		dynLayout.add(layout);
		
		dynLayout.add(new RepaintAction());
		
		return dynLayout;
	}
	
	public static ActionList getRadialTreeLayout() {
		ActionList dynLayout = new ActionList(5000);
		Layout layout = new RadialTreeLayout(SwitchableDisplay.GRAPH_GROUP);
		dynLayout.add(layout);
		
		dynLayout.add(new RepaintAction());
		
		return dynLayout;
	}
	
	public static ActionList getBalloonTreeLayout() {
		ActionList dynLayout = new ActionList(5000);
		Layout layout = new BalloonTreeLayout(SwitchableDisplay.GRAPH_GROUP);
		dynLayout.add(layout);
		
		dynLayout.add(new RepaintAction());
		
		return dynLayout;
	}
}
