package com.aegisql.id_builder.utils;

import org.junit.Test;

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

}