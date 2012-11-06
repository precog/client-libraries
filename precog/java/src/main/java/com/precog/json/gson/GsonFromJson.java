package com.precog.json.gson;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.precog.json.FromJson;
import java.lang.reflect.Type;

/**
 * FromJson instances that use the google GSON serialization library
 * to convert between JSON strings and Java objects.
 *
 * @author knuttycombe
 */
public class GsonFromJson {
	public static <T> FromJson<T> of(final TypeToken<T> typeToken) {
		final Type type = typeToken.getType(); 	
		return new FromJson<T>() {
			public T deserialize(String json) {
				return new Gson().fromJson(json, type);
			}
		};
	}	

	public static <T> FromJson<T> of(final Class<T> clazz) {
		return new FromJson<T>() {
			public T deserialize(String json) {
				return new Gson().fromJson(json, clazz);
			}
		};
	}	
}
