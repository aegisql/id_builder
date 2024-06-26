package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdParts;
import com.aegisql.id_builder.IdSourceException;

import com.aegisql.id_builder.TimeTransformer;
import com.aegisql.id_builder.utils.Utils;
import static com.aegisql.id_builder.TimeTransformer.identity;
import static com.aegisql.id_builder.utils.Utils.unixTimestamp;

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
        super(Utils::pow10Sticky,hostIdPos,idPos,startTimeStampSec);
		if (hostId > maxHostId) {
			throw new IdSourceException("Host ID > " + maxHostId);
		}
		this.hostId = hostId;
		this.hostIdBase = (long) hostId * this.idCeil;
		this.timeIdBase   = this.hostIdCeil * this.idCeil;
		this.tf = identity;
	}

	/**
	 * Instantiates a new Decimal id generator.
	 *
	 * @param startTimeStampSec the start time stamp sec
	 */
	public DecimalIdGenerator(long startTimeStampSec) {
		this(0,startTimeStampSec,9,0 );
	}

	/**
	 * Instantiates a new Decimal id generator.
	 */
	public DecimalIdGenerator() {
		this(unixTimestamp());
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

	long buildId() {
		assert currentId <= maxId : "current ID exceeded max id";
		return tf.transformTimestamp(currentTimeStampSec) * timeIdBase + hostIdBase + currentId;
	}

	private final static long _10XX19 = 1000000000000000000L;
	@Override
	public IdParts parse(long id) {
		long adjustTimestamp = 0;
		if(id < _10XX19) {
			adjustTimestamp = _10XX19/(idCeil*hostIdCeil);
			id = _10XX19 +id;
		}
		long timestamp = id / (idCeil*hostIdCeil);
		long dcHostId = id - timestamp * (idCeil*hostIdCeil);
		long dcHost = dcHostId / idCeil;
		long currentId = dcHostId - dcHost * idCeil;
		if(dcHost >= maxHostId) {
			dcHost = -1;
		}
		TimeTransformer tt;
		if(this.tf == TimeTransformer.adjustedEpoch) {
			tt = TimeTransformer.restoredEpoch;
		} else {
			tt = TimeTransformer.identity;
		}
		return new IdParts(tt.transformTimestamp(timestamp-adjustTimestamp), (int) dcHost, currentId);
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
	 * @param hostId            the host id
	 * @param startTimeStampSec the start time stamp sec
	 * @return the time host id generator
	 */
	public static DecimalIdGenerator idGenerator_10x8(int hostId, long startTimeStampSec) {
		return new DecimalIdGenerator(hostId, startTimeStampSec, 8, 1);
	}

	/**
	 * Id generator 10 x 8 time host id generator.
	 *
	 * @param hostId the host id
	 * @return the time host id generator
	 */
	public static DecimalIdGenerator idGenerator_10x8(int hostId) {
		return new DecimalIdGenerator(hostId, 8, 1);
	}

	/**
	 * Id generator 10 x 8 decimal id generator.
	 *
	 * @return the decimal id generator
	 */
	public static DecimalIdGenerator idGenerator_10x8() {
		return idGenerator_10x8(0);
	}

	public static DecimalIdGenerator fromLastKnownId(long lastKnownId, int idPos, int hostIdPos) {
		var tmpIdGenerator = new DecimalIdGenerator(0, unixTimestamp(), hostIdPos, idPos);
		var parts = tmpIdGenerator.parse(lastKnownId);
		var idGenerator = new DecimalIdGenerator(parts.hostId(),parts.timestamp(),hostIdPos,idPos);
		idGenerator.currentId = parts.currentId();
		return idGenerator;
	}

	@Override
	public String toString() {
        return "DecimalIdGenerator{" +
				"hostId=" + (hostId < maxHostId ? hostId: "N/A") +
				", maxHostId=" + maxHostId +
				", maxId=" + maxId +
                ", maxIdsPerMSec=" + maxIdPerMSec +
                ", pastShiftSlowTimeAfter=" + sleepAfter +
                '}';
	}
}
