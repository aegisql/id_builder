package com.aegisql.id_builder;

// TODO: Auto-generated Javadoc

import java.util.stream.Stream;

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
	Stream<Long> asStream();
}
