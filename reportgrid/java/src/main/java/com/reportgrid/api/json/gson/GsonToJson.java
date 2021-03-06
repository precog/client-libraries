package com.reportgrid.api.json.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;
import com.reportgrid.api.json.ToJson;

/**
 * A simple ToJson instance that uses Gson default serialization.
 *
 * @author knuttycombe
 */
public class GsonToJson implements ToJson<Object> {
  public String serialize(Object value) {
    Gson gson = new GsonBuilder()
                  .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                  .registerTypeAdapter(RawJson.class, new RawJsonSerializer())
                  .create();

		return gson.toJson(value);
	}

  private static class RawJsonSerializer implements JsonSerializer<RawJson> {
    public JsonElement serialize(RawJson src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonParser().parse(src.getJson());
    }
  }
}
