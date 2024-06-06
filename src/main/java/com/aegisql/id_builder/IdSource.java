package com.aegisql.id_builder;

import java.util.stream.LongStream;

/**
 * The Interface IdSource.
 */
public interface IdSource {

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	long getId();

	/**
	 * As stream long stream.
	 *
	 * @return the long stream
	 */
	default LongStream asStream() {
		return LongStream.generate(this::getId);
	}
}
