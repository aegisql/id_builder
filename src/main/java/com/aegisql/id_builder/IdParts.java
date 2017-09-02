package com.aegisql.id_builder;

import java.util.Date;

public class IdParts {
	
	private long timestamp;
	private int  datacenterId;
	private int  hostId;
	private long currentId;
	
	public IdParts(long timestamp, int datacenterId, int hostId,	long currentId) {
		this.timestamp = timestamp;
		this.datacenterId = datacenterId;
		this.hostId = hostId;
		this.currentId = currentId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getDatacenterId() {
		return datacenterId;
	}

	public void setDatacenterId(int datacenterId) {
		this.datacenterId = datacenterId;
	}

	public int getHostId() {
		return hostId;
	}

	public void setHostId(int hostId) {
		this.hostId = hostId;
	}

	public long getCurrentId() {
		return currentId;
	}

	public void setCurrentId(long currentId) {
		this.currentId = currentId;
	}

	public Date getIdDateTime() {
		return new Date( timestamp * 1000 );
	}
	
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
