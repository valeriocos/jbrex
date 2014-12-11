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

package fr.inria.portolan.visualization.prefuse.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import fr.inria.portolan.visualization.prefuse.viewer.SwitchableDisplay;

public class EdgesControl extends JPanel implements ActionListener {

	private static final long serialVersionUID = 7695840252211740828L;
	
	// possible actions for switch
	private static final String ARROW_BOX = "Arrow Box";
	private static final String CURVE_BOX = "Curve Box";
	
	private JCheckBox arrowBox;
	private JCheckBox curveBox;

	private SwitchableDisplay display;
	
	public EdgesControl(SwitchableDisplay display) {
		this.display = display;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// buttons group title
		JLabel title = new JLabel("EDGES");
		title.setBorder(new EmptyBorder(15, 5, 5, 5));
		this.add(title);
		
		// select arrows (must correspond to SwitchableDisplay default)
		arrowBox = new JCheckBox("Arrows", true);
		arrowBox.setActionCommand(ARROW_BOX);
		arrowBox.addActionListener(this);
		this.add(arrowBox);
		
		// select curves (must correspond to SwitchableDisplay default)
		curveBox = new JCheckBox("Curves", true);
		curveBox.setActionCommand(CURVE_BOX);
		curveBox.addActionListener(this);
		this.add(curveBox);
	}

	public void actionPerformed(ActionEvent e) {
		// manage arrows checkbox
		if (e.getActionCommand() == ARROW_BOX) {
			display.setArrows(arrowBox.isSelected());
		}
		
		// manage curves checkbox
		if (e.getActionCommand() == CURVE_BOX) {
			display.setCurves(curveBox.isSelected());
		}
		
		display.repaintImmediate();
	}

}
