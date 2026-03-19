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
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private static final AtomicInteger numDataCollected = new AtomicInteger(0);

    public static ScheduledExecutorService createDataCollector(int warmupLoops, int loops, long timerSecs,
            AtomicInteger opts, AtomicBoolean done,
            ArrayList<Double> warmupResults, ArrayList<Double> results) {
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int numCollected = numDataCollected.incrementAndGet();
                int collectedOpts = opts.getAndSet(0);
                double result = (double)collectedOpts/(double)timerSecs;
                if (numCollected <= warmupLoops) {
                    System.out.println("warm up: " + result);
                    warmupResults.add(result);
                    return;
                }
                if (numCollected > loops + warmupLoops) {
                    done.set(true);
                    return;
                }
                System.out.println("result: " + result);
                results.add(result);
            }
        }, timerSecs, timerSecs, TimeUnit.SECONDS);
        return scheduler;
    }

    public static double calculateAverage(ArrayList<Double> list) {
        return list.stream().mapToDouble(d -> d).average().orElse(0.0);
    }

    public static double calculateSTD(ArrayList<Double> list) {
        double avg = calculateAverage(list);

        double variance = 0;
        for (int i = 0; i < list.size(); i++) {
            variance += Math.pow(list.get(i) - avg, 2);
        }
        variance /= (list.size()-1);
        return Math.sqrt(variance);
     }

    /* Return avg of the list and print out the avg and std */
     public static double printResult(ArrayList<Double> list) {
         double avg = calculateAverage(list);
         double std = calculateSTD(list);
         System.out.println("Result collected has: " + list.size());
         System.out.println("avg of all samples: " + avg);
         System.out.println("Standard deviation of all samples: " + std);
         return avg;
      }
}
