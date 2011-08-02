/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.reportgrid.api.json.gson;

/**
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
