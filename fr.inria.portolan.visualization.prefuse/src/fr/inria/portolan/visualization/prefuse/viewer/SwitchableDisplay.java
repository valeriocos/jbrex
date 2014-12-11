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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Iterator;

import fr.inria.portolan.visualization.prefuse.ui.AggregateDragControl;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;

public class SwitchableDisplay extends Display {

	/**
	 * 
	 */
	private static final long serialVersionUID = 785113808645878860L;
	
	/*
	 * all current actions inserted in the visualization
	 */
	protected DataColorAction nodeLabelColorAction;
	protected ColorAction nodeTextColorAction;
	protected DataColorAction edgeColorAction;
	protected DataColorAction edgeArrowColorAction;
	protected DataColorAction shapeColorAction;
	protected ColorAction areaFillColorAction;
	/* corresponding action lists */
	protected ActionList colorsActionList;
	protected ActionList dynamicLayout;
	
	private Layout displayLayout;
	private long durationLayout;
	private boolean visibleAreas = false;
	private AggregateLayout areasLayout;
	
	// color default sets
	protected int[] blueColors;
	protected int[] greenColors;
	protected int[] redColors;
	protected int[] blackColors;
	protected int[] areasColors;
	
	// store the colors use for each kind of item
	protected int[] labelsTextColors;
	protected int[] labelsBoxColors;
	protected int[] shapesColors;
	protected int[] edgesColors;
	
	// rendering options
	protected DefaultRendererFactory rendererFactory;
	protected LabelRenderer labelRenderer;
	protected LabelRenderer pivotNodeRenderer;
	protected EdgeRenderer edgeRenderer;
	protected EdgeRenderer undirectedEdgeRenderer;
	protected WeightRenderer weightRenderer;
	protected PolygonRenderer areaRenderer;
	
	protected int edgeType		= Constants.EDGE_TYPE_CURVE;		// default value
	protected int arrowType		= Constants.EDGE_ARROW_FORWARD;		// default value
	
	public static final String GRAPH_GROUP		= "graph";
	public static final String GRAPH_NODES		= "graph.nodes";
	public static final String GRAPH_EDGES		= "graph.edges";
	public static final String COLOR_ACTION		= "color";
	public static final String LAYOUT_ACTION	= "layout";
	public static final String LABEL_FIELD		= "label";
	public static final String TYPE_FIELD		= "type";
	public static final String DESCR_FIELD		= "descr";
	public static final String GROUP_FIELD		= "group";
	public static final String AREA_FIELD		= "area";
	public static final String WEIGHT_FIELD		= "weight";
	public static final String DIRECTED_FIELD	= "directed";
	
    protected AggregateTable aggregates;

    private Layout currentLayout;
    
	public SwitchableDisplay(Graph graph) {
		// initialize the display content (m_viz)
		super(new Visualization());
		
		/////// initialize the Visualization ////////
		
		// add the data to the visu
		m_vis.addGraph(GRAPH_GROUP, graph);
		
		aggregates = m_vis.addAggregates(AggregateLayout.AGGR);
		aggregates.addColumn(VisualItem.POLYGON, float[].class);
		aggregates.addColumn(AREA_FIELD, String.class);
		aggregates.addColumn(DESCR_FIELD, String.class);
		
		areasLayout = new AggregateLayout(AggregateLayout.AGGR);
        
		computeGroups();
		
		// use the full screen
		setDisplaySize(Toolkit.getDefaultToolkit().getScreenSize());
		
		//////// initialize the Renderers ///////////
        
		setLabelRenderer();
		
        // we specialize the default factory
        rendererFactory = new DefaultRendererFactory(labelRenderer);
		m_vis.setRendererFactory(rendererFactory);
		
		/////// initialize the Colors ///////////
		
		// some predefined sets of colors
		blueColors = ColorLib.getInterpolatedPalette(
				ColorLib.rgb(150, 150, 200),
				ColorLib.rgb(100, 100, 255));
		greenColors = ColorLib.getInterpolatedPalette(
				ColorLib.rgb(150, 200, 150),
				ColorLib.rgb(50, 255, 50));
		redColors = ColorLib.getInterpolatedPalette(
				ColorLib.rgb(200, 150, 150),
				ColorLib.rgb(255, 50, 50));
		blackColors = ColorLib.getInterpolatedPalette(
				ColorLib.rgb(0, 0, 0),
				ColorLib.rgb(0, 0, 0));
		
		// list those color actions
		colorsActionList = new ActionList();
		
		labelsBoxColors = ColorLib.getCoolPalette();
		setNodeLabelColor(labelsBoxColors);
		labelsTextColors = blackColors;
		setNodeTextColor(labelsTextColors);
		edgesColors = ColorLib.getCoolPalette();
		setEdgeColor(edgesColors);
		shapesColors = labelsBoxColors;
		setShapeColor(shapesColors);
		areasColors = ColorLib.getCategoryPalette(
				aggregates.getRowCount(), 0.40f, 0.60f, 0.80f, 0.33f);
		setAreaColor(areasColors);
		
		m_vis.putAction(COLOR_ACTION, colorsActionList);
		
		// we display edges arrows
		setEdgeRenderer();
		
		// choose how to draw nodes
		setWeightRenderer();
		
		// to visualize groups
		setAreaRenderer();
		
		/////// initialize the Layout ///////////
		
		setForceDirectedLayout();
		
		/////// initialize the Controls ///////////
		
		this.addControlListener(new DragControl());
		this.addControlListener(new PanControl());
		this.addControlListener(new ZoomControl());
		this.addControlListener(new WheelZoomControl());
		this.addControlListener(new ZoomToFitControl());
		this.addControlListener(new AggregateDragControl());
		
		this.setFocusable(true);
		this.addKeyListener(new KeyListenerSwitchableDisplay(this));
		
		
		// a control to more easily catch a node
		this.addControlListener(new ControlAdapter() {
			public void itemEntered(VisualItem item, MouseEvent evt) {
				item.setFixed(true);
			}
			public void itemExited(VisualItem item, MouseEvent evt) {
				item.setFixed(false);
			}
		});
		// a double selector of nodes
		this.addControlListener(new NodeDoubleSelectionControl(this));
		
		/////// launch the painting action ///////////
	      
		run();
	}
	
