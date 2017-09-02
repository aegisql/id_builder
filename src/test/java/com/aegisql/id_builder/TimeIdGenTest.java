package com.aegisql.id_builder;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aegisql.id_builder.impl.TimeBasedIdGenerator;

public class TimeIdGenTest {

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
	public void test1() {
		long time = System.currentTimeMillis()/1000;
		IdSource ig1 = TimeBasedIdGenerator.idGenerator_10x4x5(1001,time);
		long prev = 0;
		long next = 0;
		for( int i = 1; i < 1000001; i++ ) {
			next = ig1.getId();
			assertTrue((next != prev));
			prev = next;
			if( (i % 100000) == 0 ){
				System.out.println("1: id["+i+"] = "+ next + " -- " + System.currentTimeMillis()/1000);
			}
		}
		System.out.println("last generated id = "+ next + " time = " + time + "-" + System.currentTimeMillis()/1000);
	}

	@Test
	public void test2() throws InterruptedException {
		long time = System.currentTimeMillis()/1000;
		IdSource ig1 = TimeBasedIdGenerator.idGenerator_10x4x5(1001,5+(System.currentTimeMillis()/1000));
		
		((TimeBasedIdGenerator)ig1).setPastShiftSlowDown(1.5);
		
		long prev = 0;
		long next = 0;
		for( int i = 1; i < 1000001; i++ ) {
			next = ig1.getId();
			assertTrue((next != prev));
			prev = next;
			if( (i % 50000) == 0 ){
				System.out.println("2: id["+i+"] = "+ next + " -- " + System.currentTimeMillis()/1000);
			}
		}
		System.out.println("last generated id = "+ next + " time = "+time+ "-" + System.currentTimeMillis()/1000);
	}


	@Test
	public void test3() throws InterruptedException {
		long time = System.currentTimeMillis()/1000;
		IdSource ig1 = TimeBasedIdGenerator.idGenerator_10x4x5( 3123 );
		long next = 0;
		for( int i = 0; i <= 12345; i++ ) {
			next = ig1.getId();
		}
			
		IdParts id = TimeBasedIdGenerator.split_10x4x5(next);
		assertEquals(id.getCurrentId(),12345);
		assertEquals(id.getDatacenterId(),3);
		assertEquals(id.getHostId(),123);
		System.out.println(next+" -- "+id + " @ " + id.getIdDateTime());

	}

	
	@Test
	public void test4() throws InterruptedException {
		long time = System.currentTimeMillis()/1000;
		IdSource ig1 = TimeBasedIdGenerator.idGenerator_10x4x5(1001);
		
		((TimeBasedIdGenerator)ig1).setPastShiftSlowDown(1.25);
		
		Set<Long> ids = new HashSet<>();
		
		final long now = System.currentTimeMillis();
		final long delay = 3000;
		
		((TimeBasedIdGenerator)ig1).setTimestampSupplier(()->{
			long timestamp = System.currentTimeMillis();
			if(timestamp - now < delay) {
				return timestamp;
			} else {
				return timestamp - 3000;
			}
		});
		
		long max = 0;
		for( int i = 1; i < 1000001; i++ ) {
			Long next = ig1.getId();
			assertFalse(ids.contains(next));
			ids.add(next);
			IdParts s = TimeBasedIdGenerator.split_10x4x5(next);
			max = Math.max(max, s.getCurrentId());
			if( (i % 50000) == 0 ){
				System.out.println("4: id["+i+"] = "+ s + " -- " + System.currentTimeMillis()/1000);
//				Thread.sleep(400);
			}
		}
		assertEquals(1000000, ids.size());
		System.out.println("Max GeneratedID = "+max);
	}

	@Test
	public void test8() {
		long time = System.currentTimeMillis()/1000;
		IdSource ig1 = TimeBasedIdGenerator.idGenerator_10x8(time);
		long prev = 0;
		long next = 0;
		for( int i = 1; i < 10000001; i++ ) {
			next = ig1.getId();
			assertTrue((next != prev));
			prev = next;
			IdParts ip = TimeBasedIdGenerator.split_10x8(next);
			if( (i % 100000) == 0 ){
				System.out.println("8: id["+i+"] = "+ ip + " -- " + System.currentTimeMillis()/1000);
			}
		}
		System.out.println("last generated id = "+ next + " time = " + time + "-" + System.currentTimeMillis()/1000);
	}

	@Test
	public void test8slow() throws InterruptedException {
		long time = System.currentTimeMillis()/1000;
		IdSource ig1 = TimeBasedIdGenerator.idGenerator_10x8();
		
		((TimeBasedIdGenerator)ig1).setPastShiftSlowDown(1.1);
		
		Set<Long> ids = new HashSet<>(10000001);
		
		final long now = System.currentTimeMillis();
		final long delay = 500;
		
		((TimeBasedIdGenerator)ig1).setTimestampSupplier(()->{
			long timestamp = System.currentTimeMillis();
			if(timestamp - now < delay) {
				return timestamp;
			} else {
				return timestamp - 2000;
			}
		});
		
		long max = 0;
		for( int i = 1; i < 10000001; i++ ) {
			Long next = ig1.getId();
			assertFalse(ids.contains(next));
			ids.add(next);
			IdParts s = TimeBasedIdGenerator.split_10x8(next);
			max = Math.max(max, s.getCurrentId());
			if( (i % 100000) == 0 ){
				System.out.println("8s: id["+i+"] = "+ s + " -- " + System.currentTimeMillis()/1000);
				Thread.sleep(1);
			}
		}
		assertEquals(10000000, ids.size());
		System.out.println("Max GeneratedID = "+max);
	}

	
	
}
