package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdParts;
import com.aegisql.id_builder.IdSourceException;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.aegisql.id_builder.TimeTransformer.identity;
import static com.aegisql.id_builder.utils.Utils.formatBinary;
import static com.aegisql.id_builder.utils.Utils.unixTimestamp;
import static org.junit.Assert.*;

public class BinaryIdGeneratorTest {

    @Test
    public void basicAdjustedIdTest() {
        long time = unixTimestamp();
        System.out.println("time: "+formatBinary(time));
        BinaryIdGenerator ig = new BinaryIdGenerator(time, (short) 1,0xFF,12);
        System.out.println(ig);
        AtomicLong prev = new AtomicLong();
        ig.asStream().limit(1000000).forEach(id->{
            if(ig.getGlobalCounter()%100000==0) {
                System.out.println(id + " -- " + formatBinary(id)+" -- "+ig.parse(id));
            }
            assertTrue(prev.get() < id);
            prev.set(id);
        });

    }

    @Test
    public void basicIdTest() {
        long time = unixTimestamp();
        System.out.println("time: "+formatBinary(time));
        BinaryIdGenerator ig = new BinaryIdGenerator(time, (short) 1,0xFF,12);
        ig.setTimeTransformer(identity);//want to see same bits
        System.out.println(ig);
        AtomicLong prev = new AtomicLong();
        ig.asStream().limit(1000000).forEach(id->{
            if(ig.getGlobalCounter()%100000==0) {
                System.out.println(id + " -- " + formatBinary(id));
            }
            assertTrue(prev.get() < id);
            prev.set(id);
        });

    }

    @Test(expected = IdSourceException.class)
    public void testHostIdException() {
        new BinaryIdGenerator(unixTimestamp(), (short) 1,0xFFFF,12);
    }

    @Test(expected = IdSourceException.class)
    public void testHostBitsException() {
        new BinaryIdGenerator(unixTimestamp(), (short) 1,0xFFFF,-12);
    }

    @Test
    public void noHostGeneratorTest() {
        BinaryIdGenerator ig = new BinaryIdGenerator();
        System.out.println(ig);
        AtomicLong prev = new AtomicLong();
        ig.asStream().limit(10).forEach(id->{
            System.out.println(id+" -- "+ig.parse(id) + " "+ig.parse(id).getIdDateTime());
            assertTrue(prev.get() < id);
            prev.set(id);
        });
    }

    @Test
    public void parserTest() {
        var p000 = getParts(0,0,0);
        assertEquals(-1,p000.hostId());

        var p011 = getParts(0,1,1);
        assertEquals(1,p011.hostId());

        var p132 = getParts(1,3,2);
        assertEquals(3,p132.hostId());

    }

    private IdParts getParts(int shift, int hostId, int hostBits) {
        long timestamp = unixTimestamp();

        var ig0 = new BinaryIdGenerator(timestamp, (short) shift,hostId,hostBits);
        ig0.setTimestampSupplier(()->timestamp); //always same time
        long id0 = ig0.asStream().skip(100).findFirst().orElse(-1L);
        var parts0 = ig0.parse(id0);
        System.out.println(id0+" -- " + formatBinary(id0)+" -- "+parts0);
        assertEquals(timestamp,parts0.timestamp());
        assertEquals(101,parts0.currentId());
        return parts0;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void multiThreadTest() throws InterruptedException {
        final int threadCount = 20;
        final int iterationsPerThread = 1000000;
        var ig1 = new BinaryIdGenerator();
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

    @Test
    public void lastIdTest() {
        var ig0 = new BinaryIdGenerator(unixTimestamp(), (short) 1,2,4).asStream().skip(10);
        var ig = BinaryIdGenerator.fromLastKnownId(ig0.findFirst().orElse(-1L), (short) 1,2,4);
        long id = ig.getId();
        System.out.println(id);
        var parts = ig.parse(id);
        assertEquals(2,parts.hostId());
        assertEquals(12,parts.currentId()); // this can randomly fail.
    }


}