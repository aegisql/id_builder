package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdSourceException;

import com.aegisql.id_builder.utils.Utils;

import static com.aegisql.id_builder.TimeTransformer.identity;
import static com.aegisql.id_builder.utils.Utils.*;

/**
 * The type Time host id generator.
 */
public final class DecimalIdGenerator extends AbstractIdGenerator {

	private final long hostId;
	private final long hostIdBase;
	private final long timeIdBase;

	/**
	 * Instantiates a new Time host id generator.
	 *
	 * @param hostId            the host id
	 * @param startTimeStampSec the start time stamp sec
	 * @param idPos             the id pos
	 * @param hostIdPos         the host id pos
	 */
	public DecimalIdGenerator(int hostId, long startTimeStampSec, int idPos, int hostIdPos) {
        super(Utils::pow10,hostIdPos,idPos,startTimeStampSec);
		if (hostId > maxHostId) {
			throw new IdSourceException("Host ID > " + maxHostId);
		}
		this.hostId = hostId;
		this.hostIdBase = (long) hostId * this.idCeil;
		this.timeIdBase   = this.hostIdCeil * this.idCeil;
		this.tf = identity;
	}

	/**
	 * Instantiates a new Time host id generator.
	 *
	 * @param hostId    the host id
	 * @param idPos     the id pos
	 * @param hostIdPos the host id pos
	 */
	public DecimalIdGenerator(int hostId, int idPos, int hostIdPos) {
		this(hostId,System.currentTimeMillis()/1000,idPos,hostIdPos);
	}

	IdState nextState(IdState current) {
		long nowMs = timestamp.getAsLong();
		long now   = nowMs / 1000;
		long dt    = nowMs - (now * 1000);
		long nextCounter = current.globalCounter()+1;
		if(now > current.currentTimeStampSec()) {
			return new IdState(nextCounter, 0, now);
		} else if(now == current.currentTimeStampSec()) {
			long maxPredictedId = Math.min(maxId, dt * maxIdPerMSec);
			if (current.currentId() >= maxPredictedId) {
				sleepOneMSec();
				return nextState(current);
			} else {
				return new IdState(nextCounter, current.currentId() + 1, now);
			}
		} else {
			Utils.sleepOneMSec(nextCounter,sleepAfter);
			if (current.currentId() >= maxId) {
				return new IdState(nextCounter, 0, current.currentTimeStampSec() + 1);
			} else {
				return new IdState(nextCounter, current.currentId() + 1, current.currentTimeStampSec());
			}
		}
	}

	long buildId(IdState idState) {
		assert idState.currentId() <= maxId : "current ID exceeded max id";
		return tf.transformTimestamp(idState.currentTimeStampSec()) * timeIdBase + hostIdBase + idState.currentId();
	}

    /**
	 * Id generator 10 x 4 x 5 time host id generator.
	 *
	 * @param hostId            the host id
	 * @param startTimeStampSec the start time stamp sec
	 * @return the time host id generator
	 */
	public static DecimalIdGenerator idGenerator_10x4x5(int hostId, long startTimeStampSec) {
		return new DecimalIdGenerator(hostId, startTimeStampSec, 5, 4);
	}

	/**
	 * Id generator 10 x 4 x 5 time host id generator.
	 *
	 * @param hostId the host id
	 * @return the time host id generator
	 */
	public static DecimalIdGenerator idGenerator_10x4x5(int hostId) {
		return new DecimalIdGenerator(hostId, 5, 4);
	}

	/**
	 * Id generator 10 x 8 time host id generator.
	 *
	 * @param startTimeStampSec the start time stamp sec
	 * @return the time host id generator
	 */
	public static DecimalIdGenerator idGenerator_10x8(long startTimeStampSec) {
		return new DecimalIdGenerator(0, startTimeStampSec, 8, 1);
	}

	/**
	 * Id generator 10 x 8 time host id generator.
	 *
	 * @return the time host id generator
	 */
	public static DecimalIdGenerator idGenerator_10x8() {
		return new DecimalIdGenerator(0, 8, 1);
	}

	@Override
	public String toString() {
        return "DecimalIdGenerator{" +
				"hostId=" + hostId +
				", maxHostId=" + maxHostId +
				", maxId=" + maxId +
                ", maxIdsPerMSec=" + maxIdPerMSec +
                ", pastShiftSlowTimeAfter=" + sleepAfter +
                '}';
	}
}
