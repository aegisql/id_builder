package com.aegisql.id_builder.utils;

import com.aegisql.id_builder.IdSourceException;

import java.util.function.LongSupplier;
import java.util.function.Supplier;

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

    public static String formatBinary(long value) {
        String binaryString = String.format("%64s", Long.toBinaryString(value)).replace(' ', '0');

        StringBuilder formattedBinary = new StringBuilder();

        for (int i = 0; i < binaryString.length(); i++) {
            formattedBinary.append(binaryString.charAt(i));
            if ((i + 1) % 8 == 0 && i != binaryString.length() - 1) {
                formattedBinary.append(' ');
            }
        }

        return  formattedBinary.toString();
    }

}
