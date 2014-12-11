package jbrex;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class SelectProjectWizardPage extends WizardPage {
	
	private org.eclipse.swt.widgets.List listJavaProjects;
	private Composite container;
	private ProjectSelectPageListener listener;
	private IProject selectedProject;
	private int currentProjectIndex = -1;

	protected SelectProjectWizardPage(String pageName) {
		super(pageName);
		super.setTitle("Business Rule Extraction for Java Applications");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		
		container = new Composite(parent, SWT.NULL);
			
		GridData data = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(data);
		
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		
		Label selectJavaProject = new Label (container, SWT.NONE);
		selectJavaProject.setText("Select a Java project in the workspace:");
		
		listJavaProjects = new org.eclipse.swt.widgets.List (container, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		
		
		GridData  gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		listJavaProjects.setLayoutData(gridData);
		
		if (!getJavaProjectsInWorkspace().isEmpty()) {
		
			initJavaProjectList();
	        // create listener
	        listener = new ProjectSelectPageListener(this);
	        listJavaProjects.addListener(SWT.Selection, listener);
	        
		}
		
		dialogChanged();
		setControl(container);
		
	}
	
	private void initJavaProjectList() {
		listJavaProjects.removeAll();
		for(IProject JavaProject : getJavaProjectsInWorkspace())
	    	listJavaProjects.add(JavaProject.getName(), getJavaProjectsInWorkspace().indexOf(JavaProject));      
	}
	
	public List<IProject> getJavaProjectsInWorkspace() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		
		List<IProject> javaProjects = new LinkedList<IProject>();
		
		for(int i = 0; i < projects.length; i++) {
			IProject p = projects[i];
			
			try {
				String[] natures = p.getDescription().getNatureIds();
				
				for(int j = 0; j < natures.length; j++) {
					if (natures[j].equals(JavaCore.NATURE_ID))
						javaProjects.add(p);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}	
		}
		
		return javaProjects;
	}
	
	public IProject getJavaProjectByIndex(int index) {
		return getJavaProjectsInWorkspace().get(index);
	}
	
	public void dialogChanged() {
		//if there are not Java projects
		if (getJavaProjectsInWorkspace().isEmpty()) {
			updateStatus("No Java projects exist in the workspace");
			return;
		}
		else {
			//if a Java project has been selected
			if (listener.getJavaProjectIndex() != this.currentProjectIndex) {
				this.currentProjectIndex = listener.getJavaProjectIndex();
				this.loadJavaProject();
				updateStatus(null);
				return;
			}
			else if (this.selectedProject != null) {
				updateStatus(null);
				return;
			}
			else {
				updateStatus("A Java project must be specified");
				return;
			}
		}
		
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public void loadJavaProject() {
		this.selectedProject = getJavaProjectByIndex(listJavaProjects.getFocusIndex());
		this.setPageComplete(true);
	}
	
	public IProject getSelectedProject() {
		return this.selectedProject;
	}

}
