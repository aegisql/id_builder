package com.aegisql.id_builder;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aegisql.id_builder.impl.RandomBasedIdGenerator;

public class RandomIdGeneratorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		IdSource idgen = new RandomBasedIdGenerator();
		long x1 = idgen.getId();
		System.out.println("ID1 = " + x1);
		long x2 = idgen.getId();
		System.out.println("ID2 = " + x2);
		assertTrue((x2-x1) == 1);
		assertTrue((""+x1).length() == 19);
	}

}
