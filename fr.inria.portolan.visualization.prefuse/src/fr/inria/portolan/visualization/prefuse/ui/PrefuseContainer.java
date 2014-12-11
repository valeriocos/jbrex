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
package fr.inria.portolan.visualization.prefuse.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

import fr.inria.portolan.visualization.prefuse.viewer.SwitchableDisplay;

import prefuse.data.Graph;

/**
 * @author <a href="mailto:Vincent.Mahe@inria.fr">Vincent Mahe</a>
 *
 */
public class PrefuseContainer {
	
	protected static JTextArea tooltipArea;
	
	// Singleton design pattern
	private static PrefuseContainer instance = new PrefuseContainer();
	protected PrefuseContainer() {
	}
	public static PrefuseContainer getInstance() {
		return PrefuseContainer.instance;
	}

	public void createPartControl(Composite parent, JPanel panel) {
		Composite container = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		Frame awtFrame = SWT_AWT.new_Frame(container);
		
		awtFrame.add(panel);
	}
	
	public JPanel initializeGraph(Graph graph) {
		Color BACKGROUND = Color.WHITE;
		Color FOREGROUND = Color.BLACK;
		
		// add the visual display to the viewer
		SwitchableDisplay display = getDisplay(graph);
		display.setBackground(BACKGROUND);
		display.setForeground(FOREGROUND);

		// main AWT container for all
		JPanel awtPanel = new JPanel(new BorderLayout());
		
		awtPanel.add(display, BorderLayout.CENTER);
		
		return awtPanel;
	}

	protected SwitchableDisplay getDisplay(Graph graph) {
		return new SwitchableDisplay(graph);
	}
}
