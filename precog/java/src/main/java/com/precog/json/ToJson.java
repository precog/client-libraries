package com.precog.json;

/**
 * A simple strategy interface that can be implemented to provide JSON serialization
 * for arbitrary value types.
 *
 * @author knuttycombe
 */
public interface ToJson<T> {
	public String serialize(T value);
}
