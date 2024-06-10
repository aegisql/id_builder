package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdParts;
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

}