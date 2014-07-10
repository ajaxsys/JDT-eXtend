package com.github.ajaxsys.jdtx.utils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Inputs {

	private IgnoreCaseArrayList targetClazz = new IgnoreCaseArrayList();
	private List<String> targetMethodRegExps = new ArrayList<String>();
	private List<String> targetJarPackageRegExps = new ArrayList<String>();

	public Inputs(String path1, String path2){
		try {
			UFile.addLinesToCollection(path1, "\n", targetClazz);
			UFile.addLinesToCollection(path2, "\n", targetMethodRegExps);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Inputs(String path1, String path2, String path3){
		this(path1, path2);

		try {
			UFile.addLinesToCollection(path3, "\n", targetJarPackageRegExps);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public boolean isTargetClass(String className) {
		// Notice: ignore case
		return targetClazz.contains(className);
	}

	public boolean isTargetPackageInJar(String pkgName) {
		for (String pkgRegexp : targetJarPackageRegExps) {
			if (pkgName.matches(pkgRegexp)) {
				return true;
			}
		}
		return false;
	}

	public boolean isTargetMethod(String methodName) {
		for (String methodRegexp : targetMethodRegExps) {
			if (methodName.matches(methodRegexp)) {
				return true;
			}
		}
		return false;
	}

	public IgnoreCaseArrayList getTargetClazz() {
		return targetClazz;
	}

	public List<String> getTargetMethodRegExps() {
		return targetMethodRegExps;
	}

	public List<String> getTargetJarPackageRegExps() {
		return targetJarPackageRegExps;
	}

}
