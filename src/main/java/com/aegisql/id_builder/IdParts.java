package com.aegisql.id_builder;

import java.util.Date;

// TODO: Auto-generated Javadoc

/**
 * The type Id parts.
 */
public record IdParts(long timestamp, int hostId, long currentId) {

	/**
	 * Instantiates a new Id parts.
	 *
	 * @param timestamp    the timestamp
	 * @param hostId       the host id
	 * @param currentId    the current id
	 */
	public IdParts {
	}

	/**
	 * Gets id date time.
	 *
	 * @return the id date time
	 */
	public Date getIdDateTime() {
		return new Date(timestamp * 1000);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return timestamp +
				"." +
				(hostId >= 0 ? hostId:"") +
				"." +
				currentId;
	}

}
