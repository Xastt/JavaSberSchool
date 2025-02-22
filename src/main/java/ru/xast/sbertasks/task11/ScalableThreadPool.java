package ru.xast.sbertasks.task11;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * class which realize scalable thread pool
 * @author Khasrovyan Artyom
 */
public class ScalableThreadPool implements ThreadPool {

    private final int minPoolSize;
    private final int maxPoolSize;
    private final BlockingQueue<Runnable> workQueue;
    private final WorkerThread[] workerThreads;
    private AtomicInteger currentPoolSize;
    private volatile boolean isShutdown = false;

    public ScalableThreadPool(int minPoolSize, int maxPoolSize) {
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.workQueue = new ArrayBlockingQueue<>(100);
        this.workerThreads = new WorkerThread[maxPoolSize];
        this.currentPoolSize = new AtomicInteger(minPoolSize);

        for (int i = 0; i < minPoolSize; i++) {
            workerThreads[i] = new WorkerThread(workQueue, this);
            workerThreads[i].start();
        }
    }
    /**
     * Starts thread's
     */
    @Override
    public void start() {
        //not necessary, because min pool creates in constructor
    }

    /**
     * Puts task's into the queue.
     * The thread must execute this task.
     * @param runnable
     */
    @Override
    public synchronized void execute(Runnable runnable) {
        if (isShutdown) {
            throw new IllegalStateException("ThreadPool is shutdown");
        }
        if (runnable == null) {
            throw new NullPointerException("Runnable task is null");
        }
        workQueue.offer(runnable);
        if (workQueue.size() > currentPoolSize.get() && currentPoolSize.get() < maxPoolSize) {
            addWorker();
        }
    }

    /**
     * increase amount of active threads if it's possible
     */
    private void addWorker(){
        if (currentPoolSize.get() < maxPoolSize) {
            WorkerThread worker = new WorkerThread(workQueue, this);
            worker.start();
            workerThreads[currentPoolSize.get()] = worker;
            currentPoolSize.incrementAndGet();
        }
    }

    /**
     * Shuts down the pool, new tasks will not be accepted.
     */
    public synchronized void shutdown() {
        isShutdown = true;
        for (WorkerThread worker : workerThreads) {
            if(worker != null) {
                worker.interrupt();
            }
        }
    }

    /**
     * try to stop all actively executing task
     * @return List<Runnable>
     */
    public synchronized List<Runnable> shutdownNow() {
        isShutdown = true;
        List<Runnable> remainingTasks = new ArrayList<>();
        workQueue.drainTo(remainingTasks);
        for (WorkerThread worker : workerThreads) {
            if(worker != null) {
                worker.interrupt();
            }
        }
        return remainingTasks;
    }

    /**
     * For processing tasks in a multithreaded.
     * It used in a thread pool where several threads
     * can execute tasks from the same blocking queue.
     */
    private static class WorkerThread extends Thread {
        private final BlockingQueue<Runnable> workQueue;
        private final ScalableThreadPool threadPool;

        public WorkerThread(BlockingQueue<Runnable> workQueue, ScalableThreadPool threadPool) {
            this.workQueue = workQueue;
            this.threadPool = threadPool;
        }

        public void run() {
            while (true) {
                try{
                    Runnable task = workQueue.take();
                    task.run();
                    threadPool.reduceWorkerIfNecessary();
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                    return;
                }
                if(threadPool.isShutdown && workQueue.isEmpty()){
                    break;
                }
//                if(threadPool.isShutdown){
//                    return;
//                }
            }
        }
    }

    /**
     * decrease amount of threads if it's possible
     */
    private synchronized void reduceWorkerIfNecessary() {
        if (currentPoolSize.get() > minPoolSize && workQueue.isEmpty()) {
            currentPoolSize.decrementAndGet();
        }
    }
}
