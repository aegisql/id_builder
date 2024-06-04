package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdSourceException;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

import com.aegisql.id_builder.IdSource;
import com.aegisql.id_builder.TimeTransformer;

import static com.aegisql.id_builder.TimeTransformer.identity;

/**
 * The type Time host id generator.
 */
public final class TimeHostIdGenerator implements IdSource {

	private record IdState(long globalCounter, long currentId, long currentTimeStampSec) {
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
			@Override
			public boolean equals(Object o) {
				IdState idState = (IdState) o;
				return globalCounter == idState.globalCounter;
			}
		}

	private final int maxId;
	private final long hostId;
	private final long timeIdBase;
	private final long maxIdPerMSec;
	private long sleepAfter;
	private TimeTransformer tf = identity;
	private LongSupplier timestamp = System::currentTimeMillis;
	private final AtomicReference<IdState> idStateRef = new AtomicReference<>();

	private static int pow10(int x) {
		return (int) Math.round(Math.pow(10, x));
	}

	private static void sleepOneMSec() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			throw new IdSourceException("Unexpected Interruption", e);
		}
	}

	/**
	 * Instantiates a new Time host id generator.
	 *
	 * @param hostId            the host id
	 * @param startTimeStampSec the start time stamp sec
	 * @param idPos             the id pos
	 * @param hostIdPos         the host id pos
	 */
	public TimeHostIdGenerator(int hostId, long startTimeStampSec, int idPos, int hostIdPos) {
		int maxIdValue = pow10(idPos);
		int maxHostId  = pow10(hostIdPos);
		this.maxId     = maxIdValue-1;

		if (hostId > maxHostId) {
			throw new IdSourceException("Host ID > " + maxHostId);
		}

		this.hostId       = (long) hostId * maxIdValue;
		this.timeIdBase   = (long) maxHostId * maxIdValue;
		this.maxIdPerMSec = maxIdValue / 1000;
		this.setPastShiftSlowDown(1.2);
		this.idStateRef.set(new IdState(0, 0, startTimeStampSec));
	}

	/**
	 * Instantiates a new Time host id generator.
	 *
	 * @param hostId    the host id
	 * @param idPos     the id pos
	 * @param hostIdPos the host id pos
	 */
	public TimeHostIdGenerator(int hostId, int idPos, int hostIdPos) {
		this(hostId,System.currentTimeMillis()/1000,idPos,hostIdPos);
	}

	/**
	 * Sets past shift slow down.
	 *
	 * @param x the x
	 */
	public void setPastShiftSlowDown(double x) {
		this.sleepAfter = Math.round((double) (maxId + 1) / 1000 / x);
	}

	/* (non-Javadoc)
	 * @see com.aegisql.id_builder.IdSource#getId()
	 */
	@Override
	public long getId() {
		while (true) {
			IdState expectedState = idStateRef.get();
			IdState newState = nextState(expectedState);
			if( idStateRef.compareAndSet(expectedState,newState)) {
				return buildId(newState);
            }
		}
	}

	private IdState nextState(IdState current) {
		long nowMs = timestamp.getAsLong();
		long now   = nowMs / 1000;
		long dt    = nowMs - (now * 1000);
		long nextCounter = current.globalCounter+1;
		if(now > current.currentTimeStampSec) {
			return new IdState(nextCounter, 0, now);
		} else if(now == current.currentTimeStampSec) {
			long maxPredictedId = Math.min(maxId, dt * maxIdPerMSec);
			if (current.currentId >= maxPredictedId) {
				sleepOneMSec();
				return nextState(current);
			} else {
				return new IdState(nextCounter, current.currentId + 1, now);
			}
		} else {
			if (nextCounter % sleepAfter == 0) {
				sleepOneMSec();
			}
			if (current.currentId >= maxId) {
				return new IdState(nextCounter, 0, current.currentTimeStampSec + 1);
			} else {
				return new IdState(nextCounter, current.currentId + 1, current.currentTimeStampSec);
			}
		}
	}

	private long buildId(IdState idState) {
		assert idState.currentId <= maxId : "current ID exceeded max id";
		return tf.transformTimestamp(idState.currentTimeStampSec) * timeIdBase + hostId + idState.currentId;
	}

	/**
	 * Sets time transformer.
	 *
	 * @param tf the tf
	 */
	public void setTimeTransformer(TimeTransformer tf) {
		this.tf = tf;
	}

	/**
	 * Sets timestamp supplier.
	 *
	 * @param timestamp the timestamp
	 */
	public void setTimestampSupplier(LongSupplier timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets global counter.
	 *
	 * @return the global counter
	 */
	public long getGlobalCounter() {
		return idStateRef.get().globalCounter;
	}

	/**
	 * Id generator 10 x 4 x 5 time host id generator.
	 *
	 * @param hostId            the host id
	 * @param startTimeStampSec the start time stamp sec
	 * @return the time host id generator
	 */
	public static TimeHostIdGenerator idGenerator_10x4x5(int hostId, long startTimeStampSec) {
		return new TimeHostIdGenerator(hostId, startTimeStampSec, 5, 4);
	}

	/**
	 * Id generator 10 x 4 x 5 time host id generator.
	 *
	 * @param hostId the host id
	 * @return the time host id generator
	 */
	public static TimeHostIdGenerator idGenerator_10x4x5(int hostId) {
		return new TimeHostIdGenerator(hostId, 5, 4);
	}

	/**
	 * Id generator 10 x 8 time host id generator.
	 *
	 * @param startTimeStampSec the start time stamp sec
	 * @return the time host id generator
	 */
	public static TimeHostIdGenerator idGenerator_10x8(long startTimeStampSec) {
		return new TimeHostIdGenerator(0, startTimeStampSec, 8, 1);
	}

	/**
	 * Id generator 10 x 8 time host id generator.
	 *
	 * @return the time host id generator
	 */
	public static TimeHostIdGenerator idGenerator_10x8() {
		return new TimeHostIdGenerator(0, 8, 1);
	}

}
