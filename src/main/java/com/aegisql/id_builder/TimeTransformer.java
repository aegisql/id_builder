package com.aegisql.id_builder;

/**
 * The Interface TimeTransformer.
 */
public interface TimeTransformer {

	/**
	 * Transform timestamp.
	 *
	 * @param currentTimeSec the current time sec
	 * @return the long
	 */
	long transformTimestamp( long currentTimeSec );

	/**
	 * The constant adjustingEpochTimestamp.
	 */
	long adjustingEpochTimestamp = 1718201409L;

	/**
	 * The constant identity.
	 */
	TimeTransformer identity = time->time;
	/**
	 * The constant adjustedEpoch.
	 */
	TimeTransformer adjustedEpoch = time->time-adjustingEpochTimestamp;
	/**
	 * The constant restoredEpoch.
	 */
	TimeTransformer restoredEpoch = time->time+adjustingEpochTimestamp;
}
