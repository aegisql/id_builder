package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdSourceException;
import com.aegisql.id_builder.TimeTransformer;
import com.aegisql.id_builder.utils.Utils;

/**
 * The type Time host id generator.
 */
public final class BinaryIdGenerator extends AbstractIdGenerator {

	private final long hostId;
	private final short timestampShift;
	private final short idShift;

	/**
	 * Instantiates a new Time host id generator.
	 *
	 * @param hostId            the host id
	 * @param startTimeStampSec the start time stamp sec
	 * @param hostIdBits         the host id pos
	 */
	public BinaryIdGenerator(long startTimeStampSec, short timestampExtraBits, int hostId, int hostIdBits) {
        super(Utils::pow2,hostIdBits,64 - 32 - timestampExtraBits - hostIdBits,startTimeStampSec);
		if (hostId > maxHostId) {
			throw new IdSourceException("Host ID > " + maxHostId);
		}
		this.timestampShift = (short) (32 - timestampExtraBits);
		this.idShift = (short) hostIdBits;
		this.hostId = hostId;
		this.tf = TimeTransformer.adjustedEpoch;
	}

	long buildId(IdState idState) {
		assert idState.currentId() <= maxId : "current ID exceeded max id";
		long time = tf.transformTimestamp(idState.currentTimeStampSec());
		long shiftedTime = time << timestampShift;
		long shiftedId = idState.currentId() << idShift;
		long id =  shiftedTime | shiftedId | this.hostId;
		assert id > 0 : "ID sign bit is set.";
		return id;
	}


	@Override
	public String toString() {
        return "BinaryIdGenerator{" +
				"hostId=" + hostId +
				", maxHostId=" + maxHostId +
				", maxId=" + maxId +
                ", maxIdsPerMSec=" + maxIdPerMSec +
                ", pastShiftSlowTimeAfter=" + sleepAfter +
                '}';
	}
}
