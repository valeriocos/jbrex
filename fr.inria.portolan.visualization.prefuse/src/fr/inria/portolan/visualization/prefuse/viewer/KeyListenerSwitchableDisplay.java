package fr.inria.portolan.visualization.prefuse.viewer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyListenerSwitchableDisplay implements KeyListener {

	private SwitchableDisplay display;
	private boolean toggle = false;
	
	public KeyListenerSwitchableDisplay(SwitchableDisplay display) {
		this.display = display;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyChar() == 't') {
			display.toggleInfinity(toggle);
			toggle = !toggle;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
