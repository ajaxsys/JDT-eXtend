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
import com.github.ajaxsys.jdtx.utils.ProgressMonitorJob;
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
abstract class FindMethodBase
    implements
        IWorkbenchWindowActionDelegate {

    final String SPLITER = "\t";

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
          //System.out.println(dialog.getQualifiedNames());

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
    @Override
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

                inputText.replaceAll("\r", "");

            } catch (FileNotFoundException e) {
                System.out.println("[ERROR] File read NG: " + e.getMessage());
                return;
            }

            final String[] members = inputText.split("\n");

            final ProgressMonitorJob job = new ProgressMonitorJob(
                members.length);
            job.schedule();

            // EXECUTE
            if (isMultiThread) {
                runMultiThread(action, members, job);
            } else {
                runSingleThread(action, members, job);
            }

        } catch (IOException e) {
            e.printStackTrace();
            UConsole.log("[FATAL] " + e.getMessage() + " - " + UFile.getJDTExtendHome("list.txt OR result.txt"));
        }  catch (Exception e) {
            e.printStackTrace();
            UConsole.log("[FATAL]" + e.getMessage());
        }
    }

    void runSingleThread(
        IAction action,
        final String[] members,
        final ProgressMonitorJob job) {

        UConsole.log("[[[ Start@Single Thread..... ]]]");

        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                for (int i = 0; i < members.length; i++) {
                    final String member = members[i];
                    final String logPrefix = (i + 1) + SPLITER;

                    // Skip blank line
                    if ("".equals(member.trim())) {
                        job.doneOne();
                        continue;
                    }

                    new UCaller().callHierachyOfWorkspaceProject(member,
                            logPrefix, isRecursive);
                    job.doneOne();

                    if(!job.isWorking()) {
                        UConsole.log("[[[ Cancelled at ExcuteTime "
                                + (System.currentTimeMillis() - start) + " ]]]");
                        UConsole.close();
                        return;
                    }

                }
                UConsole.log("[[[ ExcuteTime "
                        + (System.currentTimeMillis() - start) + " ]]]");
                UConsole.close();
            }

        }).start();

    }

    void runMultiThread(
        IAction action,
        String[] members,
        final ProgressMonitorJob job) {

        UConsole.log("[[[ Start@Multi Thread..... ]]]");

        final long start = System.currentTimeMillis();

        // Keep a processor for OS
        final ExecutorService exec = Executors.newFixedThreadPool(Runtime
                .getRuntime().availableProcessors() - 1);

        for (int i = 0; i < members.length; i++) {
            final String member = members[i];
            final String logPrefix = (i + 1) + SPLITER;
            // Skip blank line
            if ("".equals(member.trim())) {
                job.doneOne();
                continue;
            }

            exec.execute(new Runnable() {
                @Override
                public void run() {
                    new UCaller().callHierachyOfWorkspaceProject(member,
                            logPrefix, isRecursive);
                    job.doneOne();
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
                    while(!exec.awaitTermination(200, TimeUnit.MILLISECONDS)) {
                        if (!job.isWorking()) {
                            UConsole.log("[[[ Cancelled at ExcuteTime "
                                    + (System.currentTimeMillis() - start) + " ]]]");
                            exec.shutdownNow();
                            break;
                        }
                        // System.out.println("[[[ ExcuteTime " +
                        // (System.currentTimeMillis() -
                        // start) + " ]]]");
                        UConsole.log("[[[ ExcuteTime "
                                + (System.currentTimeMillis() - start) + " ]]]");
                    }
                } catch (InterruptedException e) {
                }

                UConsole.close();

                job.stopForce(); // Just for worry about accident
            }
        }).start();
    }

    public void runSample() {
        final String clazz = "com.test.example.pkg.MyIF";
        final String method = "query";
        try {
            System.out.println("searchMethodAllIncludeJRE----------------------------------------------------");
            new UCaller().searchMethodAllIncludeJRE(clazz, method);

            System.out.println("searchMethodWithWorkspaceProjectOnly----------------------------------------------------");
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
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * We can use this method to dispose of any system resources we previously
     * allocated.
     *
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    @Override
    public void dispose() {
    }

    /**
     * We will cache window object in order to be able to provide parent shell
     * for the message dialog.
     *
     * @see IWorkbenchWindowActionDelegate#init
     */
    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }
}