	/**
	 * 
	 */
	protected void computeGroups() {
		// compute the different groups
		TupleSet vg = m_vis.getVisualGroup(GRAPH_GROUP);
		Hashtable<String, Integer> groups = new Hashtable<String, Integer>();
		for (@SuppressWarnings("unchecked")
				Iterator<VisualItem> nodes = vg.tuples(); nodes.hasNext();) {
			
			VisualItem item = (VisualItem) nodes.next();
			
			if (item instanceof Node) {
				AggregateItem aitem;
				String groupValue = (String) item.get(GROUP_FIELD);
				if (! groupValue.equals("#na")) {
					Integer row = groups.get(groupValue);
					if (row == null) {
						aitem = (AggregateItem)aggregates.addItem();
						aitem.setString(AREA_FIELD, groupValue);
						groups.put(groupValue, aitem.getRow());
					} else {
						aitem = (AggregateItem)aggregates.getItem(row);
					}
			        // bind each node to its group
		            aitem.addItem(item);
				}
			}
		}
	}

	public void setGraph(Graph graph) {
		m_vis.removeGroup(GRAPH_GROUP);
		m_vis.addGraph(GRAPH_GROUP, graph);
	}
	
	public void run() {
//		m_vis.run(COLOR_ACTION);
		m_vis.run(LAYOUT_ACTION);
	}
	
	public void setArrows(boolean arrows) {
		if (arrows) {
			arrowType = Constants.EDGE_ARROW_FORWARD;
		} else {
			arrowType = Constants.EDGE_ARROW_NONE;
		}
		refreshEdges();
	}
	
	public void setCurves(boolean curves) {
		if (curves) {
			edgeType = Constants.EDGE_TYPE_CURVE;
		} else {
			edgeType = Constants.EDGE_TYPE_LINE;
		}
		refreshEdges();
	}
	
	protected void setLabelRenderer() {
		// we display nodes labels (key = "label")
		labelRenderer = new LabelRenderer(LABEL_FIELD);
		labelRenderer.setRoundedCorner(5, 5);
	}
	
