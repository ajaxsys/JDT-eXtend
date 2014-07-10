package com.github.ajaxsys.jdtx.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class UConsole {
	static MessageConsole console = new MessageConsole("MyJDTConsole", null);

	static PrintWriter out;

	public static void init(String filePath) throws IOException {
		// For console.
		IConsoleManager manager = ConsolePlugin.getDefault()
				.getConsoleManager();
		manager.addConsoles(new IConsole[] { console });
		manager.showConsoleView(console);

		// For file log. "d:/tmp/result.txt"
		if (filePath != null && !"".equals(filePath.trim())) {
			File result = new File(filePath);
			if (result.exists()){
				result.delete();
			}
			result.createNewFile();
			if (result.exists()){
				out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
			}
		}

	}

	public static void close() {
		if (out != null){
			out.flush();
			out.close();
		}
	}

	public static synchronized void log(String message) {
		MessageConsoleStream stream = console.newMessageStream();
		stream.println(message);
		if (out != null){
			out.println(message);
			out.flush();
		}
	}
}
