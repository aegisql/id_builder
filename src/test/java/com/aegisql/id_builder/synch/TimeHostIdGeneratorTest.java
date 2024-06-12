package com.aegisql.id_builder.synch;

import com.aegisql.id_builder.old_impl.TimeHostIdGenerator;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TimeHostIdGeneratorTest {

    @SuppressWarnings("unchecked")
    @Test
    public void multiThreadTest() throws InterruptedException {
        final int threadCount = 20;
        final int iterationsPerThread = 1000000;
        var ig1 = TimeHostIdGenerator.idGenerator_10x8();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        final Set<Long>[] results = new Set[threadCount];
        for (int i = 0; i < threadCount; i++) {
            results[i] = new HashSet<>(iterationsPerThread);
        }

        Set<Long> allResults = new HashSet<>(iterationsPerThread*threadCount);

        long startTime = System.nanoTime();
        for (int i = 0; i < threadCount; i++) {
            int thread = i;
            executorService.execute(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    long id = ig1.getId();
                    results[thread].add(id);
                    if( j > 0 && j % 100000 == 0 ) {
                        Thread.yield();
//                        System.out.println("thread "+thread+" "+(j*100)/iterationsPerThread+"%");
                    }
                }
                latch.countDown();
            });
        }
        boolean await = latch.await(10, TimeUnit.MINUTES);
        assertTrue(await);
        long elapsed = System.nanoTime() - startTime;
        System.out.println("Generated IDs: "+ig1.getGlobalCounter()+" Generation pace: "+ig1.getGlobalCounter()/(elapsed/1000000000.0)/1000000+" M/sec");

        for (int i = 0; i < threadCount; i++) {
            assertEquals(iterationsPerThread,results[i].size());
            allResults.addAll(results[i]);
        }

        assertEquals(iterationsPerThread*threadCount,allResults.size());

    }


}