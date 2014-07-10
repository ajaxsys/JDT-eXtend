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
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;

/**
 * If you want to get all class names, method names and field names from a Java
 * jar file, you can use the code below to get them.
 * 
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class ProcessRootDirectoryJar implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public ProcessRootDirectoryJar() {
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
			// createANewProject(root);
			// importAnExistingProject(root);
			processRootDirectoryJar(root);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processRootDirectoryJar(IWorkspaceRoot root)
			throws JavaModelException, CoreException {
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

					// System.out.println("Package name: " +
					// aPackage.toString());

					// We will only look at the package from the source folder
					// K_BINARY would include also included JARS, e.g. rt.jar
					// only process the JAR files
					if (aPackage.getKind() == IPackageFragmentRoot.K_BINARY
							&& aPackage.getElementName().equals("java.lang")) {

						System.out.println("inside of java.lang package");

						for (IClassFile classFile : aPackage.getClassFiles()) {

							System.out.println("----classFile: "
									+ classFile.getElementName());

							// A class file has a single child of type IType.
							// Class file elements need to be opened before they
							// can be navigated. If a class file cannot be
							// parsed, its structure remains unknown.
							// Use IJavaElement.isStructureKnown to determine
							// whether this is the case.

							// System.out.println();
							// classFile.open(null);

							for (IJavaElement javaElement : classFile
									.getChildren()) {

								if (javaElement instanceof IType) {
									System.out.println("--------IType "
											+ javaElement.getElementName());

									// IInitializer
									IInitializer[] inits = ((IType) javaElement)
											.getInitializers();
									for (IInitializer init : inits) {
										System.out
												.println("----------------initializer: "
														+ init.getElementName());
									}

									// IField
									IField[] fields = ((IType) javaElement)
											.getFields();
									for (IField field : fields) {
										System.out
												.println("----------------field: "
														+ field.getElementName());
									}

									// IMethod
									IMethod[] methods = ((IType) javaElement)
											.getMethods();
									for (IMethod method : methods) {
										System.out
												.println("----------------method: "
														+ method.getElementName());
										System.out
												.println("----------------method return type - "
														+ method.getReturnType());
									}
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