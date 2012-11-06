package com.precog.json.gson;

/**
 * A type wrapper for raw JSON strings to make it easier to embed such strings in 
 * an object being serialized with GSON. 
 *
 * @author knuttycombe
 */
public class RawJson {
	private final String json;

	public RawJson(String json) {
		this.json = json;
	}

	public String getJson() {
		return this.json;
	}
}
