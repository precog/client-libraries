/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.reportgrid.api.json;

/**
 *
 * @author knuttycombe
 */
public interface ToJson<T> {
	public String serialize(T value);
}
