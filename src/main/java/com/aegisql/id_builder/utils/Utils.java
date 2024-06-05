package com.aegisql.id_builder.utils;

import com.aegisql.id_builder.IdSourceException;

public class Utils {

    public static void sleepOneMSec() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new IdSourceException("Unexpected Interruption", e);
        }
    }

    public static void sleepOneMSec(long id, long after) {
        if (id % after == 0) {
            sleepOneMSec();
        }
    }

    public static int pow10(int x) {
        return (int) Math.round(Math.pow(10, x));
    }

    public static int pow2(int x) {
        return (int) Math.round(Math.pow(2, x));
    }

    public static void assertPositive(int x, String format) {
        if(x < 1) {
            throw new IdSourceException(format.formatted(x));
        }
    }

}
