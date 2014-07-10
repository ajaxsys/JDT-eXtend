package com.github.ajaxsys.jdtx.samples;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class SampleAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public SampleAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		// MessageDialog.openInformation(
		// window.getShell(),
		// "MyJDTPlugin",
		// "Hello, Eclipse world");
		
		// create a project with name "TESTJDT"
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			//createANewProject(root);
			//importAnExistingProject(root);
			getAllProjects(root);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void importAnExistingProject(IWorkspaceRoot root) {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		 
		System.out.println("root" + root.getLocation().toOSString());
		 
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					IPath projectDotProjectFile = new Path("D:/eclipse/wsmytest/Tmp-Java2" + "/.project");
					IProjectDescription projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
					IProject project = workspace.getRoot().getProject(projectDescription.getName());
					JavaCapabilityConfigurationPage.createProject(project, projectDescription.getLocationURI(),	null);
					//project.create(null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		};
		 
		// and now get the workbench to do the work
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().syncExec(runnable);
	}

	private void createANewProject(IWorkspaceRoot root) throws CoreException,
			JavaModelException {
		// Create a new project
		IProject project = root.getProject("TESTJDT");
		project.create(null);

		project.open(null);

		// set the Java nature
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });

		// create the project
		project.setDescription(description, null);
		IJavaProject javaProject = JavaCore.create(project);

		// set the build path
		IClasspathEntry[] buildPath = {
				JavaCore.newSourceEntry(project.getFullPath().append("src")),
				JavaRuntime.getDefaultJREContainerEntry() };

		javaProject.setRawClasspath(buildPath, project.getFullPath()
				.append("bin"), null);

		// create folder by using resources package
		IFolder folder = project.getFolder("src");
		folder.create(true, true, null);

		// Add folder to Java element
		IPackageFragmentRoot srcFolder = javaProject
				.getPackageFragmentRoot(folder);

		// create package fragment
		IPackageFragment fragment = srcFolder.createPackageFragment(
				"com.programcreek", true, null);

		// init code string and create compilation unit
		String str = "package com.programcreek;" + "\n"
				+ "public class Test  {" + "\n" + "private String name;"
				+ "\n" + "}";

		ICompilationUnit cu = fragment.createCompilationUnit("Test.java",
				str, false, null);

		// create a field
		IType type = cu.getType("Test");

		type.createField("private String age;", null, true, null);
	}

	private void getAllProjects(IWorkspaceRoot root) {
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		// Loop over all projects
		for (IProject project : projects) {
			System.out.println(project.getName());
		}
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}