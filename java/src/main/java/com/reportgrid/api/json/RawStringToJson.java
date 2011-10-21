package com.reportgrid.api.json;

/**
 *
 * @author knuttycombe
 */
public class RawStringToJson implements ToJson<String> {
	public String serialize(String value) {
    return value;
  }
}
