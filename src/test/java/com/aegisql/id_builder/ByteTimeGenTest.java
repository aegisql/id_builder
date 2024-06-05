package com.aegisql.id_builder;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertTrue;

public class ByteTimeGenTest {

    public static final long MAX_VALUE_MASK1 = Long.parseLong("0111111111111111111111111111111111111111111111111111111111111111", 2);
    public static final long MAX_VALUE_MASK2 = Long.parseLong("0011111111111111111111111111111111111111111111111111111111111111", 2);
    public static final long MAX_VALUE_MASK3 = Long.parseLong("0001111111111111111111111111111111111111111111111111111111111111", 2);
    public static final long MAX_VALUE_MASK4 = Long.parseLong("0000111111111111111111111111111111111111111111111111111111111111", 2);
    //41 bit set for original timestamp as of now.
    public static final long TIMESTAMP_MASK0 = Long.parseLong("0000000000000000000000011111111111111111111111111111111111111111", 2);
    public static final long TIMESTAMP_MASK1 = Long.parseLong("0000000000000000000000000001111111111111111111111111111111111111", 2);

    AtomicLong atomicLong = new AtomicLong();
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    static Date base;

    static {
        try {
            base = dateFormat.parse("2024-06-01");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public long getId() {
        long time = System.currentTimeMillis();
        long cutTime = (time - base.getTime())/ 1000;
        long timeShifted = (cutTime) << 24; // can be 32, but limit time to ~ 68 years
        long counterShifted = atomicLong.getAndIncrement() << 8; // can be 16 for shorter counter.
        return timeShifted | counterShifted | 15;
    }


    @Test
    public void timestampMaskingTest() {

        long ts = System.currentTimeMillis(); //41 bit set
        System.out.println(Long.toBinaryString(ts));
        System.out.println(Long.toBinaryString(ts & TIMESTAMP_MASK0));
        System.out.println(Long.toBinaryString(ts & TIMESTAMP_MASK1));

        long cutTime = (ts & TIMESTAMP_MASK1) / 1000;
        System.out.println(Long.toBinaryString(cutTime));

        long nextAfterEpoch = ((long) Integer.MAX_VALUE + 1000) * 1000;
        System.out.println(new Date((nextAfterEpoch)));

        System.out.println(Long.toBinaryString(nextAfterEpoch));
        System.out.println(Long.toBinaryString(Long.MAX_VALUE));
        System.out.println(Long.toBinaryString(Long.MAX_VALUE & MAX_VALUE_MASK2));

        long time = (nextAfterEpoch/1000);
        long timeShift = time << 31;
        System.out.println(timeShift);
        System.out.println(Long.toBinaryString(timeShift));
        System.out.println(Long.toBinaryString(timeShift & MAX_VALUE_MASK2));
    }

    @Test
    public void maxDate() {
        System.out.println(new Date((Long.MAX_VALUE>>>31)*1000));
    }

    @Test
    public void prototypeTest() throws InterruptedException {
        long prev = Long.MIN_VALUE;
        for(int i = 0; i < 10; i++) {
            long id = getId();
            assertTrue(id > prev);
            prev = id;
            System.out.println(id+" -- "+Long.toBinaryString(id));
            Thread.sleep(500);
        }
    }
/*
1100110010111110101011001100010.0000000000000000.0000000000001111
1100110010111110101011001100010.0000000000000001.0000000000001111
1100110010111110101011001100011.0000000000000010.0000000000001111
1100110010111110101011001100011.0000000000000011.0000000000001111
1100110010111110101011001100100.0000000000000100.0000000000001111
1100110010111110101011001100100.0000000000000101.0000000000001111
1100110010111110101011001100101.0000000000000110.0000000000001111
1100110010111110101011001100101.0000000000000111.0000000000001111
1100110010111110101011001100110.0000000000001000.0000000000001111
1100110010111110101011001100110.0000000000001001.0000000000001111
 */
    @Test
    public void tryByteManipulation() {
        long time = System.currentTimeMillis();
        long shortTime = time / 1000;
        long timeShift = shortTime << 32;
        System.out.println(Long.toBinaryString(Long.MAX_VALUE));

        System.out.println(Long.toBinaryString(time));
        System.out.println(Long.toBinaryString(shortTime));
        System.out.println(Long.toBinaryString(timeShift));

        long hostId = (256*256-1);
        long timeAndHost = timeShift | hostId<<16;
        System.out.println(Long.toBinaryString(timeAndHost));

        long id = timeAndHost|15;
        System.out.println(Long.toBinaryString(id));
        System.out.println(id);

    }
/*

111111111111111111111111111111111111111111111111111111111111111
11000111111100100011000101111011011111100
1100110010111110101010010111010
110011001011111010101001011101000000000000000000000000000000000
110011001011111010101001011101011111111111111110000000000000000
110011001011111010101001011101011111111111111110000000000001111
7376707876791713807

 */
}
