/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package com.canary;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;

public class CanaryUtils {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final AtomicInteger numDataCollected = new AtomicInteger(0);

    public static ScheduledExecutorService createDataCollector(int warmupLoops, int loops, long timerSecs,
            AtomicInteger opts, AtomicBoolean done,
            ArrayList<Integer> warmupResults, ArrayList<Integer> results) {
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int numCollected = numDataCollected.incrementAndGet();
                if (numCollected <= warmupLoops) {
                    int warmupResult = opts.getAndSet(0);
                    System.out.println("warm up: " + warmupResult);
                    warmupResults.add(warmupResult);
                    return;
                }
                if (numCollected > loops + warmupLoops) {
                    done.set(true);
                    return;
                }
                int result = opts.getAndSet(0);
                System.out.println("result: " + result);
                results.add(result);
            }
        }, timerSecs, timerSecs, TimeUnit.SECONDS);
        return scheduler;
    }

    public static double calculateAverage(ArrayList<Integer> list) {
        return list.stream().mapToDouble(d -> d).average().orElse(0.0);
    }

    public static double calculateVariance(ArrayList<Integer> list) {
        /* This seems wrong... */
        double sumDiffsSquared = 0.0;
        double avg = calculateAverage(list);
        for (int value : list)
        {
            double diff = value - avg;
            diff *= diff;
            sumDiffsSquared += diff;
        }
        return sumDiffsSquared  / (list.size()-1);
     }

     public static void printResult(ArrayList<Integer> list) {
         double avg = calculateAverage(list);
         double various = calculateVariance(list);
         System.out.println("Result collected has: " + list.size());
         System.out.println("avg of all samples: " + avg);
         System.out.println("various of all samples: " + various);
      }
}