	protected void setEdgeRenderer() {
		edgeRenderer = new EdgeRenderer(edgeType, arrowType);
		rendererFactory.setDefaultEdgeRenderer(edgeRenderer);
		// manage differently not directed edges
		undirectedEdgeRenderer = new EdgeRenderer(edgeType, Constants.EDGE_ARROW_NONE);
		String edgePredicate = "ingroup('" + GRAPH_EDGES + "')" + " and " + DIRECTED_FIELD +" = 'false'";
		rendererFactory.add(edgePredicate, undirectedEdgeRenderer);
		// we manage separately 'invisible' nodes (for N-nary edges)
		pivotNodeRenderer = new LabelRenderer(LABEL_FIELD);
		pivotNodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_NONE);
		String pivotPredicate = "ingroup('" + GRAPH_GROUP + "')" + " and " + TYPE_FIELD +" = 'invisible'";
		rendererFactory.add(pivotPredicate, pivotNodeRenderer);
	}
	
	protected void setAreaRenderer() {
		areaRenderer = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
        ((PolygonRenderer)areaRenderer).setCurveSlack(0.08f);
        rendererFactory.add("ingroup('" + AggregateLayout.AGGR + "')", areaRenderer);
	}
	
	protected void setWeightRenderer() {
		weightRenderer = new WeightRenderer(LABEL_FIELD);
		String predicate = "ingroup('" + GRAPH_GROUP + "')" + " and " + WEIGHT_FIELD +" > 0";
		rendererFactory.add(predicate, weightRenderer);
		// default is "no weights"
		weightRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_NONE);
	}
	
	protected void refreshEdges() {
		edgeRenderer.setArrowType(arrowType);
		edgeRenderer.setEdgeType(edgeType);
		undirectedEdgeRenderer.setEdgeType(edgeType);
	}
	
	public void setNodeLabelColor(int[] colors) {
		// we fill up the nodes labels depending of their Type
		colorsActionList.remove(nodeLabelColorAction);
		nodeLabelColorAction = new DataColorAction(GRAPH_NODES, TYPE_FIELD,
				Constants.NOMINAL, VisualItem.FILLCOLOR, colors);
		colorsActionList.add(nodeLabelColorAction);
	}
	
	public void setNodeTextColor(int[] colors) {
		// the label text is dark
		colorsActionList.remove(nodeTextColorAction);
		nodeTextColorAction = new DataColorAction(GRAPH_NODES, TYPE_FIELD,
				Constants.NOMINAL, VisualItem.TEXTCOLOR, colors);
		colorsActionList.add(nodeTextColorAction);
	}
	
	public void setEdgeColor(int[] colors) {
		colorsActionList.remove(edgeArrowColorAction);
		colorsActionList.remove(edgeColorAction);
		edgeColorAction = new DataColorAction(GRAPH_EDGES, TYPE_FIELD,
				Constants.NOMINAL, VisualItem.STROKECOLOR, colors);
		// the edges are colored depending on their source+target types
		edgeArrowColorAction = new DataColorAction(GRAPH_EDGES, TYPE_FIELD,
				Constants.NOMINAL, VisualItem.FILLCOLOR, colors);
		colorsActionList.add(edgeArrowColorAction);
		colorsActionList.add(edgeColorAction);
	}
	
	public void setShapeColor(int[] colors) {
		colorsActionList.remove(shapeColorAction);
		shapeColorAction = new DataColorAction(GRAPH_NODES, TYPE_FIELD,
				Constants.NOMINAL, VisualItem.STROKECOLOR, colors);
		colorsActionList.add(shapeColorAction);
	}
	
	public void setAreaColor(int[] colors){
		colorsActionList.remove(areaFillColorAction);
		areaFillColorAction = new DataColorAction(AggregateLayout.AGGR, AREA_FIELD,
                Constants.NOMINAL, VisualItem.FILLCOLOR, colors);
		colorsActionList.add(areaFillColorAction);
	}
	
	public void setForceDirectedLayout() {
		// let continuous computation to play with graph :-)
		ForceDirectedLayout layout = new ManagedForceDirectedLayout(GRAPH_GROUP);
		setLayout(Activity.INFINITY, layout);
		this.currentLayout = layout;
	}
	
	public void toggleInfinity(boolean toggle) {
		if (!toggle)
			this.setLayout(5, this.currentLayout);
		else
			this.setLayout(Activity.INFINITY, this.currentLayout);
	}
	
	private void setLayout(long duration, Layout layout) {
		displayLayout = layout;
		durationLayout = duration;
		refreshLayout();
	}
	
	public void setAreasVisible(boolean visible) {
		visibleAreas = visible;
		if (visible) {
			areaRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
			labelRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_NONE);
//			setNodeLabelColor(ColorLib.getCategoryPalette(1, 0, 0, 255, 0));
		} else {
			areaRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_NONE);
			labelRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
//			setNodeLabelColor(labelsBoxColors);
		}
		refreshLayout();
	}
	
	public void setWeightsVisible(boolean visible) {
		if (visible) {
			weightRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
//			setNodeLabelColor(labelsBoxColors);
		} else {
			weightRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_NONE);
//			setNodeLabelColor(ColorLib.getCategoryPalette(1, 0, 0, 255, 0));
		}
		refreshLayout();
	}
	
	protected void refreshLayout() {
		// stopping computation forbids later changes
		dynamicLayout = new ActionList(durationLayout);
		
		dynamicLayout.add(colorsActionList);
		
		dynamicLayout.add(displayLayout);
		
		if (visibleAreas)
			dynamicLayout.add(areasLayout);
		
		dynamicLayout.add(new RepaintAction());
		
		m_vis.removeAction(LAYOUT_ACTION);
		m_vis.putAction(LAYOUT_ACTION, dynamicLayout);
		m_vis.run(LAYOUT_ACTION);
	}
	
	public void setDisplaySize(Dimension screensize) {
		
		// manage multiple screens
        if (screensize.width > 1280)
        	screensize.width = 1280;
        if (screensize.height > 1024)
        	screensize.height = 1024;
        
        setPreferredSize(screensize);
        
        // center the graph
        pan(screensize.width / 2, screensize.height / 2);
	}
}
