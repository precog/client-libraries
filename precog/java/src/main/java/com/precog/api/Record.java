package com.precog.api;

import com.precog.json.ToJson;

/**
 * A record to be stored in the precog database. 
 * 
 * @author knuttycombe
 * @param <T> The value for the record. It will be necessary to supply a ToJson instance for this type 
 * to the tracking api at the time that the record is sent so that the event data can be serialized to JSON.
 */
public class Record<T> {
  private final T data;

	/**
	 * Create a new record
	 * @param data An arbitrary value that may be serialized to JSON.
	 */
	public Record(T data) {
    this.data = data;
	}

	/**
	 * @return the eventData
	 */
	public T getData() {
		return data;
	}

  public String toJson(ToJson<? super T> serializer) {
    return serializer.serialize(getData());
  }
}
