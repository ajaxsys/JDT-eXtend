package com.github.ajaxsys.jdtx.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

@SuppressWarnings("restriction")
public class UCaller {
	// Dummy source for testing: In case of same name. class > field
	class Test {
		public Test() {
		};
	}

	int Test;
	// Dummy source end:

	private final String TAB = "\t";

	private CallHierarchy callHierarchy = CallHierarchy.getDefault();;

	/**
	 *
	 * @param member
	 * @param logPrefix
	 *            lineNo + splitter(*)
	 * @param isRecursive
	 */
	public void callHierachyOfWorkspaceProject(String member, String logPrefix,
			boolean isRecursive) {
		IMember classOrFieldOrMethod = null;
		try {

			classOrFieldOrMethod = UClass.guessIMember(member);
			if (classOrFieldOrMethod == null) {
				// System.out.println(new StringBuilder().append("[NOT EXIST] ")
				// .append(member).append("\n").toString());
				UConsole.log(new StringBuilder().append(logPrefix)
						.append("[NOT EXIST] ").append(member).append("\n")
						.toString());
				return;
			}

			setWorkspaceSearchScope();

			// key:Member value:the first line number
			Map<IMember, Long> matchedMembers = new LinkedHashMap<IMember, Long>();
			StringBuilder outputBuffer = new StringBuilder();

			callHierarchyRecursive(classOrFieldOrMethod, "\t", matchedMembers,
					outputBuffer, new RecursiveCalledCouter(), isRecursive,
					logPrefix);

			// System.out.println(outputBuffer.toString());
			UConsole.log(outputBuffer.toString());

			// Free Memory used in Recursive
			matchedMembers = null;
			outputBuffer = null;

		} catch (CoreException e) {
			UConsole.log(logPrefix + "[FATAL]" + e.getMessage());
		}

	}

	private class RecursiveCalledCouter {
		public long outputLinesNum = 0;
	}

	private void callHierarchyRecursive(IMember m, String prepend,
			Map<IMember, Long> matchedMembers, StringBuilder outputBuffer,
			RecursiveCalledCouter cnt, boolean isRecursive, String logPrefix) {
		cnt.outputLinesNum++;

		// 1) Existed
		if (matchedMembers.containsKey(m)) {
			// Recursive methods!
			// System.out.println(prepend + "[R-" + (matchedMembers.indexOf(m) +
			// 1)
			// + "]" + m.toString());
			outputBuffer.append(logPrefix).append("[R-")
					.append(matchedMembers.get(m)).append("]").append(prepend)
					.append(getSimpleQulifiedName(m)).append("\n");
			return;
		} else {
			// `m` must be print before call cursively
			matchedMembers.put(m, new Long(cnt.outputLinesNum));
		}

		Set<IMember> callerMethods = getCallersOf(m);

		// 2) Inner class pattern
		if (callerMethods.size() == 0 && isInnerClass(m.getParent())) {
			// 2-0) Print method itself to [Y].
			outputBuffer.append(logPrefix).append("[Y]").append(prepend)
					.append(getSimpleQulifiedName(m)).append("\n");
			prepend = prepend + TAB;

			// 2-1) Inner class, print inner class. MUST manual
			// plus 1 line.
			// Get Inner class
			IJavaElement innerClass = m.getParent();
			IMember innerClassM = (IMember) innerClass;

			// `innerClassM` must be print before call cursively
			cnt.outputLinesNum++;

			if (!matchedMembers.containsKey(innerClassM)) {
				matchedMembers.put(innerClassM, cnt.outputLinesNum);
			}

			// 2-2) creator of anonymous class must be method, so call
			IJavaElement methodCallInnerClass = innerClass.getParent();
			if (methodCallInnerClass instanceof IMethod) {
				outputBuffer.append(logPrefix).append("[Y_con]")
						.append(prepend)
						.append(getSimpleQulifiedName(innerClassM))
						.append("\n");

				// recursively
				IMethod methodCallInnerClassM = (IMethod) methodCallInnerClass;
				callHierarchyRecursive(methodCallInnerClassM, prepend + TAB,
						matchedMembers, outputBuffer, cnt, true, logPrefix);
			} else {
				// Impossible root
				outputBuffer.append(logPrefix).append("[N_con_impossible]")
						.append(prepend)
						.append(getSimpleQulifiedName(innerClassM))
						.append("\n");
			}

			return;
		}

		// 3) Normal method, call next hierarchy
		outputBuffer.append(logPrefix)
				.append(callerMethods.size() > 0 ? "[Y]" : "[N]")
				.append(prepend).append(getSimpleQulifiedName(m)).append("\n");

		// Recursive MODE only
		if (isRecursive && callerMethods.size() > 0) {
			// Recursive
			for (Iterator<IMember> i = callerMethods.iterator(); i.hasNext();) {
				callHierarchyRecursive(i.next(), prepend + TAB, matchedMembers,
						outputBuffer, cnt, true, logPrefix);
			}
		}
	}

	public boolean isInnerClass(IJavaElement parent1) {
		return parent1 != null && (parent1 instanceof IMember)
				&& parent1.toString().startsWith("class <anonymous");
	}

	public static String getMethodFullName(IMethod iMethod) {
		StringBuilder name = new StringBuilder();
		name.append(iMethod.getDeclaringType().getFullyQualifiedName());
		name.append(".");
		name.append(iMethod.getElementName());
		name.append("(");

		String comma = "";
		String[] parameterTypes = iMethod.getParameterTypes();
		try {
			// String[] parameterNames = iMethod.getParameterNames();
			for (int i = 0; i < iMethod.getParameterTypes().length; ++i) {
				name.append(comma);
				name.append(Signature.toString(parameterTypes[i]));
				// name.append(" ");
				// name.append(parameterNames[i]);
				comma = ", ";
			}
		} catch (Exception e) {
		}

		name.append(")");

		return name.toString();
	}

