package jdtx.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.Scanner;

public class UFile {
	public static String getText(String path) throws FileNotFoundException {
		File file = new File(path);
		if (!file.exists()) {
			UConsole.log("[FATAL] File NOT exist:" + path);
			return "";
		}

		Scanner in = new Scanner(new FileReader(file));
		StringBuilder stringBuilder = new StringBuilder();
		while (in.hasNextLine())
			stringBuilder.append(in.nextLine() + "\n");
		in.close();
		return stringBuilder.toString();
	}

	public static void addLinesToCollection(String path, String splitter,
			Collection<String> collect) throws FileNotFoundException {
		String text = getText(path);
		String[] lines = text.split(splitter);
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (!"".equals(line)) {
				if (collect.contains(line)) {
					UConsole.log((i + 1) + "\t [WARN] Dumplicated inputs:\t"
							+ line);
				} else {
					collect.add(line);
				}
			}
		}
	}

	public static String getUserHome() {
		return System.getProperty("user.home") + File.separator;
	}

	public static String getJDTExtendHome() {
		return new StringBuilder().append(getUserHome())
				.append("CallCheck").append(File.separator).toString();
	}

	public static String getJDTExtendHome(String path) {
		return getJDTExtendHome() + path;
	}
}
