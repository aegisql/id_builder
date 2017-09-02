package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdSourceException;

import java.util.function.LongSupplier;

import com.aegisql.id_builder.IdSource;
import com.aegisql.id_builder.IdParts;
import com.aegisql.id_builder.TimeTransformer;

public class TimeHostIdGenerator implements IdSource {

	protected final int maxId;
	protected final int maxHostId;
	protected final long hostId;
	protected final long timeIdBase;

	protected long globalCounter       = 0;

	protected long currentId           = 0;
	protected long currentTimeStampSec = 0;
	protected final long maxIdPerMSec;

	private long sleepAfter;

	private TimeTransformer tf;

	private LongSupplier timestamp = System::currentTimeMillis;

	private TimeHostIdGenerator(int hostId, long startTimeStampSec, int maxId, int maxHostId) {
		this.maxId               = maxId;
		this.maxHostId           = maxHostId;
		this.currentTimeStampSec = startTimeStampSec;

		if (hostId > maxHostId) {
			throw new IdSourceException("Max host ID > " + maxHostId);
		}

		this.hostId       = hostId * (maxId + 1);
		this.timeIdBase   = (maxHostId + 1) * (maxId + 1);
		this.maxIdPerMSec = (maxId + 1) / 1000;
		this.setPastShiftSlowDown(1.2);
	}

	public void setPastShiftSlowDown(double x) {
		this.sleepAfter = Math.round((maxId+1) / 1000 / x);
	}

	@Override
	public final synchronized long getId() {
		long nowMs = timestamp.getAsLong();
		long now   = nowMs / 1000;
		long dt    = nowMs - (now * 1000);

		globalCounter++;

		if (now > currentTimeStampSec)
			initNextSecondId(nowMs);
		else if (now == currentTimeStampSec)
			currentSecondNextId(dt);
		else
			processShiftToPastTime(now, nowMs, dt);

		long returnId = currentId++;

		if (returnId > maxId)
			throw new IdSourceException("Internal ID reached ultimate value: " + returnId);

		return tf.transformTimestamp(currentTimeStampSec) * timeIdBase + hostId + returnId;
	}

	private void sleepOneMSec() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			throw new IdSourceException("Unexpected Interruption", e);
		}
	}

	private void currentSecondNextId(long dt) {
		long maxPredictedId = Math.min(maxId, dt * maxIdPerMSec);
		if (currentId >= maxPredictedId) {
			sleepOneMSec();
			long nowMs = timestamp.getAsLong();
			long now = nowMs / 1000;
			if (now > currentTimeStampSec) {
				initNextSecondId(nowMs);
			}
		}
	}

	private void initNextSecondId(long nowMs) {
		currentId = 0;
		currentTimeStampSec = nowMs / 1000;
	}

	private void processShiftToPastTime(long currentTime, long currentTimeMs, long dt) {
		if (globalCounter % sleepAfter == 0) {
			sleepOneMSec();
		}
		if (currentId >= maxId) {
			currentId = 0;
			currentTimeStampSec++; // add one second to current timestamp
		}
	}

	public void setTimeTransformer(TimeTransformer tf) {
		this.tf = tf;
	}

	public static TimeHostIdGenerator idGenerator_10x4x5(int hostId, long startTimeStampSec) {
		TimeHostIdGenerator idGen = new TimeHostIdGenerator(hostId, startTimeStampSec, 99999, 9999);
		idGen.setTimeTransformer(time -> time);
		return idGen;
	}
	
	public static TimeHostIdGenerator idGenerator_10x4x5(int hostId) {
		return idGenerator_10x4x5(hostId, System.currentTimeMillis() / 1000);
	}

	public static TimeHostIdGenerator idGenerator_10x8(long startTimeStampSec) {
		TimeHostIdGenerator idGen = new TimeHostIdGenerator(0, startTimeStampSec, 99999999, 9);
		idGen.setTimeTransformer(time -> time);
		return idGen;
	}

	public static TimeHostIdGenerator idGenerator_10x8() {
		TimeHostIdGenerator idGen = new TimeHostIdGenerator(0, System.currentTimeMillis()/1000, 99999999, 9);
		idGen.setTimeTransformer(time -> time);
		return idGen;
	}
	
	public static IdParts split_10x4x5(final long id) {

		long timestamp;
		long datacenterId;
		long hostId;
		long currentId;

		timestamp = id / 1000000000;
		long dcHostId = id - timestamp * 1000000000;
		long dcHost = dcHostId / 100000;
		currentId = dcHostId - dcHost * 100000;
		datacenterId = dcHost / 1000;
		hostId = dcHost - datacenterId * 1000;
		IdParts idp = new IdParts(timestamp, (int) datacenterId, (int) hostId, currentId);
		return idp;
	}

	public static IdParts split_10x8(final long id) {
		long timestamp;
		long currentId;
		timestamp = id / 1000000000L;
		currentId = id - timestamp * 1000000000L;
		IdParts idp = new IdParts(timestamp, (int) 0, 0, currentId);
		return idp;
	}

	
	public void setTimestampSupplier(LongSupplier timestamp) {
		this.timestamp = timestamp;
	}

	public long getGlobalCounter() {
		return globalCounter;
	}

}
