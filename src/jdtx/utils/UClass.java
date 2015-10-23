package jdtx.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

public class UClass {

	private UClass() {
	}

	/**
	 * Guess a IMember instance from qualified name<br>
	 * * IMember is the parent of IType, IMethod, IField<br>
	 *
	 * @param qualifiedName
	 * @return
	 * @throws CoreException
	 */
	public static IMember guessIMember(String qualifiedName)
			throws CoreException {
		new UClass();// Test

		IType type = null;

		// 1) Method
		if (isAMethod(qualifiedName)) {
			String klass = guessClassFromQualifiedName(qualifiedName);
			String method = guessMethodFromQualifiedName(qualifiedName);
			String[] methodParams = UClass
					.guessMethodParamsFromQualifiedName(qualifiedName);

			type = findTypeInWorkspace(klass);
			if (type == null) {
				return null;
			}

			return findMethod(type, method, methodParams);
		}

		// 2) Class
		type = findTypeInWorkspace(qualifiedName);

		// 3) Field
		if (type == null) {
			// Try to detect if a field. e.g: foo.bar.NOT_GIVEN = "";
			String klass = qualifiedName.substring(0,
					qualifiedName.lastIndexOf("."));
			String field = qualifiedName.substring(qualifiedName
					.lastIndexOf(".") + 1);

			type = findTypeInWorkspace(klass);
			if (type != null) {
				IField iField = findFieldByName(type, field);
				if (iField == null) {
					return null;
				}
				return iField;

			}
		} else {
			// It is a class
			return type;
		}

		return null;

	}

	/**
	 * A perfect way to get a method from IType by method name and parameters.
	 *
	 * @param type
	 *            Target type(usually a class) to search
	 * @param methodName
	 *            Human readable method name
	 * @param methodParamTypes
	 *            Human readable method parameters type
	 * @return Target method
	 */
	public static IMethod findMethod(IType type, String methodName,
			String[] methodParamTypes) {

		String[] arraySignatures = convertMethodParamsToSignatureType(methodParamTypes);

		// NOTICE: in this stage, the method is `not open`
		IMethod methodMayBeNotOpen = type
				.getMethod(methodName, arraySignatures);
		if (!methodMayBeNotOpen.toString().contains("(not open)")) {
			return methodMayBeNotOpen;
		}
		// change `not open` to `open`
		IMethod[] methods = type.findMethods(methodMayBeNotOpen);
		if (methods != null && methods.length > 0) {
			if (methods.length > 1) {
				UConsole.log("[WARN]Found results of method '"
						+ methodMayBeNotOpen + "' number is " + methods.length);
			}
			return methods[0];
		}

		// // TODO support Initializer!
		// if (methodNotOpen != null) {
		// //System.out.println("[WARN] Constructor NOT SUPPORT!");
		// try {
		// IMethod constructor = findConstructor(type, methodName,
		// methodParamTypes);
		// return constructor;
		// } catch (JavaModelException e) {
		// System.out.println("[WARN]Constructor get exception" +
		// e.getMessage());
		// }
		// return methodNotOpen;
		// }

		return null;

		// normalMethod1(String, Integer) (not open) [in NormalClazz [in
		// NormalClazz.java [in com.test.example.pkg [in src [in Tmp-Java2]]]]]
		// -- NG with --
		// void normalMethod1(java.lang.String, Integer) [in NormalClazz [in
		// NormalClazz.java [in com.test.example.pkg [in src [in Tmp-Java2]]]]]

		// IMethod[] methods = type.getMethods();
		// for (IMethod iMethod : methods) {
		// // I don't understand why method is '(not open)'!
		// // So return it from all methods
		// // NG
		// if (iMethod.toString().endsWith(
		// methodNotOpen.toString().replace(" (not open)", ""))) {
		// // if (equals(iMethod.getElementName(), method.getElementName())
		// // && equals(iMethod.getParameterNames(),
		// // method.getParameterNames())) {
		// return iMethod;
		// }
		// }

	}

	public static IField findFieldByName(IType type, String fieldName)
			throws JavaModelException {
		// IType type = project.findType(typeName);

		// IMethod[] methods = type.getMethods();
		IField[] fields = type.getFields();
		IField theField = null;
		for (IField iField : fields) {
			if (iField.getElementName().equals(fieldName)) {
				theField = iField;
			}
		}

		if (theField == null) {
			// System.out.println("Error, field " + fieldName + " not found");
			return null;
		}

		return theField;
	}

	/**
	 * For example. <br>
	 * Convert `boolean` to `Z`. <br>
	 * Convert `String` to `QString`.
	 *
	 * @param methodParams
	 * @return
	 */
	public static String[] convertMethodParamsToSignatureType(
			String[] methodParams) {
		String[] arraySignatures = new String[methodParams.length];
		for (int i = 0; i < methodParams.length; i++) {
			arraySignatures[i] = Signature.createTypeSignature(methodParams[i],
					false); // false: unresolved
		}
		return arraySignatures;
	}

