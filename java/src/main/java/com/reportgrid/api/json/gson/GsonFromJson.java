/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.reportgrid.api.json.gson;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.reportgrid.api.json.FromJson;
import java.lang.reflect.Type;

/**
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
