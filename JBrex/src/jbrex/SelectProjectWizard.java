package jbrex;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class SelectProjectWizard extends Wizard implements INewWizard {
	
	private SelectProjectWizardPage page;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}
	
	public SelectProjectWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("J-BREX"); //$NON-NLS-1$
	}
	

	@Override
	public void addPages() {
		page = new SelectProjectWizardPage("selectJavaProject");
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public boolean performCancel() {
		// TODO Auto-generated method stub
		return true;
	}

}
