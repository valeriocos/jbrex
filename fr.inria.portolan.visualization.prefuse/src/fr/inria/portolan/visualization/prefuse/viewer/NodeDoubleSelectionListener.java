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

import java.util.EventListener;

/**
 * A listener for NodeDoubleSelectionEvent events
 * @author vincent
 *
 */
public abstract class NodeDoubleSelectionListener implements EventListener {

	abstract public void nodesSelected(NodeDoubleSelectionEvent evt);
	abstract public void nodesReleased(NodeDoubleSelectionEvent evt);
}
