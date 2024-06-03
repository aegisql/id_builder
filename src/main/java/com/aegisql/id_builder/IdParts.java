package com.aegisql.id_builder;

import java.util.Date;

// TODO: Auto-generated Javadoc

/**
 * The type Id parts.
 */
public record IdParts(long timestamp, int datacenterId, int hostId, long currentId) {

	/**
	 * Instantiates a new Id parts.
	 *
	 * @param timestamp    the timestamp
	 * @param datacenterId the datacenter id
	 * @param hostId       the host id
	 * @param currentId    the current id
	 */
	public IdParts {
	}

	/**
	 * Timestamp long.
	 *
	 * @return the long
	 */
	@Override
	public long timestamp() {
		return timestamp;
	}

	/**
	 * Datacenter id int.
	 *
	 * @return the int
	 */
	@Override
	public int datacenterId() {
		return datacenterId;
	}

	/**
	 * Host id int.
	 *
	 * @return the int
	 */
	@Override
	public int hostId() {
		return hostId;
	}

	/**
	 * Current id long.
	 *
	 * @return the long
	 */
	@Override
	public long currentId() {
		return currentId;
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Long.hashCode(currentId);
		result = prime * result + datacenterId;
		result = prime * result + hostId;
		result = prime * result + Long.hashCode(timestamp);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdParts other = (IdParts) obj;
		if (currentId != other.currentId)
			return false;
		if (datacenterId != other.datacenterId)
			return false;
		if (hostId != other.hostId)
			return false;
		return timestamp == other.timestamp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return timestamp +
				"." +
				datacenterId +
				"." +
				hostId +
				"." +
				currentId;
	}

	/**
	 * Split 10 x 4 x 5 id parts.
	 *
	 * @param id the id
	 * @return the id parts
	 */
	public static IdParts split_10x4x5(final long id) {

		long timestamp;
		long datacenterId;
		long hostId;
		long currentId;

		timestamp = id / 1000000000;
		long dcHostId = id - timestamp * 1000000000;
		long dcHost = dcHostId / 100000;
		currentId = dcHostId - dcHost * 100000;
		datacenterId = dcHost / 1000;
		hostId = dcHost - datacenterId * 1000;
		return new IdParts(timestamp, (int) datacenterId, (int) hostId, currentId);
	}

	/**
	 * Split 10 x 8 id parts.
	 *
	 * @param id the id
	 * @return the id parts
	 */
	public static IdParts split_10x8(final long id) {
		long timestamp;
		long currentId;
		timestamp = id / 1000000000L;
		currentId = id - timestamp * 1000000000L;
		return new IdParts(timestamp, 0, 0, currentId);
	}


}
