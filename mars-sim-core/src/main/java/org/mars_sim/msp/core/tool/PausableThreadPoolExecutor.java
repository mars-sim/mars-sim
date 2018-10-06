/**
 * Mars Simulation Project
 * PausableThreadPoolExecutor.java
 * @version 3.1.0 2017-11-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core.tool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class PausableThreadPoolExecutor extends ThreadPoolExecutor {

    /** initialized logger for this class. */
    private static Logger logger = Logger.getLogger(PausableThreadPoolExecutor.class.getName());

   private boolean isPaused;
   private ReentrantLock pauseLock = new ReentrantLock();
   private Condition unpaused = pauseLock.newCondition();

   /**
 * @param corePoolSize    The size of the pool
 * @param maximumPoolSize The maximum size of the pool
 * @param keepAliveTime   The amount of time you wish to keep a single task alive
 * @param unit            The unit of time that the keep alive time represents
 * @param workQueue       The queue that holds your tasks
 * @see {@link ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue)}
 */
   public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
	   super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
   }

   public PausableThreadPoolExecutor(int numberOfThreadsInThreadPool) {
       super(numberOfThreadsInThreadPool, numberOfThreadsInThreadPool, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000000));
   }

   public PausableThreadPoolExecutor(int threadsNr, int threadPriority) {
       super(threadsNr, threadsNr, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
       //logger.info("PausableThreadPoolExecutor's constructor 3 is on " + Thread.currentThread().getName() + " Thread");
       // it's in pool-2-thread-1 Thread

       // bring the thread priority between min and max priority
       final int normalizedThreadPriority = Math.max(Thread.MIN_PRIORITY, Math.min(threadPriority, Thread.MAX_PRIORITY));

       this.setThreadFactory(new ThreadFactory() {
           @Override
           public Thread newThread(Runnable r) {
               Thread thread = new Thread(r);
               thread.setDaemon(false);
               if (thread.getPriority() != normalizedThreadPriority) {
                   thread.setPriority(normalizedThreadPriority);
               }
               return thread;
           }
       });
   }

   protected void beforeExecute(Thread t, Runnable r) {
	   super.beforeExecute(t, r);
	   pauseLock.lock();
	   try {
	       while (isPaused) unpaused.await();
	     } catch(InterruptedException ie) {
	       t.interrupt();
	     } finally {
	       pauseLock.unlock();
	     }
   }

   public void pause() {
	   pauseLock.lock();
	   try {
	       isPaused = true;
	     } finally {
	       pauseLock.unlock();
	     }
	}

   public boolean isRunning() {
	   return !isPaused;
   }

   public boolean isPaused() {
	   return isPaused;
   }

   public void resume() {
	   pauseLock.lock();
	   try {
		   isPaused = false;
		   unpaused.signalAll();
	   } finally {
		   pauseLock.unlock();
	   }
   }

}