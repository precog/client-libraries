package com.reportgrid.api.json;

/**
 *
 * @author knuttycombe
 */
public interface FromJson<T> {
	public T deserialize(String json);	
}
