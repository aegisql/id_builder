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
        super(Utils::pow2Sticky,hostIdBits,64 - 32 - timestampExtraBits - hostIdBits,startTimeStampSec);
		assertNotNegative(timestampExtraBits,"timestampExtraBits extra bits must be a small number or 0");
		if (hostId > maxHostId) {
			throw new IdSourceException("Host ID > " + maxHostId);
		}
		this.timestampShift = (short) (32 - timestampExtraBits);
		this.idShift = (short) hostIdBits;
		this.hostId = hostId;
		this.tf = TimeTransformer.adjustedEpoch;
	}

	/**
	 * Instantiates a new Binary id generator.
	 */
	public BinaryIdGenerator() {
		this(unixTimestamp());
	}

	/**
	 * Instantiates a new Binary id generator.
	 *
	 * @param startTimeStampSec the start time stamp sec
	 */
	public BinaryIdGenerator(long startTimeStampSec) {
		this(startTimeStampSec, (short) 0,0,0);
	}

	long buildId() {
		assert currentId <= maxId : "current ID exceeded max id";
		long time = tf.transformTimestamp(currentTimeStampSec);
		long shiftedTime = time << timestampShift;
		long shiftedId = currentId << idShift;
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
		if(hostId > maxHostId) {
			hostId = -1;
		}
		TimeTransformer tt;
		if(this.tf == TimeTransformer.adjustedEpoch) {
			tt = TimeTransformer.restoredEpoch;
		} else {
			tt = TimeTransformer.identity;
		}
		return new IdParts(tt.transformTimestamp(timestamp),hostId,currentId);
	}

	public static BinaryIdGenerator fromLastKnownId(long lastId, short timestampExtraBits, int hostId, int hostIdBits) {
		var tmpIdGenerator = new BinaryIdGenerator(unixTimestamp(),timestampExtraBits,0,hostIdBits);
		var parts = tmpIdGenerator.parse(lastId);
		var idGenerator = new BinaryIdGenerator(parts.timestamp(),timestampExtraBits, parts.hostId(), hostIdBits);
		idGenerator.currentId = parts.currentId();
		return idGenerator;
	}

	@Override
	public String toString() {
        return "BinaryIdGenerator{" +
				"hostId=" + (hostId < maxHostId ? hostId: "N/A") +
				", maxHostId=" + maxHostId +
				", maxId=" + maxId +
                ", maxIdsPerMSec=" + maxIdPerMSec +
                ", pastShiftSlowTimeAfter=" + sleepAfter +
                '}';
	}
}
