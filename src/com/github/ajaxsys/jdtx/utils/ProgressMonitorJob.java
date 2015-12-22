package com.github.ajaxsys.jdtx.utils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ProgressMonitorJob extends Job {

    private int sizeToDo;
    private AtomicInteger counter = new AtomicInteger(0);
    private AtomicBoolean isStop = new AtomicBoolean(false);

    private IProgressMonitor monitor = null;

    public ProgressMonitorJob(int sizeToDo) {
        super("JDT-eXtend job");

        this.sizeToDo = sizeToDo;

        if (this.sizeToDo <= 0 )
            throw new RuntimeException("Invalid size of job, must be > 0");
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        this.monitor  = monitor;

        monitor.beginTask("JDT-eXtend job", sizeToDo);
        try {

            while (true) {
                if (isWorking() &&
                    counter.get() < sizeToDo) {

                    // NG: monitor.worked(counter.get());
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }

                } else {
                    break;
                }
            }

            if (!isWorking()) {

                System.out.println("Stop at No: " + counter.get());
                return Status.CANCEL_STATUS;
            } else if(counter.get() == sizeToDo) {

                System.out.println("debug: stop at all job finished:" + counter.get());
                return Status.OK_STATUS;
            } else {
                System.out.println("debug: Unkown");
                return Status.CANCEL_STATUS;
            }

        } finally {
            monitor.done();
        }
    }

    public void doneOne() {
        counter.incrementAndGet();
        monitor.worked(1);
    }

    public void stopForce() {
        if (monitor != null && !monitor.isCanceled()) {
            System.out.println("Force to be cancel!");
            isStop.set(true);
        }
    }

    public
    boolean
    isWorking() {
        return !monitor.isCanceled() &&
               !isStop.get();
    }
}