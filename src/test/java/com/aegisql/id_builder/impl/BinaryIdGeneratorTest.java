package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdSourceException;
import org.junit.Test;

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
            System.out.println(id+" -- "+ig.parse(id));
            assertTrue(prev.get() < id);
            prev.set(id);
        });
    }

}