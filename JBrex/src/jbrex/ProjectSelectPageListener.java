package jbrex;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ProjectSelectPageListener implements Listener {

	private SelectProjectWizardPage page;
	private int javaProjectIndex = -1;
	
	public ProjectSelectPageListener(SelectProjectWizardPage page) {
		this.page = page;
	}

	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		javaProjectIndex = event.index;
		this.page.dialogChanged();
	}
	
	public int getJavaProjectIndex() {
		return this.javaProjectIndex;
	}
	
}
