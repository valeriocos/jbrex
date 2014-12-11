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

public class DisplayControl extends JPanel implements ActionListener {

	private static final long serialVersionUID = 7695840252211740828L;
	
	// possible actions for switch
	private static final String GROUPS_BOX = "Groups Box";
	private static final String GROUPS_LABEL = "Groups";
	private JCheckBox groupsBox;

	private static final String WEIGHTS_BOX = "Weights Box";
	private static final String WEIGHTS_LABEL = "Weights";
	private JCheckBox weightsBox;

	private SwitchableDisplay display;
	
	public DisplayControl(SwitchableDisplay display) {
		this.display = display;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// buttons group title
		JLabel title = new JLabel("DISPLAY");
//		title.setAlignmentX(CENTER_ALIGNMENT);
		title.setBorder(new EmptyBorder(15, 5, 5, 5));
		this.add(title);
		
		// select box (must correspond to SwitchableDisplay default)
		groupsBox = new JCheckBox(GROUPS_LABEL, false);
		groupsBox.setActionCommand(GROUPS_BOX);
		groupsBox.addActionListener(this);
		this.add(groupsBox);
		
		// select box (must correspond to SwitchableDisplay default)
		weightsBox = new JCheckBox(WEIGHTS_LABEL, false);
		weightsBox.setActionCommand(WEIGHTS_BOX);
		weightsBox.addActionListener(this);
		this.add(weightsBox);
	}

	public void actionPerformed(ActionEvent e) {
		// manage checkbox
		if (e.getActionCommand() == GROUPS_BOX) {
			display.setAreasVisible(groupsBox.isSelected());
		}
		
		if (e.getActionCommand() == WEIGHTS_BOX) {
			display.setWeightsVisible(weightsBox.isSelected());
		}
		
		display.repaintImmediate();
	}

}
