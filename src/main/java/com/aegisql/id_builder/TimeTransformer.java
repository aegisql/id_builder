package com.aegisql.id_builder;

// TODO: Auto-generated Javadoc
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

	TimeTransformer identity = time->time;
}
