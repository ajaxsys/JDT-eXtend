package com.github.ajaxsys.jdtx.samples;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
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
public class GetAllMethodCallers implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	private int totalMethod = 0;

	/**
	 * The constructor.
	 */
	public GetAllMethodCallers() {
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
//			getCallersOf(root);
			execute(null);
			System.out.println("-------------");
			execute2(null);
			
			
			System.out.println("-------------");
			//getCallersOf();
			// TODO com.test.example.pkg.MyIF.query(InnerKey)
			IProject[] projects = root.getProjects();
			for (IProject project : projects) {
				System.out.println("project name: " + project.getName());

				if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
					IJavaProject javaProject = JavaCore.create(project);
					 try {
						    IType type = javaProject.findType("com.test.example.pkg.MyIF");
						    
					        if(type == null){
					            continue;
					        }
					        callHierarchy(type, "query");
					    } catch (JavaModelException e) {
					        // Stacktrace
					        e.printStackTrace();
					    }    
				}
					
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	
	
	private void callHierarchy(IType type, String method) throws JavaModelException {

		IMethod m = findMethod(type, method);
		Set<IMember> methods = new HashSet<IMember>();
		methods = getCallersOf(m);
		for (Iterator<IMember> i = methods.iterator(); i.hasNext();) {
			System.out.println(i.next().toString());
		}
	}

	IMethod findMethod(IType type, String methodName) throws JavaModelException
	{
	    //IType type = project.findType(typeName);

	    IMethod[] methods = type.getMethods();
	    IMethod theMethod = null;

	    for (int i = 0; i < methods.length; i++)
	    {
	        IMethod imethod = methods[i];
	        System.out.println(imethod.getElementName()); // query
	        System.out.println(imethod.getParameterNames()); // [key]
	        System.out.println(imethod.getHandleIdentifier()); // =Tmp-Java2/src<com.test.example.pkg{Test.java[MyIF~query~QFoo.InnerKey;
	        System.out.println(imethod.getKey()); // Lcom/test/example/pkg/Test~MyIF;.query(QFoo/InnerKey;)V
	        System.out.println(imethod.getReturnType()); // V
	        System.out.println(imethod.getSignature()); // (QFoo.InnerKey;)V
	        System.out.println(imethod.getSource()); // public void query(Foo.InnerKey key); // Method to open call hierarchy
	        System.out.println(imethod.getExceptionTypes()); // []
	        
	        System.out.println(imethod.getParameterTypes()); // [QFoo.InnerKey;]
	        System.out.println(imethod.getParameterTypes()[0]); // [QFoo.InnerKey;]
	        
	        System.out.println(imethod.getClassFile());// null
	        System.out.println(imethod.getDeclaringType().getFullyQualifiedName());// com.test.example.pkg.MyIF
	        System.out.println(imethod.getNumberOfParameters());// 1
	        System.out.println(imethod.getDeclaringType());/*
	        interface MyIF [in [Working copy] Test.java [in com.test.example.pkg [in src [in Tmp-Java2]]]]
  void query(Foo.InnerKey)
  void query(Bar.InnerKey)*/
	        System.out.println(imethod.getTypeRoot()); /*
	        [Working copy] Test.java [in com.test.example.pkg [in src [in Tmp-Java2]]]
  package com.test.example.pkg
  class Test
    static void foo()
    static void bar()
    static MyIF getInstance()
  interface MyIF
    void query(Foo.InnerKey)
    void query(Bar.InnerKey)
  class Foo
    class InnerKey
  class Bar
    class InnerKey
    */
	        
	        if (imethod.getElementName().equals(methodName)) {
	            theMethod = imethod;
	        }
	    }

	    if (theMethod == null)
	    {           
	        System.out.println("Error, method" + methodName + " not found");
	        return null;
	    }

	    return theMethod;
	}
	
	
	
	
	
	
	
	
	public HashSet<IMember> getCallersOf(IMember m) {

		CallHierarchy callHierarchy = CallHierarchy.getDefault();

		IMember[] members = { m };

		MethodWrapper[] methodWrappers = callHierarchy.getCallerRoots(members);
		HashSet<IMember> callers = new HashSet<IMember>();
		for (MethodWrapper mw : methodWrappers) {
			MethodWrapper[] mw2 = mw.getCalls(new NullProgressMonitor());
			HashSet<IMethod> temp = getIMethods(mw2);
			callers.addAll(temp);
		}

		return callers;
	}

	HashSet<IMethod> getIMethods(MethodWrapper[] methodWrappers) {
		HashSet<IMethod> c = new HashSet<IMethod>();
		for (MethodWrapper m : methodWrappers) {
			IMethod im = getIMethodFromMethodWrapper(m);
			if (im != null) {
				c.add(im);
			}
		}
		return c;
	}

	IMethod getIMethodFromMethodWrapper(MethodWrapper m) {
		try {
			IMember im = m.getMember();
			if (im.getElementType() == IJavaElement.METHOD) {
				return (IMethod) m.getMember();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	
	
	
	
	
	
	
	
	
	

	public Object execute(ExecutionEvent event) throws ExecutionException {
		 
		// step 1: Create a search pattern
		// search methods having "abcde" as name
		SearchPattern pattern = SearchPattern.createPattern("query",
				IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS,
				SearchPattern.R_EXACT_MATCH);
	 
		// step 2: Create search scope
		// IJavaSearchScope scope = SearchEngine.createJavaSearchScope(packages);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
	 
		// step3: define a result collector
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				System.out.println(match.getElement());
			}
		};
	 
		// step4: start searching
		SearchEngine searchEngine = new SearchEngine();
		try {
			searchEngine.search(pattern, new SearchParticipant[] { SearchEngine
							.getDefaultSearchParticipant() }, scope, requestor,
							null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	 
		return null;
	}
	
	public Object execute2(ExecutionEvent event) throws ExecutionException {
		 
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
	 
		for (IProject project : projects) {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragment[] packages = null;
			try {
				packages = javaProject.getPackageFragments();
			} catch (JavaModelException e) {
				e.printStackTrace();
				continue;
			}
	 
			// step 1: Create a search pattern
			SearchPattern pattern = SearchPattern.createPattern("MyIF.query(Foo.InnerKey)",
					IJavaSearchConstants.METHOD,
					IJavaSearchConstants.DECLARATIONS,
					SearchPattern.R_EXACT_MATCH);
	 
			// step 2: Create search scope
//			for (IPackageFragment aPackage : packages) {
//				System.out.println(aPackage.toString());
//			}
			
			 IJavaSearchScope scope = SearchEngine.createJavaSearchScope(packages);
	 
			// step3: define a result collector
			SearchRequestor requestor = new SearchRequestor() {
				public void acceptSearchMatch(SearchMatch match) {
					System.out.println(match.getElement());
				}
			};
	 
			// step4: start searching
			SearchEngine searchEngine = new SearchEngine();
			try {
				searchEngine.search(pattern,
						new SearchParticipant[] { SearchEngine
								.getDefaultSearchParticipant() }, scope,
						requestor, null);
			} catch (CoreException e) {
				System.out.println("exception");
				e.printStackTrace();
			}
	 
		}
	 
		return null;
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