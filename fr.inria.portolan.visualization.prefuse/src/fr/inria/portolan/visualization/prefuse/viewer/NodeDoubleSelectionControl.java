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

package fr.inria.portolan.visualization.prefuse.viewer ; 

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import prefuse.controls.ControlAdapter;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * A control to select two nodes.\n
 * First click refreshes time stamp ad release previous items.\n
 * Second click throws a selection event.
 */public class NodeDoubleSelectionControl extends ControlAdapter { 
	 
	private SwitchableDisplay display ;
	private final int firstSelectedNodeColor = ColorLib.rgb(255, 0, 0);
	private final int firstSelectedNodeTextColor = ColorLib.gray(0);
	private final int secondSelectedNodeColor = ColorLib.rgb(0, 255, 0);
	private final int secondSelectedNodeTextColor = ColorLib.gray(0);
	
	public NodeDoubleSelectionControl ( SwitchableDisplay display ) { 
		this .display = display ;
		addSelectionListener(new NodesPathControl());
	}
	
	private int rank = 0 ; 
	private VisualItem item1 ; 
	private int item1TextColor ; 
	private int item1LabelColor ; 
	private VisualItem item2 ; 
	private int item2TextColor ; 
	private int item2LabelColor ;
	
	public void itemClicked (VisualItem item , MouseEvent evt ) { 
		if ( item .isInGroup( SwitchableDisplay.GRAPH_NODES ) ) { 
			switch ( rank ) { case 0 : 
				// first selection
				setItem1( item ) ; 
				rank = 1 ; 
				break ; 
			case 1 : 
				// second selection
				setItem2( item ) ; 
				rank = 2 ; 
				NodeItem [] nodes = { (NodeItem) item1 , (NodeItem) item2 } ; 
				fireNodesSelected ( new NodeDoubleSelectionEvent ( nodes ) ) ; 
				break ; 
			case 2 : 
				// new selection
				clearItems ( ) ; 
				setItem1 ( item ) ; 
				rank = 1 ; 
				break ; 
			} 
		} 
	}
	
	private void setItem1 (VisualItem item ) { 
		item1 = item ; 
		item1TextColor = display .nodeTextColorAction .getColor( item ) ; 
		item1LabelColor = display .nodeLabelColorAction .getColor ( item ) ; 
		item1 .setFillColor ( firstSelectedNodeColor ) ; 
		item1 .setTextColor ( firstSelectedNodeTextColor ) ; 
		item1 .getVisualization().repaint(); 
	}
	
	private void setItem2 (VisualItem item ) { 
		item2 = item ; 
		item2TextColor = display .nodeTextColorAction .getColor ( item ) ; 
		item2LabelColor = display .nodeLabelColorAction .getColor ( item ) ; 
		item2 .setFillColor ( secondSelectedNodeColor ) ; 
		item2 .setTextColor ( secondSelectedNodeTextColor ) ; 
		item2 .getVisualization().repaint(); 
	}
	
	private void clearItems () { 
		NodeItem [] nodes = { (NodeItem) item1 , (NodeItem) item2 } ; 
		fireNodesreleased ( new NodeDoubleSelectionEvent ( nodes ) ) ; 
		item1 .setFillColor( item1LabelColor ) ; 
		item1 .setTextColor ( item1TextColor ) ; 
		item1 .getVisualization().repaint(); 
		item2 .setFillColor ( item2LabelColor ) ; 
		item2 .setTextColor ( item2TextColor ) ; 
		item2 .getVisualization().repaint(); 
	}
	
	private ArrayList<NodeDoubleSelectionListener> listeners = new ArrayList<NodeDoubleSelectionListener>(); 
	
	public void addSelectionListener (NodeDoubleSelectionListener l ) { 
		listeners .add ( l ) ; 
	}
	
	public void removeSelectionListener (NodeDoubleSelectionListener l ) { 
		listeners .remove ( l ) ; 
	}
	
	private void fireNodesSelected (NodeDoubleSelectionEvent e ) { 
		for ( int i = 0 ; i < listeners .size ( ) ; i ++ ) { 
			NodeDoubleSelectionListener l = ( NodeDoubleSelectionListener ) listeners .get ( i ) ; 
			try { 
				l .nodesSelected ( e ) ; 
			} catch ( Exception e2 ) { 
				System .err .println ( "ERROR - NodeDoubleSelectionListener " + l + " fails on nodesSelected() method.\n" + e2 .getStackTrace() ) ; 
			} 
		} 
	}
	
	private void fireNodesreleased (NodeDoubleSelectionEvent e ) { 
		for ( int i = 0 ; i < listeners .size ( ) ; i ++ ) { 
			NodeDoubleSelectionListener l = ( NodeDoubleSelectionListener ) listeners .get ( i ) ; 
			try { 
				l .nodesReleased ( e ) ; 
			} catch ( Exception e2 ) { 
				System .err .println ( "ERROR - NodeDoubleSelectionListener " + l + " fails on nodeSelected() method.\n" + e2 .getStackTrace() ) ; 
			} 
		} 
	}
} 

