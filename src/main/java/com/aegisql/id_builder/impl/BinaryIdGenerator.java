package com.aegisql.id_builder.impl;

import com.aegisql.id_builder.IdParts;
import com.aegisql.id_builder.IdSourceException;
import com.aegisql.id_builder.TimeTransformer;
import com.aegisql.id_builder.utils.Utils;

import static com.aegisql.id_builder.utils.Utils.*;

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
	 * @param startTimeStampSec  the start time stamp sec
	 * @param timestampExtraBits the timestamp extra bits
	 * @param hostId             the host id
	 * @param hostIdBits         the host id pos
	 */
	public BinaryIdGenerator(long startTimeStampSec, short timestampExtraBits, int hostId, int hostIdBits) {
        super(Utils::pow2,hostIdBits,64 - 32 - timestampExtraBits - hostIdBits,startTimeStampSec);
		assertNotNegative(timestampExtraBits,"timestampExtraBits extra bits must be a small number or 0");
		if (hostId > maxHostId) {
			throw new IdSourceException("Host ID > " + maxHostId);
		}
		this.timestampShift = (short) (32 - timestampExtraBits);
		this.idShift = (short) hostIdBits;
		this.hostId = hostId;
		this.tf = TimeTransformer.adjustedEpoch;
	}

	public BinaryIdGenerator() {
		this(unixTimestamp());
	}

	public BinaryIdGenerator(long startTimeStampSec) {
		this(startTimeStampSec, (short) 0,0,0);
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
	public IdParts parse(long id) {
		long timestamp = id >>> timestampShift;
		long timestampMask = Utils.setLowerBits(timestampShift);
		long removedTimestamp = id & timestampMask;
		long currentId = removedTimestamp >>> idShift;
		long idMask = Utils.setLowerBits(idShift);
		long removedTimestampAndId = id & idMask;
		int hostId = (int) removedTimestampAndId;
		return new IdParts(timestamp,hostId,currentId);
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
