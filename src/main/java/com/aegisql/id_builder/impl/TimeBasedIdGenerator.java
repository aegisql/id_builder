package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdSourceException;

import java.util.function.LongSupplier;

import com.aegisql.id_builder.IdSource;
import com.aegisql.id_builder.SplittedId;
import com.aegisql.id_builder.TimeFormatter;

public class TimeBasedIdGenerator implements IdSource {

	protected final int maxId;
	protected final int maxHostId;
	protected final long hostId;
	protected final long timeIdBase;

	protected long currentId           = 0;
	protected long totalPastTimeId     = 0;
	protected long resetTimeMs         = 0;
	protected long currentTimeStampSec = 0;
	protected double pastTimeSlowDown;
	
	private TimeFormatter tf;
	
	private LongSupplier timestamp = System::currentTimeMillis;
	
	private TimeBasedIdGenerator( int hostId, long startTimeStampSec, int maxId, int maxHostId ) {
		this.maxId               = maxId;
		this.maxHostId           = maxHostId;
		this.currentTimeStampSec = startTimeStampSec;
		this.resetTimeMs         = timestamp.getAsLong();
		
		if( hostId > maxHostId ) throw new IdSourceException("Max host ID > " + maxHostId);
		
		this.hostId     = hostId * (maxId+1);
		this.timeIdBase = (maxHostId+1) * (maxId+1);
		this.setPastTimeSlowDown(2.0);
	}
	
	public void setPastTimeSlowDown(double x) {
		this.pastTimeSlowDown = (maxId + 1)/(x * 1000);
	}

	@Override
	public final synchronized long getId() {
		long nowMs = timestamp.getAsLong();
		long now   = nowMs / 1000;
		long dt    = nowMs - (now * 1000);
		
		if( now > currentTimeStampSec ) 
			initNextSecondId(nowMs);
		else 
			if( now == currentTimeStampSec ) 
				currentSecondNextId(dt);
		else 
			processShiftToPastTime(nowMs);

		long returnId = currentId++;
		
		if( returnId > maxId ) throw new IdSourceException("Internal ID reached ultimate value: " + returnId);

		return tf.formatTimeId( currentTimeStampSec ) * timeIdBase + hostId + returnId;
	}
	
	private void currentSecondNextId( long dt ) {
		long maxPredictedId = dt*(maxId+1)/1000;
		if (currentId >= maxPredictedId) {
			try {
				Thread.sleep(1);
				long nowMs = timestamp.getAsLong();
				long now   = nowMs / 1000;
				if (now > currentTimeStampSec) {
					initNextSecondId(nowMs);
				}
			} catch (InterruptedException e) { throw new IdSourceException("Unexpected Interruption",e); }
		}
	}

	private void initNextSecondId( long nowMs ) {
		currentId           = 0;
		currentTimeStampSec = nowMs / 1000;
		resetTimeMs         = nowMs;
		totalPastTimeId     = 0;
	}

	private void processShiftToPastTime( long currentTimeMs ) {
		
		double delay = (resetTimeMs-currentTimeMs);
		long maxPredictedId = Math.abs(Math.round(delay * pastTimeSlowDown));
		
		if ( totalPastTimeId >= maxPredictedId ) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) { throw new IdSourceException("Unexpected Interruption",e); }
		}
		
		if( currentId >= maxId ) {
			currentId       = 0;
			resetTimeMs     = currentTimeMs;
			totalPastTimeId = 0;
			currentTimeStampSec++; // add one second to current timestamp
		} else {
			totalPastTimeId++;
		}
		
	}

	public void setTimeFormatter(TimeFormatter tf) {
		this.tf = tf;
	}
	
	public static TimeBasedIdGenerator idGenerator_10x4x5( int hostId, long startTimeStampSec ) {
		TimeBasedIdGenerator idGen = new TimeBasedIdGenerator(hostId, startTimeStampSec , 99999, 9999);
		idGen.setTimeFormatter( new TimeFormatter() {
			@Override
			public long formatTimeId(long currentTimeSec) {
				return currentTimeSec;
			}});
		return idGen;
	}

	public static TimeBasedIdGenerator idGenerator_10x4x5( int hostId ) {
		return idGenerator_10x4x5( hostId, System.currentTimeMillis()/1000);
	}
	
	public static SplittedId split_10x4x5( final long id ) {
		
		long timestamp;
		long  datacenterId;
		long  hostId;
		long currentId;
		
		timestamp      = id / 1000000000;
		long dcHostId  = id - timestamp * 1000000000;
		long dcHost    = dcHostId / 100000;
		currentId      = dcHostId - dcHost * 100000;
		datacenterId   = dcHost / 1000;
		hostId         = dcHost - datacenterId * 1000;
		SplittedId sid = new SplittedId(timestamp, (int)datacenterId, (int)hostId, currentId);
		return sid;
	}

	public void setTimestampSupplier(LongSupplier timestamp) {
		this.timestamp = timestamp;
	}
	
}
