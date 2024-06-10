package com.aegisql.id_builder.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void upperBitTest() {
        long mask = Utils.setHigherBits(8);
        System.out.println(mask + " -- "+Utils.formatBinary(mask));
    }
    @Test
    public void lowerBitTest() {
        long mask = Utils.setLowerBits(8);
        System.out.println(mask + " -- "+Utils.formatBinary(mask));
    }

    @Test
    public void powTest() {
        assertEquals(100,Utils.pow10Sticky(2));
        assertEquals(Integer.MAX_VALUE,Utils.pow10Sticky(10));
        assertEquals(1024,Utils.pow2Sticky(10));
        assertEquals(Integer.MAX_VALUE,Utils.pow2Sticky(48));
    }

}