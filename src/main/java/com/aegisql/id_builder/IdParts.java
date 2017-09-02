package com.aegisql.id_builder;

import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class IdParts.
 */
public class IdParts {
	
	/** The timestamp. */
	private long timestamp;
	
	/** The datacenter id. */
	private int  datacenterId;
	
	/** The host id. */
	private int  hostId;
	
	/** The current id. */
	private long currentId;
	
	/**
	 * Instantiates a new id parts.
	 *
	 * @param timestamp the timestamp
	 * @param datacenterId the datacenter id
	 * @param hostId the host id
	 * @param currentId the current id
	 */
	public IdParts(long timestamp, int datacenterId, int hostId,	long currentId) {
		this.timestamp = timestamp;
		this.datacenterId = datacenterId;
		this.hostId = hostId;
		this.currentId = currentId;
	}

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp the new timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the datacenter id.
	 *
	 * @return the datacenter id
	 */
	public int getDatacenterId() {
		return datacenterId;
	}

	/**
	 * Sets the datacenter id.
	 *
	 * @param datacenterId the new datacenter id
	 */
	public void setDatacenterId(int datacenterId) {
		this.datacenterId = datacenterId;
	}

	/**
	 * Gets the host id.
	 *
	 * @return the host id
	 */
	public int getHostId() {
		return hostId;
	}

	/**
	 * Sets the host id.
	 *
	 * @param hostId the new host id
	 */
	public void setHostId(int hostId) {
		this.hostId = hostId;
	}

	/**
	 * Gets the current id.
	 *
	 * @return the current id
	 */
	public long getCurrentId() {
		return currentId;
	}

	/**
	 * Sets the current id.
	 *
	 * @param currentId the new current id
	 */
	public void setCurrentId(long currentId) {
		this.currentId = currentId;
	}

	/**
	 * Gets the id date time.
	 *
	 * @return the id date time
	 */
	public Date getIdDateTime() {
		return new Date( timestamp * 1000 );
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (currentId ^ (currentId >>> 32));
		result = prime * result + datacenterId;
		result = prime * result + hostId;
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
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
		if (timestamp != other.timestamp)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(timestamp);
		builder.append(".");
		builder.append(datacenterId);
		builder.append(".");
		builder.append(hostId);
		builder.append(".");
		builder.append(currentId);
		return builder.toString();
	}
	
	
	
	
}
