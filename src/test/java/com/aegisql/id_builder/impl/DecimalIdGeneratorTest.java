package com.aegisql.id_builder.impl;

import static com.aegisql.id_builder.utils.Utils.formatBinary;
import static com.aegisql.id_builder.utils.Utils.unixTimestamp;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

import com.aegisql.id_builder.IdParts;
import com.aegisql.id_builder.IdSource;
import com.aegisql.id_builder.IdSourceException;
import com.aegisql.id_builder.TimeTransformer;
import org.junit.Test;

public class DecimalIdGeneratorTest {

	@Test
	public void testWithConstructor() throws InterruptedException {
		final long timeBase = unixTimestamp() - 1;
		DecimalIdGenerator ig1 = new DecimalIdGenerator(1001,6,4);
		ig1.setTimeTransformer(t -> t - timeBase);
		for (int i = 0; i < 10; i++) {
			long id = ig1.getId();
			System.out.println(id + " -- "+ig1.parse(id));
			Thread.sleep(300);
			assertTrue(id < 100000000000L);
		}
	}

	@Test
	public void test1() {
		long time = unixTimestamp();
		DecimalIdGenerator ig1 = DecimalIdGenerator.idGenerator_10x4x5(1001,time);
		long prev = 0;
		long next = 0;
		for( int i = 1; i < 1000001; i++ ) {
			next = ig1.getId();
			assertTrue((next != prev));
			prev = next;
			if( (i % 100000) == 0 ){
				System.out.println("1: id["+i+"] = "+ next + " -- " + unixTimestamp() + " -- "+ig1.parse(next));
			}
		}
		System.out.println("last generated id = "+ next + " time = " + time + "-" + unixTimestamp());
		assertEquals(1000000, ig1.getGlobalCounter());
	}

	@Test
	public void test2() {
		long time = unixTimestamp();
		DecimalIdGenerator ig1 = DecimalIdGenerator.idGenerator_10x4x5(1001,5+unixTimestamp());
		
		ig1.setPastShiftSlowDown(1.5);
		
		long prev = 0;
		long next = 0;
		for( int i = 1; i < 1000001; i++ ) {
			next = ig1.getId();
			assertTrue((next != prev));
			prev = next;
			if( (i % 50000) == 0 ){
				System.out.println("2: id["+i+"] = "+ next + " -- " + unixTimestamp());
			}
		}
		System.out.println("last generated id = "+ next + " time = "+time+ "-" + unixTimestamp());
	}


