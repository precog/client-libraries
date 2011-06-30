package com.reportgrid.api;

import java.util.Date;

/**
 * A simple immutable class to represent a range of dates. 
 *
 * @author knuttycombe
 */
public class DateRange {
	protected final Date start;
	protected final Date end;

	/**
	 * Create a new DateRange.
	 * @param start The start of the date range. May be null to represent an indefinite interval 
	 * @param end The end of the date range. May be null to represent an indefinite interval.
	 */
	public DateRange(Date start, Date end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Get the value of start
	 *
	 * @return the value of start
	 */
	public Date getStart() {
		return start;
	}

	/**
	 * Get the value of end
	 *
	 * @return the value of end
	 */
	public Date getEnd() {
		return end;
	}
}
