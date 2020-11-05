package edu.coursera.parallel;

import sun.jvm.hotspot.runtime.Threads;

import java.util.concurrent.Phaser;

/**
 * Wrapper class for implementing one-dimensional iterative averaging using
 * phasers.
 */
public final class OneDimAveragingPhaser {
    /**
     * Default constructor.
     */
    private OneDimAveragingPhaser() {
    }

    /**
     * Sequential implementation of one-dimensional iterative averaging.
     *
     * @param iterations The number of iterations to run
     * @param myNew A double array that starts as the output array
     * @param myVal A double array that contains the initial input to the
     *        iterative averaging problem
     * @param n The size of this problem
     */
    public static void runSequential(final int iterations, final double[] myNew,
            final double[] myVal, final int n) {
        double[] next = myNew;
        double[] curr = myVal;

        for (int iter = 0; iter < iterations; iter++) {
            for (int j = 1; j <= n; j++) {
                next[j] = (curr[j - 1] + curr[j + 1]) / 2.0;
            }
            double[] tmp = curr;
            curr = next;
            next = tmp;
        }
    }

    /**
     * An example parallel implementation of one-dimensional iterative averaging
     * that uses phasers as a simple barrier (arriveAndAwaitAdvance).
     *
     * @param iterations The number of iterations to run
     * @param myNew A double array that starts as the output array
     * @param myVal A double array that contains the initial input to the
     *        iterative averaging problem
     * @param n The size of this problem
     * @param tasks The number of threads/tasks to use to compute the solution
     */
    public static void runParallelBarrier(final int iterations,
            final double[] myNew, final double[] myVal, final int n,
            final int tasks) {
        Phaser ph = new Phaser(0);
        ph.bulkRegister(tasks);

        Thread[] threads = new Thread[tasks];

        for (int ii = 0; ii < tasks; ii++) {
            final int i = ii;

            threads[ii] = new Thread(() -> {
                double[] threadPrivateMyVal = myVal;
                double[] threadPrivateMyNew = myNew;

                final int chunkSize = (n + tasks - 1) / tasks;
                final int left = (i * chunkSize) + 1;
                int right = (left + chunkSize) - 1;
                if (right > n) right = n;

                for (int iter = 0; iter < iterations; iter++) {
                    for (int j = left; j <= right; j++) {
                        threadPrivateMyNew[j] = (threadPrivateMyVal[j - 1]
                            + threadPrivateMyVal[j + 1]) / 2.0;
                    }
                    ph.arriveAndAwaitAdvance();

                    double[] temp = threadPrivateMyNew;
                    threadPrivateMyNew = threadPrivateMyVal;
                    threadPrivateMyVal = temp;
                }
            });
            threads[ii].start();
        }

        for (int ii = 0; ii < tasks; ii++) {
            try {
                threads[ii].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A parallel implementation of one-dimensional iterative averaging that
     * uses the Phaser.arrive and Phaser.awaitAdvance APIs to overlap
     * computation with barrier completion.
     *
     * TODO Complete this method based on the provided runSequential and
     * runParallelBarrier methods.
     *
     * @param iterations The number of iterations to run
     * @param myNew A double array that starts as the output array
     * @param myVal A double array that contains the initial input to the
     *              iterative averaging problem
     * @param n The size of this problem
     * @param tasks The number of threads/tasks to use to compute the solution
     */
    public static void runParallelFuzzyBarrier(final int iterations,
            final double[] myNew, final double[] myVal, final int n,
            final int tasks) {
//        System.out.println("size of problem: " + n);
//        System.out.println("size of arr: " + myNew.length);
//        register phaser
        Phaser[] phs = new Phaser[tasks];
        for(int i = 0; i < phs.length; i++)
            phs[i] = new Phaser(1);

        Thread[] threads = new Thread[tasks];

        for(int i = 0; i < tasks; i++){
            final int idx = i;
            threads[idx] = new Thread(() -> {
                double[] newArr = myNew;
                double[] oldArr = myVal;
                for(int iter = 0; iter < iterations; iter++){
                    int left = idx * (n / tasks) + 1;
                    //n is dividable by tasks
                    int right = (idx + 1) * (n / tasks);
                    /**
                     * left and right boundary are values would be used in other threads,
                     * group n right boundary value is used in the calculation of group n + 1 left boundary
                     */
                    newArr[left] = (oldArr[left - 1] + oldArr[left + 1]) / 2.0;
                    newArr[right] = (oldArr[right - 1] + oldArr[right + 1]) / 2.0;
                    //barrier
                    int currentPhase = phs[idx].arrive();

                    for(int j = left + 1; j < right; j++)
                        newArr[j] = (oldArr[j - 1] + oldArr[j + 1]) / 2.0;

                    //wait for previous phase
                    if(idx >= 1) phs[idx - 1].awaitAdvance(currentPhase);
                    if(idx + 1 < tasks) phs[idx + 1].awaitAdvance(currentPhase);

                    double[] temptArr = newArr;
                    newArr = oldArr;
                    oldArr = temptArr;
                }
            });

            threads[idx].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}