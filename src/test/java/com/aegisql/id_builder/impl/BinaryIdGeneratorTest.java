package com.aegisql.id_builder.impl;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static com.aegisql.id_builder.utils.Utils.formatBinary;
import static com.aegisql.id_builder.utils.Utils.unixTimestamp;
import static org.junit.Assert.*;

public class BinaryIdGeneratorTest {

    @Test
    public void basicIdTest() {
        BinaryIdGenerator ig = new BinaryIdGenerator(unixTimestamp(), (short) 0,0xFF,10);
        System.out.println(ig);
        AtomicLong prev = new AtomicLong();
        ig.asStream().limit(100).forEach(id->{
            System.out.println(id+" -- " + formatBinary(id));
            assertTrue(prev.get() < id);
            prev.set(id);
        });

    }

}