	@Test
	public void test3() {
		long time = unixTimestamp();
		var ig1 = DecimalIdGenerator.idGenerator_10x4x5( 3123 );
		System.out.println(ig1);
		long next = 0;
		for( int i = 0; i <= 12345; i++ ) {
			next = ig1.getId();
		}
			
		IdParts id = ig1.parse(next);
		System.out.println(next);
		System.out.println(id);
		assertEquals(time,id.timestamp());
		assertEquals(12346,id.currentId());
		assertEquals(id.hostId(),3123);
		System.out.println(next+" -- "+id + " @ " + id.getIdDateTime());

	}

	
	@Test
	public void test4() {
		DecimalIdGenerator ig1 = DecimalIdGenerator.idGenerator_10x4x5(1001);
		
		ig1.setPastShiftSlowDown(1.25);
		
		Set<Long> ids = new HashSet<>();
		
		final long now = currentTimeMillis();
		final long delay = 3000;
		
		ig1.setTimestampSupplier(()->{
			long timestamp = currentTimeMillis();
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
			IdParts s = ig1.parse(next);
			max = Math.max(max, s.currentId());
			if( (i % 50000) == 0 ){
				System.out.println("4: id["+i+"] = "+ s + " -- " + unixTimestamp());
			}
		}
		assertEquals(1000000, ids.size());
		System.out.println("Max GeneratedID = "+max);
	}

	@Test
	public void test8() {
		long time = unixTimestamp();
		var ig1 = DecimalIdGenerator.idGenerator_10x8(1,time);
		long prev = 0;
		long next = 0;
		for( int i = 1; i < 10000001; i++ ) {
			next = ig1.getId();
			assertTrue((next != prev));
			prev = next;
			IdParts ip = ig1.parse(next);
			if( (i % 100000) == 0 ){
				System.out.println("8: id["+i+"] = "+ ip + " -- " + unixTimestamp());
			}
		}
		System.out.println("last generated id = "+ next + " time = " + time + "-" + unixTimestamp());
	}

	@Test
	public void test8slow() throws InterruptedException {
		DecimalIdGenerator ig1 = DecimalIdGenerator.idGenerator_10x8();
		
		ig1.setPastShiftSlowDown(1.1);
		
		Set<Long> ids = new HashSet<>(10000001);
		
		final long now = currentTimeMillis();
		final long delay = 500;
		
		ig1.setTimestampSupplier(()->{
			long timestamp = currentTimeMillis();
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
			IdParts s = ig1.parse(next);
			max = Math.max(max, s.currentId());
			if( (i % 100000) == 0 ){
				System.out.println("8s: id["+i+"] = "+ s + " -- " + unixTimestamp());
				Thread.sleep(1);
			}
		}
		assertEquals(10000000, ids.size());
		System.out.println("Max GeneratedID = "+max);
	}

	@SuppressWarnings("unchecked")
    @Test
	public void mutliThreadTest() throws InterruptedException {
		final int threadCount = 20;
		final int iterationsPerThread = 1000000;
		IdSource ig1 = DecimalIdGenerator.idGenerator_10x8();
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		final Set<Long>[] results = new Set[threadCount];
		for (int i = 0; i < threadCount; i++) {
			results[i] = new HashSet<>();
		}

		Set<Long> allResults = new HashSet<>();

		for (int i = 0; i < threadCount; i++) {
			int thread = i;
			executorService.execute(() -> {
				for (int j = 0; j < iterationsPerThread; j++) {
					long id = ig1.getId();
					results[thread].add(id);
					if( j > 0 && j % 100000 == 0 ) {
						System.out.println("thread "+thread+" "+(j*100)/iterationsPerThread+"%");
					}

				}
				latch.countDown();
			});
		}

		boolean await = latch.await(10, TimeUnit.MINUTES);
		assertTrue(await);
		for (int i = 0; i < threadCount; i++) {
			assertEquals(iterationsPerThread,results[i].size());
			allResults.addAll(results[i]);
		}

		assertEquals(iterationsPerThread*threadCount,allResults.size());

	}

	@Test
	public void testStream() {
		LongStream stream = DecimalIdGenerator.idGenerator_10x4x5(1001).asStream();
		stream.limit(10).forEach(System.out::println);
	}

	@Test(expected = IdSourceException.class)
	public void testHostIdExeption() {
		DecimalIdGenerator.idGenerator_10x4x5(10010);
	}

	@Test
	public void noHostGeneratorTest() {
		DecimalIdGenerator ig = new DecimalIdGenerator();
		ig.setTimeTransformer(TimeTransformer.adjustedEpoch);
		System.out.println(ig);
		AtomicLong prev = new AtomicLong();
		ig.asStream().limit(10).forEach(id->{
			System.out.println(id+" -- "+ig.parse(id));
			assertTrue(prev.get() < id);
			prev.set(id);
		});
	}

	@Test
	public void parserTest() {
		var p090 = getParts(0,9,0);
		assertEquals(-1,p090.hostId());

		var p581 = getParts(5,8,1);
		assertEquals(5,p581.hostId());

		var p5572 = getParts(55,7,2);
		assertEquals(55,p5572.hostId());

		var p55563 = getParts(555,6,3);
		assertEquals(555,p55563.hostId());

		var p55555536 = getParts(555555,3,6);
		assertEquals(555555,p55555536.hostId());

	}

	private IdParts getParts(int hostId, int idPos, int hostIdPos) {
		long timestamp = unixTimestamp();
		var ig = new DecimalIdGenerator(hostId,timestamp,idPos,hostIdPos);
		ig.setTimestampSupplier(()->timestamp); //always the same time
		long id = ig.asStream().skip(100).findFirst().orElse(-1L);
		var parts = ig.parse(id);
		System.out.println(id+" -- " + formatBinary(id)+" -- "+parts);
		assertEquals(timestamp,parts.timestamp());
		assertEquals(101,parts.currentId());
		return parts;
	}

}
