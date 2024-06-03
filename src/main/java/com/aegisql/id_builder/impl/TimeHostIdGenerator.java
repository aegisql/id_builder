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
		/**
		 * The global counter.
		 */
		final long globalCounter;
		/**
		 * The current id.
		 */
		final long currentId;
		/**
		 * The current time stamp sec.
		 */
		final long currentTimeStampSec;

		/**
		 * Instantiates a new Id state.
		 *
		 * @param globalCounter       the global counter
		 * @param currentId           the current id
		 * @param currentTimeStampSec the current time stamp sec
		 */
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

	/**
	 * The max id.
	 */
	protected final int maxId;


	/**
	 * The host id.
	 */
	protected final long hostId;

	/**
	 * The time id base.
	 */
	protected final long timeIdBase;

	/**
	 * The max id per M sec.
	 */
	protected final long maxIdPerMSec;

	/** The sleep after. */
	private long sleepAfter;

	/** The tf. */
	private TimeTransformer tf = identity;

	/** The timestamp. */
	private LongSupplier timestamp = System::currentTimeMillis;

	private final AtomicReference<IdState> idStateRef = new AtomicReference<>();

	/**
	 * Instantiates a new time host id generator.
	 *
	 * @param hostId the host id
	 * @param startTimeStampSec the start time stamp sec
	 * @param idPos the max id
	 * @param hostIdPos the max host id
	 */
	public TimeHostIdGenerator(int hostId, long startTimeStampSec, int idPos, int hostIdPos) {
		int maxIdValue = (int) Math.round(Math.pow(10, idPos));
		int maxHostId  = (int) Math.round(Math.pow(10,hostIdPos));
		this.maxId     = maxIdValue-1;

		if (hostId > maxHostId) {
			throw new IdSourceException("Host ID > " + maxHostId);
		}

		this.hostId       = hostId * (maxIdValue);
		this.timeIdBase   = maxHostId * maxIdValue;
		this.maxIdPerMSec = maxIdValue / 1000;
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
		assert idState.currentId <= maxId : "current ID exceeded max id";
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
	 * @param hostId            the host id
	 * @param startTimeStampSec the start time stamp sec
	 * @return the time host id generator
	 */
	public static TimeHostIdGenerator idGenerator_10x4x5(int hostId, long startTimeStampSec) {
		return new TimeHostIdGenerator(hostId, startTimeStampSec, 5, 4);
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
		return new TimeHostIdGenerator(0, startTimeStampSec, 8, 1);
	}

	/**
	 * Id generator 10 x 8.
	 *
	 * @return the time host id generator
	 */
	public static TimeHostIdGenerator idGenerator_10x8() {
		return idGenerator_10x8(System.currentTimeMillis()/1000);
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
