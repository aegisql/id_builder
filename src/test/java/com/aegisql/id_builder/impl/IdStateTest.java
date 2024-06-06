package com.aegisql.id_builder.impl;

import org.junit.Test;

import static com.aegisql.id_builder.utils.Utils.unixTimestamp;
import static org.junit.Assert.*;

public class IdStateTest {
    @Test
    public void equalsTest() {
        assertEquals(new IdState(1000,1, unixTimestamp()), new IdState(1000,1, unixTimestamp()));
        assertNotEquals(new IdState(1000,1, unixTimestamp()), new IdState(1001,1, unixTimestamp()));
    }
}