	public static IType findTypeInWorkspace(String klass) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (IProject project : projects) {
			if (project.exists() && project.isOpen() &&
			        project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject javaProject = JavaCore.create(project);
				IType type = javaProject.findType(klass);
				if (type != null)
					return type;
			}
		}
		return null;
	}

	/**
	 * Guess class name from Qualified Name.<br>
	 * If it is a method, remove the method;<br>
	 * If it is a class/field, return it;<br>
	 * NOTICE: it maybe a field name, you should check it!
	 *
	 * @param path
	 * @return
	 */
	public static String guessClassFromQualifiedName(String path) {
		if (isAMethod(path)) {
			// [3] a.b.Class.Method(..) --> a.b.Class.Method
			path = path.substring(0, path.indexOf('('));
			// [4] a.b.Class.Method --> a.b.Class
			path = path.substring(0, path.lastIndexOf('.'));
		}
		return path;
	}

	/**
	 * Guess method name from Qualified Name.<br>
	 * If it is NOT a method, return null;<br>
	 * NOTICE: it maybe a field name, you should check it!
	 *
	 * @param path
	 * @return
	 */
	public static String guessMethodFromQualifiedName(String path) {
		if (isAMethod(path)) {
			// [3] a.b.Class.Method(..) --> a.b.Class.Method
			path = path.substring(0, path.indexOf('('));
			// [4] a.b.Class.Method --> Method
			path = path.substring(path.lastIndexOf('.') + 1);
			return path;
		}
		return null;
	}

	/**
	 * Return parameters of the Qualified Name of a method.<br>
	 * if it is a method but no parameter, it return a empty string.<br>
	 * If it is NOT a method, return null;
	 *
	 * @param path
	 * @return
	 */
	public static String[] guessMethodParamsFromQualifiedName(String path) {
		final String SPLITTER = ", ";
		if (isAMethod(path)) {
			// com.test.example.pkg.NormalClazz.normalMethod1(String, int)
			// --> String, int
			path = path.substring(path.lastIndexOf('(')).replace("(", "")
					.replace(")", "").trim();
			// String, int --> Array[ String, int ]
			if ("".equals(path)) {
				return new String[] {};
			}
			if (path.contains(SPLITTER)) {
				return path.split(SPLITTER);
			}
			return new String[] { path };
		}
		return null;
	}

	/**
	 * Is a method Qualified Name
	 *
	 * @param path
	 * @return
	 */
	public static boolean isAMethod(String path) {
		return path.indexOf('(') > 0;
	}

    //
    // /**
    // * This method works same with `findMethod`, but use a different way that
    // * compare by string directly.<br>
    // * But this method may cause some problems when `getSignatureSimpleName`
    // * return a wrong type
    // *
    // * @param type
    // * @param methodName
    // * @param methodParamTypesFromText
    // * @return
    // * @throws JavaModelException
    // */
    // @Deprecated
    // public static IMethod findMethod2(IType type, String methodName,
    // String[] methodParamTypesFromText) throws JavaModelException {
    //
    // IMethod[] methods = type.getMethods();
    // for (IMethod iMethod : methods) {
    // String[] methodParamTypes = getHumanReadableMethodParameters(iMethod);
    //
    // if (equals(iMethod.getElementName(), methodName)
    // && equals(methodParamTypes, methodParamTypesFromText)) {
    //
    // return iMethod;
    // }
    // }
    // return null;
    // }
    //
    // /**
    // * TODO how check constructor?
    // *
    // * @param type
    // * @param methodName
    // * @param methodParamTypesFromText
    // * @return
    // * @throws JavaModelException
    // */
    // @Deprecated
    // public static IMethod findConstructor(IType type, String methodName,
    // String[] methodParamTypesFromText) throws JavaModelException {
    //
    // // IInitializer[] methods = type.getInitializers();
    // IMethod[] methods = JavaElementUtil.getAllConstructors(type);
    // for (IMethod iMethod : methods) {
    // String[] methodParamTypes = getHumanReadableMethodParameters(iMethod);
    //
    // if (equals(iMethod.getElementName(), methodName)
    // && equals(methodParamTypes, methodParamTypesFromText)) {
    //
    // return iMethod;
    // }
    // }
    // return null;
    // }
    //
    // /**
    // * Get human readable method parameters which is not `Type Signature`.
    // *
    // * @param iMethod
    // * @return
    // * @throws JavaModelException
    // */
    // public static String[] getHumanReadableMethodParameters(IMethod iMethod)
    // throws JavaModelException {
    // // DO NOT Use getParameterNames, that returns same results of
    // // `Integer` & `int`
    // // System.out.println(iMethod.getParameterNames());
    // // FIXME `getParameters()` not work in eclipse 3.5
    // ILocalVariable[] parameters = iMethod.getParameters();
    // String[] methodParamTypes = new String[parameters.length];
    //
    // for (int i = 0; i < parameters.length; i++) {
    // // String param = iLocalVariable.getElementName();
    // ILocalVariable iLocalVariable = parameters[i];
    //
    // String signature = iLocalVariable.getTypeSignature();
    // // **************************************
    // // Returns type fragment of a type signature. The package fragment
    // // separator must be '.' and the type fragment separator must be
    // // '$'.
    // // For example:
    // // getSignatureSimpleName("Ljava.util.Map$Entry") -> "Map.Entry"
    // // **************************************
    // // NG because java.lang.String --> Ljava.lang.String
    // // String paramType = Signature.toString(signature);
    // // TODO CHECK ME: is `getSignatureSimpleName` always return the
    // // correct parameter type?
    // methodParamTypes[i] = Signature.getSignatureSimpleName(signature);
    // }
    // return methodParamTypes;
    // }

	public static boolean equals(String[] strs1, String[] strs2) {
		if (strs1.length != strs2.length) {
			return false;
		}
		for (int i = 0; i < strs1.length; i++) {
			if (!equals(strs1[i], strs2[i])) {
				return false;
			}
		}
		return true;
	}

	public static boolean equals(String str1, String str2) {
		return (str1 == null ? str2 == null : str1.equals(str2));
	}

}
