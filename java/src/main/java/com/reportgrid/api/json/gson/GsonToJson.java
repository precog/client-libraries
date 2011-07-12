package com.reportgrid.api.json.gson;

import com.google.gson.Gson;
import com.reportgrid.api.json.ToJson;

/**
 * A simple ToJson instance that uses Gson default serialization.
 *
 * @author knuttycombe
 */
public class GsonToJson implements ToJson<Object> {
	public String serialize(Object value) {
		return new Gson().toJson(value);
	}
}
