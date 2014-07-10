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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
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
public class CountTotalMethods implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	private int totalMethod = 0;

	/**
	 * The constructor.
	 */
	public CountTotalMethods() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		// create a project with name "TESTJDT"
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			// createANewProject(root);
			// importAnExistingProject(root);
			processRootDirectory(root);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processRootDirectory(IWorkspaceRoot root) throws JavaModelException,
			CoreException {
		System.out.println("root" + root.getLocation().toOSString());

		IProject[] projects = root.getProjects();

		// process each project
		for (IProject project : projects) {

			System.out.println("project name: " + project.getName());

			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject javaProject = JavaCore.create(project);
				IPackageFragment[] packages = javaProject.getPackageFragments();

				// process each package
				for (IPackageFragment aPackage : packages) {

					// We will only look at the package from the source folder
					// K_BINARY would include also included JARS, e.g. rt.jar
					// only process the JAR files
					if (aPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {

						for (ICompilationUnit unit : aPackage
								.getCompilationUnits()) {

							System.out.println("--class name: "
									+ unit.getElementName());

							IType[] allTypes = unit.getAllTypes();
							for (IType type : allTypes) {
								IField[] fields = type.getFields();// TODO from qualified name <--> get IType/IMethod/IField instance
								IMember member;
								IMethod[] methods = type.getMethods();

								for (IMethod method : methods) {
									totalMethod ++;
									System.out.println("--Method name: "
											+ method.getElementName());
									System.out.println("Signature: "
											+ method.getSignature());
									System.out.println("Return Type: "
											+ method.getReturnType());
									System.out.println("source: "
											+ method.getSource());
									System.out.println("to string: "
											+ method.toString());
									System.out.println("new: "
											+ method.getPath().toString());
								}
							}
						}
					}
				}

			}

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