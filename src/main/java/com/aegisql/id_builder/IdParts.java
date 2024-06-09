package com.aegisql.id_builder;

import java.util.Date;

// TODO: Auto-generated Javadoc

/**
 * The type Id parts.
 */
public record IdParts(long timestamp, int hostId, long currentId) {
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
