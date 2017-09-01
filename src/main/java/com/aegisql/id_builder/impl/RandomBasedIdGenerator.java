package com.aegisql.id_builder.impl;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.aegisql.id_builder.IdSource;

public class RandomBasedIdGenerator implements IdSource {

	AtomicLong idHolder = null;
	
	public RandomBasedIdGenerator() {
		Random r  = new Random( System.nanoTime() );
		long base = 1000000000000000000L;
		long seed = r.nextInt( 1000000000 );
		base     += seed * 1000000000L;
		idHolder  = new AtomicLong( base );
	}
	
	@Override
	public long getId() {
		return idHolder.getAndIncrement();
	}

}
