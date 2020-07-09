package com.github.hinaser.gfma.markdown;

public class ThrottlePoolExecutor {
    private long defaultTimeout;
    private Thread delayedThread;

    public ThrottlePoolExecutor(long throttleTimeout) {
        this.defaultTimeout = throttleTimeout;
    }

    public synchronized void queue(Runnable runnable, long timeout) {
        clear();
        this.delayedThread = new Thread(new DelayedCancelableTask(runnable, timeout));
        this.delayedThread.start();
    }

    public synchronized void queue(Runnable runnable) {
        queue(runnable, defaultTimeout);
    }

    public synchronized void clear() {
        if (delayedThread != null) {
            delayedThread.interrupt();
            delayedThread = null;
        }
    }

    public void resetTimeout(long timeout){
        if(timeout > 0) {
            this.defaultTimeout = timeout;
        }
    }

    private static class DelayedCancelableTask implements Runnable {
        private final Runnable runnable;
        private final long delay;

        public DelayedCancelableTask(Runnable runnable, long delay) {
            this.runnable = runnable;
            this.delay = delay;
        }

        @Override
        public void run() {
            try {
                synchronized (this) {
                    wait(delay);
                }
            }
            catch (InterruptedException e) {
                return;
            }
            runnable.run();
        }
    }
}
