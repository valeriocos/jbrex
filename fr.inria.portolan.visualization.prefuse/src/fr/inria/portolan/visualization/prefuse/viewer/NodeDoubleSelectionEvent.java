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

import java.util.EventObject;

import prefuse.visual.NodeItem;

public class NodeDoubleSelectionEvent extends EventObject {

	private static final long serialVersionUID = 5295892322740030591L;

	public NodeDoubleSelectionEvent(Object arg0) {
		super(arg0);
	}
	public NodeDoubleSelectionEvent(NodeItem[] arg0) {
		super(arg0);
	}

	public NodeItem[] getSelectedNodes() {
		NodeItem[] couple = new NodeItem[2];
		couple = (NodeItem[])getSource();
		return couple;
	}
}
