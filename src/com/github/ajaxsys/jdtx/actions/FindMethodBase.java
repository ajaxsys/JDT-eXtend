package com.github.ajaxsys.jdtx.actions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.github.ajaxsys.jdtx.dialog.QualifiedNameDialog;
import com.github.ajaxsys.jdtx.utils.UCaller;
import com.github.ajaxsys.jdtx.utils.UConsole;
import com.github.ajaxsys.jdtx.utils.UDialog;
import com.github.ajaxsys.jdtx.utils.UFile;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 *
 * @see IWorkbenchWindowActionDelegate
 */
abstract class FindMethodBase implements IWorkbenchWindowActionDelegate {
    final String SPLITER = "*";

    boolean isRecursive = true;
	boolean isMultiThread = true;

	private IWorkbenchWindow window;

	abstract String getText() throws Exception;

	String getTextFromFile() throws FileNotFoundException {
	    boolean isContinued = UDialog.showUsage(this, window);
        if (!isContinued){
            return null;
        }

	    String inPath = UFile.getJDTExtendHome("list.txt");
	    return UFile.getText(inPath);
	}

    String getTextFromDialog() {
        QualifiedNameDialog dialog = new QualifiedNameDialog(window.getShell());
        dialog.create();
        if (dialog.open() == Window.OK) {
          System.out.println(dialog.getQualifiedNames());

          if (dialog.getQualifiedNames().length() > 0 ) {
              return dialog.getQualifiedNames();
          }
        }
        return null;
    }

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 *
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {

		try {
		    UConsole.init(UFile.getJDTExtendHome("result.txt"));

			String inputText = null;
			try {
				// * Input sample of list.txt:
				// myjdtplugin.utils.UCaller.equals(String[], String[])
				// myjdtplugin.utils.UCaller.getCallersOf(IMember)
				// myjdtplugin.utils.UCaller.TAB
				// myjdtplugin.utils.UCaller
				// myjdtplugin.utils.UClass.UClass()
				// myjdtplugin.utils.UCaller.NOT_EXIST
				inputText = getText();

				if (inputText == null) {
				    return;
				}

			} catch (FileNotFoundException e) {
				System.out.println("[ERROR] File read NG: " + e.getMessage());
				return;
			}

			// EXECUTE
			if (isMultiThread) {
				runMultiThread(action, inputText);
			} else {
				runSingleThread(action, inputText);
			}

		} catch (IOException e) {
			e.printStackTrace();
			UConsole.log("[FATAL] " + e.getMessage() + " - " + UFile.getJDTExtendHome("list.txt OR result.txt"));
		}  catch (Exception e) {
			e.printStackTrace();
			UConsole.log("[FATAL]" + e.getMessage());
		}
	}

	void runSingleThread(IAction action, String inputText) {
		UConsole.log("[[[ Start@Single Thread..... ]]]");

		final String[] members = inputText.split("\n");

		new Thread(new Runnable() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();
				for (int i = 0; i < members.length; i++) {
					final String member = members[i];
					final String logPrefix = (i + 1) + SPLITER;

					// Skip blank line
					if ("".equals(member.trim()))
						continue;

					new UCaller().callHierachyOfWorkspaceProject(member,
							logPrefix, isRecursive);

				}
				UConsole.log("[[[ ExcuteTime "
						+ (System.currentTimeMillis() - start) + " ]]]");
				UConsole.close();
			}

		}).start();

	}

	void runMultiThread(IAction action, String inputText) {
		UConsole.log("[[[ Start@Multi Thread..... ]]]");

		final long start = System.currentTimeMillis();

		// Keep a processor for OS
		final ExecutorService exec = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors() - 1);

		String[] members = inputText.split("\n");
		for (int i = 0; i < members.length; i++) {
			final String member = members[i];
			final String logPrefix = (i + 1) + SPLITER;
			// Skip blank line
			if ("".equals(member.trim()))
				continue;

			exec.execute(new Runnable() {
				@Override
				public void run() {
					new UCaller().callHierachyOfWorkspaceProject(member,
							logPrefix, isRecursive);
				}
			});
		}

		// Not block the UI Thread
		new Thread(new Runnable() {
			@Override
			public void run() {
				// Waiting for ExecutorService end
				exec.shutdown();
				try {
					exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
				} catch (InterruptedException e) {
				}

				// System.out.println("[[[ ExcuteTime " +
				// (System.currentTimeMillis() -
				// start) + " ]]]");
				UConsole.log("[[[ ExcuteTime "
						+ (System.currentTimeMillis() - start) + " ]]]");
				UConsole.close();
			}
		}).start();
	}

	public void runSample() {
		final String clazz = "com.test.example.pkg.MyIF";
		final String method = "query";
		try {
			System.out
					.println("searchMethodAllIncludeJRE----------------------------------------------------");
			new UCaller().searchMethodAllIncludeJRE(clazz, method);
			System.out
					.println("searchMethodWithWorkspaceProjectOnly----------------------------------------------------");
			new UCaller().searchMethodWithWorkspaceProjectOnly(clazz, method);

		} catch (Exception e) {
			e.printStackTrace();
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