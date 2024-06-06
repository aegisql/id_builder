package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.utils.Utils;
import org.junit.Test;

import static com.aegisql.id_builder.utils.Utils.formatBinary;
import static org.junit.Assert.*;

public class BinaryIdGeneratorTest {

    @Test
    public void basicIdTest() {
        BinaryIdGenerator ig = new BinaryIdGenerator((short) 32, (short) 8,0xFF,System.currentTimeMillis()/1000,24,8);
        System.out.println(ig);
        ig.asStream().limit(10).forEach(id->{
            System.out.println(id);
            System.out.println(formatBinary(id));
        });

    }

}