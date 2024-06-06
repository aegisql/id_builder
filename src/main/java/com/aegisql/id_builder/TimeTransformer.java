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

	long adjustingEpochTimestamp = 1717642585L;

	TimeTransformer identity = time->time;
	TimeTransformer adjustedEpoch = time->time-adjustingEpochTimestamp;
	TimeTransformer restoredEpoch = time->time+adjustingEpochTimestamp;
}
