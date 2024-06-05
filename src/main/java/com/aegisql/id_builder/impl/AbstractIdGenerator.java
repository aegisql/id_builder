package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdSource;
import com.aegisql.id_builder.TimeTransformer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntUnaryOperator;
import java.util.function.LongSupplier;

import static com.aegisql.id_builder.TimeTransformer.identity;
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
	/**
	 * The Id state ref.
	 */
	final AtomicReference<IdState> idStateRef = new AtomicReference<>();

	/**
	 * The Sleep after.
	 */
	protected long sleepAfter;
	/**
	 * The Tf.
	 */
	protected TimeTransformer tf = identity;
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
		assertPositive(hostPositions,"Number of host ID positions must be >= 1");
		assertPositive(idPositions,"Number of ID positions must be >= 1");
		this.idCeil = idCeilFunction.applyAsInt(idPositions);
		this.hostIdCeil = idCeilFunction.applyAsInt(hostPositions);
		this.maxId = this.idCeil - 1;
		this.maxHostId = this.hostIdCeil - 1;
		this.maxIdPerMSec = this.idCeil / 1000;
		this.idStateRef.set(new IdState(0, 0, startTimeStampSec));
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
	public long getId() {
		while (true) {
			IdState expectedState = idStateRef.get();
			IdState newState = nextState(expectedState);
			if( idStateRef.compareAndSet(expectedState,newState)) {
				return buildId(newState);
            }
		}
	}

	/**
	 * Build id long.
	 *
	 * @param newState the new state
	 * @return the long
	 */
	abstract long buildId(IdState newState);

	/**
	 * Next state id state.
	 *
	 * @param current the current
	 * @return the id state
	 */
	abstract IdState nextState(IdState current);

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
		return idStateRef.get().globalCounter();
	}

}
