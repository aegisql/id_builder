package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdSourceException;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

import com.aegisql.id_builder.IdSource;
import com.aegisql.id_builder.IdParts;
import com.aegisql.id_builder.TimeTransformer;

import static com.aegisql.id_builder.TimeTransformer.identity;

// TODO: Auto-generated Javadoc
/**
 * The Class TimeHostIdGenerator.
 */
public class TimeHostIdGenerator implements IdSource {

	private class IdState {
		/** The global counter. */
		final long globalCounter;
		/** The current id. */
		final long currentId;
		/** The current time stamp sec. */
		final long currentTimeStampSec;

		public IdState(long globalCounter, long currentId, long currentTimeStampSec) {
            this.globalCounter = globalCounter;
            this.currentId = currentId;
            this.currentTimeStampSec = currentTimeStampSec;
        }

		@Override
		public boolean equals(Object o) {
			IdState idState = (IdState) o;
			return globalCounter == idState.globalCounter;
		}
	}

	/** The max id. */
	protected final int maxId;
	
	/** The max host id. */
	protected final int maxHostId;
	
	/** The host id. */
	protected final long hostId;
	
	/** The time id base. */
	protected final long timeIdBase;

	/** The max id per M sec. */
	protected final long maxIdPerMSec;

	/** The sleep after. */
	private long sleepAfter;

	/** The tf. */
	private TimeTransformer tf;

	/** The timestamp. */
	private LongSupplier timestamp = System::currentTimeMillis;

	private final AtomicReference<IdState> idStateRef = new AtomicReference<>();

	/**
	 * Instantiates a new time host id generator.
	 *
	 * @param hostId the host id
	 * @param startTimeStampSec the start time stamp sec
	 * @param maxId the max id
	 * @param maxHostId the max host id
	 */
	private TimeHostIdGenerator(int hostId, long startTimeStampSec, int maxId, int maxHostId) {
		this.maxId               = maxId;
		this.maxHostId           = maxHostId;

		if (hostId > maxHostId) {
			throw new IdSourceException("Host ID > " + maxHostId);
		}

		this.hostId       = hostId * (maxId + 1);
		this.timeIdBase   = (maxHostId + 1) * (maxId + 1);
		this.maxIdPerMSec = (maxId + 1) / 1000;
		this.setPastShiftSlowDown(1.2);
		this.idStateRef.set(new IdState(0,0,startTimeStampSec));
	}

	/**
	 * Sets the past shift slow down ratio.
	 * Default 1.2 which creates approximately 20% slow down
	 *
	 * @param x the new past shift slow down
	 */
	public void setPastShiftSlowDown(double x) {
		this.sleepAfter = Math.round((maxId+1) / 1000 / x);
	}

	/* (non-Javadoc)
	 * @see com.aegisql.id_builder.IdSource#getId()
	 */
	@Override
	public final long getId() {
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
			return new IdState(nextCounter,0,now);
		} else if(now == current.currentTimeStampSec) {
			long maxPredictedId = Math.min(maxId, dt * maxIdPerMSec);
			if (current.currentId >= maxPredictedId) {
				sleepOneMSec();
				return nextState(current);
			} else {
				return new IdState(nextCounter,current.currentId+1,now);
			}
		} else {
			if (nextCounter % sleepAfter == 0) {
				sleepOneMSec();
			}
			if (current.currentId >= maxId) {
				return new IdState(nextCounter,0,current.currentTimeStampSec+1);
			} else {
				return new IdState(nextCounter,current.currentId+1,current.currentTimeStampSec);
			}
		}
	}

	private long buildId(IdState idState) {
		return tf.transformTimestamp(idState.currentTimeStampSec) * timeIdBase + hostId + idState.currentId;
	}

	/**
	 * Sleep one M sec.
	 */
	private static void sleepOneMSec() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			throw new IdSourceException("Unexpected Interruption", e);
		}
	}

	/**
	 * Sets the time transformer.
	 *
	 * @param tf the new time transformer
	 */
	public void setTimeTransformer(TimeTransformer tf) {
		this.tf = tf;
	}

	/**
	 * Id generator 10 x 4 x 5.
	 *
	 * @param hostId the host id
	 * @param startTimeStampSec the start time stamp sec
	 * @return the time host id generator
	 */
	public static TimeHostIdGenerator idGenerator_10x4x5(int hostId, long startTimeStampSec) {
		TimeHostIdGenerator idGen = new TimeHostIdGenerator(hostId, startTimeStampSec, 99999, 9999);
		idGen.setTimeTransformer(identity);
		return idGen;
	}
	
	/**
	 * Id generator 10 x 4 x 5.
	 *
	 * @param hostId the host id
	 * @return the time host id generator
	 */
	public static TimeHostIdGenerator idGenerator_10x4x5(int hostId) {
		return idGenerator_10x4x5(hostId, System.currentTimeMillis() / 1000);
	}

	/**
	 * Id generator 10 x 8.
	 *
	 * @param startTimeStampSec the start time stamp sec
	 * @return the time host id generator
	 */
	public static TimeHostIdGenerator idGenerator_10x8(long startTimeStampSec) {
		TimeHostIdGenerator idGen = new TimeHostIdGenerator(0, startTimeStampSec, 99999999, 9);
		idGen.setTimeTransformer(identity);
		return idGen;
	}

	/**
	 * Id generator 10 x 8.
	 *
	 * @return the time host id generator
	 */
	public static TimeHostIdGenerator idGenerator_10x8() {
		TimeHostIdGenerator idGen = new TimeHostIdGenerator(0, System.currentTimeMillis()/1000, 99999999, 9);
		idGen.setTimeTransformer(identity);
		return idGen;
	}

	
	/**
	 * Sets the timestamp supplier.
	 *
	 * @param timestamp the new timestamp supplier
	 */
	public void setTimestampSupplier(LongSupplier timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the global counter.
	 *
	 * @return the global counter
	 */
	public long getGlobalCounter() {
		return idStateRef.get().globalCounter;
	}

}