	public String getSimpleQulifiedName(IMember m) {
		return m.toString().split("\n")[0];
	}

	private HashSet<IMember> getCallersOf(IMember m) {
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

	private HashSet<IMethod> getIMethods(MethodWrapper[] methodWrappers) {
		HashSet<IMethod> c = new HashSet<IMethod>();
		for (MethodWrapper m : methodWrappers) {
			IMethod im = getIMethodFromMethodWrapper(m);
			if (im != null) {
				c.add(im);
			}
		}
		return c;
	}

	private IMethod getIMethodFromMethodWrapper(MethodWrapper m) {
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

	// Set search scope to workspace only
	private void setWorkspaceSearchScope() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();

		// @Deprecated
		// IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();
		// callHierarchy.setFilters("*.jar");

		List<IJavaElement> iPackages = new ArrayList<IJavaElement>();
		for (IProject project : projects) {
			IJavaProject javaProject = JavaCore.create(project);
			if (!javaProject.exists())
				continue;
			// try {
			// IPackageFragmentRoot[] packageRoots =
			// javaProject.getPackageFragmentRoots();
			// for (IPackageFragmentRoot packageRoot : packageRoots) {
			// if (!(packageRoot instanceof JarPackageFragmentRoot)) {
			// iPackages.addAll(Arrays.asList(packageRoot.getPackageFragments()));
			// }
			// }
			//
			// } catch (JavaModelException e1) {
			// e1.printStackTrace();
			// }
			try {
				IPackageFragment[] packages = javaProject.getPackageFragments();
				// process each package
				for (IPackageFragment aPackage : packages) {

					// We will only look at the package from the source folder
					// K_BINARY would include also included JARS, e.g. rt.jar
					// only process the JAR files
					if (aPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
						iPackages.add(aPackage);
					}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}

		IJavaSearchScope searchScope = SearchEngine
				.createJavaSearchScope(iPackages.toArray(new IJavaElement[0]));

		callHierarchy.setSearchScope(searchScope);
	}

	/**
	 * TODO Test me. Return an IType (Source type, not Binary) for the given
	 * class name (Inner class support).
	 *
	 * @return null if no such class can be found.
	 * @throws JavaModelException
	 */
	public IType findTypeWithInnerClass(IJavaProject javaProject,
			final String className) throws JavaModelException {
		String primaryName = className;
		int i = primaryName.lastIndexOf('$');
		int occurence = 0;
		if (0 < i) {
			try {
				occurence = Integer.parseInt(primaryName.substring(i + 1));
				primaryName = primaryName.substring(0, i);
			} catch (NumberFormatException x) {
			}
		}

		/*
		 * IJavaProject.findType works for top level classes and named inner
		 * classes, but not for anonymous inner classes
		 */
		IType primaryType = javaProject.findType(primaryName);
		if (!primaryType.exists())
			return null;
		if (occurence <= 0) // if not anonymous then we done
			return primaryType;

		/*
		 * the following snippet never works, but according to the docs it
		 * should.
		 */
		IType innrType = primaryType.getType("", occurence);
		if (innrType != null) {
			String name = primaryType.getFullyQualifiedName();
			if (name.equals(className)) {
				return innrType;
			}
		}

		/*
		 * If we're looking for an anonymous inner class then we need to look
		 * through the primary type for it.
		 */
		LinkedList<IJavaElement> todo = new LinkedList<IJavaElement>();
		todo.add(primaryType);
		IType innerType = null;
		while (!todo.isEmpty()) {
			IJavaElement element = todo.removeFirst();

			if (element instanceof IType) {
				IType type = (IType) element;
				String name = type.getFullyQualifiedName();
				if (name.equals(className)) {
					innerType = type;
					break;
				}
			}

			if (element instanceof IParent) {
				for (IJavaElement child : ((IParent) element).getChildren()) {
					todo.add(child);
				}
			}
		}

		return innerType;
	}

	/**
	 * Return all match methods of workspace project only.
	 *
	 * @param clazz
	 * @param method
	 * @return
	 * @throws ExecutionException
	 */
	public Object searchMethodWithWorkspaceProjectOnly(String clazz,
			String method) throws ExecutionException {
		String qualifiedName = clazz + "." + method;

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();

		for (IProject project : projects) {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragment[] packages = null;
			try {
				if (!javaProject.exists())
					continue;

				packages = javaProject.getPackageFragments();
			} catch (JavaModelException e) {
				e.printStackTrace();
				continue;
			}

			// step 1: Create a search pattern
			SearchPattern pattern = SearchPattern.createPattern(qualifiedName,
					IJavaSearchConstants.METHOD,
					IJavaSearchConstants.DECLARATIONS,
					SearchPattern.R_EXACT_MATCH);

			// step 2: Create search scope
			// for (IPackageFragment aPackage : packages) {
			// System.out.println(aPackage.toString());
			// }

			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(packages);

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
	 * Return all match methods include JRE library.
	 *
	 * @param clazz
	 * @param method
	 * @return
	 * @throws ExecutionException
	 */
	public Object searchMethodAllIncludeJRE(String clazz, String method)
			throws ExecutionException {
		String qualifiedName = clazz + "." + method;

		// step 1: Create a search pattern
		// search methods having "abcde" as name
		SearchPattern pattern = SearchPattern.createPattern(qualifiedName,
				IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS,
				SearchPattern.R_EXACT_MATCH);

		// step 2: Create search scope
		// IJavaSearchScope scope =
		// SearchEngine.createJavaSearchScope(packages);
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
					.getDefaultSearchParticipant() }, scope, requestor, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return null;
	}

}
