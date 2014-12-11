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

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import prefuse.action.layout.Layout;
import prefuse.util.GraphicsLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;

/**
 * Clone of LabelRenderer Prefuse class, because we only need
 * to replace Image by Shape and get all the other stuff as-is...
 * 
 * TODO: find the corresponding license (Prefuse examples)
 * 
 * Layout algorithm that computes a convex hull surrounding
 * aggregate items and saves it in the "_polygon" field.
 */
public class AggregateLayout extends Layout {
    
    private int m_margin = 5; // convex hull pixel margin
    private double[] m_pts;   // buffer for computing convex hulls
   
    //add by me Guillaume Doux
    public static final String AGGR = "aggregates";
    
    private AggregateTable aggr = null;
    
    public AggregateLayout(String aggrGroup) {
        super(aggrGroup);
        
    }
    
    public AggregateLayout(String graphGroup, AggregateTable table) {
    	super(graphGroup);
    	aggr = table;
	}

	/**
     * @see edu.berkeley.guir.prefuse.action.Action#run(edu.berkeley.guir.prefuse.ItemRegistry, double)
     */
    @SuppressWarnings("rawtypes")
	public void run(double frac) {
        
    	//Begin modif
    
    	if(aggr==null) aggr = (AggregateTable)m_vis.getGroup(m_group);
        
        
        // do we have any  to process?
        int num = aggr.getTupleCount();
        if ( num == 0 ) return;
        
        // update buffers
        int maxsz = 0;
        for ( Iterator aggrs = aggr.tuples(); aggrs.hasNext();  )
            maxsz = Math.max(maxsz, 4*2*
                    ((AggregateItem)aggrs.next()).getAggregateSize());
        if ( m_pts == null || maxsz > m_pts.length ) {
            m_pts = new double[maxsz];
        }
        
        // compute and assign convex hull for each aggregate
        Iterator aggrs = m_vis.visibleItems(AGGR);
        while ( aggrs.hasNext() ) {
            AggregateItem aitem = (AggregateItem)aggrs.next();
            
			// initialize tooltip text
            String ttt = (String) aitem.get(SwitchableDisplay.AREA_FIELD) + "\n" + "members: \n";

            int idx = 0;
            if ( aitem.getAggregateSize() == 0 ) continue;
            VisualItem item = null;
            Iterator iter = aitem.items();
            while ( iter.hasNext() ) {
                item = (VisualItem)iter.next();
                if ( item.isVisible() ) {
                    addPoint(m_pts, idx, item, m_margin);
                    idx += 2*4;
                }
                
                // compute members for tooltip text
                ttt += "  " + item.getString(SwitchableDisplay.LABEL_FIELD) + "\n";
            }
            // if no aggregates are visible, do nothing
            if ( idx == 0 ) continue;

            // compute convex hull
            double[] nhull = GraphicsLib.convexHull(m_pts, idx);
            
            // prepare viz attribute array
            float[]  fhull = (float[])aitem.get(VisualItem.POLYGON);
            if ( fhull == null || fhull.length < nhull.length )
                fhull = new float[nhull.length];
            else if ( fhull.length > nhull.length )
                fhull[nhull.length] = Float.NaN;
            // copy hull values
            for ( int j=0; j<nhull.length; j++ )
                fhull[j] = (float)nhull[j];
            aitem.set(VisualItem.POLYGON, fhull);
            aitem.setValidated(false); // force invalidation
            
			// set tooltip text
			aitem.setString(SwitchableDisplay.DESCR_FIELD, ttt);
        }
    }
    
    private static void addPoint(double[] pts, int idx, 
                                 VisualItem item, int growth)
    {
        Rectangle2D b = item.getBounds();
        double minX = (b.getMinX())-growth, minY = (b.getMinY())-growth;
        double maxX = (b.getMaxX())+growth, maxY = (b.getMaxY())+growth;
        pts[idx]   = minX; pts[idx+1] = minY;
        pts[idx+2] = minX; pts[idx+3] = maxY;
        pts[idx+4] = maxX; pts[idx+5] = minY;
        pts[idx+6] = maxX; pts[idx+7] = maxY;
    }
    
} // end of class AggregateLayout