package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdParts;
import com.aegisql.id_builder.IdSource;
import com.aegisql.id_builder.TimeTransformer;
import com.aegisql.id_builder.utils.Utils;

import java.util.Objects;
import java.util.function.IntUnaryOperator;
import java.util.function.LongSupplier;

import static com.aegisql.id_builder.utils.Utils.*;

/**
 * The type Time host id generator.
 */
public abstract class AbstractIdGenerator implements IdSource {

	/**
	 * The Id ceil.
	 */
	protected final int idCeil;
	/**
	 * The Host id ceil.
	 */
	protected final long hostIdCeil;
	/**
	 * The Max id.
	 */
	protected final int maxId;
	/**
	 * The Max host id.
	 */
	protected final long maxHostId;
	/**
	 * The Max id per m sec.
	 */
	protected final long maxIdPerMSec;

	private long globalCounter;
	/**
	 * The Current id.
	 */
	protected long currentId;
	/**
	 * The Current time stamp sec.
	 */
	protected long currentTimeStampSec;

	/**
	 * The Sleep after.
	 */
	protected long sleepAfter;
	/**
	 * The Tf.
	 */
	protected TimeTransformer tf;
	/**
	 * The Timestamp.
	 */
	protected LongSupplier timestamp = System::currentTimeMillis;

	/**
	 * Instantiates a new Time host id generator.
	 *
	 * @param idCeilFunction    the id ceil function
	 * @param hostPositions     the host positions
	 * @param idPositions       the id positions
	 * @param startTimeStampSec the start time stamp sec
	 */
	protected AbstractIdGenerator(IntUnaryOperator idCeilFunction, int hostPositions, int idPositions, long startTimeStampSec) {
		Objects.requireNonNull(idCeilFunction,"Expected ID Ceil function");
		assertNotNegative(hostPositions,"Number of host ID positions must be >= 0");
		assertPositive(idPositions,"Number of ID positions must be >= 1");
		this.idCeil = idCeilFunction.applyAsInt(idPositions);
		this.hostIdCeil = idCeilFunction.applyAsInt(hostPositions);
		this.maxId = this.idCeil - 1;
		this.maxHostId = this.hostIdCeil - 1;
		this.maxIdPerMSec = this.idCeil / 1000;
		this.globalCounter = 0;
		this.currentId = 0;
		this.currentTimeStampSec = startTimeStampSec;
		setPastShiftSlowDown(1.2);
	}

	/**
	 * Sets past shift slow down.
	 *
	 * @param x the x
	 */
	public void setPastShiftSlowDown(double x) {
		this.sleepAfter = Math.round((double) idCeil / 1000 / x);
	}

	/* (non-Javadoc)
	 * @see com.aegisql.id_builder.IdSource#getId()
	 */
	@Override
	public synchronized long getId() {
		long nowMs = timestamp.getAsLong();
		long now   = nowMs / 1000;
		long dt    = nowMs - (now * 1000);
		long nextCounter = globalCounter+1;
		if(now > currentTimeStampSec) {
			globalCounter = nextCounter;
			currentId = 0;
			currentTimeStampSec = now;
		} else if(now == currentTimeStampSec) {
			long maxPredictedId = Math.min(maxId, dt * maxIdPerMSec);
			if (currentId >= maxPredictedId) {
				sleepOneMSec();
				return getId();
			} else {
				globalCounter = nextCounter;
				currentId = currentId+1;
			}
		} else {
			Utils.sleepOneMSec(nextCounter,sleepAfter);
			if (currentId >= maxId) {
				globalCounter = nextCounter;
				currentId = 0;
				currentTimeStampSec = currentTimeStampSec+1;
			} else {
				globalCounter = nextCounter;
				currentId = currentId+1;
			}
		}

		return buildId();
	}

	/**
	 * Build id long.
	 *
	 * @return the long
	 */
	abstract long buildId();

	/**
	 * Parse id parts.
	 *
	 * @param id the id
	 * @return the id parts
	 */
	public abstract IdParts parse(long id);

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
		return globalCounter;
	}

}
