package com.github.ajaxsys.jdtx.utils;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;

import com.github.ajaxsys.jdtx.actions.FindMethodCallers;
import com.github.ajaxsys.jdtx.actions.FindMethodCallersNoRecursive;
import com.github.ajaxsys.jdtx.actions.FindMethodCallersSingleThread;
import com.github.ajaxsys.jdtx.actions.SearchMethods;

public class UDialog {

	public static boolean showUsage(Object callers, IWorkbenchWindow window) {

		if (callers instanceof FindMethodCallersNoRecursive) {
			return showUsageOfFindMethodCallers(window, "[Check Only - show top 1/multi thead]");
		} else if (callers instanceof FindMethodCallersSingleThread) {
			return showUsageOfFindMethodCallers(window, "[Recursive - show all/single thead]");
		} else if (callers instanceof FindMethodCallers) {
			return showUsageOfFindMethodCallers(window, "[Recursive - show all/multi thead]");
		} else if (callers instanceof SearchMethods) {
			return showUsageOfSearchMethods(window);
		}
		return false;
	}

	private static boolean showUsageOfFindMethodCallers(
			IWorkbenchWindow window, String extraMessage) {
		return MessageDialog.openConfirm(window.getShell(), "Call Hierarchy " + extraMessage,
				new StringBuilder()
		.append("Get all call hierarchy automatically: \n")
		.append("\n")
		.append("- INPUT: list.txt \n(Path ")
		.append(UFile.getJDTExtendHome("list.txt"))
		.append(")\n: qualifier name of method/class/field. e.g: `foo.bar.FooClazz.getBar()`\n")
		.append("\n")
		.append("- OUTPUT: result.txt \n(Path ")
		.append(UFile.getJDTExtendHome("result.txt"))
		.append(")\n: Call hierarchy result. \n\n")
		.append("\n")
		.append("Continue?")
		.toString());
	}

	private static boolean showUsageOfSearchMethods(IWorkbenchWindow window) {
		return MessageDialog.openConfirm(
				window.getShell(),
				"Search Methods by Regexp",
				new StringBuilder()
						.append("Get all method qualifier name by specified class name & method regexp pattern. \n")
						.append("\n")
						.append("- INPUT: input1.txt \n(Path ")
						.append(UFile.getJDTExtendHome("input1.txt"))
						.append(")\n: Class Name. e.g: `FooClazz`\n")
						.append("NOTICE: no package name, no extension, no regexp.\n")
						.append("\n")
						.append("- INPUT: input2.txt \n(Path ")
						.append(UFile.getJDTExtendHome("input2.txt"))
						.append(")\n: method regexp, e.g: `update.*`\n")
						.append("\n")
						.append("- INPUT: input3.txt [optional] \n(Path ")
						.append(UFile.getJDTExtendHome("input3.txt"))
						.append(")\n: package regexp pattern in jar file, if nothing set here, it will skip search reference jar. e.g: `com.model.business.*`\n")
						.append("\n")
						.append("* Output - path:")
						.append(UFile.getJDTExtendHome("output.txt"))
						.append(")\n: Matched method list. \n\n")
						.append("\n")
						.append("Continue?")
						.toString());
	}

}
