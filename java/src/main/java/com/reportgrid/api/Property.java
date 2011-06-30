package com.reportgrid.api;

/**
 * A simple typesafe wrapper for strings that forces them to conform to
 * the ReportGrid property path format.
 *
 * @author knuttycombe
 */
public class Property {
	private final String value;

	public Property(String value) {
		this.value = (value.startsWith(".") ? value : "." + value);
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Property other = (Property) obj;
		if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + (this.value != null ? this.value.hashCode() : 0);
		return hash;
	}
}
