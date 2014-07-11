package com.github.ajaxsys.jdtx.actions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

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
public class FindMethodCallers implements IWorkbenchWindowActionDelegate {
	boolean isRecursive = true;
	boolean isMultiThread = true;

	final String SPLITER = "*";
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public FindMethodCallers() {
		this.isRecursive = true;
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 *
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(final IAction action) {
		boolean isContinued = UDialog.showUsage(this, window);
		if (!isContinued) {
			return;
		}

		try {
			UConsole.init(UFile.getJDTExtendHome("result.txt"));
			String inPath = UFile.getJDTExtendHome("list.txt");
			String inputText = "";

			try {
				// * Input sample of list.txt:
				// myjdtplugin.utils.UCaller.equals(String[], String[])
				// myjdtplugin.utils.UCaller.getCallersOf(IMember)
				// myjdtplugin.utils.UCaller.TAB
				// myjdtplugin.utils.UCaller
				// myjdtplugin.utils.UClass.UClass()
				// myjdtplugin.utils.UCaller.NOT_EXIST
				inputText = UFile.getText(inPath);

			} catch (FileNotFoundException e) {
				System.out.println("[ERROR] File read NG: " + e.getMessage());
				return;
			}

			final String[] members = inputText.split("\n");

			Job job = new Job("FindMethodCallers Job") {
				private boolean canceled = false;

				@Override
				protected void canceling() {
					System.out.println("Cancel requested.");
					canceled = true;
				}

				@Override
				protected IStatus run(IProgressMonitor monitor) {

					// Set total number of work units
					monitor.beginTask("Start task ", members.length);

					// EXECUTE
					if (isMultiThread) {
						return runMultiThread(action, members, monitor);
					} else {
						return runSingleThread(action, members, monitor);
					}
				}

				// Single
				IStatus runSingleThread(IAction action, final String[] members,
						final IProgressMonitor monitor) {
					UConsole.log("[[[ Start@Single Thread..... ]]]");

					long start = System.currentTimeMillis();

					for (int i = 0; i < members.length; i++) {
						int lineNo = i + 1;
						if (canceled)
							break;

						final String member = members[i];
						final String logPrefix = lineNo + SPLITER;

						// Skip blank line
						if ("".equals(member.trim()))
							continue;

						monitor.subTask("Calling " + lineNo + "/"
								+ members.length);

						new UCaller().callHierachyOfWorkspaceProject(member,
								logPrefix, isRecursive);

						monitor.worked(getIncreesePercentage(lineNo,
								members.length));

					}
					endLog(start);

					return canceled ? Status.CANCEL_STATUS : Status.OK_STATUS;
				}

				public int getIncreesePercentage(double now, double all) {

//					if (all <= 100) {
//						// e.g now=6 all=11, percent 11/100 = 0.11 , each 1/0.11
//						// = 9 percent
//						return new Double(100 / all).intValue();
//					} else {
//						// e.g now=62 all=110, percent 11/100 = 1.1, now_ajuest
//						// = 62/1.1 = 56.3 ~ 57
//						Double newBef = Math.floor((now - 1) / (all / 100));
//						Double newNxt = Math.floor((now) / (all / 100));
//						if (newNxt.intValue() > newBef.intValue()) {
//							return 1;
//						} else {
//							return 0;
//						}
//					}
					 if (all > now) {
						 return 1;
					 } else {
						 return 0;
					 }
				}

				// Multiple
				IStatus runMultiThread(IAction action, final String[] members,
						final IProgressMonitor monitor) {
					UConsole.log("[[[ Start@Multi Thread..... ]]]");

					final long start = System.currentTimeMillis();

					// Keep a processor for OS
					final ExecutorService exec = Executors
							.newFixedThreadPool(Runtime.getRuntime()
									.availableProcessors() - 1);

					// String[] members = inputText.split("\n");
					for (int i = 0; i < members.length; i++) {
						final int lineNo = i + 1;
						if (canceled) {
							break;
						}

						final String member = members[i];
						final String logPrefix = lineNo + SPLITER;
						// Skip blank line
						if ("".equals(member.trim()))
							continue;

						exec.execute(new Runnable() {

							@Override
							public void run() {
								if (canceled) {
									return;
								}
								monitor.subTask("Calling " + lineNo + "/"
										+ members.length);

								new UCaller().callHierachyOfWorkspaceProject(
										member, logPrefix, isRecursive);

								monitor.worked(getIncreesePercentage(lineNo,
										members.length));
							}
						});
					}

					// Waiting for ExecutorService end
					exec.shutdown();
					try {
						exec.awaitTermination(Long.MAX_VALUE,
								TimeUnit.NANOSECONDS);
					} catch (InterruptedException e) {
					}

					endLog(start);

					return canceled ? Status.CANCEL_STATUS : Status.OK_STATUS;
				}

				private void endLog(long start) {
					UConsole.log("[[[ "
							+ (canceled ? "Canceled " : "ExcuteTime ")
							+ +(System.currentTimeMillis() - start) + " ]]]");
					UConsole.close();
				}
			};

			job.schedule();

		} catch (IOException e) {
			e.printStackTrace();
			UConsole.log("[FATAL] " + e.getMessage() + " - "
					+ UFile.getJDTExtendHome("list.txt OR result.txt"));
		} catch (Exception e) {
			e.printStackTrace();
			UConsole.log("[FATAL]" + e.getMessage());
		}
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