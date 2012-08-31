package io.precog.json;

/**
 * A simple strategy interface that can be implemented to provide deserialization
 * from a JSON string to an arbitrary value type.
 *
 * @author knuttycombe
 */
public interface FromJson<T> {
	public T deserialize(String json);	
}
