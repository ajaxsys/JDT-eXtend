package com.github.ajaxsys.jdtx.actions;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.github.ajaxsys.jdtx.utils.IgnoreCaseArrayList;
import com.github.ajaxsys.jdtx.utils.Inputs;
import com.github.ajaxsys.jdtx.utils.UCaller;
import com.github.ajaxsys.jdtx.utils.UConsole;
import com.github.ajaxsys.jdtx.utils.UDialog;
import com.github.ajaxsys.jdtx.utils.UFile;

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
public class SearchMethods implements IWorkbenchWindowActionDelegate {
	// Dummy source for inner class call hierarchy
	private void dummy() {
		run(null);
	}

	void dummy(String a) {
		dummy();
	}

	// Dummy source end
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public SearchMethods() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 *
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		UDialog.showUsage(this, window);

		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					UConsole.init(UFile.getJDTExtendHome("output.txt"));

					processRootDirectoryJar(root);

					UConsole.close();
				} catch (IOException e) {
					e.printStackTrace();
					UConsole.log("[FATAL] " + e.getMessage() + " - " + UFile.getJDTExtendHome("output.txt"));
				} catch (Exception e) {
					e.printStackTrace();
					UConsole.log("[FATAL] " + e.getMessage());
				}
			}
		}).start();

	}

	private void processRootDirectoryJar(IWorkspaceRoot root)
			throws JavaModelException, CoreException {
		// System.out.println("root" + root.getLocation().toOSString());
		Inputs inputs = new Inputs(UFile.getJDTExtendHome("input1.txt"),
				UFile.getJDTExtendHome("input2.txt"),
				UFile.getJDTExtendHome("input3.txt"));
		IgnoreCaseArrayList targetClazz = inputs.getTargetClazz();
		IgnoreCaseArrayList hitClazz = new IgnoreCaseArrayList();

		// Param3 is check from JAR
		boolean isCheckClazzFromJar = false;
		List<String> targetClazzFromJar = inputs.getTargetJarPackageRegExps();
		if (targetClazzFromJar.size() > 0) {
			isCheckClazzFromJar = true;
		}

		IProject[] projects = root.getProjects();

		// process each project
		for (IProject project : projects) {

			// System.out.println("project name: " + project.getName());

			if (isValidPackage(project)) {

				IJavaProject javaProject = JavaCore.create(project);
				IPackageFragment[] packages = javaProject.getPackageFragments();

				// process each package
				for (IPackageFragment aPackage : packages) {

					// System.out.println("Package name: " +
					// aPackage.toString());

					// We will only look at the package from the source folder
					// K_BINARY would include also included JARS, e.g. rt.jar
					// only process the JAR files

					// Sources folder
					if (aPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {

						// System.out.println("inside of "
						// + aPackage.getElementName());
						for (ICompilationUnit classFile : aPackage
								.getCompilationUnits()) {

							// System.out.println("----classFile: "
							// + classFile.getElementName());

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

								analyzeJavaElement(inputs, targetClazz,
										hitClazz, javaElement,
										IPackageFragmentRoot.K_SOURCE);
							}
						}

					}

					// Jar
					if (isCheckClazzFromJar) {
						if (aPackage.getKind() == IPackageFragmentRoot.K_BINARY
								&& inputs.isTargetPackageInJar(aPackage
										.getElementName())) {
							for (IClassFile classFile : aPackage
									.getClassFiles()) {
								for (IJavaElement javaElement : classFile
										.getChildren()) {
									analyzeJavaElement(inputs, targetClazz,
											hitClazz, javaElement,
											IPackageFragmentRoot.K_BINARY);
								}
							}
						}
					}

				}

			}

		}

		printSummary(targetClazz, hitClazz);
	}

	public void printSummary(IgnoreCaseArrayList targetClazz,
			IgnoreCaseArrayList hitClazz) {
		// Not existed class:
		UConsole.log("\n\n\n"
				+ "================== Summary Begin =====================");
		UConsole.log("==================       All:" + targetClazz.size());
		UConsole.log("==================       Hit:" + hitClazz.size());
		UConsole.log("================== Summary End =====================\n\n\n");
		if (hitClazz.size() != targetClazz.size()){
			printSummaryDiff(targetClazz, hitClazz);
		}
	}

	public void printSummaryDiff(IgnoreCaseArrayList targetClazz,
			IgnoreCaseArrayList hitClazz) {
		UConsole.log("================== Not Exist LIST Begin=====================");
		for (int j = 0; j < targetClazz.size(); j++) {
			String classToCheck = targetClazz.get(j);
			if (!hitClazz.contains(classToCheck)) {
				UConsole.log((j + 1) + "\t" + classToCheck);
			}
		}
		UConsole.log("================== Not Exist Num:"
				+ (targetClazz.size() - hitClazz.size()));
		UConsole.log("================== Not Exist LIST End =====================");
	}

	public boolean isValidPackage(IProject project) throws CoreException {
		return project.isOpen()
				&& project.isNatureEnabled("org.eclipse.jdt.core.javanature");
	}

	public void analyzeJavaElement(Inputs inputs,
			IgnoreCaseArrayList targetClazz,
			IgnoreCaseArrayList hitClazzIgnoreCase, IJavaElement javaElement,
			int type) throws JavaModelException {

		if (javaElement instanceof IType) {
			// System.out.println("--------IType "
			// + javaElement.getElementName());

			String className = javaElement.getElementName();
			if (inputs.isTargetClass(className)) {
				// Class Counter
				hitClazzIgnoreCase.add(className);

				// IMethod
				IMethod[] methods = ((IType) javaElement).getMethods();
				boolean isHit = false;

				for (IMethod method : methods) {
					if (inputs.isTargetMethod(method.getElementName())) {
						isHit = true;
						// method.getDeclaringType().getFullyQualifiedName();//
						// myjdtplugin.actions.FindMethodCallers
						String prefix = (targetClazz.indexOf(className) + 1)
								+ (type == IPackageFragmentRoot.K_SOURCE ? "\t[s]\t"
										: "\t[b]\t") + className + "\t";
						UConsole.log(prefix + UCaller.getMethodFullName(method));
					}
				}

				if (!isHit) {
					// No method in this class
					String prefix = (targetClazz.indexOf(className) + 1)
							+ (type == IPackageFragmentRoot.K_SOURCE ? "\t[s_NO_METHOD]\t"
									: "\t[b_NO_METHOD]\t") + className;
					UConsole.log(prefix);
					return;
				}
			}

			/*
			 * IInitializer IInitializer[] inits = ((IType) javaElement)
			 * .getInitializers(); for (IInitializer init : inits) { System.out
			 * .println("----------------initializer: " +
			 * init.getElementName()); }
			 *
			 * // IField IField[] fields = ((IType) javaElement) .getFields();
			 * for (IField field : fields) { System.out
			 * .println("----------------field: " + field.getElementName()); }
			 */

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