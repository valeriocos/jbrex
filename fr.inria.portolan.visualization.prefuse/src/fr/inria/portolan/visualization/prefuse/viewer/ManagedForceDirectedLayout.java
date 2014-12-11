/*******************************************************************************
 * Copyright (c) 2010 INRIA Rennes Bretagne-Atlantique.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     INRIA Rennes Bretagne-Atlantique - initial API and implementation
 *******************************************************************************/
package fr.inria.portolan.visualization.prefuse.viewer;

import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.visual.VisualItem;

/**
 * @author <a href="mailto:Vincent.Mahe@inria.fr">Vincent Mahe</a>
 *
 * use the inverse of degrees of the node as mass of the node
 */
public class ManagedForceDirectedLayout extends ForceDirectedLayout {

	private static final float massValue = 1.0f;
	/**
	 * @param group
	 */
	public ManagedForceDirectedLayout(String group) {
		super(group);
	}

    protected float getMassValue(VisualItem n) {
    	return massValue;
    }
    
//    protected float getSpringLength(EdgeItem e) {
//        return LayoutsParams.getSpringLength();
//    }
//
//    protected float getSpringCoefficient(EdgeItem e) {
//        return LayoutsParams.getSpringCoefficient();
//    }
}
