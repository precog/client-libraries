package com.reportgrid.api;

import java.util.Date;

/**
 * A trackable event. 
 * 
 * @author knuttycombe
 * @param <T> The type of object used to represent properties for the event. It will be
 * necessary to supply a ToJson instance for this type to the tracking api at the time
 * that the event is sent so that the event data can be serialized to JSON.
 */
public class Event<T> {
	private final Date timestamp;
  private final String eventName;
  private final T eventData;
	private final int count;

	/**
	 * Create a new event instance. 
	 * @param timestamp The time at which the event occurs.
	 * @param eventName The name of the event. This should be an alphanumeric identifier.
	 * @param eventData An arbitrary object containing the properties of the event. 
	 * This object will be serialized to JSON before being sent to the server.
	 * @param count The number of times this event has occurred. This is useful if you
	 * want to batch occurrences of identical events. Negative values may be supplied to
	 * correct event counts within the time period associated with this event.
	 */
	public Event(Date timestamp, String eventName, T eventData, int count) {
		this.eventName = eventName;
		this.eventData = eventData;
		this.timestamp = timestamp;
		this.count = count;
	}

	/**
	 * A convenience constructor for Event for the common situation where
	 * @param timestamp
	 * @param eventName
	 * @param eventData 
	 */
	public Event(Date timestamp, String eventName, T eventData) {
		this(timestamp, eventName, eventData, 1);
	}

	/**
	 * @return the eventName
	 */
	public String getEventName() {
		return eventName;
	}

	/**
	 * @return the eventData
	 */
	public T getEventData() {
		return eventData;
	}

	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}
}
