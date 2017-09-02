package com.aegisql.id_builder;

public interface TimeTransformer {
	public long transformTimestamp( long currentTimeSec );
}
